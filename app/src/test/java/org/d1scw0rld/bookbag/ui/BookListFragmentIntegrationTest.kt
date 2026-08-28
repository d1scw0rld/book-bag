package org.d1scw0rld.bookbag.ui

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
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
import org.d1scw0rld.bookbag.HiltTestActivity
import org.d1scw0rld.bookbag.R
import org.d1scw0rld.bookbag.data.dao.BookDao
import org.d1scw0rld.bookbag.data.DbConstants
import org.d1scw0rld.bookbag.data.entity.BookEntity
import org.d1scw0rld.bookbag.data.entity.BookFieldCrossRef
import org.d1scw0rld.bookbag.data.entity.FieldEntity
import org.d1scw0rld.bookbag.launchFragmentInHiltContainer
import org.d1scw0rld.bookbag.ui.fileselector.FileOperation
import org.d1scw0rld.bookbag.ui.fileselector.FileSelectorDialog
import org.d1scw0rld.bookbag.waitFor
import org.hamcrest.Matchers.allOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowDialog
import org.robolectric.shadows.ShadowToast
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

    @DisplayName("On View Created - Initial State - Displays Zero Books Count")
    @Test
    fun onViewCreated_initialState_displaysZeroBooksCount() = runTest {
        launchFragmentInHiltContainer<BookListFragment>()
        onView(isRoot()).perform(waitFor(withText(COUNT_0_TEXT), TIMEOUT_2000))
        onView(withText(COUNT_0_TEXT)).check(matches(isDisplayed()))
    }

    @DisplayName("On View Created - With Books In Database - Expands Groups And Displays Count And Titles")
    @Test
    fun onViewCreated_withBooks_expandsGroupsAndDisplaysCountAndTitles() = runTest {
        val book1 = BookEntity(id = ID_1, title = TITLE_1, description = DESC_EMPTY, volume = VOL_1, publicationDate = PUB_DATE_2023, pages = PAGES_100, price = PRICE_EMPTY, value = VALUE_EMPTY, dueDate = DATE_ZERO, readDate = DATE_ZERO, edition = EDITION_1, isbn = ISBN_EMPTY, web = WEB_EMPTY)
        val book2 = BookEntity(id = ID_2, title = TITLE_2, description = DESC_EMPTY, volume = VOL_1, publicationDate = PUB_DATE_2023, pages = PAGES_100, price = PRICE_EMPTY, value = VALUE_EMPTY, dueDate = DATE_ZERO, readDate = DATE_ZERO, edition = EDITION_1, isbn = ISBN_EMPTY, web = WEB_EMPTY)
        bookDao.insertBook(book1)
        bookDao.insertBook(book2)

        launchFragmentInHiltContainer<BookListFragment>()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(isRoot()).perform(waitFor(withText(COUNT_2_TEXT), TIMEOUT_3000))
        onView(withText(COUNT_2_TEXT)).check(matches(isDisplayed()))

        onView(allOf(withId(R.id.tv_header), withText("A"))).perform(click())
        onView(allOf(withId(R.id.tv_header), withText("T"))).perform(click())
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView(isRoot()).perform(waitFor(withText(TITLE_1), TIMEOUT_3000))
        onView(withText(TITLE_1)).check(matches(isDisplayed()))
        onView(withText(TITLE_2)).check(matches(isDisplayed()))
    }

    @DisplayName("On Search Query Changed - Displays Matching Books Only")
    @Test
    fun onSearchQueryChanged_displaysMatchingBooksOnly() = runTest {
        val book1 = BookEntity(id = ID_1, title = TITLE_SEARCH, description = DESC_EMPTY, volume = VOL_1, publicationDate = PUB_DATE_2023, pages = PAGES_100, price = PRICE_EMPTY, value = VALUE_EMPTY, dueDate = DATE_ZERO, readDate = DATE_ZERO, edition = EDITION_1, isbn = ISBN_EMPTY, web = WEB_EMPTY)
        val book2 = BookEntity(id = ID_2, title = TITLE_OTHER, description = DESC_EMPTY, volume = VOL_1, publicationDate = PUB_DATE_2023, pages = PAGES_100, price = PRICE_EMPTY, value = VALUE_EMPTY, dueDate = DATE_ZERO, readDate = DATE_ZERO, edition = EDITION_1, isbn = ISBN_EMPTY, web = WEB_EMPTY)
        val book3 = BookEntity(id = ID_3, title = TITLE_OTHER, description = DESC_EMPTY, volume = VOL_1, publicationDate = PUB_DATE_2023, pages = PAGES_100, price = PRICE_EMPTY, value = VALUE_EMPTY, dueDate = DATE_ZERO, readDate = DATE_ZERO, edition = EDITION_1, isbn = ISBN_EMPTY, web = WEB_EMPTY)
        bookDao.insertBook(book1)
        bookDao.insertBook(book2)
        bookDao.insertBook(book3)

        launchFragmentInHiltContainer<BookListFragment>()
        onView(isRoot()).perform(waitFor(withText(COUNT_3_TEXT), TIMEOUT_5000))
        setSearchQueryViaSearchView(QUERY_SEARCH)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView(isRoot()).perform(waitFor(withText(COUNT_1_TEXT), TIMEOUT_5000))
        onView(withText(TITLE_SEARCH)).check(matches(isDisplayed()))
        onView(withText(COUNT_1_TEXT)).check(matches(isDisplayed()))
        onView(withText(TITLE_OTHER)).check(doesNotExistOrNotDisplayed())
    }

    @DisplayName("On Search Query Changed - With No Matches - Shows No Books")
    @Test
    fun onSearchQueryChanged_withNoMatches_showsNoBooks() = runTest {
        val book1 = BookEntity(id = ID_1, title = TITLE_SEARCH, description = DESC_EMPTY, volume = VOL_1, publicationDate = PUB_DATE_2023, pages = PAGES_100, price = PRICE_EMPTY, value = VALUE_EMPTY, dueDate = DATE_ZERO, readDate = DATE_ZERO, edition = EDITION_1, isbn = ISBN_EMPTY, web = WEB_EMPTY)
        val book2 = BookEntity(id = ID_2, title = TITLE_OTHER, description = DESC_EMPTY, volume = VOL_1, publicationDate = PUB_DATE_2023, pages = PAGES_100, price = PRICE_EMPTY, value = VALUE_EMPTY, dueDate = DATE_ZERO, readDate = DATE_ZERO, edition = EDITION_1, isbn = ISBN_EMPTY, web = WEB_EMPTY)
        val book3 = BookEntity(id = ID_3, title = TITLE_OTHER, description = DESC_EMPTY, volume = VOL_1, publicationDate = PUB_DATE_2023, pages = PAGES_100, price = PRICE_EMPTY, value = VALUE_EMPTY, dueDate = DATE_ZERO, readDate = DATE_ZERO, edition = EDITION_1, isbn = ISBN_EMPTY, web = WEB_EMPTY)
        bookDao.insertBook(book1)
        bookDao.insertBook(book2)
        bookDao.insertBook(book3)

        launchFragmentInHiltContainer<BookListFragment>()
        onView(isRoot()).perform(waitFor(withText(COUNT_3_TEXT), TIMEOUT_5000))
        setSearchQueryViaSearchView(QUERY_NO_MATCH)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView(isRoot()).perform(waitFor(withText(COUNT_0_TEXT), TIMEOUT_5000))
        onView(withText(COUNT_0_TEXT)).check(matches(isDisplayed()))
        onView(withText(TITLE_SEARCH)).check(doesNotExistOrNotDisplayed())
        onView(withText(TITLE_OTHER)).check(doesNotExistOrNotDisplayed())
    }

    @DisplayName("On Book Long Clicked - Shows Selection Actions")
    @Test
    fun onBookLongClicked_showsSelectionActions() {
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
            var popupMenu: PopupMenu? = null
            activity?.runOnUiThread {
                popupMenu = this.showOrderPopupMenu(View(requireContext()))
            }

            val menu = popupMenu?.menu
            assertTrue(MSG_ORDER_MENU_SHOWN, menu != null)

            val expectedItems = expectedOrderItems(requireContext())
            assertEquals(MSG_ORDER_MENU_SIZE, expectedItems.size, menu!!.size())
            expectedItems.forEachIndexed { index, (expectedId, expectedTitle) ->
                val menuItem = menu.getItem(index)
                assertEquals(MSG_ORDER_MENU_ITEM_ID, expectedId, menuItem.itemId)
                assertEquals(MSG_ORDER_MENU_ITEM_TITLE, expectedTitle, menuItem.title.toString())
                assertTrue(MSG_ORDER_MENU_CHECKABLE, menuItem.isCheckable)
            }
            assertTrue(MSG_ORDER_MENU_CHECKED, menu.findItem(DbConstants.SRT_TTL).isChecked)
        }
    }

    @DisplayName("On Order Changed - Sort By Title - Groups By Title Initial With Authors")
    @Test
    fun onOrderChanged_sortByTitle_groupsByTitleInitialWithAuthors() = runTest {
        insertBookWithFields(ID_1, TITLE_1, author = AUTHOR_1)
        insertBookWithFields(ID_2, TITLE_2, author = AUTHOR_2)

        val scenario = launchFragmentInHiltContainer<BookListFragment>()
        applyOrder(scenario, DbConstants.SRT_TTL)
        onView(isRoot()).perform(waitFor(withText(COUNT_2_TEXT), TIMEOUT_5000))

        onView(withText(HEADER_A)).check(matches(isDisplayed()))
        onView(withText(HEADER_T)).check(matches(isDisplayed()))

        expandAll(scenario)
        onView(isRoot()).perform(waitFor(withText("$TITLE_1 - $AUTHOR_1"), TIMEOUT_5000))
        onView(withText("$TITLE_1 - $AUTHOR_1")).check(matches(isDisplayed()))
        onView(withText("$TITLE_2 - $AUTHOR_2")).check(matches(isDisplayed()))
        assertRowOrder(
            scenario,
            HEADER_A,
            "$TITLE_2 - $AUTHOR_2",
            HEADER_T,
            "$TITLE_1 - $AUTHOR_1"
        )
    }

    @DisplayName("On Order Changed - Sort By Author - Groups By Author With Plain Titles")
    @Test
    fun onOrderChanged_sortByAuthor_groupsByAuthorWithPlainTitles() = runTest {
        insertBookWithFields(ID_1, TITLE_1, author = AUTHOR_1)
        insertBookWithFields(ID_2, TITLE_2, author = AUTHOR_2)

        val scenario = launchFragmentInHiltContainer<BookListFragment>()
        applyOrder(scenario, DbConstants.SRT_AUT)
        onView(isRoot()).perform(waitFor(withText(AUTHOR_1), TIMEOUT_5000))

        onView(withText(AUTHOR_1)).check(matches(isDisplayed()))
        onView(withText(AUTHOR_2)).check(matches(isDisplayed()))
        onView(withText(COUNT_2_TEXT)).check(matches(isDisplayed()))

        expandAll(scenario)
        onView(isRoot()).perform(waitFor(withText(TITLE_1), TIMEOUT_5000))
        onView(withText(TITLE_1)).check(matches(isDisplayed()))
        onView(withText(TITLE_2)).check(matches(isDisplayed()))
        assertRowOrder(scenario, AUTHOR_2, TITLE_2, AUTHOR_1, TITLE_1)
    }

    @DisplayName("On Order Changed - Sort By Author With Shared Author - Groups Both Books Under One Header")
    @Test
    fun onOrderChanged_sortByAuthorWithSharedAuthor_groupsBothBooksUnderOneHeader() = runTest {
        insertBookWithFields(ID_1, TITLE_1, author = AUTHOR_1)
        insertBookWithFields(ID_2, TITLE_2, author = AUTHOR_1)

        val scenario = launchFragmentInHiltContainer<BookListFragment>()
        applyOrder(scenario, DbConstants.SRT_AUT)
        onView(isRoot()).perform(waitFor(withText(AUTHOR_1), TIMEOUT_5000))

        onView(withText(AUTHOR_1)).check(matches(isDisplayed()))
        onView(withText(COUNT_2_TEXT)).check(matches(isDisplayed()))

        expandAll(scenario)
        onView(isRoot()).perform(waitFor(withText(TITLE_2), TIMEOUT_5000))
        onView(withText(TITLE_1)).check(matches(isDisplayed()))
        onView(withText(TITLE_2)).check(matches(isDisplayed()))
        assertRowOrder(scenario, AUTHOR_1, TITLE_2, TITLE_1)
    }

    @DisplayName("On Order Changed - Sort By Author Without Authors - Groups Under Missing Header")
    @Test
    fun onOrderChanged_sortByAuthorWithoutAuthors_groupsUnderMissingHeader() = runTest {
        insertBooks(TITLE_1, TITLE_2)

        val scenario = launchFragmentInHiltContainer<BookListFragment>()
        applyOrder(scenario, DbConstants.SRT_AUT)
        onView(isRoot()).perform(waitFor(withText(HEADER_MISSING), TIMEOUT_5000))

        onView(withText(HEADER_MISSING)).check(matches(isDisplayed()))
        onView(withText(COUNT_2_TEXT)).check(matches(isDisplayed()))
        onView(withText(HEADER_T)).check(doesNotExistOrNotDisplayed())

        expandAll(scenario)
        onView(isRoot()).perform(waitFor(withText(TITLE_2), TIMEOUT_5000))
        assertRowOrder(scenario, HEADER_MISSING, TITLE_2, TITLE_1)
    }

    @DisplayName("On Order Changed - Sort By Wanted Title - Excludes Read And In Bag Books")
    @Test
    fun onOrderChanged_sortByWantedTitle_excludesReadAndInBagBooks() = runTest {
        insertBookWithFields(ID_1, TITLE_1, status = STATUS_WANTED)
        insertBookWithFields(ID_2, TITLE_2, status = STATUS_IN_BAG)
        insertBookWithFields(ID_3, TITLE_SEARCH, status = STATUS_READ)

        val scenario = launchFragmentInHiltContainer<BookListFragment>()
        applyOrder(scenario, DbConstants.SRT_WNT_PBL_TTL)
        onView(isRoot()).perform(waitFor(withText(STATUS_WANTED), TIMEOUT_5000))

        onView(withText(STATUS_WANTED)).check(matches(isDisplayed()))
        onView(withText(COUNT_1_TEXT)).check(matches(isDisplayed()))
        onView(withText(STATUS_IN_BAG)).check(doesNotExistOrNotDisplayed())
        onView(withText(STATUS_READ)).check(doesNotExistOrNotDisplayed())

        expandAll(scenario)
        onView(isRoot()).perform(waitFor(withText(TITLE_1), TIMEOUT_5000))
        onView(withText(TITLE_1)).check(matches(isDisplayed()))
        onView(withText(TITLE_2)).check(doesNotExistOrNotDisplayed())
        assertRowOrder(scenario, STATUS_WANTED, TITLE_1)
    }

    @DisplayName("On Order Changed - Sort Order Reapplied - Regroups Existing Books")
    @Test
    fun onOrderChanged_sortOrderReapplied_regroupsExistingBooks() = runTest {
        insertBookWithFields(ID_1, TITLE_1, author = AUTHOR_1)
        insertBookWithFields(ID_2, TITLE_2, author = AUTHOR_2)

        val scenario = launchFragmentInHiltContainer<BookListFragment>()
        applyOrder(scenario, DbConstants.SRT_AUT)
        onView(isRoot()).perform(waitFor(withText(AUTHOR_1), TIMEOUT_5000))
        onView(withText(AUTHOR_1)).check(matches(isDisplayed()))

        applyOrder(scenario, DbConstants.SRT_TTL)
        onView(isRoot()).perform(waitFor(withText(HEADER_T), TIMEOUT_5000))

        onView(withText(HEADER_T)).check(matches(isDisplayed()))
        onView(withText(HEADER_A)).check(matches(isDisplayed()))
        onView(withText(AUTHOR_1)).check(doesNotExistOrNotDisplayed())
        onView(withText(AUTHOR_2)).check(doesNotExistOrNotDisplayed())
        assertRowOrder(scenario, HEADER_A, HEADER_T)

        expandAll(scenario)
        onView(isRoot()).perform(waitFor(withText("$TITLE_1 - $AUTHOR_1"), TIMEOUT_5000))
        assertRowOrder(
            scenario,
            HEADER_A,
            "$TITLE_2 - $AUTHOR_2",
            HEADER_T,
            "$TITLE_1 - $AUTHOR_1"
        )
    }

    @DisplayName("Menu Import - Import Option Selected - Shows Load File Dialog")
    @Test
    fun menuImport_importOptionSelected_showsLoadFileDialog() {
        grantStoragePermission()

        val scenario = launchFragmentInHiltContainer<BookListFragment>()
        var result = false
        scenario.onFragment { result = invokeOptionsItemSelect(this, R.id.action_imp_db) }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        assertTrue(MSG_MENU_IMPORT, result)
        assertEquals(MSG_ACTION_IMPORT, FileOperation.LOAD, shownFileOperation(scenario))
    }

    @DisplayName("Menu Export - Export Option Selected - Shows Save File Dialog")
    @Test
    fun menuExport_exportOptionSelected_showsSaveFileDialog() {
        grantStoragePermission()

        val scenario = launchFragmentInHiltContainer<BookListFragment>()
        var result = false
        scenario.onFragment { result = invokeOptionsItemSelect(this, R.id.action_exp_db) }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        assertTrue(MSG_MENU_EXPORT, result)
        assertEquals(MSG_ACTION_EXPORT, FileOperation.SAVE, shownFileOperation(scenario))
    }

    @DisplayName("Menu Expand All - Expand All Option Selected - Displays Book Titles")
    @Test
    fun menuExpandAll_expandAllOptionSelected_displaysBookTitles() = runTest {
        insertBooks(TITLE_1, TITLE_2)

        val scenario = launchFragmentInHiltContainer<BookListFragment>()
        onView(isRoot()).perform(waitFor(withText(COUNT_2_TEXT), TIMEOUT_3000))
        onView(withText(TITLE_1)).check(doesNotExistOrNotDisplayed())

        var result = false
        scenario.onFragment { result = invokeOptionsItemSelect(this, R.id.action_exp_all) }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        assertTrue(MSG_MENU_EXPAND_ALL, result)
        onView(isRoot()).perform(waitFor(withText(TITLE_1), TIMEOUT_3000))
        onView(withText(TITLE_1)).check(matches(isDisplayed()))
        onView(withText(TITLE_2)).check(matches(isDisplayed()))
    }

    @DisplayName("Menu Collapse All - Collapse All Option Selected - Hides Book Titles")
    @Test
    fun menuCollapseAll_collapseAllOptionSelected_hidesBookTitles() = runTest {
        insertBooks(TITLE_1, TITLE_2)

        val scenario = launchFragmentInHiltContainer<BookListFragment>()
        onView(isRoot()).perform(waitFor(withText(COUNT_2_TEXT), TIMEOUT_3000))
        scenario.onFragment { invokeOptionsItemSelect(this, R.id.action_exp_all) }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(isRoot()).perform(waitFor(withText(TITLE_1), TIMEOUT_3000))

        var result = false
        scenario.onFragment { result = invokeOptionsItemSelect(this, R.id.action_clp_all) }
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        assertTrue(MSG_MENU_COLLAPSE_ALL, result)
        onView(withText(TITLE_1)).check(doesNotExistOrNotDisplayed())
        onView(withText(TITLE_2)).check(doesNotExistOrNotDisplayed())
        onView(withText(HEADER_T)).check(matches(isDisplayed()))
        onView(withText(HEADER_A)).check(matches(isDisplayed()))
        onView(withText(COUNT_2_TEXT)).check(matches(isDisplayed()))
    }

    @DisplayName("Menu Sort - Sort Option Selected - Returns True")    @Test
    fun menuSort_sortOptionSelected_returnsTrue() {
        launchFragmentInHiltContainer<BookListFragment> {
            val menuItem = MenuBuilder(requireContext()).add(Menu.NONE, R.id.action_sort, Menu.NONE, "sort")
            val result = invokeOptionsItemSelect(this, menuItem.itemId)

            assertTrue(MSG_MENU_SORT, result)
        }
    }

    @DisplayName("On Order Item Selected - Different Order Chosen - Updates Displayed Order Label")
    @Test
    fun onOrderItemSelected_differentOrderChosen_updatesDisplayedOrderLabel() = runTest {
        insertBooks(TITLE_1, TITLE_2)

        val scenario = launchFragmentInHiltContainer<BookListFragment>()
        onView(isRoot()).perform(waitFor(withText(COUNT_2_TEXT), TIMEOUT_3000))

        val context: Context = ApplicationProvider.getApplicationContext()
        val defaultOrderTitle = context.getString(R.string.srt_title)
        val newOrderTitle = context.getString(R.string.srt_author)
        onView(withText(defaultOrderTitle)).check(matches(isDisplayed()))

        applyOrder(scenario, DbConstants.SRT_AUT)

        onView(isRoot()).perform(waitFor(withText(newOrderTitle), TIMEOUT_3000))
        onView(withText(newOrderTitle)).check(matches(isDisplayed()))
        onView(withText(defaultOrderTitle)).check(doesNotExistOrNotDisplayed())
    }

    @DisplayName("On View Created - With Single Book In Database - Displays Singular Count")
    @Test
    fun onViewCreated_withSingleBookInDatabase_displaysSingularCount() = runTest {
        insertBooks(TITLE_1)

        launchFragmentInHiltContainer<BookListFragment>()
        onView(isRoot()).perform(waitFor(withText(COUNT_1_TEXT), TIMEOUT_3000))
        onView(withText(COUNT_1_TEXT)).check(matches(isDisplayed()))
    }

    @DisplayName("On Search Query Changed - Query Cleared - Restores Full List")
    @Test
    fun onSearchQueryChanged_queryCleared_restoresFullList() = runTest {
        insertBooks(TITLE_SEARCH, TITLE_OTHER, TITLE_OTHER)

        launchFragmentInHiltContainer<BookListFragment>()
        onView(isRoot()).perform(waitFor(withText(COUNT_3_TEXT), TIMEOUT_5000))
        setSearchQueryViaSearchView(QUERY_SEARCH)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        onView(isRoot()).perform(waitFor(withText(COUNT_1_TEXT), TIMEOUT_5000))

        setSearchQueryViaSearchView(QUERY_EMPTY)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        onView(isRoot()).perform(waitFor(withText(COUNT_3_TEXT), TIMEOUT_5000))
        onView(withText(COUNT_3_TEXT)).check(matches(isDisplayed()))
        onView(withText(TITLE_SEARCH)).check(matches(isDisplayed()))
    }

    @DisplayName("On Request Permission Result - Is Granted - Shows Load File Dialog")
    @Test
    fun onRequestPermissionResult_isGranted_showsLoadFileDialog() {
        val scenario = launchFragmentInHiltContainer<BookListFragment>()
        scenario.onFragment { invokeOptionsItemSelect(this, R.id.action_imp_db) }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        confirmPermissionRationale()

        dispatchPermissionResult(scenario, isGranted = true)

        assertEquals(MSG_ACTION_IMPORT, FileOperation.LOAD, shownFileOperation(scenario))
    }

    @DisplayName("On Request Permission Result - Is Denied - Shows Toast And No Dialog")
    @Test
    fun onRequestPermissionResult_isDenied_showsToastAndNoDialog() {
        val scenario = launchFragmentInHiltContainer<BookListFragment>()
        scenario.onFragment { invokeOptionsItemSelect(this, R.id.action_exp_db) }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        confirmPermissionRationale()

        dispatchPermissionResult(scenario, isGranted = false)

        val context: Context = ApplicationProvider.getApplicationContext()
        assertEquals(
            MSG_ACCESS_DENIED_TOAST,
            context.getString(R.string.msg_acc_dnd),
            ShadowToast.getTextOfLatestToast()
        )
        assertEquals(MSG_NO_FILE_DIALOG, null, shownFileOperation(scenario))
    }

    private fun grantStoragePermission() {
        Shadows.shadowOf(ApplicationProvider.getApplicationContext<Application>())
            .grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun confirmPermissionRationale() {
        val dialog = ShadowDialog.getLatestDialog() as? AlertDialog
        assertTrue(MSG_RATIONALE_DIALOG, dialog != null && dialog.isShowing)
        dialog!!.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    private fun shownFileOperation(scenario: ActivityScenario<HiltTestActivity>): FileOperation? {
        var operation: FileOperation? = null
        scenario.onActivity { activity ->
            val dialog = activity.supportFragmentManager.fragments
                .filterIsInstance<FileSelectorDialog>()
                .firstOrNull { it.isAdded }
            @Suppress("DEPRECATION")
            operation = dialog?.arguments?.getSerializable(KEY_OPERATION) as? FileOperation
        }
        return operation
    }

    private fun dispatchPermissionResult(
        scenario: ActivityScenario<HiltTestActivity>,
        isGranted: Boolean
    ) {
        scenario.onActivity { activity ->
            val request = Shadows.shadowOf(activity).lastRequestedPermission
            assertTrue(MSG_PERMISSION_REQUESTED, request != null)
            assertTrue(
                MSG_STORAGE_PERMISSION_REQUESTED,
                request.requestedPermissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )

            val grantResult =
                if (isGranted) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED
            activity.onRequestPermissionsResult(
                request.requestCode,
                request.requestedPermissions,
                IntArray(request.requestedPermissions.size) { grantResult }
            )
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    private fun expectedOrderItems(context: Context): List<Pair<Int, String>> = listOf(
        DbConstants.SRT_TTL to context.getString(R.string.srt_title),
        DbConstants.SRT_AUT to context.getString(R.string.srt_author),
        DbConstants.SRT_WNT_PBL_TTL to context.getString(R.string.srt_wanted_pbl_ttl),
        DbConstants.SRT_WNT_PBL_AUT to context.getString(R.string.srt_wanted_pbl_aut),
        DbConstants.SRT_RD_AUT to context.getString(R.string.srt_read_aut),
        DbConstants.SRT_RD_TTL to context.getString(R.string.srt_read_ttl),
        DbConstants.SRT_NOT_RD_AUT to context.getString(R.string.srt_not_read_aut),
        DbConstants.SRT_NOT_RD_TTL to context.getString(R.string.srt_not_read_ttl),
        DbConstants.SRT_PBL_AUT to context.getString(R.string.srt_pbl_aut),
        DbConstants.SRT_PBL_TTL to context.getString(R.string.srt_pbl_ttl),
        DbConstants.SRT_LND_TTL to context.getString(R.string.srt_lnd_ttl),
        DbConstants.SRT_LND_BRW to context.getString(R.string.srt_lnd_brw)
    )

    private fun invokeOptionsItemSelect(fragment: BookListFragment, itemId: Int): Boolean {        val menu = MenuBuilder(fragment.requireContext())
        val item = menu.add(Menu.NONE, itemId, Menu.NONE, itemId.toString())
        val method = BookListFragment::class.java.getDeclaredMethod("optionsItemSelect", MenuItem::class.java)
        method.isAccessible = true
        return method.invoke(fragment, item) as Boolean
    }

    private fun setSearchQueryViaSearchView(query: String) {
        onView(withId(R.id.action_search)).check { view, _ ->
            val actionMenuItemView = view as ActionMenuItemView
            val searchView = actionMenuItemView.itemData.actionView as androidx.appcompat.widget.SearchView
            searchView.setQuery(query, true)
        }
    }

    private fun doesNotExistOrNotDisplayed() = doesNotExist()

    private suspend fun insertBooks(vararg titles: String) {
        titles.forEachIndexed { index, title ->
            bookDao.insertBook(
                BookEntity(
                    id = ID_1 + index,
                    title = title,
                    description = DESC_EMPTY,
                    volume = VOL_1,
                    publicationDate = PUB_DATE_2023,
                    pages = PAGES_100,
                    price = PRICE_EMPTY,
                    value = VALUE_EMPTY,
                    dueDate = DATE_ZERO,
                    readDate = DATE_ZERO,
                    edition = EDITION_1,
                    isbn = ISBN_EMPTY,
                    web = WEB_EMPTY
                )
            )
        }
    }

    private fun ActivityScenario<HiltTestActivity>.onFragment(action: BookListFragment.() -> Unit) {
        onActivity { activity ->
            val fragment = activity.supportFragmentManager
                .findFragmentByTag(FRAGMENT_TAG) as BookListFragment
            fragment.action()
        }
    }

    private suspend fun insertBookWithFields(
        id: Long,
        title: String,
        author: String? = null,
        status: String? = null
    ) {
        bookDao.insertBook(
            BookEntity(
                id = id,
                title = title,
                description = DESC_EMPTY,
                volume = VOL_1,
                publicationDate = PUB_DATE_2023,
                pages = PAGES_100,
                price = PRICE_EMPTY,
                value = VALUE_EMPTY,
                dueDate = DATE_ZERO,
                readDate = DATE_ZERO,
                edition = EDITION_1,
                isbn = ISBN_EMPTY,
                web = WEB_EMPTY
            )
        )
        author?.let { linkField(id, DbConstants.FLD_AUTHOR, it) }
        status?.let { linkField(id, DbConstants.FLD_STATUS, it) }
    }

    private suspend fun linkField(bookId: Long, typeId: Int, name: String) {
        val existing = bookDao.getFieldsByTypeId(typeId).firstOrNull { it.name == name }
        val fieldId = existing?.id
            ?: bookDao.insertField(FieldEntity(typeId = typeId, name = name))
        bookDao.insertBookFieldCrossRef(BookFieldCrossRef(bookId = bookId, fieldId = fieldId))
    }

    private fun applyOrder(scenario: ActivityScenario<HiltTestActivity>, orderId: Int) {
        scenario.onFragment {
            val popupMenu = this.showOrderPopupMenu(View(requireContext()))
            popupMenu.menu.performIdentifierAction(orderId, 0)
        }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    private fun expandAll(scenario: ActivityScenario<HiltTestActivity>) {
        scenario.onFragment { invokeOptionsItemSelect(this, R.id.action_exp_all) }
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    private fun visibleRowTexts(scenario: ActivityScenario<HiltTestActivity>): List<String> {
        val rows = mutableListOf<Pair<Int, String>>()
        scenario.onActivity { activity ->
            val recyclerView = activity.findViewById<RecyclerView>(R.id.book_list)
            for (i in 0 until recyclerView.childCount) {
                val child = recyclerView.getChildAt(i)
                val position = recyclerView.getChildAdapterPosition(child)
                val textView = child.findViewById<TextView>(R.id.tv_header)
                    ?: child.findViewById(R.id.tv_item)
                if (position != RecyclerView.NO_POSITION) {
                    rows.add(position to textView.text.toString())
                }
            }
        }
        return rows.sortedBy { it.first }.map { it.second }
    }

    private fun assertRowOrder(
        scenario: ActivityScenario<HiltTestActivity>,
        vararg expectedRows: String
    ) {
        assertEquals(MSG_ROW_ORDER, expectedRows.toList(), visibleRowTexts(scenario))
    }

    companion object {
        private const val ID_1 = 101L
        private const val ID_2 = 102L
        private const val ID_3 = 103L
        private const val TITLE_1 = "Testing Book"
        private const val TITLE_2 = "Another Book"
        private const val TITLE_SEARCH = "Apple"
        private const val TITLE_OTHER = "Banana"
        private const val QUERY_SEARCH = "Ap"
        private const val QUERY_NO_MATCH = "zzzz_non_existing_query"
        private const val QUERY_EMPTY = ""
        private const val HEADER_T = "T"
        private const val HEADER_A = "A"
        private const val HEADER_MISSING = "(missing)"
        private const val AUTHOR_1 = "Zoe Author"
        private const val AUTHOR_2 = "Yuri Author"
        private const val STATUS_WANTED = "Wanted"
        private const val STATUS_IN_BAG = "In Bag"
        private const val STATUS_READ = "Read"
        private const val FRAGMENT_TAG = "tag"
        private const val KEY_OPERATION = "key_operation"

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
        private const val COUNT_3_TEXT = "3 books"
        
        private const val TIMEOUT_2000 = 2000L
        private const val TIMEOUT_3000 = 3000L
        private const val TIMEOUT_5000 = 5000L
        
        private const val MSG_ACTION_MODE_ACTIVE = "ActionMode should be active"
        private const val MSG_ORDER_MENU_SHOWN = "Order popup menu should be created"
        private const val MSG_ORDER_MENU_SIZE = "Order popup menu should contain all order items"
        private const val MSG_ORDER_MENU_ITEM_ID = "Order popup menu item id should match order item"
        private const val MSG_ORDER_MENU_ITEM_TITLE = "Order popup menu item title should match order item"
        private const val MSG_ORDER_MENU_CHECKED = "Current order item should be checked"
        private const val MSG_ORDER_MENU_CHECKABLE = "Order menu item should be checkable"
        private const val MSG_ROW_ORDER = "Rows should be displayed in the expected order"
        private const val MSG_RATIONALE_DIALOG = "Permission rationale dialog should be shown"
        private const val MSG_PERMISSION_REQUESTED = "A runtime permission should have been requested"
        private const val MSG_STORAGE_PERMISSION_REQUESTED = "Write external storage permission should have been requested"
        private const val MSG_NO_FILE_DIALOG = "No file dialog should be shown when permission is denied"
        private const val MSG_ACTION_IMPORT = "Import action should be queued"
        private const val MSG_ACTION_EXPORT = "Export action should be queued"
        private const val MSG_MENU_IMPORT = "Import menu item should be handled"
        private const val MSG_MENU_EXPORT = "Export menu item should be handled"
        private const val MSG_MENU_EXPAND_ALL = "Expand all menu item should be handled"
        private const val MSG_MENU_COLLAPSE_ALL = "Collapse all menu item should be handled"
        private const val MSG_MENU_SORT = "Sort menu item should be handled"
        private const val MSG_ACCESS_DENIED_TOAST = "Access denied toast should be shown"
    }
}
