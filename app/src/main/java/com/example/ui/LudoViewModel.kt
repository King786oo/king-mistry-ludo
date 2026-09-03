package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.SoundManager
import com.example.model.BoardCoordinates
import com.example.model.GameMode
import com.example.model.GamePhase
import com.example.model.GameState
import com.example.model.LudoColor
import com.example.model.LudoToken
import com.example.model.Player
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class GameStats(
    val totalGamesPlayed: Int = 0,
    val totalWins: Int = 0,
    val totalCaptures: Int = 0,
    val redWins: Int = 0,
    val greenWins: Int = 0,
    val yellowWins: Int = 0,
    val blueWins: Int = 0
)

class LudoViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("ludo_game_prefs", Context.MODE_PRIVATE)
    val soundManager = SoundManager()

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _stats = MutableStateFlow(loadStats())
    val stats: StateFlow<GameStats> = _stats.asStateFlow()

    private var botJob: Job? = null
    private var rollAnimationJob: Job? = null

    init {
        // Initialize with default 2-player game (You as Blue vs Bot as Green)
        startNewGame(
            mode = GameMode.VS_BOT,
            playerCount = 2,
            playerNames = listOf("You", "Bot"),
            botFlags = listOf(false, true)
        )
    }

    private fun loadStats(): GameStats {
        return GameStats(
            totalGamesPlayed = prefs.getInt("stat_games", 0),
            totalWins = prefs.getInt("stat_wins", 0),
            totalCaptures = prefs.getInt("stat_captures", 0),
            redWins = prefs.getInt("stat_red_wins", 0),
            greenWins = prefs.getInt("stat_green_wins", 0),
            yellowWins = prefs.getInt("stat_yellow_wins", 0),
            blueWins = prefs.getInt("stat_blue_wins", 0)
        )
    }

    private fun saveStats(updated: GameStats) {
        _stats.value = updated
        prefs.edit().apply {
            putInt("stat_games", updated.totalGamesPlayed)
            putInt("stat_wins", updated.totalWins)
            putInt("stat_captures", updated.totalCaptures)
            putInt("stat_red_wins", updated.redWins)
            putInt("stat_green_wins", updated.greenWins)
            putInt("stat_yellow_wins", updated.yellowWins)
            putInt("stat_blue_wins", updated.blueWins)
            apply()
        }
    }

    fun toggleLanguage() {
        _gameState.update { it.copy(isBengali = !it.isBengali) }
    }

    fun toggleSound() {
        val newSound = !_gameState.value.soundEnabled
        soundManager.isSoundEnabled = newSound
        _gameState.update { it.copy(soundEnabled = newSound) }
    }

    fun toggleSpeed() {
        _gameState.update { it.copy(fastSpeed = !it.fastSpeed) }
    }

    fun startNewGame(
        mode: GameMode,
        playerCount: Int,
        playerNames: List<String>,
        botFlags: List<Boolean>
    ) {
        botJob?.cancel()
        rollAnimationJob?.cancel()

        val selectedColors = when (playerCount) {
            2 -> listOf(LudoColor.BLUE, LudoColor.GREEN)
            3 -> listOf(LudoColor.BLUE, LudoColor.RED, LudoColor.GREEN)
            else -> listOf(LudoColor.BLUE, LudoColor.RED, LudoColor.GREEN, LudoColor.YELLOW)
        }

        val newPlayers = selectedColors.mapIndexed { index, color ->
            Player(
                id = index,
                name = playerNames.getOrElse(index) {
                    if (botFlags.getOrElse(index) { false }) "Bot ${color.titleEn}" else "Player ${index + 1}"
                },
                color = color,
                isBot = botFlags.getOrElse(index) { false }
            )
        }

        val initialEn = "${newPlayers[0].name}'s Turn - Roll the dice!"
        val initialBn = "${newPlayers[0].name} এর চাল - ছক্কা চালুন!"

        _gameState.value = GameState(
            mode = mode,
            players = newPlayers,
            currentTurnIndex = 0,
            diceValue = 6,
            isRolling = false,
            phase = GamePhase.ROLL_DICE,
            consecutiveSixes = 0,
            winnerRanking = emptyList(),
            statusMessageEn = initialEn,
            statusMessageBn = initialBn,
            isBengali = _gameState.value.isBengali,
            soundEnabled = _gameState.value.soundEnabled,
            fastSpeed = _gameState.value.fastSpeed
        )

        // Increment total games
        saveStats(_stats.value.copy(totalGamesPlayed = _stats.value.totalGamesPlayed + 1))

        // If first player is Bot, trigger bot roll
        if (newPlayers[0].isBot) {
            triggerBotTurn()
        }
    }

    fun rollDice() {
        val state = _gameState.value
        if (state.phase != GamePhase.ROLL_DICE || state.isRolling) return

        val player = state.currentPlayer ?: return

        soundManager.playDiceRoll()

        rollAnimationJob = viewModelScope.launch {
            _gameState.update { it.copy(isRolling = true) }

            // Dice roll animation (rapid random dice faces)
            val rollTicks = if (state.fastSpeed) 4 else 8
            val tickDelay = if (state.fastSpeed) 40L else 50L
            repeat(rollTicks) {
                val tempDice = Random.nextInt(1, 7)
                _gameState.update { it.copy(diceValue = tempDice) }
                delay(tickDelay)
            }

            val finalRoll = Random.nextInt(1, 7)
            val isSix = finalRoll == 6
            val newSixes = if (isSix) state.consecutiveSixes + 1 else 0

            if (isSix) {
                soundManager.playSixRolled()
            }

            // Check 3 consecutive sixes penalty
            if (newSixes == 3) {
                val msgEn = "${player.name} rolled 3 Sixes in a row! Turn skipped."
                val msgBn = "${player.name} পরপর ৩ বার ৬ পেয়েছেন! চাল বাতিল হলো।"
                _gameState.update {
                    it.copy(
                        diceValue = finalRoll,
                        isRolling = false,
                        consecutiveSixes = 0,
                        statusMessageEn = msgEn,
                        statusMessageBn = msgBn
                    )
                }
                delay(if (state.fastSpeed) 600L else 1200L)
                nextTurn()
                return@launch
            }

            // Determine valid movable tokens
            val validTokens = getMovableTokens(player, finalRoll)

            if (validTokens.isEmpty()) {
                val msgEn = "${player.name} rolled $finalRoll. No valid moves."
                val msgBn = "${player.name} $finalRoll পেয়েছেন। কোনো চাল নেই।"
                _gameState.update {
                    it.copy(
                        diceValue = finalRoll,
                        isRolling = false,
                        consecutiveSixes = newSixes,
                        statusMessageEn = msgEn,
                        statusMessageBn = msgBn
                    )
                }
                delay(if (state.fastSpeed) 400L else 800L)
                if (isSix) {
                    // Rolled 6 gets another chance even if no tokens can move, or pass?
                    // Standard rules: If 6 is rolled and no moves possible (e.g. blocked near goal), bonus roll still applies
                    _gameState.update {
                        it.copy(
                            phase = GamePhase.ROLL_DICE,
                            statusMessageEn = "${player.name} rolled 6! Roll again.",
                            statusMessageBn = "${player.name} ৬ পেয়েছেন! আবার চালুন।"
                        )
                    }
                    if (player.isBot) {
                        triggerBotTurn()
                    }
                } else {
                    nextTurn()
                }
            } else {
                // Highlight movable tokens
                val updatedPlayers = state.players.map { p ->
                    if (p.id == player.id) {
                        p.copy(tokens = p.tokens.map { token ->
                            token.copy(isHighlighted = validTokens.any { it.id == token.id })
                        })
                    } else p
                }

                val msgEn = if (isSix) "${player.name} rolled 6! Pick a token to move." else "${player.name} rolled $finalRoll! Select a token."
                val msgBn = if (isSix) "${player.name} ৬ পেয়েছেন! গুটি নির্বাচন করুন।" else "${player.name} $finalRoll পেয়েছেন! গুটি নির্বাচন করুন।"

                _gameState.update {
                    it.copy(
                        players = updatedPlayers,
                        diceValue = finalRoll,
                        isRolling = false,
                        consecutiveSixes = newSixes,
                        phase = GamePhase.SELECT_TOKEN,
                        statusMessageEn = msgEn,
                        statusMessageBn = msgBn
                    )
                }

                if (player.isBot) {
                    val chosenToken = selectBotMove(player, validTokens, finalRoll)
                    delay(if (state.fastSpeed) 300L else 600L)
                    moveToken(player.id, chosenToken.id)
                } else if (validTokens.size == 1) {
                    // If only 1 valid token and auto-move or quick single choice
                    delay(if (state.fastSpeed) 250L else 400L)
                    moveToken(player.id, validTokens.first().id)
                }
            }
        }
    }

    private fun getMovableTokens(player: Player, roll: Int): List<LudoToken> {
        return player.tokens.filter { token ->
            if (token.isFinished) false
            else if (token.isInYard) roll == 6
            else token.step + roll <= BoardCoordinates.MAX_STEPS
        }
    }

    fun onTokenClicked(playerId: Int, tokenId: Int) {
        val state = _gameState.value
        if (state.phase != GamePhase.SELECT_TOKEN) return
        val player = state.currentPlayer ?: return
        if (player.id != playerId || player.isBot) return

        val token = player.tokens.find { it.id == tokenId } ?: return
        if (!token.isHighlighted) return

        moveToken(playerId, tokenId)
    }

    private fun moveToken(playerId: Int, tokenId: Int) {
        val state = _gameState.value
        val player = state.players.find { it.id == playerId } ?: return
        val token = player.tokens.find { it.id == tokenId } ?: return
        val roll = state.diceValue

        viewModelScope.launch {
            // Un-highlight tokens
            _gameState.update { s ->
                s.copy(
                    phase = GamePhase.TOKEN_MOVING,
                    players = s.players.map { p ->
                        if (p.id == playerId) {
                            p.copy(tokens = p.tokens.map { it.copy(isHighlighted = false) })
                        } else p
                    }
                )
            }

            val startStep = token.step
            val targetStep = if (token.isInYard) 0 else token.step + roll

            // Step-by-step token movement
            val stepDelay = if (state.fastSpeed) 60L else 120L
            if (token.isInYard) {
                soundManager.playTokenMove()
                updateSingleTokenStep(playerId, tokenId, 0)
                delay(stepDelay * 2)
            } else {
                for (s in (startStep + 1)..targetStep) {
                    soundManager.playTokenMove()
                    updateSingleTokenStep(playerId, tokenId, s)
                    delay(stepDelay)
                }
            }

            // Movement completed! Check outcomes:
            val landedInGoal = targetStep == BoardCoordinates.STEP_GOAL
            if (landedInGoal) {
                soundManager.playTokenEnterGoal()
            }

            // Check if captured any opponent token
            var capturedAny = false
            if (targetStep in 0..50) {
                val landingTrackIndex = (player.color.startTrackIndex + targetStep) % 52
                val isSafe = BoardCoordinates.SAFE_TRACK_INDICES.contains(landingTrackIndex)

                if (!isSafe) {
                    val updatedOtherPlayers = _gameState.value.players.map { otherPlayer ->
                        if (otherPlayer.id != playerId) {
                            val capturedTokens = otherPlayer.tokens.map { opponentToken ->
                                if (opponentToken.isOnBoard && opponentToken.step in 0..50) {
                                    val opponentTrack = (otherPlayer.color.startTrackIndex + opponentToken.step) % 52
                                    if (opponentTrack == landingTrackIndex) {
                                        capturedAny = true
                                        opponentToken.copy(step = BoardCoordinates.STEP_YARD)
                                    } else opponentToken
                                } else opponentToken
                            }
                            otherPlayer.copy(tokens = capturedTokens)
                        } else otherPlayer
                    }

                    if (capturedAny) {
                        soundManager.playTokenCapture()
                        saveStats(_stats.value.copy(totalCaptures = _stats.value.totalCaptures + 1))
                        _gameState.update { it.copy(players = updatedOtherPlayers) }
                    }
                }
            }

            // Check if player just completed all tokens!
            val updatedCurrentPlayer = _gameState.value.players.find { it.id == playerId }!!
            val hasJustWon = updatedCurrentPlayer.isFinished && !_gameState.value.winnerRanking.contains(updatedCurrentPlayer)

            var newRanking = _gameState.value.winnerRanking
            if (hasJustWon) {
                newRanking = newRanking + updatedCurrentPlayer.copy(rank = newRanking.size + 1)
                _gameState.update { s ->
                    s.copy(
                        winnerRanking = newRanking,
                        players = s.players.map { p -> if (p.id == playerId) p.copy(rank = newRanking.size) else p }
                    )
                }

                // Update wins stats
                val statsUpdate = when (updatedCurrentPlayer.color) {
                    LudoColor.RED -> _stats.value.copy(redWins = _stats.value.redWins + 1, totalWins = _stats.value.totalWins + 1)
                    LudoColor.GREEN -> _stats.value.copy(greenWins = _stats.value.greenWins + 1)
                    LudoColor.YELLOW -> _stats.value.copy(yellowWins = _stats.value.yellowWins + 1)
                    LudoColor.BLUE -> _stats.value.copy(blueWins = _stats.value.blueWins + 1)
                }
                saveStats(statsUpdate)

                if (newRanking.size == 1) {
                    soundManager.playVictoryFanfare()
                }
            }

            // Check if whole game is finished
            val activeRemainingPlayers = _gameState.value.players.filter { !it.isFinished }
            if (activeRemainingPlayers.size <= 1) {
                // Game Over! Add last remaining player if any
                val finalRanking = if (activeRemainingPlayers.isNotEmpty()) {
                    newRanking + activeRemainingPlayers.first().copy(rank = newRanking.size + 1)
                } else newRanking

                val firstWinner = finalRanking.firstOrNull()?.name ?: "Player"
                val winMsgEn = "Game Over! $firstWinner is the Winner!"
                val winMsgBn = "খেলা সমাপ্ত! $firstWinner বিজয়ী হয়েছেন!"

                _gameState.update {
                    it.copy(
                        phase = GamePhase.GAME_OVER,
                        winnerRanking = finalRanking,
                        statusMessageEn = winMsgEn,
                        statusMessageBn = winMsgBn
                    )
                }
                return@launch
            }

            // Determine if bonus roll applies:
            val rolledSix = roll == 6
            val grantBonusRoll = (rolledSix || capturedAny || landedInGoal) && !hasJustWon

            if (grantBonusRoll) {
                val bonusReasonEn = when {
                    capturedAny -> "Captured opponent! Bonus Roll awarded!"
                    landedInGoal -> "Reached Home! Bonus Roll awarded!"
                    else -> "Rolled 6! Roll again."
                }
                val bonusReasonBn = when {
                    capturedAny -> "গুটি কেটেছেন! বোনাস চাল পেয়েছেন!"
                    landedInGoal -> "ঘরে গুটি পৌঁছেছে! বোনাস চাল!"
                    else -> "৬ পেয়েছেন! আবার চালুন।"
                }

                _gameState.update {
                    it.copy(
                        phase = GamePhase.ROLL_DICE,
                        statusMessageEn = "${player.name}: $bonusReasonEn",
                        statusMessageBn = "${player.name}: $bonusReasonBn"
                    )
                }

                if (player.isBot) {
                    triggerBotTurn()
                }
            } else {
                nextTurn()
            }
        }
    }

    private fun updateSingleTokenStep(playerId: Int, tokenId: Int, newStep: Int) {
        _gameState.update { state ->
            state.copy(
                players = state.players.map { p ->
                    if (p.id == playerId) {
                        p.copy(tokens = p.tokens.map { t ->
                            if (t.id == tokenId) t.copy(step = newStep) else t
                        })
                    } else p
                }
            )
        }
    }

    private fun nextTurn() {
        val state = _gameState.value
        val numPlayers = state.players.size
        var nextIndex = (state.currentTurnIndex + 1) % numPlayers

        // Skip players who have already finished all 4 tokens
        var attempts = 0
        while (state.players[nextIndex].isFinished && attempts < numPlayers) {
            nextIndex = (nextIndex + 1) % numPlayers
            attempts++
        }

        val nextPlayer = state.players[nextIndex]
        val turnEn = "${nextPlayer.name}'s Turn - Roll the dice!"
        val turnBn = "${nextPlayer.name} এর চাল - ছক্কা চালুন!"

        _gameState.update {
            it.copy(
                currentTurnIndex = nextIndex,
                phase = GamePhase.ROLL_DICE,
                consecutiveSixes = 0,
                statusMessageEn = turnEn,
                statusMessageBn = turnBn
            )
        }

        if (nextPlayer.isBot) {
            triggerBotTurn()
        }
    }

    private fun triggerBotTurn() {
        botJob?.cancel()
        botJob = viewModelScope.launch {
            val waitDelay = if (_gameState.value.fastSpeed) 350L else 700L
            delay(waitDelay)
            rollDice()
        }
    }

    /**
     * Smart Bot AI evaluation function
     */
    private fun selectBotMove(botPlayer: Player, validTokens: List<LudoToken>, roll: Int): LudoToken {
        if (validTokens.size == 1) return validTokens.first()

        val otherPlayers = _gameState.value.players.filter { it.id != botPlayer.id }

        var bestToken = validTokens.first()
        var highestScore = -9999

        for (token in validTokens) {
            var score = 0
            val currentStep = token.step
            val targetStep = if (token.isInYard) 0 else currentStep + roll

            // 1. Can enter Goal
            if (targetStep == BoardCoordinates.STEP_GOAL) {
                score += 500
            }

            // 2. Can move out from Yard
            if (token.isInYard && roll == 6) {
                score += 350
            }

            // 3. Can capture opponent
            if (targetStep in 0..50) {
                val targetTrackIndex = (botPlayer.color.startTrackIndex + targetStep) % 52
                val isSafe = BoardCoordinates.SAFE_TRACK_INDICES.contains(targetTrackIndex)

                if (!isSafe) {
                    val canCapture = otherPlayers.any { other ->
                        other.tokens.any { oppToken ->
                            oppToken.isOnBoard && oppToken.step in 0..50 &&
                                    (other.color.startTrackIndex + oppToken.step) % 52 == targetTrackIndex
                        }
                    }
                    if (canCapture) {
                        score += 600
                    }
                }

                // 4. Land on safe spot
                if (isSafe) {
                    score += 150
                }
            }

            // 5. Enter home column
            if (targetStep >= 51 && currentStep < 51) {
                score += 250
            }

            // 6. Escape opponent threat if currently in danger on non-safe square
            if (currentStep in 0..50) {
                val currentTrackIndex = (botPlayer.color.startTrackIndex + currentStep) % 52
                val currentlyInDanger = !BoardCoordinates.SAFE_TRACK_INDICES.contains(currentTrackIndex) &&
                        otherPlayers.any { other ->
                            other.tokens.any { oppToken ->
                                if (oppToken.isOnBoard && oppToken.step in 0..50) {
                                    val oppTrack = (other.color.startTrackIndex + oppToken.step) % 52
                                    val dist = (currentTrackIndex - oppTrack + 52) % 52
                                    dist in 1..6
                                } else false
                            }
                        }
                if (currentlyInDanger) {
                    score += 200
                }
            }

            // 7. General progression bonus
            score += targetStep * 2

            if (score > highestScore) {
                highestScore = score
                bestToken = token
            }
        }

        return bestToken
    }
}
