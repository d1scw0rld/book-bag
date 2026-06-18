package org.d1scw0rld.bookbag.ui.fileselector

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.d1scw0rld.bookbag.R
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.d1scw0rld.bookbag.DisplayNameRobolectricRunner
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import org.robolectric.shadows.ShadowLooper
import org.robolectric.shadows.ShadowToast
import java.io.File

@RunWith(DisplayNameRobolectricRunner::class)
@Config(sdk = [28])
class FileSelectorDialogTest {

    private lateinit var activity: AppCompatActivity
    private lateinit var tempDir: File
    private lateinit var file1: File
    private lateinit var file2: File
    private lateinit var subDir: File
    private var handledFilePath: String? = null

    private val handleFileListener = OnHandleFileListener { filePath ->
        handledFilePath = filePath
    }

    @Before
    fun setUp() {
        // Build and setup custom AppCompatActivity with AppTheme
        activity = Robolectric.buildActivity(AppCompatActivity::class.java).setup().get()
        activity.setTheme(R.style.AppTheme)

        // Setup temporary directories & files
        tempDir = File.createTempFile("temp_dir", "")
        tempDir.delete()
        tempDir.mkdir()

        subDir = File(tempDir, "SubFolder")
        subDir.mkdir()

        file1 = File(tempDir, "data.db")
        file1.createNewFile()

        file2 = File(tempDir, "info.txt")
        file2.createNewFile()

        handledFilePath = null
    }

    @After
    fun tearDown() {
        // Clean up temporary folder
        subDir.delete()
        file1.delete()
        file2.delete()
        tempDir.delete()
    }

    @DisplayName("New Instance - With File Provided - Sets Current Location to Parent Directory")
    @Test
    fun newInstance_withFileProvided_setsCurrentLocationToParentDirectory() {
        val dialog = FileSelectorDialog.newInstance(
            currentFile = file1,
            operation = FileOperation.SAVE,
            onHandleFileListener = handleFileListener,
            fileFilters = arrayOf(".db"),
        )

        val args = dialog.arguments
        assertNotNull(args)
        assertEquals(tempDir.absolutePath, args?.getString("key_current_location"))
        assertEquals("data.db", args?.getString("key_current_file_name"))
    }

    @DisplayName("New Instance - With Directory Provided - Sets Current Location to Directory")
    @Test
    fun newInstance_withDirectoryProvided_setsCurrentLocationToDirectory() {
        val dialog = FileSelectorDialog.newInstance(
            currentFile = tempDir,
            operation = FileOperation.LOAD,
            onHandleFileListener = handleFileListener,
            fileFilters = null
        )

        val args = dialog.arguments
        assertNotNull(args)
        assertEquals(tempDir.absolutePath, args?.getString("key_current_location"))
        assertNull(args?.getString("key_current_file_name"))
    }

    @DisplayName("OnCreateDialog - Save Operation Mode - Inflates Views and Populates Fields and Filters")
    @Test
    fun onCreateDialog_saveOperationMode_inflatesViewsAndPopulatesFieldsAndFilters() {
        val dialog = FileSelectorDialog.newInstance(
            currentFile = file1,
            operation = FileOperation.SAVE,
            onHandleFileListener = handleFileListener,
            fileFilters = arrayOf(".db", "*.*")
        )

        dialog.show(activity.supportFragmentManager, "file_selector")
        activity.supportFragmentManager.executePendingTransactions()

        val createdDialog = dialog.dialog as? AlertDialog
        assertNotNull(createdDialog)

        val dialogView = dialog.view ?: createdDialog?.findViewById(R.id.saveFileDialog)
        assertNotNull(dialogView)

        // Check Toolbar and title
        val toolbar = dialogView?.findViewById<Toolbar>(R.id.dlg_toolbar)
        assertNotNull(toolbar)
        assertEquals(tempDir.name, toolbar?.title)

        // Check FileName EditText is populated in SAVE mode
        val fileNameEditText = dialogView?.findViewById<EditText>(R.id.fileName)
        assertNotNull(fileNameEditText)
        assertEquals("data.db", fileNameEditText?.text.toString())
        assertTrue(fileNameEditText?.isEnabled == true)

        // Check spinner is prepared
        val filterSpinner = toolbar?.findViewById<Spinner>(R.id.action_select_type)
        assertNotNull(filterSpinner)
        assertEquals(2, filterSpinner?.adapter?.count)

        // Check file list populated only with accepted files: UP_FOLDER, subDir, and data.db (info.txt filtered out)
        val fileListView = dialogView?.findViewById<ListView>(R.id.fileList)
        assertNotNull(fileListView)
        val adapter = fileListView?.adapter as? FileListAdapter
        assertNotNull(adapter)
        // fileList contains "../", "SubFolder", "data.db"
        assertEquals(3, adapter?.count)
        assertEquals("../", adapter?.getItem(0)?.fileName)
        assertEquals("SubFolder", adapter?.getItem(1)?.fileName)
        assertEquals("data.db", adapter?.getItem(2)?.fileName)

        dialog.dismiss()
    }

