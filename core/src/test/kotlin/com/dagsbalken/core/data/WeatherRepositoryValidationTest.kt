package com.dagsbalken.core.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherRepositoryValidationTest {

    @Test
    fun testIsValidSearchQuery() {
        // Too short
        assertFalse("Query with 1 char should be invalid", WeatherRepository.isValidSearchQuery("a"))
        assertFalse("Empty query should be invalid", WeatherRepository.isValidSearchQuery(""))

        // Valid
        assertTrue("Query with 2 chars should be valid", WeatherRepository.isValidSearchQuery("ab"))
        assertTrue("Query with 100 chars should be valid", WeatherRepository.isValidSearchQuery("a".repeat(100)))

        // Too long
        assertFalse("Query with 101 chars should be invalid", WeatherRepository.isValidSearchQuery("a".repeat(101)))
    }
}
