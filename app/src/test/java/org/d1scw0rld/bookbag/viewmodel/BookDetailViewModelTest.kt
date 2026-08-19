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
import org.d1scw0rld.bookbag.data.entity.FieldEntity
import org.d1scw0rld.bookbag.data.relation.BookWithFields
import org.d1scw0rld.bookbag.data.repository.BookRepository
import org.d1scw0rld.bookbag.ui.state.UiState
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.d1scw0rld.bookbag.DisplayNameRunner
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(DisplayNameRunner::class)
class BookDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: BookRepository
    private lateinit var viewModel: BookDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock(BookRepository::class.java)
        viewModel = BookDetailViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @DisplayName("Load Book - Valid Book ID - Fetches Currencies and Book Details Successfully")
    @Test
    fun loadBook_validBookId_fetchesCurrenciesAndBookDetailsSuccessfully() = runTest {
        // Arrange
        val bookId = TEST_BOOK_ID
        val mockBook = BookWithFields(
            book = BookEntity(id = bookId, title = TITLE_CLEAN_CODE, description = null, volume = null, publicationDate = null, pages = null, price = null, value = null, dueDate = null, readDate = null, edition = null, isbn = null, web = null),
            fields = emptyList()
        )
        val mockCurrency = FieldEntity(id = FIELD_ID_USD, typeId = FIELD_TYPE_ID_USD, name = CURRENCY_USD)

        whenever(repository.getFieldsByType(any())).thenReturn(listOf(mockCurrency))
        whenever(repository.getBookWithFieldsFlow(bookId)).thenReturn(flowOf(mockBook))

        // Act
        viewModel.loadBook(bookId)

        // Assert
        assertTrue(viewModel.uiState.value is UiState.Success)
        val data = (viewModel.uiState.value as UiState.Success).data
        assertEquals(bookId, data.bookWithFields?.book?.id)
        assertEquals(CURRENCY_USD, data.currencies[0].value)
        assertEquals(FIELD_TYPE_ID_USD, data.currencies[0].fieldTypeId)
    }

    @DisplayName("Load Book - Repository Currencies Fetch Fails - Emits Error UI State")
    @Test
    fun loadBook_repositoryCurrenciesFetchFails_emitsErrorUiState() = runTest {
        // Arrange
        whenever(repository.getFieldsByType(any())).thenThrow(RuntimeException(ERROR_CURRENCY_FETCH_FAILED))

        // Act
        viewModel.loadBook(TEST_BOOK_ID)

        // Assert
        assertTrue(viewModel.uiState.value is UiState.Error)
        val exception = (viewModel.uiState.value as UiState.Error).exception
        assertEquals(ERROR_CURRENCY_FETCH_FAILED, exception.message)
    }

    @DisplayName("Load Book - Repository Flow Throws Exception - Emits Error UI State")
    @Test
    fun loadBook_repositoryFlowThrowsException_emitsErrorUiState() = runTest {
        // Arrange
        val bookId = TEST_BOOK_ID
        val mockCurrency = FieldEntity(id = FIELD_ID_USD, typeId = FIELD_TYPE_ID_USD, name = CURRENCY_USD)

        whenever(repository.getFieldsByType(any())).thenReturn(listOf(mockCurrency))
        whenever(repository.getBookWithFieldsFlow(bookId)).thenReturn(flow { throw RuntimeException(ERROR_DB_STREAM_ERROR) })

        // Act
        viewModel.loadBook(bookId)

        // Assert
        assertTrue(viewModel.uiState.value is UiState.Error)
        val exception = (viewModel.uiState.value as UiState.Error).exception
        assertEquals(ERROR_DB_STREAM_ERROR, exception.message)
    }

    companion object {
        const val TEST_BOOK_ID = 12L
        const val TITLE_CLEAN_CODE = "Clean Code"
        const val FIELD_ID_USD = 50L
        const val FIELD_TYPE_ID_USD = 12
        const val CURRENCY_USD = "USD"
        
        const val ERROR_CURRENCY_FETCH_FAILED = "Currencies fetch failed"
        const val ERROR_DB_STREAM_ERROR = "Database stream error"
    }
}
