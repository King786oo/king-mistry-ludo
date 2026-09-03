package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BoardCoordinates
import com.example.model.GridPos
import com.example.model.LudoColor
import com.example.model.LudoToken
import com.example.model.Player

@Composable
fun LudoBoardView(
    players: List<Player>,
    onTokenClicked: (playerId: Int, tokenId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .aspectRatio(1.0f)
            .shadow(16.dp, RoundedCornerShape(22.dp), spotColor = Color(0x77000000))
            .border(
                width = 4.5.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFF0D47A1), // Enterprise Deep Blue
                        Color(0xFF1976D2), // Enterprise Cobalt Blue
                        Color(0xFFFFD54F), // Royal Gold
                        Color(0xFF0A2540)  // Enterprise Navy
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            ),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFFFFFFFF) // Crisp Enterprise White Base
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)
        ) {
            val boardPixelSize = maxWidth
            val cellSize = boardPixelSize / 15f
            // Big Ghuti tokens
            val tokenSize = cellSize * 1.25f

            // 1. Draw Board Grid, Deep Color Yards, Center Triangles & Tracks on Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val cW = w / 15f
                val cH = h / 15f

                // Clean Crisp White Porcelain Base
                drawRect(Color(0xFFFFFFFF), Offset.Zero, Size(w, h))

                // Draw Neutral Track Base Tiles
                drawTrackBaseTiles(cW, cH)

                // Draw 4 Corner Yards (Red Top-Left, Green Top-Right, Blue Bottom-Left, Yellow Bottom-Right)
                drawDeepYard(cW, cH, 0, 0, LudoColor.RED)
                drawDeepYard(cW, cH, 9, 0, LudoColor.GREEN)
                drawDeepYard(cW, cH, 0, 9, LudoColor.BLUE)
                drawDeepYard(cW, cH, 9, 9, LudoColor.YELLOW)

                // Draw Home Path Corridors (Red Left, Green Top, Yellow Right, Blue Bottom)
                // Red Home Corridor: row 7, cols 1..5
                for (c in 1..5) {
                    drawHomeCorridorCell(c, 7, cW, cH, LudoColor.RED)
                }
                // Green Home Corridor: col 7, rows 1..5
                for (r in 1..5) {
                    drawHomeCorridorCell(7, r, cW, cH, LudoColor.GREEN)
                }
                // Yellow Home Corridor: row 7, cols 9..13
                for (c in 9..13) {
                    drawHomeCorridorCell(c, 7, cW, cH, LudoColor.YELLOW)
                }
                // Blue Home Corridor: col 7, rows 9..13
                for (r in 9..13) {
                    drawHomeCorridorCell(7, r, cW, cH, LudoColor.BLUE)
                }

                // Draw Start Cells (Solid launch squares)
                drawStartCell(1, 6, cW, cH, LudoColor.RED)    // Red Start
                drawStartCell(8, 1, cW, cH, LudoColor.GREEN)  // Green Start
                drawStartCell(13, 8, cW, cH, LudoColor.YELLOW)// Yellow Start
                drawStartCell(6, 13, cW, cH, LudoColor.BLUE)  // Blue Start

                // Draw Directional Entry Arrow Cells
                drawEntryArrowCell(0, 7, cW, cH, LudoColor.RED, ArrowDirection.RIGHT)
                drawEntryArrowCell(7, 0, cW, cH, LudoColor.GREEN, ArrowDirection.DOWN)
                drawEntryArrowCell(14, 7, cW, cH, LudoColor.YELLOW, ArrowDirection.LEFT)
                drawEntryArrowCell(7, 14, cW, cH, LudoColor.BLUE, ArrowDirection.UP)

                // Draw Center Home Victory Triangles
                drawCenterHome(cW, cH)

                // Draw Track Arm Grid Lines
                drawTrackGridLines(cW, cH)

                // Draw Outer Board Boundary Line
                drawRect(
                    color = Color(0xFF1E293B),
                    topLeft = Offset.Zero,
                    size = Size(w, h),
                    style = Stroke(width = 3.5f)
                )
            }

            // 2. Safe Stars Overlay (5-pointed stars on row 8 col 2, row 2 col 6, row 6 col 12, row 12 col 8)
            SafeStarsOverlay(cellSize)

            // 3. Yard Player Labels Overlay ("Bot" above Green yard, "You" below Blue yard)
            YardLabelsOverlay(cellSize = cellSize, players = players)

            // 4. Tokens Overlay with collision stacking & glow
            TokensOverlay(
                players = players,
                cellSize = cellSize,
                tokenSize = tokenSize,
                onTokenClicked = onTokenClicked
            )
        }
    }
}

