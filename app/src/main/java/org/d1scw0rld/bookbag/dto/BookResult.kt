package org.d1scw0rld.bookbag.dto

data class BookResult @JvmOverloads constructor(
    @JvmField val id: Long = 0,
    @JvmField val content: String = "",
)
