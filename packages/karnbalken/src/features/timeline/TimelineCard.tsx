import { useEffect, useRef, useCallback } from "react";
import type { DayEvent, CustomBlock } from "@dagsbalken/shared";
import { useTheme } from "@dagsbalken/ui";

interface TimelineProps {
  events: DayEvent[];
  customBlocks: CustomBlock[];
  now: Date;
}

const FULL_DAY = 1440; // minuter per dygn
const ZOOM_WINDOW = 180; // 3-timmars zoom

function minutesSinceMidnight(d: Date): number {
  return d.getHours() * 60 + d.getMinutes();
}

function hexToRgb(hex: string): [number, number, number] {
  const v = parseInt(hex.replace("#", ""), 16);
  return [(v >> 16) & 255, (v >> 8) & 255, v & 255];
}

function blendColor(a: string, b: string, t: number): string {
  const [ar, ag, ab] = hexToRgb(a);
  const [br, bg, bb] = hexToRgb(b);
  const r = Math.round(ar + (br - ar) * t);
  const g = Math.round(ag + (bg - ag) * t);
  const bl = Math.round(ab + (bb - ab) * t);
  return `rgb(${r},${g},${bl})`;
}

/** Bakgrundsfärg för en given minut (nattsanfärg → dagsfärg → natttfärg) */
function bgColorForMinute(minute: number, nightColor: string, dayColor: string): string {
  // 0 = midnatt, 720 = middag — cosinus-interpolation
  const t = (1 - Math.cos((minute / FULL_DAY) * 2 * Math.PI)) / 2;
  return blendColor(nightColor, dayColor, t);
}

