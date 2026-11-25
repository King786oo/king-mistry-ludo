package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.zIndex
import android.graphics.BitmapFactory
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.graphics.ImageBitmap
import com.example.model.CarromPowerRing
import com.example.model.CarromPuckColorTone
import com.example.model.CarromPuckSet
import com.example.model.CarromSidePieceVisual
import com.example.model.CarromStriker
import com.example.model.drawPowerRingEffect
import kotlin.math.roundToInt
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.engine.CarromEngine
import com.example.engine.CarromGameMode
import com.example.engine.CarromNetworkHealth
import com.example.engine.CarromPhysicsEngine
import com.example.engine.CarromWeakNetworkCoordinator
import com.example.engine.CoinManager
import com.example.engine.PlayerLevelManager
import com.example.engine.QueenStatus
import com.example.engine.SoundManager
import com.example.model.AppLanguage
import com.example.multiplayer.CarromPuckSyncData
import com.example.multiplayer.CarromStateSnapshot
import com.example.multiplayer.CarromSyncProtocol
import com.example.multiplayer.OnlineMultiplayerManager
import com.example.ui.components.CoinMatchResultArena
import com.example.ui.components.NasidulAvatar
import androidx.compose.runtime.DisposableEffect
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

private fun decodeSampledBitmapSafe(context: Context, resId: Int, maxDim: Int = 256): androidx.compose.ui.graphics.ImageBitmap? {
    return try {
        val boundsOpts = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            inScaled = false
        }
        BitmapFactory.decodeResource(context.resources, resId, boundsOpts)
        var sampleSize = 1
        while (boundsOpts.outWidth / sampleSize > maxDim || boundsOpts.outHeight / sampleSize > maxDim) {
            sampleSize *= 2
        }
        val decodeOpts = BitmapFactory.Options().apply {
            inSampleSize = sampleSize.coerceAtLeast(1)
            inScaled = false
        }
        BitmapFactory.decodeResource(context.resources, resId, decodeOpts)?.asImageBitmap()
    } catch (_: Throwable) {
        null
    }
}

/**
 * Carrom Piece Model
 */
enum class CarromPieceType {
    STRIKER,
    WHITE_PUCK,
    BLACK_PUCK,
    RED_QUEEN
}

data class CarromPiece(
    val id: Int,
    val type: CarromPieceType,
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    val radius: Float,
    var isPotted: Boolean = false,
    var isFalling: Boolean = false,
    var fallProgress: Float = 0f,
    var fallPocketX: Float = 0f,
    var fallPocketY: Float = 0f,
    var pottedScale: Float = 1f,
    val pointValue: Int
)

/**
 * HIGH-END 3D CARROM GAME SCREEN (COPYRIGHT-SAFE UNIQUE ORIGINAL DESIGN)
 *
 * 1. Top Scorebar & Profile Header:
 *    - Rich dark purple/navy-blue gradient
 *    - 3D glossy gold coin stack displaying Total Prize Pool in center
 *    - Left & Right player avatars enclosed in metallic golden frames with names & scores
 * 2. Unique Royal Carrom Board:
 *    - HD smooth pine-wood varnished finish with subtle grain & gloss highlight
 *    - Outer frame: Royal Golden and Dark Mahogany wooden border
 *    - 4 deep 3D corner pockets with metallic golden rims
 *    - Original royal golden-line center mandala pattern & baseline circular anchors
 * 3. 3D Pucks, Striker & Aiming Physics:
 *    - 9 Pearl White, 9 Obsidian Black, 1 Glowing Crimson Red Queen in center star
 *    - 3D Royal Striker with bottom positioning bar
 *    - Smooth drag-and-release aiming with fine dotted trajectory line & acoustic sounds
 * 4. In-Game Action Buttons:
 *    - Bottom Left: Circular 3D Emoji / Quick Chat icon
 *    - Bottom Right: Circular 3D Settings / Menu icon
 */
