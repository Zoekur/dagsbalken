import { TimelineCard } from "@dagsbalken/ui";
import { useMinuteTicker } from "../hooks/useMinuteTicker.js";
import { useCurrentActivity } from "../hooks/useCurrentActivity.js";
import { NuKort, NastaKort } from "../components/ActivityCards.js";
import { DEMO_SCHEDULE } from "../data/demoSchedule.js";

const WEEKDAYS = ["söndag", "måndag", "tisdag", "onsdag", "torsdag", "fredag", "lördag"];
const MONTHS = ["januari","februari","mars","april","maj","juni","juli","augusti","september","oktober","november","december"];

function formatDate(d: Date): string {
  return `${WEEKDAYS[d.getDay()]} ${d.getDate()} ${MONTHS[d.getMonth()]}`;
}

function formatClock(d: Date): string {
  return `${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
}

export function ClassroomScreen() {
  const now = useMinuteTicker();
  const { current, next, progress, minutesLeft } = useCurrentActivity(DEMO_SCHEDULE, now);

  // Visa en övergångsvarning de sista 5 minuterna
  const transitionSoon = minutesLeft > 0 && minutesLeft <= 5 && next !== null;

  return (
    <div
      style={{
        height: "100dvh",
        display: "flex",
        flexDirection: "column",
        backgroundColor: "#0a0e1a",
        color: "#fff",
        fontFamily: "system-ui, -apple-system, sans-serif",
        padding: "20px 24px 24px",
        gap: 16,
        boxSizing: "border-box",
      }}
    >
      {/* Header — klockan är diskret, primärt för läraren */}
      <header
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "baseline",
          flexShrink: 0,
        }}
      >
        <span
          style={{
            fontSize: "clamp(16px, 2.5vw, 28px)",
            color: "rgba(255,255,255,0.4)",
            textTransform: "capitalize",
          }}
        >
          {formatDate(now)}
        </span>
        <span
          style={{
            fontSize: "clamp(18px, 2.5vw, 32px)",
            fontWeight: 400,
            fontVariantNumeric: "tabular-nums",
            color: "rgba(255,255,255,0.35)",
          }}
        >
          {formatClock(now)}
        </span>
      </header>

      {/* Tidslinje — Dagsbalken USP, begränsad till skoldag 08:00–17:00 */}
      <div style={{ flexShrink: 0 }}>
        <TimelineCard
          events={DEMO_SCHEDULE}
          customBlocks={[]}
          now={now}
          dayStartMinute={8 * 60}
          dayEndMinute={17 * 60}
        />
      </div>

      {/* Övergångsvarning */}
      {transitionSoon && next && (
        <div
          style={{
            flexShrink: 0,
            borderRadius: 12,
            backgroundColor: "rgba(255,152,0,0.15)",
            border: "2px solid #FF9800",
            padding: "12px 20px",
            display: "flex",
            alignItems: "center",
            gap: 12,
            fontSize: "clamp(15px, 2vw, 24px)",
            color: "#FFB74D",
          }}
        >
          <span style={{ fontSize: "clamp(20px, 3vw, 32px)" }}>🔔</span>
          Strax är {current?.title} slut — {next.icon} {next.title} börjar snart
        </div>
      )}

      {/* Nu-kort */}
      {current ? (
        <NuKort activity={current} progress={progress} minutesLeft={minutesLeft} />
      ) : (
        <div
          style={{
            flex: 1,
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            color: "rgba(255,255,255,0.3)",
            fontSize: "clamp(20px, 3vw, 36px)",
          }}
        >
          Ingen schemalagd aktivitet just nu
        </div>
      )}

      {/* Nästa aktivitet */}
      {next && <NastaKort activity={next} />}
    </div>
  );
}
