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
        assertEquals(DEFAULT_DAY, date.day)
        assertEquals(DEFAULT_MONTH, date.month)
        assertEquals(DEFAULT_YEAR, date.year)
    }

    @DisplayName("Constructor - Valid Date Value Int Provided - Parses Year Month Day Correctly")
    @Test
    fun constructor_validDateValueIntProvided_parsesYearMonthDayCorrectly() {
        val dateValue = DATE_VALUE_NOV_24_2023 // 24th November 2023
        val date = Date(dateValue)
        assertEquals(DAY_24, date.day)
        assertEquals(MONTH_11, date.month)
        assertEquals(YEAR_2023, date.year)
    }

    @DisplayName("Constructor - Zero Date Value Int Provided - Parses to Zeros")
    @Test
    fun constructor_zeroDateValueIntProvided_parsesToZeros() {
        val date = Date(ZERO_DATE_VALUE)
        assertEquals(ZERO_DAY, date.day)
        assertEquals(ZERO_MONTH, date.month)
        assertEquals(ZERO_YEAR, date.year)
    }

    @DisplayName("Constructor - Copy Instantiation - Duplicates Date Fields Exactly")
    @Test
    fun constructor_copyInstantiation_duplicatesDateFieldsExactly() {
        val original = Date(DAY_12, MONTH_6, YEAR_2018)
        val copy = Date(original)
        assertEquals(original.day, copy.day)
        assertEquals(original.month, copy.month)
        assertEquals(original.year, copy.year)
    }

    @DisplayName("To Int - Date Instance Parsed - Converts to Yyyymmdd Integer")
    @Test
    fun toInt_dateInstanceParsed_convertsToYyyymmddInteger() {
        val date = Date(DAY_25, MONTH_12, YEAR_2024)
        assertEquals(DATE_VALUE_DEC_25_2024, date.toInt())
    }

    @DisplayName("ToString - Date Instance Formatted - Returns Dd/Mm/Yyyy String")
    @Test
    fun toString_dateInstanceFormatted_returnsDdMmYyyyString() {
        val date = Date(DAY_5, MONTH_9, YEAR_2023)
        assertEquals(FORMATTED_DATE_STR_SEP_05_2023, date.toString())
    }

    @DisplayName("Compare To - Different Dates Provided - Orders Dates Chronologically")
    @Test
    fun compareTo_differentDatesProvided_ordersDatesChronologically() {
        val earlyDate = Date(DAY_15, MONTH_5, YEAR_2021)
        val laterDate = Date(DAY_10, MONTH_8, YEAR_2022)
        val sameDate = Date(DAY_15, MONTH_5, YEAR_2021)

        assertTrue(earlyDate < laterDate)
        assertTrue(laterDate > earlyDate)
        assertEquals(COMPARE_TO_EQUAL_RESULT, earlyDate.compareTo(sameDate))
    }

    @DisplayName("Equals and Hash Code - Identical and Copy Dates - Compares and Evaluates Correctly")
    @Test
    fun equalsAndHashCode_identicalAndCopyDates_comparesAndEvaluatesCorrectly() {
        val d1 = Date(DAY_1, MONTH_1, YEAR_2020)
        val d2 = d1.copy()
        val d3 = d1.copy(day = DAY_2)

        assertEquals(d1, d2)
        assertNotEquals(d1, d3)
        assertEquals(d1.hashCode(), d2.hashCode())
    }

    companion object {
        const val DEFAULT_DAY = 1
        const val DEFAULT_MONTH = 1
        const val DEFAULT_YEAR = 1900

        const val DATE_VALUE_NOV_24_2023 = 20231124
        const val DAY_24 = 24
        const val MONTH_11 = 11
        const val YEAR_2023 = 2023

        const val ZERO_DATE_VALUE = 0
        const val ZERO_DAY = 0
        const val ZERO_MONTH = 0
        const val ZERO_YEAR = 0

        const val DAY_12 = 12
        const val MONTH_6 = 6
        const val YEAR_2018 = 2018

        const val DAY_25 = 25
        const val MONTH_12 = 12
        const val YEAR_2024 = 2024
        const val DATE_VALUE_DEC_25_2024 = 20241225

        const val DAY_5 = 5
        const val MONTH_9 = 9
        const val FORMATTED_DATE_STR_SEP_05_2023 = "05/09/2023"

        const val DAY_15 = 15
        const val MONTH_5 = 5
        const val YEAR_2021 = 2021

        const val DAY_10 = 10
        const val MONTH_8 = 8
        const val YEAR_2022 = 2022
        const val COMPARE_TO_EQUAL_RESULT = 0

        const val DAY_1 = 1
        const val MONTH_1 = 1
        const val YEAR_2020 = 2020
        const val DAY_2 = 2
    }
}
