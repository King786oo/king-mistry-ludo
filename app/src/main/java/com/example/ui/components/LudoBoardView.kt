package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.model.BoardCoordinates
import com.example.model.GridPos
import com.example.model.LudoColor
import com.example.model.LudoPremiumToken
import com.example.model.LudoToken
import com.example.model.Player

/**
 * Metric calculator for uniform 15x15 board scaling:
 * - Every single cell on the board is a uniform square of size cellSize x cellSize (width / 15f)
 * - 4 Corner Yards are 6x6 cells (40% of board width/height)
 * - 4 Middle Running Track Arms are 3x6 cells (20% wide, 40% long)
 * - Center Home is a 3x3 cells victory square (20% of board)
 */
class BoardMetrics(val width: Float, val height: Float) {
    // 15x15 uniform grid: every single cell on the board is an exact square of size cellSize x cellSize
    val cellSize: Float = width / 15f

    // 6-cell yard corner dimensions (6 * cellSize)
    val yardWidth: Float = cellSize * 6f
    val yardHeight: Float = cellSize * 6f

    // 3-cell middle corridor & center victory zone (3 * cellSize)
    val midTrackWidth: Float = cellSize * 3f
    val midTrackHeight: Float = cellSize * 3f

    fun colLeft(col: Int): Float = col.coerceIn(0, 15) * cellSize
    fun colRight(col: Int): Float = (col + 1).coerceIn(0, 15) * cellSize
    fun colWidth(col: Int): Float = cellSize

    fun rowTop(row: Int): Float = row.coerceIn(0, 15) * cellSize
    fun rowBottom(row: Int): Float = (row + 1).coerceIn(0, 15) * cellSize
    fun rowHeight(row: Int): Float = cellSize

    fun cellCenter(col: Int, row: Int): Offset {
        return Offset((col.coerceIn(0, 14) + 0.5f) * cellSize, (row.coerceIn(0, 14) + 0.5f) * cellSize)
    }

    fun getYardSocket(color: LudoColor, tokenIndex: Int): Offset {
        val (yLeft, yTop) = when (color) {
            LudoColor.RED -> Pair(0f, 0f)
            LudoColor.GREEN -> Pair(9f * cellSize, 0f)
            LudoColor.BLUE -> Pair(0f, 9f * cellSize)
            LudoColor.YELLOW -> Pair(9f * cellSize, 9f * cellSize)
        }
        val (rx, ry) = when (tokenIndex) {
            0 -> Pair(1.85f, 1.85f)
            1 -> Pair(4.15f, 1.85f)
            2 -> Pair(1.85f, 4.15f)
            else -> Pair(4.15f, 4.15f)
        }
        return Offset(yLeft + rx * cellSize, yTop + ry * cellSize)
    }
}

@Composable
fun LudoBoardView(
    players: List<Player>,
    activePlayerId: Int = -1,
    localPlayerId: Int = 0,
    equippedToken: LudoPremiumToken = LudoPremiumToken.PAWN_CRYSTAL_GEM,
    onTokenClicked: (playerId: Int, tokenId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .aspectRatio(1.0f)
            .shadow(16.dp, RoundedCornerShape(18.dp), spotColor = Color(0x77000000))
            .border(
                width = 3.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFF0D47A1),
                        Color(0xFF1976D2),
                        Color(0xFFFFD54F),
                        Color(0xFF0A2540)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            ),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFFFFFFF)
    ) {
        Layout(
            content = {
                // 1. Static Board Canvas with uniform 15x15 strictly square (1:1) cells
                StaticBoardCanvas(modifier = Modifier.fillMaxSize())

                // 2. Tokens Overlay with collision stacking & glow (player names removed)
                TokensOverlay(
                    players = players,
                    activePlayerId = activePlayerId,
                    localPlayerId = localPlayerId,
                    equippedToken = equippedToken,
                    onTokenClicked = onTokenClicked,
                    modifier = Modifier.fillMaxSize()
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp)
        ) { measurables, constraints ->
            val boardSize = minOf(constraints.maxWidth, constraints.maxHeight)
            val childConstraints = Constraints.fixed(boardSize, boardSize)
            val placeables = measurables.map { it.measure(childConstraints) }

            layout(boardSize, boardSize) {
                placeables.forEach { it.place(0, 0) }
            }
        }
    }
}

