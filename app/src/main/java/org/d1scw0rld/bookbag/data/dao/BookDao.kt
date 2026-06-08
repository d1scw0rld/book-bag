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
