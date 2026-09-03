package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import com.example.model.LudoToken

/**
 * Classic Arcade Ludo Pawn Token matching the second screenshot.
 * Features a shiny 3D spherical head, tapered pin body, base skirt,
 * and animated golden selection aura.
 */
@Composable
fun LudoTokenView(
    token: LudoToken,
    size: Dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by if (token.isHighlighted) {
        infiniteTransition.animateFloat(
            initialValue = 1.06f,
            targetValue = 1.26f,
            animationSpec = infiniteRepeatable(
                animation = tween(450, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
    } else {
        remember { androidx.compose.animation.core.Animatable(1.0f) }.asState()
    }

    val glowAlpha by if (token.isHighlighted) {
        infiniteTransition.animateFloat(
            initialValue = 0.45f,
            targetValue = 0.95f,
            animationSpec = infiniteRepeatable(
                animation = tween(450, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow_alpha"
        )
    } else {
        remember { androidx.compose.animation.core.Animatable(0f) }.asState()
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size)
            .scale(pulseScale)
            .testTag("token_${token.color.name.lowercase()}_${token.id}")
            .clickable(
                enabled = token.isHighlighted,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f

            // 1. Glowing selection halo if highlighted
            if (token.isHighlighted) {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(
                            Color(0xFFFFD700).copy(alpha = glowAlpha),
                            token.color.glowColor.copy(alpha = glowAlpha * 0.6f),
                            Color.Transparent
                        ),
                        center = Offset(cx, h * 0.52f),
                        radius = w * 0.64f
                    ),
                    center = Offset(cx, h * 0.52f),
                    radius = w * 0.64f
                )
            }

            // 2. Drop shadow under token base
            drawOval(
                color = Color(0x66000000),
                topLeft = Offset(cx - w * 0.36f, h * 0.74f),
                size = Size(w * 0.72f, h * 0.22f)
            )

            // 3. Pawn Base Skirt
            val skirtPath = Path().apply {
                moveTo(cx - w * 0.20f, h * 0.46f)
                cubicTo(
                    cx - w * 0.22f, h * 0.64f,
                    cx - w * 0.38f, h * 0.76f,
                    cx - w * 0.38f, h * 0.84f
                )
                quadraticBezierTo(cx, h * 0.94f, cx + w * 0.38f, h * 0.84f)
                cubicTo(
                    cx + w * 0.38f, h * 0.76f,
                    cx + w * 0.22f, h * 0.64f,
                    cx + w * 0.20f, h * 0.46f
                )
                close()
            }

            // Fill Skirt with 3D gradient
            drawPath(
                path = skirtPath,
                brush = Brush.linearGradient(
                    listOf(
                        token.color.secondaryColor,
                        token.color.primaryColor,
                        token.color.deepColor
                    ),
                    start = Offset(cx - w * 0.38f, h * 0.5f),
                    end = Offset(cx + w * 0.38f, h * 0.9f)
                )
            )

            // Skirt metallic highlight rim
            drawPath(
                path = skirtPath,
                brush = Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.8f), token.color.secondaryColor, token.color.darkColor)
                ),
                style = Stroke(width = 1.8f)
            )

            // 4. Base rim oval
            drawOval(
                brush = Brush.linearGradient(
                    listOf(token.color.secondaryColor, token.color.primaryColor, token.color.deepColor)
                ),
                topLeft = Offset(cx - w * 0.36f, h * 0.78f),
                size = Size(w * 0.72f, h * 0.14f)
            )
            drawOval(
                color = Color.White.copy(alpha = 0.7f),
                topLeft = Offset(cx - w * 0.36f, h * 0.78f),
                size = Size(w * 0.72f, h * 0.14f),
                style = Stroke(width = 1.4f)
            )

            // 5. Spherical Head (Pawn Pin Top)
            val headRadius = w * 0.26f
            val headCenter = Offset(cx, h * 0.34f)

            // Head drop shadow on skirt
            drawCircle(
                color = Color(0x44000000),
                radius = headRadius,
                center = Offset(headCenter.x + 1f, headCenter.y + 2f)
            )

            // Head 3D Sphere Fill
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(
                        Color.White.copy(alpha = 0.9f),
                        token.color.secondaryColor,
                        token.color.primaryColor,
                        token.color.deepColor
                    ),
                    center = Offset(headCenter.x - headRadius * 0.3f, headCenter.y - headRadius * 0.35f),
                    radius = headRadius * 1.25f
                ),
                radius = headRadius,
                center = headCenter
            )

            // Golden / White Outline on Head
            drawCircle(
                brush = Brush.linearGradient(
                    listOf(Color.White, Color(0xFFFFD54F), token.color.deepColor)
                ),
                radius = headRadius,
                center = headCenter,
                style = Stroke(width = 1.8f)
            )

            // Specular shiny glint
            drawOval(
                brush = Brush.radialGradient(
                    listOf(Color.White, Color.White.copy(alpha = 0f)),
                    center = Offset(headCenter.x - headRadius * 0.25f, headCenter.y - headRadius * 0.35f),
                    radius = headRadius * 0.35f
                ),
                topLeft = Offset(headCenter.x - headRadius * 0.45f, headCenter.y - headRadius * 0.55f),
                size = Size(headRadius * 0.55f, headRadius * 0.38f)
            )
        }
    }
}
