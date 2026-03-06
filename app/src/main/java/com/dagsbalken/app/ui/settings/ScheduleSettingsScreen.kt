package com.dagsbalken.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.platform.LocalContext
import com.dagsbalken.app.ui.icons.DagsbalkenIcons
import com.dagsbalken.core.schedule.DayOfWeekKey
import com.dagsbalken.core.schedule.DailySymbolPlacement
import com.dagsbalken.core.schedule.IconStyle
import com.dagsbalken.core.schedule.ScheduleSymbol
import com.dagsbalken.core.schedule.TimeRange
import com.dagsbalken.core.schedule.TimelineSymbolSchedule
import com.dagsbalken.core.schedule.TimelineSymbolScheduleRepositoryImpl
import com.dagsbalken.core.schedule.glyphForIcon
import kotlinx.coroutines.launch

/**
 * Simple, text/list-based editor for user timeline symbols and daily placements.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleSettingsScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val appPrefs = remember { AppPreferences(context) }
    val iconStyle by appPrefs.iconStyleFlow.collectAsState(initial = IconStyle.EmojiClassic)
    val scope = rememberCoroutineScope()
    val repo = remember { TimelineSymbolScheduleRepositoryImpl(context) }

    val schedule by repo.scheduleFlow.collectAsState(initial = TimelineSymbolSchedule())

    var showSymbolDialog by remember { mutableStateOf(false) }
    var editingSymbol by remember { mutableStateOf<ScheduleSymbol?>(null) }

    var showPlacementDialog by remember { mutableStateOf(false) }
    var editingPlacement by remember { mutableStateOf<DailySymbolPlacement?>(null) }

    LaunchedEffect(Unit) {
        // Seed with a simple default if empty
        if (schedule.symbols.isEmpty() && schedule.placements.isEmpty()) {
            scope.launch {
                repo.update { current ->
                    val baseSymbols = listOf(
                        ScheduleSymbol(
                            id = "symbol_food",
                            label = "Mat",
                            iconKey = "food",
                            colorArgb = 0xFFFFA500.toInt(),
                            isSchoolRelated = false
                        ),
                        ScheduleSymbol(
                            id = "symbol_school",
                            label = "Skola",
                            iconKey = "school",
                            colorArgb = 0xFF3B82F6.toInt(),
                            isSchoolRelated = true
                        ),
                        ScheduleSymbol(
                            id = "symbol_sleep",
                            label = "Sömn",
                            iconKey = "sleep",
                            colorArgb = 0xFF4B5563.toInt(),
                            isSchoolRelated = false
                        )
                    )
                    val basePlacements = listOf(
                        DailySymbolPlacement(
                            symbolId = "symbol_food",
                            dayOfWeek = null,
                            timeRange = TimeRange(7 * 60, 7 * 60 + 30)
                        ),
                        DailySymbolPlacement(
                            symbolId = "symbol_food",
                            dayOfWeek = null,
                            timeRange = TimeRange(12 * 60, 12 * 60 + 30)
                        ),
                        DailySymbolPlacement(
                            symbolId = "symbol_sleep",
                            dayOfWeek = null,
                            timeRange = TimeRange(21 * 60, 21 * 60 + 60)
                        )
                    )
                    current.copy(
                        symbols = baseSymbols,
                        placements = basePlacements
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schema & symboler") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(DagsbalkenIcons.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Symboler", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                items(schedule.symbols, key = { it.id }) { symbol ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                editingSymbol = symbol
                                showSymbolDialog = true
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val glyph = glyphForIcon(symbol.iconKey, iconStyle)
                        Text(
                            text = glyph,
                            fontSize = 24.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(symbol.label, fontWeight = FontWeight.Medium)
                            Text(symbol.iconKey, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (symbol.isSchoolRelated) {
                            Text("Skola", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = {
                            scope.launch {
                                repo.update { current ->
                                    current.copy(
                                        symbols = current.symbols.filterNot { it.id == symbol.id },
                                        placements = current.placements.filterNot { it.symbolId == symbol.id }
                                    )
                                }
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Ta bort symbol")
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    editingSymbol = null
                    showSymbolDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lägg till symbol")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Tidsblock", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(schedule.placements) { placement ->
                    val symbol = schedule.symbols.firstOrNull { it.id == placement.symbolId }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                editingPlacement = placement
                                showPlacementDialog = true
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val glyph = glyphForIcon(symbol?.iconKey ?: "", iconStyle)
                        Text(
                            text = glyph,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(symbol?.label ?: placement.symbolId)
                            Text(
                                text = "${formatDay(placement.dayOfWeek)} ${formatTimeRange(placement.timeRange)}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = {
                            scope.launch {
                                repo.update { current ->
                                    current.copy(placements = current.placements - placement)
                                }
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Ta bort block")
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    editingPlacement = null
                    showPlacementDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lägg till tidsblock")
            }
        }
    }

    if (showSymbolDialog) {
        SymbolEditDialog(
            initial = editingSymbol,
            onDismiss = { showSymbolDialog = false },
            onSave = { symbol ->
                scope.launch {
                    repo.update { current ->
                        val others = current.symbols.filterNot { it.id == symbol.id }
                        current.copy(symbols = others + symbol)
                    }
                }
                showSymbolDialog = false
            }
        )
    }

    if (showPlacementDialog) {
        PlacementEditDialog(
            schedule = schedule,
            initial = editingPlacement,
            onDismiss = { showPlacementDialog = false },
            onSave = { placement ->
                scope.launch {
                    repo.update { current ->
                        val others = current.placements.filterNot { it == editingPlacement }
                        current.copy(placements = others + placement)
                    }
                }
                showPlacementDialog = false
            }
        )
    }
}

private fun formatDay(day: DayOfWeekKey?): String = when (day) {
    null -> "Alla dagar"
    DayOfWeekKey.MONDAY -> "Mån"
    DayOfWeekKey.TUESDAY -> "Tis"
    DayOfWeekKey.WEDNESDAY -> "Ons"
    DayOfWeekKey.THURSDAY -> "Tors"
    DayOfWeekKey.FRIDAY -> "Fre"
    DayOfWeekKey.SATURDAY -> "Lör"
    DayOfWeekKey.SUNDAY -> "Sön"
}

private fun formatTimeRange(range: TimeRange): String {
    fun format(m: Int): String {
        val h = m / 60
        val min = m % 60
        return "%02d:%02d".format(h, min)
    }
    return "${format(range.startMinutes)}–${format(range.endMinutes)}"
}

@Composable
private fun SymbolEditDialog(
    initial: ScheduleSymbol?,
    onDismiss: () -> Unit,
    onSave: (ScheduleSymbol) -> Unit
) {
    var label by remember { mutableStateOf(initial?.label ?: "") }
    var iconStyleIndex by remember { mutableStateOf(0) }
    var isSchoolRelated by remember { mutableStateOf(initial?.isSchoolRelated ?: false) }

    val styles = listOf("Emoji", "Enkel", "Kontrast")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Ny aktivitet" else "Redigera aktivitet") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Namn") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Ikonset: ${styles[iconStyleIndex]}")
                Slider(
                    value = iconStyleIndex.toFloat(),
                    onValueChange = { iconStyleIndex = it.toInt().coerceIn(0, styles.lastIndex) },
                    steps = styles.size - 2,
                    valueRange = 0f..styles.lastIndex.toFloat()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isSchoolRelated = !isSchoolRelated }
                ) {
                    Text("Skolrelaterad")
                    Spacer(modifier = Modifier.width(8.dp))
                    androidx.compose.material3.Switch(
                        checked = isSchoolRelated,
                        onCheckedChange = { isSchoolRelated = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val id = initial?.id ?: "symbol_${label}_${System.currentTimeMillis()}"
                    val iconKey = deriveIconKeyFromLabel(label)
                    val color = colorForIconKey(iconKey)
                    onSave(
                        ScheduleSymbol(
                            id = id,
                            label = label.ifBlank { "Aktivitet" },
                            iconKey = iconKey,
                            colorArgb = color,
                            isSchoolRelated = isSchoolRelated
                        )
                    )
                }
            ) {
                Text("Spara")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Avbryt") }
        }
    )
}

@Composable
private fun PlacementEditDialog(
    schedule: TimelineSymbolSchedule,
    initial: DailySymbolPlacement?,
    onDismiss: () -> Unit,
    onSave: (DailySymbolPlacement) -> Unit
) {
    var selectedSymbolId by remember { mutableStateOf(initial?.symbolId ?: schedule.symbols.firstOrNull()?.id.orEmpty()) }
    var dayIndex by remember { mutableStateOf(initial?.dayOfWeek?.ordinal ?: -1) }
    var startHour by remember { mutableStateOf((initial?.timeRange?.startMinutes ?: 8 * 60) / 60) }
    var startMinute by remember { mutableStateOf((initial?.timeRange?.startMinutes ?: 8 * 60) % 60) }
    var durationMinutes by remember { mutableStateOf((initial?.timeRange?.endMinutes ?: (8 * 60 + 30)) - (initial?.timeRange?.startMinutes ?: 8 * 60)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Nytt tidsblock" else "Redigera tidsblock") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Symbol")
                schedule.symbols.forEach { symbol ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSymbolId = symbol.id }
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = selectedSymbolId == symbol.id,
                            onClick = { selectedSymbolId = symbol.id }
                        )
                        Text(symbol.label, modifier = Modifier.padding(start = 8.dp))
                    }
                }

                Text("Dag")
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val labels = listOf("Alla", "Mån", "Tis", "Ons", "Tors", "Fre", "Lör", "Sön")
                    labels.forEachIndexed { index, label ->
                        OutlinedButton(
                            onClick = { dayIndex = index - 1 },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(label, fontSize = 12.sp)
                        }
                    }
                }

                Text("Starttid (timme:minut)")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startHour.toString(),
                        onValueChange = { value ->
                            value.toIntOrNull()?.let { startHour = it.coerceIn(0, 23) }
                        },
                        label = { Text("Timme") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = startMinute.toString(),
                        onValueChange = { value ->
                            value.toIntOrNull()?.let { startMinute = it.coerceIn(0, 59) }
                        },
                        label = { Text("Minut") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Längd (min)")
                OutlinedTextField(
                    value = durationMinutes.toString(),
                    onValueChange = { value ->
                        value.toIntOrNull()?.let { durationMinutes = it.coerceIn(5, 12 * 60) }
                    },
                    label = { Text("Minuter") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val start = startHour * 60 + startMinute
                val end = (start + durationMinutes).coerceAtMost(24 * 60)
                val day = if (dayIndex < 0) null else DayOfWeekKey.values()[dayIndex]
                onSave(
                    DailySymbolPlacement(
                        symbolId = selectedSymbolId,
                        dayOfWeek = day,
                        timeRange = TimeRange(start, end),
                        schoolModeOnly = false
                    )
                )
            }) {
                Text("Spara")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Avbryt") }
        }
    )
}

private fun deriveIconKeyFromLabel(label: String): String {
    val lower = label.lowercase()
    return when {
        listOf("mat", "lunch", "frukost", "middag", "fika").any { lower.contains(it) } -> "food"
        listOf("skola", "lektion", "klass", "läxa").any { lower.contains(it) } -> "school"
        listOf("sov", "natt", "läggdags", "sömn").any { lower.contains(it) } -> "sleep"
        listOf("träning", "sport", "gym", "fotboll").any { lower.contains(it) } -> "sport"
        else -> "other"
    }
}

private fun colorForIconKey(iconKey: String): Int = when (iconKey) {
    "food" -> 0xFFFFA500.toInt()
    "school" -> 0xFF3B82F6.toInt()
    "sport" -> 0xFF22C55E.toInt()
    "sleep" -> 0xFF4B5563.toInt()
    else -> 0xFF6B7280.toInt()
}
