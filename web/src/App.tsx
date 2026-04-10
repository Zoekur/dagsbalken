import { ThemeProvider } from "./theme/ThemeProvider";
import HomeScreen from "./features/home/HomeScreen";

export default function App() {
  return (
    <ThemeProvider>
      <HomeScreen />
    </ThemeProvider>
  );
}
