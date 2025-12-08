package com.dagsbalken.app.widget

import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object LinearClockPrefs {
    // --- Nycklar för Glance DataStore ---
    val FONT_FAMILY = stringPreferencesKey("font_family")
    val FONT_SCALE = floatPreferencesKey("font_scale")
    val COLOR_BG = intPreferencesKey("color_bg")
    val COLOR_TEXT = intPreferencesKey("color_text")
    val COLOR_ACCENT = intPreferencesKey("color_accent")
    val HOURS_TO_SHOW = intPreferencesKey("hours_to_show")

    // --- Default-värden ---
    const val DEF_FONT = "sans-serif"
    const val DEF_SCALE = 1.0f
    const val DEF_BG = 0xFFFFFFFF.toInt() // Vit
    const val DEF_TEXT = 0xFF000000.toInt() // Svart
    const val DEF_ACCENT = 0xFF888888.toInt() // Grå
    const val DEF_HOURS_TO_SHOW = 4 // Visar +/- 2 timmar
}
