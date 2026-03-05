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

// Lager-på-lager outfit-logik
fun WeatherContext.toOutfitDescriptor(): OutfitDescriptor {
    // Basfigur utan hår används alltid
    val base = "baspojken-hairless"

    val temp = temperatureC
    val isCold = temp != null && temp <= 5
    val isHot = temp != null && temp >= 25

    val isRainy = when (condition) {
        WeatherCondition.RAIN, WeatherCondition.STORM -> true
        else -> false
    }

    val isSnowy = condition == WeatherCondition.SNOW

    val top: String
    val bottom: String
    val shoes: String
    val hat: String?

    when {
        isSnowy || (isCold && isRainy) -> {
            top = "coat-winter"
            bottom = "jeans_g"
            shoes = "boots_winter"
            hat = "winter-hat"
        }

        isCold && isRainy -> {
            top = "raincoat_hood"
            bottom = "jeans_g"
            shoes = "boots_winter"
            hat = "winter-hat"
        }

        isCold -> {
            top = "coat-winter"
            bottom = "jeans_g"
            shoes = "boots_winter"
            hat = "winter-hat"
        }

        isHot && isRainy -> {
            top = "raincoat"
            bottom = "jorts"
            shoes = "sneakers_v"
            hat = null
        }

        isHot -> {
            top = "shirt_lb"
            bottom = "jorts"
            shoes = "sneakers_v"
            hat = null
        }

        isRainy -> {
            top = "raincoat"
            bottom = "jeans"
            shoes = "boot_grey"
            hat = null
        }

        else -> {
            top = "shirt_lb"
            bottom = "jeans"
            shoes = "sneakers_v"
            hat = null
        }
    }

    val hair: String? = if (hat != null) null else "hair"

    return OutfitDescriptor(
        baseName = base,
        hairName = hair,
        topName = top,
        bottomName = bottom,
        shoesName = shoes,
        hatName = hat,
    )
}
