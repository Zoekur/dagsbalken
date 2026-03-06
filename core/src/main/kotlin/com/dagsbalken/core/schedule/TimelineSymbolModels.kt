package com.dagsbalken.core.schedule

import kotlinx.serialization.Serializable

@Serializable
enum class DayOfWeekKey {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY
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
    val isSchoolRelated: Boolean
)

@Serializable
data class DailySymbolPlacement(
    val symbolId: String,
    val dayOfWeek: DayOfWeekKey? = null,
    val timeRange: TimeRange,
    val schoolModeOnly: Boolean = false
)

@Serializable
data class TimelineSymbolSchedule(
    val symbols: List<ScheduleSymbol> = emptyList(),
    val placements: List<DailySymbolPlacement> = emptyList()
)
