# 🗺️ SQLite to Room Migration: Option B (The Modern Kotlin Way with Relations + Map)

This plan outlines the database migration using **standard simple queries** combined with **Kotlin's collections library** to handle relationships, groupings, and layout formatting in memory.

Instead of writing and maintaining complex, nested SQL subqueries with database-level string concatenation (like `QR_WNT_AUT`), we fetch standard objects and use safe, highly expressive Kotlin collection functions (like `.filter()`, `.map()`, `.groupBy()`, and `.joinToString()`) in our ViewModel/Presenter layer.

---

## 📅 Roadmap Overview

1. [Phase 1: Gradle Setup](#-phase-1-gradle-setup)
2. [Phase 2: Define Room Entities](#-phase-2-define-room-entities)
3. [Phase 3: Define BookWithFields Relationships](#-phase-3-define-bookwithfields-relationships)
4. [Phase 4: Define BookDao with Simple SQL Queries](#-phase-4-define-bookdao-with-simple-sql-queries)
5. [Phase 5: AppDatabase & Seed Configuration](#-phase-5-appdatabase--seed-configuration)
6. [Phase 6: Format & Group Data in Memory using Kotlin](#-phase-6-format--group-data-in-memory-using-kotlin)
7. [Phase 7: Backup / Restore & Integration](#-phase-7-backup--restore--integration)

---

## 📦 Phase 1: Gradle Setup

To enable Room, we add the Room library and Kapt/KSP symbol processing inside `app/build.gradle`.

### 1. In `app/build.gradle`:
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

## 💎 Phase 3: Define BookWithFields Relationships

Using Room's `@Relation` and `@Embedded` annotations, we represent many-to-many relationships, completely avoiding manual SQL JOIN statements in the DB layer.

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

---

## 🔍 Phase 4: Define BookDao with Simple SQL Queries

Instead of long, complex queries, your `BookDao` contains simple, highly readable queries.

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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookFieldCrossRef(crossRef: BookFieldCrossRef)

    @Query("DELETE FROM book_fields WHERE book_id = :bookId")
    suspend fun deleteBookFields(bookId: Long)

    // ------------------------------------------------------------
    // Highly readable relationship queries:
    // ------------------------------------------------------------

    @Transaction
    @Query("SELECT * FROM books WHERE _id = :bookId")
    suspend fun getBookWithFields(bookId: Long): BookWithFields?

    @Transaction
    @Query("SELECT * FROM books")
    suspend fun getAllBooksWithFields(): List<BookWithFields>
}
```

---

## 🗛 Phase 5: AppDatabase & Seed Configuration

We configure Room to database build name `book_bag.db` and implement an asynchronousCallback to seed standard fields during first creation.

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
                    "book_bag.db"
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
                // Populate default FieldEntities here...
            }
        }
    }
}
```

---

## 🎨 Phase 6: Format & Group Data in Memory using Kotlin

This is where the magic happens. We fetch your relational entity list (`List<BookWithFields>`) from the database, and use **Kotlin's powerful functional collection operators** to group, format, and filter the book records dynamically.

We define a custom projection class to hold adapter results:
```kotlin
data class BookQueryResult(
    val parent: String,
    val childId: Long,
    val child: String
)
```

And format them in your Presenter/ViewModel:

### Example 1: Migrating `QR_WNT_AUT` (Wanted books grouped by Author)
```kotlin
// In DBAdapter.kt constants FLD_STATUS = 7, FLD_AUTHOR = 1
val wantedBooks: List<BookQueryResult> = allBooksWithFields
    .filter { relation ->
        val status = relation.fields.firstOrNull { it.typeId == 7 }?.name ?: ""
        status.lowercase() != "in bag" && status.lowercase() != "read"
    }
    .map { relation ->
        val authors = relation.fields.filter { it.typeId == 1 }
        val authorText = if (authors.isNotEmpty()) {
            authors.joinToString(", ") { it.name } + " - " + relation.book.title
        } else {
            relation.book.title
        }
        val statusName = relation.fields.firstOrNull { it.typeId == 7 }?.name ?: "(missing)"
        
        BookQueryResult(
            parent = statusName,
            childId = relation.book.id,
            child = authorText
        )
    }
    .sortedWith(compareBy({ it.parent }, { it.child }))
```

### Example 2: Migrating `QR_TTL` (All books sorted by Title)
```kotlin
val booksSortedByTitle: List<BookQueryResult> = allBooksWithFields
    .map { relation ->
        val authors = relation.fields.filter { it.typeId == 1 }
        val childText = if (authors.isNotEmpty()) {
            relation.book.title + " - " + authors.joinToString(", ") { it.name }
        } else {
            relation.book.title
        }
        val parentLetter = relation.book.title.take(1).uppercase()
        
        BookQueryResult(
            parent = parentLetter,
            childId = relation.book.id,
            child = childText
        )
    }
    .sortedWith(compareBy({ it.parent }, { it.child }))
```

---

## 📂 Phase 7: Backup / Restore & Integration

Because we use standard, direct SQL queries, Room operates directly on the `book_bag.db` SQLite file. Your backup/restore copy methods (`FileUtils.copyFile`) will **continue to work seamlessly without modifications**.

Before importing/exporting files, simply close Room to flush WAL write-ahead-logging logs:

```kotlin
AppDatabase.getDatabase(context, scope).close()
```

---

### 🏆 Key Benefits of Option B:
* **No Database-Level Concatenation**: Avoids complex database subqueries and SQL string concatenations, which are notoriously slow and hard to debug.
* **Extreme Readability**: Written in standard, clean, highly readable Kotlin collection structures instead of massive raw SQL blocks.
* **Easy to Unit Test**: Since the logic of string formatting and grouping resides in Kotlin (rather than database raw queries), it is incredibly easy to write fast, local JUnit tests for your filtering and formatting algorithms.
* **Locale Independent Case-Folding**: Handles lowercasing and sorting with Kotlin's standard locale methods natively.
