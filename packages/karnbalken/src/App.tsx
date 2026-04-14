import { ThemeProvider } from "@dagsbalken/ui";
import HomeScreen from "./features/home/HomeScreen";

export default function App() {
  return (
    <ThemeProvider>
      <HomeScreen />
    </ThemeProvider>
  );
}
