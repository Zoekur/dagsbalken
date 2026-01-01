## 2024-05-23 - Avoiding Object Allocation in Widget Updates
**Learning:** App Widgets updated via `WorkManager` or `AlarmManager` (like `LinearClockWidgetReceiver`) run frequently. Allocating objects like `Paint` inside the generation loop adds unnecessary GC pressure, even if it's "only" once a minute.
**Action:** Use `ThreadLocal<Paint>` for stateless helper objects in singletons that are called from potentially concurrent contexts (Glance composition).

## 2025-12-20 - Caching Native Assets in Widget Generators
**Learning:** Even simple calls like `Typeface.create()` involve JNI overhead. In high-frequency paths like widget bitmap generation (every minute), repeated creation of identical immutable objects (fonts) is wasteful.
**Action:** Use `ConcurrentHashMap.computeIfAbsent` to cache immutable native resources like `Typeface` in the singleton generator, ensuring thread safety and reducing JNI calls.

## 2026-01-01 - Pre-calculation for Draw Loops
**Learning:** Calculations in `onDraw` blocks (like `LocalTime.hour * 60 + minute`) run on every frame during scrolling, even if the data hasn't changed. For complex lists, this adds up.
**Action:** Pre-calculate derived values (like minutes-from-midnight) in the data model wrapper (e.g., `UiCustomBlock`) when the list is created/remembered, rather than computing them inside the high-frequency draw loop.