private fun DrawScope.drawTrackBaseTiles(cW: Float, cH: Float) {
    val arms = listOf(
        // Top arm
        (0..5).flatMap { r -> (6..8).map { c -> Pair(c, r) } },
        // Bottom arm
        (9..14).flatMap { r -> (6..8).map { c -> Pair(c, r) } },
        // Left arm
        (6..8).flatMap { r -> (0..5).map { c -> Pair(c, r) } },
        // Right arm
        (6..8).flatMap { r -> (9..14).map { c -> Pair(c, r) } }
    ).flatten()

    for ((col, row) in arms) {
        val left = col * cW
        val top = row * cH
        drawRect(
            color = Color.White,
            topLeft = Offset(left, top),
            size = Size(cW, cH)
        )
    }
}

private fun DrawScope.drawHomeCorridorCell(
    col: Int,
    row: Int,
    cW: Float,
    cH: Float,
    color: LudoColor
) {
    val left = col * cW
    val top = row * cH

    // Solid vibrant color tile
    drawRect(
        color = color.primaryColor,
        topLeft = Offset(left, top),
        size = Size(cW, cH)
    )

    // Cell border
    drawRect(
        color = Color(0xFF212121),
        topLeft = Offset(left, top),
        size = Size(cW, cH),
        style = Stroke(width = 1.4f)
    )
}

private fun DrawScope.drawStartCell(
    col: Int,
    row: Int,
    cW: Float,
    cH: Float,
    color: LudoColor
) {
    val left = col * cW
    val top = row * cH

    // Solid vibrant color launch tile
    drawRect(
        color = color.primaryColor,
        topLeft = Offset(left, top),
        size = Size(cW, cH)
    )

    // Cell border
    drawRect(
        color = Color(0xFF212121),
        topLeft = Offset(left, top),
        size = Size(cW, cH),
        style = Stroke(width = 1.4f)
    )
}

enum class ArrowDirection { RIGHT, DOWN, LEFT, UP }

private fun DrawScope.drawEntryArrowCell(
    col: Int,
    row: Int,
    cW: Float,
    cH: Float,
    color: LudoColor,
    direction: ArrowDirection
) {
    val left = col * cW
    val top = row * cH

    // Solid colored background
    drawRect(
        color = color.primaryColor,
        topLeft = Offset(left, top),
        size = Size(cW, cH)
    )

    // Bold Arrow pointing in corridor direction
    val midX = left + cW / 2f
    val midY = top + cH / 2f
    val arrowPath = Path().apply {
        when (direction) {
            ArrowDirection.RIGHT -> {
                moveTo(left + cW * 0.20f, midY - cH * 0.14f)
                lineTo(left + cW * 0.52f, midY - cH * 0.14f)
                lineTo(left + cW * 0.52f, midY - cH * 0.32f)
                lineTo(left + cW * 0.84f, midY)
                lineTo(left + cW * 0.52f, midY + cH * 0.32f)
                lineTo(left + cW * 0.52f, midY + cH * 0.14f)
                lineTo(left + cW * 0.20f, midY + cH * 0.14f)
                close()
            }
            ArrowDirection.DOWN -> {
                moveTo(midX - cW * 0.14f, top + cH * 0.20f)
                lineTo(midX - cW * 0.14f, top + cH * 0.52f)
                lineTo(midX - cW * 0.32f, top + cH * 0.52f)
                lineTo(midX, top + cH * 0.84f)
                lineTo(midX + cW * 0.32f, top + cH * 0.52f)
                lineTo(midX + cW * 0.14f, top + cH * 0.52f)
                lineTo(midX + cW * 0.14f, top + cH * 0.20f)
                close()
            }
            ArrowDirection.LEFT -> {
                moveTo(left + cW * 0.80f, midY - cH * 0.14f)
                lineTo(left + cW * 0.48f, midY - cH * 0.14f)
                lineTo(left + cW * 0.48f, midY - cH * 0.32f)
                lineTo(left + cW * 0.16f, midY)
                lineTo(left + cW * 0.48f, midY + cH * 0.32f)
                lineTo(left + cW * 0.48f, midY + cH * 0.14f)
                lineTo(left + cW * 0.80f, midY + cH * 0.14f)
                close()
            }
            ArrowDirection.UP -> {
                moveTo(midX - cW * 0.14f, top + cH * 0.80f)
                lineTo(midX - cW * 0.14f, top + cH * 0.48f)
                lineTo(midX - cW * 0.32f, top + cH * 0.48f)
                lineTo(midX, top + cH * 0.16f)
                lineTo(midX + cW * 0.32f, top + cH * 0.48f)
                lineTo(midX + cW * 0.14f, top + cH * 0.48f)
                lineTo(midX + cW * 0.14f, top + cH * 0.80f)
                close()
            }
        }
    }
    drawPath(arrowPath, color = Color.White)
    drawPath(arrowPath, color = Color(0xFF212121), style = Stroke(width = 1.2f))

    // Cell border
    drawRect(
        color = Color(0xFF212121),
        topLeft = Offset(left, top),
        size = Size(cW, cH),
        style = Stroke(width = 1.4f)
    )
}

