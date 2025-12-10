package com.dagsbalken.core.widget

/**
 * Konfigurationsmodell för widgeten i core-modulen.
 */
data class WidgetConfig(
    val font: String = LinearClockPrefs.DEF_FONT,
    val scale: Float = LinearClockPrefs.DEF_SCALE,
    val backgroundColor: Int = LinearClockPrefs.DEF_BG,
    val textColor: Int = LinearClockPrefs.DEF_TEXT,
    val accentColor: Int = LinearClockPrefs.DEF_ACCENT,
    val hoursToShow: Int = LinearClockPrefs.DEF_HOURS_TO_SHOW,
    val showClock: Boolean = LinearClockPrefs.DEF_SHOW_CLOCK,
    val showEvents: Boolean = LinearClockPrefs.DEF_SHOW_EVENTS,
    val showWeather: Boolean = LinearClockPrefs.DEF_SHOW_WEATHER,
    val showClothing: Boolean = LinearClockPrefs.DEF_SHOW_CLOTHING,
    val clockSize: String = LinearClockPrefs.DEF_CLOCK_SIZE
)
