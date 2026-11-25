package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.model.LudoPremiumToken
import com.example.model.LudoToken
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Premium High-Contrast 3D Tournament Pawn Token.
 * - Perfectly centered on its axis (cx, cy) so 360-degree rotation spins in place with ZERO wobble or shift.
 * - Multi-layered high-contrast bezel (Bright White + Polished Metallic Gold + Dark Separation Trench)
 *   ensures the pawn visually pops out sharply against any board tile.
 * - 3D Spherical Domed jewel core with ambient drop shadow.
 * - Turning gear/wheel with 12 perimeter teeth & 4 radial spokes for active movable pawns.
 * - Smooth rewind rotation when captured pawn retreats along its path.
 */
@Composable
fun LudoTokenView(
    token: LudoToken,
    modifier: Modifier = Modifier,
    size: Dp = Dp.Unspecified,
    isDoubleGuard: Boolean = false,
    isActivePlayer: Boolean = false,
    isStacked: Boolean = false,
    premiumToken: LudoPremiumToken? = null,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "token_animations")

    // Continuous 360-degree rotation animation for active movable pawns (spinning wheel on center axis)
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "token_spin_angle"
    )

    // Reverse rewind spin when a pawn gets captured and walks back
    val rewindAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "token_rewind_angle"
    )

    // Smooth halo glow alpha
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.50f,
        targetValue = 0.98f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val sizeMod = if (size != Dp.Unspecified) Modifier.size(size) else Modifier.fillMaxSize()
    val pawnDrawableRes = premiumToken?.getPawnDrawableForColor(token.color)
    val styleDescriptor = premiumToken?.id ?: "classic"

    Box(
        modifier = modifier
            .then(sizeMod)
            .testTag("token_${token.color.name.lowercase()}_${token.id}")
            .semantics {
                contentDescription = "pawn_${token.color.name.lowercase()}_${token.id}_style_$styleDescriptor"
            }
            .graphicsLayer {
                transformOrigin = TransformOrigin.Center
                if (pawnDrawableRes != null) {
                    // Keep upright 3D pawn art upright; subtle pop when highlighted
                    val pop = if (token.isHighlighted) 1.0f + (glowAlpha - 0.5f) * 0.10f else 1.0f
                    scaleX = pop
                    scaleY = pop
                    if (token.isReturning) alpha = 0.85f
                } else {
                    if (token.isHighlighted) {
                        rotationZ = spinAngle
                        scaleX = 1.0f
                        scaleY = 1.0f
                    } else if (token.isReturning) {
                        rotationZ = rewindAngle
                        alpha = 0.88f
                    }
                }
            }
            .clickable(
                enabled = token.isHighlighted,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (pawnDrawableRes != null) {
            // Render upright 3D pawn with turning ground ring/halo under base when active/movable
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = this.size.width
                val h = this.size.height
                val cx = w / 2f
                val baseCenterY = h * 0.78f
                val ringR = min(w, h) * 0.44f

                if (token.isHighlighted) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(
                                Color(0xFFFFD700).copy(alpha = glowAlpha * 0.75f),
                                token.color.glowColor.copy(alpha = glowAlpha * 0.45f),
                                Color.Transparent
                            ),
                            center = Offset(cx, h * 0.56f),
                            radius = ringR * 1.22f
                        ),
                        center = Offset(cx, h * 0.56f),
                        radius = ringR * 1.22f
                    )
                    rotate(degrees = spinAngle, pivot = Offset(cx, baseCenterY)) {
                        for (i in 0 until 10) {
                            val angleRad = Math.toRadians(i * 36.0)
                            val rx = cx + ringR * 0.92f * cos(angleRad).toFloat()
                            val ry = baseCenterY + ringR * 0.36f * sin(angleRad).toFloat()
                            drawCircle(
                                color = Color(0xFFFFE082).copy(alpha = glowAlpha),
                                radius = 2.4f,
                                center = Offset(rx, ry)
                            )
                        }
                    }
                } else if (token.isReturning) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(
                                Color(0xFFFF1744).copy(alpha = 0.55f),
                                Color.Transparent
                            ),
                            center = Offset(cx, h * 0.55f),
                            radius = ringR * 1.15f
                        ),
                        center = Offset(cx, h * 0.55f),
                        radius = ringR * 1.15f
                    )
                }

                // Soft grounded shadow under the 3D pawn pedestal
                drawOval(
                    color = Color.Black.copy(alpha = if (isActivePlayer) 0.38f else 0.28f),
                    topLeft = Offset(cx - w * 0.32f, h * 0.78f),
                    size = Size(w * 0.64f, h * 0.16f)
                )
            }

            Image(
                painter = painterResource(id = pawnDrawableRes),
                contentDescription = "${token.color.titleEn} Pawn ${token.id + 1}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            return@Box
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f
            val r = min(w, h) * 0.485f

            // 1. ACTIVE SELECTION GROUND HALO
            if (token.isHighlighted) {
                val haloRadius = if (isStacked) r * 1.18f else r * 1.35f
                val activeHaloGlow = premiumToken?.glowColor ?: token.color.glowColor
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(
                            Color(0xFFFFD700).copy(alpha = glowAlpha * 0.85f),
                            activeHaloGlow.copy(alpha = glowAlpha * 0.65f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = haloRadius
                    ),
                    center = Offset(cx, cy),
                    radius = haloRadius
                )
            }

            // 2. CAPTURED RETREATING AURA
            if (token.isReturning) {
                val auraRadius = if (isStacked) r * 1.15f else r * 1.30f
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(
                            Color(0xFFFF1744).copy(alpha = 0.65f),
                            Color(0xFF00E5FF).copy(alpha = 0.35f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = auraRadius
                    ),
                    center = Offset(cx, cy),
                    radius = auraRadius
                )
            }

            // 3. 3D ELEVATION DROP SHADOW & FLOATING EFFECT (সবাই উপরে ভেসে ওঠা)
            if (isActivePlayer) {
                val shadowR1 = if (isStacked) r * 1.02f else r * 1.08f
                val shadowR2 = if (isStacked) r * 1.06f else r * 1.15f
                val shadowOffX = if (isStacked) r * 0.03f else r * 0.06f
                val shadowOffY = if (isStacked) r * 0.06f else r * 0.12f
                drawCircle(
                    color = Color(0x60000000),
                    radius = shadowR1,
                    center = Offset(cx + shadowOffX, cy + shadowOffY)
                )
                drawCircle(
                    color = Color(0x35000000),
                    radius = shadowR2,
                    center = Offset(cx + shadowOffX * 1.2f, cy + shadowOffY * 1.2f)
                )
                // Active player subtle ambient elevation ring if not highlighted
                if (!token.isHighlighted && !isStacked) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(
                                token.color.primaryColor.copy(alpha = 0.30f),
                                Color(0xFFFFD54F).copy(alpha = 0.20f),
                                Color.Transparent
                            ),
                            center = Offset(cx, cy),
                            radius = r * 1.18f
                        ),
                        center = Offset(cx, cy),
                        radius = r * 1.18f
                    )
                }
            } else {
                val shadowR = if (isStacked) r * 0.98f else r * 1.04f
                drawCircle(
                    color = Color(0x55000000),
                    radius = shadowR,
                    center = Offset(cx + r * 0.03f, cy + r * 0.06f)
                )
            }

            // 4. DISTINCT HIGH-CONTRAST OUTER BORDER: BRIGHT WHITE / CHROME RING
            // This guarantees the pawn never blends into tiles or neighboring tokens!
            if (isStacked) {
                // Crisp dark separation border to keep adjacent stacked tokens vividly separated
                drawCircle(
                    color = Color(0xFF0F172A),
                    radius = r + 1.6f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 2.4f)
                )
            }
            drawCircle(
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFE2E8F0),
                        Color(0xFFCBD5E1),
                        Color(0xFFFFFFFF)
                    ),
                    start = Offset(cx - r, cy - r),
                    end = Offset(cx + r, cy + r)
                ),
                radius = r,
                center = Offset(cx, cy)
            )

            // 5. METALLIC GOLD / PREMIUM BEZEL RIM
            val bezelList = if (premiumToken != null) {
                listOf(
                    Color(0xFFFFF9C4),
                    premiumToken.ringBorderColor,
                    Color(0xFFFFD54F),
                    premiumToken.ringBorderColor,
                    Color(0xFFFFF9C4)
                )
            } else {
                listOf(
                    Color(0xFFFFE082),
                    Color(0xFFFFA000),
                    Color(0xFFFFD54F),
                    Color(0xFFFF8F00),
                    Color(0xFFFFF9C4),
                    Color(0xFFFFE082)
                )
            }
            drawCircle(
                brush = Brush.sweepGradient(bezelList, center = Offset(cx, cy)),
                radius = r * 0.91f,
                center = Offset(cx, cy)
            )

            // 6. GEAR TEETH & SPOKES (TURNING WHEEL EFFECT FOR ACTIVE MOVABLE PAWNS)
            if (token.isHighlighted) {
                val numCogs = 12
                val innerCogR = r * 0.82f
                val outerCogR = r * 0.99f
                for (i in 0 until numCogs) {
                    val angleRad = Math.toRadians((i * (360.0 / numCogs)))
                    val x1 = cx + innerCogR * cos(angleRad).toFloat()
                    val y1 = cy + innerCogR * sin(angleRad).toFloat()
                    val x2 = cx + outerCogR * cos(angleRad).toFloat()
                    val y2 = cy + outerCogR * sin(angleRad).toFloat()
                    drawLine(
                        brush = Brush.linearGradient(
                            listOf(Color(0xFFFFF9C4), Color(0xFFFF8F00))
                        ),
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = 3.6f,
                        cap = StrokeCap.Round
                    )
                }

                // 4 Turning Wheel Radial Spokes
                for (s in 0 until 4) {
                    val spokeRad = Math.toRadians((s * 90.0))
                    val sx1 = cx + (r * 0.28f) * cos(spokeRad).toFloat()
                    val sy1 = cy + (r * 0.28f) * sin(spokeRad).toFloat()
                    val sx2 = cx + (r * 0.74f) * cos(spokeRad).toFloat()
                    val sy2 = cy + (r * 0.74f) * sin(spokeRad).toFloat()
                    drawLine(
                        color = Color(0xFFFFE082).copy(alpha = glowAlpha * 0.95f),
                        start = Offset(sx1, sy1),
                        end = Offset(sx2, sy2),
                        strokeWidth = 2.4f,
                        cap = StrokeCap.Round
                    )
                }
            }

            // 7. OBSIDIAN CONTRAST TRENCH (Separates gold rim from internal player color)
            drawCircle(
                color = Color(0xFF0F172A),
                radius = r * 0.77f,
                center = Offset(cx, cy),
                style = Stroke(width = 2.4f)
            )

            // 8. 3D SPHERICAL DOMED CORE (Preserve each player's Red/Yellow/Green/Blue color identity!)
            val domeR = r * 0.74f
            val domeColors = listOf(
                Color.White.copy(alpha = 0.92f),
                token.color.secondaryColor,
                token.color.primaryColor,
                token.color.deepColor,
                Color(0xFF0A0A0A)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    domeColors,
                    center = Offset(cx - domeR * 0.32f, cy - domeR * 0.32f),
                    radius = domeR * 1.35f
                ),
                radius = domeR,
                center = Offset(cx, cy)
            )

            // Subtle 3D Inner Shadow rim around the dome
            drawCircle(
                color = Color(0x33000000),
                radius = domeR,
                center = Offset(cx, cy),
                style = Stroke(width = 2.0f)
            )

            // Gloss Specular Highlight
            drawOval(
                brush = Brush.radialGradient(
                    listOf(Color.White.copy(alpha = 0.85f), Color.White.copy(alpha = 0f)),
                    center = Offset(cx - domeR * 0.30f, cy - domeR * 0.36f),
                    radius = domeR * 0.38f
                ),
                topLeft = Offset(cx - domeR * 0.52f, cy - domeR * 0.58f),
                size = Size(domeR * 0.50f, domeR * 0.32f)
            )

            // 9. CENTER ROYAL EMBLEM: KING CROWN / STAR / DEFENSE SHIELD
            if (isDoubleGuard) {
                // Double Token Guard Shield Emblem
                val shieldR = domeR * 0.55f
                drawCircle(
                    color = Color(0xFF0D47A1),
                    radius = shieldR,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFFFD54F), Color(0xFFFF8F00))
                    ),
                    radius = shieldR,
                    center = Offset(cx, cy),
                    style = Stroke(width = 2.2f)
                )
                // Center Star of Defense
                drawCircle(
                    color = Color.White,
                    radius = shieldR * 0.36f,
                    center = Offset(cx, cy)
                )
            } else if (premiumToken == null) {
                // Center Golden Crown of the King (when standard pawn)
                val crownW = domeR * 0.62f
                val crownH = domeR * 0.44f
                val crownTop = cy - crownH * 0.45f
                val crownBottom = cy + crownH * 0.55f

                val crownPath = Path().apply {
                    moveTo(cx - crownW * 0.48f, crownBottom)
                    lineTo(cx + crownW * 0.48f, crownBottom)
                    lineTo(cx + crownW * 0.48f, crownTop + crownH * 0.28f)
                    lineTo(cx + crownW * 0.24f, crownTop + crownH * 0.55f)
                    lineTo(cx, crownTop)
                    lineTo(cx - crownW * 0.24f, crownTop + crownH * 0.55f)
                    lineTo(cx - crownW * 0.48f, crownTop + crownH * 0.28f)
                    close()
                }

                // Crown 3D Gold Fill
                drawPath(
                    path = crownPath,
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFFFF9C4), Color(0xFFFFD54F), Color(0xFFFF8F00)),
                        start = Offset(cx, crownTop),
                        end = Offset(cx, crownBottom)
                    )
                )

                // Crown Crisp Outline
                drawPath(
                    path = crownPath,
                    color = Color(0xFF3E2723),
                    style = Stroke(width = 1.4f)
                )

                // 3 Crown Jewels (Left, Center, Right) with proportional radius
                val jewelR = (domeR * 0.08f).coerceIn(1.2f, 2.5f)
                drawCircle(
                    color = Color.White,
                    radius = jewelR,
                    center = Offset(cx - crownW * 0.48f, crownTop + crownH * 0.28f)
                )
                drawCircle(
                    color = Color(0xFFFFEB3B),
                    radius = jewelR * 1.15f,
                    center = Offset(cx, crownTop)
                )
                drawCircle(
                    color = Color.White,
                    radius = jewelR,
                    center = Offset(cx + crownW * 0.48f, crownTop + crownH * 0.28f)
                )
            }
        }

        // Center Premium Token Symbol Emblem (Dynamic selected token emoji)
        if (premiumToken != null && !isDoubleGuard) {
            Text(
                text = premiumToken.symbolEmoji,
                fontSize = if (size != Dp.Unspecified) (size.value * 0.38f).sp else 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
