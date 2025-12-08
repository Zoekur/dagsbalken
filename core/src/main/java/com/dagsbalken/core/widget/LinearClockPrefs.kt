package com.dagsbalken.core.widget

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object LinearClockPrefs {
    // Preference keys
    val FONT_FAMILY = stringPreferencesKey("linear_clock_font")
    val FONT_SCALE = floatPreferencesKey("linear_clock_scale")
    val COLOR_BG = intPreferencesKey("linear_clock_bg")
    val COLOR_TEXT = intPreferencesKey("linear_clock_text")
    val COLOR_ACCENT = intPreferencesKey("linear_clock_accent")
    val HOURS_TO_SHOW = intPreferencesKey("linear_clock_hours")

    // Default values
    const val DEF_FONT: String = "sans-serif"
    const val DEF_SCALE: Float = 1.0f
    const val DEF_BG: Int = 0xFFFFFFFF.toInt()
    const val DEF_TEXT: Int = 0xFF000000.toInt()
    const val DEF_ACCENT: Int = 0xFF3B82F6.toInt()
    const val DEF_HOURS_TO_SHOW: Int = 24
}

