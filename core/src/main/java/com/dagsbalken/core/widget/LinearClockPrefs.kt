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

    // Component visibility
    val SHOW_CLOCK = booleanPreferencesKey("linear_clock_show_clock")
    val SHOW_EVENTS = booleanPreferencesKey("linear_clock_show_events") // Toggles event rendering on timeline
    val SHOW_WEATHER = booleanPreferencesKey("linear_clock_show_weather")
    val SHOW_CLOTHING = booleanPreferencesKey("linear_clock_show_clothing")

    // Sizes/Layout
    val CLOCK_SIZE = stringPreferencesKey("linear_clock_size_mode")

    // Default values
    const val DEF_FONT: String = "sans-serif"
    const val DEF_SCALE: Float = 1.0f
    const val DEF_BG: Int = 0xFFFFFFFF.toInt()
    const val DEF_TEXT: Int = 0xFF000000.toInt()
    const val DEF_ACCENT: Int = 0xFF3B82F6.toInt()
    const val DEF_HOURS_TO_SHOW: Int = 24

    const val DEF_SHOW_CLOCK: Boolean = true
    const val DEF_SHOW_EVENTS: Boolean = true
    const val DEF_SHOW_WEATHER: Boolean = true
    const val DEF_SHOW_CLOTHING: Boolean = false

    // Size Constants
    const val SIZE_4x1 = "4x1"
    const val SIZE_4x2 = "4x2"
    const val SIZE_2x1 = "2x1"
    const val DEF_CLOCK_SIZE = SIZE_4x1
}
