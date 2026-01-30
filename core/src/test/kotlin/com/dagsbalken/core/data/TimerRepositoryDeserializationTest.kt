package com.dagsbalken.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.json.JSONException

class TimerRepositoryDeserializationTest {

    private val validJson = """
        [
            {"id":"1","name":"Test","durationHours":1,"durationMinutes":30,"colorHex":123},
            {"id":"2","name":"Test2","durationHours":0,"durationMinutes":45,"colorHex":456}
        ]
    """

    // Item 2 has invalid int value -> Corrupt Item
    private val corruptItemJson = """
        [
            {"id":"1","name":"Test","durationHours":1,"durationMinutes":30,"colorHex":123},
            {"id":"2","name":"Test2","durationHours":"INVALID_INT","durationMinutes":45,"colorHex":456}
        ]
    """

    // JSON syntax error after first item -> Partial Parse + Corrupt Remainder
    private val partialJson = """
        [
            {"id":"1","name":"Test","durationHours":1,"durationMinutes":30,"colorHex":123},
            INVALID_JSON
        ]
    """

    // Not a JSON array -> Total Failure
    private val totallyInvalidJson = "INVALID_JSON_CONTENT"

    private val backupJson = """
        [
            {"id":"backup","name":"Backup","durationHours":2,"durationMinutes":0,"colorHex":789}
        ]
    """

    @Test
    fun `testStrict_ValidJson_ReturnsList`() {
        val result = TimerRepository.deserializeTimerTemplatesWithRecovery(validJson, null, throwOnFailure = true)
        assertEquals(2, result.size)
        assertEquals("Test", result[0].name)
    }

    @Test(expected = JSONException::class)
    fun `testStrict_CorruptItem_ThrowsException`() {
        TimerRepository.deserializeTimerTemplatesWithRecovery(corruptItemJson, backupJson, throwOnFailure = true)
    }

    @Test(expected = JSONException::class)
    fun `testStrict_PartialJson_ThrowsException`() {
        TimerRepository.deserializeTimerTemplatesWithRecovery(partialJson, backupJson, throwOnFailure = true)
    }

    @Test(expected = JSONException::class)
    fun `testStrict_TotallyInvalid_ThrowsException`() {
        TimerRepository.deserializeTimerTemplatesWithRecovery(totallyInvalidJson, backupJson, throwOnFailure = true)
    }

    @Test
    fun `testRecovery_CorruptItem_ReturnsPartialList`() {
        val result = TimerRepository.deserializeTimerTemplatesWithRecovery(corruptItemJson, backupJson, throwOnFailure = false)
        assertEquals(1, result.size)
        assertEquals("Test", result[0].name)
    }

    @Test
    fun `testRecovery_PartialJson_ReturnsPartialList`() {
        // Partial JSON allows recovering the first valid item
        val result = TimerRepository.deserializeTimerTemplatesWithRecovery(partialJson, backupJson, throwOnFailure = false)
        assertEquals(1, result.size)
        assertEquals("Test", result[0].name)
    }

    @Test
    fun `testRecovery_TotallyInvalid_ReturnsBackup`() {
        val result = TimerRepository.deserializeTimerTemplatesWithRecovery(totallyInvalidJson, backupJson, throwOnFailure = false)
        assertEquals(1, result.size)
        assertEquals("Backup", result[0].name)
    }

    @Test
    fun `testRecovery_TotallyInvalid_NoBackup_ReturnsEmpty`() {
        val result = TimerRepository.deserializeTimerTemplatesWithRecovery(totallyInvalidJson, null, throwOnFailure = false)
        assertTrue(result.isEmpty())
    }
}
