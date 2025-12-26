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
    val date: LocalDate,
    val type: BlockType = BlockType.TIMER,
    val color: Int? = null,
    val metadata: Map<String, String> = emptyMap()
) {
    companion object {
        /**
         * Creates a CustomBlock with the current date.
         * Use this factory function to ensure the date is evaluated at creation time, not at class definition time.
         */
        fun createForToday(
            id: String = UUID.randomUUID().toString(),
            title: String,
            startTime: LocalTime,
            endTime: LocalTime,
            type: BlockType = BlockType.TIMER,
            color: Int? = null,
            metadata: Map<String, String> = emptyMap()
        ): CustomBlock = CustomBlock(
            id = id,
            title = title,
            startTime = startTime,
            endTime = endTime,
            date = LocalDate.now(),
            type = type,
            color = color,
            metadata = metadata
        )
    }
}

enum class BlockType {
    TIMER,
    EVENT
}
