package com.example

import com.example.model.BoardCoordinates
import com.example.model.LudoColor
import com.example.model.LudoToken
import com.example.ui.LudoViewModel
import com.example.ui.components.computeTokenCellScale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LudoAutoMoveAndTokenTest {

    @Test
    fun `token scale matches yard circle and fills single board cell while keeping stack split intact`() {
        val yardScale = computeTokenCellScale(
            isInYard = true,
            totalInCell = 1,
            isHighlighted = false,
            isMoving = false
        )
        // Yard socket diameter is 1.44f * cellSize; yard pawn scale closely matches it
        assertTrue(yardScale in 1.38f..1.44f)

        val singleIdle = computeTokenCellScale(
            isInYard = false,
            totalInCell = 1,
            isHighlighted = false,
            isMoving = false
        )
        val singleHighlight = computeTokenCellScale(
            isInYard = false,
            totalInCell = 1,
            isHighlighted = true,
            isMoving = false
        )
        val singleMoving = computeTokenCellScale(
            isInYard = false,
            totalInCell = 1,
            isHighlighted = false,
            isMoving = true
        )
        assertTrue(singleIdle >= 1.35f)
        assertTrue(singleHighlight > singleIdle)
        assertTrue(singleMoving >= singleHighlight)

        // Multi-token grouping / half-size split scales remain strictly intact
        assertEquals(0.72f, computeTokenCellScale(false, 2, isHighlighted = false, isMoving = false), 0.0001f)
        assertEquals(0.80f, computeTokenCellScale(false, 2, isHighlighted = true, isMoving = false), 0.0001f)
        assertEquals(0.62f, computeTokenCellScale(false, 3, isHighlighted = false, isMoving = false), 0.0001f)
        assertEquals(0.52f, computeTokenCellScale(false, 4, isHighlighted = false, isMoving = false), 0.0001f)
        assertEquals(0.46f, computeTokenCellScale(false, 5, isHighlighted = false, isMoving = false), 0.0001f)
    }

    @Test
    fun `resolveSingleForcedToken returns token when only one legal move exists`() {
        val single = listOf(LudoToken(id = 2, color = LudoColor.RED, step = 14))
        assertEquals(2, LudoViewModel.resolveSingleForcedToken(single)?.id)
    }

    @Test
    fun `resolveSingleForcedToken returns token when all valid tokens share exact same step`() {
        val allInYard = listOf(
            LudoToken(id = 0, color = LudoColor.GREEN, step = BoardCoordinates.STEP_YARD),
            LudoToken(id = 1, color = LudoColor.GREEN, step = BoardCoordinates.STEP_YARD),
            LudoToken(id = 2, color = LudoColor.GREEN, step = BoardCoordinates.STEP_YARD),
            LudoToken(id = 3, color = LudoColor.GREEN, step = BoardCoordinates.STEP_YARD)
        )
        assertEquals(0, LudoViewModel.resolveSingleForcedToken(allInYard)?.id)

        val stackedPair = listOf(
            LudoToken(id = 1, color = LudoColor.BLUE, step = 22),
            LudoToken(id = 3, color = LudoColor.BLUE, step = 22)
        )
        assertEquals(1, LudoViewModel.resolveSingleForcedToken(stackedPair)?.id)
    }

    @Test
    fun `resolveSingleForcedToken returns null when player has multiple distinct choices or no moves`() {
        assertNull(LudoViewModel.resolveSingleForcedToken(emptyList()))

        val distinctChoices = listOf(
            LudoToken(id = 0, color = LudoColor.YELLOW, step = BoardCoordinates.STEP_YARD),
            LudoToken(id = 1, color = LudoColor.YELLOW, step = 10)
        )
        assertNull(LudoViewModel.resolveSingleForcedToken(distinctChoices))

        val twoTrackPositions = listOf(
            LudoToken(id = 0, color = LudoColor.RED, step = 4),
            LudoToken(id = 2, color = LudoColor.RED, step = 19)
        )
        assertNull(LudoViewModel.resolveSingleForcedToken(twoTrackPositions))
    }
}
