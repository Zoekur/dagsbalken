package com.dagsbalken.core.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherRepositoryTest {

    @Test
    fun isValidSearchQuery_validLength_returnsTrue() {
        // 2 chars
        assertTrue(WeatherRepository.isValidSearchQuery("ab"))
        // 50 chars
        assertTrue(WeatherRepository.isValidSearchQuery("a".repeat(50)))
        // 100 chars
        assertTrue(WeatherRepository.isValidSearchQuery("a".repeat(100)))
    }

    @Test
    fun isValidSearchQuery_tooShort_returnsFalse() {
        assertFalse(WeatherRepository.isValidSearchQuery(""))
        assertFalse(WeatherRepository.isValidSearchQuery("a"))
    }

    @Test
    fun isValidSearchQuery_tooLong_returnsFalse() {
        // 101 chars
        assertFalse(WeatherRepository.isValidSearchQuery("a".repeat(101)))
        // 1000 chars
        assertFalse(WeatherRepository.isValidSearchQuery("a".repeat(1000)))
    }
}
