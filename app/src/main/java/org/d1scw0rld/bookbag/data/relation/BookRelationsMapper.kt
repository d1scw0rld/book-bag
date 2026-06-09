package org.d1scw0rld.bookbag.data.relation

import org.d1scw0rld.bookbag.data.DbConstants
import org.d1scw0rld.bookbag.dto.BookResult
import org.d1scw0rld.bookbag.dto.ParentResult
import java.util.Locale

object BookRelationsMapper {

    private const val MISSING = "(missing)"

    /**
     * Highly optimized, pure Kotlin-native mapping and grouping implementation (Option B).
     * Replaces legacy SQL aggregations with clean, type-safe, and locale-safe collections processing.
     */
    fun mapBooksToParents(allBooks: List<BookWithFields>, order: Int): ArrayList<ParentResult> {
        val parentResults = ArrayList<ParentResult>()

        when (order) {
            DbConstants.SRT_TTL -> {
                // Group by first letter of title
                val grouped = allBooks.groupBy { 
                    it.book.title.take(1).uppercase(Locale.getDefault()) 
                }
                for ((parent, list) in grouped.entries.sortedBy { it.key }) {
                    val childList = ArrayList<BookResult>()
                    val sortedList = list.sortedBy { it.book.title }
                    for (relation in sortedList) {
                        val authors = relation.fields.filter { it.typeId == DbConstants.FLD_AUTHOR }.joinToString(", ") { it.name }
                        val content = if (authors.isNotEmpty()) "${relation.book.title} - $authors" else relation.book.title
                        childList.add(BookResult(relation.book.id, content))
                    }
                    parentResults.add(ParentResult(parent, childList))
                }
            }

            DbConstants.SRT_AUT -> {
                // Group by author name
                val authorMap = HashMap<String, ArrayList<BookWithFields>>()
                for (relation in allBooks) {
                    val authors = relation.fields.filter { it.typeId == DbConstants.FLD_AUTHOR }
                    if (authors.isEmpty()) {
                        authorMap.getOrPut(MISSING) { ArrayList() }.add(relation)
                    } else {
                        for (author in authors) {
                            authorMap.getOrPut(author.name) { ArrayList() }.add(relation)
                        }
                    }
                }
                for ((authorName, list) in authorMap.entries.sortedBy { it.key }) {
                    val childList = ArrayList<BookResult>()
                    val sortedList = list.sortedBy { it.book.title }
                    for (relation in sortedList) {
                        childList.add(BookResult(relation.book.id, relation.book.title))
                    }
                    parentResults.add(ParentResult(authorName, childList))
                }
            }

            DbConstants.SRT_WNT_PBL_AUT -> {
                // Wanted books sorted/grouped by Status
                val filtered = allBooks.filter { relation ->
                    val status = relation.fields.firstOrNull { it.typeId == DbConstants.FLD_STATUS }?.name.orEmpty()
                    !status.equals("In Bag", ignoreCase = true) && !status.equals("Read", ignoreCase = true)
                }
                val grouped = filtered.groupBy { relation ->
                    relation.fields.firstOrNull { it.typeId == DbConstants.FLD_STATUS }?.name ?: MISSING
                }
                for ((status, list) in grouped.entries.sortedBy { it.key }) {
                    val childList = ArrayList<BookResult>()
                    val sortedList = list.sortedWith(compareBy({ relation ->
                        relation.fields.filter { it.typeId == DbConstants.FLD_AUTHOR }.joinToString(", ") { it.name }
                    }, { it.book.title }))
                    for (relation in sortedList) {
                        val authors = relation.fields.filter { it.typeId == DbConstants.FLD_AUTHOR }.joinToString(", ") { it.name }
                        val content = if (authors.isNotEmpty()) "$authors - ${relation.book.title}" else relation.book.title
                        childList.add(BookResult(relation.book.id, content))
                    }
                    parentResults.add(ParentResult(status, childList))
                }
            }

            DbConstants.SRT_WNT_PBL_TTL -> {
                // Wanted books sorted/grouped by Status, children sorted by Title
                val filtered = allBooks.filter { relation ->
                    val status = relation.fields.firstOrNull { it.typeId == DbConstants.FLD_STATUS }?.name.orEmpty()
                    !status.equals("In Bag", ignoreCase = true) && !status.equals("Read", ignoreCase = true)
                }
                val grouped = filtered.groupBy { relation ->
                    relation.fields.firstOrNull { it.typeId == DbConstants.FLD_STATUS }?.name ?: MISSING
                }
                for ((status, list) in grouped.entries.sortedBy { it.key }) {
                    val childList = ArrayList<BookResult>()
                    val sortedList = list.sortedBy { it.book.title }
                    for (relation in sortedList) {
                        val authors = relation.fields.filter { it.typeId == DbConstants.FLD_AUTHOR }.joinToString(", ") { it.name }
                        val content = if (authors.isNotEmpty()) "${relation.book.title} - $authors" else relation.book.title
                        childList.add(BookResult(relation.book.id, content))
                    }
                    parentResults.add(ParentResult(status, childList))
                }
            }

            DbConstants.SRT_RD_AUT -> {
                // Read books grouped by Read Date (Year)
                val filtered = allBooks.filter { relation ->
                    val read = relation.fields.firstOrNull { it.typeId == DbConstants.FLD_READ }?.name.orEmpty()
                    read.equals("true", ignoreCase = true)
                }
                val grouped = filtered.groupBy { relation ->
                    val readDate = relation.book.readDate ?: 0
                    if (readDate != 0) (readDate / 10000).toString() else MISSING
                }
                for ((year, list) in grouped.entries.sortedByDescending { it.key }) {
                    val childList = ArrayList<BookResult>()
                    val sortedList = list.sortedWith(compareBy({ relation ->
                        relation.fields.filter { it.typeId == DbConstants.FLD_AUTHOR }.joinToString(", ") { it.name }
                    }, { it.book.title }))
                    for (relation in sortedList) {
                        val authors = relation.fields.filter { it.typeId == DbConstants.FLD_AUTHOR }.joinToString(", ") { it.name }
                        val content = if (authors.isNotEmpty()) "$authors - ${relation.book.title}" else relation.book.title
                        childList.add(BookResult(relation.book.id, content))
                    }
                    parentResults.add(ParentResult(year, childList))
                }
            }

            DbConstants.SRT_NOT_RD_AUT -> {
                // Unread books grouped by Format
                val filtered = allBooks.filter { relation ->
                    val read = relation.fields.firstOrNull { it.typeId == DbConstants.FLD_READ }?.name.orEmpty()
                    !read.equals("true", ignoreCase = true)
                }
                val grouped = filtered.groupBy { relation ->
                    relation.fields.firstOrNull { it.typeId == DbConstants.FLD_FORMAT }?.name ?: MISSING
                }
                for ((format, list) in grouped.entries.sortedBy { it.key }) {
                    val childList = ArrayList<BookResult>()
                    val sortedList = list.sortedWith(compareBy({ relation ->
                        relation.fields.filter { it.typeId == DbConstants.FLD_AUTHOR }.joinToString(", ") { it.name }
                    }, { it.book.title }))
                    for (relation in sortedList) {
                        val authors = relation.fields.filter { it.typeId == DbConstants.FLD_AUTHOR }.joinToString(", ") { it.name }
                        val content = if (authors.isNotEmpty()) "$authors - ${relation.book.title}" else relation.book.title
                        childList.add(BookResult(relation.book.id, content))
                    }
                    parentResults.add(ParentResult(format, childList))
                }
            }

            DbConstants.SRT_NOT_RD_TTL -> {
                // Unread books grouped by Format, sorted by Title
                val filtered = allBooks.filter { relation ->
                    val read = relation.fields.firstOrNull { it.typeId == DbConstants.FLD_READ }?.name.orEmpty()
                    !read.equals("true", ignoreCase = true)
                }
                val grouped = filtered.groupBy { relation ->
                    relation.fields.firstOrNull { it.typeId == DbConstants.FLD_FORMAT }?.name ?: MISSING
                }
                for ((format, list) in grouped.entries.sortedBy { it.key }) {
                    val childList = ArrayList<BookResult>()
                    val sortedList = list.sortedBy { it.book.title }
                    for (relation in sortedList) {
                        val authors = relation.fields.filter { it.typeId == DbConstants.FLD_AUTHOR }.joinToString(", ") { it.name }
                        val content = if (authors.isNotEmpty()) "${relation.book.title} - $authors" else relation.book.title
                        childList.add(BookResult(relation.book.id, content))
                    }
                    parentResults.add(ParentResult(format, childList))
                }
            }

            DbConstants.SRT_RD_TTL -> {
                // Read books grouped by Read Date (Year), sorted by Title
                val filtered = allBooks.filter { relation ->
                    val read = relation.fields.firstOrNull { it.typeId == DbConstants.FLD_READ }?.name.orEmpty()
                    read.equals("true", ignoreCase = true)
                }
                val grouped = filtered.groupBy { relation ->
                    val readDate = relation.book.readDate ?: 0
                    if (readDate != 0) (readDate / 10000).toString() else MISSING
                }
                for ((year, list) in grouped.entries.sortedByDescending { it.key }) {
                    val childList = ArrayList<BookResult>()
                    val sortedList = list.sortedBy { it.book.title }
                    for (relation in sortedList) {
                        val authors = relation.fields.filter { it.typeId == DbConstants.FLD_AUTHOR }.joinToString(", ") { it.name }
                        val content = if (authors.isNotEmpty()) "${relation.book.title} - $authors" else relation.book.title
                        childList.add(BookResult(relation.book.id, content))
                    }
                    parentResults.add(ParentResult(year, childList))
                }
            }

            DbConstants.SRT_PBL_AUT -> {
                // Grouped by Publisher
                val grouped = allBooks.groupBy { relation ->
                    relation.fields.firstOrNull { it.typeId == DbConstants.FLD_PUBLISHER }?.name ?: MISSING
                }
                for ((publisher, list) in grouped.entries.sortedBy { it.key }) {
                    val childList = ArrayList<BookResult>()
                    val sortedList = list.sortedWith(compareBy({ relation ->
                        relation.fields.filter { it.typeId == DbConstants.FLD_AUTHOR }.joinToString(", ") { it.name }
                    }, { it.book.title }))
                    for (relation in sortedList) {
                        val authors = relation.fields.filter { it.typeId == DbConstants.FLD_AUTHOR }.joinToString(", ") { it.name }
                        val content = if (authors.isNotEmpty()) "$authors - ${relation.book.title}" else relation.book.title
                        childList.add(BookResult(relation.book.id, content))
                    }
                    parentResults.add(ParentResult(publisher, childList))
                }
            }

            DbConstants.SRT_PBL_TTL -> {
                // Grouped by Publisher, sorted by Title
                val grouped = allBooks.groupBy { relation ->
                    relation.fields.firstOrNull { it.typeId == DbConstants.FLD_PUBLISHER }?.name ?: MISSING
                }
                for ((publisher, list) in grouped.entries.sortedBy { it.key }) {
                    val childList = ArrayList<BookResult>()
                    val sortedList = list.sortedBy { it.book.title }
                    for (relation in sortedList) {
                        val authors = relation.fields.filter { it.typeId == DbConstants.FLD_AUTHOR }.joinToString(", ") { it.name }
                        val content = if (authors.isNotEmpty()) "${relation.book.title} - $authors" else relation.book.title
                        childList.add(BookResult(relation.book.id, content))
                    }
                    parentResults.add(ParentResult(publisher, childList))
                }
            }

            DbConstants.SRT_LND_TTL -> {
                // Grouped by Loaned To, sorted by Title
                val grouped = allBooks.groupBy { relation ->
                    relation.fields.firstOrNull { it.typeId == DbConstants.FLD_LOANED_TO }?.name ?: MISSING
                }
                for ((loanedTo, list) in grouped.entries.sortedBy { it.key }) {
                    val childList = ArrayList<BookResult>()
                    val sortedList = list.sortedBy { it.book.title }
                    for (relation in sortedList) {
                        val authors = relation.fields.filter { it.typeId == DbConstants.FLD_AUTHOR }.joinToString(", ") { it.name }
                        val content = if (authors.isNotEmpty()) "${relation.book.title} - $authors" else relation.book.title
                        childList.add(BookResult(relation.book.id, content))
                    }
                    parentResults.add(ParentResult(loanedTo, childList))
                }
            }

            DbConstants.SRT_LND_BRW -> {
                // Grouped by Loaned To
                val grouped = allBooks.groupBy { relation ->
                    relation.fields.firstOrNull { it.typeId == DbConstants.FLD_LOANED_TO }?.name ?: MISSING
                }
                for ((loanedTo, list) in grouped.entries.sortedBy { it.key }) {
                    val childList = ArrayList<BookResult>()
                    val sortedList = list.sortedWith(compareBy({ relation ->
                        relation.fields.filter { it.typeId == DbConstants.FLD_AUTHOR }.joinToString(", ") { it.name }
                    }, { it.book.title }))
                    for (relation in sortedList) {
                        val authors = relation.fields.filter { it.typeId == DbConstants.FLD_AUTHOR }.joinToString(", ") { it.name }
                        val content = if (authors.isNotEmpty()) "$authors - ${relation.book.title}" else relation.book.title
                        childList.add(BookResult(relation.book.id, content))
                    }
                    parentResults.add(ParentResult(loanedTo, childList))
                }
            }
        }

        return parentResults
    }
}
