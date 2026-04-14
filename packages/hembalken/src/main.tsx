import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { ThemeProvider } from "@dagsbalken/ui";

function App() {
  return (
    <ThemeProvider>
      <div style={{ padding: 32, fontFamily: "system-ui, sans-serif", color: "var(--color-onBackground)" }}>
        <h1>Hembalken</h1>
        <p>Parallax-landskap och aktiviteter — under utveckling.</p>
      </div>
    </ThemeProvider>
  );
}

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <App />
  </StrictMode>
);
