package org.d1scw0rld.bookbag.data.relation

import org.d1scw0rld.bookbag.data.DbConstants
import org.d1scw0rld.bookbag.data.entity.BookEntity
import org.d1scw0rld.bookbag.data.entity.FieldEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.d1scw0rld.bookbag.DisplayNameRunner

@RunWith(DisplayNameRunner::class)
class BookRelationsMapperTest {

    private fun createBook(id: Long, title: String, readDate: Int = 0): BookEntity {
        return BookEntity(
            id = id,
            title = title,
            description = null,
            volume = null,
            publicationDate = null,
            pages = null,
            price = null,
            value = null,
            dueDate = null,
            readDate = readDate,
            edition = null,
            isbn = null,
            web = null
        )
    }

    @DisplayName("Map Books To Parents - SRT_TTL Sorting - Groups and Sorts Alphabetically By First Upper Case Letter")
    @Test
    fun mapBooksToParents_srtTtlSorting_groupsAndSortsAlphabeticallyByFirstUpperCaseLetter() {
        // Arrange
        val b1 = BookWithFields(createBook(TEST_ID_1, TITLE_HOBBIT), emptyList())
        val b2 = BookWithFields(createBook(TEST_ID_2, TITLE_HARRY_POTTER), emptyList())
        val b3 = BookWithFields(createBook(TEST_ID_3, TITLE_CLEAN_CODE), emptyList())
        val input = listOf(b1, b2, b3)

        // Act
        val result = BookRelationsMapper.mapBooksToParents(input, DbConstants.SRT_TTL)

        // Assert: Groups are 'C' and 'H', sorted alphabetically
        assertEquals(2, result.size)

        assertEquals(GROUP_C, result[0].name)
        assertEquals(TITLE_CLEAN_CODE, result[0].childList[0].content)

        assertEquals(GROUP_H, result[1].name)
        assertEquals(TITLE_HARRY_POTTER, result[1].childList[0].content) // 'Harry Potter' before 'Hobbit'
        assertEquals(TITLE_HOBBIT, result[1].childList[1].content)
    }

    @DisplayName("Map Books To Parents - SRT_AUT Sorting - Groups by Author and Handles Missing Authors")
    @Test
    fun mapBooksToParents_srtAutSorting_groupsByAuthorAndHandlesMissingAuthors() {
        // Arrange
        val authorField1 = FieldEntity(id = FIELD_ID_101, typeId = DbConstants.FLD_AUTHOR, name = AUTHOR_TOLKIEN)
        val authorField2 = FieldEntity(id = FIELD_ID_102, typeId = DbConstants.FLD_AUTHOR, name = AUTHOR_ORWELL)

        val b1 = BookWithFields(createBook(TEST_ID_1, TITLE_LOTR), listOf(authorField1))
        val b2 = BookWithFields(createBook(TEST_ID_2, TITLE_1984), listOf(authorField2))
        val b3 = BookWithFields(createBook(TEST_ID_3, TITLE_ANONYMOUS), emptyList()) // missing author
        val input = listOf(b1, b2, b3)

        // Act
        val result = BookRelationsMapper.mapBooksToParents(input, DbConstants.SRT_AUT)

        // Assert: Parent groups sorted alphabetically by Author, then (missing)
        // Order: (missing) -> George Orwell -> J.R.R. Tolkien
        assertEquals(3, result.size)

        assertEquals(GROUP_MISSING, result[0].name)
        assertEquals(TITLE_ANONYMOUS, result[0].childList[0].content)

        assertEquals(AUTHOR_ORWELL, result[1].name)
        assertEquals(TITLE_1984, result[1].childList[0].content)

        assertEquals(AUTHOR_TOLKIEN, result[2].name)
        assertEquals(TITLE_LOTR, result[2].childList[0].content)
    }

