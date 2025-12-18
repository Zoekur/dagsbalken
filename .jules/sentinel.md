# Sentinel's Journal

## 2025-05-18 - Uncaught JSON Exceptions in Cache Loading
**Vulnerability:** The `WeatherRepository` loads geocoding caches from DataStore using `JSONArray(json)` without error handling.
**Learning:** Even persistent local data can be corrupted or malformed. Assuming it's valid can lead to application crashes (DoS).
**Prevention:** Always wrap parsing logic (JSON, XML, etc.) in `try-catch` blocks, even for internal data sources.
