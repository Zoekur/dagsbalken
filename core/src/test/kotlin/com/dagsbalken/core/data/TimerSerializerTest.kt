package com.dagsbalken.core.data

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class TimerSerializerTest {

    private val logger: (String, Throwable?) -> Unit = { msg, err ->
        println("LOG: $msg")
        err?.printStackTrace()
    }

    @Test
    fun serializeTimerTemplates_roundTrip() {
        val original = listOf(
            TimerModel(id = "1", name = "Test 1", durationHours = 1, durationMinutes = 30, colorHex = 0xFF0000),
            TimerModel(id = "2", name = "Test 2", durationHours = 0, durationMinutes = 45, colorHex = 0x00FF00)
        )

        val json = TimerSerializer.serializeTimerTemplates(original)
        val deserialized = TimerSerializer.deserializeTimerTemplatesStrict(json)

        assertEquals(original, deserialized)
    }

    @Test(expected = Exception::class)
    fun deserializeTimerTemplatesStrict_throwsOnMalformedJSON() {
        val json = "[{\"id\": \"1\", \"name\": \"Test 1\"" // incomplete
        TimerSerializer.deserializeTimerTemplatesStrict(json)
    }

    @Test(expected = Exception::class)
    fun deserializeTimerTemplatesStrict_throwsOnMissingFields() {
        val json = "[{\"id\": \"1\", \"name\": \"Test 1\"}]" // missing duration/color
        TimerSerializer.deserializeTimerTemplatesStrict(json)
    }

    @Test
    fun deserializeTimerTemplatesSafe_recoversValidItems() {
        val json = """
            [
                {"id": "1", "name": "Valid", "durationHours": 1, "durationMinutes": 0, "colorHex": 123},
                {"id": "2", "name": "Invalid"},
                {"id": "3", "name": "Valid 2", "durationHours": 0, "durationMinutes": 30, "colorHex": 456}
            ]
        """

        val result = TimerSerializer.deserializeTimerTemplatesSafe(json, null, logger)

        assertEquals(2, result.size)
        assertEquals("Valid", result[0].name)
        assertEquals("Valid 2", result[1].name)
    }

    @Test
    fun deserializeTimerTemplatesSafe_usesBackupOnTotalFailure() {
        val invalidJson = "not json"
        val backupJson = """
            [
                {"id": "backup", "name": "Backup", "durationHours": 1, "durationMinutes": 0, "colorHex": 123}
            ]
        """

        val result = TimerSerializer.deserializeTimerTemplatesSafe(invalidJson, backupJson, logger)

        assertEquals(1, result.size)
        assertEquals("Backup", result[0].name)
    }

    // --- Active Timers Tests ---

    @Test
    fun serializeActiveTimers_roundTrip() {
        val original = listOf(
            CustomBlock.createForToday(title = "Block 1", startTime = LocalTime.of(10, 0), endTime = LocalTime.of(11, 0), type = BlockType.TIMER),
            CustomBlock.createForToday(title = "Block 2", startTime = LocalTime.of(12, 0), endTime = LocalTime.of(13, 0), type = BlockType.EVENT)
        )

        val json = TimerSerializer.serializeActiveTimers(original)
        val deserialized = TimerSerializer.deserializeActiveTimersStrict(json)

        assertEquals(original.size, deserialized.size)
        assertEquals(original[0].title, deserialized[0].title)
        assertEquals(original[0].startTime, deserialized[0].startTime)
        assertEquals(original[0].date, deserialized[0].date)
    }

    @Test(expected = Exception::class)
    fun deserializeActiveTimersStrict_throwsOnError() {
        val json = """[{"id": "1"}]""" // missing fields
        TimerSerializer.deserializeActiveTimersStrict(json)
    }
}
