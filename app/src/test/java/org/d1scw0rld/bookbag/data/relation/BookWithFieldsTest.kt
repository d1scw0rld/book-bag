package org.d1scw0rld.bookbag.data.relation

import org.d1scw0rld.bookbag.data.entity.BookEntity
import org.d1scw0rld.bookbag.data.entity.FieldEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.d1scw0rld.bookbag.DisplayNameRunner

@RunWith(DisplayNameRunner::class)
class BookWithFieldsTest {

    @DisplayName("To DTO - Valid Relation - Maps Book and Fields to Book DTO")
    @Test
    fun toDto_validRelation_mapsBookAndFieldsToBookDto() {
        // Arrange
        val entity = BookEntity(
            id = TEST_BOOK_ID,
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
        
        val fields = listOf(
            FieldEntity(id = TEST_FIELD_ID_1, typeId = TEST_FIELD_TYPE_1, name = TEST_FIELD_NAME_1),
            FieldEntity(id = TEST_FIELD_ID_2, typeId = TEST_FIELD_TYPE_2, name = TEST_FIELD_NAME_2)
        )
        
        val relation = BookWithFields(entity, fields)

        // Act
        val dto = relation.toDto()

        // Assert core book attributes
        assertEquals(TEST_BOOK_ID, dto.id)
        assertEquals(TEST_TITLE, dto.title.value)
        assertEquals(TEST_DESCRIPTION, dto.description.value)
        assertEquals(TEST_VOLUME, dto.volume.value)
        assertEquals(TEST_PUBLICATION_DATE, dto.publicationDate.value)
        assertEquals(TEST_PAGES, dto.pages.value)
        assertEquals(TEST_PRICE, dto.price.value)
        assertEquals(TEST_VALUE, dto.value.value)
        assertEquals(TEST_DUE_DATE, dto.dueDate.value)
        assertEquals(TEST_READ_DATE, dto.readDate.value)
        assertEquals(TEST_EDITION, dto.edition.value)
        assertEquals(TEST_ISBN, dto.isbn.value)
        assertEquals(TEST_WEB, dto.web.value)

        // Assert properties / custom fields list size and values
        assertEquals(2, dto.properties.size)
        assertEquals(TEST_FIELD_TYPE_1, dto.properties[0].fieldTypeId)
        assertEquals(TEST_FIELD_NAME_1, dto.properties[0].value)
        assertEquals(TEST_FIELD_ID_1, dto.properties[0].id)

        assertEquals(TEST_FIELD_TYPE_2, dto.properties[1].fieldTypeId)
        assertEquals(TEST_FIELD_NAME_2, dto.properties[1].value)
        assertEquals(TEST_FIELD_ID_2, dto.properties[1].id)
    }

    companion object {
        const val TEST_BOOK_ID = 5L
        const val TEST_TITLE = "Test Book"
        const val TEST_DESCRIPTION = "Some Description"
        const val TEST_VOLUME = 3
        const val TEST_PUBLICATION_DATE = 2020
        const val TEST_PAGES = 320
        const val TEST_PRICE = "1500|1"
        const val TEST_VALUE = "2000|1"
        const val TEST_DUE_DATE = 20231231
        const val TEST_READ_DATE = 20231015
        const val TEST_EDITION = 2
        const val TEST_ISBN = "1234567890"
        const val TEST_WEB = "http://test.com"

        const val TEST_FIELD_ID_1 = 101L
        const val TEST_FIELD_TYPE_1 = 1
        const val TEST_FIELD_NAME_1 = "Author One"

        const val TEST_FIELD_ID_2 = 102L
        const val TEST_FIELD_TYPE_2 = 3
        const val TEST_FIELD_NAME_2 = "Fiction"
    }
}
