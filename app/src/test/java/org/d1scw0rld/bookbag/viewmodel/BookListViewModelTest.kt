package org.d1scw0rld.bookbag.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.d1scw0rld.bookbag.data.entity.BookEntity
import org.d1scw0rld.bookbag.data.relation.BookWithFields
import org.d1scw0rld.bookbag.data.repository.BookRepository
import org.d1scw0rld.bookbag.ui.state.UiState
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.d1scw0rld.bookbag.DisplayNameRunner
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(DisplayNameRunner::class)
class BookListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: BookRepository
    private lateinit var viewModel: BookListViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock(BookRepository::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @DisplayName("Load Books - Repository Succeeds - Emits Success UI State With Book List")
    @Test
    fun loadBooks_repositorySucceeds_emitsSuccessUiStateWithBookList() = runTest {
        // Arrange
        val bookRelation = BookWithFields(
            book = BookEntity(id = 1L, title = "Clean Code", description = null, volume = null, publicationDate = null, pages = null, price = null, value = null, dueDate = null, readDate = null, edition = null, isbn = null, web = null),
            fields = emptyList()
        )
        val expectedBooks = listOf(bookRelation)
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(expectedBooks))

        // Act
        viewModel = BookListViewModel(repository)

        // Assert
        assertTrue(viewModel.uiState.value is UiState.Success)
        val successData = (viewModel.uiState.value as UiState.Success).data
        assertEquals(1, successData.size)
        assertEquals("Clean Code", successData[0].book.title)
    }

    @DisplayName("Load Books - Repository Throws Exception - Emits Error UI State")
    @Test
    fun loadBooks_repositoryThrowsException_emitsErrorUiState() = runTest {
        // Arrange
        val expectedException = RuntimeException("Database error")
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flow { throw expectedException })

        // Act
        viewModel = BookListViewModel(repository)

        // Assert
        assertTrue(viewModel.uiState.value is UiState.Error)
        val errorException = (viewModel.uiState.value as UiState.Error).exception
        assertEquals("Database error", errorException.message)
    }

    @DisplayName("Delete Book - Valid Book ID Provided - Invokes Repository Delete")
    @Test
    fun deleteBook_validBookIdProvided_invokesRepositoryDelete() = runTest {
        // Arrange
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        viewModel = BookListViewModel(repository)

        // Act
        viewModel.deleteBook(100L)

        // Assert
        verify(repository).deleteBookAndRelations(100L)
    }

    @DisplayName("Import Database - Import Succeeds - Updates File Op State With Success")
    @Test
    fun importDatabase_importSucceeds_updatesFileOpStateWithSuccess() = runTest {
        // Arrange
        val filePath = "/path/to/import.db"
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        whenever(repository.importDatabase(filePath)).thenReturn(true)
        viewModel = BookListViewModel(repository)

        // Act
        viewModel.importDatabase(filePath)

        // Assert
        assertTrue(viewModel.fileOpState.value is UiState.Success)
        assertEquals(FileOperationType.IMPORT, (viewModel.fileOpState.value as UiState.Success).data)
    }

    @DisplayName("Import Database - Import Fails - Updates File Op State With Error")
    @Test
    fun importDatabase_importFails_updatesFileOpStateWithError() = runTest {
        // Arrange
        val filePath = "/path/to/import.db"
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        whenever(repository.importDatabase(filePath)).thenReturn(false)
        viewModel = BookListViewModel(repository)

        // Act
        viewModel.importDatabase(filePath)

        // Assert
        assertTrue(viewModel.fileOpState.value is UiState.Error)
        assertEquals("Import failed", (viewModel.fileOpState.value as UiState.Error).exception.message)
    }

    @DisplayName("Export Database - Export Succeeds - Updates File Op State With Success")
    @Test
    fun exportDatabase_exportSucceeds_updatesFileOpStateWithSuccess() = runTest {
        // Arrange
        val filePath = "/path/to/export.db"
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        whenever(repository.exportDatabase(filePath)).thenReturn(true)
        viewModel = BookListViewModel(repository)

        // Act
        viewModel.exportDatabase(filePath)

        // Assert
        assertTrue(viewModel.fileOpState.value is UiState.Success)
        assertEquals(FileOperationType.EXPORT, (viewModel.fileOpState.value as UiState.Success).data)
    }

    @DisplayName("Export Database - Export Fails - Updates File Op State With Error")
    @Test
    fun exportDatabase_exportFails_updatesFileOpStateWithError() = runTest {
        // Arrange
        val filePath = "/path/to/export.db"
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        whenever(repository.exportDatabase(filePath)).thenReturn(false)
        viewModel = BookListViewModel(repository)

        // Act
        viewModel.exportDatabase(filePath)

        // Assert
        assertTrue(viewModel.fileOpState.value is UiState.Error)
        assertEquals("Export failed", (viewModel.fileOpState.value as UiState.Error).exception.message)
    }

    @DisplayName("Consume File Operation - Active File Op State - Resets File Op State to Null")
    @Test
    fun consumeFileOperation_activeFileOpState_resetsFileOpStateToNull() = runTest {
        // Arrange
        val filePath = "/path/to/export.db"
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        whenever(repository.exportDatabase(filePath)).thenReturn(true)
        viewModel = BookListViewModel(repository)

        viewModel.exportDatabase(filePath)

        // Act
        viewModel.consumeFileOperation()

        // Assert
        assertNull(viewModel.fileOpState.value)
    }
}
