package com.example

import com.example.engine.PlayerLevelManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigInteger

class PlayerLevelManagerTest {

    @Test
    fun testAllAZPiecewiseWinRequirementsFromLevel1To200() {
        // Level 1 to 30 (N = 1..29): +2 wins increment per level (3, 5, 7, ..., 59)
        assertEquals(BigInteger.valueOf(3), PlayerLevelManager.getWinsRequiredForLevel(1))
        assertEquals(BigInteger.valueOf(5), PlayerLevelManager.getWinsRequiredForLevel(2))
        assertEquals(BigInteger.valueOf(7), PlayerLevelManager.getWinsRequiredForLevel(3))
        assertEquals(BigInteger.valueOf(59), PlayerLevelManager.getWinsRequiredForLevel(29))

        // Level 30 to 50 (N = 30..49): +5 wins increment per level (64, 69, ..., 159)
        assertEquals(BigInteger.valueOf(64), PlayerLevelManager.getWinsRequiredForLevel(30))
        assertEquals(BigInteger.valueOf(69), PlayerLevelManager.getWinsRequiredForLevel(31))
        assertEquals(BigInteger.valueOf(159), PlayerLevelManager.getWinsRequiredForLevel(49))

        // Level 50 to 80 (N = 50..79): +7 wins increment per level (166, 173, ..., 369)
        assertEquals(BigInteger.valueOf(166), PlayerLevelManager.getWinsRequiredForLevel(50))
        assertEquals(BigInteger.valueOf(173), PlayerLevelManager.getWinsRequiredForLevel(51))
        assertEquals(BigInteger.valueOf(369), PlayerLevelManager.getWinsRequiredForLevel(79))

        // Level 80 to 120 (N = 80..119): +9 wins increment per level (378, 387, ..., 729)
        assertEquals(BigInteger.valueOf(378), PlayerLevelManager.getWinsRequiredForLevel(80))
        assertEquals(BigInteger.valueOf(387), PlayerLevelManager.getWinsRequiredForLevel(81))
        assertEquals(BigInteger.valueOf(729), PlayerLevelManager.getWinsRequiredForLevel(119))

        // Level 120 to 150 (N = 120..149): +10 wins increment per level ONLY (739, 749, ..., 1029)
        assertEquals(BigInteger.valueOf(739), PlayerLevelManager.getWinsRequiredForLevel(120))
        assertEquals(BigInteger.valueOf(749), PlayerLevelManager.getWinsRequiredForLevel(121))
        assertEquals(BigInteger.valueOf(1029), PlayerLevelManager.getWinsRequiredForLevel(149))

        // After Level 150 up to 200 (N = 150..199): start at 2 wins, then double progressively (2 -> 4 -> 8 -> ...)
        assertEquals(BigInteger.valueOf(2), PlayerLevelManager.getWinsRequiredForLevel(150))
        assertEquals(BigInteger.valueOf(4), PlayerLevelManager.getWinsRequiredForLevel(151))
        assertEquals(BigInteger.valueOf(8), PlayerLevelManager.getWinsRequiredForLevel(152))
        assertEquals(BigInteger.valueOf(16), PlayerLevelManager.getWinsRequiredForLevel(153))
        assertEquals(BigInteger.valueOf(2L).pow(50), PlayerLevelManager.getWinsRequiredForLevel(199))
    }

    @Test
    fun testLevelCalculationAndBoundariesAcrossAllTiers() {
        // Level 1: 0..2 wins
        assertEquals(1, PlayerLevelManager.calculateLevel(0L))
        assertEquals(1, PlayerLevelManager.calculateLevel(2L))

        // Level 2: 3..7 wins (needs 5 to reach 8)
        assertEquals(2, PlayerLevelManager.calculateLevel(3L))
        assertEquals(2, PlayerLevelManager.calculateLevel(7L))

        // Level 3: 8..14 wins (3 + 5 = 8; needs 7 to reach 15)
        assertEquals(3, PlayerLevelManager.calculateLevel(8L))
        assertEquals(3, PlayerLevelManager.calculateLevel(14L))

        // Level 4: 15+ wins (8 + 7 = 15)
        assertEquals(4, PlayerLevelManager.calculateLevel(15L))

        // Exact transition thresholds for Levels 30, 50, 80, 120, 150, 151, 152, 200
        for (targetLevel in listOf(30, 50, 80, 120, 150, 151, 152, 180, 200)) {
            val threshold = PlayerLevelManager.getCumulativeWinsToReachLevel(targetLevel).toLong()
            assertEquals(targetLevel - 1, PlayerLevelManager.calculateLevel(threshold - 1L))
            assertEquals(targetLevel, PlayerLevelManager.calculateLevel(threshold))
        }
    }

