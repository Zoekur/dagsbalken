package com.dagsbalken.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.dagsbalken.core.data.TimerModel
import com.dagsbalken.core.data.TimerRepository
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun TimerEditor() {
    val context = LocalContext.current
    val timerRepository = remember { TimerRepository(context) }
    val timers by timerRepository.timerTemplatesFlow.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    TimerEditor(
        timers = timers,
        onAddTimer = { timer ->
            scope.launch { timerRepository.saveTimerTemplate(timer) }
        },
        onUpdateTimer = { timer ->
            scope.launch { timerRepository.saveTimerTemplate(timer) }
        },
        onDeleteTimer = { id ->
            scope.launch { timerRepository.deleteTimerTemplate(id) }
        }
    )
}

@Composable
fun TimerEditor(
    timers: List<TimerModel>,
    onAddTimer: (TimerModel) -> Unit,
    onUpdateTimer: (TimerModel) -> Unit,
    onDeleteTimer: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var editingTimer by remember { mutableStateOf<TimerModel?>(null) }
    var timerToDelete by remember { mutableStateOf<TimerModel?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingTimer = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Timer")
            }
        }
    ) { padding ->
        if (timers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No timers yet. Add one!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(timers) { timer ->
                    TimerItem(
                        timer = timer,
                        onEdit = {
                            editingTimer = timer
                            showDialog = true
                        },
                        onDelete = { timerToDelete = timer }
                    )
                }
            }
        }
    }

    if (showDialog) {
        TimerDialog(
            timer = editingTimer,
            onDismiss = { showDialog = false },
            onSave = { timer ->
                if (editingTimer == null) {
                    onAddTimer(timer)
                } else {
                    onUpdateTimer(timer)
                }
                showDialog = false
            }
        )
    }

    if (timerToDelete != null) {
        AlertDialog(
            onDismissRequest = { timerToDelete = null },
            title = { Text("Delete Timer") },
            text = { Text("Are you sure you want to delete '${timerToDelete?.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        timerToDelete?.let { onDeleteTimer(it.id) }
                        timerToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { timerToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TimerItem(
    timer: TimerModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(timer.colorHex))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = timer.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${timer.durationHours}h ${timer.durationMinutes}m",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimerDialog(
    timer: TimerModel?,
    onDismiss: () -> Unit,
    onSave: (TimerModel) -> Unit
) {
    var name by remember { mutableStateOf(timer?.name ?: "") }
    var hours by remember { mutableStateOf(timer?.durationHours?.toString() ?: "0") }
    var minutes by remember { mutableStateOf(timer?.durationMinutes?.toString() ?: "15") }
    var selectedColor by remember { mutableStateOf(timer?.colorHex ?: Color.Blue.toArgb()) }

    // Define colors with names for better accessibility
    val colorOptions = listOf(
        Color.Blue to "Blue",
        Color.Red to "Red",
        Color.Green to "Green",
        Color.Yellow to "Yellow",
        Color.Magenta to "Magenta",
        Color.Cyan to "Cyan",
        Color(0xFFFFA500) to "Orange",
        Color(0xFF800080) to "Purple",
        Color(0xFF008080) to "Teal"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (timer == null) "Add Timer" else "Edit Timer",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hours,
                        onValueChange = { if (it.all { char -> char.isDigit() }) hours = it },
                        label = { Text("Hours") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = minutes,
                        onValueChange = { if (it.all { char -> char.isDigit() }) minutes = it },
                        label = { Text("Minutes") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Color", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                     modifier = Modifier.fillMaxWidth(),
                     horizontalArrangement = Arrangement.spacedBy(8.dp),
                     verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colorOptions.forEach { (color, colorName) ->
                        val isSelected = selectedColor == color.toArgb()
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = color.toArgb() }
                                .semantics {
                                    role = Role.RadioButton
                                    selected = isSelected
                                    contentDescription = "$colorName color"
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(onClick = {
                        val h = hours.toIntOrNull() ?: 0
                        val m = minutes.toIntOrNull() ?: 0
                        if (name.isNotBlank() && (h > 0 || m > 0)) {
                            onSave(
                                TimerModel(
                                    id = timer?.id ?: UUID.randomUUID().toString(),
                                    name = name,
                                    durationHours = h,
                                    durationMinutes = m,
                                    colorHex = selectedColor
                                )
                            )
                        }
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun TimerEditorPreview() {
    MaterialTheme {
        TimerEditor(
            timers = listOf(
                TimerModel(name = "Focus", durationHours = 0, durationMinutes = 25, colorHex = Color.Blue.toArgb()),
                TimerModel(name = "Break", durationHours = 0, durationMinutes = 5, colorHex = Color.Green.toArgb())
            ),
            onAddTimer = {},
            onUpdateTimer = {},
            onDeleteTimer = {}
        )
    }
}
