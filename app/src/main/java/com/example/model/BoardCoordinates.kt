package com.example.model

data class GridPos(val col: Int, val row: Int)

object BoardCoordinates {
    const val BOARD_SIZE = 15
    const val MAX_STEPS = 56 // 0..50 (51 track steps) + 51..55 (5 home steps) + 56 (Home Goal)
    const val STEP_YARD = -1
    const val STEP_GOAL = 56

    // 52 Common Track Coordinates (Clockwise starting from Red Start at col 1, row 6)
    val TRACK_COORDINATES: List<GridPos> = listOf(
        // 0..4: Red Start (col 1, row 6) moving right along row 6
        GridPos(1, 6),  // 0: Red Start (Safe)
        GridPos(2, 6),  // 1
        GridPos(3, 6),  // 2
        GridPos(4, 6),  // 3
        GridPos(5, 6),  // 4
        // 5..10: Turn Up into Column 6 moving up
        GridPos(6, 5),  // 5
        GridPos(6, 4),  // 6
        GridPos(6, 3),  // 7
        GridPos(6, 2),  // 8: Green Safe Star (☆)
        GridPos(6, 1),  // 9
        GridPos(6, 0),  // 10
        // 11..12: Top edge
        GridPos(7, 0),  // 11: Green Entry Arrow (↓)
        GridPos(8, 0),  // 12
        // 13..17: Green Start (col 8, row 1) moving down column 8
        GridPos(8, 1),  // 13: Green Start (Safe)
        GridPos(8, 2),  // 14
        GridPos(8, 3),  // 15
        GridPos(8, 4),  // 16
        GridPos(8, 5),  // 17
        // 18..23: Turn Right into Row 6 moving right
        GridPos(9, 6),  // 18
        GridPos(10, 6), // 19
        GridPos(11, 6), // 20
        GridPos(12, 6), // 21: Yellow Safe Star (☆)
        GridPos(13, 6), // 22
        GridPos(14, 6), // 23
        // 24..25: Right edge
        GridPos(14, 7), // 24: Yellow Entry Arrow (←)
        GridPos(14, 8), // 25
        // 26..30: Yellow Start (col 13, row 8) moving left along row 8
        GridPos(13, 8), // 26: Yellow Start (Safe)
        GridPos(12, 8), // 27
        GridPos(11, 8), // 28
        GridPos(10, 8), // 29
        GridPos(9, 8),  // 30
        // 31..36: Turn Down into Column 8 moving down
        GridPos(8, 9),  // 31
        GridPos(8, 10), // 32
        GridPos(8, 11), // 33
        GridPos(8, 12), // 34: Blue Safe Star (☆)
        GridPos(8, 13), // 35
        GridPos(8, 14), // 36
        // 37..38: Bottom edge
        GridPos(7, 14), // 37: Blue Entry Arrow (↑)
        GridPos(6, 14), // 38
        // 39..43: Blue Start (col 6, row 13) moving up column 6
        GridPos(6, 13), // 39: Blue Start (Safe)
        GridPos(6, 12), // 40
        GridPos(6, 11), // 41
        GridPos(6, 10), // 42
        GridPos(6, 9),  // 43
        // 44..49: Turn Left into Row 8 moving left
        GridPos(5, 8),  // 44
        GridPos(4, 8),  // 45
        GridPos(3, 8),  // 46
        GridPos(2, 8),  // 47: Red Safe Star (☆)
        GridPos(1, 8),  // 48
        GridPos(0, 8),  // 49
        // 50..51: Left edge
        GridPos(0, 7),  // 50: Red Entry Arrow (→)
        GridPos(0, 6)   // 51
    )

    // 8 Safe track indices on the common 52-step board
    val SAFE_TRACK_INDICES = setOf(0, 8, 13, 21, 26, 34, 39, 47)

