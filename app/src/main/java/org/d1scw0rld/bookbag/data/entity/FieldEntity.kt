package org.d1scw0rld.bookbag.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "fields",
    indices = [Index(value = ["type_id"])]
)
data class FieldEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id") val id: Long = 0,
    @ColumnInfo(name = "type_id") val typeId: Int,
    @ColumnInfo(name = "name") val name: String
)
