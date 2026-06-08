# 🗺️ SQLite to Room Database Migration Plan

This document outlines the detailed step-by-step roadmap to migrate the local database layer of the **Book Bag** application from manual SQLite helper classes to Android Jetpack **Room**.

Since the application currently supports importing and exporting database backups (`book_bag.db`), Room is the perfect choice. By configuring Room to map onto your exact existing schema, we ensure **100% database compatibility, zero data loss, and seamless backup import/export support**.

---

## 📅 Roadmap Overview

1. [Phase 1: Gradle Setup](#-phase-1-gradle-setup)
2. [Phase 2: Define Room Entities](#-phase-2-define-room-entities)
3. [Phase 3: Define DAOs & Relations](#-phase-3-define-daos--relations)
4. [Phase 4: AppDatabase & Seed Configuration](#-phase-4-appdatabase--seed-configuration)
5. [Phase 5: Backup / Restore & Integration](#-phase-5-backup--restore--integration)

---

## 📦 Phase 1: Gradle Setup

To enable Room compilation, we add the Room library dependencies and Kapt/KSP symbol processing to the application's build script.

### 1. In `app/build.gradle`:
Ensure your plugins block has Kotlin Kapt or KSP loaded:
```groovy
plugins {
    id 'com.android.application'
    id 'kotlin-android'
    id 'kotlin-kapt' // Or 'com.google.devtools.ksp'
}
```

### 2. Add Room Dependencies:
```groovy
dependencies {
    def room_version = "2.6.1"

    implementation "androidx.room:room-runtime:$room_version"
    implementation "androidx.room:room-ktx:$room_version"
    kapt "androidx.room:room-compiler:$room_version" // Or ksp
}
```

---

## 🗂️ Phase 2: Define Room Entities

We map the existing three tables (`books`, `fields`, and `book_fields`) exactly onto Kotlin data classes annotated with Room decorators.

### 1. `BookEntity` (Maps to `books` table)
```kotlin
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
```

### 2. `FieldEntity` (Maps to `fields` table)
```kotlin
package org.d1scw0rld.bookbag.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fields")
data class FieldEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id") val id: Long = 0,
    @ColumnInfo(name = "type_id") val typeId: Int,
    @ColumnInfo(name = "name") val name: String
)
```

### 3. `BookFieldCrossRef` (Maps to `book_fields` join table)
```kotlin
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
```

---

## 🔍 Phase 3: Define DAOs & Relations

We use DAOs to handle type-safe, compile-time verified SQLite transactions, completely eliminating manual cursor mappings and query-string syntax mistakes.

### 1. Define Relations (Retrieves books with all their metadata fields)
Using Room's `@Relation` and `@Embedded` annotations, we represent many-to-many relationships beautifully:
```kotlin
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
```

### 2. `BookDao` (Handles core list, search, filter, and modify transactions)
```kotlin
package org.d1scw0rld.bookbag.data.dao

import androidx.room.*
import org.d1scw0rld.bookbag.data.entity.BookEntity
import org.d1scw0rld.bookbag.data.entity.BookFieldCrossRef
import org.d1scw0rld.bookbag.data.relation.BookWithFields

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    @Update
    suspend fun updateBook(book: BookEntity)

    @Query("DELETE FROM books WHERE _id = :bookId")
    suspend fun deleteBook(bookId: Long)

    @Transaction
    @Query("SELECT * FROM books WHERE _id = :bookId")
    suspend fun getBookWithFields(bookId: Long): BookWithFields?

    @Query("SELECT * FROM books ORDER BY title ASC")
    suspend fun getAllBooksSortedByTitle(): List<BookEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookFieldCrossRef(crossRef: BookFieldCrossRef)

    @Query("DELETE FROM book_fields WHERE book_id = :bookId")
    suspend fun deleteBookFields(bookId: Long)
}
```

---

## 🗛 Phase 4: AppDatabase & Seed Configuration

When the application is launched for the very first time, Room needs to seed the default static field rows into the `fields` table from `R.array.fields_values`. We implement this asynchronously inside a custom `RoomDatabase.Callback`.

```kotlin
package org.d1scw0rld.bookbag.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.d1scw0rld.bookbag.data.dao.BookDao
import org.d1scw0rld.bookbag.data.entity.BookEntity
import org.d1scw0rld.bookbag.data.entity.BookFieldCrossRef
import org.d1scw0rld.bookbag.data.entity.FieldEntity

@Database(
    entities = [BookEntity::class, FieldEntity::class, BookFieldCrossRef::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "book_bag.db" // ⚠️ Matches exact legacy file name
                )
                .addCallback(AppDatabaseCallback(context, scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val context: Context,
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            scope.launch(Dispatchers.IO) {
                // Parse R.array.fields_values and seed FieldEntity rows here...
            }
        }
    }
}
```

---

## 📂 Phase 5: Backup / Restore & Integration

Since Room is an abstraction over standard SQLite files, your existing import/export byte-copy logic (`FileUtils.copyFile`) will **continue to work flawlessly!**

Before copying standard backups, we simply instruct Room to close and flush all write-ahead logging (WAL) caches to disk, and then re-open after completion:

```kotlin
// Safely flushing and closing Room for file operations:
AppDatabase.getDatabase(context, scope).close()
```

---

### 🏆 Key Business Benefits
1. **0% Data Loss**: Seamless migration of all existing active user databases because our entities map directly on top of database version `1` schema structures.
2. **Compile-Time Safety**: Any broken SQL syntax or incorrect column types will trigger compilation failures, completely avoiding runtime database crashes.
3. **No More Cursors**: Eliminates all boilerplate `Cursor`, `ContentValues`, and `SQLiteDatabase` manual resource management.
