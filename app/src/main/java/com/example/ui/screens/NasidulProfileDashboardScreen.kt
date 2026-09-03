package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * Initial Landing Screen matching the user's first uploaded image:
 * Futuristic Enterprise Profile Dashboard of Nasidul Mistry with "GET START" button.
 */
@Composable
fun NasidulProfileDashboardScreen(
    onGetStarted: () -> Unit,
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showNotificationDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "get_start_pulse")
    val buttonGlowPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    val hudPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hud_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F2642),
                        Color(0xFF1B3B5F),
                        Color(0xFF2A557F),
                        Color(0xFF14314E)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Ambient background bokeh / light circles
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0x4038BDF8), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.15f),
                    radius = size.width * 0.55f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0x300284C7), Color.Transparent),
                    center = Offset(size.width * 0.15f, size.height * 0.85f),
                    radius = size.width * 0.65f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Main Futuristic Profile Dashboard Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
                    .shadow(28.dp, RoundedCornerShape(20.dp), spotColor = Color(0x99000000))
                    .border(
                        width = 2.5.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFF67E8F9), // Cyber Cyan highlight
                                Color(0xFF2563EB), // Enterprise Blue
                                Color(0xFF1E3A8A), // Deep Navy
                                Color(0xFF38BDF8)
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF112239) // Brushed Navy Metallic Body
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF1A365D),
                                    Color(0xFF0F2544),
                                    Color(0xFF0C1D36)
                                )
                            )
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Header: Holographic Circle with Profile Icon + "PROFILE DASHBOARD"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF1E3A8A).copy(alpha = 0.8f),
                                        Color(0xFF2563EB).copy(alpha = 0.6f),
                                        Color(0xFF1E3A8A).copy(alpha = 0.8f)
                                    )
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            // Holographic Target Icon
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0284C7).copy(alpha = 0.35f))
                                    .border(1.5.dp, Color(0xFF38BDF8), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = "User Profile",
                                    tint = Color(0xFFE0F2FE),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = "PROFILE DASHBOARD",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 2.sp,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    // 2. Center: Nasidul Mistry's Portrait matching the uploaded screenshot
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = 1.5.dp,
                                color = Color(0xFF2563EB).copy(alpha = 0.8f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        // User Portrait Photograph
                        Image(
                            painter = painterResource(id = R.drawable.img_nasidul_mistry_portrait_1788375269214),
                            contentDescription = "Nasidul Mistry Portrait",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.86f),
                            contentScale = ContentScale.Crop
                        )

                        // Subtle Corner Camera Viewfinder Brackets matching screenshot
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val bracketLen = 18f
                            val strokeW = 2.5f
                            val bracketColor = Color(0xFF60A5FA).copy(alpha = 0.6f)

                            // Top-left viewfinder corner: ┌
                            drawLine(bracketColor, Offset(12f, 12f), Offset(12f + bracketLen, 12f), strokeW)
                            drawLine(bracketColor, Offset(12f, 12f), Offset(12f, 12f + bracketLen), strokeW)

                            // Top-right viewfinder corner: ┐
                            drawLine(bracketColor, Offset(w - 12f, 12f), Offset(w - 12f - bracketLen, 12f), strokeW)
                            drawLine(bracketColor, Offset(w - 12f, 12f), Offset(w - 12f, 12f + bracketLen), strokeW)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 3. Name Banner: Crisp White with Bold Dark Text
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .shadow(4.dp, RoundedCornerShape(4.dp)),
                        shape = RoundedCornerShape(4.dp),
                        color = Color.White
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "NASIDUL MISTRY",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.2.sp,
                                    fontSize = 21.sp,
                                    color = Color(0xFF0F172A)
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 4. Action Button: "GET START" Glowing Enterprise Blue Pill
                    Box(
                        modifier = Modifier
                            .scale(buttonGlowPulse)
                            .padding(horizontal = 24.dp)
                            .fillMaxWidth(0.82f)
                            .shadow(16.dp, RoundedCornerShape(30.dp), spotColor = Color(0xFF0284C7))
                            .clip(RoundedCornerShape(30.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF0284C7), // Bright Cyan Blue
                                        Color(0xFF0D47A1), // Enterprise Deep Blue
                                        Color(0xFF0369A1)  // Rich Blue
                                    )
                                )
                            )
                            .border(
                                width = 2.2.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF7DD3FC),
                                        Color(0xFFE0F2FE),
                                        Color(0xFF38BDF8)
                                    )
                                ),
                                shape = RoundedCornerShape(30.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onGetStarted
                            )
                            .padding(vertical = 14.dp)
                            .testTag("get_start_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "GET START",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                fontSize = 18.sp,
                                color = Color.White
                            ),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 5. Bottom Card Footer: Settings, Share & Notification Icons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Settings",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        val profileContext = androidx.compose.ui.platform.LocalContext.current
                        IconButton(
                            onClick = { shareKingMistryApp(profileContext, isBengali = true) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Share APK",
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        IconButton(
                            onClick = { showNotificationDialog = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "Notifications",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // Notification Modal Dialog
        if (showNotificationDialog) {
            AlertDialog(
                onDismissRequest = { showNotificationDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "বিজ্ঞপ্তি / Notification",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "স্বাগতম নাসিদুল মিস্ত্রি!",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            fontSize = 15.sp
                        )
                        Text(
                            text = "লুডো কিং মিস্ত্রি সম্পূর্ণ প্রস্তুত! আপনি ২ জন বা ৪ জন খেলোয়াড় নিয়ে বটের সাথে অথবা বন্ধুদের সাথে পাস & প্লে মোডে খেলতে পারেন।",
                            fontSize = 14.sp,
                            color = Color(0xFF475569)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showNotificationDialog = false }) {
                        Text("ঠিক আছে / OK", fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                    }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = Color.White
            )
        }
    }
}
