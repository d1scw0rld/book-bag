package org.d1scw0rld.bookbag.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.d1scw0rld.bookbag.DisplayNameRunner

@RunWith(DisplayNameRunner::class)
class DateTest {

    @DisplayName("Constructor - Default Instantiation - Sets Date to Default 1900")
    @Test
    fun constructor_defaultInstantiation_setsDateToDefault1900() {
        val date = Date()
        assertEquals(1, date.day)
        assertEquals(1, date.month)
        assertEquals(1900, date.year)
    }

    @DisplayName("Constructor - Valid Date Value Int Provided - Parses Year Month Day Correctly")
    @Test
    fun constructor_validDateValueIntProvided_parsesYearMonthDayCorrectly() {
        val dateValue = 20231124 // 24th November 2023
        val date = Date(dateValue)
        assertEquals(24, date.day)
        assertEquals(11, date.month)
        assertEquals(2023, date.year)
    }

    @DisplayName("Constructor - Zero Date Value Int Provided - Parses to Zeros")
    @Test
    fun constructor_zeroDateValueIntProvided_parsesToZeros() {
        val date = Date(0)
        assertEquals(0, date.day)
        assertEquals(0, date.month)
        assertEquals(0, date.year)
    }

    @DisplayName("Constructor - Copy Instantiation - Duplicates Date Fields Exactly")
    @Test
    fun constructor_copyInstantiation_duplicatesDateFieldsExactly() {
        val original = Date(12, 6, 2018)
        val copy = Date(original)
        assertEquals(original.day, copy.day)
        assertEquals(original.month, copy.month)
        assertEquals(original.year, copy.year)
    }

    @DisplayName("To Int - Date Instance Parsed - Converts to Yyyymmdd Integer")
    @Test
    fun toInt_dateInstanceParsed_convertsToYyyymmddInteger() {
        val date = Date(25, 12, 2024)
        assertEquals(20241225, date.toInt())
    }

    @DisplayName("ToString - Date Instance Formatted - Returns Dd/Mm/Yyyy String")
    @Test
    fun toString_dateInstanceFormatted_returnsDdMmYyyyString() {
        val date = Date(5, 9, 2023)
        assertEquals("05/09/2023", date.toString())
    }

    @DisplayName("Compare To - Different Dates Provided - Orders Dates Chronologically")
    @Test
    fun compareTo_differentDatesProvided_ordersDatesChronologically() {
        val earlyDate = Date(15, 5, 2021)
        val laterDate = Date(10, 8, 2022)
        val sameDate = Date(15, 5, 2021)

        assertTrue(earlyDate < laterDate)
        assertTrue(laterDate > earlyDate)
        assertEquals(0, earlyDate.compareTo(sameDate))
    }

    @DisplayName("Equals and Hash Code - Identical and Copy Dates - Compares and Evaluates Correctly")
    @Test
    fun equalsAndHashCode_identicalAndCopyDates_comparesAndEvaluatesCorrectly() {
        val d1 = Date(1, 1, 2020)
        val d2 = d1.copy()
        val d3 = d1.copy(day = 2)

        assertEquals(d1, d2)
        assertNotEquals(d1, d3)
        assertEquals(d1.hashCode(), d2.hashCode())
    }
}