@Composable
private fun StaticBoardCanvas(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                val w = size.width
                val h = size.height
                val metrics = BoardMetrics(w, h)

                onDrawBehind {
                    // Crisp Enterprise White Base
                    drawRect(Color(0xFFFFFFFF), Offset.Zero, Size(w, h))

                    // Draw Neutral Track Base Tiles
                    drawTrackBaseTiles(metrics)

                    // Draw 4 Corner Yards scaled to 90% (Red Top-Left, Green Top-Right, Blue Bottom-Left, Yellow Bottom-Right)
                    drawDeepYard(metrics, 0, 0, LudoColor.RED)
                    drawDeepYard(metrics, 9, 0, LudoColor.GREEN)
                    drawDeepYard(metrics, 0, 9, LudoColor.BLUE)
                    drawDeepYard(metrics, 9, 9, LudoColor.YELLOW)

                    // Draw Home Path Corridors (Red Left, Green Top, Yellow Right, Blue Bottom)
                    for (c in 1..5) {
                        drawHomeCorridorCell(c, 7, metrics, LudoColor.RED)
                    }
                    for (r in 1..5) {
                        drawHomeCorridorCell(7, r, metrics, LudoColor.GREEN)
                    }
                    for (c in 9..13) {
                        drawHomeCorridorCell(c, 7, metrics, LudoColor.YELLOW)
                    }
                    for (r in 9..13) {
                        drawHomeCorridorCell(7, r, metrics, LudoColor.BLUE)
                    }

                    // Draw Start Cells (Solid launch squares)
                    drawStartCell(1, 6, metrics, LudoColor.RED)
                    drawStartCell(8, 1, metrics, LudoColor.GREEN)
                    drawStartCell(13, 8, metrics, LudoColor.YELLOW)
                    drawStartCell(6, 13, metrics, LudoColor.BLUE)

                    // Draw Directional Entry Arrow Cells
                    drawEntryArrowCell(0, 7, metrics, LudoColor.RED, ArrowDirection.RIGHT)
                    drawEntryArrowCell(7, 0, metrics, LudoColor.GREEN, ArrowDirection.DOWN)
                    drawEntryArrowCell(14, 7, metrics, LudoColor.YELLOW, ArrowDirection.LEFT)
                    drawEntryArrowCell(7, 14, metrics, LudoColor.BLUE, ArrowDirection.UP)

                    // Draw 4 Safe Stars directly on board canvas
                    drawSafeStar(metrics, 2, 8)
                    drawSafeStar(metrics, 6, 2)
                    drawSafeStar(metrics, 12, 6)
                    drawSafeStar(metrics, 8, 12)

                    // Draw Center Home Victory Triangles
                    drawCenterHome(metrics)

                    // Draw Track Arm Grid Lines
                    drawTrackGridLines(metrics)

                    // Draw Outer Board Boundary Line
                    drawRect(
                        color = Color(0xFF1E293B),
                        topLeft = Offset.Zero,
                        size = Size(w, h),
                        style = Stroke(width = 3.5f)
                    )
                }
            }
    ) {
    }
}

private fun DrawScope.drawTrackBaseTiles(metrics: BoardMetrics) {
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
        val left = metrics.colLeft(col)
        val top = metrics.rowTop(row)
        val w = metrics.colWidth(col)
        val h = metrics.rowHeight(row)
        drawRect(
            color = Color(0xFFFAFAFA),
            topLeft = Offset(left, top),
            size = Size(w, h)
        )
    }
}

private fun DrawScope.drawHomeCorridorCell(
    col: Int,
    row: Int,
    metrics: BoardMetrics,
    color: LudoColor
) {
    val left = metrics.colLeft(col)
    val top = metrics.rowTop(row)
    val w = metrics.colWidth(col)
    val h = metrics.rowHeight(row)

    drawRect(
        color = color.primaryColor,
        topLeft = Offset(left, top),
        size = Size(w, h)
    )

    drawRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(left, top),
        size = Size(w, h),
        style = Stroke(width = 1.8f)
    )
}

private fun DrawScope.drawStartCell(
    col: Int,
    row: Int,
    metrics: BoardMetrics,
    color: LudoColor
) {
    val left = metrics.colLeft(col)
    val top = metrics.rowTop(row)
    val w = metrics.colWidth(col)
    val h = metrics.rowHeight(row)

    drawRect(
        color = color.primaryColor,
        topLeft = Offset(left, top),
        size = Size(w, h)
    )

    drawRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(left, top),
        size = Size(w, h),
        style = Stroke(width = 1.8f)
    )
}

