package org.d1scw0rld.bookbag.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.d1scw0rld.bookbag.DisplayNameRobolectricRunner
import org.d1scw0rld.bookbag.data.AppDatabase
import org.d1scw0rld.bookbag.data.DbConstants
import org.d1scw0rld.bookbag.data.dao.BookDao
import org.d1scw0rld.bookbag.data.dao.BookDaoProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.d1scw0rld.bookbag.data.entity.BookEntity
import org.d1scw0rld.bookbag.data.entity.BookFieldCrossRef
import org.d1scw0rld.bookbag.data.entity.FieldEntity
import org.d1scw0rld.bookbag.dto.Book
import org.d1scw0rld.bookbag.dto.Changeable
import org.d1scw0rld.bookbag.dto.Property
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
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
            description = Changeable(DEFAULT_DESCRIPTION),
            volume = Changeable(DEFAULT_VOLUME),
            publicationDate = Changeable(DEFAULT_PUBLICATION_DATE),
            pages = Changeable(DEFAULT_PAGES),
            price = Changeable(DEFAULT_PRICE),
            value = Changeable(DEFAULT_VALUE),
            dueDate = Changeable(DEFAULT_DUE_DATE),
            readDate = Changeable(DEFAULT_READ_DATE),
            edition = Changeable(DEFAULT_EDITION),
            isbn = Changeable(DEFAULT_ISBN),
            web = Changeable(DEFAULT_WEB),
            properties = ArrayList()
        )
    }

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

    @DisplayName("Get Book With Fields - Valid Book ID - Returns Correct Relation")
    @Test
    fun getBookWithFields_validBookId_returnsCorrectRelation() = runTest {
        // Arrange
        val book = createBookEntity(TEST_BOOK_ID_1, TITLE_REFACTORING)
        bookDao.insertBook(book)

        val field = FieldEntity(id = TEST_FIELD_ID_10, typeId = DbConstants.FLD_AUTHOR, name = AUTHOR_MARTIN_FOWLER)
        bookDao.insertField(field)

        bookDao.insertBookFieldCrossRef(BookFieldCrossRef(TEST_BOOK_ID_1, TEST_FIELD_ID_10))

        // Act
        val result = repository.getBookWithFields(TEST_BOOK_ID_1)

        // Assert
        assertNotNull(result)
        assertEquals(TITLE_REFACTORING, result?.book?.title)
        assertEquals(1, result?.fields?.size)
        assertEquals(AUTHOR_MARTIN_FOWLER, result?.fields?.get(0)?.name)
    }

    @DisplayName("Get Book With Fields Flow - Valid Book ID - Emits Correct Relation")
    @Test
    fun getBookWithFieldsFlow_validBookId_emitsCorrectRelation() = runTest {
        // Arrange
        val bookId = TEST_BOOK_ID_2
        val book = createBookEntity(bookId, TITLE_CLEAN_CODE)
        bookDao.insertBook(book)

        val field = FieldEntity(id = TEST_FIELD_ID_20, typeId = DbConstants.FLD_AUTHOR, name = AUTHOR_ROBERT_MARTIN)
        bookDao.insertField(field)

        bookDao.insertBookFieldCrossRef(BookFieldCrossRef(bookId, TEST_FIELD_ID_20))

        // Act & Assert
        val emission = repository.getBookWithFieldsFlow(bookId).first()
        assertNotNull(emission)
        assertEquals(TITLE_CLEAN_CODE, emission?.book?.title)
        assertEquals(1, emission?.fields?.size)
        assertEquals(AUTHOR_ROBERT_MARTIN, emission?.fields?.get(0)?.name)
    }

    @DisplayName("Get All Books With Fields - Multiple Books Exist - Returns All Items")
    @Test
    fun getAllBooksWithFields_multipleBooksExist_returnsAllItems() = runTest {
        // Arrange
        val b1 = createBookEntity(TEST_BOOK_ID_1, TITLE_BOOK_ONE)
        val b2 = createBookEntity(TEST_BOOK_ID_2, TITLE_BOOK_TWO)
        bookDao.insertBook(b1)
        bookDao.insertBook(b2)

        // Act
        val list = repository.getAllBooksWithFields()

        // Assert
        assertEquals(2, list.size)
        assertTrue(list.any { it.book.title == TITLE_BOOK_ONE })
        assertTrue(list.any { it.book.title == TITLE_BOOK_TWO })
    }

    @DisplayName("Get All Books With Fields Flow - Multiple Books Exist - Emits All Items")
    @Test
    fun getAllBooksWithFieldsFlow_multipleBooksExist_emitsAllItems() = runTest {
        // Arrange
        val b1 = createBookEntity(TEST_BOOK_ID_1, TITLE_BOOK_ONE)
        bookDao.insertBook(b1)

        // Act & Assert
        val list = repository.getAllBooksWithFieldsFlow().first()
        assertEquals(1, list.size)
        assertEquals(TITLE_BOOK_ONE, list[0].book.title)
    }

    @DisplayName("Save Book With Fields - New Book With Properties - Inserts Book and Fields and Updates IDs")
    @Test
    fun saveBookWithFields_newBookWithProperties_insertsBookAndFieldsAndUpdatesIds() = runTest {
        // Arrange
        val bookDto = createBookDto(TEST_BOOK_ID_NEW, TITLE_NEW_BOOK)
        val prop = Property(fieldTypeId = DbConstants.FLD_AUTHOR, value = AUTHOR_NEW_AUTHOR, id = TEST_FIELD_ID_NEW)
        bookDto.properties.add(prop)

        // Act
        repository.saveBookWithFields(bookDto)

        // Assert
        val booksInDb = repository.getAllBooksWithFields()
        assertEquals(1, booksInDb.size)
        val savedBookWithFields = booksInDb[0]
        assertEquals(TITLE_NEW_BOOK, savedBookWithFields.book.title)
        assertEquals(1, savedBookWithFields.fields.size)
        assertEquals(AUTHOR_NEW_AUTHOR, savedBookWithFields.fields[0].name)
        
        // Check that the returned property ID was updated with generated DB field ID
        assertNotEquals(TEST_FIELD_ID_NEW, prop.id)
    }

    @DisplayName("Save Book With Fields - Existing Book and Modified Properties - Updates Book and Cleans Old Relations")
    @Test
    fun saveBookWithFields_existingBookAndModifiedProperties_updatesBookAndCleansOldRelations() = runTest {
        // Arrange
        // First save a book
        val bookDto = createBookDto(TEST_BOOK_ID_NEW, TITLE_ORIGINAL)
        val prop1 = Property(fieldTypeId = DbConstants.FLD_AUTHOR, value = AUTHOR_ONE, id = TEST_FIELD_ID_NEW)
        bookDto.properties.add(prop1)
        repository.saveBookWithFields(bookDto)

        // Get inserted book ID
        val savedList = repository.getAllBooksWithFields()
        val bookId = savedList[0].book.id

        // Modify DTO
        val updatedBookDto = createBookDto(bookId, TITLE_UPDATED)
        // Update the property's ID and keep it
        prop1.id = savedList[0].fields[0].id
        updatedBookDto.properties.add(prop1)
        // Add a brand new property
        val prop2 = Property(fieldTypeId = DbConstants.FLD_GENRE, value = GENRE_CS, id = TEST_FIELD_ID_NEW)
        updatedBookDto.properties.add(prop2)

        // Act
        repository.saveBookWithFields(updatedBookDto)

        // Assert
        val updatedList = repository.getAllBooksWithFields()
        assertEquals(1, updatedList.size)
        val savedBook = updatedList[0]
        assertEquals(TITLE_UPDATED, savedBook.book.title)
        assertEquals(2, savedBook.fields.size)
        assertTrue(savedBook.fields.any { it.name == AUTHOR_ONE })
        assertTrue(savedBook.fields.any { it.name == GENRE_CS })
        assertNotEquals(TEST_FIELD_ID_NEW, prop2.id)
    }

    @DisplayName("Delete Book And Relations - Valid Book ID - Removes Book and Cross References But Retains Global Fields")
    @Test
    fun deleteBookAndRelations_validBookId_removesBookAndCrossReferencesButRetainsGlobalFields() = runTest {
        // Arrange
        val bookDto = createBookDto(TEST_BOOK_ID_NEW, TITLE_DELETE)
        val prop = Property(fieldTypeId = DbConstants.FLD_AUTHOR, value = AUTHOR_SOME_AUTHOR, id = TEST_FIELD_ID_NEW)
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
        assertEquals(AUTHOR_SOME_AUTHOR, fields[0].name)
    }

    @DisplayName("Get Fields By Type - Valid Type ID - Returns Only Requested Types")
    @Test
    fun getFieldsByType_validTypeId_returnsOnlyRequestedTypes() = runTest {
        // Arrange
        val f1 = FieldEntity(id = TEST_FIELD_ID_100, typeId = DbConstants.FLD_AUTHOR, name = AUTHOR_GENERIC)
        val f2 = FieldEntity(id = TEST_FIELD_ID_101, typeId = DbConstants.FLD_GENRE, name = GENRE_GENERIC)
        bookDao.insertField(f1)
        bookDao.insertField(f2)

        // Act
        val authorFields = repository.getFieldsByType(DbConstants.FLD_AUTHOR)

        // Assert
        assertEquals(1, authorFields.size)
        assertEquals(AUTHOR_GENERIC, authorFields[0].name)
    }

    @DisplayName("Export And Import Database - Valid Database and Target Files - Delegates Correctly and Restores Schema")
    @Test
    fun exportAndImportDatabase_validDatabaseAndTargetFiles_delegatesCorrectlyAndRestoresSchema() = runTest {
        // To test import/export, we'll perform a basic check using temporary files
        // We initialize a valid SQLite database file so AppDatabase functions have something valid to sanitize and copy from/to
        val dbFile = context.getDatabasePath(DB_FILE_NAME)
        dbFile.parentFile?.mkdirs()
        if (dbFile.exists()) dbFile.delete()
        
        val sqliteDb = android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(dbFile, null)
        sqliteDb.execSQL(SQL_CREATE_BOOKS)
        sqliteDb.close()

        val targetExportFile = File(context.cacheDir, TEMP_EXPORT_FILE_NAME)
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

    @DisplayName("Import Database - Backup Restored Into Live Repository - Reads Data From Newly Imported File")
    @Test
    fun importDatabase_backupRestoredIntoLiveRepository_readsDataFromNewlyImportedFile() = runTest {
        val dbFile = context.getDatabasePath(DB_FILE_NAME)
        dbFile.parentFile?.mkdirs()
        AppDatabase.closeAndReset()
        if (dbFile.exists()) dbFile.delete()
        File(dbFile.path + WAL_SUFFIX).delete()
        File(dbFile.path + SHM_SUFFIX).delete()

        val scope = CoroutineScope(SupervisorJob())
        // Mirrors the production wiring: the DAO is resolved from the current AppDatabase instance.
        val liveRepository = BookRepositoryImpl(
            { AppDatabase.getDatabase(context, scope).bookDao() },
            context
        )

        // 1. Seed the live database and take a backup of it.
        liveRepository.saveBookWithFields(createBookDto(TEST_BOOK_ID_NEW, TITLE_IN_BACKUP))
        val backupFile = File(context.cacheDir, TEMP_EXPORT_FILE_NAME)
        if (backupFile.exists()) backupFile.delete()
        assertTrue(EXPORT_TRUE_MSG, liveRepository.exportDatabase(backupFile.absolutePath))

        // 2. Diverge the live database so the imported content is distinguishable.
        liveRepository.saveBookWithFields(createBookDto(TEST_BOOK_ID_NEW, TITLE_AFTER_BACKUP))
        assertEquals(TWO_BOOKS_MSG, 2, liveRepository.getAllBooksWithFields().size)

        // 3. Import the backup, replacing the database file underneath the app.
        assertTrue(IMPORT_TRUE_MSG, liveRepository.importDatabase(backupFile.absolutePath))

        // 4. The repository must now read the imported file, not the discarded instance.
        val restoredBooks = liveRepository.getAllBooksWithFields()
        assertEquals(RESTORED_COUNT_MSG, 1, restoredBooks.size)
        assertEquals(TITLE_IN_BACKUP, restoredBooks.first().book.title)

        // 5. Book details must resolve too, which is what appeared blank before the fix.
        val restoredId = restoredBooks.first().book.id
        val detail = liveRepository.getBookWithFields(restoredId)
        assertNotNull(DETAIL_READABLE_MSG, detail)
        assertEquals(TITLE_IN_BACKUP, detail!!.book.title)
        assertEquals(DEFAULT_ISBN, detail.book.isbn)
        assertEquals(DEFAULT_PAGES, detail.book.pages)

        // 6. Freshly opened flows must be bound to the imported database as well.
        assertEquals(TITLE_IN_BACKUP, liveRepository.getAllBooksWithFieldsFlow().first().first().book.title)

        backupFile.delete()
        AppDatabase.closeAndReset()
        if (dbFile.exists()) dbFile.delete()
    }

    companion object {
        const val TITLE_IN_BACKUP = "Book Captured In Backup"
        const val TITLE_AFTER_BACKUP = "Book Added After Backup"

        const val WAL_SUFFIX = "-wal"
        const val SHM_SUFFIX = "-shm"

        const val EXPORT_TRUE_MSG = "Export should succeed"
        const val IMPORT_TRUE_MSG = "Import should succeed"
        const val TWO_BOOKS_MSG = "Live database should hold both books before import"
        const val RESTORED_COUNT_MSG = "Only the backed-up book should remain after import"
        const val DETAIL_READABLE_MSG = "Book details must be readable after import"

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

        const val TEST_BOOK_ID_NEW = 0L
        const val TEST_BOOK_ID_1 = 1L
        const val TEST_BOOK_ID_2 = 2L

        const val TEST_FIELD_ID_NEW = 0L
        const val TEST_FIELD_ID_10 = 10L
        const val TEST_FIELD_ID_20 = 20L
        const val TEST_FIELD_ID_100 = 100L
        const val TEST_FIELD_ID_101 = 101L

        const val TITLE_REFACTORING = "Refactoring"
        const val TITLE_CLEAN_CODE = "Clean Code"
        const val TITLE_BOOK_ONE = "Book One"
        const val TITLE_BOOK_TWO = "Book Two"
        const val TITLE_NEW_BOOK = "New Book"
        const val TITLE_ORIGINAL = "Original Title"
        const val TITLE_UPDATED = "Updated Title"
        const val TITLE_DELETE = "Book to Delete"

        const val AUTHOR_MARTIN_FOWLER = "Martin Fowler"
        const val AUTHOR_ROBERT_MARTIN = "Robert C. Martin"
        const val AUTHOR_NEW_AUTHOR = "New Author"
        const val AUTHOR_ONE = "Author One"
        const val AUTHOR_SOME_AUTHOR = "Some Author"
        const val AUTHOR_GENERIC = "Author"

        const val GENRE_CS = "Computer Science"
        const val GENRE_GENERIC = "Genre"

        const val DB_FILE_NAME = "book_bag.db"
        const val TEMP_EXPORT_FILE_NAME = "temp_export.db"
        const val SQL_CREATE_BOOKS = "CREATE TABLE books (_id INTEGER PRIMARY KEY, title TEXT)"
    }
}
