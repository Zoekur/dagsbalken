package com.dagsbalken.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.CalendarContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dagsbalken.app.ui.MainViewModel
import com.dagsbalken.app.ui.UiCustomBlock
import com.dagsbalken.app.ui.icons.DagsbalkenIcons
import com.dagsbalken.app.ui.settings.AppPreferences
import com.dagsbalken.app.ui.settings.SettingsScreen
import com.dagsbalken.app.ui.settings.ThemePreferences
import com.dagsbalken.app.ui.theme.DagsbalkenTheme
import com.dagsbalken.app.ui.theme.ThemeOption
import com.dagsbalken.core.data.BlockType
import com.dagsbalken.core.data.CalendarRepository
import com.dagsbalken.core.data.CustomBlock
import com.dagsbalken.core.data.DayEvent
import com.dagsbalken.core.data.TimerRepository
import com.dagsbalken.core.data.WeatherData
import com.dagsbalken.core.data.WeatherLocationSettings
import com.dagsbalken.core.data.WeatherRepository
import com.dagsbalken.core.workers.WeatherWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// -------- HUVUDAKTIVITET --------
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val themePrefs = ThemePreferences(applicationContext)
                val appPrefs = AppPreferences(applicationContext)
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(themePrefs, appPrefs) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WeatherWorker.schedule(applicationContext)

        setContent {
            val themeOption by viewModel.themeOptionFlow.collectAsState(initial = ThemeOption.Cold)

            DagsbalkenTheme(themeOption = themeOption) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            LinearClockScreen(
                                viewModel = viewModel,
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
                                },
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------- SKÄRM (Kombinerar tidslinje & kort) --------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinearClockScreen(
    viewModel: MainViewModel,
    themeOption: ThemeOption,
    onThemeOptionChange: (ThemeOption) -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val weatherRepository = remember { WeatherRepository(context) }
    val calendarRepository = remember { CalendarRepository(context) }
    val timerRepository = remember { TimerRepository(context) }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    val weatherData by weatherRepository.weatherDataFlow.collectAsState(initial = WeatherData())
    var calendarEvents by remember { mutableStateOf(emptyList<DayEvent>()) }
    val activeTimers by timerRepository.activeTimersFlow.collectAsState(initial = emptyList())
    val timerTemplates by timerRepository.timerTemplatesFlow.collectAsState(initial = emptyList())

    // Visibility Settings
    val showClock by viewModel.showClockFlow.collectAsState(initial = true)
    val showEvents by viewModel.showEventsFlow.collectAsState(initial = true)
    val showTimers by viewModel.showTimersFlow.collectAsState(initial = true)
    val showWeather by viewModel.showWeatherFlow.collectAsState(initial = true)
    val showClothing by viewModel.showClothingFlow.collectAsState(initial = true)

    // Funktion för att ladda events
    fun loadEvents() {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALENDAR
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            scope.launch {
                calendarEvents = calendarRepository.getEventsForToday()
            }
        }
    }

    // Unified Permission Launcher
    val multiplePermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        if (permissionsMap[Manifest.permission.READ_CALENDAR] == true) {
            loadEvents()
        }
        if (permissionsMap[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissionsMap[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
             scope.launch { weatherRepository.fetchAndSaveWeatherOnce() }
        }
    }

    val locationSettings by weatherRepository.locationSettingsFlow.collectAsState(initial = WeatherLocationSettings())

    LaunchedEffect(locationSettings.useCurrentLocation, locationSettings.manualLocationName) {
        if (locationSettings.useCurrentLocation) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                weatherRepository.fetchAndSaveWeatherOnce()
            } else {
                 multiplePermissionsLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            }
        } else {
            weatherRepository.fetchAndSaveWeatherOnce()
        }
    }

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

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_CALENDAR)
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
             permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
             permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (permissionsToRequest.isNotEmpty()) {
            multiplePermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            loadEvents()
        }
    }

    val now by rememberTicker1s()
    val currentEpochDay = now.toLocalDate().toEpochDay()
    val today = remember(currentEpochDay) { LocalDate.ofEpochDay(currentEpochDay) }

    // --- Prepare Items ---
    val todaysTimers = remember(activeTimers, today) {
        activeTimers.mapNotNull { timer ->
            when {
                timer.date == today -> timer
                timer.date == today.minusDays(1) && timer.endTime < timer.startTime -> {
                    timer.copy(startTime = LocalTime.MIDNIGHT, date = today)
                }
                else -> null
            }
        }
    }

    val allItems = remember(calendarEvents, todaysTimers) {
        val convertedEvents = calendarEvents.map {
            CustomBlock(
                id = it.id,
                title = it.title,
                startTime = it.start,
                endTime = it.end ?: it.start.plusHours(1),
                date = today,
                type = BlockType.EVENT,
                color = it.color
            )
        }
        (convertedEvents + todaysTimers).sortedBy { it.startTime }.map { UiCustomBlock(it) }
    }

    val calendarUiItems = remember(allItems) { allItems.filter { it.block.type == BlockType.EVENT } }
    val timerUiItems = remember(allItems) { allItems.filter { it.block.type == BlockType.TIMER } }

    // --- Timer Selection Sheet ---
    var showTimerSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // Stable lambda for deletion
    val onDeleteTimerLambda = remember(scope, timerRepository) {
        { id: String -> scope.launch { timerRepository.removeActiveTimer(id) } }
    }

    if (showTimerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTimerSheet = false },
            sheetState = sheetState
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Välj timer att starta", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                if (timerTemplates.isEmpty()) {
                    Text("Inga timers skapade. Gå till inställningar för att skapa.", color = Color.Gray)
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(timerTemplates) { timer ->
                        Card(
                            onClick = {
                                val startTime = LocalTime.now()
                                val endTime = startTime.plusHours(timer.durationHours.toLong())
                                    .plusMinutes(timer.durationMinutes.toLong())
                                val block = CustomBlock(
                                    title = timer.name,
                                    startTime = startTime,
                                    endTime = endTime,
                                    date = LocalDate.now(),
                                    type = BlockType.TIMER,
                                    color = timer.colorHex
                                )
                                scope.launch {
                                    timerRepository.addActiveTimer(block)
                                    showTimerSheet = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier
                                        .size(24.dp)
                                        .background(Color(timer.colorHex), CircleShape)
                                )
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(timer.name, style = MaterialTheme.typography.titleMedium)
                                    Text("${timer.durationHours}h ${timer.durationMinutes}m")
                                }
                                Spacer(Modifier.weight(1f))
                                Icon(Icons.Default.PlayArrow, null)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // Logo/Title
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
            if (showClock) {
                val unwrappedItems = remember(allItems) { allItems.map { it.block } }
                LinearDayCard(
                    now = now.toLocalTime(),
                    height = 168.dp,
                    items = unwrappedItems,
                    themeOption = themeOption
                )
                Spacer(Modifier.height(16.dp))
            }

            // 2. Events Section
            if (showEvents) {
                CalendarSection(
                    items = calendarUiItems,
                    now = now.toLocalTime(),
                    onAddEventClick = {
                        val intent = Intent(Intent.ACTION_INSERT)
                            .setData(CalendarContract.Events.CONTENT_URI)
                        context.startActivity(intent)
                    }
                )
                Spacer(Modifier.height(16.dp))
            }

            // 3. Timers Section
            if (showTimers) {
                TimerSection(
                    items = timerUiItems,
                    onStartTimerClick = { showTimerSheet = true },
                    onDeleteTimer = onDeleteTimerLambda
                )
                Spacer(Modifier.height(24.dp))
            }

            // 4. Väder- och Klädrådsrutor
            if (showWeather || showClothing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (showWeather) {
                        WeatherInfoCard(
                            modifier = Modifier.weight(1f),
                            data = weatherData,
                            onRefresh = {
                                scope.launch {
                                    val success = weatherRepository.fetchAndSaveWeatherOnce()
                                    Toast.makeText(
                                        context,
                                        if (success) "Uppdaterat väder" else "Uppdatering misslyckades",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    } else if (showClothing) {
                         // Filler to keep clothing card ratio if weather is hidden but clothing shown
                         Spacer(Modifier.weight(1f))
                    }

                    if (showClothing) {
                        ClothingAdviceCard(modifier = Modifier.weight(1f), data = weatherData)
                    } else if (showWeather) {
                        // Filler to keep weather card ratio if clothing is hidden
                        Spacer(Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Version info
            Text(
                text = "v${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray.copy(alpha = 0.5f)
            )
        }

        // Settings Icon
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
    items: List<CustomBlock> = emptyList(),
    themeOption: ThemeOption
) {
    val cornerRadiusDp = 28.dp

    // Theme colors
    val tickColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val majorTickColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
    val nowColor = MaterialTheme.colorScheme.error

    Box(
        Modifier
            .fillMaxWidth()
            .height(height)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(cornerRadiusDp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(cornerRadiusDp))
    ) {
        // Tidslinje med drawWithCache
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val width = size.width
                    val heightPx = size.height
                    val cornerRadiusPx = cornerRadiusDp.toPx()

                    val gradientBrush = Brush.horizontalGradient(
                        0.0f to nightColor,
                        0.25f to blendColors(nightColor, dayColor, 0.4f),
                        0.5f to dayColor,
                        0.75f to blendColors(nightColor, dayColor, 0.4f),
                        1.0f to nightColor,
                        startX = 0f,
                        endX = width
                    )

                    val pxPerMin = width / (24 * 60)

                    onDrawBehind {
                        val currentMinutes = now.hour * 60 + now.minute
                        val currentX = currentMinutes * pxPerMin

                        // 1. Gradient Background
                        drawRect(
                            brush = gradientBrush,
                            topLeft = Offset.Zero,
                            size = Size(currentX.coerceIn(0f, width), heightPx)
                        )

                        // 2. Rita events/timers
                        for (i in items.indices) {
                            val item = items[i]
                            val startMin = item.startTime.hour * 60 + item.startTime.minute
                            val endMin = item.endTime.hour * 60 + item.endTime.minute
                            
                            val actualEndMin = if (endMin > startMin) {
                                endMin
                            } else if (item.startTime == LocalTime.MIDNIGHT) {
                                endMin
                            } else {
                                24 * 60
                            }

                            val eventStartPx = startMin * pxPerMin
                            val eventWidthPx = (actualEndMin - startMin) * pxPerMin

                            val color = item.color?.let { Color(it) } ?: Color.Gray

                            drawRect(
                                color = color.copy(alpha = 0.3f),
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

                        // 4. Nu-markör
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

// -------- SECTION: CALENDAR --------
@Composable
fun CalendarSection(
    items: List<UiCustomBlock>,
    now: LocalTime,
    onAddEventClick: () -> Unit
) {
    val upcomingItems = remember(items, now) {
        items.filter {
             val end = it.block.endTime
             !end.isBefore(now.minusMinutes(1))
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Kalender", style = MaterialTheme.typography.titleMedium)
            // Add fallback button if list is not empty, so user can still add events
            if (upcomingItems.isNotEmpty()) {
                IconButton(onClick = onAddEventClick, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Add, "Lägg till händelse")
                }
            }
        }

        if (upcomingItems.isEmpty()) {
            EmptyStateCard(text = "Inget planerat", onClick = onAddEventClick)
        } else {
            upcomingItems.forEach { item ->
                key(item.block.id) {
                    EventListItem(item = item, onDelete = null)
                }
            }
        }
    }
}

// -------- SECTION: TIMERS --------
@Composable
fun TimerSection(
    items: List<UiCustomBlock>,
    onStartTimerClick: () -> Unit,
    onDeleteTimer: (String) -> Unit
) {
    // Show all active timers, even if expired? Usually active timers are removed when done or manually.
    // For now, assume activeTimers list contains what we want to show.

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Timers", style = MaterialTheme.typography.titleMedium)
             if (items.isNotEmpty()) {
                IconButton(onClick = onStartTimerClick, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Add, "Starta timer")
                }
            }
        }

        if (items.isEmpty()) {
            EmptyStateCard(text = "Inga timers", onClick = onStartTimerClick)
        } else {
            items.forEach { item ->
                key(item.block.id) {
                    EventListItem(item = item, onDelete = { onDeleteTimer(item.block.id) })
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(text: String, onClick: () -> Unit) {
     Box(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .innerShadow(
                color = Color.Black.copy(alpha = 0.1f),
                blur = 6.dp,
                offsetY = 2.dp,
                offsetX = 2.dp,
                cornerRadius = 16.dp
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun EventListItem(item: UiCustomBlock, onDelete: (() -> Unit)? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon / Dot
        Box(
             Modifier
                 .size(12.dp)
                 .background(item.block.color?.let { Color(it) } ?: MaterialTheme.colorScheme.primary, CircleShape)
        )
        Spacer(Modifier.width(16.dp))

        Column(Modifier.weight(1f)) {
            Text(item.block.title, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Text(
                text = "${item.block.startTime.format(DateTimeFormatter.ofPattern("HH:mm"))} – ${item.block.endTime.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }

        if (onDelete != null) {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Ta bort timer", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// ----------------------------------------------------------
// 2. VÄDER-KOMPONENT
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Laddar väderdata...", fontSize = 14.sp, color = Color.Gray)
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val title = if (data.locationName.isNotBlank()) data.locationName else "Väderinformation"
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                         textAlign = TextAlign.Center
                    )
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
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(8.dp))

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
// 3. KLÄDRÅDS-KOMPONENT
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

@Composable
fun rememberTicker1s(): State<LocalDateTime> {
	val state = remember { mutableStateOf(LocalDateTime.now()) }
	LaunchedEffect(Unit) {
		while (true) {
			state.value = LocalDateTime.now()
			delay(60000)
		}
	}
	return state
}

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
