package org.d1scw0rld.bookbag.dto

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.d1scw0rld.bookbag.DisplayNameRunner

@RunWith(DisplayNameRunner::class)
class PriceTest {

    @DisplayName("Constructor - Default Instantiation - Sets Default Values")
    @Test
    fun constructor_defaultInstantiation_setsDefaultValues() {
        val price = Price()
        assertEquals(0, price.value)
        assertEquals(0L, price.currencyId)
    }

    @DisplayName("Constructor - Serialized Price String Provided - Parses Value and Currency Correctly")
    @Test
    fun constructor_serializedPriceStringProvided_parsesValueAndCurrencyCorrectly() {
        val price = Price("1599|2")
        assertEquals(1599, price.value)
        assertEquals(2L, price.currencyId)
    }

    @DisplayName("Constructor - Serialized Price String Without Currency Provided - Parses Value and Defaults Currency")
    @Test
    fun constructor_serializedPriceStringWithoutCurrencyProvided_parsesValueAndDefaultsCurrency() {
        val price = Price("950")
        assertEquals(950, price.value)
        assertEquals(0L, price.currencyId)
    }

    @DisplayName("Constructor - Empty or Malformed String Provided - Handles Gracefully and Defaults")
    @Test
    fun constructor_emptyOrMalformedStringProvided_handlesGracefullyAndDefaults() {
        val price1 = Price("abc|2")
        val price2 = Price("|5")

        assertEquals(0, price1.value)
        assertEquals(2L, price1.currencyId)

        assertEquals(0, price2.value)
        assertEquals(5L, price2.currencyId)
    }

    @DisplayName("ToString - Non Zero Values Provided - Formats Serialized Price Correctly")
    @Test
    fun toString_nonZeroValuesProvided_formatsSerializedPriceCorrectly() {
        val price = Price(1999, 1L)
        assertEquals("1999|1", price.toString())
    }

    @DisplayName("ToString - Zero Values Provided - Returns Empty String")
    @Test
    fun toString_zeroValuesProvided_returnsEmptyString() {
        val price = Price(0, 5L)
        assertEquals("", price.toString())
    }
}
