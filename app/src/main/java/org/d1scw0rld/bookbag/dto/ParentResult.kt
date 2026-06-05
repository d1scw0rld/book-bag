package org.d1scw0rld.bookbag.dto

/**
 * Data Transfer Object representing a parent item containing a list of child book results.
 * Implementing [Parent] allows this DTO to be used in hierarchical structures such as expandable lists.
 */
data class ParentResult(
    override val name: String,
    override val childList: List<BookResult>,
    override val isInitiallyExpanded: Boolean = true,
) : Parent<BookResult>
