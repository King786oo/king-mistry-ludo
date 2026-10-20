package com.example.ui.screens

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FullScreen
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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
enum class GameMode {
    VS_BOT, PASS_N_PLAY, ONLINE
}

@Composable
fun LudoMenuScreen(
    isBengali: Boolean = false,
    onStartGame: (GameMode, Int, List<String>, List<Boolean>) -> Unit = { _, _, _, _ -> },
    onOpenStats: () -> Unit = {},
    onOpenRules: () -> Unit = {},
    onToggleLanguage: () -> Unit = {},
    onBackToProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedGameMode by remember { mutableStateOf(GameMode.VS_BOT) }
    var playerCount by remember { mutableIntStateOf(2) }

    var p1Name by remember { mutableStateOf("Player 1") }
    var p2Name by remember { mutableStateOf("Player 2") }
    var p3Name by remember { mutableStateOf("Player 3") }
    var p4Name by remember { mutableStateOf("Player 4") }

    var showPlayerSelectDialog by remember { mutableStateOf(false) }
    var showApkShareDialog by remember { mutableStateOf(false) }
    var showQuickPlayModal by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "banner_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F1B29),
                        Color(0xFF162A45),
                        Color(0xFF0B121E)
                    )
                )
            )
    ) {
        // Decorative background elements
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            drawCircle(
                color = Color(0x0A00E5FF),
                radius = width * 0.6f,
                center = Offset(width * 0.2f, height * 0.1f)
            )
            drawCircle(
                color = Color(0x0AFFE082),
                radius = width * 0.5f,
                center = Offset(width * 0.8f, height * 0.8f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToProfile,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0x22FFFFFF), CircleShape)
                        .border(1.dp, Color(0x44FFFFFF), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0x33000000),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Coins",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "2,500",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                    IconButton(
                        onClick = onToggleLanguage,
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0x22FFFFFF), CircleShape)
                            .border(1.dp, Color(0x44FFFFFF), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Language",
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Guest Profile Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2D42))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF00E5FF), Color(0xFF0055FF))
                                ),
                                shape = CircleShape
                            )
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBengali) "গেস্ট প্লেয়ার" else "GUEST PLAYER",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (isBengali) "কিং মিস্ত্রি লুডু ক্লাব" else "King Mistry Ludo Club",
                            color = Color(0xFFB0BEC5),
                            fontSize = 12.sp
                        )
                    }

                    IconButton(onClick = onOpenStats) {
                        Icon(
                            imageVector = Icons.Default.QueryStats,
                            contentDescription = "Stats",
                            tint = Color(0xFF00E5FF)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3D Ludo Logo Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(pulseScale)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFFFD700), Color(0xFFFF8C00))
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .border(2.dp, Color.White, RoundedCornerShape(20.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = "Ludo Banner",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "KING MISTRY LUDO",
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (isBengali) "অফলাইন ও অনলাইন গেম মোড" else "Offline & Online Game Modes",
                        fontSize = 12.sp,
                        color = Color(0xFF3E2723)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            // Game Mode Selection Cards
            Text(
                text = if (isBengali) "গেম মোড নির্বাচন করুন" else "SELECT GAME MODE",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Mode 1: Vs Computer
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedGameMode = GameMode.VS_BOT
                        showPlayerSelectDialog = true
                    },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF253852))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Smartphone,
                        contentDescription = "Vs Computer",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBengali) "কম্পিউটারের সাথে খেলুন" else "VS COMPUTER / BOT",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (isBengali) "অফলাইনে বটের বিরুদ্ধে প্র্যাকটিস করুন" else "Practice offline against AI bots",
                            color = Color(0xFF90A4AE),
                            fontSize = 12.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Mode 2: Pass & Play
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedGameMode = GameMode.PASS_N_PLAY
                        showPlayerSelectDialog = true
                    },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF253852))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "Pass and Play",
                        tint = Color(0xFFFFB74D),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBengali) "পাস অ্যান্ড প্লে (একই ফোনে)" else "PASS & PLAY (LOCAL)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (isBengali) "বন্ধুদের সাথে একই মোবাইলে খেলুন" else "Play with friends on same device",
                            color = Color(0xFF90A4AE),
                            fontSize = 12.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            // Mode 3: Online Multiplayer
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedGameMode = GameMode.ONLINE
                        showQuickPlayModal = true
                    },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF253852))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "Online Multiplayer",
                        tint = Color(0xFF81C784),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBengali) "অনলাইন মাল্টিপ্লেয়ার" else "ONLINE MULTIPLAYER",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (isBengali) "বিশ্বজুড়ে আসল প্লেয়ারদের সাথে খেলুন" else "Match with players worldwide",
                            color = Color(0xFF90A4AE),
                            fontSize = 12.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Additional Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenRules,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF))
                ) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = "Rules",
                        tint = Color(0xFF00E5FF)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBengali) "নিয়মাবলী" else "RULES",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }

                OutlinedButton(
                    onClick = { showApkShareDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700))
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color(0xFFFFD700)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBengali) "শেয়ার করুন" else "SHARE APK",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
        // Player Selection & Configuration Dialog
        if (showPlayerSelectDialog) {
            Dialog(onDismissRequest = { showPlayerSelectDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 16.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isBengali) "প্লেয়ার সংখ্যা নির্বাচন করুন" else "SELECT PLAYER COUNT",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color(0xFF0D54A4)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(2, 3, 4).forEach { count ->
                                val isSelected = playerCount == count
                                Button(
                                    onClick = { playerCount = count },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) Color(0xFF2E7D32) else Color(0xFFE0E0E0)
                                    )
                                ) {
                                    Text(
                                        text = "$count P",
                                        color = if (isSelected) Color.White else Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Player Name Inputs
                        OutlinedTextField(
                            value = p1Name,
                            onValueChange = { p1Name = it },
                            label = { Text(if (isBengali) "প্লেয়ার ১" else "Player 1") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = if (selectedGameMode == GameMode.VS_BOT) "Computer 1" else p2Name,
                            onValueChange = { p2Name = it },
                            label = { Text(if (isBengali) "প্লেয়ার ২" else "Player 2") },
                            enabled = selectedGameMode != GameMode.VS_BOT,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (playerCount >= 3) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = if (selectedGameMode == GameMode.VS_BOT) "Computer 2" else p3Name,
                                onValueChange = { p3Name = it },
                                label = { Text(if (isBengali) "প্লেয়ার ৩" else "Player 3") },
                                enabled = selectedGameMode != GameMode.VS_BOT,
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (playerCount >= 4) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = if (selectedGameMode == GameMode.VS_BOT) "Computer 3" else p4Name,
                                onValueChange = { p4Name = it },
                                label = { Text(if (isBengali) "প্লেয়ার ৪" else "Player 4") },
                                enabled = selectedGameMode != GameMode.VS_BOT,
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Start Match Button
                        Button(
                            onClick = {
                                showPlayerSelectDialog = false
                                val names = when (playerCount) {
                                    2 -> listOf(p1Name, if (selectedGameMode == GameMode.VS_BOT) "Computer" else p2Name)
                                    3 -> listOf(p1Name, if (selectedGameMode == GameMode.VS_BOT) "Bot Green" else p2Name, if (selectedGameMode == GameMode.VS_BOT) "Bot Yellow" else p3Name)
                                    else -> listOf(p1Name, if (selectedGameMode == GameMode.VS_BOT) "Bot Green" else p2Name, if (selectedGameMode == GameMode.VS_BOT) "Bot Yellow" else p3Name, if (selectedGameMode == GameMode.VS_BOT) "Bot Blue" else p4Name)
                                }
                                val bots = when (selectedGameMode) {
                                    GameMode.VS_BOT -> listOf(false, true, true, true).take(playerCount)
                                    else -> listOf(false, false, false, false).take(playerCount)
                                }
                                onStartGame(selectedGameMode, playerCount, names, bots)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Text(
                                text = if (isBengali) "খেলা শুরু করুন" else "START MATCH",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
        // APK Share Modal Dialog
        if (showApkShareDialog) {
            Dialog(onDismissRequest = { showApkShareDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 16.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.InstallMobile,
                            contentDescription = "Install App",
                            tint = Color(0xFF0D54A4),
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isBengali) "এপিকে শেয়ার ও ডাউনলোড" else "SHARE & DOWNLOAD APK",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color(0xFF0D54A4)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isBengali) 
                                "আপনার বন্ধুদের সাথে অ্যাপ শেয়ার করুন অথবা সরাসরি গিটহাব থেকে ফাইল ডাউনলোড করুন।" 
                            else 
                                "Share King Mistry Ludo APK directly with friends or download latest release build.",
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Download King Mistry Ludo App: https://github.com/King786oo/king-mistry-ludo")
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, null)
                                context.startActivity(shareIntent)
                                showApkShareDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D54A4))
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isBengali) "লিংক শেয়ার করুন" else "SHARE APP LINK", color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { showApkShareDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isBengali) "বন্ধ করুন" else "CLOSE", color = Color.Gray)
                        }
                    }
                }
            }
        }
        // Quick Play / Online Matchmaking Dialog
        if (showQuickPlayModal) {
            Dialog(onDismissRequest = { showQuickPlayModal = false }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF162A45),
                    shadowElevation = 16.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "Online",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(54.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (isBengali) "অনলাইন ম্যাচমেকিং" else "ONLINE MATCHMAKING",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isBengali) "অনলাইন রুম সার্চ করা হচ্ছে... অনুগ্রহ করে অপেক্ষা করুন" else "Searching for live rooms... Please wait",
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            color = Color(0xFFB0BEC5)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                showQuickPlayModal = false
                                onStartGame(GameMode.ONLINE, 2, listOf("Player 1", "Online Rival"), listOf(false, false))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                        ) {
                            Text(
                                text = if (isBengali) "দ্রুত খেলা শুরু করুন" else "QUICK START",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { showQuickPlayModal = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isBengali) "বাতিল করুন" else "CANCEL", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun LudoBoardDecorationCanvas(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Background Box Shadow / Frame
        drawRoundRect(
            color = Color(0xFF0B121E),
            size = Size(w, h),
            cornerRadius = CornerRadius(16f, 16f)
        )

        // Outer Golden Border
        drawRoundRect(
            color = Color(0xFFFFD700),
            size = Size(w - 8f, h - 8f),
            topLeft = Offset(4f, 4f),
            cornerRadius = CornerRadius(12f, 12f),
            style = Stroke(width = 4f)
        )
    }
}

@Composable
fun LudoCustomCardHeader(
    title: String,
    subtitle: String,
    isBengali: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = Color.White
        )
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = Color(0xFFB0BEC5)
        )
    }
}
@Composable
fun LudoStatItemRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp)
            .background(Color(0x1AFFFFFF), RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = value,
            color = Color(0xFF00E5FF),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LudoGameRulesDialog(
    isBengali: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E2D42),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (isBengali) "লুডু খেলার নিয়মাবলী" else "LUDO GAME RULES",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFFFFD700)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isBengali)
                        "১. ছক্কায় গুটি ঘর থেকে বের হবে।\n" +
                        "২. প্রতিপক্ষের গুটি কাটলে বাড়তি চাল পাওয়া যাবে।\n" +
                        "৩. সব গুটি পাকা করে কেন্দ্রে পৌঁছালে জয়ী হবেন।"
                    else
                        "1. Rolling a 6 unlocks a token from home.\n" +
                        "2. Capturing an opponent's token grants a bonus turn.\n" +
                        "3. First player to reach home with all tokens wins.",
                    color = Color.White,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                ) {
                    Text(if (isBengali) "ঠিক আছে" else "GOT IT", color = Color.Black)
                }
            }
        }
    }
}
@Composable
fun LudoProfileBadgeOverlay(
    level: Int = 1,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFFFFD700), CircleShape)
            .border(1.dp, Color.White, CircleShape)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "LVL $level",
            color = Color.Black,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun LudoBottomActionFooter(
    onRulesClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(onClick = onRulesClick) {
            Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.White)
        }
        IconButton(onClick = onShareClick) {
            Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
        }
    }
}
// ========================================================
// KING MISTRY LUDO - MENU SCREEN HELPER UTILITIES
// ========================================================

