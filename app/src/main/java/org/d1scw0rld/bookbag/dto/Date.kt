package org.d1scw0rld.bookbag.dto

import java.util.Locale

data class Date(
    val day: Int,
    val month: Int,
    val year: Int
) : Comparable<Date> {

    // Default constructor (sets day=1, month=1, year=1900 as in original)
    constructor() : this(1, 1, 1900)

    // Parse from an integer value (e.g., yyyymmdd)
    constructor(dateValue: Int) : this(
        day = dateValue % 100,
        month = (dateValue / 100) % 100,
        year = dateValue / 10000
    )

    // Copy constructor for compatibility
    constructor(otherDate: Date) : this(otherDate.day, otherDate.month, otherDate.year)

    fun toInt(): Int {
        return year * 10000 + month * 100 + day
    }

    override fun toString(): String {
        return String.format(Locale.getDefault(), "%02d/%02d/%d", day, month, year)
    }

    override fun compareTo(other: Date): Int {
        return toInt().compareTo(other.toInt())
    }
}
