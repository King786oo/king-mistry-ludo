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
import androidx.compose.material.icons.filled.Fullscreen
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
import com.example.model.GameMode
import com.example.model.LudoColor

/**
 * Game Lobby / Menu matching the user's second uploaded image:
 * Classic Ludo King Enterprise interface with Guest1234 profile, coins,
 * 3D Ludo logo, and golden framed game buttons.
 */
@Composable
fun LudoMenuScreen(
    isBengali: Boolean,
    onStartGame: (mode: GameMode, playerCount: Int, names: List<String>, botFlags: List<Boolean>) -> Unit,
    onOpenStats: () -> Unit,
    onOpenRules: () -> Unit,
    onToggleLanguage: () -> Unit,
    onBackToProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    var coins by remember { mutableIntStateOf(2500) }
    var showPlayerSelectDialog by remember { mutableStateOf(false) }
    var showShareApkDialog by remember { mutableStateOf(false) }
    var selectedGameMode by remember { mutableStateOf(GameMode.VS_BOT) }
    var playerCount by remember { mutableIntStateOf(2) }

    var p1Name by remember { mutableStateOf("Nasidul Mistry") }
    var p2Name by remember { mutableStateOf("Player 2") }
    var p3Name by remember { mutableStateOf("Player 3") }
    var p4Name by remember { mutableStateOf("Player 4") }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_reward")
    val rewardBounce by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "reward_bounce"
    )

    fun shareAppText() {
        val shareUrl = "https://ais-pre-22degxp5tmj4om7dmfqho7-913901272718.asia-southeast1.run.app"
        val shareText = if (isBengali) {
            "👑 কিং মিস্ত্রি লুডু (King Mistry Ludo) 🎲\n\nআসসালামু আলাইকুম! আমার সাথে সেরা রয়েল লুডু গেমটি খেলুন এবং উপভোগ করুন!\n\n👉 সরাসরি গেম লিংক:\n$shareUrl\n\n💡 মোবাইলে ইনস্টল করতে:\n১. লিংকে ঢুকে থ্রি-ডট (⋮) মেনু থেকে 'Add to Home screen' বা 'Install app' দিন\n২. অথবা সরাসরি APK ফাইল বন্ধুদের সাথে ব্লুটুথ/হোয়াটসঅ্যাপে শেয়ার করে ইনস্টল করুন!"
        } else {
            "👑 King Mistry Ludo 🎲\n\nPlay the ultimate royal Ludo game with me!\n\n👉 Game Link:\n$shareUrl\n\n💡 To install on mobile:\n1. Open link in Chrome & tap (⋮) -> 'Install app' or 'Add to Home screen'\n2. Or share the APK file directly via Bluetooth/WhatsApp!"
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

    fun shareAppApkFile() {
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
                    if (isBengali) "APK ফাইল শেয়ার করুন (হোয়াটসঅ্যাপ / ব্লুটুথ)" else "Share APK File (WhatsApp/Bluetooth)"
                )
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } else {
                shareAppText()
            }
        } catch (e: Exception) {
            shareAppText()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D54A4))
    ) {
        // Authentic Ludo King Blue Dice Pattern Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 140f
            var row = 0
            var y = 0f
            while (y < size.height + step) {
                var x = if (row % 2 == 0) 0f else step / 2f
                while (x < size.width + step) {
                    rotate(degrees = 35f, pivot = Offset(x, y)) {
                        // Background Diamond / Die Tile
                        drawRoundRect(
                            color = Color(0xFF1363BD).copy(alpha = 0.5f),
                            topLeft = Offset(x - 42f, y - 42f),
                            size = Size(84f, 84f),
                            cornerRadius = CornerRadius(16f, 16f)
                        )
                        // Dice pips
                        drawCircle(color = Color(0x33FFFFFF), radius = 6f, center = Offset(x - 20f, y - 20f))
                        drawCircle(color = Color(0x33FFFFFF), radius = 6f, center = Offset(x + 20f, y + 20f))
                        drawCircle(color = Color(0x33FFFFFF), radius = 6f, center = Offset(x, y))
                    }
                    x += step
                }
                y += step * 0.85f
                row++
            }
        }

        // Main Vertical Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. TOP BAR: User Avatar & Name on Left, Coins & "MORE COINS" on Right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Box (Guest1234 / Nasidul Mistry) with Golden Frame & Star Meter
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0B3A75),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFB300)),
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .clickable { onBackToProfile() }
                            .testTag("profile_badge_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            // Avatar with Red Notification Dot
                            Box(modifier = Modifier.size(44.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFE0E0E0),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD54F)),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_nasidul_mistry_portrait_1788375269214),
                                        contentDescription = "User Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                // Red Badge Dot
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE53935))
                                        .border(1.dp, Color.White, CircleShape)
                                        .align(Alignment.TopEnd)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                Text(
                                    text = "Guest1234",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                                // Star Level Bar
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = "Star",
                                        tint = Color(0xFFFFD54F),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(50.dp)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color(0xFF1565C0))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.65f)
                                                .fillMaxSize()
                                                .background(Color(0xFFFFB300))
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Coins Counter & "MORE COINS" Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Coin Display Box
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF0B3A75).copy(alpha = 0.9f),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFB300)),
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🪙", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$coins",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        // "MORE COINS" Golden Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFF9800),
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFE082)),
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .clickable {
                                    coins += 500
                                }
                                .testTag("more_coins_button")
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color(0xFFFFD54F),
                                                Color(0xFFFF9800),
                                                Color(0xFFE65100)
                                            )
                                        )
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isBengali) "কয়েন নিন" else "MORE COINS",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Navigation / Utility Action Row: Settings, Back to Profile, Language, Rules
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Settings Gear Button in Ludo King Blue rounded box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0C3D77),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFB300)),
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .size(42.dp)
                            .clickable { onOpenRules() }
                            .testTag("menu_settings_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Settings",
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Return to Profile Dashboard
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF0C3D77),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF38BDF8)),
                            modifier = Modifier.clickable { onBackToProfile() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Profile",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Language Toggle
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF0C3D77),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFB300)),
                            modifier = Modifier.clickable { onToggleLanguage() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Language, contentDescription = "Lang", tint = Color(0xFFFFD54F), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isBengali) "বাংলা" else "ENG",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Stats
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF0C3D77),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFB300)),
                            modifier = Modifier.clickable { onOpenStats() }
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.QueryStats, contentDescription = "Stats", tint = Color(0xFFFFD54F), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. CENTER: Ludo King 3D Title Graphic / Banner
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(20.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(175.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_ludo_king_title_banner_1788371824667),
                            contentDescription = "Ludo King Title Banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. MAIN GAME BUTTONS (The iconic golden-bordered cards from the screenshot)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // BUTTON 1: ONLINE MULTIPLAYER
                    LudoKingMenuCard(
                        title = if (isBengali) "অনলাইন মাল্টিপ্লেয়ার" else "ONLINE MULTIPLAYER",
                        hasInfoBadge = true,
                        topIllustration = {
                            // Two golden phones with glowing connected dots around globe
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFFB300),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFE082)),
                                    modifier = Modifier.size(42.dp, 64.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(4.dp)
                                            .background(Color(0xFF003875), RoundedCornerShape(4.dp))
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Glowing dots & Globe
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFFFD54F)))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF1565C0))
                                            .border(2.dp, Color(0xFFFFD54F), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Public,
                                            contentDescription = "Globe",
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFFFD54F)))
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFFB300),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFE082)),
                                    modifier = Modifier.size(42.dp, 64.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(4.dp)
                                            .background(Color(0xFF003875), RoundedCornerShape(4.dp))
                                    )
                                }
                            }
                        },
                        onClick = {
                            selectedGameMode = GameMode.PASS_AND_PLAY
                            showPlayerSelectDialog = true
                        },
                        testTag = "online_multiplayer_card"
                    )

                    // BUTTON 2: COMPUTER (VS COMPUTER)
                    LudoKingMenuCard(
                        title = if (isBengali) "বনাম কম্পিউটার" else "COMPUTER",
                        hasInfoBadge = false,
                        topIllustration = {
                            // VS Phone Badge
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFFB300),
                                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFE082)),
                                modifier = Modifier
                                    .size(54.dp, 72.dp)
                                    .shadow(6.dp, RoundedCornerShape(10.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(5.dp)
                                        .background(Color(0xFF052B5B), RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "VS",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        },
                        onClick = {
                            selectedGameMode = GameMode.VS_BOT
                            showPlayerSelectDialog = true
                        },
                        testTag = "vs_computer_card"
                    )

                    // BUTTON 3: PASS & PLAY (Local Multiplayer)
                    LudoKingMenuCard(
                        title = if (isBengali) "পাস অ্যান্ড প্লে" else "PASS & PLAY",
                        hasInfoBadge = false,
                        topIllustration = {
                            // 4 Pawns token icons (Red, Green, Yellow, Blue)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(
                                    Color(0xFFE53935),
                                    Color(0xFF43A047),
                                    Color(0xFFFFB300),
                                    Color(0xFF1E88E5)
                                ).forEach { col ->
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp, 36.dp)
                                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                            .background(col)
                                            .border(1.5.dp, Color.White, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                    )
                                }
                            }
                        },
                        onClick = {
                            selectedGameMode = GameMode.PASS_AND_PLAY
                            showPlayerSelectDialog = true
                        },
                        testTag = "pass_and_play_card"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. BOTTOM BAR: Video Bonus (Play coin stack) on Left, Fullscreen & Share on Right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Video Coins Reward Button
                Box(
                    modifier = Modifier
                        .scale(rewardBounce)
                        .clickable { coins += 300 }
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFB300),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFE082)),
                        shadowElevation = 6.dp,
                        modifier = Modifier.size(50.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0xFFFFD54F), Color(0xFFE65100))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Reward Video",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    // Notification Badge "3"
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD32F2F))
                            .border(1.5.dp, Color.White, CircleShape)
                            .align(Alignment.BottomEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "3",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                // Center: Share & Install APK Button with Friends
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF0A3060),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD54F)),
                    shadowElevation = 6.dp,
                    modifier = Modifier.clickable { showShareApkDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share", tint = Color(0xFFFFD54F), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBengali) "শেয়ার / APK মোড" else "SHARE / APK MOD",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }

                // Fullscreen Icon
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0C3D77),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFB300)),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .size(46.dp)
                        .clickable { /* Toggle visual immersion */ }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Fullscreen,
                            contentDescription = "Fullscreen",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
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
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedGameMode == GameMode.VS_BOT) {
                                if (isBengali) "কম্পিউটার ম্যাচ" else "VS Computer"
                            } else {
                                if (isBengali) "মাল্টিপ্লেয়ার ম্যাচ" else "Multiplayer Match"
                            },
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color(0xFF0D47A1)
                        )

                        IconButton(onClick = { showPlayerSelectDialog = false }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isBengali) "খেলোয়াড় সংখ্যা নির্বাচন করুন" else "Select Player Count",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF37474F)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2, 3, 4 Player selector buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(2, 3, 4).forEach { count ->
                            val isSelected = playerCount == count
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFF0D47A1) else Color(0xFFECEFF1),
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCFD8DC)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clickable { playerCount = count }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$count Players",
                                        color = if (isSelected) Color.White else Color(0xFF37474F),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Player names
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = p1Name,
                            onValueChange = { p1Name = it },
                            label = { Text(if (isBengali) "প্লেয়ার ১ (লাল)" else "Player 1 (Red)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = if (selectedGameMode == GameMode.VS_BOT) "Computer 1" else p2Name,
                            onValueChange = { p2Name = it },
                            label = { Text(if (isBengali) "প্লেয়ার ২" else "Player 2") },
                            enabled = selectedGameMode != GameMode.VS_BOT,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (playerCount >= 3) {
                            OutlinedTextField(
                                value = if (selectedGameMode == GameMode.VS_BOT) "Computer 2" else p3Name,
                                onValueChange = { p3Name = it },
                                label = { Text(if (isBengali) "প্লেয়ার ৩" else "Player 3") },
                                enabled = selectedGameMode != GameMode.VS_BOT,
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (playerCount == 4) {
                            OutlinedTextField(
                                value = if (selectedGameMode == GameMode.VS_BOT) "Computer 3" else p4Name,
                                onValueChange = { p4Name = it },
                                label = { Text(if (isBengali) "প্লেয়ার ৪" else "Player 4") },
                                enabled = selectedGameMode != GameMode.VS_BOT,
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
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
                            val botFlags = when (playerCount) {
                                2 -> listOf(false, selectedGameMode == GameMode.VS_BOT)
                                3 -> listOf(false, selectedGameMode == GameMode.VS_BOT, selectedGameMode == GameMode.VS_BOT)
                                else -> listOf(false, selectedGameMode == GameMode.VS_BOT, selectedGameMode == GameMode.VS_BOT, selectedGameMode == GameMode.VS_BOT)
                            }
                            onStartGame(selectedGameMode, playerCount, names, botFlags)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("dialog_start_match_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBengali) "খেলা শুরু করুন" else "START MATCH",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    // Share & APK Mod Dialog
    if (showShareApkDialog) {
        ShareApkDialog(
            isBengali = isBengali,
            onDismiss = { showShareApkDialog = false },
            onShareApkFile = { shareAppApkFile() },
            onShareTextLink = { shareAppText() }
        )
    }
}

/**
 * Reusable Ludo King Style Card with golden embossed border,
 * deep blue upper illustration compartment, and shiny yellow bottom banner.
 */
@Composable
private fun LudoKingMenuCard(
    title: String,
    hasInfoBadge: Boolean,
    topIllustration: @Composable () -> Unit,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF093770),
        border = androidx.compose.foundation.BorderStroke(
            width = 3.5.dp,
            brush = Brush.verticalGradient(
                listOf(
                    Color(0xFFFFE082),
                    Color(0xFFFFB300),
                    Color(0xFFFF8F00),
                    Color(0xFFFFE082)
                )
            )
        ),
        shadowElevation = 10.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Compartment: Dark Blue with Graphic/Illustration
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF093770),
                                    Color(0xFF03224B)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    topIllustration()
                }

                // Bottom Banner: Bright Yellow/Golden with 3D Embossed Dark Blue Title
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFFFFE082),
                                    Color(0xFFFFB300),
                                    Color(0xFFFFA000)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFFFFE082),
                            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = 1.2.sp,
                            color = Color(0xFF072B59) // Deep Contrast Ludo King Blue
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Info (i) badge in top right corner if applicable
            if (hasInfoBadge) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF6F00))
                        .border(1.5.dp, Color(0xFFFFD54F), CircleShape)
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "i",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

