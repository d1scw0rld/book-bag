package org.d1scw0rld.bookbag.ui

import android.os.Bundle
import android.os.Looper.getMainLooper
import androidx.appcompat.R.style
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.google.android.material.appbar.CollapsingToolbarLayout
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.test.runTest
import org.d1scw0rld.bookbag.DisplayNameRobolectricRunner
import org.d1scw0rld.bookbag.R
import org.d1scw0rld.bookbag.data.DbConstants
import org.d1scw0rld.bookbag.data.dao.BookDao
import org.d1scw0rld.bookbag.data.entity.BookEntity
import org.d1scw0rld.bookbag.data.entity.BookFieldCrossRef
import org.d1scw0rld.bookbag.data.entity.FieldEntity
import org.d1scw0rld.bookbag.dto.Date
import org.d1scw0rld.bookbag.dto.Price
import org.d1scw0rld.bookbag.launchFragmentInHiltContainer
import org.d1scw0rld.bookbag.waitFor
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@RunWith(DisplayNameRobolectricRunner::class)
@Config(application = HiltTestApplication::class, sdk = [28])
class BookDetailFragmentIntegrationTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var bookDao: BookDao

    @Before
    fun init() {
        hiltRule.inject()
        DbConstants.initFields(RuntimeEnvironment.getApplication().resources)
    }

    @DisplayName("On View Created - Fragment Inflates - Categories Layout Visible")
    @Test
    fun onViewCreated_fragmentInflates_categoriesLayoutVisible() = runTest {
        // Act: Launch fragment without arguments (loads bookId = 0L)
        launchFragmentInHiltContainer<BookDetailFragment>()

        // Assert: Main container layout is visible
        onView(withId(R.id.ll_categories)).check(matches(isDisplayed()))
    }

    @DisplayName("On View Created - All Book Properties - Detail Screen Shows All Populated Fields")
    @Test
    @Config(application = HiltTestApplication::class, sdk = [28], qualifiers = "w480dp-h3000dp")
    fun onViewCreated_allBookProperties_detailScreenShowsAllPopulatedFields() = runTest {
        val book = BookEntity(
            id = ID_203,
            title = TITLE_COMPLETE,
            description = DESC_COMPLETE,
            volume = VOL_5,
            publicationDate = PUB_DATE_2024,
            pages = PAGES_500,
            price = PRICE_5000_2,
            value = VALUE_6000_3,
            dueDate = DUE_DATE_20240101,
            readDate = READ_DATE_20231225,
            edition = EDITION_3,
            isbn = ISBN_COMPLETE,
            web = WEB_COMPLETE
        )
        bookDao.insertBook(book)
        
        val authorField = FieldEntity(id = FIELD_ID_AUTHOR_TOLKIEN, typeId = DbConstants.FLD_AUTHOR, name = FIELD_NAME_TOLKIEN)
        val authorField2 = FieldEntity(id = FIELD_ID_AUTHOR_PRATCHETT, typeId = DbConstants.FLD_AUTHOR, name = FIELD_NAME_PRATCHETT)
        val seriesField = FieldEntity(id = FIELD_ID_SERIES_LOTR, typeId = DbConstants.FLD_SERIE, name = FIELD_NAME_LOTR)
        val genreField = FieldEntity(id = FIELD_ID_GENRE_FANTASY, typeId = DbConstants.FLD_GENRE, name = FIELD_NAME_FANTASY)
        val languageField = FieldEntity(id = FIELD_ID_LANGUAGE_EN, typeId = DbConstants.FLD_LANGUAGE, name = FIELD_NAME_EN)
        val publisherField = FieldEntity(id = FIELD_ID_PUBLISHER_ALLEN, typeId = DbConstants.FLD_PUBLISHER, name = FIELD_NAME_ALLEN)
        val publicationLocationField = FieldEntity(id = FIELD_ID_LOCATION_LONDON, typeId = DbConstants.FLD_PUBLICATION_LOCATION, name = FIELD_NAME_LONDON)
        val statusField = FieldEntity(id = FIELD_ID_STATUS_OWNED, typeId = DbConstants.FLD_STATUS, name = FIELD_NAME_OWNED)
        val ratingField = FieldEntity(id = FIELD_ID_RATING_5, typeId = DbConstants.FLD_RATING, name = FIELD_NAME_5)
        val formatField = FieldEntity(id = FIELD_ID_FORMAT_HARDCOVER, typeId = DbConstants.FLD_FORMAT, name = FIELD_NAME_HARDCOVER)
        val locationField = FieldEntity(id = FIELD_ID_LOC_HOME, typeId = DbConstants.FLD_LOCATION, name = FIELD_NAME_HOME)
        val conditionField = FieldEntity(id = FIELD_ID_CONDITION_MINT, typeId = DbConstants.FLD_CONDITION, name = FIELD_NAME_MINT)
        val readField = FieldEntity(id = FIELD_ID_READ_TRUE, typeId = DbConstants.FLD_READ, name = FIELD_NAME_TRUE)
        val loanedToField = FieldEntity(id = FIELD_ID_LOANED_TO_JOHN, typeId = DbConstants.FLD_LOANED_TO, name = FIELD_NAME_JOHN)
        
        bookDao.insertField(authorField)
        bookDao.insertField(authorField2)
        bookDao.insertField(seriesField)
        bookDao.insertField(genreField)
        bookDao.insertField(languageField)
        bookDao.insertField(publisherField)
        bookDao.insertField(publicationLocationField)
        bookDao.insertField(statusField)
        bookDao.insertField(ratingField)
        bookDao.insertField(formatField)
        bookDao.insertField(locationField)
        bookDao.insertField(conditionField)
        bookDao.insertField(readField)
        bookDao.insertField(loanedToField)
        
        bookDao.insertBookFieldCrossRef(BookFieldCrossRef(bookId = ID_203, fieldId = FIELD_ID_AUTHOR_TOLKIEN))
        bookDao.insertBookFieldCrossRef(BookFieldCrossRef(bookId = ID_203, fieldId = FIELD_ID_AUTHOR_PRATCHETT))
        bookDao.insertBookFieldCrossRef(BookFieldCrossRef(bookId = ID_203, fieldId = FIELD_ID_SERIES_LOTR))
        bookDao.insertBookFieldCrossRef(BookFieldCrossRef(bookId = ID_203, fieldId = FIELD_ID_GENRE_FANTASY))
        bookDao.insertBookFieldCrossRef(BookFieldCrossRef(bookId = ID_203, fieldId = FIELD_ID_LANGUAGE_EN))
        bookDao.insertBookFieldCrossRef(BookFieldCrossRef(bookId = ID_203, fieldId = FIELD_ID_PUBLISHER_ALLEN))
        bookDao.insertBookFieldCrossRef(BookFieldCrossRef(bookId = ID_203, fieldId = FIELD_ID_LOCATION_LONDON))
        bookDao.insertBookFieldCrossRef(BookFieldCrossRef(bookId = ID_203, fieldId = FIELD_ID_STATUS_OWNED))
        bookDao.insertBookFieldCrossRef(BookFieldCrossRef(bookId = ID_203, fieldId = FIELD_ID_RATING_5))
        bookDao.insertBookFieldCrossRef(BookFieldCrossRef(bookId = ID_203, fieldId = FIELD_ID_FORMAT_HARDCOVER))
        bookDao.insertBookFieldCrossRef(BookFieldCrossRef(bookId = ID_203, fieldId = FIELD_ID_LOC_HOME))
        bookDao.insertBookFieldCrossRef(BookFieldCrossRef(bookId = ID_203, fieldId = FIELD_ID_CONDITION_MINT))
        bookDao.insertBookFieldCrossRef(BookFieldCrossRef(bookId = ID_203, fieldId = FIELD_ID_READ_TRUE))
        bookDao.insertBookFieldCrossRef(BookFieldCrossRef(bookId = ID_203, fieldId = FIELD_ID_LOANED_TO_JOHN))

        // Act: Launch BookFragment (the real parent), which embeds BookDetailFragment as a
        // child fragment. The toolbar title is only set via
        // parentFragment?.view?.findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout),
        // so BookDetailFragment must be launched through its real parent for this to work.
        val mockNavController = mock(NavController::class.java)
        val args = Bundle().apply { putLong(ARG_BOOK_ID, ID_203) }
        launchFragmentInHiltContainer<BookFragment>(
            fragmentArgs = args,
            themeResId = style.Theme_AppCompat_Light_NoActionBar
        ) {
            Navigation.setViewNavController(requireView(), mockNavController)
        }
        Shadows.shadowOf(getMainLooper()).idle()

        // Assert: Wait for the form to finish rendering all rows
        onView(androidx.test.espresso.matcher.ViewMatchers.isRoot())
            .perform(waitFor(withText(R.string.fld_publication_date), TIMEOUT_5000))

        // Assert: Book title is set on the CollapsingToolbarLayout
        onView(withId(R.id.toolbar_layout)).check { view, _ ->
            assertEquals(TITLE_COMPLETE, (view as CollapsingToolbarLayout).title)
        }

        // Assert: Description field label and value are displayed
        onView(withText(R.string.fld_description)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(DESC_COMPLETE)).perform(scrollTo()).check(matches(isDisplayed()))

        // Assert: Volume field label and value are displayed
        onView(withText(R.string.fld_volume)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(VOL_5.toString())).perform(scrollTo()).check(matches(isDisplayed()))

        // Assert: Publication date field label and value are displayed
        onView(withText(R.string.fld_publication_date)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(PUB_DATE_2024.toString())).perform(scrollTo()).check(matches(isDisplayed()))

        // Assert: Pages field label and value are displayed
        onView(withText(R.string.fld_pages)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(PAGES_500.toString())).perform(scrollTo()).check(matches(isDisplayed()))

        // Assert: Edition field label and value are displayed
        onView(withText(R.string.fld_edition)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(EDITION_3.toString())).perform(scrollTo()).check(matches(isDisplayed()))

        // Assert: ISBN field label and value are displayed
        onView(withText(R.string.fld_isbn)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(ISBN_COMPLETE)).perform(scrollTo()).check(matches(isDisplayed()))

        // Assert: Web field label and value are displayed
        onView(withText(R.string.fld_web)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(WEB_COMPLETE)).perform(scrollTo()).check(matches(isDisplayed()))
        
        // Assert: Author field label and value are displayed
        onView(withText("Authors")).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText("$FIELD_NAME_TOLKIEN, $FIELD_NAME_PRATCHETT")).perform(scrollTo()).check(matches(isDisplayed()))

        // Assert: Series field label and value are displayed
        onView(withText("Series")).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(FIELD_NAME_LOTR)).perform(scrollTo()).check(matches(isDisplayed()))
        
        // Assert: Genre field label and value are displayed
        onView(withText("Genres")).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(FIELD_NAME_FANTASY)).perform(scrollTo()).check(matches(isDisplayed()))
        
        // Assert: Language field label and value are displayed
        onView(withText("Language")).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(FIELD_NAME_EN)).perform(scrollTo()).check(matches(isDisplayed()))
        
        // Assert: Publisher field label and value are displayed
        onView(withText("Publisher")).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(FIELD_NAME_ALLEN)).perform(scrollTo()).check(matches(isDisplayed()))
        
        // Assert: Publication location field label and value are displayed
        onView(withText("Place of publication")).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(FIELD_NAME_LONDON)).perform(scrollTo()).check(matches(isDisplayed()))
        
        // Assert: Status field label and value are displayed
        onView(withText("Status")).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(FIELD_NAME_OWNED)).perform(scrollTo()).check(matches(isDisplayed()))
        
        // Assert: Rating field label and value are displayed
        onView(withText("Rating")).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withId(R.id.rating_bar)).perform(scrollTo()).check(matches(isDisplayed()))
        
        // Assert: Format field label and value are displayed
        onView(withText("Format")).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(FIELD_NAME_HARDCOVER)).perform(scrollTo()).check(matches(isDisplayed()))
        
        // Assert: Location field label and value are displayed
        onView(withText("Location")).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(FIELD_NAME_HOME)).perform(scrollTo()).check(matches(isDisplayed()))
        
        // Assert: Condition field label and value are displayed
        onView(withText("Condition")).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(FIELD_NAME_MINT)).perform(scrollTo()).check(matches(isDisplayed()))
        
        // Assert: Read field label and value are displayed
        onView(withText("Read")).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withId(R.id.check_box)).perform(scrollTo()).check(matches(isDisplayed()))
        
        // Assert: Loaned to field label and value are displayed
        onView(withText("Lent to")).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(FIELD_NAME_JOHN)).perform(scrollTo()).check(matches(isDisplayed()))

        // Assert: Price field label and formatted money value are displayed
        onView(withText(R.string.fld_price)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(formatPrice(PRICE_5000_2))).perform(scrollTo()).check(matches(isDisplayed()))

        // Assert: Value field label and formatted money value are displayed
        onView(withText(R.string.fld_value)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(formatPrice(VALUE_6000_3))).perform(scrollTo()).check(matches(isDisplayed()))

        // Assert: Due date field label and formatted date value are displayed
        onView(withText(R.string.fld_due_date)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(formatDate(DUE_DATE_20240101))).perform(scrollTo()).check(matches(isDisplayed()))

        // Assert: Read date field label and formatted date value are displayed
        onView(withText(R.string.fld_read_date)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(formatDate(READ_DATE_20231225))).perform(scrollTo()).check(matches(isDisplayed()))
    }

    @DisplayName("On View Created - Empty Or Zero Values - Fields Not Displayed")
    @Test
    @Config(application = HiltTestApplication::class, sdk = [28], qualifiers = "w480dp-h3000dp")
    fun onViewCreated_emptyOrZeroValues_fieldsNotDisplayed() = runTest {
        val book = BookEntity(
            id = ID_204,
            title = TITLE_EMPTY,
            description = DESC_EMPTY,
            volume = 0,
            publicationDate = 0,
            pages = 0,
            price = PRICE_EMPTY,
            value = PRICE_EMPTY,
            dueDate = DATE_ZERO,
            readDate = DATE_ZERO,
            edition = 0,
            isbn = "",
            web = ""
        )
        bookDao.insertBook(book)

        val mockNavController = mock(NavController::class.java)
        val args = Bundle().apply { putLong(ARG_BOOK_ID, ID_204) }
        launchFragmentInHiltContainer<BookFragment>(
            fragmentArgs = args,
            themeResId = style.Theme_AppCompat_Light_NoActionBar
        ) {
            Navigation.setViewNavController(requireView(), mockNavController)
        }
        Shadows.shadowOf(getMainLooper()).idle()

        // Assert: Wait for the form to finish loading by checking the toolbar title
        onView(androidx.test.espresso.matcher.ViewMatchers.isRoot())
            .perform(waitFor(object : org.hamcrest.TypeSafeMatcher<android.view.View>() {
                override fun describeTo(description: org.hamcrest.Description) {
                    description.appendText("toolbar title matches $TITLE_EMPTY")
                }
                override fun matchesSafely(view: android.view.View): Boolean {
                    val toolbar = view.findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)
                    return toolbar?.title == TITLE_EMPTY
                }
            }, TIMEOUT_5000))

        // Assert: Title is set on the CollapsingToolbarLayout
        onView(withId(R.id.toolbar_layout)).check { view, _ ->
            assertEquals(TITLE_EMPTY, (view as CollapsingToolbarLayout).title)
        }

        // Assert: No field labels are displayed because values are empty/zero
        onView(withText(R.string.fld_description)).check(doesNotExist())
        onView(withText(R.string.fld_volume)).check(doesNotExist())
        onView(withText(R.string.fld_publication_date)).check(doesNotExist())
        onView(withText(R.string.fld_pages)).check(doesNotExist())
        onView(withText(R.string.fld_edition)).check(doesNotExist())
        onView(withText(R.string.fld_isbn)).check(doesNotExist())
        onView(withText(R.string.fld_web)).check(doesNotExist())
        onView(withText(R.string.fld_price)).check(doesNotExist())
        onView(withText(R.string.fld_value)).check(doesNotExist())
        onView(withText(R.string.fld_due_date)).check(doesNotExist())
        onView(withText(R.string.fld_read_date)).check(doesNotExist())
    }

    @DisplayName("On View Created - Multiple Books - Fragment Handles Data Loading Properly")
    @Test
    @Config(application = HiltTestApplication::class, sdk = [28], qualifiers = "w480dp-h3000dp")
    fun onViewCreated_multipleBooks_fragmentHandlesDataLoadingProperly() = runTest {
        // Arrange: Insert multiple books
        val book1 = BookEntity(
            id = ID_201,
            title = TITLE_FIRST,
            description = DESC_FIRST,
            volume = VOL_1,
            publicationDate = PUB_DATE_2022,
            pages = PAGES_100,
            price = PRICE_1000_1,
            value = VALUE_1500_1,
            dueDate = DUE_DATE_20220101,
            readDate = READ_DATE_20211225,
            edition = EDITION_1,
            isbn = ISBN_1,
            web = WEB_FIRST
        )
        val book2 = BookEntity(
            id = ID_202,
            title = TITLE_SECOND,
            description = DESC_SECOND,
            volume = VOL_2,
            publicationDate = PUB_DATE_2023,
            pages = PAGES_200,
            price = PRICE_2000_1,
            value = VALUE_3000_2,
            dueDate = DUE_DATE_20230101,
            readDate = READ_DATE_20221225,
            edition = EDITION_2,
            isbn = ISBN_2,
            web = WEB_SECOND
        )
        bookDao.insertBook(book1)
        bookDao.insertBook(book2)

        // Act: Launch fragment requesting first book
        val mockNavController = mock(NavController::class.java)
        val args = Bundle().apply {
            putLong(ARG_BOOK_ID, ID_201)
        }
        launchFragmentInHiltContainer<BookFragment>(
            fragmentArgs = args,
            themeResId = style.Theme_AppCompat_Light_NoActionBar
        ) {
            Navigation.setViewNavController(requireView(), mockNavController)
        }
        Shadows.shadowOf(getMainLooper()).idle()

        // Assert: Wait for the form to finish rendering all rows
        onView(androidx.test.espresso.matcher.ViewMatchers.isRoot())
            .perform(waitFor(withText(R.string.fld_publication_date), TIMEOUT_5000))

        // Assert: Book1's title is set on the CollapsingToolbarLayout
        onView(withId(R.id.toolbar_layout)).check { view, _ ->
            assertEquals(TITLE_FIRST, (view as CollapsingToolbarLayout).title)
        }

        // Assert: Book1's own field values are displayed
        onView(withText(R.string.fld_description)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(DESC_FIRST)).perform(scrollTo()).check(matches(isDisplayed()))

        onView(withText(R.string.fld_volume)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(VOL_1.toString())).perform(scrollTo()).check(matches(isDisplayed()))

        onView(withText(R.string.fld_publication_date)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(PUB_DATE_2022.toString())).perform(scrollTo()).check(matches(isDisplayed()))

        onView(withText(R.string.fld_pages)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(PAGES_100.toString())).perform(scrollTo()).check(matches(isDisplayed()))

        onView(withText(R.string.fld_edition)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(EDITION_1.toString())).perform(scrollTo()).check(matches(isDisplayed()))

        onView(withText(R.string.fld_isbn)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(ISBN_1)).perform(scrollTo()).check(matches(isDisplayed()))

        onView(withText(R.string.fld_web)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(WEB_FIRST)).perform(scrollTo()).check(matches(isDisplayed()))

        onView(withText(R.string.fld_price)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(formatPrice(PRICE_1000_1))).perform(scrollTo()).check(matches(isDisplayed()))

        onView(withText(R.string.fld_value)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(formatPrice(VALUE_1500_1))).perform(scrollTo()).check(matches(isDisplayed()))

        onView(withText(R.string.fld_due_date)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(formatDate(DUE_DATE_20220101))).perform(scrollTo()).check(matches(isDisplayed()))

        onView(withText(R.string.fld_read_date)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText(formatDate(READ_DATE_20211225))).perform(scrollTo()).check(matches(isDisplayed()))

        // Assert: Book2's distinct field values are NOT displayed (correct book was loaded)
        onView(withText(DESC_SECOND)).check(doesNotExist())
        onView(withText(VOL_2.toString())).check(doesNotExist())
        onView(withText(PUB_DATE_2023.toString())).check(doesNotExist())
        onView(withText(PAGES_200.toString())).check(doesNotExist())
        onView(withText(EDITION_2.toString())).check(doesNotExist())
        onView(withText(ISBN_2)).check(doesNotExist())
        onView(withText(WEB_SECOND)).check(doesNotExist())
        onView(withText(formatPrice(PRICE_2000_1))).check(doesNotExist())
        onView(withText(formatPrice(VALUE_3000_2))).check(doesNotExist())
        onView(withText(formatDate(DUE_DATE_20230101))).check(doesNotExist())
        onView(withText(formatDate(READ_DATE_20221225))).check(doesNotExist())
    }

    private fun formatPrice(priceString: String): String {
        return Price(priceString).toFormattedString(null, DbConstants.separator)
    }

    private fun formatDate(dateValue: Int): String = Date(dateValue).toString()

    companion object {
        private const val ARG_BOOK_ID = "bookID"

        private const val ID_201 = 201L
        private const val ID_202 = 202L
        private const val ID_203 = 203L
        private const val ID_204 = 204L

        private const val TITLE_FIRST = "First Book"
        private const val TITLE_SECOND = "Second Book"
        private const val TITLE_COMPLETE = "Complete Book Data"
        private const val TITLE_EMPTY = "Empty Book"

        private const val DESC_FIRST = "First"
        private const val DESC_SECOND = "Second"
        private const val DESC_COMPLETE = "Full description text"
        private const val DESC_EMPTY = ""

        private const val VOL_1 = 1
        private const val VOL_2 = 2
        private const val VOL_5 = 5

        private const val PUB_DATE_2022 = 2022
        private const val PUB_DATE_2023 = 2023
        private const val PUB_DATE_2024 = 2024

        private const val PAGES_100 = 100
        private const val PAGES_200 = 200
        private const val PAGES_500 = 500

        private const val PRICE_1000_1 = "1000|1"
        private const val VALUE_1500_1 = "1500|1"
        private const val PRICE_2000_1 = "2000|1"
        private const val VALUE_3000_2 = "3000|2"
        private const val PRICE_5000_2 = "5000|2"
        private const val VALUE_6000_3 = "6000|3"
        private const val PRICE_EMPTY = ""

        private const val DUE_DATE_20220101 = 20220101
        private const val DUE_DATE_20230101 = 20230101
        private const val DUE_DATE_20240101 = 20240101

        private const val READ_DATE_20211225 = 20211225
        private const val READ_DATE_20221225 = 20221225
        private const val READ_DATE_20231225 = 20231225

        private const val EDITION_1 = 11
        private const val EDITION_2 = 2
        private const val EDITION_3 = 3

        private const val ISBN_1 = "1111"
        private const val ISBN_2 = "2222"
        private const val ISBN_COMPLETE = "9999999999"

        private const val WEB_FIRST = "https://first.com"
        private const val WEB_SECOND = "https://second.com"
        private const val WEB_COMPLETE = "https://completebook.com"
        
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
        private const val FIELD_NAME_HARDCOVER = "Hardcover"
        private const val FIELD_NAME_HOME = "Home Library"
        private const val FIELD_NAME_MINT = "Mint Condition"
        private const val FIELD_NAME_TRUE = "true"
        private const val FIELD_NAME_JOHN = "John Doe"
        
        private const val DATE_ZERO = 0
        
        private const val TIMEOUT_5000 = 5000L
    }
}
