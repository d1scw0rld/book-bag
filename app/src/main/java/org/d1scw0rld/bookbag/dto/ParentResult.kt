package org.d1scw0rld.bookbag.dto

class ParentResult(
    override val name: String,
    override val childList: List<BookResult>,
) : Parent<BookResult> {

    override val isInitiallyExpanded: Boolean
        get() = true

}
