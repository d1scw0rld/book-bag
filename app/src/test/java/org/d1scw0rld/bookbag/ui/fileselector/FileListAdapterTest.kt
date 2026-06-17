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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class FileListAdapterTest {

    private lateinit var context: Context
    private lateinit var files: List<FileData>
    private lateinit var adapter: FileListAdapter

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        files = listOf(
            FileData("../", FileType.UP_FOLDER),
            FileData("Documents", FileType.FOLDER),
            FileData("backup.db", FileType.FILE)
        )
        adapter = FileListAdapter(context, files)
    }

    @Test
    fun testAdapterBasicMethods() {
        assertEquals(3, adapter.count)
        assertEquals("Documents", adapter.getItem(1).fileName)
        assertEquals(1L, adapter.getItemId(1))
    }

    @Test
    fun testGetView_bindsCorrectDataAndIcons() {
        val parentView = LinearLayout(context)

        // View 0: UP_FOLDER -> R.drawable.ic_folder_open
        val view0 = adapter.getView(0, null, parentView)
        val nameTv0 = view0.findViewById<TextView>(R.id.tv_file_name)
        val typeIv0 = view0.findViewById<ImageView>(R.id.iv_file_type)
        assertEquals("../", nameTv0.text.toString())
        assertNotNull(typeIv0.drawable)

        // View 1: FOLDER -> R.drawable.ic_folder
        val view1 = adapter.getView(1, null, parentView)
        val nameTv1 = view1.findViewById<TextView>(R.id.tv_file_name)
        assertEquals("Documents", nameTv1.text.toString())

        // View 2: FILE -> R.drawable.ic_file
        val view2 = adapter.getView(2, null, parentView)
        val nameTv2 = view2.findViewById<TextView>(R.id.tv_file_name)
        assertEquals("backup.db", nameTv2.text.toString())
    }

    @Test
    fun testUpdateData_reloadsDataSet() {
        assertEquals(3, adapter.count)

        val newFiles = listOf(
            FileData("new_file.db", FileType.FILE)
        )
        adapter.updateData(newFiles)

        assertEquals(1, adapter.count)
        assertEquals("new_file.db", adapter.getItem(0).fileName)
    }
}
