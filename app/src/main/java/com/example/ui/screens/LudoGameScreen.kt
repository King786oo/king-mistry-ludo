package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.example.R
import com.example.model.GamePhase
import com.example.model.LudoColor
import com.example.ui.LudoViewModel
import com.example.ui.components.GameSettingsDialog
import com.example.ui.components.LudoBoardView
import com.example.ui.components.PlayerScoreCard
import com.example.ui.components.RulesDialog
import com.example.ui.components.WinnerDialog

fun shareKingMistryApp(context: Context, isBengali: Boolean) {
    try {
        val appInfo = context.applicationInfo
        val sourceApk = java.io.File(appInfo.sourceDir)
        if (sourceApk.exists()) {
            val tempApk = java.io.File(context.cacheDir, "King_Mistry_Ludo.apk")
            if (!tempApk.exists() || tempApk.length() != sourceApk.length()) {
                sourceApk.copyTo(tempApk, overwrite = true)
            }
            val apkUri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempApk
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, apkUri)
                putExtra(
                    Intent.EXTRA_TEXT,
                    if (isBengali)
                        "👑 কিং মিস্ত্রি লুডু APK ফাইল! এখনই ডাউনলোড করে ইনস্টল করুন এবং আমার সাথে খেলুন! 🎲"
                    else
                        "👑 King Mistry Ludo APK File! Install now and play with me! 🎲"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(
                shareIntent,
                if (isBengali) "কিং মিস্ত্রি লুডু APK শেয়ার করুন" else "Share King Mistry Ludo APK"
            )
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            return
        }
    } catch (_: Exception) {}

    val shareUrl = "https://ais-pre-22degxp5tmj4om7dmfqho7-913901272718.asia-southeast1.run.app"
    val shareText = if (isBengali) {
        "👑 কিং মিস্ত্রি লুডু (King Mistry Ludo) 🎲\n\nআসসালামু আলাইকুম! আমার সাথে সেরা রয়েল লুডু গেমটি খেলুন এবং উপভোগ করুন!\n\n👉 সরাসরি গেম লিংক:\n$shareUrl\n\n💡 মোবাইলে ইনস্টল করতে ক্রোম ব্রাউজার থেকে 'Add to Home screen' বা APK ডাউনলোড করুন!"
    } else {
        "👑 King Mistry Ludo 🎲\n\nPlay the ultimate royal Ludo game with me!\n\n👉 Game Link:\n$shareUrl\n\n💡 To install, tap 'Add to Home screen' in Chrome or install the APK!"
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, if (isBengali) "কিং মিস্ত্রি লুডু (King Mistry Ludo)" else "King Mistry Ludo")
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    val chooser = Intent.createChooser(intent, if (isBengali) "বন্ধুদের সাথে শেয়ার করুন" else "Share with Friends")
    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(chooser)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LudoGameScreen(
    viewModel: LudoViewModel,
    onBackToMenu: () -> Unit
) {
    val context = LocalContext.current
    val gameState by viewModel.gameState.collectAsState()
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }

    val currentPlayer = gameState.currentPlayer
    val activeColor = currentPlayer?.color ?: LudoColor.RED

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFD54F),
                            modifier = Modifier
                                .size(34.dp)
                                .shadow(4.dp, CircleShape)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_king_mistry_user_avatar_1788345245315),
                                    contentDescription = "King Mistry",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (gameState.isBengali) "কিং মিস্ত্রি লুডু" else "King Mistry Ludo",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToMenu, modifier = Modifier.testTag("nav_home_button")) {
                        Icon(Icons.Filled.Home, contentDescription = "Menu", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { shareKingMistryApp(context, gameState.isBengali) }) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share App",
                            tint = Color(0xFFFFD54F)
                        )
                    }
                    IconButton(onClick = { viewModel.toggleSound() }) {
                        Icon(
                            imageVector = if (gameState.soundEnabled) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                            contentDescription = "Sound Toggle",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { showRulesDialog = true }) {
                        Icon(Icons.Filled.Gavel, contentDescription = "Rules", tint = Color.White)
                    }
                    IconButton(onClick = { showSettingsDialog = true }, modifier = Modifier.testTag("settings_button")) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A2540), // Enterprise Navy Blue
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0A2540), // Enterprise Deep Navy
                            Color(0xFF0D47A1), // Enterprise Royal Blue
                            Color(0xFF1565C0), // Enterprise Cobalt
                            Color(0xFFF4F7FB)  // Crisp Pearl White
                        )
                    )
                )
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Container centered for tablet & large phones with high width coverage
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 620.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Row: Player Cards (Red Top-Left, Green Top-Right)
                val redPlayer = gameState.players.find { it.color == LudoColor.RED }
                val greenPlayer = gameState.players.find { it.color == LudoColor.GREEN }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (redPlayer != null) {
                        PlayerScoreCard(
                            player = redPlayer,
                            isCurrentTurn = gameState.currentPlayer?.id == redPlayer.id,
                            currentDiceValue = gameState.diceValue,
                            isRolling = gameState.isRolling,
                            gamePhase = gameState.phase,
                            isBengali = gameState.isBengali,
                            modifier = Modifier.weight(1f),
                            onRollClick = { viewModel.rollDice() }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    if (greenPlayer != null) {
                        PlayerScoreCard(
                            player = greenPlayer,
                            isCurrentTurn = gameState.currentPlayer?.id == greenPlayer.id,
                            currentDiceValue = gameState.diceValue,
                            isRolling = gameState.isRolling,
                            gamePhase = gameState.phase,
                            isBengali = gameState.isBengali,
                            modifier = Modifier.weight(1f),
                            onRollClick = { viewModel.rollDice() }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Sleek Floating Turn Status Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.98f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, activeColor.primaryColor),
                    shadowElevation = 5.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .clip(CircleShape)
                                .background(activeColor.primaryColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (gameState.isBengali) gameState.statusMessageBn else gameState.statusMessageEn,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = activeColor.darkColor,
                                fontSize = 13.5.sp
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("status_banner")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Main Central Ludo Board (Maximized pattern size & big ghuti)
                LudoBoardView(
                    players = gameState.players,
                    onTokenClicked = { playerId, tokenId ->
                        viewModel.onTokenClicked(playerId, tokenId)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom Row: Player Cards (Blue Bottom-Left, Yellow Bottom-Right)
                val bluePlayer = gameState.players.find { it.color == LudoColor.BLUE }
                val yellowPlayer = gameState.players.find { it.color == LudoColor.YELLOW }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (bluePlayer != null) {
                        PlayerScoreCard(
                            player = bluePlayer,
                            isCurrentTurn = gameState.currentPlayer?.id == bluePlayer.id,
                            currentDiceValue = gameState.diceValue,
                            isRolling = gameState.isRolling,
                            gamePhase = gameState.phase,
                            isBengali = gameState.isBengali,
                            modifier = Modifier.weight(1f),
                            onRollClick = { viewModel.rollDice() }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    if (yellowPlayer != null) {
                        PlayerScoreCard(
                            player = yellowPlayer,
                            isCurrentTurn = gameState.currentPlayer?.id == yellowPlayer.id,
                            currentDiceValue = gameState.diceValue,
                            isRolling = gameState.isRolling,
                            gamePhase = gameState.phase,
                            isBengali = gameState.isBengali,
                            modifier = Modifier.weight(1f),
                            onRollClick = { viewModel.rollDice() }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    // Winner Dialog Overlay
    if (gameState.phase == GamePhase.GAME_OVER && gameState.winnerRanking.isNotEmpty()) {
        WinnerDialog(
            ranking = gameState.winnerRanking,
            isBengali = gameState.isBengali,
            onPlayAgain = {
                viewModel.startNewGame(
                    mode = gameState.mode,
                    playerCount = gameState.players.size,
                    playerNames = gameState.players.map { it.name },
                    botFlags = gameState.players.map { it.isBot }
                )
            },
            onBackToMenu = onBackToMenu
        )
    }

    // Settings Dialog Overlay
    if (showSettingsDialog) {
        GameSettingsDialog(
            soundEnabled = gameState.soundEnabled,
            fastSpeed = gameState.fastSpeed,
            isBengali = gameState.isBengali,
            onDismiss = { showSettingsDialog = false },
            onToggleSound = { viewModel.toggleSound() },
            onToggleSpeed = { viewModel.toggleSpeed() },
            onToggleLanguage = { viewModel.toggleLanguage() },
            onRestartGame = {
                showSettingsDialog = false
                viewModel.startNewGame(
                    mode = gameState.mode,
                    playerCount = gameState.players.size,
                    playerNames = gameState.players.map { it.name },
                    botFlags = gameState.players.map { it.isBot }
                )
            },
            onBackToMenu = {
                showSettingsDialog = false
                onBackToMenu()
            }
        )
    }

    // Rules Dialog Overlay
    if (showRulesDialog) {
        RulesDialog(
            isBengali = gameState.isBengali,
            onDismiss = { showRulesDialog = false }
        )
    }
}
