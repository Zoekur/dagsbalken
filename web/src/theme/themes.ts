export type ThemeOption = "Cold" | "Warm" | "ColdHighContrast" | "WarmHighContrast";

export interface ThemeColors {
  background: string;
  surface: string;
  surfaceVariant: string;
  onBackground: string;
  onSurface: string;
  primary: string;
  onPrimary: string;
  secondary: string;
  outline: string;
  /** Tidslinjens nattfärg (midnatt) */
  timelineNight: string;
  /** Tidslinjens dagfärg (middag) */
  timelineDay: string;
}

const THEMES: Record<ThemeOption, ThemeColors> = {
  Cold: {
    background: "#0d1b2a",
    surface: "#1e2d3d",
    surfaceVariant: "#263545",
    onBackground: "#e8f4f8",
    onSurface: "#cce0ec",
    primary: "#4FC3F7",
    onPrimary: "#003547",
    secondary: "#81D4FA",
    outline: "#4a6070",
    timelineNight: "#1A237E",
    timelineDay: "#4FC3F7",
  },
  Warm: {
    background: "#1a0a00",
    surface: "#2d1500",
    surfaceVariant: "#3d2000",
    onBackground: "#fff3e0",
    onSurface: "#ffe0b2",
    primary: "#FFAB40",
    onPrimary: "#3e1f00",
    secondary: "#FFD54F",
    outline: "#6d4c1a",
    timelineNight: "#BF360C",
    timelineDay: "#FFEB3B",
  },
  ColdHighContrast: {
    background: "#000000",
    surface: "#111111",
    surfaceVariant: "#1a1a1a",
    onBackground: "#ffffff",
    onSurface: "#eeeeee",
    primary: "#00FFFF",
    onPrimary: "#000000",
    secondary: "#80FFFF",
    outline: "#444444",
    timelineNight: "#000000",
    timelineDay: "#00FFFF",
  },
  WarmHighContrast: {
    background: "#000000",
    surface: "#111111",
    surfaceVariant: "#1a1a1a",
    onBackground: "#ffffff",
    onSurface: "#eeeeee",
    primary: "#FFFF00",
    onPrimary: "#000000",
    secondary: "#FFD700",
    outline: "#444444",
    timelineNight: "#000000",
    timelineDay: "#FFFF00",
  },
};

export function getTheme(option: ThemeOption): ThemeColors {
  return THEMES[option];
}

/** Migrering av gamla temanamn */
export function migrateThemeName(name: string): ThemeOption {
  if (name === "NordicCalm") return "Cold";
  if (name === "SolarDawn") return "Warm";
  if (name in THEMES) return name as ThemeOption;
  return "Cold";
}

const STORAGE_KEY = "dagsbalken_theme";

export function loadTheme(): ThemeOption {
  const stored = localStorage.getItem(STORAGE_KEY) ?? "";
  return migrateThemeName(stored);
}

export function saveTheme(theme: ThemeOption): void {
  localStorage.setItem(STORAGE_KEY, theme);
}
