import { useAppStore } from "../../store/appStore";
import { useMinuteTicker } from "../../hooks/useMinuteTicker";
import { useWeather } from "../../hooks/useWeather";
import TimelineCard from "../timeline/TimelineCard";
import WeatherCard from "../weather/WeatherCard";
import { useTheme } from "../../theme/ThemeProvider";

export default function HomeScreen() {
  const { colors } = useTheme();
  const now = useMinuteTicker();
  const weather = useAppStore((s) => s.weather);
  const customBlocks = useAppStore((s) => s.customBlocks);
  const showWeather = useAppStore((s) => s.showWeather);
  const { refresh, loading } = useWeather();

  // Kalender-events är tomma tills webbkalender-integration läggs till
  const events = useAppStore(() => []);

  const dateStr = now.toLocaleDateString("sv-SE", {
    weekday: "long",
    day: "numeric",
    month: "long",
  });

  return (
    <main
      style={{
        minHeight: "100dvh",
        backgroundColor: colors.background,
        color: colors.onBackground,
        fontFamily: "system-ui, sans-serif",
        padding: "env(safe-area-inset-top, 16px) 16px 32px",
        maxWidth: 600,
        margin: "0 auto",
        display: "flex",
        flexDirection: "column",
        gap: 16,
      }}
    >
      {/* Datumrubrik */}
      <header style={{ paddingTop: 8 }}>
        <h1
          style={{
            margin: 0,
            fontSize: 22,
            fontWeight: 600,
            color: colors.primary,
            textTransform: "capitalize",
          }}
        >
          {dateStr}
        </h1>
      </header>

      {/* Tidslinje */}
      <section aria-label="Tidslinje">
        <TimelineCard events={events} customBlocks={customBlocks} now={now} />
      </section>

      {/* Väder */}
      {showWeather && (
        <section aria-label="Väder">
          <WeatherCard weather={weather} onRefresh={() => refresh()} loading={loading} />
        </section>
      )}
    </main>
  );
}
