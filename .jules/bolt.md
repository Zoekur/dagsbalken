## 2024-05-23 - Avoiding Object Allocation in Widget Updates
**Learning:** App Widgets updated via `WorkManager` or `AlarmManager` (like `LinearClockWidgetReceiver`) run frequently. Allocating objects like `Paint` inside the generation loop adds unnecessary GC pressure, even if it's "only" once a minute.
**Action:** Use `ThreadLocal<Paint>` for stateless helper objects in singletons that are called from potentially concurrent contexts (Glance composition).

## 2025-12-20 - Caching Native Assets in Widget Generators
**Learning:** Even simple calls like `Typeface.create()` involve JNI overhead. In high-frequency paths like widget bitmap generation (every minute), repeated creation of identical immutable objects (fonts) is wasteful.
**Action:** Use `ConcurrentHashMap.computeIfAbsent` to cache immutable native resources like `Typeface` in the singleton generator, ensuring thread safety and reducing JNI calls.

## 2026-01-05 - Caching ContentProvider Results in Widget Singletons
**Learning:** Widgets that update frequently (e.g., every minute) via `AlarmManager` often trigger expensive operations like `ContentResolver.query` (IPC + DB). For data that changes infrequently (like daily calendar events), fetching on every tick is wasteful.
**Action:** Implement a time-based cache (e.g., 5-minute TTL) within the Widget Singleton (`object`) to reuse expensive query results across frequent updates, drastically reducing IPC and battery usage.
