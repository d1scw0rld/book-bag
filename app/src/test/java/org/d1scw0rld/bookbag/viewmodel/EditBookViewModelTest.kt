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
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever



@OptIn(ExperimentalCoroutinesApi::class)
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
        DbConstants.FIELDS.add(org.d1scw0rld.bookbag.dto.Field(DbConstants.FLD_TITLE, "Title", org.d1scw0rld.bookbag.dto.Field.TYPE_TEXT))
        DbConstants.FIELDS.add(org.d1scw0rld.bookbag.dto.Field(DbConstants.FLD_AUTHOR, "Author", org.d1scw0rld.bookbag.dto.Field.TYPE_MULTIFIELD))
        DbConstants.FIELDS.add(org.d1scw0rld.bookbag.dto.Field(101, "Author Property", org.d1scw0rld.bookbag.dto.Field.TYPE_TEXT))
        DbConstants.FIELDS.add(org.d1scw0rld.bookbag.dto.Field(DbConstants.FLD_CURRENCY, "Currency", org.d1scw0rld.bookbag.dto.Field.TYPE_SPINNER))

        viewModel = EditBookViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadBook with id 0 loads empty book and success state`() = runTest {
        // Arrange
        whenever(repository.getFieldsByType(any())).thenReturn(emptyList())

        // Act
        viewModel.loadBook(bookId = 0L, isCopy = false)

        // Assert
        assertTrue(viewModel.uiState.value is UiState.Success)
        val successData = (viewModel.uiState.value as UiState.Success).data
        assertEquals(0L, successData.book.id)
        assertTrue(viewModel.book.title.value.isEmpty())
    }

    @Test
    fun `loadBook with existing id loads book details and fetches properties map`() = runTest {
        // Arrange
        val bookId = 15L
        val mockRelation = BookWithFields(
            book = BookEntity(id = bookId, title = "Clean Code", description = null, volume = null, publicationDate = null, pages = null, price = null, value = null, dueDate = null, readDate = null, edition = null, isbn = null, web = null),
            fields = emptyList()
        )
        val mockField = FieldEntity(id = 1L, name = "Author", typeId = 101)

        whenever(repository.getBookWithFields(bookId)).thenReturn(mockRelation)
        whenever(repository.getFieldsByType(any())).thenReturn(listOf(mockField))

        // Act
        viewModel.loadBook(bookId = bookId, isCopy = false)

        // Assert
        assertTrue(viewModel.uiState.value is UiState.Success)
        val data = (viewModel.uiState.value as UiState.Success).data
        assertEquals(bookId, data.book.id)
        assertEquals("Clean Code", data.book.title.value)
        assertEquals(bookId, viewModel.book.id)

        // Check properties are successfully mapped
        val fetchedList = data.propertiesMap[101]
        assertEquals(1, fetchedList?.size)
        assertEquals("Author", fetchedList?.get(0)?.value)
    }

    @Test
    fun `loadBook with isCopy true loads existing book but resets its id to 0`() = runTest {
        // Arrange
        val bookId = 42L
        val mockRelation = BookWithFields(
            book = BookEntity(id = bookId, title = "Refactoring", description = null, volume = null, publicationDate = null, pages = null, price = null, value = null, dueDate = null, readDate = null, edition = null, isbn = null, web = null),
            fields = emptyList()
        )
        whenever(repository.getBookWithFields(bookId)).thenReturn(mockRelation)
        whenever(repository.getFieldsByType(any())).thenReturn(emptyList())

        // Act
        viewModel.loadBook(bookId = bookId, isCopy = true)

        // Assert
        assertTrue(viewModel.uiState.value is UiState.Success)
        val data = (viewModel.uiState.value as UiState.Success).data
        assertEquals(0L, data.book.id) // Id is cleared for duplicates
        assertEquals("Refactoring", data.book.title.value)
    }

    @Test
    fun `saveBook cleans up empty property values and invokes repository save successfully`() = runTest {
        // Arrange
        val book = viewModel.book
        book.title.value = "Design Patterns"

        // Add one valid and one empty property to test filtering logic
        val prop1 = Property(fieldTypeId = 1, value = "Erich Gamma", id = 101L)
        val prop2 = Property(fieldTypeId = 2, value = "   ", id = 102L) // empty string
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
        assertEquals(1, book.properties.size)
        assertEquals("Erich Gamma", book.properties[0].value)

        // Verify repository save was called
        verify(repository).saveBookWithFields(book)

        // Verify save flow emitted true
        assertEquals(1, results.size)
        assertTrue(results[0])
        collectJob.cancel()
    }

    @Test
    fun `saveBook emits false on saveSuccess when repository throws exception`() = runTest {
        // Arrange: Make repository save fail
        whenever(repository.saveBookWithFields(any())).thenThrow(RuntimeException("DB Constraint Failed"))

        val results = mutableListOf<Boolean>()
        val collectJob = launch(UnconfinedTestDispatcher()) {
            viewModel.saveSuccess.collect { results.add(it) }
        }

        // Act
        viewModel.saveBook()

        // Assert
        assertEquals(1, results.size)
        assertEquals(false, results[0])
        collectJob.cancel()
    }
}
