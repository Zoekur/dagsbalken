package com.dagsbalken.core.data

import java.util.UUID

data class TimerModel(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val durationHours: Int,
    val durationMinutes: Int,
    val colorHex: Int
)