enum class ArrowDirection { RIGHT, DOWN, LEFT, UP }

private fun DrawScope.drawEntryArrowCell(
    col: Int,
    row: Int,
    metrics: BoardMetrics,
    color: LudoColor,
    direction: ArrowDirection
) {
    val left = metrics.colLeft(col)
    val top = metrics.rowTop(row)
    val s = metrics.colWidth(col)

    drawRect(
        color = color.primaryColor,
        topLeft = Offset(left, top),
        size = Size(s, s)
    )

    val midX = left + s / 2f
    val midY = top + s / 2f
    val arrowPath = Path().apply {
        when (direction) {
            ArrowDirection.RIGHT -> {
                moveTo(left + s * 0.14f, midY - s * 0.15f)
                lineTo(left + s * 0.52f, midY - s * 0.15f)
                lineTo(left + s * 0.52f, midY - s * 0.35f)
                lineTo(left + s * 0.88f, midY)
                lineTo(left + s * 0.52f, midY + s * 0.35f)
                lineTo(left + s * 0.52f, midY + s * 0.15f)
                lineTo(left + s * 0.14f, midY + s * 0.15f)
                close()
            }
            ArrowDirection.DOWN -> {
                moveTo(midX - s * 0.15f, top + s * 0.14f)
                lineTo(midX - s * 0.15f, top + s * 0.52f)
                lineTo(midX - s * 0.35f, top + s * 0.52f)
                lineTo(midX, top + s * 0.88f)
                lineTo(midX + s * 0.35f, top + s * 0.52f)
                lineTo(midX + s * 0.15f, top + s * 0.52f)
                lineTo(midX + s * 0.15f, top + s * 0.14f)
                close()
            }
            ArrowDirection.LEFT -> {
                moveTo(left + s * 0.86f, midY - s * 0.15f)
                lineTo(left + s * 0.48f, midY - s * 0.15f)
                lineTo(left + s * 0.48f, midY - s * 0.35f)
                lineTo(left + s * 0.12f, midY)
                lineTo(left + s * 0.48f, midY + s * 0.35f)
                lineTo(left + s * 0.48f, midY + s * 0.15f)
                lineTo(left + s * 0.86f, midY + s * 0.15f)
                close()
            }
            ArrowDirection.UP -> {
                moveTo(midX - s * 0.15f, top + s * 0.86f)
                lineTo(midX - s * 0.15f, top + s * 0.48f)
                lineTo(midX - s * 0.35f, top + s * 0.48f)
                lineTo(midX, top + s * 0.12f)
                lineTo(midX + s * 0.35f, top + s * 0.48f)
                lineTo(midX + s * 0.15f, top + s * 0.48f)
                lineTo(midX + s * 0.15f, top + s * 0.86f)
                close()
            }
        }
    }
    drawPath(arrowPath, color = Color.White)
    drawPath(arrowPath, color = Color(0xFF1E293B), style = Stroke(width = 1.8f))

    drawRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(left, top),
        size = Size(s, s),
        style = Stroke(width = 1.8f)
    )
}

private fun DrawScope.drawDeepYard(
    metrics: BoardMetrics,
    startCol: Int,
    startRow: Int,
    color: LudoColor
) {
    val yardLeft = metrics.colLeft(startCol)
    val yardTop = metrics.rowTop(startRow)
    val yardWidth = metrics.yardWidth
    val yardHeight = metrics.yardHeight

    // 1. Solid Outer colored quadrant (6x6 cells)
    drawRect(
        color = color.primaryColor,
        topLeft = Offset(yardLeft, yardTop),
        size = Size(yardWidth, yardHeight)
    )

    // 2. Inner White Square Base (centered inside yard)
    val margin = metrics.cellSize * 0.80f
    val innerLeft = yardLeft + margin
    val innerTop = yardTop + margin
    val innerWidth = yardWidth - margin * 2f
    val innerHeight = yardHeight - margin * 2f

    drawRoundRect(
        color = Color.White,
        topLeft = Offset(innerLeft, innerTop),
        size = Size(innerWidth, innerHeight),
        cornerRadius = CornerRadius(14f, 14f)
    )

    drawRoundRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(innerLeft, innerTop),
        size = Size(innerWidth, innerHeight),
        cornerRadius = CornerRadius(14f, 14f),
        style = Stroke(width = 2.4f)
    )

    // 3. Four Token Socket Circles inside the white square
    val socketRadius = metrics.cellSize * 0.72f
    for (i in 0..3) {
        val socket = metrics.getYardSocket(color, i)
        drawCircle(
            color = Color(0xFFFFD54F).copy(alpha = 0.65f),
            radius = socketRadius + 2.5f,
            center = socket
        )
        drawCircle(
            color = color.primaryColor,
            radius = socketRadius,
            center = socket
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.35f),
            radius = socketRadius * 0.68f,
            center = Offset(socket.x - socketRadius * 0.12f, socket.y - socketRadius * 0.12f)
        )
        drawCircle(
            color = Color(0xFF1E293B),
            radius = socketRadius,
            center = socket,
            style = Stroke(width = 2.0f)
        )
    }

    drawRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(yardLeft, yardTop),
        size = Size(yardWidth, yardHeight),
        style = Stroke(width = 3.0f)
    )
}