/**
 * Dialog offering comprehensive APK Mod & App Sharing Options:
 * 1. Direct APK Send (WhatsApp / Bluetooth / ShareIt / Telegram)
 * 2. Share Web / Online Link with Install Instructions
 * 3. Step-by-step guidance on Unknown Sources & Installation
 */
@Composable
fun ShareApkDialog(
    isBengali: Boolean,
    onDismiss: () -> Unit,
    onShareApkFile: () -> Unit,
    onShareTextLink: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0A2244),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD54F)),
            shadowElevation = 20.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row with Crown Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFD54F),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.InstallMobile,
                                    contentDescription = "APK Mod",
                                    tint = Color(0xFF0A2244),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isBengali) "APK মোড ও শেয়ার" else "APK MOD & SHARE",
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp
                            )
                            Text(
                                text = if (isBengali) "বন্ধুদের ফোনে ইনস্টল করুন" else "Install on Friends' Phones",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Option 1: Direct APK Send (WhatsApp / Bluetooth / Nearby Share)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF13386B),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF29B6F6)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onShareApkFile()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF00C853),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Download,
                                    contentDescription = "Send APK",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBengali) "সরাসরি APK ফাইল পাঠান" else "Send Direct APK File",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isBengali)
                                    "হোয়াটসঅ্যাপ, ব্লুটুথ বা শেয়ারইটে সরাসরি .apk ফাইল পাঠান যাতে বন্ধু এক ক্লিকে ইনস্টল করতে পারে।"
                                else
                                    "Share the .apk file via WhatsApp, Bluetooth, or Nearby Share for instant 1-click install.",
                                color = Color(0xFFB0BEC5),
                                fontSize = 11.5.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Option 2: Share Web / Online Game Link with Install Guide
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF13386B),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFB300)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onShareTextLink()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFF8F00),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Share,
                                    contentDescription = "Share Link",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBengali) "গেম লিংক ও গাইড শেয়ার" else "Share Link & Guide",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isBengali)
                                    "সরাসরি খেলার লিংক এবং ক্রোম ব্রাউজার থেকে 'Add to Home screen' নির্দেশিকা পাঠান।"
                                else
                                    "Share direct play link with Chrome 'Add to Home screen' instructions.",
                                color = Color(0xFFB0BEC5),
                                fontSize = 11.5.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Instructions Card for Friends' Phones:
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF071933),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E4976)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = "Help",
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBengali) "বন্ধুদের ফোনের জন্য ইনস্টল নিয়ম:" else "How Friends Can Install:",
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isBengali)
                                "১. APK ফাইলটি হোয়াটসঅ্যাপ বা ফাইলে গ্রহণ করার পর ট্যাপ করুন।\n" +
                                "২. 'Install unknown apps' অপশন চাইলে 'Allow' বা চালু করে দিন।\n" +
                                "৩. 'Install Anyway' বাটনে ক্লিক করলেই কিং মিস্ত্রি লুডু গেম ফোনে ইনস্টল হয়ে যাবে!"
                            else
                                "1. Tap the received APK file in WhatsApp or File Manager.\n" +
                                "2. Enable 'Allow from this source' if prompted.\n" +
                                "3. Tap 'Install Anyway' to enjoy King Mistry Ludo!",
                            color = Color(0xFFECEFF1),
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Close Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isBengali) "ঠিক আছে" else "OK, GOT IT",
                        color = Color(0xFF0A2244),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
