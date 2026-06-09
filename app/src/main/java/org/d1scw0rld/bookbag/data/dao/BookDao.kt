package org.d1scw0rld.bookbag.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.d1scw0rld.bookbag.data.entity.BookEntity
import org.d1scw0rld.bookbag.data.entity.BookFieldCrossRef
import org.d1scw0rld.bookbag.data.entity.FieldEntity
import org.d1scw0rld.bookbag.data.relation.BookWithFields

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    @Upsert
    suspend fun upsertBook(book: BookEntity): Long

    @Update
    suspend fun updateBook(book: BookEntity)

    @Query("DELETE FROM books WHERE _id = :bookId")
    suspend fun deleteBook(bookId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertField(field: FieldEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookFieldCrossRef(crossRef: BookFieldCrossRef)

    @Query("DELETE FROM book_fields WHERE book_id = :bookId")
    suspend fun deleteBookFields(bookId: Long)

    @Transaction
    suspend fun deleteBookAndFields(bookId: Long) {
        deleteBookFields(bookId)
        deleteBook(bookId)
    }

    @Query("SELECT * FROM fields WHERE type_id = :typeId")
    suspend fun getFieldsByTypeId(typeId: Int): List<FieldEntity>

    @Transaction
    @Query("SELECT * FROM books WHERE _id = :bookId")
    suspend fun getBookWithFields(bookId: Long): BookWithFields?

    @Transaction
    @Query("SELECT * FROM books WHERE _id = :bookId")
    fun getBookWithFieldsFlow(bookId: Long): Flow<BookWithFields?>

    @Transaction
    @Query("SELECT * FROM books")
    suspend fun getAllBooksWithFields(): List<BookWithFields>

    @Transaction
    @Query("SELECT * FROM books")
    fun getAllBooksWithFieldsFlow(): Flow<List<BookWithFields>>
}
