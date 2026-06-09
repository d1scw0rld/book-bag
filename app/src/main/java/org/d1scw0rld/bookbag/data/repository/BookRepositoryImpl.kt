package org.d1scw0rld.bookbag.data.repository

import kotlinx.coroutines.flow.Flow
import org.d1scw0rld.bookbag.data.dao.BookDao
import org.d1scw0rld.bookbag.data.entity.BookEntity
import org.d1scw0rld.bookbag.data.entity.BookFieldCrossRef
import org.d1scw0rld.bookbag.data.entity.FieldEntity
import org.d1scw0rld.bookbag.data.relation.BookWithFields
import org.d1scw0rld.bookbag.dto.Book
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepositoryImpl @Inject constructor(
    private val bookDao: BookDao,
) : BookRepository {

    override fun getBookWithFieldsFlow(bookId: Long): Flow<BookWithFields?> {
        return bookDao.getBookWithFieldsFlow(bookId)
    }

    override fun getAllBooksWithFieldsFlow(): Flow<List<BookWithFields>> {
        return bookDao.getAllBooksWithFieldsFlow()
    }

    override suspend fun getBookWithFields(bookId: Long): BookWithFields? {
        return bookDao.getBookWithFields(bookId)
    }

    override suspend fun getAllBooksWithFields(): List<BookWithFields> {
        return bookDao.getAllBooksWithFields()
    }

    override suspend fun saveBookWithFields(book: Book) {
        val bookEntity = BookEntity(
            id = book.id,
            title = book.title.value,
            description = book.description.value,
            volume = book.volume.value,
            publicationDate = book.publicationDate.value,
            pages = book.pages.value,
            price = book.price.value,
            value = book.value.value,
            dueDate = book.dueDate.value,
            readDate = book.readDate.value,
            edition = book.edition.value,
            isbn = book.isbn.value,
            web = book.web.value
        )

        val idToUse = if (book.id != 0L) {
            bookDao.updateBook(bookEntity)
            book.id
        } else {
            bookDao.insertBook(bookEntity)
        }

        bookDao.deleteBookFields(idToUse)
        for (property in book.properties) {
            if (property.id == 0L) {
                val fieldEntity = FieldEntity(typeId = property.fieldTypeId, name = property.value)
                property.id = bookDao.insertField(fieldEntity)
            }
            bookDao.insertBookFieldCrossRef(BookFieldCrossRef(bookId = idToUse, fieldId = property.id))
        }
    }

    override suspend fun deleteBookAndRelations(bookId: Long) {
        bookDao.deleteBookAndFields(bookId)
    }

    override suspend fun getFieldsByType(typeId: Int): List<FieldEntity> {
        return bookDao.getFieldsByTypeId(typeId)
    }
}