private fun DrawScope.drawTrackGridLines(metrics: BoardMetrics) {
    val strokeColor = Color(0xFF1E293B)
    val strokeWidth = 2.4f

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
        val left = metrics.colLeft(c)
        val top = metrics.rowTop(r)
        val w = metrics.colWidth(c)
        val h = metrics.rowHeight(r)
        drawRect(
            color = strokeColor,
            topLeft = Offset(left, top),
            size = Size(w, h),
            style = Stroke(width = strokeWidth)
        )
    }
}

private fun DrawScope.drawCenterHome(metrics: BoardMetrics) {
    val centerLeft = metrics.colLeft(6)
    val centerTop = metrics.rowTop(6)
    val centerRight = metrics.colLeft(9)
    val centerBottom = metrics.rowTop(9)
    val midX = (centerLeft + centerRight) / 2f
    val midY = (centerTop + centerBottom) / 2f

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

    // Center dividing lines
    drawLine(
        color = Color(0xFF1E293B),
        start = Offset(centerLeft, centerTop),
        end = Offset(centerRight, centerBottom),
        strokeWidth = 2.4f
    )
    drawLine(
        color = Color(0xFF1E293B),
        start = Offset(centerLeft, centerBottom),
        end = Offset(centerRight, centerTop),
        strokeWidth = 2.4f
    )

    // Center boundary frame
    drawRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(centerLeft, centerTop),
        size = Size(metrics.midTrackWidth, metrics.midTrackHeight),
        style = Stroke(width = 2.8f)
    )
}

private fun DrawScope.drawSafeStar(metrics: BoardMetrics, col: Int, row: Int) {
    val center = metrics.cellCenter(col, row)
    val cx = center.x
    val cy = center.y
    val cW = metrics.colWidth(col)
    val outerR = cW * 0.44f
    val innerR = outerR * 0.44f
    val starPath = Path()
    for (i in 0 until 10) {
        val angle = (i * 36.0 - 90.0) * (Math.PI / 180.0)
        val r = if (i % 2 == 0) outerR else innerR
        val x = cx + (r * Math.cos(angle)).toFloat()
        val y = cy + (r * Math.sin(angle)).toFloat()
        if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
    }
    starPath.close()
    drawPath(starPath, color = Color(0xFFFFD700).copy(alpha = 0.45f))
    drawPath(starPath, color = Color(0xFF1E293B), style = Stroke(width = 2.4f))
}

@Immutable
data class TokenPlacementData(
    val tokenKey: String,
    val playerId: Int,
    val token: LudoToken,
    val gridPos: GridPos,
    val stackIndex: Int,
    val totalInCell: Int,
    val sameOwnerInCell: Int = 1,
    val isDoubleGuard: Boolean = false,
    val isActivePlayer: Boolean = false
)

