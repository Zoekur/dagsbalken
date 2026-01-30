package com.dagsbalken.core.data

import android.content.Context
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime

private val Context.timerDataStore by preferencesDataStore(name = "timer_preferences")

class TimerRepository(private val context: Context) {

    companion object {
        private const val TAG = "TimerRepository"
        private val TIMER_TEMPLATES_KEY = stringPreferencesKey("timer_templates")
        private val ACTIVE_TIMERS_KEY = stringPreferencesKey("active_timers")
        private val TIMER_TEMPLATES_BACKUP_KEY = stringPreferencesKey("timer_templates_backup")
        private val ACTIVE_TIMERS_BACKUP_KEY = stringPreferencesKey("active_timers_backup")
        
        private const val MAX_LOG_LENGTH = 500
        
        /**
         * Safely truncates JSON string for logging to avoid exposing sensitive data.
         */
        private fun truncateForLog(jsonString: String): String {
            return if (jsonString.length > MAX_LOG_LENGTH) {
                "${jsonString.take(MAX_LOG_LENGTH)}... (truncated, length: ${jsonString.length})"
            } else {
                jsonString
            }
        }

        @VisibleForTesting
        internal fun serializeTimerTemplates(timers: List<TimerModel>): String {
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

        @VisibleForTesting
        internal fun deserializeTimerModel(obj: JSONObject): TimerModel {
            return TimerModel(
                id = obj.getString("id"),
                name = obj.getString("name"),
                durationHours = obj.getInt("durationHours"),
                durationMinutes = obj.getInt("durationMinutes"),
                colorHex = obj.getInt("colorHex")
            )
        }

        private fun deserializeTimerTemplates(jsonString: String): List<TimerModel> {
            val list = mutableListOf<TimerModel>()
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                try {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(deserializeTimerModel(obj))
                } catch (e: Exception) {
                    Log.e(TAG, "Skipping invalid timer template at index $i", e)
                }
            }
            return list
        }

        /**
         * Attempts to deserialize timer templates with item-level recovery.
         * On complete failure, attempts to restore from backup.
         *
         * @param throwOnFailure if true, any corruption (JSON format or item deserialization)
         *                       will throw an exception instead of swallowing it.
         *                       Backup restoration is NOT attempted in this mode.
         */
        @VisibleForTesting
        internal fun deserializeTimerTemplatesWithRecovery(
            jsonString: String,
            backupString: String?,
            throwOnFailure: Boolean = false
        ): List<TimerModel> {
            if (throwOnFailure) {
                // Strict mode: Fail fast on any corruption
                val jsonArray = JSONArray(jsonString)
                val list = mutableListOf<TimerModel>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(deserializeTimerModel(obj))
                }
                return list
            }

            // Recovery mode
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
                    return deserializeTimerTemplates(backupString)
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
                        return deserializeTimerTemplates(backupString)
                    } catch (backupError: Exception) {
                        Log.e(TAG, "Backup also corrupted", backupError)
                    }
                }
                emptyList()
            }
        }

        @VisibleForTesting
        internal fun serializeActiveTimers(blocks: List<CustomBlock>): String {
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

        @VisibleForTesting
        internal fun deserializeCustomBlock(obj: JSONObject): CustomBlock {
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

        private fun deserializeActiveTimers(jsonString: String): List<CustomBlock> {
            val list = mutableListOf<CustomBlock>()
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                try {
                    val obj = jsonArray.getJSONObject(i)
                    val dateStr = obj.optString("date", LocalDate.now().toString())
                    list.add(
                        CustomBlock(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            startTime = LocalTime.parse(obj.getString("startTime")),
                            endTime = LocalTime.parse(obj.getString("endTime")),
                            date = LocalDate.parse(dateStr),
                            type = BlockType.valueOf(obj.getString("type")),
                            color = obj.optInt("color", 0).takeIf { it != 0 }
                        )
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Skipping invalid active timer at index $i", e)
                }
            }
            return list
        }

        /**
         * Attempts to deserialize active timers with item-level recovery.
         * On complete failure, attempts to restore from backup.
         *
         * @param throwOnFailure if true, any corruption (JSON format or item deserialization)
         *                       will throw an exception instead of swallowing it.
         *                       Backup restoration is NOT attempted in this mode.
         */
        @VisibleForTesting
        internal fun deserializeActiveTimersWithRecovery(
            jsonString: String,
            backupString: String?,
            throwOnFailure: Boolean = false
        ): List<CustomBlock> {
            if (throwOnFailure) {
                // Strict mode
                val jsonArray = JSONArray(jsonString)
                val list = mutableListOf<CustomBlock>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(deserializeCustomBlock(obj))
                }
                return list
            }

            // Recovery mode
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
                    return deserializeActiveTimers(backupString)
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
                        return deserializeActiveTimers(backupString)
                    } catch (backupError: Exception) {
                        Log.e(TAG, "Backup also corrupted", backupError)
                    }
                }
                emptyList()
            }
        }
    }

    // --- Timer Templates ---

    val timerTemplatesFlow: Flow<List<TimerModel>> = context.timerDataStore.data
        .map { preferences ->
            val jsonString = preferences[TIMER_TEMPLATES_KEY] ?: "[]"
            val backupString = preferences[TIMER_TEMPLATES_BACKUP_KEY]
            deserializeTimerTemplatesWithRecovery(jsonString, backupString, throwOnFailure = false)
        }

    suspend fun saveTimerTemplate(timer: TimerModel) {
        context.timerDataStore.edit { preferences ->
            val jsonString = preferences[TIMER_TEMPLATES_KEY] ?: "[]"
            val backupString = preferences[TIMER_TEMPLATES_BACKUP_KEY]
            // Strict deserialization to prevent data loss on write
            val initialList = deserializeTimerTemplatesWithRecovery(jsonString, backupString, throwOnFailure = true)
            val currentList = initialList.toMutableList()

            val index = currentList.indexOfFirst { it.id == timer.id }
            if (index != -1) {
                currentList[index] = timer
            } else {
                currentList.add(timer)
            }
            
            val serialized = serializeTimerTemplates(currentList)
            preferences[TIMER_TEMPLATES_BACKUP_KEY] = serializeTimerTemplates(initialList)
            preferences[TIMER_TEMPLATES_KEY] = serialized
        }
    }

    suspend fun deleteTimerTemplate(timerId: String) {
        context.timerDataStore.edit { preferences ->
            val jsonString = preferences[TIMER_TEMPLATES_KEY] ?: "[]"
            val backupString = preferences[TIMER_TEMPLATES_BACKUP_KEY]
            // Strict deserialization to prevent data loss on write
            val initialList = deserializeTimerTemplatesWithRecovery(jsonString, backupString, throwOnFailure = true)
            val currentList = initialList.toMutableList()

            currentList.removeAll { it.id == timerId }
            
            val serialized = serializeTimerTemplates(currentList)
            preferences[TIMER_TEMPLATES_BACKUP_KEY] = serializeTimerTemplates(initialList)
            preferences[TIMER_TEMPLATES_KEY] = serialized
        }
    }

    // --- Active Timers (CustomBlocks) ---

    val activeTimersFlow: Flow<List<CustomBlock>> = context.timerDataStore.data
        .map { preferences ->
            val jsonString = preferences[ACTIVE_TIMERS_KEY] ?: "[]"
            val backupString = preferences[ACTIVE_TIMERS_BACKUP_KEY]
            deserializeActiveTimersWithRecovery(jsonString, backupString, throwOnFailure = false)
        }

    suspend fun addActiveTimer(block: CustomBlock) {
        context.timerDataStore.edit { preferences ->
            val jsonString = preferences[ACTIVE_TIMERS_KEY] ?: "[]"
            val backupString = preferences[ACTIVE_TIMERS_BACKUP_KEY]
            // Strict deserialization to prevent data loss on write
            val initialList = deserializeActiveTimersWithRecovery(jsonString, backupString, throwOnFailure = true)
            val currentList = initialList.toMutableList()

            currentList.add(block)
            
            val serialized = serializeActiveTimers(currentList)
            preferences[ACTIVE_TIMERS_BACKUP_KEY] = serializeActiveTimers(initialList)
            preferences[ACTIVE_TIMERS_KEY] = serialized
        }
    }

    suspend fun removeActiveTimer(blockId: String) {
        context.timerDataStore.edit { preferences ->
            val jsonString = preferences[ACTIVE_TIMERS_KEY] ?: "[]"
            val backupString = preferences[ACTIVE_TIMERS_BACKUP_KEY]
            // Strict deserialization to prevent data loss on write
            val initialList = deserializeActiveTimersWithRecovery(jsonString, backupString, throwOnFailure = true)
            val currentList = initialList.toMutableList()

            currentList.removeAll { it.id == blockId }
            
            val serialized = serializeActiveTimers(currentList)
            preferences[ACTIVE_TIMERS_BACKUP_KEY] = serializeActiveTimers(initialList)
            preferences[ACTIVE_TIMERS_KEY] = serialized
        }
    }
}
