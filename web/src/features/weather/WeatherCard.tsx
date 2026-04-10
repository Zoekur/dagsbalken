import { useTheme } from "../../theme/ThemeProvider";
import type { WeatherData } from "../../core/data/models";

interface Props {
  weather: WeatherData;
  onRefresh: () => void;
  loading?: boolean;
}

export default function WeatherCard({ weather, onRefresh, loading }: Props) {
  const { colors } = useTheme();

  const lastUpdated = weather.lastUpdatedMs
    ? new Date(weather.lastUpdatedMs).toLocaleTimeString("sv-SE", {
        hour: "2-digit",
        minute: "2-digit",
      })
    : null;

  return (
    <div
      style={{
        backgroundColor: colors.surface,
        borderRadius: 16,
        padding: "16px 20px",
        display: "flex",
        alignItems: "center",
        gap: 16,
        cursor: "pointer",
      }}
      onClick={onRefresh}
      role="button"
      aria-label="Uppdatera väder"
    >
      <span style={{ fontSize: 40 }}>{weather.adviceIcon || "🌤️"}</span>
      <div style={{ flex: 1 }}>
        <div style={{ color: colors.onSurface, fontSize: 13, marginBottom: 2 }}>
          {weather.locationName || "Okänd plats"}
        </div>
        {weather.isDataLoaded ? (
          <>
            <div style={{ color: colors.onBackground, fontSize: 28, fontWeight: 700, lineHeight: 1 }}>
              {weather.temperatureCelsius}°
            </div>
            <div style={{ color: colors.onSurface, fontSize: 12, marginTop: 4 }}>
              {weather.adviceText}
            </div>
          </>
        ) : (
          <div style={{ color: colors.outline, fontSize: 14 }}>
            {loading ? "Hämtar väder…" : "Tryck för att hämta väder"}
          </div>
        )}
      </div>
      {lastUpdated && (
        <div style={{ color: colors.outline, fontSize: 11, textAlign: "right" }}>
          {lastUpdated}
          <br />
          {weather.provider}
        </div>
      )}
    </div>
  );
}
