package com.example.model

data class LudoToken(
    val id: Int, // 0..3
    val color: LudoColor,
    val step: Int = BoardCoordinates.STEP_YARD, // -1 is yard, 0..50 track, 51..55 home column, 56 goal
    val isHighlighted: Boolean = false,
    val isMoving: Boolean = false
) {
    val isInYard: Boolean get() = step == BoardCoordinates.STEP_YARD
    val isFinished: Boolean get() = step == BoardCoordinates.STEP_GOAL
    val isOnBoard: Boolean get() = step in 0..55
    val isInHomeColumn: Boolean get() = step in 51..55
}

data class Player(
    val id: Int,
    val name: String,
    val color: LudoColor,
    val isBot: Boolean = false,
    val tokens: List<LudoToken> = List(4) { index -> LudoToken(id = index, color = color) },
    val rank: Int = 0 // 0 = playing, 1 = 1st place, 2 = 2nd, etc.
) {
    val isFinished: Boolean get() = tokens.all { it.isFinished }
    val finishedTokensCount: Int get() = tokens.count { it.isFinished }
}

enum class GamePhase {
    ROLL_DICE,
    SELECT_TOKEN,
    TOKEN_MOVING,
    GAME_OVER
}

enum class GameMode {
    VS_BOT,
    PASS_AND_PLAY
}

data class GameState(
    val mode: GameMode = GameMode.VS_BOT,
    val players: List<Player> = emptyList(),
    val currentTurnIndex: Int = 0,
    val diceValue: Int = 1,
    val isRolling: Boolean = false,
    val phase: GamePhase = GamePhase.ROLL_DICE,
    val consecutiveSixes: Int = 0,
    val winnerRanking: List<Player> = emptyList(),
    val statusMessageEn: String = "Roll the dice to start!",
    val statusMessageBn: String = "খেলা শুরু করতে ছক্কা চালুন!",
    val lastCapturedToken: LudoToken? = null,
    val isBengali: Boolean = false,
    val soundEnabled: Boolean = true,
    val fastSpeed: Boolean = false,
    val activeMovingToken: LudoToken? = null,
    val movingPath: List<GridPos> = emptyList(),
    val currentMovingIndex: Int = 0
) {
    val currentPlayer: Player?
        get() = if (players.isNotEmpty() && currentTurnIndex in players.indices) players[currentTurnIndex] else null

    val isGameOver: Boolean
        get() = phase == GamePhase.GAME_OVER || players.count { !it.isFinished } <= 1
}
