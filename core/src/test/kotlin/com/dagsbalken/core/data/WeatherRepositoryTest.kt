package com.dagsbalken.core.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherRepositoryTest {

    @Test
    fun testIsValidSearchQuery() {
        // Valid query (length < 100)
        assertTrue(WeatherRepository.isValidSearchQuery("Stockholm"))
        assertTrue(WeatherRepository.isValidSearchQuery("A"))
        assertTrue(WeatherRepository.isValidSearchQuery("This is a valid query with 99 characters..........................................................x"))

        // Invalid query (length > 100)
        val longQuery = "This is a very very very very very very very very very very very very very very very very very very very very very very very very long query that exceeds 100 characters limit."
        assertFalse(WeatherRepository.isValidSearchQuery(longQuery))

        // Edge case: exactly 100 chars
        val query100 = "x".repeat(100)
        assertTrue(WeatherRepository.isValidSearchQuery(query100))

        // Edge case: 101 chars
        val query101 = "x".repeat(101)
        assertFalse(WeatherRepository.isValidSearchQuery(query101))
    }
}
