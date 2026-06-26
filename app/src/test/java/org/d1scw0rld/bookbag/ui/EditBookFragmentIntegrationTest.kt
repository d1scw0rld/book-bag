package org.d1scw0rld.bookbag.ui

import android.os.Bundle
import android.widget.EditText
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.withHint
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.test.runTest
import org.d1scw0rld.bookbag.DisplayNameRobolectricRunner
import org.d1scw0rld.bookbag.R
import org.d1scw0rld.bookbag.data.dao.BookDao
import org.d1scw0rld.bookbag.data.entity.BookEntity
import org.d1scw0rld.bookbag.launchFragmentInHiltContainer
import org.d1scw0rld.bookbag.waitFor
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@RunWith(DisplayNameRobolectricRunner::class)
@Config(application = HiltTestApplication::class, sdk = [28])
class EditBookFragmentIntegrationTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var bookDao: BookDao

    @Before
    fun init() {
        hiltRule.inject()
        org.d1scw0rld.bookbag.data.DbConstants.initFields(org.robolectric.RuntimeEnvironment.getApplication().resources)
    }

    @DisplayName("On View Created - Form Inflated - Toolbar and ScrollView Visible")
    @Test
    fun onViewCreated_formInflated_toolbarAndScrollViewVisible() = runTest {
        // Act: Launch fragment for creating a new book (bookID=0, isCopy=false)
        val args = Bundle().apply {
            putLong(KEY_BOOK_ID, ID_0)
            putBoolean(KEY_IS_COPY, false)
        }
        launchFragmentInHiltContainer<EditBookFragment>(fragmentArgs = args)

        // Assert: Toolbar is visible
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        
        // Assert: NestedScrollView (form container) is visible
        onView(withId(R.id.book_detail_container)).check(matches(isDisplayed()))
    }

    @DisplayName("On View Created - New Book - AppBar Layout Present")
    @Test
    fun onViewCreated_newBook_appBarLayoutPresent() = runTest {
        // Act: Launch fragment for new book
        val args = Bundle().apply {
            putLong(KEY_BOOK_ID, ID_0)
            putBoolean(KEY_IS_COPY, false)
        }
        launchFragmentInHiltContainer<EditBookFragment>(fragmentArgs = args)

        // Assert: AppBar is displayed for toolbar
        onView(withId(R.id.app_bar)).check(matches(isDisplayed()))
    }

    @DisplayName("On View Created - Edit Existing Book - Fragment Initializes")
    @Test
    fun onViewCreated_editExistingBook_fragmentInitializes() = runTest {
        // Arrange: Insert a book to edit
        val book = BookEntity(
            id = ID_301,
            title = TITLE_EDIT,
            description = DESC_EDIT,
            volume = VOL_1,
            publicationDate = PUB_DATE_2023,
            pages = PAGES_300,
            price = PRICE_3000_1,
            value = PRICE_3000_1,
            dueDate = DATE_ZERO,
            readDate = DATE_ZERO,
            edition = EDITION_1,
            isbn = ISBN_EDIT,
            web = WEB_EDIT
        )
        bookDao.insertBook(book)

        // Act: Launch fragment for editing existing book
        val args = Bundle().apply {
            putLong(KEY_BOOK_ID, ID_301)
            putBoolean(KEY_IS_COPY, false)
        }
        launchFragmentInHiltContainer<EditBookFragment>(fragmentArgs = args)

        // Assert: Form container is visible
        onView(withId(R.id.book_detail_container)).check(matches(isDisplayed()))
    }

    @DisplayName("On View Created - Copy Book - Handles Book Copy Mode")
    @Test
    fun onViewCreated_copyBook_handlesBookCopyMode() = runTest {
        // Arrange: Insert a book to copy
        val book = BookEntity(
            id = ID_302,
            title = TITLE_COPY,
            description = DESC_COPY,
            volume = VOL_2,
            publicationDate = PUB_DATE_2022,
            pages = PAGES_400,
            price = PRICE_4000_1,
            value = PRICE_4000_1,
            dueDate = DATE_ZERO,
            readDate = DATE_ZERO,
            edition = EDITION_2,
            isbn = ISBN_COPY,
            web = WEB_COPY
        )
        bookDao.insertBook(book)

        // Act: Launch fragment with isCopy=true
        val args = Bundle().apply {
            putLong(KEY_BOOK_ID, ID_302)
            putBoolean(KEY_IS_COPY, true)
        }
        launchFragmentInHiltContainer<EditBookFragment>(fragmentArgs = args)

        // Assert: Main layout structure is visible
        onView(withId(R.id.book_detail_container)).check(matches(isDisplayed()))
    }

    @DisplayName("On View Created - Form Inflation - Add Field Button Visible")
    @Test
    fun onViewCreated_formInflation_addFieldButtonVisible() = runTest {
        // Act: Launch fragment
        val args = Bundle().apply {
            putLong(KEY_BOOK_ID, ID_0)
            putBoolean(KEY_IS_COPY, false)
        }
        launchFragmentInHiltContainer<EditBookFragment>(fragmentArgs = args)

        // Assert: Add field button is accessible
        onView(withId(R.id.btn_add_field)).check(matches(isDisplayed()))
    }

    @DisplayName("On View Created - Arguments Passed - Fragment Loads Successfully")
    @Test
    fun onViewCreated_argumentsPassed_fragmentLoadsSuccessfully() = runTest {
        // Act: Launch fragment with valid arguments
        val args = Bundle().apply {
            putLong(KEY_BOOK_ID, ID_303)
            putBoolean(KEY_IS_COPY, false)
        }
        launchFragmentInHiltContainer<EditBookFragment>(fragmentArgs = args)

        // Assert: Toolbar initialization succeeds
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
    }

    @DisplayName("On Save Tapped - Empty Title - Shows Error")
    @Test
    fun onSaveTapped_emptyTitle_showsError() {
        val args = Bundle().apply {
            putLong(KEY_BOOK_ID, ID_0)
            putBoolean(KEY_IS_COPY, false)
        }
        
        launchFragmentInHiltContainer<EditBookFragment>(fragmentArgs = args)
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        
        // 1. Wait for the Title field's internal EditText (it has ID R.id.editTextX).
        // Using wait by hint for more robustness as multiple views might have the same ID.
        onView(isRoot()).perform(waitFor(withHint(R.string.fld_title), TIMEOUT_5000))

        // 2. Click save with empty title
        onView(withText(R.string.done)).perform(click())

        // 3. Verify error is set on the title field using TreeIterables to find the view
        onView(isRoot()).check { view, _ ->
            val iterable = androidx.test.espresso.util.TreeIterables.breadthFirstViewTraversal(view)
            val editText = iterable.filterIsInstance<EditText>().firstOrNull { it.hint == view.context.getString(R.string.fld_title) }
            assertTrue(MSG_FIND_EDITTEXT, editText != null)
            assertTrue(MSG_TITLE_ERROR, editText?.error != null)
        }
    }

    companion object {
        private const val KEY_BOOK_ID = "bookID"
        private const val KEY_IS_COPY = "isCopy"

        private const val ID_0 = 0L
        private const val ID_301 = 301L
        private const val ID_302 = 302L
        private const val ID_303 = 303L

        private const val TITLE_EDIT = "Book to Edit"
        private const val TITLE_COPY = "Book to Copy"

        private const val DESC_EDIT = "Edit this book"
        private const val DESC_COPY = "Copy this"

        private const val VOL_1 = 1
        private const val VOL_2 = 2

        private const val PUB_DATE_2022 = 2022
        private const val PUB_DATE_2023 = 2023

        private const val PAGES_300 = 300
        private const val PAGES_400 = 400

        private const val PRICE_3000_1 = "3000|1"
        private const val PRICE_4000_1 = "4000|1"

        private const val EDITION_1 = 1
        private const val EDITION_2 = 2

        private const val ISBN_EDIT = "4444444444"
        private const val ISBN_COPY = "5555555555"

        private const val WEB_EDIT = "https://editable.com"
        private const val WEB_COPY = "https://copyable.com"

        private const val DATE_ZERO = 0

        private const val TIMEOUT_5000 = 5000L

        private const val MSG_FIND_EDITTEXT = "Should find at least one EditText (Title)"
        private const val MSG_TITLE_ERROR = "Title should have error"
    }
}
