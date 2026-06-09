package org.d1scw0rld.bookbag.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.d1scw0rld.bookbag.data.DbConstants
import org.d1scw0rld.bookbag.data.relation.BookWithFields
import org.d1scw0rld.bookbag.data.repository.BookRepository
import org.d1scw0rld.bookbag.dto.Property
import org.d1scw0rld.bookbag.ui.state.UiState
import javax.inject.Inject

data class BookDetailData(
    val bookWithFields: BookWithFields?,
    val currencies: List<Property>
)

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val repository: BookRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<BookDetailData>>(UiState.Loading)
    val uiState: StateFlow<UiState<BookDetailData>> = _uiState.asStateFlow()

    fun loadBook(bookId: Long) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                // Fetch currencies asynchronously off the UI thread
                val currencyEntities = repository.getFieldsByType(DbConstants.FLD_CURRENCY)
                val currencies = currencyEntities.map {
                    Property(fieldTypeId = it.typeId, value = it.name, id = it.id)
                }

                repository.getBookWithFieldsFlow(bookId)
                    .catch { e -> _uiState.value = UiState.Error(e) }
                    .collect { book ->
                        _uiState.value = UiState.Success(BookDetailData(book, currencies))
                    }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e)
            }
        }
    }
}
