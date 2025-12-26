# wear-watchface (disabled)

This folder contains early work on a Wear OS watch face.

The module is intentionally **excluded from the Gradle build** to keep Android Studio sync/build stable.

## Re-enable

1. Open `settings.gradle.kts`
2. Uncomment:

- `include(":wear-watchface")`

3. Sync Gradle

## Keep it “out of the way” in Android Studio

- If you see old Run/Debug configurations for the watch face, delete/ignore them.
- Use the `Project` tool window (not `Android`) if you want to collapse the `wear-watchface/` directory.
- If needed, you can right-click `wear-watchface/` → **Mark Directory as** → **Excluded** (IDE-only). This does not affect git.

## Notes

- The phone app (`:app`) is the canonical Dagsbalken app.
- This module may require additional Wear OS / watch face picker work on newer emulator images (API 36+).
