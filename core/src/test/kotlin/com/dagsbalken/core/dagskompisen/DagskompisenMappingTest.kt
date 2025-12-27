package com.dagsbalken.core.dagskompisen

import org.junit.Assert.assertEquals
import org.junit.Test

class DagskompisenMappingTest {

    @Test
    fun `rain maps to rain outfit and rain overlay`() {
        val ctx = WeatherContext(WeatherCondition.RAIN, temperatureC = 12)
        assertEquals("outfit_rain", ctx.toOutfitName())
        assertEquals("overlay_rain", ctx.toOverlayName())
    }

    @Test
    fun `snow maps to snow outfit and snow overlay`() {
        val ctx = WeatherContext(WeatherCondition.SNOW, temperatureC = -3)
        assertEquals("outfit_snow", ctx.toOutfitName())
        assertEquals("overlay_snow", ctx.toOverlayName())
    }

    @Test
    fun `sun maps to no outfit and sun overlay`() {
        val ctx = WeatherContext(WeatherCondition.SUN, temperatureC = 20)
        assertEquals("", ctx.toOutfitName())
        assertEquals("overlay_sun", ctx.toOverlayName())
    }

    @Test
    fun `windy maps to windy outfit and overlay`() {
        val ctx = WeatherContext(WeatherCondition.WINDY, windSpeedMs = 10)
        assertEquals("outfit_windy", ctx.toOutfitName())
        assertEquals("overlay_windy", ctx.toOverlayName())
    }
}

