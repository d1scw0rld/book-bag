package org.d1scw0rld.bookbag.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.d1scw0rld.bookbag.data.DbConstants
import org.d1scw0rld.bookbag.data.relation.toDto
import org.d1scw0rld.bookbag.data.repository.BookRepository
import org.d1scw0rld.bookbag.dto.Book
import org.d1scw0rld.bookbag.dto.Property
import org.d1scw0rld.bookbag.ui.state.UiState
import javax.inject.Inject

data class EditBookData(
    val book: Book,
    val propertiesMap: Map<Int, List<Property>>
)

@HiltViewModel
class EditBookViewModel @Inject constructor(
    private val repository: BookRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<EditBookData>>(UiState.Loading)
    val uiState: StateFlow<UiState<EditBookData>> = _uiState.asStateFlow()

    private val _saveSuccess = MutableSharedFlow<Boolean>()
    val saveSuccess: SharedFlow<Boolean> = _saveSuccess.asSharedFlow()

    // Keep reference to the working Book model so it survives configuration changes
    var book: Book = Book()
        private set

    fun loadBook(bookId: Long, isCopy: Boolean) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val loadedBookDeferred = async {
                    if (bookId != 0L) {
                        repository.getBookWithFields(bookId)?.toDto() ?: Book()
                    } else {
                        Book()
                    }
                }

                // Fetch property values asynchronously and concurrently
                val fieldIdsToFetch = DbConstants.FIELDS.map { it.id }.toMutableList().apply {
                    if (!contains(DbConstants.FLD_CURRENCY)) {
                        add(DbConstants.FLD_CURRENCY)
                    }
                }

                val propertiesDeferred = fieldIdsToFetch.map { fieldId ->
                    async {
                        fieldId to repository.getFieldsByType(fieldId).map {
                            Property(fieldTypeId = it.typeId, value = it.name, id = it.id)
                        }
                    }
                }

                val loadedBook = loadedBookDeferred.await()
                if (isCopy) {
                    loadedBook.id = 0
                }
                book = loadedBook

                val propertiesMap = propertiesDeferred.awaitAll().toMap()

                _uiState.value = UiState.Success(EditBookData(loadedBook, propertiesMap))
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e)
            }
        }
    }

    fun saveBook() {
        viewModelScope.launch {
            try {
                // Remove empty fields
                for (i in book.properties.indices.reversed()) {
                    if (book.properties[i].value.trim().isEmpty()) {
                        book.properties.removeAt(i)
                    }
                }
                repository.saveBookWithFields(book)
                _saveSuccess.emit(true)
            } catch (e: Exception) {
                _saveSuccess.emit(false)
            }
        }
    }
}
