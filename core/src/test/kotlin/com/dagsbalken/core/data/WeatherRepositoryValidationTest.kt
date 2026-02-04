package com.dagsbalken.core.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherRepositoryValidationTest {

    @Test
    fun `isValidSearchQuery returns true for valid queries`() {
        assertTrue(WeatherRepository.isValidSearchQuery("Stockholm"))
        assertTrue(WeatherRepository.isValidSearchQuery("New York"))
        assertTrue(WeatherRepository.isValidSearchQuery("A"))
    }

    @Test
    fun `isValidSearchQuery returns false for queries exceeding max length`() {
        val longQuery = "a".repeat(101)
        assertFalse("Query of 101 chars should be invalid", WeatherRepository.isValidSearchQuery(longQuery))
    }

    @Test
    fun `isValidSearchQuery returns true for max length query`() {
        val maxQuery = "a".repeat(100)
        assertTrue("Query of 100 chars should be valid", WeatherRepository.isValidSearchQuery(maxQuery))
    }
}
