package org.d1scw0rld.bookbag.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "book_fields",
    primaryKeys = ["book_id", "field_id"],
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["_id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = FieldEntity::class,
            parentColumns = ["_id"],
            childColumns = ["field_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ]
)
data class BookFieldCrossRef(
    @ColumnInfo(name = "book_id") val bookId: Long,
    @ColumnInfo(name = "field_id") val fieldId: Long,
)
