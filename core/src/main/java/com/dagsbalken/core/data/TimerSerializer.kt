package com.dagsbalken.core.data

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime

object TimerSerializer {

    private const val MAX_LOG_LENGTH = 500

    fun truncateForLog(jsonString: String): String {
        return if (jsonString.length > MAX_LOG_LENGTH) {
            "${jsonString.take(MAX_LOG_LENGTH)}... (truncated, length: ${jsonString.length})"
        } else {
            jsonString
        }
    }

    // --- Timer Templates ---

    fun serializeTimerTemplates(timers: List<TimerModel>): String {
        val jsonArray = JSONArray()
        timers.forEach { timer ->
            val jsonObj = JSONObject().apply {
                put("id", timer.id)
                put("name", timer.name)
                put("durationHours", timer.durationHours)
                put("durationMinutes", timer.durationMinutes)
                put("colorHex", timer.colorHex)
            }
            jsonArray.put(jsonObj)
        }
        return jsonArray.toString()
    }

    /**
     * Strict version: Throws exception on ANY failure.
     * Use this for read-modify-write cycles to prevent data loss.
     */
    fun deserializeTimerTemplatesStrict(jsonString: String): List<TimerModel> {
        val list = mutableListOf<TimerModel>()
        val jsonArray = JSONArray(jsonString)
        for (i in 0 until jsonArray.length()) {
             val obj = jsonArray.getJSONObject(i)
             list.add(deserializeTimerModel(obj))
        }
        return list
    }

    /**
     * Safe version: Recovers valid items, uses backup on total failure.
     * Use this for UI reads to prevent crashes.
     */
    fun deserializeTimerTemplatesSafe(
        jsonString: String,
        backupString: String?,
        logWarning: (String, Throwable?) -> Unit
    ): List<TimerModel> {
        return try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<TimerModel>()
            var hasCorruption = false

            for (i in 0 until jsonArray.length()) {
                try {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(deserializeTimerModel(obj))
                } catch (e: Exception) {
                    hasCorruption = true
                    logWarning("Skipping corrupted timer template at index $i", e)
                }
            }

            if (hasCorruption && list.isEmpty() && backupString != null) {
                logWarning("All items corrupted, attempting restore from backup", null)
                return deserializeTimerTemplatesBackup(backupString)
            }

            if (hasCorruption) {
                logWarning("Recovered ${list.size} timer templates, some items were corrupted", null)
            }

            list
        } catch (e: Exception) {
            logWarning("Failed to parse timer templates JSON. Data: ${truncateForLog(jsonString)}", e)
            if (backupString != null) {
                logWarning("Attempting restore from backup", null)
                try {
                     return deserializeTimerTemplatesBackup(backupString)
                } catch (backupError: Exception) {
                    logWarning("Backup also corrupted", backupError)
                }
            }
            emptyList()
        }
    }

    private fun deserializeTimerTemplatesBackup(jsonString: String): List<TimerModel> {
        val list = mutableListOf<TimerModel>()
        val jsonArray = JSONArray(jsonString)
        for (i in 0 until jsonArray.length()) {
            try {
                val obj = jsonArray.getJSONObject(i)
                list.add(deserializeTimerModel(obj))
            } catch (_: Exception) {
                // Ignore errors in backup
            }
        }
        return list
    }

    private fun deserializeTimerModel(obj: JSONObject): TimerModel {
        return TimerModel(
            id = obj.getString("id"),
            name = obj.getString("name"),
            durationHours = obj.getInt("durationHours"),
            durationMinutes = obj.getInt("durationMinutes"),
            colorHex = obj.getInt("colorHex")
        )
    }

    // --- Active Timers (CustomBlocks) ---

    fun serializeActiveTimers(blocks: List<CustomBlock>): String {
        val jsonArray = JSONArray()
        blocks.forEach { block ->
            val jsonObj = JSONObject().apply {
                put("id", block.id)
                put("title", block.title)
                put("startTime", block.startTime.toString())
                put("endTime", block.endTime.toString())
                put("date", block.date.toString())
                put("type", block.type.name)
                put("color", block.color ?: 0)
            }
            jsonArray.put(jsonObj)
        }
        return jsonArray.toString()
    }

    /**
     * Strict version: Throws exception on ANY failure.
     */
    fun deserializeActiveTimersStrict(jsonString: String): List<CustomBlock> {
        val list = mutableListOf<CustomBlock>()
        val jsonArray = JSONArray(jsonString)
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            list.add(deserializeCustomBlock(obj))
        }
        return list
    }

    /**
     * Safe version: Recovers valid items, uses backup on total failure.
     */
    fun deserializeActiveTimersSafe(
        jsonString: String,
        backupString: String?,
        logWarning: (String, Throwable?) -> Unit
    ): List<CustomBlock> {
        return try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<CustomBlock>()
            var hasCorruption = false

            for (i in 0 until jsonArray.length()) {
                try {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(deserializeCustomBlock(obj))
                } catch (e: Exception) {
                    hasCorruption = true
                    logWarning("Skipping corrupted active timer at index $i", e)
                }
            }

            if (hasCorruption && list.isEmpty() && backupString != null) {
                logWarning("All items corrupted, attempting restore from backup", null)
                return deserializeActiveTimersBackup(backupString)
            }

            if (hasCorruption) {
                logWarning("Recovered ${list.size} active timers, some items were corrupted", null)
            }

            list
        } catch (e: Exception) {
            logWarning("Failed to parse active timers JSON. Data: ${truncateForLog(jsonString)}", e)
            if (backupString != null) {
                logWarning("Attempting restore from backup", null)
                try {
                    return deserializeActiveTimersBackup(backupString)
                } catch (backupError: Exception) {
                    logWarning("Backup also corrupted", backupError)
                }
            }
            emptyList()
        }
    }

    private fun deserializeActiveTimersBackup(jsonString: String): List<CustomBlock> {
        val list = mutableListOf<CustomBlock>()
        val jsonArray = JSONArray(jsonString)
        for (i in 0 until jsonArray.length()) {
            try {
                val obj = jsonArray.getJSONObject(i)
                list.add(deserializeCustomBlock(obj))
            } catch (_: Exception) {
                // Ignore errors in backup
            }
        }
        return list
    }

    private fun deserializeCustomBlock(obj: JSONObject): CustomBlock {
        val dateStr = obj.optString("date", LocalDate.now().toString())

        return CustomBlock(
            id = obj.getString("id"),
            title = obj.getString("title"),
            startTime = LocalTime.parse(obj.getString("startTime")),
            endTime = LocalTime.parse(obj.getString("endTime")),
            date = LocalDate.parse(dateStr),
            type = BlockType.valueOf(obj.getString("type")),
            color = obj.optInt("color", 0).takeIf { it != 0 }
        )
    }
}