    @Test
    fun testWinsInCurrentLevelAndProgressSafeGuards() {
        // Negative / zero safety
        assertEquals(1, PlayerLevelManager.calculateLevel(-50L))
        assertEquals(0L, PlayerLevelManager.getWinsInCurrentLevel(-10L))
        assertEquals(0.0f, PlayerLevelManager.getProgressToNextLevel(-10L), 0.001f)

        // At 1 win: Level 1, 1 / 3 wins
        assertEquals(1L, PlayerLevelManager.getWinsInCurrentLevel(1L))
        assertEquals(1.0f / 3.0f, PlayerLevelManager.getProgressToNextLevel(1L), 0.001f)

        // At 5 wins: Level 2 (entered at 3), 2 / 5 wins
        assertEquals(2L, PlayerLevelManager.getWinsInCurrentLevel(5L))
        assertEquals(2.0f / 5.0f, PlayerLevelManager.getProgressToNextLevel(5L), 0.001f)

        // Level 150 + 1 win: inside Level 150 (requires 2 -> 0.5f)
        val at150 = PlayerLevelManager.getCumulativeWinsToReachLevel(150).toLong()
        assertEquals(0L, PlayerLevelManager.getWinsInCurrentLevel(at150))
        assertEquals(1L, PlayerLevelManager.getWinsInCurrentLevel(at150 + 1L))
        assertEquals(0.5f, PlayerLevelManager.getProgressToNextLevel(at150 + 1L), 0.001f)

        // Level 200 & Long.MAX_VALUE safety
        assertEquals(200, PlayerLevelManager.calculateLevel(Long.MAX_VALUE))
        assertEquals(1.0f, PlayerLevelManager.getProgressToNextLevel(Long.MAX_VALUE), 0.001f)
        assertTrue(PlayerLevelManager.getWinsRequiredForLevel(200) > BigInteger.ZERO)
    }

    @Test
    fun testExhaustiveStepIncrementsAndLargeFormatting() {
        // Verify exact +2, +5, +7, +9, +10 ONLY, and progressive doubling across every single level 1..199
        for (n in 2..29) {
            assertEquals(
                "Level $n increment must be +2",
                BigInteger.valueOf(2),
                PlayerLevelManager.getWinsRequiredForLevel(n).subtract(PlayerLevelManager.getWinsRequiredForLevel(n - 1))
            )
        }
        for (n in 30..49) {
            assertEquals(
                "Level $n increment must be +5",
                BigInteger.valueOf(5),
                PlayerLevelManager.getWinsRequiredForLevel(n).subtract(PlayerLevelManager.getWinsRequiredForLevel(n - 1))
            )
        }
        for (n in 50..79) {
            assertEquals(
                "Level $n increment must be +7",
                BigInteger.valueOf(7),
                PlayerLevelManager.getWinsRequiredForLevel(n).subtract(PlayerLevelManager.getWinsRequiredForLevel(n - 1))
            )
        }
        for (n in 80..119) {
            assertEquals(
                "Level $n increment must be +9",
                BigInteger.valueOf(9),
                PlayerLevelManager.getWinsRequiredForLevel(n).subtract(PlayerLevelManager.getWinsRequiredForLevel(n - 1))
            )
        }
        for (n in 120..149) {
            assertEquals(
                "Level $n increment must be +10 ONLY",
                BigInteger.valueOf(10),
                PlayerLevelManager.getWinsRequiredForLevel(n).subtract(PlayerLevelManager.getWinsRequiredForLevel(n - 1))
            )
        }
        assertEquals(BigInteger.valueOf(2), PlayerLevelManager.getWinsRequiredForLevel(150))
        for (n in 151..199) {
            assertEquals(
                "Level $n must double progressively from Level ${n - 1}",
                PlayerLevelManager.getWinsRequiredForLevel(n - 1).multiply(BigInteger.valueOf(2)),
                PlayerLevelManager.getWinsRequiredForLevel(n)
            )
        }

        // Formatting safety for zero, negative, and Level 199 huge BigInteger
        assertEquals("0", PlayerLevelManager.formatWinCount(-5L))
        assertEquals("1,029", PlayerLevelManager.formatWinCount(PlayerLevelManager.getWinsRequiredForLevel(149)))
        assertTrue(PlayerLevelManager.formatWinCount(PlayerLevelManager.getWinsRequiredForLevel(199)).isNotBlank())
    }
}
