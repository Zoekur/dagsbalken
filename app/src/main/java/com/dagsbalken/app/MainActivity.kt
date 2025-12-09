package com.dagsbalken.app

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dagsbalken.app.ui.MainViewModel
import com.dagsbalken.app.ui.icons.DagsbalkenIcons
import com.dagsbalken.app.ui.settings.SettingsScreen
import com.dagsbalken.app.ui.settings.ThemePreferences
import com.dagsbalken.app.ui.theme.DagsbalkenTheme
import com.dagsbalken.app.ui.theme.ThemeOption
import com.dagsbalken.app.ui.theme.ThemeSelector
import com.dagsbalken.core.data.CalendarRepository
import com.dagsbalken.core.data.DayEvent
import com.dagsbalken.core.data.WeatherData
import com.dagsbalken.core.data.WeatherRepository
import com.dagsbalken.core.data.WeatherLocationSettings
import com.dagsbalken.core.workers.WeatherWorker
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// -------- HUVUDAKTIVITET --------
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val themePrefs = ThemePreferences(applicationContext)
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(themePrefs) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable Edge to Edge to draw behind system bars
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView)?.let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // Schemalägger väder-workern att starta så snart appen öppnas
        WeatherWorker.schedule(applicationContext)

        setContent {
            val themeOption by viewModel.themeOptionFlow.collectAsState(initial = ThemeOption.NordicCalm)

            DagsbalkenTheme(themeOption = themeOption) {
                Surface(Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            LinearClockScreen(
                                themeOption = themeOption,
                                onThemeOptionChange = {
                                    viewModel.onThemeOptionChange(it)
                                },
                                onSettingsClick = {
                                    navController.navigate("settings")
                                }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(onBack = {
                                navController.popBackStack()
                            })
                        }
                    }
                }
            }
        }
    }
}

