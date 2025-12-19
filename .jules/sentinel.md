# Sentinel's Journal

## 2025-05-18 - Uncaught JSON Exceptions in Cache Loading
**Vulnerability:** The `WeatherRepository` loads geocoding caches from DataStore using `JSONArray(json)` without error handling.
**Learning:** Even persistent local data can be corrupted or malformed. Assuming it's valid can lead to application crashes (DoS).
**Prevention:** Always wrap parsing logic (JSON, XML, etc.) in `try-catch` blocks, even for internal data sources.

## 2025-05-19 - DoS Risk in Exported Widget Receivers
**Vulnerability:** The `LinearClockWidgetReceiver` was exported (required for system updates) but also handled an internal `ACTION_UPDATE_TICK`. This allowed any external app to spam the update intent, causing resource exhaustion (CPU/Battery) by triggering expensive bitmap generation repeatedly.
**Learning:** Receivers dealing with internal logic (like alarms) should be separate from those handling system broadcasts.
**Prevention:** Split receivers: keep the system-facing receiver exported, but move internal logic (alarms, ticks) to a separate, non-exported receiver.
