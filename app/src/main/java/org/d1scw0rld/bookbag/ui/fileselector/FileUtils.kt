package org.d1scw0rld.bookbag.ui.fileselector

import java.io.File
import java.util.Locale

/**
 * A set of tools for file operations
 */
object FileUtils {

    /** Filter which accepts every file */
    const val FILTER_ALLOW_ALL = "*.*"

    /**
     * This method checks that the file is accepted by the filter
     *
     * @param file
     *           - file that will be checked if there is a specific type
     * @param filter
     *           - criterion - the file type (for example ".jpg")
     * @return true - if file meets the criterion - false otherwise.
     */
    fun accept(file: File, filter: String): Boolean {
        if (filter == FILTER_ALLOW_ALL) {
            return true
        }
        if (file.isDirectory) {
            return true
        }
        val dotIndex = file.name.lastIndexOf('.')
        if (dotIndex == -1) {
            return false
        }
        val fileType = file.name
            .substring(dotIndex)
            .lowercase(Locale.getDefault())
        return fileType == filter
    }

}