@Composable
fun CarromGameScreen(
    tournamentName: String = "CARROM POOL 1v1",
    prizePool: Int = 1000,
    entryFeePerPlayer: Int = 0,
    localPlayerId: Int = 1,
    player1Name: String = "Player 1",
    player2Name: String = "Smart Striker AI",
    isOpponentBot: Boolean = true,
    isTournament: Boolean = false,
    carromMode: CarromGameMode = CarromGameMode.STANDARD,
    language: AppLanguage = AppLanguage.ENGLISH,
    onBackToMenu: () -> Unit,
    initialPucks: List<CarromPiece>? = null,
    initialP1Score: Int = 0,
    initialP2Score: Int = 0,
    initialQueenStatus: QueenStatus = QueenStatus.ON_BOARD
) {
    val context = LocalContext.current
    try {
        CoinManager.initialize(context)
    } catch (_: Throwable) {}
    try {
        PlayerLevelManager.init(context)
    } catch (_: Throwable) {}
    try {
        CarromPowerRing.initialize(context)
    } catch (_: Throwable) {}
    val equippedPowerRing by CarromPowerRing.equippedRingFlow.collectAsStateWithLifecycle()
    val currentLevel by PlayerLevelManager.currentLevel.collectAsStateWithLifecycle()
    val soundManager = remember { SoundManager(context) }
    DisposableEffect(soundManager) {
        onDispose { soundManager.release() }
    }
    val coroutineScope = rememberCoroutineScope()
    val prefs = remember {
        try {
            context.getSharedPreferences("king_mistry_ludo_prefs", Context.MODE_PRIVATE)
        } catch (_: Throwable) {
            null
        }
    }
    val boardImageBitmap = remember {
        decodeSampledBitmapSafe(context, R.drawable.art_carrom_board_3d, maxDim = 1024)
    }
    val equippedPuckSet = remember {
        try {
            CarromPuckSet.getEquippedSet(context)
        } catch (_: Throwable) {
            CarromPuckSet.TOKEN_1
        }
    }
    val customPuckImageBitmap = remember(equippedPuckSet) {
        decodeSampledBitmapSafe(context, equippedPuckSet.singleDrawableRes, maxDim = 256)
    }
    val equippedStriker = remember {
        try {
            CarromStriker.getEquippedStriker(context)
        } catch (_: Throwable) {
            CarromStriker.STRIKER_1
        }
    }
    val customStrikerImageBitmap = remember(equippedStriker) {
        decodeSampledBitmapSafe(context, equippedStriker.drawableRes, maxDim = 256)
    }
    val equippedPowerRingImageBitmap = remember(equippedPowerRing) {
        decodeSampledBitmapSafe(context, equippedPowerRing.drawableResId, maxDim = 256)
    }

    // Determine effective display name for Player 2 (Tournament bot override to Nasidul Mistry)
    val effectivePlayer2Name = remember(player2Name, isOpponentBot, isTournament) {
        if (isTournament && isOpponentBot) {
            "Nasidul Mistry"
        } else if (player2Name.isBlank() || player2Name == "Live Striker" || player2Name == "Bot" || player2Name == "Computer" || player2Name == "Smart Striker AI") {
            if (isOpponentBot) "Nasidul Mistry" else "Player 2"
        } else {
            player2Name
        }
    }

    // 50% Win/Loss tracking for tournament AI opponent ("Nasidul Mistry")
    // Consecutive matches: Match 0 -> AI Loses (Human Wins), Match 1 -> AI Wins
    val carromAiMatchCount = remember {
        try {
            prefs?.getInt("carrom_tournament_nasidul_match_count", 0) ?: 0
        } catch (_: Throwable) {
            0
        }
    }
    val shouldAiWinThisMatch = remember(carromAiMatchCount, isTournament, isOpponentBot) {
        isTournament && isOpponentBot && (carromAiMatchCount % 2 == 1)
    }

    // Handle Android system back button
    var showExitDialog by remember { mutableStateOf(false) }
    BackHandler {
        showExitDialog = true
    }

    // Board Dimensions in Virtual Coordinates (1000 x 1000)
    val boardDim = CarromPhysicsEngine.BOARD_DIM
    val frameBorder = CarromPhysicsEngine.INNER_MIN
    val innerMin = CarromPhysicsEngine.INNER_MIN
    val innerMax = CarromPhysicsEngine.INNER_MAX
    val pocketRadius = CarromPhysicsEngine.POCKET_RADIUS

    // 4 Corner Pockets (Centered accurately inside the 4 visual board pockets)
    val pockets = remember {
        CarromPhysicsEngine.pockets.map { Offset(it.x, it.y) }
    }

    // Baselines (Virtual Coordinates)
    val baselineYBottom = 803f
    val baselineYTop = 197f
    val baselineMinX = 225f
    val baselineMaxX = 775f

    val activeOnlineRoomCode = OnlineMultiplayerManager.activeRoomCode
    val isOnlinePeerMatch = !isOpponentBot && !activeOnlineRoomCode.isNullOrBlank()

    // Opponent Puck Set Sync & Same-Color Conflict Auto-Swap Resolution
    var opponentPuckSet by remember(activeOnlineRoomCode, isOnlinePeerMatch) {
        mutableStateOf<CarromPuckSet?>(null)
    }
    var hasSentGuestPuckPref by remember(activeOnlineRoomCode) {
        mutableStateOf(false)
    }
    val resolvedBoardAppearance by remember(localPlayerId, equippedPuckSet, opponentPuckSet, isOnlinePeerMatch) {
        derivedStateOf {
            CarromPuckSet.resolveBoardAppearance(
                localPlayerId = localPlayerId,
                mySelectedSet = equippedPuckSet,
                opponentSelectedSet = opponentPuckSet,
                isOnlinePeerMatch = isOnlinePeerMatch
            )
        }
    }
    val p1CustomSet = (resolvedBoardAppearance.p1Visual as? CarromSidePieceVisual.CustomSet)?.puckSet
    val p2CustomSet = (resolvedBoardAppearance.p2Visual as? CarromSidePieceVisual.CustomSet)?.puckSet
    val p1PuckImageBitmap = remember(p1CustomSet, equippedPuckSet, customPuckImageBitmap) {
        when {
            p1CustomSet == null -> null
            p1CustomSet == equippedPuckSet -> customPuckImageBitmap
            else -> decodeSampledBitmapSafe(context, p1CustomSet.singleDrawableRes, maxDim = 256)
        }
    }
    val p2PuckImageBitmap = remember(p2CustomSet, equippedPuckSet, customPuckImageBitmap) {
        when {
            p2CustomSet == null -> null
            p2CustomSet == equippedPuckSet -> customPuckImageBitmap
            else -> decodeSampledBitmapSafe(context, p2CustomSet.singleDrawableRes, maxDim = 256)
        }
    }

    // Match State
    var p1Score by remember { mutableIntStateOf(initialP1Score) }
    var p2Score by remember { mutableIntStateOf(initialP2Score) }
    var queenStatus by remember { mutableStateOf(initialQueenStatus) }
    var matchSessionCounter by remember { mutableIntStateOf(1) }
    var matchSessionId by remember {
        mutableStateOf("carrom_${carromMode.id}_${System.currentTimeMillis()}_$matchSessionCounter")
    }
    var currentTurnPlayer by remember { mutableIntStateOf(1) } // 1: Player 1 (Host/Bottom), 2: Player 2 (Guest/Top or AI)
    var currentMatchShotSeq by remember { mutableLongStateOf(0L) }
    var lastAppliedSyncSeq by remember { mutableLongStateOf(0L) }
    var activeShotOwnerId by remember { mutableIntStateOf(1) }
    var awaitingAuthoritativeSync by remember { mutableStateOf(false) }
    var pendingRemoteSnapshot by remember { mutableStateOf<CarromStateSnapshot?>(null) }
    var lastStrikerSyncTimeMs by remember { mutableLongStateOf(0L) }
    var incomingRematchRequester by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember {
        mutableStateOf(
            if (isOnlinePeerMatch && localPlayerId != 1) {
                if (language == AppLanguage.BENGALI) "$player1Name-এর চাল • লাইভ রুম সিঙ্ক"
                else "$player1Name'S TURN • Live Room Sync"
            } else if (language == AppLanguage.BENGALI) {
                "আপনার চাল: স্ট্রাইকার সেট করে তাক করুন!"
            } else {
                when (carromMode) {
                    CarromGameMode.FOGGY -> "FOGGY CARROM: First to 160 PTS Wins!"
                    CarromGameMode.NO_QUEEN -> "NO QUEEN: Clear All 9 White Coins First!"
                    CarromGameMode.STANDARD -> "STANDARD CARROM: Pot & Cover Queen + Clear 9!"
                }
            }
        )
    }
    var isSimulatingPhysics by remember { mutableStateOf(false) }
    var matchOver by remember { mutableStateOf(false) }
    var winnerPlayerId by remember { mutableIntStateOf(0) }

    // Aiming & Direct-Touch Slingshot State (hoisted so match finalization always clears active drag/aim)
    var isAiming by remember { mutableStateOf(false) }
    var isPositioning by remember { mutableStateOf(false) }
    var aimStartOffset by remember { mutableStateOf(Offset.Zero) }
    var aimCurrentOffset by remember { mutableStateOf(Offset.Zero) }
    var aimPower by remember { mutableFloatStateOf(0f) }
    var aimPullDist by remember { mutableFloatStateOf(0f) }
    var aimAngle by remember { mutableFloatStateOf(-PI.toFloat() / 2f) }

    // Weak-Network & Turn/Score Resilience Coordinator
    val networkCoordinator = remember { CarromWeakNetworkCoordinator(context, coroutineScope) }
    val networkHealth by networkCoordinator.networkHealth.collectAsStateWithLifecycle()
    val pendingSyncCount by networkCoordinator.pendingSyncCount.collectAsStateWithLifecycle()
    DisposableEffect(Unit) {
        val stopMonitoring = try {
            networkCoordinator.startMonitoring()
        } catch (_: Throwable) {
            { }
        }
        onDispose {
            try {
                stopMonitoring()
            } catch (_: Throwable) {}
        }
    }

    // Striker Foul & Penalty Banner State
    var showFoulBanner by remember { mutableStateOf(false) }
    var foulBannerTitle by remember { mutableStateOf("⚠️ STRIKER FOUL / PENALTY") }
    var foulBannerSubtitle by remember { mutableStateOf("") }

    // Auto-dismiss foul banner after 3.2s
    LaunchedEffect(showFoulBanner) {
        if (showFoulBanner) {
            delay(3200)
            showFoulBanner = false
        }
    }

    // 30-Second Shot Turn Timer State (Strict Turn Limit across Online & Offline Modes)
    var turnTimeRemaining by remember { mutableIntStateOf(30) }
    var turnTimerResetTrigger by remember { mutableIntStateOf(0) }

    // Quick Chat & Settings Dialog State
    var showChatDialog by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }
    var p1ChatBubble by remember { mutableStateOf<String?>(null) }
    var p2ChatBubble by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(p1ChatBubble) {
        if (p1ChatBubble != null) {
            delay(2500)
            p1ChatBubble = null
        }
    }

    LaunchedEffect(p2ChatBubble) {
        if (p2ChatBubble != null) {
            delay(2500)
            p2ChatBubble = null
        }
    }

    // Striker Baseline Position (X coordinate along baseline)
    var strikerBaselineX by remember { mutableFloatStateOf(500f) }
    var physicsTick by remember { mutableLongStateOf(0L) }
    var hudPocketVersion by remember { mutableIntStateOf(0) }
    val strikerRadius = 46f // Standard Carrom Pro championship ratio (1.33x puck diameter, fits baseline precisely)
    val puckRadius = 34.5f // Scaled relative to board, pockets and baseline circles

    // Striker Initial Object
    val striker = remember {
        CarromPiece(
            id = 999,
            type = CarromPieceType.STRIKER,
            x = strikerBaselineX,
            y = baselineYBottom,
            radius = strikerRadius,
            pointValue = 0
        )
    }

    val pucks = remember {
        mutableStateListOf<CarromPiece>().apply {
            val built = try {
                initialPucks ?: CarromEngine.buildInitialPucks(mode = carromMode, puckRadius = puckRadius)
            } catch (_: Throwable) {
                CarromEngine.buildInitialPucks(mode = CarromGameMode.NO_QUEEN, puckRadius = puckRadius)
            }
            addAll(built)
        }
    }

    fun finalizeCarromMatch(winnerId: Int, customMessage: String? = null) {
        if (winnerId == 0) return
        try {
            val wasAlreadyOver = matchOver
            matchOver = true
            winnerPlayerId = winnerId
            isSimulatingPhysics = false
            isAiming = false
            isPositioning = false
            aimPower = 0f
            aimPullDist = 0f
            if (!wasAlreadyOver) {
                val isLocalWinner = CarromEngine.isLocalPlayerWinner(winnerId, localPlayerId)
                val winningPlayerName = if (winnerId == 1) player1Name else effectivePlayer2Name
                if (isTournament && isOpponentBot) {
                    try {
                        prefs?.edit()?.putInt("carrom_tournament_nasidul_match_count", carromAiMatchCount + 1)?.apply()
                    } catch (_: Throwable) {}
                }
                if (isLocalWinner) {
                    if (prizePool > 0) {
                        networkCoordinator.claimMatchRewardIdempotently(
                            sessionId = matchSessionId,
                            prizePool = prizePool,
                            reason = "Carrom ${carromMode.titleEn} Victory"
                        )
                    }
                    soundManager.playVictoryFanfare()
                    statusMessage = customMessage ?: "🏆 YOU ARE WINNER! $winningPlayerName won ${carromMode.titleEn}!"
                    if (isTournament) {
                        try {
                            PlayerLevelManager.recordTournamentWin(context)
                        } catch (_: Throwable) {}
                    }
                } else {
                    soundManager.playDefeatSound()
                    statusMessage = customMessage ?: "🏆 OPPONENT WINNER! $winningPlayerName won ${carromMode.titleEn}!"
                }
            }
        } catch (t: Throwable) {
            android.util.Log.e("CarromGameScreen", "Error finalizing match", t)
            matchOver = true
            winnerPlayerId = winnerId
            isSimulatingPhysics = false
        }
    }

    fun buildPuckSyncSnapshot(): List<CarromPuckSyncData> {
        return pucks.map { p ->
            CarromPuckSyncData(
                id = p.id,
                isPotted = p.isPotted || p.isFalling,
                x = p.x,
                y = p.y
            )
        }
    }

    fun emitThrottledStrikerPosition(x: Float, force: Boolean = false) {
        if (!isOnlinePeerMatch || currentTurnPlayer != localPlayerId) return
        val roomCode = activeOnlineRoomCode ?: return
        val now = System.currentTimeMillis()
        if (force || now - lastStrikerSyncTimeMs >= 180L) {
            lastStrikerSyncTimeMs = now
            OnlineMultiplayerManager.broadcastCarromStrikerPos(
                roomCode = roomCode,
                turnPlayer = currentTurnPlayer,
                strikerX = x
            )
        }
    }

    fun isCoinObstructing(x: Float, bY: Float): Boolean {
        if (!x.isFinite() || !bY.isFinite()) return false
        val minSafeDistance = strikerRadius + puckRadius + 1.2f
        return pucks.any { p ->
            !p.isPotted && !p.isFalling && p.x.isFinite() && p.y.isFinite() &&
            hypot(x - p.x, bY - p.y) < minSafeDistance
        }
    }

    fun findNearestClearBaselineX(targetX: Float, bY: Float): Float {
        val safeTarget = if (targetX.isFinite()) targetX else 500f
        val safeBY = if (bY.isFinite()) bY else baselineYBottom
        val clamped = safeTarget.coerceIn(baselineMinX, baselineMaxX)
        if (!isCoinObstructing(clamped, safeBY)) return clamped

        // If obstructed by a coin on the baseline, auto-shift outwards to find nearest legal open space
        var offset = 1.0f
        val maxOffset = baselineMaxX - baselineMinX
        while (offset <= maxOffset) {
            val left = clamped - offset
            val right = clamped + offset

            val leftClear = left >= baselineMinX && !isCoinObstructing(left, safeBY)
            val rightClear = right <= baselineMaxX && !isCoinObstructing(right, safeBY)

            if (leftClear && rightClear) {
                // If open space exists on both sides equidistant from target, prefer towards center of board
                return if (clamped <= (baselineMinX + baselineMaxX) / 2f) left else right
            } else if (leftClear) {
                return left
            } else if (rightClear) {
                return right
            }

            offset += 1.0f
        }
        return clamped
    }

    fun safeResetStrikerToBaseline(playerTurn: Int, preferredX: Float = strikerBaselineX) {
        val targetBaselineY = if (playerTurn == 1) baselineYBottom else baselineYTop
        val initialX = if (playerTurn == 1) preferredX else (if (isOpponentBot) 500f else preferredX)
        val clearX = findNearestClearBaselineX(initialX, targetBaselineY)
        striker.x = clearX
        striker.y = targetBaselineY
        strikerBaselineX = clearX
        striker.vx = 0f
        striker.vy = 0f
        striker.isPotted = false
        striker.isFalling = false
        striker.fallProgress = 0f
        striker.pottedScale = 1f
        physicsTick++
    }

    fun applyRemoteCarromSnapshot(snapshot: CarromStateSnapshot) {
        if (!CarromSyncProtocol.shouldApplySyncSnapshot(snapshot, lastAppliedSyncSeq, awaitingAuthoritativeSync)) {
            return
        }
        lastAppliedSyncSeq = maxOf(lastAppliedSyncSeq, snapshot.shotSeq)
        currentMatchShotSeq = maxOf(currentMatchShotSeq, snapshot.shotSeq)
        awaitingAuthoritativeSync = false
        pendingRemoteSnapshot = null
        isSimulatingPhysics = false
        isAiming = false
        isPositioning = false
        aimPower = 0f
        aimPullDist = 0f

        p1Score = snapshot.p1Score
        p2Score = snapshot.p2Score
        currentTurnPlayer = snapshot.turnPlayer.coerceIn(1, 2)
        queenStatus = QueenStatus.entries.firstOrNull { it.name == snapshot.queenStatusName } ?: queenStatus

        if (snapshot.pucks.isNotEmpty()) {
            val byId = snapshot.pucks.associateBy { it.id }
            pucks.forEach { p ->
                val synced = byId[p.id]
                if (synced != null) {
                    p.vx = 0f
                    p.vy = 0f
                    p.isFalling = false
                    p.fallProgress = 0f
                    if (synced.isPotted) {
                        p.isPotted = true
                        p.pottedScale = 0f
                    } else {
                        p.isPotted = false
                        p.pottedScale = 1f
                        p.x = synced.x.coerceIn(innerMin + p.radius, innerMax - p.radius)
                        p.y = synced.y.coerceIn(innerMin + p.radius, innerMax - p.radius)
                    }
                } else if (p.id in snapshot.pottedIds) {
                    p.isPotted = true
                    p.isFalling = false
                    p.pottedScale = 0f
                    p.vx = 0f
                    p.vy = 0f
                }
            }
        } else {
            pucks.forEach { p ->
                if (p.id in snapshot.pottedIds) {
                    p.isPotted = true
                    p.isFalling = false
                    p.pottedScale = 0f
                    p.vx = 0f
                    p.vy = 0f
                } else if (p.isPotted || p.isFalling) {
                    CarromEngine.placeReturnedPieceSafely(p, pucks)
                }
            }
        }

        hudPocketVersion++
        if (snapshot.matchOver && snapshot.winnerPlayerId != 0) {
            finalizeCarromMatch(snapshot.winnerPlayerId)
        } else {
            val turnOwnerName = if (currentTurnPlayer == 1) player1Name else effectivePlayer2Name
            statusMessage = if (currentTurnPlayer == localPlayerId) {
                if (language == AppLanguage.BENGALI) "আপনার চাল: স্ট্রাইকার সেট করে তাক করুন!" else "YOUR TURN: Position Striker & Shoot!"
            } else {
                if (language == AppLanguage.BENGALI) "$turnOwnerName-এর চাল • লাইভ সিঙ্ক" else "$turnOwnerName'S TURN • Live Room Sync"
            }
            safeResetStrikerToBaseline(currentTurnPlayer)
            turnTimerResetTrigger++
        }
        physicsTick++
    }

    var showCarromRematchWaiting by remember { mutableStateOf(false) }
    var showCarromCannotPlay by remember { mutableStateOf(false) }
    var carromCannotPlayMessage by remember { mutableStateOf("") }

    val restartCarromMatch = {
        try {
            p1Score = 0
            p2Score = 0
            queenStatus = QueenStatus.ON_BOARD
            matchSessionCounter++
            matchSessionId = "carrom_${carromMode.id}_${System.currentTimeMillis()}_$matchSessionCounter"
            currentTurnPlayer = 1
            currentMatchShotSeq = 0L
            lastAppliedSyncSeq = 0L
            activeShotOwnerId = 1
            awaitingAuthoritativeSync = false
            pendingRemoteSnapshot = null
            incomingRematchRequester = null
            showFoulBanner = false
            showCarromRematchWaiting = false
            showCarromCannotPlay = false
            pucks.clear()
            pucks.addAll(CarromEngine.buildInitialPucks(mode = carromMode, puckRadius = puckRadius))
            isAiming = false
            isPositioning = false
            aimPower = 0f
            aimPullDist = 0f
            turnTimeRemaining = 30
            winnerPlayerId = 0
            matchOver = false
            hudPocketVersion++
            safeResetStrikerToBaseline(1, (baselineMinX + baselineMaxX) / 2f)
            statusMessage = if (isOnlinePeerMatch && localPlayerId != 1) {
                if (language == AppLanguage.BENGALI) "$player1Name-এর চাল • লাইভ সিঙ্ক" else "$player1Name'S TURN • Live Room Sync"
            } else {
                if (language == AppLanguage.BENGALI) "আপনার চাল: স্ট্রাইকার সেট করে তাক করুন!" else "YOUR TURN: Position Striker & Aim!"
            }
        } catch (t: Throwable) {
            android.util.Log.e("CarromGameScreen", "Error restarting Carrom match", t)
        }
    }

    // Online Room State Recovery & Peer Sync (Non-blocking on weak/slow networks)
    DisposableEffect(isOpponentBot, activeOnlineRoomCode) {
        try {
            val roomCode = activeOnlineRoomCode
            if (!isOpponentBot && !roomCode.isNullOrBlank()) {
                OnlineMultiplayerManager.listenToCarromSync(
                    roomCode = roomCode,
                    onStateReceived = { _, _, _, _, _, _, _, _ ->
                        // Handled with full puck coordinates + shotSeq by onFullStateReceived
                    },
                    onSyncRequested = {
                        try {
                            val shouldRespond = !awaitingAuthoritativeSync &&
                                (!isSimulatingPhysics || activeShotOwnerId == localPlayerId) &&
                                (currentTurnPlayer == localPlayerId || currentMatchShotSeq > 0L || localPlayerId == 1)
                            if (shouldRespond) {
                                networkCoordinator.recordShotCheckpointAndSync(
                                    sessionId = matchSessionId,
                                    mode = carromMode,
                                    turnPlayer = currentTurnPlayer,
                                    p1Score = p1Score,
                                    p2Score = p2Score,
                                    queenStatus = queenStatus,
                                    pottedIds = pucks.filter { it.isPotted }.map { it.id }.toSet(),
                                    matchOver = matchOver,
                                    winnerPlayerId = winnerPlayerId,
                                    isOnlineMatch = true,
                                    shotSeq = currentMatchShotSeq,
                                    puckStates = buildPuckSyncSnapshot()
                                )
                            }
                        } catch (_: Throwable) {}
                    },
                    onFullStateReceived = { snapshot ->
                        try {
                            snapshot.senderPuckSetId?.let { remoteSetId ->
                                opponentPuckSet = CarromPuckSet.fromId(remoteSetId)
                            }
                            if (localPlayerId == 2 && !hasSentGuestPuckPref) {
                                hasSentGuestPuckPref = true
                                OnlineMultiplayerManager.broadcastCarromPuckPref(roomCode, equippedPuckSet.id)
                            }
                            if (matchOver && !snapshot.matchOver) return@listenToCarromSync
                            if (isSimulatingPhysics && activeShotOwnerId != localPlayerId && !snapshot.matchOver) {
                                // Let observer's smooth roll animation finish before snapping to shooter's exact result
                                pendingRemoteSnapshot = snapshot
                            } else {
                                applyRemoteCarromSnapshot(snapshot)
                            }
                        } catch (t: Throwable) {
                            android.util.Log.e("CarromGameScreen", "Error applying remote Carrom sync", t)
                        }
                    },
                    onStrikerMoved = { remoteTurn, remoteX ->
                        if (!isSimulatingPhysics && !matchOver && remoteTurn != localPlayerId) {
                            striker.x = remoteX.coerceIn(baselineMinX, baselineMaxX)
                            striker.y = if (remoteTurn == 1) baselineYBottom else baselineYTop
                            strikerBaselineX = striker.x
                            physicsTick++
                        }
                    },
                    onShotFired = { shotSeq, remoteTurn, sx, sy, angle, power ->
                        try {
                            if (!CarromSyncProtocol.shouldAcceptRemoteShot(shotSeq, lastAppliedSyncSeq, currentMatchShotSeq, isSimulatingPhysics, matchOver)) {
                                return@listenToCarromSync
                            }
                            if (isSimulatingPhysics) {
                                CarromPhysicsEngine.settleFallingPieces(striker, pucks)
                            }
                            currentMatchShotSeq = maxOf(currentMatchShotSeq, shotSeq)
                            activeShotOwnerId = remoteTurn.coerceIn(1, 2)
                            currentTurnPlayer = activeShotOwnerId
                            pendingRemoteSnapshot = null
                            awaitingAuthoritativeSync = true
                            striker.isPotted = false
                            striker.isFalling = false
                            striker.fallProgress = 0f
                            striker.pottedScale = 1f
                            striker.x = sx.coerceIn(baselineMinX, baselineMaxX)
                            striker.y = sy.coerceIn(innerMin + strikerRadius, innerMax - strikerRadius)
                            strikerBaselineX = striker.x
                            val speed = CarromPhysicsEngine.computeImpulseSpeed(power)
                            striker.vx = cos(angle) * speed
                            striker.vy = sin(angle) * speed
                            val shooterName = if (activeShotOwnerId == 1) player1Name else effectivePlayer2Name
                            statusMessage = if (language == AppLanguage.BENGALI) "⚡ $shooterName শট নিয়েছে..." else "⚡ $shooterName struck!"
                            soundManager.playCarromStrikerShot()
                            isSimulatingPhysics = true
                            physicsTick++
                        } catch (t: Throwable) {
                            android.util.Log.e("CarromGameScreen", "Error launching remote shot", t)
                        }
                    },
                    localPuckSetId = equippedPuckSet.id,
                    onPeerPuckSetReceived = { _, remoteSetId ->
                        opponentPuckSet = CarromPuckSet.fromId(remoteSetId)
                    }
                )
                OnlineMultiplayerManager.onRematchRequestedCallback = { _, senderName ->
                    incomingRematchRequester = senderName
                }
                OnlineMultiplayerManager.onRematchResponseCallback = { accepted, responderName ->
                    if (accepted) {
                        showCarromRematchWaiting = false
                        restartCarromMatch()
                    } else {
                        showCarromRematchWaiting = false
                        carromCannotPlayMessage = if (language == AppLanguage.BENGALI) {
                            "${responderName.ifBlank { effectivePlayer2Name }} রিম্যাচ প্রত্যাখ্যান করেছে।"
                        } else {
                            "${responderName.ifBlank { effectivePlayer2Name }} declined the rematch."
                        }
                        showCarromCannotPlay = true
                    }
                }
                // Initial handshake on entering Friends room so both boards align right away
                if (localPlayerId == 2) {
                    OnlineMultiplayerManager.requestCarromSync(roomCode, equippedPuckSet.id)
                } else {
                    networkCoordinator.recordShotCheckpointAndSync(
                        sessionId = matchSessionId,
                        mode = carromMode,
                        turnPlayer = currentTurnPlayer,
                        p1Score = p1Score,
                        p2Score = p2Score,
                        queenStatus = queenStatus,
                        pottedIds = pucks.filter { it.isPotted }.map { it.id }.toSet(),
                        matchOver = matchOver,
                        winnerPlayerId = winnerPlayerId,
                        isOnlineMatch = true,
                        shotSeq = currentMatchShotSeq,
                        puckStates = buildPuckSyncSnapshot()
                    )
                }
            }
        } catch (t: Throwable) {
            android.util.Log.e("CarromGameScreen", "Error subscribing to online Carrom sync", t)
        }
        onDispose {
            OnlineMultiplayerManager.clearCarromListeners()
            OnlineMultiplayerManager.onRematchRequestedCallback = null
            OnlineMultiplayerManager.onRematchResponseCallback = null
        }
    }

    // Weak-network safety net: if remote observer finished simulating opponent's shot and hasn't
    // received authoritative CARROM_STATE_SYNC yet, auto-retry CARROM_SYNC_REQUEST non-blockingly.
    LaunchedEffect(awaitingAuthoritativeSync, isSimulatingPhysics, isOnlinePeerMatch, activeOnlineRoomCode) {
        if (isOnlinePeerMatch && awaitingAuthoritativeSync && !isSimulatingPhysics && !matchOver) {
            val roomCode = activeOnlineRoomCode ?: return@LaunchedEffect
            delay(900L)
            repeat(4) {
                if (!awaitingAuthoritativeSync || matchOver || isSimulatingPhysics) return@LaunchedEffect
                OnlineMultiplayerManager.requestCarromSync(roomCode, equippedPuckSet.id)
                delay(1500L)
            }
        }
    }

    // Baseline Coin Overlap Detection & Auto-Placement Snapping Helper
    val activeBaselineY = if (currentTurnPlayer == 1) baselineYBottom else baselineYTop

    val hasCoinOnActiveBaseline by remember(activeBaselineY, hudPocketVersion, isSimulatingPhysics) {
        derivedStateOf {
            if (isSimulatingPhysics) false
            else {
                pucks.any { p ->
                    !p.isPotted && !p.isFalling && p.x.isFinite() && p.y.isFinite() &&
                    abs(p.y - activeBaselineY) < (strikerRadius + puckRadius) &&
                    p.x in (baselineMinX - puckRadius)..(baselineMaxX + puckRadius)
                }
            }
        }
    }

    val isBaselineOverlapFoul by remember(activeBaselineY, hudPocketVersion) {
        derivedStateOf {
            if (isSimulatingPhysics) false
            else isCoinObstructing(strikerBaselineX, activeBaselineY)
        }
    }

    // Keep striker reset to baseline when player turn starts with auto-placement snapping
    LaunchedEffect(currentTurnPlayer, matchOver) {
        try {
            if (!isSimulatingPhysics && !matchOver) {
                safeResetStrikerToBaseline(currentTurnPlayer)
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            android.util.Log.e("CarromGameScreen", "Error resetting striker on turn change", t)
        }
    }

    // 100% Real-Board Match-End Safety Net: whenever board is idle & settled, verify actual remaining pucks / scores
    LaunchedEffect(hudPocketVersion, isSimulatingPhysics, p1Score, p2Score, queenStatus, matchOver) {
        try {
            if (!isSimulatingPhysics && !matchOver) {
                val realBoardEval = CarromEngine.evaluateMatchWinner(
                    mode = carromMode,
                    pucks = pucks,
                    p1Score = p1Score,
                    p2Score = p2Score,
                    queenStatus = queenStatus,
                    activeShooterPlayerId = currentTurnPlayer
                )
                if (realBoardEval.isMatchOver && realBoardEval.winnerPlayerId != 0) {
                    networkCoordinator.recordShotCheckpointAndSync(
                        sessionId = matchSessionId,
                        mode = carromMode,
                        turnPlayer = currentTurnPlayer,
                        p1Score = p1Score,
                        p2Score = p2Score,
                        queenStatus = queenStatus,
                        pottedIds = pucks.filter { it.isPotted }.map { it.id }.toSet(),
                        matchOver = true,
                        winnerPlayerId = realBoardEval.winnerPlayerId,
                        isOnlineMatch = !isOpponentBot && !activeOnlineRoomCode.isNullOrBlank(),
                    shotSeq = currentMatchShotSeq,
                    puckStates = buildPuckSyncSnapshot()
                    )
                    finalizeCarromMatch(realBoardEval.winnerPlayerId)
                }
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            android.util.Log.e("CarromGameScreen", "Error evaluating match state", t)
        }
    }

    // Auto-route back to lobby on Cannot Play in Carrom
    LaunchedEffect(showCarromCannotPlay) {
        if (showCarromCannotPlay) {
            delay(2500)
            showCarromCannotPlay = false
            if (isOnlinePeerMatch) OnlineMultiplayerManager.leaveRoom()
            onBackToMenu()
        }
    }

    // AI / Online rematch consent timeout timer in Carrom
    LaunchedEffect(showCarromRematchWaiting, isOpponentBot) {
        if (showCarromRematchWaiting && isOpponentBot) {
            delay(1500)
            if (showCarromRematchWaiting) {
                showCarromRematchWaiting = false
                restartCarromMatch()
            }
        } else if (showCarromRematchWaiting && !isOpponentBot) {
            delay(15000)
            if (showCarromRematchWaiting) {
                showCarromRematchWaiting = false
                carromCannotPlayMessage = if (language == AppLanguage.BENGALI) {
                    "প্রতিপক্ষ সময়মত রিম্যাচে রাজি হয়নি।"
                } else {
                    "Opponent did not respond to the rematch in time."
                }
                showCarromCannotPlay = true
            }
        }
    }

    // 30-Second Turn Countdown Timer Loop (In online peer matches, only the active turn owner triggers turnover & broadcasts state)
    LaunchedEffect(currentTurnPlayer, turnTimerResetTrigger, isSimulatingPhysics, matchOver) {
        if (matchOver || isSimulatingPhysics) return@LaunchedEffect

        try {
            turnTimeRemaining = 30
            while (turnTimeRemaining > 0) {
                delay(1000)
                if (isSimulatingPhysics || matchOver) break
                turnTimeRemaining -= 1
                if (turnTimeRemaining in 1..4) {
                    soundManager.playAlertPop()
                }
            }

            // 30s Shot Limit Exceeded
            if (turnTimeRemaining <= 0 && !isSimulatingPhysics && !matchOver) {
                if (isOnlinePeerMatch && currentTurnPlayer != localPlayerId) {
                    // Remote observer waits for active turn owner's authoritative sync instead of diverging locally
                    activeOnlineRoomCode?.let { OnlineMultiplayerManager.requestCarromSync(it) }
                    return@LaunchedEffect
                }
                isAiming = false
                isPositioning = false
                aimPower = 0f
                aimPullDist = 0f

                soundManager.playDefeatSound()
                val expiredPlayerName = if (currentTurnPlayer == 1) player1Name else effectivePlayer2Name
                if (carromMode.hasRedQueen &&
                    (queenStatus == QueenStatus.PENDING_COVER_P1 || queenStatus == QueenStatus.PENDING_COVER_P2)
                ) {
                    pucks.firstOrNull { it.type == CarromPieceType.RED_QUEEN }?.let { red ->
                        CarromEngine.placeReturnedPieceSafely(red, pucks)
                    }
                    queenStatus = QueenStatus.ON_BOARD
                }
                foulBannerTitle = "⏱️ TIME EXPIRED (30s)"
                foulBannerSubtitle = "$expiredPlayerName took too long! Turn passed to opponent."
                showFoulBanner = true

                // Automatically pass turn to the opposing player
                currentTurnPlayer = if (currentTurnPlayer == 1) 2 else 1
                statusMessage = if (currentTurnPlayer == localPlayerId) {
                    "YOUR TURN: Position Striker & Aim!"
                } else {
                    val nextOwnerName = if (currentTurnPlayer == 1) player1Name else effectivePlayer2Name
                    "$nextOwnerName'S TURN"
                }

                // Reset striker to baseline for the next player with auto-placement snapping
                safeResetStrikerToBaseline(currentTurnPlayer)
                turnTimerResetTrigger++

                if (isOnlinePeerMatch) {
                    currentMatchShotSeq++
                    networkCoordinator.recordShotCheckpointAndSync(
                        sessionId = matchSessionId,
                        mode = carromMode,
                        turnPlayer = currentTurnPlayer,
                        p1Score = p1Score,
                        p2Score = p2Score,
                        queenStatus = queenStatus,
                        pottedIds = pucks.filter { it.isPotted }.map { it.id }.toSet(),
                        matchOver = false,
                        winnerPlayerId = 0,
                        isOnlineMatch = true,
                        shotSeq = currentMatchShotSeq,
                        puckStates = buildPuckSyncSnapshot()
                    )
                }
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            android.util.Log.e("CarromGameScreen", "Turn timer error", t)
        }
    }

    // AI Turn Automation (50% Win-Loss performance sequence when playing against AI Nasidul Mistry)
    LaunchedEffect(currentTurnPlayer, isSimulatingPhysics, matchOver) {
        if (currentTurnPlayer == 2 && isOpponentBot && !isSimulatingPhysics && !matchOver) {
            try {
                statusMessage = if (language == AppLanguage.BENGALI) "প্রতিপক্ষ তাক করছে..." else "$effectivePlayer2Name is aiming..."
                delay(1000)
                if (matchOver) return@LaunchedEffect

                // Position striker on top baseline
                striker.y = baselineYTop

                // Select AI target puck smartly according to active CarromGameMode
                val activeUnpotted = pucks.filter { !it.isPotted }
                if (activeUnpotted.isEmpty()) {
                    val fallbackEval = CarromEngine.evaluateMatchWinner(
                        mode = carromMode,
                        pucks = pucks,
                        p1Score = p1Score,
                        p2Score = p2Score,
                        queenStatus = queenStatus,
                        activeShooterPlayerId = currentTurnPlayer
                    )
                    if (fallbackEval.isMatchOver) {
                        finalizeCarromMatch(fallbackEval.winnerPlayerId)
                    }
                    return@LaunchedEffect
                }
                val poolPucks = when (carromMode) {
                    CarromGameMode.FOGGY -> {
                        if (queenStatus == QueenStatus.PENDING_COVER_P2) {
                            activeUnpotted.filter { it.type == CarromPieceType.WHITE_PUCK || it.type == CarromPieceType.BLACK_PUCK }
                        } else {
                            val red = activeUnpotted.filter { it.type == CarromPieceType.RED_QUEEN }
                            val whites = activeUnpotted.filter { it.type == CarromPieceType.WHITE_PUCK }
                            when {
                                whites.isNotEmpty() && red.isNotEmpty() && (whites.size >= 4) -> red + whites
                                whites.isNotEmpty() -> whites
                                red.isNotEmpty() -> red
                                else -> activeUnpotted
                            }
                        }
                    }
                    CarromGameMode.STANDARD -> {
                        val blacks = activeUnpotted.filter { it.type == CarromPieceType.BLACK_PUCK }
                        val red = activeUnpotted.filter { it.type == CarromPieceType.RED_QUEEN }
                        when {
                            queenStatus == QueenStatus.PENDING_COVER_P2 ->
                                activeUnpotted.filter { it.type == CarromPieceType.BLACK_PUCK || it.type == CarromPieceType.WHITE_PUCK }
                            blacks.size <= 2 && red.isNotEmpty() -> red
                            blacks.isNotEmpty() -> blacks
                            else -> activeUnpotted
                        }
                    }
                    CarromGameMode.NO_QUEEN -> {
                        val assignedPucks = activeUnpotted.filter { it.type == CarromPieceType.BLACK_PUCK }
                        if (assignedPucks.isNotEmpty()) assignedPucks else activeUnpotted
                    }
                }.ifEmpty { activeUnpotted }

                val targetPuck = poolPucks.minByOrNull {
                    val p = it
                    pockets.minOf { pkt -> hypot(p.x - pkt.x, p.y - pkt.y) }
                } ?: poolPucks.firstOrNull()

                if (targetPuck != null) {
                    // Find a clear spot on top baseline that does not overlap any coin
                    val rawX = (targetPuck.x + Random.nextFloat() * 20f - 10f).coerceIn(baselineMinX, baselineMaxX)
                    val chosenStrikerX = findNearestClearBaselineX(rawX, baselineYTop)

                    striker.x = chosenStrikerX
                    strikerBaselineX = chosenStrikerX
                    striker.y = baselineYTop
                    physicsTick++

                    delay(600)
                    if (matchOver) return@LaunchedEffect

                    if (shouldAiWinThisMatch) {
                        // SECOND MATCH: AI ("Nasidul Mistry") MUST WIN
                        // High precision vector calculation directly potting coins into pocket
                        val bestPocket = pockets.minByOrNull { pkt -> hypot(targetPuck.x - pkt.x, targetPuck.y - pkt.y) } ?: pockets[0]
                        val puckToPocketAngle = atan2(bestPocket.y - targetPuck.y, bestPocket.x - targetPuck.x)
                        val ghostX = targetPuck.x - cos(puckToPocketAngle) * (strikerRadius + targetPuck.radius)
                        val ghostY = targetPuck.y - sin(puckToPocketAngle) * (strikerRadius + targetPuck.radius)
                        val angle = atan2(ghostY - striker.y, ghostX - striker.x)
                        val speed = CarromPhysicsEngine.computeImpulseSpeed(0.62f + Random.nextFloat() * 0.25f)
                        striker.vx = cos(angle) * speed
                        striker.vy = sin(angle) * speed
                    } else {
                        // FIRST MATCH: AI ("Nasidul Mistry") MUST LOSE
                        // Controlled gentle shot with slight angle jitter so human player easily dominates and wins
                        val angle = atan2(targetPuck.y - striker.y, targetPuck.x - striker.x) + (Random.nextFloat() * 0.26f - 0.13f)
                        val power = 0.22f + Random.nextFloat() * 0.16f
                        val speed = CarromPhysicsEngine.computeImpulseSpeed(power)
                        striker.vx = cos(angle) * speed
                        striker.vy = sin(angle) * speed
                    }

                    soundManager.playCarromStrikerShot()
                    isSimulatingPhysics = true
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                android.util.Log.e("CarromGameScreen", "AI turn error", t)
                currentTurnPlayer = 1
                isSimulatingPhysics = false
            }
        }
    }

    // Physics Engine Loop (Display-synced & frame-rate-independent via withFrameNanos delta scaling)
    LaunchedEffect(isSimulatingPhysics) {
        if (!isSimulatingPhysics || matchOver) {
            isSimulatingPhysics = false
            return@LaunchedEffect
        }

        try {
            var pottedInCurrentShot = 0
            val pottedPiecesInCurrentShot = mutableListOf<CarromPiece>()
            var strikerFouled = false
            var prevFrameNanos = 0L
            var elapsedSeconds = 0f
            var frameCount = 0
            var collisionSoundCooldown = 0
            var cushionSoundCooldown = 0

        while (elapsedSeconds < 9.0f && !matchOver) {
            frameCount++
            var frameScale = 1.0f
            withFrameNanos { nowNanos ->
                if (prevFrameNanos != 0L) {
                    val deltaNs = (nowNanos - prevFrameNanos).coerceIn(4_000_000L, 35_000_000L)
                    frameScale = (deltaNs / 16_666_667f).coerceIn(0.25f, 2.0f)
                    elapsedSeconds += deltaNs / 1_000_000_000f
                } else {
                    elapsedSeconds += 1f / 60f
                }
                prevFrameNanos = nowNanos
            }

            val events = CarromPhysicsEngine.stepFrame(
                striker = striker,
                pucks = pucks,
                frameScale = frameScale
            )

            if (events.strikerFouled) {
                strikerFouled = true
                soundManager.playCarromPocketDrop()
            }

            for (puck in events.newlyPocketedPucks) {
                if (puck in pottedPiecesInCurrentShot) continue
                pottedInCurrentShot++
                pottedPiecesInCurrentShot.add(puck)
                hudPocketVersion++
                soundManager.playCarromPocketDrop()

                val pts = puck.pointValue
                val p1DisplayCoin = resolvedBoardAppearance.p1HudLabel.lowercase().replaceFirstChar { it.uppercase() }
                val p2DisplayCoin = resolvedBoardAppearance.p2HudLabel.lowercase().replaceFirstChar { it.uppercase() }
                when (puck.type) {
                    CarromPieceType.WHITE_PUCK -> {
                        if (carromMode == CarromGameMode.FOGGY) {
                            if (currentTurnPlayer == 1) p1Score += pts else p2Score += pts
                            val shooterName = if (currentTurnPlayer == 1) player1Name else effectivePlayer2Name
                            statusMessage = "🎯 $shooterName potted $p1DisplayCoin (+$pts PTS)!"
                        } else {
                            p1Score += pts
                            statusMessage = if (currentTurnPlayer == 1) {
                                "🎯 $player1Name potted $p1DisplayCoin Coin! +$pts PTS"
                            } else {
                                "🎯 $effectivePlayer2Name potted $p1DisplayCoin Coin! ($player1Name +$pts PTS)"
                            }
                        }
                    }
                    CarromPieceType.BLACK_PUCK -> {
                        if (carromMode == CarromGameMode.FOGGY) {
                            if (currentTurnPlayer == 1) p1Score += pts else p2Score += pts
                            val shooterName = if (currentTurnPlayer == 1) player1Name else effectivePlayer2Name
                            statusMessage = "🎯 $shooterName potted $p2DisplayCoin (+$pts PTS)!"
                        } else {
                            p2Score += pts
                            statusMessage = if (currentTurnPlayer == 2) {
                                "🎯 $effectivePlayer2Name potted $p2DisplayCoin Coin! +$pts PTS"
                            } else {
                                "🎯 $player1Name potted $p2DisplayCoin Coin! ($effectivePlayer2Name +$pts PTS)"
                            }
                        }
                    }
                    CarromPieceType.RED_QUEEN -> {
                        val shooterName = if (currentTurnPlayer == 1) player1Name else effectivePlayer2Name
                        statusMessage = "👑 $shooterName potted Red Queen! Pot $p1DisplayCoin/$p2DisplayCoin to cover (+${carromMode.redQueenPoints} PTS)!"
                    }
                    else -> {}
                }
            }

            if (events.bouncedCushion && cushionSoundCooldown <= 0) {
                soundManager.playCarromCushionBounce()
                cushionSoundCooldown = 3
            }

            if (events.maxCollisionImpactSpeed > 0f && collisionSoundCooldown <= 0) {
                soundManager.playCarromPuckCollision((events.maxCollisionImpactSpeed / 22f).coerceIn(0.2f, 1f))
                collisionSoundCooldown = 2
            }

            if (cushionSoundCooldown > 0) cushionSoundCooldown--
            if (collisionSoundCooldown > 0) collisionSoundCooldown--

            // Trigger canvas redraw for each animated frame
            physicsTick++

            // If all movement stopped and no pocket fall animations remain, break physics loop
            if (!events.anyMoving && frameCount > 6) {
                break
            }
        }

        // Ensure any mid-drop pocketed pieces settle before evaluating 9-coin win condition
        CarromPhysicsEngine.settleFallingPieces(striker, pucks)
        hudPocketVersion++
        physicsTick++

        // Post-shot evaluation
        delay(150)
        if (matchOver) {
            isSimulatingPhysics = false
            return@LaunchedEffect
        }

        // In online peer 1v1, ONLY the active shooter's device is authoritative for post-shot resolution.
        // The remote observer plays out the shot locally and reconciles to the shooter's exact CARROM_STATE_SYNC.
        if (isOnlinePeerMatch && activeShotOwnerId != localPlayerId) {
            val bufferedSnapshot = pendingRemoteSnapshot
            if (bufferedSnapshot != null) {
                applyRemoteCarromSnapshot(bufferedSnapshot)
            } else {
                isSimulatingPhysics = false
                activeOnlineRoomCode?.let { OnlineMultiplayerManager.requestCarromSync(it, equippedPuckSet.id) }
            }
            return@LaunchedEffect
        }

        // =========================================================================
        // MODE-AWARE POST-SHOT RESOLUTION (STANDARD / NO QUEEN / FOGGY 160)
        // =========================================================================
        val shotResolution = CarromEngine.resolvePostShotOutcome(
            mode = carromMode,
            pucks = pucks,
            pottedInShot = pottedPiecesInCurrentShot,
            strikerFouled = strikerFouled,
            shooterPlayerId = currentTurnPlayer,
            p1ScoreAfterDrop = p1Score,
            p2ScoreAfterDrop = p2Score,
            currentQueenStatus = queenStatus,
            player1Name = player1Name,
            player2Name = effectivePlayer2Name
        )

        p1Score = shotResolution.p1Score
        p2Score = shotResolution.p2Score
        queenStatus = shotResolution.queenStatus
        statusMessage = if (isOnlinePeerMatch && !shotResolution.winnerEvaluation.isMatchOver && shotResolution.nextTurnPlayer != localPlayerId) {
            val nextOwner = if (shotResolution.nextTurnPlayer == 1) player1Name else effectivePlayer2Name
            "$nextOwner'S TURN • Waiting for Shot"
        } else {
            shotResolution.statusMessage
        }
        hudPocketVersion++
        physicsTick++

        if (shotResolution.foulBannerTitle != null) {
            foulBannerTitle = shotResolution.foulBannerTitle
            foulBannerSubtitle = shotResolution.foulBannerSubtitle ?: ""
            showFoulBanner = true
            soundManager.playDefeatSound()
        } else if (shotResolution.queenCoveredByPlayerId != null) {
            soundManager.playVictoryFanfare()
        }

        val resolvedNextTurn = if (shotResolution.winnerEvaluation.isMatchOver) currentTurnPlayer else shotResolution.nextTurnPlayer
        currentTurnPlayer = resolvedNextTurn

        // Reset Striker to baseline for next turn BEFORE building/syncing authoritative state
        striker.isPotted = false
        striker.isFalling = false
        striker.fallProgress = 0f
        striker.vx = 0f
        striker.vy = 0f
        striker.pottedScale = 1f
        val targetBaselineY = if (currentTurnPlayer == 1) baselineYBottom else baselineYTop
        val initialX = if (currentTurnPlayer == 1) strikerBaselineX else (if (isOpponentBot) 500f else strikerBaselineX)
        val clearX = findNearestClearBaselineX(initialX, targetBaselineY)
        striker.x = clearX
        striker.y = targetBaselineY
        strikerBaselineX = clearX
        physicsTick++

        // Checkpoint locally and broadcast compact authoritative state (scores, turn, queen, and all puck coordinates)
        networkCoordinator.recordShotCheckpointAndSync(
            sessionId = matchSessionId,
            mode = carromMode,
            turnPlayer = resolvedNextTurn,
            p1Score = p1Score,
            p2Score = p2Score,
            queenStatus = queenStatus,
            pottedIds = pucks.filter { it.isPotted }.map { it.id }.toSet(),
            matchOver = shotResolution.winnerEvaluation.isMatchOver,
            winnerPlayerId = shotResolution.winnerEvaluation.winnerPlayerId,
            isOnlineMatch = !isOpponentBot && !activeOnlineRoomCode.isNullOrBlank(),
            shotSeq = currentMatchShotSeq,
            puckStates = buildPuckSyncSnapshot()
        )

        if (shotResolution.winnerEvaluation.isMatchOver) {
            finalizeCarromMatch(
                winnerId = shotResolution.winnerEvaluation.winnerPlayerId,
                customMessage = shotResolution.statusMessage
            )
            return@LaunchedEffect
        }

        isSimulatingPhysics = false
        turnTimerResetTrigger++
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            android.util.Log.e("CarromGameScreen", "Physics loop error", t)
            safeResetStrikerToBaseline(currentTurnPlayer)
        } finally {
            isSimulatingPhysics = false
        }
    }

    // UI Root Container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090D1A))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Compute live potting counts strictly when pucks are potted/returned/reset (decoupled from 60fps physicsTick)
            @Suppress("UNUSED_VARIABLE")
            val hudSyncVersion = hudPocketVersion
            val activeWhiteRemaining = pucks.count { it.type == CarromPieceType.WHITE_PUCK && !it.isPotted }
            val activeBlackRemaining = pucks.count { it.type == CarromPieceType.BLACK_PUCK && !it.isPotted }
            val whitePucksPotted = (CarromEngine.COINS_PER_SIDE - activeWhiteRemaining).coerceIn(0, CarromEngine.COINS_PER_SIDE)
            val blackPucksPotted = (CarromEngine.COINS_PER_SIDE - activeBlackRemaining).coerceIn(0, CarromEngine.COINS_PER_SIDE)

            // =========================================================================
            // 1. PROFESSIONAL DUAL PLAYER PROFILES (CARROM POOL STYLE)
            // =========================================================================
            // =========================================================================
            // 1. COMPLETELY SEPARATE INDEPENDENT PLAYER 1 & PLAYER 2 BOXES WITH CORNER EXIT BUTTON
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp, end = 6.dp, top = 4.dp, bottom = 2.dp)
                    .testTag("carrom_top_scorebar"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // -------------------------------------------------------------
                // LEFT: INDEPENDENT PLAYER 1 PROFILE BOX WITH CORNER EXIT BUTTON
                // -------------------------------------------------------------
                val p1IsTurn = (currentTurnPlayer == 1) && !matchOver
                val p1Progress = if (p1IsTurn) (turnTimeRemaining / 30f).coerceIn(0f, 1f) else 0f

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Clean Exit/Back button beside Player 1 box with full touch target
                    Surface(
                        onClick = {
                            soundManager.playButtonClick()
                            showExitDialog = true
                        },
                        shape = CircleShape,
                        color = Color(0xFF071428),
                        border = BorderStroke(1.4.dp, Color(0xFFFFD54F)),
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .size(32.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("carrom_corner_exit_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("✕", color = Color(0xFFFFD54F), fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                    }

                    CarromPlayerProfileBox(
                        isCurrentTurn = p1IsTurn,
                        turnProgress = p1Progress,
                        backgroundColor = if (p1IsTurn) Color(0xFF102344) else Color(0xFF09162C),
                        inactiveBorderColor = Color(0x3300E5FF),
                        modifier = Modifier.testTag("player_1_box")
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 5.dp, bottom = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // P1 Avatar with clean gold ring
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF132A52),
                                border = BorderStroke(1.5.dp, Color(0xFFFFD700)),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("👑", fontSize = 15.sp)
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Column {
                                // P1 Name and Unified Tournament Level
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = player1Name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                    if (localPlayerId == 1) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(3.dp),
                                            color = Color(0xFFFFD700),
                                            modifier = Modifier.testTag("carrom_local_player_level_badge")
                                        ) {
                                            Text(
                                                text = "LVL $currentLevel",
                                                color = Color(0xFF031429),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 7.5.sp,
                                                modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                                            )
                                        }
                                    }
                                    if (p1IsTurn) {
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("🎯", fontSize = 9.sp)
                                    }
                                }

                                // Assigned Coin / Point Goal
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 1.dp)
                                ) {
                                    CarromCoinIcon(
                                        isWhite = resolvedBoardAppearance.p1Visual.isWhitePolarity,
                                        size = 12.dp,
                                        visual = resolvedBoardAppearance.p1Visual
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (carromMode == CarromGameMode.FOGGY) {
                                            "TARGET: 160 PTS"
                                        } else {
                                            "${resolvedBoardAppearance.p1HudLabel} ($whitePucksPotted/9)"
                                        },
                                        color = Color(0xFFE0F7FA),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 8.5.sp,
                                        modifier = Modifier.testTag("carrom_p1_coin_counter")
                                    )
                                }

                                // P1 Score (CLEAN - No numeric/second clock text!)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(3.dp),
                                        color = Color(0xFF00E5FF)
                                    ) {
                                        Text(
                                            text = if (carromMode == CarromGameMode.FOGGY) "SCORE: $p1Score/160" else "SCORE: $p1Score",
                                            color = Color(0xFF021B35),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 8.5.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 0.5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Center spacing: clean layout without overlapping badges
                Spacer(modifier = Modifier.weight(1f))

                // -------------------------------------------------------------
                // RIGHT: INDEPENDENT PLAYER 2 PROFILE BOX & MENU BUTTONS
                // -------------------------------------------------------------
                val p2IsTurn = (currentTurnPlayer == 2) && !matchOver
                val p2Progress = if (p2IsTurn) (turnTimeRemaining / 30f).coerceIn(0f, 1f) else 0f

                Row(verticalAlignment = Alignment.CenterVertically) {
                    CarromPlayerProfileBox(
                        isCurrentTurn = p2IsTurn,
                        turnProgress = p2Progress,
                        backgroundColor = if (p2IsTurn) Color(0xFF381B09) else Color(0xFF1F0F05),
                        inactiveBorderColor = Color(0x33FF9100),
                        modifier = Modifier.testTag("player_2_box")
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 5.dp, bottom = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                // P2 Name
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (p2IsTurn) {
                                        Text("🎯", fontSize = 9.sp)
                                        Spacer(modifier = Modifier.width(3.dp))
                                    }
                                    if (localPlayerId == 2) {
                                        Surface(
                                            shape = RoundedCornerShape(3.dp),
                                            color = Color(0xFFFFD700),
                                            modifier = Modifier.testTag("carrom_local_player_level_badge")
                                        ) {
                                            Text(
                                                text = "LVL $currentLevel",
                                                color = Color(0xFF031429),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 7.5.sp,
                                                modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = effectivePlayer2Name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }

                                // Assigned Coin / Point Goal
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 1.dp)
                                ) {
                                    Text(
                                        text = if (carromMode == CarromGameMode.FOGGY) {
                                            "TARGET: 160 PTS"
                                        } else {
                                            "${resolvedBoardAppearance.p2HudLabel} ($blackPucksPotted/9)"
                                        },
                                        color = Color(0xFFFFD54F),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 8.5.sp,
                                        modifier = Modifier.testTag("carrom_p2_coin_counter")
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    CarromCoinIcon(
                                        isWhite = resolvedBoardAppearance.p2Visual.isWhitePolarity,
                                        size = 12.dp,
                                        visual = resolvedBoardAppearance.p2Visual
                                    )
                                }

                                // P2 Score (CLEAN - No numeric/second clock text!)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(3.dp),
                                        color = Color(0xFFFF9100)
                                    ) {
                                        Text(
                                            text = if (carromMode == CarromGameMode.FOGGY) "SCORE: $p2Score/160" else "SCORE: $p2Score",
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 8.5.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 0.5.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // P2 Avatar with clean gold ring
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF3E1F0F),
                                border = BorderStroke(1.5.dp, Color(0xFFFFD700)),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isTournament && isOpponentBot) {
                                        NasidulAvatar()
                                    } else {
                                        Text(if (isOpponentBot) "🤖" else "👤", fontSize = 15.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(3.dp))

                    // Quick Chat, Rules & Settings buttons
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Surface(
                            onClick = {
                                soundManager.playButtonClick()
                                showChatDialog = true
                            },
                            shape = CircleShape,
                            color = Color(0xFF0F2B59),
                            border = BorderStroke(1.dp, Color(0xFF00E5FF)),
                            modifier = Modifier
                                .size(26.dp)
                                .testTag("carrom_chat_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("💬", fontSize = 11.sp)
                            }
                        }
                        Surface(
                            onClick = {
                                soundManager.playButtonClick()
                                showRulesDialog = true
                            },
                            shape = CircleShape,
                            color = Color(0xFF1E3A1A),
                            border = BorderStroke(1.dp, Color(0xFF00E676)),
                            modifier = Modifier
                                .size(26.dp)
                                .testTag("carrom_rules_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("📜", fontSize = 11.sp)
                            }
                        }
                        Surface(
                            onClick = {
                                soundManager.playButtonClick()
                                showSettingsMenu = true
                            },
                            shape = CircleShape,
                            color = Color(0xFF381504),
                            border = BorderStroke(1.dp, Color(0xFFFFD54F)),
                            modifier = Modifier
                                .size(26.dp)
                                .testTag("carrom_settings_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("⚙️", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Floating Speech Bubbles
            val bubble1 = p1ChatBubble
            val bubble2 = p2ChatBubble
            if (bubble1 != null || bubble2 != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (bubble1 != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF00E5FF),
                            shadowElevation = 4.dp
                        ) {
                            Text(
                                text = bubble1,
                                color = Color(0xFF01142F),
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (bubble2 != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFD54F),
                            shadowElevation = 4.dp
                        ) {
                            Text(
                                text = bubble2,
                                color = Color(0xFF231200),
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Compact Mode + Queen + Weak-Network Resilience Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0E2242),
                    border = BorderStroke(1.dp, Color(0xFF00E5FF)),
                    modifier = Modifier.testTag("carrom_mode_badge")
                ) {
                    Text(
                        text = when (carromMode) {
                            CarromGameMode.STANDARD -> "👑 STANDARD • 19 COINS"
                            CarromGameMode.NO_QUEEN -> "⚡ NO QUEEN • 9v9"
                            CarromGameMode.FOGGY -> "🌫️ FOGGY • 160 PTS WIN (320 POOL)"
                        },
                        color = Color(0xFF80D8FF),
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                if (carromMode.hasRedQueen) {
                    Spacer(modifier = Modifier.width(6.dp))
                    val (qColor, qText) = when (queenStatus) {
                        QueenStatus.ON_BOARD -> Color(0xFFFF5252) to "🔴 QUEEN ON BOARD"
                        QueenStatus.PENDING_COVER_P1 -> Color(0xFFFFD54F) to "👑 P1 COVER QUEEN!"
                        QueenStatus.PENDING_COVER_P2 -> Color(0xFFFFD54F) to "👑 P2 COVER QUEEN!"
                        QueenStatus.COVERED_P1 -> Color(0xFF00E676) to "👑 P1 +50"
                        QueenStatus.COVERED_P2 -> Color(0xFF00E676) to "👑 P2 +50"
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = qColor.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, qColor),
                        modifier = Modifier.testTag("carrom_queen_status_pill")
                    ) {
                        Text(
                            text = qText,
                            color = qColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 8.5.sp,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Non-blocking Weak-Network / Auto-Sync Retry Indicator (Never freezes board!)
            AnimatedVisibility(
                visible = networkHealth != CarromNetworkHealth.CONNECTED_STABLE
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF3E2200),
                    border = BorderStroke(1.dp, Color(0xFFFFB300)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                        .clickable {
                            networkCoordinator.triggerPendingSyncRetry()
                            val roomCode = OnlineMultiplayerManager.activeRoomCode
                            if (!roomCode.isNullOrBlank()) {
                                OnlineMultiplayerManager.requestCarromSync(roomCode)
                            }
                        }
                        .testTag("carrom_network_status_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (networkHealth) {
                                CarromNetworkHealth.OFFLINE_LOCAL_ACTIVE ->
                                    if (language == AppLanguage.BENGALI) "নেটওয়ার্ক দুর্বল • লোকাল খেলা চালু ($pendingSyncCount সিঙ্ক বাকি)"
                                    else "Weak/Offline Network • Local Play Active (Auto-Sync Ready)"
                                else ->
                                    if (language == AppLanguage.BENGALI) "পুনরায় সংযোগ হচ্ছে... ($pendingSyncCount প্যাক বাকি)"
                                    else "Slow Network • Retrying Turn/Score Sync ($pendingSyncCount)"
                            },
                            color = Color(0xFFFFE082),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFFB300),
                            modifier = Modifier.testTag("carrom_network_retry_btn")
                        ) {
                            Text(
                                text = if (language == AppLanguage.BENGALI) "রিট্রাই" else "Retry Sync",
                                color = Color(0xFF1E1000),
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Status message strip (isolated to prevent recomposition during aiming)
            CarromStatusBar(
                currentTurnPlayer = currentTurnPlayer,
                turnTimeRemaining = turnTimeRemaining,
                isAiming = isAiming,
                isPositioning = isPositioning,
                statusMessage = statusMessage,
                aimPowerPercent = { (aimPower * 100).toInt() },
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // =========================================================================
            // 2. UNIQUE ROYAL CARROM BOARD CANVAS (MASSIVE 1:1 FULL SCREEN WIDTH VIEW)
            // =========================================================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .testTag("carrom_board_canvas_container"),
                contentAlignment = Alignment.Center
            ) {
                // Background: Standard Primary 3D Carrom Board with 'NM' Mandala Logo & Golden Wooden Frame (safe sampled bitmap + procedural fallback)
                if (boardImageBitmap != null) {
                    Image(
                        bitmap = boardImageBitmap,
                        contentDescription = "3D Royal Carrom Board with NM Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                } else {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        try {
                            drawRect(color = Color(0xFF4E2A0E))
                            val pad = size.minDimension * 0.086f
                            drawRect(
                                color = Color(0xFFF3D5A5),
                                topLeft = Offset(pad, pad),
                                size = Size(
                                    (size.width - pad * 2f).coerceAtLeast(1f),
                                    (size.height - pad * 2f).coerceAtLeast(1f)
                                )
                            )
                        } catch (_: Throwable) {}
                    }
                }

                // Subtle atmospheric mist border overlay exclusively in FOGGY mode (keeps all 19 coins crisp & clear)
                if (carromMode == CarromGameMode.FOGGY) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0x18B2EBF2),
                                        Color(0x36004D5A)
                                    )
                                )
                            )
                    )
                }

                val canLocalDeviceAct = CarromSyncProtocol.canDeviceAct(
                    matchOver = matchOver,
                    isSimulatingPhysics = isSimulatingPhysics,
                    awaitingAuthoritativeSync = awaitingAuthoritativeSync,
                    isOpponentBot = isOpponentBot,
                    isOnlinePeerMatch = isOnlinePeerMatch,
                    currentTurnPlayer = currentTurnPlayer,
                    localPlayerId = localPlayerId
                )

                var cachedDashScale by remember { mutableFloatStateOf(-1f) }
                var cachedDash6_6 by remember { mutableStateOf<PathEffect?>(null) }
                var cachedDash10_9 by remember { mutableStateOf<PathEffect?>(null) }
                var cachedDash6_5 by remember { mutableStateOf<PathEffect?>(null) }
                val powerRingTransition = rememberInfiniteTransition(label = "carromPowerRingSpin")
                val powerRingSpinState = powerRingTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 4800, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "carromPowerRingRotation"
                )

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(currentTurnPlayer, isSimulatingPhysics, matchOver, isOpponentBot, localPlayerId, isOnlinePeerMatch, awaitingAuthoritativeSync) {
                            if (!canLocalDeviceAct) return@pointerInput

                            val activeBaselineY = if (currentTurnPlayer == 1) baselineYBottom else baselineYTop

                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                val canvasWidth = size.width
                                if (canvasWidth <= 1) return@awaitEachGesture
                                val scale = canvasWidth / boardDim
                                if (scale <= 0f || !scale.isFinite()) return@awaitEachGesture
                                val touchX = down.position.x / scale
                                val touchY = down.position.y / scale
                                if (!touchX.isFinite() || !touchY.isFinite()) return@awaitEachGesture

                                down.consume()

                                // If touch is directly on the striker along the baseline, allow baseline sliding initially
                                val distToStriker = hypot(touchX - striker.x, touchY - activeBaselineY)
                                val touchDirectlyOnStriker = distToStriker < strikerRadius * 1.6f
                                var isSlidingBaseline = touchDirectlyOnStriker && (abs(touchY - activeBaselineY) < 36f)

                                if (isSlidingBaseline) {
                                    isPositioning = true
                                    isAiming = false
                                    aimPower = 0f
                                    aimPullDist = 0f
                                } else {
                                    // Direct slingshot aim in full 360 degrees (strictly proportional to pull distance)
                                    val pullVecX = striker.x - touchX
                                    val pullVecY = striker.y - touchY
                                    val pullDist = hypot(pullVecX, pullVecY)
                                    val initialPower = CarromPhysicsEngine.computeShotPower(pullDist)
                                    if (initialPower > 0f) {
                                        aimAngle = atan2(pullVecY, pullVecX)
                                        aimPower = initialPower
                                        aimPullDist = pullDist.coerceAtMost(CarromPhysicsEngine.MAX_PULL_DIST)
                                        isAiming = true
                                        isPositioning = false
                                    }
                                }

                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break

                                    if (!change.pressed) {
                                        change.consume()
                                        val releaseTouchX = change.position.x / scale
                                        val releaseTouchY = change.position.y / scale
                                        val finalPullVecX = striker.x - releaseTouchX
                                        val finalPullVecY = striker.y - releaseTouchY
                                        val finalPullDist = hypot(finalPullVecX, finalPullVecY)
                                        val finalPower = CarromPhysicsEngine.computeShotPower(finalPullDist)

                                        if (isSlidingBaseline) {
                                            emitThrottledStrikerPosition(striker.x, force = true)
                                        }
                                        if (isAiming && finalPower > 0f) {
                                            if (finalPullDist > CarromPhysicsEngine.CANCEL_PULL_RADIUS) {
                                                aimAngle = atan2(finalPullVecY, finalPullVecX)
                                            }
                                            if (isBaselineOverlapFoul) {
                                                val clearX = findNearestClearBaselineX(striker.x, activeBaselineY)
                                                striker.x = clearX
                                                strikerBaselineX = clearX
                                            }
                                            activeShotOwnerId = currentTurnPlayer
                                            currentMatchShotSeq++
                                            if (isOnlinePeerMatch && !activeOnlineRoomCode.isNullOrBlank()) {
                                                OnlineMultiplayerManager.broadcastCarromShot(
                                                    roomCode = activeOnlineRoomCode,
                                                    shotSeq = currentMatchShotSeq,
                                                    turnPlayer = currentTurnPlayer,
                                                    strikerX = striker.x,
                                                    strikerY = striker.y,
                                                    aimAngle = aimAngle,
                                                    power = finalPower
                                                )
                                            }
                                            val impulseSpeed = CarromPhysicsEngine.computeImpulseSpeed(finalPower)
                                            striker.vx = cos(aimAngle) * impulseSpeed
                                            striker.vy = sin(aimAngle) * impulseSpeed
                                            soundManager.playCarromStrikerShot()
                                            isSimulatingPhysics = true
                                        }
                                        isAiming = false
                                        isPositioning = false
                                        aimPower = 0f
                                        aimPullDist = 0f
                                        break
                                    }

                                    change.consume()
                                    val curTouchX = change.position.x / scale
                                    val curTouchY = change.position.y / scale
                                    val curPullDist = hypot(striker.x - curTouchX, striker.y - curTouchY)

                                    if (isSlidingBaseline) {
                                        val deltaY = abs(curTouchY - touchY)
                                        // Pulling away vertically or diagonally immediately engages 360-degree aiming mode
                                        if (deltaY > 20f || hypot(curTouchX - striker.x, curTouchY - activeBaselineY) > strikerRadius * 2.2f) {
                                            isSlidingBaseline = false
                                            isPositioning = false
                                            emitThrottledStrikerPosition(striker.x, force = true)
                                        } else {
                                            val clampedX = curTouchX.coerceIn(baselineMinX, baselineMaxX)
                                            val snappedX = findNearestClearBaselineX(clampedX, activeBaselineY)
                                            striker.x = snappedX
                                            strikerBaselineX = snappedX
                                            striker.y = activeBaselineY
                                            isPositioning = true
                                            isAiming = false
                                            aimPower = 0f
                                            aimPullDist = 0f
                                            emitThrottledStrikerPosition(snappedX)
                                        }
                                    }

                                    if (!isSlidingBaseline) {
                                        // UNLOCKED FULL 360-DEGREE CONTINUOUS ROTATION AROUND STRIKER
                                        val pullVecX = striker.x - curTouchX
                                        val pullVecY = striker.y - curTouchY
                                        val dynamicPower = CarromPhysicsEngine.computeShotPower(curPullDist)

                                        if (dynamicPower > 0f) {
                                            aimAngle = atan2(pullVecY, pullVecX)
                                            aimPower = dynamicPower
                                            aimPullDist = curPullDist.coerceAtMost(CarromPhysicsEngine.MAX_PULL_DIST)
                                            isAiming = true
                                            isPositioning = false
                                        } else {
                                            // Finger pulled directly back onto striker -> cancels aim safely
                                            aimPower = 0f
                                            aimPullDist = 0f
                                            isAiming = false
                                        }
                                    }
                                }
                            }
                        }
                ) {
                    try {
                    val _currentTick = physicsTick
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    if (canvasWidth <= 1f || canvasHeight <= 1f || !canvasWidth.isFinite() || !canvasHeight.isFinite()) {
                        return@Canvas
                    }
                    val scale = canvasWidth / boardDim
                    if (scale <= 0f || !scale.isFinite()) return@Canvas
                    val innerX = innerMin * scale
                    val innerY = innerMin * scale
                    val innerW = (innerMax - innerMin) * scale
                    val innerH = (innerMax - innerMin) * scale

                    // G. Render 19 Pucks on 3D Carrom Board with High-End Realistic 3D Shading & Specular Highlights
                    for (puck in pucks) {
                        if (puck.isPotted && !puck.isFalling) continue
                        val currentScale = if (puck.isFalling) puck.pottedScale else 1f
                        if (currentScale <= 0.04f || !puck.x.isFinite() || !puck.y.isFinite()) continue
                        val px = puck.x * scale
                        val py = puck.y * scale
                        val pr = puck.radius * scale * currentScale
                        if (pr <= 0.5f || !px.isFinite() || !py.isFinite() || !pr.isFinite()) continue

                        // Multi-layer Grounding Shadow (Directional Light from Top-Left ~10 o'clock)
                        // 1. Soft Ambient Occlusion shadow
                      drawCircle(
                            color = Color(0x33000000),
                            radius = pr * 1.08f,
                            center = Offset(px + 4f * scale, py + 5.2f * scale)
                        )
                        // 2. Crisp Contact shadow directly under puck base
                        drawCircle(
                            color = Color(0x550A0500),
                            radius = pr * 0.96f,
                            center = Offset(px + 2f * scale, py + 2.8f * scale)
                        )

                        when (puck.type) {
                            CarromPieceType.WHITE_PUCK -> {
                                drawSidePieceVisual(
                                    px = px,
                                    py = py,
                                    pr = pr,
                                    scale = scale,
                                    visual = resolvedBoardAppearance.p1Visual,
                                    customBitmap = p1PuckImageBitmap
                                )
                            }

                            CarromPieceType.BLACK_PUCK -> {
                                drawSidePieceVisual(
                                    px = px,
                                    py = py,
                                    pr = pr,
                                    scale = scale,
                                    visual = resolvedBoardAppearance.p2Visual,
                                    customBitmap = p2PuckImageBitmap
                                )
                            }

                            CarromPieceType.RED_QUEEN -> {
                                // 1. Cylindrical Beveled Crimson Rim (Ruby top-left highlight to deep maroon shadow)
                                drawCircle(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFFFF8A80), Color(0xFFD50000), Color(0xFF9B0000), Color(0xFF4A0000)),
                                        start = Offset(px - pr, py - pr),
                                        end = Offset(px + pr, py + pr)
                                    ),
                                    radius = pr,
                                    center = Offset(px, py)
                                )

                                // 2. Inset Royal Ruby Top Face
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color(0xFFFF5252), Color(0xFFE5002B), Color(0xFFB71C1C), Color(0xFF6A0008)),
                                        center = Offset(px - pr * 0.28f, py - pr * 0.28f),
                                        radius = pr * 1.15f
                                    ),
                                    radius = pr * 0.91f,
                                    center = Offset(px, py)
                                )

                                // 3. Golden Royal Crown Filigree Ring
                                drawCircle(
                                    color = Color(0xFFFFD54F),
                                    radius = pr * 0.62f,
                                    center = Offset(px, py),
                                    style = Stroke(width = 1.6f * scale)
                                )

                                // 4. Center Queen Gem Core
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color(0xFFFFF8E1), Color(0xFFFFD54F), Color(0xFFFF8F00)),
                                        center = Offset(px - pr * 0.08f, py - pr * 0.08f),
                                        radius = pr * 0.34f
                                    ),
                                    radius = pr * 0.28f,
                                    center = Offset(px, py)
                                )

                                // 5. Glossy Specular Highlight
                                drawOval(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color(0xEEFFFFFF), Color(0x55FF8A80), Color.Transparent),
                                        center = Offset(px - pr * 0.36f, py - pr * 0.36f),
                                        radius = pr * 0.42f
                                    ),
                                    topLeft = Offset(px - pr * 0.65f, py - pr * 0.65f),
                                    size = Size(pr * 0.58f, pr * 0.38f)
                                )
                            }

                            else -> {}
                        }

                        if (puck.isFalling) {
                            val dropDarkness = puck.fallProgress.coerceIn(0f, 0.88f)
                            drawCircle(
                                color = Color.Black.copy(alpha = dropDarkness),
                                radius = pr,
                                center = Offset(px, py)
                            )
                        }
                    }

                    // H. Render 3D Royal Striker with Multi-layer Shading & Metallic Rim
                    if (!striker.isPotted || striker.isFalling) {
                        val currentScale = if (striker.isFalling) striker.pottedScale else 1f
                        if (currentScale > 0.04f && striker.x.isFinite() && striker.y.isFinite()) {
                            val sx = striker.x * scale
                            val sy = striker.y * scale
                            val sr = striker.radius * scale * currentScale
                            if (sr > 0.5f && sx.isFinite() && sy.isFinite() && sr.isFinite()) {

                        // Equipped Carrom Striker Power Ring (Ring #1 default across all modes; uses player's custom ring asset + scales/spins with pull power)
                        if (!striker.isFalling && !matchOver) {
                            val activePullPower = if (isAiming) aimPower else 0f
                            val ringRadius = sr * (1.18f + activePullPower * 0.44f)
                            val ringRotationDeg = (powerRingSpinState.value * (1f + activePullPower * 2.5f)) % 360f
                            val ringSpan = (sr * (2.92f + activePullPower * 0.92f)).roundToInt()
                            val glowAlpha = if (isAiming || canLocalDeviceAct) 0.26f + activePullPower * 0.48f else 0.18f
                            if (equippedPowerRingImageBitmap != null && ringSpan > 0) {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            equippedPowerRing.glowColor.copy(alpha = glowAlpha),
                                            equippedPowerRing.primaryColor.copy(alpha = glowAlpha * 0.35f),
                                            Color.Transparent
                                        ),
                                        center = Offset(sx, sy),
                                        radius = ringRadius * 1.38f
                                    ),
                                    radius = ringRadius * 1.38f,
                                    center = Offset(sx, sy)
                                )
                                try {
                                    rotate(degrees = ringRotationDeg, pivot = Offset(sx, sy)) {
                                        drawImage(
                                            image = equippedPowerRingImageBitmap,
                                            dstOffset = IntOffset(
                                                (sx - ringSpan * 0.5f).roundToInt(),
                                                (sy - ringSpan * 0.5f).roundToInt()
                                            ),
                                            dstSize = IntSize(ringSpan, ringSpan)
                                        )
                                    }
                                } catch (_: Throwable) {
                                    drawPowerRingEffect(
                                        ring = equippedPowerRing,
                                        center = Offset(sx, sy),
                                        radius = ringRadius,
                                        scale = scale,
                                        rotationDeg = ringRotationDeg,
                                        power = if (isAiming) activePullPower else 0.22f,
                                        isAimingOrActive = isAiming || canLocalDeviceAct
                                    )
                                }
                            } else {
                                drawPowerRingEffect(
                                    ring = equippedPowerRing,
                                    center = Offset(sx, sy),
                                    radius = ringRadius,
                                    scale = scale,
                                    rotationDeg = ringRotationDeg,
                                    power = if (isAiming) activePullPower else 0.22f,
                                    isAimingOrActive = isAiming || canLocalDeviceAct
                                )
                            }
                        }

                        // Striker Interactive Direct-Touch Pulse Halo when waiting for shot
                        if (canLocalDeviceAct && !isAiming) {
                            drawCircle(
                                color = Color(0x3300E5FF),
                                radius = sr * 1.35f,
                                center = Offset(sx, sy)
                            )
                        }

                        // Multi-layer Grounding Drop Shadow
                        drawCircle(
                            color = Color(0x38000000),
                            radius = sr * 1.10f,
                            center = Offset(sx + 5.5f * scale, sy + 7f * scale)
                        )
                        drawCircle(
                            color = Color(0x660A0600),
                            radius = sr * 0.98f,
                            center = Offset(sx + 3f * scale, sy + 3.8f * scale)
                        )

                        val strikerSpan = (sr * 2.16f).roundToInt()
                        val drewCustomStriker = if (customStrikerImageBitmap != null && strikerSpan > 0) {
                            try {
                                drawImage(
                                    image = customStrikerImageBitmap,
                                    dstOffset = IntOffset((sx - strikerSpan * 0.5f).roundToInt(), (sy - strikerSpan * 0.5f).roundToInt()),
                                    dstSize = IntSize(strikerSpan, strikerSpan)
                                )
                                true
                            } catch (_: Throwable) {
                                false
                            }
                        } else false

                        if (drewCustomStriker) {
                            // 2. Subtle Accent Rim Contour matching native striker bezel
                            drawCircle(
                                color = equippedStriker.accentColor.copy(alpha = 0.28f),
                                radius = sr * 0.99f,
                                center = Offset(sx, sy),
                                style = Stroke(width = 1.1f * scale)
                            )
                            // 3. High-Gloss Top-Left Specular Glass Reflection
                            drawOval(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xDDFFFFFF), Color(0x33FFFFFF), Color.Transparent),
                                    center = Offset(sx - sr * 0.35f, sy - sr * 0.35f),
                                    radius = sr * 0.45f
                                ),
                                topLeft = Offset(sx - sr * 0.65f, sy - sr * 0.65f),
                                size = Size(sr * 0.65f, sr * 0.40f)
                            )
                            // 4. Rim Specular Glint
                            drawArc(
                                color = Color(0xEEFFFFFF),
                                startAngle = 195f,
                                sweepAngle = 80f,
                                useCenter = false,
                                topLeft = Offset(sx - sr * 0.95f, sy - sr * 0.95f),
                                size = Size(sr * 1.90f, sr * 1.90f),
                                style = Stroke(width = 2.0f * scale, cap = StrokeCap.Round)
                            )
                        } else {
                            // 1. Heavy Outer Beveled Metallic Gold Rim (Illuminated top-left to shadow bottom-right)
                            drawCircle(
                                brush = Brush.linearGradient(
                                    listOf(Color(0xFFFFF9C4), Color(0xFFFFD54F), Color(0xFFFF9100), Color(0xFF6D3B00)),
                                    start = Offset(sx - sr, sy - sr),
                                    end = Offset(sx + sr, sy + sr)
                                ),
                                radius = sr,
                                center = Offset(sx, sy)
                            )

                            // 2. Beveled Inset Deep Blue Lacquered Body
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFE0F7FA), Color(0xFF00E5FF), Color(0xFF0091EA), Color(0xFF003882), Color(0xFF061838)),
                                    center = Offset(sx - sr * 0.32f, sy - sr * 0.32f),
                                    radius = sr * 1.2f
                                ),
                                radius = sr * 0.90f,
                                center = Offset(sx, sy)
                            )

                            // 3. Inner Golden Filigree Beading
                            drawCircle(
                                brush = Brush.linearGradient(
                                    listOf(Color(0xFFFFEA00), Color(0xFFFFB300), Color(0xFF7A4500))
                                ),
                                radius = sr * 0.72f,
                                center = Offset(sx, sy),
                                style = Stroke(width = 2.5f * scale)
                            )

                            // 4. Center Crystal Gem Emblem with Crown
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFF0D254C), Color(0xFF041026)),
                                    center = Offset(sx, sy),
                                    radius = sr * 0.52f
                                ),
                                radius = sr * 0.50f,
                                center = Offset(sx, sy)
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFFFD54F), Color(0xFFFF8F00), Color(0xFFB25000)),
                                    center = Offset(sx - sr * 0.1f, sy - sr * 0.1f),
                                    radius = sr * 0.35f
                                ),
                                radius = sr * 0.32f,
                                center = Offset(sx, sy)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = sr * 0.08f,
                                center = Offset(sx - sr * 0.09f, sy - sr * 0.09f)
                            )

                            // 5. High-Gloss Top-Left Specular Reflection
                            drawOval(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xEEFFFFFF), Color(0x66E0F7FA), Color.Transparent),
                                    center = Offset(sx - sr * 0.35f, sy - sr * 0.35f),
                                    radius = sr * 0.45f
                                ),
                                topLeft = Offset(sx - sr * 0.65f, sy - sr * 0.65f),
                                size = Size(sr * 0.65f, sr * 0.40f)
                            )
                            // Striker Gold Rim Specular Glint
                            drawArc(
                                color = Color(0xEEFFFFFF),
                                startAngle = 195f,
                                sweepAngle = 80f,
                                useCenter = false,
                                topLeft = Offset(sx - sr * 0.95f, sy - sr * 0.95f),
                                size = Size(sr * 1.90f, sr * 1.90f),
                                style = Stroke(width = 2.4f * scale, cap = StrokeCap.Round)
                            )
                        }

                        if (striker.isFalling) {
                            val dropDarkness = striker.fallProgress.coerceIn(0f, 0.88f)
                            drawCircle(
                                color = Color.Black.copy(alpha = dropDarkness),
                                radius = sr,
                                center = Offset(sx, sy)
                            )
                        }

                        // Baseline Overlap Foul Pulsing Red Indicator
                        if (isBaselineOverlapFoul && !isSimulatingPhysics) {
                            if (cachedDashScale != scale || cachedDash6_6 == null) {
                                cachedDashScale = scale
                                cachedDash6_6 = PathEffect.dashPathEffect(floatArrayOf(6f * scale, 6f * scale), 0f)
                                cachedDash10_9 = PathEffect.dashPathEffect(floatArrayOf(10f * scale, 9f * scale), 0f)
                                cachedDash6_5 = PathEffect.dashPathEffect(floatArrayOf(6f * scale, 5f * scale), 0f)
                            }
                            drawCircle(
                                color = Color(0xFFFF1744),
                                radius = sr * 1.25f,
                                center = Offset(sx, sy),
                                style = Stroke(width = 3.5f * scale)
                            )
                            drawCircle(
                                color = Color(0xFFFFEA00),
                                radius = sr * 1.42f,
                                center = Offset(sx, sy),
                                style = Stroke(
                                    width = 1.8f * scale,
                                    pathEffect = cachedDash6_6
                                )
                            )
                        }
                            }
                    }
                }

                // I. Professional Single-Segment Dotted Aim Line & Rim Guide (Stops at First Coin or Wall)
                if (isAiming && aimPower > 0f) {
                        val sx = striker.x * scale
                        val sy = striker.y * scale
                        val sr = striker.radius * scale
                        val dirX = cos(aimAngle)
                        val dirY = sin(aimAngle)

                        val aimGuide = CarromPhysicsEngine.computeSingleSegmentAimGuide(
                            strikerX = striker.x,
                            strikerY = striker.y,
                            strikerRadius = striker.radius,
                            aimAngle = aimAngle,
                            pucks = pucks
                        )

                        // Dynamic Color palette & Glow based on Power
                        val (primaryColor, glowColor, trailColor) = when {
                            aimPower < 0.35f -> Triple(Color(0xFF00E5FF), Color(0x6600E5FF), Color(0xFF80D8FF)) // Electric Cyan (Soft/Controlled)
                            aimPower < 0.70f -> Triple(Color(0xFFFFEA00), Color(0x77FFD600), Color(0xFFFFD54F)) // Radiant Gold (Medium)
                            else -> Triple(Color(0xFFFF1744), Color(0x99FF1744), Color(0xFFFF5252))              // Blazing Neon Red (Power Break)
                        }

                        if (cachedDashScale != scale || cachedDash6_5 == null) {
                            cachedDashScale = scale
                            cachedDash6_6 = PathEffect.dashPathEffect(floatArrayOf(6f * scale, 6f * scale), 0f)
                            cachedDash10_9 = PathEffect.dashPathEffect(floatArrayOf(10f * scale, 9f * scale), 0f)
                            cachedDash6_5 = PathEffect.dashPathEffect(floatArrayOf(6f * scale, 5f * scale), 0f)
                        }
                        // Pull-back slingshot tension handle & distance line behind striker
                        val pullHandleDistCanvas = aimPullDist * scale
                        val handleX = sx - dirX * pullHandleDistCanvas
                        val handleY = sy - dirY * pullHandleDistCanvas
                        drawLine(
                            color = primaryColor.copy(alpha = 0.72f),
                            start = Offset(sx, sy),
                            end = Offset(handleX, handleY),
                            strokeWidth = (2.2f + aimPower * 3.0f) * scale,
                            cap = StrokeCap.Round,
                            pathEffect = cachedDash6_5
                        )
                        drawCircle(
                            color = glowColor,
                            radius = (11f + aimPower * 10f) * scale,
                            center = Offset(handleX, handleY)
                        )
                        drawCircle(
                            color = primaryColor,
                            radius = (6.5f + aimPower * 5.0f) * scale,
                            center = Offset(handleX, handleY),
                            style = Stroke(width = 2.4f * scale)
                        )

                        // Single straight aim ray stopping strictly at center limit, first coin contact, OR inner wall/cushion
                        val fullRayDist = hypot(aimGuide.endX - striker.x, aimGuide.endY - striker.y)
                        val rayDistCanvas = (fullRayDist * scale).coerceAtLeast(0f)
                        val rayEndX = (aimGuide.endX * scale).coerceIn(innerX, innerX + innerW)
                        val rayEndY = (aimGuide.endY * scale).coerceIn(innerY, innerY + innerH)

                        // Single crisp straight forward dotted guide + core line (never extends past middle range, first coin, or wall)
                        if (rayDistCanvas > 1f && rayEndX.isFinite() && rayEndY.isFinite()) {
                            drawLine(
                                brush = Brush.linearGradient(
                                    colors = listOf(glowColor, glowColor.copy(alpha = 0.22f), Color.Transparent),
                                    start = Offset(sx, sy),
                                    end = Offset(rayEndX, rayEndY)
                                ),
                                start = Offset(sx, sy),
                                end = Offset(rayEndX, rayEndY),
                                strokeWidth = (3.6f + aimPower * 5.5f) * scale,
                                cap = StrokeCap.Round
                            )

                            drawLine(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color.White, primaryColor, trailColor.copy(alpha = 0.85f)),
                                    start = Offset(sx, sy),
                                    end = Offset(rayEndX, rayEndY)
                                ),
                                start = Offset(sx, sy),
                                end = Offset(rayEndX, rayEndY),
                                strokeWidth = (1.6f + aimPower * 1.8f) * scale,
                                cap = StrokeCap.Round,
                                pathEffect = cachedDash10_9
                            )
                        }

                        // Directional Forward Arrowhead stopping strictly at the ray endpoint
                        if (rayDistCanvas > 10f * scale && rayEndX.isFinite() && rayEndY.isFinite()) {
                            val arrowHeadSize = ((9f + aimPower * 7f) * scale).coerceAtMost(rayDistCanvas * 0.45f)
                            val perpX = -dirY
                            val perpY = dirX
                            val tipBackX = rayEndX - dirX * arrowHeadSize
                            val tipBackY = rayEndY - dirY * arrowHeadSize
                            drawLine(
                                color = primaryColor,
                                start = Offset(tipBackX + perpX * (arrowHeadSize * 0.55f), tipBackY + perpY * (arrowHeadSize * 0.55f)),
                                end = Offset(rayEndX, rayEndY),
                                strokeWidth = (2.2f + aimPower * 1.2f) * scale,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = primaryColor,
                                start = Offset(tipBackX - perpX * (arrowHeadSize * 0.55f), tipBackY - perpY * (arrowHeadSize * 0.55f)),
                                end = Offset(rayEndX, rayEndY),
                                strokeWidth = (2.2f + aimPower * 1.2f) * scale,
                                cap = StrokeCap.Round
                            )
                        }

                        // Crisp Alignment Dots along the single straight vector up to the stop point (board-relative spacing)
                        val dotStep = 22f * scale
                        val maxDots = ((rayDistCanvas - 8f * scale).coerceAtLeast(0f) / dotStep).toInt().coerceIn(0, 40)
                        for (d in 1..maxDots) {
                            val dotX = sx + dirX * dotStep * d
                            val dotY = sy + dirY * dotStep * d

                            if (dotX < innerX || dotX > innerX + innerW || dotY < innerY || dotY > innerY + innerH) break

                            val progress = d.toFloat() / maxDots.coerceAtLeast(1)
                            val dotAlpha = (1f - progress * 0.30f).coerceIn(0.35f, 1f)
                            val dotRadius = ((2.8f + aimPower * 1.6f) - progress * 0.8f).coerceAtLeast(1.6f) * scale

                            drawCircle(
                                color = glowColor.copy(alpha = dotAlpha * 0.55f),
                                radius = dotRadius * 1.6f,
                                center = Offset(dotX, dotY)
                            )
                            drawCircle(
                                color = if (d % 2 == 0) primaryColor.copy(alpha = dotAlpha) else Color.White.copy(alpha = dotAlpha),
                                radius = dotRadius,
                                center = Offset(dotX, dotY)
                            )
                        }

                        // Clean Rim Contact Ring ONLY where the striker touches the first coin (zero secondary/hack paths)
                        if (aimGuide.hitPuck != null) {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.88f),
                                radius = sr * 0.96f,
                                center = Offset(rayEndX, rayEndY),
                                style = Stroke(
                                    width = 2.0f * scale,
                                    pathEffect = cachedDash6_5
                                )
                            )
                        }

                        // Scaling Outer Power Ring + Sweep Arc (expands smoothly with pull distance)
                        val gaugeRadius = sr + (6f + aimPower * 24f) * scale
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.28f),
                            radius = gaugeRadius,
                            center = Offset(sx, sy),
                            style = Stroke(width = 2.0f * scale)
                        )
                        val powerSweep = aimPower * 360f
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(Color(0xFF00E676), Color(0xFFFFEA00), Color(0xFFFF1744), Color(0xFF00E676))
                            ),
                            startAngle = -90f,
                            sweepAngle = powerSweep,
                            useCenter = false,
                            topLeft = Offset(sx - gaugeRadius, sy - gaugeRadius),
                            size = Size(gaugeRadius * 2f, gaugeRadius * 2f),
                            style = Stroke(width = (3.2f + aimPower * 3.0f) * scale, cap = StrokeCap.Round)
                        )
                    }
                    } catch (drawErr: Throwable) {
                        android.util.Log.e("CarromGameScreen", "Board canvas draw error", drawErr)
                    }
                }
            }

            // 3. HORIZONTAL BASELINE SLIDER CONTROL (SMOOTH PRECISE PLACEMENT)
            val isSliderEnabled = CarromSyncProtocol.canDeviceAct(
                matchOver = matchOver,
                isSimulatingPhysics = isSimulatingPhysics,
                awaitingAuthoritativeSync = awaitingAuthoritativeSync,
                isOpponentBot = isOpponentBot,
                isOnlinePeerMatch = isOnlinePeerMatch,
                currentTurnPlayer = currentTurnPlayer,
                localPlayerId = localPlayerId
            )
            CarromBaselineSlider(
                strikerX = strikerBaselineX,
                minX = baselineMinX,
                maxX = baselineMaxX,
                isFoul = isBaselineOverlapFoul,
                enabled = isSliderEnabled,
                onPositionChange = { newX ->
                    val snappedX = findNearestClearBaselineX(newX, activeBaselineY)
                    strikerBaselineX = snappedX
                    striker.x = snappedX
                    striker.y = activeBaselineY
                    physicsTick++
                    emitThrottledStrikerPosition(snappedX)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 2.dp)
            )

            Spacer(modifier = Modifier.weight(1f))
        }

        // =========================================================================
        // ANIMATED STRIKER FOUL NOTIFICATION OVERLAY BANNER
        // =========================================================================
        AnimatedVisibility(
            visible = showFoulBanner,
            enter = fadeIn(tween(250)) + slideInVertically(initialOffsetY = { -it / 2 }),
            exit = fadeOut(tween(300)) + slideOutVertically(targetOffsetY = { -it / 2 }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 75.dp, start = 14.dp, end = 14.dp)
                .zIndex(99f)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF26050B),
                border = BorderStroke(
                    2.dp,
                    Brush.horizontalGradient(
                        listOf(Color(0xFFFF1744), Color(0xFFFFD700), Color(0xFFFF1744))
                    )
                ),
                shadowElevation = 18.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("carrom_foul_penalty_banner")
            ) {
                Row(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF380812), Color(0xFF1E040A))
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pulsing Warning Icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD50000))
                            .border(1.5.dp, Color(0xFFFFD700), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⚠️", fontSize = 22.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = foulBannerTitle,
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = foulBannerSubtitle,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }

                    // Dismiss button
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF))
                            .clickable { showFoulBanner = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✕", color = Color(0xFFFF8A80), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // =========================================================================
        // QUICK CHAT MODAL DIALOG
        // =========================================================================
        if (showChatDialog) {
            Dialog(onDismissRequest = { showChatDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF0B1E3F),
                    border = BorderStroke(2.dp, Color(0xFF00E5FF)),
                    shadowElevation = 16.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("carrom_chat_dialog")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "💬 QUICK CHAT & EMOJIS",
                            color = Color(0xFFFFEA00),
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val chatOptions = listOf(
                            "Good Shot! 🎯",
                            "Well Played! 👑",
                            "Nice Strike! ⚡",
                            "Oops! 😅",
                            "Nice Try! 👏",
                            "Good Game! 🤝"
                        )

                        chatOptions.chunked(2).forEach { rowChats ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowChats.forEach { msg ->
                                    Button(
                                        onClick = {
                                            soundManager.playButtonClick()
                                            p1ChatBubble = msg
                                            showChatDialog = false
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF132F5C)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                    ) {
                                        Text(text = msg, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // =========================================================================
        // SETTINGS & PAUSE MENU MODAL
        // =========================================================================
        if (showSettingsMenu) {
            Dialog(onDismissRequest = { showSettingsMenu = false }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF0B1D38),
                    border = BorderStroke(2.dp, Color(0xFFFFD54F)),
                    shadowElevation = 20.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("carrom_settings_dialog")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚙️ CARROM GAME MENU",
                            color = Color(0xFFFFEA00),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Resume
                        Button(
                            onClick = {
                                soundManager.playButtonClick()
                                showSettingsMenu = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Text("▶️ RESUME MATCH", color = Color(0xFF021B35), fontWeight = FontWeight.Black)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Sound Toggle
                        Button(
                            onClick = {
                                soundManager.isSoundEnabled = !soundManager.isSoundEnabled
                                soundManager.playButtonClick()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3359)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Text(
                                text = if (soundManager.isSoundEnabled) "🔊 SOUND: ON" else "🔇 SOUND: OFF",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // View Rules & Guide
                        Button(
                            onClick = {
                                soundManager.playButtonClick()
                                showSettingsMenu = false
                                showRulesDialog = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Text("📜 TOURNAMENT RULES & FOULS", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Surrender / Quit
                        Button(
                            onClick = {
                                soundManager.playButtonClick()
                                showSettingsMenu = false
                                if (isOnlinePeerMatch) OnlineMultiplayerManager.leaveRoom()
                                onBackToMenu()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Text("🏳️ SURRENDER & EXIT", color = Color.White, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        // =========================================================================
        // EXIT CONFIRMATION DIALOG (BACK PRESSED)
        // =========================================================================
        if (showExitDialog) {
            Dialog(onDismissRequest = { showExitDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF09162C),
                    border = BorderStroke(2.dp, Color(0xFFFFD54F)),
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .padding(16.dp)
                        .testTag("exit_confirmation_dialog")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🚪", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Are you sure you want to exit?",
                            color = Color(0xFFFFD54F),
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (language == AppLanguage.BENGALI) "আপনি কি নিশ্চিতভাবে ম্যাচ থেকে বের হতে চান? বের হলে ম্যাচ ফোরফিট হবে।" else "Leaving the match will forfeit your current progress and return to menu.",
                            color = Color(0xFFB0BEC5),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = {
                                    soundManager.playButtonClick()
                                    showExitDialog = false
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A5F)),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("exit_dialog_no")
                            ) {
                                Text("No", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Button(
                                onClick = {
                                    soundManager.playButtonClick()
                                    showExitDialog = false
                                    if (isOnlinePeerMatch) OnlineMultiplayerManager.leaveRoom()
                                    onBackToMenu()
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("exit_dialog_yes")
                            ) {
                                Text("Yes", fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // COMPREHENSIVE CARROM TOURNAMENT RULES MODAL DIALOG
        // =========================================================================
        if (showRulesDialog) {
            CarromRulesDialog(
                carromMode = carromMode,
                onDismiss = { showRulesDialog = false }
            )
        }

        // =========================================================================
        // MATCH OVER / VICTORY MODAL DIALOG
        // =========================================================================
        if (matchOver) {
            val isLocalWinner = (winnerPlayerId == localPlayerId)
            val liveCoins by CoinManager.coinsFlow.collectAsStateWithLifecycle()
            val liveDiamonds by CoinManager.diamondsFlow.collectAsStateWithLifecycle()
            val effectiveStake = when {
                entryFeePerPlayer > 0 -> entryFeePerPlayer
                prizePool > 0 -> prizePool / 2
                else -> 0
            }
            val formattedPool = NumberFormat.getNumberInstance(Locale.US).format(prizePool)
            val formattedWalletCoins = NumberFormat.getNumberInstance(Locale.US).format(liveCoins)
            val winnerDisplayName = if (winnerPlayerId == 1) player1Name else effectivePlayer2Name

            Dialog(onDismissRequest = { /* Force choose back to menu */ }) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xFF06152D),
                    border = BorderStroke(
                        2.5.dp,
                        Brush.linearGradient(
                            if (isLocalWinner) {
                                listOf(Color(0xFFFFD54F), Color(0xFF00E676), Color(0xFFFFD54F))
                            } else {
                                listOf(Color(0xFFFF5252), Color(0xFFFF9800), Color(0xFFFF5252))
                            }
                        )
                    ),
                    shadowElevation = 24.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .testTag("carrom_match_result_dialog")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Exact Match Status Label: "YOU ARE WINNER" vs "OPPONENT WINNER"
                        Text(
                            text = if (isLocalWinner) "YOU ARE WINNER" else "OPPONENT WINNER",
                            color = if (isLocalWinner) Color(0xFFFFEA00) else Color(0xFFFF6E6E),
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            letterSpacing = 0.8.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("carrom_result_headline")
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isLocalWinner) {
                                if (language == AppLanguage.BENGALI) "অভিনন্দন! আপনি ক্যারাম ম্যাচ জিতেছেন!" else "Congratulations! $winnerDisplayName dominated the board!"
                            } else {
                                if (language == AppLanguage.BENGALI) "$winnerDisplayName এই ম্যাচে জয়ী হয়েছেন!" else "$winnerDisplayName won the match!"
                            },
                            color = Color(0xFF80D8FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Two-Player Logo + Center Coin Pool Arena with "WINNER" Badge Above Winning Avatar
                        CoinMatchResultArena(
                            leftName = player1Name,
                            rightName = effectivePlayer2Name,
                            leftIsWinner = (winnerPlayerId == 1),
                            rightIsWinner = (winnerPlayerId == 2),
                            leftUseNasidulAvatar = player1Name.contains("Mistry", ignoreCase = true),
                            rightUseNasidulAvatar = (isTournament && isOpponentBot) || effectivePlayer2Name.contains("Mistry", ignoreCase = true),
                            leftColor = Color(0xFF132A52),
                            rightColor = Color(0xFF3E1F0F),
                            leftEmoji = "👑",
                            rightEmoji = if (isOpponentBot) "🤖" else "👤",
                            totalCoinPool = prizePool,
                            entryFeePerPlayer = effectiveStake,
                            localIsLeft = (localPlayerId == 1),
                            isBengali = (language == AppLanguage.BENGALI)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Wallet Credit & Final Score Card
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isLocalWinner) Color(0xFF0C2E1E) else Color(0xFF0E2850),
                            border = BorderStroke(1.2.dp, if (isLocalWinner) Color(0xFF00E676) else Color(0xFFFFD54F)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("carrom_result_wallet_credit_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (isLocalWinner && prizePool > 0) {
                                    Text(
                                        text = if (language == AppLanguage.BENGALI) "🪙 +$formattedPool কয়েন আপনার ওয়ালেটে যোগ হয়েছে!" else "🪙 +$formattedPool Coins Credited to Your Wallet!",
                                        color = Color(0xFF00E676),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.5.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                }
                                Text(
                                    text = if (liveDiamonds > 0) {
                                        "Wallet Balance: 🪙 $formattedWalletCoins • 💎 $liveDiamonds"
                                    } else {
                                        "Wallet Balance: 🪙 $formattedWalletCoins Coins"
                                    },
                                    color = Color(0xFFFFEA00),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "$player1Name: $p1Score • $effectivePlayer2Name: $p2Score",
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons: Return to Lobby & Rematch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    if (isOnlinePeerMatch) OnlineMultiplayerManager.leaveRoom()
                                    onBackToMenu()
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.2.dp, Color(0xFFFFD600)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("carrom_return_to_lobby_btn")
                            ) {
                                Text(
                                    text = if (language == AppLanguage.BENGALI) "লবি" else "LOBBY",
                                    color = Color(0xFFFFD600),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            Button(
                                onClick = {
                                    if (effectiveStake > 0) {
                                        if (!CoinManager.spendCoins(effectiveStake)) {
                                            carromCannotPlayMessage = if (language == AppLanguage.BENGALI) {
                                                "রিম্যাচের জন্য পর্যাপ্ত কয়েন নেই ($effectiveStake কয়েন প্রয়োজন)।"
                                            } else {
                                                "Insufficient coins for rematch ($effectiveStake coins required)."
                                            }
                                            showCarromCannotPlay = true
                                            return@Button
                                        }
                                    }
                                    if (isOpponentBot) {
                                        // Tournament / Online Bot rematch
                                        showCarromRematchWaiting = true
                                    } else if (isOnlinePeerMatch && !activeOnlineRoomCode.isNullOrBlank()) {
                                        // Synchronized 1v1 peer rematch via room
                                        val myName = if (localPlayerId == 1) player1Name else effectivePlayer2Name
                                        OnlineMultiplayerManager.sendRematchRequest(
                                            roomCode = activeOnlineRoomCode,
                                            senderPlayerId = localPlayerId - 1,
                                            senderName = myName
                                        )
                                        showCarromRematchWaiting = true
                                    } else {
                                        // Offline 2-Player match
                                        restartCarromMatch()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("carrom_rematch_btn")
                            ) {
                                Text(
                                    text = if (language == AppLanguage.BENGALI) "রিম্যাচ" else "REMATCH",
                                    color = Color(0xFF02162E),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Incoming Rematch Consent Modal from Remote Friend (1v1 Online)
        if (incomingRematchRequester != null) {
            Dialog(onDismissRequest = { incomingRematchRequester = null }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF06152D),
                    border = BorderStroke(2.dp, Color(0xFF00E676)),
                    shadowElevation = 16.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (language == AppLanguage.BENGALI) "${incomingRematchRequester} রিম্যাচ খেলতে চান!" else "${incomingRematchRequester} wants a Rematch!",
                            color = Color(0xFFFFD54F),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    activeOnlineRoomCode?.let { code ->
                                        OnlineMultiplayerManager.sendRematchResponse(
                                            roomCode = code,
                                            accepted = false,
                                            responderPlayerId = localPlayerId - 1,
                                            responderName = if (localPlayerId == 1) player1Name else effectivePlayer2Name
                                        )
                                    }
                                    incomingRematchRequester = null
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (language == AppLanguage.BENGALI) "না" else "Decline", color = Color(0xFFFF8A80))
                            }
                            Button(
                                onClick = {
                                    activeOnlineRoomCode?.let { code ->
                                        OnlineMultiplayerManager.sendRematchResponse(
                                            roomCode = code,
                                            accepted = true,
                                            responderPlayerId = localPlayerId - 1,
                                            responderName = if (localPlayerId == 1) player1Name else effectivePlayer2Name
                                        )
                                    }
                                    incomingRematchRequester = null
                                    restartCarromMatch()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (language == AppLanguage.BENGALI) "খেলুন" else "Accept", color = Color(0xFF02162E), fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }

        // Carrom Rematch Waiting Dialog
        if (showCarromRematchWaiting) {
            val opponentWaitName = if (isOpponentBot) "Nasidul Mistry" else effectivePlayer2Name
            Dialog(onDismissRequest = {}) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF06152D),
                    border = BorderStroke(2.dp, Color(0xFFFFD54F)),
                    shadowElevation = 16.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFFFD54F),
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (language == AppLanguage.BENGALI) "রিম্যাচ অনুরোধ পাঠানো হয়েছে" else "Rematch Request Sent",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (language == AppLanguage.BENGALI) {
                                "$opponentWaitName-এর সম্মতির অপেক্ষা করা হচ্ছে..."
                            } else {
                                "Waiting for $opponentWaitName to accept..."
                            },
                            color = Color(0xFF80D8FF),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        OutlinedButton(
                            onClick = {
                                showCarromRematchWaiting = false
                                carromCannotPlayMessage = if (language == AppLanguage.BENGALI) {
                                    "রিম্যাচ অনুরোধ বাতিল করা হয়েছে।"
                                } else {
                                    "Rematch request cancelled."
                                }
                                showCarromCannotPlay = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.2.dp, Color(0xFFFF8A80)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Text(
                                text = if (language == AppLanguage.BENGALI) "বাতিল করুন" else "Cancel",
                                color = Color(0xFFFF8A80),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Carrom Cannot Play Dialog
        if (showCarromCannotPlay) {
            Dialog(onDismissRequest = {
                showCarromCannotPlay = false
                if (isOnlinePeerMatch) OnlineMultiplayerManager.leaveRoom()
                onBackToMenu()
            }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF06152D),
                    border = BorderStroke(2.dp, Color(0xFFFF5252)),
                    shadowElevation = 18.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (language == AppLanguage.BENGALI) "খেলা সম্ভব নয়" else "Cannot Play",
                            color = Color(0xFFFF5252),
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = carromCannotPlayMessage.ifBlank {
                                if (language == AppLanguage.BENGALI) "প্রতিপক্ষ রাজি না হওয়ায় খেলা সম্ভব নয়।" else "Opponent declined. Cannot play."
                            },
                            color = Color.White,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                showCarromCannotPlay = false
                                if (isOnlinePeerMatch) OnlineMultiplayerManager.leaveRoom()
                                onBackToMenu()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Text(
                                text = if (language == AppLanguage.BENGALI) "লবিতে ফিরে যান" else "Return to Lobby",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Visual 3D Miniature Carrom Coin Graphic for Player HUD Score Cards
 * Displays a realistic ivory wood piece for white and ebony wood piece for black
 */
@Composable
fun CarromCoinIcon(
    isWhite: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 14.dp,
    visual: CarromSidePieceVisual? = null
) {
    Canvas(modifier = modifier.size(size)) {
        val r = this.size.minDimension / 2f
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        if (r <= 0.5f || !r.isFinite() || !cx.isFinite() || !cy.isFinite()) return@Canvas

        val customColored = (visual as? CarromSidePieceVisual.CustomSet)?.puckSet
            ?.takeIf { it.colorTone == CarromPuckColorTone.COLORED }
        val renderAsWhite = when (visual) {
            is CarromSidePieceVisual.ForcedWhite -> true
            is CarromSidePieceVisual.ForcedBlack -> false
            is CarromSidePieceVisual.CustomSet -> visual.puckSet.colorTone == CarromPuckColorTone.WHITE
            null -> isWhite
        }

        if (customColored != null) {
            val baseColor = customColored.primaryColor
            val accentColor = customColored.accentColor
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(accentColor, baseColor, baseColor.copy(alpha = 0.72f), Color(0xFF0A0F1A)),
                    start = Offset(cx - r, cy - r),
                    end = Offset(cx + r, cy + r)
                ),
                radius = r,
                center = Offset(cx, cy)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accentColor, baseColor, baseColor.copy(alpha = 0.85f)),
                    center = Offset(cx - r * 0.25f, cy - r * 0.25f),
                    radius = r * 1.05f
                ),
                radius = r * 0.86f,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = Color(0xAAFFFFFF),
                radius = r * 0.54f,
                center = Offset(cx, cy),
                style = Stroke(width = 1.0f)
            )
            drawCircle(
                color = Color(0xDDFFFFFF),
                radius = r * 0.2f,
                center = Offset(cx - r * 0.35f, cy - r * 0.35f)
            )
        } else if (renderAsWhite) {
            // 1. 3D Beveled Outer Rim (Turned Ivory Wood)
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFE8DEC8), Color(0xFFBCAAA4), Color(0xFF6D4C41)),
                    start = Offset(cx - r, cy - r),
                    end = Offset(cx + r, cy + r)
                ),
                radius = r,
                center = Offset(cx, cy)
            )
            // 2. Inset Polished Face
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFF7F3E8), Color(0xFFE0D5C1), Color(0xFFBCAAA4)),
                    center = Offset(cx - r * 0.25f, cy - r * 0.25f),
                    radius = r * 1.1f
                ),
                radius = r * 0.88f,
                center = Offset(cx, cy)
            )
            // 3. Concentric Groove Hairline
            drawCircle(
                color = Color(0x668D6E63),
                radius = r * 0.56f,
                center = Offset(cx, cy),
                style = Stroke(width = 1.0f)
            )
            // 4. Center Bullseye Dot
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFD7CCC8)),
                    center = Offset(cx, cy),
                    radius = r * 0.3f
                ),
                radius = r * 0.25f,
                center = Offset(cx, cy)
            )
            // 5. Lacquered Specular Gloss Highlight
            drawCircle(
                color = Color(0xDDFFFFFF),
                radius = r * 0.22f,
                center = Offset(cx - r * 0.35f, cy - r * 0.35f)
            )
        } else {
            // 1. 3D Beveled Outer Rim (Turned Ebony / Obsidian Wood)
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF5D697B), Color(0xFF263238), Color(0xFF141922), Color(0xFF000000)),
                    start = Offset(cx - r, cy - r),
                    end = Offset(cx + r, cy + r)
                ),
                radius = r,
                center = Offset(cx, cy)
            )
            // 2. Inset Polished Face (Deep Obsidian Finish)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF37474F), Color(0xFF212121), Color(0xFF10141A), Color(0xFF000000)),
                    center = Offset(cx - r * 0.25f, cy - r * 0.25f),
                    radius = r * 1.1f
                ),
                radius = r * 0.88f,
                center = Offset(cx, cy)
            )
            // 3. Concentric Groove Hairline (Warm Golden Bronze)
            drawCircle(
                color = Color(0x88FFD54F),
                radius = r * 0.56f,
                center = Offset(cx, cy),
                style = Stroke(width = 1.0f)
            )
            // 4. Center Bullseye Dot
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF455A64), Color(0xFF212121), Color(0xFF000000)),
                    center = Offset(cx, cy),
                    radius = r * 0.3f
                ),
                radius = r * 0.25f,
                center = Offset(cx, cy)
            )
            // 5. Glossy Specular Highlight
            drawCircle(
                color = Color(0xAAFFFFFF),
                radius = r * 0.2f,
                center = Offset(cx - r * 0.35f, cy - r * 0.35f)
            )
        }
    }
}

private fun DrawScope.drawSidePieceVisual(
    px: Float,
    py: Float,
    pr: Float,
    scale: Float,
    visual: CarromSidePieceVisual,
    customBitmap: ImageBitmap?
) {
    if (visual is CarromSidePieceVisual.CustomSet) {
        val puckSpan = (pr * 2.14f).roundToInt()
        val drewCustomPuck = if (customBitmap != null && puckSpan > 0) {
            try {
                drawImage(
                    image = customBitmap,
                    dstOffset = IntOffset((px - puckSpan * 0.5f).roundToInt(), (py - puckSpan * 0.5f).roundToInt()),
                    dstSize = IntSize(puckSpan, puckSpan)
                )
                true
            } catch (_: Throwable) {
                false
            }
        } else false

        if (drewCustomPuck) {
            drawCircle(
                color = visual.puckSet.accentColor.copy(alpha = 0.25f),
                radius = pr * 0.99f,
                center = Offset(px, py),
                style = Stroke(width = 1.0f * scale)
            )
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xCCFFFFFF), Color(0x33FFFFFF), Color.Transparent),
                    center = Offset(px - pr * 0.35f, py - pr * 0.35f),
                    radius = pr * 0.42f
                ),
                topLeft = Offset(px - pr * 0.65f, py - pr * 0.65f),
                size = Size(pr * 0.58f, pr * 0.38f)
            )
            return
        }
        if (visual.puckSet.colorTone == CarromPuckColorTone.COLORED) {
            val base = visual.puckSet.primaryColor
            val accent = visual.puckSet.accentColor
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(accent, base, base.copy(alpha = 0.72f), Color(0xFF0A0F1A)),
                    start = Offset(px - pr, py - pr),
                    end = Offset(px + pr, py + pr)
                ),
                radius = pr,
                center = Offset(px, py)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accent, base, base.copy(alpha = 0.85f)),
                    center = Offset(px - pr * 0.28f, py - pr * 0.28f),
                    radius = pr * 1.1f
                ),
                radius = pr * 0.91f,
                center = Offset(px, py)
            )
            drawCircle(
                color = Color(0xAAFFFFFF),
                radius = pr * 0.58f,
                center = Offset(px, py),
                style = Stroke(width = 1.2f * scale)
            )
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xDDFFFFFF), Color(0x44FFFFFF), Color.Transparent),
                    center = Offset(px - pr * 0.36f, py - pr * 0.36f),
                    radius = pr * 0.42f
                ),
                topLeft = Offset(px - pr * 0.65f, py - pr * 0.65f),
                size = Size(pr * 0.58f, pr * 0.38f)
            )
            return
        }
    }

    val drawIvoryWhite = when (visual) {
        is CarromSidePieceVisual.ForcedWhite -> true
        is CarromSidePieceVisual.ForcedBlack -> false
        is CarromSidePieceVisual.CustomSet -> visual.puckSet.colorTone == CarromPuckColorTone.WHITE
    }

    if (drawIvoryWhite) {
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFFFFFFF), Color(0xFFEFE6D8), Color(0xFFD2C5B0), Color(0xFF8A7D69)),
                start = Offset(px - pr, py - pr),
                end = Offset(px + pr, py + pr)
            ),
            radius = pr,
            center = Offset(px, py)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFFFFF), Color(0xFFF9F5EC), Color(0xFFE8DECF), Color(0xFFBEAF97)),
                center = Offset(px - pr * 0.28f, py - pr * 0.28f),
                radius = pr * 1.15f
            ),
            radius = pr * 0.91f,
            center = Offset(px, py)
        )
        drawCircle(
            color = Color(0x668D6E63),
            radius = pr * 0.62f,
            center = Offset(px, py),
            style = Stroke(width = 1.2f * scale)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFFFFF), Color(0xFFD5C8B4), Color(0xFFA1917A)),
                center = Offset(px - pr * 0.08f, py - pr * 0.08f),
                radius = pr * 0.35f
            ),
            radius = pr * 0.28f,
            center = Offset(px, py)
        )
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xEEFFFFFF), Color(0x88FFFFFF), Color.Transparent),
                center = Offset(px - pr * 0.36f, py - pr * 0.36f),
                radius = pr * 0.42f
            ),
            topLeft = Offset(px - pr * 0.65f, py - pr * 0.65f),
            size = Size(pr * 0.58f, pr * 0.38f)
        )
    } else {
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF5A6678), Color(0xFF2B3340), Color(0xFF141922), Color(0xFF030508)),
                start = Offset(px - pr, py - pr),
                end = Offset(px + pr, py + pr)
            ),
            radius = pr,
            center = Offset(px, py)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF3B4657), Color(0xFF1F2633), Color(0xFF11151D), Color(0xFF05070A)),
                center = Offset(px - pr * 0.28f, py - pr * 0.28f),
                radius = pr * 1.15f
            ),
            radius = pr * 0.91f,
            center = Offset(px, py)
        )
        drawCircle(
            color = Color(0x88000000),
            radius = pr * 0.91f,
            center = Offset(px + 0.8f * scale, py + 1f * scale),
            style = Stroke(width = 1.4f * scale)
        )
        drawCircle(
            color = Color(0x99000000),
            radius = pr * 0.62f,
            center = Offset(px + 0.5f * scale, py + 0.6f * scale),
            style = Stroke(width = 1.8f * scale)
        )
        drawCircle(
            color = Color(0x5594A3B8),
            radius = pr * 0.62f,
            center = Offset(px - 0.5f * scale, py - 0.5f * scale),
            style = Stroke(width = 1.0f * scale)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF3F4B5E), Color(0xFF1A212D), Color(0xFF080B10)),
                center = Offset(px - pr * 0.08f, py - pr * 0.08f),
                radius = pr * 0.35f
            ),
            radius = pr * 0.28f,
            center = Offset(px, py)
        )
        drawCircle(
            color = Color(0x6694A3B8),
            radius = pr * 0.28f,
            center = Offset(px, py),
            style = Stroke(width = 0.9f * scale)
        )
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xDDFFFFFF), Color(0x5594A3B8), Color.Transparent),
                center = Offset(px - pr * 0.36f, py - pr * 0.36f),
                radius = pr * 0.42f
            ),
            topLeft = Offset(px - pr * 0.65f, py - pr * 0.65f),
            size = Size(pr * 0.58f, pr * 0.38f)
        )
        drawArc(
            color = Color(0xAAFFFFFF),
            startAngle = 200f,
            sweepAngle = 75f,
            useCenter = false,
            topLeft = Offset(px - pr * 0.94f, py - pr * 0.94f),
            size = Size(pr * 1.88f, pr * 1.88f),
            style = Stroke(width = 1.5f * scale, cap = StrokeCap.Round)
        )
    }
}

