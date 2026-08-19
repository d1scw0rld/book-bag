package org.d1scw0rld.bookbag.ui

import android.os.Bundle
import android.os.Looper.getMainLooper
import android.widget.EditText
import androidx.appcompat.widget.PopupMenu
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.core.view.size
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.test.runTest
import org.d1scw0rld.bookbag.DisplayNameRobolectricRunner
import org.d1scw0rld.bookbag.R
import org.d1scw0rld.bookbag.data.DbConstants
import org.d1scw0rld.bookbag.data.dao.BookDao
import org.d1scw0rld.bookbag.data.entity.BookEntity
import org.d1scw0rld.bookbag.launchFragmentInHiltContainer
import org.d1scw0rld.bookbag.waitFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.hamcrest.Matchers.allOf
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
        DbConstants.initFields(org.robolectric.RuntimeEnvironment.getApplication().resources)
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

    @DisplayName("On View Created - Fully Populated Book - All Properties Are Initialized")
    @Test
    fun onViewCreated_fullyPopulatedBook_allPropertiesAreInitialized() = runTest {
        // Arrange: Insert a book with all properties populated
        val testDate = 20240815
        val book = BookEntity(
            id = ID_304,
            title = TITLE_COMPLETE,
            description = DESC_COMPLETE,
            volume = VOL_5,
            publicationDate = PUB_DATE_2024,
            pages = PAGES_500,
            price = PRICE_5000_2,
            value = PRICE_5000_2,
            dueDate = testDate,
            readDate = testDate,
            edition = EDITION_3,
            isbn = ISBN_COMPLETE,
            web = WEB_COMPLETE
        )
        bookDao.insertBook(book)

        // Act: Launch fragment for editing
        val args = Bundle().apply {
            putLong(KEY_BOOK_ID, ID_304)
            putBoolean(KEY_IS_COPY, false)
        }
        launchFragmentInHiltContainer<EditBookFragment>(fragmentArgs = args)

        // Assert: Form initializes successfully with all data
        onView(withId(R.id.book_detail_container)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
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

    @DisplayName("On View Created - All DB Constants Fields - Base Fields Visible")
    @Test
    fun onViewCreated_allDbConstantsFields_baseFieldsVisible() = runTest {
        launchNewEditBookFragment()

        onView(isRoot()).perform(waitFor(withHint(R.string.fld_title), TIMEOUT_5000))
        onView(withHint(R.string.fld_title)).check(matches(isDisplayed()))

        val authorField = DbConstants.FIELDS.first { it.id == DbConstants.FLD_AUTHOR }
        val authorTitle = authorField.name.substringAfter("|", authorField.name)
        val authorHint = authorField.name.substringBefore("|", authorField.name)

        onView(withText(authorTitle)).check(matches(isDisplayed()))
        onView(withHint(authorHint)).check(matches(isDisplayed()))
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
        Shadows.shadowOf(getMainLooper()).idle()
        
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

    @DisplayName("On Add Field Clicked - Opens Hidden Fields Popup Menu")
    @Test
    fun onAddFieldClicked_opensHiddenFieldsPopupMenu() = runTest {
        var fragment: EditBookFragment? = null
        launchNewEditBookFragment {
            fragment = this
        }

        onView(isRoot()).perform(waitFor(withHint(R.string.fld_title), TIMEOUT_5000))
        assertEquals(hiddenFieldLabels(), fragment!!.hiddenFieldsPopupMenu().hiddenFieldsPopupMenuLabels())
    }

    @DisplayName("On Missing Field Selected - Adds Every Hidden DB Constants Field To Form")
    @Test
    fun onMissingFieldSelected_addsEveryHiddenFieldToForm() = runTest {
        DbConstants.FIELDS
            .filterNot { it.isVisible }
            .forEach { field ->
                val fieldLabel = field.name.displayLabel()
                var fragment: EditBookFragment? = null
                launchNewEditBookFragment {
                    fragment = this
                }
                onView(isRoot()).perform(waitFor(withHint(R.string.fld_title), TIMEOUT_5000))
                fragment!!.hiddenFieldsPopupMenu().performFieldSelection(fieldLabel)
                Shadows.shadowOf(getMainLooper()).idle()

                onView(isRoot()).perform(waitFor(allOf(withId(R.id.tv_title), withText(fieldLabel)), TIMEOUT_5000))
                onView(allOf(withId(R.id.tv_title), withText(fieldLabel))).check(matches(isDisplayed()))
            }
    }

    private fun launchNewEditBookFragment(action: EditBookFragment.() -> Unit = {}) {
        val args = Bundle().apply {
            putLong(KEY_BOOK_ID, ID_0)
            putBoolean(KEY_IS_COPY, false)
        }
        launchFragmentInHiltContainer<EditBookFragment>(fragmentArgs = args, action = action)
        Shadows.shadowOf(getMainLooper()).idle()
    }

    private fun String.displayLabel(): String = substringAfter("|", this)

    private fun hiddenFieldLabels(): List<String> {
        return DbConstants.FIELDS.filterNot { it.isVisible }.map { it.name.displayLabel() }
    }

    private fun EditBookFragment.hiddenFieldsPopupMenu(): PopupMenu {
        val field = EditBookFragment::class.java.getDeclaredField("hiddenFieldsPopupMenu")
        field.isAccessible = true
        return field.get(this) as PopupMenu
    }

    private fun PopupMenu.hiddenFieldsPopupMenuLabels(): List<String> {
        return (0 until menu.size).map { menu.getItem(it).title.toString() }
    }

    private fun PopupMenu.performFieldSelection(fieldLabel: String) {
        val menuItem = (0 until menu.size)
            .asSequence()
            .map { menu.getItem(it) }
            .first { it.title.toString() == fieldLabel }

        menu.performIdentifierAction(menuItem.itemId, 0)
    }

    companion object {
        private const val KEY_BOOK_ID = "bookID"
        private const val KEY_IS_COPY = "isCopy"

        private const val ID_0 = 0L
        private const val ID_301 = 301L
        private const val ID_302 = 302L
        private const val ID_303 = 303L
        private const val ID_304 = 304L

        private const val TITLE_EDIT = "Book to Edit"
        private const val TITLE_COPY = "Book to Copy"

        private const val DESC_EDIT = "Edit this book"
        private const val DESC_COPY = "Copy this"

        private const val VOL_1 = 1
        private const val VOL_2 = 2
        private const val VOL_5 = 5

        private const val PUB_DATE_2022 = 2022
        private const val PUB_DATE_2023 = 2023
        private const val PUB_DATE_2024 = 2024

        private const val PAGES_300 = 300
        private const val PAGES_400 = 400
        private const val PAGES_500 = 500

        private const val PRICE_3000_1 = "3000|1"
        private const val PRICE_4000_1 = "4000|1"
        private const val PRICE_5000_2 = "5000|2"

        private const val EDITION_1 = 1
        private const val EDITION_2 = 2
        private const val EDITION_3 = 3

        private const val ISBN_EDIT = "4444444444"
        private const val ISBN_COPY = "5555555555"
        private const val ISBN_COMPLETE = "9999999999"

        private const val WEB_EDIT = "https://editable.com"
        private const val WEB_COPY = "https://copyable.com"
        private const val WEB_COMPLETE = "https://completebook.com"

        private const val DATE_ZERO = 0

        private const val TIMEOUT_5000 = 5000L

        private const val MSG_FIND_EDITTEXT = "Should find at least one EditText (Title)"
        private const val MSG_TITLE_ERROR = "Title should have error"

        private const val TITLE_COMPLETE = "Complete Book Data"
        private const val DESC_COMPLETE = "Full description text"
    }
}
