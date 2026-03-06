package com.dagsbalken.core.schedule

import java.time.DayOfWeek
import java.time.LocalDate

/** Utility to map java.time.DayOfWeek to [DayOfWeekKey]. */
fun LocalDate.toDayOfWeekKey(): DayOfWeekKey = when (this.dayOfWeek) {
    DayOfWeek.MONDAY -> DayOfWeekKey.MONDAY
    DayOfWeek.TUESDAY -> DayOfWeekKey.TUESDAY
    DayOfWeek.WEDNESDAY -> DayOfWeekKey.WEDNESDAY
    DayOfWeek.THURSDAY -> DayOfWeekKey.THURSDAY
    DayOfWeek.FRIDAY -> DayOfWeekKey.FRIDAY
    DayOfWeek.SATURDAY -> DayOfWeekKey.SATURDAY
    DayOfWeek.SUNDAY -> DayOfWeekKey.SUNDAY
}

/** Resolve placements for a specific date and mode. */
fun TimelineSymbolSchedule.symbolPlacementsFor(
    date: LocalDate,
    schoolMode: Boolean
): List<DailySymbolPlacement> {
    if (symbols.isEmpty() || placements.isEmpty()) return emptyList()

    val dow = date.toDayOfWeekKey()

    val daySpecific = placements.filter { it.dayOfWeek == dow }
    val dailyDefaults = placements.filter { it.dayOfWeek == null }

    val base = if (daySpecific.isNotEmpty()) daySpecific else dailyDefaults

    if (base.isEmpty()) return emptyList()

    if (!schoolMode) return base.sortedBy { it.timeRange.startMinutes }

    // In school mode, only keep school-related or explicit school-only blocks
    val filtered = base.filter { placement ->
        if (placement.schoolModeOnly) return@filter true
        val symbol = symbols.firstOrNull { it.id == placement.symbolId }
        symbol?.isSchoolRelated == true
    }

    return filtered.sortedBy { it.timeRange.startMinutes }
}

/** Helper to resolve a [ScheduleSymbol] for a placement. */
fun TimelineSymbolSchedule.resolveSymbol(placement: DailySymbolPlacement): ScheduleSymbol? =
    symbols.firstOrNull { it.id == placement.symbolId }

