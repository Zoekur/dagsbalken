export type DayOfWeekKey = "MONDAY" | "TUESDAY" | "WEDNESDAY" | "THURSDAY" | "FRIDAY" | "SATURDAY" | "SUNDAY";

export interface TimeRange {
  /** Minuter sedan midnatt 0–1439 */
  startMinutes: number;
  endMinutes: number;
}

export interface ScheduleSymbol {
  id: string;
  label: string;
  /** Nyckel till emoji-mappning, t.ex. "food", "school", "sport", "sleep" */
  iconKey: string;
  color: string;
  isSchoolRelated: boolean;
  description?: string;
}

export interface DailySymbolPlacement {
  symbolId: string;
  /** Om undefined = gäller alla dagar */
  dayOfWeek?: DayOfWeekKey;
  timeRange: TimeRange;
  schoolModeOnly: boolean;
  zoneLabel?: string;
}

export interface TimelineSymbolSchedule {
  symbols: ScheduleSymbol[];
  placements: DailySymbolPlacement[];
}

export const ICON_EMOJI: Record<string, string> = {
  food: "🍽️",
  school: "🎒",
  sport: "⚽",
  sleep: "😴",
  work: "💼",
  free: "🎮",
};

const ISO_DAY_TO_KEY: DayOfWeekKey[] = [
  "MONDAY",
  "TUESDAY",
  "WEDNESDAY",
  "THURSDAY",
  "FRIDAY",
  "SATURDAY",
  "SUNDAY",
];

/** Returnerar placements som är aktiva för ett givet datum */
export function symbolPlacementsFor(
  schedule: TimelineSymbolSchedule,
  date: Date,
  schoolModeEnabled: boolean
): DailySymbolPlacement[] {
  const iso = date.getDay(); // 0=sön, 1=mån …
  const dayKey: DayOfWeekKey = ISO_DAY_TO_KEY[(iso + 6) % 7];

  return schedule.placements.filter((p) => {
    if (p.schoolModeOnly && !schoolModeEnabled) return false;
    if (!p.dayOfWeek || p.dayOfWeek === dayKey) return true;
    return false;
  });
}
