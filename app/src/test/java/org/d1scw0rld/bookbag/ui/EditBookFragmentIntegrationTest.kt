package org.d1scw0rld.bookbag.ui

import android.os.Bundle
import android.os.Looper.getMainLooper
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.Spinner
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import androidx.core.view.size
import androidx.test.espresso.util.TreeIterables
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.d1scw0rld.bookbag.DisplayNameRobolectricRunner
import org.d1scw0rld.bookbag.R
import org.d1scw0rld.bookbag.data.DbConstants
import org.d1scw0rld.bookbag.data.dao.BookDao
import org.d1scw0rld.bookbag.data.entity.BookEntity
import org.d1scw0rld.bookbag.data.entity.BookFieldCrossRef
import org.d1scw0rld.bookbag.data.entity.FieldEntity
import org.d1scw0rld.bookbag.data.relation.BookWithFields
import org.d1scw0rld.bookbag.dto.Property
import org.d1scw0rld.bookbag.launchFragmentInHiltContainer
import org.d1scw0rld.bookbag.ui.fields.AutoCompleteTextViewX
import org.d1scw0rld.bookbag.ui.fields.EditTextX
import org.d1scw0rld.bookbag.ui.fields.FieldAutoCompleteTextView
import org.d1scw0rld.bookbag.ui.fields.FieldCheckBox
import org.d1scw0rld.bookbag.ui.fields.FieldDate
import org.d1scw0rld.bookbag.ui.fields.FieldEditTextUpdatableClearable
import org.d1scw0rld.bookbag.ui.fields.FieldMoney
import org.d1scw0rld.bookbag.ui.fields.FieldMultiSpinner
import org.d1scw0rld.bookbag.ui.fields.FieldMultiText
import org.d1scw0rld.bookbag.ui.fields.FieldRating
import org.d1scw0rld.bookbag.ui.fields.FieldSpinner
import org.d1scw0rld.bookbag.waitFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockingDetails
import org.mockito.Mockito.verify
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
            value = PRICE_6000_1,
            dueDate = DATE_ZERO,
            readDate = DATE_ZERO,
            edition = EDITION_1,
            isbn = ISBN_EDIT,
            web = WEB_EDIT
        )
        bookDao.insertBook(book)
        insertCustomFieldsForBook(ID_301)

        // Act: Launch fragment for editing existing book
        val args = Bundle().apply {
            putLong(KEY_BOOK_ID, ID_301)
            putBoolean(KEY_IS_COPY, false)
        }
        launchFragmentInHiltContainer<EditBookFragment>(fragmentArgs = args)

        // Assert: Form container is visible
        onView(withId(R.id.book_detail_container)).check(matches(isDisplayed()))
        assertCustomFieldValuesDisplayed()
        assertBookEntityFieldsDisplayed(
            title = TITLE_EDIT,
            description = DESC_EDIT,
            volume = VOL_1,
            publicationDate = PUB_DATE_2023,
            pages = PAGES_300,
            price = PRICE_3000_DISPLAY,
            value = PRICE_6000_DISPLAY,
            edition = EDITION_1,
            isbn = ISBN_EDIT,
            web = WEB_EDIT
        )
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
            value = PRICE_7000_1,
            dueDate = DATE_ZERO,
            readDate = DATE_ZERO,
            edition = EDITION_2,
            isbn = ISBN_COPY,
            web = WEB_COPY
        )
        bookDao.insertBook(book)
        insertCustomFieldsForBook(ID_302)

        // Act: Launch fragment with isCopy=true
        val args = Bundle().apply {
            putLong(KEY_BOOK_ID, ID_302)
            putBoolean(KEY_IS_COPY, true)
        }
        launchFragmentInHiltContainer<EditBookFragment>(fragmentArgs = args)

        // Assert: Main layout structure is visible
        onView(withId(R.id.book_detail_container)).check(matches(isDisplayed()))
        assertCustomFieldValuesDisplayed()
        assertBookEntityFieldsDisplayed(
            title = TITLE_COPY,
            description = DESC_COPY,
            volume = VOL_2,
            publicationDate = PUB_DATE_2022,
            pages = PAGES_400,
            price = PRICE_4000_DISPLAY,
            value = PRICE_7000_DISPLAY,
            edition = EDITION_2,
            isbn = ISBN_COPY,
            web = WEB_COPY
        )
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
            value = PRICE_8000_2,
            dueDate = testDate,
            readDate = testDate,
            edition = EDITION_3,
            isbn = ISBN_COMPLETE,
            web = WEB_COMPLETE
        )
        bookDao.insertBook(book)
        insertCustomFieldsForBook(ID_304)

        // Act: Launch fragment for editing
        val args = Bundle().apply {
            putLong(KEY_BOOK_ID, ID_304)
            putBoolean(KEY_IS_COPY, false)
        }
        launchFragmentInHiltContainer<EditBookFragment>(fragmentArgs = args)

        // Assert: Form initializes successfully with all data
        onView(withId(R.id.book_detail_container)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        assertCustomFieldValuesDisplayed()
        assertBookEntityFieldsDisplayed(
            title = TITLE_COMPLETE,
            description = DESC_COMPLETE,
            volume = VOL_5,
            publicationDate = PUB_DATE_2024,
            pages = PAGES_500,
            price = PRICE_5000_DISPLAY,
            value = PRICE_8000_DISPLAY,
            edition = EDITION_3,
            isbn = ISBN_COMPLETE,
            web = WEB_COMPLETE
        )
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
            val iterable = TreeIterables.breadthFirstViewTraversal(view)
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
        assertEquals(hiddenFieldLabels(), fragment?.hiddenFieldsPopupMenu()?.hiddenFieldsPopupMenuLabels())
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
                fragment?.hiddenFieldsPopupMenu()?.performFieldSelection(fieldLabel)
                Shadows.shadowOf(getMainLooper()).idle()

                onView(isRoot()).perform(waitFor(allOf(withId(R.id.tv_title), withText(fieldLabel)), TIMEOUT_5000))
                onView(allOf(withId(R.id.tv_title), withText(fieldLabel))).check(matches(isDisplayed()))
            }
    }

    @DisplayName("On Save Tapped - All Fields And Custom Fields Filled - Persists Every Value To Database")
    @Test
    fun onSaveTapped_allFieldsFilled_persistsEveryValueToDatabase() = runTest {
        val selectableFields = insertSelectableFieldOptions()

        var fragment: EditBookFragment? = null
        val navController = mock(NavController::class.java)
        launchNewEditBookFragment {
            Navigation.setViewNavController(requireView(), navController)
            fragment = this
        }
        onView(isRoot()).perform(waitFor(withHint(R.string.fld_title), TIMEOUT_5000))

        val form = requireNotNull(fragment) { "EditBookFragment should be launched before filling the form." }
        form.revealAllHiddenFields()

        form.fillText(DbConstants.FLD_TITLE, TITLE_ALL)
        form.fillText(DbConstants.FLD_DESCRIPTION, DESC_ALL)
        form.fillText(DbConstants.FLD_VOLUME, VOLUME_ALL.toString())
        form.fillText(DbConstants.FLD_PAGES, PAGES_ALL.toString())
        form.fillText(DbConstants.FLD_PUBLICATION_DATE, PUB_DATE_ALL.toString())
        form.fillText(DbConstants.FLD_EDITION, EDITION_ALL.toString())
        form.fillText(DbConstants.FLD_ISBN, ISBN_ALL)
        form.fillText(DbConstants.FLD_WEB, WEB_ALL)

        form.fillAutoComplete(DbConstants.FLD_SERIE, SERIES_ALL)
        form.fillAutoComplete(DbConstants.FLD_PUBLISHER, PUBLISHER_ALL)
        form.fillAutoComplete(DbConstants.FLD_PUBLICATION_LOCATION, PUB_LOCATION_ALL)
        form.fillAutoComplete(DbConstants.FLD_LOANED_TO, LOANED_TO_ALL)
        form.fillAutoComplete(DbConstants.FLD_LOCATION, LOCATION_ALL)

        form.fillMultiText(DbConstants.FLD_AUTHOR, listOf(AUTHOR_1_ALL, AUTHOR_2_ALL))

        form.selectSpinnerValue(DbConstants.FLD_LANGUAGE, LANGUAGE_ALL)
        form.selectSpinnerValue(DbConstants.FLD_STATUS, STATUS_ALL)
        form.selectSpinnerValue(DbConstants.FLD_FORMAT, FORMAT_ALL)
        form.selectSpinnerValue(DbConstants.FLD_CONDITION, CONDITION_ALL)

        form.selectMultiSpinnerValues(DbConstants.FLD_GENRE, listOf(GENRE_1_ALL, GENRE_2_ALL))

        val currencyId = form.fillMoney(DbConstants.FLD_PRICE, PRICE_WHOLE, PRICE_CENTS, CURRENCY_ALL)
        form.fillMoney(DbConstants.FLD_VALUE, VALUE_WHOLE, VALUE_CENTS, CURRENCY_ALL)

        form.pickDate(DbConstants.FLD_READ_DATE, READ_YEAR, READ_MONTH, READ_DAY)
        form.pickDate(DbConstants.FLD_DUE_DATE, DUE_YEAR, DUE_MONTH, DUE_DAY)

        form.setRating(RATING_ALL)
        form.setChecked(DbConstants.FLD_READ, true)

        onView(withText(R.string.done)).perform(click())

        val saved = awaitSavedBook(navController)
        verify(navController).navigate(R.id.action_to_book_list)

        assertEquals(MSG_SAVED_TITLE, TITLE_ALL, saved.book.title)
        assertEquals(MSG_SAVED_DESCRIPTION, DESC_ALL, saved.book.description)
        assertEquals(MSG_SAVED_VOLUME, VOLUME_ALL, saved.book.volume)
        assertEquals(MSG_SAVED_PAGES, PAGES_ALL, saved.book.pages)
        assertEquals(MSG_SAVED_PUBLICATION_DATE, PUB_DATE_ALL, saved.book.publicationDate)
        assertEquals(MSG_SAVED_EDITION, EDITION_ALL, saved.book.edition)
        assertEquals(MSG_SAVED_ISBN, ISBN_ALL, saved.book.isbn)
        assertEquals(MSG_SAVED_WEB, WEB_ALL, saved.book.web)
        assertEquals(MSG_SAVED_PRICE, "$PRICE_STORED|$currencyId", saved.book.price)
        assertEquals(MSG_SAVED_VALUE, "$VALUE_STORED|$currencyId", saved.book.value)
        assertEquals(MSG_SAVED_READ_DATE, READ_DATE_STORED, saved.book.readDate)
        assertEquals(MSG_SAVED_DUE_DATE, DUE_DATE_STORED, saved.book.dueDate)

        val expectedProperties = listOf(
            DbConstants.FLD_AUTHOR to AUTHOR_1_ALL,
            DbConstants.FLD_AUTHOR to AUTHOR_2_ALL,
            DbConstants.FLD_SERIE to SERIES_ALL,
            DbConstants.FLD_PUBLISHER to PUBLISHER_ALL,
            DbConstants.FLD_PUBLICATION_LOCATION to PUB_LOCATION_ALL,
            DbConstants.FLD_LOANED_TO to LOANED_TO_ALL,
            DbConstants.FLD_LOCATION to LOCATION_ALL,
            DbConstants.FLD_LANGUAGE to LANGUAGE_ALL,
            DbConstants.FLD_STATUS to STATUS_ALL,
            DbConstants.FLD_FORMAT to FORMAT_ALL,
            DbConstants.FLD_CONDITION to CONDITION_ALL,
            DbConstants.FLD_GENRE to GENRE_1_ALL,
            DbConstants.FLD_GENRE to GENRE_2_ALL,
            DbConstants.FLD_RATING to RATING_ALL.toString(),
            DbConstants.FLD_READ to true.toString()
        )
        // Compared as sorted lists rather than sets so a duplicated row cannot hide.
        assertEquals(
            MSG_SAVED_PROPERTIES,
            expectedProperties.sortedWith(PROPERTY_ORDER),
            saved.fields.map { it.typeId to it.name }.sortedWith(PROPERTY_ORDER)
        )

        assertEquals(
            MSG_REUSED_OPTIONS,
            selectableFields.filterKeys { it.first != DbConstants.FLD_CURRENCY }.values.toSet(),
            saved.fields.filter { it.typeId in SELECTABLE_FIELD_TYPES }.map { it.id }.toSet()
        )
        SELECTABLE_FIELD_TYPES.forEach { typeId ->
            val names = bookDao.getFieldsByTypeId(typeId).map { it.name }.sorted()
            assertEquals("$MSG_NO_DUPLICATE_OPTIONS $typeId", names.distinct(), names)
        }
    }

    private suspend fun insertSelectableFieldOptions(): Map<Pair<Int, String>, Long> {
        val options = listOf(
            DbConstants.FLD_LANGUAGE to LANGUAGE_ALL,
            DbConstants.FLD_STATUS to STATUS_ALL,
            DbConstants.FLD_FORMAT to FORMAT_ALL,
            DbConstants.FLD_CONDITION to CONDITION_ALL,
            DbConstants.FLD_GENRE to GENRE_1_ALL,
            DbConstants.FLD_GENRE to GENRE_2_ALL,
            DbConstants.FLD_CURRENCY to CURRENCY_UNUSED,
            DbConstants.FLD_CURRENCY to CURRENCY_ALL
        )

        return options.associateWith { (typeId, name) ->
            bookDao.insertField(FieldEntity(typeId = typeId, name = name))
        }
    }

    private suspend fun awaitSavedBook(navController: NavController): BookWithFields {
        val deadline = System.currentTimeMillis() + TIMEOUT_5000
        while (System.currentTimeMillis() < deadline) {
            Shadows.shadowOf(getMainLooper()).idle()
            val books = bookDao.getAllBooksWithFields()
            // The save coroutine writes the book row before its custom field rows, so wait for the
            // post-save forward navigation, which only happens once the whole save has completed.
            if (navController.navigationCalls().contains(R.id.action_to_book_list)) {
                return books.single()
            }
        }
        throw AssertionError(
            "$MSG_BOOK_NOT_SAVED (books=${bookDao.getAllBooksWithFields().size}," +
                " navigation=${navController.navigationCalls()})"
        )
    }

    private fun NavController.navigationCalls(): List<Any?> =
        mockingDetails(this).invocations
            .filter { it.method.name == NAVIGATE_METHOD }
            .map { it.arguments.firstOrNull() }

    private fun EditBookFragment.revealAllHiddenFields() {
        val menu = hiddenFieldsPopupMenu().menu
        while (menu.size > 0) {
            menu.performIdentifierAction(menu.getItem(0).itemId, 0)
        }
        Shadows.shadowOf(getMainLooper()).idle()
    }

    private inline fun <reified T : View> EditBookFragment.fieldView(fieldId: Int): T {
        val fieldTitle = DbConstants.FIELDS.first { it.id == fieldId }.name.displayLabel()
        return TreeIterables.breadthFirstViewTraversal(requireView())
            .filterIsInstance<T>()
            .first { (it as org.d1scw0rld.bookbag.ui.fields.Field).getTitle() == fieldTitle }
    }

    private fun EditText.enterValue(value: String) {
        if (this is AutoCompleteTextView) {
            setText(value, false)
        } else {
            setText(value)
        }
        onEditorAction(EditorInfo.IME_ACTION_NEXT)
    }

    private fun EditBookFragment.fillText(fieldId: Int, value: String) {
        fieldView<FieldEditTextUpdatableClearable>(fieldId)
            .findViewById<EditTextX>(R.id.editTextX)
            .enterValue(value)
    }

    private fun EditBookFragment.fillAutoComplete(fieldId: Int, value: String) {
        fieldView<FieldAutoCompleteTextView>(fieldId)
            .findViewById<AutoCompleteTextViewX>(R.id.autoCompleteTextView)
            .enterValue(value)
    }

    private fun EditBookFragment.fillMultiText(fieldId: Int, values: List<String>) {
        val multiText = fieldView<FieldMultiText>(fieldId)
        val addButton = multiText.findViewById<View>(R.id.ib_add_field)
        val rows = multiText.findViewById<LinearLayout>(R.id.ll_fields)

        values.forEachIndexed { index, value ->
            if (index >= rows.childCount) {
                addButton.performClick()
            }
            TreeIterables.breadthFirstViewTraversal(rows.getChildAt(index))
                .filterIsInstance<AutoCompleteTextViewX>()
                .first()
                .enterValue(value)
        }
    }

    private fun EditBookFragment.selectSpinnerValue(fieldId: Int, value: String) {
        val fieldSpinner = fieldView<FieldSpinner>(fieldId)
        fieldSpinner.findViewById<Spinner>(R.id.action_select_type).selectItem(value)
        assertEquals(MSG_SPINNER_SELECTION, value, (fieldSpinner.tag as Property).value)
    }

    private fun Spinner.selectItem(value: String) {
        val position = (0 until count).first { getItemAtPosition(it) == value }
        setSelection(position)
        Shadows.shadowOf(getMainLooper()).idle()
    }

    private fun EditBookFragment.selectMultiSpinnerValues(fieldId: Int, values: List<String>) {
        val multiSpinner = fieldView<FieldMultiSpinner>(fieldId)
        val selectButton = multiSpinner.findViewById<Button>(R.id.action_select_type)
        selectButton.performClick()

        val menu = (multiSpinner.popupMenu?.menu as? MenuBuilder)
            ?: error("MultiSpinner popup menu should be available after click.")
        values.forEach { value ->
            val item = (0 until menu.size)
                .map { menu.getItem(it) }
                .first { it.title.toString() == value }
            menu.performItemAction(item, 0)
        }
        Shadows.shadowOf(getMainLooper()).idle()

        assertEquals(
            MSG_MULTI_SPINNER_SELECTION,
            values.toSet(),
            selectButton.text.toString().split(MULTI_SPINNER_SEPARATOR).toSet()
        )
    }

    private fun EditBookFragment.fillMoney(
        fieldId: Int,
        wholePart: Int,
        centsPart: Int,
        currency: String
    ): Long {
        val money = fieldView<FieldMoney>(fieldId)
        money.findViewById<EditTextX>(R.id.editTextX)
            .enterValue("$wholePart${DbConstants.separator}$centsPart")

        val currencySpinner = money.findViewById<Spinner>(R.id.action_select_type)
        currencySpinner.selectItem(currency)

        return runBlocking {
            bookDao.getFieldsByTypeId(DbConstants.FLD_CURRENCY).first { it.name == currency }.id
        }
    }

    private fun EditBookFragment.pickDate(fieldId: Int,
                                          year: Int,
                                          month: Int,
                                          day: Int) {
        val fieldDate = fieldView<FieldDate>(fieldId)

        fieldDate.findViewById<Button>(R.id.action_select_type)
            .performClick()

        val picker = fieldDate.datePickerDialog
        assertTrue(MSG_DATE_PICKER_SHOWN, picker != null)

        fieldDate.onDateSet(picker,
            year,
            month - 1,
            day)

        // Required in the Robolectric test so the dialog doesn't cover the form.
        @Suppress("DEPRECATION")
        picker?.dismiss()

        Shadows.shadowOf(getMainLooper()).idle()
    }

    private fun EditBookFragment.setRating(rating: Float) {
        fieldView<FieldRating>(DbConstants.FLD_RATING)
            .findViewById<RatingBar>(R.id.rating_bar).rating = rating
    }

    private fun EditBookFragment.setChecked(fieldId: Int, checked: Boolean) {
        fieldView<FieldCheckBox>(fieldId)
            .findViewById<CheckBox>(R.id.check_box).isChecked = checked
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

    private suspend fun insertCustomFieldsForBook(bookId: Long) {
        val fields = customFieldEntities()
        fields.forEach { field ->
            bookDao.insertField(field)
        }

        fields.map { field ->
            BookFieldCrossRef(bookId = bookId, fieldId = field.id)
        }.forEach { crossRef ->
            bookDao.insertBookFieldCrossRef(crossRef)
        }
    }

    private fun assertCustomFieldValuesDisplayed() {
        val valuesToAssert = listOf(
            FIELD_NAME_TOLKIEN,
            FIELD_NAME_PRATCHETT,
            FIELD_NAME_LOTR,
            FIELD_NAME_FANTASY,
            FIELD_NAME_EN,
            FIELD_NAME_ALLEN,
            FIELD_NAME_LONDON,
            FIELD_NAME_OWNED,
            FIELD_NAME_HARDCOVER,
            FIELD_NAME_HOME,
            FIELD_NAME_MINT,
            FIELD_NAME_JOHN
        )

        valuesToAssert.forEach { value ->
            onView(isRoot()).perform(
                waitFor(allOf(withText(value), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)), TIMEOUT_5000)
            )
        }

        onView(withId(R.id.rating_bar)).check(matches(withRating(RATING_5)))
        onView(isRoot()).perform(waitFor(allOf(withId(R.id.check_box), isChecked()), TIMEOUT_5000))
    }

    private fun assertBookEntityFieldsDisplayed(
        title: String,
        description: String,
        volume: Int,
        publicationDate: Int,
        pages: Int,
        price: String,
        value: String,
        edition: Int,
        isbn: String,
        web: String,
    ) {
        val valuesToAssert = listOf(
            title,
            description,
            volume.toString(),
            publicationDate.toString(),
            pages.toString(),
            price,
            value,
            edition.toString(),
            isbn,
            web
        )

        valuesToAssert.forEach { valueText ->
            onView(isRoot()).perform(
                waitFor(allOf(withText(valueText), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)), TIMEOUT_5000)
            )
        }
    }

    private fun customFieldEntities(): List<FieldEntity> = listOf(
        FieldEntity(id = FIELD_ID_AUTHOR_TOLKIEN, typeId = DbConstants.FLD_AUTHOR, name = FIELD_NAME_TOLKIEN),
        FieldEntity(id = FIELD_ID_AUTHOR_PRATCHETT, typeId = DbConstants.FLD_AUTHOR, name = FIELD_NAME_PRATCHETT),
        FieldEntity(id = FIELD_ID_SERIES_LOTR, typeId = DbConstants.FLD_SERIE, name = FIELD_NAME_LOTR),
        FieldEntity(id = FIELD_ID_GENRE_FANTASY, typeId = DbConstants.FLD_GENRE, name = FIELD_NAME_FANTASY),
        FieldEntity(id = FIELD_ID_LANGUAGE_EN, typeId = DbConstants.FLD_LANGUAGE, name = FIELD_NAME_EN),
        FieldEntity(id = FIELD_ID_PUBLISHER_ALLEN, typeId = DbConstants.FLD_PUBLISHER, name = FIELD_NAME_ALLEN),
        FieldEntity(id = FIELD_ID_LOCATION_LONDON, typeId = DbConstants.FLD_PUBLICATION_LOCATION, name = FIELD_NAME_LONDON),
        FieldEntity(id = FIELD_ID_STATUS_OWNED, typeId = DbConstants.FLD_STATUS, name = FIELD_NAME_OWNED),
        FieldEntity(id = FIELD_ID_RATING_5, typeId = DbConstants.FLD_RATING, name = FIELD_NAME_5),
        FieldEntity(id = FIELD_ID_FORMAT_HARDCOVER, typeId = DbConstants.FLD_FORMAT, name = FIELD_NAME_HARDCOVER),
        FieldEntity(id = FIELD_ID_LOC_HOME, typeId = DbConstants.FLD_LOCATION, name = FIELD_NAME_HOME),
        FieldEntity(id = FIELD_ID_CONDITION_MINT, typeId = DbConstants.FLD_CONDITION, name = FIELD_NAME_MINT),
        FieldEntity(id = FIELD_ID_READ_TRUE, typeId = DbConstants.FLD_READ, name = FIELD_NAME_TRUE),
        FieldEntity(id = FIELD_ID_LOANED_TO_JOHN, typeId = DbConstants.FLD_LOANED_TO, name = FIELD_NAME_JOHN)
    )

    private fun withRating(expectedRating: Float) = object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description) {
            description.appendText("RatingBar with rating $expectedRating")
        }

        override fun matchesSafely(view: View): Boolean {
            return view is RatingBar && view.rating == expectedRating
        }
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

        private const val VOL_1 = 101
        private const val VOL_2 = 202
        private const val VOL_5 = 505

        private const val PUB_DATE_2022 = 2022
        private const val PUB_DATE_2023 = 2023
        private const val PUB_DATE_2024 = 2024

        private const val PAGES_300 = 300
        private const val PAGES_400 = 400
        private const val PAGES_500 = 500

        private const val PRICE_3000_1 = "3000|1"
        private const val PRICE_4000_1 = "4000|1"
        private const val PRICE_5000_2 = "5000|2"
        private const val PRICE_6000_1 = "6000|1"
        private const val PRICE_7000_1 = "7000|1"
        private const val PRICE_8000_2 = "8000|2"

        private val PRICE_3000_DISPLAY = "30${java.text.DecimalFormatSymbols.getInstance().decimalSeparator}00"
        private val PRICE_4000_DISPLAY = "40${java.text.DecimalFormatSymbols.getInstance().decimalSeparator}00"
        private val PRICE_5000_DISPLAY = "50${java.text.DecimalFormatSymbols.getInstance().decimalSeparator}00"
        private val PRICE_6000_DISPLAY = "60${java.text.DecimalFormatSymbols.getInstance().decimalSeparator}00"
        private val PRICE_7000_DISPLAY = "70${java.text.DecimalFormatSymbols.getInstance().decimalSeparator}00"
        private val PRICE_8000_DISPLAY = "80${java.text.DecimalFormatSymbols.getInstance().decimalSeparator}00"

        private const val EDITION_1 = 11
        private const val EDITION_2 = 22
        private const val EDITION_3 = 33

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
        
        private const val FIELD_ID_AUTHOR_TOLKIEN = 501L
        private const val FIELD_ID_AUTHOR_PRATCHETT = 502L
        private const val FIELD_ID_SERIES_LOTR = 503L
        private const val FIELD_ID_GENRE_FANTASY = 504L
        private const val FIELD_ID_LANGUAGE_EN = 505L
        private const val FIELD_ID_PUBLISHER_ALLEN = 506L
        private const val FIELD_ID_LOCATION_LONDON = 507L
        private const val FIELD_ID_STATUS_OWNED = 508L
        private const val FIELD_ID_RATING_5 = 509L
        private const val FIELD_ID_FORMAT_HARDCOVER = 510L
        private const val FIELD_ID_LOC_HOME = 511L
        private const val FIELD_ID_CONDITION_MINT = 512L
        private const val FIELD_ID_READ_TRUE = 513L
        private const val FIELD_ID_LOANED_TO_JOHN = 514L
        
        private const val FIELD_NAME_TOLKIEN = "J.R.R. Tolkien"
        private const val FIELD_NAME_PRATCHETT = "Terry Pratchett"
        private const val FIELD_NAME_LOTR = "The Lord of the Rings"
        private const val FIELD_NAME_FANTASY = "Fantasy"
        private const val FIELD_NAME_EN = "English"
        private const val FIELD_NAME_ALLEN = "George Allen & Unwin"
        private const val FIELD_NAME_LONDON = "London"
        private const val FIELD_NAME_OWNED = "Owned"
        private const val FIELD_NAME_5 = "5.0"
        private const val RATING_5 = 5.0f
        private const val FIELD_NAME_HARDCOVER = "Hardcover"
        private const val FIELD_NAME_HOME = "Home Library"
        private const val FIELD_NAME_MINT = "Mint Condition"
        private const val FIELD_NAME_TRUE = "true"
        private const val FIELD_NAME_JOHN = "John Doe"

        private const val TITLE_ALL = "Fully Filled Book"
        private const val DESC_ALL = "Every field of this book is filled in"
        private const val VOLUME_ALL = 7
        private const val PAGES_ALL = 321
        private const val PUB_DATE_ALL = 1999
        private const val EDITION_ALL = 3
        private const val ISBN_ALL = "1234567890"
        private const val WEB_ALL = "https://fully-filled.example.com"

        private const val AUTHOR_1_ALL = "Ada Lovelace"
        private const val AUTHOR_2_ALL = "Grace Hopper"
        private const val SERIES_ALL = "Pioneers Of Computing"
        private const val PUBLISHER_ALL = "Analytical Press"
        private const val PUB_LOCATION_ALL = "Cambridge"
        private const val LOANED_TO_ALL = "Alan Turing"
        private const val LOCATION_ALL = "Top Shelf"

        private const val LANGUAGE_ALL = "Esperanto"
        private const val STATUS_ALL = "Borrowed"
        private const val FORMAT_ALL = "Paperback"
        private const val CONDITION_ALL = "Good"
        private const val GENRE_1_ALL = "Science"
        private const val GENRE_2_ALL = "History"
        private const val CURRENCY_UNUSED = "USD"
        private const val CURRENCY_ALL = "EUR"

        private const val PRICE_WHOLE = 12
        private const val PRICE_CENTS = 34
        private const val PRICE_STORED = 1234
        private const val VALUE_WHOLE = 56
        private const val VALUE_CENTS = 78
        private const val VALUE_STORED = 5678

        private const val READ_YEAR = 2021
        private const val READ_MONTH = 6
        private const val READ_DAY = 15
        private const val READ_DATE_STORED = 20210615
        private const val DUE_YEAR = 2022
        private const val DUE_MONTH = 7
        private const val DUE_DAY = 20
        private const val DUE_DATE_STORED = 20220720

        private const val RATING_ALL = 4.0f

        private const val MULTI_SPINNER_SEPARATOR = ", "
        private const val NAVIGATE_METHOD = "navigate"

        private val SELECTABLE_FIELD_TYPES = listOf(
            DbConstants.FLD_LANGUAGE,
            DbConstants.FLD_STATUS,
            DbConstants.FLD_FORMAT,
            DbConstants.FLD_CONDITION,
            DbConstants.FLD_GENRE
        )

        private val PROPERTY_ORDER = compareBy<Pair<Int, String>>({ it.first }, { it.second })

        private const val MSG_BOOK_NOT_SAVED = "Book should have been saved to the database"
        private const val MSG_SPINNER_SELECTION = "Spinner selection should update the bound property"
        private const val MSG_MULTI_SPINNER_SELECTION = "Multi spinner should show the selected values"
        private const val MSG_DATE_PICKER_SHOWN = "Date picker dialog should be created on click"
        private const val MSG_SAVED_TITLE = "Saved title should match the entered title"
        private const val MSG_SAVED_DESCRIPTION = "Saved description should match the entered description"
        private const val MSG_SAVED_VOLUME = "Saved volume should match the entered volume"
        private const val MSG_SAVED_PAGES = "Saved pages should match the entered pages"
        private const val MSG_SAVED_PUBLICATION_DATE = "Saved publication date should match the entered year"
        private const val MSG_SAVED_EDITION = "Saved edition should match the entered edition"
        private const val MSG_SAVED_ISBN = "Saved ISBN should match the entered ISBN"
        private const val MSG_SAVED_WEB = "Saved web address should match the entered address"
        private const val MSG_SAVED_PRICE = "Saved price should keep amount and selected currency"
        private const val MSG_SAVED_VALUE = "Saved value should keep amount and selected currency"
        private const val MSG_SAVED_READ_DATE = "Saved read date should match the picked date"
        private const val MSG_SAVED_DUE_DATE = "Saved due date should match the picked date"
        private const val MSG_SAVED_PROPERTIES = "Saved custom fields should match the entered custom fields"
        private const val MSG_REUSED_OPTIONS = "Selected existing options should be reused instead of duplicated"
        private const val MSG_NO_DUPLICATE_OPTIONS = "Saving should not duplicate existing options of field type"
    }
}
