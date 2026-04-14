import type { DayEvent } from "@dagsbalken/shared";

/** Exempelschema baserat på ett verkligt lågstadieschema */
export const DEMO_SCHEDULE: DayEvent[] = [
  { id: "1", title: "Samling",    startMinute: 8*60,      endMinute: 8*60+30,   color: "#7B1FA2", icon: "🌅" },
  { id: "2", title: "Svenska",    startMinute: 8*60+30,   endMinute: 9*60+30,   color: "#1565C0", icon: "📖" },
  { id: "3", title: "Rast",       startMinute: 9*60+30,   endMinute: 9*60+45,   color: "#2E7D32", icon: "⚽" },
  { id: "4", title: "Matematik",  startMinute: 9*60+45,   endMinute: 11*60,     color: "#E65100", icon: "🔢" },
  { id: "5", title: "Lunch",      startMinute: 11*60,     endMinute: 11*60+45,  color: "#558B2F", icon: "🥗" },
  { id: "6", title: "Bild",       startMinute: 11*60+45,  endMinute: 12*60+45,  color: "#AD1457", icon: "🎨" },
  { id: "7", title: "Rast",       startMinute: 12*60+45,  endMinute: 13*60,     color: "#2E7D32", icon: "⚽" },
  { id: "8", title: "Teknik",     startMinute: 13*60,     endMinute: 13*60+30,  color: "#00695C", icon: "⚙️" },
  { id: "9", title: "Fritids",    startMinute: 13*60+30,  endMinute: 17*60,     color: "#4527A0", icon: "🎮" },
];
