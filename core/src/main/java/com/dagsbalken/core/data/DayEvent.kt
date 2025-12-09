package com.dagsbalken.core.data

import java.time.LocalTime

/**
 * Representerar en enskild händelse under dagen.
 *
 * @property id Unikt ID för händelsen från kalendern.
 * @property title Händelsens titel.
 * @property start Starttid.
 * @property end Sluttid (kan vara null).
 * @property color Färgen på händelsen från kalendern.
 * @property icon En valfri emoji/ikon för att representera händelsen.
 */
data class DayEvent(
    val id: String,
    val title: String,
    val start: LocalTime,
    val end: LocalTime?,
    val color: Int = 0xFF000000.toInt(), // Default Svart
    val icon: String? = null
)