@Composable
private fun TokensOverlay(
    players: List<Player>,
    activePlayerId: Int = -1,
    localPlayerId: Int = 0,
    equippedToken: LudoPremiumToken = LudoPremiumToken.PAWN_CRYSTAL_GEM,
    onTokenClicked: (playerId: Int, tokenId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val resolvedActivePlayerId = if (activePlayerId != -1) {
        activePlayerId
    } else {
        players.firstOrNull { p -> p.tokens.any { it.isHighlighted || it.isMoving } }?.id ?: -1
    }

    val placements = remember(players, resolvedActivePlayerId) {
        val rawList = players.flatMap { player ->
            player.tokens.map { token ->
                val gridPos = BoardCoordinates.getTokenGridPosition(token.color, token.id, token.step)
                Triple(player.id, token, gridPos)
            }
        }

        val byCell = rawList.groupBy { it.third }

        rawList.map { (playerId, token, gridPos) ->
            val cellOccupants = byCell[gridPos] ?: emptyList()
            val isActivePlayer = (playerId == resolvedActivePlayerId)

            val sortedOccupants = cellOccupants.sortedWith(
                compareBy<Triple<Int, LudoToken, GridPos>> { it.first == resolvedActivePlayerId }
                    .thenBy { it.first }
                    .thenBy { it.second.id }
            )
            val indexInCell = sortedOccupants.indexOfFirst {
                it.first == playerId && it.second.id == token.id
            }.coerceAtLeast(0)

            val sameOwnerCount = cellOccupants.count { it.first == playerId }
            val isFriendlyStack = cellOccupants.count { occ ->
                occ.first == playerId
            } >= 2

            TokenPlacementData(
                tokenKey = "${token.color.name}_${token.id}",
                playerId = playerId,
                token = token,
                gridPos = gridPos,
                stackIndex = indexInCell,
                totalInCell = cellOccupants.size,
                sameOwnerInCell = sameOwnerCount,
                isDoubleGuard = isFriendlyStack && !token.isInYard,
                isActivePlayer = isActivePlayer
            )
        }
    }

    BoxWithConstraints(modifier = modifier) {
        val boardPx = minOf(constraints.maxWidth, constraints.maxHeight).toFloat()
        val metrics = remember(boardPx) { BoardMetrics(boardPx, boardPx) }

        placements.forEach { item ->
            key(item.tokenKey) {
                AnimatedStackedToken(
                    item = item,
                    metrics = metrics,
                    localPlayerId = localPlayerId,
                    equippedToken = equippedToken,
                    onClick = { onTokenClicked(item.playerId, item.token.id) }
                )
            }
        }
    }
}

@Composable
private fun AnimatedStackedToken(
    item: TokenPlacementData,
    metrics: BoardMetrics,
    localPlayerId: Int,
    equippedToken: LudoPremiumToken,
    onClick: () -> Unit
) {
    val isMultiCell = !item.token.isInYard && item.totalInCell >= 2
    val (targetOffX, targetOffY) = getStackOffset(item.stackIndex, item.totalInCell, item.token.isInYard)

    val animOffX by animateFloatAsState(
        targetValue = targetOffX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "stack_off_x_${item.tokenKey}"
    )
    val animOffY by animateFloatAsState(
        targetValue = targetOffY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "stack_off_y_${item.tokenKey}"
    )

    // Token Scale: yard pawns match the 1.44f circular home slots; single track pawns fill the square cell boldly.
    // Multi-token cells (totalInCell >= 2) keep the existing grouping/half-size split ratios untouched.
    val baseScale = computeTokenCellScale(
        isInYard = item.token.isInYard,
        totalInCell = item.totalInCell,
        isHighlighted = item.token.isHighlighted,
        isMoving = item.token.isMoving
    )
    val animScale by animateFloatAsState(
        targetValue = baseScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "stack_scale_${item.tokenKey}"
    )

    val baseCellDim = minOf(metrics.colWidth(item.gridPos.col), metrics.rowHeight(item.gridPos.row))
    val tokenSizePx = baseCellDim * animScale
    val density = LocalDensity.current
    val tokenSizeDp = with(density) { tokenSizePx.toDp() }

    val centerOffset = if (item.token.isInYard) {
        metrics.getYardSocket(item.token.color, item.token.id)
    } else {
        val cellCenter = metrics.cellCenter(item.gridPos.col, item.gridPos.row)
        Offset(
            cellCenter.x + metrics.colWidth(item.gridPos.col) * animOffX,
            cellCenter.y + metrics.rowHeight(item.gridPos.row) * animOffY
        )
    }

    val targetLeftPx = centerOffset.x - tokenSizePx / 2f
    val targetTopPx = centerOffset.y - tokenSizePx / 2f

    val targetLeftDp = with(density) { targetLeftPx.toDp() }
    val targetTopDp = with(density) { targetTopPx.toDp() }

    val animLeftPos by animateDpAsState(
        targetValue = targetLeftDp,
        animationSpec = tween(
            durationMillis = if (item.token.isReturning) 35 else 65,
            easing = FastOutSlowInEasing
        ),
        label = "token_pos_x_${item.tokenKey}"
    )
    val animTopPos by animateDpAsState(
        targetValue = targetTopDp,
        animationSpec = tween(
            durationMillis = if (item.token.isReturning) 35 else 65,
            easing = FastOutSlowInEasing
        ),
        label = "token_pos_y_${item.tokenKey}"
    )

    val zIndexValue = when {
        item.token.isMoving -> 500f
        item.token.isHighlighted -> 400f + (item.stackIndex * 2f)
        item.isActivePlayer -> 300f + (item.stackIndex * 2f)
        else -> 10f + (item.stackIndex * 1f)
    }

    val touchSize = if (isMultiCell) {
        maxOf(tokenSizeDp, with(density) { (baseCellDim * 0.70f).toDp() }).coerceAtLeast(48.dp)
    } else {
        maxOf(tokenSizeDp, 48.dp)
    }
    val touchOffX = (touchSize - tokenSizeDp) / 2f
    val touchOffY = (touchSize - tokenSizeDp) / 2f

    val outerTouchInteraction = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .zIndex(zIndexValue)
            .offset(x = animLeftPos - touchOffX, y = animTopPos - touchOffY)
            .size(touchSize)
            .clickable(
                enabled = item.token.isHighlighted,
                interactionSource = outerTouchInteraction,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(tokenSizeDp),
            contentAlignment = Alignment.Center
        ) {
            val effectivePremiumToken = item.token.styleId?.let { LudoPremiumToken.fromId(it) } ?: equippedToken
            LudoTokenView(
                token = item.token,
                size = tokenSizeDp,
                isDoubleGuard = item.isDoubleGuard,
                isActivePlayer = item.isActivePlayer,
                isStacked = isMultiCell,
                premiumToken = effectivePremiumToken,
                onClick = onClick
            )

            if (item.sameOwnerInCell >= 2 && !item.token.isInYard) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 3.dp, y = (-3).dp)
                        .shadow(4.dp, CircleShape),
                    shape = CircleShape,
                    color = Color(0xFF0F172A),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFFFD54F))
                ) {
                    Text(
                        text = "${item.sameOwnerInCell}",
                        color = Color(0xFFFFD54F),
                        fontSize = (tokenSizeDp.value * 0.32f).coerceIn(8.5f, 12.5f).sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 3.5.dp, vertical = 0.5.dp)
                    )
                }
            }
        }
    }
}

