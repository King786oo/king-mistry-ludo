package com.example

import com.example.ui.components.BoardMetrics
import org.junit.Assert.assertEquals
import org.junit.Test

class LudoBoardMetricsTest {

    @Test
    fun `every Ludo board cell is strictly 1 to 1 square and scales cleanly`() {
        val baseMetrics = BoardMetrics(width = 420f, height = 420f)
        val enlargedMetrics = BoardMetrics(width = 462f, height = 462f)

        // Strict 10% scale-up from 420 to 462
        assertEquals(baseMetrics.cellSize * 1.10f, enlargedMetrics.cellSize, 0.0001f)

        // Every cell across the 15x15 grid must remain strictly square (1:1 aspect ratio)
        for (col in 0 until 15) {
            for (row in 0 until 15) {
                val w = enlargedMetrics.colWidth(col)
                val h = enlargedMetrics.rowHeight(row)
                assertEquals("Cell ($col,$row) must be strictly square", w, h, 0.0001f)
            }
        }

        // Yards (6x6) and Center Home (3x3) must remain exact multiples of square cells
        assertEquals(enlargedMetrics.cellSize * 6f, enlargedMetrics.yardWidth, 0.0001f)
        assertEquals(enlargedMetrics.cellSize * 6f, enlargedMetrics.yardHeight, 0.0001f)
        assertEquals(enlargedMetrics.cellSize * 3f, enlargedMetrics.midTrackWidth, 0.0001f)
        assertEquals(enlargedMetrics.cellSize * 3f, enlargedMetrics.midTrackHeight, 0.0001f)
    }
}
