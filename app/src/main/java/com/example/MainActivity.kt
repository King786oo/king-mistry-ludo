package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.ads.AdMobBanner
import com.example.ads.AdMobManager
import com.example.engine.CarromGameMode
import com.example.engine.CoinManager
import com.example.ui.LudoViewModel
import com.example.ui.components.RulesDialog
import com.example.ui.screens.CarromGameScreen
import com.example.ui.screens.LudoGameScreen
import com.example.ui.screens.LudoMenuScreen
import com.example.ui.screens.LudoStatsScreen
import com.example.ui.screens.NasidulProfileDashboardScreen
import com.example.ui.theme.MyApplicationTheme

enum class ScreenState {
    HOME_PROFILE,
    MENU,
    GAME,
    CARROM_GAME,
    STATS
}

class MainActivity : ComponentActivity() {

    private val viewModel: LudoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Initialize Coin & Rewards System
        try {
            CoinManager.initialize(this)
        } catch (e: Throwable) {
            android.util.Log.e("MainActivity", "Error initializing CoinManager", e)
        }
        // Initialize AdMob Mobile Ads SDK with user's publisher and unit IDs
        try {
            AdMobManager.initialize(this)
        } catch (e: Throwable) {
            android.util.Log.e("MainActivity", "Error initializing AdMob", e)
        }

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LudoApp(viewModel = viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

@Composable
fun LudoApp(viewModel: LudoViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    // Support Intent-based launch destination (e.g. from notifications, deep links, or shortcuts)
    val startScreen = remember {
        val target = activity?.intent?.getStringExtra("target") ?: activity?.intent?.getStringExtra("screen")
        if (target.equals("menu", ignoreCase = true) || target.equals("dashboard", ignoreCase = true)) {
            ScreenState.MENU
        } else {
            ScreenState.HOME_PROFILE
        }
    }
    var currentScreen by rememberSaveable { mutableStateOf(startScreen) }

    androidx.compose.runtime.LaunchedEffect(activity?.intent) {
        val target = activity?.intent?.getStringExtra("target") ?: activity?.intent?.getStringExtra("screen")
        if ((target.equals("menu", ignoreCase = true) || target.equals("dashboard", ignoreCase = true)) &&
            currentScreen != ScreenState.CARROM_GAME &&
            currentScreen != ScreenState.GAME
        ) {
            currentScreen = ScreenState.MENU
        }
    }
    var showMenuRulesDialog by remember { mutableStateOf(false) }

    // Carrom Game Parameters (saved cleanly across configuration changes + keyed per launch)
    var carromMatchLaunchId by rememberSaveable { mutableIntStateOf(0) }
    var carromTournamentName by rememberSaveable { mutableStateOf("CARROM POOL 1v1") }
    var carromPrizePool by rememberSaveable { mutableIntStateOf(1000) }
    var carromEntryFeePerPlayer by rememberSaveable { mutableIntStateOf(0) }
    var carromLocalPlayerId by rememberSaveable { mutableIntStateOf(1) }
    var carromPlayer1Name by rememberSaveable { mutableStateOf("Player 1") }
    var carromPlayer2Name by rememberSaveable { mutableStateOf("Smart Striker AI") }
    var carromIsOpponentBot by rememberSaveable { mutableStateOf(true) }
    var carromIsTournament by rememberSaveable { mutableStateOf(false) }
    var carromModeId by rememberSaveable { mutableStateOf(CarromGameMode.STANDARD.id) }
    val carromMode = CarromGameMode.fromId(carromModeId)

    val gameState by viewModel.gameState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (currentScreen) {
                ScreenState.HOME_PROFILE -> {
                    NasidulProfileDashboardScreen(
                        onGetStarted = {
                            currentScreen = ScreenState.MENU
                        },
                        onOpenSettings = {
                            showMenuRulesDialog = true
                        }
                    )
                }
                ScreenState.MENU -> {
                    BackHandler {
                        currentScreen = ScreenState.HOME_PROFILE
                    }
                    LudoMenuScreen(
                        isBengali = gameState.isBengali,
                        language = gameState.language,
                        onStartGame = { mode, playerCount, names, botFlags, tournamentNetPrize, tournamentName, targetTokensToWin, tournamentTierId, entryFeePerPlayer ->
                            viewModel.startNewGame(mode, playerCount, names, botFlags, tournamentNetPrize, tournamentName, targetTokensToWin, tournamentTierId, entryFeePerPlayer)
                            currentScreen = ScreenState.GAME
                        },
                        onStartOnlineGame = { roomCode, localPlayerId, playerCount, playerNames, isDoubleTeam, coinWager ->
                            viewModel.startOnlineFriendsGame(roomCode, localPlayerId, playerCount, playerNames, isDoubleTeam, coinWager)
                            currentScreen = ScreenState.GAME
                        },
                        onStartSnakesAndLaddersGame = { isOnline, playerCount, names, botFlags ->
                            viewModel.startSnakesAndLaddersGame(isOnline, playerCount, names, botFlags)
                            currentScreen = ScreenState.GAME
                        },
                        onStartQuickLudoGame = { isOnline, playerCount, names, botFlags ->
                            viewModel.startQuickLudoGame(isOnline, playerCount, names, botFlags)
                            currentScreen = ScreenState.GAME
                        },
                        onStartCarromGame = { isTourney, tourneyName, pool, p1, p2, isBot, localPId, entryFee, selectedMode ->
                            try {
                                carromIsTournament = isTourney
                                carromTournamentName = tourneyName.ifBlank { selectedMode.titleEn }
                                carromPrizePool = pool.coerceAtLeast(0)
                                carromEntryFeePerPlayer = entryFee.coerceAtLeast(0)
                                carromLocalPlayerId = localPId.coerceIn(1, 2)
                                carromPlayer1Name = p1.ifBlank { "Player 1" }
                                carromPlayer2Name = p2.ifBlank { if (isBot) "Nasidul Mistry" else "Player 2" }
                                carromIsOpponentBot = isBot
                                carromModeId = selectedMode.id
                                carromMatchLaunchId++
                                currentScreen = ScreenState.CARROM_GAME
                            } catch (t: Throwable) {
                                android.util.Log.e("MainActivity", "Error starting Carrom match, applying safe fallback defaults", t)
                                carromIsTournament = false
                                carromTournamentName = "CARROM POOL 1v1"
                                carromPrizePool = 0
                                carromEntryFeePerPlayer = 0
                                carromLocalPlayerId = 1
                                carromPlayer1Name = "Player 1"
                                carromPlayer2Name = "Nasidul Mistry"
                                carromIsOpponentBot = true
                                carromModeId = CarromGameMode.STANDARD.id
                                carromMatchLaunchId++
                                currentScreen = ScreenState.CARROM_GAME
                            }
                        },
                        onOpenStats = {
                            if (activity != null) {
                                AdMobManager.showInterstitialAd(activity) {
                                    currentScreen = ScreenState.STATS
                                }
                            } else {
                                currentScreen = ScreenState.STATS
                            }
                        },
                        onOpenRules = {
                            showMenuRulesDialog = true
                        },
                        onToggleLanguage = {
                            viewModel.toggleLanguage()
                        },
                        onSelectLanguage = { lang ->
                            viewModel.setLanguage(lang)
                        },
                        onSelectToken = { token ->
                            viewModel.setEquippedToken(token)
                        },
                        onSelectDiceSkin = { diceSkin ->
                            viewModel.setEquippedDiceSkin(diceSkin)
                        },
                        onBackToProfile = {
                            android.util.Log.d("LudoNavDiag", "onBackToProfile invoked! Navigating to ScreenState.HOME_PROFILE")
                            currentScreen = ScreenState.HOME_PROFILE
                        }
                    )
                }
                ScreenState.GAME -> {
                    LudoGameScreen(
                        viewModel = viewModel,
                        onBackToMenu = {
                            if (activity != null) {
                                AdMobManager.showInterstitialAd(activity) {
                                    currentScreen = ScreenState.MENU
                                }
                            } else {
                                currentScreen = ScreenState.MENU
                            }
                        }
                    )
                }
                ScreenState.CARROM_GAME -> {
                    key(carromMatchLaunchId) {
                        CarromGameScreen(
                            tournamentName = carromTournamentName,
                            prizePool = carromPrizePool,
                            entryFeePerPlayer = carromEntryFeePerPlayer,
                            localPlayerId = carromLocalPlayerId,
                            player1Name = carromPlayer1Name,
                            player2Name = carromPlayer2Name,
                            isOpponentBot = carromIsOpponentBot,
                            isTournament = carromIsTournament,
                            carromMode = carromMode,
                            language = gameState.language,
                            onBackToMenu = {
                                currentScreen = ScreenState.MENU
                            }
                        )
                    }
                }
                ScreenState.STATS -> {
                    val stats by viewModel.stats.collectAsStateWithLifecycle()
                    BackHandler {
                        currentScreen = ScreenState.MENU
                    }
                    LudoStatsScreen(
                        stats = stats,
                        isBengali = gameState.isBengali,
                        onBack = {
                            currentScreen = ScreenState.MENU
                        }
                    )
                }
                }

                if (showMenuRulesDialog) {
                RulesDialog(
                    isBengali = gameState.isBengali,
                    onDismiss = { showMenuRulesDialog = false }
                )
            }
        }

        // AdMob Banner Ad View at the bottom during Gameplay and Stats screens
        if (currentScreen == ScreenState.GAME || currentScreen == ScreenState.STATS || currentScreen == ScreenState.CARROM_GAME) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                AdMobBanner(
                    modifier = Modifier.fillMaxWidth(),
                    adUnitId = AdMobManager.BANNER_AD_UNIT_ID
                )
            }
        }
    }
}