export default function TimelineCard({ events, customBlocks, now }: TimelineProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const { colors } = useTheme();

  // Zoomtillstånd
  const zoomRef = useRef({ active: false, centerMinute: 0 });
  const lastTapRef = useRef(0);

  const draw = useCallback(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    const W = canvas.width;
    const H = canvas.height;
    const zoom = zoomRef.current;
    const startMinute = zoom.active
      ? Math.max(0, zoom.centerMinute - ZOOM_WINDOW / 2)
      : 0;
    const endMinute = zoom.active
      ? Math.min(FULL_DAY, zoom.centerMinute + ZOOM_WINDOW / 2)
      : FULL_DAY;
    const duration = endMinute - startMinute;

    function minuteToX(m: number): number {
      return ((m - startMinute) / duration) * W;
    }

    // 1. Bakgrundsgradient
    const grad = ctx.createLinearGradient(0, 0, W, 0);
    const steps = 24;
    for (let i = 0; i <= steps; i++) {
      const minute = startMinute + (duration / steps) * i;
      grad.addColorStop(i / steps, bgColorForMinute(minute, colors.timelineNight, colors.timelineDay));
    }
    ctx.fillStyle = grad;
    ctx.fillRect(0, 0, W, H);

    // 2. Timstreck
    const hourStep = zoom.active ? 1 : 3;
    ctx.strokeStyle = "rgba(255,255,255,0.25)";
    ctx.lineWidth = hourStep > 1 ? 0.75 : 1;
    ctx.font = "11px system-ui";
    ctx.fillStyle = "rgba(255,255,255,0.55)";
    ctx.textAlign = "center";

    for (let h = 0; h <= 24; h++) {
      const m = h * 60;
      if (m < startMinute || m > endMinute) continue;
      const x = minuteToX(m);
      const major = h % 3 === 0;
      ctx.lineWidth = major ? 1.5 : 0.75;
      ctx.strokeStyle = major ? "rgba(255,255,255,0.4)" : "rgba(255,255,255,0.18)";
      ctx.beginPath();
      ctx.moveTo(x, 0);
      ctx.lineTo(x, major ? H * 0.45 : H * 0.25);
      ctx.stroke();
      if (major || zoom.active) {
        ctx.fillText(`${h}`, x, H * 0.55);
      }
    }

    // 3. Kalender-events
    const eventH = H * 0.3;
    const eventY = H * 0.05;
    for (const ev of events) {
      const x1 = minuteToX(ev.startMinute);
      const x2 = minuteToX(ev.endMinute ?? ev.startMinute + 60);
      if (x2 < 0 || x1 > W) continue;
      ctx.fillStyle = ev.color + "bb";
      ctx.beginPath();
      const radius = 4;
      const ew = Math.max(x2 - x1, 4);
      ctx.roundRect(x1, eventY, ew, eventH, radius);
      ctx.fill();
      // Titel
      if (ew > 30) {
        ctx.font = "bold 10px system-ui";
        ctx.fillStyle = "#fff";
        ctx.textAlign = "left";
        ctx.fillText(ev.title, x1 + 4, eventY + 13, ew - 8);
      }
    }

    // 4. Custom blocks (timers/egna events)
    const blockH = H * 0.25;
    const blockY = H * 0.65;
    for (const b of customBlocks) {
      const x1 = minuteToX(b.startMinute);
      const x2 = minuteToX(b.endMinute);
      if (x2 < 0 || x1 > W) continue;
      const bw = Math.max(x2 - x1, 4);
      ctx.fillStyle = (b.color ?? colors.primary) + "cc";
      ctx.beginPath();
      ctx.roundRect(x1, blockY, bw, blockH, 4);
      ctx.fill();
    }

    // 5. Nu-markör
    const nowMinute = minutesSinceMidnight(now);
    if (nowMinute >= startMinute && nowMinute <= endMinute) {
      const nx = minuteToX(nowMinute);
      ctx.strokeStyle = "#ef5350";
      ctx.lineWidth = 2;
      ctx.beginPath();
      ctx.moveTo(nx, 0);
      ctx.lineTo(nx, H);
      ctx.stroke();
      // Triangel-topp
      ctx.fillStyle = "#ef5350";
      ctx.beginPath();
      ctx.moveTo(nx - 5, 0);
      ctx.lineTo(nx + 5, 0);
      ctx.lineTo(nx, 8);
      ctx.closePath();
      ctx.fill();
    }
  }, [events, customBlocks, now, colors]);

  // Rita om vid förändringar
  useEffect(() => {
    draw();
  }, [draw]);

  // Resize observer
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ro = new ResizeObserver(() => {
      canvas.width = canvas.offsetWidth * window.devicePixelRatio;
      canvas.height = canvas.offsetHeight * window.devicePixelRatio;
      const ctx = canvas.getContext("2d");
      if (ctx) ctx.scale(window.devicePixelRatio, window.devicePixelRatio);
      draw();
    });
    ro.observe(canvas);
    return () => ro.disconnect();
  }, [draw]);

  // Klick → zoom
  const handleClick = useCallback(
    (e: React.MouseEvent<HTMLCanvasElement>) => {
      const now2 = Date.now();
      const canvas = canvasRef.current;
      if (!canvas) return;
      const W = canvas.offsetWidth;
      const zoom = zoomRef.current;

      if (zoom.active) {
        const dt = now2 - lastTapRef.current;
        if (dt < 300) {
          // dubbelklick → zooma ut
          zoom.active = false;
          draw();
        }
        lastTapRef.current = now2;
        return;
      }

      const rect = canvas.getBoundingClientRect();
      const x = e.clientX - rect.left;
      const minute = Math.round((x / W) * FULL_DAY);
      zoom.active = true;
      zoom.centerMinute = minute;
      lastTapRef.current = now2;
      draw();
    },
    [draw]
  );

  // Drag för att panorera under zoom
  const dragRef = useRef({ dragging: false, startX: 0, startCenter: 0 });

  const handleMouseDown = (e: React.MouseEvent<HTMLCanvasElement>) => {
    if (!zoomRef.current.active) return;
    dragRef.current = {
      dragging: true,
      startX: e.clientX,
      startCenter: zoomRef.current.centerMinute,
    };
  };

  const handleMouseMove = useCallback(
    (e: React.MouseEvent<HTMLCanvasElement>) => {
      const d = dragRef.current;
      const canvas = canvasRef.current;
      if (!d.dragging || !canvas) return;
      const dx = e.clientX - d.startX;
      const minutePerPx = ZOOM_WINDOW / canvas.offsetWidth;
      const newCenter = Math.max(
        ZOOM_WINDOW / 2,
        Math.min(FULL_DAY - ZOOM_WINDOW / 2, d.startCenter - dx * minutePerPx)
      );
      zoomRef.current.centerMinute = newCenter;
      draw();
    },
    [draw]
  );

  const handleMouseUp = () => {
    dragRef.current.dragging = false;
  };

  return (
    <canvas
      ref={canvasRef}
      onClick={handleClick}
      onMouseDown={handleMouseDown}
      onMouseMove={handleMouseMove}
      onMouseUp={handleMouseUp}
      onMouseLeave={handleMouseUp}
      style={{
        width: "100%",
        height: "120px",
        borderRadius: "16px",
        cursor: zoomRef.current.active ? "grab" : "pointer",
        display: "block",
      }}
      aria-label="24-timmars tidslinje"
    />
  );
}
