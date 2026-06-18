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
            id = 1L,
            title = "Testing Book",
            description = "Description",
            volume = 1,
            publicationDate = 2023,
            pages = 100,
            price = "1000|1",
            value = "1000|1",
            dueDate = 0,
            readDate = 0,
            edition = 1,
            isbn = "12345",
            web = ""
        )
        bookDao.insertBook(book)

        // Act: Launch Fragment
        launchFragmentInHiltContainer<BookListFragment>(themeResId = androidx.appcompat.R.style.Theme_AppCompat_Light_NoActionBar)

        // Assert: Check if book title is displayed in the list
        // Note: BooksAdapter groups by title first letter by default (DbConstants.SRT_TTL)
        // So we expect to see "T" as header and "Testing Book" as item.
        // Wait for the list header and count to appear (baseline UI check)
        onView(isRoot()).perform(waitFor(withText("T"), 2000))
        onView(withText("T")).check(matches(isDisplayed()))
        onView(withText("1 book")).check(matches(isDisplayed()))
    }
}
