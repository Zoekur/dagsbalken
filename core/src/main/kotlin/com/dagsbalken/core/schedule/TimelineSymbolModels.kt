package com.dagsbalken.core.schedule

import kotlinx.serialization.Serializable

@Serializable
enum class DayOfWeekKey {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
}

/**
 * Representerar de tre nivåerna i Dagsbalken-visionen.
 * Används av UI för att bestämma hur mycket detaljer som ska visas.
 */
enum class TimelineZoomLevel {
    DAY,      // Nivå A: Översikt, stora former, landskap
    HOUR,     // Nivå B: Timzon, varje timme är en plats (t.ex. "Hemma")
    ACTIVITY  // Nivå C: Fokus, detaljer, konkret aktivitet
}

@Serializable
data class TimeRange(
    val startMinutes: Int,
    val endMinutes: Int
) {
    init {
        require(startMinutes in 0..1439) { "startMinutes must be in [0, 1439]" }
        require(endMinutes in 1..1440) { "endMinutes must be in [1, 1440]" }
        require(endMinutes > startMinutes) { "endMinutes must be greater than startMinutes" }
    }
}

@Serializable
data class ScheduleSymbol(
    val id: String,
    val label: String,
    val iconKey: String,
    val colorArgb: Int,
    val isSchoolRelated: Boolean,
    /**
     * Relevanta detaljer för Nivå C (Aktivitet).
     * Blir synligt när man "flyger in" i aktiviteten.
     */
    val description: String = ""
)

@Serializable
data class DailySymbolPlacement(
    val symbolId: String,
    val dayOfWeek: DayOfWeekKey? = null,
    val timeRange: TimeRange,
    val schoolModeOnly: Boolean = false,
    /**
     * Etikett för Nivå B (Timzon).
     * Gör varje timme till en egen plats (t.ex. 08 = "Hemma", 09 = "Resa").
     */
    val zoneLabel: String = ""
)

@Serializable
data class TimelineSymbolSchedule(
    val symbols: List<ScheduleSymbol> = emptyList(),
    val placements: List<DailySymbolPlacement> = emptyList()
)