/**
 * Professional Player Profile Box with Border-Only Animated Glowing Turn Timer Progress
 * Seamless color transition: Green (>15s) -> Yellow (halfway mark ~15s) -> Red (final <=5s)
 */
@Composable
fun CarromPlayerProfileBox(
    isCurrentTurn: Boolean,
    turnProgress: Float,
    backgroundColor: Color,
    inactiveBorderColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (isCurrentTurn) turnProgress else 0f,
        animationSpec = tween(durationMillis = 350, easing = LinearEasing),
        label = "boxTimerProgress"
    )

    // Dynamic color transition along outer border:
    // Starting with Green, shifting to Yellow at the halfway mark, and turning Red in the final seconds
    val timerColor = when {
        animatedProgress > 0.5f -> {
            val factor = ((1f - animatedProgress) / 0.5f).coerceIn(0f, 1f)
            lerp(Color(0xFF00E676), Color(0xFFFFD700), factor)
        }
        animatedProgress > 0.17f -> {
            val factor = ((0.5f - animatedProgress) / 0.33f).coerceIn(0f, 1f)
            lerp(Color(0xFFFFD700), Color(0xFFFF3D00), factor)
        }
        else -> {
            Color(0xFFFF1744)
        }
    }

    // Glowing pulsation in final 5 seconds before time expires
    val isFinalSeconds = isCurrentTurn && animatedProgress <= 0.17f && animatedProgress > 0f
    val pulseTransition = rememberInfiniteTransition(label = "timerPulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = if (isFinalSeconds) 0.35f else 0.45f,
        targetValue = if (isFinalSeconds) 0.90f else 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isCurrentTurn) 8.dp else 2.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = if (isCurrentTurn) timerColor else Color.Black,
                spotColor = if (isCurrentTurn) timerColor else Color.Black
            )
            .background(backgroundColor, RoundedCornerShape(14.dp))
            .drawWithContent {
                drawContent()
                try {
                val cornerRadiusPx = 14.dp.toPx()
                val strokeWidthPx = if (isCurrentTurn) 2.5.dp.toPx() else 1.2.dp.toPx()
                val glowWidthPx = 5.5.dp.toPx()
                val halfStroke = strokeWidthPx / 2f
                if (size.width <= strokeWidthPx * 2f || size.height <= strokeWidthPx * 2f) {
                    return@drawWithContent
                }

                val rect = Rect(
                    left = halfStroke,
                    top = halfStroke,
                    right = size.width - halfStroke,
                    bottom = size.height - halfStroke
                )
                val safeCornerRadius = (cornerRadiusPx - halfStroke).coerceAtLeast(1f)
                val rr = RoundRect(
                    rect = rect,
                    cornerRadius = CornerRadius(safeCornerRadius, safeCornerRadius)
                )

                val fullPath = Path().apply {
                    addRoundRect(rr)
                }

                if (!isCurrentTurn) {
                    // Static subtle inactive border
                    drawPath(
                        path = fullPath,
                        color = inactiveBorderColor,
                        style = Stroke(width = strokeWidthPx)
                    )
                } else {
                    // 1. Subtle background dim track along the full perimeter
                    drawPath(
                        path = fullPath,
                        color = timerColor.copy(alpha = 0.20f),
                        style = Stroke(width = strokeWidthPx)
                    )

                    // 2. Extract and draw the animated progress segment along the border
                    val pathMeasure = PathMeasure()
                    pathMeasure.setPath(fullPath, forceClosed = true)
                    val totalLen = pathMeasure.length

                    if (animatedProgress > 0.005f && totalLen > 0f) {
                        val segmentPath = Path()
                        pathMeasure.getSegment(
                            startDistance = 0f,
                            stopDistance = (totalLen * animatedProgress).coerceIn(0f, totalLen),
                            destination = segmentPath,
                            startWithMoveTo = true
                        )

                        // Outer luminous glowing halo
                        drawPath(
                            path = segmentPath,
                            color = timerColor.copy(alpha = pulseAlpha),
                            style = Stroke(width = glowWidthPx, cap = StrokeCap.Round)
                        )

                        // Crisp solid progress border
                        drawPath(
                            path = segmentPath,
                            color = timerColor,
                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                        )
                    }
                }
                } catch (_: Throwable) {}
            }
            .clip(RoundedCornerShape(14.dp))
    ) {
        content()
    }
}