private fun DrawScope.drawDeepYard(
    cW: Float,
    cH: Float,
    startCol: Int,
    startRow: Int,
    color: LudoColor
) {
    val yardLeft = startCol * cW
    val yardTop = startRow * cH
    val yardWidth = 6 * cW
    val yardHeight = 6 * cH

    // 1. Solid Outer colored quadrant
    drawRect(
        color = color.primaryColor,
        topLeft = Offset(yardLeft, yardTop),
        size = Size(yardWidth, yardHeight)
    )

    // 2. Large Inner White Square Base (matching screenshot)
    val marginW = 0.88f * cW
    val marginH = 0.88f * cH
    val innerLeft = yardLeft + marginW
    val innerTop = yardTop + marginH
    val innerWidth = yardWidth - marginW * 2
    val innerHeight = yardHeight - marginH * 2

    // Crisp White Rounded Rectangle
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(innerLeft, innerTop),
        size = Size(innerWidth, innerHeight),
        cornerRadius = CornerRadius(14f, 14f)
    )

    // Inner White Box Border
    drawRoundRect(
        color = Color(0xFF212121),
        topLeft = Offset(innerLeft, innerTop),
        size = Size(innerWidth, innerHeight),
        cornerRadius = CornerRadius(14f, 14f),
        style = Stroke(width = 1.6f)
    )

    // 3. Four Token Socket Circles inside the white square
    val socketOffsets = listOf(
        Offset(yardLeft + 1.85f * cW, yardTop + 1.85f * cH),
        Offset(yardLeft + 4.15f * cW, yardTop + 1.85f * cH),
        Offset(yardLeft + 1.85f * cW, yardTop + 4.15f * cH),
        Offset(yardLeft + 4.15f * cW, yardTop + 4.15f * cH)
    )

    val socketRadius = 0.72f * cW
    for (socket in socketOffsets) {
        // Colored circle fill
        drawCircle(
            color = color.primaryColor,
            radius = socketRadius,
            center = socket
        )
        // Dark outline
        drawCircle(
            color = Color(0xFF212121),
            radius = socketRadius,
            center = socket,
            style = Stroke(width = 1.4f)
        )
    }

    // Yard outer black boundary line
    drawRect(
        color = Color(0xFF212121),
        topLeft = Offset(yardLeft, yardTop),
        size = Size(yardWidth, yardHeight),
        style = Stroke(width = 2.4f)
    )
}

