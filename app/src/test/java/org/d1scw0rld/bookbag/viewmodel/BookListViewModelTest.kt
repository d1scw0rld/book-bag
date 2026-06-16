package org.d1scw0rld.bookbag.viewmodel

import android.content.Context
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class BookListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: BookRepository
    private lateinit var context: Context
    private lateinit var viewModel: BookListViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock(BookRepository::class.java)
        context = mock(Context::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadBooks emits Success with book list when repository succeeds`() = runTest {
        // Arrange
        val bookRelation = BookWithFields(
            book = BookEntity(id = 1L, title = "Clean Code", description = null, volume = null, publicationDate = null, pages = null, price = null, value = null, dueDate = null, readDate = null, edition = null, isbn = null, web = null),
            fields = emptyList()
        )
        val expectedBooks = listOf(bookRelation)
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(expectedBooks))

        // Act
        viewModel = BookListViewModel(repository, context)

        // Assert
        assertTrue(viewModel.uiState.value is UiState.Success)
        val successData = (viewModel.uiState.value as UiState.Success).data
        assertEquals(1, successData.size)
        assertEquals("Clean Code", successData[0].book.title)
    }

    @Test
    fun `loadBooks emits Error when repository throws exception`() = runTest {
        // Arrange
        val expectedException = RuntimeException("Database error")
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flow { throw expectedException })

        // Act
        viewModel = BookListViewModel(repository, context)

        // Assert
        assertTrue(viewModel.uiState.value is UiState.Error)
        val errorException = (viewModel.uiState.value as UiState.Error).exception
        assertEquals("Database error", errorException.message)
    }

    @Test
    fun `deleteBook invokes repository method`() = runTest {
        // Arrange
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        viewModel = BookListViewModel(repository, context)

        // Act
        viewModel.deleteBook(100L)

        // Assert
        verify(repository).deleteBookAndRelations(100L)
    }
}