    @DisplayName("Map Books To Parents - SRT_RD_AUT Sorting - Filters Read Books and Groups by Read Year")
    @Test
    fun mapBooksToParents_srtRdAutSorting_filtersReadBooksAndGroupsByReadYear() {
        // Arrange
        val readField = FieldEntity(id = FIELD_ID_201, typeId = DbConstants.FLD_READ, name = VALUE_TRUE)
        val authorField = FieldEntity(id = FIELD_ID_202, typeId = DbConstants.FLD_AUTHOR, name = AUTHOR_FOWLER)

        val b1 = BookWithFields(createBook(TEST_ID_1, TITLE_REFACTORING, readDate = DATE_2023_11_15), listOf(readField, authorField))
        val b2 = BookWithFields(createBook(TEST_ID_2, TITLE_PATTERNS, readDate = DATE_2022_05_12), listOf(readField, authorField))
        val b3 = BookWithFields(createBook(TEST_ID_3, TITLE_UNREAD, readDate = DATE_ZERO), emptyList()) // not read
        val input = listOf(b1, b2, b3)

        // Act
        val result = BookRelationsMapper.mapBooksToParents(input, DbConstants.SRT_RD_AUT)

        // Assert: Groups sorted descending by Year: 2023 -> 2022
        assertEquals(2, result.size)

        assertEquals(YEAR_2023, result[0].name)
        assertEquals(EXPECTED_FOWLER_REFACTORING, result[0].childList[0].content)

        assertEquals(YEAR_2022, result[1].name)
        assertEquals(EXPECTED_FOWLER_PATTERNS, result[1].childList[0].content)
    }

    @DisplayName("Map Books To Parents - SRT_NOT_RD_TTL Sorting - Filters Unread Books and Groups by Format")
    @Test
    fun mapBooksToParents_srtNotRdTtlSorting_filtersUnreadBooksAndGroupsByFormat() {
        // Arrange
        val formatEpub = FieldEntity(id = FIELD_ID_301, typeId = DbConstants.FLD_FORMAT, name = FORMAT_EPUB)
        val formatHardcopy = FieldEntity(id = FIELD_ID_302, typeId = DbConstants.FLD_FORMAT, name = FORMAT_HARDCOPY)
        val readField = FieldEntity(id = FIELD_ID_303, typeId = DbConstants.FLD_READ, name = VALUE_TRUE)

        val b1 = BookWithFields(createBook(TEST_ID_1, TITLE_EFFECTIVE_KOTLIN), listOf(formatEpub)) // unread
        val b2 = BookWithFields(createBook(TEST_ID_2, TITLE_CLEAN_ARCH), listOf(formatHardcopy)) // unread
        val b3 = BookWithFields(createBook(TEST_ID_3, TITLE_ALREADY_READ), listOf(formatEpub, readField)) // read
        val input = listOf(b1, b2, b3)

        // Act
        val result = BookRelationsMapper.mapBooksToParents(input, DbConstants.SRT_NOT_RD_TTL)

        // Assert: 2 unread groups (ePub and Hardcopy), sorted alphabetically
        assertEquals(2, result.size)

        assertEquals(FORMAT_HARDCOPY, result[0].name)
        assertEquals(TITLE_CLEAN_ARCH, result[0].childList[0].content)

        assertEquals(FORMAT_EPUB, result[1].name)
        assertEquals(TITLE_EFFECTIVE_KOTLIN, result[1].childList[0].content)
    }

    @DisplayName("Map Books To Parents - SRT_WNT_PBL_AUT Sorting - Filters Wanted Books and Groups by Status Sorted by Author Then Title")
    @Test
    fun mapBooksToParents_srtWntPblAutSorting_filtersWantedBooksAndGroupsByStatusSortedByAuthorThenTitle() {
        // Arrange
        val statusWishlist = FieldEntity(id = FIELD_ID_401, typeId = DbConstants.FLD_STATUS, name = STATUS_WISHLIST)
        val statusPreorder = FieldEntity(id = FIELD_ID_402, typeId = DbConstants.FLD_STATUS, name = STATUS_PREORDER)
        val statusInBag = FieldEntity(id = FIELD_ID_403, typeId = DbConstants.FLD_STATUS, name = STATUS_IN_BAG) // excluded
        
        val authorJoshua = FieldEntity(id = FIELD_ID_404, typeId = DbConstants.FLD_AUTHOR, name = AUTHOR_BLOCH)
        val authorKotlin = FieldEntity(id = FIELD_ID_405, typeId = DbConstants.FLD_AUTHOR, name = AUTHOR_KOTLIN_TEAM)

        val b1 = BookWithFields(createBook(TEST_ID_1, TITLE_EFFECTIVE_JAVA), listOf(statusWishlist, authorJoshua))
        val b2 = BookWithFields(createBook(TEST_ID_2, TITLE_KOTLIN_IN_ACTION), listOf(statusWishlist, authorKotlin))
        val b3 = BookWithFields(createBook(TEST_ID_3, TITLE_PREORDER), listOf(statusPreorder))
        val b4 = BookWithFields(createBook(TEST_ID_4, TITLE_OWNED), listOf(statusInBag)) // should be filtered out

        val input = listOf(b1, b2, b3, b4)

        // Act
        val result = BookRelationsMapper.mapBooksToParents(input, DbConstants.SRT_WNT_PBL_AUT)

        // Assert: Sorted by status alphabetically: Preorder -> Wishlist
        assertEquals(2, result.size)

        assertEquals(STATUS_PREORDER, result[0].name)
        assertEquals(TITLE_PREORDER, result[0].childList[0].content)

        assertEquals(STATUS_WISHLIST, result[1].name)
        // Children sorted by author: Joshua Bloch (Effective Java) -> Kotlin Team (Kotlin In Action)
        assertEquals(EXPECTED_BLOCH_EFFECTIVE_JAVA, result[1].childList[0].content)
        assertEquals(EXPECTED_KOTLIN_IN_ACTION, result[1].childList[1].content)
    }

