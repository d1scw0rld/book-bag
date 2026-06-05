package org.d1scw0rld.bookbag.dto

/**
 * Data Transfer Object representing a Book.
 */
class Book(
    var id: Long = 0,

    var title: Changeable<String> = Changeable(""),
    var description: Changeable<String> = Changeable(""),
    var price: Changeable<String> = Changeable(""),
    var value: Changeable<String> = Changeable(""),
    var isbn: Changeable<String> = Changeable(""),
    var web: Changeable<String> = Changeable(""),

    var volume: Changeable<Int> = Changeable(0),
    var pages: Changeable<Int> = Changeable(0),
    var publicationDate: Changeable<Int> = Changeable(0),
    var edition: Changeable<Int> = Changeable(0),
    var readDate: Changeable<Int> = Changeable(0),
    var dueDate: Changeable<Int> = Changeable(0),

    var properties: ArrayList<Property> = ArrayList()
) {
    /**
     * Secondary constructor designed for database initialization.
     * Maps primitives directly to [Changeable] wrappers.
     */
    constructor(
        id: Int,
        title: String,
        description: String,
        volume: Int,
        publicationDate: Int,
        pages: Int,
        price: String,
        value: String,
        dueDate: Int,
        readDate: Int,
        edition: Int,
        isbn: String,
        web: String
    ) : this(
        id = id.toLong(),
        title = Changeable(title),
        description = Changeable(description),
        price = Changeable(price),
        value = Changeable(value),
        isbn = Changeable(isbn),
        web = Changeable(web),
        volume = Changeable(volume),
        pages = Changeable(pages),
        publicationDate = Changeable(publicationDate),
        edition = Changeable(edition),
        readDate = Changeable(readDate),
        dueDate = Changeable(dueDate)
    )
}
