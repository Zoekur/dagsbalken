package com.dagsbalken.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException

class TimerSerializerTest {

    @Test
    fun deserializeTimerTemplates_validJson_returnsList() {
        val validJson = """
            [
                {
                    "id": "1",
                    "name": "Test Timer",
                    "durationHours": 1,
                    "durationMinutes": 30,
                    "colorHex": -16777216
                }
            ]
        """.trimIndent()

        val result = TimerSerializer.deserializeTimerTemplatesWithRecovery(validJson, null, throwOnFailure = true)
        assertEquals(1, result.size)
        assertEquals("Test Timer", result[0].name)
    }

    @Test
    fun deserializeTimerTemplates_corruptJson_throwOnFailureTrue_throwsException() {
        val corruptJson = """
            [
                {
                    "id": "1",
                    "name": "Test Timer",
                    "durationHours": 1,
                    "durationMinutes": 30,
                    "colorHex": -16777216
                },
                {
                    "name": "Invalid Timer (no id)"
                }
            ]
        """.trimIndent()

        try {
            TimerSerializer.deserializeTimerTemplatesWithRecovery(corruptJson, null, throwOnFailure = true)
            fail("Expected IOException was not thrown")
        } catch (e: IOException) {
            // Expected
        }
    }

    @Test
    fun deserializeTimerTemplates_corruptJson_throwOnFailureFalse_recoversPartial() {
        val corruptJson = """
            [
                {
                    "id": "1",
                    "name": "Valid Timer",
                    "durationHours": 1,
                    "durationMinutes": 30,
                    "colorHex": -16777216
                },
                {
                    "name": "Invalid Timer (no id)"
                }
            ]
        """.trimIndent()

        val result = TimerSerializer.deserializeTimerTemplatesWithRecovery(corruptJson, null, throwOnFailure = false)
        assertEquals(1, result.size)
        assertEquals("Valid Timer", result[0].name)
    }

    @Test
    fun deserializeTimerTemplates_fullyCorrupt_restoresBackup() {
        val corruptJson = "invalid json"
        val backupJson = """
            [
                {
                    "id": "backup",
                    "name": "Backup Timer",
                    "durationHours": 0,
                    "durationMinutes": 10,
                    "colorHex": 0
                }
            ]
        """.trimIndent()

        val result = TimerSerializer.deserializeTimerTemplatesWithRecovery(corruptJson, backupJson, throwOnFailure = false)
        assertEquals(1, result.size)
        assertEquals("Backup Timer", result[0].name)
    }

    @Test
    fun deserializeActiveTimers_corruptJson_throwOnFailureTrue_throwsException() {
        val corruptJson = """
            [
                { "broken": }
            ]
        """.trimIndent()

        try {
            TimerSerializer.deserializeActiveTimersWithRecovery(corruptJson, null, throwOnFailure = true)
            fail("Expected IOException was not thrown")
        } catch (e: IOException) {
            // Expected
        }
    }
}
