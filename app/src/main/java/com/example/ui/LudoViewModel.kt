package com.example.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.CoinManager
import com.example.engine.SnakesAndLaddersEngine
import com.example.engine.SoundManager
import com.example.model.AppLanguage
import com.example.model.BoardCoordinates
import com.example.model.DiceSkin
import com.example.model.GameMode
import com.example.model.GamePhase
import com.example.model.GameState
import com.example.model.LudoColor
import com.example.model.LudoPremiumToken
import com.example.model.LudoToken
import com.example.model.Player
import com.example.model.SnakeOrLadder
import com.example.model.SnakesAndLaddersPatternPool
import com.example.model.SnakesAndLaddersPlayer
import com.example.model.SnakesAndLaddersState
import com.example.multiplayer.OnlineMultiplayerManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.security.SecureRandom
import kotlin.random.Random

data class GameStats(
    val totalGamesPlayed: Int = 0,
    val totalWins: Int = 0,
    val totalCaptures: Int = 0,
    val redWins: Int = 0,
    val greenWins: Int = 0,
    val yellowWins: Int = 0,
    val blueWins: Int = 0,
    val teamAWins: Int = 0,
    val teamBWins: Int = 0
)

class LudoViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("ludo_game_prefs", Context.MODE_PRIVATE)
    private val legacyPrefs = application.getSharedPreferences("king_mistry_ludo_prefs", Context.MODE_PRIVATE)
    val soundManager = SoundManager(application)
    private val secureRandom = SecureRandom()

    // Fair dice tracking: ensures no player/house is starved of a 6 when stuck in the yard
    private val turnsWithoutSix = mutableMapOf<Int, Int>()

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _stats = MutableStateFlow(loadStats())
    val stats: StateFlow<GameStats> = _stats.asStateFlow()

    private var botJob: Job? = null
    private var botMoveJob: Job? = null
    private var rollAnimationJob: Job? = null
    private var moveJob: Job? = null
    private var turnWatchdogJob: Job? = null

    // High-concurrency atomic locks for zero race conditions and null-safety during rapid tapping
    private val isRollingAtomic = java.util.concurrent.atomic.AtomicBoolean(false)
    private val isMovingAtomic = java.util.concurrent.atomic.AtomicBoolean(false)

    init {
        CoinManager.initialize(application)
        val savedLangCode = prefs.getString("app_language", null)
        val initialLang = if (savedLangCode != null) AppLanguage.fromCode(savedLangCode) else AppLanguage.ENGLISH
        val savedTokenId = prefs.getString("selected_premium_token", null)
            ?: legacyPrefs.getString("selected_premium_token", LudoPremiumToken.PAWN_CRYSTAL_GEM.id)
        val initialToken = LudoPremiumToken.fromId(savedTokenId)
        val savedDiceSkin = prefs.getString("selected_dice_skin", null)
            ?: legacyPrefs.getString("selected_dice_skin", DiceSkin.ROYAL_GOLD_KING.id)
        val initialDiceSkin = DiceSkin.fromId(savedDiceSkin)
        val initialSoundEnabled = prefs.getBoolean("sound_enabled", true)
        val initialFastSpeed = prefs.getBoolean("fast_speed", false)
        soundManager.isSoundEnabled = initialSoundEnabled
        _gameState.update {
            it.copy(
                language = initialLang,
                isBengali = (initialLang == AppLanguage.BENGALI),
                soundEnabled = initialSoundEnabled,
                fastSpeed = initialFastSpeed,
                equippedToken = initialToken,
                equippedDiceSkin = initialDiceSkin,
                colorPawnRoster = LudoPremiumToken.createAllColorPawnRoster(initialToken)
            )
        }

        // Initialize with default match: Human User "Player 1" vs Computer "Computer (AI)"
        val defaultHumanName = "Player 1"
        val defaultBotName = if (initialLang == AppLanguage.BENGALI) "কম্পিউটার (AI)" else "Computer (AI)"
        startNewGame(
            mode = GameMode.VS_BOT,
            playerCount = 2,
            playerNames = listOf(defaultHumanName, defaultBotName),
            botFlags = listOf(false, true)
        )
    }

    fun setEquippedDiceSkin(skin: DiceSkin) {
        _gameState.update { it.copy(equippedDiceSkin = skin) }
        prefs.edit().putString("selected_dice_skin", skin.id).apply()
        legacyPrefs.edit().putString("selected_dice_skin", skin.id).apply()
    }

    fun setEquippedToken(token: LudoPremiumToken) {
        _gameState.update { state ->
            val updatedPlayers = state.players.map { player ->
                val filledTokens = if (player.tokens.size == 4) {
                    player.tokens.map { t -> t.copy(styleId = token.id) }
                } else {
                    LudoPremiumToken.createFourPawnsForColor(player.color, token)
                }
                player.copy(tokens = filledTokens)
            }
            val updatedRoster = LudoColor.entries.associateWith { color ->
                val activeTokens = updatedPlayers.firstOrNull { it.color == color }?.tokens
                if (activeTokens != null && activeTokens.size == 4) {
                    activeTokens
                } else {
                    LudoPremiumToken.createFourPawnsForColor(color, token)
                }
            }
            state.copy(
                equippedToken = token,
                players = updatedPlayers,
                colorPawnRoster = updatedRoster
            )
        }
        prefs.edit().putString("selected_premium_token", token.id).apply()
        legacyPrefs.edit().putString("selected_premium_token", token.id).apply()
    }

    private fun loadStats(): GameStats {
        return GameStats(
            totalGamesPlayed = prefs.getInt("stat_games", 0),
            totalWins = prefs.getInt("stat_wins", 0),
            totalCaptures = prefs.getInt("stat_captures", 0),
            redWins = prefs.getInt("stat_red_wins", 0),
            greenWins = prefs.getInt("stat_green_wins", 0),
            yellowWins = prefs.getInt("stat_yellow_wins", 0),
            blueWins = prefs.getInt("stat_blue_wins", 0),
            teamAWins = prefs.getInt("stat_team_a_wins", 0),
            teamBWins = prefs.getInt("stat_team_b_wins", 0)
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
            putInt("stat_team_a_wins", updated.teamAWins)
            putInt("stat_team_b_wins", updated.teamBWins)
            apply()
        }
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString("app_language", language.code).apply()
        _gameState.update {
            it.copy(
                language = language,
                isBengali = (language == AppLanguage.BENGALI)
            )
        }
    }

    fun toggleLanguage() {
        val next = when (_gameState.value.language) {
            AppLanguage.ENGLISH -> AppLanguage.BENGALI
            AppLanguage.BENGALI -> AppLanguage.HINDI
            AppLanguage.HINDI -> AppLanguage.ENGLISH
        }
        setLanguage(next)
    }

    fun toggleSound() {
        val newSound = !_gameState.value.soundEnabled
        soundManager.isSoundEnabled = newSound
        prefs.edit().putBoolean("sound_enabled", newSound).apply()
        _gameState.update { it.copy(soundEnabled = newSound) }
        if (newSound) {
            soundManager.playButtonClick()
        }
    }

    fun toggleSpeed() {
        val newSpeed = !_gameState.value.fastSpeed
        prefs.edit().putBoolean("fast_speed", newSpeed).apply()
        _gameState.update { it.copy(fastSpeed = newSpeed) }
    }

    fun cancelActiveJobs() {
        botJob?.cancel()
        botMoveJob?.cancel()
        rollAnimationJob?.cancel()
        moveJob?.cancel()
        turnWatchdogJob?.cancel()
        isRollingAtomic.set(false)
        isMovingAtomic.set(false)
    }

    fun getLocalPlayerId(state: GameState = _gameState.value): Int {
        return if (state.mode == GameMode.ONLINE_FRIENDS && state.onlineRoomCode != null) {
            state.onlineLocalPlayerId
        } else {
            state.players.firstOrNull { !it.isBot }?.id ?: 0
        }
    }

    private fun syncOnlineTokenMoveIfNeeded(
        state: GameState,
        tokenOwnerId: Int,
        tokenId: Int,
        targetStep: Int
    ) {
        if (state.mode == GameMode.ONLINE_FRIENDS && state.onlineRoomCode != null && tokenOwnerId == state.onlineLocalPlayerId) {
            val latest = _gameState.value
            val matrix = latest.players.associate { it.id to it.tokens.map { t -> t.step } }
            OnlineMultiplayerManager.syncTokenMove(
                roomCode = state.onlineRoomCode,
                playerId = tokenOwnerId,
                tokenId = tokenId,
                targetStep = targetStep,
                nextTurnIndex = latest.currentTurnIndex,
                tokensMatrix = matrix
            )
            if (latest.phase != GamePhase.GAME_OVER) {
                startTurnWatchdog()
            } else {
                turnWatchdogJob?.cancel()
            }
        }
    }

    fun startNewGame(
        mode: GameMode,
        playerCount: Int,
        playerNames: List<String>,
        botFlags: List<Boolean>,
        tournamentNetPrize: Int? = null,
        tournamentName: String? = null,
        targetTokensToWin: Int = 4,
        tournamentTierId: String? = null,
        entryFeePerPlayer: Int = 0
    ) {
        botJob?.cancel()
        botMoveJob?.cancel()
        rollAnimationJob?.cancel()
        moveJob?.cancel()
        isRollingAtomic.set(false)
        isMovingAtomic.set(false)
        turnsWithoutSix.clear()

        val actualCount = if (mode == GameMode.TEAM_2V2) 4 else playerCount

        val selectedColors = when {
            mode == GameMode.TEAM_2V2 -> listOf(
                LudoColor.RED,    // Player 1 (Team A)
                LudoColor.GREEN,  // Player 2 (Team B)
                LudoColor.YELLOW, // Player 3 (Team A)
                LudoColor.BLUE    // Player 4 (Team B)
            )
            actualCount == 2 -> listOf(LudoColor.BLUE, LudoColor.GREEN)
            actualCount == 3 -> listOf(LudoColor.BLUE, LudoColor.RED, LudoColor.GREEN)
            else -> listOf(LudoColor.BLUE, LudoColor.RED, LudoColor.GREEN, LudoColor.YELLOW)
        }

        val newPlayers = selectedColors.mapIndexed { index, color ->
            val customName = playerNames.getOrNull(index)?.takeIf { it.isNotBlank() }
            val isBot = botFlags.getOrElse(index) { false }
            val defaultName = when {
                index == 0 -> "Player 1"
                customName != null -> customName
                isBot -> if (_gameState.value.isBengali) "কম্পিউটার (AI)" else "Computer (AI)"
                mode == GameMode.TEAM_2V2 -> {
                    val teamTag = if (index == 0 || index == 2) "Team 1" else "Team 2"
                    if (isBot) "Partner (${color.titleEn})" else "Player ${index + 1}"
                }
                else -> if (_gameState.value.isBengali) "খেলোয়াড় ${index + 1}" else "Player ${index + 1}"
            }

            val activeStyle = _gameState.value.equippedToken
            val initialStep = if (mode == GameMode.OPEN_TOKEN) 0 else BoardCoordinates.STEP_YARD
            val initialTokens = LudoPremiumToken.createFourPawnsForColor(color, activeStyle, initialStep)
            Player(
                id = index,
                name = defaultName,
                color = color,
                isBot = botFlags.getOrElse(index) { false },
                tokens = initialTokens
            )
        }

        val effectiveTargetTokens = when {
            mode == GameMode.QUICK_LUDO -> 1
            tournamentTierId in listOf("teju", "classic", "star", "superstar", "mega_star", "ultra_star") -> 1
            tournamentTierId in listOf("king", "emperor") -> 2
            tournamentTierId == "masta_king" -> 4
            targetTokensToWin in 1..4 -> targetTokensToWin
            else -> 4
        }
        val isQuickRulesMatch = effectiveTargetTokens == 1

        val firstPlayerName = newPlayers.firstOrNull()?.name ?: "Player"
        val initialEn = when {
            mode == GameMode.TEAM_2V2 -> "$firstPlayerName (Team 1) Turn - Roll the dice!"
            mode == GameMode.OPEN_TOKEN -> "⭐ Open Token Ludo: All 4 tokens unlocked on star points! $firstPlayerName's Turn - Roll the dice!"
            tournamentName != null && effectiveTargetTokens == 1 -> "⚡ $tournamentName: Quick Rules! 1 token home WINS! $firstPlayerName's Turn - Roll the dice!"
            tournamentName != null && effectiveTargetTokens == 2 -> "👑 $tournamentName: Home 2 tokens to WIN! $firstPlayerName's Turn - Roll the dice!"
            tournamentName != null && effectiveTargetTokens == 4 -> "🏆 $tournamentName: Classic Rules! Home all 4 tokens to WIN! $firstPlayerName's Turn - Roll the dice!"
            else -> "$firstPlayerName's Turn - Roll the dice!"
        }
        val initialBn = when {
            mode == GameMode.TEAM_2V2 -> "$firstPlayerName (টিম ১) এর চাল - ছক্কা চালুন!"
            mode == GameMode.OPEN_TOKEN -> "⭐ ওপেন টোকেন লুডো: সব গুটি আগেই আনলক! $firstPlayerName এর চাল - ছক্কা চালুন!"
            tournamentName != null && effectiveTargetTokens == 1 -> "⚡ $tournamentName: কুইক রুলস! ১টি গুটি পাকা করলেই জয়! $firstPlayerName এর চাল - ছক্কা চালুন!"
            tournamentName != null && effectiveTargetTokens == 2 -> "👑 $tournamentName: ২টি গুটি পাকা করলেই জয়! $firstPlayerName এর চাল - ছক্কা চালুন!"
            tournamentName != null && effectiveTargetTokens == 4 -> "🏆 $tournamentName: ক্লাসিক রুলস! ৪টি গুটিই পাকা করতে হবে! $firstPlayerName এর চাল - ছক্কা চালুন!"
            else -> "$firstPlayerName এর চাল - ছক্কা চালুন!"
        }

        val computedTotalPool = when {
            entryFeePerPlayer > 0 -> entryFeePerPlayer * actualCount
            tournamentNetPrize != null && tournamentNetPrize > 0 -> tournamentNetPrize
            else -> 0
        }

        _gameState.value = GameState(
            mode = mode,
            isQuickMode = isQuickRulesMatch,
            targetTokensToWin = effectiveTargetTokens,
            tournamentTierId = tournamentTierId,
            players = newPlayers,
            currentTurnIndex = 0,
            diceValue = 6,
            isRolling = false,
            phase = GamePhase.ROLL_DICE,
            consecutiveSixes = 0,
            winnerRanking = emptyList(),
            winningTeamName = null,
            tournamentNetPrize = tournamentNetPrize,
            tournamentName = tournamentName,
            entryFeePerPlayer = entryFeePerPlayer,
            totalCoinPool = computedTotalPool,
            statusMessageEn = initialEn,
            statusMessageBn = initialBn,
            language = _gameState.value.language,
            isBengali = _gameState.value.isBengali,
            soundEnabled = _gameState.value.soundEnabled,
            fastSpeed = _gameState.value.fastSpeed,
            equippedToken = _gameState.value.equippedToken,
            equippedDiceSkin = _gameState.value.equippedDiceSkin,
            colorPawnRoster = LudoPremiumToken.createAllColorPawnRoster(_gameState.value.equippedToken)
        )

        // Increment total games
        saveStats(_stats.value.copy(totalGamesPlayed = _stats.value.totalGamesPlayed + 1))

        if (newPlayers.firstOrNull()?.isBot == true) {
            triggerBotTurn()
        }
    }

    private var lastUsedOnlinePatternId: String? = null

    /**
     * Starts a Snakes & Ladders game:
     * - Online mode: Dynamic randomizer automatically changes the board's layout/pattern every single time (from a pool of 5 presets).
     * - Offline mode: Keeps the board layout fixed with only a single standard static pattern (no random changes).
     */
    fun startSnakesAndLaddersGame(
        isOnline: Boolean,
        playerCount: Int = 2,
        playerNames: List<String> = emptyList(),
        botFlags: List<Boolean> = emptyList()
    ) {
        cancelActiveJobs()

        val chosenPattern = if (isOnline) {
            val pattern = SnakesAndLaddersPatternPool.getRandomOnlinePattern(lastUsedOnlinePatternId)
            lastUsedOnlinePatternId = pattern.id
            pattern
        } else {
            SnakesAndLaddersPatternPool.getOfflineFixedPattern()
        }

        val count = playerCount.coerceIn(2, 4)
        val colors = listOf(LudoColor.RED, LudoColor.GREEN, LudoColor.YELLOW, LudoColor.BLUE)

        val slPlayers = (0 until count).map { index ->
            val isBot = botFlags.getOrElse(index) { false }
            val customName = playerNames.getOrNull(index)?.takeIf { it.isNotBlank() }
            val defaultName = customName ?: if (isBot) "Player ${index + 1} (Bot)" else "Player ${index + 1}"
            SnakesAndLaddersPlayer(
                id = index,
                name = defaultName,
                color = colors[index],
                isBot = isBot,
                position = SnakesAndLaddersEngine.START_SQUARE,
                hasStarted = true,
                previousPosition = SnakesAndLaddersEngine.START_SQUARE,
                pathHistory = listOf(SnakesAndLaddersEngine.START_SQUARE)
            )
        }

        val initialEn = "🎲 ${slPlayers[0].name}'s Turn - Roll the dice to advance!"
        val initialBn = "🎲 ${slPlayers[0].name} এর চাল - এগোতে ছক্কা চালুন!"

        val slState = SnakesAndLaddersState(
            isOnline = isOnline,
            pattern = chosenPattern,
            players = slPlayers,
            currentTurnIndex = 0,
            diceValue = 6,
            rollingDisplayValue = 6,
            consecutiveSixes = 0,
            consecutiveSixesPlayerId = slPlayers.firstOrNull()?.id,
            isRolling = false,
            isMoving = false,
            actionAlertEn = null,
            actionAlertBn = null,
            winnerPlayer = null
        )

        _gameState.value = GameState(
            mode = GameMode.SNAKES_AND_LADDERS,
            isSnakesAndLadders = true,
            isQuickMode = false,
            snakesAndLaddersState = slState,
            players = slPlayers.map { p ->
                Player(
                    id = p.id,
                    name = p.name,
                    color = p.color,
                    isBot = p.isBot
                )
            },
            currentTurnIndex = 0,
            diceValue = 6,
            rollingDisplayValue = 6,
            consecutiveSixes = 0,
            isRolling = false,
            phase = GamePhase.ROLL_DICE,
            statusMessageEn = initialEn,
            statusMessageBn = initialBn,
            language = _gameState.value.language,
            isBengali = _gameState.value.isBengali,
            soundEnabled = _gameState.value.soundEnabled,
            fastSpeed = _gameState.value.fastSpeed,
            equippedToken = _gameState.value.equippedToken,
            equippedDiceSkin = _gameState.value.equippedDiceSkin
        )

        saveStats(_stats.value.copy(totalGamesPlayed = _stats.value.totalGamesPlayed + 1))
    }

    /**
     * Starts QUICK LUDO mode: fast match where first player to get 1 pawn home WINS!
     */
    fun startQuickLudoGame(
        isOnline: Boolean = false,
        playerCount: Int = 2,
        playerNames: List<String> = emptyList(),
        botFlags: List<Boolean> = emptyList()
    ) {
        cancelActiveJobs()
        val actualCount = playerCount.coerceIn(2, 4)
        val selectedColors = when (actualCount) {
            2 -> listOf(LudoColor.BLUE, LudoColor.GREEN)
            3 -> listOf(LudoColor.BLUE, LudoColor.RED, LudoColor.GREEN)
            else -> listOf(LudoColor.BLUE, LudoColor.RED, LudoColor.GREEN, LudoColor.YELLOW)
        }

        val activeStyle = _gameState.value.equippedToken
        val newPlayers = selectedColors.mapIndexed { index, color ->
            val isBot = botFlags.getOrElse(index) { false }
            val customName = playerNames.getOrNull(index)?.takeIf { it.isNotBlank() }
            val defaultName = if (index == 0) "Player 1" else (customName ?: if (isBot) "Computer (AI)" else "Player ${index + 1}")
            Player(
                id = index,
                name = defaultName,
                color = color,
                isBot = isBot,
                tokens = LudoPremiumToken.createFourPawnsForColor(color, activeStyle)
            )
        }

        val initialEn = "⚡ QUICK LUDO! 1 pawn home WINS! ${newPlayers[0].name}'s Turn - Roll the dice!"
        val initialBn = "⚡ কুইক লুডো! ১টি গুটি ঘরে পৌঁছালেই জয়! ${newPlayers[0].name} এর চাল - ছক্কা চালুন!"

        _gameState.value = GameState(
            mode = GameMode.QUICK_LUDO,
            isQuickMode = true,
            targetTokensToWin = 1,
            isSnakesAndLadders = false,
            players = newPlayers,
            currentTurnIndex = 0,
            diceValue = 6,
            rollingDisplayValue = 6,
            isRolling = false,
            phase = GamePhase.ROLL_DICE,
            consecutiveSixes = 0,
            winnerRanking = emptyList(),
            winningTeamName = null,
            statusMessageEn = initialEn,
            statusMessageBn = initialBn,
            language = _gameState.value.language,
            isBengali = _gameState.value.isBengali,
            soundEnabled = _gameState.value.soundEnabled,
            fastSpeed = true,
            equippedToken = activeStyle,
            equippedDiceSkin = _gameState.value.equippedDiceSkin,
            colorPawnRoster = LudoPremiumToken.createAllColorPawnRoster(activeStyle)
        )

        saveStats(_stats.value.copy(totalGamesPlayed = _stats.value.totalGamesPlayed + 1))
    }

    /**
     * Starts an Online Multiplayer match with friends via Room Code and WhatsApp sharing.
     */
    fun startOnlineFriendsGame(
        roomCode: String,
        localPlayerId: Int,
        playerCount: Int,
        playerNames: List<String>,
        isDoubleTeam: Boolean = false,
        coinWager: Int = 500
    ) {
        botJob?.cancel()
        rollAnimationJob?.cancel()
        moveJob?.cancel()
        turnsWithoutSix.clear()

        val actualCount = if (isDoubleTeam) 4 else playerCount.coerceIn(2, 4)
        val pooledPrize = coinWager * actualCount
        // Red, Green, Yellow, Blue across all 4 houses
        val selectedColors = when {
            isDoubleTeam -> listOf(
                LudoColor.RED,    // Player 0 (Team A)
                LudoColor.GREEN,  // Player 1 (Team B)
                LudoColor.YELLOW, // Player 2 (Team A)
                LudoColor.BLUE    // Player 3 (Team B)
            )
            actualCount == 4 -> listOf(
                LudoColor.RED,
                LudoColor.GREEN,
                LudoColor.YELLOW,
                LudoColor.BLUE
            )
            actualCount == 3 -> listOf(LudoColor.RED, LudoColor.GREEN, LudoColor.BLUE)
            else -> listOf(LudoColor.BLUE, LudoColor.GREEN)
        }

        val activeStyle = _gameState.value.equippedToken
        val newPlayers = selectedColors.mapIndexed { index, color ->
            val defaultName = if (index == 0) {
                if (isDoubleTeam) "Player 1 (Team A)" else "Player 1"
            } else if (isDoubleTeam) {
                val teamTag = if (index == 2) "Team A" else "Team B"
                playerNames.getOrElse(index) { "Player ${index + 1} ($teamTag)" }
            } else {
                playerNames.getOrElse(index) { "Player ${index + 1}" }
            }
            Player(
                id = index,
                name = defaultName,
                color = color,
                isBot = false,
                tokens = LudoPremiumToken.createFourPawnsForColor(color, activeStyle)
            )
        }

        val initialEn = if (isDoubleTeam) {
            "${newPlayers[0].name} (Team A) Turn - Roll the dice!"
        } else {
            "${newPlayers[0].name}'s Turn - Roll the dice!"
        }
        val initialBn = if (isDoubleTeam) {
            "${newPlayers[0].name} (টিম এ) এর চাল - ছক্কা চালুন!"
        } else {
            "${newPlayers[0].name} এর চাল - ছক্কা চালুন!"
        }

        _gameState.value = GameState(
            mode = GameMode.ONLINE_FRIENDS,
            players = newPlayers,
            currentTurnIndex = 0,
            diceValue = 6,
            isRolling = false,
            phase = GamePhase.ROLL_DICE,
            consecutiveSixes = 0,
            winnerRanking = emptyList(),
            winningTeamName = null,
            tournamentNetPrize = pooledPrize,
            tournamentName = "FRIENDS MATCH (🪙 $coinWager STAKES)",
            entryFeePerPlayer = coinWager,
            totalCoinPool = pooledPrize,
            statusMessageEn = initialEn,
            statusMessageBn = initialBn,
            language = _gameState.value.language,
            isBengali = _gameState.value.isBengali,
            soundEnabled = _gameState.value.soundEnabled,
            fastSpeed = _gameState.value.fastSpeed,
            onlineRoomCode = roomCode,
            onlineLocalPlayerId = localPlayerId,
            isOnlineTeam = isDoubleTeam,
            equippedToken = activeStyle,
            equippedDiceSkin = _gameState.value.equippedDiceSkin,
            colorPawnRoster = LudoPremiumToken.createAllColorPawnRoster(activeStyle)
        )

        saveStats(_stats.value.copy(totalGamesPlayed = _stats.value.totalGamesPlayed + 1))

        // Listen for real-time multiplayer game state synchronization with zero-loss fallback
        OnlineMultiplayerManager.listenToGameSync(
            roomCode = roomCode,
            onDiceRolled = { remotePlayerId, roll, turnIndex ->
                if (remotePlayerId != localPlayerId) {
                    applyRemoteDiceRoll(remotePlayerId, roll, turnIndex)
                }
            },
            onTokenMoved = { remotePlayerId, tokenId, targetStep, nextTurn, tokensMatrix ->
                if (remotePlayerId != localPlayerId) {
                    applyRemoteTokenMove(remotePlayerId, tokenId, targetStep, nextTurn, tokensMatrix)
                }
            },
            onTurnPassed = { remotePlayerId, nextTurn, tokensMatrix ->
                if (remotePlayerId != localPlayerId) {
                    _gameState.update { s ->
                        var updatedPlayers = s.players
                        if (tokensMatrix != null && tokensMatrix.isNotEmpty()) {
                            updatedPlayers = s.players.map { p ->
                                val remoteSteps = tokensMatrix[p.id]
                                if (remoteSteps != null && remoteSteps.size == 4) {
                                    p.copy(tokens = p.tokens.mapIndexed { idx, t ->
                                        t.copy(step = remoteSteps[idx])
                                    })
                                } else p
                            }
                        }
                        val nextIndex = nextTurn.coerceIn(0, (s.players.size - 1).coerceAtLeast(0))
                        val nextPlayer = updatedPlayers.getOrNull(nextIndex)
                        val turnEn = if (nextPlayer != null) "${nextPlayer.name}'s Turn - Roll the dice!" else "Roll the dice!"
                        val turnBn = if (nextPlayer != null) "${nextPlayer.name} এর চাল - ছক্কা চালুন!" else "ছক্কা চালুন!"
                        s.copy(
                            players = updatedPlayers,
                            currentTurnIndex = nextIndex,
                            phase = GamePhase.ROLL_DICE,
                            isRolling = false,
                            consecutiveSixes = 0,
                            statusMessageEn = turnEn,
                            statusMessageBn = turnBn
                        )
                    }
                    startTurnWatchdog()
                }
            },
            onSyncRequested = {
                val curState = _gameState.value
                val matrix = curState.players.associate { it.id to it.tokens.map { t -> t.step } }
                OnlineMultiplayerManager.sendSyncState(
                    roomCode = roomCode,
                    turnIndex = curState.currentTurnIndex,
                    phase = curState.phase.name,
                    diceValue = curState.diceValue,
                    tokensMatrix = matrix
                )
            },
            onSyncReceived = { remoteTurnIndex, remotePhase, remoteDice, tokensMatrix ->
                _gameState.update { s ->
                    var updatedPlayers = s.players
                    if (tokensMatrix.isNotEmpty()) {
                        updatedPlayers = s.players.map { p ->
                            val remoteSteps = tokensMatrix[p.id]
                            if (remoteSteps != null && remoteSteps.size == 4) {
                                p.copy(tokens = p.tokens.mapIndexed { idx, t ->
                                    t.copy(step = remoteSteps[idx])
                                })
                            } else p
                        }
                    }
                    val validPhase = try { GamePhase.valueOf(remotePhase) } catch (_: Exception) { GamePhase.ROLL_DICE }
                    s.copy(
                        players = updatedPlayers,
                        currentTurnIndex = remoteTurnIndex.coerceIn(0, (s.players.size - 1).coerceAtLeast(0)),
                        phase = validPhase,
                        diceValue = remoteDice,
                        isRolling = false
                    )
                }
                startTurnWatchdog()
            }
        )

        startTurnWatchdog()
    }

    /**
     * Balanced, Fair & Purely Natural Dice Roll System (Version 30):
     *
     * 1. Fully Random & Impartial Gameplay:
     *    - Dice rolls and opponent token captures are 100% natural, fair, and purely based on chance
     *      and board movement rules without any artificial probability manipulation.
     *
     * 2. Last Token Safety Rule (Smart Dice Constraint):
     *    - Condition: When a player has only 1 token remaining on the board (the other 3 tokens have
     *      already completed the home journey/won), and that final token is strictly between 1 to 5
     *      steps away from the final home/goal block (dist in 1..5).
     *    - Behavior: Value of 6 (Six) is strictly omitted/restricted. The random number generator only
     *      outputs values between 1 and 5 (inclusive). This guarantees the turn is never wasted on an
     *      unusable 6.
     *
     * 3. Maximum 2 Consecutive Sixes Rule:
     *    - Strictly capped at 2 sixes per turn so 3 consecutive sixes cannot be rolled (capping 3rd roll to 1..5).
     *
     * 4. Symmetrical Yard Protection:
     *    - Unbiased 4-turn yard safety protection equal across all houses (Red, Green, Yellow, Blue).
     */
    private fun isLastTokenSafetyRuleActive(player: Player): Boolean {
        val state = _gameState.value
        val tokensNeededToWin = (state.targetTokensToWin - player.finishedTokensCount).coerceAtLeast(1)
        if (tokensNeededToWin != 1) return false

        val tokensOnBoard = player.tokens.filter { it.isOnBoard }
        if (tokensOnBoard.size != 1) return false

        val remainingToken = tokensOnBoard.first()
        // Must be actively on board and strictly 1 to 5 steps away from home (BoardCoordinates.STEP_GOAL = 56)
        val distToGoal = BoardCoordinates.MAX_STEPS - remainingToken.step
        return distToGoal in 1..5
    }

    private fun generateFairDiceRoll(player: Player, consecutiveSixes: Int): Int {
        val state = _gameState.value

        // Resolve active tokens for this turn (supports 2v2 teammate tokens when finished)
        val activePlayer = if (state.isTeamMode() && player.isFinished) {
            state.players.find { state.isTeammate(it.id, player.id) && it.id != player.id } ?: player
        } else {
            player
        }

        // Rule: Last Token Safety Rule (Smart Dice Constraint)
        // If a player has only 1 token remaining on the board and it is strictly 1 to 5 steps away from home,
        // prevent rolling a 6 so the turn is never wasted. Random generator outputs values between 1 and 5 (inclusive).
        val restrictSixForLastToken = isLastTokenSafetyRuleActive(activePlayer)
        if (restrictSixForLastToken) {
            return secureRandom.nextInt(5) + 1
        }

        // Strictly enforce Maximum 2 Consecutive Sixes Rule:
        // On 3rd roll (consecutiveSixes >= 2), 6 is strictly capped. Roll uniformly from 1..5.
        if (consecutiveSixes >= 2) {
            return secureRandom.nextInt(5) + 1
        }

        val hasTokensInYard = player.tokens.any { it.isInYard }

        // Symmetrical bad-luck protection: completely equal across all 4 houses (Red, Yellow, Green, Blue)
        // If a player has tokens in yard and misses 6 for 4 consecutive turns, guarantee a 6.
        if (hasTokensInYard) {
            val missedTurns = turnsWithoutSix[player.id] ?: 0
            if (missedTurns >= 4) {
                turnsWithoutSix[player.id] = 0
                return 6
            }
        }

        // Completely random, fair, and impartial natural dice roll: 1..6 with exact equal probability (1/6)
        val roll = secureRandom.nextInt(6) + 1

        if (roll == 6) {
            turnsWithoutSix[player.id] = 0
        } else if (hasTokensInYard) {
            turnsWithoutSix[player.id] = (turnsWithoutSix[player.id] ?: 0) + 1
        }

        return roll
    }

    private fun rollSnakesAndLaddersDice() {
        val state = _gameState.value
        val slState = state.snakesAndLaddersState
        if (slState.isRolling || slState.isMoving || slState.isAnimatingClimbOrSlide || slState.isGameOver) return

        val player = slState.currentPlayer ?: return
        if (!isRollingAtomic.compareAndSet(false, true)) return

        rollAnimationJob?.cancel()
        moveJob?.cancel()
        soundManager.playDiceRoll()

        val effectiveSixStreak = if (slState.consecutiveSixesPlayerId == null || slState.consecutiveSixesPlayerId == player.id) {
            slState.consecutiveSixes
        } else {
            0
        }

        _gameState.update {
            it.copy(
                isRolling = true,
                snakesAndLaddersState = it.snakesAndLaddersState.copy(
                    isRolling = true,
                    consecutiveSixes = effectiveSixStreak,
                    consecutiveSixesPlayerId = player.id,
                    actionAlertEn = null,
                    actionAlertBn = null
                )
            )
        }

        rollAnimationJob = viewModelScope.launch {
            try {
                // Strict No-Three-6s Rule (Online & Offline): if player already rolled 6 twice in a row
                // (effectiveSixStreak >= 2), the 3rd roll is strictly restricted to 1..5.
                val rawRoll = SnakesAndLaddersEngine.rollDiceValue(
                    consecutiveSixes = effectiveSixStreak,
                    random = secureRandom
                )
                val finalRoll = if (effectiveSixStreak >= SnakesAndLaddersEngine.MAX_CONSECUTIVE_SIXES && rawRoll == 6) {
                    (secureRandom.nextInt(5) + 1)
                } else {
                    rawRoll
                }
            val rollTicks = if (state.fastSpeed) 10 else 14
            var tickDelay = 36L
            val otherFaces = (1..6).filter { it != finalRoll }.shuffled()
            val facesSequence = otherFaces + listOf(finalRoll) + (1..6).shuffled()

            for (step in 0 until (rollTicks - 1)) {
                val temp = facesSequence.getOrElse(step) { (step % 6) + 1 }
                _gameState.update {
                    it.copy(
                        rollingDisplayValue = temp,
                        snakesAndLaddersState = it.snakesAndLaddersState.copy(rollingDisplayValue = temp)
                    )
                }
                delay(tickDelay)
                tickDelay += 4L
            }

            val isSix = (finalRoll == 6)
            if (isSix) {
                soundManager.playSixRolled()
            }

            _gameState.update {
                it.copy(
                    diceValue = finalRoll,
                    rollingDisplayValue = finalRoll,
                    isRolling = false,
                    snakesAndLaddersState = it.snakesAndLaddersState.copy(
                        diceValue = finalRoll,
                        rollingDisplayValue = finalRoll,
                        isRolling = false,
                        isMoving = true
                    )
                )
            }

            delay(250L)

            // CUSTOM RULES ENGINE:
            // 1. Pre-opened tokens: all tokens start open & active on Square 1; any roll (1..6) moves immediately.
            // 2. Rolling 6 grants an extra turn (capped at max 2 consecutive sixes).
            // 3. Overflow reverse movement: if remaining steps to 100 < diceRoll, move backward by full diceRoll.
            // 4. Capture ("cut") on reverse/forward landing: opponent animates backward step-by-step along
            //    the exact tiles previously covered back to Square 1.
            val movePlan = SnakesAndLaddersEngine.planMove(
                startPosition = player.position,
                diceRoll = finalRoll,
                pattern = slState.pattern
            )

            if (movePlan.isOverflowReverse) {
                val reverseTarget = movePlan.landedPositionBeforeHazard
                val bounceEn = "↩️ BOUNCE BACK! ${player.name} needed ${movePlan.remainingToGoal} to win — moving back $finalRoll steps (${movePlan.startPosition} ➔ $reverseTarget)!"
                val bounceBn = "↩️ রিভার্স! ${player.name}-এর জিততে ${movePlan.remainingToGoal} দরকার ছিল — পিছনে $finalRoll ঘর যাচ্ছে (${movePlan.startPosition} ➔ $reverseTarget)!"
                _gameState.update { current ->
                    val updatedPlayers = current.snakesAndLaddersState.players.map { p ->
                        if (p.id == player.id) p.copy(hasStarted = true, previousPosition = movePlan.startPosition) else p
                    }
                    current.copy(
                        statusMessageEn = bounceEn,
                        statusMessageBn = bounceBn,
                        snakesAndLaddersState = current.snakesAndLaddersState.copy(
                            players = updatedPlayers,
                            actionAlertEn = bounceEn,
                            actionAlertBn = bounceBn
                        )
                    )
                }
            } else {
                _gameState.update { current ->
                    val updatedPlayers = current.snakesAndLaddersState.players.map { p ->
                        if (p.id == player.id) p.copy(hasStarted = true, previousPosition = movePlan.startPosition) else p
                    }
                    current.copy(
                        snakesAndLaddersState = current.snakesAndLaddersState.copy(players = updatedPlayers)
                    )
                }
            }

            // Animate tile-by-tile movement (forward OR reverse bounce-back) and record covered pathHistory
            for (step in movePlan.stepPath) {
                soundManager.playTokenMove()
                _gameState.update { current ->
                    val updatedPlayers = current.snakesAndLaddersState.players.map { p ->
                        if (p.id == player.id) {
                            p.copy(
                                hasStarted = true,
                                position = step,
                                pathHistory = SnakesAndLaddersEngine.appendPathSteps(
                                    currentHistory = p.pathHistory,
                                    startPosition = p.position,
                                    steps = listOf(step)
                                )
                            )
                        } else {
                            p
                        }
                    }
                    current.copy(
                        snakesAndLaddersState = current.snakesAndLaddersState.copy(players = updatedPlayers)
                    )
                }
                delay(if (state.fastSpeed) 75L else 130L)
            }

            // Check for Ladder or Snake at landed square and trigger IMMEDIATELY without delay
            val targetPos = movePlan.landedPositionBeforeHazard
            val hazard = movePlan.hazardTriggered
            if (hazard != null) {
                val destination = hazard.to
                val isLadder = hazard.isLadder

                if (isLadder) {
                    // LADDER: Immediate climb up to the top of the ladder
                    soundManager.playTokenEnterGoal()
                    val ladderMsgEn = "🪜 LADDER! ${player.name} climbed $targetPos ➔ $destination!"
                    val ladderMsgBn = "🪜 মই! ${player.name} $targetPos থেকে $destination-এ উঠেছে!"
                    _gameState.update { current ->
                        val updatedPlayers = current.snakesAndLaddersState.players.map { p ->
                            if (p.id == player.id) {
                                p.copy(
                                    previousPosition = targetPos,
                                    position = destination,
                                    pathHistory = SnakesAndLaddersEngine.appendPathSteps(
                                        currentHistory = p.pathHistory,
                                        startPosition = targetPos,
                                        steps = emptyList(),
                                        hazardDestination = destination
                                    )
                                )
                            } else {
                                p
                            }
                        }
                        current.copy(
                            statusMessageEn = ladderMsgEn,
                            statusMessageBn = ladderMsgBn,
                            snakesAndLaddersState = current.snakesAndLaddersState.copy(
                                players = updatedPlayers,
                                isAnimatingClimbOrSlide = true,
                                isClimbingLadder = true,
                                animatingPlayerId = player.id,
                                animatingProgress = 0f,
                                currentClimbingOrSlidingItem = hazard,
                                actionAlertEn = ladderMsgEn,
                                actionAlertBn = ladderMsgBn
                            )
                        )
                    }

                    val climbSteps = 20
                    for (s in 1..climbSteps) {
                        val progress = s.toFloat() / climbSteps
                        _gameState.update { current ->
                            current.copy(
                                snakesAndLaddersState = current.snakesAndLaddersState.copy(
                                    animatingProgress = progress
                                )
                            )
                        }
                        if (s % 4 == 0) soundManager.playTokenMove()
                        delay(if (state.fastSpeed) 18L else 26L)
                    }

                    _gameState.update { current ->
                        current.copy(
                            snakesAndLaddersState = current.snakesAndLaddersState.copy(
                                isAnimatingClimbOrSlide = false,
                                animatingPlayerId = null,
                                animatingProgress = 0f,
                                currentClimbingOrSlidingItem = null
                            )
                        )
                    }
                } else {
                    // SNAKE: Immediate slide down along the twisting curved body segments to tail
                    soundManager.playTokenCapture()
                    val snakeMsgEn = "🐍 SNAKE BITE! ${player.name} slid down $targetPos ➔ $destination!"
                    val snakeMsgBn = "🐍 সাপের কামড়! ${player.name} $targetPos থেকে $destination-এ নেমেছে!"
                    _gameState.update { current ->
                        val updatedPlayers = current.snakesAndLaddersState.players.map { p ->
                            if (p.id == player.id) {
                                p.copy(
                                    previousPosition = targetPos,
                                    position = destination,
                                    pathHistory = SnakesAndLaddersEngine.appendPathSteps(
                                        currentHistory = p.pathHistory,
                                        startPosition = targetPos,
                                        steps = emptyList(),
                                        hazardDestination = destination
                                    )
                                )
                            } else {
                                p
                            }
                        }
                        current.copy(
                            statusMessageEn = snakeMsgEn,
                            statusMessageBn = snakeMsgBn,
                            snakesAndLaddersState = current.snakesAndLaddersState.copy(
                                players = updatedPlayers,
                                isAnimatingClimbOrSlide = true,
                                isClimbingLadder = false,
                                animatingPlayerId = player.id,
                                animatingProgress = 0f,
                                currentClimbingOrSlidingItem = hazard,
                                actionAlertEn = snakeMsgEn,
                                actionAlertBn = snakeMsgBn
                            )
                        )
                    }

                    val slideSteps = 24
                    for (s in 1..slideSteps) {
                        val progress = s.toFloat() / slideSteps
                        _gameState.update { current ->
                            current.copy(
                                snakesAndLaddersState = current.snakesAndLaddersState.copy(
                                    animatingProgress = progress
                                )
                            )
                        }
                        if (s % 5 == 0) soundManager.playTokenMove()
                        delay(if (state.fastSpeed) 18L else 26L)
                    }

                    _gameState.update { current ->
                        current.copy(
                            snakesAndLaddersState = current.snakesAndLaddersState.copy(
                                isAnimatingClimbOrSlide = false,
                                animatingPlayerId = null,
                                animatingProgress = 0f,
                                currentClimbingOrSlidingItem = null
                            )
                        )
                    }
                }
            }

            // Check if reached 100 (WINNER!)
            val finalPlayerPos = _gameState.value.snakesAndLaddersState.players.find { it.id == player.id }?.position ?: movePlan.finalPosition
            if (finalPlayerPos >= SnakesAndLaddersEngine.GOAL_SQUARE) {
                val localPlayerId = getLocalPlayerId(_gameState.value)
                val isLocalWinner = player.id == localPlayerId
                if (isLocalWinner) {
                    soundManager.playVictoryFanfare()
                } else {
                    soundManager.playDefeatSound()
                }
                val winEn = "🏆 ${player.name} reached 100 and WON the Snakes & Ladders Match!"
                val winBn = "🏆 ${player.name} ১০০ ঘরে পৌঁছে সাপ-লুডো জয়ী হয়েছেন!"
                _gameState.update { current ->
                    val winPlayer = current.snakesAndLaddersState.players.find { it.id == player.id }?.copy(rank = 1)
                    val winnerLudoPlayer = current.players.find { it.id == player.id }
                        ?: current.players.firstOrNull()
                        ?: Player(id = player.id, name = player.name, color = LudoColor.BLUE)
                    current.copy(
                        phase = GamePhase.GAME_OVER,
                        statusMessageEn = winEn,
                        statusMessageBn = winBn,
                        winnerRanking = listOf(winnerLudoPlayer) + current.players.filter { it.id != winnerLudoPlayer.id },
                        snakesAndLaddersState = current.snakesAndLaddersState.copy(
                            isMoving = false,
                            winnerPlayer = winPlayer,
                            actionAlertEn = winEn,
                            actionAlertBn = winBn
                        )
                    )
                }
                if (isLocalWinner) {
                    saveStats(_stats.value.copy(totalWins = _stats.value.totalWins + 1))
                    CoinManager.addCoins(CoinManager.SNAKES_WIN_COINS, "Snakes & Ladders Victory")
                }
                return@launch
            }

            // TOKEN CAPTURE / CUT MECHANIC (Forward OR Reverse Bounce-Back Movement):
            // Landing on an opponent's square (2..99) captures ("cuts") them and animates them
            // stepping backward tile-by-tile along their exact covered path back to Square 1.
            val captureResult = SnakesAndLaddersEngine.resolveCaptures(
                players = _gameState.value.snakesAndLaddersState.players,
                movingPlayerId = player.id,
                finalPosition = finalPlayerPos
            )
            if (captureResult.capturedPlayers.isNotEmpty()) {
                soundManager.playTokenCapture()
                val capNames = captureResult.capturedPlayers.joinToString(", ") { it.name }
                val capEn = if (movePlan.isOverflowReverse) {
                    "⚔️ REVERSE CUT! ${player.name} bounced back & captured $capNames's token back to Square 1!"
                } else {
                    "⚔️ MARAMARI! ${player.name} captured $capNames's token and sent it back to Square 1!"
                }
                val capBn = if (movePlan.isOverflowReverse) {
                    "⚔️ রিভার্স কাট! ${player.name} পিছনে ফিরে $capNames এর গুটি কেটে ১ নম্বর ঘরে পাঠাল!"
                } else {
                    "⚔️ মারামারি! ${player.name} $capNames এর গুটি কেটে ১ নম্বর ঘরে ফেরত পাঠাল!"
                }

                // Keep isMoving = true while animating captured token(s) step-by-step back home
                _gameState.update { current ->
                    current.copy(
                        statusMessageEn = capEn,
                        statusMessageBn = capBn,
                        snakesAndLaddersState = current.snakesAndLaddersState.copy(
                            isMoving = true,
                            actionAlertEn = capEn,
                            actionAlertBn = capBn
                        )
                    )
                }

                delay(if (state.fastSpeed) 220L else 360L)

                val returnPaths = captureResult.returnPaths
                val maxReturnSteps = returnPaths.values.maxOfOrNull { it.size } ?: 0
                val returnStepDelayMs = when {
                    maxReturnSteps > 50 -> if (state.fastSpeed) 18L else 28L
                    maxReturnSteps > 20 -> if (state.fastSpeed) 28L else 45L
                    else -> if (state.fastSpeed) 42L else 68L
                }

                for (stepIdx in 0 until maxReturnSteps) {
                    _gameState.update { current ->
                        val updatedPlayers = current.snakesAndLaddersState.players.map { p ->
                            val path = returnPaths[p.id]
                            if (path != null && stepIdx < path.size) {
                                val nextTile = path[stepIdx]
                                val trimmedHistory = if (p.pathHistory.size > 1) p.pathHistory.dropLast(1) else listOf(nextTile)
                                p.copy(
                                    previousPosition = p.position,
                                    position = nextTile,
                                    pathHistory = trimmedHistory
                                )
                            } else {
                                p
                            }
                        }
                        current.copy(
                            snakesAndLaddersState = current.snakesAndLaddersState.copy(players = updatedPlayers)
                        )
                    }
                    if (maxReturnSteps <= 24 || stepIdx % 2 == 0) {
                        soundManager.playTokenMove()
                    }
                    delay(returnStepDelayMs)
                }

                // Finalize captured player state at Square 1 with clean reset pathHistory
                _gameState.update { current ->
                    current.copy(
                        snakesAndLaddersState = current.snakesAndLaddersState.copy(
                            players = captureResult.updatedPlayers,
                            isMoving = false
                        )
                    )
                }
                delay(if (state.fastSpeed) 200L else 350L)
                advanceSnakesAndLaddersTurn(
                    isBonusTurn = true,
                    rolledValue = finalRoll,
                    isCaptureBonus = true
                )
                return@launch
            }

            _gameState.update {
                it.copy(
                    snakesAndLaddersState = it.snakesAndLaddersState.copy(isMoving = false)
                )
            }

                advanceSnakesAndLaddersTurn(
                    isBonusTurn = movePlan.isSixExtraTurn,
                    rolledValue = finalRoll,
                    isCaptureBonus = false
                )
            } finally {
                isRollingAtomic.set(false)
                _gameState.update { current ->
                    val currentSl = current.snakesAndLaddersState
                    if (current.isRolling || currentSl.isRolling || currentSl.isMoving || currentSl.isAnimatingClimbOrSlide) {
                        current.copy(
                            isRolling = false,
                            snakesAndLaddersState = currentSl.copy(
                                isRolling = false,
                                isMoving = false,
                                isAnimatingClimbOrSlide = false,
                                animatingPlayerId = null,
                                animatingProgress = 0f,
                                currentClimbingOrSlidingItem = null
                            )
                        )
                    } else {
                        current
                    }
                }
            }
        }
    }

    private fun advanceSnakesAndLaddersTurn(
        isBonusTurn: Boolean,
        rolledValue: Int,
        isCaptureBonus: Boolean = false
    ) {
        val state = _gameState.value
        val slState = state.snakesAndLaddersState
        if (slState.isGameOver || slState.players.isEmpty()) return

        val rollingPlayer = slState.players.getOrNull(slState.currentTurnIndex)
        val priorSixStreak = if (slState.consecutiveSixesPlayerId == null || slState.consecutiveSixesPlayerId == rollingPlayer?.id) {
            slState.consecutiveSixes
        } else {
            0
        }
        val rolledSix = (rolledValue == 6)
        val nextIndex = if (isBonusTurn) {
            slState.currentTurnIndex.coerceIn(0, slState.players.lastIndex)
        } else {
            (slState.currentTurnIndex + 1) % slState.players.size
        }
        val nextSixStreak = SnakesAndLaddersEngine.nextConsecutiveSixes(
            currentConsecutiveSixes = priorSixStreak,
            rolledValue = rolledValue,
            grantsSamePlayerBonusTurn = isBonusTurn
        )

        val nextPlayer = slState.players[nextIndex]
        val turnEn = when {
            rolledSix && isBonusTurn && nextSixStreak >= SnakesAndLaddersEngine.MAX_CONSECUTIVE_SIXES ->
                "🎲 ${nextPlayer.name} rolled two 6s! Final bonus roll (no 3rd six)."
            rolledSix && isBonusTurn -> "🎲 ${nextPlayer.name} rolled 6! Extra turn!"
            isCaptureBonus && isBonusTurn -> "⚔️ ${nextPlayer.name} captured an opponent! Bonus turn!"
            else -> "🎲 ${nextPlayer.name}'s Turn - Roll the dice!"
        }
        val turnBn = when {
            rolledSix && isBonusTurn && nextSixStreak >= SnakesAndLaddersEngine.MAX_CONSECUTIVE_SIXES ->
                "🎲 ${nextPlayer.name} পরপর ২টি ছক্কা পেয়েছে! শেষ বোনাস চাল (৩য় ছক্কা নয়)।"
            rolledSix && isBonusTurn -> "🎲 ${nextPlayer.name} ছক্কা পেয়েছে! আরেকটি চাল দিন!"
            isCaptureBonus && isBonusTurn -> "⚔️ ${nextPlayer.name} গুটি কেটেছে! বোনাস চাল দিন!"
            else -> "🎲 ${nextPlayer.name} এর চাল - ছক্কা চালুন!"
        }

        _gameState.update {
            it.copy(
                currentTurnIndex = nextIndex,
                consecutiveSixes = nextSixStreak,
                statusMessageEn = turnEn,
                statusMessageBn = turnBn,
                snakesAndLaddersState = it.snakesAndLaddersState.copy(
                    currentTurnIndex = nextIndex,
                    consecutiveSixes = nextSixStreak,
                    consecutiveSixesPlayerId = nextPlayer.id,
                    actionAlertEn = if (isBonusTurn) turnEn else null,
                    actionAlertBn = if (isBonusTurn) turnBn else null
                )
            )
        }

        if (nextPlayer.isBot) {
            botJob?.cancel()
            botJob = viewModelScope.launch {
                delay(900L)
                rollDice()
            }
        }
    }

    fun rollDice() {
        val state = _gameState.value
        if (state.phase != GamePhase.ROLL_DICE || state.isRolling) return
        if (state.isSnakesAndLadders) {
            rollSnakesAndLaddersDice()
            return
        }

        val player = state.currentPlayer ?: return

        // Online mode check: Only prevent remote human players from rolling locally.
        // A computer-controlled bot (e.g. 'Nasidul Mistry') MUST ALWAYS roll locally without freezing!
        if (state.mode == GameMode.ONLINE_FRIENDS && state.onlineRoomCode != null && state.currentTurnIndex != state.onlineLocalPlayerId && !player.isBot) {
            return
        }

        if (!isRollingAtomic.compareAndSet(false, true)) return

        rollAnimationJob?.cancel()
        soundManager.playDiceRoll()

        // Synchronously lock rolling state to prevent duplicate inputs
        _gameState.update { it.copy(isRolling = true) }

        rollAnimationJob = viewModelScope.launch {
            try {
                // 1. Determine the EXACT, fair dice roll outcome FIRST
            // This strictly guarantees that what is rolled, displayed, and moved are 100% identical!
            val finalRoll = generateFairDiceRoll(player, state.consecutiveSixes)
            val isSix = finalRoll == 6
            val newSixes = if (isSix) state.consecutiveSixes + 1 else 0

            // 2. Realistic multi-axis 3D tumbling showing all 6 faces (1 to 6 pips) clearly before landing on final result
            val rollTicks = if (state.fastSpeed) 10 else 14
            var tickDelay = 36L
            val effectiveTurnPlayer = if (state.isTeamMode() && player.isFinished) {
                state.players.find { state.isTeammate(it.id, player.id) && it.id != player.id } ?: player
            } else {
                player
            }
            // Tour through all 6 faces (1 to 6 pips) clearly during the roll tumble
            val otherFaces = (1..6).filter { it != finalRoll }.shuffled()
            val facesSequence = otherFaces + listOf(finalRoll) + (1..6).shuffled()

            for (step in 0 until (rollTicks - 1)) {
                val tempDice = facesSequence.getOrElse(step) { (step % 6) + 1 }
                // Update only rollingDisplayValue, leaving authoritative diceValue untouched
                _gameState.update { it.copy(rollingDisplayValue = tempDice) }
                delay(tickDelay)
                tickDelay += 4L
            }

            // CRITICAL FIX: The final animation tick lands firmly on the exact finalRoll
            _gameState.update {
                it.copy(
                    diceValue = finalRoll,
                    rollingDisplayValue = finalRoll,
                    isRolling = false
                )
            }

            if (isSix) {
                soundManager.playSixRolled()
            }

            // Synchronize dice roll across real-time channel for online friends
            if (state.mode == GameMode.ONLINE_FRIENDS && state.onlineRoomCode != null && player.id == state.onlineLocalPlayerId) {
                OnlineMultiplayerManager.syncDiceRoll(state.onlineRoomCode, player.id, finalRoll, newSixes, player.id)
                startTurnWatchdog()
            }

            // Determine valid movable tokens strictly using finalRoll
            val (effectiveOwner, validTokens) = getMovableTokensForTurn(player, finalRoll)

            if (validTokens.isEmpty()) {
                val msgEn = if (state.consecutiveSixes == 2) {
                    "${player.name} rolled $finalRoll. No valid moves to conclude turn."
                } else {
                    "${player.name} rolled $finalRoll. No valid moves."
                }
                val msgBn = if (state.consecutiveSixes == 2) {
                    "${player.name} $finalRoll পেয়েছেন। চাল শেষ করার মতো কোনো চাল নেই।"
                } else {
                    "${player.name} $finalRoll পেয়েছেন। কোনো চাল নেই।"
                }
                _gameState.update {
                    it.copy(
                        diceValue = finalRoll,
                        rollingDisplayValue = finalRoll,
                        isRolling = false,
                        consecutiveSixes = newSixes,
                        statusMessageEn = msgEn,
                        statusMessageBn = msgBn
                    )
                }
                delay(if (state.fastSpeed) 400L else 750L)
                if (isSix) {
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
                    if (state.mode == GameMode.ONLINE_FRIENDS && state.onlineRoomCode != null && player.id == state.onlineLocalPlayerId) {
                        val matrix = _gameState.value.players.associate { it.id to it.tokens.map { t -> t.step } }
                        OnlineMultiplayerManager.syncTurnPass(state.onlineRoomCode, player.id, _gameState.value.currentTurnIndex, matrix)
                        startTurnWatchdog()
                    }
                }
            } else {
                // Highlight movable tokens on the board
                val currentPlayers = _gameState.value.players
                val updatedPlayers = currentPlayers.map { p ->
                    if (p.id == effectiveOwner.id) {
                        p.copy(tokens = p.tokens.map { token ->
                            token.copy(isHighlighted = validTokens.any { it.id == token.id })
                        })
                    } else {
                        p.copy(tokens = p.tokens.map { it.copy(isHighlighted = false) })
                    }
                }

                val helperNote = if (state.mode == GameMode.TEAM_2V2 && player.isFinished) " (Moving partner's token)" else ""
                val msgEn = when {
                    isSix && newSixes == 2 -> "${player.name} rolled 2nd Six in a row! Pick a token.$helperNote"
                    isSix -> "${player.name} rolled 6! Pick a token.$helperNote"
                    state.consecutiveSixes == 2 -> "${player.name} rolled $finalRoll! Final move of turn.$helperNote"
                    else -> "${player.name} rolled $finalRoll! Select a token.$helperNote"
                }
                val msgBn = when {
                    isSix && newSixes == 2 -> "${player.name} পরপর ২য় বার ৬ পেয়েছেন! গুটি চালান।"
                    isSix -> "${player.name} ৬ পেয়েছেন! গুটি নির্বাচন করুন।"
                    state.consecutiveSixes == 2 -> "${player.name} $finalRoll পেয়েছেন! চালের শেষ গুটি চালান।"
                    else -> "${player.name} $finalRoll পেয়েছেন! গুটি নির্বাচন করুন।"
                }

                _gameState.update {
                    it.copy(
                        players = updatedPlayers,
                        diceValue = finalRoll,
                        rollingDisplayValue = finalRoll,
                        isRolling = false,
                        consecutiveSixes = newSixes,
                        phase = GamePhase.SELECT_TOKEN,
                        statusMessageEn = msgEn,
                        statusMessageBn = msgBn
                    )
                }

                // Computer opponent (bot) automatically calculates and executes its move
                if (player.isBot) {
                    val chosenToken = selectBotMove(effectiveOwner, validTokens, finalRoll)
                    val moveDelay = if (state.fastSpeed) 350L else 700L
                    delay(moveDelay)
                    if (_gameState.value.phase == GamePhase.SELECT_TOKEN) {
                        moveToken(effectiveOwner.id, chosenToken.id, finalRoll)
                    }
                } else {
                    // Across all Ludo modes (Offline, Online, Tournament, 2v2, Quick, Open),
                    // auto-execute the move when the active local player has only ONE forced/mandatory move.
                    val isLocalHumanTurn = state.mode != GameMode.ONLINE_FRIENDS ||
                            state.onlineRoomCode == null ||
                            player.id == state.onlineLocalPlayerId
                    val forcedToken = if (isLocalHumanTurn) resolveSingleForcedToken(validTokens) else null
                    if (forcedToken != null) {
                        val autoMoveDelay = if (state.fastSpeed) 180L else 300L
                        delay(autoMoveDelay)
                        if (_gameState.value.phase == GamePhase.SELECT_TOKEN) {
                            moveToken(effectiveOwner.id, forcedToken.id, finalRoll)
                        }
                    }
                }
            }
        } finally {
            isRollingAtomic.set(false)
            _gameState.update { current ->
                if (current.isRolling) current.copy(isRolling = false) else current
            }
        }
    }
}

    /**
     * Resolves which player's tokens are to be moved.
     * In 2v2 mode: If a player's own 4 tokens have already entered home,
     * they roll to move their teammate's tokens!
     */
    private fun getMovableTokensForTurn(player: Player, roll: Int): Pair<Player, List<LudoToken>> {
        val state = _gameState.value
        if (state.isTeamMode() && player.isFinished) {
            val teammate = state.players.find { state.isTeammate(it.id, player.id) && it.id != player.id }
            if (teammate != null) {
                return Pair(teammate, getMovableTokens(teammate, roll))
            }
        }
        return Pair(player, getMovableTokens(player, roll))
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
        if (state.phase != GamePhase.SELECT_TOKEN || state.isRolling) return
        val currentTurnPlayer = state.currentPlayer ?: return
        if (state.mode == GameMode.ONLINE_FRIENDS && state.onlineRoomCode != null && currentTurnPlayer.id != state.onlineLocalPlayerId) return

        // In team mode, can click own token OR teammate's token if current turn player is finished
        val isAllowedPlayer = (playerId == currentTurnPlayer.id) ||
                (state.isTeamMode() && currentTurnPlayer.isFinished && state.isTeammate(playerId, currentTurnPlayer.id))

        if (!isAllowedPlayer) return

        val targetPlayer = state.players.find { it.id == playerId } ?: return
        val token = targetPlayer.tokens.find { it.id == tokenId } ?: return
        if (!token.isHighlighted) return

        val currentRoll = state.diceValue
        if (currentRoll !in 1..6) return

        // Cancel any pending auto-move delay immediately
        rollAnimationJob?.cancel()

        moveToken(playerId, tokenId, currentRoll)
    }

    private fun moveToken(tokenOwnerId: Int, tokenId: Int, stepsToMove: Int = _gameState.value.diceValue) {
        val state = _gameState.value
        if (state.phase != GamePhase.SELECT_TOKEN) return
        val tokenOwner = state.players.find { it.id == tokenOwnerId } ?: return
        val currentTurnPlayer = state.currentPlayer ?: tokenOwner
        val token = tokenOwner.tokens.find { it.id == tokenId } ?: return

        if (!isMovingAtomic.compareAndSet(false, true)) return

        // Cancel any pending auto-move or roll job immediately
        rollAnimationJob?.cancel()

        val exactSteps = if (stepsToMove in 1..6) stepsToMove else state.diceValue.coerceIn(1, 6)

        // Atomically lock phase to TOKEN_MOVING to prevent duplicate taps or race conditions
        _gameState.update { s ->
            s.copy(
                phase = GamePhase.TOKEN_MOVING,
                diceValue = exactSteps,
                rollingDisplayValue = exactSteps,
                players = s.players.map { p ->
                    p.copy(tokens = p.tokens.map { it.copy(isHighlighted = false) })
                }
            )
        }

        moveJob?.cancel()
        moveJob = viewModelScope.launch {
            try {
                val startStep = token.step
                val targetStep = if (token.isInYard) 0 else (token.step + exactSteps).coerceAtMost(BoardCoordinates.STEP_GOAL)

                // Step-by-step token movement strictly matching exactSteps at smooth 60 FPS
                val stepDelay = if (state.fastSpeed) 45L else 85L
                if (token.isInYard) {
                    soundManager.playTokenMove()
                    updateSingleTokenStep(tokenOwnerId, tokenId, 0)
                    delay(stepDelay * 2)
                } else {
                    for (s in (startStep + 1)..targetStep) {
                        soundManager.playTokenMove()
                        updateSingleTokenStep(tokenOwnerId, tokenId, s)
                        delay(stepDelay)
                    }
                }

                // Clear isMoving state across all tokens upon completing path traversal
                _gameState.update { s ->
                    s.copy(players = s.players.map { p ->
                        p.copy(tokens = p.tokens.map { it.copy(isMoving = false) })
                    })
                }

                // Movement completed! Check outcomes:
                val landedInGoal = targetStep == BoardCoordinates.STEP_GOAL
            if (landedInGoal) {
                soundManager.playTokenEnterGoal()
            }

            // Check Double Token Protection / Block Rule and capturing
            var capturedAny = false
            var blockDefended = false

            if (targetStep in 0..50) {
                val landingTrackIndex = (tokenOwner.color.startTrackIndex + targetStep) % 52
                val isSafe = BoardCoordinates.SAFE_TRACK_INDICES.contains(landingTrackIndex)

                if (!isSafe) {
                    val currentPlayers = _gameState.value.players

                    // Attacking tokens count on this tile (including the moving token)
                    val attackingTokensCount = currentPlayers
                        .filter { p ->
                            p.id == tokenOwnerId || (state.isTeamMode() && state.isTeammate(tokenOwnerId, p.id))
                        }
                        .flatMap { it.tokens }
                        .count { it.isOnBoard && it.step in 0..50 && ((it.color.startTrackIndex + it.step) % 52 == landingTrackIndex) }

                    // Opponent tokens on this tile
                    val opponentTokensOnTile = currentPlayers
                        .filter { otherPlayer ->
                            val isTeammate = state.isTeamMode() && state.isTeammate(tokenOwnerId, otherPlayer.id)
                            otherPlayer.id != tokenOwnerId && !isTeammate
                        }
                        .flatMap { otherPlayer ->
                            otherPlayer.tokens
                                .filter { it.isOnBoard && it.step in 0..50 && ((otherPlayer.color.startTrackIndex + it.step) % 52 == landingTrackIndex) }
                                .map { otherPlayer to it }
                        }

                    val opponentCount = opponentTokensOnTile.size

                    if (opponentCount > 0) {
                        // Official Ludo Double-Token Rule:
                        // When two tokens of the same player (or team) stand on the same tile, they form a Block/Double Guard.
                        // A single opponent token CANNOT capture a double-token block.
                        // To capture two stacked tokens, the opponent MUST also move two stacked tokens onto that exact square.
                        if (opponentCount >= 2 && attackingTokensCount < 2) {
                            // Block protected! Single attacking token cannot capture the double block.
                            blockDefended = true
                            soundManager.playBlockShield()
                        } else {
                            // Either opponent only had 1 token, OR attacker brought 2 stacked tokens to break the block!
                            val victims = opponentTokensOnTile
                            if (victims.isNotEmpty()) {
                                capturedAny = true
                                soundManager.playTokenCapture()

                                val victimNames = victims.map { it.first.name }.distinct().joinToString(" & ")
                                val captureMsgEn = "💥 ${tokenOwner.name} captured $victimNames's pawn!"
                                val captureMsgBn = "💥 ${tokenOwner.name} $victimNames-এর গুটি কেটে দিয়েছেন!"

                                _gameState.update {
                                    it.copy(
                                        statusMessageEn = captureMsgEn,
                                        statusMessageBn = captureMsgBn,
                                        lastCapturedToken = victims.first().second
                                    )
                                }

                                // Mark captured tokens with isReturning = true for retreat animation
                                _gameState.update { s ->
                                    s.copy(players = s.players.map { p ->
                                        p.copy(tokens = p.tokens.map { t ->
                                            if (victims.any { v -> v.first.id == p.id && v.second.id == t.id }) {
                                                t.copy(isReturning = true)
                                            } else t
                                        })
                                    })
                                }

                                // Pause briefly for the knockout impact sound and visual to register
                                delay(if (state.fastSpeed) 140L else 220L)

                                // Reverse movement animation: step backwards cell-by-cell along path to start
                                val maxVictimStep = victims.maxOf { it.second.step }
                                val revDelay = if (state.fastSpeed) {
                                    if (maxVictimStep > 25) 24L else 38L
                                } else {
                                    if (maxVictimStep > 25) 34L else 52L
                                }

                                for (revStep in (maxVictimStep - 1) downTo 0) {
                                    if (revStep % 4 == 0) {
                                        soundManager.playTokenMove()
                                    }
                                    _gameState.update { s ->
                                        s.copy(players = s.players.map { p ->
                                            p.copy(tokens = p.tokens.map { t ->
                                                val isVictim = victims.any { v -> v.first.id == p.id && v.second.id == t.id }
                                                if (isVictim && revStep <= t.step) {
                                                    t.copy(step = revStep)
                                                } else t
                                            })
                                        })
                                    }
                                    delay(revDelay)
                                }

                                // Brief pause at start cell (step 0) before returning into home yard base
                                delay(if (state.fastSpeed) 70L else 120L)

                                // Enter home yard base and clear isReturning
                                _gameState.update { s ->
                                    s.copy(players = s.players.map { p ->
                                        p.copy(tokens = p.tokens.map { t ->
                                            if (victims.any { v -> v.first.id == p.id && v.second.id == t.id }) {
                                                t.copy(step = BoardCoordinates.STEP_YARD, isReturning = false)
                                            } else t
                                        })
                                    })
                                }
                                soundManager.playTokenMove()
                                saveStats(_stats.value.copy(totalCaptures = _stats.value.totalCaptures + victims.size))
                                val localPlayerId = getLocalPlayerId(_gameState.value)
                                if (tokenOwnerId == localPlayerId) {
                                    CoinManager.recordTokenCapture()
                                }
                                delay(if (state.fastSpeed) 100L else 180L)
                            }
                        }
                    } else if (attackingTokensCount >= 2) {
                        // Friendly Double Guard / Block formed!
                        soundManager.playBlockShield()
                    }
                }
            }

            // TEAM PLAY WINNING CHECK:
            if (state.isTeamMode()) {
                val team1Finished = _gameState.value.isTeamAFinished
                val team2Finished = _gameState.value.isTeamBFinished

                if (team1Finished || team2Finished) {
                    val winningTeam = if (team1Finished) "TEAM 1" else "TEAM 2"
                    val winners = if (team1Finished) _gameState.value.teamAPlayers else _gameState.value.teamBPlayers
                    val winMsgEn = "🎉 $winningTeam WINS! (${winners.joinToString(" & ") { it.name }}) 🎉"
                    val winMsgBn = "🎉 $winningTeam জয়লাভ করেছে! (${winners.joinToString(" ও ") { it.name }}) 🎉"

                    val localPlayerId = getLocalPlayerId(_gameState.value)
                    val userWon = winners.any { it.id == localPlayerId }
                    if (userWon) {
                        soundManager.playVictoryFanfare()
                        val poolAward = _gameState.value.totalCoinPool.takeIf { it > 0 } ?: _gameState.value.tournamentNetPrize
                        CoinManager.recordMatchVictory(poolAward)
                    } else {
                        soundManager.playDefeatSound()
                    }

                    if (team1Finished) {
                        saveStats(_stats.value.copy(teamAWins = _stats.value.teamAWins + 1, totalWins = if (userWon) _stats.value.totalWins + 1 else _stats.value.totalWins))
                    } else {
                        saveStats(_stats.value.copy(teamBWins = _stats.value.teamBWins + 1, totalWins = if (userWon) _stats.value.totalWins + 1 else _stats.value.totalWins))
                    }

                    _gameState.update {
                        it.copy(
                            phase = GamePhase.GAME_OVER,
                            winningTeamName = winningTeam,
                            winnerRanking = winners,
                            statusMessageEn = winMsgEn,
                            statusMessageBn = winMsgBn
                        )
                    }
                    syncOnlineTokenMoveIfNeeded(state, tokenOwnerId, tokenId, targetStep)
                    return@launch
                }
            } else {
                // SOLO 4-PLAYER MODE, TOURNAMENT & QUICK LUDO WINNING CHECK
                val updatedCurrentPlayer = _gameState.value.players.find { it.id == tokenOwnerId } ?: return@launch
                val targetTokens = state.targetTokensToWin
                val isSingleTokenWinMode = state.mode == GameMode.QUICK_LUDO || state.isQuickMode || targetTokens <= 1
                val isPlayerDone = if (isSingleTokenWinMode) {
                    updatedCurrentPlayer.finishedTokensCount >= 1
                } else {
                    updatedCurrentPlayer.finishedTokensCount >= targetTokens
                }
                val hasJustWon = isPlayerDone && _gameState.value.winnerRanking.none { it.id == updatedCurrentPlayer.id }

                var newRanking = _gameState.value.winnerRanking
                if (hasJustWon) {
                    newRanking = newRanking + updatedCurrentPlayer.copy(rank = newRanking.size + 1)
                    _gameState.update { s ->
                        s.copy(
                            winnerRanking = newRanking,
                            players = s.players.map { p -> if (p.id == tokenOwnerId) p.copy(rank = newRanking.size) else p }
                        )
                    }

                    val localPlayerId = getLocalPlayerId(_gameState.value)
                    val isLocalWinner = updatedCurrentPlayer.id == localPlayerId && newRanking.size == 1
                    val statsUpdate = when (updatedCurrentPlayer.color) {
                        LudoColor.RED -> _stats.value.copy(redWins = _stats.value.redWins + 1, totalWins = if (isLocalWinner) _stats.value.totalWins + 1 else _stats.value.totalWins)
                        LudoColor.GREEN -> _stats.value.copy(greenWins = _stats.value.greenWins + 1, totalWins = if (isLocalWinner) _stats.value.totalWins + 1 else _stats.value.totalWins)
                        LudoColor.YELLOW -> _stats.value.copy(yellowWins = _stats.value.yellowWins + 1, totalWins = if (isLocalWinner) _stats.value.totalWins + 1 else _stats.value.totalWins)
                        LudoColor.BLUE -> _stats.value.copy(blueWins = _stats.value.blueWins + 1, totalWins = if (isLocalWinner) _stats.value.totalWins + 1 else _stats.value.totalWins)
                    }
                    saveStats(statsUpdate)

                    if (updatedCurrentPlayer.id == localPlayerId && newRanking.size == 1) {
                        soundManager.playVictoryFanfare()
                        val poolAward = _gameState.value.totalCoinPool.takeIf { it > 0 } ?: _gameState.value.tournamentNetPrize
                        CoinManager.recordMatchVictory(poolAward)
                    } else if (newRanking.size == 1) {
                        // An opponent won 1st place
                        soundManager.playDefeatSound()
                    }
                }

                val activeRemainingPlayers = _gameState.value.players.filter { p ->
                    if (isSingleTokenWinMode) {
                        p.finishedTokensCount < 1
                    } else {
                        p.finishedTokensCount < targetTokens
                    }
                }
                val shouldEndGame = (isSingleTokenWinMode && hasJustWon) || (targetTokens < 4 && hasJustWon) || (activeRemainingPlayers.size <= 1)
                if (shouldEndGame) {
                    val finalRanking = if (activeRemainingPlayers.isNotEmpty()) {
                        newRanking + activeRemainingPlayers.first().copy(rank = newRanking.size + 1)
                    } else newRanking

                    val firstWinner = finalRanking.firstOrNull()?.name ?: "Player"
                    val winMsgEn = if (state.tournamentName != null) {
                        "Tournament Finished! $firstWinner is the Champion!"
                    } else {
                        "Game Over! $firstWinner is the Winner!"
                    }
                    val winMsgBn = if (state.tournamentName != null) {
                        "টুর্নামেন্ট সমাপ্ত! $firstWinner চ্যাম্পিয়ন হয়েছেন!"
                    } else {
                        "খেলা সমাপ্ত! $firstWinner বিজয়ী হয়েছেন!"
                    }

                    _gameState.update {
                        it.copy(
                            phase = GamePhase.GAME_OVER,
                            winnerRanking = finalRanking,
                            statusMessageEn = winMsgEn,
                            statusMessageBn = winMsgBn
                        )
                    }
                    syncOnlineTokenMoveIfNeeded(state, tokenOwnerId, tokenId, targetStep)
                    return@launch
                }
            }

            // Determine if bonus roll applies:
            val rolledSix = exactSteps == 6
            val grantBonusRoll = (rolledSix || capturedAny || landedInGoal)

            if (grantBonusRoll) {
                val bonusReasonEn = when {
                    capturedAny -> "Captured piece! Bonus Roll awarded!"
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
                        statusMessageEn = "${currentTurnPlayer.name}: $bonusReasonEn",
                        statusMessageBn = "${currentTurnPlayer.name}: $bonusReasonBn"
                    )
                }

                if (currentTurnPlayer.isBot) {
                    triggerBotTurn()
                }
            } else {
                nextTurn()
            }

                // Sync token move to real-time multiplayer relay
                syncOnlineTokenMoveIfNeeded(state, tokenOwnerId, tokenId, targetStep)
            } finally {
                isMovingAtomic.set(false)
                _gameState.update { current ->
                    if (current.phase == GamePhase.TOKEN_MOVING) {
                        current.copy(
                            phase = GamePhase.ROLL_DICE,
                            players = current.players.map { p ->
                                p.copy(tokens = p.tokens.map { it.copy(isMoving = false, isReturning = false) })
                            }
                        )
                    } else {
                        current
                    }
                }
            }
        }
    }

    /**
     * Applies a remote player's dice roll received from real-time relay.
     */
    private fun applyRemoteDiceRoll(remotePlayerId: Int, roll: Int, turnIndex: Int) {
        val state = _gameState.value
        if (state.mode != GameMode.ONLINE_FRIENDS) return
        val player = state.players.find { it.id == remotePlayerId } ?: return

        soundManager.playDiceRoll()

        rollAnimationJob?.cancel()
        rollAnimationJob = viewModelScope.launch {
            _gameState.update { cur ->
                if (cur.players.isEmpty()) {
                    cur
                } else {
                    cur.copy(isRolling = true, currentTurnIndex = turnIndex.coerceIn(0, cur.players.size - 1))
                }
            }

            val rollTicks = if (state.fastSpeed) 10 else 14
            var tickDelay = 35L
            val otherFaces = (1..6).filter { it != roll }.shuffled()
            val facesSequence = otherFaces + listOf(roll) + (1..6).shuffled()
            for (step in 0 until (rollTicks - 1)) {
                val tempDice = facesSequence.getOrElse(step) { (step % 6) + 1 }
                _gameState.update { it.copy(diceValue = tempDice, rollingDisplayValue = tempDice) }
                delay(tickDelay)
                tickDelay += 4L
            }
            _gameState.update { it.copy(diceValue = roll, rollingDisplayValue = roll, isRolling = false) }

            val isSix = roll == 6
            val newSixes = if (isSix) state.consecutiveSixes + 1 else 0

            if (isSix) {
                soundManager.playSixRolled()
            }

            val (effectiveOwner, validTokens) = getMovableTokensForTurn(player, roll)

            if (validTokens.isEmpty()) {
                val msgEn = "${player.name} rolled $roll. No valid moves."
                val msgBn = "${player.name} $roll পেয়েছেন। কোনো চাল নেই।"
                _gameState.update {
                    it.copy(
                        diceValue = roll,
                        isRolling = false,
                        consecutiveSixes = newSixes,
                        statusMessageEn = msgEn,
                        statusMessageBn = msgBn
                    )
                }
                delay(if (state.fastSpeed) 400L else 750L)
                if (isSix) {
                    _gameState.update {
                        it.copy(
                            phase = GamePhase.ROLL_DICE,
                            statusMessageEn = "${player.name} rolled 6! Roll again.",
                            statusMessageBn = "${player.name} ৬ পেয়েছেন! আবার চালুন।"
                        )
                    }
                    startTurnWatchdog()
                } else {
                    nextTurn()
                }
            } else {
                val updatedPlayers = state.players.map { p ->
                    if (p.id == effectiveOwner.id) {
                        p.copy(tokens = p.tokens.map { token ->
                            token.copy(isHighlighted = validTokens.any { it.id == token.id })
                        })
                    } else {
                        p.copy(tokens = p.tokens.map { it.copy(isHighlighted = false) })
                    }
                }

                val msgEn = if (isSix) "${player.name} rolled 6! Selecting token..." else "${player.name} rolled $roll! Selecting token..."
                val msgBn = if (isSix) "${player.name} ৬ পেয়েছেন! গুটি চালাচ্ছেন..." else "${player.name} $roll পেয়েছেন! গুটি চালাচ্ছেন..."

                _gameState.update {
                    it.copy(
                        players = updatedPlayers,
                        diceValue = roll,
                        isRolling = false,
                        consecutiveSixes = newSixes,
                        phase = GamePhase.SELECT_TOKEN,
                        statusMessageEn = msgEn,
                        statusMessageBn = msgBn
                    )
                }
                startTurnWatchdog()
            }
        }
    }

    /**
     * Applies a remote player's token movement with deterministic board state sync.
     */
    private fun applyRemoteTokenMove(
        remotePlayerId: Int,
        tokenId: Int,
        targetStep: Int,
        nextTurnIndex: Int,
        tokensMatrix: Map<Int, List<Int>>?
    ) {
        val state = _gameState.value
        if (state.mode != GameMode.ONLINE_FRIENDS) return
        val player = state.players.find { it.id == remotePlayerId } ?: return
        val token = player.tokens.find { it.id == tokenId } ?: return

        moveJob?.cancel()
        isMovingAtomic.set(true)
        moveJob = viewModelScope.launch {
            try {
                // Un-highlight all tokens
            _gameState.update { s ->
                s.copy(
                    phase = GamePhase.TOKEN_MOVING,
                    players = s.players.map { p ->
                        p.copy(tokens = p.tokens.map { it.copy(isHighlighted = false) })
                    }
                )
            }

            val startStep = token.step
            val stepDelay = if (state.fastSpeed) 35L else 75L
            if (token.isInYard) {
                soundManager.playTokenMove()
                updateSingleTokenStep(remotePlayerId, tokenId, 0)
                delay(stepDelay * 2)
            } else if (startStep < targetStep) {
                for (s in (startStep + 1)..targetStep) {
                    soundManager.playTokenMove()
                    updateSingleTokenStep(remotePlayerId, tokenId, s)
                    delay(stepDelay)
                }
            } else {
                updateSingleTokenStep(remotePlayerId, tokenId, targetStep)
            }

            // Check goal landing
            if (targetStep == BoardCoordinates.STEP_GOAL) {
                soundManager.playTokenEnterGoal()
            }

            // Check captures
            if (targetStep in 0..50) {
                val landingTrackIndex = (player.color.startTrackIndex + targetStep) % 52
                val isSafe = BoardCoordinates.SAFE_TRACK_INDICES.contains(landingTrackIndex)
                if (!isSafe) {
                    val currentPlayers = _gameState.value.players
                    val victims = mutableListOf<Pair<Player, LudoToken>>()
                    currentPlayers.filter { it.id != remotePlayerId }.forEach { opp ->
                        opp.tokens.forEach { oppToken ->
                            if (oppToken.isOnBoard && oppToken.step in 0..50) {
                                val oppTrackIndex = (opp.color.startTrackIndex + oppToken.step) % 52
                                if (oppTrackIndex == landingTrackIndex) {
                                    victims.add(Pair(opp, oppToken))
                                }
                            }
                        }
                    }
                    if (victims.isNotEmpty()) {
                        soundManager.playTokenCapture()
                        victims.forEach { (vicPlayer, vicToken) ->
                            updateSingleTokenStep(vicPlayer.id, vicToken.id, BoardCoordinates.STEP_YARD)
                        }
                    }
                }
            }

            // Reconcile board token matrix if provided
            if (tokensMatrix != null) {
                _gameState.update { s ->
                    s.copy(players = s.players.map { p ->
                        val remoteSteps = tokensMatrix[p.id]
                        if (remoteSteps != null && remoteSteps.size == 4) {
                            p.copy(tokens = p.tokens.mapIndexed { idx, t ->
                                t.copy(step = remoteSteps[idx])
                            })
                        } else p
                    })
                }
            }

            // Check if player or team finished and determine game completion
            val currentLatest = _gameState.value
            val localPlayerId = getLocalPlayerId(currentLatest)
            if (currentLatest.isTeamMode()) {
                val team1Finished = currentLatest.isTeamAFinished
                val team2Finished = currentLatest.isTeamBFinished
                if (team1Finished || team2Finished) {
                    val winningTeam = if (team1Finished) "TEAM 1" else "TEAM 2"
                    val winners = if (team1Finished) currentLatest.teamAPlayers else currentLatest.teamBPlayers
                    val userWon = winners.any { it.id == localPlayerId }
                    if (userWon) {
                        soundManager.playVictoryFanfare()
                        val poolAward = currentLatest.totalCoinPool.takeIf { it > 0 } ?: currentLatest.tournamentNetPrize
                        CoinManager.recordMatchVictory(poolAward)
                    } else {
                        soundManager.playDefeatSound()
                    }
                    _gameState.update {
                        it.copy(
                            phase = GamePhase.GAME_OVER,
                            winningTeamName = winningTeam,
                            winnerRanking = winners,
                            statusMessageEn = "🎉 $winningTeam WINS! (${winners.joinToString(" & ") { p -> p.name }}) 🎉",
                            statusMessageBn = "🎉 $winningTeam জয়লাভ করেছে! (${winners.joinToString(" ও ") { p -> p.name }}) 🎉"
                        )
                    }
                    turnWatchdogJob?.cancel()
                    return@launch
                }
            } else {
                val updatedRemotePlayer = currentLatest.players.find { it.id == remotePlayerId }
                val targetTokens = currentLatest.targetTokensToWin
                val isSingleTokenWinMode = currentLatest.isQuickMode || targetTokens <= 1
                val remoteDone = updatedRemotePlayer != null && if (isSingleTokenWinMode) {
                    updatedRemotePlayer.finishedTokensCount >= 1
                } else {
                    updatedRemotePlayer.finishedTokensCount >= targetTokens
                }
                var newRanking = currentLatest.winnerRanking
                val newlyRanked = updatedRemotePlayer != null && remoteDone && newRanking.none { it.id == updatedRemotePlayer.id }
                if (newlyRanked && updatedRemotePlayer != null) {
                    newRanking = newRanking + updatedRemotePlayer.copy(rank = newRanking.size + 1)
                    _gameState.update { s ->
                        s.copy(
                            winnerRanking = newRanking,
                            players = s.players.map { p ->
                                if (p.id == remotePlayerId) p.copy(rank = newRanking.size) else p
                            }
                        )
                    }
                    if (newRanking.size == 1 && remotePlayerId != localPlayerId) {
                        soundManager.playDefeatSound()
                    }
                }
                val activeRemaining = _gameState.value.players.filter { p ->
                    if (isSingleTokenWinMode) p.finishedTokensCount < 1 else p.finishedTokensCount < targetTokens
                }
                val shouldEndGame = (isSingleTokenWinMode && newlyRanked) || (targetTokens < 4 && newlyRanked) || (activeRemaining.size <= 1)
                if (shouldEndGame) {
                    val finalRanking = if (activeRemaining.isNotEmpty()) {
                        newRanking + activeRemaining.first().copy(rank = newRanking.size + 1)
                    } else newRanking
                    val winner = finalRanking.firstOrNull()?.name ?: "Player"
                    _gameState.update {
                        it.copy(
                            phase = GamePhase.GAME_OVER,
                            winnerRanking = finalRanking,
                            statusMessageEn = "Game Over! $winner is the Winner!",
                            statusMessageBn = "খেলা সমাপ্ত! $winner বিজয়ী হয়েছেন!"
                        )
                    }
                    turnWatchdogJob?.cancel()
                    return@launch
                }
            }

            // Set turn index deterministically
            if (!_gameState.value.isGameOver) {
                if (nextTurnIndex in _gameState.value.players.indices) {
                    val nextP = _gameState.value.players[nextTurnIndex]
                    val turnEn = "${nextP.name}'s turn! Roll the dice."
                    val turnBn = "${nextP.name}-এর চাল! ছক্কা চালুন।"
                    _gameState.update {
                        it.copy(
                            currentTurnIndex = nextTurnIndex,
                            phase = GamePhase.ROLL_DICE,
                            consecutiveSixes = 0,
                            statusMessageEn = turnEn,
                            statusMessageBn = turnBn
                        )
                    }
                } else {
                    nextTurn()
                }
                startTurnWatchdog()
            }
            } finally {
                isMovingAtomic.set(false)
            }
        }
    }

    /**
     * Anti-Freeze Watchdog:
     * Monitors active turns in Online Multiplayer matches to guarantee games NEVER get permanently stuck:
     * 1. After 12 seconds: If remote turn has had no activity, queries the room for a sync snapshot.
     * 2. After 22 seconds: If a turn has still not progressed, auto-progresses the turn so play continues.
     */
    private fun startTurnWatchdog() {
        val state = _gameState.value
        if (state.mode != GameMode.ONLINE_FRIENDS || state.onlineRoomCode == null || state.phase == GamePhase.GAME_OVER) {
            turnWatchdogJob?.cancel()
            return
        }

        turnWatchdogJob?.cancel()
        val roomCode = state.onlineRoomCode
        val currentTurn = state.currentTurnIndex
        val isLocalTurn = currentTurn == state.onlineLocalPlayerId

        turnWatchdogJob = viewModelScope.launch {
            delay(12000)
            if (_gameState.value.currentTurnIndex == currentTurn && _gameState.value.mode == GameMode.ONLINE_FRIENDS && _gameState.value.phase != GamePhase.GAME_OVER) {
                if (!isLocalTurn) {
                    OnlineMultiplayerManager.requestSync(roomCode)
                }
            }

            delay(10000)
            val currentState = _gameState.value
            if (currentState.currentTurnIndex == currentTurn && currentState.mode == GameMode.ONLINE_FRIENDS && currentState.phase != GamePhase.GAME_OVER) {
                val player = currentState.currentPlayer ?: return@launch
                Log.d("LudoViewModel", "Turn timeout safeguard triggered for player ${player.name} (id: ${player.id})")

                if (isLocalTurn) {
                    // Do NOT auto-roll or auto-move for the local player!
                    // Wait patiently for the user to manually tap the dice or valid token.
                    return@launch
                } else {
                    // Remote player timed out or disconnected - auto-progress turn locally
                    if (currentState.phase == GamePhase.ROLL_DICE && !currentState.isRolling) {
                        val autoRoll = generateFairDiceRoll(player, currentState.consecutiveSixes)
                        applyRemoteDiceRoll(player.id, autoRoll, currentTurn)
                        OnlineMultiplayerManager.syncDiceRoll(roomCode, player.id, autoRoll, 0, currentTurn)
                    } else if (currentState.phase == GamePhase.SELECT_TOKEN) {
                        val (owner, movable) = getMovableTokensForTurn(player, currentState.diceValue)
                        if (movable.isNotEmpty()) {
                            val chosen = selectBotMove(owner, movable, currentState.diceValue)
                            val target = if (chosen.isInYard) 0 else chosen.step + currentState.diceValue
                            val nextTurnIdx = (currentTurn + 1) % currentState.players.size
                            applyRemoteTokenMove(owner.id, chosen.id, target, nextTurnIdx, null)
                        } else {
                            nextTurn()
                            val matrix = _gameState.value.players.associate { it.id to it.tokens.map { t -> t.step } }
                            OnlineMultiplayerManager.syncTurnPass(roomCode, player.id, _gameState.value.currentTurnIndex, matrix)
                            startTurnWatchdog()
                        }
                    }
                }
            }
        }
    }

    /**
     * Manual Re-sync button action for players to immediately refresh game state if ever needed.
     */
    fun resyncOnlineGame() {
        val state = _gameState.value
        val roomCode = state.onlineRoomCode ?: return
        if (state.mode == GameMode.ONLINE_FRIENDS) {
            OnlineMultiplayerManager.requestSync(roomCode)
            val matrix = state.players.associate { it.id to it.tokens.map { t -> t.step } }
            OnlineMultiplayerManager.sendSyncState(
                roomCode = roomCode,
                turnIndex = state.currentTurnIndex,
                phase = state.phase.name,
                diceValue = state.diceValue,
                tokensMatrix = matrix
            )
        }
    }

    private fun updateSingleTokenStep(playerId: Int, tokenId: Int, newStep: Int) {
        _gameState.update { state ->
            state.copy(
                players = state.players.map { p ->
                    if (p.id == playerId) {
                        p.copy(tokens = p.tokens.map { t ->
                            if (t.id == tokenId) t.copy(step = newStep, isMoving = true) else t.copy(isMoving = false)
                        })
                    } else {
                        p.copy(tokens = p.tokens.map { it.copy(isMoving = false) })
                    }
                }
            )
        }
    }

    private fun nextTurn() {
        val state = _gameState.value
        val numPlayers = state.players.size
        if (numPlayers == 0) return
        var nextIndex = (state.currentTurnIndex + 1) % numPlayers

        // In solo mode, skip players who have finished.
        // In team mode, players can still play to move their partner's tokens unless their whole team is finished.
        var attempts = 0
        while (attempts < numPlayers) {
            val candidate = state.players.getOrNull(nextIndex) ?: break
            val shouldSkip = if (state.isTeamMode()) {
                val isTeamA = nextIndex == 0 || nextIndex == 2
                if (isTeamA) state.isTeamAFinished else state.isTeamBFinished
            } else {
                state.isPlayerDone(candidate)
            }

            if (!shouldSkip) break
            nextIndex = (nextIndex + 1) % numPlayers
            attempts++
        }

        val nextPlayer = state.players.getOrNull(nextIndex) ?: return
        val teamSuffix = if (state.isTeamMode()) " (${state.getTeamName(nextPlayer.id)})" else ""
        val turnEn = "${nextPlayer.name}$teamSuffix's Turn - Roll the dice!"
        val turnBn = "${nextPlayer.name}$teamSuffix এর চাল - ছক্কা চালুন!"

        _gameState.update {
            it.copy(
                currentTurnIndex = nextIndex,
                phase = GamePhase.ROLL_DICE,
                consecutiveSixes = 0,
                statusMessageEn = turnEn,
                statusMessageBn = turnBn
            )
        }

        botJob?.cancel()
        botMoveJob?.cancel()

        if (nextPlayer.isBot) {
            triggerBotTurn()
        } else if (state.mode == GameMode.ONLINE_FRIENDS && state.onlineRoomCode != null) {
            startTurnWatchdog()
        }
    }

    private fun triggerBotTurn() {
        val player = _gameState.value.currentPlayer
        if (player == null || !player.isBot) return
        botJob?.cancel()
        botJob = viewModelScope.launch {
            // Natural thinking delay for computer opponent before rolling
            val waitDelay = if (_gameState.value.fastSpeed) 450L else 900L
            delay(waitDelay)
            val curr = _gameState.value.currentPlayer
            if (curr != null && curr.isBot && _gameState.value.phase == GamePhase.ROLL_DICE && !_gameState.value.isRolling) {
                rollDice()
            }
        }
    }

    private fun triggerBotTokenMove() {
        botMoveJob?.cancel()
    }

    /**
     * Smart Bot AI evaluation function.
     * In team mode: bot never targets teammate tokens and protects partner!
     */
    private fun selectBotMove(botPlayer: Player, validTokens: List<LudoToken>, roll: Int): LudoToken {
        if (validTokens.size == 1) return validTokens.first()

        val state = _gameState.value
        // Opponents only (exclude self and teammate in team mode)
        val opponentPlayers = state.players.filter {
            it.id != botPlayer.id && !(state.isTeamMode() && state.isTeammate(it.id, botPlayer.id))
        }

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

            // 3. Can capture opponent piece (never teammates!) - 22% capture preference
            if (targetStep in 0..50) {
                val targetTrackIndex = (botPlayer.color.startTrackIndex + targetStep) % 52
                val isSafe = BoardCoordinates.SAFE_TRACK_INDICES.contains(targetTrackIndex)

                if (!isSafe) {
                    val canCapture = opponentPlayers.any { other ->
                        other.tokens.any { oppToken ->
                            oppToken.isOnBoard && oppToken.step in 0..50 &&
                                    (other.color.startTrackIndex + oppToken.step) % 52 == targetTrackIndex
                        }
                    }
                    if (canCapture) {
                        // 22% capture preference for balanced pacing
                        val isAggressive = secureRandom.nextInt(100) < 22
                        score += if (isAggressive) 700 else 300
                    }
                }

                // 4. Land on safe spot
                if (isSafe) {
                    score += 160
                }
            }

            // 5. Enter home column
            if (targetStep >= 51 && currentStep < 51) {
                score += 260
            }

            // 6. Escape opponent threat if currently in danger on non-safe square
            if (currentStep in 0..50) {
                val currentTrackIndex = (botPlayer.color.startTrackIndex + currentStep) % 52
                val currentlyInDanger = !BoardCoordinates.SAFE_TRACK_INDICES.contains(currentTrackIndex) &&
                        opponentPlayers.any { other ->
                            other.tokens.any { oppToken ->
                                if (oppToken.isOnBoard && oppToken.step in 0..50) {
                                    val oppTrack = (other.color.startTrackIndex + oppToken.step) % 52
                                    val dist = (currentTrackIndex - oppTrack + 52) % 52
                                    dist in 1..6
                                } else false
                            }
                        }
                if (currentlyInDanger) {
                    score += 220
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

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
        soundManager.release()
        OnlineMultiplayerManager.detachListeners()
    }

    companion object {
        /**
         * Returns the forced token when a player has only ONE valid/mandatory move:
         * - Literally one movable token (`validTokens.size == 1`), OR
         * - Multiple movable tokens at the exact same board step (e.g. all in yard on 6, or stacked together
         *   with no other distinct movable token), where every choice produces the identical move.
         */
        internal fun resolveSingleForcedToken(validTokens: List<LudoToken>): LudoToken? {
            if (validTokens.isEmpty()) return null
            if (validTokens.size == 1) return validTokens.first()
            return if (validTokens.distinctBy { it.step }.size == 1) validTokens.first() else null
        }
    }
}
