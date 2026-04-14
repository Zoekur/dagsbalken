import type { WeatherData, WeatherCondition, LocationResult, SavedLocation } from "./models.js";

const WEATHER_BASE = "https://api.open-meteo.com/v1/forecast";
const GEOCODING_BASE = "https://geocoding-api.open-meteo.com/v1/search";
const NOMINATIM_BASE = "https://nominatim.openstreetmap.org";

const COLD_THRESHOLD = 5;
const HOT_THRESHOLD = 25;
const PRECIPITATION_THRESHOLD = 30;

interface OpenMeteoCurrentWeather {
  temperature: number;
  windspeed: number;
  weathercode: number;
}

interface OpenMeteoResponse {
  current_weather: OpenMeteoCurrentWeather;
}

interface OpenMeteoGeoResult {
  results?: Array<{
    name: string;
    country: string;
    latitude: number;
    longitude: number;
  }>;
}

interface NominatimResult {
  address?: {
    city?: string;
    town?: string;
    village?: string;
    county?: string;
    country_code?: string;
  };
}

/** Mappning från WMO weather code → WeatherCondition */
function wmoToCondition(code: number): WeatherCondition {
  if (code === 0 || code === 1) return "SUN";
  if (code <= 3) return "CLOUDY";
  if (code <= 49) return "FOG";
  if (code <= 67) return "RAIN";
  if (code <= 77) return "SNOW";
  if (code <= 82) return "RAIN";
  if (code <= 86) return "SNOW";
  return "STORM";
}

function conditionToAdvice(
  condition: WeatherCondition,
  temp: number,
  precipChance: number
): { adviceIcon: string; adviceText: string; clothingType: string } {
  const isWet = precipChance >= PRECIPITATION_THRESHOLD;

  if (temp <= 0) {
    return { adviceIcon: "❄️", adviceText: "Minusgrader, klä dig varmt!", clothingType: "cold" };
  }
  if (condition === "SNOW") {
    return { adviceIcon: "🌨️", adviceText: "Det snöar idag.", clothingType: "cold" };
  }
  if (temp <= COLD_THRESHOLD) {
    return { adviceIcon: "🧥", adviceText: "Kallt ute, ta på dig jacka.", clothingType: "cold" };
  }
  if (condition === "RAIN" || condition === "STORM") {
    return { adviceIcon: "🌧️", adviceText: "Ta med regnjacka.", clothingType: "rain" };
  }
  if (isWet) {
    return { adviceIcon: "☂️", adviceText: "Risk för regn, ta ett paraply.", clothingType: "rain" };
  }
  if (temp >= HOT_THRESHOLD) {
    return { adviceIcon: "☀️", adviceText: "Varmt och skönt idag!", clothingType: "hot" };
  }
  return { adviceIcon: "🌤️", adviceText: "Normalt väder idag.", clothingType: "normal" };
}

export async function fetchWeather(location: SavedLocation): Promise<WeatherData> {
  const url =
    `${WEATHER_BASE}?latitude=${location.latitude}&longitude=${location.longitude}` +
    `&current_weather=true&timezone=auto`;

  const response = await fetch(url);
  if (!response.ok) throw new Error(`Väder-API svarar med ${response.status}`);

  const json: OpenMeteoResponse = await response.json();
  const cw = json.current_weather;
  const temp = Math.round(cw.temperature);
  const condition = wmoToCondition(cw.weathercode);
  const { adviceIcon, adviceText, clothingType } = conditionToAdvice(condition, temp, 0);

  return {
    temperatureCelsius: temp,
    precipitationChance: 0,
    adviceIcon,
    adviceText,
    clothingType,
    isDataLoaded: true,
    locationName: location.name,
    lastUpdatedMs: Date.now(),
    provider: "Open-Meteo",
  };
}

export async function searchLocations(query: string): Promise<LocationResult[]> {
  if (!query.trim()) return [];
  const url = `${GEOCODING_BASE}?name=${encodeURIComponent(query)}&count=5&language=sv&format=json`;
  const response = await fetch(url);
  if (!response.ok) return [];
  const json: OpenMeteoGeoResult = await response.json();
  return (json.results ?? []).map((r) => ({
    name: r.name,
    country: r.country,
    latitude: r.latitude,
    longitude: r.longitude,
  }));
}

export async function reverseGeocode(lat: number, lon: number): Promise<string> {
  const url =
    `${NOMINATIM_BASE}/reverse?lat=${lat}&lon=${lon}&format=json&addressdetails=1`;
  const response = await fetch(url, {
    headers: { "Accept-Language": "sv" },
  });
  if (!response.ok) return `${lat.toFixed(2)}, ${lon.toFixed(2)}`;
  const json: NominatimResult = await response.json();
  const a = json.address ?? {};
  const city = a.city ?? a.town ?? a.village ?? a.county ?? "";
  const cc = (a.country_code ?? "").toUpperCase();
  return [city, cc].filter(Boolean).join(", ");
}

export async function getCurrentPosition(): Promise<GeolocationPosition> {
  return new Promise((resolve, reject) =>
    navigator.geolocation.getCurrentPosition(resolve, reject, {
      timeout: 10_000,
      maximumAge: 60_000,
    })
  );
}
