package org.d1scw0rld.bookbag.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.d1scw0rld.bookbag.data.relation.BookWithFields
import org.d1scw0rld.bookbag.data.repository.BookRepository
import org.d1scw0rld.bookbag.ui.state.UiState
import javax.inject.Inject

enum class FileOperationType {
    IMPORT, EXPORT
}

@HiltViewModel
class BookListViewModel @Inject constructor(
    private val repository: BookRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<BookWithFields>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<BookWithFields>>> = _uiState.asStateFlow()

    private val _fileOpState = MutableStateFlow<UiState<FileOperationType>?>(null)
    val fileOpState: StateFlow<UiState<FileOperationType>?> = _fileOpState.asStateFlow()

    init {
        loadBooks()
    }

    fun loadBooks() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.getAllBooksWithFieldsFlow()
                .catch { e -> _uiState.value = UiState.Error(e) }
                .collect { books -> _uiState.value = UiState.Success(books) }
        }
    }

    fun deleteBook(bookId: Long) {
        viewModelScope.launch {
            repository.deleteBookAndRelations(bookId)
        }
    }

    fun importDatabase(filePath: String) {
        viewModelScope.launch {
            _fileOpState.value = UiState.Loading
            val success = repository.importDatabase(filePath)
            if (success) {
                _fileOpState.value = UiState.Success(FileOperationType.IMPORT)
                loadBooks()
            } else {
                _fileOpState.value = UiState.Error(Exception("Import failed"))
            }
        }
    }

    fun exportDatabase(filePath: String) {
        viewModelScope.launch {
            _fileOpState.value = UiState.Loading
            val success = repository.exportDatabase(filePath)
            if (success) {
                _fileOpState.value = UiState.Success(FileOperationType.EXPORT)
            } else {
                _fileOpState.value = UiState.Error(Exception("Export failed"))
            }
        }
    }

    fun consumeFileOperation() {
        _fileOpState.value = null
    }
}
