package org.d1scw0rld.bookbag.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.d1scw0rld.bookbag.data.AppDatabase
import org.d1scw0rld.bookbag.data.DbConstants
import org.d1scw0rld.bookbag.data.dao.BookDao
import org.d1scw0rld.bookbag.data.entity.BookEntity
import org.d1scw0rld.bookbag.data.entity.BookFieldCrossRef
import org.d1scw0rld.bookbag.data.entity.FieldEntity
import org.d1scw0rld.bookbag.dto.Book
import org.d1scw0rld.bookbag.dto.Changeable
import org.d1scw0rld.bookbag.dto.Property
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.d1scw0rld.bookbag.DisplayNameRobolectricRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(DisplayNameRobolectricRunner::class)
@Config(sdk = [28])
class BookRepositoryImplTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var bookDao: BookDao
    private lateinit var repository: BookRepositoryImpl

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        bookDao = db.bookDao()
        repository = BookRepositoryImpl(bookDao, context)
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        db.close()
    }

    private fun createBookDto(id: Long, title: String): Book {
        return Book(
            id = id,
            title = Changeable(title),
            description = Changeable("Test Description"),
            volume = Changeable(1),
            publicationDate = Changeable(2023),
            pages = Changeable(350),
            price = Changeable("1500|1"),
            value = Changeable("2000|1"),
            dueDate = Changeable(0),
            readDate = Changeable(0),
            edition = Changeable(1),
            isbn = Changeable("1234567890"),
            web = Changeable("http://test.com"),
            properties = ArrayList()
        )
    }

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

    @DisplayName("Get Book With Fields - Valid Book ID - Returns Correct Relation")
    @Test
    fun getBookWithFields_validBookId_returnsCorrectRelation() = runTest {
        // Arrange
        val bookId = 1L
        val book = createBookEntity(bookId, "Refactoring")
        bookDao.insertBook(book)

        val field = FieldEntity(id = 10L, typeId = DbConstants.FLD_AUTHOR, name = "Martin Fowler")
        bookDao.insertField(field)

        bookDao.insertBookFieldCrossRef(BookFieldCrossRef(bookId, 10L))

        // Act
        val result = repository.getBookWithFields(bookId)

        // Assert
        assertNotNull(result)
        assertEquals("Refactoring", result?.book?.title)
        assertEquals(1, result?.fields?.size)
        assertEquals("Martin Fowler", result?.fields?.get(0)?.name)
    }

    @DisplayName("Get Book With Fields Flow - Valid Book ID - Emits Correct Relation")
    @Test
    fun getBookWithFieldsFlow_validBookId_emitsCorrectRelation() = runTest {
        // Arrange
        val bookId = 2L
        val book = createBookEntity(bookId, "Clean Code")
        bookDao.insertBook(book)

        val field = FieldEntity(id = 20L, typeId = DbConstants.FLD_AUTHOR, name = "Robert C. Martin")
        bookDao.insertField(field)

        bookDao.insertBookFieldCrossRef(BookFieldCrossRef(bookId, 20L))

        // Act & Assert
        val emission = repository.getBookWithFieldsFlow(bookId).first()
        assertNotNull(emission)
        assertEquals("Clean Code", emission?.book?.title)
        assertEquals(1, emission?.fields?.size)
        assertEquals("Robert C. Martin", emission?.fields?.get(0)?.name)
    }

    @DisplayName("Get All Books With Fields - Multiple Books Exist - Returns All Items")
    @Test
    fun getAllBooksWithFields_multipleBooksExist_returnsAllItems() = runTest {
        // Arrange
        val b1 = createBookEntity(1L, "Book One")
        val b2 = createBookEntity(2L, "Book Two")
        bookDao.insertBook(b1)
        bookDao.insertBook(b2)

        // Act
        val list = repository.getAllBooksWithFields()

        // Assert
        assertEquals(2, list.size)
        assertTrue(list.any { it.book.title == "Book One" })
        assertTrue(list.any { it.book.title == "Book Two" })
    }

    @DisplayName("Get All Books With Fields Flow - Multiple Books Exist - Emits All Items")
    @Test
    fun getAllBooksWithFieldsFlow_multipleBooksExist_emitsAllItems() = runTest {
        // Arrange
        val b1 = createBookEntity(1L, "Book One")
        bookDao.insertBook(b1)

        // Act & Assert
        val list = repository.getAllBooksWithFieldsFlow().first()
        assertEquals(1, list.size)
        assertEquals("Book One", list[0].book.title)
    }

    @DisplayName("Save Book With Fields - New Book With Properties - Inserts Book and Fields and Updates IDs")
    @Test
    fun saveBookWithFields_newBookWithProperties_insertsBookAndFieldsAndUpdatesIds() = runTest {
        // Arrange
        val bookDto = createBookDto(0L, "New Book")
        val prop = Property(fieldTypeId = DbConstants.FLD_AUTHOR, value = "New Author", id = 0L)
        bookDto.properties.add(prop)

        // Act
        repository.saveBookWithFields(bookDto)

        // Assert
        val booksInDb = repository.getAllBooksWithFields()
        assertEquals(1, booksInDb.size)
        val savedBookWithFields = booksInDb[0]
        assertEquals("New Book", savedBookWithFields.book.title)
        assertEquals(1, savedBookWithFields.fields.size)
        assertEquals("New Author", savedBookWithFields.fields[0].name)
        
        // Check that the returned property ID was updated with generated DB field ID
        assertNotEquals(0L, prop.id)
    }

    @DisplayName("Save Book With Fields - Existing Book and Modified Properties - Updates Book and Cleans Old Relations")
    @Test
    fun saveBookWithFields_existingBookAndModifiedProperties_updatesBookAndCleansOldRelations() = runTest {
        // Arrange
        // First save a book
        val bookDto = createBookDto(0L, "Original Title")
        val prop1 = Property(fieldTypeId = DbConstants.FLD_AUTHOR, value = "Author One", id = 0L)
        bookDto.properties.add(prop1)
        repository.saveBookWithFields(bookDto)

        // Get inserted book ID
        val savedList = repository.getAllBooksWithFields()
        val bookId = savedList[0].book.id

        // Modify DTO
        val updatedBookDto = createBookDto(bookId, "Updated Title")
        // Update the property's ID and keep it
        prop1.id = savedList[0].fields[0].id
        updatedBookDto.properties.add(prop1)
        // Add a brand new property
        val prop2 = Property(fieldTypeId = DbConstants.FLD_GENRE, value = "Computer Science", id = 0L)
        updatedBookDto.properties.add(prop2)

        // Act
        repository.saveBookWithFields(updatedBookDto)

        // Assert
        val updatedList = repository.getAllBooksWithFields()
        assertEquals(1, updatedList.size)
        val savedBook = updatedList[0]
        assertEquals("Updated Title", savedBook.book.title)
        assertEquals(2, savedBook.fields.size)
        assertTrue(savedBook.fields.any { it.name == "Author One" })
        assertTrue(savedBook.fields.any { it.name == "Computer Science" })
        assertNotEquals(0L, prop2.id)
    }

    @DisplayName("Delete Book And Relations - Valid Book ID - Removes Book and Cross References But Retains Global Fields")
    @Test
    fun deleteBookAndRelations_validBookId_removesBookAndCrossReferencesButRetainsGlobalFields() = runTest {
        // Arrange
        val bookDto = createBookDto(0L, "Book to Delete")
        val prop = Property(fieldTypeId = DbConstants.FLD_AUTHOR, value = "Some Author", id = 0L)
        bookDto.properties.add(prop)
        repository.saveBookWithFields(bookDto)

        val savedList = repository.getAllBooksWithFields()
        assertEquals(1, savedList.size)
        val bookId = savedList[0].book.id

        // Act
        repository.deleteBookAndRelations(bookId)

        // Assert
        val currentList = repository.getAllBooksWithFields()
        assertTrue(currentList.isEmpty())
        
        // Assert that the fields table still holds the global FieldEntity (it's not cascade-deleted)
        val fields = repository.getFieldsByType(DbConstants.FLD_AUTHOR)
        assertEquals(1, fields.size)
        assertEquals("Some Author", fields[0].name)
    }

    @DisplayName("Get Fields By Type - Valid Type ID - Returns Only Requested Types")
    @Test
    fun getFieldsByType_validTypeId_returnsOnlyRequestedTypes() = runTest {
        // Arrange
        val f1 = FieldEntity(id = 100L, typeId = DbConstants.FLD_AUTHOR, name = "Author")
        val f2 = FieldEntity(id = 101L, typeId = DbConstants.FLD_GENRE, name = "Genre")
        bookDao.insertField(f1)
        bookDao.insertField(f2)

        // Act
        val authorFields = repository.getFieldsByType(DbConstants.FLD_AUTHOR)

        // Assert
        assertEquals(1, authorFields.size)
        assertEquals("Author", authorFields[0].name)
    }

    @DisplayName("Export And Import Database - Valid Database and Target Files - Delegates Correctly and Restores Schema")
    @Test
    fun exportAndImportDatabase_validDatabaseAndTargetFiles_delegatesCorrectlyAndRestoresSchema() = runTest {
        // To test import/export, we'll perform a basic check using temporary files
        // We initialize a valid SQLite database file so AppDatabase functions have something valid to sanitize and copy from/to
        val dbFile = context.getDatabasePath("book_bag.db")
        dbFile.parentFile?.mkdirs()
        if (dbFile.exists()) dbFile.delete()
        
        val sqliteDb = android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(dbFile, null)
        sqliteDb.execSQL("CREATE TABLE books (_id INTEGER PRIMARY KEY, title TEXT)")
        sqliteDb.close()

        val targetExportFile = File(context.cacheDir, "temp_export.db")
        if (targetExportFile.exists()) targetExportFile.delete()

        // Act - Export
        val exportResult = repository.exportDatabase(targetExportFile.absolutePath)

        // Assert - Export
        assertTrue(exportResult)
        assertTrue(targetExportFile.exists())

        // Act - Import
        // Clean original db file first to verify it gets restored
        dbFile.delete()
        assertFalse(dbFile.exists())

        val importResult = repository.importDatabase(targetExportFile.absolutePath)

        // Assert - Import
        assertTrue(importResult)
        assertTrue(dbFile.exists())

        // Clean up
        targetExportFile.delete()
        if (dbFile.exists()) dbFile.delete()
    }
}
