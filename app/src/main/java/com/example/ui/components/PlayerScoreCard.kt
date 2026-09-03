package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.GamePhase
import com.example.model.LudoColor
import com.example.model.Player

@Composable
fun PlayerScoreCard(
    player: Player,
    isCurrentTurn: Boolean,
    currentDiceValue: Int,
    isRolling: Boolean,
    gamePhase: GamePhase,
    isBengali: Boolean,
    modifier: Modifier = Modifier,
    onRollClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "turn_pulse")
    val pulseBorderAlpha by if (isCurrentTurn) {
        infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(450, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "border_alpha"
        )
    } else {
        remember { androidx.compose.animation.core.Animatable(0f) }.asState()
    }

    val elevation = if (isCurrentTurn) 10.dp else 3.dp
    val backgroundColor by animateColorAsState(
        targetValue = if (isCurrentTurn) player.color.lightColor else Color.White,
        label = "bg_color"
    )

    Card(
        modifier = modifier
            .testTag("player_card_${player.color.name.lowercase()}")
            .shadow(elevation, RoundedCornerShape(16.dp), spotColor = player.color.primaryColor)
            .border(
                width = if (isCurrentTurn) 2.6.dp else 1.5.dp,
                brush = if (isCurrentTurn) {
                    Brush.linearGradient(
                        listOf(
                            Color(0xFFFFD54F),
                            Color(0xFFFFA000),
                            Color(0xFFFFD54F)
                        )
                    )
                } else {
                    Brush.linearGradient(listOf(Color(0xFFFFE082).copy(alpha = 0.6f), Color(0xFFCFD8DC)))
                },
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Player Profile & Token Progress
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with deep gradient + gold rim
                val isKingMistry = !player.isBot && (player.id == 0 || player.name.contains("Mistry", ignoreCase = true) || player.name.contains("King", ignoreCase = true))

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    player.color.glowColor,
                                    player.color.secondaryColor,
                                    player.color.primaryColor,
                                    player.color.deepColor
                                )
                            )
                        )
                        .border(1.8.dp, Color(0xFFFFD54F), CircleShape)
                        .shadow(4.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isKingMistry) {
                        Image(
                            painter = painterResource(id = R.drawable.img_king_mistry_user_avatar_1788345245315),
                            contentDescription = "King Mistry",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (player.isBot) {
                        Icon(
                            imageVector = Icons.Filled.SmartToy,
                            contentDescription = "Bot",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = player.name.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Name, Color tag, and Progress dots
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = player.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isCurrentTurn) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (isCurrentTurn) player.color.darkColor else Color(0xFF263238),
                                fontSize = 13.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (player.rank > 0) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (player.rank) {
                                    1 -> Color(0xFFFFD700)
                                    2 -> Color(0xFFC0C0C0)
                                    3 -> Color(0xFFCD7F32)
                                    else -> Color(0xFF90A4AE)
                                },
                                modifier = Modifier.padding(start = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.EmojiEvents,
                                        contentDescription = "Rank",
                                        tint = Color.Black,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = "#${player.rank}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // 4 Token Progress Indicator diamonds
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        player.tokens.forEach { token ->
                            val (dotBg, dotBorder) = when {
                                token.isFinished -> Pair(
                                    Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFF8F00))),
                                    Color(0xFFE65100)
                                )
                                token.isOnBoard -> Pair(
                                    player.color.getGradientBrush(),
                                    Color.White
                                )
                                else -> Pair(
                                    Brush.linearGradient(listOf(Color(0xFFECEFF1), Color(0xFFCFD8DC))),
                                    Color(0xFFB0BEC5)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(dotBg)
                                    .border(0.8.dp, dotBorder, CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${player.finishedTokensCount}/4",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrentTurn) player.color.darkColor else Color(0xFF78909C)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Right: Dedicated Color Dice Throwing Station ("ছক্কা মারার জায়গা")
            CornerDiceStation(
                player = player,
                isCurrentTurn = isCurrentTurn,
                currentDiceValue = currentDiceValue,
                isRolling = isRolling,
                gamePhase = gamePhase,
                isBengali = isBengali,
                onRollClick = onRollClick
            )
        }
    }
}

/**
 * Dedicated Dice Throwing Pad for each Color Side ("ছক্কা মারার জায়গা")
 */
@Composable
private fun CornerDiceStation(
    player: Player,
    isCurrentTurn: Boolean,
    currentDiceValue: Int,
    isRolling: Boolean,
    gamePhase: GamePhase,
    isBengali: Boolean,
    onRollClick: () -> Unit
) {
    val canRoll = isCurrentTurn && gamePhase == GamePhase.ROLL_DICE && !player.isBot && !isRolling

    val infiniteTransition = rememberInfiniteTransition(label = "corner_dice_pulse")
    val diceBounce by if (canRoll) {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.12f,
            animationSpec = infiniteRepeatable(
                animation = tween(500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dice_bounce"
        )
    } else {
        remember { androidx.compose.animation.core.Animatable(1.0f) }.asState()
    }

    val diceRotation by if (isCurrentTurn && isRolling) {
        infiniteTransition.animateFloat(
            initialValue = -30f,
            targetValue = 30f,
            animationSpec = infiniteRepeatable(
                animation = tween(60, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dice_wobble"
        )
    } else {
        remember { androidx.compose.animation.core.Animatable(0f) }.asState()
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (canRoll) {
            val arrowOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "arrow_offset"
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Roll Arrow",
                tint = Color(0xFFFF9800),
                modifier = Modifier
                    .size(20.dp)
                    .offset(x = arrowOffset.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Interactive Dice Pad Box
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .scale(diceBounce)
                    .rotate(diceRotation)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isCurrentTurn) {
                            Brush.radialGradient(
                                listOf(
                                    Color.White,
                                    player.color.lightColor,
                                    player.color.secondaryColor.copy(alpha = 0.4f)
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                listOf(Color(0xFFF5F5F5), Color(0xFFEEEEEE))
                            )
                        }
                    )
                    .border(
                        width = if (isCurrentTurn) 2.dp else 1.dp,
                        brush = if (isCurrentTurn) {
                            Brush.linearGradient(
                                listOf(Color(0xFFFFD54F), player.color.primaryColor, player.color.deepColor)
                            )
                        } else {
                            Brush.linearGradient(listOf(Color(0xFFE0E0E0), Color(0xFFBDBDBD)))
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    .shadow(if (isCurrentTurn) 6.dp else 1.dp, RoundedCornerShape(12.dp))
                    .clickable(enabled = canRoll, onClick = onRollClick),
                contentAlignment = Alignment.Center
            ) {
            // Dice Pip Display
            val displayValue = if (isCurrentTurn) currentDiceValue else 6
            DicePips(
                value = displayValue.coerceIn(1, 6),
                pipColor = if (isCurrentTurn) {
                    when (displayValue) {
                        6 -> player.color.primaryColor
                        1 -> Color(0xFFC62828)
                        else -> Color(0xFF212121)
                    }
                } else {
                    player.color.primaryColor.copy(alpha = 0.5f)
                },
                size = 30.dp
            )
        }

        // Tiny Label under dice pad
        Text(
            text = if (isCurrentTurn) {
                if (isRolling) (if (isBengali) "ঘুরছে..." else "Rolling...")
                else if (canRoll) (if (isBengali) "ছক্কা মারুন" else "TAP ROLL")
                else if (player.isBot) (if (isBengali) "বট চালছে" else "Bot Turn")
                else (if (isBengali) "চালুন" else "Active")
            } else {
                if (isBengali) "ছক্কা" else "Dice"
            },
            fontSize = 9.sp,
            fontWeight = if (isCurrentTurn) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isCurrentTurn) player.color.darkColor else Color(0xFF9E9E9E)
        )
    }
}
}
