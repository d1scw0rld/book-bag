package org.d1scw0rld.bookbag.data.relation

import org.d1scw0rld.bookbag.data.DbConstants
import org.d1scw0rld.bookbag.data.entity.BookEntity
import org.d1scw0rld.bookbag.data.entity.FieldEntity
import org.junit.Assert.assertEquals
import org.junit.Test

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

    @Test
    fun `mapBooksToParents with SRT_TTL groups and sorts books alphabetically by first upper case letter`() {
        // Arrange
        val b1 = BookWithFields(createBook(1L, "Hobbit"), emptyList())
        val b2 = BookWithFields(createBook(2L, "Harry Potter"), emptyList())
        val b3 = BookWithFields(createBook(3L, "Clean Code"), emptyList())
        val input = listOf(b1, b2, b3)

        // Act
        val result = BookRelationsMapper.mapBooksToParents(input, DbConstants.SRT_TTL)

        // Assert: Groups are 'C' and 'H', sorted alphabetically
        assertEquals(2, result.size)

        assertEquals("C", result[0].name)
        assertEquals("Clean Code", result[0].childList[0].content)

        assertEquals("H", result[1].name)
        assertEquals("Harry Potter", result[1].childList[0].content) // 'Harry Potter' before 'Hobbit'
        assertEquals("Hobbit", result[1].childList[1].content)
    }

    @Test
    fun `mapBooksToParents with SRT_AUT groups books by author and handles missing authors gracefully`() {
        // Arrange
        val authorField1 = FieldEntity(id = 101L, typeId = DbConstants.FLD_AUTHOR, name = "J.R.R. Tolkien")
        val authorField2 = FieldEntity(id = 102L, typeId = DbConstants.FLD_AUTHOR, name = "George Orwell")

        val b1 = BookWithFields(createBook(1L, "The Lord of the Rings"), listOf(authorField1))
        val b2 = BookWithFields(createBook(2L, "1984"), listOf(authorField2))
        val b3 = BookWithFields(createBook(3L, "Anonymous Book"), emptyList()) // missing author
        val input = listOf(b1, b2, b3)

        // Act
        val result = BookRelationsMapper.mapBooksToParents(input, DbConstants.SRT_AUT)

        // Assert: Parent groups sorted alphabetically by Author, then (missing)
        // Order: (missing) -> George Orwell -> J.R.R. Tolkien
        assertEquals(3, result.size)

        assertEquals("(missing)", result[0].name)
        assertEquals("Anonymous Book", result[0].childList[0].content)

        assertEquals("George Orwell", result[1].name)
        assertEquals("1984", result[1].childList[0].content)

        assertEquals("J.R.R. Tolkien", result[2].name)
        assertEquals("The Lord of the Rings", result[2].childList[0].content)
    }

    @Test
    fun `mapBooksToParents with SRT_RD_AUT filters read books and groups them by read year`() {
        // Arrange
        val readField = FieldEntity(id = 201L, typeId = DbConstants.FLD_READ, name = "true")
        val authorField = FieldEntity(id = 202L, typeId = DbConstants.FLD_AUTHOR, name = "Martin Fowler")

        val b1 = BookWithFields(createBook(1L, "Refactoring", readDate = 20231115), listOf(readField, authorField))
        val b2 = BookWithFields(createBook(2L, "Patterns of Enterprise Application Architecture", readDate = 20220512), listOf(readField, authorField))
        val b3 = BookWithFields(createBook(3L, "Unread Book", readDate = 0), emptyList()) // not read
        val input = listOf(b1, b2, b3)

        // Act
        val result = BookRelationsMapper.mapBooksToParents(input, DbConstants.SRT_RD_AUT)

        // Assert: Groups sorted descending by Year: 2023 -> 2022
        assertEquals(2, result.size)

        assertEquals("2023", result[0].name)
        assertEquals("Martin Fowler - Refactoring", result[0].childList[0].content)

        assertEquals("2022", result[1].name)
        assertEquals("Martin Fowler - Patterns of Enterprise Application Architecture", result[1].childList[0].content)
    }

    @Test
    fun `mapBooksToParents with SRT_NOT_RD_TTL filters unread books and groups them by format`() {
        // Arrange
        val formatEpub = FieldEntity(id = 301L, typeId = DbConstants.FLD_FORMAT, name = "ePub")
        val formatHardcopy = FieldEntity(id = 302L, typeId = DbConstants.FLD_FORMAT, name = "Hardcopy")
        val readField = FieldEntity(id = 303L, typeId = DbConstants.FLD_READ, name = "true")

        val b1 = BookWithFields(createBook(1L, "Effective Kotlin"), listOf(formatEpub)) // unread
        val b2 = BookWithFields(createBook(2L, "Clean Architecture"), listOf(formatHardcopy)) // unread
        val b3 = BookWithFields(createBook(3L, "Already Read Book"), listOf(formatEpub, readField)) // read
        val input = listOf(b1, b2, b3)

        // Act
        val result = BookRelationsMapper.mapBooksToParents(input, DbConstants.SRT_NOT_RD_TTL)

        // Assert: 2 unread groups (ePub and Hardcopy), sorted alphabetically
        assertEquals(2, result.size)

        assertEquals("Hardcopy", result[0].name)
        assertEquals("Clean Architecture", result[0].childList[0].content)

        assertEquals("ePub", result[1].name)
        assertEquals("Effective Kotlin", result[1].childList[0].content)
    }

    @Test
    fun `mapBooksToParents with SRT_WNT_PBL_AUT filters wanted books and groups by status sorted by author then title`() {
        // Arrange
        val statusWishlist = FieldEntity(id = 401L, typeId = DbConstants.FLD_STATUS, name = "Wishlist")
        val statusPreorder = FieldEntity(id = 402L, typeId = DbConstants.FLD_STATUS, name = "Preorder")
        val statusInBag = FieldEntity(id = 403L, typeId = DbConstants.FLD_STATUS, name = "In Bag") // excluded
        
        val authorJoshua = FieldEntity(id = 404L, typeId = DbConstants.FLD_AUTHOR, name = "Joshua Bloch")
        val authorKotlin = FieldEntity(id = 405L, typeId = DbConstants.FLD_AUTHOR, name = "Kotlin Team")

        val b1 = BookWithFields(createBook(1L, "Effective Java"), listOf(statusWishlist, authorJoshua))
        val b2 = BookWithFields(createBook(2L, "Kotlin In Action"), listOf(statusWishlist, authorKotlin))
        val b3 = BookWithFields(createBook(3L, "Preorder Book"), listOf(statusPreorder))
        val b4 = BookWithFields(createBook(4L, "Owned Book"), listOf(statusInBag)) // should be filtered out

        val input = listOf(b1, b2, b3, b4)

        // Act
        val result = BookRelationsMapper.mapBooksToParents(input, DbConstants.SRT_WNT_PBL_AUT)

        // Assert: Sorted by status alphabetically: Preorder -> Wishlist
        assertEquals(2, result.size)

        assertEquals("Preorder", result[0].name)
        assertEquals("Preorder Book", result[0].childList[0].content)

        assertEquals("Wishlist", result[1].name)
        // Children sorted by author: Joshua Bloch (Effective Java) -> Kotlin Team (Kotlin In Action)
        assertEquals("Joshua Bloch - Effective Java", result[1].childList[0].content)
        assertEquals("Kotlin Team - Kotlin In Action", result[1].childList[1].content)
    }

    @Test
    fun `mapBooksToParents with SRT_WNT_PBL_TTL filters wanted books and groups by status sorted by title`() {
        // Arrange
        val statusWishlist = FieldEntity(id = 501L, typeId = DbConstants.FLD_STATUS, name = "Wishlist")
        val authorKotlin = FieldEntity(id = 502L, typeId = DbConstants.FLD_AUTHOR, name = "Kotlin Team")
        val authorBloch = FieldEntity(id = 503L, typeId = DbConstants.FLD_AUTHOR, name = "Joshua Bloch")

        val b1 = BookWithFields(createBook(1L, "Kotlin In Action"), listOf(statusWishlist, authorKotlin))
        val b2 = BookWithFields(createBook(2L, "Effective Java"), listOf(statusWishlist, authorBloch))
        val input = listOf(b1, b2)

        // Act
        val result = BookRelationsMapper.mapBooksToParents(input, DbConstants.SRT_WNT_PBL_TTL)

        // Assert
        assertEquals(1, result.size)
        assertEquals("Wishlist", result[0].name)
        // Sorted by Title: Effective Java -> Kotlin In Action
        assertEquals("Effective Java - Joshua Bloch", result[0].childList[0].content)
        assertEquals("Kotlin In Action - Kotlin Team", result[0].childList[1].content)
    }

    @Test
    fun `mapBooksToParents with SRT_RD_TTL filters read books and groups by year sorted by title`() {
        // Arrange
        val readField = FieldEntity(id = 601L, typeId = DbConstants.FLD_READ, name = "true")
        val authorKotlin = FieldEntity(id = 602L, typeId = DbConstants.FLD_AUTHOR, name = "Kotlin Team")
        val authorBloch = FieldEntity(id = 603L, typeId = DbConstants.FLD_AUTHOR, name = "Joshua Bloch")

        val b1 = BookWithFields(createBook(1L, "Kotlin In Action", readDate = 20231115), listOf(readField, authorKotlin))
        val b2 = BookWithFields(createBook(2L, "Effective Java", readDate = 20230512), listOf(readField, authorBloch))
        val input = listOf(b1, b2)

        // Act
        val result = BookRelationsMapper.mapBooksToParents(input, DbConstants.SRT_RD_TTL)

        // Assert
        assertEquals(1, result.size)
        assertEquals("2023", result[0].name)
        // Sorted by Title: Effective Java -> Kotlin In Action
        assertEquals("Effective Java - Joshua Bloch", result[0].childList[0].content)
        assertEquals("Kotlin In Action - Kotlin Team", result[0].childList[1].content)
    }

    @Test
    fun `mapBooksToParents with SRT_NOT_RD_AUT filters unread books and groups by format sorted by author`() {
        // Arrange
        val formatEpub = FieldEntity(id = 701L, typeId = DbConstants.FLD_FORMAT, name = "ePub")
        val authorKotlin = FieldEntity(id = 702L, typeId = DbConstants.FLD_AUTHOR, name = "Kotlin Team")
        val authorBloch = FieldEntity(id = 703L, typeId = DbConstants.FLD_AUTHOR, name = "Joshua Bloch")

        val b1 = BookWithFields(createBook(1L, "Kotlin In Action"), listOf(formatEpub, authorKotlin))
        val b2 = BookWithFields(createBook(2L, "Effective Java"), listOf(formatEpub, authorBloch))
        val input = listOf(b1, b2)

        // Act
        val result = BookRelationsMapper.mapBooksToParents(input, DbConstants.SRT_NOT_RD_AUT)

        // Assert
        assertEquals(1, result.size)
        assertEquals("ePub", result[0].name)
        // Sorted by Author: Joshua Bloch (Effective Java) -> Kotlin Team (Kotlin In Action)
        assertEquals("Joshua Bloch - Effective Java", result[0].childList[0].content)
        assertEquals("Kotlin Team - Kotlin In Action", result[0].childList[1].content)
    }

    @Test
    fun `mapBooksToParents with SRT_PBL_AUT groups by publisher sorted by author`() {
        // Arrange
        val pubOReilly = FieldEntity(id = 801L, typeId = DbConstants.FLD_PUBLISHER, name = "O'Reilly")
        val authorKotlin = FieldEntity(id = 802L, typeId = DbConstants.FLD_AUTHOR, name = "Kotlin Team")
        val authorBloch = FieldEntity(id = 803L, typeId = DbConstants.FLD_AUTHOR, name = "Joshua Bloch")

        val b1 = BookWithFields(createBook(1L, "Kotlin In Action"), listOf(pubOReilly, authorKotlin))
        val b2 = BookWithFields(createBook(2L, "Effective Java"), listOf(pubOReilly, authorBloch))
        val input = listOf(b1, b2)

        // Act
        val result = BookRelationsMapper.mapBooksToParents(input, DbConstants.SRT_PBL_AUT)

        // Assert
        assertEquals(1, result.size)
        assertEquals("O'Reilly", result[0].name)
        // Sorted by Author: Joshua Bloch (Effective Java) -> Kotlin Team (Kotlin In Action)
        assertEquals("Joshua Bloch - Effective Java", result[0].childList[0].content)
        assertEquals("Kotlin Team - Kotlin In Action", result[0].childList[1].content)
    }

    @Test
    fun `mapBooksToParents with SRT_PBL_TTL groups by publisher sorted by title`() {
        // Arrange
        val pubOReilly = FieldEntity(id = 901L, typeId = DbConstants.FLD_PUBLISHER, name = "O'Reilly")
        val authorKotlin = FieldEntity(id = 902L, typeId = DbConstants.FLD_AUTHOR, name = "Kotlin Team")
        val authorBloch = FieldEntity(id = 903L, typeId = DbConstants.FLD_AUTHOR, name = "Joshua Bloch")

        val b1 = BookWithFields(createBook(1L, "Kotlin In Action"), listOf(pubOReilly, authorKotlin))
        val b2 = BookWithFields(createBook(2L, "Effective Java"), listOf(pubOReilly, authorBloch))
        val input = listOf(b1, b2)

        // Act
        val result = BookRelationsMapper.mapBooksToParents(input, DbConstants.SRT_PBL_TTL)

        // Assert
        assertEquals(1, result.size)
        assertEquals("O'Reilly", result[0].name)
        // Sorted by Title: Effective Java -> Kotlin In Action
        assertEquals("Effective Java - Joshua Bloch", result[0].childList[0].content)
        assertEquals("Kotlin In Action - Kotlin Team", result[0].childList[1].content)
    }

    @Test
    fun `mapBooksToParents with SRT_LND_TTL groups by loaned to sorted by title`() {
        // Arrange
        val loanedJohn = FieldEntity(id = 1001L, typeId = DbConstants.FLD_LOANED_TO, name = "John Doe")
        val authorKotlin = FieldEntity(id = 1002L, typeId = DbConstants.FLD_AUTHOR, name = "Kotlin Team")
        val authorBloch = FieldEntity(id = 1003L, typeId = DbConstants.FLD_AUTHOR, name = "Joshua Bloch")

        val b1 = BookWithFields(createBook(1L, "Kotlin In Action"), listOf(loanedJohn, authorKotlin))
        val b2 = BookWithFields(createBook(2L, "Effective Java"), listOf(loanedJohn, authorBloch))
        val input = listOf(b1, b2)

        // Act
        val result = BookRelationsMapper.mapBooksToParents(input, DbConstants.SRT_LND_TTL)

        // Assert
        assertEquals(1, result.size)
        assertEquals("John Doe", result[0].name)
        // Sorted by Title: Effective Java -> Kotlin In Action
        assertEquals("Effective Java - Joshua Bloch", result[0].childList[0].content)
        assertEquals("Kotlin In Action - Kotlin Team", result[0].childList[1].content)
    }

    @Test
    fun `mapBooksToParents with SRT_LND_BRW groups by loaned to sorted by author`() {
        // Arrange
        val loanedJohn = FieldEntity(id = 1101L, typeId = DbConstants.FLD_LOANED_TO, name = "John Doe")
        val authorKotlin = FieldEntity(id = 1102L, typeId = DbConstants.FLD_AUTHOR, name = "Kotlin Team")
        val authorBloch = FieldEntity(id = 1103L, typeId = DbConstants.FLD_AUTHOR, name = "Joshua Bloch")

        val b1 = BookWithFields(createBook(1L, "Kotlin In Action"), listOf(loanedJohn, authorKotlin))
        val b2 = BookWithFields(createBook(2L, "Effective Java"), listOf(loanedJohn, authorBloch))
        val input = listOf(b1, b2)

        // Act
        val result = BookRelationsMapper.mapBooksToParents(input, DbConstants.SRT_LND_BRW)

        // Assert
        assertEquals(1, result.size)
        assertEquals("John Doe", result[0].name)
        // Sorted by Author: Joshua Bloch (Effective Java) -> Kotlin Team (Kotlin In Action)
        assertEquals("Joshua Bloch - Effective Java", result[0].childList[0].content)
        assertEquals("Kotlin Team - Kotlin In Action", result[0].childList[1].content)
    }
}
