package org.d1scw0rld.bookbag.dto

/**
 * Data Transfer Object representing a Book.
 *
 * This class uses Kotlin's primary constructor with default values and [JvmOverloads]
 * to allow seamless usage from Java (e.g., direct field access via [JvmField] and overload constructors).
 */
class Book @JvmOverloads constructor(
    @JvmField var id: Long = 0,

    @JvmField var title: Changeable<String> = Changeable(""),
    @JvmField var description: Changeable<String> = Changeable(""),
    @JvmField var price: Changeable<String> = Changeable(""),
    @JvmField var value: Changeable<String> = Changeable(""),
    @JvmField var isbn: Changeable<String> = Changeable(""),
    @JvmField var web: Changeable<String> = Changeable(""),

    @JvmField var volume: Changeable<Int> = Changeable(0),
    @JvmField var pages: Changeable<Int> = Changeable(0),
    @JvmField var publicationDate: Changeable<Int> = Changeable(0),
    @JvmField var edition: Changeable<Int> = Changeable(0),
    @JvmField var readDate: Changeable<Int> = Changeable(0),
    @JvmField var dueDate: Changeable<Int> = Changeable(0),

    @JvmField var properties: ArrayList<Property> = ArrayList()
) {
    /**
     * Secondary constructor designed for database initialization or Java clients passing raw types.
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
