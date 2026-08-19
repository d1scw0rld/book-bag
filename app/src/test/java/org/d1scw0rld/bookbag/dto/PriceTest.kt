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
        assertEquals(DEFAULT_VALUE, price.value)
        assertEquals(DEFAULT_CURRENCY_ID, price.currencyId)
    }

    @DisplayName("Constructor - Serialized Price String Provided - Parses Value and Currency Correctly")
    @Test
    fun constructor_serializedPriceStringProvided_parsesValueAndCurrencyCorrectly() {
        val price = Price(SERIALIZED_PRICE)
        assertEquals(PRICE_VALUE, price.value)
        assertEquals(PRICE_CURRENCY_ID, price.currencyId)
    }

    @DisplayName("Constructor - Serialized Price String Without Currency Provided - Parses Value and Defaults Currency")
    @Test
    fun constructor_serializedPriceStringWithoutCurrencyProvided_parsesValueAndDefaultsCurrency() {
        val price = Price(SERIALIZED_PRICE_NO_CURRENCY)
        assertEquals(PRICE_VALUE_NO_CURRENCY, price.value)
        assertEquals(DEFAULT_CURRENCY_ID, price.currencyId)
    }

    @DisplayName("Constructor - Empty or Malformed String Provided - Handles Gracefully and Defaults")
    @Test
    fun constructor_emptyOrMalformedStringProvided_handlesGracefullyAndDefaults() {
        val price1 = Price(MALFORMED_PRICE_1)
        val price2 = Price(MALFORMED_PRICE_2)

        assertEquals(DEFAULT_VALUE, price1.value)
        assertEquals(MALFORMED_CURRENCY_ID_1, price1.currencyId)

        assertEquals(DEFAULT_VALUE, price2.value)
        assertEquals(MALFORMED_CURRENCY_ID_2, price2.currencyId)
    }

    @DisplayName("ToString - Non Zero Values Provided - Formats Serialized Price Correctly")
    @Test
    fun toString_nonZeroValuesProvided_formatsSerializedPriceCorrectly() {
        val price = Price(NON_ZERO_VALUE, NON_ZERO_CURRENCY_ID)
        assertEquals(FORMATTED_SERIALIZED_PRICE, price.toString())
    }

    @DisplayName("ToString - Zero Values Provided - Returns Empty String")
    @Test
    fun toString_zeroValuesProvided_returnsEmptyString() {
        val price = Price(DEFAULT_VALUE, ZERO_VALUE_CURRENCY_ID)
        assertEquals(EMPTY_STRING, price.toString())
    }

    companion object {
        const val DEFAULT_VALUE = 0
        const val DEFAULT_CURRENCY_ID = 0L

        const val SERIALIZED_PRICE = "1599|2"
        const val PRICE_VALUE = 1599
        const val PRICE_CURRENCY_ID = 2L

        const val SERIALIZED_PRICE_NO_CURRENCY = "950"
        const val PRICE_VALUE_NO_CURRENCY = 950

        const val MALFORMED_PRICE_1 = "abc|2"
        const val MALFORMED_PRICE_2 = "|5"
        const val MALFORMED_CURRENCY_ID_1 = 2L
        const val MALFORMED_CURRENCY_ID_2 = 5L

        const val NON_ZERO_VALUE = 1999
        const val NON_ZERO_CURRENCY_ID = 1L
        const val FORMATTED_SERIALIZED_PRICE = "1999|1"

        const val ZERO_VALUE_CURRENCY_ID = 5L
        const val EMPTY_STRING = ""
    }
}
