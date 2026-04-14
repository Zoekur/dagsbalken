import { useState, useCallback } from "react";
import { fetchWeather, getCurrentPosition, reverseGeocode } from "@dagsbalken/shared";
import type { SavedLocation } from "@dagsbalken/shared";
import { useAppStore } from "../store/appStore";

export function useWeather() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const setWeather = useAppStore((s) => s.setWeather);
  const setLocation = useAppStore((s) => s.setLocation);
  const savedLocation = useAppStore((s) => s.location);

  const refresh = useCallback(
    async (override?: SavedLocation) => {
      const loc = override ?? savedLocation;
      if (!loc) {
        setError("Ingen plats angiven.");
        return;
      }
      setLoading(true);
      setError(null);
      try {
        const data = await fetchWeather(loc);
        setWeather(data);
      } catch (e) {
        setError(e instanceof Error ? e.message : "Okänt fel");
      } finally {
        setLoading(false);
      }
    },
    [savedLocation, setWeather]
  );

  const useGps = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const pos = await getCurrentPosition();
      const { latitude, longitude } = pos.coords;
      const name = await reverseGeocode(latitude, longitude);
      const loc: SavedLocation = { name, latitude, longitude };
      setLocation(loc);
      const data = await fetchWeather(loc);
      setWeather(data);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Kunde inte hämta position");
    } finally {
      setLoading(false);
    }
  }, [setLocation, setWeather]);

  return { refresh, useGps, loading, error };
}
