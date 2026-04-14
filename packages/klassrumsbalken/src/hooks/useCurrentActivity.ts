import type { DayEvent } from "@dagsbalken/shared";

function minutesSinceMidnight(d: Date): number {
  return d.getHours() * 60 + d.getMinutes();
}

export interface ActivityState {
  current: DayEvent | null;
  next: DayEvent | null;
  /** 0–1, hur långt igenom den aktiva aktiviteten vi är */
  progress: number;
  /** Minuter kvar av aktiviteten */
  minutesLeft: number;
}

export function useCurrentActivity(schedule: DayEvent[], now: Date): ActivityState {
  const nowMinute = minutesSinceMidnight(now);

  const current = schedule.find((e) => {
    const end = e.endMinute ?? e.startMinute + 60;
    return e.startMinute <= nowMinute && end > nowMinute;
  }) ?? null;

  const next = schedule.find((e) => e.startMinute > nowMinute) ?? null;

  let progress = 0;
  let minutesLeft = 0;

  if (current) {
    const end = current.endMinute ?? current.startMinute + 60;
    const duration = end - current.startMinute;
    const elapsed = nowMinute - current.startMinute;
    progress = Math.min(1, Math.max(0, elapsed / duration));
    minutesLeft = Math.max(0, end - nowMinute);
  }

  return { current, next, progress, minutesLeft };
}
