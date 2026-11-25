package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import com.example.engine.CoinManager
import kotlinx.coroutines.delay
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
import com.example.model.GameMode
import com.example.model.GamePhase
import com.example.model.GameState
import com.example.multiplayer.MultiplayerConnectionStatus
import com.example.multiplayer.OnlineMultiplayerManager
import com.example.ui.components.ExitConfirmationDialog
import com.example.model.LudoColor
import com.example.ui.LudoViewModel
import com.example.ui.components.GameSettingsDialog
import com.example.ui.components.LudoBoardView
import com.example.ui.components.SnakesAndLaddersBoardView
import com.example.ui.components.SnakesAndLaddersTopPlayerSlots
import com.example.ui.components.SnakesAndLaddersBottomPlayerSlots
import com.example.ui.components.PlayerScoreCard
import com.example.ui.components.LudoCornerPlayerBox
import com.example.ui.components.RulesDialog
import com.example.ui.components.WinnerDialog
import com.example.ui.components.NasidulAvatar

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
                        "🎲 কিং মিস্ত্রি লুডু (Version 90 • v90) APK ফাইল! এখনই ইনস্টল করুন এবং আমার সাথে খেলুন! 🎲"
                    else
                        "🎲 King Mistry Ludo (Version 90 • v90) APK File! Install now and play with me! 🎲"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(
                shareIntent,
                if (isBengali) "মিস্ত্রি লুডু APK শেয়ার করুন" else "Share Mistry Ludo APK"
            )
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            return
        }
    } catch (_: Exception) {}

    val shareText = if (isBengali) {
        "🎲 মিস্ত্রি লুডু (King Mistry Ludo v90) 🎲\n\nআসসালামু আলাইকুম! রুম কোড দিয়ে আমার সাথে লুডু ও ৩ডি ক্যারাম খেলুন!"
    } else {
        "🎲 King Mistry Ludo (v90) 🎲\n\nJoin my room and play King Mistry Ludo & 3D Carrom!"
    }
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, if (isBengali) "মিস্ত্রি লুডু (Mistry Ludo)" else "Mistry Ludo")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        val chooser = Intent.createChooser(intent, if (isBengali) "বন্ধুদের সাথে শেয়ার করুন" else "Share with Friends")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (_: Throwable) {
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LudoGameScreen(
    viewModel: LudoViewModel,
    onBackToMenu: () -> Unit
) {
    val context = LocalContext.current
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val connectionStatus by OnlineMultiplayerManager.connectionStatus.collectAsStateWithLifecycle()
    val queuedPacketsCount by OnlineMultiplayerManager.queuedPacketsCount.collectAsStateWithLifecycle()
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }
    var showExitConfirmationDialog by remember { mutableStateOf(false) }

    // Rematch States for Offline, Online, and Tournament Modes
    var showRematchWaitingDialog by remember { mutableStateOf(false) }
    var rematchWaitingSeconds by remember { mutableIntStateOf(15) }
    var showRematchConsentDialog by remember { mutableStateOf(false) }
    var rematchRequesterName by remember { mutableStateOf("Opponent") }
    var showCannotPlayDialog by remember { mutableStateOf(false) }
    var cannotPlayMessage by remember { mutableStateOf("") }

    fun exitLudoToMenu() {
        viewModel.cancelActiveJobs()
        if (gameState.mode == GameMode.ONLINE_FRIENDS) {
            OnlineMultiplayerManager.leaveRoom()
        }
        onBackToMenu()
    }

    // Listen for online rematch requests and responses
    DisposableEffect(Unit) {
        OnlineMultiplayerManager.onRematchRequestedCallback = { senderId, senderName ->
            rematchRequesterName = senderName.ifBlank { "Opponent" }
            showRematchConsentDialog = true
        }

        OnlineMultiplayerManager.onRematchResponseCallback = { accepted, responderName ->
            showRematchWaitingDialog = false
            if (accepted) {
                // Opponent agreed! Restart match for both players!
                if (gameState.isSnakesAndLadders) {
                    viewModel.startSnakesAndLaddersGame(
                        isOnline = true,
                        playerCount = gameState.players.size,
                        playerNames = gameState.players.map { it.name },
                        botFlags = gameState.players.map { it.isBot }
                    )
                } else {
                    viewModel.startNewGame(
                        mode = GameMode.ONLINE_FRIENDS,
                        playerCount = gameState.players.size,
                        playerNames = gameState.players.map { it.name },
                        botFlags = gameState.players.map { it.isBot },
                        targetTokensToWin = gameState.targetTokensToWin
                    )
                }
            } else {
                // Opponent declined or did not agree
                cannotPlayMessage = if (gameState.isBengali) {
                    "প্রতিপক্ষ $responderName রিম্যাচ প্রত্যাখ্যান করেছে।"
                } else {
                    "Opponent $responderName declined or did not agree to the rematch."
                }
                showCannotPlayDialog = true
            }
        }

        onDispose {
            OnlineMultiplayerManager.onRematchRequestedCallback = null
            OnlineMultiplayerManager.onRematchResponseCallback = null
        }
    }

    // Rematch waiting countdown & AI opponent agreement
    LaunchedEffect(showRematchWaitingDialog) {
        if (showRematchWaitingDialog) {
            val isTournament = gameState.tournamentName != null || gameState.tournamentTierId != null
            if (isTournament && gameState.mode != GameMode.ONLINE_FRIENDS) {
                // Tournament Match against Nasidul Mistry (AI Pro)
                delay(1500)
                showRematchWaitingDialog = false
                viewModel.startNewGame(
                    mode = gameState.mode,
                    playerCount = gameState.players.size,
                    playerNames = gameState.players.map { it.name },
                    botFlags = gameState.players.map { it.isBot },
                    tournamentNetPrize = gameState.tournamentNetPrize,
                    tournamentName = gameState.tournamentName,
                    targetTokensToWin = gameState.targetTokensToWin,
                    tournamentTierId = gameState.tournamentTierId
                )
            } else {
                // Online Room Multiplayer countdown (15s)
                for (s in 15 downTo 1) {
                    rematchWaitingSeconds = s
                    delay(1000)
                    if (!showRematchWaitingDialog) break
                }
                if (showRematchWaitingDialog) {
                    showRematchWaitingDialog = false
                    cannotPlayMessage = if (gameState.isBengali) {
                        "প্রতিপক্ষ সময়মত রিম্যাচে রাজি হয়নি।"
                    } else {
                        "Opponent did not agree to the rematch in time."
                    }
                    showCannotPlayDialog = true
                }
            }
        }
    }

    // Auto-route back to lobby on Cannot Play
    LaunchedEffect(showCannotPlayDialog) {
        if (showCannotPlayDialog) {
            delay(2500)
            showCannotPlayDialog = false
            exitLudoToMenu()
        }
    }

    // Intercept hardware/system back button during active match
    BackHandler(enabled = true) {
        if (gameState.phase == GamePhase.GAME_OVER) {
            exitLudoToMenu()
        } else {
            viewModel.soundManager.playAlertPop()
            showExitConfirmationDialog = true
        }
    }

    val currentPlayer = gameState.currentPlayer
    val activeColor = currentPlayer?.color ?: LudoColor.RED

    if (gameState.isSnakesAndLadders) {
        SnakesAndLaddersFullScreenContent(
            viewModel = viewModel,
            gameState = gameState,
            onExitClick = { showExitConfirmationDialog = true }
        )
    } else {
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
                                NasidulAvatar(
                                    contentDescription = "Mistry Ludo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        val onlineRoom = gameState.onlineRoomCode
                        if (gameState.mode == GameMode.ONLINE_FRIENDS && onlineRoom != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0F3666),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F)),
                                modifier = Modifier.clickable {
                                    OnlineMultiplayerManager.shareViaWhatsApp(context, onlineRoom)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "#$onlineRoom",
                                        color = Color(0xFFFFD54F),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("💬", fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "v90",
                                        color = Color(0xFF38BDF8),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.5.sp
                                    )
                                }
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = when {
                                        gameState.tournamentName != null -> gameState.tournamentName.orEmpty()
                                        gameState.isSnakesAndLadders -> if (gameState.isBengali) "সাপ ও মই" else "Snakes & Ladders"
                                        gameState.isQuickMode -> if (gameState.isBengali) "কুইক লুডো" else "Quick Ludo"
                                        gameState.isBengali -> "মিস্ত্রি লুডু"
                                        else -> "King Mistry Ludo"
                                    },
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when {
                                        gameState.targetTokensToWin == 1 -> Color(0xFF004D40)
                                        gameState.targetTokensToWin == 2 -> Color(0xFF4A148C)
                                        else -> Color(0xFF0C3D77)
                                    },
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        when {
                                            gameState.targetTokensToWin == 1 -> Color(0xFF00E676)
                                            gameState.targetTokensToWin == 2 -> Color(0xFFFFD700)
                                            else -> Color(0xFFFFB300)
                                        }
                                    )
                                ) {
                                    Text(
                                        text = when {
                                            gameState.isSnakesAndLadders -> "100"
                                            gameState.targetTokensToWin == 1 -> if (gameState.isBengali) "১ গুটি জয়" else "1 GOAL WIN"
                                            gameState.targetTokensToWin == 2 -> if (gameState.isBengali) "২ গুটি জয়" else "2 GOAL WIN"
                                            gameState.tournamentName != null -> if (gameState.isBengali) "৪ গুটি জয়" else "4 GOAL WIN"
                                            gameState.isQuickMode -> "FAST"
                                            else -> "v90"
                                        },
                                        color = when {
                                            gameState.targetTokensToWin == 1 -> Color(0xFFB9F6CA)
                                            gameState.targetTokensToWin == 2 -> Color(0xFFFFE082)
                                            else -> Color(0xFFFFD54F)
                                        },
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (gameState.phase == GamePhase.GAME_OVER) {
                                exitLudoToMenu()
                            } else {
                                viewModel.soundManager.playAlertPop()
                                showExitConfirmationDialog = true
                            }
                        },
                        modifier = Modifier.testTag("nav_home_button")
                    ) {
                        Icon(Icons.Filled.Home, contentDescription = "Menu", tint = Color.White)
                    }
                },
                actions = {
                    // Clean, prominent Audio Toggle Button (Sound ON / OFF)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (gameState.soundEnabled) Color(0xFF1B5E20).copy(alpha = 0.95f) else Color(0xFFB71C1C).copy(alpha = 0.95f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.2.dp,
                            if (gameState.soundEnabled) Color(0xFF81C784) else Color(0xFFEF9A9A)
                        ),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .testTag("audio_toggle_button")
                            .clickable { viewModel.toggleSound() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (gameState.soundEnabled) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                                contentDescription = if (gameState.soundEnabled) "Sound ON" else "Sound OFF",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (gameState.soundEnabled) {
                                    if (gameState.isBengali) "সাউন্ড অন" else "SOUND ON"
                                } else {
                                    if (gameState.isBengali) "মিউট" else "MUTED"
                                },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                    if (gameState.mode == GameMode.ONLINE_FRIENDS) {
                        IconButton(onClick = { viewModel.resyncOnlineGame() }) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Re-sync Match",
                                tint = Color(0xFF4FC3F7)
                            )
                        }
                    }
                    IconButton(onClick = { shareKingMistryApp(context, gameState.isBengali) }) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share App",
                            tint = Color(0xFFFFD54F)
                        )
                    }
                    IconButton(
                        onClick = { showRulesDialog = true },
                        modifier = Modifier.testTag("rules_button")
                    ) {
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
        val onTokenClicked = remember(viewModel) {
            { playerId: Int, tokenId: Int ->
                viewModel.onTokenClicked(playerId, tokenId)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF071426), // Enterprise Deep Navy
                            Color(0xFF0C2B52), // Enterprise Royal Blue
                            Color(0xFF071426)  // Crisp Pearl Deep
                        )
                    )
                )
                .padding(innerPadding)
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Container centered for tablet & large phones with high width coverage
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Subtle non-blocking Network Reconnecting Status Indicator for active room multiplayer
                if (gameState.mode == GameMode.ONLINE_FRIENDS && gameState.onlineRoomCode != null) {
                    AnimatedVisibility(
                        visible = connectionStatus != MultiplayerConnectionStatus.CONNECTED,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        val isReconnecting = connectionStatus == MultiplayerConnectionStatus.RECONNECTING
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse_reconnect")
                        val pulseAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.4f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(700, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulse_alpha"
                        )

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                                .clickable {
                                    OnlineMultiplayerManager.reconnectNow()
                                    viewModel.resyncOnlineGame()
                                }
                                .testTag("network_status_indicator"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isReconnecting) Color(0xFFE65100).copy(alpha = 0.94f) else Color(0xFFC62828).copy(alpha = 0.94f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.2.dp,
                                if (isReconnecting) Color(0xFFFFD54F) else Color(0xFFFF8A80)
                            ),
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(
                                                if (isReconnecting) Color(0xFFFFD54F).copy(alpha = pulseAlpha) else Color(0xFFFF5252),
                                                CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        val title = if (isReconnecting) {
                                            if (gameState.isBengali) "পুনরায় সংযোগ হচ্ছে... (দুর্বল নেটওয়ার্ক)" else "Reconnecting... (Weak network)"
                                        } else {
                                            if (gameState.isBengali) "অফলাইন • পুনরায় চেষ্টা চলছে..." else "Offline • Retrying connection..."
                                        }
                                        Text(
                                            text = title,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        val subtext = if (queuedPacketsCount > 0) {
                                            if (gameState.isBengali) "$queuedPacketsCount টি চাল জমা আছে • স্ক্রিন ফ্রিজ হবে না" else "$queuedPacketsCount action(s) queued • Board active"
                                        } else {
                                            if (gameState.isBengali) "খেলা চালু রাখুন • সংযোগ পেলেই সিঙ্ক হবে" else "Keep playing • Board does not freeze"
                                        }
                                        Text(
                                            text = subtext,
                                            color = Color(0xFFFFE082),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.22f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Refresh,
                                            contentDescription = "Retry",
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = if (gameState.isBengali) "রিট্রাই" else "Retry",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2v2 Team Score Banner if in 2v2 Mode
                if (gameState.isTeamMode()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0C2B52),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFE53935)))
                                Spacer(modifier = Modifier.width(3.dp))
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFFB300)))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "TEAM 1: ${gameState.teamAFinishedCount}/8",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = "⚔️ 2v2 PARTNERSHIP ⚔️",
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "TEAM 2: ${gameState.teamBFinishedCount}/8",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF43A047)))
                                Spacer(modifier = Modifier.width(3.dp))
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF1E88E5)))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                }

                val redPlayer = gameState.players.find { it.color == LudoColor.RED }
                val greenPlayer = gameState.players.find { it.color == LudoColor.GREEN }
                val bluePlayer = gameState.players.find { it.color == LudoColor.BLUE }
                val yellowPlayer = gameState.players.find { it.color == LudoColor.YELLOW }

                val localPlayerId = remember(gameState.mode, gameState.onlineRoomCode, gameState.onlineLocalPlayerId, gameState.players) {
                    if (gameState.mode == GameMode.ONLINE_FRIENDS && gameState.onlineRoomCode != null) {
                        gameState.onlineLocalPlayerId
                    } else {
                        gameState.players.firstOrNull { !it.isBot }?.id ?: 0
                    }
                }

                // Top Row: Player Slots (Top-Left: Red / Player 2, Top-Right: Green / Player 3 or Opponent)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (redPlayer != null) {
                            val redPlayerNumber = redPlayer.id + 1
                            LudoCornerPlayerBox(
                                player = redPlayer,
                                playerNumber = redPlayerNumber,
                                houseColor = LudoColor.RED,
                                isCurrentTurn = gameState.currentPlayer?.id == redPlayer.id,
                                currentDiceValue = gameState.displayDiceValue,
                                isRolling = gameState.isRolling,
                                gamePhase = gameState.phase,
                                isBengali = gameState.isBengali,
                                isOnlineMatch = gameState.mode == GameMode.ONLINE_FRIENDS,
                                diceSkin = gameState.equippedDiceSkin,
                                targetTokensToWin = gameState.targetTokensToWin,
                                labelAbove = true,
                                indicatorOnLeft = true,
                                localPlayerId = localPlayerId,
                                onRollClick = { viewModel.rollDice() },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Spacer(modifier = Modifier.fillMaxWidth())
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        if (greenPlayer != null) {
                            val greenPlayerNumber = if (gameState.players.size == 2) 2 else (greenPlayer.id + 1)
                            LudoCornerPlayerBox(
                                player = greenPlayer,
                                playerNumber = greenPlayerNumber,
                                houseColor = LudoColor.GREEN,
                                isCurrentTurn = gameState.currentPlayer?.id == greenPlayer.id,
                                currentDiceValue = gameState.displayDiceValue,
                                isRolling = gameState.isRolling,
                                gamePhase = gameState.phase,
                                isBengali = gameState.isBengali,
                                isOnlineMatch = gameState.mode == GameMode.ONLINE_FRIENDS,
                                diceSkin = gameState.equippedDiceSkin,
                                targetTokensToWin = gameState.targetTokensToWin,
                                labelAbove = true,
                                indicatorOnLeft = false,
                                localPlayerId = localPlayerId,
                                onRollClick = { viewModel.rollDice() },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Spacer(modifier = Modifier.fillMaxWidth())
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Main Central Game Board: Enlarged (+10% cap 462.dp) with strict 1:1 square cells & zero vertical clipping
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                        .padding(horizontal = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        val boardDim = minOf(this.maxWidth, this.maxHeight, 462.dp)
                        Box(
                            modifier = Modifier.size(boardDim),
                            contentAlignment = Alignment.Center
                        ) {
                            if (gameState.isSnakesAndLadders) {
                                SnakesAndLaddersBoardView(
                                    state = gameState.snakesAndLaddersState,
                                    isBengali = gameState.isBengali,
                                    equippedToken = gameState.equippedToken,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                LudoBoardView(
                                    players = gameState.players,
                                    activePlayerId = gameState.currentPlayer?.id ?: -1,
                                    localPlayerId = gameState.onlineLocalPlayerId ?: 0,
                                    equippedToken = gameState.equippedToken,
                                    onTokenClicked = onTokenClicked,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Bottom Row: Player Slots (Bottom-Left: Blue / Player 1, Bottom-Right: Yellow / Player 4)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (bluePlayer != null) {
                            LudoCornerPlayerBox(
                                player = bluePlayer,
                                playerNumber = 1,
                                houseColor = LudoColor.BLUE,
                                isCurrentTurn = gameState.currentPlayer?.id == bluePlayer.id,
                                currentDiceValue = gameState.displayDiceValue,
                                isRolling = gameState.isRolling,
                                gamePhase = gameState.phase,
                                isBengali = gameState.isBengali,
                                isOnlineMatch = gameState.mode == GameMode.ONLINE_FRIENDS,
                                diceSkin = gameState.equippedDiceSkin,
                                targetTokensToWin = gameState.targetTokensToWin,
                                labelAbove = true,
                                indicatorOnLeft = true,
                                localPlayerId = localPlayerId,
                                onRollClick = { viewModel.rollDice() },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Spacer(modifier = Modifier.fillMaxWidth())
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        if (yellowPlayer != null) {
                            val yellowPlayerNumber = yellowPlayer.id + 1
                            LudoCornerPlayerBox(
                                player = yellowPlayer,
                                playerNumber = yellowPlayerNumber,
                                houseColor = LudoColor.YELLOW,
                                isCurrentTurn = gameState.currentPlayer?.id == yellowPlayer.id,
                                currentDiceValue = gameState.displayDiceValue,
                                isRolling = gameState.isRolling,
                                gamePhase = gameState.phase,
                                isBengali = gameState.isBengali,
                                isOnlineMatch = gameState.mode == GameMode.ONLINE_FRIENDS,
                                diceSkin = gameState.equippedDiceSkin,
                                targetTokensToWin = gameState.targetTokensToWin,
                                labelAbove = true,
                                indicatorOnLeft = false,
                                localPlayerId = localPlayerId,
                                onRollClick = { viewModel.rollDice() },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Spacer(modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
    }
    }

    // Winner Dialog Overlay
    if (gameState.phase == GamePhase.GAME_OVER && gameState.winnerRanking.isNotEmpty()) {
        val localPlayerId = remember(gameState.mode, gameState.onlineRoomCode, gameState.onlineLocalPlayerId, gameState.players) {
            if (gameState.mode == GameMode.ONLINE_FRIENDS && gameState.onlineRoomCode != null) {
                gameState.onlineLocalPlayerId
            } else {
                gameState.players.firstOrNull { !it.isBot }?.id ?: 0
            }
        }
        val localPlayerDisplayName = remember(gameState.players, localPlayerId) {
            gameState.players.find { it.id == localPlayerId }?.name ?: "Player ${localPlayerId + 1}"
        }
        WinnerDialog(
            ranking = gameState.winnerRanking,
            isBengali = gameState.isBengali,
            localPlayerId = localPlayerId,
            isTeamMode = gameState.isTeamMode(),
            winningTeamName = gameState.winningTeamName,
            tournamentNetPrize = gameState.tournamentNetPrize,
            tournamentName = gameState.tournamentName,
            entryFeePerPlayer = gameState.entryFeePerPlayer,
            totalCoinPool = gameState.totalCoinPool,
            defaultWinReward = if (gameState.isSnakesAndLadders) CoinManager.SNAKES_WIN_COINS else CoinManager.MATCH_WIN_COINS,
            onPlayAgain = {
                val isOnline = gameState.mode == GameMode.ONLINE_FRIENDS
                val isTournament = gameState.tournamentName != null || gameState.tournamentTierId != null

                if (!isOnline && !isTournament) {
                    // 1. OFFLINE REMATCH LOGIC:
                    // Instantly restart match with the same players without delay!
                    if (gameState.isSnakesAndLadders) {
                        viewModel.startSnakesAndLaddersGame(
                            isOnline = false,
                            playerCount = gameState.snakesAndLaddersState.players.size,
                            playerNames = gameState.snakesAndLaddersState.players.map { it.name },
                            botFlags = gameState.snakesAndLaddersState.players.map { it.isBot }
                        )
                    } else {
                        viewModel.startNewGame(
                            mode = gameState.mode,
                            playerCount = gameState.players.size,
                            playerNames = gameState.players.map { it.name },
                            botFlags = gameState.players.map { it.isBot },
                            targetTokensToWin = gameState.targetTokensToWin
                        )
                    }
                } else if (isOnline) {
                    // 2. ONLINE MULTIPLAYER REMATCH CONSENT LOGIC:
                    val roomCode = gameState.onlineRoomCode
                    if (roomCode != null) {
                        showRematchWaitingDialog = true
                        rematchWaitingSeconds = 15
                        OnlineMultiplayerManager.sendRematchRequest(
                            roomCode = roomCode,
                            senderPlayerId = localPlayerId,
                            senderName = localPlayerDisplayName
                        )
                    } else {
                        cannotPlayMessage = if (gameState.isBengali) {
                            "অনলাইন সংযোগ বিচ্ছিন্ন হয়েছে। খেলা সম্ভব নয়।"
                        } else {
                            "Online connection lost. Cannot Play."
                        }
                        showCannotPlayDialog = true
                    }
                } else {
                    // 3. TOURNAMENT REMATCH CONSENT LOGIC:
                    val entryFee = gameState.entryFeePerPlayer.takeIf { it > 0 } ?: (gameState.tournamentTierId?.let { 500 } ?: 100)
                    if (!CoinManager.hasEnoughCoins(entryFee)) {
                        cannotPlayMessage = if (gameState.isBengali) {
                            "টুর্নামেন্ট রিম্যাচের জন্য পর্যাপ্ত কয়েন নেই।"
                        } else {
                            "Insufficient coins for tournament rematch. Cannot Play."
                        }
                        showCannotPlayDialog = true
                    } else {
                        showRematchWaitingDialog = true
                        rematchWaitingSeconds = 3
                    }
                }
            },
            onBackToMenu = { exitLudoToMenu() }
        )
    }

    // Rematch Waiting Dialog Overlay (For Online & Tournament Matches)
    if (showRematchWaitingDialog) {
        Dialog(onDismissRequest = {}) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF071426),
                border = BorderStroke(2.dp, Color(0xFFFFD54F)),
                shadowElevation = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("rematch_waiting_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFFFD54F),
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (gameState.isBengali) "রিম্যাচ অনুরোধ পাঠানো হয়েছে" else "Rematch Request Sent",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val isTournament = gameState.tournamentName != null || gameState.tournamentTierId != null
                    val waitingText = if (isTournament && gameState.mode != GameMode.ONLINE_FRIENDS) {
                        if (gameState.isBengali) "নাসিদুল মিস্ত্রির সম্মতির অপেক্ষা করা হচ্ছে..." else "Waiting for Nasidul Mistry to accept..."
                    } else {
                        if (gameState.isBengali) "প্রতিপক্ষের উত্তরের অপেক্ষা করা হচ্ছে... (${rematchWaitingSeconds}s)" else "Waiting for opponent response... (${rematchWaitingSeconds}s)"
                    }
                    Text(
                        text = waitingText,
                        color = Color(0xFF80D8FF),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedButton(
                        onClick = {
                            showRematchWaitingDialog = false
                            cannotPlayMessage = if (gameState.isBengali) "রিম্যাচ অনুরোধ বাতিল করা হয়েছে।" else "Rematch request cancelled."
                            showCannotPlayDialog = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.2.dp, Color(0xFFFF8A80)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("rematch_cancel_btn")
                    ) {
                        Text(
                            text = if (gameState.isBengali) "বাতিল করুন" else "Cancel",
                            color = Color(0xFFFF8A80),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    // Rematch Consent Dialog Overlay (Shown to Opponent when Rematch is Requested)
    if (showRematchConsentDialog) {
        val roomCode = gameState.onlineRoomCode
        Dialog(onDismissRequest = {}) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF071426),
                border = BorderStroke(2.dp, Color(0xFF00E676)),
                shadowElevation = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("rematch_consent_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.HourglassTop,
                        contentDescription = "Rematch Offer",
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (gameState.isBengali) "রিম্যাচের অনুরোধ এসেছে!" else "Rematch Request!",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (gameState.isBengali) {
                            "প্রতিপক্ষ $rematchRequesterName আপনার সাথে পুনরায় খেলতে চান। আপনি কি খেলতে রাজি?"
                        } else {
                            "Opponent $rematchRequesterName wants to play a rematch! Do you agree to play again?"
                        },
                        color = Color(0xFFE0E0E0),
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (roomCode != null) {
                                    OnlineMultiplayerManager.sendRematchResponse(roomCode, false)
                                }
                                showRematchConsentDialog = false
                                cannotPlayMessage = if (gameState.isBengali) "আপনি রিম্যাচ প্রত্যাখ্যান করেছেন।" else "You declined the rematch request."
                                showCannotPlayDialog = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.2.dp, Color(0xFFFF5252)),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("rematch_decline_btn")
                        ) {
                            Text(
                                text = if (gameState.isBengali) "বাতিল" else "Decline",
                                color = Color(0xFFFF5252),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Button(
                            onClick = {
                                if (roomCode != null) {
                                    OnlineMultiplayerManager.sendRematchResponse(roomCode, true)
                                }
                                showRematchConsentDialog = false
                                if (gameState.isSnakesAndLadders) {
                                    viewModel.startSnakesAndLaddersGame(
                                        isOnline = true,
                                        playerCount = gameState.players.size,
                                        playerNames = gameState.players.map { it.name },
                                        botFlags = gameState.players.map { it.isBot }
                                    )
                                } else {
                                    viewModel.startNewGame(
                                        mode = GameMode.ONLINE_FRIENDS,
                                        playerCount = gameState.players.size,
                                        playerNames = gameState.players.map { it.name },
                                        botFlags = gameState.players.map { it.isBot },
                                        targetTokensToWin = gameState.targetTokensToWin
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("rematch_agree_btn")
                        ) {
                            Text(
                                text = if (gameState.isBengali) "সম্মতি (Play)" else "Agree (Play)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Cannot Play Dialog Overlay (When Rematch is Declined, Timed Out, or Not Agreed)
    if (showCannotPlayDialog) {
        Dialog(onDismissRequest = {
            showCannotPlayDialog = false
            exitLudoToMenu()
        }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF071426),
                border = BorderStroke(2.dp, Color(0xFFFF5252)),
                shadowElevation = 18.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("cannot_play_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0x33FF5252),
                        border = BorderStroke(2.dp, Color(0xFFFF5252)),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Block,
                                contentDescription = "Cannot Play",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (gameState.isBengali) "খেলা সম্ভব নয়" else "Cannot Play",
                        color = Color(0xFFFF5252),
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = cannotPlayMessage.ifBlank {
                            if (gameState.isBengali) "প্রতিপক্ষ রাজি না হওয়ায় খেলা শুরু করা যাচ্ছে না।" else "Opponent declined or did not agree to the rematch."
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.5.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (gameState.isBengali) "স্বয়ংক্রিয়ভাবে লবিতে ফিরে যাচ্ছি..." else "Returning to lobby automatically...",
                        color = Color(0xFFFFD54F),
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.5.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = {
                            showCannotPlayDialog = false
                            exitLudoToMenu()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("cannot_play_return_btn")
                    ) {
                        Text(
                            text = if (gameState.isBengali) "লবিতে ফিরে যান" else "Return to Lobby",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
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
                viewModel.soundManager.playAlertPop()
                showExitConfirmationDialog = true
            }
        )
    }

    // Exit Confirmation Dialog Overlay
    if (showExitConfirmationDialog) {
        ExitConfirmationDialog(
            isBengali = gameState.isBengali,
            onExit = {
                viewModel.soundManager.playButtonClick()
                showExitConfirmationDialog = false
                exitLudoToMenu()
            },
            onResume = {
                viewModel.soundManager.playButtonClick()
                showExitConfirmationDialog = false
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

/**
 * High-end International Snakes & Ladders Full Screen Game Interface:
 * - 1.5x Maximized Board Canvas filling the maximum possible screen area.
 * - Completely free of top header bars, creator profiles, sound toggles, and pattern banners.
 * - Sleek 4-corner rectangular player cards (top-left, top-right, bottom-left, bottom-right).
 * - Two clean sections per player: (A) Token icon & title, (B) Active 3D turn dice with glowing light ring.
 * - Minimalist top-edge Back/Exit button with confirmation dialog.
 */
@Composable
fun SnakesAndLaddersFullScreenContent(
    viewModel: LudoViewModel,
    gameState: GameState,
    onExitClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val slState = gameState.snakesAndLaddersState

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF070E1A),
                        Color(0xFF0F1E36),
                        Color(0xFF070E1A)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Minimalist Single Exit Button at top-left edge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Surface(
                    onClick = onExitClick,
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0x33FFFFFF),
                    border = BorderStroke(1.dp, Color(0x44FFFFFF)),
                    modifier = Modifier
                        .heightIn(min = 36.dp)
                        .testTag("sl_minimal_exit_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Exit Game",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (gameState.isBengali) "প্রস্থান" else "Exit",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            val slLocalPlayerId = remember(gameState.mode, gameState.onlineRoomCode, gameState.onlineLocalPlayerId, slState.players) {
                if (gameState.mode == GameMode.ONLINE_FRIENDS && gameState.onlineRoomCode != null) {
                    gameState.onlineLocalPlayerId
                } else {
                    slState.players.firstOrNull { !it.isBot }?.id ?: 0
                }
            }

            // Top Player Slots (Player 2 on Left, Player 3 on Right)
            SnakesAndLaddersTopPlayerSlots(
                state = slState,
                isBengali = gameState.isBengali,
                diceSkin = gameState.equippedDiceSkin,
                localPlayerId = slLocalPlayerId,
                onRollClick = { viewModel.rollDice() },
                modifier = Modifier.fillMaxWidth()
            )

            // Central Snakes & Ladders Game Board - Perfectly Centered Vertically with Optimal HD Board Aspect Ratio (941:1173)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val naturalAspect = 941f / 1173f
                    val availWidth = this.maxWidth
                    val availHeight = this.maxHeight
                    val targetWidth = if (availWidth.value / availHeight.value > naturalAspect) {
                        availHeight * naturalAspect
                    } else {
                        availWidth
                    }
                    val targetHeight = targetWidth / naturalAspect

                    Box(
                        modifier = Modifier
                            .size(targetWidth, targetHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        SnakesAndLaddersBoardView(
                            state = slState,
                            isBengali = gameState.isBengali,
                            equippedToken = gameState.equippedToken,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Bottom Player Slots (Player 1 on Left, Player 4 on Right)
            SnakesAndLaddersBottomPlayerSlots(
                state = slState,
                isBengali = gameState.isBengali,
                diceSkin = gameState.equippedDiceSkin,
                localPlayerId = slLocalPlayerId,
                onRollClick = { viewModel.rollDice() },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
