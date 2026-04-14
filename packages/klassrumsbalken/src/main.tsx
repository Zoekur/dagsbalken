import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { ThemeProvider } from "@dagsbalken/ui";
import { ClassroomScreen } from "./screens/ClassroomScreen.js";
import "./index.css";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <ThemeProvider>
      <ClassroomScreen />
    </ThemeProvider>
  </StrictMode>
);
