package com.dagsbalken.core.dagskompisen

import com.dagsbalken.core.dagskompisen.assistant.RuleBasedMessageProvider
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageProviderTest {
    private val provider = RuleBasedMessageProvider()

    @Test
    fun `rain message contains regn`() {
        val ctx = WeatherContext(WeatherCondition.RAIN, temperatureC = 10)
        val msg = provider.message(ctx)
        assertTrue(msg.contains("regn") || msg.contains("regnjack"))
    }

    @Test
    fun `cold message suggests mössa`() {
        val ctx = WeatherContext(WeatherCondition.SNOW, temperatureC = -5)
        val msg = provider.message(ctx)
        assertTrue(msg.contains("Mössa") || msg.contains("varma"))
    }

    @Test
    fun `hot message mentions solskydd or varma`() {
        val ctx = WeatherContext(WeatherCondition.HOT, temperatureC = 30)
        val msg = provider.message(ctx)
        assertTrue(msg.contains("solskydd") || msg.contains("Varmt"))
    }
}

