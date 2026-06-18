package org.d1scw0rld.bookbag.data.repository

import kotlinx.coroutines.flow.Flow
import org.d1scw0rld.bookbag.data.entity.FieldEntity
import org.d1scw0rld.bookbag.data.relation.BookWithFields
import org.d1scw0rld.bookbag.dto.Book

interface BookRepository {
    fun getBookWithFieldsFlow(bookId: Long): Flow<BookWithFields?>
    fun getAllBooksWithFieldsFlow(): Flow<List<BookWithFields>>
    suspend fun getBookWithFields(bookId: Long): BookWithFields?
    suspend fun getAllBooksWithFields(): List<BookWithFields>
    suspend fun saveBookWithFields(book: Book)
    suspend fun deleteBookAndRelations(bookId: Long)
    suspend fun getFieldsByType(typeId: Int): List<FieldEntity>
    suspend fun importDatabase(filePath: String): Boolean
    suspend fun exportDatabase(filePath: String): Boolean
}
