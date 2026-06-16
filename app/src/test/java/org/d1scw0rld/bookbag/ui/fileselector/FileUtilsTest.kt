package org.d1scw0rld.bookbag.ui.fileselector

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FileUtilsTest {

    @Test
    fun `accept with FILTER_ALLOW_ALL returns true for any file`() {
        val file1 = File("test.db")
        val file2 = File("document.pdf")
        val file3 = File("no_extension")

        assertTrue(FileUtils.accept(file1, FileUtils.FILTER_ALLOW_ALL))
        assertTrue(FileUtils.accept(file2, FileUtils.FILTER_ALLOW_ALL))
        assertTrue(FileUtils.accept(file3, FileUtils.FILTER_ALLOW_ALL))
    }

    @Test
    fun `accept returns true if the file is a directory`() {
        val mockDirectory = File("my_folder")
        // We mock isDirectory or simulate a directory. Note: File("my_folder") isDirectory will be false
        // unless it exists on disk. But we can test with a temporary directory or let isDirectory resolve normally.
        val tempDir = File.createTempFile("test_dir", "")
        tempDir.delete()
        tempDir.mkdir()

        try {
            assertTrue(FileUtils.accept(tempDir, ".txt"))
        } finally {
            tempDir.delete()
        }
    }

    @Test
    fun `accept matches file extension correctly regardless of case`() {
        val file1 = File("database.db")
        val file2 = File("DATABASE.DB")
        val file3 = File("database.db.txt")

        assertTrue(FileUtils.accept(file1, ".db"))
        assertTrue(FileUtils.accept(file2, ".db"))
        assertFalse(FileUtils.accept(file3, ".db"))
    }

    @Test
    fun `accept returns false if file has no extension`() {
        val file = File("no_extension")
        assertFalse(FileUtils.accept(file, ".db"))
    }
}
