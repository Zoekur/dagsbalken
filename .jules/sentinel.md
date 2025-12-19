# Sentinel's Journal

## 2025-05-18 - Uncaught JSON Exceptions in Cache Loading
**Vulnerability:** The `WeatherRepository` loads geocoding caches from DataStore using `JSONArray(json)` without error handling.
**Learning:** Even persistent local data can be corrupted or malformed. Assuming it's valid can lead to application crashes (DoS).
**Prevention:** Always wrap parsing logic (JSON, XML, etc.) in `try-catch` blocks, even for internal data sources.

## 2025-05-19 - Transitive Build Vulnerabilities
**Vulnerability:** High-severity vulnerabilities detected in build tool dependencies (`protobuf-java` DoS, `jdom` XXE, `netty` HTTP/2, `commons-compress` DoS) via Dependabot.
**Learning:** Build tools and plugins pull in their own transitive dependencies which may become outdated and vulnerable, even if the application code doesn't use them directly.
**Prevention:** Explicitly enforce safe versions of vulnerable transitive dependencies using `resolutionStrategy` in the root `build.gradle.kts` (targeting both `buildscript` and `allprojects` configurations).