    @DisplayName("Map Books To Parents - SRT_WNT_PBL_TTL Sorting - Filters Wanted Books and Groups by Status Sorted by Title")
    @Test
    fun mapBooksToParents_srtWntPblTtlSorting_filtersWantedBooksAndGroupsByStatusSortedByTitle() {
        // Arrange
        val statusWishlist = FieldEntity(id = FIELD_ID_501, typeId = DbConstants.FLD_STATUS, name = STATUS_WISHLIST)
        val authorKotlin = FieldEntity(id = FIELD_ID_502, typeId = DbConstants.FLD_AUTHOR, name = AUTHOR_KOTLIN_TEAM)
        val authorBloch = FieldEntity(id = FIELD_ID_503, typeId = DbConstants.FLD_AUTHOR, name = AUTHOR_BLOCH)

        val b1 = BookWithFields(createBook(TEST_ID_1, TITLE_KOTLIN_IN_ACTION), listOf(statusWishlist, authorKotlin))
        val b2 = BookWithFields(createBook(TEST_ID_2, TITLE_EFFECTIVE_JAVA), listOf(statusWishlist, authorBloch))
        val input = listOf(b1, b2)

        // Act
        val result = BookRelationsMapper.mapBooksToParents(input, DbConstants.SRT_WNT_PBL_TTL)

        // Assert
        assertEquals(1, result.size)
        assertEquals(STATUS_WISHLIST, result[0].name)
        // Sorted by Title: Effective Java -> Kotlin In Action
        assertEquals(EXPECTED_EFFECTIVE_JAVA_BLOCH, result[0].childList[0].content)
        assertEquals(EXPECTED_KOTLIN_IN_ACTION_TEAM, result[0].childList[1].content)
    }

    @DisplayName("Map Books To Parents - SRT_RD_TTL Sorting - Filters Read Books and Groups by Year Sorted by Title")
    @Test
    fun mapBooksToParents_srtRdTtl_filtersReadBooksAndGroupsByYearSortedByTitle() {
        // Arrange
        val readField = FieldEntity(id = FIELD_ID_601, typeId = DbConstants.FLD_READ, name = VALUE_TRUE)
        val authorKotlin = FieldEntity(id = FIELD_ID_602, typeId = DbConstants.FLD_AUTHOR, name = AUTHOR_KOTLIN_TEAM)
        val authorBloch = FieldEntity(id = FIELD_ID_603, typeId = DbConstants.FLD_AUTHOR, name = AUTHOR_BLOCH)

        val b1 = BookWithFields(createBook(TEST_ID_1, TITLE_KOTLIN_IN_ACTION, readDate = DATE_2023_11_15), listOf(readField, authorKotlin))
        val b2 = BookWithFields(createBook(TEST_ID_2, TITLE_EFFECTIVE_JAVA, readDate = DATE_2023_05_12), listOf(readField, authorBloch))
        val input = listOf(b1, b2)

        // Act
        val result = BookRelationsMapper.mapBooksToParents(input, DbConstants.SRT_RD_TTL)

        // Assert
        assertEquals(1, result.size)
        assertEquals(YEAR_2023, result[0].name)
        // Sorted by Title: Effective Java -> Kotlin In Action
        assertEquals(EXPECTED_EFFECTIVE_JAVA_BLOCH, result[0].childList[0].content)
        assertEquals(EXPECTED_KOTLIN_IN_ACTION_TEAM, result[0].childList[1].content)
    }

