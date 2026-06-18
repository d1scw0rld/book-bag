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
        assertEquals(0L, book.id)
        assertTrue(book.title.value.isEmpty())
        assertTrue(book.properties.isEmpty())
    }

    @DisplayName("Constructor - Secondary Instantiation - Maps Primitives to Changeable Wrappers Correctly")
    @Test
    fun constructor_secondaryInstantiation_mapsPrimitivesToChangeableWrappers() {
        val book = Book(
            id = 15,
            title = "Title",
            description = "Description",
            volume = 4,
            publicationDate = 2021,
            pages = 420,
            price = "1200|1",
            value = "1800|1",
            dueDate = 20241231,
            readDate = 20241010,
            edition = 2,
            isbn = "0987654321",
            web = "http://web.com"
        )

        assertEquals(15L, book.id)
        assertEquals("Title", book.title.value)
        assertEquals("Description", book.description.value)
        assertEquals(4, book.volume.value)
        assertEquals(2021, book.publicationDate.value)
        assertEquals(420, book.pages.value)
        assertEquals("1200|1", book.price.value)
        assertEquals("1800|1", book.value.value)
        assertEquals(20241231, book.dueDate.value)
        assertEquals(20241010, book.readDate.value)
        assertEquals(2, book.edition.value)
        assertEquals("0987654321", book.isbn.value)
        assertEquals("http://web.com", book.web.value)
    }
}
