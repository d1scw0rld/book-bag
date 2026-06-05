package org.d1scw0rld.bookbag.fileselector

/**
 * Represents the type of a file system entry.
 */
enum class FileType {
    /** Constant that specifies the object is a reference to the parent */
    UP_FOLDER,
    /** Constant that specifies the object is a folder */
    FOLDER,
    /** Constant that specifies the object is a file */
    FILE
}

/**
 * This class contains information about the file name and type.
 */
data class FileData(
    val fileName: String,
    val fileType: FileType,
) : Comparable<FileData> {

    override fun compareTo(other: FileData): Int {
        val typeComparison = fileType.compareTo(other.fileType)
        if (typeComparison != 0) return typeComparison
        return fileName.compareTo(other.fileName, ignoreCase = true)
    }
}
