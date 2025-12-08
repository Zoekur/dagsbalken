package com.example.core.data

import androidx.compose.ui.graphics.Color
import java.time.LocalTime

data class DayEvent(
	val id: String,
	val title: String,
	val start: LocalTime,
	val end: LocalTime? = null,
	val icon: String? = null,
	val color: Color = Color(0xFF6AA6FF)
)