    @DisplayName("Map Books To Parents - SRT_NOT_RD_AUT Sorting - Filters Unread Books and Groups by Format Sorted by Author")
    @Test
    fun mapBooksToParents_srtNotRdAut_filtersUnreadBooksAndGroupsByFormatSortedByAuthor() {
        // Arrange
        val formatEpub = FieldEntity(id = FIELD_ID_701, typeId = DbConstants.FLD_FORMAT, name = FORMAT_EPUB)
        val authorKotlin = FieldEntity(id = FIELD_ID_702, typeId = DbConstants.FLD_AUTHOR, name = AUTHOR_KOTLIN_TEAM)
        val authorBloch = FieldEntity(id = FIELD_ID_703, typeId = DbConstants.FLD_AUTHOR, name = AUTHOR_BLOCH)

        val b1 = BookWithFields(createBook(TEST_ID_1, TITLE_KOTLIN_IN_ACTION), listOf(formatEpub, authorKotlin))
        val b2 = BookWithFields(createBook(TEST_ID_2, TITLE_EFFECTIVE_JAVA), listOf(formatEpub, authorBloch))
        val input = listOf(b1, b2)

        // Act
        val result = BookRelationsMapper.mapBooksToParents(input, DbConstants.SRT_NOT_RD_AUT)

        // Assert
        assertEquals(1, result.size)
        assertEquals(FORMAT_EPUB, result[0].name)
        // Sorted by Author: Joshua Bloch (Effective Java) -> Kotlin Team (Kotlin In Action)
        assertEquals(EXPECTED_BLOCH_EFFECTIVE_JAVA, result[0].childList[0].content)
        assertEquals(EXPECTED_KOTLIN_IN_ACTION, result[0].childList[1].content)
    }

    @DisplayName("Map Books To Parents - SRT_PBL_AUT Sorting - Groups by Publisher Sorted by Author")
    @Test
    fun mapBooksToParents_srtPblAut_groupsByPublisherSortedByAuthor() {
        // Arrange
        val pubOReilly = FieldEntity(id = FIELD_ID_801, typeId = DbConstants.FLD_PUBLISHER, name = PUBLISHER_OREILLY)
        val authorKotlin = FieldEntity(id = FIELD_ID_802, typeId = DbConstants.FLD_AUTHOR, name = AUTHOR_KOTLIN_TEAM)
        val authorBloch = FieldEntity(id = FIELD_ID_803, typeId = DbConstants.FLD_AUTHOR, name = AUTHOR_BLOCH)

        val b1 = BookWithFields(createBook(TEST_ID_1, TITLE_KOTLIN_IN_ACTION), listOf(pubOReilly, authorKotlin))
        val b2 = BookWithFields(createBook(TEST_ID_2, TITLE_EFFECTIVE_JAVA), listOf(pubOReilly, authorBloch))
        val input = listOf(b1, b2)

        // Act
        val result = BookRelationsMapper.mapBooksToParents(input, DbConstants.SRT_PBL_AUT)

        // Assert
        assertEquals(1, result.size)
        assertEquals(PUBLISHER_OREILLY, result[0].name)
        // Sorted by Author: Joshua Bloch (Effective Java) -> Kotlin Team (Kotlin In Action)
        assertEquals(EXPECTED_BLOCH_EFFECTIVE_JAVA, result[0].childList[0].content)
        assertEquals(EXPECTED_KOTLIN_IN_ACTION, result[0].childList[1].content)
    }

    @DisplayName("Map Books To Parents - SRT_PBL_TTL Sorting - Groups by Publisher Sorted by Title")
    @Test
    fun mapBooksToParents_srtPblTtl_groupsByPublisherSortedByTitle() {
        // Arrange
        val pubOReilly = FieldEntity(id = FIELD_ID_901, typeId = DbConstants.FLD_PUBLISHER, name = PUBLISHER_OREILLY)
        val authorKotlin = FieldEntity(id = FIELD_ID_902, typeId = DbConstants.FLD_AUTHOR, name = AUTHOR_KOTLIN_TEAM)
        val authorBloch = FieldEntity(id = FIELD_ID_903, typeId = DbConstants.FLD_AUTHOR, name = AUTHOR_BLOCH)

        val b1 = BookWithFields(createBook(TEST_ID_1, TITLE_KOTLIN_IN_ACTION), listOf(pubOReilly, authorKotlin))
        val b2 = BookWithFields(createBook(TEST_ID_2, TITLE_EFFECTIVE_JAVA), listOf(pubOReilly, authorBloch))
        val input = listOf(b1, b2)

        // Act
        val result = BookRelationsMapper.mapBooksToParents(input, DbConstants.SRT_PBL_TTL)

        // Assert
        assertEquals(1, result.size)
        assertEquals(PUBLISHER_OREILLY, result[0].name)
        // Sorted by Title: Effective Java -> Kotlin In Action
        assertEquals(EXPECTED_EFFECTIVE_JAVA_BLOCH, result[0].childList[0].content)
        assertEquals(EXPECTED_KOTLIN_IN_ACTION_TEAM, result[0].childList[1].content)
    }

