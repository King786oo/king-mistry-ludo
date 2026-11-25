package com.example.ui.components

import android.graphics.Matrix
import android.graphics.SweepGradient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.example.engine.PlayerLevelManager
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DiceSkin
import com.example.model.GamePhase
import com.example.model.LudoColor
import com.example.model.Player

/**
 * Version 79 4-Corner Two-Section Player Box:
 * 1. SECTION A: Animated 3D Dice Graphic (with dynamic tumbling roll animation, prominent size).
 * 2. SECTION B: Numeric value of rolled dice clearly displayed (large bold digits with golden glow on 6).
 * 3. OUTSIDE COLOR INDICATOR: Assigned token/house color indicator positioned on the outside edge.
 * 4. PLAYER IDENTIFIER: Placed cleanly above or below the box with online opponent display.
 */
@Composable
fun LudoCornerPlayerBox(
    player: Player?,
    playerNumber: Int,
    houseColor: LudoColor,
    isCurrentTurn: Boolean,
    currentDiceValue: Int,
    isRolling: Boolean,
    gamePhase: GamePhase,
    isBengali: Boolean = false,
    isOnlineMatch: Boolean = false,
    diceSkin: DiceSkin = DiceSkin.ROYAL_GOLD_KING,
    targetTokensToWin: Int = 4,
    labelAbove: Boolean = true,
    indicatorOnLeft: Boolean = true,
    localPlayerId: Int = 0,
    onRollClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val canRoll = player != null && isCurrentTurn && gamePhase == GamePhase.ROLL_DICE && !player.isBot && !isRolling

    val infiniteTransition = rememberInfiniteTransition(label = "corner_dice_pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.70f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    // Dynamic tumbling animation for Section A animated dice during roll
    val rollRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dice_rot"
    )
    val rollScale by infiniteTransition.animateFloat(
        initialValue = 0.84f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 160, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dice_scale"
    )

    val safeValue = currentDiceValue.coerceIn(1, 6)
    val isSix = safeValue == 6

    val displayStr = if (player == null) {
        "-"
    } else if (isBengali) {
        when (safeValue) {
            1 -> "১"
            2 -> "২"
            3 -> "৩"
            4 -> "৪"
            5 -> "৫"
            6 -> "৬"
            else -> "$safeValue"
        }
    } else {
        "$safeValue"
    }

    // Resolve Player Display Name per V79 specifications
    val displayName = when {
        player == null -> if (isBengali) "খেলোয়াড় $playerNumber" else "Player $playerNumber"
        player.id == 0 -> "Player 1"
        isOnlineMatch -> {
            if (player.name.isNotBlank() && player.name != "Player ${player.id + 1}") {
                player.name
            } else if (player.isBot) {
                "Nasidul Mistry"
            } else {
                "Player $playerNumber"
            }
        }
        player.isBot -> if (isBengali) "কম্পিউটার (AI)" else "Computer (AI)"
        else -> player.name.ifBlank { "Player $playerNumber" }
    }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        PlayerLevelManager.init(context)
    }
    val playerLevel by PlayerLevelManager.currentLevel.collectAsState()
    val isLocalHuman = player != null && !player.isBot && player.id == localPlayerId

    val actualHouseColor = player?.color ?: houseColor

    // Outside Color Indicator Composable (50dp height matching enlarged dice box cleanly)
    val outsideColorIndicator: @Composable () -> Unit = {
        Surface(
            shape = CircleShape,
            color = actualHouseColor.primaryColor.copy(alpha = if (isCurrentTurn) 0.35f else 0.15f),
            border = BorderStroke(
                width = if (isCurrentTurn) 2.2.dp else 1.2.dp,
                color = actualHouseColor.primaryColor
            ),
            shadowElevation = if (isCurrentTurn) 6.dp else 1.dp,
            modifier = Modifier
                .size(50.dp)
                .testTag("outside_color_indicator_${actualHouseColor.name.lowercase()}")
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Authentic3DLudoPawn(
                    color = actualHouseColor,
                    size = 33.dp,
                    isGlowing = isCurrentTurn
                )
            }
        }
    }

    // Two-Section Player Box Composable (54dp height accommodating +14.6% larger 44dp dice cleanly)
    val twoSectionBox: @Composable (Modifier) -> Unit = { boxModifier ->
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isCurrentTurn) Color(0xFF0C2448) else Color(0xFF071220),
            shadowElevation = if (isCurrentTurn) 8.dp else 1.dp,
            modifier = boxModifier
                .height(54.dp)
                .testTag("player_box_${actualHouseColor.name.lowercase()}")
                .then(
                    if (isCurrentTurn) {
                        Modifier.movingLightRingBorder(
                            playerColor = actualHouseColor.primaryColor,
                            strokeWidth = 2.4.dp,
                            glowWidth = 6.dp,
                            cornerRadius = 12.dp
                        )
                    } else {
                        Modifier.border(
                            BorderStroke(1.dp, actualHouseColor.primaryColor.copy(alpha = if (player != null) 0.30f else 0.15f)),
                            RoundedCornerShape(12.dp)
                        )
                    }
                )
                .clickable(enabled = canRoll, onClick = onRollClick)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isCurrentTurn) {
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF0F2F5A),
                                    Color(0xFF091C36)
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF091526),
                                    Color(0xFF060F1C)
                                )
                            )
                        }
                    )
                    .padding(horizontal = 3.dp, vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // -------------------------------------------------------------
                // SECTION A: Animated Dice Graphic (+14.6% larger 44dp dice for clarity & tap comfort)
                // -------------------------------------------------------------
                Box(
                    modifier = Modifier
                        .weight(1.15f)
                        .fillMaxHeight()
                        .testTag("section_a_dice_${actualHouseColor.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCurrentTurn && player != null) {
                        Glossy3DDiceFace(
                            value = safeValue,
                            size = 44.dp,
                            diceSkin = diceSkin,
                            modifier = Modifier.graphicsLayer {
                                if (isRolling) {
                                    rotationZ = rollRotation
                                    scaleX = rollScale
                                    scaleY = rollScale
                                }
                            }
                        )
                    } else {
                        // Inactive player turn: Dice is HIDDEN per V80 requirement
                        Box(
                            modifier = Modifier
                                .size(27.dp)
                                .clip(RoundedCornerShape(6.5.dp))
                                .background(actualHouseColor.primaryColor.copy(alpha = 0.08f))
                                .border(
                                    BorderStroke(0.8.dp, actualHouseColor.primaryColor.copy(alpha = 0.20f)),
                                    RoundedCornerShape(6.5.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(actualHouseColor.primaryColor.copy(alpha = 0.35f))
                            )
                        }
                    }
                }

                // Divider between Section A and Section B
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight(0.65f)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    actualHouseColor.primaryColor.copy(alpha = if (isCurrentTurn) 0.65f else 0.18f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // -------------------------------------------------------------
                // SECTION B: Rolled Numeric Value (EXCLUSIVE TO CURRENT TURN)
                // -------------------------------------------------------------
                Box(
                    modifier = Modifier
                        .weight(0.85f)
                        .fillMaxHeight()
                        .testTag("section_b_number_${actualHouseColor.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCurrentTurn && player != null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (gamePhase == GamePhase.ROLL_DICE && !isRolling) {
                                Text(
                                    text = if (isBengali) "চালুন" else "ROLL",
                                    color = Color(0xFFFFD54F).copy(alpha = pulseGlow),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp,
                                    style = TextStyle(
                                        shadow = Shadow(
                                            color = Color(0xFFFFD54F).copy(alpha = 0.75f),
                                            offset = Offset(0f, 0f),
                                            blurRadius = 6f
                                        )
                                    )
                                )
                            } else {
                                Text(
                                    text = displayStr,
                                    color = if (isSix) Color(0xFFFFD54F) else Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    lineHeight = 25.sp,
                                    textAlign = TextAlign.Center,
                                    style = TextStyle(
                                        shadow = Shadow(
                                            color = if (isSix) Color(0xFFFFD54F).copy(alpha = 0.85f) else Color(0xFF00E5FF).copy(alpha = 0.50f),
                                            offset = Offset(0f, 0f),
                                            blurRadius = 6f
                                        )
                                    )
                                )
                            }
                        }
                    } else {
                        // Inactive turn: Hide number and roll prompt
                        Text(
                            text = "-",
                            color = Color.White.copy(alpha = 0.20f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    // Player Label Row - Clean Upper Line (no extra flag/circle icons)
    val playerLabelRow: @Composable () -> Unit = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (indicatorOnLeft) Arrangement.Start else Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            if (!indicatorOnLeft && isLocalHuman) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFFFD700),
                    modifier = Modifier.testTag("ludo_local_player_level_badge")
                ) {
                    Text(
                        text = "LVL $playerLevel",
                        color = Color(0xFF031429),
                        fontWeight = FontWeight.Black,
                        fontSize = 8.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = displayName,
                color = if (isCurrentTurn) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Black,
                fontSize = 12.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (indicatorOnLeft && isLocalHuman) {
                Spacer(modifier = Modifier.width(4.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFFFD700),
                    modifier = Modifier.testTag("ludo_local_player_level_badge")
                ) {
                    Text(
                        text = "LVL $playerLevel",
                        color = Color(0xFF031429),
                        fontWeight = FontWeight.Black,
                        fontSize = 8.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }

    // Outer Container: Player Label strictly in UPPER LINE, Box & Token in LOWER LINE
    Column(
        modifier = modifier.testTag("corner_box_slot_$playerNumber"),
        horizontalAlignment = if (indicatorOnLeft) Alignment.Start else Alignment.End
    ) {
        // Line 1: Strictly Player Name/Label in upper line
        playerLabelRow()
        Spacer(modifier = Modifier.height(2.dp))

        // Line 2: Box and Token Indicator aligned properly in lower line
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (indicatorOnLeft) {
                outsideColorIndicator()
                twoSectionBox(Modifier.weight(1f))
            } else {
                twoSectionBox(Modifier.weight(1f))
                outsideColorIndicator()
            }
        }
    }
}

/**
 * Backwards compatible PlayerScoreCard delegating to LudoCornerPlayerBox
 */
@Composable
fun PlayerScoreCard(
    player: Player,
    isCurrentTurn: Boolean,
    currentDiceValue: Int,
    isRolling: Boolean,
    gamePhase: GamePhase,
    isBengali: Boolean = false,
    modifier: Modifier = Modifier,
    teamLabel: String? = null,
    diceSkin: DiceSkin = DiceSkin.ROYAL_GOLD_KING,
    targetTokensToWin: Int = 4,
    isOnlineMatch: Boolean = false,
    onRollClick: () -> Unit = {}
) {
    LudoCornerPlayerBox(
        player = player,
        playerNumber = player.id + 1,
        houseColor = player.color,
        isCurrentTurn = isCurrentTurn,
        currentDiceValue = currentDiceValue,
        isRolling = isRolling,
        gamePhase = gamePhase,
        isBengali = isBengali,
        isOnlineMatch = isOnlineMatch,
        diceSkin = diceSkin,
        targetTokensToWin = targetTokensToWin,
        labelAbove = player.color == LudoColor.RED || player.color == LudoColor.GREEN,
        indicatorOnLeft = player.color == LudoColor.RED || player.color == LudoColor.BLUE,
        onRollClick = onRollClick,
        modifier = modifier
    )
}

/**
 * Dynamic Moving Colorful Light Ring Border Modifier:
 * Cycles a rotating multi-color neon beam continuously around the rounded rectangle border.
 */
@Composable
fun Modifier.movingLightRingBorder(
    playerColor: Color,
    strokeWidth: Dp = 2.8.dp,
    glowWidth: Dp = 6.dp,
    cornerRadius: Dp = 14.dp
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "light_ring_anim")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "light_ring_angle"
    )

    return this.drawWithCache {
        val w = size.width
        val h = size.height
        val strokePx = strokeWidth.toPx()
        val glowPx = glowWidth.toPx()
        val cornerPx = cornerRadius.toPx()
        val cx = w / 2f
        val cy = h / 2f

        // High-energy cycling spectrum anchored by the active player's primary color
        val ringColors = intArrayOf(
            playerColor.toArgb(),
            0xFF00E5FF.toInt(), // Electric Cyan
            0xFFFFD700.toInt(), // Pure Gold
            0xFFFF1744.toInt(), // Electric Coral / Red
            0xFF76FF03.toInt(), // Neon Lime
            0xFFD500F9.toInt(), // Vivid Violet
            0xFF00E5FF.toInt(), // Electric Cyan
            playerColor.toArgb()
        )

        val matrix = Matrix()

        onDrawWithContent {
            // 1. Draw inner content (dark capsule background, number, divider, 3D dice)
            drawContent()

            // 2. Rotate sweep gradient shader matrix smoothly around the box center
            matrix.reset()
            matrix.postRotate(rotationAngle, cx, cy)

            val sweepShader = SweepGradient(cx, cy, ringColors, null).apply {
                setLocalMatrix(matrix)
            }
            val sweepBrush = ShaderBrush(sweepShader)

            // 3. Draw soft exterior ambient glow
            drawRoundRect(
                brush = sweepBrush,
                topLeft = Offset(strokePx / 2f, strokePx / 2f),
                size = Size(w - strokePx, h - strokePx),
                cornerRadius = CornerRadius(cornerPx, cornerPx),
                style = Stroke(width = glowPx),
                alpha = 0.38f
            )

            // 4. Draw sharp, focused moving neon light ring
            drawRoundRect(
                brush = sweepBrush,
                topLeft = Offset(strokePx / 2f, strokePx / 2f),
                size = Size(w - strokePx, h - strokePx),
                cornerRadius = CornerRadius(cornerPx, cornerPx),
                style = Stroke(width = strokePx)
            )
        }
    }
}

/**
 * Active Turn Dice Dock:
 * Rectangular container with dynamic cycling light ring border, bold rolled number, and stable 3D dice.
 */
@Composable
private fun ActiveTurnDiceDock(
    player: Player,
    currentDiceValue: Int,
    isRolling: Boolean,
    gamePhase: GamePhase,
    isBengali: Boolean = false,
    diceSkin: DiceSkin = DiceSkin.ROYAL_GOLD_KING,
    onRollClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canRoll = gamePhase == GamePhase.ROLL_DICE && !player.isBot && !isRolling

    val infiniteTransition = rememberInfiniteTransition(label = "dice_dock_pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.70f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    val safeValue = currentDiceValue.coerceIn(1, 6)
    val isSix = safeValue == 6

    val displayStr = if (isBengali) {
        when (safeValue) {
            1 -> "১"
            2 -> "২"
            3 -> "৩"
            4 -> "৪"
            5 -> "৫"
            6 -> "৬"
            else -> "$safeValue"
        }
    } else {
        "$safeValue"
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF091428),
        shadowElevation = 8.dp,
        modifier = modifier
            .testTag("active_dice_dock_${player.color.name.lowercase()}")
            .movingLightRingBorder(
                playerColor = player.color.primaryColor,
                strokeWidth = 2.8.dp,
                glowWidth = 6.dp,
                cornerRadius = 14.dp
            )
            .clickable(enabled = canRoll, onClick = onRollClick)
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF0B1B36),
                            Color(0xFF061022)
                        )
                    )
                )
                .padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Large Bold Rolled Number on the left
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 3.dp)
            ) {
                Text(
                    text = displayStr,
                    color = if (isSix) Color(0xFFFFD54F) else Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 30.sp,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        shadow = Shadow(
                            color = if (isSix) Color(0xFFFFD54F).copy(alpha = 0.7f) else Color(0xFF00E5FF).copy(alpha = 0.55f),
                            offset = Offset(0f, 0f),
                            blurRadius = 8f
                        )
                    )
                )

                if (canRoll) {
                    Text(
                        text = if (isBengali) "চালুন" else "ROLL",
                        color = Color(0xFFFFD54F).copy(alpha = pulseGlow),
                        fontWeight = FontWeight.Black,
                        fontSize = 8.sp,
                        letterSpacing = 0.6.sp
                    )
                }
            }

            // Divider Line
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(38.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0xFF38BDF8).copy(alpha = 0.65f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Completely Stable 3D Acrylic Dice Asset (+11.9% enlarged to 47dp)
            Glossy3DDiceFace(
                value = safeValue,
                size = 47.dp,
                diceSkin = diceSkin
            )
        }
    }
}

