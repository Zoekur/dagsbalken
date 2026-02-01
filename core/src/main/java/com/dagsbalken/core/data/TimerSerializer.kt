package com.dagsbalken.core.data

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime

object TimerSerializer {
    private const val TAG = "TimerSerializer"
    private const val MAX_LOG_LENGTH = 500

    private fun truncateForLog(jsonString: String): String {
        return if (jsonString.length > MAX_LOG_LENGTH) {
            "${jsonString.take(MAX_LOG_LENGTH)}... (truncated, length: ${jsonString.length})"
        } else {
            jsonString
        }
    }

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

    private fun deserializeTimerModel(obj: JSONObject): TimerModel {
        return TimerModel(
            id = obj.getString("id"),
            name = obj.getString("name"),
            durationHours = obj.getInt("durationHours"),
            durationMinutes = obj.getInt("durationMinutes"),
            colorHex = obj.getInt("colorHex")
        )
    }

    fun deserializeTimerTemplatesWithRecovery(
        jsonString: String,
        backupString: String?,
        throwOnFailure: Boolean = false
    ): List<TimerModel> {
        if (throwOnFailure) {
            try {
                val jsonArray = JSONArray(jsonString)
                val list = mutableListOf<TimerModel>()
                for (i in 0 until jsonArray.length()) {
                    list.add(deserializeTimerModel(jsonArray.getJSONObject(i)))
                }
                return list
            } catch (e: Exception) {
                throw IOException("Failed to deserialize timer templates during write", e)
            }
        }

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
                    Log.w(TAG, "Skipping corrupted timer template at index $i", e)
                }
            }

            if (hasCorruption && list.isEmpty() && backupString != null) {
                Log.w(TAG, "All items corrupted, attempting restore from backup")
                return deserializeTimerTemplatesWithRecovery(backupString, null, false)
            }

            if (hasCorruption) {
                Log.w(TAG, "Recovered ${list.size} timer templates, some items were corrupted")
            }

            list
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse timer templates JSON. Data: ${truncateForLog(jsonString)}", e)
            if (backupString != null) {
                Log.w(TAG, "Attempting restore from backup")
                try {
                    return deserializeTimerTemplatesWithRecovery(backupString, null, false)
                } catch (backupError: Exception) {
                    Log.e(TAG, "Backup also corrupted", backupError)
                }
            }
            emptyList()
        }
    }

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

    fun deserializeActiveTimersWithRecovery(
        jsonString: String,
        backupString: String?,
        throwOnFailure: Boolean = false
    ): List<CustomBlock> {
        if (throwOnFailure) {
            try {
                val jsonArray = JSONArray(jsonString)
                val list = mutableListOf<CustomBlock>()
                for (i in 0 until jsonArray.length()) {
                    list.add(deserializeCustomBlock(jsonArray.getJSONObject(i)))
                }
                return list
            } catch (e: Exception) {
                throw IOException("Failed to deserialize active timers during write", e)
            }
        }

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
                    Log.w(TAG, "Skipping corrupted active timer at index $i", e)
                }
            }

            if (hasCorruption && list.isEmpty() && backupString != null) {
                Log.w(TAG, "All items corrupted, attempting restore from backup")
                return deserializeActiveTimersWithRecovery(backupString, null, false)
            }

            if (hasCorruption) {
                Log.w(TAG, "Recovered ${list.size} active timers, some items were corrupted")
            }

            list
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse active timers JSON. Data: ${truncateForLog(jsonString)}", e)
            if (backupString != null) {
                Log.w(TAG, "Attempting restore from backup")
                try {
                    return deserializeActiveTimersWithRecovery(backupString, null, false)
                } catch (backupError: Exception) {
                    Log.e(TAG, "Backup also corrupted", backupError)
                }
            }
            emptyList()
        }
    }
}