private fun DrawScope.drawTrackGridLines(cW: Float, cH: Float) {
    val strokeColor = Color(0xFF212121)
    val strokeWidth = 1.4f

    val armCells = listOf(
        // Top Arm (cols 6..8, rows 0..5)
        (0..5).flatMap { r -> (6..8).map { c -> Pair(c, r) } },
        // Bottom Arm (cols 6..8, rows 9..14)
        (9..14).flatMap { r -> (6..8).map { c -> Pair(c, r) } },
        // Left Arm (cols 0..5, rows 6..8)
        (6..8).flatMap { r -> (0..5).map { c -> Pair(c, r) } },
        // Right Arm (cols 9..14, rows 6..8)
        (6..8).flatMap { r -> (9..14).map { c -> Pair(c, r) } }
    ).flatten()

    for ((c, r) in armCells) {
        drawRect(
            color = strokeColor,
            topLeft = Offset(c * cW, r * cH),
            size = Size(cW, cH),
            style = Stroke(width = strokeWidth)
        )
    }
}

private fun DrawScope.drawCenterHome(cW: Float, cH: Float) {
    val centerLeft = 6 * cW
    val centerTop = 6 * cH
    val centerRight = 9 * cW
    val centerBottom = 9 * cH
    val midX = 7.5f * cW
    val midY = 7.5f * cH

    // 1. Red (Left) Triangle
    val redPath = Path().apply {
        moveTo(centerLeft, centerTop)
        lineTo(midX, midY)
        lineTo(centerLeft, centerBottom)
        close()
    }
    drawPath(redPath, color = LudoColor.RED.primaryColor)

    // 2. Green (Top) Triangle
    val greenPath = Path().apply {
        moveTo(centerLeft, centerTop)
        lineTo(midX, midY)
        lineTo(centerRight, centerTop)
        close()
    }
    drawPath(greenPath, color = LudoColor.GREEN.primaryColor)

    // 3. Yellow (Right) Triangle
    val yellowPath = Path().apply {
        moveTo(centerRight, centerTop)
        lineTo(midX, midY)
        lineTo(centerRight, centerBottom)
        close()
    }
    drawPath(yellowPath, color = LudoColor.YELLOW.primaryColor)

    // 4. Blue (Bottom) Triangle
    val bluePath = Path().apply {
        moveTo(centerLeft, centerBottom)
        lineTo(midX, midY)
        lineTo(centerRight, centerBottom)
        close()
    }
    drawPath(bluePath, color = LudoColor.BLUE.primaryColor)

    // Center dividing lines (black)
    drawLine(
        color = Color(0xFF212121),
        start = Offset(centerLeft, centerTop),
        end = Offset(centerRight, centerBottom),
        strokeWidth = 2f
    )
    drawLine(
        color = Color(0xFF212121),
        start = Offset(centerLeft, centerBottom),
        end = Offset(centerRight, centerTop),
        strokeWidth = 2f
    )

    // Center boundary frame (black)
    drawRect(
        color = Color(0xFF212121),
        topLeft = Offset(centerLeft, centerTop),
        size = Size(3 * cW, 3 * cH),
        style = Stroke(width = 2.4f)
    )
}

@Composable
private fun SafeStarsOverlay(cellSize: Dp) {
    val safePositions = listOf(
        GridPos(2, 8),  // Red arm safe star (☆)
        GridPos(6, 2),  // Green arm safe star (☆)
        GridPos(12, 6), // Yellow arm safe star (☆)
        GridPos(8, 12)  // Blue arm safe star (☆)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        for (pos in safePositions) {
            Box(
                modifier = Modifier
                    .size(cellSize)
                    .offset(x = cellSize * pos.col, y = cellSize * pos.row),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(cellSize * 0.76f)) {
                    val w = size.width
                    val h = size.height
                    val cx = w / 2f
                    val cy = h / 2f
                    val outerR = w * 0.46f
                    val innerR = outerR * 0.42f
                    val starPath = Path()
                    for (i in 0 until 10) {
                        val angle = (i * 36.0 - 90.0) * (Math.PI / 180.0)
                        val r = if (i % 2 == 0) outerR else innerR
                        val x = cx + (r * Math.cos(angle)).toFloat()
                        val y = cy + (r * Math.sin(angle)).toFloat()
                        if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
                    }
                    starPath.close()
                    drawPath(starPath, color = Color(0xFF212121), style = Stroke(width = 2.0f))
                }
            }
        }
    }
}

