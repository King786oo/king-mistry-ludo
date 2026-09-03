package com.example.model

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

enum class LudoColor(
    val titleEn: String,
    val titleBn: String,
    val primaryColor: Color,      // Deep rich main color
    val secondaryColor: Color,    // Rich vibrant gradient top
    val deepColor: Color,         // Deep dark tone for shadows / borders
    val lightColor: Color,        // Soft tint for backgrounds
    val darkColor: Color,         // High contrast text / accent
    val glowColor: Color,         // Bright glowing tone
    val startTrackIndex: Int      // Index in the 52-cell track
) {
    RED(
        titleEn = "Red",
        titleBn = "লাল (Red)",
        primaryColor = Color(0xFFE52521),      // Classic Vivid Red
        secondaryColor = Color(0xFFFF5252),    // Bright Ruby Red
        deepColor = Color(0xFFB71C1C),         // Deep Red Shadow
        lightColor = Color(0xFFFFEBEE),        // Soft Red Tint
        darkColor = Color(0xFF5D0000),         // Dark Burgundy
        glowColor = Color(0xFFFF1744),         // Bright Crimson Glow
        startTrackIndex = 0
    ),
    GREEN(
        titleEn = "Green",
        titleBn = "সবুজ (Green)",
        primaryColor = Color(0xFF0E9D32),      // Classic Vivid Green
        secondaryColor = Color(0xFF00E676),    // Bright Emerald Green
        deepColor = Color(0xFF1B5E20),         // Deep Forest Green
        lightColor = Color(0xFFE8F5E9),        // Soft Green Tint
        darkColor = Color(0xFF003300),         // Dark Evergreen
        glowColor = Color(0xFF00E676),         // Bright Emerald Glow
        startTrackIndex = 13
    ),
    YELLOW(
        titleEn = "Yellow",
        titleBn = "হলুদ (Yellow)",
        primaryColor = Color(0xFFFFC107),      // Classic Vivid Gold Yellow
        secondaryColor = Color(0xFFFFEE58),    // Bright Lemon Yellow
        deepColor = Color(0xFFFFA000),         // Deep Amber Gold
        lightColor = Color(0xFFFFFDE7),        // Soft Yellow Tint
        darkColor = Color(0xFF4E2600),         // Dark Warm Amber
        glowColor = Color(0xFFFFD600),         // Bright Gold Glow
        startTrackIndex = 26
    ),
    BLUE(
        titleEn = "Blue",
        titleBn = "নীল (Blue)",
        primaryColor = Color(0xFF0078D7),      // Classic Vivid Royal Blue
        secondaryColor = Color(0xFF448AFF),    // Bright Cobalt Blue
        deepColor = Color(0xFF0D47A1),         // Deep Navy Blue
        lightColor = Color(0xFFE3F2FD),        // Soft Blue Tint
        darkColor = Color(0xFF002171),         // Dark Midnight Navy
        glowColor = Color(0xFF2979FF),         // Bright Royal Blue Glow
        startTrackIndex = 39
    );

    fun getDisplayName(isBengali: Boolean): String = if (isBengali) titleBn else titleEn

    fun getGradientBrush(): Brush {
        return Brush.verticalGradient(
            listOf(secondaryColor, primaryColor, deepColor)
        )
    }

    fun getRadialBrush(): Brush {
        return Brush.radialGradient(
            listOf(secondaryColor, primaryColor, deepColor)
        )
    }
}
