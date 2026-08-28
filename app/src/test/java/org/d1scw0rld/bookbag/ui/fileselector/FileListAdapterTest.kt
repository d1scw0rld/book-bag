package org.d1scw0rld.bookbag.ui.fileselector

import android.content.Context
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import org.d1scw0rld.bookbag.R
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.d1scw0rld.bookbag.DisplayNameRobolectricRunner
import org.robolectric.annotation.Config

@RunWith(DisplayNameRobolectricRunner::class)
@Config(sdk = [28])
class FileListAdapterTest {

    private lateinit var context: Context
    private lateinit var files: List<FileData>
    private lateinit var adapter: FileListAdapter

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        files = listOf(
            FileData(UP_FOLDER_NAME, FileType.UP_FOLDER),
            FileData(FOLDER_NAME, FileType.FOLDER),
            FileData(FILE_NAME, FileType.FILE)
        )
        adapter = FileListAdapter(context, files)
    }

    @DisplayName("Basic Methods - Query Counts and Items - Returns Correct Sizes and IDs")
    @Test
    fun basicMethods_queryCountsAndItems_returnsCorrectSizesAndIds() {
        assertEquals(EXPECTED_COUNT_INITIAL, adapter.count)
        assertEquals(FOLDER_NAME, adapter.getItem(INDEX_1).fileName)
        assertEquals(EXPECTED_ID_1, adapter.getItemId(INDEX_1))
    }

    @DisplayName("Get View - Request Views at Indices - Binds Correct Data and Type Icons")
    @Test
    fun getView_requestViewsAtIndices_bindsCorrectDataAndTypeIcons() {
        val parentView = LinearLayout(context)

        // View 0: UP_FOLDER -> R.drawable.ic_folder_open
        val view0 = adapter.getView(INDEX_0, null, parentView)
        val nameTv0 = view0.findViewById<TextView>(R.id.tv_file_name)
        val typeIv0 = view0.findViewById<ImageView>(R.id.iv_file_type)
        assertEquals(UP_FOLDER_NAME, nameTv0.text.toString())
        assertNotNull(typeIv0.drawable)

        // View 1: FOLDER -> R.drawable.ic_folder
        val view1 = adapter.getView(INDEX_1, null, parentView)
        val nameTv1 = view1.findViewById<TextView>(R.id.tv_file_name)
        assertEquals(FOLDER_NAME, nameTv1.text.toString())

        // View 2: FILE -> R.drawable.ic_file
        val view2 = adapter.getView(INDEX_2, null, parentView)
        val nameTv2 = view2.findViewById<TextView>(R.id.tv_file_name)
        assertEquals(FILE_NAME, nameTv2.text.toString())
    }

    @DisplayName("Update Data - New Files List Provided - Updates Dataset Count and Data")
    @Test
    fun updateData_newFilesListProvided_updatesDatasetCountAndData() {
        assertEquals(EXPECTED_COUNT_INITIAL, adapter.count)

        val newFiles = listOf(
            FileData(NEW_FILE_NAME, FileType.FILE)
        )
        adapter.updateData(newFiles)

        assertEquals(EXPECTED_COUNT_NEW, adapter.count)
        assertEquals(NEW_FILE_NAME, adapter.getItem(INDEX_0).fileName)
    }

    companion object {
        const val UP_FOLDER_NAME = "../"
        const val FOLDER_NAME = "Documents"
        const val FILE_NAME = "backup.db"
        const val NEW_FILE_NAME = "new_file.db"

        const val EXPECTED_COUNT_INITIAL = 3
        const val EXPECTED_COUNT_NEW = 1
        const val EXPECTED_ID_1 = 1L

        const val INDEX_0 = 0
        const val INDEX_1 = 1
        const val INDEX_2 = 2
    }
}
