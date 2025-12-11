package com.dagsbalken.app.ui.theme

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

// --- Cold (formerly Nordic Calm) ---
val ColdLightPalette = lightColorScheme(
    primary = Color(0xFF4CAF50),
    onPrimary = Color.White,
    secondary = Color(0xFF81C784),
    background = Color(0xFFF5F5F5),
    surface = Color.White,
    onSurface = Color.Black
)

val ColdDarkPalette = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color.Black,
    secondary = Color(0xFF4CAF50),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White
)

// --- Warm (formerly Solar Dawn) ---
val WarmLightPalette = lightColorScheme(
    primary = Color(0xFFFF9800),
    onPrimary = Color.Black,
    secondary = Color(0xFFFFB74D),
    background = Color(0xFFFFF3E0),
    surface = Color.White,
    onSurface = Color.Black
)

val WarmDarkPalette = darkColorScheme(
    primary = Color(0xFFFFB74D),
    onPrimary = Color.Black,
    secondary = Color(0xFFFF9800),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White
)

// --- Cold High Contrast ---
val ColdHCLightPalette = lightColorScheme(
    primary = Color(0xFF0000FF), // Pure Blue
    onPrimary = Color.White,
    secondary = Color(0xFF000080),
    background = Color.White,
    surface = Color.White,
    onSurface = Color.Black
)

val ColdHCDarkPalette = darkColorScheme(
    primary = ColdHC_Cyan, // Bright Cyan
    onPrimary = Color.Black,
    secondary = Color.White,
    background = Color.Black,
    surface = Color.Black,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF222222) // Slightly lighter for contrast
)

// --- Warm High Contrast ---
val WarmHCLightPalette = lightColorScheme(
    primary = Color(0xFFFF0000), // Pure Red
    onPrimary = Color.White,
    secondary = Color(0xFF800000),
    background = Color.White,
    surface = Color.White,
    onSurface = Color.Black
)

val WarmHCDarkPalette = darkColorScheme(
    primary = Color(0xFFFFD700), // Gold/Yellow
    onPrimary = Color.Black,
    secondary = Color.White,
    background = Color.Black,
    surface = Color.Black,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF222222)
)

@Composable
fun DagsbalkenTheme(
    themeOption: ThemeOption = ThemeOption.Cold,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic to enforce our themes
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> themeOption.darkColors
        else -> themeOption.lightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
