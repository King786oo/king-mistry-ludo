package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.LocalFireDepartment
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LudoColor

@Composable
fun DiceView(
    diceValue: Int,
    isRolling: Boolean,
    playerColor: LudoColor,
    isRollEnabled: Boolean,
    consecutiveSixes: Int = 0,
    size: Dp = 68.dp,
    modifier: Modifier = Modifier,
    onRollClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dice_wobble")

    val rotation by if (isRolling) {
        infiniteTransition.animateFloat(
            initialValue = -35f,
            targetValue = 35f,
            animationSpec = infiniteRepeatable(
                animation = tween(60, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dice_rot"
        )
    } else {
        remember { androidx.compose.animation.core.Animatable(0f) }.asState()
    }

    val rollScale by if (isRolling) {
        infiniteTransition.animateFloat(
            initialValue = 0.92f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(120, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dice_roll_scale"
        )
    } else if (isRollEnabled) {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(550, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dice_bounce"
        )
    } else {
        remember { androidx.compose.animation.core.Animatable(1.0f) }.asState()
    }

    val glowAlpha by if (isRollEnabled && !isRolling) {
        infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
                animation = tween(550, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dice_glow"
        )
    } else {
        remember { androidx.compose.animation.core.Animatable(0.2f) }.asState()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .scale(rollScale)
                .rotate(rotation)
                .testTag("dice_roller")
                .clickable(
                    enabled = isRollEnabled && !isRolling,
                    onClick = onRollClick
                ),
            contentAlignment = Alignment.Center
        ) {
            // Outer glowing ring matching player's signature color
            if (isRollEnabled || isRolling) {
                Box(
                    modifier = Modifier
                        .size(size + 16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    playerColor.glowColor.copy(alpha = glowAlpha),
                                    playerColor.primaryColor.copy(alpha = glowAlpha * 0.5f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // Dice Body (Ivory White 3D Cube with rich gold/player border)
            Surface(
                modifier = Modifier
                    .size(size)
                    .shadow(
                        elevation = if (isRolling) 14.dp else 6.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = playerColor.deepColor
                    )
                    .border(
                        width = if (isRollEnabled) 3.dp else 1.5.dp,
                        brush = if (isRollEnabled) {
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFFFFD54F),
                                    playerColor.primaryColor,
                                    playerColor.deepColor
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                listOf(Color.White, Color(0xFFE0E0E0), Color(0xFFCFD8DC))
                            )
                        },
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFCFCFC)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFFFFFFF), Color(0xFFF0F2F5))
                            )
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DicePips(
                        value = diceValue.coerceIn(1, 6),
                        pipColor = when (diceValue) {
                            6 -> playerColor.primaryColor
                            1 -> Color(0xFFD32F2F)
                            else -> Color(0xFF212121)
                        },
                        size = size - 16.dp
                    )
                }
            }
        }

        // Consecutive 6s Flame Indicator dots
        if (consecutiveSixes > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                repeat(consecutiveSixes.coerceAtMost(2)) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = "Six Streak",
                        tint = Color(0xFFFF6D00),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DicePips(
    value: Int,
    pipColor: Color,
    size: Dp
) {
    val pipSize = (size.value / 4.2).dp

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        when (value) {
            1 -> {
                Pip(pipColor, pipSize * 1.3f)
            }
            2 -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                        Pip(pipColor, pipSize)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Pip(pipColor, pipSize)
                    }
                }
            }
            3 -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                        Pip(pipColor, pipSize)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Pip(pipColor, pipSize)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Pip(pipColor, pipSize)
                    }
                }
            }
            4 -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Pip(pipColor, pipSize)
                        Pip(pipColor, pipSize)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Pip(pipColor, pipSize)
                        Pip(pipColor, pipSize)
                    }
                }
            }
            5 -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Pip(pipColor, pipSize)
                            Pip(pipColor, pipSize)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Pip(pipColor, pipSize)
                            Pip(pipColor, pipSize)
                        }
                    }
                    Box(modifier = Modifier.align(Alignment.Center)) {
                        Pip(pipColor, pipSize)
                    }
                }
            }
            6 -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Pip(pipColor, pipSize)
                        Pip(pipColor, pipSize)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Pip(pipColor, pipSize)
                        Pip(pipColor, pipSize)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Pip(pipColor, pipSize)
                        Pip(pipColor, pipSize)
                    }
                }
            }
        }
    }
}

@Composable
private fun Pip(color: Color, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .shadow(2.dp, CircleShape)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(
                        color.copy(alpha = 0.9f),
                        color,
                        Color.Black.copy(alpha = 0.2f)
                    )
                )
            )
            .border(0.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
    )
}
