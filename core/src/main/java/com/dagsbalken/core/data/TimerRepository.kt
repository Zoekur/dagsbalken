package com.dagsbalken.core.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

private val Context.timerDataStore by preferencesDataStore(name = "timer_preferences")

/**
 * Repository for managing timer templates and active timers.
 *
 * This repository handles two distinct but related concepts:
 *
 * **Timer Templates** ([TimerModel]):
 * - Reusable timer configurations that users can create and save
 * - Define the name, duration, and color for a timer type
 * - Serve as blueprints for creating active timer instances
 * - Persist across app sessions and can be used multiple times
 *
 * **Active Timers** ([CustomBlock]):
 * - Running timer instances created from timer templates (or manually)
 * - Have specific start and end times, and are tied to a particular date
 * - Displayed on the timeline as visual blocks
 * - Can be started from a template or created ad-hoc
 *
 * **Relationship**:
 * When a user starts a timer from a template, the template's properties (name, duration, color)
 * are used to create a new [CustomBlock] with calculated start/end times. The template remains
 * unchanged and can be reused to create additional active timer instances.
 *
 * Both timer templates and active timers are stored in the same DataStore but under different
 * keys to maintain separation of concerns.
 */
class TimerRepository(private val context: Context) {

    companion object {
        private const val TAG = "TimerRepository"
        private val TIMER_TEMPLATES_KEY = stringPreferencesKey("timer_templates")
        private val ACTIVE_TIMERS_KEY = stringPreferencesKey("active_timers")
    }

    // --- Timer Templates ---

    val timerTemplatesFlow: Flow<List<TimerModel>> = context.timerDataStore.data
        .map { preferences ->
            val jsonString = preferences[TIMER_TEMPLATES_KEY] ?: "[]"
            deserializeTimerTemplates(jsonString)
        }

    suspend fun saveTimerTemplate(timer: TimerModel) {
        context.timerDataStore.edit { preferences ->
            val currentList = deserializeTimerTemplates(preferences[TIMER_TEMPLATES_KEY] ?: "[]").toMutableList()
            val index = currentList.indexOfFirst { it.id == timer.id }
            if (index != -1) {
                currentList[index] = timer
            } else {
                currentList.add(timer)
            }
            preferences[TIMER_TEMPLATES_KEY] = serializeTimerTemplates(currentList)
        }
    }

    suspend fun deleteTimerTemplate(timerId: String) {
        context.timerDataStore.edit { preferences ->
            val currentList = deserializeTimerTemplates(preferences[TIMER_TEMPLATES_KEY] ?: "[]").toMutableList()
            currentList.removeAll { it.id == timerId }
            preferences[TIMER_TEMPLATES_KEY] = serializeTimerTemplates(currentList)
        }
    }

    private fun serializeTimerTemplates(timers: List<TimerModel>): String {
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

    private fun deserializeTimerTemplates(jsonString: String): List<TimerModel> {
        val list = mutableListOf<TimerModel>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    TimerModel(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        durationHours = obj.getInt("durationHours"),
                        durationMinutes = obj.getInt("durationMinutes"),
                        colorHex = obj.getInt("colorHex")
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to deserialize timer templates. Data may be corrupt. Returning empty list.", e)
            Log.e(TAG, "Corrupted JSON data: $jsonString")
        }
        return list
    }

    // --- Active Timers (CustomBlocks) ---

    val activeTimersFlow: Flow<List<CustomBlock>> = context.timerDataStore.data
        .map { preferences ->
            val jsonString = preferences[ACTIVE_TIMERS_KEY] ?: "[]"
            deserializeActiveTimers(jsonString)
        }

    suspend fun addActiveTimer(block: CustomBlock) {
        context.timerDataStore.edit { preferences ->
            val currentList = deserializeActiveTimers(preferences[ACTIVE_TIMERS_KEY] ?: "[]").toMutableList()
            currentList.add(block)
            preferences[ACTIVE_TIMERS_KEY] = serializeActiveTimers(currentList)
        }
    }

    suspend fun removeActiveTimer(blockId: String) {
        context.timerDataStore.edit { preferences ->
            val currentList = deserializeActiveTimers(preferences[ACTIVE_TIMERS_KEY] ?: "[]").toMutableList()
            currentList.removeAll { it.id == blockId }
            preferences[ACTIVE_TIMERS_KEY] = serializeActiveTimers(currentList)
        }
    }

    private fun serializeActiveTimers(blocks: List<CustomBlock>): String {
        val jsonArray = JSONArray()
        blocks.forEach { block ->
            val jsonObj = JSONObject().apply {
                put("id", block.id)
                put("title", block.title)
                put("startTime", block.startTime.toString())
                put("endTime", block.endTime.toString())
                put("date", block.date.toString()) // Serialize date
                put("type", block.type.name)
                put("color", block.color ?: 0)
            }
            jsonArray.put(jsonObj)
        }
        return jsonArray.toString()
    }

    private fun deserializeActiveTimers(jsonString: String): List<CustomBlock> {
        val list = mutableListOf<CustomBlock>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                // Use current date if date field is missing (backward compatibility)
                val dateStr = obj.optString("date", LocalDate.now().toString())

                list.add(
                    CustomBlock(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        startTime = java.time.LocalTime.parse(obj.getString("startTime")),
                        endTime = java.time.LocalTime.parse(obj.getString("endTime")),
                        date = java.time.LocalDate.parse(dateStr),
                        type = BlockType.valueOf(obj.getString("type")),
                        color = obj.optInt("color", 0).takeIf { it != 0 }
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to deserialize active timers. Data may be corrupt. Returning empty list.", e)
            Log.e(TAG, "Corrupted JSON data: $jsonString")
        }
        return list
    }
}
