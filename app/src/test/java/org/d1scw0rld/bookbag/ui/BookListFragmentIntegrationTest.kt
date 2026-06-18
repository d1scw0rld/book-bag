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
import org.d1scw0rld.bookbag.data.dao.BookDao
import org.d1scw0rld.bookbag.data.entity.BookEntity
import org.d1scw0rld.bookbag.launchFragmentInHiltContainer
import org.d1scw0rld.bookbag.waitFor
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import javax.inject.Inject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue

@HiltAndroidTest
@RunWith(DisplayNameRobolectricRunner::class)
@Config(application = HiltTestApplication::class, sdk = [28])
class BookListFragmentIntegrationTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var bookDao: BookDao

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun onViewCreated_initialState_loadsBooksFromViewModel() = runTest {
        launchFragmentInHiltContainer<BookListFragment>()
        onView(isRoot()).perform(waitFor(withText("0 books"), 2000))
        onView(withText("0 books")).check(matches(isDisplayed()))
    }

    @Test
    fun onViewCreated_withBooksInDatabase_displaysTotalCount() = runTest {
        val book1 = BookEntity(
            id = 101L,
            title = "Testing Book",
            description = "A test book",
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
        val book2 = BookEntity(
            id = 102L,
            title = "Another Book",
            description = "Another test book",
            volume = 1,
            publicationDate = 2023,
            pages = 150,
            price = "1500|1",
            value = "1500|1",
            dueDate = 0,
            readDate = 0,
            edition = 1,
            isbn = "67890",
            web = ""
        )
        bookDao.insertBook(book1)
        bookDao.insertBook(book2)

        launchFragmentInHiltContainer<BookListFragment>()
        onView(isRoot()).perform(waitFor(withText("2 books"), 3000))
        onView(withText("2 books")).check(matches(isDisplayed()))
    }

    @Test
    fun onSearchQueryChanged_adapterSupportsFilterMethod() = runTest {
        val book1 = BookEntity(
            id = 101L,
            title = "Testing Book",
            description = "A test book",
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
        val book2 = BookEntity(
            id = 102L,
            title = "Other Book",
            description = "Another test book",
            volume = 1,
            publicationDate = 2023,
            pages = 150,
            price = "1500|1",
            value = "1500|1",
            dueDate = 0,
            readDate = 0,
            edition = 1,
            isbn = "67890",
            web = ""
        )
        bookDao.insertBook(book1)
        bookDao.insertBook(book2)

        launchFragmentInHiltContainer<BookListFragment> {
            val adapterField = this::class.java.getDeclaredField("booksAdapter")
            adapterField.isAccessible = true
            val adapter = adapterField.get(this)

            // Verify adapter has filter method
            val filterMethod = adapter.javaClass.getMethod("filter", String::class.java)
            assertTrue("Adapter should have filter method", filterMethod != null)

            // Verify adapter can be expanded
            val expandMethod = adapter.javaClass.getMethod("expandAll")
            assertTrue("Adapter should have expandAll method", expandMethod != null)
        }
    }

    @Test
    fun onToolbarActionClicked_backupOrRestore_launchesFileSelectorDialog() = runTest {
        launchFragmentInHiltContainer<BookListFragment> {
            val method = this::class.java.getDeclaredMethod("showImportDbDialog")
            method.isAccessible = true
            method.invoke(this)

            val field = this::class.java.getDeclaredField("fileSelectorDialog")
            field.isAccessible = true
            val dialog = field.get(this)
            assertTrue("FileSelectorDialog should be created for import", dialog != null)
        }
    }

    @Test
    fun onToolbarActionClicked_export_launchesFileSelectorDialog() = runTest {
        launchFragmentInHiltContainer<BookListFragment> {
            val method = this::class.java.getDeclaredMethod("showExportDbDialog")
            method.isAccessible = true
            method.invoke(this)

            val field = this::class.java.getDeclaredField("fileSelectorDialog")
            field.isAccessible = true
            val dialog = field.get(this)
            assertTrue("FileSelectorDialog should be created for export", dialog != null)
        }
    }
}
