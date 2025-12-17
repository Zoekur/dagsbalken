package com.dagsbalken.core.data

import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

/**
 * Represents a user-defined timer/event block that should be visualised on the timeline.
 */
data class CustomBlock(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val date: LocalDate = LocalDate.now(), // Added date to handle one-off timers
    val type: BlockType = BlockType.TIMER,
    val color: Int? = null
)

enum class BlockType {
    TIMER,
    EVENT
}
