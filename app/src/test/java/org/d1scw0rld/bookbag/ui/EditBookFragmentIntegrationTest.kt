package org.d1scw0rld.bookbag.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.hasErrorText
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import org.hamcrest.Matchers.allOf
import androidx.test.espresso.action.ViewActions.click
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.test.runTest
import org.d1scw0rld.bookbag.DisplayNameRobolectricRunner
import org.d1scw0rld.bookbag.R
import org.d1scw0rld.bookbag.data.dao.BookDao
import org.d1scw0rld.bookbag.data.entity.BookEntity
import org.d1scw0rld.bookbag.launchFragmentInHiltContainer
import org.d1scw0rld.bookbag.ui.fields.FieldEditTextUpdatableClearable
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
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
    }

    @Test
    fun onViewCreated_formInflated_toolbarAndScrollViewVisible() = runTest {
        // Act: Launch fragment for creating a new book (bookID=0, isCopy=false)
        val args = Bundle().apply {
            putLong("bookID", 0L)
            putBoolean("isCopy", false)
        }
        launchFragmentInHiltContainer<EditBookFragment>(fragmentArgs = args)

        // Assert: Toolbar is visible
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        
        // Assert: NestedScrollView (form container) is visible
        onView(withId(R.id.book_detail_container)).check(matches(isDisplayed()))
    }

    @Test
    fun onViewCreated_newBook_appBarLayoutPresent() = runTest {
        // Act: Launch fragment for new book
        val args = Bundle().apply {
            putLong("bookID", 0L)
            putBoolean("isCopy", false)
        }
        launchFragmentInHiltContainer<EditBookFragment>(fragmentArgs = args)

        // Assert: AppBar is displayed for toolbar
        onView(withId(R.id.app_bar)).check(matches(isDisplayed()))
    }

    @Test
    fun onViewCreated_editExistingBook_fragmentInitializes() = runTest {
        // Arrange: Insert a book to edit
        val book = BookEntity(
            id = 301L,
            title = "Book to Edit",
            description = "Edit this book",
            volume = 1,
            publicationDate = 2023,
            pages = 300,
            price = "3000|1",
            value = "3000|1",
            dueDate = 0,
            readDate = 0,
            edition = 1,
            isbn = "4444444444",
            web = "https://editable.com"
        )
        bookDao.insertBook(book)

        // Act: Launch fragment for editing existing book
        val args = Bundle().apply {
            putLong("bookID", 301L)
            putBoolean("isCopy", false)
        }
        launchFragmentInHiltContainer<EditBookFragment>(fragmentArgs = args)

        // Assert: Form container is visible
        onView(withId(R.id.book_detail_container)).check(matches(isDisplayed()))
    }

    @Test
    fun onViewCreated_copyBook_handlesBookCopyMode() = runTest {
        // Arrange: Insert a book to copy
        val book = BookEntity(
            id = 302L,
            title = "Book to Copy",
            description = "Copy this",
            volume = 2,
            publicationDate = 2022,
            pages = 400,
            price = "4000|1",
            value = "4000|1",
            dueDate = 0,
            readDate = 0,
            edition = 2,
            isbn = "5555555555",
            web = "https://copyable.com"
        )
        bookDao.insertBook(book)

        // Act: Launch fragment with isCopy=true
        val args = Bundle().apply {
            putLong("bookID", 302L)
            putBoolean("isCopy", true)
        }
        launchFragmentInHiltContainer<EditBookFragment>(fragmentArgs = args)

        // Assert: Main layout structure is visible
        onView(withId(R.id.book_detail_container)).check(matches(isDisplayed()))
    }

    @Test
    fun onViewCreated_formInflation_addFieldButtonVisible() = runTest {
        // Act: Launch fragment
        val args = Bundle().apply {
            putLong("bookID", 0L)
            putBoolean("isCopy", false)
        }
        launchFragmentInHiltContainer<EditBookFragment>(fragmentArgs = args)

        // Assert: Add field button is accessible
        onView(withId(R.id.btn_add_field)).check(matches(isDisplayed()))
    }

    @Test
    fun onViewCreated_argumentsPassed_fragmentLoadsSuccessfully() = runTest {
        // Act: Launch fragment with valid arguments
        val args = Bundle().apply {
            putLong("bookID", 303L)
            putBoolean("isCopy", false)
        }
        launchFragmentInHiltContainer<EditBookFragment>(fragmentArgs = args)

        // Assert: Toolbar initialization succeeds
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
    }

    @Test
    fun onSaveTapped_emptyTitle_showsError() {
        val args = Bundle().apply {
            putLong("bookID", 0L)
            putBoolean("isCopy", false)
        }
        
        launchFragmentInHiltContainer<EditBookFragment>(fragmentArgs = args)
        
        // 1. Wait for the fragment to load and inflate fields (async)
        onView(withId(R.id.btn_add_field)).check(matches(isDisplayed()))

        // 2. Click save with empty title
        onView(withText(R.string.done)).perform(click())

        // 3. Verify error is set on the title field using TreeIterables to find the view
        onView(isRoot()).check { view, _ ->
            val iterable = androidx.test.espresso.util.TreeIterables.breadthFirstViewTraversal(view)
            val editText = iterable.filterIsInstance<android.widget.EditText>().firstOrNull()
            assertTrue("Should find at least one EditText (Title)", editText != null)
            assertTrue("Title should have error", editText?.error != null)
        }
    }
}
