## 2025-05-18 - Uncaught JSON Exceptions in Cache Loading
**Vulnerability:** The `WeatherRepository` loads geocoding caches from DataStore using `JSONArray(json)` without error handling.
**Learning:** Even persistent local data can be corrupted or malformed. Assuming it's valid can lead to application crashes (DoS).
**Prevention:** Always wrap parsing logic (JSON, XML, etc.) in `try-catch` blocks, even for internal data sources.

## 2025-05-19 - Transitive Build Vulnerabilities
**Vulnerability:** High-severity vulnerabilities detected in build tool dependencies (`protobuf-java` DoS, `jdom` XXE, `netty` HTTP/2, `commons-compress` DoS) via Dependabot.
**Learning:** Build tools and plugins pull in their own transitive dependencies which may become outdated and vulnerable, even if the application code doesn't use them directly.
**Prevention:** Explicitly enforce safe versions of vulnerable transitive dependencies using `resolutionStrategy` in the root `build.gradle.kts` (targeting both `buildscript` and `allprojects` configurations).

## 2025-12-12 - Data Integrity in DataStore Transactions
**Vulnerability:** Swallowing exceptions during data deserialization within a read-modify-write cycle (DataStore `edit` block) leads to data loss. The application would read a "safe" empty list instead of failing, and then overwrite the corrupted on-disk data with a new, nearly empty state.
**Learning:** "Fail Securely" applies to data integrity too. If data is corrupted, it is better to abort a write transaction than to silently overwrite user data with a reset state.
**Prevention:** Allow deserialization exceptions to propagate within DataStore `edit` blocks to trigger automatic transaction abortion. Handle exceptions only at the UI read layer (Flows) to prevent crashes.
