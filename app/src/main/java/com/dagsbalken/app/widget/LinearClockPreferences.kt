package com.dagsbalken.app.widget

import androidx.datastore.preferences.core.*

object LinearClockPrefs {
    val FONT_FAMILY = stringPreferencesKey("font_family")
    val FONT_SCALE  = floatPreferencesKey("font_scale")
    val COLOR_BG    = intPreferencesKey("color_bg")
    val COLOR_TEXT  = intPreferencesKey("color_text")
    val COLOR_ACCENT= intPreferencesKey("color_accent")
    val HOURS_TO_SHOW = intPreferencesKey("hours_to_show")

    const val DEF_FONT = "system"
    const val DEF_SCALE = 1.0f
    const val DEF_BG = 0xFFFFFFFF.toInt()
    const val DEF_TEXT = 0xFF111111.toInt()
    const val DEF_ACCENT = 0xFFB7EA27.toInt()
    const val DEF_HOURS_TO_SHOW = 6
}