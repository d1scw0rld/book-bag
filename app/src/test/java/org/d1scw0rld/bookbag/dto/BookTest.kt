package org.d1scw0rld.bookbag.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.d1scw0rld.bookbag.DisplayNameRunner

@RunWith(DisplayNameRunner::class)
class BookTest {

    @DisplayName("Constructor - Default Instantiation - Sets Correct Defaults")
    @Test
    fun constructor_defaultInstantiation_setsCorrectDefaults() {
        val book = Book()
        assertEquals(DEFAULT_ID, book.id)
        assertTrue(book.title.value.isEmpty())
        assertTrue(book.properties.isEmpty())
    }

    @DisplayName("Constructor - Secondary Instantiation - Maps Primitives to Changeable Wrappers Correctly")
    @Test
    fun constructor_secondaryInstantiation_mapsPrimitivesToChangeableWrappers() {
        val book = Book(
            id = TEST_ID,
            title = TEST_TITLE,
            description = TEST_DESCRIPTION,
            volume = TEST_VOLUME,
            publicationDate = TEST_PUBLICATION_DATE,
            pages = TEST_PAGES,
            price = TEST_PRICE,
            value = TEST_VALUE,
            dueDate = TEST_DUE_DATE,
            readDate = TEST_READ_DATE,
            edition = TEST_EDITION,
            isbn = TEST_ISBN,
            web = TEST_WEB
        )

        assertEquals(TEST_ID.toLong(), book.id)
        assertEquals(TEST_TITLE, book.title.value)
        assertEquals(TEST_DESCRIPTION, book.description.value)
        assertEquals(TEST_VOLUME, book.volume.value)
        assertEquals(TEST_PUBLICATION_DATE, book.publicationDate.value)
        assertEquals(TEST_PAGES, book.pages.value)
        assertEquals(TEST_PRICE, book.price.value)
        assertEquals(TEST_VALUE, book.value.value)
        assertEquals(TEST_DUE_DATE, book.dueDate.value)
        assertEquals(TEST_READ_DATE, book.readDate.value)
        assertEquals(TEST_EDITION, book.edition.value)
        assertEquals(TEST_ISBN, book.isbn.value)
        assertEquals(TEST_WEB, book.web.value)
    }

    companion object {
        const val DEFAULT_ID = 0L
        const val TEST_ID = 15
        const val TEST_TITLE = "Title"
        const val TEST_DESCRIPTION = "Description"
        const val TEST_VOLUME = 4
        const val TEST_PUBLICATION_DATE = 2021
        const val TEST_PAGES = 420
        const val TEST_PRICE = "1200|1"
        const val TEST_VALUE = "1800|1"
        const val TEST_DUE_DATE = 20241231
        const val TEST_READ_DATE = 20241010
        const val TEST_EDITION = 2
        const val TEST_ISBN = "0987654321"
        const val TEST_WEB = "http://web.com"
    }
}
