import { useEffect, useRef, useState } from "react";

/** Returnerar nuvarande tid och uppdaterar den varje minut */
export function useMinuteTicker(): Date {
  const [now, setNow] = useState(() => new Date());
  const ref = useRef<ReturnType<typeof setTimeout>>(null);

  useEffect(() => {
    function tick() {
      setNow(new Date());
      const msToNextMinute = 60_000 - (Date.now() % 60_000);
      ref.current = setTimeout(tick, msToNextMinute);
    }
    const msToNextMinute = 60_000 - (Date.now() % 60_000);
    ref.current = setTimeout(tick, msToNextMinute);
    return () => {
      if (ref.current) clearTimeout(ref.current);
    };
  }, []);

  return now;
}
