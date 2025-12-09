package com.dagsbalken.core.data

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
    val type: BlockType = BlockType.TIMER,
    val color: Int? = null
)

enum class BlockType {
    TIMER,
    EVENT
}

