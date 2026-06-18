package org.d1scw0rld.bookbag.ui.factories

import android.content.Context
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import org.d1scw0rld.bookbag.R
import org.d1scw0rld.bookbag.data.DbConstants
import org.d1scw0rld.bookbag.dto.Book
import org.d1scw0rld.bookbag.dto.Changeable
import org.d1scw0rld.bookbag.dto.Property
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.d1scw0rld.bookbag.DisplayNameRobolectricRunner
import org.robolectric.annotation.Config

@RunWith(DisplayNameRobolectricRunner::class)
@Config(sdk = [28])
class BookDetailFieldsFactoryTest {

    private lateinit var context: Context
    private lateinit var currencies: List<Property>
    private lateinit var book: Book

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        DbConstants.initFields(context.resources)
        currencies = listOf(Property(DbConstants.FLD_CURRENCY, "USD", 1L))
        book = Book(
            id = 10L,
            title = Changeable("My Book Title"),
            description = Changeable("Excellent reads"),
            pages = Changeable(320),
            price = Changeable("2999|1"), // $29.99
            properties = ArrayList(),
        )
    }

    @DisplayName("Add Fields - Populated Book Fields Provided - Populates Expected Categories Layouts")
    @Test
    fun addFields_populatedBookFieldsProvided_populatesExpectedCategoriesLayouts() {
        val factory = BookDetailFieldsFactory(context, currencies, book)

        val rootView = LinearLayout(context)
        val categoriesLayout = LinearLayout(context).apply {
            id = R.id.ll_categories
        }
        rootView.addView(categoriesLayout)

        // Act
        factory.addFields(rootView)

        // Assert
        var hasDesc = false
        var hasPages = false
        var hasPrice = false

        for (i in 0 until categoriesLayout.childCount) {
            val child = categoriesLayout.getChildAt(i)
            val titleText = child.findViewById<TextView>(R.id.tv_title)?.text?.toString()
            val valueText = child.findViewById<TextView>(R.id.tv_value)?.text?.toString()

            if (titleText == context.resources.getString(R.string.fld_description)) {
                hasDesc = true
                assertEquals("Excellent reads", valueText)
            }
            if (titleText == context.resources.getString(R.string.fld_pages)) {
                hasPages = true
                assertEquals("320", valueText)
            }
            if (titleText == context.resources.getString(R.string.fld_price)) {
                hasPrice = true
                assertTrue(valueText?.contains("29") == true)
                assertTrue(valueText?.contains("99") == true)
                assertTrue(valueText?.contains("USD") == true)
            }
        }

        assertTrue(hasDesc)
        assertTrue(hasPages)
        assertTrue(hasPrice)
    }

    @DisplayName("Add Fields - Null Book Provided - Returns Early Without Adding Views")
    @Test
    fun addFields_nullBookProvided_returnsEarlyWithoutAddingViews() {
        val factory = BookDetailFieldsFactory(context, currencies, null)

        val rootView = LinearLayout(context)
        val categoriesLayout = LinearLayout(context).apply {
            id = R.id.ll_categories
        }
        rootView.addView(categoriesLayout)

        factory.addFields(rootView)

        assertEquals(0, categoriesLayout.childCount)
    }

    @DisplayName("Add Fields - Rating and Checkbox Properties Provided - Creates Rating and CheckBox Category Rows")
    @Test
    fun addFields_ratingAndCheckboxPropertiesProvided_createsRatingAndCheckBoxCategoryRows() {
        // Add rating and checkbox properties
        val ratingProperty = Property(DbConstants.FLD_RATING, "4.0")
        val readProperty = Property(DbConstants.FLD_READ, "true")
        book.properties.add(ratingProperty)
        book.properties.add(readProperty)

        val factory = BookDetailFieldsFactory(context, currencies, book)

        val rootView = LinearLayout(context)
        val categoriesLayout = LinearLayout(context).apply {
            id = R.id.ll_categories
        }
        rootView.addView(categoriesLayout)

        factory.addFields(rootView)

        var hasRating = false
        var hasRead = false

        for (i in 0 until categoriesLayout.childCount) {
            val child = categoriesLayout.getChildAt(i)
            val titleText = child.findViewById<TextView>(R.id.tv_title)?.text?.toString()

            if (titleText == context.resources.getString(R.string.fld_rating)) {
                hasRating = true
                val ratingBar = child.findViewById<RatingBar>(R.id.rating_bar)
                assertNotNull(ratingBar)
                assertEquals(4f, ratingBar.rating)
            }
            if (titleText == context.resources.getString(R.string.fld_read)) {
                hasRead = true
                val checkBox = child.findViewById<android.widget.CheckBox>(R.id.check_box)
                assertNotNull(checkBox)
                assertTrue(checkBox.isChecked)
            }
        }

        assertTrue(hasRating)
        assertTrue(hasRead)
    }
}