    @DisplayName("Map Books To Parents - SRT_LND_TTL Sorting - Groups by Loaned To Sorted by Title")
    @Test
    fun mapBooksToParents_srtLndTtl_groupsByLoanedToSortedByTitle() {
        // Arrange
        val loanedJohn = FieldEntity(id = FIELD_ID_1001, typeId = DbConstants.FLD_LOANED_TO, name = PERSON_JOHN_DOE)
        val authorKotlin = FieldEntity(id = FIELD_ID_1002, typeId = DbConstants.FLD_AUTHOR, name = AUTHOR_KOTLIN_TEAM)
        val authorBloch = FieldEntity(id = FIELD_ID_1003, typeId = DbConstants.FLD_AUTHOR, name = AUTHOR_BLOCH)

        val b1 = BookWithFields(createBook(TEST_ID_1, TITLE_KOTLIN_IN_ACTION), listOf(loanedJohn, authorKotlin))
        val b2 = BookWithFields(createBook(TEST_ID_2, TITLE_EFFECTIVE_JAVA), listOf(loanedJohn, authorBloch))
        val input = listOf(b1, b2)

        // Act
        val result = BookRelationsMapper.mapBooksToParents(input, DbConstants.SRT_LND_TTL)

        // Assert
        assertEquals(1, result.size)
        assertEquals(PERSON_JOHN_DOE, result[0].name)
        // Sorted by Title: Effective Java -> Kotlin In Action
        assertEquals(EXPECTED_EFFECTIVE_JAVA_BLOCH, result[0].childList[0].content)
        assertEquals(EXPECTED_KOTLIN_IN_ACTION_TEAM, result[0].childList[1].content)
    }

    @DisplayName("Map Books To Parents - SRT_LND_BRW Sorting - Groups by Loaned To Sorted by Author")
    @Test
    fun mapBooksToParents_srtLndBrw_groupsByLoanedToSortedByAuthor() {
        // Arrange
        val loanedJohn = FieldEntity(id = FIELD_ID_1101, typeId = DbConstants.FLD_LOANED_TO, name = PERSON_JOHN_DOE)
        val authorKotlin = FieldEntity(id = FIELD_ID_1102, typeId = DbConstants.FLD_AUTHOR, name = AUTHOR_KOTLIN_TEAM)
        val authorBloch = FieldEntity(id = FIELD_ID_1103, typeId = DbConstants.FLD_AUTHOR, name = AUTHOR_BLOCH)

        val b1 = BookWithFields(createBook(TEST_ID_1, TITLE_KOTLIN_IN_ACTION), listOf(loanedJohn, authorKotlin))
        val b2 = BookWithFields(createBook(TEST_ID_2, TITLE_EFFECTIVE_JAVA), listOf(loanedJohn, authorBloch))
        val input = listOf(b1, b2)

        // Act
        val result = BookRelationsMapper.mapBooksToParents(input, DbConstants.SRT_LND_BRW)

        // Assert
        assertEquals(1, result.size)
        assertEquals(PERSON_JOHN_DOE, result[0].name)
        // Sorted by Author: Joshua Bloch (Effective Java) -> Kotlin Team (Kotlin In Action)
        assertEquals(EXPECTED_BLOCH_EFFECTIVE_JAVA, result[0].childList[0].content)
        assertEquals(EXPECTED_KOTLIN_IN_ACTION, result[0].childList[1].content)
    }

