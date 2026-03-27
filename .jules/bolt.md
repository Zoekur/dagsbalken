## 2024-05-23 - Avoiding Object Allocation in Widget Updates
**Learning:** App Widgets updated via `WorkManager` or `AlarmManager` (like `LinearClockWidgetReceiver`) run frequently. Allocating objects like `Paint` inside the generation loop adds unnecessary GC pressure, even if it's "only" once a minute.
**Action:** Use `ThreadLocal<Paint>` for stateless helper objects in singletons that are called from potentially concurrent contexts (Glance composition).

## 2025-12-20 - Caching Native Assets in Widget Generators
**Learning:** Even simple calls like `Typeface.create()` involve JNI overhead. In high-frequency paths like widget bitmap generation (every minute), repeated creation of identical immutable objects (fonts) is wasteful.
**Action:** Use `ConcurrentHashMap.computeIfAbsent` to cache immutable native resources like `Typeface` in the singleton generator, ensuring thread safety and reducing JNI calls.

## 2025-12-11 - Java/Kotlin Lambda Allocation in Map Operations
**Learning:** `computeIfAbsent` in Kotlin when calling Java collections (`ConcurrentHashMap`) causes implicit lambda wrapper allocation on every call, even if the key exists. This is because the lambda is converted to a Java `Function` instance.
**Action:** For hot paths (like widget drawing every minute), replace `computeIfAbsent` with manual `get` -> `null check` -> `putIfAbsent` logic to avoid this allocation completely on the "hit" path.
