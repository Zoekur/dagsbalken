# Dagsbalken Watch Face for Galaxy Watch

## Overview
Dagsbalken is a unique watch face for Samsung Galaxy Watch (Wear OS) that displays time as a horizontal 24-hour timeline with a day/night gradient. The watch face captures the essence of the Dagsbalken mobile app by showing your day's progress with calendar events integrated directly into the timeline.

## Features

### Interactive Mode
- **Horizontal 24-hour timeline**: A bar at the bottom showing the full 24-hour day
- **Day/night gradient background**: Visual representation of day and night cycles using theme-specific colors
  - Cold theme: Deep blue (night) to light blue (day)
  - Warm theme: Deep orange (night) to yellow (day)
  - High contrast variants for accessibility
- **Calendar events**: Color-coded event blocks on the timeline showing when events occur
- **Digital time**: Large, centered time display (HH:MM format) above the timeline
- **Date display**: Month and day shown below the time
- **Hour markers**: Visual markers every 3 hours on the timeline
- **Current time indicator**: Red vertical line and dot showing exact current position on timeline
- **Sunrise/sunset indicators**: Small colored circles at 8:00 (yellow) and 20:00 (orange) positions
- **Passed time overlay**: Semi-transparent gray overlay showing elapsed portion of the day

### Ambient Mode (Battery Saving / AOD)
When the watch enters ambient mode (screen dim):
- Simplified black and white design
- Configurable vertical position (Top/Middle/Bottom)
- Simple timeline bar showing progress
- Current time indicator
- No calendar events shown
- No date display
- Minimal visual elements for battery saving
- Anti-aliasing disabled for performance

### Customization Options
Access via watch face settings (long-press on watch face, then "Customize"):

1. **Theme Selection**
   - Cold: Blue color palette (Nordic-inspired)
   - Warm: Orange/yellow palette (Solar-inspired)
   - Cold High Contrast: Black background with cyan accents
   - Warm High Contrast: Black background with yellow/orange accents

2. **AOD Position**
   - Top: Time display at 30% from top
   - Middle: Time display centered (50%)
   - Bottom: Time display at 70% from top

3. **Show Events**
   - Toggle to show/hide calendar events on timeline
   - Useful for privacy or simplicity

## Installation

1. Build the APK:
   ```bash
   ./gradlew :app:assembleDebug
   ```

2. Install on your Galaxy Watch via ADB or through the Wear OS companion app

3. Select "Dagsbalken" from the watch face picker on your Galaxy Watch

4. Long-press the watch face to customize themes and settings

## Technical Details

### Architecture
- Uses Androidx Wear Watchface library (1.2.1)
- Implements `WatchFaceService` with `CanvasRenderer2`
- Hardware-accelerated canvas rendering
- Updates every minute in interactive mode
- Coroutine-based calendar event loading
- UserStyleSchema for watch face customization

### Calendar Integration
- Reads calendar events for the current day
- Displays events as colored blocks on the timeline
- Automatically reloads events every hour
- Handles missing calendar permissions gracefully

### Performance
- Minimal battery impact with ambient mode optimization
- Hardware-accelerated rendering
- Efficient Paint object reuse within drawWithCache blocks
- Smart update frequency (60 seconds in interactive mode)
- Gradient shader computed once per frame

### Design Philosophy
The watch face design translates Dagsbalken's core concept - a horizontal linear timeline showing the day's events - to a round watch form factor by:
- Placing the timeline as a horizontal bar that spans the full width
- Using familiar day/night gradient colors from the mobile app themes
- Maintaining the "passed time" gray overlay concept
- Keeping the red current-time indicator as a focal point
- Adding sunrise/sunset as temporal anchors on the timeline

## Permissions Required
- `WAKE_LOCK`: Required for watch face functionality
- `READ_CALENDAR`: To display calendar events (optional, works without it)
- Feature: `android.hardware.type.watch`: Identifies app as Wear OS compatible (not required)

## Color Scheme

### Cold Theme (Interactive Mode)
- Background: Black
- Night gradient: Deep Blue (#1A237E)
- Day gradient: Light Blue (#4FC3F7)
- Accent/current time: Red (#FF0000)
- Calendar events: Original event colors with 200 alpha
- Hour markers: White (150 alpha)
- Sunrise: Yellow/Orange (#FFB300)
- Sunset: Orange/Red (#FF6D00)

### Warm Theme (Interactive Mode)  
- Background: Black
- Night gradient: Deep Orange (#BF360C)
- Day gradient: Yellow (#FFEB3B)
- Accent/current time: Red (#FF0000)
- Other elements: Same as Cold theme

### High Contrast Themes
- Simplified color palettes for better visibility
- Cold HC: Black to Cyan gradient with Yellow accent
- Warm HC: Black to Yellow gradient with Orange accent

### Ambient Mode (All Themes)
- Background: Black
- Timeline background: Dark gray (#333333)
- Progress: White
- Current time line: White
- Time text: White
- Simplified visuals for battery saving

## Future Enhancements
Potential future additions:
- Weather integration on timeline
- Complications support for additional data
- Custom sunrise/sunset times based on location
- Week view option
- Customizable hour marker intervals
- Animation transitions
- More theme variants

## Development
The watch face is part of the Dagsbalken Android app and shares core data repositories with the main app and widgets.

Files:
- `app/src/main/java/com/dagsbalken/app/watchface/DagsbalkenWatchFaceService.kt`
- `app/src/main/res/xml/watch_face.xml`
- `app/src/main/AndroidManifest.xml` (service declaration)
- `app/src/main/res/values/strings.xml` (user style strings)

## Troubleshooting

### "Missing uses-feature watch" error
This has been resolved by adding the `uses-feature` declaration for `android.hardware.type.watch` in the manifest. The feature is marked as `required="false"` so the app can still run on non-watch devices.

### Calendar events not showing
1. Check that calendar permission is granted
2. Verify that "Show Events" is enabled in watch face settings
3. Ensure you have events scheduled for today
4. Events are only shown in interactive mode, not AOD

### Watch face not appearing in picker
1. Ensure the APK is installed correctly
2. Check that the service is properly declared in manifest
3. Restart the watch
4. Check logcat for any errors during watch face initialization
