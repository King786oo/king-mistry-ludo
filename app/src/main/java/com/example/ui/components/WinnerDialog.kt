package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.Player
import kotlin.random.Random

data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val size: Float,
    val color: Color,
    val speedY: Float,
    val speedX: Float
)

@Composable
fun WinnerDialog(
    ranking: List<Player>,
    isBengali: Boolean,
    onPlayAgain: () -> Unit,
    onBackToMenu: () -> Unit
) {
    val context = LocalContext.current
    val scaleAnim = remember { Animatable(0.7f) }
    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        )
    }

    val firstWinner = ranking.firstOrNull()

    Dialog(onDismissRequest = {}) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scaleAnim.value),
            contentAlignment = Alignment.Center
        ) {
            // Confetti Canvas in background
            ConfettiEffect()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .border(
                        3.dp,
                        Brush.linearGradient(
                            listOf(Color(0xFFFFD700), Color(0xFFFF9800), Color(0xFFFFD700))
                        ),
                        RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Trophy / Winner Badge
                    val isFirstKing = firstWinner?.let { !it.isBot && (it.id == 0 || it.name.contains("Mistry", ignoreCase = true) || it.name.contains("King", ignoreCase = true)) } ?: false

                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFFFFF9C4), Color(0xFFFFD54F), Color(0xFFFFA000))
                                )
                            )
                            .border(2.5.dp, Color(0xFFFFD54F), CircleShape)
                            .shadow(6.dp, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isFirstKing) {
                            Image(
                                painter = painterResource(id = R.drawable.img_king_mistry_user_avatar_1788345245315),
                                contentDescription = "King Mistry Winner",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.EmojiEvents,
                                contentDescription = "Trophy",
                                tint = Color(0xFF795548),
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isBengali) "অভিনন্দন! বিজয় উৎসব!" else "Victory! Game Over!",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF263238),
                            fontSize = 22.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isBengali) {
                            "${firstWinner?.name ?: "খেলোয়াড়"} ১ম স্থান অধিকার করেছেন!"
                        } else {
                            "${firstWinner?.name ?: "Player"} has won 1st Place!"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = firstWinner?.color?.darkColor ?: Color(0xFF37474F)
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Ranking Podium List
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F7FA), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ranking.forEachIndexed { index, player ->
                            val rank = index + 1
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (rank == 1) Color(0xFFFFF9C4) else Color.White,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (rank == 1) Color(0xFFFFD54F) else Color(0xFFE0E0E0),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = when (rank) {
                                            1 -> Color(0xFFFFD700)
                                            2 -> Color(0xFFB0BEC5)
                                            3 -> Color(0xFFCD7F32)
                                            else -> Color(0xFFECEFF1)
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "$rank",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color.Black
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    val isRowKing = !player.isBot && (player.id == 0 || player.name.contains("Mistry", ignoreCase = true) || player.name.contains("King", ignoreCase = true))

                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(player.color.primaryColor)
                                            .border(0.8.dp, Color(0xFFFFD54F), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isRowKing) {
                                            Image(
                                                painter = painterResource(id = R.drawable.img_king_mistry_user_avatar_1788345245315),
                                                contentDescription = "King Mistry",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = player.name,
                                        fontWeight = if (rank == 1) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = Color(0xFF263238)
                                    )
                                }

                                Text(
                                    text = if (rank == 1) (if (isBengali) "১ম" else "1st Place")
                                    else if (rank == 2) (if (isBengali) "২য়" else "2nd Place")
                                    else if (rank == 3) (if (isBengali) "৩য়" else "3rd Place")
                                    else (if (isBengali) "৪র্থ" else "4th Place"),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (rank == 1) Color(0xFFE65100) else Color(0xFF78909C)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Share Victory Button
                    Button(
                        onClick = {
                            val winnerName = firstWinner?.name ?: "King Mistry"
                            val shareUrl = "https://ais-pre-22degxp5tmj4om7dmfqho7-913901272718.asia-southeast1.run.app"
                            val shareText = if (isBengali) {
                                "🏆 কিং মিস্ত্রি লুডু (King Mistry Ludo) 🎲\n\nঅভিনন্দন! $winnerName খেলায় চ্যাম্পিয়ন হয়েছেন!\n\n👉 আপনিও খেলুন:\n$shareUrl\n\n🎮 এখনই ক্লিক করে আপনার বন্ধুদের সাথে লুডু ম্যাচ শুরু করুন!"
                            } else {
                                "🏆 King Mistry Ludo 🎲\n\nCongratulations! $winnerName won the match!\n\n👉 Play now:\n$shareUrl\n\n🎮 Tap the link to challenge your friends!"
                            }
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_SUBJECT, if (isBengali) "কিং মিস্ত্রি লুডু চ্যাম্পিয়ন" else "King Mistry Ludo Champion")
                                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                            }
                            val chooser = android.content.Intent.createChooser(intent, if (isBengali) "ফলাফল শেয়ার করুন" else "Share Match Result")
                            chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(chooser)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("share_result_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0D47A1)
                        )
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share", modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBengali) "ফলাফল বন্ধুদের সাথে শেয়ার করুন" else "Share Victory with Friends",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onBackToMenu,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("menu_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Home, contentDescription = "Menu", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isBengali) "মেনু" else "Menu", fontSize = 14.sp)
                        }

                        Button(
                            onClick = onPlayAgain,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("play_again_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32)
                            )
                        ) {
                            Icon(Icons.Filled.Replay, contentDescription = "Play Again", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isBengali) "আবার খেলুন" else "Play Again", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfettiEffect() {
    val particles = remember {
        List(40) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 12f + 6f,
                color = listOf(
                    Color(0xFFE53935), Color(0xFF43A047), Color(0xFFFDD835),
                    Color(0xFF1E88E5), Color(0xFFFF9800), Color(0xFFE91E63)
                ).random(),
                speedY = Random.nextFloat() * 2f + 1f,
                speedX = (Random.nextFloat() - 0.5f) * 1.5f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_anim"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        for (p in particles) {
            val currentY = ((p.y + animProgress * p.speedY) % 1.0f) * h
            val currentX = ((p.x + animProgress * p.speedX + 1.0f) % 1.0f) * w
            drawCircle(
                color = p.color,
                radius = p.size / 2f,
                center = Offset(currentX, currentY)
            )
        }
    }
}
