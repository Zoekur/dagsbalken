package com.dagsbalken.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val NordicCalmLightPalette = lightColorScheme(
    primary = Color(0xFF4CAF50),
    onPrimary = Color.White,
    secondary = Color(0xFF81C784),
    background = Color(0xFFF5F5F5),
    surface = Color.White,
    onSurface = Color.Black
)

private val NordicCalmDarkPalette = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color.Black,
    secondary = Color(0xFF4CAF50),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White
)

private val SolarDawnLightPalette = lightColorScheme(
    primary = Color(0xFFFF9800),
    onPrimary = Color.Black,
    secondary = Color(0xFFFFB74D),
    background = Color(0xFFFFF3E0),
    surface = Color.White,
    onSurface = Color.Black
)

private val SolarDawnDarkPalette = darkColorScheme(
    primary = Color(0xFFFFB74D),
    onPrimary = Color.Black,
    secondary = Color(0xFFFF9800),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White
)

private data class ThemePalette(val light: ColorScheme, val dark: ColorScheme)

enum class ThemeOption(val lightColors: ColorScheme, val darkColors: ColorScheme) {
    NordicCalm(NordicCalmLightPalette, NordicCalmDarkPalette),
    SolarDawn(SolarDawnLightPalette, SolarDawnDarkPalette)
}

@Composable
fun DagsbalkenTheme(
    themeOption: ThemeOption = ThemeOption.NordicCalm,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
