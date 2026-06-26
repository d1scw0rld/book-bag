package org.d1scw0rld.bookbag.viewmodel

import androidx.core.content.edit
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
            book = BookEntity(id = 1L, title = "Clean Code", description = null, volume = null, publicationDate = null, pages = null, price = null, value = null, dueDate = null, readDate = null, edition = null, isbn = null, web = null),
            fields = emptyList()
        )
        val expectedBooks = listOf(bookRelation)
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(expectedBooks))

        // Act
        viewModel = BookListViewModel(repository, preferences, context, permissionsManager)

        // Assert
        assertTrue(viewModel.uiState.value is UiState.Success)
        val successData = (viewModel.uiState.value as UiState.Success).data
        assertEquals(1, successData.size)
        assertEquals("Clean Code", successData[0].book.title)
    }

    @DisplayName("Load Books - Repository Throws Exception - Emits Error UI State")
    @Test
    fun loadBooks_repositoryThrowsException_emitsErrorUiState() = runTest(testDispatcher) {
        // Arrange
        val expectedException = RuntimeException("Database error")
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flow { throw expectedException })

        // Act
        viewModel = BookListViewModel(repository, preferences, context, permissionsManager)

        // Assert
        assertTrue(viewModel.uiState.value is UiState.Error)
        val errorException = (viewModel.uiState.value as UiState.Error).exception
        assertEquals("Database error", errorException.message)
    }

    @DisplayName("Delete Book - Valid Book ID Provided - Invokes Repository Delete")
    @Test
    fun deleteBook_validBookIdProvided_invokesRepositoryDelete() = runTest(testDispatcher) {
        // Arrange
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        viewModel = BookListViewModel(repository, preferences, context, permissionsManager)

        // Act
        viewModel.deleteBook(100L)

        // Assert
        verify(repository).deleteBookAndRelations(100L)
    }

    @DisplayName("Import Database - Import Succeeds - Updates File Op State With Success")
    @Test
    fun importDatabase_importSucceeds_updatesFileOpStateWithSuccess() = runTest(testDispatcher) {
        // Arrange
        val filePath = "/path/to/import.db"
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        whenever(repository.importDatabase(filePath)).thenReturn(true)
        viewModel = BookListViewModel(repository, preferences, context, permissionsManager)

        // Act
        viewModel.importDatabase(filePath)

        // Assert
        assertTrue(viewModel.fileOpState.value is UiState.Success)
        assertEquals(FileOperationType.IMPORT, (viewModel.fileOpState.value as UiState.Success).data)
    }

    @DisplayName("Import Database - Import Fails - Updates File Op State With Error")
    @Test
    fun importDatabase_importFails_updatesFileOpStateWithError() = runTest(testDispatcher) {
        // Arrange
        val filePath = "/path/to/import.db"
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        whenever(repository.importDatabase(filePath)).thenReturn(false)
        viewModel = BookListViewModel(repository, preferences, context, permissionsManager)

        // Act
        viewModel.importDatabase(filePath)

        // Assert
        assertTrue(viewModel.fileOpState.value is UiState.Error)
        assertEquals("Import failed", (viewModel.fileOpState.value as UiState.Error).exception.message)
    }

    @DisplayName("Export Database - Export Succeeds - Updates File Op State With Success")
    @Test
    fun exportDatabase_exportSucceeds_updatesFileOpStateWithSuccess() = runTest(testDispatcher) {
        // Arrange
        val filePath = "/path/to/export.db"
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        whenever(repository.exportDatabase(filePath)).thenReturn(true)
        viewModel = BookListViewModel(repository, preferences, context, permissionsManager)

        // Act
        viewModel.exportDatabase(filePath)

        // Assert
        assertTrue(viewModel.fileOpState.value is UiState.Success)
        assertEquals(FileOperationType.EXPORT, (viewModel.fileOpState.value as UiState.Success).data)
    }

    @DisplayName("Export Database - Export Fails - Updates File Op State With Error")
    @Test
    fun exportDatabase_exportFails_updatesFileOpStateWithError() = runTest(testDispatcher) {
        // Arrange
        val filePath = "/path/to/export.db"
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        whenever(repository.exportDatabase(filePath)).thenReturn(false)
        viewModel = BookListViewModel(repository, preferences, context, permissionsManager)

        // Act
        viewModel.exportDatabase(filePath)

        // Assert
        assertTrue(viewModel.fileOpState.value is UiState.Error)
        assertEquals("Export failed", (viewModel.fileOpState.value as UiState.Error).exception.message)
    }

    @DisplayName("Consume File Operation - Active File Op State - Resets File Op State to Null")
    @Test
    fun consumeFileOperation_activeFileOpState_resetsFileOpStateToNull() = runTest(testDispatcher) {
        // Arrange
        val filePath = "/path/to/export.db"
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        whenever(repository.exportDatabase(filePath)).thenReturn(true)
        viewModel = BookListViewModel(repository, preferences, context, permissionsManager)

        viewModel.exportDatabase(filePath)

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
        viewModel = BookListViewModel(repository, preferences, context, permissionsManager)

        // Act
        val fileName = viewModel.getExportFileName()

        // Assert
        // Format from strings.xml: "%s_%d%02d%02d%02d%02d.%s"
        // DbConstants.DATABASE_NAME is "book_bag.db"
        // Expected pattern: book_bag_YYYYMMDDHHMM.db
        val regex = Regex("""book_bag_\d{12}\.db""")
        assertTrue("Filename '$fileName' should match pattern", regex.matches(fileName))
    }

    @DisplayName("Update Order ID - Valid New ID - Updates State and Preferences and Reloads Books")
    @Test
    fun updateOrderId_validNewId_updatesStateAndPreferencesAndReloadsBooks() = runTest(testDispatcher) {
        // Arrange
        val newOrderId = 5
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        viewModel = BookListViewModel(repository, preferences, context, permissionsManager)

        // Act
        viewModel.updateOrderId(newOrderId)

        // Assert
        assertEquals(newOrderId, viewModel.orderId.value)
        assertEquals(newOrderId, preferences.getInt("order_id", -1))
        verify(repository, atLeastOnce()).getAllBooksWithFieldsFlow()
    }

    @DisplayName("On Action Clicked - Permission Already Granted - Emits Permission Granted Event")
    @Test
    fun onActionClicked_permissionAlreadyGranted_emitsPermissionGrantedEvent() = runTest(testDispatcher) {
        // Arrange
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        whenever(permissionsManager.hasStoragePermission()).thenReturn(true)
        viewModel = BookListViewModel(repository, preferences, context, permissionsManager)
        
        val events = mutableListOf<PermissionEvent>()
        val job = launch { viewModel.permissionEvent.toList(events) }

        // Act
        viewModel.onActionClicked(PendingAction.IMPORT)

        // Assert
        assertEquals(PendingAction.IMPORT, viewModel.pendingAction.value)
        assertEquals(1, events.size)
        assertTrue(events[0] is PermissionEvent.PermissionGranted)
        job.cancel()
    }

    @DisplayName("On Action Clicked - Permission Not Granted - Emits Show Rationale Event")
    @Test
    fun onActionClicked_permissionNotGranted_emitsShowRationaleEvent() = runTest(testDispatcher) {
        // Arrange
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        whenever(permissionsManager.hasStoragePermission()).thenReturn(false)
        viewModel = BookListViewModel(repository, preferences, context, permissionsManager)
        
        val events = mutableListOf<PermissionEvent>()
        val job = launch { viewModel.permissionEvent.toList(events) }

        // Act
        viewModel.onActionClicked(PendingAction.EXPORT)

        // Assert
        assertEquals(PendingAction.EXPORT, viewModel.pendingAction.value)
        assertEquals(1, events.size)
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
        viewModel = BookListViewModel(repository, preferences, context, permissionsManager)
        
        val events = mutableListOf<PermissionEvent>()
        val job = launch { viewModel.permissionEvent.toList(events) }

        // Act
        viewModel.onPermissionRationaleConfirmed()

        // Assert
        assertEquals(1, events.size)
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
        whenever(permissionsManager.getStoragePermissionRequest()).thenReturn("android.permission.READ_EXTERNAL_STORAGE")
        viewModel = BookListViewModel(repository, preferences, context, permissionsManager)
        
        val events = mutableListOf<PermissionEvent>()
        val job = launch { viewModel.permissionEvent.toList(events) }

        // Act
        viewModel.onPermissionRationaleConfirmed()

        // Assert
        assertEquals(1, events.size)
        assertTrue(events[0] is PermissionEvent.RequestLegacyPermission)
        assertEquals("android.permission.READ_EXTERNAL_STORAGE", (events[0] as PermissionEvent.RequestLegacyPermission).permission)
        job.cancel()
    }

    @DisplayName("On Manage Storage Result - Permission Granted - Emits Permission Granted Event")
    @Test
    fun onManageStorageResult_permissionGranted_emitsPermissionGrantedEvent() = runTest(testDispatcher) {
        // Arrange
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        whenever(permissionsManager.hasStoragePermission()).thenReturn(true)
        viewModel = BookListViewModel(repository, preferences, context, permissionsManager)
        
        val events = mutableListOf<PermissionEvent>()
        val job = launch { viewModel.permissionEvent.toList(events) }

        // Act
        viewModel.onManageStorageResult()

        // Assert
        assertEquals(1, events.size)
        assertTrue(events[0] is PermissionEvent.PermissionGranted)
        job.cancel()
    }

    @DisplayName("On Manage Storage Result - Permission Denied - Resets Pending Action")
    @Test
    fun onManageStorageResult_permissionDenied_resetsPendingAction() = runTest(testDispatcher) {
        // Arrange
        whenever(repository.getAllBooksWithFieldsFlow()).thenReturn(flowOf(emptyList()))
        whenever(permissionsManager.hasStoragePermission()).thenReturn(false)
        viewModel = BookListViewModel(repository, preferences, context, permissionsManager)
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
        viewModel = BookListViewModel(repository, preferences, context, permissionsManager)
        
        // Act
        preferences.edit { putBoolean("pref_expand_all", true) }

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

        val viewModel = BookListViewModel(repository, mockPreferences, context, permissionsManager)
        
        // Act: Manually invoke onCleared via reflection
        val onClearedMethod = Class.forName("androidx.lifecycle.ViewModel").getDeclaredMethod("onCleared")
        onClearedMethod.isAccessible = true
        onClearedMethod.invoke(viewModel)

        // Assert
        verify(mockPreferences).unregisterOnSharedPreferenceChangeListener(capturedListener)
    }
}
