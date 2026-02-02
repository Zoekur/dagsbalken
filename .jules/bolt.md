## 2024-05-23 - Avoiding Object Allocation in Widget Updates
**Learning:** App Widgets updated via `WorkManager` or `AlarmManager` (like `LinearClockWidgetReceiver`) run frequently. Allocating objects like `Paint` inside the generation loop adds unnecessary GC pressure, even if it's "only" once a minute.
**Action:** Use `ThreadLocal<Paint>` for stateless helper objects in singletons that are called from potentially concurrent contexts (Glance composition).

## 2025-12-20 - Caching Native Assets in Widget Generators
**Learning:** Even simple calls like `Typeface.create()` involve JNI overhead. In high-frequency paths like widget bitmap generation (every minute), repeated creation of identical immutable objects (fonts) is wasteful.
**Action:** Use `ConcurrentHashMap.computeIfAbsent` to cache immutable native resources like `Typeface` in the singleton generator, ensuring thread safety and reducing JNI calls.

## 2025-12-20 - Pre-calculating Inverses for Drawing Loops
**Learning:** In tight drawing loops (e.g., widget bitmaps), repeatedly dividing by a constant (e.g., `minutesPerPixel`) is less efficient than multiplying by a pre-calculated inverse (`pixelsPerMinute`).
**Action:** Compute the inverse once outside the loop and use multiplication inside, ensuring to handle potential division-by-zero (though unlikely with validated config). Also, discovered unrelated syntax errors in `WeatherAssistantCard.kt` which blocked builds, emphasizing the need to verify clean builds before optimizing.