object LudoMenuConfig {
    const val MAX_PLAYERS = 4
    const val MIN_PLAYERS = 2
    const val DEFAULT_COINS = 2500
    const val BOT_THINK_DELAY = 1000L
}

fun formatCoinsDisplay(coins: Int): String {
    return String.format("%,d", coins)
}

fun getModeTitle(mode: GameMode, isBengali: Boolean): String {
    return when (mode) {
        GameMode.VS_BOT -> if (isBengali) "কম্পিউটার" else "Vs Computer"
        GameMode.PASS_N_PLAY -> if (isBengali) "পাস অ্যান্ড প্লে" else "Pass & Play"
        GameMode.ONLINE -> if (isBengali) "অনলাইন মাল্টিপ্লেয়ার" else "Online Multiplayer"
    }
}
// ========================================================
// EXTENDED LUDO BOARD & GAME ENGINE LOGIC
// ========================================================

enum class PlayerColor {
    RED, GREEN, YELLOW, BLUE
}

data class LudoPawnState(
    val id: Int,
    val color: PlayerColor,
    var currentPosition: Int = -1, // -1 means in Base/Home
    var isHomeSafe: Boolean = false,
    var isFinished: Boolean = false
)

data class PlayerData(
    val name: String,
    val color: PlayerColor,
    val isBot: Boolean = false,
    val pawns: List<LudoPawnState> = List(4) { id -> LudoPawnState(id = id, color = color) },
    var score: Int = 0
)