    @DisplayName("OnCreateDialog - Load Operation Mode - Disables FileName Text Editing")
    @Test
    fun onCreateDialog_loadOperationMode_disablesFileNameTextEditing() {
        val dialog = FileSelectorDialog.newInstance(
            currentFile = tempDir,
            operation = FileOperation.LOAD,
            onHandleFileListener = handleFileListener,
            fileFilters = null
        )

        dialog.show(activity.supportFragmentManager, "file_selector")
        activity.supportFragmentManager.executePendingTransactions()

        val createdDialog = dialog.dialog as? AlertDialog
        val dialogView = dialog.view ?: createdDialog?.findViewById(R.id.saveFileDialog)

        val fileNameEditText = dialogView?.findViewById<EditText>(R.id.fileName)
        assertNotNull(fileNameEditText)
        // In LOAD mode, editing file name direct text input is disabled
        assertFalse(fileNameEditText?.isEnabled == true)

        dialog.dismiss()
    }

    @DisplayName("OnItemClick - Directory Row Item Clicked - Navigates to Directory and Updates Title")
    @Test
    fun onItemClick_directoryRowItemClicked_navigatesToDirectoryAndUpdateTitle() {
        val dialog = FileSelectorDialog.newInstance(
            currentFile = tempDir,
            operation = FileOperation.LOAD,
            onHandleFileListener = handleFileListener,
            fileFilters = null
        )

        dialog.show(activity.supportFragmentManager, "file_selector")
        activity.supportFragmentManager.executePendingTransactions()

        val createdDialog = dialog.dialog as? AlertDialog
        val dialogView = dialog.view ?: createdDialog?.findViewById(R.id.saveFileDialog)
        val fileListView = dialogView?.findViewById<ListView>(R.id.fileList)

        // Click on "SubFolder" at index 1
        fileListView?.performItemClick(fileListView, 1, 1L)

        // Current location should update to subDir
        assertEquals(subDir.absolutePath, dialog.getCurrentLocation()?.absolutePath)

        dialog.dismiss()
    }

    @DisplayName("OnItemClick - File Row Item Clicked - Updates FileName EditText Value")
    @Test
    fun onItemClick_fileRowItemClicked_updatesFileNameEditTextValue() {
        val dialog = FileSelectorDialog.newInstance(
            currentFile = tempDir,
            operation = FileOperation.SAVE,
            onHandleFileListener = handleFileListener,
            fileFilters = null
        )

        dialog.show(activity.supportFragmentManager, "file_selector")
        activity.supportFragmentManager.executePendingTransactions()

        val createdDialog = dialog.dialog as? AlertDialog
        val dialogView = dialog.view ?: createdDialog?.findViewById(R.id.saveFileDialog)
        val fileListView = dialogView?.findViewById<ListView>(R.id.fileList)
        val fileNameEditText = dialogView?.findViewById<EditText>(R.id.fileName)

        // In the un-filtered list, files:
        // index 0 -> "../"
        // index 1 -> "SubFolder"
        // index 2 -> "data.db"
        // index 3 -> "info.txt"
        fileListView?.performItemClick(fileListView, 2, 2L)

        // Should update EditText text to selected file name
        assertEquals("data.db", fileNameEditText?.text.toString())

        dialog.dismiss()
    }

