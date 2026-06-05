package org.d1scw0rld.bookbag.dto

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

object FileUtils {
    @Throws(IOException::class)
    fun copyFile(fromFile: FileInputStream, toFile: FileOutputStream) {
        fromFile.channel.use { fromChannel ->
            toFile.channel.use { toChannel ->
                fromChannel.transferTo(0, fromChannel.size(), toChannel)
            }
        }
    }
}
