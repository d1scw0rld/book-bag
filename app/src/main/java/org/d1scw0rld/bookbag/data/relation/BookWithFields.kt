package org.d1scw0rld.bookbag.data.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import org.d1scw0rld.bookbag.data.entity.BookEntity
import org.d1scw0rld.bookbag.data.entity.BookFieldCrossRef
import org.d1scw0rld.bookbag.data.entity.FieldEntity
import org.d1scw0rld.bookbag.dto.Book
import org.d1scw0rld.bookbag.dto.Changeable
import org.d1scw0rld.bookbag.dto.Property

data class BookWithFields(
    @Embedded val book: BookEntity,
    @Relation(
        parentColumn = "_id",
        entityColumn = "_id",
        associateBy = Junction(
            BookFieldCrossRef::class,
            parentColumn = "book_id",
            entityColumn = "field_id"
        )
    )
    val fields: List<FieldEntity>
)

fun BookWithFields.toDto(): Book {
    val book = Book(
        id = this@toDto.book.id,
        title = Changeable(this@toDto.book.title),
        description = Changeable(this@toDto.book.description.orEmpty()),
        price = Changeable(this@toDto.book.price.orEmpty()),
        value = Changeable(this@toDto.book.value.orEmpty()),
        isbn = Changeable(this@toDto.book.isbn.orEmpty()),
        web = Changeable(this@toDto.book.web.orEmpty()),
        volume = Changeable(this@toDto.book.volume ?: 0),
        pages = Changeable(this@toDto.book.pages ?: 0),
        publicationDate = Changeable(this@toDto.book.publicationDate ?: 0),
        edition = Changeable(this@toDto.book.edition ?: 0),
        readDate = Changeable(this@toDto.book.readDate ?: 0),
        dueDate = Changeable(this@toDto.book.dueDate ?: 0)
    )
    book.properties.addAll(fields.map {
        Property(fieldTypeId = it.typeId, value = it.name, id = it.id) 
    })
    return book
}
