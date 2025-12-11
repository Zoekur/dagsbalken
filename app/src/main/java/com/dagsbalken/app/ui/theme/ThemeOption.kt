package com.dagsbalken.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

enum class ThemeOption(
    val displayName: String,
    val lightColors: ColorScheme,
    val darkColors: ColorScheme,
    val timelineNightColor: Color,
    val timelineDayColor: Color
) {
    Cold(
        displayName = "Cold Colors",
        lightColors = ColdLightPalette,
        darkColors = ColdDarkPalette,
        timelineNightColor = ColdNight,
        timelineDayColor = ColdDay
    ),
    Warm(
        displayName = "Warm Colors",
        lightColors = WarmLightPalette,
        darkColors = WarmDarkPalette,
        timelineNightColor = WarmNight,
        timelineDayColor = WarmDay
    ),
    ColdHighContrast(
        displayName = "Cold (High Contrast)",
        lightColors = ColdHCLightPalette,
        darkColors = ColdHCDarkPalette,
        timelineNightColor = Color.Black,
        timelineDayColor = ColdHC_Cyan
    ),
    WarmHighContrast(
        displayName = "Warm (High Contrast)",
        lightColors = WarmHCLightPalette,
        darkColors = WarmHCDarkPalette,
        timelineNightColor = Color.Black,
        timelineDayColor = Color.Yellow
    )
}
