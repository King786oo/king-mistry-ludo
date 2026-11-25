package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.engine.SoundManager
import com.example.model.AppLanguage
import com.example.model.DiceSkin
import com.example.model.GameMode
import com.example.model.LudoColor
import com.example.model.LudoPremiumToken
import com.example.multiplayer.OnlineMultiplayerManager
import com.example.engine.CoinManager
import com.example.engine.LoanManager
import com.example.engine.PlayerLevelManager
import com.example.ui.components.AuthenticationDialog
import com.example.ui.components.CityTournamentLoungeDialog
import com.example.ui.components.CityTournamentTier
import com.example.ui.components.DiamondCoinStoreDialog
import com.example.ui.components.RoyalChampionsHallOfFameDialog
import com.example.ui.components.DiceSelectionInterface
import com.example.ui.components.DiceView
import com.example.ui.components.EarnCoinsDialog
import com.example.ui.components.LuckySpinWheelDialog
import com.example.ui.components.NasidulAvatar
import com.example.ui.components.OnlineMatchmakingDialog
import com.example.ui.components.PlayWithFriendsDialog
import com.example.ui.components.Token3DPreview
import com.example.ui.components.TokenSelectionDialog
import com.example.engine.CarromGameMode
import com.example.ui.components.CarromLeagueModeSelectionDialog
import com.example.ui.components.CarromTournamentTier
import com.example.ui.components.CarromTourneyLoungeDialog
import com.example.ui.components.CarromMatchmakingDialog
import com.example.model.CarromPuckSet
import com.example.ui.components.CarromPuckCollectionDialog
import com.example.model.CarromStriker
import com.example.ui.components.CarromStrikerCollectionDialog
import com.example.util.WhatsAppHelper
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableLongStateOf
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * King Mistry Ludo - Version 47 Main Game Dashboard.
 * Strict Single-Screen Mobile Fit:
 * 1. Top Bar: 4 Interactive Elements (Sign-in, Coins, Daily Spin, Language)
 * 2. Middle Section: Profile Info Card + 6 Proportional Game Mode Cards
 * 3. Bottom Bar: 4 Active Navigation Buttons (Home, Friends, Dice, Token)
 * Zero vertical scrolling required.
 */
