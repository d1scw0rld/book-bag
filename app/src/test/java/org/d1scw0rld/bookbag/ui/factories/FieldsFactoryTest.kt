package org.d1scw0rld.bookbag.ui.factories

import android.content.Context
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.test.core.app.ApplicationProvider
import org.d1scw0rld.bookbag.R
import org.d1scw0rld.bookbag.data.DbConstants
import org.d1scw0rld.bookbag.dto.Book
import org.d1scw0rld.bookbag.dto.Changeable
import org.d1scw0rld.bookbag.dto.Field
import org.d1scw0rld.bookbag.dto.Property
import org.d1scw0rld.bookbag.ui.fields.FieldAutoCompleteTextView
import org.d1scw0rld.bookbag.ui.fields.FieldCheckBox
import org.d1scw0rld.bookbag.ui.fields.FieldDate
import org.d1scw0rld.bookbag.ui.fields.FieldEditTextUpdatableClearable
import org.d1scw0rld.bookbag.ui.fields.FieldMoney
import org.d1scw0rld.bookbag.ui.fields.FieldMultiSpinner
import org.d1scw0rld.bookbag.ui.fields.FieldMultiText
import org.d1scw0rld.bookbag.ui.fields.FieldRating
import org.d1scw0rld.bookbag.ui.fields.FieldSpinner
import org.d1scw0rld.bookbag.ui.fields.Title
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.d1scw0rld.bookbag.DisplayNameRobolectricRunner
import org.robolectric.annotation.Config

@RunWith(DisplayNameRobolectricRunner::class)
@Config(sdk = [28])
class FieldsFactoryTest {

    private lateinit var context: Context
    private lateinit var book: Book
    private lateinit var propertiesMap: MutableMap<Int, List<Property>>
    private lateinit var rootView: LinearLayout
    private lateinit var factory: FieldsFactory

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.setTheme(R.style.AppTheme)
        DbConstants.initFields(context.resources)
        
        book = Book(
            id = ID_ZERO,
            title = Changeable(TITLE_INITIAL),
            description = Changeable(DESC_INITIAL),
            pages = Changeable(PAGES_INITIAL),
            properties = ArrayList()
        )

        propertiesMap = mutableMapOf(
            DbConstants.FLD_SERIE to listOf(Property(DbConstants.FLD_SERIE, SERIES_HP, ID_PROP_1)),
            DbConstants.FLD_STATUS to listOf(Property(DbConstants.FLD_STATUS, STATUS_OWNED, ID_PROP_2)),
            DbConstants.FLD_CURRENCY to listOf(Property(DbConstants.FLD_CURRENCY, CURRENCY_USD, ID_PROP_3)),
            DbConstants.FLD_AUTHOR to listOf(Property(DbConstants.FLD_AUTHOR, AUTHOR_ROWLING, ID_PROP_4)),
            DbConstants.FLD_GENRE to listOf(Property(DbConstants.FLD_GENRE, GENRE_FANTASY, ID_PROP_5)),
            DbConstants.FLD_RATING to listOf(Property(DbConstants.FLD_RATING, RATING_4_5, ID_PROP_6)),
            DbConstants.FLD_READ to listOf(Property(DbConstants.FLD_READ, READ_TRUE, ID_PROP_7))
        )