internal fun computeTokenCellScale(
    isInYard: Boolean,
    totalInCell: Int,
    isHighlighted: Boolean,
    isMoving: Boolean
): Float = when {
    // Match the 4 circular yard slots (diameter = 1.44f * cellSize)
    isInYard -> 1.42f
    // Preserve multi-token grouping / half-size split ratios unchanged
    totalInCell >= 5 -> if (isHighlighted) 0.52f else 0.46f
    totalInCell == 4 -> if (isHighlighted) 0.60f else 0.52f
    totalInCell == 3 -> if (isHighlighted) 0.70f else 0.62f
    totalInCell == 2 -> if (isHighlighted) 0.80f else 0.72f
    // Enlarged single pawn on board cell to boldly fill the square tile without overflowing
    else -> when {
        isMoving -> 1.44f
        isHighlighted -> 1.40f
        else -> 1.36f
    }
}

private fun getStackOffset(index: Int, total: Int, isInYard: Boolean = false): Pair<Float, Float> {
    if (isInYard || total <= 1) return Pair(0f, 0f)
    return when (total) {
        2 -> {
            if (index == 0) Pair(-0.25f, 0f) else Pair(0.25f, 0f)
        }
        3 -> {
            when (index) {
                0 -> Pair(0f, -0.24f)
                1 -> Pair(-0.25f, 0.22f)
                else -> Pair(0.25f, 0.22f)
            }
        }
        4 -> {
            when (index) {
                0 -> Pair(-0.23f, -0.23f)
                1 -> Pair(0.23f, -0.23f)
                2 -> Pair(-0.23f, 0.23f)
                else -> Pair(0.23f, 0.23f)
            }
        }
        else -> {
            when (index) {
                0 -> Pair(-0.24f, -0.24f)
                1 -> Pair(0.24f, -0.24f)
                2 -> Pair(0f, 0f)
                3 -> Pair(-0.24f, 0.24f)
                else -> Pair(0.24f, 0.24f)
            }
        }
    }
}
