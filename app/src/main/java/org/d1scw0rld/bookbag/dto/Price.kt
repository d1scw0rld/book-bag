package org.d1scw0rld.bookbag.dto

class Price {
    @JvmField var value: Int = 0
    @JvmField var currencyId: Long = 0

    constructor()

    constructor(priceString: String) : this() {
        if (priceString.isEmpty()) return

        val parts = priceString.split("|")
        if (parts.isNotEmpty()) {
            value = parts[0].toIntOrNull() ?: 0
        }
        if (parts.size > 1) {
            currencyId = parts[1].toLongOrNull() ?: 0
        }
    }

    constructor(value: Int, currency: Int) {
        this.value = value
        this.currencyId = currency.toLong()
    }

    override fun toString(): String {
        return if (value == 0) "" else "$value|$currencyId"
    }
}
