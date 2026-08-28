package org.d1scw0rld.bookbag.ui.adapters

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.d1scw0rld.bookbag.DisplayNameRobolectricRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(DisplayNameRobolectricRunner::class)
@Config(sdk = [28])
class FilteredArrayAdapterTest {

    private lateinit var context: Context
    private lateinit var itemsList: ArrayList<String>
    private lateinit var adapter: FilteredArrayAdapter<String>

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        itemsList = arrayListOf(ITEM_APPLE, ITEM_BANANA, ITEM_CHERRY, ITEM_APRICOT)
        adapter = FilteredArrayAdapter(context, android.R.layout.simple_list_item_1, itemsList)
    }

    @DisplayName("Initial State - Adapter Created - Displays All Items")
    @Test
    fun initialState_adapterCreated_displaysAllItems() {
        assertEquals(EXPECTED_COUNT_ALL, adapter.count)
        assertEquals(ITEM_APPLE, adapter.getItem(INDEX_0))
        assertEquals(ITEM_BANANA, adapter.getItem(INDEX_1))
        assertEquals(ITEM_CHERRY, adapter.getItem(INDEX_2))
        assertEquals(ITEM_APRICOT, adapter.getItem(INDEX_3))
    }

    @DisplayName("Filter - Prefix Matches Items - Returns Only Matching Suggestions")
    @Test
    fun filter_prefixMatchesItems_returnsOnlyMatchingSuggestions() {
        // Filter for strings starting with "Ap"
        adapter.filter.filter(FILTER_QUERY_AP_UPPER)
        ShadowLooper.idleMainLooper()

        // Should match "Apple" and "Apricot"
        assertEquals(EXPECTED_COUNT_FILTERED, adapter.count)
        assertEquals(ITEM_APPLE, adapter.getItem(INDEX_0))
        assertEquals(ITEM_APRICOT, adapter.getItem(INDEX_1))
    }

    @DisplayName("Filter - Lowercase Prefix Matches Uppercase Items - Returns Only Matching Suggestions")
    @Test
    fun filter_lowercasePrefixMatchesUppercaseItems_returnsOnlyMatchingSuggestions() {
        // Filter for strings starting with lowercase "ap"
        adapter.filter.filter(FILTER_QUERY_AP_LOWER)
        ShadowLooper.idleMainLooper()

        // Should still match "Apple" and "Apricot" due to case-insensitivity
        assertEquals(EXPECTED_COUNT_FILTERED, adapter.count)
        assertEquals(ITEM_APPLE, adapter.getItem(INDEX_0))
        assertEquals(ITEM_APRICOT, adapter.getItem(INDEX_1))
    }

    @DisplayName("Filter - No Prefix Matches Items - Invalidates Dataset But Leaves Count Unchanged")
    @Test
    fun filter_noPrefixMatchesItems_invalidatesDatasetButLeavesCountUnchanged() {
        // Filter for string with no matches
        adapter.filter.filter(FILTER_QUERY_NO_MATCH)
        ShadowLooper.idleMainLooper()

        // When there are no matches, notifyDataSetInvalidated() is called.
        // Let's verify that the adapter's contents or behavior is invalidated/empty.
        // Note: ArrayAdapter's count becomes 0 or remains unchanged?
        // Let's verify what the count is (it calls notifyDataSetInvalidated without clearing or clears).
        // Since publishResults does clear and addAll ONLY when result.count > 0,
        // if count is 0, publishResults goes to the 'else' branch: notifyDataSetInvalidated().
        // Let's check that state correctly.
        assertEquals(EXPECTED_COUNT_ALL, adapter.count) // count remains 4 because publishResults did not mutate it, but dataset is invalidated
    }

    @DisplayName("Filter - Empty Query Constraint - Restores All Suggestions")
    @Test
    fun filter_emptyQueryConstraint_restoresAllSuggestions() {
        // First filter to restrict items
        adapter.filter.filter(FILTER_QUERY_AP_UPPER)
        ShadowLooper.idleMainLooper()
        assertEquals(EXPECTED_COUNT_FILTERED, adapter.count)

        // Then filter with empty string
        adapter.filter.filter(FILTER_QUERY_EMPTY)
        ShadowLooper.idleMainLooper()

        // All items should be restored
        assertEquals(EXPECTED_COUNT_ALL, adapter.count)
        assertEquals(ITEM_APPLE, adapter.getItem(INDEX_0))
        assertEquals(ITEM_BANANA, adapter.getItem(INDEX_1))
        assertEquals(ITEM_CHERRY, adapter.getItem(INDEX_2))
        assertEquals(ITEM_APRICOT, adapter.getItem(INDEX_3))
    }

    @DisplayName("Get View - Request Item View - Returns TextView with Formatted Item Text")
    @Test
    fun getView_requestItemView_returnsTextViewWithFormattedItemText() {
        val parentView = android.widget.LinearLayout(context)
        val view = adapter.getView(INDEX_1, null, parentView) as android.widget.TextView
        assertNotNull(view)
        assertEquals(ITEM_BANANA, view.text.toString())
    }

    companion object {
        const val ITEM_APPLE = "Apple"
        const val ITEM_BANANA = "Banana"
        const val ITEM_CHERRY = "Cherry"
        const val ITEM_APRICOT = "Apricot"

        const val EXPECTED_COUNT_ALL = 4
        const val EXPECTED_COUNT_FILTERED = 2

        const val INDEX_0 = 0
        const val INDEX_1 = 1
        const val INDEX_2 = 2
        const val INDEX_3 = 3

        const val FILTER_QUERY_AP_UPPER = "Ap"
        const val FILTER_QUERY_AP_LOWER = "ap"
        const val FILTER_QUERY_NO_MATCH = "Z"
        const val FILTER_QUERY_EMPTY = ""
    }
}
