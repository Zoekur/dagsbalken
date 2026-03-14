package com.dagsbalken.app.ui.panorama

enum class PanoramaStyle(
    val storageValue: String,
    val label: String,
    val description: String
) {
    Storybook(
        storageValue = "storybook",
        label = "Mer sagobok",
        description = "Varmare himmel, mjukare glöd och tydligare skymningskänsla."
    ),
    Nordic(
        storageValue = "nordic",
        label = "Mer nordiskt diskret",
        description = "Lugnare toner med tydligt djup men mer återhållsam känsla."
    ),
    Arcade(
        storageValue = "arcade",
        label = "Mer spelkänsla",
        description = "Starkare kontraster, snabbare parallax och mer markerade lager."
    );

    companion object {
        fun fromStorageValue(value: String?): PanoramaStyle =
            entries.firstOrNull { it.storageValue == value } ?: Nordic
    }
}

