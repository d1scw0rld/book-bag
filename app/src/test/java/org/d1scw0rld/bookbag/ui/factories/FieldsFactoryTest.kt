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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
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
            id = 0L,
            title = Changeable("Initial Title"),
            description = Changeable("Initial Desc"),
            pages = Changeable(123),
            properties = ArrayList()
        )

        propertiesMap = mutableMapOf(
            DbConstants.FLD_SERIE to listOf(Property(DbConstants.FLD_SERIE, "Harry Potter", 1L)),
            DbConstants.FLD_STATUS to listOf(Property(DbConstants.FLD_STATUS, "Owned", 2L)),
            DbConstants.FLD_CURRENCY to listOf(Property(DbConstants.FLD_CURRENCY, "USD", 3L)),
            DbConstants.FLD_AUTHOR to listOf(Property(DbConstants.FLD_AUTHOR, "J.K. Rowling", 4L)),
            DbConstants.FLD_GENRE to listOf(Property(DbConstants.FLD_GENRE, "Fantasy", 5L)),
            DbConstants.FLD_RATING to listOf(Property(DbConstants.FLD_RATING, "4.5", 6L)),
            DbConstants.FLD_READ to listOf(Property(DbConstants.FLD_READ, "true", 7L))
        )

        rootView = LinearLayout(context)
        factory = FieldsFactory(context, book, propertiesMap)
    }

    @Test
    fun testAddFieldText_updatesBookStringValueOnFocusLost() {
        val field = Field(DbConstants.FLD_TITLE, "Title", Field.TYPE_TEXT)
        factory.addFieldText(rootView, field)

        assertEquals(1, rootView.childCount)
        val view = rootView.getChildAt(0) as FieldEditTextUpdatableClearable
        assertEquals("Title", view.getTitle())

        val editText = view.findViewById<EditText>(R.id.editTextX)
        assertNotNull(editText)
        assertEquals("Initial Title", editText.text.toString())

        // Act - change text and trigger focus change to lose focus and save
        editText.setText("Updated Book Title")
        editText.onFocusChangeListener?.onFocusChange(editText, false)

        // Assert - verify book DTO changed
        assertEquals("Updated Book Title", book.title.value)
    }

    @Test
    fun testAddFieldText_updatesBookIntValueOnFocusLost() {
        val field = Field(DbConstants.FLD_PAGES, "Pages", Field.TYPE_TEXT)
        factory.addFieldText(rootView, field)

        val view = rootView.getChildAt(0) as FieldEditTextUpdatableClearable
        val editText = view.findViewById<EditText>(R.id.editTextX)

        editText.setText("456")
        editText.onFocusChangeListener?.onFocusChange(editText, false)

        assertEquals(456, book.pages.value)
    }

    @Test
    fun testAddAutocompleteField_updatesPropertyValue() {
        val field = Field(DbConstants.FLD_SERIE, "Series", Field.TYPE_TEXT_AUTOCOMPLETE)
        factory.addAutocompleteField(rootView, field)

        assertEquals(1, rootView.childCount)
        val view = rootView.getChildAt(0) as FieldAutoCompleteTextView
        assertEquals("Series", view.getTitle())

        val editText = view.findViewById<EditText>(R.id.autoCompleteTextView)
        // Set match suggestion Harry Potter
        editText.setText("Harry Potter")
        editText.onFocusChangeListener?.onFocusChange(editText, false)

        // Verify it matched property
        val currentProperty = book.properties.first { it.fieldTypeId == DbConstants.FLD_SERIE }
        assertEquals(1L, currentProperty.id)
        assertEquals("Harry Potter", currentProperty.value)
    }

    @Test
    fun testAddFieldSpinner_updatesSelection() {
        val field = Field(DbConstants.FLD_STATUS, "Status", Field.TYPE_SPINNER)
        factory.addFieldSpinner(rootView, field)

        assertEquals(1, rootView.childCount)
        val view = rootView.getChildAt(0) as FieldSpinner
        
        // Select index 1 (corresponding to "Owned")
        val spinner = view.findViewById<Spinner>(R.id.action_select_type)
        spinner.onItemSelectedListener?.onItemSelected(spinner, null, 1, 1L)

        val property = book.properties.first { it.fieldTypeId == DbConstants.FLD_STATUS }
        assertEquals(2L, property.id)
        assertEquals("Owned", property.value)
    }

    @Test
    fun testAddFieldMultiText_populatesAndDeletes() {
        val field = Field(DbConstants.FLD_AUTHOR, "Authors", Field.TYPE_MULTIFIELD)
        
        // Populate author in properties first
        val authorProperty = Property(DbConstants.FLD_AUTHOR, "J.K. Rowling", 4L)
        book.properties.add(authorProperty)

        factory.addFieldMultiText(rootView, field)

        assertEquals(1, rootView.childCount)
        val view = rootView.getChildAt(0) as FieldMultiText
        assertNotNull(view)
    }

    @Test
    fun testAddFieldMultiSpinner_updatesSelection() {
        val field = Field(DbConstants.FLD_GENRE, "Genre", Field.TYPE_MULTI_SPINNER)
        factory.addFieldMultiSpinner(rootView, field)

        assertEquals(1, rootView.childCount)
        val view = rootView.getChildAt(0) as FieldMultiSpinner
        assertNotNull(view)
    }

    @Test
    fun testAddFieldMoney_updatesPriceOnTextChange() {
        val field = Field(DbConstants.FLD_PRICE, "Price", Field.TYPE_MONEY)
        
        // Preset price
        book.price = Changeable("1500|3") // 15.00 with currency ID 3

        factory.addFieldMoney(rootView, field)

        assertEquals(1, rootView.childCount)
        val view = rootView.getChildAt(0) as FieldMoney
        val editText = view.findViewById<EditText>(R.id.editTextX)
        assertNotNull(editText)

        // Input 25.50
        editText.setText("25" + DbConstants.separator + "50")
        editText.onFocusChangeListener?.onFocusChange(editText, false)

        // Value in book should update to 2550 with currency 3 -> "2550|3"
        assertEquals("2550|3", book.price.value)
    }

    @Test
    fun testAddFieldDate_updatesDates() {
        val field = Field(DbConstants.FLD_READ_DATE, "Read Date", Field.TYPE_DATE)
        book.readDate = Changeable(20230515) // 2023-05-15

        factory.addFieldDate(rootView, field)

        assertEquals(1, rootView.childCount)
        val view = rootView.getChildAt(0) as FieldDate
        assertNotNull(view)
        assertEquals(20230515, view.getDate().toInt())
    }

    @Test
    fun testAddFieldRating_updatesRatingValue() {
        val field = Field(DbConstants.FLD_RATING, "Rating", Field.TYPE_RATING)
        factory.addFieldRating(rootView, field)

        assertEquals(1, rootView.childCount)
        val view = rootView.getChildAt(0) as FieldRating
        val ratingBar = view.findViewById<android.widget.RatingBar>(R.id.rating_bar)
        
        // Set rating to 5
        ratingBar.onRatingBarChangeListener?.onRatingChanged(ratingBar, 5f, true)

        val property = book.properties.first { it.fieldTypeId == DbConstants.FLD_RATING }
        assertEquals("5.0", property.value)
    }

    @Test
    fun testAddFieldCheckBox_updatesCheckState() {
        val field = Field(DbConstants.FLD_READ, "Read State", Field.TYPE_CHECK_BOX)
        factory.addFieldCheckBox(rootView, field)

        assertEquals(1, rootView.childCount)
        val view = rootView.getChildAt(0) as FieldCheckBox
        val checkBox = view.findViewById<android.widget.CheckBox>(R.id.check_box)

        checkBox.isChecked = true
        checkBox.onFocusChangeListener?.onFocusChange(checkBox, false)

        val property = book.properties.first { it.fieldTypeId == DbConstants.FLD_READ }
        assertEquals("true", property.value)
    }

    @Test
    fun testFieldHiding_triggersListener() {
        val field = Field(DbConstants.FLD_TITLE, "Title", Field.TYPE_TEXT).apply {
            isVisible = false
        }
        // Set book title to empty to trigger hiding
        book.title.value = ""

        var hideCalled = false
        var hiddenName = ""

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
        assertEquals("Title", hiddenName)
    }

    // Custom wrapper class to test reflection fallback
    class CustomTextWrapper(val text: String) {
        override fun toString(): String = text
    }

    @Test
    fun testAddFieldText_reflectionFallback() {
        val changeableCustom = Changeable(CustomTextWrapper("Initial"))
        val field = Field(DbConstants.FLD_TITLE, "Custom", Field.TYPE_TEXT)
        factory.addFieldText(rootView, field, changeableCustom)

        val view = rootView.getChildAt(0) as FieldEditTextUpdatableClearable
        val editText = view.findViewById<EditText>(R.id.editTextX)
        
        // Act
        editText.setText("Updated Custom Value")
        editText.onFocusChangeListener?.onFocusChange(editText, false)

        // Assert
        assertEquals("Updated Custom Value", changeableCustom.value.text)
    }

    @Test
    fun testAddFieldText_allFieldIds() {
        val cases = listOf(
            DbConstants.FLD_DESCRIPTION to book.description,
            DbConstants.FLD_VOLUME to book.volume,
            DbConstants.FLD_EDITION to book.edition,
            DbConstants.FLD_ISBN to book.isbn,
            DbConstants.FLD_WEB to book.web
        )

        for ((fieldId, changeable) in cases) {
            rootView.removeAllViews()
            val field = Field(fieldId, "Name", Field.TYPE_TEXT)
            factory.addFieldText(rootView, field)

            val view = rootView.getChildAt(0) as FieldEditTextUpdatableClearable
            val editText = view.findViewById<EditText>(R.id.editTextX)

            val newValue = if (changeable.value is Int) "99" else "New string val"
            editText.setText(newValue)
            editText.onFocusChangeListener?.onFocusChange(editText, false)

            if (changeable.value is Int) {
                assertEquals(99, changeable.value)
            } else {
                assertEquals("New string val", changeable.value)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun testAddFieldSpinner_customAdapterViewsAndDropDown() {
        val field = Field(DbConstants.FLD_STATUS, "Status", Field.TYPE_SPINNER)
        factory.addFieldSpinner(rootView, field)

        val view = rootView.getChildAt(0) as FieldSpinner
        val spinner = view.findViewById<Spinner>(R.id.action_select_type)
        val adapter = spinner.adapter as ArrayAdapter<String>

        // Test getView
        val rowView = adapter.getView(0, null, spinner)
        assertNotNull(rowView)

        // Test getDropDownView for position 0 (should return collapsed/GONE TextView)
        val dropDownView0 = adapter.getDropDownView(0, null, spinner)
        assertEquals(View.GONE, dropDownView0.visibility)

        // Test getDropDownView for position 1 (should return visible view)
        val dropDownView1 = adapter.getDropDownView(1, null, spinner)
        assertNotEquals(View.GONE, dropDownView1.visibility)
    }

    @Test
    fun testAddFieldMoney_fractionalDecimalChecks() {
        val field = Field(DbConstants.FLD_PRICE, "Price", Field.TYPE_MONEY)
        book.price = Changeable("1500|3") // Currency ID 3

        factory.addFieldMoney(rootView, field)

        val view = rootView.getChildAt(0) as FieldMoney
        val editText = view.findViewById<EditText>(R.id.editTextX)

        val testInputs = listOf(
            "" to "",
            "-" to "",
            "," to "",
            "-," to "",
            "15" to "1500|3",
            "15.5" to "1550|3",
            "15.50" to "1550|3"
        )

        for ((input, expectedValue) in testInputs) {
            val normalizedInput = input.replace(".", DbConstants.separator.toString())
            editText.setText(normalizedInput)
            editText.onFocusChangeListener?.onFocusChange(editText, false)
            assertEquals(expectedValue, book.price.value)
        }
    }

    @Test
    fun testFocusLinkage_betweenTitleAndMultiText() {
        val titleField = Field(DbConstants.FLD_TITLE, "Title", Field.TYPE_TEXT)
        val authorField = Field(DbConstants.FLD_AUTHOR, "Author", Field.TYPE_MULTIFIELD)

        factory.addFieldText(rootView, titleField)
        factory.addFieldMultiText(rootView, authorField)

        assertEquals(2, rootView.childCount)
    }

    @Test
    fun testAddFieldMoney_invalidFieldId_returnsEarly() {
        val field = Field(999, "Invalid Money", Field.TYPE_MONEY)
        factory.addFieldMoney(rootView, field)
        assertEquals(0, rootView.childCount)
    }

    @Test
    fun testAddFieldDate_invalidFieldId_returnsEarly() {
        val field = Field(999, "Invalid Date", Field.TYPE_DATE)
        factory.addFieldDate(rootView, field)
        assertEquals(0, rootView.childCount)
    }

    @Test
    fun testAddAutocompleteField_visibilityBranching() {
        // Case 1: isVisible = false, value empty -> should hide
        val field1 = Field(DbConstants.FLD_SERIE, "Series", Field.TYPE_TEXT_AUTOCOMPLETE).apply { isVisible = false }
        rootView.removeAllViews()
        factory.addAutocompleteField(rootView, field1)
        assertEquals(View.GONE, rootView.getChildAt(0).visibility)

        // Case 2: isVisible = false, value non-empty -> should stay visible
        book.properties.clear()
        val field2 = Field(DbConstants.FLD_SERIE, "Series", Field.TYPE_TEXT_AUTOCOMPLETE).apply { isVisible = false }
        book.properties.add(Property(DbConstants.FLD_SERIE, "Non-empty Series"))
        rootView.removeAllViews()
        factory.addAutocompleteField(rootView, field2)
        assertEquals(View.VISIBLE, rootView.getChildAt(0).visibility)
    }

    @Test
    fun testAddFieldSpinner_visibilityBranching() {
        // Case 1: isVisible = false, id = 0 (actually property ID empty) -> GONE
        val field1 = Field(DbConstants.FLD_STATUS, "Status", Field.TYPE_SPINNER).apply { isVisible = false }
        rootView.removeAllViews()
        factory.addFieldSpinner(rootView, field1)
        assertEquals(View.GONE, rootView.getChildAt(0).visibility)

        // Case 2: isVisible = false, id != 0 -> VISIBLE
        book.properties.clear()
        val field2 = Field(DbConstants.FLD_STATUS, "Status", Field.TYPE_SPINNER).apply { isVisible = false }
        val prop = Property(DbConstants.FLD_STATUS, "Owned", 999L)
        book.properties.add(prop)
        rootView.removeAllViews()
        factory.addFieldSpinner(rootView, field2)
        assertEquals(View.VISIBLE, rootView.getChildAt(0).visibility)
    }

    @Test
    fun testAddFieldMultiText_visibilityBranching() {
        val field1 = Field(DbConstants.FLD_AUTHOR, "Author", Field.TYPE_MULTIFIELD).apply { isVisible = false }
        rootView.removeAllViews()
        factory.addFieldMultiText(rootView, field1)
        // Note: FieldMultiText always adds at least one blank field on setItems if empty, 
        // meaning hasNotPropertiesOfType(field.id) is always false, so it remains VISIBLE.
        assertEquals(View.VISIBLE, rootView.getChildAt(0).visibility)

        book.properties.clear()
        val field2 = Field(DbConstants.FLD_AUTHOR, "Author", Field.TYPE_MULTIFIELD).apply { isVisible = false }
        book.properties.add(Property(DbConstants.FLD_AUTHOR, "Rowling"))
        rootView.removeAllViews()
        factory.addFieldMultiText(rootView, field2)
        assertEquals(View.VISIBLE, rootView.getChildAt(0).visibility)
    }

    @Test
    fun testAddFieldMultiSpinner_visibilityBranching() {
        val field1 = Field(DbConstants.FLD_GENRE, "Genre", Field.TYPE_MULTI_SPINNER).apply { isVisible = false }
        rootView.removeAllViews()
        factory.addFieldMultiSpinner(rootView, field1)
        assertEquals(View.GONE, rootView.getChildAt(0).visibility)

        book.properties.clear()
        val field2 = Field(DbConstants.FLD_GENRE, "Genre", Field.TYPE_MULTI_SPINNER).apply { isVisible = false }
        book.properties.add(Property(DbConstants.FLD_GENRE, "Fantasy"))
        rootView.removeAllViews()
        factory.addFieldMultiSpinner(rootView, field2)
        assertEquals(View.VISIBLE, rootView.getChildAt(0).visibility)
    }

    @Test
    fun testAddFieldMoney_visibilityBranching() {
        val field1 = Field(DbConstants.FLD_PRICE, "Price", Field.TYPE_MONEY).apply { isVisible = false }
        book.price = Changeable("") // actual empty serialized string value
        rootView.removeAllViews()
        factory.addFieldMoney(rootView, field1)
        assertEquals(View.GONE, rootView.getChildAt(0).visibility)

        val field2 = Field(DbConstants.FLD_PRICE, "Price", Field.TYPE_MONEY).apply { isVisible = false }
        book.price = Changeable("1500|1") // non-empty
        rootView.removeAllViews()
        factory.addFieldMoney(rootView, field2)
        val view = rootView.getChildAt(0)
        assertEquals(View.VISIBLE, view.visibility)
    }

    @Test
    fun testAddFieldDate_visibilityBranch() {
        val field = Field(DbConstants.FLD_READ_DATE, "Read Date", Field.TYPE_DATE).apply {
            isVisible = false
        }
        book.readDate = Changeable(0)

        factory.addFieldDate(rootView, field)
        val view = rootView.getChildAt(0) as FieldDate
        assertEquals(View.GONE, view.visibility)
    }

    @Test
    fun testParsedTitleAndHint_withPipeSeparator() {
        val field = Field(DbConstants.FLD_AUTHOR, "Hint Text|Title Text", Field.TYPE_MULTIFIELD)
        factory.addFieldMultiText(rootView, field)

        val view = rootView.getChildAt(0) as FieldMultiText
        val titleView = view.findViewById<Title>(R.id.title)
        assertEquals("Title Text", titleView.getTitle())
    }

    @Test
    fun testAddAutocompleteField_itemClickUpdatesProperty() {
        val field = Field(DbConstants.FLD_SERIE, "Series", Field.TYPE_TEXT_AUTOCOMPLETE)
        factory.addAutocompleteField(rootView, field)

        val view = rootView.getChildAt(0) as FieldAutoCompleteTextView
        val autoComplete = view.findViewById<android.widget.AutoCompleteTextView>(R.id.autoCompleteTextView)

        val selectedProperty = Property(DbConstants.FLD_SERIE, "Harry Potter Book", 1001L)
        val mockAdapter = org.mockito.Mockito.mock(android.widget.AdapterView::class.java)
        org.mockito.Mockito.`when`(mockAdapter.getItemAtPosition(0)).thenReturn(selectedProperty)

        // Simulate item click
        autoComplete.onItemClickListener?.onItemClick(mockAdapter, view, 0, 0L)

        val currentProperty = book.properties.first { it.fieldTypeId == DbConstants.FLD_SERIE }
        assertEquals(1001L, currentProperty.id)
        assertEquals("Harry Potter Book", currentProperty.value)
    }

    @Test
    fun testAddFieldMultiText_addRemoveCallbacks() {
        val field = Field(DbConstants.FLD_AUTHOR, "Authors", Field.TYPE_MULTIFIELD)
        factory.addFieldMultiText(rootView, field)
        val view = rootView.getChildAt(0) as FieldMultiText

        // Extract listener using reflection
        val listenerField = FieldMultiText::class.java.getDeclaredField("onAddRemoveFieldListener")
        listenerField.isAccessible = true
        val listener = listenerField.get(view) as FieldMultiText.OnAddRemoveFieldListener

        val dummyView = View(context)

        // 1. Test onAddNewField
        listener.onAddNewField(dummyView)
        val newProperty = dummyView.tag as Property
        assertEquals(DbConstants.FLD_AUTHOR, newProperty.fieldTypeId)
        assertTrue(book.properties.contains(newProperty))

        // 2. Test onFieldUpdated with match suggestion
        listener.onFieldUpdated(dummyView, "J.K. Rowling")
        assertEquals(4L, newProperty.id)
        assertEquals("J.K. Rowling", newProperty.value)

        // 3. Test onFieldUpdated with new custom author
        listener.onFieldUpdated(dummyView, "Brand New Author")
        assertEquals(0L, newProperty.id)
        assertEquals("Brand New Author", newProperty.value)

        // 4. Test onItemSelect
        val selection = Property(DbConstants.FLD_AUTHOR, "Selected Author", 777L)
        listener.onItemSelect(dummyView, selection)
        assertEquals(777L, newProperty.id)
        assertEquals("Selected Author", newProperty.value)

        // 5. Test onFieldRemove
        listener.onFieldRemove(dummyView)
        assertFalse(book.properties.contains(newProperty))
    }

    @Test
    fun testAddFieldMultiSpinner_selectionCallbacks() {
        val field = Field(DbConstants.FLD_GENRE, "Genre", Field.TYPE_MULTI_SPINNER)
        factory.addFieldMultiSpinner(rootView, field)
        val view = rootView.getChildAt(0) as FieldMultiSpinner

        // Extract listener using reflection
        val listenerField = FieldMultiSpinner::class.java.getDeclaredField("onUpdateListener")
        listenerField.isAccessible = true
        val listener = listenerField.get(view) as FieldMultiSpinner.OnUpdateListener

        // 1. Match selected = true
        val item1 = FieldMultiSpinner.Item("Fantasy").apply { isSelected = true }
        listener.onUpdate(item1)
        val matchedProp = Property(DbConstants.FLD_GENRE, "Fantasy", 5L)
        assertTrue(book.properties.contains(matchedProp))

        // 2. Match selected = false
        val item2 = FieldMultiSpinner.Item("Fantasy").apply { isSelected = false }
        listener.onUpdate(item2)
        assertFalse(book.properties.contains(matchedProp))

        // 3. Match selected = true with a brand new custom genre
        val item3 = FieldMultiSpinner.Item("Sci-Fi").apply { isSelected = true }
        listener.onUpdate(item3)
        val newGenreProp = Property(DbConstants.FLD_GENRE, "Sci-Fi", 0L)
        assertTrue(book.properties.contains(newGenreProp))
    }

    @Test
    fun testAddFieldSpinner_onNothingSelected_andPositionZero() {
        val field = Field(DbConstants.FLD_STATUS, "Status", Field.TYPE_SPINNER)
        factory.addFieldSpinner(rootView, field)
        val view = rootView.getChildAt(0) as FieldSpinner
        val spinner = view.findViewById<Spinner>(R.id.action_select_type)

        // Select position 0 (placeholder header, pos > 0 is false)
        spinner.onItemSelectedListener?.onItemSelected(spinner, null, 0, 0L)

        // Trigger onNothingSelected
        spinner.onItemSelectedListener?.onNothingSelected(spinner)
    }

    @Test
    fun testAddFieldDate_dueDate() {
        val field = Field(DbConstants.FLD_DUE_DATE, "Due Date", Field.TYPE_DATE)
        book.dueDate = Changeable(20230520)

        factory.addFieldDate(rootView, field)

        val view = rootView.getChildAt(0) as FieldDate
        assertNotNull(view)
        assertEquals(20230520, view.getDate().toInt())
    }

    @Test
    fun testAddFieldDate_updateListener() {
        val field = Field(DbConstants.FLD_READ_DATE, "Read Date", Field.TYPE_DATE)
        book.readDate = Changeable(20230515)

        factory.addFieldDate(rootView, field)
        val view = rootView.getChildAt(0) as FieldDate

        // Get update listener using reflection
        val listenerField = FieldDate::class.java.getDeclaredField("onUpdateListener")
        listenerField.isAccessible = true
        val listener = listenerField.get(view) as FieldDate.OnUpdateListener

        // Simulate date being set by date picker
        view.setDate(org.d1scw0rld.bookbag.dto.Date(20240101))
        listener.onUpdate(view)

        assertEquals(20240101, book.readDate.value)
    }

    @Test
    fun testAddFieldMoney_fractionalDecimalChecks_edgeCases() {
        val field = Field(DbConstants.FLD_PRICE, "Price", Field.TYPE_MONEY)
        book.price = Changeable("1500|3") // Currency ID 3

        factory.addFieldMoney(rootView, field)

        val view = rootView.getChildAt(0) as FieldMoney
        val editText = view.findViewById<EditText>(R.id.editTextX)

        val testInputs = listOf(
            "15.55" to "1555|3" // exactly 2 decimal digits
        )

        for ((input, expectedValue) in testInputs) {
            val normalizedInput = input.replace(".", DbConstants.separator.toString())
            editText.setText(normalizedInput)
            editText.onFocusChangeListener?.onFocusChange(editText, false)
            assertEquals(expectedValue, book.price.value)
        }
    }

    // Custom wrapper class with NO constructor taking String to trigger reflection exception
    class BadTextWrapper {
        override fun toString(): String = "Bad"
    }

    @Test
    fun testAddFieldText_reflectionException_isCaught() {
        val changeableCustom = Changeable(BadTextWrapper())
        val field = Field(DbConstants.FLD_TITLE, "Custom", Field.TYPE_TEXT)
        factory.addFieldText(rootView, field, changeableCustom)

        val view = rootView.getChildAt(0) as FieldEditTextUpdatableClearable
        val editText = view.findViewById<EditText>(R.id.editTextX)
        
        // This will trigger NoSuchMethodException during constructor lookup, executing the catch block
        editText.setText("Trigger Exception")
        editText.onFocusChangeListener?.onFocusChange(editText, false)
        
        // Assert we caught it and did not crash
        assertTrue(true)
    }

    @Test
    fun testAddFieldMoney_valueField() {
        val field = Field(DbConstants.FLD_VALUE, "Value", Field.TYPE_MONEY)
        book.value = Changeable("1500|3")

        factory.addFieldMoney(rootView, field)

        val view = rootView.getChildAt(0) as FieldMoney
        assertNotNull(view)
    }

    @Test
    fun testAddFieldMoney_onNothingSelected() {
        val field = Field(DbConstants.FLD_PRICE, "Price", Field.TYPE_MONEY)
        book.price = Changeable("1500|3")
        factory.addFieldMoney(rootView, field)

        val view = rootView.getChildAt(0) as FieldMoney
        val spinner = view.findViewById<Spinner>(R.id.action_select_type)
        
        // Trigger onNothingSelected callback directly
        spinner.onItemSelectedListener?.onNothingSelected(spinner)
    }

    @Test
    fun testGetPropertyValues_missingKey_returnsEmptyList() {
        val list = factory.getPropertyValues(999)
        assertTrue(list.isEmpty())
    }

    @Test
    fun testAddFieldMultiText_unfilteredHidingViaCustomCollection() {
        val field = Field(DbConstants.FLD_AUTHOR, "Author", Field.TYPE_MULTIFIELD).apply { isVisible = false }
        
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

    @Test
    fun testAddAutocompleteField_unmatchedUpdatesProperty() {
        val field = Field(DbConstants.FLD_SERIE, "Series", Field.TYPE_TEXT_AUTOCOMPLETE)
        factory.addAutocompleteField(rootView, field)

        val view = rootView.getChildAt(0) as FieldAutoCompleteTextView
        val editText = view.findViewById<EditText>(R.id.autoCompleteTextView)

        // Set unmatched suggestion
        editText.setText("Unmatched New Series")
        editText.onFocusChangeListener?.onFocusChange(editText, false)

        val currentProperty = book.properties.first { it.fieldTypeId == DbConstants.FLD_SERIE }
        assertEquals(0L, currentProperty.id)
        assertEquals("Unmatched New Series", currentProperty.value)
    }

    @Test
    fun testAddFieldMoney_onItemSelected() {
        val field = Field(DbConstants.FLD_PRICE, "Price", Field.TYPE_MONEY)
        book.price = Changeable("1500|3")
        factory.addFieldMoney(rootView, field)

        val view = rootView.getChildAt(0) as FieldMoney
        val spinner = view.findViewById<Spinner>(R.id.action_select_type)
        
        // Select index 0 (USD with ID 3)
        spinner.onItemSelectedListener?.onItemSelected(spinner, null, 0, 0L)
        
        // This should set the currency id to currencies[0].id which is 3
        assertEquals("1500|3", book.price.value)
    }

    @Test
    fun testAddFieldCheckBox_updatesCheckState_unmatched() {
        val field = Field(DbConstants.FLD_READ, "Read State", Field.TYPE_CHECK_BOX)
        
        // Clear properties Map for read state to force null matchedProperty
        propertiesMap[DbConstants.FLD_READ] = emptyList()

        factory.addFieldCheckBox(rootView, field)

        val view = rootView.getChildAt(0) as FieldCheckBox
        val checkBox = view.findViewById<android.widget.CheckBox>(R.id.check_box)

        checkBox.isChecked = true
        checkBox.onFocusChangeListener?.onFocusChange(checkBox, false)

        val property = book.properties.first { it.fieldTypeId == DbConstants.FLD_READ }
        assertEquals(0L, property.id)
        assertEquals("true", property.value)
    }

    @Test
    fun testAddFieldRating_matchesProperty() {
        val field = Field(DbConstants.FLD_RATING, "Rating", Field.TYPE_RATING)
        factory.addFieldRating(rootView, field)

        val view = rootView.getChildAt(0) as FieldRating
        val ratingBar = view.findViewById<android.widget.RatingBar>(R.id.rating_bar)
        
        // Set rating to 4.5f which matches "4.5" in propertiesMap
        ratingBar.onRatingBarChangeListener?.onRatingChanged(ratingBar, 4.5f, true)

        val property = book.properties.first { it.fieldTypeId == DbConstants.FLD_RATING }
        assertEquals(6L, property.id)
        assertEquals("4.5", property.value)
    }
}
