package com.example.helatrack.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// 1. Set up the Dark Mode version (Deep Navy backgrounds)
private val DarkColorScheme = darkColorScheme(
    primary = HelaNavy,            // Keep buttons Navy Blue even in dark mode
    onPrimary = White,             // Ensure text on buttons is White
    secondary = HelaNavyLight,
    tertiary = HelaAccent,
    background = Color(0xFF000814), // A very deep, near-black navy
    surface = HelaNavyLight,       // Cards should be slightly lighter than background
    onBackground = White,          // Text on background should be white
    onSurface = White
)

// 2. Set up the Light Mode version (The standard "Clean" look)
private val LightColorScheme = lightColorScheme(
    primary = HelaNavy,            // Your Brand Blue for buttons/bars
    secondary = HelaNavyLight,     // For accents
    tertiary = HelaAccent,         // Success Green 🫰🏾
    background = OffWhite,         // Slightly gray/white for contrast
    surface = Color.White,
    onPrimary = Color.White,       // White text on blue buttons
    onBackground = HelaNavy,
    onSurface = HelaNavy
)

@Composable
fun HelaTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set dynamicColor to false to FORCE your brand colors
    // instead of letting Android 12+ change them to the wallpaper colors.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}