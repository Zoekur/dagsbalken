package com.dagsbalken.core.dagskompisen

/**
 * Small, serializable-friendly weather context that Dagskompisen uses to decide
 * outfits and overlays. Keep it minimal and deterministic for unit testing.
 */
data class WeatherContext(
    val condition: WeatherCondition,
    val temperatureC: Int? = null,
    val windSpeedMs: Int? = null
)

/**
 * Mapping functions that return the drawable resource *names* (without extension).
 * Returning names keeps the core module Android-free and easily unit-testable.
 */
fun WeatherContext.toOutfitName(): String = when (condition) {
    WeatherCondition.RAIN, WeatherCondition.STORM -> "outfit_rain"
    WeatherCondition.SNOW -> "outfit_snow"
    WeatherCondition.WINDY -> "outfit_windy"
    WeatherCondition.HOT -> "outfit_hot"
    else -> "" // no outfit overlay (use base character only)
}

fun WeatherContext.toOverlayName(): String = when (condition) {
    WeatherCondition.SUN -> "overlay_sun"
    WeatherCondition.CLOUDY -> "overlay_cloudy"
    WeatherCondition.RAIN -> "overlay_rain"
    WeatherCondition.STORM -> "overlay_storm"
    WeatherCondition.SNOW -> "overlay_snow"
    WeatherCondition.WINDY -> "overlay_windy"
    WeatherCondition.FOG -> "overlay_fog"
    WeatherCondition.HOT -> "overlay_hot"
}