// -------- SKÄRM (Kombinerar tidslinje & kort) --------
@Composable
fun LinearClockScreen(
    themeOption: ThemeOption,
    onThemeOptionChange: (ThemeOption) -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val weatherRepository = remember { WeatherRepository(context) }
    val calendarRepository = remember { CalendarRepository(context) }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // Samlar in väderdata från DataStore i realtid
    val weatherData by weatherRepository.weatherDataFlow.collectAsState(initial = WeatherData())

    // --- New: observe location settings and request location permission if needed ---
    val locationSettings by weatherRepository.locationSettingsFlow.collectAsState(initial = WeatherLocationSettings())

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                // Trigger a fetch since permission is now granted
                scope.launch { weatherRepository.fetchAndSaveWeatherOnce() }
            } else {
                // If denied, fallback: save setting to use manual location (handled elsewhere in SettingsScreen)
            }
        }
    )

    // When the location setting changes, trigger an immediate fetch
    LaunchedEffect(locationSettings.useCurrentLocation, locationSettings.manualLocationName) {
        if (locationSettings.useCurrentLocation) {
            // Ensure we have location permission before attempting a GPS-based fetch
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                weatherRepository.fetchAndSaveWeatherOnce()
            } else {
                // Ask for permission (the permission result path will trigger fetch if granted)
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        } else {
            // Manual location selected -> fetch for manual name
            weatherRepository.fetchAndSaveWeatherOnce()
        }
    }

    // Events state
    var events by remember { mutableStateOf(emptyList<DayEvent>()) }

    // Funktion för att ladda events
    fun loadEvents() {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALENDAR
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            scope.launch {
                events = calendarRepository.getEventsForToday()
            }
        }
    }

    // Permission launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                loadEvents()
            }
        }
    )

    // Lyssna på Lifecycle ON_RESUME för att uppdatera events om användaren ändrat kalendern
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                loadEvents()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Initiera laddning vid start om permission finns, annars fråga
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            launcher.launch(Manifest.permission.READ_CALENDAR)
        } else {
            loadEvents()
        }
    }

    val now by rememberTicker1s()

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP HEADER: Title (Left) + Settings (Right)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dagsbalken",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )

            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = DagsbalkenIcons.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Theme Selector
        ThemeSelector(
            selectedOption = themeOption,
            onOptionSelected = onThemeOptionChange,
        )

        Spacer(Modifier.height(24.dp))

        // 1. Tidslinjen (Huvudkomponenten) - Nu med dubbel höjd och hela dygnet
        LinearDayCard(
            now = now.toLocalTime(),
            height = 168.dp,
            events = events
        )

        Spacer(Modifier.height(16.dp))

        // 2. Nästa Händelse (Tilläggsinformation)
        NextEventCard(events = events, now = now.toLocalTime())

        Spacer(Modifier.height(24.dp))

        // 3. Väder- och Klädrådsrutor
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Väderinformationsruta (Vänster)
            WeatherInfoCard(
                modifier = Modifier.weight(1f),
                data = weatherData,
                onRefresh = {
                    scope.launch {
                        val success = weatherRepository.fetchAndSaveWeatherOnce()
                        Toast.makeText(context, if (success) "Uppdaterat väder" else "Uppdatering misslyckades — fallback användes", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            // Klädrådsruta (Höger)
            ClothingAdviceCard(modifier = Modifier.weight(1f), data = weatherData)
        }

        Spacer(Modifier.height(16.dp))

        // Version info
        Text(
            text = "v${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray.copy(alpha = 0.5f)
        )
    }
}

// ----------------------------------------------------------
// 1. TIDSLINJE KOMPONENTER
// ----------------------------------------------------------

@Composable
fun LinearDayCard(
    now: LocalTime,
    height: Dp = 160.dp,
    events: List<DayEvent> = emptyList()
) {
    val hourLabelPaint = remember {
        Paint().apply {
            textAlign = Paint.Align.CENTER
            textSize = 36f
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
        }
    }
    val hourLabelColor = MaterialTheme.colorScheme.onSurface.toArgb()
    SideEffect { hourLabelPaint.color = hourLabelColor }

    val cornerRadiusDp = 28.dp

    // Theme colors
    val surfaceColor = MaterialTheme.colorScheme.surface
    val trackBgColor = MaterialTheme.colorScheme.surfaceVariant
    val borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
    val tickColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val passedColor = Color(0xFFB7EA27)
    val nowColor = Color(0xFFEF4444)

    Box(
        Modifier
            .fillMaxWidth()
            .height(height + 24.dp)
            .background(Color.Transparent)
    ) {
        // Yttre kapsel (Bakgrund)
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(height)
                .background(surfaceColor, RoundedCornerShape(cornerRadiusDp))
        )

        // Canvas för tidslinje (00 - 24)
        Canvas(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(height)
        ) {
            val pad = 20.dp.toPx()
            val trackTop = size.height * 0.10f
            val trackHeight = size.height * 0.80f
            val right = size.width - pad
            val trackWidth = right - pad
            val left = pad
            val cornerRadiusPx = 24f

            // Inre kapsel (Bakgrund)
            drawRoundRect(
                color = trackBgColor,
                topLeft = Offset(left, trackTop),
                size = Size(trackWidth, trackHeight),
                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
            )

            // Beräkna pixlar per minut för HELA dygnet (24h = 1440 min)
            val totalMinutes = 24 * 60
            val pxPerMin = trackWidth / totalMinutes

            // Rita events
            events.forEach { event ->
                val startMin = event.start.hour * 60 + event.start.minute
                val endMin = (event.end?.hour ?: 0) * 60 + (event.end?.minute ?: 0)

                // Enkel hantering av events som går över midnatt eller saknar slut -> visa 1h
                val actualEndMin = if (event.end != null && endMin > startMin) endMin else startMin + 60

                val eventStartPx = left + (startMin * pxPerMin)
                val eventWidthPx = (actualEndMin - startMin) * pxPerMin

                // Rita event som ett färgat block (svagt)
                drawRect(
                    color = Color(event.color).copy(alpha = 0.3f),
                    topLeft = Offset(eventStartPx, trackTop),
                    size = Size(eventWidthPx, trackHeight)
                )
            }

            // Nuvarande tid i minuter
            val currentMinutes = now.hour * 60 + now.minute
            val currentX = left + (currentMinutes * pxPerMin)

            // Grön fyllnad (Från 00:00 till nu)
            val passedWidth = currentX - left
            if (passedWidth > 0) {
                 drawRoundRect(
                    color = passedColor,
                    topLeft = Offset(left, trackTop),
                    size = Size(passedWidth, trackHeight),
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                )
                 // Om "passed" är bredare än radiens kurva, rita en fyrkant över högra hörnen
                 // för att få en skarp kant mot "framtiden" (eller behåll rundad om det är designen)
                 // Här behåller vi den klippt vid 'currentX' men säkerställer att vi inte ritar utanför vänster kant.
                 if (passedWidth > cornerRadiusPx) {
                     // Fyll ut högra hörnen så det ser ut som progress bar som fortsätter
                     drawRect(
                         color = passedColor,
                         topLeft = Offset(currentX - 10f, trackTop), // Lite överlapp
                         size = Size(10f, trackHeight)
                     )
                 }
            }

            // Inre kapsel (Border - ritas ovanpå för snyggare kant)
            drawRoundRect(
                color = borderColor,
                topLeft = Offset(left, trackTop),
                size = Size(trackWidth, trackHeight),
                style = Stroke(width = 2f),
                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
            )

            // Loopa igenom 24 timmar
            for (h in 0..24) {
                val min = h * 60
                val x = left + (min * pxPerMin)

                // Rita Tim-markering (långt streck)
                drawLine(
                    color = tickColor,
                    start = Offset(x, trackTop),
                    end = Offset(x, trackTop + trackHeight * 0.4f),
                    strokeWidth = 2f
                )

                // Text: Endast för 6, 12, 18, 24
                if (h in listOf(6, 12, 18)) {
                    val label = h.toString()
                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        x,
                        trackTop + trackHeight / 2f + 18f,
                        hourLabelPaint
                    )
                }

                // Halvtimmar (korta streck) - men inte efter 24
                if (h < 24) {
                    val halfMin = min + 30
                    val halfX = left + (halfMin * pxPerMin)
                    drawLine(
                        color = tickColor.copy(alpha = 0.3f),
                        start = Offset(halfX, trackTop),
                        end = Offset(halfX, trackTop + trackHeight * 0.2f),
                        strokeWidth = 2f
                    )
                }
            }

            // Nu-markör (röd linje)
            drawLine(
                color = nowColor,
                start = Offset(currentX, trackTop),
                end = Offset(currentX, trackTop + trackHeight),
                strokeWidth = 4f,
                cap = StrokeCap.Square
            )
        }
    }
}

