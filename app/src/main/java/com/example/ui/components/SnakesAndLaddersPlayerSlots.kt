package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.PlayerLevelManager
import com.example.model.DiceSkin
import com.example.model.LudoColor
import com.example.model.SnakesAndLaddersPlayer
import com.example.model.SnakesAndLaddersState

/**
 * Top Dynamic Player Slots for Snakes & Ladders:
 * - 2 Players: Displays Player 2 centered.
 * - 3 Players: Displays Player 2 (left) and Player 3 (right).
 * - 4 Players: Displays Player 2 (left) and Player 3 (right).
 */
@Composable
fun SnakesAndLaddersTopPlayerSlots(
    state: SnakesAndLaddersState,
    isBengali: Boolean,
    diceSkin: DiceSkin = DiceSkin.CLASSIC_WHITE,
    localPlayerId: Int = 0,
    onRollClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val players = state.players
    val p2 = players.getOrNull(1) // Top-Left: Player 2 (Green)
    val p3 = players.getOrNull(2) // Top-Right: Player 3 (Yellow)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (p2 != null) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                SnakesAndLaddersPlayerCard(
                    player = p2,
                    isCurrentTurn = state.currentTurnIndex == 1,
                    currentDiceValue = if (state.isRolling) state.rollingDisplayValue else state.diceValue,
                    isRolling = state.isRolling,
                    isBengali = isBengali,
                    diceSkin = diceSkin,
                    isLocalPlayer = !p2.isBot && p2.id == localPlayerId,
                    onRollClick = onRollClick,
                    modifier = Modifier.fillMaxWidth(0.90f)
                )
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        if (p3 != null) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                SnakesAndLaddersPlayerCard(
                    player = p3,
                    isCurrentTurn = state.currentTurnIndex == 2,
                    currentDiceValue = if (state.isRolling) state.rollingDisplayValue else state.diceValue,
                    isRolling = state.isRolling,
                    isBengali = isBengali,
                    diceSkin = diceSkin,
                    isLocalPlayer = !p3.isBot && p3.id == localPlayerId,
                    onRollClick = onRollClick,
                    modifier = Modifier.fillMaxWidth(0.90f)
                )
            }
        } else if (players.size > 2) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

/**
 * Bottom Dynamic Player Slots for Snakes & Ladders:
 * - Bottom-Left: Player 1 (Red)
 * - Bottom-Right: Player 4 (Blue, if present)
 */
@Composable
fun SnakesAndLaddersBottomPlayerSlots(
    state: SnakesAndLaddersState,
    isBengali: Boolean,
    diceSkin: DiceSkin = DiceSkin.CLASSIC_WHITE,
    localPlayerId: Int = 0,
    onRollClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val players = state.players
    val p1 = players.getOrNull(0) // Bottom-Left: Player 1 (Red)
    val p4 = players.getOrNull(3) // Bottom-Right: Player 4 (Blue)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (p1 != null) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                SnakesAndLaddersPlayerCard(
                    player = p1,
                    isCurrentTurn = state.currentTurnIndex == 0,
                    currentDiceValue = if (state.isRolling) state.rollingDisplayValue else state.diceValue,
                    isRolling = state.isRolling,
                    isBengali = isBengali,
                    diceSkin = diceSkin,
                    isLocalPlayer = !p1.isBot && p1.id == localPlayerId,
                    onRollClick = onRollClick,
                    modifier = Modifier.fillMaxWidth(0.90f)
                )
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        if (p4 != null) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                SnakesAndLaddersPlayerCard(
                    player = p4,
                    isCurrentTurn = state.currentTurnIndex == 3,
                    currentDiceValue = if (state.isRolling) state.rollingDisplayValue else state.diceValue,
                    isRolling = state.isRolling,
                    isBengali = isBengali,
                    diceSkin = diceSkin,
                    isLocalPlayer = !p4.isBot && p4.id == localPlayerId,
                    onRollClick = onRollClick,
                    modifier = Modifier.fillMaxWidth(0.90f)
                )
            }
        } else if (players.size > 3) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

/**
 * Sleek International Player Profile Card with authentic 3D Ludo token,
 * live board progress bar, PUT start lock indicator, and integrated 3D turn dice dock.
 */