class LudoEngineController(
    val playerList: List<PlayerData>
) {
    var activeTurnIndex by mutableIntStateOf(0)
    var lastDiceRoll by mutableIntStateOf(1)
    var isDiceRolled by mutableStateOf(false)
    var isGameOver by mutableStateOf(false)

    fun rollDice(): Int {
        if (isGameOver) return lastDiceRoll
        lastDiceRoll = (1..6).random()
        isDiceRolled = true
        return lastDiceRoll
    }

    fun canMovePawn(pawn: LudoPawnState): Boolean {
        if (!isDiceRolled) return false
        if (pawn.currentPosition == -1 && lastDiceRoll == 6) return true
        if (pawn.currentPosition >= 0 && !pawn.isFinished) return true
        return false
    }

    fun movePawn(pawn: LudoPawnState) {
        if (!canMovePawn(pawn)) return
        
        if (pawn.currentPosition == -1 && lastDiceRoll == 6) {
            pawn.currentPosition = 0 // Enter starting tile
        } else if (pawn.currentPosition >= 0) {
            pawn.currentPosition += lastDiceRoll
            if (pawn.currentPosition >= 56) {
                pawn.isFinished = true
            }
        }
        
        // Reset turn state after movement
        if (lastDiceRoll != 6) {
            switchNextTurn()
        }
        isDiceRolled = false
    }

    private fun switchNextTurn() {
        activeTurnIndex = (activeTurnIndex + 1) % playerList.size
    }
}
@Composable
fun LudoEngineViewContainer(
    controller: LudoEngineController,
    onBackToMenu: () -> Unit
) {
    val activePlayer = controller.playerList[controller.activeTurnIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1B29))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar - Current Player Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToMenu,
                    modifier = Modifier
                        .background(Color(0x22FFFFFF), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Exit", tint = Color.White)
                }

                Text(
                    text = "TURN: ${activePlayer.name}",
                    color = when (activePlayer.color) {
                        PlayerColor.RED -> Color.Red
                        PlayerColor.GREEN -> Color.Green
                        PlayerColor.YELLOW -> Color.Yellow
                        PlayerColor.BLUE -> Color(0xFF00E5FF)
                    },
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )

                IconButton(onClick = { /* Sound Toggle */ }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                }
            }

            // Ludo Board Canvas Graphic Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(4.dp, Color(0xFFFFD700), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "LUDO BOARD CANVAS ACTIVE",
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
            }

            // Bottom Dice Control Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2D42))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "DICE VALUE", color = Color.Gray, fontSize = 12.sp)
                        Text(
                            text = if (controller.isDiceRolled) "${controller.lastDiceRoll}" else "-",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                    }

                    Button(
                        onClick = { controller.rollDice() },
                        enabled = !controller.isDiceRolled,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                    ) {
                        Text(text = "ROLL DICE", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
// ========================================================
// KING MISTRY LUDO - AUDIO, ADMOB & SYSTEM INITIALIZERS
// ========================================================

object LudoSystemSoundManager {
    fun playDiceRollSound(context: Context) {
        // Sound Pool or MediaPlayer triggering logic for Dice Roll
    }

    fun playTokenMoveSound(context: Context) {
        // Sound Pool triggering logic for Token Move
    }

    fun playTokenKillSound(context: Context) {
        // Sound Pool triggering logic for Token Capture
    }

    fun playVictorySound(context: Context) {
        // Sound Pool triggering logic for Win State
    }
}

object LudoAdMobHelper {
    fun loadInterstitialAd(context: Context) {
        // AdMob Interstitial ad preload sequence
    }

    fun showInterstitialAd(context: Context) {
        // Display AdMob ad logic after match completion
    }

    fun loadRewardedAd(context: Context) {
        // Rewarded Video Ad preload logic for extra coins
    }
}

class LudoGamePreferences(context: Context) {
    private val prefs = context.getSharedPreferences("king_mistry_ludo_prefs", Context.MODE_PRIVATE)

    fun saveUserCoins(coins: Int) {
        prefs.edit().putInt("user_coins", coins).apply()
    }

    fun getUserCoins(): Int {
        return prefs.getInt("user_coins", 2500)
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
    }

    fun isSoundEnabled(): Boolean {
        return prefs.getBoolean("sound_enabled", true)
    }
}

// End of LudoMenuScreen.kt Complete File
