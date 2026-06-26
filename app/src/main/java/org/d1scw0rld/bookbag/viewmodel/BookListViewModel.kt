package org.d1scw0rld.bookbag.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.d1scw0rld.bookbag.R
import org.d1scw0rld.bookbag.data.DbConstants
import org.d1scw0rld.bookbag.data.relation.BookWithFields
import org.d1scw0rld.bookbag.data.repository.BookRepository
import org.d1scw0rld.bookbag.ui.state.UiState
import java.io.File
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

import org.d1scw0rld.bookbag.util.PermissionsManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import android.content.Intent
import android.util.Log

enum class FileOperationType {
    IMPORT, EXPORT
}

enum class PendingAction {
    NONE, IMPORT, EXPORT
}

sealed class PermissionEvent {
    data class ShowRationale(val action: PendingAction) : PermissionEvent()
    data class RequestLegacyPermission(val permission: String) : PermissionEvent()
    data class RequestManageStorage(val intent: Intent) : PermissionEvent()
    object PermissionGranted : PermissionEvent()
}

data class OrderItem(val id: Int, val title: String)

const val TAG = "BookListViewModel"

@HiltViewModel
class BookListViewModel @Inject constructor(
    private val repository: BookRepository,
    private val preferences: SharedPreferences,
    @ApplicationContext private val context: Context,
    private val permissionsManager: PermissionsManager
) : ViewModel() {

    companion object {
        private const val PREF_ORDER_ID = "order_id"
        private const val PREF_EXPAND_ALL = "pref_expand_all"
        private const val PREF_EXPORT_FOLDER = "pref_export_folder"
    }

    private val _uiState = MutableStateFlow<UiState<List<BookWithFields>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<BookWithFields>>> = _uiState.asStateFlow()

    private val _fileOpState = MutableStateFlow<UiState<FileOperationType>?>(null)
    val fileOpState: StateFlow<UiState<FileOperationType>?> = _fileOpState.asStateFlow()

    private val _permissionEvent = MutableSharedFlow<PermissionEvent>()
    val permissionEvent: SharedFlow<PermissionEvent> = _permissionEvent.asSharedFlow()

    private val _pendingAction = MutableStateFlow(PendingAction.NONE)
    val pendingAction: StateFlow<PendingAction> = _pendingAction.asStateFlow()

    private val _orderId = MutableStateFlow(preferences.getInt(PREF_ORDER_ID, DbConstants.SRT_TTL))
    val orderId: StateFlow<Int> = _orderId.asStateFlow()

    private val _isExpandAll = MutableStateFlow(preferences.getBoolean(PREF_EXPAND_ALL, false))
    val isExpandAll: StateFlow<Boolean> = _isExpandAll.asStateFlow()

    private val _exportFolderAbsPath = MutableStateFlow("")
    val exportFolderAbsPath: StateFlow<String> = _exportFolderAbsPath.asStateFlow()

    val orderItems = listOf(
        OrderItem(DbConstants.SRT_TTL, context.getString(R.string.srt_title)),
        OrderItem(DbConstants.SRT_AUT, context.getString(R.string.srt_author)),
        OrderItem(DbConstants.SRT_WNT_PBL_TTL, context.getString(R.string.srt_wanted_pbl_ttl)),
        OrderItem(DbConstants.SRT_WNT_PBL_AUT, context.getString(R.string.srt_wanted_pbl_aut)),
        OrderItem(DbConstants.SRT_RD_AUT, context.getString(R.string.srt_read_aut)),
        OrderItem(DbConstants.SRT_RD_TTL, context.getString(R.string.srt_read_ttl)),
        OrderItem(DbConstants.SRT_NOT_RD_AUT, context.getString(R.string.srt_not_read_aut)),
        OrderItem(DbConstants.SRT_NOT_RD_TTL, context.getString(R.string.srt_not_read_ttl)),
        OrderItem(DbConstants.SRT_PBL_AUT, context.getString(R.string.srt_pbl_aut)),
        OrderItem(DbConstants.SRT_PBL_TTL, context.getString(R.string.srt_pbl_ttl)),
        OrderItem(DbConstants.SRT_LND_TTL, context.getString(R.string.srt_lnd_ttl)),
        OrderItem(DbConstants.SRT_LND_BRW, context.getString(R.string.srt_lnd_brw))
    )

    private val onSharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
        when (key) {
            PREF_EXPAND_ALL -> _isExpandAll.value = prefs.getBoolean(PREF_EXPAND_ALL, false)
            PREF_EXPORT_FOLDER -> updateExportFolderPath(prefs.getString(PREF_EXPORT_FOLDER, context.getString(R.string.app_name)) ?: context.getString(R.string.app_name))
        }
    }

    init {
        preferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
        updateExportFolderPath(preferences.getString(PREF_EXPORT_FOLDER, context.getString(R.string.app_name)) ?: context.getString(R.string.app_name))
        loadBooks()
    }

    override fun onCleared() {
        super.onCleared()
        preferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
    }

    fun onActionClicked(action: PendingAction) {
        _pendingAction.value = action
        if (permissionsManager.hasStoragePermission()) {
            viewModelScope.launch {
                _permissionEvent.emit(PermissionEvent.PermissionGranted)
            }
        } else {
            viewModelScope.launch {
                _permissionEvent.emit(PermissionEvent.ShowRationale(action))
            }
        }
    }

    fun onPermissionRationaleConfirmed() {
        viewModelScope.launch {
            if (permissionsManager.isAndroidRorAbove()) {
                _permissionEvent.emit(PermissionEvent.RequestManageStorage(permissionsManager.getManageStorageIntent()))
            } else {
                _permissionEvent.emit(PermissionEvent.RequestLegacyPermission(permissionsManager.getStoragePermissionRequest()))
            }
        }
    }

    fun onPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            checkCreateExportFolder(_exportFolderAbsPath.value)
            viewModelScope.launch {
                _permissionEvent.emit(PermissionEvent.PermissionGranted)
            }
        } else {
            _pendingAction.value = PendingAction.NONE
        }
    }

    fun onManageStorageResult() {
        if (permissionsManager.hasStoragePermission()) {
            checkCreateExportFolder(_exportFolderAbsPath.value)
            viewModelScope.launch {
                _permissionEvent.emit(PermissionEvent.PermissionGranted)
            }
        } else {
            _pendingAction.value = PendingAction.NONE
        }
    }

    fun resetPendingAction() {
        _pendingAction.value = PendingAction.NONE
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

    fun updateOrderId(newOrderId: Int) {
        _orderId.value = newOrderId
        preferences.edit { putInt(PREF_ORDER_ID, newOrderId) }
        loadBooks()
    }

    fun getExportFileName(): String {
        val calendar = Calendar.getInstance(Locale.getDefault())
        val extIndex = DbConstants.DATABASE_NAME.lastIndexOf(".")
        return String.format(
            context.getString(R.string.fmt_fl_nm),
            DbConstants.DATABASE_NAME.substring(0, extIndex),
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            DbConstants.DATABASE_NAME.substring(extIndex + 1)
        )
    }

    private fun updateExportFolderPath(exportFolder: String) {
        @Suppress("DEPRECATION")
        _exportFolderAbsPath.value = Environment.getExternalStorageDirectory().toString() + File.separator + exportFolder + File.separator
        checkCreateExportFolder(_exportFolderAbsPath.value)
    }

    fun checkCreateExportFolder(path: String) {
        val file = File(path)
        if (!file.isDirectory) {
            if (!file.mkdirs()) {
                 Log.w(TAG, context.getString(R.string.log_err_create_export_folder, path))
            }
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
