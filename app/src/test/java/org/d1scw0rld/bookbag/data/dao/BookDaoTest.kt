package org.d1scw0rld.bookbag.data.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.d1scw0rld.bookbag.data.AppDatabase
import org.d1scw0rld.bookbag.data.DbConstants
import org.d1scw0rld.bookbag.data.entity.BookEntity
import org.d1scw0rld.bookbag.data.entity.BookFieldCrossRef
import org.d1scw0rld.bookbag.data.entity.FieldEntity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.d1scw0rld.bookbag.DisplayNameRobolectricRunner
import org.robolectric.annotation.Config
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(DisplayNameRobolectricRunner::class)
@Config(sdk = [28])
class BookDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var bookDao: BookDao

    private fun createBookEntity(id: Long, title: String): BookEntity {
        return BookEntity(
            id = id,
            title = title,
            description = DEFAULT_DESCRIPTION,
            volume = DEFAULT_VOLUME,
            publicationDate = DEFAULT_PUBLICATION_DATE,
            pages = DEFAULT_PAGES,
            price = DEFAULT_PRICE,
            value = DEFAULT_VALUE,
            dueDate = DEFAULT_DUE_DATE,
            readDate = DEFAULT_READ_DATE,
            edition = DEFAULT_EDITION,
            isbn = DEFAULT_ISBN,
            web = DEFAULT_WEB
        )
    }

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Initialize real in-memory SQLite DB
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        bookDao = db.bookDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @DisplayName("Insert Book - Valid Book Entity - Inserts Book and Can Be Loaded With Fields")
    @Test
    fun insertBook_validBookEntity_insertsBookAndCanBeLoadedWithFields() = runTest {
        // Arrange
        val book = createBookEntity(TEST_BOOK_ID_1, TITLE_KOTLIN)

        // Act
        val insertedId = bookDao.insertBook(book)
        val loadedBook = bookDao.getBookWithFields(TEST_BOOK_ID_1)

        // Assert
        assertEquals(TEST_BOOK_ID_1, insertedId)
        assertNotNull(loadedBook)
        assertEquals(TITLE_KOTLIN, loadedBook?.book?.title)
    }

    @DisplayName("Insert Book - Conflict Replace Scenario - Replaces Existing Book")
    @Test
    fun insertBook_conflictReplaceScenario_replacesExistingBook() = runTest {
        // Arrange
        val book = createBookEntity(TEST_BOOK_ID_1, TITLE_ORIGINAL)
        bookDao.insertBook(book)

        // Act: Insert same ID with different title should replace
        val replacedBook = createBookEntity(TEST_BOOK_ID_1, TITLE_REPLACED)
        bookDao.insertBook(replacedBook)

        // Assert
        val loaded = bookDao.getBookWithFields(TEST_BOOK_ID_1)
        assertEquals(TITLE_REPLACED, loaded?.book?.title)
    }

    @DisplayName("Upsert Book - Existing Book ID Provided - Updates Existing Book Title")
    @Test
    fun upsertBook_existingBookIdProvided_updatesExistingBookTitle() = runTest {
        // Arrange: Insert initial book
        val book = createBookEntity(TEST_BOOK_ID_1, TITLE_ORIGINAL_TITLE)
        bookDao.insertBook(book)

        // Act: Upsert update on the same primary key
        val updatedBook = createBookEntity(TEST_BOOK_ID_1, TITLE_UPDATED)
        bookDao.upsertBook(updatedBook)

        // Assert
        val loadedBook = bookDao.getBookWithFields(TEST_BOOK_ID_1)
        assertEquals(TITLE_UPDATED, loadedBook?.book?.title)
    }

    @DisplayName("Update Book - Existing Book ID Provided - Updates Existing Book Title")
    @Test
    fun updateBook_existingBookIdProvided_updatesExistingBookTitle() = runTest {
        // Arrange: Insert initial book
        val book = createBookEntity(TEST_BOOK_ID_1, TITLE_DRAFT)
        bookDao.insertBook(book)

        // Act: Update
        val updatedBook = createBookEntity(TEST_BOOK_ID_1, TITLE_FINAL)
        bookDao.updateBook(updatedBook)

        // Assert
        val loaded = bookDao.getBookWithFields(TEST_BOOK_ID_1)
        assertEquals(TITLE_FINAL, loaded?.book?.title)
    }

    @DisplayName("Delete Book - Valid Book ID Provided - Removes Book From Database")
    @Test
    fun deleteBook_validBookIdProvided_removesBookFromDatabase() = runTest {
        // Arrange
        val book = createBookEntity(TEST_BOOK_ID_1, TITLE_DELETE)
        bookDao.insertBook(book)

        // Act
        bookDao.deleteBook(TEST_BOOK_ID_1)

        // Assert
        val loaded = bookDao.getBookWithFields(TEST_BOOK_ID_1)
        assertNull(loaded)
    }

    @DisplayName("Insert Field - Multiple Fields Provided - Retrieves Only Fields Of Requested Type ID")
    @Test
    fun insertField_multipleFieldsProvided_retrievesOnlyFieldsOfRequestedTypeId() = runTest {
        // Arrange
        val field1 = FieldEntity(id = TEST_FIELD_ID_1, typeId = TEST_FIELD_TYPE_1, name = FIELD_NAME_AUTHOR)
        val field2 = FieldEntity(id = TEST_FIELD_ID_2, typeId = TEST_FIELD_TYPE_1, name = FIELD_NAME_PUBLISHER)
        val field3 = FieldEntity(id = TEST_FIELD_ID_3, typeId = TEST_FIELD_TYPE_2, name = FIELD_NAME_GENRE)

        // Act
        bookDao.insertField(field1)
        bookDao.insertField(field2)
        bookDao.insertField(field3)

        val retrievedFields = bookDao.getFieldsByTypeId(TEST_FIELD_TYPE_1)

        // Assert: should only retrieve fields of typeId 1
        assertEquals(2, retrievedFields.size)
        assertTrue(retrievedFields.any { it.name == FIELD_NAME_AUTHOR })
        assertTrue(retrievedFields.any { it.name == FIELD_NAME_PUBLISHER })
    }

    @DisplayName("Insert Field - Conflict Replace Scenario - Replaces Existing Field")
    @Test
    fun insertField_conflictReplaceScenario_replacesExistingField() = runTest {
        // Arrange
        val field = FieldEntity(id = TEST_FIELD_ID_1, typeId = TEST_FIELD_TYPE_1, name = FIELD_NAME_INITIAL)
        bookDao.insertField(field)

        // Act: Insert with same ID should replace
        val replacedField = FieldEntity(id = TEST_FIELD_ID_1, typeId = TEST_FIELD_TYPE_1, name = FIELD_NAME_REPLACED)
        bookDao.insertField(replacedField)

        // Assert
        val fields = bookDao.getFieldsByTypeId(TEST_FIELD_TYPE_1)
        assertEquals(1, fields.size)
        assertEquals(FIELD_NAME_REPLACED, fields[0].name)
    }

    @DisplayName("Insert Book Field Cross Ref - Valid Relation - Maps Book To Fields Correctly")
    @Test
    fun insertBookFieldCrossRef_validRelation_mapsBookToFieldsCorrectly() = runTest {
        // Arrange: Insert a book and a field definition
        val book = createBookEntity(TEST_BOOK_ID_3, TITLE_REFACTORING)
        val field = FieldEntity(id = TEST_FIELD_ID_4, typeId = DbConstants.FLD_AUTHOR, name = FIELD_NAME_MARTIN_FOWLER)
        
        bookDao.insertBook(book)
        bookDao.insertField(field)

        // Create cross reference association
        val crossRef = BookFieldCrossRef(bookId = TEST_BOOK_ID_3, fieldId = TEST_FIELD_ID_4)

        // Act
        bookDao.insertBookFieldCrossRef(crossRef)
        val result = bookDao.getBookWithFields(TEST_BOOK_ID_3)

        // Assert
        assertNotNull(result)
        assertEquals(TITLE_REFACTORING, result?.book?.title)
        assertEquals(1, result?.fields?.size)
        assertEquals(FIELD_NAME_MARTIN_FOWLER, result?.fields?.get(0)?.name)
        assertEquals(DbConstants.FLD_AUTHOR, result?.fields?.get(0)?.typeId)
    }

    @DisplayName("Insert Book Field Cross Ref - Conflict Ignore Scenario - Ignores Duplicate Relation")
    @Test
    fun insertBookFieldCrossRef_conflictIgnoreScenario_ignoresDuplicateRelation() = runTest {
        // Arrange
        val book = createBookEntity(TEST_BOOK_ID_1, TITLE_GENERIC)
        val field = FieldEntity(id = TEST_FIELD_ID_1, typeId = TEST_FIELD_TYPE_1, name = FIELD_NAME_GENERIC)
        bookDao.insertBook(book)
        bookDao.insertField(field)
        val crossRef = BookFieldCrossRef(TEST_BOOK_ID_1, TEST_FIELD_ID_1)
        bookDao.insertBookFieldCrossRef(crossRef)

        // Act: Insert same crossRef should be ignored
        bookDao.insertBookFieldCrossRef(crossRef)

        // Assert: Still exactly one relation
        val result = bookDao.getBookWithFields(TEST_BOOK_ID_1)
        assertEquals(1, result?.fields?.size)
    }

    @DisplayName("Delete Book Fields - Valid Book ID Provided - Removes Standalone Cross References")
    @Test
    fun deleteBookFields_validBookIdProvided_removesStandaloneCrossReferences() = runTest {
        // Arrange
        val book = createBookEntity(TEST_BOOK_ID_1, TITLE_GENERIC)
        val field = FieldEntity(id = TEST_FIELD_ID_1, typeId = TEST_FIELD_TYPE_1, name = FIELD_NAME_GENERIC)
        bookDao.insertBook(book)
        bookDao.insertField(field)
        bookDao.insertBookFieldCrossRef(BookFieldCrossRef(TEST_BOOK_ID_1, TEST_FIELD_ID_1))

        // Act
        bookDao.deleteBookFields(TEST_BOOK_ID_1)

        // Assert
        val result = bookDao.getBookWithFields(TEST_BOOK_ID_1)
        assertTrue(result?.fields?.isEmpty() ?: false)
    }

    @DisplayName("Delete Book And Fields - Valid Book ID Provided - Atomically Removes Book and Cross References")
    @Test
    fun deleteBookAndFields_validBookIdProvided_atomicallyRemovesBookAndCrossReferences() = runTest {
        // Arrange
        val book = createBookEntity(TEST_BOOK_ID_4, TITLE_1984)
        val field = FieldEntity(id = TEST_FIELD_ID_5, typeId = DbConstants.FLD_AUTHOR, name = FIELD_NAME_GEORGE_ORWELL)
        val crossRef = BookFieldCrossRef(bookId = TEST_BOOK_ID_4, fieldId = TEST_FIELD_ID_5)

        bookDao.insertBook(book)
        bookDao.insertField(field)
        bookDao.insertBookFieldCrossRef(crossRef)

        // Act: Delete book and fields atomically using custom SQL transaction
        bookDao.deleteBookAndFields(TEST_BOOK_ID_4)

        // Assert
        val loadedBook = bookDao.getBookWithFields(TEST_BOOK_ID_4)
        assertNull(loadedBook)
    }

    @DisplayName("Get Book With Fields Flow - Valid Book ID - Emits Updates in Real Time")
    @Test
    fun getBookWithFieldsFlow_validBookId_emitsUpdatesInRealTime() = runTest {
        // Arrange
        val bookId = TEST_BOOK_ID_5
        val book = createBookEntity(bookId, TITLE_INITIAL)
        bookDao.insertBook(book)

        // Act & Assert: Get Flow and collect first emission
        val firstEmission = bookDao.getBookWithFieldsFlow(bookId).first()
        assertNotNull(firstEmission)
        assertEquals(TITLE_INITIAL, firstEmission?.book?.title)
    }

    @DisplayName("Get All Books With Fields - Multiple Books Exist - Retrieves Multiple Relations")
    @Test
    fun getAllBooksWithFields_multipleBooksExist_retrievesMultipleRelations() = runTest {
        // Arrange
        val book1 = createBookEntity(TEST_BOOK_ID_1, TITLE_BOOK_ONE)
        val book2 = createBookEntity(TEST_BOOK_ID_2, TITLE_BOOK_TWO)
        bookDao.insertBook(book1)
        bookDao.insertBook(book2)

        // Act
        val allBooks = bookDao.getAllBooksWithFields()

        // Assert
        assertEquals(2, allBooks.size)
        assertTrue(allBooks.any { it.book.title == TITLE_BOOK_ONE })
        assertTrue(allBooks.any { it.book.title == TITLE_BOOK_TWO })
    }

    @DisplayName("Get All Books With Fields Flow - Multiple Books Exist - Emits All Updates")
    @Test
    fun getAllBooksWithFieldsFlow_multipleBooksExist_emitsAllUpdates() = runTest {
        // Arrange
        val book1 = createBookEntity(TEST_BOOK_ID_1, TITLE_BOOK_ONE)
        bookDao.insertBook(book1)

        // Act & Assert
        val emission = bookDao.getAllBooksWithFieldsFlow().first()
        assertEquals(1, emission.size)
        assertEquals(TITLE_BOOK_ONE, emission[0].book.title)
    }

    companion object {
        const val DEFAULT_DESCRIPTION = "Test Description"
        const val DEFAULT_VOLUME = 1
        const val DEFAULT_PUBLICATION_DATE = 2023
        const val DEFAULT_PAGES = 350
        const val DEFAULT_PRICE = "1500|1"
        const val DEFAULT_VALUE = "2000|1"
        const val DEFAULT_DUE_DATE = 0
        const val DEFAULT_READ_DATE = 0
        const val DEFAULT_EDITION = 1
        const val DEFAULT_ISBN = "1234567890"
        const val DEFAULT_WEB = "http://test.com"

        const val TEST_BOOK_ID_1 = 1L
        const val TEST_BOOK_ID_2 = 2L
        const val TEST_BOOK_ID_3 = 5L
        const val TEST_BOOK_ID_4 = 10L
        const val TEST_BOOK_ID_5 = 15L

        const val TITLE_KOTLIN = "Kotlin in Action"
        const val TITLE_ORIGINAL = "Original"
        const val TITLE_REPLACED = "Replaced"
        const val TITLE_ORIGINAL_TITLE = "Original Title"
        const val TITLE_UPDATED = "Updated Title"
        const val TITLE_DRAFT = "Draft Title"
        const val TITLE_FINAL = "Final Title"
        const val TITLE_DELETE = "Book to Delete"
        const val TITLE_GENERIC = "Book"
        const val TITLE_REFACTORING = "Refactoring"
        const val TITLE_1984 = "1984"
        const val TITLE_INITIAL = "Initial Book"
        const val TITLE_BOOK_ONE = "Book One"
        const val TITLE_BOOK_TWO = "Book Two"

        const val TEST_FIELD_ID_1 = 101L
        const val TEST_FIELD_ID_2 = 102L
        const val TEST_FIELD_ID_3 = 103L
        const val TEST_FIELD_ID_4 = 201L
        const val TEST_FIELD_ID_5 = 301L

        const val TEST_FIELD_TYPE_1 = 1
        const val TEST_FIELD_TYPE_2 = 2

        const val FIELD_NAME_AUTHOR = "Author"
        const val FIELD_NAME_PUBLISHER = "Publisher"
        const val FIELD_NAME_GENRE = "Genre"
        const val FIELD_NAME_INITIAL = "Initial"
        const val FIELD_NAME_REPLACED = "Replaced"
        const val FIELD_NAME_MARTIN_FOWLER = "Martin Fowler"
        const val FIELD_NAME_GENERIC = "Field"
        const val FIELD_NAME_GEORGE_ORWELL = "George Orwell"
    }
}
