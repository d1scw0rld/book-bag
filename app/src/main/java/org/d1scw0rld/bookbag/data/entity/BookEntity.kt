package org.d1scw0rld.bookbag.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id") val id: Long = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "volume") val volume: Int?,
    @ColumnInfo(name = "publication_date") val publicationDate: Int?,
    @ColumnInfo(name = "pages") val pages: Int?,
    @ColumnInfo(name = "price") val price: String?,
    @ColumnInfo(name = "value") val value: String?,
    @ColumnInfo(name = "due_date") val dueDate: Int?,
    @ColumnInfo(name = "read_date") val readDate: Int?,
    @ColumnInfo(name = "edition") val edition: Int?,
    @ColumnInfo(name = "isbn") val isbn: String?,
    @ColumnInfo(name = "web") val web: String?
)
