package org.d1scw0rld.bookbag.dto

import org.junit.Assert.assertEquals
import org.junit.Test

class PriceTest {

    @Test
    fun `default constructor sets default values`() {
        val price = Price()
        assertEquals(0, price.value)
        assertEquals(0L, price.currencyId)
    }

    @Test
    fun `priceString constructor parses value and currency correctly`() {
        val price = Price("1599|2")
        assertEquals(1599, price.value)
        assertEquals(2L, price.currencyId)
    }

    @Test
    fun `priceString constructor parses string without currency correctly`() {
        val price = Price("950")
        assertEquals(950, price.value)
        assertEquals(0L, price.currencyId)
    }

    @Test
    fun `priceString constructor handles empty or malformed strings gracefully`() {
        val price1 = Price("abc|2")
        val price2 = Price("|5")

        assertEquals(0, price1.value)
        assertEquals(2L, price1.currencyId)

        assertEquals(0, price2.value)
        assertEquals(5L, price2.currencyId)
    }

    @Test
    fun `toString formats serialized price correctly for non-zero values`() {
        val price = Price(1999, 1L)
        assertEquals("1999|1", price.toString())
    }

    @Test
    fun `toString returns empty string for zero values`() {
        val price = Price(0, 5L)
        assertEquals("", price.toString())
    }
}
