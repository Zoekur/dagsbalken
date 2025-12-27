package com.dagsbalken.core.dagskompisen.assistant

import com.dagsbalken.core.dagskompisen.WeatherContext
import com.dagsbalken.core.dagskompisen.WeatherCondition

/**
 * Simple rule-based Swedish message provider. Short, friendly, slightly playful.
 * No network calls, pure function outputs for easy unit testing.
 */
class RuleBasedMessageProvider(
    private val mood: AssistantMood = AssistantMood.CHEERFUL
) : AssistantMessageProvider {
    override fun message(context: WeatherContext): String {
        // Temperature based hints
        val temp = context.temperatureC
        if (temp != null) {
            if (temp <= 0) return "Kallt idag. Mössa = bra beslut."
            if (temp in 1..7) return "Kyligt — ta en varm jacka."
            if (temp >= 25) return "Varmt ute. Lätta kläder räcker gott."
        }

        // Condition-based hints (priority over neutral temp hints)
        return when (context.condition) {
            WeatherCondition.WINDY -> "Det ser blåsigt ut – en vindtät jacka är ingen dum idé."
            WeatherCondition.RAIN -> "Oj, regn på gång! Ska jag lägga fram regnjackan?"
            WeatherCondition.STORM -> "Storm varning — håll dig inne om du kan."
            WeatherCondition.SNOW -> "Snö på väg. Varma stövlar och mössa rekommenderas."
            WeatherCondition.FOG -> "Dimma ute — köra försiktigt om du ska iväg."
            WeatherCondition.HOT -> "Solen skiner starkt — drick vatten och tänk på solskydd."
            WeatherCondition.CLOUDY -> "Molnigt idag. Lätt lager + en jacka räcker oftast."
            WeatherCondition.SUN -> "Härligt soligt! Solglasögon kan vara en bra idé."
        }
    }
}

