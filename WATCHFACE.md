# Dagsbalken Watch Face for Galaxy Watch

## Overview
Dagsbalken is a unique watch face for Samsung Galaxy Watch (Wear OS) that displays time as a circular 24-hour timeline. The watch face shows your day's progress with calendar events integrated directly into the timeline.

## Features

### Interactive Mode
- **Circular 24-hour timeline**: A ring showing the full 24-hour day
- **Progress indicator**: The ring fills up as the day progresses
- **Calendar events**: Color-coded event segments on the timeline showing when events occur
- **Digital time**: Large, centered time display (HH:MM format)
- **Date display**: Month and day shown below the time
- **Hour markers**: Visual markers at key hours (0, 6, 9, 12, 15, 18, 21)
- **Current time indicator**: Red dot on the ring showing exact current position
- **Full color**: Purple progress arc with calendar events in their original colors

### Ambient Mode (Battery Saving)
When the watch enters ambient mode (screen dim):
- Simplified black and white design
- No calendar events shown
- No date display
- Reduced visual elements for battery saving
- White progress arc on gray background
- Anti-aliasing disabled for performance

## Installation

1. Build the APK:
   ```bash
   ./gradlew :app:assembleDebug
   ```

2. Install on your Galaxy Watch via ADB or through the Wear OS companion app

3. Select "Dagsbalken" from the watch face picker on your Galaxy Watch

## Technical Details

### Architecture
- Uses Androidx Wear Watchface library (1.2.1)
- Implements `WatchFaceService` with `CanvasRenderer2`
- Hardware-accelerated canvas rendering
- Updates every minute in interactive mode
- Coroutine-based calendar event loading

### Calendar Integration
- Reads calendar events for the current day
- Displays events as colored arcs on the timeline
- Automatically reloads events every hour
- Handles missing calendar permissions gracefully

### Performance
- Minimal battery impact with ambient mode optimization
- Hardware-accelerated rendering
- Efficient Paint object reuse
- Smart update frequency (60 seconds in interactive mode)

## Permissions Required
- `WAKE_LOCK`: Required for watch face functionality
- `READ_CALENDAR`: To display calendar events (optional, works without it)

## Color Scheme

### Interactive Mode
- Background: Black
- Ring background: Dark gray (#444444)
- Progress arc: Purple (#6200EE)
- Calendar events: Original event colors with 180 alpha
- Time/Date text: White
- Current time marker: Red (#FF0000)
- Hour markers: White

### Ambient Mode  
- Background: Black
- Ring background: Medium gray (#666666)
- Progress arc: White
- Time text: White
- Simplified visuals for battery saving

## Future Enhancements
Potential future additions:
- User-customizable colors
- Complications support
- Weather information integration
- Multiple timeline styles (linear, segmented)
- Customizable hour markers
- Theme selection

## Development
The watch face is part of the Dagsbalken Android app and shares core data repositories with the main app and widgets.

Files:
- `app/src/main/java/com/dagsbalken/app/watchface/DagsbalkenWatchFaceService.kt`
- `app/src/main/res/xml/watch_face.xml`
- `app/src/main/AndroidManifest.xml` (service declaration)
