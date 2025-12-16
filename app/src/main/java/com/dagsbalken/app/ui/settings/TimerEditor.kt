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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.util.UUID

data class TimerModel(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val durationHours: Int,
    val durationMinutes: Int,
    val colorHex: Int
)

@Composable
fun TimerEditor() {
    var timers by remember { mutableStateOf(emptyList<TimerModel>()) }
    TimerEditor(
        timers = timers,
        onAddTimer = { timers = timers + it },
        onUpdateTimer = { updated -> timers = timers.map { if (it.id == updated.id) updated else it } },
        onDeleteTimer = { id -> timers = timers.filter { it.id != id } }
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
                        onDelete = { onDeleteTimer(timer.id) }
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

    val colors = listOf(
        Color.Blue, Color.Red, Color.Green, Color.Yellow, Color.Magenta, Color.Cyan,
        Color(0xFFFFA500), Color(0xFF800080), Color(0xFF008080)
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
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (selectedColor == color.toArgb()) 2.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = color.toArgb() }
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