/**
 * Pristine 3D Glossy Acrylic Dice Face (Screenshot 3 Style):
 * Pure glossy white cube with specular reflection shine and vibrant neon-blue pips.
 * Rock-solid stable with ZERO shaking or vibrating.
 */
@Composable
private fun Glossy3DDiceFace(
    value: Int,
    size: Dp = 47.dp,
    diceSkin: DiceSkin = DiceSkin.ROYAL_GOLD_KING,
    modifier: Modifier = Modifier
) {
    val safeValue = value.coerceIn(1, 6)
    val cornerRadius = (size.value * 0.25f).dp
    val innerPadding = (size.value * (3f / 32f)).dp

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // High-end 3D glossy cube face matching Screenshot 3
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(cornerRadius),
                    spotColor = Color(0xFF0284C7).copy(alpha = 0.55f),
                    ambientColor = Color(0xFF001B44)
                )
                .clip(RoundedCornerShape(cornerRadius))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFF8FAFC),
                            Color(0xFFE2E8F0)
                        )
                    )
                )
                .border(
                    width = 1.2.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFBAE6FD),
                            Color(0xFF38BDF8),
                            Color(0xFF0284C7)
                        )
                    ),
                    shape = RoundedCornerShape(cornerRadius)
                )
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            // Pips with deep blue glossy styling scaled proportionally
            DicePips(
                value = safeValue,
                pipColor = Color(0xFF0284C7),
                size = size * 0.75f,
                isSpecial = true
            )

            // Specular gloss reflection across top-left edge
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(this@Canvas.size.width * 0.65f, 0f)
                    lineTo(0f, this@Canvas.size.height * 0.65f)
                    close()
                }
                drawPath(
                    path = path,
                    brush = Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.60f),
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
            }
        }
    }
}
