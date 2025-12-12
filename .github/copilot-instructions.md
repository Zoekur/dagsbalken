# GitHub Copilot Instructions for Dagsbalken

## Project Overview

Dagsbalken is an Android calendar and weather application built with Kotlin and Jetpack Compose. The name "Dagsbalken" means "day bar" in Swedish. The app features:

- 24-hour linear timeline visualization with calendar events
- Weather information and forecasts
- Multiple customizable themes (Cold, Warm, and High Contrast variants)
- Home screen widgets (Linear Clock, Weather, and Clothing recommendation)
- Location-based weather data
- Material Design 3 UI

## Architecture

### Module Structure
- **app**: Main application module containing UI, activities, and widgets
- **core**: Shared core module with data repositories, network utilities, and worker classes

### Key Technologies
- **Kotlin 2.2.21**: Primary programming language
- **Jetpack Compose**: Modern declarative UI framework
- **Material Design 3**: UI design system
- **Glance 1.1.1**: App widget framework
- **WorkManager**: Background task scheduling
- **DataStore**: Preferences and settings storage
- **Navigation Compose**: In-app navigation
- **OkHttp**: HTTP client for API calls

### Target Platform
- **Min SDK**: 33 (Android 13)
- **Target SDK**: 36
- **Compile SDK**: 36
- **Java Version**: 17

## Coding Conventions

### Kotlin Style
- Use idiomatic Kotlin patterns
- Prefer immutable data structures (`val` over `var`)
- Use data classes for DTOs and models
- Leverage Kotlin's null safety features
- Use sealed classes for state management
- Follow Kotlin naming conventions (camelCase for functions/variables, PascalCase for classes)

### Jetpack Compose
- Use `@Composable` functions for UI components
- Follow composition over inheritance
- Keep composables small and focused
- Use `remember` and `rememberSaveable` appropriately for state
- Prefer `LaunchedEffect` and `DisposableEffect` for side effects
- Use `derivedStateOf` for computed state
- Follow Material Design 3 guidelines

### Comments
- Code should be self-documenting; avoid obvious comments
- Add comments only when necessary to explain complex logic or business requirements
- Comments are often in Swedish in this codebase, but English is also acceptable
- Document public APIs and non-trivial functions

## Performance Best Practices

### Drawing and Canvas
- **Always use `drawWithCache`** for custom drawing modifiers to optimize performance
- Cache expensive `Paint` and `Path` objects within `drawWithCache` blocks
- Example pattern:
```kotlin
.drawWithCache {
    val paint = Paint().apply { /* configuration */ }
    onDrawBehind {
        drawIntoCanvas { canvas ->
            // Use cached paint object
        }
    }
}
```

### Build Configuration
- **Avoid Gradle milestone versions** - they cause IDE crashes with threading issues
- **Avoid experimental AGP versions** - stick to stable releases
- Use version catalog (`libs.versions.toml`) for dependency management

## Security Guidelines

### Network Configuration
- App uses `networkSecurityConfig` for secure communications
- `allowBackup` is set to `false` for security
- All API endpoints use HTTPS:
  - Open-Meteo API: `api.open-meteo.com`
  - Open-Meteo Geocoding: `geocoding-api.open-meteo.com`
  - OpenStreetMap Nominatim: `nominatim.openstreetmap.org`

### HTTP Client Configuration
- Use `OkHttpClient` with appropriate timeouts:
  - Connect timeout: 10 seconds
  - Write timeout: 10 seconds
  - Read timeout: 10 seconds
  - Enable `retryOnConnectionFailure`

### Permissions
- Handle runtime permissions properly (Calendar, Location, Internet)
- Request permissions only when needed
- Provide clear rationale to users

## Theme System

### Theme Options
The app uses a custom theme system with the following options:
- **Cold**: Cool color palette with Nordic-inspired colors
- **Warm**: Warm color palette with solar-inspired colors
- **ColdHighContrast**: High contrast cold theme
- **WarmHighContrast**: High contrast warm theme

### Theme Migration
- Use legacy mapping in `ThemePreferences` for backward compatibility
- Old theme names map to new names: `NordicCalm` → `Cold`, `SolarDawn` → `Warm`

### Timeline Colors
- Each theme defines `timelineNightColor` and `timelineDayColor`
- Timeline shows 24-hour gradient background with event blocks

## UI Components

### LinearDayCard
- Displays 24-hour timeline with gradient background
- Shows event blocks from calendar
- Hour ticks and labels
- Current time marker
- Custom drawing using Canvas and `drawWithCache`

### Layout Guidelines
- Use `statusBarsPadding()` modifier to prevent UI elements from being obscured by the Android status bar
- Follow Material Design spacing and elevation guidelines
- Maintain consistent padding and margins

## Data Management

### Repositories
- `WeatherRepository`: Handles weather data fetching and location services
- Repository pattern for data access
- Use coroutines for asynchronous operations

### Preferences
- Use DataStore for key-value storage
- Preferences are stored reactively using Flow
- Theme preferences support migration from legacy values

## Widget Development

### Glance Widgets
- Three widgets: Linear Clock, Weather, and Clothing recommendation
- Use Glance framework (not RemoteViews)
- Workers update widgets on schedule and boot
- `BootCompletedReceiver` ensures widgets update after device restart

## Testing

### Test Structure
- Unit tests in `app/src/test/`
- Instrumented tests in `app/src/androidTest/`
- Use JUnit 4 for testing
- Compose UI testing with `ui-test-junit4`

### Testing Guidelines
- Write tests for business logic and data transformations
- Test composable functions with Compose testing framework
- Mock external dependencies (network, database)
- Test edge cases and error scenarios

## Dependencies

### Adding New Dependencies
- Add to `gradle/libs.versions.toml` version catalog
- Use stable versions, avoid alpha/beta when possible
- Consider app size impact
- Check for existing solutions before adding new libraries

### Current Key Dependencies
- Compose BOM for unified Compose versions
- Material Icons Extended for icon support
- Accompanist System UI Controller for system bars
- WorkManager for background tasks

## Common Patterns

### State Management
```kotlin
val uiState by viewModel.uiState.collectAsState()
```

### Navigation
```kotlin
navController.navigate("route") {
    // Navigation options
}
```

### Side Effects
```kotlin
LaunchedEffect(key) {
    // Effect code
}
```

### Settings/Preferences
```kotlin
val settingFlow = context.dataStore.data.map { preferences ->
    preferences[KEY]
}
```

## File Organization

### Package Structure
```
com.dagsbalken.app/
├── widget/          # Widget receivers and providers
├── ui/
│   ├── theme/      # Theme definitions and colors
│   └── settings/   # Settings screens and preferences
└── MainActivity.kt  # Main app entry point

com.dagsbalken.core/
├── data/           # Repositories and network utilities
├── widget/         # Shared widget utilities
└── workers/        # WorkManager workers
```

## Additional Notes

- Application is primarily in Swedish (comments, strings), but code should be readable
- Focus on Material Design 3 aesthetics
- Prioritize performance and battery efficiency
- Consider accessibility (high contrast themes available)
- Respect user privacy (no analytics or tracking mentioned)

## When Making Changes

1. **Minimal Changes**: Make the smallest possible changes to achieve the goal
2. **Test**: Run relevant tests after changes
3. **Build**: Ensure the project builds successfully with `./gradlew build`
4. **Lint**: Follow existing code style and patterns
5. **Document**: Update comments or documentation if changing public APIs
6. **Security**: Never introduce security vulnerabilities
7. **Performance**: Consider performance implications, especially for UI and drawing code