@Composable
private fun YardLabelsOverlay(
    cellSize: Dp,
    players: List<Player>
) {
    val greenPlayer = players.find { it.color == LudoColor.GREEN }
    val bluePlayer = players.find { it.color == LudoColor.BLUE }
    val redPlayer = players.find { it.color == LudoColor.RED }
    val yellowPlayer = players.find { it.color == LudoColor.YELLOW }

    Box(modifier = Modifier.fillMaxSize()) {
        // Green Yard Label: Top-Right (Above white square, centered over cols 9..14)
        val greenLabel = greenPlayer?.name ?: "Bot"
        Box(
            modifier = Modifier
                .width(cellSize * 6)
                .height(cellSize * 0.85f)
                .offset(x = cellSize * 9, y = cellSize * 0.05f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = greenLabel,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                maxLines = 1
            )
        }

        // Blue Yard Label: Bottom-Left (Below white square, centered over cols 0..5)
        val blueLabel = bluePlayer?.name ?: "You"
        Box(
            modifier = Modifier
                .width(cellSize * 6)
                .height(cellSize * 0.85f)
                .offset(x = cellSize * 0, y = cellSize * 15.0f - cellSize * 0.90f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = blueLabel,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                maxLines = 1
            )
        }

        // Red Yard Label (if 4-player): Top-Left
        if (redPlayer != null && players.size > 2) {
            Box(
                modifier = Modifier
                    .width(cellSize * 6)
                    .height(cellSize * 0.85f)
                    .offset(x = cellSize * 0, y = cellSize * 0.05f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = redPlayer.name,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }
        }

        // Yellow Yard Label (if 4-player): Bottom-Right
        if (yellowPlayer != null && players.size > 2) {
            Box(
                modifier = Modifier
                    .width(cellSize * 6)
                    .height(cellSize * 0.85f)
                    .offset(x = cellSize * 9, y = cellSize * 15.0f - cellSize * 0.90f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = yellowPlayer.name,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }
        }
    }
}

data class TokenPlacement(
    val token: LudoToken,
    val player: Player,
    val gridPos: GridPos,
    val offsetFractionX: Float = 0f,
    val offsetFractionY: Float = 0f
)

@Composable
private fun TokensOverlay(
    players: List<Player>,
    cellSize: Dp,
    tokenSize: Dp,
    onTokenClicked: (playerId: Int, tokenId: Int) -> Unit
) {
    val allPlacements = mutableListOf<TokenPlacement>()

    for (player in players) {
        for (token in player.tokens) {
            val gridPos = BoardCoordinates.getTokenGridPosition(token.color, token.id, token.step)
            allPlacements.add(TokenPlacement(token, player, gridPos))
        }
    }

    val groupedByCell = allPlacements.groupBy { it.gridPos }

    Box(modifier = Modifier.fillMaxSize()) {
        for ((_, cellTokens) in groupedByCell) {
            val count = cellTokens.size
            cellTokens.forEachIndexed { index, placement ->
                val (offsetX, offsetY) = getStackOffset(index, count)
                val token = placement.token
                val player = placement.player

                val xPos = cellSize * placement.gridPos.col + (cellSize - tokenSize) / 2f + cellSize * offsetX
                val yPos = cellSize * placement.gridPos.row + (cellSize - tokenSize) / 2f + cellSize * offsetY

                LudoTokenView(
                    token = token,
                    size = if (count > 1 && !token.isInYard) tokenSize * 0.94f else if (token.isInYard) tokenSize * 1.15f else tokenSize,
                    modifier = Modifier.offset(x = xPos, y = yPos),
                    onClick = {
                        onTokenClicked(player.id, token.id)
                    }
                )
            }
        }
    }
}

private fun getStackOffset(index: Int, total: Int): Pair<Float, Float> {
    if (total <= 1) return Pair(0f, 0f)
    return when (total) {
        2 -> {
            if (index == 0) Pair(-0.16f, -0.16f) else Pair(0.16f, 0.16f)
        }
        3 -> {
            when (index) {
                0 -> Pair(0f, -0.2f)
                1 -> Pair(-0.2f, 0.16f)
                else -> Pair(0.2f, 0.16f)
            }
        }
        else -> {
            when (index % 4) {
                0 -> Pair(-0.18f, -0.18f)
                1 -> Pair(0.18f, -0.18f)
                2 -> Pair(-0.18f, 0.18f)
                else -> Pair(0.18f, 0.18f)
            }
        }
    }
}
