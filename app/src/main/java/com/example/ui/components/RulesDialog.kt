package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun RulesDialog(
    isBengali: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Gavel,
                            contentDescription = "Rules",
                            tint = Color(0xFF1E88E5),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBengali) "লুডু খেলার নিয়মাবলি" else "Ludo Rules & Guide",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF263238),
                                fontSize = 18.sp
                            )
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color(0xFF78909C))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Rules List
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RuleItem(
                        icon = Icons.Filled.Casino,
                        title = if (isBengali) "১. ঘর থেকে গুটি বের করা" else "1. Moving Out from Yard",
                        desc = if (isBengali) "ছক্কায় ৬ (Six) পড়লে ঘর (Yard) থেকে একটি গুটি বোর্ডে নামানো যায়।"
                        else "You must roll a 6 to move a pawn out of the yard onto your starting square.",
                        iconBg = Color(0xFFE3F2FD),
                        iconTint = Color(0xFF1976D2)
                    )

                    RuleItem(
                        icon = Icons.Filled.Star,
                        title = if (isBengali) "২. বোনাস চাল পাওয়া" else "2. Bonus Rolls",
                        desc = if (isBengali) "ছক্কা (৬) পড়লে, বিপক্ষের গুটি কাটলে বা নিজের গুটি পাকা (Home Goal) করলে অতিরিক্ত বোনাস চাল পাবেন।"
                        else "Rolling a 6, capturing an opponent token, or reaching the home goal grants a bonus roll!",
                        iconBg = Color(0xFFFFF9C4),
                        iconTint = Color(0xFFF57F17)
                    )

                    RuleItem(
                        icon = Icons.Filled.Shield,
                        title = if (isBengali) "৩. নিরাপদ ঘর (Safe Zones)" else "3. Safe Zones (Stars)",
                        desc = if (isBengali) "স্টার (★) চিহ্নিত ৮টি নিরাপদ ঘরে থাকা গুটি বিপক্ষ কাটতে পারবে না।"
                        else "Tokens positioned on the 8 Star (★) marked cells cannot be captured by opponents.",
                        iconBg = Color(0xFFE8F5E9),
                        iconTint = Color(0xFF388E3C)
                    )

                    RuleItem(
                        icon = Icons.Filled.EmojiEvents,
                        title = if (isBengali) "৪. জয়ী হওয়া (Winning)" else "4. Victory Condition",
                        desc = if (isBengali) "যার ৪টি গুটিই সবার আগে সেন্ট্রাল হোমে প্রবেশ করবে তিনিই ১ম স্থান অর্জন করবেন।"
                        else "The first player to guide all 4 tokens into the central home triangle wins the match!",
                        iconBg = Color(0xFFFFEBEE),
                        iconTint = Color(0xFFD32F2F)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = if (isBengali) "বুঝেছি (Got It)" else "Got It!", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun RuleItem(
    icon: ImageVector,
    title: String,
    desc: String,
    iconBg: Color,
    iconTint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = iconBg,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF263238)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                fontSize = 12.sp,
                color = Color(0xFF546E7A),
                lineHeight = 16.sp
            )
        }
    }
}