    companion object {
        const val TEST_ID_1 = 1L
        const val TEST_ID_2 = 2L
        const val TEST_ID_3 = 3L
        const val TEST_ID_4 = 4L

        const val FIELD_ID_101 = 101L
        const val FIELD_ID_102 = 102L
        const val FIELD_ID_201 = 201L
        const val FIELD_ID_202 = 202L
        const val FIELD_ID_301 = 301L
        const val FIELD_ID_302 = 302L
        const val FIELD_ID_303 = 303L
        const val FIELD_ID_401 = 401L
        const val FIELD_ID_402 = 402L
        const val FIELD_ID_403 = 403L
        const val FIELD_ID_404 = 404L
        const val FIELD_ID_405 = 405L
        const val FIELD_ID_501 = 501L
        const val FIELD_ID_502 = 502L
        const val FIELD_ID_503 = 503L
        const val FIELD_ID_601 = 601L
        const val FIELD_ID_602 = 602L
        const val FIELD_ID_603 = 603L
        const val FIELD_ID_701 = 701L
        const val FIELD_ID_702 = 702L
        const val FIELD_ID_703 = 703L
        const val FIELD_ID_801 = 801L
        const val FIELD_ID_802 = 802L
        const val FIELD_ID_803 = 803L
        const val FIELD_ID_901 = 901L
        const val FIELD_ID_902 = 902L
        const val FIELD_ID_903 = 903L
        const val FIELD_ID_1001 = 1001L
        const val FIELD_ID_1002 = 1002L
        const val FIELD_ID_1003 = 1003L
        const val FIELD_ID_1101 = 1101L
        const val FIELD_ID_1102 = 1102L
        const val FIELD_ID_1103 = 1103L

        const val DATE_ZERO = 0
        const val DATE_2023_11_15 = 20231115
        const val DATE_2023_05_12 = 20230512
        const val DATE_2022_05_12 = 20220512

        const val TITLE_HOBBIT = "Hobbit"
        const val TITLE_HARRY_POTTER = "Harry Potter"
        const val TITLE_CLEAN_CODE = "Clean Code"
        const val TITLE_LOTR = "The Lord of the Rings"
        const val TITLE_1984 = "1984"
        const val TITLE_ANONYMOUS = "Anonymous Book"
        const val TITLE_REFACTORING = "Refactoring"
        const val TITLE_PATTERNS = "Patterns of Enterprise Application Architecture"
        const val TITLE_UNREAD = "Unread Book"
        const val TITLE_EFFECTIVE_KOTLIN = "Effective Kotlin"
        const val TITLE_CLEAN_ARCH = "Clean Architecture"
        const val TITLE_ALREADY_READ = "Already Read Book"
        const val TITLE_EFFECTIVE_JAVA = "Effective Java"
        const val TITLE_KOTLIN_IN_ACTION = "Kotlin In Action"
        const val TITLE_PREORDER = "Preorder Book"
        const val TITLE_OWNED = "Owned Book"

        const val AUTHOR_TOLKIEN = "J.R.R. Tolkien"
        const val AUTHOR_ORWELL = "George Orwell"
        const val AUTHOR_FOWLER = "Martin Fowler"
        const val AUTHOR_BLOCH = "Joshua Bloch"
        const val AUTHOR_KOTLIN_TEAM = "Kotlin Team"

        const val STATUS_WISHLIST = "Wishlist"
        const val STATUS_PREORDER = "Preorder"
        const val STATUS_IN_BAG = "In Bag"

        const val FORMAT_EPUB = "ePub"
        const val FORMAT_HARDCOPY = "Hardcopy"

        const val PUBLISHER_OREILLY = "O'Reilly"
        
        const val PERSON_JOHN_DOE = "John Doe"

        const val VALUE_TRUE = "true"

        const val GROUP_C = "C"
        const val GROUP_H = "H"
        const val GROUP_MISSING = "(missing)"
        const val YEAR_2023 = "2023"
        const val YEAR_2022 = "2022"

        const val EXPECTED_FOWLER_REFACTORING = "Martin Fowler - Refactoring"
        const val EXPECTED_FOWLER_PATTERNS = "Martin Fowler - Patterns of Enterprise Application Architecture"
        const val EXPECTED_BLOCH_EFFECTIVE_JAVA = "Joshua Bloch - Effective Java"
        const val EXPECTED_KOTLIN_IN_ACTION = "Kotlin Team - Kotlin In Action"
        const val EXPECTED_EFFECTIVE_JAVA_BLOCH = "Effective Java - Joshua Bloch"
        const val EXPECTED_KOTLIN_IN_ACTION_TEAM = "Kotlin In Action - Kotlin Team"
    }
}
