// Datamodeller porterade från Android core-modulen

export interface DayEvent {
  id: string;
  title: string;
  /** Minuter sedan midnatt (0–1439) */
  startMinute: number;
  /** Minuter sedan midnatt, undefined = heldag */
  endMinute?: number;
  /** Hex-färg, t.ex. "#4FC3F7" */
  color: string;
  icon?: string;
}

export type BlockType = "TIMER" | "EVENT";

export interface CustomBlock {
  id: string;
  title: string;
  /** Minuter sedan midnatt */
  startMinute: number;
  /** Minuter sedan midnatt */
  endMinute: number;
  /** ISO-datum "YYYY-MM-DD" */
  date: string;
  type: BlockType;
  color?: string;
}

export interface TimerModel {
  id: string;
  name: string;
  durationHours: number;
  durationMinutes: number;
  colorHex: string;
}

export interface WeatherData {
  temperatureCelsius: number;
  precipitationChance: number;
  adviceIcon: string;
  adviceText: string;
  clothingType: string;
  isDataLoaded: boolean;
  locationName: string;
  lastUpdatedMs: number;
  provider: string;
}

export const emptyWeatherData: WeatherData = {
  temperatureCelsius: 0,
  precipitationChance: 0,
  adviceIcon: "",
  adviceText: "",
  clothingType: "normal",
  isDataLoaded: false,
  locationName: "",
  lastUpdatedMs: 0,
  provider: "",
};

export type WeatherCondition =
  | "SUN"
  | "CLOUDY"
  | "RAIN"
  | "STORM"
  | "SNOW"
  | "WINDY"
  | "FOG"
  | "HOT";

export interface WeatherContext {
  condition: WeatherCondition;
  temperatureC?: number;
  windSpeedMs?: number;
}

export interface LocationResult {
  name: string;
  country: string;
  latitude: number;
  longitude: number;
}

export interface SavedLocation {
  name: string;
  latitude: number;
  longitude: number;
}
