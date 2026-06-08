package org.d1scw0rld.bookbag.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "book_fields",
    primaryKeys = ["book_id", "field_id"]
)
data class BookFieldCrossRef(
    @ColumnInfo(name = "book_id") val bookId: Long,
    @ColumnInfo(name = "field_id") val fieldId: Long
)
