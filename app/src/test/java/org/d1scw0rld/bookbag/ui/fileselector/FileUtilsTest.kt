package org.d1scw0rld.bookbag.ui.fileselector

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.d1scw0rld.bookbag.DisplayNameRunner
import java.io.File

@RunWith(DisplayNameRunner::class)
class FileUtilsTest {

    @DisplayName("Accept - Filter Allow All And Any File - Returns True")
    @Test
    fun accept_filterAllowAllAndAnyFile_returnsTrue() {
        val file1 = File(FILE_TEST_DB)
        val file2 = File(FILE_DOCUMENT_PDF)
        val file3 = File(FILE_NO_EXTENSION)

        assertTrue(FileUtils.accept(file1, FileUtils.FILTER_ALLOW_ALL))
        assertTrue(FileUtils.accept(file2, FileUtils.FILTER_ALLOW_ALL))
        assertTrue(FileUtils.accept(file3, FileUtils.FILTER_ALLOW_ALL))
    }

    @DisplayName("Accept - Directory File Passed - Returns True")
    @Test
    fun accept_directoryFilePassed_returnsTrue() {
        // We mock isDirectory or simulate a directory. Note: File("my_folder") isDirectory will be false
        // unless it exists on disk. But we can test with a temporary directory or let isDirectory resolve normally.
        val tempDir = File.createTempFile(TEMP_DIR_PREFIX, TEMP_DIR_SUFFIX)
        tempDir.delete()
        tempDir.mkdir()

        try {
            assertTrue(FileUtils.accept(tempDir, FILTER_TXT))
        } finally {
            tempDir.delete()
        }
    }

    @DisplayName("Accept - Matching File Extension With Different Cases - Returns True")
    @Test
    fun accept_matchingFileExtensionWithDifferentCases_returnsTrue() {
        val file1 = File(FILE_DATABASE_DB_LOWER)
        val file2 = File(FILE_DATABASE_DB_UPPER)
        val file3 = File(FILE_DATABASE_DB_TXT)

        assertTrue(FileUtils.accept(file1, FILTER_DB))
        assertTrue(FileUtils.accept(file2, FILTER_DB))
        assertFalse(FileUtils.accept(file3, FILTER_DB))
    }

    @DisplayName("Accept - File With No Extension And Specific Extension Filter - Returns False")
    @Test
    fun accept_fileWithNoExtensionAndSpecificExtensionFilter_returnsFalse() {
        val file = File(FILE_NO_EXTENSION)
        assertFalse(FileUtils.accept(file, FILTER_DB))
    }

    companion object {
        const val FILE_TEST_DB = "test.db"
        const val FILE_DOCUMENT_PDF = "document.pdf"
        const val FILE_NO_EXTENSION = "no_extension"

        const val TEMP_DIR_PREFIX = "test_dir"
        const val TEMP_DIR_SUFFIX = ""
        const val FILTER_TXT = ".txt"

        const val FILE_DATABASE_DB_LOWER = "database.db"
        const val FILE_DATABASE_DB_UPPER = "DATABASE.DB"
        const val FILE_DATABASE_DB_TXT = "database.db.txt"
        const val FILTER_DB = ".db"
    }
}