@Composable
fun LudoMenuScreen(
    isBengali: Boolean,
    language: AppLanguage = if (isBengali) AppLanguage.BENGALI else AppLanguage.ENGLISH,
    onStartGame: (mode: GameMode, playerCount: Int, names: List<String>, botFlags: List<Boolean>, tournamentNetPrize: Int?, tournamentName: String?, targetTokensToWin: Int, tournamentTierId: String?, entryFeePerPlayer: Int) -> Unit,
    onStartOnlineGame: (roomCode: String, localPlayerId: Int, playerCount: Int, playerNames: List<String>, isDoubleTeam: Boolean, coinWager: Int) -> Unit = { _, _, _, _, _, _ -> },
    onStartSnakesAndLaddersGame: (isOnline: Boolean, playerCount: Int, names: List<String>, botFlags: List<Boolean>) -> Unit = { _, _, _, _ -> },
    onStartQuickLudoGame: (isOnline: Boolean, playerCount: Int, names: List<String>, botFlags: List<Boolean>) -> Unit = { _, _, _, _ -> },
    onStartCarromGame: (isTournament: Boolean, tournamentName: String, prizePool: Int, player1Name: String, player2Name: String, isOpponentBot: Boolean, localPlayerId: Int, entryFeePerPlayer: Int, carromMode: CarromGameMode) -> Unit = { _, _, _, _, _, _, _, _, _ -> },
    onOpenStats: () -> Unit,
    onOpenRules: () -> Unit,
    onToggleLanguage: () -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit = {},
    onSelectToken: (LudoPremiumToken) -> Unit = {},
    onSelectDiceSkin: (DiceSkin) -> Unit = {},
    onBackToProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }
    DisposableEffect(soundManager) {
        onDispose { soundManager.release() }
    }
    val prefs = remember { context.getSharedPreferences("king_mistry_ludo_prefs", Context.MODE_PRIVATE) }

    // Persistent & Real-Time Coin & Diamond State
    val coins by CoinManager.coinsFlow.collectAsState()
    val diamonds by CoinManager.diamondsFlow.collectAsState()
    val isSignedIn by CoinManager.isSignedInFlow.collectAsState()
    val authProvider by CoinManager.authProviderFlow.collectAsState()

    fun updateCoins(newAmount: Int) {
        CoinManager.updateCoins(newAmount)
    }

    var userName by remember {
        mutableStateOf("Player 1")
    }

    var selectedToken by remember {
        val savedId = prefs.getString("selected_premium_token", LudoPremiumToken.PAWN_CRYSTAL_GEM.id)
        mutableStateOf(LudoPremiumToken.fromId(savedId))
    }

    var selectedDiceSkin by remember {
        val savedId = prefs.getString("selected_dice_skin", DiceSkin.GOLD_DICE.id)
        mutableStateOf(DiceSkin.fromId(savedId))
    }

    // Dialog Visibilities
    var showSignInDialog by remember { mutableStateOf(false) }
    var showCoinStoreDialog by remember { mutableStateOf(false) }
    var showLuckySpinDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showShopModalHub by remember { mutableStateOf(false) }
    var showPlayWithFriendsDialog by remember { mutableStateOf(false) }
    var friendsDialogGameType by remember { mutableStateOf("LUDO") }
    var showDiceCustomizationDialog by remember { mutableStateOf(false) }
    var showTokenSelectDialog by remember { mutableStateOf(false) }
    var showPlayerSelectDialog by remember { mutableStateOf(false) }
    var showShareApkDialog by remember { mutableStateOf(false) }
    var showCityTournamentDialog by remember { mutableStateOf(false) }
    var showRoyalChampionsDialog by remember { mutableStateOf(false) }
    var showMatchmakingDialog by remember { mutableStateOf(false) }
    var selectedTournamentForMatchmaking by remember { mutableStateOf<CityTournamentTier?>(null) }
    var showEarnCoinsDialog by remember { mutableStateOf(false) }
    var showCarromPoolDialog by remember { mutableStateOf(false) }
    var showCarromLeagueModeDialog by remember { mutableStateOf(false) }
    var selectedCarromGameMode by remember { mutableStateOf(CarromGameMode.STANDARD) }
    var showCarromTourneyDialog by remember { mutableStateOf(false) }
    var showCarromMatchmakingDialog by remember { mutableStateOf(false) }
    var selectedCarromTierForMatchmaking by remember { mutableStateOf<CarromTournamentTier?>(null) }
    var selectedPuckSet by remember {
        mutableStateOf(CarromPuckSet.getEquippedSet(context))
    }
    var showCarromPuckDialog by remember { mutableStateOf(false) }
    var selectedStriker by remember {
        mutableStateOf(CarromStriker.getEquippedStriker(context))
    }
    var showCarromStrikerDialog by remember { mutableStateOf(false) }

    // Auto-Rotating Carousel Banner state (switches every 3.5 seconds)
    var currentCarouselSlide by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(3500L)
            currentCarouselSlide = (currentCarouselSlide + 1) % 4
        }
    }

    // Board Lighting Theme (Dark Theme vs Classic Theme)
    var isDarkTheme by remember {
        mutableStateOf(prefs.getBoolean("board_theme_dark", true))
    }

    // 24-Hour Spin Cooldown tracking
    val lastSpinTime by CoinManager.lastSpinTimeFlow.collectAsState()
    var currentClockTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentClockTime = System.currentTimeMillis()
        }
    }
    val remainingSpinCooldown = if (lastSpinTime > 0L) {
        (lastSpinTime + CoinManager.SPIN_COOLDOWN_MS - currentClockTime).coerceAtLeast(0L)
    } else 0L
    val isSpinLocked = remainingSpinCooldown > 0L

    val spinHours = remainingSpinCooldown / (1000 * 60 * 60)
    val spinMinutes = (remainingSpinCooldown % (1000 * 60 * 60)) / (1000 * 60)
    val spinSeconds = (remainingSpinCooldown % (1000 * 60)) / 1000
    val spinCountdownFormatted = String.format("%02d:%02d:%02d", spinHours, spinMinutes, spinSeconds)

    // Selected Match Configuration for PlayerSelectDialog
    var selectedGameMode by remember { mutableStateOf<GameMode>(GameMode.PASS_AND_PLAY) }
    var playerCount by remember { mutableIntStateOf(2) }
    var isSnakesAndLaddersOnline by remember { mutableStateOf(false) }
    var team2v2WithBots by remember { mutableStateOf(true) }

    // Online vs Offline Filter State (Rule 2 & 3)
    var isOnlineTab by remember { mutableStateOf(true) }
    var showNoInternetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        PlayerLevelManager.init(context)
        com.example.model.CarromPowerRing.initialize(context)
    }
    val playerLevel by PlayerLevelManager.currentLevel.collectAsState()

    val lastLoanRepaidEvent by LoanManager.lastRepaidEvent.collectAsState()
    LaunchedEffect(lastLoanRepaidEvent) {
        lastLoanRepaidEvent?.let { event ->
            Toast.makeText(context, "🎉 $event", Toast.LENGTH_LONG).show()
            soundManager.playVictoryFanfare()
            LoanManager.clearRepaidEvent()
        }
    }

    fun isNetworkConnected(): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return true
            val activeNet = cm.activeNetwork ?: return true
            val caps = cm.getNetworkCapabilities(activeNet) ?: return true
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Throwable) {
            true
        }
    }

    fun launchOnlineMode(action: () -> Unit) {
        if (!isNetworkConnected()) {
            soundManager.playButtonClick()
            showNoInternetDialog = true
            Toast.makeText(context, "No internet connection. Please check your network.", Toast.LENGTH_LONG).show()
        } else {
            action()
        }
    }

    var p1Name by remember { mutableStateOf(userName) }
    var p2Name by remember { mutableStateOf("Player 2") }
    var p3Name by remember { mutableStateOf("Player 3") }
    var p4Name by remember { mutableStateOf("Player 4") }

    // Active bottom navigation state (-1: None/Home, 0: Coins/Tokens, 1: Shop, 2: Strikers, 3: Champions)
    var activeBottomNavIndex by remember { mutableIntStateOf(-1) }

    val isAnyMenuModalOrDialogActive = showSignInDialog ||
        showCoinStoreDialog ||
        showLuckySpinDialog ||
        showDiceCustomizationDialog ||
        showTokenSelectDialog ||
        showPlayWithFriendsDialog ||
        showPlayerSelectDialog ||
        showShareApkDialog ||
        showLanguageDialog ||
        showCityTournamentDialog ||
        showRoyalChampionsDialog ||
        showMatchmakingDialog ||
        showEarnCoinsDialog ||
        showCarromPoolDialog ||
        showCarromLeagueModeDialog ||
        showCarromTourneyDialog ||
        showCarromMatchmakingDialog ||
        showCarromPuckDialog ||
        showCarromStrikerDialog ||
        showSettingsDialog ||
        showShopModalHub ||
        showNoInternetDialog

    fun dismissAllMenuDialogs() {
        activeBottomNavIndex = -1
        showSignInDialog = false
        showCoinStoreDialog = false
        showLuckySpinDialog = false
        showDiceCustomizationDialog = false
        showTokenSelectDialog = false
        showPlayWithFriendsDialog = false
        showPlayerSelectDialog = false
        showShareApkDialog = false
        showLanguageDialog = false
        showCityTournamentDialog = false
        showRoyalChampionsDialog = false
        showMatchmakingDialog = false
        selectedTournamentForMatchmaking = null
        showEarnCoinsDialog = false
        showCarromPoolDialog = false
        showCarromLeagueModeDialog = false
        showCarromTourneyDialog = false
        showCarromMatchmakingDialog = false
        selectedCarromTierForMatchmaking = null
        showCarromPuckDialog = false
        showCarromStrikerDialog = false
        showSettingsDialog = false
        showShopModalHub = false
        showNoInternetDialog = false
    }

    // Intercept Back button ONLY when a dialog or sub-view modal is open to dismiss it.
    // On the main menu dashboard, BackHandler is disabled so it NEVER automatically navigates backward on entry!
    BackHandler(enabled = isAnyMenuModalOrDialogActive) {
        dismissAllMenuDialogs()
    }

    // Ambient Animation for Glowing Borders & Badges
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_v47")
    val borderAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "v47_ring_border"
    )

    val spinIconPulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "spin_pulse"
    )

    // Sharing Helpers
    fun shareToWhatsApp() {
        val shareText = if (language == AppLanguage.BENGALI) {
            "👑 কিং মিস্ত্রি লুডু রয়্যাল (King Mistry Ludo - Version 90) 👑\n\nবন্ধুদের সাথে লুডু ও ৩ডি ক্যারাম খেলুন এবং উপভোগ করুন ১৫,০০০ ফ্রী কয়েন! 🎁"
        } else {
            "👑 King Mistry Ludo Royale (Version 90) 👑\n\nPlay Ludo & 3D Carrom with friends and enjoy 15,000 FREE COINS! 🎁"
        }
        try {
            val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                `package` = "com.whatsapp"
                putExtra(Intent.EXTRA_TEXT, shareText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(whatsappIntent)
        } catch (_: Throwable) {
            try {
                val genericIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "King Mistry Ludo v90")
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                val chooser = Intent.createChooser(genericIntent, if (language == AppLanguage.BENGALI) "বন্ধুদের সাথে শেয়ার করুন" else "Share with Friends")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } catch (_: Throwable) {
            }
        }
    }

    fun shareAppText() {
        val shareText = if (language == AppLanguage.BENGALI) {
            "🎲 কিং মিস্ত্রি লুডু (King Mistry Ludo - Version 90) 🎲\n\nআসসালামু আলাইকুম! আসুন বন্ধুদের সাথে লুডু ও ৩ডি ক্যারাম খেলি একসাথে!"
        } else {
            "🎲 King Mistry Ludo (Version 90) 🎲\n\nJoin me in King Mistry Ludo & 3D Carrom Royale (v90)!"
        }
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "King Mistry Ludo v90")
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            val chooser = Intent.createChooser(intent, if (language == AppLanguage.BENGALI) "বন্ধুদের সাথে শেয়ার করুন" else "Share with Friends")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (_: Throwable) {
        }
    }

    // MAIN SINGLE-SCREEN FIT CONTAINER (Zero Scrolling)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF091426))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // High-end ambient background diamond tiles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 130f
            val w = size.width
            val h = size.height
            if (w > 0f && h > 0f) {
                val cols = (w / step).toInt() + 2
                val rows = (h / (step * 0.85f)).toInt() + 2
                for (r in 0..rows) {
                    val startX = if (r % 2 == 0) 0f else step / 2f
                    val y = r * step * 0.85f
                    for (c in 0..cols) {
                        val x = startX + c * step
                        rotate(degrees = 35f, pivot = Offset(x, y)) {
                            drawRoundRect(
                                color = Color(0xFF132238).copy(alpha = 0.35f),
                                topLeft = Offset(x - 36f, y - 36f),
                                size = Size(72f, 72f),
                                cornerRadius = CornerRadius(14f, 14f)
                            )
                        }
                    }
                }
            }
        }

        // Single Screen Fixed Column - Fits screen cleanly from top status bar to bottom nav
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 10.dp, top = 4.dp, end = 10.dp, bottom = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // =========================================================================
            // 1. TOP BAR LAYOUT (4 Neon Pills — ENG Button Removed)
            // 1st (Left):      [ 👤 Sign in / Profile ]
            // 2nd (Primary):   [ 🪙 Coins + ]
            // 3rd (Primary):   [ 💎 Diamonds ] (1,00,000 Coins = 1 Diamond)
            // 4th (Right):     [ 🎲 Spin✨ ]
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1st: Sign-In / Profile Button (Left)
                Surface(
                    onClick = {
                        soundManager.playButtonClick()
                        dismissAllMenuDialogs()
                        showSignInDialog = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF05172E),
                    border = BorderStroke(2.dp, if (isSignedIn) Color(0xFF00E676) else Color(0xFF00E5FF)),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .weight(0.95f)
                        .height(44.dp)
                        .shadow(
                            6.dp,
                            RoundedCornerShape(12.dp),
                            spotColor = if (isSignedIn) Color(0xFF00E676) else Color(0xFF00E5FF),
                            ambientColor = if (isSignedIn) Color(0xFF00E676) else Color(0xFF00E5FF)
                        )
                        .testTag("top_bar_sign_in_btn")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF08274A), Color(0xFF031429))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isSignedIn) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1B263B))
                                        .border(1.2.dp, Color(0xFF00E676), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    NasidulAvatar()
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Column(verticalArrangement = Arrangement.Center) {
                                    Text(
                                        text = if (language == AppLanguage.BENGALI) "প্রোফাইল" else "Profile",
                                        color = Color(0xFF00E676),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp,
                                        lineHeight = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Lv.$playerLevel",
                                        color = Color(0xFFFFD700),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 8.5.sp,
                                        lineHeight = 9.sp,
                                        maxLines = 1
                                    )
                                }
                            } else {
                                Icon(
                                    Icons.Filled.Person,
                                    contentDescription = "Sign In",
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = when (language) {
                                        AppLanguage.BENGALI -> "সাইন ইন"
                                        AppLanguage.HINDI -> "लॉगिन"
                                        else -> "Sign in"
                                    },
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // 2nd: Coin Balance Display (Primary Spot)
                Surface(
                    onClick = {
                        soundManager.playButtonClick()
                        dismissAllMenuDialogs()
                        showCoinStoreDialog = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF05172E),
                    border = BorderStroke(2.dp, Color(0xFFFFD700)),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .weight(1.16f)
                        .height(44.dp)
                        .shadow(6.dp, RoundedCornerShape(12.dp), spotColor = Color(0xFFFFD700), ambientColor = Color(0xFFFFD700))
                        .testTag("top_bar_coin_balance_btn")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF261D02), Color(0xFF07192C))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🪙", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = NumberFormat.getNumberInstance(Locale.US).format(coins),
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00C853)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "Add Coins",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }

                // 3rd: Diamond Currency Indicator (Primary Spot beside Coins: 1,00,000 Coins = 1 Diamond)
                Surface(
                    onClick = {
                        soundManager.playButtonClick()
                        dismissAllMenuDialogs()
                        showCoinStoreDialog = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF05172E),
                    border = BorderStroke(
                        2.dp,
                        Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFF80D8FF), Color(0xFF00B0FF)))
                    ),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .weight(0.98f)
                        .height(44.dp)
                        .shadow(6.dp, RoundedCornerShape(12.dp), spotColor = Color(0xFF00E5FF), ambientColor = Color(0xFF00B0FF))
                        .testTag("top_bar_diamond_balance_btn")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF062C46), Color(0xFF041428))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("💎", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = NumberFormat.getNumberInstance(Locale.US).format(diamonds),
                                color = Color(0xFF80D8FF),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // 4th: Daily Spin Button (Moved to Far Right / 4th Position)
                Surface(
                    onClick = {
                        soundManager.playButtonClick()
                        if (isSpinLocked) {
                            Toast.makeText(
                                context,
                                if (language == AppLanguage.BENGALI)
                                    "স্পিন ২৪ ঘণ্টার জন্য লক করা আছে! বাকি: $spinCountdownFormatted"
                                else
                                    "Spin locked! Next free spin available in $spinCountdownFormatted",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        dismissAllMenuDialogs()
                        showLuckySpinDialog = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF05172E),
                    border = BorderStroke(2.dp, if (isSpinLocked) Color(0xFFFF5252) else Color(0xFFFF00E5)),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .weight(if (isSpinLocked) 1.12f else 0.91f)
                        .height(44.dp)
                        .scale(if (isSpinLocked) 1.0f else spinIconPulse)
                        .shadow(6.dp, RoundedCornerShape(12.dp), spotColor = if (isSpinLocked) Color(0xFFFF5252) else Color(0xFFFF00E5), ambientColor = if (isSpinLocked) Color(0xFFFF5252) else Color(0xFFFF00E5))
                        .testTag("top_bar_daily_spin_btn")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    if (isSpinLocked) {
                                        listOf(Color(0xFF2A0A12), Color(0xFF0C0614))
                                    } else {
                                        listOf(Color(0xFF2E0238), Color(0xFF07132B))
                                    }
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isSpinLocked) {
                                Text("🔒", fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = spinCountdownFormatted,
                                    color = Color(0xFFFF5252),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            } else {
                                Text("🎲", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = when (language) {
                                        AppLanguage.BENGALI -> "স্পিন"
                                        AppLanguage.HINDI -> "स्पिन"
                                        else -> "Spin"
                                    },
                                    color = Color(0xFFFF4081),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.5.sp,
                                    maxLines = 1
                                )
                                Text("✨", fontSize = 10.sp, color = Color(0xFFFFD54F))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // =========================================================================
            // 2. MIDDLE SECTION: USER PROFILE INFO CARD
            // High-intensity multi-color neon glowing border with continuous rotating/
            // circulating light ring effect in vibrant Neon Red, Neon Pink, and Neon Blue.
            // =========================================================================
            val neonRingTransition = rememberInfiniteTransition(label = "NeonProfileBorderTransition")

            // 1. Primary continuous circulating light ring rotation (0f -> 360f, 2800ms)
            val neonSweepRotation by neonRingTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 2800, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "neonSweepRotation"
            )

            // 2. Counter-circulating secondary light shimmer (360f -> 0f, 3800ms)
            val neonCounterRotation by neonRingTransition.animateFloat(
                initialValue = 360f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 3800, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "neonCounterRotation"
            )

            // 3. High-intensity neon glow & shimmer pulse (breathing luminosity)
            val neonGlowIntensity by neonRingTransition.animateFloat(
                initialValue = 0.85f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "neonGlowIntensity"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .shadow(
                        elevation = 22.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = Color(0xFFFF007F), // Vivid Neon Pink glow
                        ambientColor = Color(0xFF00E5FF) // Electric Neon Blue glow
                    )
                    .clip(RoundedCornerShape(20.dp))
                    // Base Dark Neon Bed underneath the glowing rings
                    .background(Color(0xFF020714))
                    // Continuous rotating/circulating multi-color neon light ring: Neon Red, Neon Pink, Neon Blue
                    .drawBehind {
                        val maxDim = maxOf(size.width, size.height)
                        val cx = size.width / 2f
                        val cy = size.height / 2f

                        // Helper to create rotating multi-color neon brush: Neon Red -> Neon Pink -> Neon Blue
                        fun createCirculatingNeonBrush(
                            angleDegrees: Float,
                            alphaMultiplier: Float = 1.0f
                        ): Brush {
                            val rad = Math.toRadians(angleDegrees.toDouble())
                            val cos = Math.cos(rad).toFloat()
                            val sin = Math.sin(rad).toFloat()
                            val start = Offset(cx - cos * maxDim, cy - sin * maxDim)
                            val end = Offset(cx + cos * maxDim, cy + sin * maxDim)
                            return Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFFF0033).copy(alpha = 0.95f * alphaMultiplier), // Vibrant Neon Red
                                    Color(0xFFFF007F).copy(alpha = 1.0f * alphaMultiplier),  // Vibrant Neon Pink
                                    Color(0xFFFF4081).copy(alpha = 0.95f * alphaMultiplier), // Hot Pink
                                    Color(0xFFE040FB).copy(alpha = 0.90f * alphaMultiplier), // Neon Magenta/Purple transition
                                    Color(0xFF00E5FF).copy(alpha = 1.0f * alphaMultiplier),  // Electric Neon Blue (Cyan)
                                    Color(0xFF0091EA).copy(alpha = 0.95f * alphaMultiplier), // Deep Vivid Blue
                                    Color(0xFF00E5FF).copy(alpha = 0.95f * alphaMultiplier), // Electric Neon Blue
                                    Color(0xFFFF007F).copy(alpha = 1.0f * alphaMultiplier),  // Vibrant Neon Pink
                                    Color(0xFFFF0033).copy(alpha = 0.95f * alphaMultiplier)  // Seamless loop to Neon Red
                                ),
                                start = start,
                                end = end
                            )
                        }

                        // Travelling luminous highlight spark/comet flare
                        fun createShimmerSparkBrush(angleDegrees: Float): Brush {
                            val rad = Math.toRadians(angleDegrees.toDouble())
                            val cos = Math.cos(rad).toFloat()
                            val sin = Math.sin(rad).toFloat()
                            val start = Offset(cx - cos * maxDim, cy - sin * maxDim)
                            val end = Offset(cx + cos * maxDim, cy + sin * maxDim)
                            return Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0x33FF007F),
                                    Color.White.copy(alpha = 0.95f * neonGlowIntensity), // Brilliant White Spark
                                    Color(0xFF00E5FF).copy(alpha = 0.90f * neonGlowIntensity), // Cyan Flare
                                    Color(0x66FF0033),
                                    Color.Transparent
                                ),
                                start = start,
                                end = end
                            )
                        }

                        // =====================================================
                        // 1. PRIMARY OUTER CIRCULATING NEON RING (Full Bloom)
                        // =====================================================
                        val outerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx())
                        val outerNeonBrush = createCirculatingNeonBrush(neonSweepRotation, neonGlowIntensity)

                        // 1A. Wide Diffused Outer Bloom Halo
                        drawRoundRect(
                            brush = outerNeonBrush,
                            cornerRadius = outerRadius,
                            style = Stroke(width = 8.0.dp.toPx())
                        )
                        // 1B. Concentrated Vibrant Neon Glow
                        drawRoundRect(
                            brush = outerNeonBrush,
                            cornerRadius = outerRadius,
                            style = Stroke(width = 4.8.dp.toPx())
                        )
                        // 1C. Razor-Sharp Brilliant Core Wire
                        drawRoundRect(
                            brush = outerNeonBrush,
                            cornerRadius = outerRadius,
                            style = Stroke(width = 2.4.dp.toPx())
                        )

                        // =====================================================
                        // 2. SECONDARY INNER COUNTER-CIRCULATING RING
                        // =====================================================
                        val innerInset = 2.8.dp.toPx()
                        val innerSize = Size(
                            (size.width - innerInset * 2).coerceAtLeast(0f),
                            (size.height - innerInset * 2).coerceAtLeast(0f)
                        )
                        val innerRadius = CornerRadius(
                            (20.dp.toPx() - innerInset).coerceAtLeast(0f),
                            (20.dp.toPx() - innerInset).coerceAtLeast(0f)
                        )
                        val innerNeonBrush = createCirculatingNeonBrush(neonCounterRotation, neonGlowIntensity * 0.88f)

                        // 2A. Inner Glow Halo
                        drawRoundRect(
                            brush = innerNeonBrush,
                            topLeft = Offset(innerInset, innerInset),
                            size = innerSize,
                            cornerRadius = innerRadius,
                            style = Stroke(width = 4.5.dp.toPx())
                        )
                        // 2B. Inner Core Wire
                        drawRoundRect(
                            brush = innerNeonBrush,
                            topLeft = Offset(innerInset, innerInset),
                            size = innerSize,
                            cornerRadius = innerRadius,
                            style = Stroke(width = 2.0.dp.toPx())
                        )

                        // =====================================================
                        // 3. TRAVELLING HIGH-INTENSITY SHIMMER BEAMS
                        // =====================================================
                        val sparkBrush = createShimmerSparkBrush(neonSweepRotation)
                        drawRoundRect(
                            brush = sparkBrush,
                            cornerRadius = outerRadius,
                            style = Stroke(width = 3.6.dp.toPx())
                        )
                    }
                    .padding(5.8.dp) // Inset for the circulating neon border
                    .clip(RoundedCornerShape(15.dp))
                    .background(Color(0xFF03142C)) // Deep Navy Background
                    .drawBehind {
                        // Diagonal electric cyan neon laser streak line cutting across background as seen in screenshot
                        drawLine(
                            brush = Brush.linearGradient(
                                listOf(Color.Transparent, Color(0x6600E5FF), Color(0xEE00E5FF), Color.Transparent),
                                start = Offset(size.width * 0.45f, size.height),
                                end = Offset(size.width * 0.95f, 0f)
                            ),
                            start = Offset(size.width * 0.45f, size.height),
                            end = Offset(size.width * 0.95f, 0f),
                            strokeWidth = 3.dp.toPx()
                        )
                    }
                    .clickable {
                        soundManager.playButtonClick()
                        dismissAllMenuDialogs()
                        showSignInDialog = true
                    }
                    .testTag("middle_profile_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Avatar (Nasidul Mistry) - Clean gold frame restoring authentic v88 home banner photo
                    Box(modifier = Modifier.size(70.dp), contentAlignment = Alignment.Center) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF1B263B),
                            border = BorderStroke(2.8.dp, Color(0xFFFFD700)), // Rich Metallic Gold Border
                            shadowElevation = 8.dp,
                            modifier = Modifier.size(70.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.img_nasidul_mistry_profile),
                                contentDescription = "Nasidul Mistry Profile Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // User Details - Bold, royal typography strictly matching reference screenshot
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Nasidul Mistry",
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = "KING MISTRY",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 23.sp,
                            letterSpacing = 1.3.sp
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = "LUDO ROYALE",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            letterSpacing = 1.0.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⭐⭐⭐⭐⭐",
                                fontSize = 14.sp,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // =========================================================================
            // ONLINE & OFFLINE BUTTONS (Fully Interactive M3 Clickable Surfaces with Active Highlighting)
            // =========================================================================
            val isOnline = isOnlineTab
            val isOffline = !isOnlineTab

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ONLINE BUTTON (Active: Neon Cyan/Blue, Inactive: Dark Blue)
                // 1. ONLINE BUTTON (Active: Bright Neon Green Glow, Inactive: Muted Dark Blue)
                Surface(
                    onClick = {
                        soundManager.playButtonClick()
                        isOnlineTab = true
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isOnline) Color(0xFF00C853) else Color(0xFF04152D),
                    border = BorderStroke(
                        if (isOnline) 2.dp else 1.dp,
                        if (isOnline) Color(0xFF00FF66) else Color(0xFF13365E)
                    ),
                    shadowElevation = if (isOnline) 8.dp else 0.dp,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .shadow(if (isOnline) 10.dp else 0.dp, RoundedCornerShape(10.dp), spotColor = Color(0xFF00FF66), ambientColor = Color(0xFF00E676))
                        .testTag("toggle_online_btn")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (isOnline) Brush.horizontalGradient(
                                    listOf(Color(0xFF00FF66), Color(0xFF00E676), Color(0xFF00B248))
                                ) else Brush.horizontalGradient(
                                    listOf(Color(0xFF04152D), Color(0xFF021024))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isOnline) Color(0xFF00FF66) else Color(0xFF37474F))
                                    .then(if (isOnline) Modifier.border(1.2.dp, Color.White, CircleShape) else Modifier)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("🟢", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "ONLINE",
                                color = if (isOnline) Color.White else Color(0xFF78909C),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                }

                // 2. OFFLINE BUTTON (Active: Bright Cyan/Blue Neon Glow, Inactive: Muted Dark Blue)
                Surface(
                    onClick = {
                        soundManager.playButtonClick()
                        isOnlineTab = false
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isOffline) Color(0xFF0091EA) else Color(0xFF04152D),
                    border = BorderStroke(
                        if (isOffline) 2.dp else 1.dp,
                        if (isOffline) Color(0xFF00E5FF) else Color(0xFF13365E)
                    ),
                    shadowElevation = if (isOffline) 8.dp else 0.dp,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .shadow(if (isOffline) 10.dp else 0.dp, RoundedCornerShape(10.dp), spotColor = Color(0xFF00E5FF), ambientColor = Color(0xFF0091EA))
                        .testTag("toggle_offline_btn")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (isOffline) Brush.horizontalGradient(
                                    listOf(Color(0xFF00E5FF), Color(0xFF00B0FF), Color(0xFF0091EA))
                                ) else Brush.horizontalGradient(
                                    listOf(Color(0xFF04152D), Color(0xFF021024))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isOffline) Color(0xFF00E5FF) else Color(0xFF37474F))
                                    .then(if (isOffline) Modifier.border(1.2.dp, Color.White, CircleShape) else Modifier)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("🔵", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "OFFLINE",
                                color = if (isOffline) Color.White else Color(0xFF78909C),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                }
            }

            // Live Mode Status Indicator Strip
            AnimatedContent(
                targetState = isOnlineTab,
                transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(140)) },
                label = "mode_indicator_text"
            ) { online ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (online) Color(0xFF00E5FF) else Color(0xFFFFD54F))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (online) {
                            if (language == AppLanguage.BENGALI) "🟢 অনলাইন মাল্টিপ্লেয়ার মোড সক্রিয়" else "🟢 ONLINE MULTIPLAYER (ROOMS & TOURNAMENTS)"
                        } else {
                            if (language == AppLanguage.BENGALI) "🎮 অফলাইন লোকাল মোড সক্রিয় (ইন্টারনেট প্রয়োজন নেই)" else "🎮 OFFLINE LOCAL PASS & PLAY (NO INTERNET NEEDED)"
                        },
                        color = if (online) Color(0xFF80D8FF) else Color(0xFFFFD54F),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // =========================================================================
            // 3. MIDDLE SECTION: 6 UNIFORM 3D GLASSMORPHISM CARDS (3 ROWS x 2 COLUMNS)
            // + Sleek Single-Line WhatsApp Refer Strip (Replaces bulky Loan Banner)
            // =========================================================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 3D Glassmorphism Game Mode Cards Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    AnimatedContent(
                        targetState = isOnlineTab,
                        transitionSpec = {
                            fadeIn(tween(220)) togetherWith fadeOut(tween(180))
                        },
                        label = "online_offline_cards_transition",
                        modifier = Modifier.fillMaxSize()
                    ) { activeOnline ->
                        if (activeOnline) {
                            // --- 🟢 ONLINE TAB MODES: EXACTLY 6 GAME CARDS (3 ROWS x 2 COLUMNS) ---
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // ROW 1: 1. Ludo League & 2. Carrom League
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // 1. Ludo League — Professional Ludo Tournament (Premium 3D Trophy Arena)
                                    RoyaleGameModeCard(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        title = if (language == AppLanguage.BENGALI) "লুডো লীগ" else "Ludo League",
                                        artworkResId = R.drawable.art_ludo_league_3d,
                                        isOffline = false,
                                        onClick = {
                                            launchOnlineMode {
                                                soundManager.playButtonClick()
                                                showCityTournamentDialog = true
                                            }
                                        },
                                        onInfoClick = onOpenRules,
                                        testTag = "mode_card_online_ludo_league"
                                    )

                                    // 2. Carrom League — 7 Tiers Online Match
                                    RoyaleGameModeCard(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        title = if (language == AppLanguage.BENGALI) "ক্যারাম লীগ" else "Carrom League",
                                        artworkResId = R.drawable.art_carrom_board_3d,
                                        artworkScale = ContentScale.Fit,
                                        isOffline = false,
                                        onClick = {
                                            soundManager.playButtonClick()
                                            showCarromLeagueModeDialog = true
                                        },
                                        onInfoClick = onOpenRules,
                                        testTag = "mode_card_online_carrom_league"
                                    )
                                }

                                // ROW 2: 3. Express Ludo & 4. Snakes & Ladders
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // 3. Express Ludo — High-Speed Quick Ludo (3D Neon Lightning Action)
                                    RoyaleGameModeCard(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        title = if (language == AppLanguage.BENGALI) "এক্সপ্রেস লুডো" else "Express Ludo",
                                        artworkResId = R.drawable.art_express_ludo_3d,
                                        isOffline = false,
                                        onClick = {
                                            launchOnlineMode {
                                                soundManager.playButtonClick()
                                                selectedGameMode = GameMode.QUICK_LUDO
                                                playerCount = 2
                                                showPlayerSelectDialog = true
                                            }
                                        },
                                        onInfoClick = onOpenRules,
                                        testTag = "mode_card_online_express_ludo"
                                    )

                                    // 4. Snakes & Ladders — Classic Board
                                    RoyaleGameModeCard(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        title = if (language == AppLanguage.BENGALI) "সাপ ও মই" else "Snakes & Ladders",
                                        artworkResId = R.drawable.art_snakes_ladders,
                                        isOffline = false,
                                        onClick = {
                                            launchOnlineMode {
                                                soundManager.playButtonClick()
                                                selectedGameMode = GameMode.SNAKES_AND_LADDERS
                                                isSnakesAndLaddersOnline = true
                                                playerCount = 2
                                                showPlayerSelectDialog = true
                                            }
                                        },
                                        onInfoClick = onOpenRules,
                                        testTag = "mode_card_online_snakes_ladders"
                                    )
                                }

                                // ROW 3: 5. Ludo Friends & 6. Carrom Friends
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // 5. Ludo Friends — Private Room
                                    RoyaleGameModeCard(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        title = if (language == AppLanguage.BENGALI) "লুডো ফ্রেন্ডস" else "Ludo Friends",
                                        artworkResId = R.drawable.art_pass_play,
                                        isOffline = false,
                                        onClick = {
                                            launchOnlineMode {
                                                soundManager.playButtonClick()
                                                friendsDialogGameType = "LUDO"
                                                showPlayWithFriendsDialog = true
                                            }
                                        },
                                        onInfoClick = onOpenRules,
                                        testTag = "mode_card_online_ludo_friends"
                                    )

                                    // 6. Carrom Friends — Private Room 3D Carrom
                                    RoyaleGameModeCard(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        title = if (language == AppLanguage.BENGALI) "ক্যারাম ফ্রেন্ডস" else "Carrom Friends",
                                        artworkResId = R.drawable.art_carrom_board_3d,
                                        artworkScale = ContentScale.Fit,
                                        isOffline = false,
                                        onClick = {
                                            launchOnlineMode {
                                                soundManager.playButtonClick()
                                                friendsDialogGameType = "CARROM"
                                                showPlayWithFriendsDialog = true
                                            }
                                        },
                                        onInfoClick = onOpenRules,
                                        testTag = "mode_card_online_carrom_friends"
                                    )
                                }
                            }
                        } else {
                            // --- 🔵 OFFLINE TAB MODES: EXACTLY 6 GAME CARDS (3 ROWS x 2 COLUMNS) ---
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // ROW 1: 1. Pass & Play & 2. Open Token Ludo
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // 1. Pass & Play — Classic Ludo
                                    RoyaleGameModeCard(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        title = if (language == AppLanguage.BENGALI) "পাস অ্যান্ড প্লে" else "Pass & Play",
                                        artworkResId = R.drawable.art_pass_play,
                                        isOffline = true,
                                        onClick = {
                                            soundManager.playButtonClick()
                                            selectedGameMode = GameMode.PASS_AND_PLAY
                                            playerCount = 2
                                            p1Name = "Player 1"
                                            p2Name = if (language == AppLanguage.BENGALI) "খেলোয়াড় ২" else "Player 2"
                                            showPlayerSelectDialog = true
                                        },
                                        onInfoClick = onOpenRules,
                                        testTag = "mode_card_offline_pass_play"
                                    )

                                    // 2. Open Token Ludo — All 4 Tokens Unlocked
                                    RoyaleGameModeCard(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        title = if (language == AppLanguage.BENGALI) "ওপেন টোকেন লুডো" else "Open Token Ludo",
                                        artworkResId = R.drawable.img_royal_gold_banner,
                                        contentBadges = {
                                            OpenTokenBadgeOverlay()
                                        },
                                        isOffline = true,
                                        onClick = {
                                            soundManager.playButtonClick()
                                            selectedGameMode = GameMode.OPEN_TOKEN
                                            playerCount = 2
                                            p1Name = "Player 1"
                                            p2Name = if (language == AppLanguage.BENGALI) "কম্পিউটার (AI)" else "Computer (AI)"
                                            showPlayerSelectDialog = true
                                        },
                                        onInfoClick = onOpenRules,
                                        testTag = "mode_card_offline_open_token"
                                    )
                                }

                                // ROW 2: 3. Team vs Team & 4. Express Ludo
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // 3. Team vs Team — 2v2 Local Duet (Custom 3D Ruby vs Sapphire Board)
                                    RoyaleGameModeCard(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        title = if (language == AppLanguage.BENGALI) "টিম বনাম টিম" else "Team vs Team",
                                        artworkResId = R.drawable.art_team_vs_team_3d,
                                        isOffline = true,
                                        onClick = {
                                            soundManager.playButtonClick()
                                            selectedGameMode = GameMode.TEAM_2V2
                                            playerCount = 4
                                            p1Name = userName
                                            p2Name = if (language == AppLanguage.BENGALI) "খেলোয়াড় ২" else "Player 2"
                                            p3Name = if (language == AppLanguage.BENGALI) "অংশীদার ৩" else "Partner 3"
                                            p4Name = if (language == AppLanguage.BENGALI) "খেলোয়াড় ৪" else "Player 4"
                                            showPlayerSelectDialog = true
                                        },
                                        onInfoClick = onOpenRules,
                                        testTag = "mode_card_offline_team_vs_team"
                                    )

                                    // 4. Express Ludo — High-Speed Quick Offline Ludo (3D Lightning Dice)
                                    RoyaleGameModeCard(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        title = if (language == AppLanguage.BENGALI) "এক্সপ্রেস লুডো" else "Express Ludo",
                                        artworkResId = R.drawable.art_express_ludo_3d,
                                        isOffline = true,
                                        onClick = {
                                            soundManager.playButtonClick()
                                            selectedGameMode = GameMode.QUICK_LUDO
                                            playerCount = 2
                                            showPlayerSelectDialog = true
                                        },
                                        onInfoClick = onOpenRules,
                                        testTag = "mode_card_offline_express_ludo"
                                    )
                                }

                                // ROW 3: 5. Snakes & Ladders & 6. Carrom Pool
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // 5. Snakes & Ladders — Classic Board
                                    RoyaleGameModeCard(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        title = if (language == AppLanguage.BENGALI) "সাপ ও মই" else "Snakes & Ladders",
                                        artworkResId = R.drawable.art_snakes_ladders,
                                        isOffline = true,
                                        onClick = {
                                            soundManager.playButtonClick()
                                            selectedGameMode = GameMode.SNAKES_AND_LADDERS
                                            isSnakesAndLaddersOnline = false
                                            playerCount = 2
                                            showPlayerSelectDialog = true
                                        },
                                        onInfoClick = onOpenRules,
                                        testTag = "mode_card_offline_snakes_ladders"
                                    )

                                    // 6. Carrom Pool — Offline 3D Board
                                    RoyaleGameModeCard(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        title = if (language == AppLanguage.BENGALI) "ক্যারাম পুল" else "Carrom Pool",
                                        artworkResId = R.drawable.art_carrom_board_3d,
                                        artworkScale = ContentScale.Fit,
                                        isOffline = true,
                                        onClick = {
                                            soundManager.playButtonClick()
                                            showCarromPoolDialog = true
                                        },
                                        onInfoClick = onOpenRules,
                                        testTag = "mode_card_offline_carrom_pool"
                                    )
                                }
                            }
                        }
                    }
                }

                // =========================================================================
                // =========================================================================
                // 3.5. CLEAN & SPACIOUS REFERRAL BANNER
                // Single centered bold line: "Invite Friends & Get 15,000 Coins"
                // Fully clickable across entire banner to trigger native share options
                // =========================================================================
                Surface(
                    onClick = {
                        soundManager.playButtonClick()
                        shareAppText()
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF04180E),
                    border = BorderStroke(
                        1.8.dp,
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFFFFEA00), // 24K Brilliant Gold
                                Color(0xFF00FF66), // Vibrant Neon Emerald
                                Color(0xFFFFD700), // Royal Gold
                                Color(0xFF00E5FF), // Electric Diamond Cyan
                                Color(0xFFFFEA00)  // Seamless Loop
                            )
                        )
                    ),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .shadow(8.dp, RoundedCornerShape(14.dp), spotColor = Color(0xFF00FF66), ambientColor = Color(0xFFFFD700))
                        .testTag("sleek_refer_strip")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF021B10),
                                        Color(0xFF052A18),
                                        Color(0xFF021B10)
                                    )
                                )
                            )
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (language == AppLanguage.BENGALI) "বন্ধুদের ইনভাইট করুন আর পান ১৫,০০০ কয়েন" else "Invite Friends & Get 15,000 Coins",
                            color = Color(0xFFFFEA00),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.5.sp,
                            letterSpacing = 0.5.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            // =========================================================================
            // 4. 4-BUTTON BOTTOM NAVIGATION BAR [Coins/Tokens, Shop, Strikers, Champions]
            // Leaderboard removed from bottom bar & integrated directly into Champions
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .testTag("bottom_navigation_bar"),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1st: [🪙] DEDICATED CARROM COIN / TOKEN SHOWCASE
                BottomNavItem(
                    modifier = Modifier.weight(1f),
                    icon = {
                        Box(
                            modifier = Modifier.size(26.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            SafePuckIcon(
                                puckSet = selectedPuckSet,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    label = if (language == AppLanguage.BENGALI) "কয়েন/টোকেন" else "Coins/Tokens",
                    isActive = activeBottomNavIndex == 0 || showCarromPuckDialog,
                    onClick = {
                        soundManager.playButtonClick()
                        dismissAllMenuDialogs()
                        activeBottomNavIndex = 0
                        showCarromPuckDialog = true
                    },
                    testTag = "bottom_nav_carrom_coins"
                )

                // 2nd (Replaces Old Leaderboard): [🛒] SHOP (Instantly opens Diamond & Coin Purchase Store)
                BottomNavItem(
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = "Shop",
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = if (language == AppLanguage.BENGALI) "শপ" else "Shop",
                    isActive = activeBottomNavIndex == 1 || showCoinStoreDialog,
                    onClick = {
                        soundManager.playButtonClick()
                        dismissAllMenuDialogs()
                        activeBottomNavIndex = 1
                        showCoinStoreDialog = true
                    },
                    testTag = "bottom_nav_shop"
                )

                // 3rd: [🎯] CARROM STRIKER ARSENAL (4K Strikers)
                BottomNavItem(
                    modifier = Modifier.weight(1f),
                    icon = {
                        Box(
                            modifier = Modifier.size(26.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            SafeStrikerIcon(
                                striker = selectedStriker,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    label = if (language == AppLanguage.BENGALI) "স্ট্রাইকার" else "Strikers",
                    isActive = activeBottomNavIndex == 2 || showCarromStrikerDialog,
                    onClick = {
                        soundManager.playButtonClick()
                        dismissAllMenuDialogs()
                        activeBottomNavIndex = 2
                        showCarromStrikerDialog = true
                    },
                    testTag = "bottom_nav_carrom_striker"
                )

                // 4th: [👑] CHAMPIONS (Hall of Fame + Integrated Top 30 Leaderboard + Stats)
                BottomNavItem(
                    modifier = Modifier.weight(1f),
                    icon = {
                        Text(
                            text = "👑",
                            fontSize = 20.sp
                        )
                    },
                    label = if (language == AppLanguage.BENGALI) "চ্যাম্পিয়ন" else "Champions",
                    isActive = activeBottomNavIndex == 3 || showRoyalChampionsDialog,
                    onClick = {
                        soundManager.playButtonClick()
                        dismissAllMenuDialogs()
                        activeBottomNavIndex = 3
                        showRoyalChampionsDialog = true
                    },
                    testTag = "bottom_nav_royal_champions"
                )
            }
        }
    }

    // =========================================================================
    // DIALOGS & POPUPS (Fully Operational, Clickable, No Placeholders)
    // =========================================================================

    // 0A. Royal Shop Modal Hub Dialog
    if (showShopModalHub) {
        RoyalShopModalHubDialog(
            language = language,
            onDismiss = { showShopModalHub = false },
            onOpenDiceSkins = { showDiceCustomizationDialog = true },
            onOpenTokens = { showTokenSelectDialog = true },
            onOpenCoinLoan = { showEarnCoinsDialog = true },
            onOpenPuckSets = { showCarromPuckDialog = true },
            onOpenStrikers = { showCarromStrikerDialog = true }
        )
    }

    // 0B. Glowing 3D Settings Dialog
    if (showSettingsDialog) {
        RoyalSettingsDialog(
            language = language,
            onLanguageChange = { newLang ->
                onSelectLanguage(newLang)
            },
            soundManager = soundManager,
            isDarkTheme = isDarkTheme,
            onToggleDarkTheme = { dark ->
                isDarkTheme = dark
                prefs.edit().putBoolean("board_theme_dark", dark).apply()
            },
            onLogout = {
                CoinManager.signOut()
                showSettingsDialog = false
                Toast.makeText(context, if (language == AppLanguage.BENGALI) "লগ আউট সম্পন্ন হয়েছে!" else "Logged out successfully!", Toast.LENGTH_SHORT).show()
            },
            onOpenRules = onOpenRules,
            onShareApp = { shareAppText() },
            onDismiss = { showSettingsDialog = false }
        )
    }

    // 1. Sign-In & Profile Flow Dialog
    if (showSignInDialog) {
        AuthenticationDialog(
            language = language,
            onDismiss = { showSignInDialog = false },
            onSuccessAuth = { _ ->
                soundManager.playVictoryFanfare()
            }
        )
    }

    // 2. Diamond & Coin Purchase Store (Google Play Billing + Daily Bonus)
    if (showCoinStoreDialog) {
        DiamondCoinStoreDialog(
            language = language,
            currentCoins = coins,
            currentDiamonds = diamonds,
            onClaimDailyBonus = { bonusAmount ->
                soundManager.playVictoryFanfare()
                CoinManager.addCoins(bonusAmount, "Daily Bonus")
            },
            onOpenDiceSkins = { showDiceCustomizationDialog = true },
            onOpenTokens = { showTokenSelectDialog = true },
            onOpenCoinLoan = { showEarnCoinsDialog = true },
            onDismiss = {
                showCoinStoreDialog = false
                activeBottomNavIndex = -1
            }
        )
    }

    // 3. Puzzle & Daily Lucky Spin Wheel Dialog
    if (showLuckySpinDialog) {
        LuckySpinWheelDialog(
            language = language,
            onWinPrize = { prizeCoins, prizeTitle ->
                soundManager.playVictoryFanfare()
                Toast.makeText(context, "🎉 $prizeTitle!", Toast.LENGTH_LONG).show()
            },
            onDismiss = {
                showLuckySpinDialog = false
            }
        )
    }

    // 4. Language Selection Dialog (English, Bengali, Hindi)
    if (showLanguageDialog) {
        Dialog(onDismissRequest = { showLanguageDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F1E33),
                border = BorderStroke(2.dp, Color(0xFFFFD700)),
                shadowElevation = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .testTag("language_selection_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Language,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (language) {
                                    AppLanguage.BENGALI -> "ভাষা নির্বাচন করুন"
                                    AppLanguage.HINDI -> "भाषा चुनें"
                                    else -> "Select Language"
                                },
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                        IconButton(onClick = { showLanguageDialog = false }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.LightGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val languageOptions = listOf(
                        Triple(AppLanguage.ENGLISH, "English", "Default International"),
                        Triple(AppLanguage.BENGALI, "বাংলা (Bengali)", "সম্পূর্ণ বাংলা ইন্টারফেস"),
                        Triple(AppLanguage.HINDI, "हिन्दी (Hindi)", "पूर्ण हिन्दी इंटरफ़ेस")
                    )

                    languageOptions.forEach { (lang, label, subtitle) ->
                        val isSelected = language == lang
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.18f) else Color(0xFF1B263B),
                            border = BorderStroke(
                                1.5.dp,
                                if (isSelected) Color(0xFF00E5FF) else Color(0xFF2A3C59)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clickable {
                                    soundManager.playButtonClick()
                                    onSelectLanguage(lang)
                                    showLanguageDialog = false
                                }
                                .testTag("lang_opt_${lang.code}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color(0xFF00E5FF) else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = subtitle,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                }
                                if (isSelected) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF00E5FF),
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                "✓",
                                                color = Color(0xFF0D1B2A),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 5. Dice Customization Dialog
    if (showDiceCustomizationDialog) {
        DiceSelectionInterface(
            language = language,
            selectedSkin = selectedDiceSkin,
            onSelectSkin = { newSkin ->
                selectedDiceSkin = newSkin
                onSelectDiceSkin(newSkin)
                prefs.edit().putString("selected_dice_skin", newSkin.id).apply()
                context.getSharedPreferences("ludo_game_prefs", Context.MODE_PRIVATE)
                    .edit().putString("selected_dice_skin", newSkin.id).apply()
            },
            onDismiss = {
                showDiceCustomizationDialog = false
                activeBottomNavIndex = -1
            }
        )
    }

    // 6. Token/Guti Selection Shop Dialog
    if (showTokenSelectDialog) {
        TokenSelectionDialog(
            isBengali = (language == AppLanguage.BENGALI),
            selectedToken = selectedToken,
            onSelectToken = { token ->
                selectedToken = token
                onSelectToken(token)
                prefs.edit().putString("selected_premium_token", token.id).apply()
                context.getSharedPreferences("ludo_game_prefs", Context.MODE_PRIVATE)
                    .edit().putString("selected_premium_token", token.id).apply()
                context.getSharedPreferences("king_mistry_ludo_prefs", Context.MODE_PRIVATE)
                    .edit().putString("selected_premium_token", token.id).apply()
                showTokenSelectDialog = false
                activeBottomNavIndex = -1
                Toast.makeText(
                    context,
                    if (language == AppLanguage.BENGALI) "${token.titleBn} — ৪ রঙের সব গুটিতে যুক্ত হয়েছে!" else "${token.titleEn} Equipped for All 4 Colors!",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onDismiss = {
                showTokenSelectDialog = false
                activeBottomNavIndex = -1
            }
        )
    }

    // 6b. Carrom High-Definition Custom Puck Sets Collection Dialog
    if (showCarromPuckDialog) {
        CarromPuckCollectionDialog(
            isBengali = (language == AppLanguage.BENGALI),
            currentEquippedSet = selectedPuckSet,
            onSelectPuckSet = { puckSet ->
                if (CarromPuckSet.setEquippedSet(context, puckSet)) {
                    selectedPuckSet = puckSet
                }
            },
            onOpenStrikers = {
                showCarromPuckDialog = false
                showCarromStrikerDialog = true
                activeBottomNavIndex = 2
            },
            onDismiss = {
                showCarromPuckDialog = false
                activeBottomNavIndex = -1
            }
        )
    }

    // 6c. Carrom High-Definition 4K Custom Striker Arsenal Dialog
    if (showCarromStrikerDialog) {
        CarromStrikerCollectionDialog(
            isBengali = (language == AppLanguage.BENGALI),
            currentEquippedStriker = selectedStriker,
            onSelectStriker = { striker ->
                if (CarromStriker.setEquippedStriker(context, striker)) {
                    selectedStriker = striker
                }
            },
            onOpenPuckSets = {
                showCarromStrikerDialog = false
                showCarromPuckDialog = true
                activeBottomNavIndex = 0
            },
            onDismiss = {
                showCarromStrikerDialog = false
                activeBottomNavIndex = -1
            }
        )
    }

    // 7. Play with Friends (WhatsApp Room Code) Dialog
    if (showPlayWithFriendsDialog) {
        PlayWithFriendsDialog(
            isBengali = (language == AppLanguage.BENGALI),
            gameType = friendsDialogGameType,
            onDismiss = {
                showPlayWithFriendsDialog = false
                activeBottomNavIndex = -1
            },
            onStartGame = { roomCode, localPlayerId, pCount, names, isDoubleTeam, coinWager ->
                try {
                    showPlayWithFriendsDialog = false
                    if (friendsDialogGameType.equals("CARROM", ignoreCase = true)) {
                        val p1 = names.getOrNull(0)?.takeIf { it.isNotBlank() } ?: userName
                        val p2 = names.getOrNull(1)?.takeIf { it.isNotBlank() } ?: "Opponent"
                        onStartCarromGame(false, "CARROM FRIENDS 1v1", (coinWager * 2).coerceAtLeast(0), p1, p2, false, (localPlayerId + 1).coerceIn(1, 2), coinWager.coerceAtLeast(0), CarromGameMode.STANDARD)
                    } else {
                        onStartOnlineGame(roomCode, localPlayerId, pCount, names, isDoubleTeam, coinWager)
                    }
                } catch (t: Throwable) {
                    android.util.Log.e("LudoMenuScreen", "Failed starting friends match", t)
                }
            }
        )
    }

    // 8. No Internet Connection Warning Modal
    if (showNoInternetDialog) {
        NoInternetDialog(
            language = language,
            onDismiss = { showNoInternetDialog = false },
            onSwitchToOffline = {
                showNoInternetDialog = false
                isOnlineTab = false
            }
        )
    }

    // 8. Player Selection & Match Configuration Dialog
    if (showPlayerSelectDialog) {
        Dialog(onDismissRequest = { showPlayerSelectDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F1E33),
                border = BorderStroke(2.dp, Color(0xFFFFD54F)),
                shadowElevation = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (selectedGameMode) {
                                GameMode.SNAKES_AND_LADDERS -> if (language == AppLanguage.BENGALI) "লুডু সাপ ও মই ম্যাচ" else "Snakes & Ladders"
                                GameMode.QUICK_LUDO -> if (language == AppLanguage.BENGALI) "কুইক লুডো ম্যাচ" else "Quick Ludo Match"
                                GameMode.TEAM_2V2 -> if (language == AppLanguage.BENGALI) "২ বনাম ২ টিম ম্যাচ" else "2v2 Team Play"
                                GameMode.OPEN_TOKEN -> if (language == AppLanguage.BENGALI) "ওপেন টোকেন লুডো (স্টার আনলকড)" else "Open Token Ludo (Instant Star Unlock)"
                                else -> if (playerCount == 2) (if (language == AppLanguage.BENGALI) "ডুয়েট লোকাল ম্যাচ (২ জন)" else "DUET Local Match (2 Players)") else (if (language == AppLanguage.BENGALI) "পাস অ্যান্ড প্লে ম্যাচ" else "Pass & Play Match")
                            },
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = Color(0xFFFFD54F)
                        )

                        IconButton(onClick = { showPlayerSelectDialog = false }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.LightGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (selectedGameMode == GameMode.TEAM_2V2) {
                        // 2v2 Partnership Setup Badge (Team 1: P1 & P3 vs Team 2: P2 & P4)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF071B33),
                            border = BorderStroke(1.2.dp, Color(0xFF00E5FF)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = if (language == AppLanguage.BENGALI) "⚔️ ২ বনাম ২ পার্টনারশিপ মোড (৪ জন খেলোয়াড়)" else "⚔️ 2v2 PARTNERSHIP MODE (4 PLAYERS)",
                                    color = Color(0xFFFFD54F),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Team 1: Player 1 (Red) & Player 3 (Yellow)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF1E1010),
                                        border = BorderStroke(1.dp, Color(0xFFE53935)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(6.dp)) {
                                            Text(
                                                text = if (language == AppLanguage.BENGALI) "টিম ১ (লাল ও হলুদ)" else "TEAM 1 (Red & Yellow)",
                                                color = Color(0xFFFF8A80),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                            Text(
                                                text = "P1 ($userName) & P3",
                                                color = Color.White,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Text(" ⚔️ ", fontSize = 12.sp)

                                    // Team 2: Player 2 (Green) & Player 4 (Blue)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF0A1B24),
                                        border = BorderStroke(1.dp, Color(0xFF00E5FF)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(6.dp)) {
                                            Text(
                                                text = if (language == AppLanguage.BENGALI) "টিম ২ (সবুজ ও নীল)" else "TEAM 2 (Green & Blue)",
                                                color = Color(0xFF80D8FF),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                            Text(
                                                text = "P2 & P4",
                                                color = Color.White,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (language == AppLanguage.BENGALI)
                                        "নিয়ম: অংশীদাররা বিপরীত দিকে বসেন, একে অপরের গুটি রক্ষা করেন এবং একসাথে ৮টি গুটি ঘরে এনে জয়ী হন!"
                                    else
                                        "Rules: Partners sit opposite, protect each other's pawns, and win together when all 8 tokens reach home!",
                                    color = Color(0xFFB0BEC5),
                                    fontSize = 9.5.sp,
                                    lineHeight = 12.sp
                                )
                            }
                        }
                    } else {
                        // Player Count Selector (2, 3, 4)
                        Text(
                            text = if (language == AppLanguage.BENGALI) "খেলোয়াড়ের সংখ্যা নির্বাচন করুন:" else "Select Number of Players:",
                            fontSize = 12.5.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(2, 3, 4).forEach { count ->
                                val isSelected = playerCount == count
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF1B2A47),
                                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF00E5FF) else Color(0xFF2A3C59)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clickable {
                                            soundManager.playButtonClick()
                                            playerCount = count
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "$count ${if (language == AppLanguage.BENGALI) "জন" else "Players"}",
                                            color = if (isSelected) Color(0xFF0D1B2A) else Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Launch Match Button
                    Button(
                        onClick = {
                            soundManager.playButtonClick()
                            showPlayerSelectDialog = false
                            val names = when (playerCount) {
                                2 -> listOf(p1Name, p2Name)
                                3 -> listOf(p1Name, p2Name, p3Name)
                                else -> listOf(p1Name, p2Name, p3Name, p4Name)
                            }
                            val botFlags = listOf(false, false, false, false)

                            when (selectedGameMode) {
                                GameMode.SNAKES_AND_LADDERS -> onStartSnakesAndLaddersGame(isSnakesAndLaddersOnline, playerCount, names, botFlags)
                                GameMode.QUICK_LUDO -> onStartQuickLudoGame(false, playerCount, names, botFlags)
                                else -> onStartGame(selectedGameMode, playerCount, names, botFlags, null, null, 4, null, 0)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("dialog_start_match_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.BENGALI) "খেলা শুরু করুন" else "START MATCH",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    // 9. Online City Tournaments Lounge Dialog
    if (showCityTournamentDialog) {
        CityTournamentLoungeDialog(
            language = language,
            onSelectTournament = { tournament ->
                showCityTournamentDialog = false
                soundManager.playButtonClick()
                selectedTournamentForMatchmaking = tournament
                showMatchmakingDialog = true
            },
            onDismiss = {
                showCityTournamentDialog = false
                activeBottomNavIndex = -1
            }
        )
    }

    // 9B. Royal Champions & Integrated Top 30 Leaderboard + Stats Dialog
    if (showRoyalChampionsDialog) {
        RoyalChampionsHallOfFameDialog(
            language = language,
            currentUserName = userName,
            onDismiss = {
                showRoyalChampionsDialog = false
                activeBottomNavIndex = -1
            }
        )
    }

    // 10. Online Matchmaking Radar System
    val tournament = selectedTournamentForMatchmaking
    if (showMatchmakingDialog && tournament != null) {
        OnlineMatchmakingDialog(
            tournament = tournament,
            userName = userName,
            language = language,
            onMatchFound = { opponentName ->
                showMatchmakingDialog = false
                selectedTournamentForMatchmaking = null
                soundManager.playButtonClick()
                Toast.makeText(
                    context,
                    if (language == AppLanguage.BENGALI)
                        "${tournament.nameBn} ম্যাচ শুরু হয়েছে! প্রতিপক্ষ: $opponentName"
                    else
                        "${tournament.nameEn} Match Started! Opponent: $opponentName",
                    Toast.LENGTH_SHORT
                ).show()
                onStartGame(
                    GameMode.ONLINE_FRIENDS,
                    2,
                    listOf(userName, opponentName),
                    listOf(false, true),
                    tournament.entryFee * 2,
                    if (language == AppLanguage.BENGALI) tournament.nameBn else tournament.nameEn,
                    tournament.targetTokensToWin,
                    tournament.id,
                    tournament.entryFee
                )
            },
            onCancel = {
                CoinManager.addCoins(tournament.entryFee, "Matchmaking Cancelled Refund")
                showMatchmakingDialog = false
                selectedTournamentForMatchmaking = null
                activeBottomNavIndex = -1
            }
        )
    }

    // 11. Earn Coins Reward Modal Dialog
    if (showEarnCoinsDialog) {
        EarnCoinsDialog(
            language = language,
            onDismiss = { showEarnCoinsDialog = false }
        )
    }

    // 12. Carrom Pool Lounge Dialog
    if (showCarromPoolDialog) {
        CarromPoolLoungeDialog(
            isOnline = isOnlineTab,
            language = language,
            userName = userName,
            onStartCarrom = { _, names, chosenMode ->
                try {
                    soundManager.playButtonClick()
                    val p2 = if (names.size > 1 && names[1].isNotBlank()) names[1] else (if (isOnlineTab) "Live Striker" else "Player 2 (Local)")
                    if (isOnlineTab) {
                        if (!CoinManager.spendCoins(500)) {
                            try {
                                Toast.makeText(
                                    context,
                                    if (language == AppLanguage.BENGALI) "অনলাইন ম্যাচের জন্য ৫০০ কয়েন প্রয়োজন!" else "Need 500 coins to enter online match!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } catch (_: Throwable) {}
                            return@CarromPoolLoungeDialog
                        }
                    }
                    showCarromPoolDialog = false
                    try {
                        Toast.makeText(
                            context,
                            if (language == AppLanguage.BENGALI) "ক্যারাম পুল ৩ডি বোর্ড লোড হচ্ছে..." else "Carrom Pool 3D Board Ready!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (_: Throwable) {}
                    // Launch into dedicated 3D Carrom Game Screen
                    onStartCarromGame(
                        false,
                        chosenMode.titleEn,
                        if (isOnlineTab) 1000 else 0,
                        userName,
                        p2,
                        isOnlineTab,
                        1,
                        if (isOnlineTab) 500 else 0,