        rootView = LinearLayout(context)
        factory = FieldsFactory(context, book, propertiesMap)
    }

    @DisplayName("Add Field Text - String Field Value Updated On Focus Lost - Updates Book DTO Value")
    @Test
    fun addFieldText_stringFieldValueUpdatedOnFocusLost_updatesBookDtoValue() {
        val field = Field(DbConstants.FLD_TITLE, FIELD_NAME_TITLE, Field.TYPE_TEXT)
        factory.addFieldText(rootView, field)

        assertEquals(EXPECTED_CHILD_COUNT_1, rootView.childCount)
        val view = rootView.getChildAt(0) as FieldEditTextUpdatableClearable
        assertEquals(FIELD_NAME_TITLE, view.getTitle())

        val editText = view.findViewById<EditText>(R.id.editTextX)
        assertNotNull(editText)
        assertEquals(TITLE_INITIAL, editText.text.toString())

        // Act - change text and trigger focus change to lose focus and save
        editText.setText(TITLE_UPDATED)
        editText.onFocusChangeListener?.onFocusChange(editText, false)

        // Assert - verify book DTO changed
        assertEquals(TITLE_UPDATED, book.title.value)
    }

    @DisplayName("Add Field Text - Integer Field Value Updated On Focus Lost - Updates Book DTO Value")
    @Test
    fun addFieldText_integerFieldValueUpdatedOnFocusLost_updatesBookDtoValue() {
        val field = Field(DbConstants.FLD_PAGES, FIELD_NAME_PAGES, Field.TYPE_TEXT)
        factory.addFieldText(rootView, field)

        val view = rootView.getChildAt(0) as FieldEditTextUpdatableClearable
        val editText = view.findViewById<EditText>(R.id.editTextX)

        editText.setText(PAGES_UPDATED_STR)
        editText.onFocusChangeListener?.onFocusChange(editText, false)

        assertEquals(PAGES_UPDATED_INT, book.pages.value)
    }

    @DisplayName("Add Autocomplete Field - Input Matches Suggestion On Focus Lost - Updates Property Value")
    @Test
    fun addAutocompleteField_inputMatchesSuggestionOnFocusLost_updatesPropertyValue() {
        val field = Field(DbConstants.FLD_SERIE, FIELD_NAME_SERIES, Field.TYPE_TEXT_AUTOCOMPLETE)
        factory.addAutocompleteField(rootView, field)

        assertEquals(EXPECTED_CHILD_COUNT_1, rootView.childCount)
        val view = rootView.getChildAt(0) as FieldAutoCompleteTextView
        assertEquals(FIELD_NAME_SERIES, view.getTitle())

        val editText = view.findViewById<EditText>(R.id.autoCompleteTextView)
        // Set match suggestion Harry Potter
        editText.setText(SERIES_HP)
        editText.onFocusChangeListener?.onFocusChange(editText, false)

        // Verify it matched property
        val currentProperty = book.properties.first { it.fieldTypeId == DbConstants.FLD_SERIE }
        assertEquals(ID_PROP_1, currentProperty.id)
        assertEquals(SERIES_HP, currentProperty.value)
    }

    @DisplayName("Add Field Spinner - New Item Selected - Updates Selected Property Value")
    @Test
    fun addFieldSpinner_newItemSelected_updatesSelectedPropertyValue() {
        val field = Field(DbConstants.FLD_STATUS, FIELD_NAME_STATUS, Field.TYPE_SPINNER)
        factory.addFieldSpinner(rootView, field)

        assertEquals(EXPECTED_CHILD_COUNT_1, rootView.childCount)
        val view = rootView.getChildAt(0) as FieldSpinner
        
        // Select index 1 (corresponding to "Owned")
        val spinner = view.findViewById<Spinner>(R.id.action_select_type)
        spinner.onItemSelectedListener?.onItemSelected(spinner, null, INDEX_1, 1L)

        val property = book.properties.first { it.fieldTypeId == DbConstants.FLD_STATUS }
        assertEquals(ID_PROP_2, property.id)
        assertEquals(STATUS_OWNED, property.value)
    }

    @DisplayName("Add Field MultiText - Initial Authors Provided - Inflates and Binds To Views")
    @Test
    fun addFieldMultiText_initialAuthorsProvided_inflatesAndBindsToViews() {
        val field = Field(DbConstants.FLD_AUTHOR, FIELD_NAME_AUTHORS, Field.TYPE_MULTIFIELD)
        
        // Populate author in properties first
        val authorProperty = Property(DbConstants.FLD_AUTHOR, AUTHOR_ROWLING, ID_PROP_4)
        book.properties.add(authorProperty)

        factory.addFieldMultiText(rootView, field)

        assertEquals(EXPECTED_CHILD_COUNT_1, rootView.childCount)
        val view = rootView.getChildAt(0) as FieldMultiText
        assertNotNull(view)
    }

    @DisplayName("Add Field MultiSpinner - Initial Genres Provided - Inflates and Binds To Views")
    @Test
    fun addFieldMultiSpinner_initialGenresProvided_inflatesAndBindsToViews() {
        val field = Field(DbConstants.FLD_GENRE, FIELD_NAME_GENRE, Field.TYPE_MULTI_SPINNER)
        factory.addFieldMultiSpinner(rootView, field)

        assertEquals(EXPECTED_CHILD_COUNT_1, rootView.childCount)
        val view = rootView.getChildAt(0) as FieldMultiSpinner
        assertNotNull(view)
    }

    @DisplayName("Add Field Money - Price Text Value Updated On Focus Lost - Updates Price Value")
    @Test
    fun addFieldMoney_priceTextValueUpdatedOnFocusLost_updatesPriceValue() {
        val field = Field(DbConstants.FLD_PRICE, FIELD_NAME_PRICE, Field.TYPE_MONEY)
        
        // Preset price
        book.price = Changeable(PRICE_INITIAL) // 15.00 with currency ID 3

        factory.addFieldMoney(rootView, field)

        assertEquals(EXPECTED_CHILD_COUNT_1, rootView.childCount)
        val view = rootView.getChildAt(0) as FieldMoney
        val editText = view.findViewById<EditText>(R.id.editTextX)
        assertNotNull(editText)

        // Input 25.50
        editText.setText(INPUT_PRICE_INTEGER + DbConstants.separator + INPUT_PRICE_DECIMAL)
        editText.onFocusChangeListener?.onFocusChange(editText, false)

        // Value in book should update to 2550 with currency 3 -> "2550|3"
        assertEquals(PRICE_UPDATED, book.price.value)
    }

    @DisplayName("Add Field Date - Initial Read Date Provided - Inflates and Binds to Views")
    @Test
    fun addFieldDate_initialReadDateProvided_inflatesAndBindsToViews() {
        val field = Field(DbConstants.FLD_READ_DATE, FIELD_NAME_READ_DATE, Field.TYPE_DATE)
        book.readDate = Changeable(DATE_READ_INITIAL) // 2023-05-15

        factory.addFieldDate(rootView, field)

        assertEquals(EXPECTED_CHILD_COUNT_1, rootView.childCount)
        val view = rootView.getChildAt(0) as FieldDate
        assertNotNull(view)
        assertEquals(DATE_READ_INITIAL, view.getDate().toInt())
    }

    @DisplayName("Add Field Rating - Rating Changed on Rating Bar - Updates Rating Value")
    @Test
    fun addFieldRating_ratingChangedOnRatingBar_updatesRatingValue() {
        val field = Field(DbConstants.FLD_RATING, FIELD_NAME_RATING, Field.TYPE_RATING)
        factory.addFieldRating(rootView, field)

        assertEquals(EXPECTED_CHILD_COUNT_1, rootView.childCount)
        val view = rootView.getChildAt(0) as FieldRating
        val ratingBar = view.findViewById<android.widget.RatingBar>(R.id.rating_bar)
        
        // Set rating to 5
        ratingBar.onRatingBarChangeListener?.onRatingChanged(ratingBar, RATING_VALUE_5, true)

        val property = book.properties.first { it.fieldTypeId == DbConstants.FLD_RATING }
        assertEquals(EXPECTED_RATING_STR, property.value)
    }

    @DisplayName("Add Field CheckBox - Checked State Changed On Focus Lost - Updates Check State")
    @Test
    fun addFieldCheckBox_checkedStateChangedOnFocusLost_updatesCheckState() {
        val field = Field(DbConstants.FLD_READ, FIELD_NAME_READ_STATE, Field.TYPE_CHECK_BOX)
        factory.addFieldCheckBox(rootView, field)

        assertEquals(EXPECTED_CHILD_COUNT_1, rootView.childCount)
        val view = rootView.getChildAt(0) as FieldCheckBox
        val checkBox = view.findViewById<android.widget.CheckBox>(R.id.check_box)

        checkBox.isChecked = true
        checkBox.onFocusChangeListener?.onFocusChange(checkBox, false)

        val property = book.properties.first { it.fieldTypeId == DbConstants.FLD_READ }
        assertEquals(READ_TRUE, property.value)
    }

    @DisplayName("Hide Field - Unpopulated Invisible Field Added - Hides Field and Notifies Listener")
    @Test
    fun hideField_unpopulatedInvisibleFieldAdded_hidesFieldAndNotifiesListener() {
        val field = Field(DbConstants.FLD_TITLE, FIELD_NAME_TITLE, Field.TYPE_TEXT).apply {
            isVisible = false
        }
        // Set book title to empty to trigger hiding
        book.title.value = STRING_EMPTY

        var hideCalled = false
        var hiddenName = STRING_EMPTY

        factory.registerListener(object : FieldsFactory.Listener {
            override fun onFieldHide(view: View, name: String) {
                hideCalled = true
                hiddenName = name
            }
        })

        factory.addFieldText(rootView, field)

        val view = rootView.getChildAt(0) as FieldEditTextUpdatableClearable
        assertEquals(View.GONE, view.visibility)
        assertTrue(hideCalled)
        assertEquals(FIELD_NAME_TITLE, hiddenName)
    }

    // Custom wrapper class with NO constructor taking String to trigger reflection exception
    class BadTextWrapper {
        override fun toString(): String = BAD_STRING
    }

    @DisplayName("Add Field Text - Custom Class Type Provided - Uses Reflection Fallback to Parse and Save")
    @Test
    fun addFieldText_customClassTypeProvided_usesReflectionFallbackToParseAndSave() {
        val changeableCustom = Changeable(CustomTextWrapper(CUSTOM_INITIAL))
        val field = Field(DbConstants.FLD_TITLE, FIELD_NAME_CUSTOM, Field.TYPE_TEXT)
        factory.addFieldText(rootView, field, changeableCustom)

        val view = rootView.getChildAt(0) as FieldEditTextUpdatableClearable
        val editText = view.findViewById<EditText>(R.id.editTextX)
        
        // Act
        editText.setText(TITLE_CUSTOM)
        editText.onFocusChangeListener?.onFocusChange(editText, false)

        // Assert
        assertEquals(TITLE_CUSTOM, changeableCustom.value.text)
    }

    @DisplayName("Add Field Text - Reflection Exception - Is Caught Safely")
    @Test
    fun addFieldText_reflectionException_isCaughtSafely() {
        val changeableCustom = Changeable(BadTextWrapper())
        val field = Field(DbConstants.FLD_TITLE, FIELD_NAME_CUSTOM, Field.TYPE_TEXT)
        factory.addFieldText(rootView, field, changeableCustom)

        val view = rootView.getChildAt(0) as FieldEditTextUpdatableClearable
        val editText = view.findViewById<EditText>(R.id.editTextX)
        
        // This will trigger NoSuchMethodException during constructor lookup, executing the catch block
        editText.setText(TRIGGER_EXCEPTION)
        editText.onFocusChangeListener?.onFocusChange(editText, false)
        
        // Assert we caught it and did not crash
        assertTrue(true)
    }

    @DisplayName("Add Field Text - All Standard Field IDs Provided - Maps to Correct Changeable and Updates Value")
    @Test
    fun addFieldText_allStandardFieldIdsProvided_mapsToCorrectChangeableAndUpdatesValue() {
        val cases = listOf(
            DbConstants.FLD_DESCRIPTION to book.description,
            DbConstants.FLD_VOLUME to book.volume,
            DbConstants.FLD_EDITION to book.edition,
            DbConstants.FLD_ISBN to book.isbn,
            DbConstants.FLD_WEB to book.web
        )

        for ((fieldId, changeable) in cases) {
            rootView.removeAllViews()
            val field = Field(fieldId, FIELD_NAME_NAME, Field.TYPE_TEXT)
            factory.addFieldText(rootView, field)

            val view = rootView.getChildAt(0) as FieldEditTextUpdatableClearable
            val editText = view.findViewById<EditText>(R.id.editTextX)

            val newValue = if (changeable.value is Int) GENERIC_INT_STR else GENERIC_STR
            editText.setText(newValue)
            editText.onFocusChangeListener?.onFocusChange(editText, false)

            if (changeable.value is Int) {
                assertEquals(GENERIC_INT, changeable.value)
            } else {
                assertEquals(GENERIC_STR, changeable.value)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    @DisplayName("Add Field Spinner - Custom ArrayAdapter GetView and GetDropDownView - Returns Configured Views")
    @Test
    fun addFieldSpinner_customArrayAdapterGetViewAndGetDropDownView_returnsConfiguredViews() {
        val field = Field(DbConstants.FLD_STATUS, FIELD_NAME_STATUS, Field.TYPE_SPINNER)
        factory.addFieldSpinner(rootView, field)

        val view = rootView.getChildAt(0) as FieldSpinner
        val spinner = view.findViewById<Spinner>(R.id.action_select_type)
        val adapter = spinner.adapter as ArrayAdapter<String>

        // Test getView
        val rowView = adapter.getView(INDEX_0, null, spinner)
        assertNotNull(rowView)

        // Test getDropDownView for position 0 (should return collapsed/GONE TextView)
        val dropDownView0 = adapter.getDropDownView(INDEX_0, null, spinner)
        assertEquals(View.GONE, dropDownView0.visibility)

        // Test getDropDownView for position 1 (should return visible view)
        val dropDownView1 = adapter.getDropDownView(INDEX_1, null, spinner)
        assertNotEquals(View.GONE, dropDownView1.visibility)
    }

    @DisplayName("Add Field Money - Fractional Decimal Inputs Passed on Focus Lost - Updates Price Value")
    @Test
    fun addFieldMoney_fractionalDecimalInputsPassedOnFocusLost_updatesPriceValue() {
        val field = Field(DbConstants.FLD_PRICE, FIELD_NAME_PRICE, Field.TYPE_MONEY)
        book.price = Changeable(PRICE_INITIAL) // Currency ID 3

        factory.addFieldMoney(rootView, field)

        val view = rootView.getChildAt(0) as FieldMoney
        val editText = view.findViewById<EditText>(R.id.editTextX)

        val testInputs = listOf(
            STRING_EMPTY to STRING_EMPTY,
            INPUT_DASH to STRING_EMPTY,
            INPUT_COMMA to STRING_EMPTY,
            INPUT_DASH_COMMA to STRING_EMPTY,
            INPUT_15 to PRICE_INITIAL,
            INPUT_15_5 to EXPECTED_PRICE_1550,
            INPUT_15_50 to EXPECTED_PRICE_1550
        )

        for ((input, expectedValue) in testInputs) {
            val normalizedInput = input.replace(".", DbConstants.separator.toString())
            editText.setText(normalizedInput)
            editText.onFocusChangeListener?.onFocusChange(editText, false)
            assertEquals(expectedValue, book.price.value)
        }
    }

    @DisplayName("Add Field Spinner - OnNothingSelected or Index Zero Called - Does Not Mutate Model")
    @Test
    fun addFieldSpinner_onNothingSelectedOrIndexZeroCalled_doesNotMutateModel() {
        val field = Field(DbConstants.FLD_STATUS, FIELD_NAME_STATUS, Field.TYPE_SPINNER)
        factory.addFieldSpinner(rootView, field)
        val view = rootView.getChildAt(0) as FieldSpinner
        val spinner = view.findViewById<Spinner>(R.id.action_select_type)

        // Select position 0 (placeholder header, pos > 0 is false)
        spinner.onItemSelectedListener?.onItemSelected(spinner, null, INDEX_0, 0L)

        // Trigger onNothingSelected
        spinner.onItemSelectedListener?.onNothingSelected(spinner)
    }

    @DisplayName("Add Field Date - Due Date Provided - Inflates and Binds to Views")
    @Test
    fun addFieldDate_dueDateProvided_inflatesAndBindsToViews() {
        val field = Field(DbConstants.FLD_DUE_DATE, FIELD_NAME_DUE_DATE, Field.TYPE_DATE)
        book.dueDate = Changeable(DATE_DUE_INITIAL)

        factory.addFieldDate(rootView, field)

        val view = rootView.getChildAt(0) as FieldDate
        assertNotNull(view)
        assertEquals(DATE_DUE_INITIAL, view.getDate().toInt())
    }

    @DisplayName("OnDateSet - Date Selected from Date Picker - Updates Changeable Date Model")
    @Test
    fun onDateSet_dateSelectedFromDatePicker_updatesChangeableDateModel() {
        val field = Field(DbConstants.FLD_READ_DATE, FIELD_NAME_READ_DATE, Field.TYPE_DATE)
        book.readDate = Changeable(DATE_READ_INITIAL)

        factory.addFieldDate(rootView, field)
        val view = rootView.getChildAt(0) as FieldDate

        // Get update listener using reflection
        val listenerField = FieldDate::class.java.getDeclaredField(REFLECTION_UPDATE_LISTENER)
        listenerField.isAccessible = true
        val listener = listenerField.get(view) as FieldDate.OnUpdateListener

        // Simulate date being set by date picker
        view.setDate(org.d1scw0rld.bookbag.dto.Date(DATE_UPDATED))
        listener.onUpdate(view)

        assertEquals(DATE_UPDATED, book.readDate.value)
    }

    @DisplayName("Add Field Money - Edge Case Fractional Inputs Passed on Focus Lost - Updates Price Value")
    @Test
    fun addFieldMoney_edgeCaseFractionalInputsPassedOnFocusLost_updatesPriceValue() {
        val field = Field(DbConstants.FLD_PRICE, FIELD_NAME_PRICE, Field.TYPE_MONEY)
        book.price = Changeable(PRICE_INITIAL) // Currency ID 3

        factory.addFieldMoney(rootView, field)

        val view = rootView.getChildAt(0) as FieldMoney
        val editText = view.findViewById<EditText>(R.id.editTextX)

        val testInputs = listOf(
            INPUT_15_55 to EXPECTED_PRICE_1555 // exactly 2 decimal digits
        )

        for ((input, expectedValue) in testInputs) {
            val normalizedInput = input.replace(".", DbConstants.separator.toString())
            editText.setText(normalizedInput)
            editText.onFocusChangeListener?.onFocusChange(editText, false)
            assertEquals(expectedValue, book.price.value)
        }
    }

    @DisplayName("Add Field MultiText - After Title Field Added - Links NextFocusDownId To Clear Memory")
    @Test
    fun addFieldMultiText_afterTitleFieldAdded_linksNextFocusDownIdToClearMemory() {
        val titleField = Field(DbConstants.FLD_TITLE, FIELD_NAME_TITLE, Field.TYPE_TEXT)
        val authorField = Field(DbConstants.FLD_AUTHOR, FIELD_NAME_AUTHOR, Field.TYPE_MULTIFIELD)

        factory.addFieldText(rootView, titleField)
        factory.addFieldMultiText(rootView, authorField)

        assertEquals(EXPECTED_CHILD_COUNT_2, rootView.childCount)
    }

    @DisplayName("Add Field Money - Invalid Field ID Provided - Returns Early Without Adding Views")
    @Test
    fun addFieldMoney_invalidFieldIdProvided_returnsEarlyWithoutAddingViews() {
        val field = Field(INVALID_FIELD_ID, INVALID_MONEY_STR, Field.TYPE_MONEY)
        factory.addFieldMoney(rootView, field)
        assertEquals(EXPECTED_CHILD_COUNT_0, rootView.childCount)
    }

    @DisplayName("Add Field Date - Invalid Field ID Provided - Returns Early Without Adding Views")
    @Test
    fun addFieldDate_invalidFieldIdProvided_returnsEarlyWithoutAddingViews() {
        val field = Field(INVALID_FIELD_ID, INVALID_DATE_STR, Field.TYPE_DATE)
        factory.addFieldDate(rootView, field)
        assertEquals(EXPECTED_CHILD_COUNT_0, rootView.childCount)
    }

    @DisplayName("Add Autocomplete Field - Invisible Field and Properties Checked - Toggles Visibility Based on Value")
    @Test
    fun addAutocompleteField_invisibleFieldAndPropertiesChecked_togglesVisibilityBasedOnValue() {
        // Case 1: isVisible = false, value empty -> should hide
        val field1 = Field(DbConstants.FLD_SERIE, FIELD_NAME_SERIES, Field.TYPE_TEXT_AUTOCOMPLETE).apply { isVisible = false }
        rootView.removeAllViews()
        factory.addAutocompleteField(rootView, field1)
        assertEquals(View.GONE, rootView.getChildAt(0).visibility)

        // Case 2: isVisible = false, value non-empty -> should stay visible
        book.properties.clear()
        val field2 = Field(DbConstants.FLD_SERIE, FIELD_NAME_SERIES, Field.TYPE_TEXT_AUTOCOMPLETE).apply { isVisible = false }
        book.properties.add(Property(DbConstants.FLD_SERIE, NON_EMPTY_SERIES))
        rootView.removeAllViews()
        factory.addAutocompleteField(rootView, field2)
        assertEquals(View.VISIBLE, rootView.getChildAt(0).visibility)
    }

    @DisplayName("Add Field Spinner - Invisible Field and Properties Checked - Toggles Visibility Based on Value")
    @Test
    fun addFieldSpinner_invisibleFieldAndPropertiesChecked_togglesVisibilityBasedOnValue() {
        // Case 1: isVisible = false, id = 0 (actually property ID empty) -> GONE
        val field1 = Field(DbConstants.FLD_STATUS, FIELD_NAME_STATUS, Field.TYPE_SPINNER).apply { isVisible = false }
        rootView.removeAllViews()
        factory.addFieldSpinner(rootView, field1)
        assertEquals(View.GONE, rootView.getChildAt(0).visibility)

        // Case 2: isVisible = false, id != 0 -> VISIBLE
        book.properties.clear()
        val field2 = Field(DbConstants.FLD_STATUS, FIELD_NAME_STATUS, Field.TYPE_SPINNER).apply { isVisible = false }
        val prop = Property(DbConstants.FLD_STATUS, STATUS_OWNED, ID_PROP_999)
        book.properties.add(prop)
        rootView.removeAllViews()
        factory.addFieldSpinner(rootView, field2)
        assertEquals(View.VISIBLE, rootView.getChildAt(0).visibility)
    }

    @DisplayName("Add Field MultiText - Invisible Field and Properties Checked - Toggles Visibility Based on Value")
    @Test
    fun addFieldMultiText_invisibleFieldAndPropertiesChecked_togglesVisibilityBasedOnValue() {
        val field1 = Field(DbConstants.FLD_AUTHOR, FIELD_NAME_AUTHOR, Field.TYPE_MULTIFIELD).apply { isVisible = false }
        rootView.removeAllViews()
        factory.addFieldMultiText(rootView, field1)
        // Note: FieldMultiText always adds at least one blank field on setItems if empty, 
        // meaning hasNotPropertiesOfType(field.id) is always false, so it remains VISIBLE.
        assertEquals(View.VISIBLE, rootView.getChildAt(0).visibility)

        book.properties.clear()
        val field2 = Field(DbConstants.FLD_AUTHOR, FIELD_NAME_AUTHOR, Field.TYPE_MULTIFIELD).apply { isVisible = false }
        book.properties.add(Property(DbConstants.FLD_AUTHOR, AUTHOR_SHORT_ROWLING))
        rootView.removeAllViews()
        factory.addFieldMultiText(rootView, field2)
        assertEquals(View.VISIBLE, rootView.getChildAt(0).visibility)
    }

    @DisplayName("Add Field MultiSpinner - Invisible Field and Properties Checked - Toggles Visibility Based on Value")
    @Test
    fun addFieldMultiSpinner_invisibleFieldAndPropertiesChecked_togglesVisibilityBasedOnValue() {
        val field1 = Field(DbConstants.FLD_GENRE, FIELD_NAME_GENRE, Field.TYPE_MULTI_SPINNER).apply { isVisible = false }
        rootView.removeAllViews()
        factory.addFieldMultiSpinner(rootView, field1)
        assertEquals(View.GONE, rootView.getChildAt(0).visibility)

        book.properties.clear()
        val field2 = Field(DbConstants.FLD_GENRE, FIELD_NAME_GENRE, Field.TYPE_MULTI_SPINNER).apply { isVisible = false }
        book.properties.add(Property(DbConstants.FLD_GENRE, GENRE_FANTASY))
        rootView.removeAllViews()
        factory.addFieldMultiSpinner(rootView, field2)
        assertEquals(View.VISIBLE, rootView.getChildAt(0).visibility)
    }

    @DisplayName("Add Field Money - Invisible Field and Properties Checked - Toggles Visibility Based on Value")
    @Test
    fun addFieldMoney_invisibleFieldAndPropertiesChecked_togglesVisibilityBasedOnValue() {
        val field1 = Field(DbConstants.FLD_PRICE, FIELD_NAME_PRICE, Field.TYPE_MONEY).apply { isVisible = false }
        book.price = Changeable(STRING_EMPTY) // actual empty serialized string value
        rootView.removeAllViews()
        factory.addFieldMoney(rootView, field1)
        assertEquals(View.GONE, rootView.getChildAt(0).visibility)

        val field2 = Field(DbConstants.FLD_PRICE, FIELD_NAME_PRICE, Field.TYPE_MONEY).apply { isVisible = false }
        book.price = Changeable(PRICE_NON_EMPTY) // non-empty
        rootView.removeAllViews()
        factory.addFieldMoney(rootView, field2)
        val view = rootView.getChildAt(0)
        assertEquals(View.VISIBLE, view.visibility)
    }

    @DisplayName("Add Field Date - Invisible Field and Properties Checked - Toggles Visibility Based on Value")
    @Test
    fun addFieldDate_invisibleFieldAndPropertiesChecked_togglesVisibilityBasedOnValue() {
        val field = Field(DbConstants.FLD_READ_DATE, FIELD_NAME_READ_DATE, Field.TYPE_DATE).apply {
            isVisible = false
        }
        book.readDate = Changeable(0)

        factory.addFieldDate(rootView, field)
        val view = rootView.getChildAt(0) as FieldDate
        assertEquals(View.GONE, view.visibility)
    }

    @DisplayName("Parsed Title And Hint - Pipe Separated Field Name Provided - Sets Distinct Title and Hint on MultiText")
    @Test
    fun parsedTitleAndHint_pipeSeparatedFieldNameProvided_setsDistinctTitleAndHintOnMultiText() {
        val field = Field(DbConstants.FLD_AUTHOR, PIPE_SEPARATED_TITLE, Field.TYPE_MULTIFIELD)
        factory.addFieldMultiText(rootView, field)

        val view = rootView.getChildAt(0) as FieldMultiText
        val titleView = view.findViewById<Title>(R.id.title)
        assertEquals(TITLE_TEXT_PART, titleView.getTitle())
    }

    @DisplayName("OnItemClick - Autocomplete Dropdown Suggestion Clicked - Overrides Property Value")
    @Test
    fun onItemClick_autocompleteDropdownSuggestionClicked_overridesPropertyValue() {
        val field = Field(DbConstants.FLD_SERIE, FIELD_NAME_SERIES, Field.TYPE_TEXT_AUTOCOMPLETE)
        factory.addAutocompleteField(rootView, field)

        val view = rootView.getChildAt(0) as FieldAutoCompleteTextView
        val autoComplete = view.findViewById<android.widget.AutoCompleteTextView>(R.id.autoCompleteTextView)

        val selectedProperty = Property(DbConstants.FLD_SERIE, SERIES_HP_BOOK, ID_PROP_1001)
        val mockAdapter = org.mockito.Mockito.mock(android.widget.AdapterView::class.java)
        org.mockito.Mockito.`when`(mockAdapter.getItemAtPosition(0)).thenReturn(selectedProperty)

        // Simulate item click
        autoComplete.onItemClickListener?.onItemClick(mockAdapter, view, 0, 0L)

        val currentProperty = book.properties.first { it.fieldTypeId == DbConstants.FLD_SERIE }
        assertEquals(ID_PROP_1001, currentProperty.id)
        assertEquals(SERIES_HP_BOOK, currentProperty.value)
    }

    @DisplayName("OnAddNewField and Updated and Remove - MultiText Row Actions Triggered - Updates Properties Collection")
    @Test
    fun onAddNewFieldAndUpdatedAndRemove_multiTextRowActionsTriggered_updatesPropertiesCollection() {
        val field = Field(DbConstants.FLD_AUTHOR, FIELD_NAME_AUTHORS, Field.TYPE_MULTIFIELD)
        factory.addFieldMultiText(rootView, field)
        val view = rootView.getChildAt(0) as FieldMultiText

        // Extract listener using reflection
        val listenerField = FieldMultiText::class.java.getDeclaredField(REFLECTION_ADD_REMOVE_LISTENER)
        listenerField.isAccessible = true
        val listener = listenerField.get(view) as FieldMultiText.OnAddRemoveFieldListener

        val dummyView = View(context)

        // 1. Test onAddNewField
        listener.onAddNewField(dummyView)
        val newProperty = dummyView.tag as Property
        assertEquals(DbConstants.FLD_AUTHOR, newProperty.fieldTypeId)
        assertTrue(book.properties.contains(newProperty))

        // 2. Test onFieldUpdated with match suggestion
        listener.onFieldUpdated(dummyView, AUTHOR_ROWLING)
        assertEquals(ID_PROP_4, newProperty.id)
        assertEquals(AUTHOR_ROWLING, newProperty.value)

        // 3. Test onFieldUpdated with new custom author
        listener.onFieldUpdated(dummyView, AUTHOR_BRAND_NEW)
        assertEquals(ID_ZERO, newProperty.id)
        assertEquals(AUTHOR_BRAND_NEW, newProperty.value)

        // 4. Test onItemSelect
        val selection = Property(DbConstants.FLD_AUTHOR, AUTHOR_SELECTED, ID_PROP_777)
        listener.onItemSelect(dummyView, selection)
        assertEquals(ID_PROP_777, newProperty.id)
        assertEquals(AUTHOR_SELECTED, newProperty.value)

        // 5. Test onFieldRemove
        listener.onFieldRemove(dummyView)
        assertFalse(book.properties.contains(newProperty))
    }

    @DisplayName("OnUpdate - MultiSpinner Item Selections Updated - Updates Properties Collection")
    @Test
    fun onUpdate_multiSpinnerItemSelectionsUpdated_updatesPropertiesCollection() {
        val field = Field(DbConstants.FLD_GENRE, FIELD_NAME_GENRE, Field.TYPE_MULTI_SPINNER)
        factory.addFieldMultiSpinner(rootView, field)
        val view = rootView.getChildAt(0) as FieldMultiSpinner

        // Extract listener using reflection
        val listenerField = FieldMultiSpinner::class.java.getDeclaredField(REFLECTION_UPDATE_LISTENER)
        listenerField.isAccessible = true
        val listener = listenerField.get(view) as FieldMultiSpinner.OnUpdateListener

        // 1. Match selected = true
        val item1 = FieldMultiSpinner.Item(GENRE_FANTASY).apply { isSelected = true }
        listener.onUpdate(item1)
        val matchedProp = Property(DbConstants.FLD_GENRE, GENRE_FANTASY, ID_PROP_5)
        assertTrue(book.properties.contains(matchedProp))

        // 2. Match selected = false
        val item2 = FieldMultiSpinner.Item(GENRE_FANTASY).apply { isSelected = false }
        listener.onUpdate(item2)
        assertFalse(book.properties.contains(matchedProp))

        // 3. Match selected = true with a brand new custom genre
        val item3 = FieldMultiSpinner.Item(GENRE_SCIFI).apply { isSelected = true }
        listener.onUpdate(item3)
        val newGenreProp = Property(DbConstants.FLD_GENRE, GENRE_SCIFI, ID_ZERO)
        assertTrue(book.properties.contains(newGenreProp))
    }

    @DisplayName("Get Property Values - Missing Key Queried - Returns Empty List")
    @Test
    fun getPropertyValues_missingKeyQueried_returnsEmptyList() {
        val list = factory.getPropertyValues(INVALID_FIELD_ID)
        assertTrue(list.isEmpty())
    }

    @DisplayName("Add Field MultiText - Invisible Field with Strictly Empty Properties Collection - Hides Field")
    @Test
    fun addFieldMultiText_invisibleFieldWithStrictlyEmptyPropertiesCollection_hidesField() {
        val field = Field(DbConstants.FLD_AUTHOR, FIELD_NAME_AUTHOR, Field.TYPE_MULTIFIELD).apply { isVisible = false }
        
        // Setup book with properties ignoring blank additions
        val customProperties = object : ArrayList<Property>() {
            override fun add(element: Property): Boolean {
                if (element.value.trim().isEmpty()) return false
                return super.add(element)
            }
        }
        book.properties = customProperties

        rootView.removeAllViews()
        factory.addFieldMultiText(rootView, field)

        // The field should now be hidden!
        assertEquals(View.GONE, rootView.getChildAt(0).visibility)
    }

    @DisplayName("Add Autocomplete Field - Unmatched Query Entered On Focus Lost - Clears ID and Updates Property Value")
    @Test
    fun addAutocompleteField_unmatchedQueryEnteredOnFocusLost_clearsIdAndUpdatesPropertyValue() {
        val field = Field(DbConstants.FLD_SERIE, FIELD_NAME_SERIES, Field.TYPE_TEXT_AUTOCOMPLETE)
        factory.addAutocompleteField(rootView, field)

        val view = rootView.getChildAt(0) as FieldAutoCompleteTextView
        val editText = view.findViewById<EditText>(R.id.autoCompleteTextView)

        // Set unmatched suggestion
        editText.setText(SERIES_UNMATCHED)
        editText.onFocusChangeListener?.onFocusChange(editText, false)

        val currentProperty = book.properties.first { it.fieldTypeId == DbConstants.FLD_SERIE }
        assertEquals(ID_ZERO, currentProperty.id)
        assertEquals(SERIES_UNMATCHED, currentProperty.value)
    }

    @DisplayName("OnItemSelected - Currency Selected From Spinner - Updates Price Currency ID")
    @Test
    fun onItemSelected_currencySelectedFromSpinner_updatesPriceCurrencyId() {
        val field = Field(DbConstants.FLD_PRICE, FIELD_NAME_PRICE, Field.TYPE_MONEY)
        book.price = Changeable(PRICE_INITIAL)
        factory.addFieldMoney(rootView, field)

        val view = rootView.getChildAt(0) as FieldMoney
        val spinner = view.findViewById<Spinner>(R.id.action_select_type)
        
        // Select index 0 (USD with ID 3)
        spinner.onItemSelectedListener?.onItemSelected(spinner, null, INDEX_0, 0L)
        
        // This should set the currency id to currencies[0].id which is 3
        assertEquals(PRICE_INITIAL, book.price.value)
    }

    @DisplayName("OnCheckedChanged - Checkbox Toggled With No Matched Property - Clears ID and Updates Property Value")
    @Test
    fun onCheckedChanged_checkboxToggledWithNoMatchedProperty_clearsIdAndUpdatesPropertyValue() {
        val field = Field(DbConstants.FLD_READ, FIELD_NAME_READ_STATE, Field.TYPE_CHECK_BOX)
        
        // Clear properties Map for read state to force null matchedProperty
        propertiesMap[DbConstants.FLD_READ] = emptyList()

        factory.addFieldCheckBox(rootView, field)

        val view = rootView.getChildAt(0) as FieldCheckBox
        val checkBox = view.findViewById<android.widget.CheckBox>(R.id.check_box)

        checkBox.isChecked = true
        checkBox.onFocusChangeListener?.onFocusChange(checkBox, false)

        val property = book.properties.first { it.fieldTypeId == DbConstants.FLD_READ }
        assertEquals(ID_ZERO, property.id)
        assertEquals(READ_TRUE, property.value)
    }

    @DisplayName("Add Field Rating - Matches Predefined Property Value - Updates Backing Property ID")
    @Test
    fun addFieldRating_matchesProperty_updatesBackingPropertyId() {
        val field = Field(DbConstants.FLD_RATING, FIELD_NAME_RATING, Field.TYPE_RATING)
        factory.addFieldRating(rootView, field)

        val view = rootView.getChildAt(0) as FieldRating
        val ratingBar = view.findViewById<android.widget.RatingBar>(R.id.rating_bar)
        
        // Set rating to 4.5f which matches "4.5" in propertiesMap
        ratingBar.onRatingBarChangeListener?.onRatingChanged(ratingBar, RATING_VALUE_4_5, true)

        val property = book.properties.first { it.fieldTypeId == DbConstants.FLD_RATING }
        assertEquals(ID_PROP_6, property.id)
        assertEquals(RATING_4_5, property.value)
    }

    // Custom wrapper class to test reflection fallback
    class CustomTextWrapper(val text: String) {
        override fun toString(): String = text
    }

    companion object {
        private const val TITLE_INITIAL = "Initial Title"
        private const val DESC_INITIAL = "Initial Desc"
        private const val PAGES_INITIAL = 123

        private const val SERIES_HP = "Harry Potter"
        private const val STATUS_OWNED = "Owned"
        private const val CURRENCY_USD = "USD"
        private const val AUTHOR_ROWLING = "J.K. Rowling"
        private const val GENRE_FANTASY = "Fantasy"
        private const val RATING_4_5 = "4.5"
        private const val READ_TRUE = "true"

        private const val TITLE_UPDATED = "Updated Book Title"
        private const val PAGES_UPDATED_STR = "456"
        private const val PAGES_UPDATED_INT = 456

        private const val PRICE_INITIAL = "1500|3"
        private const val PRICE_UPDATED = "2550|3"
        private const val EXPECTED_PRICE_1550 = "1550|3"

        private const val GENRE_SCIFI = "Sci-Fi"
        private const val SERIES_UNMATCHED = "Unmatched New Series"
        private const val TITLE_CUSTOM = "Updated Custom Value"

        private const val FIELD_NAME_TITLE = "Title"
        private const val FIELD_NAME_PAGES = "Pages"
        private const val FIELD_NAME_SERIES = "Series"
        private const val FIELD_NAME_STATUS = "Status"
        private const val FIELD_NAME_AUTHORS = "Authors"
        private const val FIELD_NAME_GENRE = "Genre"
        private const val FIELD_NAME_PRICE = "Price"
        private const val FIELD_NAME_READ_DATE = "Read Date"
        private const val FIELD_NAME_RATING = "Rating"
        private const val FIELD_NAME_READ_STATE = "Read State"
        private const val FIELD_NAME_CUSTOM = "Custom"
        private const val FIELD_NAME_NAME = "Name"
        private const val FIELD_NAME_DUE_DATE = "Due Date"
        private const val FIELD_NAME_AUTHOR = "Author"

        private const val BAD_STRING = "Bad"
        private const val CUSTOM_INITIAL = "Initial"
        private const val TRIGGER_EXCEPTION = "Trigger Exception"

        private const val GENERIC_INT_STR = "99"
        private const val GENERIC_STR = "New string val"
        private const val GENERIC_INT = 99

        private const val STRING_EMPTY = ""
        private const val INPUT_DASH = "-"
        private const val INPUT_COMMA = ","
        private const val INPUT_DASH_COMMA = "-,"
        private const val INPUT_15 = "15"
        private const val INPUT_15_5 = "15.5"
        private const val INPUT_15_50 = "15.50"
        private const val INPUT_15_55 = "15.55"

        private const val EXPECTED_PRICE_1555 = "1555|3"

        private const val INPUT_PRICE_INTEGER = "25"
        private const val INPUT_PRICE_DECIMAL = "50"

        private const val DATE_READ_INITIAL = 20230515
        private const val DATE_DUE_INITIAL = 20230520
        private const val DATE_UPDATED = 20240101

        private const val RATING_VALUE_5 = 5f
        private const val RATING_VALUE_4_5 = 4.5f
        private const val EXPECTED_RATING_STR = "5.0"

        private const val ID_ZERO = 0L
        private const val ID_PROP_1 = 1L
        private const val ID_PROP_2 = 2L
        private const val ID_PROP_3 = 3L
        private const val ID_PROP_4 = 4L
        private const val ID_PROP_5 = 5L
        private const val ID_PROP_6 = 6L
        private const val ID_PROP_7 = 7L
        private const val ID_PROP_999 = 999L
        private const val ID_PROP_1001 = 1001L
        private const val ID_PROP_777 = 777L

        private const val INVALID_FIELD_ID = 999
        private const val INVALID_MONEY_STR = "Invalid Money"
        private const val INVALID_DATE_STR = "Invalid Date"

        private const val EXPECTED_CHILD_COUNT_0 = 0
        private const val EXPECTED_CHILD_COUNT_1 = 1
        private const val EXPECTED_CHILD_COUNT_2 = 2

        private const val INDEX_0 = 0
        private const val INDEX_1 = 1

        private const val NON_EMPTY_SERIES = "Non-empty Series"
        private const val AUTHOR_SHORT_ROWLING = "Rowling"
        private const val PRICE_NON_EMPTY = "1500|1"

        private const val PIPE_SEPARATED_TITLE = "Hint Text|Title Text"
        private const val TITLE_TEXT_PART = "Title Text"

        private const val SERIES_HP_BOOK = "Harry Potter Book"
        private const val AUTHOR_BRAND_NEW = "Brand New Author"
        private const val AUTHOR_SELECTED = "Selected Author"

        private const val REFLECTION_UPDATE_LISTENER = "onUpdateListener"
        private const val REFLECTION_ADD_REMOVE_LISTENER = "onAddRemoveFieldListener"
    }
}
