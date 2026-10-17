package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LudoMenuScreen(
    isBengali: Boolean,
    onStartGame: (mode: String, playerCount: Int, names: List<String>, botFlags: List<Boolean>) -> Unit,
    onOpenStats: () -> Unit,
    onOpenRules: () -> Unit,
    onToggleLanguage: () -> Unit,
    onBackToProfile: () -> Unit
) {
    // Premium Red-Gold Royal Theme Gradient
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A0003),
            Color(0xFF4A000B),
            Color(0xFF800016)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile & Coins Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF2D0008),
                    modifier = Modifier.border(1.dp, Color(0xFFFFD700), RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👑 King Mistry",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // Language & Rules Toggle
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onToggleLanguage,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A0010)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isBengali) "বাংলা" else "ENG",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onOpenRules,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A0010)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "❓", fontSize = 16.sp)
                    }
                }
            }

            // Game Logo / Title Area
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Text(
                    text = if (isBengali) "কিং মিস্ত্রি লুডো" else "KING MISTRY LUDO",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFD700),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "v59.0 Royal Edition",
                    fontSize = 12.sp,
                    color = Color(0xFFFFA500),
                    fontWeight = FontWeight.Medium
                )
            }

            // Game Mode Cards
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MenuOptionCard(
                    title = if (isBengali) "অনলাইন মাল্টিপ্লেয়ার" else "ONLINE MULTIPLAYER",
                    subtitle = if (isBengali) "বিশ্বজুড়ে বন্ধুদের সাথে খেলুন" else "Play with players worldwide",
                    icon = "🌐",
                    badgeText = "HOT",
                    onClick = {
                        onStartGame("ONLINE", 4, listOf("Player 1", "Player 2", "Player 3", "Player 4"), listOf(false, false, false, false))
                    }
                )

                MenuOptionCard(
                    title = if (isBengali) "বনাম কম্পিউটার (বট)" else "VS COMPUTER",
                    subtitle = if (isBengali) "স্মার্ট এআই বটের বিরুদ্ধে খেলুন" else "Practice offline with AI Bots",
                    icon = "🤖",
                    badgeText = "OFFLINE",
                    onClick = {
                        onStartGame("VS_BOT", 2, listOf("You", "Bot Master"), listOf(false, true))
                    }
                )

                MenuOptionCard(
                    title = if (isBengali) "পাস অ্যান্ড প্লে (লোকাল)" else "PASS & PLAY (2v2 / 4P)",
                    subtitle = if (isBengali) "এক ফোনে ৪ জন মিলে খেলুন" else "Local multiplayer on single phone",
                    icon = "🎲",
                    badgeText = "POPULAR",
                    onClick = {
                        onStartGame("LOCAL", 4, listOf("Player A", "Player B", "Player C", "Player D"), listOf(false, false, false, false))
                    }
                )
            }

            // Footer / Stats Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = onOpenStats,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD700)),
                    modifier = Modifier.weight(1f).padding(end = 6.dp)
                ) {
                    Text(text = if (isBengali) "🏆 পরিসংখ্যান" else "🏆 STATS", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onBackToProfile,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier.weight(1f).padding(start = 6.dp)
                ) {
                    Text(text = if (isBengali) "👤 প্রোফাইল" else "👤 PROFILE", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MenuOptionCard(
    title: String,
    subtitle: String,
    icon: String,
    badgeText: String,
    onClick: () -> Unit
) {
    val cardGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF3B000B),
            Color(0xFF6B0014)
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(85.dp)
            .shadow(8.dp, RoundedCornerShape(18.dp))
            .border(1.5.dp, Color(0xFFFFD700), RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(cardGradient)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = icon, fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = title,
                            color = Color(0xFFFFD700),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = subtitle,
                            color = Color(0xFFE0E0E0),
                            fontSize = 11.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFD700))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