// -------- NÄSTA HÄNDELSE --------
@Composable
fun NextEventCard(events: List<DayEvent>, now: LocalTime) {
    val sortedEvents = remember(events) { events.sortedBy { it.start } }
    val next = remember(sortedEvents, now) {
        // Hitta nästa händelse som inte har passerat (minus 1 minut för att hantera tickern)
        sortedEvents.firstOrNull { !it.start.isBefore(now.minusMinutes(1)) }
    } ?: return

    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFFF7F7F7), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(next.icon ?: "•", fontSize = 22.sp, modifier = Modifier.padding(end = 8.dp))
        Column {
            Text(next.title, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Text(
                text = "${next.start.format(DateTimeFormatter.ofPattern("HH:mm"))}${
                    next.end?.let {
                        " – ${it.format(DateTimeFormatter.ofPattern("HH:mm"))}"
                    } ?: ""
                }",
                color = Color(0xFF6B7280),
                fontSize = 14.sp
            )
        }
    }
}

// ----------------------------------------------------------
// 2. VÄDER-KOMPONENT (Uppdaterad för att ta emot data)
// ----------------------------------------------------------

@Composable
fun WeatherInfoCard(modifier: Modifier = Modifier, data: WeatherData, onRefresh: () -> Unit = {}) {
    Card(
        modifier = modifier.height(200.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!data.isDataLoaded) {
                // Laddningsindikator
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Laddar väderdata...", fontSize = 14.sp, color = Color.Gray)
                }
            } else {
                // Visar riktig data
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Show location name as title (fallback to generic label)
                    val title = if (data.locationName.isNotBlank()) data.locationName else "Väderinformation"
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    // Show provider small label
                    if (data.provider.isNotBlank()) {
                        Text(
                            text = data.provider,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${data.temperatureCelsius}°C",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "${data.precipitationChance}% risk för nederbörd",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )

                    Spacer(Modifier.height(8.dp))

                    // Last updated row (click to refresh)
                    val lastUpdatedText = if (data.lastUpdatedMillis > 0L) {
                        try {
                            val instant = java.time.Instant.ofEpochMilli(data.lastUpdatedMillis)
                            val z = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
                            val fmt = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
                            "Uppdaterad senast: ${z.format(fmt)}"
                        } catch (e: Exception) {
                            "Uppdaterad senast: --:--"
                        }
                    } else "Uppdaterad senast: --:--"

                    Text(
                        text = lastUpdatedText,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onRefresh() }
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------------
// 3. KLÄDRÅDS-KOMPONENT (Uppdaterad för att ta emot data)
// ----------------------------------------------------------

@Composable
fun ClothingAdviceCard(modifier: Modifier = Modifier, data: WeatherData) {
    Card(
        modifier = modifier.height(200.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!data.isDataLoaded) {
                // Laddningsindikator (synkroniserad med väderkortet)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(data.adviceIcon, fontSize = 48.sp)
                    Text(
                        data.adviceText,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                // Visar klädråd baserat på logik i WeatherRepository
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Klädråd",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(data.adviceIcon, fontSize = 48.sp)
                    Text(
                        data.adviceText,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// -------- HJÄLPARE --------

// -------- ENKEL TICKER --------
@Composable
fun rememberTicker1s(): State<LocalDateTime> {
	val state = remember { mutableStateOf(LocalDateTime.now()) }
	LaunchedEffect(Unit) {
		while (true) {
			// Uppdatera varje minut (eller oftare vid behov, men minut räcker för UI)
			state.value = LocalDateTime.now()
			delay(60000)
		}
	}
	return state
}