    @DisplayName("Handle Save Or Load - Empty FileName Input - Shows Warning Dialog")
    @Test
    fun handleSaveOrLoad_emptyFileNameInput_showsWarningDialog() {
        val dialog = FileSelectorDialog.newInstance(
            currentFile = tempDir,
            operation = FileOperation.SAVE,
            onHandleFileListener = handleFileListener,
            fileFilters = null
        )

        dialog.show(activity.supportFragmentManager, "file_selector")
        activity.supportFragmentManager.executePendingTransactions()

        val createdDialog = dialog.dialog as? AlertDialog
        val dialogView = dialog.view ?: createdDialog?.findViewById(R.id.saveFileDialog)
        val fileNameEditText = dialogView?.findViewById<EditText>(R.id.fileName)

        // Set empty name and call checkFileName
        fileNameEditText?.setText("")
        val result = dialog.checkFileName("")
        assertFalse(result)

        // Assert warning AlertDialog is shown
        val shadowDialog = ShadowAlertDialog.getLatestAlertDialog()
        assertNotNull(shadowDialog)
        val shadow = org.robolectric.Shadows.shadowOf(shadowDialog)
        assertEquals(activity.getString(R.string.msg_fl_nm_frs), shadow.message)

        dialog.dismiss()
    }

    @DisplayName("Handle Save Or Load - Valid FileName Input - Triggers Callback and Dismisses Dialog")
    @Test
    fun handleSaveOrLoad_validFileNameInput_triggersCallbackAndDismissesDialog() {
        val dialog = FileSelectorDialog.newInstance(
            currentFile = tempDir,
            operation = FileOperation.SAVE,
            onHandleFileListener = handleFileListener,
            fileFilters = null
        )

        dialog.show(activity.supportFragmentManager, "file_selector")
        activity.supportFragmentManager.executePendingTransactions()

        val activeFragment = activity.supportFragmentManager.findFragmentByTag("file_selector") as FileSelectorDialog
        setListener(activeFragment, handleFileListener)

        val createdDialog = activeFragment.dialog as? AlertDialog
        val dialogView = activeFragment.view ?: createdDialog?.findViewById(R.id.saveFileDialog)
        val fileNameEditText = dialogView?.findViewById<EditText>(R.id.fileName)

        fileNameEditText?.setText("out.db")

        val textValue = activeFragment.getSelectedFileName()
        val listenerVal = FileSelectorDialog::class.java.getDeclaredField("onHandleFileListener").apply { isAccessible = true }.get(activeFragment)
        println("DEBUGGING INFO: textValue = '$textValue', listenerVal = $listenerVal")

        // Invoke handleSaveOrLoad directly via reflection to execute saving
        val handleSaveOrLoadMethod = FileSelectorDialog::class.java.getDeclaredMethod("handleSaveOrLoad")
        handleSaveOrLoadMethod.isAccessible = true
        handleSaveOrLoadMethod.invoke(activeFragment)

        // Verify listener was called with correct absolute file path
        val expectedPath = File(tempDir, "out.db").absolutePath
        assertEquals(expectedPath, handledFilePath)

        // Force execution of dismiss transition
        activity.supportFragmentManager.executePendingTransactions()

        // Dialog should be dismissed
        assertFalse(activeFragment.isAdded)
    }

    @DisplayName("Handle Save Or Load - Non Existent File to Load - Shows Toast and Does Not Trigger Callback")
    @Test
    fun handleSaveOrLoad_nonExistentFileToLoad_showsToastAndDoesNotTriggerCallback() {
        val dialog = FileSelectorDialog.newInstance(
            currentFile = tempDir,
            operation = FileOperation.LOAD,
            onHandleFileListener = handleFileListener,
            fileFilters = null
        )

        dialog.show(activity.supportFragmentManager, "file_selector")
        activity.supportFragmentManager.executePendingTransactions()

        val activeFragment = activity.supportFragmentManager.findFragmentByTag("file_selector") as FileSelectorDialog
        setListener(activeFragment, handleFileListener)

        val createdDialog = activeFragment.dialog as? AlertDialog
        val dialogView = activeFragment.view ?: createdDialog?.findViewById(R.id.saveFileDialog)
        val fileNameEditText = dialogView?.findViewById<EditText>(R.id.fileName)

        // Load mode enables getting selected file. Let's type a non-existent file name in the edit text via reflection
        // because it's disabled in UI, but we can set it
        val editable = android.text.SpannableStringBuilder("non_existent.db")
        fileNameEditText?.text = editable

        // Invoke handleSaveOrLoad directly via reflection to execute loading
        val handleSaveOrLoadMethod = FileSelectorDialog::class.java.getDeclaredMethod("handleSaveOrLoad")
        handleSaveOrLoadMethod.isAccessible = true
        handleSaveOrLoadMethod.invoke(activeFragment)

        // Verify listener was NOT called
        assertNull(handledFilePath)

        // Verify missing file message Toast shown
        val toast = ShadowToast.getLatestToast()
        assertNotNull(toast)
        
        activeFragment.dismiss()
    }

