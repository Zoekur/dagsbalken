import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { CustomBlock, TimerModel, WeatherData, SavedLocation } from "../core/data/models";
import { emptyWeatherData } from "../core/data/models";

interface AppState {
  // Väder
  weather: WeatherData;
  location: SavedLocation | null;
  setWeather: (w: WeatherData) => void;
  setLocation: (loc: SavedLocation) => void;

  // Blocks (timers/events skapade av användaren)
  customBlocks: CustomBlock[];
  addBlock: (b: CustomBlock) => void;
  removeBlock: (id: string) => void;

  // Timer-mallar
  timerModels: TimerModel[];
  addTimer: (t: TimerModel) => void;
  removeTimer: (id: string) => void;

  // UI-inställningar
  schoolModeEnabled: boolean;
  setSchoolMode: (v: boolean) => void;
  showWeather: boolean;
  showClothing: boolean;
  setShowWeather: (v: boolean) => void;
  setShowClothing: (v: boolean) => void;
}

export const useAppStore = create<AppState>()(
  persist(
    (set) => ({
      weather: emptyWeatherData,
      location: null,
      setWeather: (w) => set({ weather: w }),
      setLocation: (loc) => set({ location: loc }),

      customBlocks: [],
      addBlock: (b) => set((s) => ({ customBlocks: [...s.customBlocks, b] })),
      removeBlock: (id) =>
        set((s) => ({ customBlocks: s.customBlocks.filter((b) => b.id !== id) })),

      timerModels: [],
      addTimer: (t) => set((s) => ({ timerModels: [...s.timerModels, t] })),
      removeTimer: (id) =>
        set((s) => ({ timerModels: s.timerModels.filter((t) => t.id !== id) })),

      schoolModeEnabled: false,
      setSchoolMode: (v) => set({ schoolModeEnabled: v }),
      showWeather: true,
      showClothing: true,
      setShowWeather: (v) => set({ showWeather: v }),
      setShowClothing: (v) => set({ showClothing: v }),
    }),
    { name: "dagsbalken-store" }
  )
);
