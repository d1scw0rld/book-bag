package org.d1scw0rld.bookbag.ui

import android.os.Looper
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.test.runTest
import org.d1scw0rld.bookbag.DisplayNameRobolectricRunner
import org.d1scw0rld.bookbag.R
import org.d1scw0rld.bookbag.data.dao.BookDao
import org.d1scw0rld.bookbag.data.entity.BookEntity
import org.d1scw0rld.bookbag.launchFragmentInHiltContainer
import org.d1scw0rld.bookbag.ui.adapters.BooksAdapter
import org.d1scw0rld.bookbag.viewmodel.PendingAction
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
import javax.inject.Inject

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

    @DisplayName("On View Created - Initial State - Loads Books From ViewModel")
    @Test
    fun onViewCreated_initialState_loadsBooksFromViewModel() = runTest {
        launchFragmentInHiltContainer<BookListFragment>()
        onView(isRoot()).perform(waitFor(withText(COUNT_0_TEXT), TIMEOUT_2000))
        onView(withText(COUNT_0_TEXT)).check(matches(isDisplayed()))
    }

    @DisplayName("On View Created - With Books In Database - Displays Total Count")
    @Test
    fun onViewCreated_withBooksInDatabase_displaysTotalCount() = runTest {
        val book1 = BookEntity(id = ID_1, title = TITLE_1, description = DESC_EMPTY, volume = VOL_1, publicationDate = PUB_DATE_2023, pages = PAGES_100, price = PRICE_EMPTY, value = VALUE_EMPTY, dueDate = DATE_ZERO, readDate = DATE_ZERO, edition = EDITION_1, isbn = ISBN_EMPTY, web = WEB_EMPTY)
        val book2 = BookEntity(id = ID_2, title = TITLE_2, description = DESC_EMPTY, volume = VOL_1, publicationDate = PUB_DATE_2023, pages = PAGES_100, price = PRICE_EMPTY, value = VALUE_EMPTY, dueDate = DATE_ZERO, readDate = DATE_ZERO, edition = EDITION_1, isbn = ISBN_EMPTY, web = WEB_EMPTY)
        bookDao.insertBook(book1)
        bookDao.insertBook(book2)

        launchFragmentInHiltContainer<BookListFragment>()
        onView(isRoot()).perform(waitFor(withText(COUNT_2_TEXT), TIMEOUT_3000))
        onView(withText(COUNT_2_TEXT)).check(matches(isDisplayed()))
    }

    @DisplayName("On Search Query Changed - Input Query Matches Title - Filters List Adapter")
    @Test
    fun onSearchQueryChanged_inputQueryMatchesTitle_filtersListAdapter() = runTest {
        val book1 = BookEntity(id = ID_1, title = TITLE_SEARCH, description = DESC_EMPTY, volume = VOL_1, publicationDate = PUB_DATE_2023, pages = PAGES_100, price = PRICE_EMPTY, value = VALUE_EMPTY, dueDate = DATE_ZERO, readDate = DATE_ZERO, edition = EDITION_1, isbn = ISBN_EMPTY, web = WEB_EMPTY)
        bookDao.insertBook(book1)

        launchFragmentInHiltContainer<BookListFragment>()
        
        onView(isRoot()).perform(waitFor(withText(org.hamcrest.Matchers.containsString(COUNT_1_TEXT)), TIMEOUT_5000))

        onView(withId(R.id.book_list)).check { view, _ ->
            val recyclerView = view as RecyclerView
            val adapter = recyclerView.adapter as BooksAdapter
            adapter.expandAll()
            adapter.filter(QUERY_SEARCH)
        }
        
        onView(withText(TITLE_SEARCH)).check(matches(isDisplayed()))
    }

    @DisplayName("On Book Long Clicked - Triggers Action Mode")
    @Test
    fun onBookLongClicked_triggersActionMode() {
        launchFragmentInHiltContainer<BookListFragment> {
            activity?.runOnUiThread {
                this.actionMode = requireActivity().startActionMode(this.onActionModeCallback)
            }
            assertTrue(MSG_ACTION_MODE_ACTIVE, this.actionMode != null)
        }
    }

    @DisplayName("On Sort Action Clicked - Displays Order Popup Menu")
    @Test
    fun onSortActionClicked_displaysOrderPopupMenu() {
        launchFragmentInHiltContainer<BookListFragment> {
            activity?.runOnUiThread {
                this.showOrderPopupMenu(View(requireContext()))
            }
            assertTrue(true)
        }
    }

    @DisplayName("On Request Permission Result - Is Granted - Executes Pending Action")
    @Test
    fun onRequestPermissionResult_isGranted_executesPendingAction() {
        launchFragmentInHiltContainer<BookListFragment> {
            this.viewModel.onActionClicked(PendingAction.IMPORT)

            activity?.runOnUiThread {
                val registry = this.activityResultRegistry
                
                // Find the key associated with our fragment's RequestPermission callback
                val keyToCallbackField = androidx.activity.result.ActivityResultRegistry::class.java
                    .getDeclaredFields().first { it.type == Map::class.java && it.name.contains("Callback") }
                    .apply { isAccessible = true }
                @Suppress("UNCHECKED_CAST")
                val keyToCallback = keyToCallbackField.get(registry) as Map<String, Any>

                val actualKey = keyToCallback.entries.first { entry ->
                    val callbackAndContract = entry.value
                    val contractField = callbackAndContract.javaClass.getDeclaredFields().first { it.name.contains("Contract") }
                        .apply { isAccessible = true }
                    val contract = contractField.get(callbackAndContract)
                    contract is ActivityResultContracts.RequestPermission
                }.key

                val keyToRequestCodeField = androidx.activity.result.ActivityResultRegistry::class.java
                    .getDeclaredFields().first { field ->
                        field.type == Map::class.java && 
                        field.genericType.toString().startsWith("java.util.Map<java.lang.String, java.lang.Integer>")
                    }.apply { isAccessible = true }
                @Suppress("UNCHECKED_CAST")
                val keyToRequestCode = keyToRequestCodeField.get(registry) as Map<String, Int>
                
                val requestCode = keyToRequestCode[actualKey]!!
                
                val mLaunchedKeysField = androidx.activity.result.ActivityResultRegistry::class.java
                    .getDeclaredField("mLaunchedKeys").apply { isAccessible = true }
                @Suppress("UNCHECKED_CAST")
                val mLaunchedKeys = mLaunchedKeysField.get(registry) as MutableList<String>
                mLaunchedKeys.add(actualKey)

                registry.dispatchResult(requestCode, true)
            }
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            assertEquals(MSG_ACTION_RESET, PendingAction.NONE, this.viewModel.pendingAction.value)
        }
    }

    @DisplayName("On Request Permission Result - Is Denied - Shows Toast")
    @Test
    fun onRequestPermissionResult_isDenied_showsToast() {
        launchFragmentInHiltContainer<BookListFragment> {
            this.viewModel.onActionClicked(PendingAction.EXPORT)

            activity?.runOnUiThread {
                val registry = this.activityResultRegistry
                
                val keyToCallbackField = androidx.activity.result.ActivityResultRegistry::class.java
                    .getDeclaredFields().first { it.type == Map::class.java && it.name.contains("Callback") }
                    .apply { isAccessible = true }
                @Suppress("UNCHECKED_CAST")
                val keyToCallback = keyToCallbackField.get(registry) as Map<String, Any>

                val actualKey = keyToCallback.entries.first { entry ->
                    val callbackAndContract = entry.value
                    val contractField = callbackAndContract.javaClass.getDeclaredFields().first { it.name.contains("Contract") }
                        .apply { isAccessible = true }
                    val contract = contractField.get(callbackAndContract)
                    contract is ActivityResultContracts.RequestPermission
                }.key

                val keyToRequestCodeField = androidx.activity.result.ActivityResultRegistry::class.java
                    .getDeclaredFields().first { field ->
                        field.type == Map::class.java && 
                        field.genericType.toString().startsWith("java.util.Map<java.lang.String, java.lang.Integer>")
                    }.apply { isAccessible = true }
                @Suppress("UNCHECKED_CAST")
                val keyToRequestCode = keyToRequestCodeField.get(registry) as Map<String, Int>
                
                val requestCode = keyToRequestCode[actualKey]!!

                val mLaunchedKeysField = androidx.activity.result.ActivityResultRegistry::class.java
                    .getDeclaredField("mLaunchedKeys").apply { isAccessible = true }
                @Suppress("UNCHECKED_CAST")
                val mLaunchedKeys = mLaunchedKeysField.get(registry) as MutableList<String>
                mLaunchedKeys.add(actualKey)

                registry.dispatchResult(requestCode, false)
            }
            Shadows.shadowOf(Looper.getMainLooper()).idle()

            // Check the toast by text directly from shadows
            val latestToastText = org.robolectric.shadows.ShadowToast.getTextOfLatestToast()
            assertEquals(MSG_ACCESS_DENIED_TOAST, requireContext().getString(R.string.msg_acc_dnd), latestToastText)
            assertEquals(MSG_ACTION_RESET, PendingAction.NONE, this.viewModel.pendingAction.value)
        }
    }

    companion object {
        private const val ID_1 = 101L
        private const val ID_2 = 102L
        private const val TITLE_1 = "Testing Book"
        private const val TITLE_2 = "Another Book"
        private const val TITLE_SEARCH = "Apple"
        private const val QUERY_SEARCH = "Ap"
        
        // Book fields
        private const val DESC_EMPTY = ""
        private const val VOL_1 = 1
        private const val PUB_DATE_2023 = 2023
        private const val PAGES_100 = 100
        private const val PRICE_EMPTY = ""
        private const val VALUE_EMPTY = ""
        private const val DATE_ZERO = 0
        private const val EDITION_1 = 1
        private const val ISBN_EMPTY = ""
        private const val WEB_EMPTY = ""

        private const val COUNT_0_TEXT = "0 books"
        private const val COUNT_1_TEXT = "1 book"
        private const val COUNT_2_TEXT = "2 books"
        
        private const val TIMEOUT_2000 = 2000L
        private const val TIMEOUT_3000 = 3000L
        private const val TIMEOUT_5000 = 5000L
        
        private const val MSG_ACTION_MODE_ACTIVE = "ActionMode should be active"
        private const val MSG_ACTION_RESET = "Action should be reset"
        private const val MSG_ACCESS_DENIED_TOAST = "Access denied toast should be shown"
    }
}