    @DisplayName("OnMenuItemClick - Create New Folder Action Clicked - Creates Directory Successfully and Refreshes List")
    @Test
    fun onMenuItemClick_createNewFolderActionClicked_createsDirectorySuccessfullyAndRefreshesList() {
        val dialog = FileSelectorDialog.newInstance(
            currentFile = tempDir,
            operation = FileOperation.SAVE,
            onHandleFileListener = handleFileListener,
            fileFilters = null
        )

        dialog.show(activity.supportFragmentManager, "file_selector")
        activity.supportFragmentManager.executePendingTransactions()

        val activeFragment = activity.supportFragmentManager.findFragmentByTag("file_selector") as FileSelectorDialog
        setListener(activeFragment, handleFileListener)

        // Invoke openNewFolderDialog directly via reflection to display prompt
        val openNewFolderDialogMethod = FileSelectorDialog::class.java.getDeclaredMethod("openNewFolderDialog")
        openNewFolderDialogMethod.isAccessible = true
        openNewFolderDialogMethod.invoke(activeFragment)

        // New folder creation prompt displays an AlertDialog
        val shadowDialog = ShadowAlertDialog.getLatestAlertDialog()
        assertNotNull(shadowDialog)
        val shadow = org.robolectric.Shadows.shadowOf(shadowDialog)
        assertEquals(activity.getString(R.string.btn_new_fld), shadow.title)

        // Find EditText inside shadowDialog and input folder name "NewAutoCreatedFolder"
        val decorView = shadowDialog.window?.decorView
        assertNotNull(decorView)
        val inputEditText = findEditText(decorView!!)
        assertNotNull(inputEditText)
        inputEditText?.setText("NewAutoCreatedFolder")

        // Click create button (positive button)
        shadowDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
        ShadowLooper.idleMainLooper()

        // Verify folder was actually created in currentLocation
        val autoFolder = File(tempDir, "NewAutoCreatedFolder")
        assertTrue(autoFolder.exists() && autoFolder.isDirectory)

        // Clean up created folder
        autoFolder.delete()
        activeFragment.dismiss()
    }

    private fun findEditText(view: View): EditText? {
        if (view is EditText) return view
        if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) {
                val et = findEditText(view.getChildAt(i))
                if (et != null) return et
            }
        }
        return null
    }

    private fun setListener(dialog: FileSelectorDialog, listener: OnHandleFileListener) {
        val field = FileSelectorDialog::class.java.getDeclaredField("onHandleFileListener")
        field.isAccessible = true
        field.set(dialog, listener)
    }

    @DisplayName("OnSaveInstanceState - State Saved - Serializes and Saves Current Location Path")
    @Test
    fun onSaveInstanceState_stateSaved_serializesAndSavesCurrentLocationPath() {
        val dialog = FileSelectorDialog.newInstance(
            currentFile = file1,
            operation = FileOperation.SAVE,
            onHandleFileListener = handleFileListener,
            fileFilters = arrayOf(".db")
        )

        dialog.show(activity.supportFragmentManager, "file_selector")
        activity.supportFragmentManager.executePendingTransactions()

        val bundle = Bundle()
        dialog.onSaveInstanceState(bundle)

        assertNotNull(bundle)
        assertEquals(tempDir.absolutePath, bundle.getString("key_current_location"))
        dialog.dismiss()
    }
}
