package org.d1scw0rld.bookbag.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.isRoot
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
import org.d1scw0rld.bookbag.waitFor
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import javax.inject.Inject
import kotlinx.coroutines.test.runTest

@HiltAndroidTest
@RunWith(DisplayNameRobolectricRunner::class)
@Config(application = HiltTestApplication::class, sdk = [28])
class BookListFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var bookDao: BookDao

    @Before
    fun init() {
        hiltRule.inject()
    }

    @DisplayName("On View Created - Initial State - Loads Books From ViewModel and Displays in List")
    @Test
    fun onViewCreated_initialState_loadsBooksFromViewModel() = runTest {
        // Arrange: Insert a book into the database
        val book = BookEntity(
            id = ID_1,
            title = TITLE_1,
            description = DESC_1,
            volume = 1,
            publicationDate = 2023,
            pages = 100,
            price = PRICE_1,
            value = PRICE_1,
            dueDate = 0,
            readDate = 0,
            edition = 1,
            isbn = ISBN_1,
            web = ""
        )
        bookDao.insertBook(book)

        // Act: Launch Fragment
        launchFragmentInHiltContainer<BookListFragment>(themeResId = androidx.appcompat.R.style.Theme_AppCompat_Light_NoActionBar)

        // Assert: Check if book title is displayed in the list
        // Note: BooksAdapter groups by title first letter by default (DbConstants.SRT_TTL)
        // So we expect to see "T" as header and "Testing Book" as item.
        // Wait for the list header and count to appear (baseline UI check)
        onView(isRoot()).perform(waitFor(withText(LETTER_T), TIMEOUT_2000))
        onView(withText(LETTER_T)).check(matches(isDisplayed()))
        onView(withText(COUNT_1_TEXT)).check(matches(isDisplayed()))
    }

    companion object {
        private const val ID_1 = 1L
        private const val TITLE_1 = "Testing Book"
        private const val DESC_1 = "Description"
        private const val PRICE_1 = "1000|1"
        private const val ISBN_1 = "12345"

        private const val LETTER_T = "T"
        private const val COUNT_1_TEXT = "1 book"

        private const val TIMEOUT_2000 = 2000L
    }
}
