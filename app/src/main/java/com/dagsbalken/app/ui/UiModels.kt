package com.dagsbalken.app.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.dagsbalken.core.data.CustomBlock

/**
 * Immutable wrapper for CustomBlock to make it stable in Compose.
 * This allows LazyColumn/Lists to skip recomposition of items that haven't changed.
 */
@Immutable
data class UiCustomBlock(val block: CustomBlock)

// Shared Color Options to prevent allocation on every recomposition
val TIMER_COLOR_OPTIONS = listOf(
    Color.Blue to "Blue",
    Color.Red to "Red",
    Color.Green to "Green",
    Color.Yellow to "Yellow",
    Color.Magenta to "Magenta",
    Color.Cyan to "Cyan",
    Color(0xFFFFA500) to "Orange",
    Color(0xFF800080) to "Purple",
    Color(0xFF008080) to "Teal"
)

val AOD_COLOR_OPTIONS = TIMER_COLOR_OPTIONS + (Color.White to "White")
