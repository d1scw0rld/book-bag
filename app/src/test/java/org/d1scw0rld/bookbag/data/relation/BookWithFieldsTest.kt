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
            id = 5L,
            title = "Test Book",
            description = "Some Description",
            volume = 3,
            publicationDate = 2020,
            pages = 320,
            price = "1500|1",
            value = "2000|1",
            dueDate = 20231231,
            readDate = 20231015,
            edition = 2,
            isbn = "1234567890",
            web = "http://test.com"
        )
        
        val fields = listOf(
            FieldEntity(id = 101L, typeId = 1, name = "Author One"),
            FieldEntity(id = 102L, typeId = 3, name = "Fiction")
        )
        
        val relation = BookWithFields(entity, fields)

        // Act
        val dto = relation.toDto()

        // Assert core book attributes
        assertEquals(5L, dto.id)
        assertEquals("Test Book", dto.title.value)
        assertEquals("Some Description", dto.description.value)
        assertEquals(3, dto.volume.value)
        assertEquals(2020, dto.publicationDate.value)
        assertEquals(320, dto.pages.value)
        assertEquals("1500|1", dto.price.value)
        assertEquals("2000|1", dto.value.value)
        assertEquals(20231231, dto.dueDate.value)
        assertEquals(20231015, dto.readDate.value)
        assertEquals(2, dto.edition.value)
        assertEquals("1234567890", dto.isbn.value)
        assertEquals("http://test.com", dto.web.value)

        // Assert properties / custom fields list size and values
        assertEquals(2, dto.properties.size)
        assertEquals(1, dto.properties[0].fieldTypeId)
        assertEquals("Author One", dto.properties[0].value)
        assertEquals(101L, dto.properties[0].id)

        assertEquals(3, dto.properties[1].fieldTypeId)
        assertEquals("Fiction", dto.properties[1].value)
        assertEquals(102L, dto.properties[1].id)
    }
}