@Composable
fun SnakesAndLaddersPlayerCard(
    player: SnakesAndLaddersPlayer,
    isCurrentTurn: Boolean,
    currentDiceValue: Int,
    isRolling: Boolean,
    isBengali: Boolean,
    diceSkin: DiceSkin = DiceSkin.CLASSIC_WHITE,
    isLocalPlayer: Boolean = !player.isBot && player.id == 0,
    onRollClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        PlayerLevelManager.init(context)
    }
    val playerLevel by PlayerLevelManager.currentLevel.collectAsState()
    val canRoll = isCurrentTurn && !player.isBot && !isRolling

    val infiniteTransition = rememberInfiniteTransition(label = "player_card_anim")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    val safeDice = currentDiceValue.coerceIn(1, 6)

    Surface(
        shape = RoundedCornerShape(12.6.dp),
        color = Color(0xFF091426),
        shadowElevation = if (isCurrentTurn) 9.dp else 2.7.dp,
        modifier = modifier
            .testTag("sl_player_card_${player.id}")
            .then(
                if (isCurrentTurn) {
                    Modifier.movingLightRingBorder(
                        playerColor = player.color.primaryColor,
                        strokeWidth = 2.34.dp,
                        glowWidth = 5.4.dp,
                        cornerRadius = 12.6.dp
                    )
                } else {
                    Modifier.border(
                        BorderStroke(1.08.dp, player.color.primaryColor.copy(alpha = 0.35f)),
                        RoundedCornerShape(12.6.dp)
                    )
                }
            )
            .clickable(enabled = canRoll, onClick = onRollClick)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0B1930),
                            Color(0xFF071120)
                        )
                    )
                )
                .padding(all = 7.2.dp),
            verticalArrangement = Arrangement.spacedBy(5.4.dp)
        ) {
            // TOP ROW: Classic Colored Ludo Token + Clean Single White 3D Dice scaled proportionally to 90%
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 1.8.dp, vertical = 1.8.dp)
            ) {
                val fitFactor = (maxWidth.value / (57.6f + 79.2f)).coerceIn(0.75f, 1.0f)
                val pawnBoxSize = (57.6f * fitFactor).dp
                val pawnSize = (52.2f * fitFactor).dp
                val diceBoxSize = (79.2f * fitFactor).dp
                val diceSize = (70.2f * fitFactor).dp

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Classic Colored Ludo Token (90% of 64dp / 58dp)
                    Box(
                        modifier = Modifier.size(pawnBoxSize),
                        contentAlignment = Alignment.Center
                    ) {
                        Authentic3DLudoPawn(
                            color = player.color,
                            size = pawnSize,
                            isGlowing = isCurrentTurn
                        )
                    }

                    // Right: Active Player-Only Dice Display (90% of 88dp / 78dp)
                    if (isCurrentTurn) {
                        Box(
                            modifier = Modifier.size(diceBoxSize),
                            contentAlignment = Alignment.Center
                        ) {
                            CleanSingleWhiteDice(
                                value = safeDice,
                                isRolling = isRolling,
                                isCurrentTurn = true,
                                size = diceSize
                            )
                        }
                    } else {
                        // INACTIVE PLAYERS: DICE BOX IS HIDDEN (90% matching container)
                        Box(
                            modifier = Modifier
                                .size(diceBoxSize)
                                .padding((3.6f * fitFactor).dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape((9f * fitFactor).dp),
                                color = Color(0xFF0F172A).copy(alpha = 0.85f),
                                border = BorderStroke(0.9.dp, player.color.primaryColor.copy(alpha = 0.45f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(
                                        horizontal = (7.2f * fitFactor).dp,
                                        vertical = (3.6f * fitFactor).dp
                                    ),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = if (isBengali) "ঘর" else "TILE",
                                        color = Color(0xFF94A3B8),
                                        fontSize = (8.1f * fitFactor).sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${player.position.coerceAtLeast(1)}",
                                        color = Color(0xFFFFD700),
                                        fontSize = (18f * fitFactor).sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // BOTTOM ROW: Only the player label ("Player 1", "Player 2", "Player 3", "Player 4")
            val standardPlayerLabel = if (isBengali) {
                when (player.id) {
                    0 -> "খেলোয়াড় ১"
                    1 -> "খেলোয়াড় ২"
                    2 -> "খেলোয়াড় ৩"
                    3 -> "খেলোয়াড় ৪"
                    else -> "খেলোয়াড় ${player.id + 1}"
                }
            } else {
                "Player ${player.id + 1}"
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(7.2.dp))
                    .background(Color(0xFF060E1A).copy(alpha = 0.75f))
                    .padding(horizontal = 5.4.dp, vertical = 3.6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.3.dp)
                            .clip(CircleShape)
                            .background(player.color.primaryColor)
                    )
                    Spacer(modifier = Modifier.width(4.5.dp))
                    Text(
                        text = standardPlayerLabel,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                    if (isLocalPlayer) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(3.5.dp),
                            color = Color(0xFFFFD700),
                            modifier = Modifier.testTag("sl_local_player_level_badge")
                        ) {
                            Text(
                                text = "LVL $playerLevel",
                                color = Color(0xFF031429),
                                fontWeight = FontWeight.Black,
                                fontSize = 7.5.sp,
                                modifier = Modifier.padding(horizontal = 3.5.dp, vertical = 0.5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Authentic 3D Ludo Token / Pawn drawing with flared pedestal base,
 * sculpted waist, polished metallic gold collar, and jewel dome head with specular shine.
 */
@Composable
fun Authentic3DLudoPawn(
    color: LudoColor,
    size: Dp = 32.dp,
    isGlowing: Boolean = false,
    pawnDrawableRes: Int? = null,
    modifier: Modifier = Modifier
) {
    if (pawnDrawableRes != null) {
        Box(
            modifier = modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = pawnDrawableRes),
                contentDescription = "${color.titleEn} Pawn",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )
        }
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pawn_anim")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pawn_glow"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f

            // 1. Glowing ambient halo if active
            if (isGlowing) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color.glowColor.copy(alpha = glowAlpha * 0.75f),
                            Color.Transparent
                        ),
                        center = Offset(cx, h * 0.55f),
                        radius = w * 0.52f
                    ),
                    center = Offset(cx, h * 0.55f),
                    radius = w * 0.52f
                )
            }

            // 2. Drop Shadow under flared pedestal
            drawOval(
                color = Color.Black.copy(alpha = 0.45f),
                topLeft = Offset(cx - w * 0.38f, h * 0.78f),
                size = androidx.compose.ui.geometry.Size(w * 0.76f, h * 0.18f)
            )

            // 3. Flared Conical 3D Pedestal Base
            val baseTopY = h * 0.62f
            val baseBottomY = h * 0.88f
            val baseTopHalfW = w * 0.24f
            val baseBottomHalfW = w * 0.38f

            val basePath = Path().apply {
                moveTo(cx - baseTopHalfW, baseTopY)
                lineTo(cx + baseTopHalfW, baseTopY)
                lineTo(cx + baseBottomHalfW, baseBottomY)
                lineTo(cx - baseBottomHalfW, baseBottomY)
                close()
            }
            drawPath(
                path = basePath,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        color.darkColor,
                        color.primaryColor,
                        color.lightColor,
                        color.primaryColor,
                        color.deepColor
                    ),
                    startX = cx - baseBottomHalfW,
                    endX = cx + baseBottomHalfW
                )
            )

            // Bottom base rim ring
            drawOval(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.5f), color.lightColor, color.darkColor)
                ),
                topLeft = Offset(cx - baseBottomHalfW, baseBottomY - h * 0.05f),
                size = androidx.compose.ui.geometry.Size(baseBottomHalfW * 2f, h * 0.09f)
            )

            // 4. Polished 3D Gold Collar Ring
            val collarY = h * 0.52f
            val collarHalfW = w * 0.20f
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color(0xFFB78103),
                        Color(0xFFFFD700),
                        Color(0xFFFFF9C4),
                        Color(0xFFFFD700),
                        Color(0xFFB78103)
                    )
                ),
                topLeft = Offset(cx - collarHalfW, collarY),
                size = androidx.compose.ui.geometry.Size(collarHalfW * 2f, h * 0.10f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
            )

            // 5. Spherical Jewel Dome Head
            val headRadius = w * 0.24f
            val headCenter = Offset(cx, h * 0.30f)

            // Main head circle with rich volumetric 3D radial gradient
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        color.lightColor,
                        color.primaryColor,
                        color.darkColor,
                        color.deepColor
                    ),
                    center = Offset(headCenter.x - headRadius * 0.35f, headCenter.y - headRadius * 0.35f),
                    radius = headRadius * 1.35f
                ),
                radius = headRadius,
                center = headCenter
            )

            // Top-left crisp specular gloss reflection dot
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = headRadius * 0.25f,
                center = Offset(headCenter.x - headRadius * 0.35f, headCenter.y - headRadius * 0.35f)
            )
        }
    }
}

