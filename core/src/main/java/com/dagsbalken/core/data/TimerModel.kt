package com.dagsbalken.core.data

import java.util.UUID

/**
 * Represents a reusable timer template with predefined duration and color.
 *
 * TimerModel serves as a template for creating timer instances. It defines the
 * characteristics of a timer (name, duration, color) that can be reused multiple times.
 * When a user starts a timer based on this model, a [CustomBlock] instance is created
 * with specific start and end times.
 *
 * Key differences from [CustomBlock]:
 * - TimerModel: Template with duration (hours/minutes) and color - reusable
 * - CustomBlock: Active timer instance with specific start/end times - one-time use
 *
 * @property id Unique identifier for this timer template
 * @property name Display name for the timer
 * @property durationHours Duration in hours (0-23)
 * @property durationMinutes Duration in minutes (0-59)
 * @property colorHex Color for the timer as an Android color integer
 */
data class TimerModel(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val durationHours: Int,
    val durationMinutes: Int,
    val colorHex: Int
)
