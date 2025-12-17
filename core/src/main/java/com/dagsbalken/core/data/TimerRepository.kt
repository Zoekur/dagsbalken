package com.dagsbalken.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

private val Context.timerDataStore by preferencesDataStore(name = "timer_preferences")

class TimerRepository(private val context: Context) {

    companion object {
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
            e.printStackTrace()
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
            e.printStackTrace()
        }
        return list
    }
}
