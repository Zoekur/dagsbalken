import type { DayEvent } from "@dagsbalken/shared";

/** Kvalitativ fasbeskrivning baserad på progress 0–1 */
function progressPhrase(progress: number): string {
  if (progress < 0.08) return "Precis börjat";
  if (progress < 0.4)  return "Pågår";
  if (progress < 0.65) return "Mer än hälften klart";
  if (progress < 0.85) return "Snart klart";
  if (progress < 0.97) return "Håller på att avslutas";
  return "Avslutas nu";
}

/** Kvalitativ tidsbeskrivning baserad på minuter kvar */
function timeLeftPhrase(minutesLeft: number): string {
  if (minutesLeft > 60)  return "Lång tid kvar";
  if (minutesLeft > 30)  return "Ett tag kvar";
  if (minutesLeft > 15)  return "Dags snart";
  if (minutesLeft > 5)   return "Snart klart";
  if (minutesLeft > 1)   return "Strax";
  return "Nu avslutas det";
}

/** Progressbarens färg förändras mot orange/rött när det är nära slut */
function progressColor(baseColor: string, progress: number): string {
  if (progress > 0.85) return "#ef5350";
  if (progress > 0.65) return "#FF9800";
  return baseColor;
}

interface NuKortProps {
  activity: DayEvent;
  progress: number;
  minutesLeft: number;
}

export function NuKort({ activity, progress, minutesLeft }: NuKortProps) {
  const isNearEnd = progress > 0.85;
  const barColor = progressColor(activity.color, progress);

  return (
    <div
      style={{
        flex: 1,
        borderRadius: 24,
        backgroundColor: activity.color + (isNearEnd ? "33" : "18"),
        border: `3px solid ${isNearEnd ? barColor : activity.color}`,
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        padding: "32px 40px",
        gap: 20,
        minHeight: 0,
        transition: "border-color 2s ease, background-color 2s ease",
      }}
    >
      {/* Piktogram */}
      <div style={{ fontSize: "clamp(64px, 12vw, 120px)", lineHeight: 1 }}>
        {activity.icon ?? "📌"}
      </div>

      {/* Aktivitetsnamn */}
      <div
        style={{
          fontSize: "clamp(36px, 7vw, 80px)",
          fontWeight: 800,
          color: "#fff",
          textAlign: "center",
          lineHeight: 1.1,
        }}
      >
        {activity.title}
      </div>

      {/* Upplevd fas — primär kommunikation */}
      <div
        style={{
          fontSize: "clamp(20px, 3.5vw, 42px)",
          fontWeight: 600,
          color: isNearEnd ? barColor : "rgba(255,255,255,0.85)",
          transition: "color 2s ease",
        }}
      >
        {progressPhrase(progress)}
      </div>

      {/* Progressbar */}
      <div
        style={{
          width: "100%",
          maxWidth: 520,
          height: 20,
          borderRadius: 10,
          backgroundColor: "rgba(255,255,255,0.12)",
          overflow: "hidden",
        }}
      >
        <div
          style={{
            width: `${progress * 100}%`,
            height: "100%",
            backgroundColor: barColor,
            borderRadius: 10,
            transition: "width 30s linear, background-color 2s ease",
          }}
        />
      </div>

      {/* Upplevd tid kvar — sekundär kommunikation */}
      <div
        style={{
          fontSize: "clamp(16px, 2.5vw, 30px)",
          color: isNearEnd ? barColor : "rgba(255,255,255,0.5)",
          transition: "color 2s ease",
        }}
      >
        {timeLeftPhrase(minutesLeft)}
      </div>
    </div>
  );
}

interface NastaKortProps {
  activity: DayEvent;
}

export function NastaKort({ activity }: NastaKortProps) {
  return (
    <div
      style={{
        borderRadius: 16,
        backgroundColor: "rgba(255,255,255,0.06)",
        border: `2px solid rgba(255,255,255,0.12)`,
        display: "flex",
        alignItems: "center",
        gap: 20,
        padding: "20px 28px",
      }}
    >
      <div style={{ fontSize: "clamp(28px, 4vw, 48px)", lineHeight: 1 }}>
        {activity.icon ?? "📌"}
      </div>
      <div>
        <div
          style={{
            fontSize: 13,
            color: "rgba(255,255,255,0.4)",
            marginBottom: 4,
            letterSpacing: "0.08em",
            textTransform: "uppercase",
          }}
        >
          Nästa
        </div>
        <div style={{ fontSize: "clamp(20px, 3vw, 36px)", fontWeight: 700, color: "#fff" }}>
          {activity.title}
        </div>
      </div>
      <div
        style={{
          marginLeft: "auto",
          width: 12,
          height: 40,
          borderRadius: 6,
          backgroundColor: activity.color,
          flexShrink: 0,
        }}
      />
    </div>
  );
}

