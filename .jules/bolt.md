## 2024-05-23 - Avoiding Object Allocation in Widget Updates
**Learning:** App Widgets updated via `WorkManager` or `AlarmManager` (like `LinearClockWidgetReceiver`) run frequently. Allocating objects like `Paint` inside the generation loop adds unnecessary GC pressure, even if it's "only" once a minute.
**Action:** Use `ThreadLocal<Paint>` for stateless helper objects in singletons that are called from potentially concurrent contexts (Glance composition).

## 2025-12-20 - Caching Native Assets in Widget Generators
**Learning:** Even simple calls like `Typeface.create()` involve JNI overhead. In high-frequency paths like widget bitmap generation (every minute), repeated creation of identical immutable objects (fonts) is wasteful.
**Action:** Use `ConcurrentHashMap.computeIfAbsent` to cache immutable native resources like `Typeface` in the singleton generator, ensuring thread safety and reducing JNI calls.

## 2026-06-15 - Lambda Allocation in Hot Paths
**Learning:** Kotlin's `computeIfAbsent` with a capturing lambda (e.g., capturing `config.font`) allocates a new lambda object on every call, even if the key exists. In hot paths (like widget generation every minute), this constant allocation adds up.
**Action:** Use explicit `get()` then `putIfAbsent()` (double-checked pattern) to avoid creating the lambda object when the value is already cached.
