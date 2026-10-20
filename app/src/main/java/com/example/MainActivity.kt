package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.LudoViewModel
import com.example.ui.components.RulesDialog
import com.example.ui.screens.LudoGameScreen
import com.example.ui.screens.LudoMenuScreen
import com.example.ui.screens.LudoStatsScreen
import com.example.ui.screens.NasidulProfileDashboardScreen
import com.example.ui.theme.MyApplicationTheme

enum class ScreenState {
    HOME_PROFILE,
    MENU,
    GAME,
    STATS
}

class MainActivity : ComponentActivity() {

    private val viewModel: LudoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LudoApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun LudoApp(viewModel: LudoViewModel) {
    var currentScreen by remember { mutableStateOf(ScreenState.HOME_PROFILE) }
    var showMenuRulesDialog by remember { mutableStateOf(false) }

    val gameState by viewModel.gameState.collectAsState()
    val stats by viewModel.stats.collectAsState()

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
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
                LudoMenuScreen(
                    isBengali = gameState.isBengali,
                    onStartGame = { mode, playerCount, names, botFlags ->
                        viewModel.startNewGame(mode, playerCount, names, botFlags)
                        currentScreen = ScreenState.GAME
                    },
                    onOpenStats = {
                        currentScreen = ScreenState.STATS
                    },
                    onOpenRules = {
                        showMenuRulesDialog = true
                    },
                    onToggleLanguage = {
                        viewModel.toggleLanguage()
                    },
                    onBackToProfile = {
                        currentScreen = ScreenState.HOME_PROFILE
                    }
                )
            }
            ScreenState.GAME -> {
                LudoGameScreen(
                    viewModel = viewModel,
                    onBackToMenu = {
                        currentScreen = ScreenState.MENU
                    }
                )
            }
            ScreenState.STATS -> {
                LudoStatsScreen(
                    stats = stats,
                    isBengali = gameState.isBengali,
                    onBack = {
                        currentScreen = ScreenState.MENU
                    }
                )
            }
        }
    }

    if (showMenuRulesDialog) {
        RulesDialog(
            isBengali = gameState.isBengali,
            onDismiss = { showMenuRulesDialog = false }
        )
    }
}
