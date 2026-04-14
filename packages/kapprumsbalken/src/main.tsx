import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { ThemeProvider } from "@dagsbalken/ui";

function App() {
  return (
    <ThemeProvider>
      <div style={{ padding: 32, fontFamily: "system-ui, sans-serif", color: "var(--color-onBackground)" }}>
        <h1>Kapprumsbalken</h1>
        <p>Visuell kapprumsdisplay — under utveckling.</p>
      </div>
    </ThemeProvider>
  );
}

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <App />
  </StrictMode>
);