/**
 * Clean Single White 3D Dice:
 * - 100% transparent container background (zero yellow/duplicate background dice or glow).
 * - Only ONE clean, pristine 3D white dice cube.
 * - Smooth isometric lighting, subtle specular highlights, and crisp authentic pips.
 * - Gentle animated wobble rotation during active roll.
 */
@Composable
fun CleanSingleWhiteDice(
    value: Int,
    isRolling: Boolean,
    isCurrentTurn: Boolean,
    size: Dp = 76.dp,
    modifier: Modifier = Modifier
) {
    val safeValue = value.coerceIn(1, 6)
    val infiniteTransition = rememberInfiniteTransition(label = "clean_dice_roll_anim")

    // Smooth continuous 360-degree tumbling roll around Z axis
    val rotationZAngle by if (isRolling) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(280, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "clean_dice_rot_360"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    // Dynamic 3D tumbling rotation on X and Y axes
    val rotationXAngle by if (isRolling) {
        infiniteTransition.animateFloat(
            initialValue = -35f,
            targetValue = 35f,
            animationSpec = infiniteRepeatable(
                animation = tween(140, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "clean_dice_rot_x"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    val rotationYAngle by if (isRolling) {
        infiniteTransition.animateFloat(
            initialValue = -35f,
            targetValue = 35f,
            animationSpec = infiniteRepeatable(
                animation = tween(180, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "clean_dice_rot_y"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    // Dynamic bounce/pulse scaling during roll
    val tumbleScale by if (isRolling) {
        infiniteTransition.animateFloat(
            initialValue = 0.90f,
            targetValue = 1.10f,
            animationSpec = infiniteRepeatable(
                animation = tween(140, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dice_tumble_scale"
        )
    } else {
        remember { mutableFloatStateOf(1.0f) }
    }

    val cornerRadius = (size.value * 0.18f).dp
    val diceBorderWidth = (size.value * (1.8f / 78f)).dp
    val diceShadowElevation = if (isRolling) {
        (size.value * (8f / 78f)).dp
    } else {
        (size.value * (4f / 78f)).dp
    }

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                rotationZ = rotationZAngle
                rotationX = rotationXAngle
                rotationY = rotationYAngle
                scaleX = tumbleScale
                scaleY = tumbleScale
                cameraDistance = 14f * density
            },
        contentAlignment = Alignment.Center
    ) {
        // Pure single 3D white dice cube (Zero duplicate dice, Zero background overlay)
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = diceShadowElevation,
                    shape = RoundedCornerShape(cornerRadius),
                    spotColor = Color(0xFF0F172A).copy(alpha = 0.35f),
                    ambientColor = Color.Black.copy(alpha = 0.20f)
                )
                .border(
                    width = diceBorderWidth,
                    brush = Brush.linearGradient(
                        listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFE2E8F0),
                            Color(0xFFCBD5E1),
                            Color(0xFF94A3B8)
                        )
                    ),
                    shape = RoundedCornerShape(cornerRadius)
                ),
            shape = RoundedCornerShape(cornerRadius),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFFFFFFF),
                                Color(0xFFF8FAFC),
                                Color(0xFFF1F5F9),
                                Color(0xFFE2E8F0)
                            )
                        )
                    )
            ) {
                // Subtle 3D volumetric specular highlights & bevel
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = this.size.width
                    val h = this.size.height

                    // Top-left glossy specular shine
                    val glossPath = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(w * 0.70f, 0f)
                        lineTo(0f, h * 0.70f)
                        close()
                    }
                    drawPath(
                        path = glossPath,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.65f),
                                Color.White.copy(alpha = 0.18f),
                                Color.Transparent
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(w * 0.70f, h * 0.70f)
                        )
                    )

                    // Bottom-right subtle edge cast shadow for 3D depth
                    val shadowPath = Path().apply {
                        moveTo(w, h * 0.35f)
                        lineTo(w, h)
                        lineTo(w * 0.35f, h)
                        close()
                    }
                    drawPath(
                        path = shadowPath,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFF64748B).copy(alpha = 0.25f)
                            ),
                            start = Offset(w * 0.35f, h * 0.35f),
                            end = Offset(w, h)
                        )
                    )
                }

                // Clean authentic dice pips
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding((size.value * 0.12f).dp),
                    contentAlignment = Alignment.Center
                ) {
                    DicePips(
                        value = safeValue,
                        pipColor = Color(0xFF0284C7),
                        size = size * 0.74f,
                        isSpecial = true
                    )
                }
            }
        }
    }
}