    // 5 Home Column Steps for each color (steps 51..55)
    // Red Home Corridor: row 7, cols 1..5 moving right into center
    val RED_HOME_PATH: List<GridPos> = listOf(
        GridPos(1, 7), GridPos(2, 7), GridPos(3, 7), GridPos(4, 7), GridPos(5, 7)
    )
    // Green Home Corridor: col 7, rows 1..5 moving down into center
    val GREEN_HOME_PATH: List<GridPos> = listOf(
        GridPos(7, 1), GridPos(7, 2), GridPos(7, 3), GridPos(7, 4), GridPos(7, 5)
    )
    // Yellow Home Corridor: row 7, cols 13..9 moving left into center
    val YELLOW_HOME_PATH: List<GridPos> = listOf(
        GridPos(13, 7), GridPos(12, 7), GridPos(11, 7), GridPos(10, 7), GridPos(9, 7)
    )
    // Blue Home Corridor: col 7, rows 13..9 moving up into center
    val BLUE_HOME_PATH: List<GridPos> = listOf(
        GridPos(7, 13), GridPos(7, 12), GridPos(7, 11), GridPos(7, 10), GridPos(7, 9)
    )

    // Center Goal Coordinates (Col, Row)
    val RED_GOAL = GridPos(6, 7)
    val GREEN_GOAL = GridPos(7, 6)
    val YELLOW_GOAL = GridPos(8, 7)
    val BLUE_GOAL = GridPos(7, 8)
    val CENTER_GOAL = GridPos(7, 7)

    // 4 Yard Base Positions for each color
    // RED: Top-Left (Cols 0..5, Rows 0..5)
    val RED_YARD_BASES: List<GridPos> = listOf(
        GridPos(1, 1), GridPos(4, 1),
        GridPos(1, 4), GridPos(4, 4)
    )
    // GREEN: Top-Right (Cols 9..14, Rows 0..5)
    val GREEN_YARD_BASES: List<GridPos> = listOf(
        GridPos(10, 1), GridPos(13, 1),
        GridPos(10, 4), GridPos(13, 4)
    )
    // BLUE: Bottom-Left (Cols 0..5, Rows 9..14)
    val BLUE_YARD_BASES: List<GridPos> = listOf(
        GridPos(1, 10), GridPos(4, 10),
        GridPos(1, 13), GridPos(4, 13)
    )
    // YELLOW: Bottom-Right (Cols 9..14, Rows 9..14)
    val YELLOW_YARD_BASES: List<GridPos> = listOf(
        GridPos(10, 10), GridPos(13, 10),
        GridPos(10, 13), GridPos(13, 13)
    )

    fun getYardPositions(color: LudoColor): List<GridPos> = when (color) {
        LudoColor.RED -> RED_YARD_BASES
        LudoColor.GREEN -> GREEN_YARD_BASES
        LudoColor.YELLOW -> YELLOW_YARD_BASES
        LudoColor.BLUE -> BLUE_YARD_BASES
    }

    fun getHomeColumnPath(color: LudoColor): List<GridPos> = when (color) {
        LudoColor.RED -> RED_HOME_PATH
        LudoColor.GREEN -> GREEN_HOME_PATH
        LudoColor.YELLOW -> YELLOW_HOME_PATH
        LudoColor.BLUE -> BLUE_HOME_PATH
    }

    fun getGoalPosition(color: LudoColor): GridPos = when (color) {
        LudoColor.RED -> RED_GOAL
        LudoColor.GREEN -> GREEN_GOAL
        LudoColor.YELLOW -> YELLOW_GOAL
        LudoColor.BLUE -> BLUE_GOAL
    }

    /**
     * Converts a token's color, tokenIndex, and step (0..56 or -1) into grid coordinates.
     */
    fun getTokenGridPosition(color: LudoColor, tokenIndex: Int, step: Int): GridPos {
        if (step == STEP_YARD) {
            return getYardPositions(color).getOrElse(tokenIndex) { GridPos(0, 0) }
        }
        if (step == STEP_GOAL) {
            return getGoalPosition(color)
        }
        if (step in 0..50) {
            val trackIndex = (color.startTrackIndex + step) % 52
            return TRACK_COORDINATES[trackIndex]
        }
        if (step in 51..55) {
            val homeIndex = step - 51
            return getHomeColumnPath(color)[homeIndex]
        }
        return CENTER_GOAL
    }

    /**
     * Checks if a token at a given step is on a safe square.
     */
    fun isSafeSquare(color: LudoColor, step: Int): Boolean {
        if (step == STEP_YARD || step >= 51) return true // In yard, home path or goal is safe
        val trackIndex = (color.startTrackIndex + step) % 52
        return SAFE_TRACK_INDICES.contains(trackIndex)
    }
}
