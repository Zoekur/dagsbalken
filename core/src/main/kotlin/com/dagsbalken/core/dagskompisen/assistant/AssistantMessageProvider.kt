package com.dagsbalken.core.dagskompisen.assistant

import com.dagsbalken.core.dagskompisen.WeatherContext

/**
 * Provide short assistant messages based on the weather context.
 * Implementations must be offline and deterministic.
 */
interface AssistantMessageProvider {
    fun message(context: WeatherContext): String
}

