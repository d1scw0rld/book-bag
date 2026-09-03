package org.d1scw0rld.bookbag.viewmodel

import androidx.core.content.edit
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.d1scw0rld.bookbag.DisplayNameRobolectricRunner
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.whenever
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(DisplayNameRobolectricRunner::class)
@Config(sdk = [28])
class BookListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: BookRepository
    private lateinit var viewModel: BookListViewModel
    private lateinit var context: android.content.Context
    private lateinit var preferences: android.content.SharedPreferences
    private lateinit var permissionsManager: org.d1scw0rld.bookbag.util.PermissionsManager

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock(BookRepository::class.java)
        context = RuntimeEnvironment.getApplication()
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
        permissionsManager = mock(org.d1scw0rld.bookbag.util.PermissionsManager::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @DisplayName("Load Books - Repository Succeeds - Emits Success UI State With Book List")
    @Test
    fun loadBooks_repositorySucceeds_emitsSuccessUiStateWithBookList() = runTest(testDispatcher) {
        // Arrange
        val bookRelation = BookWithFields(
            book = BookEntity(id = TEST_BOOK_ID_1, title = TITLE_CLEAN_CODE, description = null, volume = null, publicationDate = null, pages = null, price = null, value = null, dueDate = null, readDate = null, edition = null, isbn = null, web = null),
            fields = emptyList()
        )
        val expectedBooks = listOf(bookRelation)
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(expectedBooks))

        // Act
        viewModel = BookListViewModel(repository, preferences, permissionsManager, context)

        // Assert
        assertTrue(viewModel.uiState.value is UiState.Success)
        val successData = (viewModel.uiState.value as UiState.Success).data
        assertEquals(EXPECTED_SUCCESS_DATA_SIZE_1, successData.size)
        assertEquals(TITLE_CLEAN_CODE, successData[0].book.title)
    }

    @DisplayName("Load Books - Repository Throws Exception - Emits Error UI State")
    @Test
    fun loadBooks_repositoryThrowsException_emitsErrorUiState() = runTest(testDispatcher) {
        // Arrange
        val expectedException = RuntimeException(ERROR_DB_MESSAGE)
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flow { throw expectedException })

        // Act
        viewModel = BookListViewModel(repository, preferences, permissionsManager, context)

        // Assert
        assertTrue(viewModel.uiState.value is UiState.Error)
        val errorException = (viewModel.uiState.value as UiState.Error).exception
        assertEquals(ERROR_DB_MESSAGE, errorException.message)
    }

    @DisplayName("Load Books - Reloaded After Import - Cancels Stale Collection So Dead Flow Cannot Overwrite State")
    @Test
    fun loadBooks_reloadedAfterImport_cancelsStaleCollectionSoDeadFlowCannotOverwriteState() = runTest(testDispatcher) {
        // Arrange: the first flow represents the pre-import database, the second the imported one.
        val staleFlow = MutableSharedFlow<List<BookWithFields>>()
        val importedFlow = MutableSharedFlow<List<BookWithFields>>()
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(staleFlow, importedFlow)

        viewModel = BookListViewModel(repository, preferences, permissionsManager, context)
        staleFlow.emit(listOf(bookRelation(TEST_BOOK_ID_1, TITLE_CLEAN_CODE)))
        assertEquals(TITLE_CLEAN_CODE, singleBookTitle())

        // Act: an import triggers a reload, which must abandon the stale collection.
        viewModel.loadBooks()
        importedFlow.emit(listOf(bookRelation(TEST_BOOK_ID_2, TITLE_REFACTORING)))
        assertEquals(TITLE_REFACTORING, singleBookTitle())

        // Assert: the stale flow is no longer collected and cannot clobber the imported state.
        assertEquals(NO_STALE_SUBSCRIBERS_MSG, 0, staleFlow.subscriptionCount.value)
        staleFlow.emit(listOf(bookRelation(TEST_BOOK_ID_1, TITLE_CLEAN_CODE)))
        assertEquals(STALE_MUST_NOT_OVERWRITE_MSG, TITLE_REFACTORING, singleBookTitle())
    }

    private fun bookRelation(id: Long, title: String) = BookWithFields(
        book = BookEntity(id = id, title = title, description = null, volume = null, publicationDate = null, pages = null, price = null, value = null, dueDate = null, readDate = null, edition = null, isbn = null, web = null),
        fields = emptyList()
    )

    private fun singleBookTitle(): String {
        val state = viewModel.uiState.value
        assertTrue(EXPECTED_SUCCESS_STATE_MSG, state is UiState.Success)
        return (state as UiState.Success).data.single().book.title
    }

    @DisplayName("Delete Book - Valid Book ID Provided - Invokes Repository Delete")
    @Test
    fun deleteBook_validBookIdProvided_invokesRepositoryDelete() = runTest(testDispatcher) {
        // Arrange
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        viewModel = BookListViewModel(repository, preferences, permissionsManager, context)
        viewModel.updateBookId(TEST_BOOK_ID_100)

        // Act
        viewModel.deleteBook()

        // Assert
        verify(repository).deleteBookAndRelations(TEST_BOOK_ID_100)
    }

    @DisplayName("Import Database - Import Succeeds - Updates File Op State With Success")
    @Test
    fun importDatabase_importSucceeds_updatesFileOpStateWithSuccess() = runTest(testDispatcher) {
        // Arrange
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        whenever(repository.importDatabase(FILE_PATH_IMPORT)).thenReturn(true)
        viewModel = BookListViewModel(repository, preferences, permissionsManager, context)

        // Act
        viewModel.importDatabase(FILE_PATH_IMPORT)

        // Assert
        assertTrue(viewModel.fileOpState.value is UiState.Success)
        assertEquals(FileOperationType.IMPORT, (viewModel.fileOpState.value as UiState.Success).data)
    }

    @DisplayName("Import Database - Import Fails - Updates File Op State With Error")
    @Test
    fun importDatabase_importFails_updatesFileOpStateWithError() = runTest(testDispatcher) {
        // Arrange
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        whenever(repository.importDatabase(FILE_PATH_IMPORT)).thenReturn(false)
        viewModel = BookListViewModel(repository, preferences, permissionsManager, context)

        // Act
        viewModel.importDatabase(FILE_PATH_IMPORT)

        // Assert
        assertTrue(viewModel.fileOpState.value is UiState.Error)
        assertEquals(ERROR_IMPORT_FAILED, (viewModel.fileOpState.value as UiState.Error).exception.message)
    }

    @DisplayName("Export Database - Export Succeeds - Updates File Op State With Success")
    @Test
    fun exportDatabase_exportSucceeds_updatesFileOpStateWithSuccess() = runTest(testDispatcher) {
        // Arrange
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        whenever(repository.exportDatabase(FILE_PATH_EXPORT)).thenReturn(true)
        viewModel = BookListViewModel(repository, preferences, permissionsManager, context)

        // Act
        viewModel.exportDatabase(FILE_PATH_EXPORT)

        // Assert
        assertTrue(viewModel.fileOpState.value is UiState.Success)
        assertEquals(FileOperationType.EXPORT, (viewModel.fileOpState.value as UiState.Success).data)
    }

    @DisplayName("Export Database - Export Fails - Updates File Op State With Error")
    @Test
    fun exportDatabase_exportFails_updatesFileOpStateWithError() = runTest(testDispatcher) {
        // Arrange
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        whenever(repository.exportDatabase(FILE_PATH_EXPORT)).thenReturn(false)
        viewModel = BookListViewModel(repository, preferences, permissionsManager, context)

        // Act
        viewModel.exportDatabase(FILE_PATH_EXPORT)

        // Assert
        assertTrue(viewModel.fileOpState.value is UiState.Error)
        assertEquals(ERROR_EXPORT_FAILED, (viewModel.fileOpState.value as UiState.Error).exception.message)
    }

    @DisplayName("Consume File Operation - Active File Op State - Resets File Op State to Null")
    @Test
    fun consumeFileOperation_activeFileOpState_resetsFileOpStateToNull() = runTest(testDispatcher) {
        // Arrange
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        whenever(repository.exportDatabase(FILE_PATH_EXPORT)).thenReturn(true)
        viewModel = BookListViewModel(repository, preferences, permissionsManager, context)

        viewModel.exportDatabase(FILE_PATH_EXPORT)

        // Act
        viewModel.consumeFileOperation()

        // Assert
        assertNull(viewModel.fileOpState.value)
    }

    @DisplayName("Get Export File Name - Default Database Name - Returns Formatted Filename With Timestamp")
    @Test
    fun getExportFileName_defaultDatabaseName_returnsFormattedFilenameWithTimestamp() {
        // Arrange
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        viewModel = BookListViewModel(repository, preferences, permissionsManager, context)

        // Act
        val fileName = viewModel.getExportFileName()

        // Assert
        // Format from strings.xml: "%s_%d%02d%02d%02d%02d.%s"
        // DbConstants.DATABASE_NAME is "book_bag.db"
        // Expected pattern: book_bag_YYYYMMDDHHMM.db
        val regex = Regex(PATTERN_FILE_NAME)
        assertTrue(ASSERT_MSG_FILENAME_MATCH, regex.matches(fileName))
    }

    @DisplayName("Update Order ID - Valid New ID - Updates State and Preferences and Reloads Books")
    @Test
    fun updateOrderId_validNewId_updatesStateAndPreferencesAndReloadsBooks() = runTest(testDispatcher) {
        // Arrange
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        viewModel = BookListViewModel(repository, preferences, permissionsManager, context)

        // Act
        viewModel.updateOrderId(TEST_ORDER_ID)

        // Assert
        assertEquals(TEST_ORDER_ID, viewModel.orderId.value)
        assertEquals(TEST_ORDER_ID, preferences.getInt(PREF_KEY_ORDER_ID, -1))
        verify(repository, atLeastOnce()).getAllBooksWithFieldsFlow()
    }

    @DisplayName("On Action Clicked - Permission Already Granted - Emits Permission Granted Event")
    @Test
    fun onActionClicked_permissionAlreadyGranted_emitsPermissionGrantedEvent() = runTest(testDispatcher) {
        // Arrange
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        whenever(permissionsManager.hasStoragePermission()).thenReturn(true)
        viewModel = BookListViewModel(repository, preferences, permissionsManager, context)
        
        val events = mutableListOf<PermissionEvent>()
        val job = launch { viewModel.permissionEvent.toList(events) }

        // Act
        viewModel.onActionClicked(PendingAction.IMPORT)

        // Assert
        assertEquals(PendingAction.IMPORT, viewModel.pendingAction.value)
        assertEquals(EXPECTED_EVENTS_SIZE_1, events.size)
        assertTrue(events[0] is PermissionEvent.PermissionGranted)
        job.cancel()
    }

    @DisplayName("On Action Clicked - Permission Not Granted - Emits Show Rationale Event")
    @Test
    fun onActionClicked_permissionNotGranted_emitsShowRationaleEvent() = runTest(testDispatcher) {
        // Arrange
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        whenever(permissionsManager.hasStoragePermission()).thenReturn(false)
        viewModel = BookListViewModel(repository, preferences, permissionsManager, context)
        
        val events = mutableListOf<PermissionEvent>()
        val job = launch { viewModel.permissionEvent.toList(events) }

        // Act
        viewModel.onActionClicked(PendingAction.EXPORT)

        // Assert
        assertEquals(PendingAction.EXPORT, viewModel.pendingAction.value)
        assertEquals(EXPECTED_EVENTS_SIZE_1, events.size)
        assertTrue(events[0] is PermissionEvent.ShowRationale)
        assertEquals(PendingAction.EXPORT, (events[0] as PermissionEvent.ShowRationale).action)
        job.cancel()
    }

    @DisplayName("On Permission Rationale Confirmed - Android R or Above - Emits Request Manage Storage")
    @Test
    fun onPermissionRationaleConfirmed_androidRorAbove_emitsRequestManageStorage() = runTest(testDispatcher) {
        // Arrange
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        whenever(permissionsManager.isAndroidRorAbove()).thenReturn(true)
        val mockIntent = mock(android.content.Intent::class.java)
        whenever(permissionsManager.getManageStorageIntent()).thenReturn(mockIntent)
        viewModel = BookListViewModel(repository, preferences, permissionsManager, context)
        
        val events = mutableListOf<PermissionEvent>()
        val job = launch { viewModel.permissionEvent.toList(events) }

        // Act
        viewModel.onPermissionRationaleConfirmed()

        // Assert
        assertEquals(EXPECTED_EVENTS_SIZE_1, events.size)
        assertTrue(events[0] is PermissionEvent.RequestManageStorage)
        assertEquals(mockIntent, (events[0] as PermissionEvent.RequestManageStorage).intent)
        job.cancel()
    }

    @DisplayName("On Permission Rationale Confirmed - Below Android R - Emits Request Legacy Permission")
    @Test
    fun onPermissionRationaleConfirmed_belowAndroidR_emitsRequestLegacyPermission() = runTest(testDispatcher) {
        // Arrange
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        whenever(permissionsManager.isAndroidRorAbove()).thenReturn(false)
        whenever(permissionsManager.getStoragePermissionRequest()).thenReturn(PERMISSION_READ_EXTERNAL_STORAGE)
        viewModel = BookListViewModel(repository, preferences, permissionsManager, context)
        
        val events = mutableListOf<PermissionEvent>()
        val job = launch { viewModel.permissionEvent.toList(events) }

        // Act
        viewModel.onPermissionRationaleConfirmed()

        // Assert
        assertEquals(EXPECTED_EVENTS_SIZE_1, events.size)
        assertTrue(events[0] is PermissionEvent.RequestLegacyPermission)
        assertEquals(PERMISSION_READ_EXTERNAL_STORAGE, (events[0] as PermissionEvent.RequestLegacyPermission).permission)
        job.cancel()
    }

    @DisplayName("On Manage Storage Result - Permission Granted - Emits Permission Granted Event")
    @Test
    fun onManageStorageResult_permissionGranted_emitsPermissionGrantedEvent() = runTest(testDispatcher) {
        // Arrange
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        whenever(permissionsManager.hasStoragePermission()).thenReturn(true)
        viewModel = BookListViewModel(repository, preferences, permissionsManager, context)
        
        val events = mutableListOf<PermissionEvent>()
        val job = launch { viewModel.permissionEvent.toList(events) }

        // Act
        viewModel.onManageStorageResult()

        // Assert
        assertEquals(EXPECTED_EVENTS_SIZE_1, events.size)
        assertTrue(events[0] is PermissionEvent.PermissionGranted)
        job.cancel()
    }

    @DisplayName("On Manage Storage Result - Permission Denied - Resets Pending Action")
    @Test
    fun onManageStorageResult_permissionDenied_resetsPendingAction() = runTest(testDispatcher) {
        // Arrange
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        whenever(permissionsManager.hasStoragePermission()).thenReturn(false)
        viewModel = BookListViewModel(repository, preferences, permissionsManager, context)
        viewModel.onActionClicked(PendingAction.IMPORT)
        
        // Act
        viewModel.onManageStorageResult()

        // Assert
        assertEquals(PendingAction.NONE, viewModel.pendingAction.value)
    }

    @DisplayName("Shared Preference Change Listener - Expand All Changed - Updates State Flow")
    @Test
    fun sharedPreferenceChangeListener_expandAllChanged_updatesStateFlow() {
        // Arrange
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        viewModel = BookListViewModel(repository, preferences, permissionsManager, context)
        
        // Act
        preferences.edit { putBoolean(PREF_KEY_EXPAND_ALL, true) }

        // Assert
        assertTrue(viewModel.isExpandAll.value)
    }

    @DisplayName("On Cleared - Listener Unregistered - No Longer Responds to Preference Changes")
    @Test
    fun onCleared_listenerUnregistered_noLongerRespondsToPreferenceChanges() {
        // Arrange
        val mockPreferences = mock(android.content.SharedPreferences::class.java)
        val mockEditor = mock(android.content.SharedPreferences.Editor::class.java)
        whenever(mockPreferences.edit()).thenReturn(mockEditor)
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        
        var capturedListener: android.content.SharedPreferences.OnSharedPreferenceChangeListener? = null
        whenever(mockPreferences.registerOnSharedPreferenceChangeListener(any())).thenAnswer {
            capturedListener = it.arguments[0] as android.content.SharedPreferences.OnSharedPreferenceChangeListener
            null
        }

        val viewModel = BookListViewModel(repository, mockPreferences, permissionsManager, context)
        
        // Act: Manually invoke onCleared via reflection
        val onClearedMethod = Class.forName(CLASS_VIEW_MODEL).getDeclaredMethod(METHOD_ON_CLEARED)
        onClearedMethod.isAccessible = true
        onClearedMethod.invoke(viewModel)

        // Assert
        verify(mockPreferences).unregisterOnSharedPreferenceChangeListener(capturedListener)
    }

    companion object {
        const val TEST_BOOK_ID_1 = 1L
        const val TEST_BOOK_ID_100 = 100L
        const val TEST_ORDER_ID = 5

        const val TITLE_CLEAN_CODE = "Clean Code"
        const val TITLE_REFACTORING = "Refactoring"

        const val TEST_BOOK_ID_2 = 2L

        const val EXPECTED_SUCCESS_STATE_MSG = "UI state should be Success"
        const val NO_STALE_SUBSCRIBERS_MSG = "Stale flow must have no collectors after reload"
        const val STALE_MUST_NOT_OVERWRITE_MSG = "Stale flow must not overwrite imported state"

        const val ERROR_DB_MESSAGE = "Database error"
        const val ERROR_IMPORT_FAILED = "Import failed"
        const val ERROR_EXPORT_FAILED = "Export failed"

        const val FILE_PATH_IMPORT = "/path/to/import.db"
        const val FILE_PATH_EXPORT = "/path/to/export.db"
        const val PATTERN_FILE_NAME = """book_bag_\d{12}\.db"""

        const val PREF_KEY_ORDER_ID = "order_id"
        const val PREF_KEY_EXPAND_ALL = "pref_expand_all"

        const val PERMISSION_READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE"

        const val CLASS_VIEW_MODEL = "androidx.lifecycle.ViewModel"
        const val METHOD_ON_CLEARED = "onCleared"

        const val EXPECTED_SUCCESS_DATA_SIZE_1 = 1
        const val EXPECTED_EVENTS_SIZE_1 = 1
        
        const val ASSERT_MSG_FILENAME_MATCH = "Filename should match pattern"
    }
}
