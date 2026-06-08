# 🗺️ SQLite to Room Migration: Option A (Direct SQL Migration)

This plan outlines the database migration using **Direct SQL queries** inside Room. In this approach, we retain your existing complex SQL query strings (such as `QR_WNT_AUT`) and run them directly inside Room DAOs. 

Room compiles, parses, and verifies these raw SQL strings at **compile-time**, ensuring complete type and null safety without changing your database logic.

---

## 📅 Roadmap Overview

1. [Phase 1: Gradle Setup](#-phase-1-gradle-setup)
2. [Phase 2: Define Room Entities](#-phase-2-define-room-entities)
3. [Phase 3: Define Custom DTO Projections](#-phase-3-define-custom-dto-projections)
4. [Phase 4: Define BookDao with Complex SQL Queries](#-phase-4-define-bookdao-with-complex-sql-queries)
5. [Phase 5: AppDatabase & Seed Configuration](#-phase-5-appdatabase--seed-configuration)
6. [Phase 6: Backup / Restore & Integration](#-phase-6-backup--restore--integration)

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

## 💎 Phase 3: Define Custom DTO Projections

Since complex SQL queries (like `QR_WNT_AUT`, `QR_TTL`, etc.) perform joins and aggregations returning custom columns (`parent`, `child_id`, and `child`), we define a custom data class (or projection) to hold the results:

```kotlin
package org.d1scw0rld.bookbag.data.dto

import androidx.room.ColumnInfo

data class BookQueryResult(
    @ColumnInfo(name = "parent") val parent: String,
    @ColumnInfo(name = "child_id") val childId: Long,
    @ColumnInfo(name = "child") val child: String
)
```

---

## 🔍 Phase 4: Define BookDao with Complex SQL Queries

We paste your exact, complex SQL queries directly into the `@Query` annotation on our `BookDao` methods, returning a list of `BookQueryResult` projection.

```kotlin
package org.d1scw0rld.bookbag.data.dao

import androidx.room.*
import org.d1scw0rld.bookbag.data.dto.BookQueryResult
import org.d1scw0rld.bookbag.data.entity.BookEntity
import org.d1scw0rld.bookbag.data.entity.BookFieldCrossRef

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
    // Complex SQL Queries Migrated Directly:
    // ------------------------------------------------------------

    // 1. QR_TTL (Sort by Title)
    @Query("""
        SELECT UPPER(SUBSTR(b.title, 1, 1)) AS parent, 
               b._id AS child_id, 
               COALESCE(b.title || " - " || GROUP_CONCAT(a.f_name, ", "), b.title) AS child
        FROM books AS b
        LEFT JOIN (
            SELECT bf.field_id AS bf_field_id, bf.book_id AS bf_book_id, f.name AS f_name
            FROM book_fields AS bf 
            JOIN fields AS f ON f._id = bf.field_id
            WHERE f.type_id = 1 -- FLD_AUTHOR
        ) AS a ON a.bf_book_id = b._id
        GROUP BY b._id
        ORDER BY parent, child
    """)
    suspend fun getBooksSortedByTitle(): List<BookQueryResult>

    // 2. QR_WNT_AUT (Sort by Wanted Authors)
    @Query("""
        SELECT IFNULL(p.f_name, '"(missing)"') AS parent, 
               b._id AS child_id, 
               COALESCE(GROUP_CONCAT(a.f_name, ", ") || " - " || b.title, b.title) AS child
        FROM books AS b 
        LEFT JOIN (
            SELECT bf.field_id AS bf_field_id, bf.book_id AS bf_book_id, f.name AS f_name 
            FROM book_fields AS bf 
            JOIN fields AS f ON f._id = bf.field_id
            WHERE f.type_id = 1 -- FLD_AUTHOR
        ) AS a ON a.bf_book_id = b._id
        JOIN (
            SELECT bf.field_id AS bf_field_id, bf.book_id AS bf_book_id, f.name AS f_name 
            FROM book_fields AS bf 
            JOIN fields AS f ON f._id = bf.field_id
            WHERE f.type_id = 7 -- FLD_STATUS
        ) AS p ON p.bf_book_id = b._id
        WHERE LOWER(p.f_name) != LOWER('In Bag') AND LOWER(p.f_name) != LOWER('Read')
        GROUP BY b._id
        ORDER BY parent, child
    """)
    suspend fun getWantedBooksGroupedByAuthor(): List<BookQueryResult>
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

## 📂 Phase 6: Backup / Restore & Integration

Because we use standard, direct SQL queries, Room operates directly on the `book_bag.db` SQLite file. Your backup/restore copy methods (`FileUtils.copyFile`) will **continue to work seamlessly without modifications**.

Before importing/exporting files, simply close Room to flush WAL write-ahead-logging logs:

```kotlin
AppDatabase.getDatabase(context, scope).close()
```

---

### 🏆 Key Benefits of Option A:
* **Preserves Query Optimization**: Retains your specialized, highly optimized SQLite queries exactly as they are.
* **Compact ViewModel Logic**: No sorting or group-concatenating logic needs to be written in Kotlin, keeping your presenter/ViewModel thin.
* **Compile-Time Checked SQL**: Room verifies that all table names, JOINs, and column names inside your long SQL strings are 100% correct, raising compile-time errors for any typos.