/**
 * Isolated Status Bar to eliminate performance lag and screen-wide recompositions during aiming drag
 */
@Composable
fun CarromStatusBar(
    currentTurnPlayer: Int,
    turnTimeRemaining: Int = 30,
    isAiming: Boolean,
    isPositioning: Boolean,
    statusMessage: String,
    aimPowerPercent: () -> Int,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (currentTurnPlayer == 1) Color(0xFF072146) else Color(0xFF2E1508),
        border = BorderStroke(1.dp, if (currentTurnPlayer == 1) Color(0xFF00E5FF) else Color(0xFFFF9100)),
        modifier = modifier
    ) {
        val text = if (isAiming) {
            "RELEASE TO SHOOT • POWER ${aimPowerPercent()}%"
        } else if (isPositioning) {
            "SLIDE BASELINE • PULL TO SHOOT"
        } else {
            statusMessage
        }
        Text(
            text = text,
            color = if (currentTurnPlayer == 1) Color(0xFF80D8FF) else Color(0xFFFFD54F),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

/**
 * Smooth, precise horizontal baseline slider for placing the striker
 */
@Composable
fun CarromBaselineSlider(
    strikerX: Float,
    minX: Float,
    maxX: Float,
    isFoul: Boolean,
    enabled: Boolean,
    onPositionChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF071428),
        border = BorderStroke(
            1.5.dp,
            if (isFoul) Brush.horizontalGradient(listOf(Color(0xFFFF1744), Color(0xFFFFD700), Color(0xFFFF1744)))
            else Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFFFFD54F), Color(0xFF00E5FF)))
        ),
        shadowElevation = 8.dp,
        modifier = modifier.testTag("carrom_baseline_slider")
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0B203E), Color(0xFF061224))
                    )
                )
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header / Warning Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isFoul) "⚠️" else "🎯",
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isFoul) "COIN ON BASELINE: MOVE TO CLEAR SPOT" else "STRIKER BASELINE POSITION",
                        color = if (isFoul) Color(0xFFFF5252) else Color(0xFFFFD54F),
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                val baselineSpan = (maxX - minX).coerceAtLeast(1f)
                val baselinePercent = (((strikerX - minX) / baselineSpan) * 100f).let {
                    if (it.isFinite()) it.coerceIn(0f, 100f).toInt() else 50
                }
                Text(
                    text = "$baselinePercent%",
                    color = Color(0xFF80D8FF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Slider with fine nudge buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left fine-nudge button
                Surface(
                    onClick = {
                        val newX = (strikerX - 18f).coerceIn(minX, maxX)
                        onPositionChange(newX)
                    },
                    enabled = enabled,
                    shape = RoundedCornerShape(10.dp),
                    color = if (enabled) Color(0xFF132F54) else Color(0xFF0A182B),
                    border = BorderStroke(1.2.dp, if (enabled) Color(0xFF00E5FF) else Color(0xFF1E3A5F)),
                    modifier = Modifier
                        .size(width = 44.dp, height = 40.dp)
                        .testTag("carrom_nudge_left_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("◀", color = if (enabled) Color.White else Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Slider(
                    value = strikerX.coerceIn(minX, maxX),
                    onValueChange = { onPositionChange(it) },
                    valueRange = minX..maxX,
                    enabled = enabled,
                    colors = SliderDefaults.colors(
                        thumbColor = if (isFoul) Color(0xFFFF1744) else Color(0xFFFFD54F),
                        activeTrackColor = if (isFoul) Color(0xFFFF5252) else Color(0xFF00E5FF),
                        inactiveTrackColor = Color(0xFF102847)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .testTag("carrom_baseline_slider")
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Right fine-nudge button
                Surface(
                    onClick = {
                        val newX = (strikerX + 18f).coerceIn(minX, maxX)
                        onPositionChange(newX)
                    },
                    enabled = enabled,
                    shape = RoundedCornerShape(10.dp),
                    color = if (enabled) Color(0xFF132F54) else Color(0xFF0A182B),
                    border = BorderStroke(1.2.dp, if (enabled) Color(0xFF00E5FF) else Color(0xFF1E3A5F)),
                    modifier = Modifier
                        .size(width = 44.dp, height = 40.dp)
                        .testTag("carrom_nudge_right_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("▶", color = if (enabled) Color.White else Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Comprehensive Official Carrom Tournament Rules & Guide Dialog
 */
@Composable
fun CarromRulesDialog(
    carromMode: CarromGameMode = CarromGameMode.STANDARD,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF07152B),
            border = BorderStroke(
                2.dp,
                Brush.linearGradient(
                    listOf(Color(0xFFFFD54F), Color(0xFF00E5FF), Color(0xFFFFD54F))
                )
            ),
            shadowElevation = 24.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(vertical = 16.dp)
                .testTag("carrom_rules_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF091A36), Color(0xFF040C1A))
                        )
                    )
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD50000))
                                .border(1.5.dp, Color(0xFFFFD54F), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👑", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = carromMode.titleEn,
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            Text(
                                text = carromMode.badgeEn,
                                color = Color(0xFF80D8FF),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF))
                            .testTag("close_carrom_rules_dialog")
                    ) {
                        Text("✕", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFF1E3A5F), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Rules Body
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Coin Assignment & Point Values
                    item {
                        val (title1, body1) = when (carromMode) {
                            CarromGameMode.STANDARD -> "1. 19 COINS (9 WHITE + 9 BLACK + 1 RED)" to
                                "• Player 1 plays 9 White Coins (10 pts each) and Player 2 plays 9 Black Coins (10 pts each).\n• 1 Crimson Red Queen (50 pts) sits in the center."
                            CarromGameMode.NO_QUEEN -> "1. 18 COINS (9 WHITE vs 9 BLACK)" to
                                "• Player 1 is assigned 9 White Coins and Player 2 is assigned 9 Black Coins.\n• There is NO Red/Queen coin — pure 9 vs 9 showdown."
                            CarromGameMode.FOGGY -> "1. FOGGY CARROM (19 COINS • 320 TOTAL PTS)" to
                                "• 9 White Coins = 20 pts each (180 pts total).\n• 9 Black Coins = 10 pts each (90 pts total).\n• 1 Red Coin = 50 pts (Total possible = 320 pts)."
                        }
                        CarromRuleSectionCard(
                            icon = "⚪⚫",
                            title = title1,
                            accentColor = Color(0xFF00E5FF),
                            body = body1
                        )
                    }

                    // 2. Winning Objective & Queen Cover Rule
                    item {
                        val (title2, body2) = when (carromMode) {
                            CarromGameMode.STANDARD -> "2. QUEEN COVER & WINNING RULE" to
                                "• Red Queen must be pocketed AND covered by pocketing a White or Black coin on the same or immediate next shot.\n• If uncovered, Red returns to center with 0 pts. Clear all 9 assigned coins after Queen is covered to win!"
                            CarromGameMode.NO_QUEEN -> "2. FIRST TO CLEAR ALL 9 COINS WINS" to
                                "• Whichever side pockets all 9 of its assigned coins first wins the match IMMEDIATELY once moving pieces settle!\n• No Queen cover required."
                            CarromGameMode.FOGGY -> "2. RED COVER RULE & 160 POINTS TO WIN" to
                                "• Red (50 pts) requires a White or Black cover piece to be valid; otherwise Red returns to center with 0 unclaimed pts.\n• First player to reach 160 points (or finish with >= 160 / highest once cleared) immediately wins!"
                        }
                        CarromRuleSectionCard(
                            icon = "🏆",
                            title = title2,
                            accentColor = Color(0xFFFFD54F),
                            body = body2
                        )
                    }

                    // 3. Baseline Placement & Coin Overlap Foul
                    item {
                        CarromRuleSectionCard(
                            icon = "📏",
                            title = "3. BASELINE PLACEMENT & OVERLAP FOUL",
                            accentColor = Color(0xFFFFEA00),
                            body = "• The striker must be placed strictly on your baseline touching both lines or covering the baseline circle.\n• Baseline Coin Collision Foul: You CANNOT place the striker overlapping any coin sitting on the baseline! The game will restrict shooting until you slide the striker to a clear legal spot."
                        )
                    }

                    // 4. Consecutive Turns & Extra Shots
                    item {
                        CarromRuleSectionCard(
                            icon = "🎯",
                            title = "4. EXTRA SHOT BONUS",
                            accentColor = Color(0xFF00E676),
                            body = "• Every time you successfully pocket one of your assigned coins, you earn an immediate extra shot!\n• Failing to pocket your own coin passes the turn to your opponent."
                        )
                    }

                    // 5. Penalties & Fouls
                    item {
                        CarromRuleSectionCard(
                            icon = "⚠️",
                            title = "5. FOULS & PENALTIES",
                            accentColor = Color(0xFFFF5252),
                            body = "• Striker Pocketing: If your striker enters a pocket, it is a foul! A 10-point penalty applies and a previously potted coin is returned to the center.\n• Opponent Coin Pocketed: Pocketing an opponent's coin gives points to the opponent and does NOT grant you an extra shot!"
                        )
                    }

                    // 6. 360° Aiming & Powder Friction
                    item {
                        CarromRuleSectionCard(
                            icon = "✨",
                            title = "6. 360° PRO SLINGSHOT AIMING",
                            accentColor = Color(0xFFE040FB),
                            body = "• Pull back on the striker and drag in any direction to aim smoothly across 360°.\n• Straight rim guide stops cleanly at the first target coin or inner cushion with authentic powder slide physics."
                        )
                    }

                    // 7. 30-Second Shot Turn Timer Limit
                    item {
                        CarromRuleSectionCard(
                            icon = "⏱️",
                            title = "7. 30-SECOND SHOT TIMER LIMIT",
                            accentColor = Color(0xFFFFD700),
                            body = "• Strict 30-second turn timer for every shot across all modes (Online, Offline & Tournaments).\n• If you fail to release your shot within 30 seconds, your turn automatically expires and passes immediately to your opponent!"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Dismiss Button
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = "GOT IT • PLAY NOW! 🎯",
                        color = Color(0xFF040E1E),
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

/**
 * Reusable Rule Section Card
 */
@Composable
private fun CarromRuleSectionCard(
    icon: String,
    title: String,
    accentColor: Color,
    body: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0E223D),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = accentColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.5.sp,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = body,
                color = Color(0xFFECEFF1),
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}
