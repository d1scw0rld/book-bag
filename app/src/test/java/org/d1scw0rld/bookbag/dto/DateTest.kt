package org.d1scw0rld.bookbag.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DateTest {

    @Test
    fun `default constructor sets date to 01-01-1900`() {
        val date = Date()
        assertEquals(1, date.day)
        assertEquals(1, date.month)
        assertEquals(1900, date.year)
    }

    @Test
    fun `dateValue constructor parses yyyymmdd correctly`() {
        val dateValue = 20231124 // 24th November 2023
        val date = Date(dateValue)
        assertEquals(24, date.day)
        assertEquals(11, date.month)
        assertEquals(2023, date.year)
    }

    @Test
    fun `dateValue constructor parses empty or zero date value to 0-0-0`() {
        val date = Date(0)
        assertEquals(0, date.day)
        assertEquals(0, date.month)
        assertEquals(0, date.year)
    }

    @Test
    fun `copy constructor duplicates date fields exactly`() {
        val original = Date(12, 6, 2018)
        val copy = Date(original)
        assertEquals(original.day, copy.day)
        assertEquals(original.month, copy.month)
        assertEquals(original.year, copy.year)
    }

    @Test
    fun `toInt converts date to yyyymmdd integer correctly`() {
        val date = Date(25, 12, 2024)
        assertEquals(20241225, date.toInt())
    }

    @Test
    fun `toString formats date to dd-mm-yyyy string`() {
        val date = Date(5, 9, 2023)
        assertEquals("05/09/2023", date.toString())
    }

    @Test
    fun `compareTo correctly orders dates chronologically`() {
        val earlyDate = Date(15, 5, 2021)
        val laterDate = Date(10, 8, 2022)
        val sameDate = Date(15, 5, 2021)

        assertTrue(earlyDate < laterDate)
        assertTrue(laterDate > earlyDate)
        assertEquals(0, earlyDate.compareTo(sameDate))
    }

    @Test
    fun `data class equality copy and hashCode work correctly`() {
        val d1 = Date(1, 1, 2020)
        val d2 = d1.copy()
        val d3 = d1.copy(day = 2)

        assertEquals(d1, d2)
        assertNotEquals(d1, d3)
        assertEquals(d1.hashCode(), d2.hashCode())
    }
}
