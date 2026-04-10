import React, { createContext, useContext, useState, useEffect } from "react";
import type { ThemeColors, ThemeOption } from "./themes";
import { getTheme, loadTheme, saveTheme } from "./themes";

interface ThemeContextValue {
  option: ThemeOption;
  colors: ThemeColors;
  setTheme: (t: ThemeOption) => void;
}

const ThemeContext = createContext<ThemeContextValue>({
  option: "Cold",
  colors: getTheme("Cold"),
  setTheme: () => {},
});

export function ThemeProvider({ children }: { children: React.ReactNode }) {
  const [option, setOption] = useState<ThemeOption>(loadTheme);

  useEffect(() => {
    saveTheme(option);
    const colors = getTheme(option);
    const root = document.documentElement;
    Object.entries(colors).forEach(([key, value]) => {
      root.style.setProperty(`--color-${key}`, value as string);
    });
  }, [option]);

  return (
    <ThemeContext.Provider value={{ option, colors: getTheme(option), setTheme: setOption }}>
      {children}
    </ThemeContext.Provider>
  );
}

export function useTheme() {
  return useContext(ThemeContext);
}
