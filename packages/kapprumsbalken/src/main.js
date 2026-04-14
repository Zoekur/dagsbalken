import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { ThemeProvider } from "@dagsbalken/ui";
function App() {
    return (_jsx(ThemeProvider, { children: _jsxs("div", { style: { padding: 32, fontFamily: "system-ui, sans-serif", color: "var(--color-onBackground)" }, children: [_jsx("h1", { children: "Kapprumsbalken" }), _jsx("p", { children: "Visuell kapprumsdisplay \u2014 under utveckling." })] }) }));
}
createRoot(document.getElementById("root")).render(_jsx(StrictMode, { children: _jsx(App, {}) }));
