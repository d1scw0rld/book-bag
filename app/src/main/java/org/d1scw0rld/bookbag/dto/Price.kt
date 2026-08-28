package org.d1scw0rld.bookbag.dto

import kotlin.math.abs

/**
 * Data Transfer Object representing a Price.
 */
data class Price(
    var value: Int = 0,
    var currencyId: Long = 0
) {
    /**
     * Parses a Price from a serialized string format (e.g., "value|currencyId").
     * Uses efficient inline string operations to delegate to the primary constructor.
     */
    constructor(priceString: String) : this(
        value = priceString.substringBefore('|').toIntOrNull() ?: 0,
        currencyId = if ('|' in priceString) priceString.substringAfter('|').toLongOrNull() ?: 0 else 0
    )

    /**
     * Custom string serialization format used for database storage.
     * Keeps standard compatibility with existing storage formats.
     */
    override fun toString(): String {
        return if (value == 0) "" else "$value|$currencyId"
    }

    fun toFormattedString(currencyValue: String? = null, separator: Char): String {
        val wholePart = value / 100
        val fractionalPart = abs(value % 100).toString().padStart(2, '0')
        val amount = "$wholePart$separator$fractionalPart"

        return currencyValue?.let { "$amount $it" } ?: amount
    }
}
