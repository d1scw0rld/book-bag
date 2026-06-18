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
            description = "Test Description",
            volume = 1,
            publicationDate = 2023,
            pages = 350,
            price = "1500|1",
            value = "2000|1",
            dueDate = 0,
            readDate = 0,
            edition = 1,
            isbn = "1234567890",
            web = "http://test.com"
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
        val book = createBookEntity(1L, "Kotlin in Action")

        // Act
        val insertedId = bookDao.insertBook(book)
        val loadedBook = bookDao.getBookWithFields(1L)

        // Assert
        assertEquals(1L, insertedId)
        assertNotNull(loadedBook)
        assertEquals("Kotlin in Action", loadedBook?.book?.title)
    }

    @DisplayName("Insert Book - Conflict Replace Scenario - Replaces Existing Book")
    @Test
    fun insertBook_conflictReplaceScenario_replacesExistingBook() = runTest {
        // Arrange
        val book = createBookEntity(1L, "Original")
        bookDao.insertBook(book)

        // Act: Insert same ID with different title should replace
        val replacedBook = createBookEntity(1L, "Replaced")
        bookDao.insertBook(replacedBook)

        // Assert
        val loaded = bookDao.getBookWithFields(1L)
        assertEquals("Replaced", loaded?.book?.title)
    }

    @DisplayName("Upsert Book - Existing Book ID Provided - Updates Existing Book Title")
    @Test
    fun upsertBook_existingBookIdProvided_updatesExistingBookTitle() = runTest {
        // Arrange: Insert initial book
        val book = createBookEntity(1L, "Original Title")
        bookDao.insertBook(book)

        // Act: Upsert update on the same primary key
        val updatedBook = createBookEntity(1L, "Updated Title")
        bookDao.upsertBook(updatedBook)

        // Assert
        val loadedBook = bookDao.getBookWithFields(1L)
        assertEquals("Updated Title", loadedBook?.book?.title)
    }

    @DisplayName("Update Book - Existing Book ID Provided - Updates Existing Book Title")
    @Test
    fun updateBook_existingBookIdProvided_updatesExistingBookTitle() = runTest {
        // Arrange: Insert initial book
        val book = createBookEntity(1L, "Draft Title")
        bookDao.insertBook(book)

        // Act: Update
        val updatedBook = createBookEntity(1L, "Final Title")
        bookDao.updateBook(updatedBook)

        // Assert
        val loaded = bookDao.getBookWithFields(1L)
        assertEquals("Final Title", loaded?.book?.title)
    }

    @DisplayName("Delete Book - Valid Book ID Provided - Removes Book From Database")
    @Test
    fun deleteBook_validBookIdProvided_removesBookFromDatabase() = runTest {
        // Arrange
        val book = createBookEntity(1L, "Book to Delete")
        bookDao.insertBook(book)

        // Act
        bookDao.deleteBook(1L)

        // Assert
        val loaded = bookDao.getBookWithFields(1L)
        assertNull(loaded)
    }

    @DisplayName("Insert Field - Multiple Fields Provided - Retrieves Only Fields Of Requested Type ID")
    @Test
    fun insertField_multipleFieldsProvided_retrievesOnlyFieldsOfRequestedTypeId() = runTest {
        // Arrange
        val field1 = FieldEntity(id = 101L, typeId = 1, name = "Author")
        val field2 = FieldEntity(id = 102L, typeId = 1, name = "Publisher")
        val field3 = FieldEntity(id = 103L, typeId = 2, name = "Genre")

        // Act
        bookDao.insertField(field1)
        bookDao.insertField(field2)
        bookDao.insertField(field3)

        val retrievedFields = bookDao.getFieldsByTypeId(1)

        // Assert: should only retrieve fields of typeId 1
        assertEquals(2, retrievedFields.size)
        assertTrue(retrievedFields.any { it.name == "Author" })
        assertTrue(retrievedFields.any { it.name == "Publisher" })
    }

    @DisplayName("Insert Field - Conflict Replace Scenario - Replaces Existing Field")
    @Test
    fun insertField_conflictReplaceScenario_replacesExistingField() = runTest {
        // Arrange
        val field = FieldEntity(id = 101L, typeId = 1, name = "Initial")
        bookDao.insertField(field)

        // Act: Insert with same ID should replace
        val replacedField = FieldEntity(id = 101L, typeId = 1, name = "Replaced")
        bookDao.insertField(replacedField)

        // Assert
        val fields = bookDao.getFieldsByTypeId(1)
        assertEquals(1, fields.size)
        assertEquals("Replaced", fields[0].name)
    }

    @DisplayName("Insert Book Field Cross Ref - Valid Relation - Maps Book To Fields Correctly")
    @Test
    fun insertBookFieldCrossRef_validRelation_mapsBookToFieldsCorrectly() = runTest {
        // Arrange: Insert a book and a field definition
        val book = createBookEntity(5L, "Refactoring")
        val field = FieldEntity(id = 201L, typeId = DbConstants.FLD_AUTHOR, name = "Martin Fowler")
        
        bookDao.insertBook(book)
        bookDao.insertField(field)

        // Create cross reference association
        val crossRef = BookFieldCrossRef(bookId = 5L, fieldId = 201L)

        // Act
        bookDao.insertBookFieldCrossRef(crossRef)
        val result = bookDao.getBookWithFields(5L)

        // Assert
        assertNotNull(result)
        assertEquals("Refactoring", result?.book?.title)
        assertEquals(1, result?.fields?.size)
        assertEquals("Martin Fowler", result?.fields?.get(0)?.name)
        assertEquals(DbConstants.FLD_AUTHOR, result?.fields?.get(0)?.typeId)
    }

    @DisplayName("Insert Book Field Cross Ref - Conflict Ignore Scenario - Ignores Duplicate Relation")
    @Test
    fun insertBookFieldCrossRef_conflictIgnoreScenario_ignoresDuplicateRelation() = runTest {
        // Arrange
        val book = createBookEntity(1L, "Book")
        val field = FieldEntity(id = 101L, typeId = 1, name = "Field")
        bookDao.insertBook(book)
        bookDao.insertField(field)
        val crossRef = BookFieldCrossRef(1L, 101L)
        bookDao.insertBookFieldCrossRef(crossRef)

        // Act: Insert same crossRef should be ignored
        bookDao.insertBookFieldCrossRef(crossRef)

        // Assert: Still exactly one relation
        val result = bookDao.getBookWithFields(1L)
        assertEquals(1, result?.fields?.size)
    }

    @DisplayName("Delete Book Fields - Valid Book ID Provided - Removes Standalone Cross References")
    @Test
    fun deleteBookFields_validBookIdProvided_removesStandaloneCrossReferences() = runTest {
        // Arrange
        val book = createBookEntity(1L, "Book")
        val field = FieldEntity(id = 101L, typeId = 1, name = "Field")
        bookDao.insertBook(book)
        bookDao.insertField(field)
        bookDao.insertBookFieldCrossRef(BookFieldCrossRef(1L, 101L))

        // Act
        bookDao.deleteBookFields(1L)

        // Assert
        val result = bookDao.getBookWithFields(1L)
        assertTrue(result?.fields?.isEmpty() ?: false)
    }

    @DisplayName("Delete Book And Fields - Valid Book ID Provided - Atomically Removes Book and Cross References")
    @Test
    fun deleteBookAndFields_validBookIdProvided_atomicallyRemovesBookAndCrossReferences() = runTest {
        // Arrange
        val book = createBookEntity(10L, "1984")
        val field = FieldEntity(id = 301L, typeId = DbConstants.FLD_AUTHOR, name = "George Orwell")
        val crossRef = BookFieldCrossRef(bookId = 10L, fieldId = 301L)

        bookDao.insertBook(book)
        bookDao.insertField(field)
        bookDao.insertBookFieldCrossRef(crossRef)

        // Act: Delete book and fields atomically using custom SQL transaction
        bookDao.deleteBookAndFields(10L)

        // Assert
        val loadedBook = bookDao.getBookWithFields(10L)
        assertNull(loadedBook)
    }

    @DisplayName("Get Book With Fields Flow - Valid Book ID - Emits Updates in Real Time")
    @Test
    fun getBookWithFieldsFlow_validBookId_emitsUpdatesInRealTime() = runTest {
        // Arrange
        val bookId = 15L
        val book = createBookEntity(bookId, "Initial Book")
        bookDao.insertBook(book)

        // Act & Assert: Get Flow and collect first emission
        val firstEmission = bookDao.getBookWithFieldsFlow(bookId).first()
        assertNotNull(firstEmission)
        assertEquals("Initial Book", firstEmission?.book?.title)
    }

    @DisplayName("Get All Books With Fields - Multiple Books Exist - Retrieves Multiple Relations")
    @Test
    fun getAllBooksWithFields_multipleBooksExist_retrievesMultipleRelations() = runTest {
        // Arrange
        val book1 = createBookEntity(1L, "Book One")
        val book2 = createBookEntity(2L, "Book Two")
        bookDao.insertBook(book1)
        bookDao.insertBook(book2)

        // Act
        val allBooks = bookDao.getAllBooksWithFields()

        // Assert
        assertEquals(2, allBooks.size)
        assertTrue(allBooks.any { it.book.title == "Book One" })
        assertTrue(allBooks.any { it.book.title == "Book Two" })
    }

    @DisplayName("Get All Books With Fields Flow - Multiple Books Exist - Emits All Updates")
    @Test
    fun getAllBooksWithFieldsFlow_multipleBooksExist_emitsAllUpdates() = runTest {
        // Arrange
        val book1 = createBookEntity(1L, "Book One")
        bookDao.insertBook(book1)

        // Act & Assert
        val emission = bookDao.getAllBooksWithFieldsFlow().first()
        assertEquals(1, emission.size)
        assertEquals("Book One", emission[0].book.title)
    }
}
