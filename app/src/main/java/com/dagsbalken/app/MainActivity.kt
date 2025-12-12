package com.dagsbalken.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.provider.CalendarContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.graphics.Brush
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
        // We don't need edge-to-edge for this simple fullscreen style; let the system handle status bar.
        // enableEdgeToEdge()
        // Remove custom system bar handling so Android draws a normal status bar
        // WindowCompat.setDecorFitsSystemWindows(window, false)
        // WindowCompat.getInsetsController(window, window.decorView)?.let { controller ->
        //     controller.show(WindowInsetsCompat.Type.statusBars())
        //     controller.systemBarsBehavior =
        //         WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // }

        // Schemalägger väder-workern att starta så snart appen öppnas
        WeatherWorker.schedule(applicationContext)

        setContent {
            val themeOption by viewModel.themeOptionFlow.collectAsState(initial = ThemeOption.Cold)

            DagsbalkenTheme(themeOption = themeOption) {
                // Flytta bakgrundsfärgen hit så vi inte får en separat "rand" i toppen
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
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
                            SettingsScreen(
                                currentTheme = themeOption,
                                onThemeSelected = { viewModel.onThemeOptionChange(it) },
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
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

    Box(
        Modifier
            .fillMaxSize()
            // Ingen egen "panel"-bakgrund här; endast global Surface-bakgrund används
            .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // Logo/Title placeholder (Top Left, separate from card)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Dagsbalken",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }

            // 1. Tidslinjen
            LinearDayCard(
                now = now.toLocalTime(),
                height = 168.dp,
                events = events,
                themeOption = themeOption
            )

            Spacer(Modifier.height(16.dp))

            // 2. Nästa Händelse (Tilläggsinformation)
            NextEventCard(
                events = events,
                now = now.toLocalTime(),
                onAddEventClick = {
                    val intent = Intent(Intent.ACTION_INSERT)
                        .setData(CalendarContract.Events.CONTENT_URI)
                    context.startActivity(intent)
                }
            )

            Spacer(Modifier.height(24.dp))

            // 3. Väder- och Klädrådsrutor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                WeatherInfoCard(
                    modifier = Modifier.weight(1f),
                    data = weatherData,
                    onRefresh = {
                        scope.launch {
                            val success = weatherRepository.fetchAndSaveWeatherOnce()
                            Toast.makeText(
                                context,
                                if (success) "Uppdaterat väder" else "Uppdatering misslyckades — fallback användes",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )

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

        // Settings Icon (Floating TopEnd)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
        ) {
            IconButton(
                onClick = onSettingsClick
            ) {
                Icon(
                    imageVector = DagsbalkenIcons.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// ----------------------------------------------------------
// 1. TIDSLINJE KOMPONENTER
// ----------------------------------------------------------

@Composable
fun LinearDayCard(
    now: LocalTime,
    height: Dp = 160.dp,
    events: List<DayEvent> = emptyList(),
    themeOption: ThemeOption
) {
    val cornerRadiusDp = 28.dp

    // Theme colors
    val tickColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val majorTickColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
    val nowColor = Color(0xFFEF4444)

    Box(
        Modifier
            .fillMaxWidth()
            .height(height)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(cornerRadiusDp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(cornerRadiusDp))
    ) {
        // Tidslinje med drawWithCache för att undvika objektallokeringar i draw-loopen (CodeQL fix)
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val width = size.width
                    val heightPx = size.height
                    val cornerRadiusPx = cornerRadiusDp.toPx()

                    // Skapa Gradient Brush i cache-blocket (undviker allokering vid varje ritning)
                    val gradientBrush = Brush.horizontalGradient(
                        0.0f to themeOption.timelineNightColor,
                        0.5f to themeOption.timelineDayColor,
                        1.0f to themeOption.timelineNightColor,
                        startX = 0f,
                        endX = width
                    )

                    // Förbereda Path objekt i cache-blocket
                    val cardPath = androidx.compose.ui.graphics.Path()
                    cardPath.addRoundRect(
                        androidx.compose.ui.geometry.RoundRect(
                            0f, 0f, width, heightPx,
                            androidx.compose.ui.geometry.CornerRadius(cornerRadiusPx)
                        )
                    )

                    // Beräkna konstanter
                    val pxPerMin = width / (24 * 60)

                    onDrawBehind {
                        // Nuvarande tid
                        val currentMinutes = now.hour * 60 + now.minute
                        val currentX = currentMinutes * pxPerMin

                        // 1. Gradient Background (Endast passerad tid)
                        // Clip drawing to card shape
                        clipPath(cardPath) {
                            drawRect(
                                brush = gradientBrush,
                                topLeft = Offset.Zero,
                                size = Size(currentX, heightPx)
                            )
                        }

                        // 2. Rita events (Använd index-loop för att undvika iterator-allokering)
                        for (i in events.indices) {
                            val event = events[i]
                            val startMin = event.start.hour * 60 + event.start.minute
                            val endMin = (event.end?.hour ?: 0) * 60 + (event.end?.minute ?: 0)
                            val actualEndMin = if (event.end != null && endMin > startMin) endMin else startMin + 60

                            val eventStartPx = startMin * pxPerMin
                            val eventWidthPx = (actualEndMin - startMin) * pxPerMin

                            drawRect(
                                color = Color(event.color).copy(alpha = 0.3f),
                                topLeft = Offset(eventStartPx, 0f),
                                size = Size(eventWidthPx, heightPx)
                            )
                        }

                        // 3. Ticks
                        for (h in 0 until 24) {
                            val min = h * 60
                            val x = min * pxPerMin

                            val isMajor = h == 6 || h == 12 || h == 18
                            val tickHeight = if (isMajor) heightPx * 0.4f else heightPx * 0.2f
                            val tickStroke = if (isMajor) 4f else 2f
                            val color = if (isMajor) majorTickColor else tickColor

                            val startY = (heightPx - tickHeight) / 2f
                            val endY = startY + tickHeight

                            drawLine(
                                color = color,
                                start = Offset(x, startY),
                                end = Offset(x, endY),
                                strokeWidth = tickStroke,
                                cap = StrokeCap.Round
                            )
                        }

                        // 4. Nu-markör (röd linje)
                        drawLine(
                            color = nowColor,
                            start = Offset(currentX, 0f),
                            end = Offset(currentX, heightPx),
                            strokeWidth = 4f,
                            cap = StrokeCap.Square
                        )
                    }
                }
        )
    }
}

// -------- NÄSTA HÄNDELSE --------
@Composable
fun NextEventCard(events: List<DayEvent>, now: LocalTime, onAddEventClick: () -> Unit) {
    val sortedEvents = remember(events) { events.sortedBy { it.start } }
    val next = remember(sortedEvents, now) {
        // Hitta nästa händelse som inte har passerat (minus 1 minut för att hantera tickern)
        sortedEvents.firstOrNull { !it.start.isBefore(now.minusMinutes(1)) }
    }

    if (next == null) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .innerShadow(
                    color = Color.Black.copy(alpha = 0.2f),
                    blur = 6.dp,
                    offsetY = 2.dp,
                    offsetX = 2.dp,
                    cornerRadius = 16.dp
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Inget planerat",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconButton(onClick = onAddEventClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Lägg till event",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    } else {
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
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

// -------- INNER SHADOW MODIFIER --------
fun Modifier.innerShadow(
    color: Color,
    blur: Dp = 6.dp,
    offsetY: Dp = 2.dp,
    offsetX: Dp = 2.dp,
    cornerRadius: Dp = 0.dp
) = this.drawWithCache {
    val paint = android.graphics.Paint().apply {
        this.color = color.toArgb()
        this.isAntiAlias = true
        this.style = android.graphics.Paint.Style.STROKE
        this.strokeWidth = blur.toPx() * 2
        this.maskFilter = android.graphics.BlurMaskFilter(blur.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
    }

    val path = androidx.compose.ui.graphics.Path().apply {
        addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                left = 0f, top = 0f, right = size.width, bottom = size.height,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx())
            )
        )
    }

    onDrawWithContent {
        drawContent()
        drawIntoCanvas { canvas ->
            canvas.save()
            canvas.clipPath(path)

            val strokeW = paint.strokeWidth
            val left = -strokeW / 2 + offsetX.toPx()
            val top = -strokeW / 2 + offsetY.toPx()
            val right = size.width + strokeW / 2 + offsetX.toPx()
            val bottom = size.height + strokeW / 2 + offsetY.toPx()

            canvas.nativeCanvas.drawRoundRect(
                left, top, right, bottom,
                cornerRadius.toPx(), cornerRadius.toPx(),
                paint
            )
            canvas.restore()
        }
    }
}
