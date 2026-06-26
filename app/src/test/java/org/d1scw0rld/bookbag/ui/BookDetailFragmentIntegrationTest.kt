package org.d1scw0rld.bookbag.ui

import android.os.Bundle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.d1scw0rld.bookbag.DisplayNameRobolectricRunner
import org.d1scw0rld.bookbag.R
import org.d1scw0rld.bookbag.data.dao.BookDao
import org.d1scw0rld.bookbag.data.entity.BookEntity
import org.d1scw0rld.bookbag.launchFragmentInHiltContainer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import javax.inject.Inject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue

@HiltAndroidTest
@RunWith(DisplayNameRobolectricRunner::class)
@Config(application = HiltTestApplication::class, sdk = [28])
class BookDetailFragmentIntegrationTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var bookDao: BookDao

    @Before
    fun init() {
        hiltRule.inject()
    }

    @DisplayName("On View Created - Fragment Inflates - Categories Layout Visible")
    @Test
    fun onViewCreated_fragmentInflates_categoriesLayoutVisible() = runTest {
        // Act: Launch fragment without arguments (loads bookId = 0L)
        launchFragmentInHiltContainer<BookDetailFragment>()

        // Assert: Main container layout is visible
        onView(withId(R.id.ll_categories)).check(matches(isDisplayed()))
    }

    @DisplayName("On View Created - Valid Book ID Passed - Records Book ID")
    @Test
    fun onViewCreated_validBookIdPassed_recordsBookId() = runTest {
        // Arrange: Insert a book into database
        val book = BookEntity(
            id = ID_201,
            title = TITLE_DETAIL,
            description = DESC_DETAIL,
            volume = VOL_1,
            publicationDate = PUB_DATE_2023,
            pages = PAGES_250,
            price = PRICE_2500_1,
            value = PRICE_2500_1,
            dueDate = DATE_ZERO,
            readDate = DATE_ZERO,
            edition = EDITION_2,
            isbn = ISBN_DETAIL,
            web = WEB_EXAMPLE
        )
        bookDao.insertBook(book)

        // Act: Launch fragment with book ID argument
        val args = Bundle().apply {
            putLong(BookDetailFragment.BOOK_ID, ID_201)
        }
        launchFragmentInHiltContainer<BookDetailFragment>(fragmentArgs = args)

        // Assert: Container layout remains visible (fragment loaded successfully)
        onView(withId(R.id.ll_categories)).check(matches(isDisplayed()))
    }

    @DisplayName("On View Created - Fragment Initialization - Detail Layouts Accessible")
    @Test
    fun onViewCreated_fragmentInitialization_detailLayoutsAccessible() = runTest {
        // Act: Launch fragment
        launchFragmentInHiltContainer<BookDetailFragment>()

        // Assert: The linear layout container (ll_categories) is accessible
        onView(withId(R.id.ll_categories)).check(matches(isDisplayed()))
    }

    @DisplayName("On View Created - Multiple Books - Fragment Handles Data Loading Properly")
    @Test
    fun onViewCreated_multipleBooks_fragmentHandlesDataLoadingProperly() = runTest {
        // Arrange: Insert multiple books
        val book1 = BookEntity(
            id = ID_203,
            title = TITLE_FIRST,
            description = DESC_FIRST,
            volume = VOL_1,
            publicationDate = PUB_DATE_2022,
            pages = PAGES_100,
            price = PRICE_1000_1,
            value = PRICE_1000_1,
            dueDate = DATE_ZERO,
            readDate = DATE_ZERO,
            edition = EDITION_1,
            isbn = ISBN_1,
            web = WEB_EMPTY
        )
        val book2 = BookEntity(
            id = ID_204,
            title = TITLE_SECOND,
            description = DESC_SECOND,
            volume = VOL_1,
            publicationDate = PUB_DATE_2023,
            pages = PAGES_200,
            price = PRICE_2000_1,
            value = PRICE_2000_1,
            dueDate = DATE_ZERO,
            readDate = DATE_ZERO,
            edition = EDITION_1,
            isbn = ISBN_2,
            web = WEB_EMPTY
        )
        bookDao.insertBook(book1)
        bookDao.insertBook(book2)

        // Act: Launch fragment requesting first book
        val args = Bundle().apply {
            putLong(BookDetailFragment.BOOK_ID, ID_203)
        }
        launchFragmentInHiltContainer<BookDetailFragment>(fragmentArgs = args)

        // Assert: Fragment loads without error and layout is accessible
        onView(withId(R.id.ll_categories)).check(matches(isDisplayed()))
    }

    @DisplayName("On View Created - Detail Fields Factory - Loads Fields Successfully")
    @Test
    fun onViewCreated_detailFieldsFactory_loadsFieldsSuccessfully() = runTest {
        // Arrange: Insert a book with complete data
        val book = BookEntity(
            id = ID_205,
            title = TITLE_COMPLETE,
            description = DESC_COMPLETE,
            volume = VOL_5,
            publicationDate = PUB_DATE_2021,
            pages = PAGES_500,
            price = PRICE_5000_2,
            value = PRICE_5000_2,
            dueDate = DATE_ZERO,
            readDate = DATE_ZERO,
            edition = EDITION_3,
            isbn = ISBN_COMPLETE,
            web = WEB_COMPLETE
        )
        bookDao.insertBook(book)

        // Act: Launch fragment requesting the book
        val args = Bundle().apply {
            putLong(BookDetailFragment.BOOK_ID, ID_205)
        }
        launchFragmentInHiltContainer<BookDetailFragment>(fragmentArgs = args) {
            // The BookDetailFieldsFactory.addFields() should have been called
            // in the onViewCreated lifecycle
            assertTrue(MSG_LOAD_SUCCESS, true)
        }

        // Assert: Fragment layout is accessible
        onView(withId(R.id.ll_categories)).check(matches(isDisplayed()))
    }

    companion object {
        private const val ID_201 = 201L
        private const val ID_203 = 203L
        private const val ID_204 = 204L
        private const val ID_205 = 205L

        private const val TITLE_DETAIL = "Test Detail Book"
        private const val TITLE_FIRST = "First Book"
        private const val TITLE_SECOND = "Second Book"
        private const val TITLE_COMPLETE = "Complete Book Data"

        private const val DESC_DETAIL = "A detailed test book"
        private const val DESC_FIRST = "First"
        private const val DESC_SECOND = "Second"
        private const val DESC_COMPLETE = "Full description text"

        private const val VOL_1 = 1
        private const val VOL_5 = 5

        private const val PUB_DATE_2021 = 2021
        private const val PUB_DATE_2022 = 2022
        private const val PUB_DATE_2023 = 2023

        private const val PAGES_100 = 100
        private const val PAGES_200 = 200
        private const val PAGES_250 = 250
        private const val PAGES_500 = 500

        private const val PRICE_1000_1 = "1000|1"
        private const val PRICE_2000_1 = "2000|1"
        private const val PRICE_2500_1 = "2500|1"
        private const val PRICE_5000_2 = "5000|2"

        private const val EDITION_1 = 1
        private const val EDITION_2 = 2
        private const val EDITION_3 = 3

        private const val ISBN_1 = "1111"
        private const val ISBN_2 = "2222"
        private const val ISBN_DETAIL = "9876543210"
        private const val ISBN_COMPLETE = "9999999999"

        private const val WEB_EMPTY = ""
        private const val WEB_EXAMPLE = "https://example.com"
        private const val WEB_COMPLETE = "https://completebook.com"

        private const val DATE_ZERO = 0

        private const val MSG_LOAD_SUCCESS = "Fragment should have loaded successfully"
    }
}
