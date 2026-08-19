package org.d1scw0rld.bookbag.ui.fileselector

import org.junit.Assert.*
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.d1scw0rld.bookbag.DisplayNameRunner

@RunWith(DisplayNameRunner::class)
class FileDataTest {

    @DisplayName("Compare To - Different File Types - Sorts Up Folder Before Folders Before Files")
    @Test
    fun compareTo_differentFileTypes_sortsUpFolderBeforeFoldersBeforeFiles() {
        val upFolder = FileData(UP_FOLDER_NAME, FileType.UP_FOLDER)
        val folder = FileData(FOLDER_NAME, FileType.FOLDER)
        val file = FileData(FILE_NAME, FileType.FILE)

        // UP_FOLDER should be smaller than FOLDER, and FOLDER smaller than FILE
        assertTrue(upFolder < folder)
        assertTrue(folder < file)
        assertTrue(upFolder < file)
    }

    @DisplayName("Compare To - Same File Type - Sorts Names Case Insensitively")
    @Test
    fun compareTo_sameFileType_sortsNamesCaseInsensitively() {
        val f1 = FileData(FILE_APPLE, FileType.FILE)
        val f2 = FileData(FILE_BANANA, FileType.FILE)
        val f3 = FileData(FILE_CHERRY, FileType.FILE)

        assertTrue(f1 < f2)
        assertTrue(f2 < f3)

        // Test exact case insensitivity
        val lowercaseFile = FileData(FILE_BANANA_LOWER, FileType.FILE)
        val uppercaseFile = FileData(FILE_BANANA_UPPER, FileType.FILE)
        assertEquals(0, lowercaseFile.compareTo(uppercaseFile))
    }

    @DisplayName("Equals and Hash Code - Identical Files - Evaluates Equal and Computes Same Hash Code")
    @Test
    fun equalsAndHashCode_identicalFiles_evaluatesEqualAndComputesSameHashCode() {
        val file1 = FileData(FILE_TEXT, FileType.FILE)
        val file2 = FileData(FILE_TEXT, FileType.FILE)
        val file3 = FileData(FILE_TEXT, FileType.FOLDER)

        assertEquals(file1, file2)
        assertNotEquals(file1, file3)
        assertEquals(file1.hashCode(), file2.hashCode())
    }

    companion object {
        const val UP_FOLDER_NAME = "../"
        const val FOLDER_NAME = "MyFolder"
        const val FILE_NAME = "MyFile.txt"
        const val FILE_APPLE = "apple.txt"
        const val FILE_BANANA = "Banana.txt"
        const val FILE_CHERRY = "cherry.txt"
        const val FILE_BANANA_LOWER = "banana.txt"
        const val FILE_BANANA_UPPER = "BANANA.TXT"
        const val FILE_TEXT = "text.txt"
    }
}
