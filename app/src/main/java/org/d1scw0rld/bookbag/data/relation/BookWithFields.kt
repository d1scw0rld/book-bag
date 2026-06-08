package org.d1scw0rld.bookbag.data.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import org.d1scw0rld.bookbag.data.entity.BookEntity
import org.d1scw0rld.bookbag.data.entity.BookFieldCrossRef
import org.d1scw0rld.bookbag.data.entity.FieldEntity

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
