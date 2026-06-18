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
        val upFolder = FileData("../", FileType.UP_FOLDER)
        val folder = FileData("MyFolder", FileType.FOLDER)
        val file = FileData("MyFile.txt", FileType.FILE)

        // UP_FOLDER should be smaller than FOLDER, and FOLDER smaller than FILE
        assertTrue(upFolder < folder)
        assertTrue(folder < file)
        assertTrue(upFolder < file)
    }

    @DisplayName("Compare To - Same File Type - Sorts Names Case Insensitively")
    @Test
    fun compareTo_sameFileType_sortsNamesCaseInsensitively() {
        val f1 = FileData("apple.txt", FileType.FILE)
        val f2 = FileData("Banana.txt", FileType.FILE)
        val f3 = FileData("cherry.txt", FileType.FILE)

        assertTrue(f1 < f2)
        assertTrue(f2 < f3)

        // Test exact case insensitivity
        val lowercaseFile = FileData("banana.txt", FileType.FILE)
        val uppercaseFile = FileData("BANANA.TXT", FileType.FILE)
        assertEquals(0, lowercaseFile.compareTo(uppercaseFile))
    }

    @DisplayName("Equals and Hash Code - Identical Files - Evaluates Equal and Computes Same Hash Code")
    @Test
    fun equalsAndHashCode_identicalFiles_evaluatesEqualAndComputesSameHashCode() {
        val file1 = FileData("text.txt", FileType.FILE)
        val file2 = FileData("text.txt", FileType.FILE)
        val file3 = FileData("text.txt", FileType.FOLDER)

        assertEquals(file1, file2)
        assertNotEquals(file1, file3)
        assertEquals(file1.hashCode(), file2.hashCode())
    }
}
