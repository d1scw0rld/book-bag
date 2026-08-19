package org.d1scw0rld.bookbag.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.d1scw0rld.bookbag.data.DbConstants
import org.d1scw0rld.bookbag.data.entity.BookEntity
import org.d1scw0rld.bookbag.data.entity.FieldEntity
import org.d1scw0rld.bookbag.data.relation.BookWithFields
import org.d1scw0rld.bookbag.data.repository.BookRepository
import org.d1scw0rld.bookbag.dto.Property
import org.d1scw0rld.bookbag.ui.state.UiState
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.d1scw0rld.bookbag.DisplayNameRunner
import org.d1scw0rld.bookbag.dto.Field
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(DisplayNameRunner::class)
class EditBookViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: BookRepository
    private lateinit var viewModel: EditBookViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock(BookRepository::class.java)
        
        // Initialize DbConstants manually to avoid Android resource loading in JVM tests
        DbConstants.FIELDS.clear()
        DbConstants.FIELDS.add(Field(DbConstants.FLD_TITLE, FIELD_NAME_TITLE, Field.TYPE_TEXT))
        DbConstants.FIELDS.add(Field(DbConstants.FLD_AUTHOR, FIELD_NAME_AUTHOR, Field.TYPE_MULTIFIELD))
        DbConstants.FIELDS.add(Field(FIELD_ID_AUTHOR_PROPERTY, FIELD_NAME_AUTHOR_PROPERTY, Field.TYPE_TEXT))
        DbConstants.FIELDS.add(Field(DbConstants.FLD_CURRENCY, FIELD_NAME_CURRENCY, Field.TYPE_SPINNER))

        viewModel = EditBookViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @DisplayName("Load Book - ID Zero Provided - Loads Empty Book and Emits Success State")
    @Test
    fun loadBook_idZeroProvided_loadsEmptyBookAndEmitsSuccessState() = runTest {
        // Arrange
        whenever(repository.getFieldsByType(any())).thenReturn(emptyList())

        // Act
        viewModel.loadBook(bookId = BOOK_ID_ZERO, isCopy = false)

        // Assert
        assertTrue(viewModel.uiState.value is UiState.Success)
        val successData = (viewModel.uiState.value as UiState.Success).data
        assertEquals(BOOK_ID_ZERO, successData.book.id)
        assertTrue(viewModel.book.title.value.isEmpty())
    }

    @DisplayName("Load Book - Existing Book ID Provided - Loads Book Details and Fetches Properties Map")
    @Test
    fun loadBook_existingBookIdProvided_loadsBookDetailsAndFetchesPropertiesMap() = runTest {
        // Arrange
        val bookId = BOOK_ID_EXISTING
        val mockRelation = BookWithFields(
            book = BookEntity(id = bookId, title = TITLE_CLEAN_CODE, description = null, volume = null, publicationDate = null, pages = null, price = null, value = null, dueDate = null, readDate = null, edition = null, isbn = null, web = null),
            fields = emptyList()
        )
        val mockField = FieldEntity(id = FIELD_ID_1, name = FIELD_NAME_AUTHOR, typeId = FIELD_ID_AUTHOR_PROPERTY)

        whenever(repository.getBookWithFields(bookId)).thenReturn(mockRelation)
        whenever(repository.getFieldsByType(any())).thenReturn(listOf(mockField))

        // Act
        viewModel.loadBook(bookId = bookId, isCopy = false)

        // Assert
        assertTrue(viewModel.uiState.value is UiState.Success)
        val data = (viewModel.uiState.value as UiState.Success).data
        assertEquals(bookId, data.book.id)
        assertEquals(TITLE_CLEAN_CODE, data.book.title.value)
        assertEquals(bookId, viewModel.book.id)

        // Check properties are successfully mapped
        val fetchedList = data.propertiesMap[FIELD_ID_AUTHOR_PROPERTY]
        assertEquals(EXPECTED_PROPERTIES_SIZE_1, fetchedList?.size)
        assertEquals(FIELD_NAME_AUTHOR, fetchedList?.get(0)?.value)
    }

    @DisplayName("Load Book - Is Copy True Provided - Loads Book Details But Resets ID to Zero")
    @Test
    fun loadBook_isCopyTrueProvided_loadsBookDetailsButResetsIdToZero() = runTest {
        // Arrange
        val bookId = BOOK_ID_COPY
        val mockRelation = BookWithFields(
            book = BookEntity(id = bookId, title = TITLE_REFACTORING, description = null, volume = null, publicationDate = null, pages = null, price = null, value = null, dueDate = null, readDate = null, edition = null, isbn = null, web = null),
            fields = emptyList()
        )
        whenever(repository.getBookWithFields(bookId)).thenReturn(mockRelation)
        whenever(repository.getFieldsByType(any())).thenReturn(emptyList())

        // Act
        viewModel.loadBook(bookId = bookId, isCopy = true)

        // Assert
        assertTrue(viewModel.uiState.value is UiState.Success)
        val data = (viewModel.uiState.value as UiState.Success).data
        assertEquals(BOOK_ID_ZERO, data.book.id) // Id is cleared for duplicates
        assertEquals(TITLE_REFACTORING, data.book.title.value)
    }

    @DisplayName("Save Book - Valid Book With Some Empty Properties - Cleans Empty Properties and Saves Successfully")
    @Test
    fun saveBook_validBookWithSomeEmptyProperties_cleansEmptyPropertiesAndSavesSuccessfully() = runTest {
        // Arrange
        val book = viewModel.book
        book.title.value = TITLE_DESIGN_PATTERNS

        // Add one valid and one empty property to test filtering logic
        val prop1 = Property(fieldTypeId = FIELD_TYPE_ID_1, value = AUTHOR_ERICH_GAMMA, id = FIELD_ID_101)
        val prop2 = Property(fieldTypeId = FIELD_TYPE_ID_2, value = EMPTY_STRING, id = FIELD_ID_102) // empty string
        book.properties.add(prop1)
        book.properties.add(prop2)

        // Collect save results in a separate job with UnconfinedTestDispatcher to ensure no missed hot-flow emissions
        val results = mutableListOf<Boolean>()
        val collectJob = launch(UnconfinedTestDispatcher()) {
            viewModel.saveSuccess.collect { results.add(it) }
        }

        // Act
        viewModel.saveBook()

        // Assert
        // Verify property list filtered out empty elements
        assertEquals(EXPECTED_PROPERTIES_SIZE_1, book.properties.size)
        assertEquals(AUTHOR_ERICH_GAMMA, book.properties[0].value)

        // Verify repository save was called
        verify(repository).saveBookWithFields(book)

        // Verify save flow emitted true
        assertEquals(EXPECTED_RESULTS_SIZE_1, results.size)
        assertTrue(results[0])
        collectJob.cancel()
    }

    @DisplayName("Save Book - Repository Throws Exception - Emits False on Save Success")
    @Test
    fun saveBook_repositoryThrowsException_emitsFalseOnSaveSuccess() = runTest {
        // Arrange: Make repository save fail
        whenever(repository.saveBookWithFields(any())).thenThrow(RuntimeException(ERROR_DB_CONSTRAINT_FAILED))

        val results = mutableListOf<Boolean>()
        val collectJob = launch(UnconfinedTestDispatcher()) {
            viewModel.saveSuccess.collect { results.add(it) }
        }

        // Act
        viewModel.saveBook()

        // Assert
        assertEquals(EXPECTED_RESULTS_SIZE_1, results.size)
        assertEquals(false, results[0])
        collectJob.cancel()
    }

    companion object {
        const val FIELD_NAME_TITLE = "Title"
        const val FIELD_NAME_AUTHOR = "Author"
        const val FIELD_NAME_AUTHOR_PROPERTY = "Author Property"
        const val FIELD_NAME_CURRENCY = "Currency"
        
        const val FIELD_ID_AUTHOR_PROPERTY = 101
        
        const val BOOK_ID_ZERO = 0L
        const val BOOK_ID_EXISTING = 15L
        const val BOOK_ID_COPY = 42L
        
        const val FIELD_ID_1 = 1L
        const val FIELD_ID_101 = 101L
        const val FIELD_ID_102 = 102L
        
        const val FIELD_TYPE_ID_1 = 1
        const val FIELD_TYPE_ID_2 = 2
        
        const val TITLE_CLEAN_CODE = "Clean Code"
        const val TITLE_REFACTORING = "Refactoring"
        const val TITLE_DESIGN_PATTERNS = "Design Patterns"
        
        const val AUTHOR_ERICH_GAMMA = "Erich Gamma"
        const val EMPTY_STRING = "   "
        
        const val EXPECTED_PROPERTIES_SIZE_1 = 1
        const val EXPECTED_RESULTS_SIZE_1 = 1
        
        const val ERROR_DB_CONSTRAINT_FAILED = "DB Constraint Failed"
    }
}
