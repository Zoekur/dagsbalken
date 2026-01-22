## 2024-05-23 - Avoiding Object Allocation in Widget Updates
**Learning:** App Widgets updated via `WorkManager` or `AlarmManager` (like `LinearClockWidgetReceiver`) run frequently. Allocating objects like `Paint` inside the generation loop adds unnecessary GC pressure, even if it's "only" once a minute.
**Action:** Use `ThreadLocal<Paint>` for stateless helper objects in singletons that are called from potentially concurrent contexts (Glance composition).

## 2025-12-20 - Caching Native Assets in Widget Generators
**Learning:** Even simple calls like `Typeface.create()` involve JNI overhead. In high-frequency paths like widget bitmap generation (every minute), repeated creation of identical immutable objects (fonts) is wasteful.
**Action:** Use `ConcurrentHashMap.computeIfAbsent` to cache immutable native resources like `Typeface` in the singleton generator, ensuring thread safety and reducing JNI calls.

## 2025-12-21 - Optimizing Float Arithmetic in Drawing Loops
**Learning:** In high-frequency drawing loops (like widget bitmap generation), repeatedly dividing by a constant factor (`x / factor`) is less efficient than pre-calculating the reciprocal and multiplying (`x * (1/factor)`).
**Action:** When iterating over collections (events, ticks) to calculate pixel coordinates, hoist the division out of the loop by calculating a multiplier (e.g., `pixelsPerMinute`) and using multiplication inside the loop.
