package com.dagsbalken.app.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

fun blendColors(color1: Color, color2: Color, ratio: Float): Color {
    val color1Argb = color1.toArgb()
    val color2Argb = color2.toArgb()
    val blendedArgb = ColorUtils.blendARGB(color1Argb, color2Argb, ratio)
    return Color(blendedArgb)
}
