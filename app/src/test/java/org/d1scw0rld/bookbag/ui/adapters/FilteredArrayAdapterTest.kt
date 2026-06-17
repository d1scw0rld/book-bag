package org.d1scw0rld.bookbag.ui.adapters

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class FilteredArrayAdapterTest {

    private lateinit var context: Context
    private lateinit var itemsList: ArrayList<String>
    private lateinit var adapter: FilteredArrayAdapter<String>

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        itemsList = arrayListOf("Apple", "Banana", "Cherry", "Apricot")
        adapter = FilteredArrayAdapter(context, android.R.layout.simple_list_item_1, itemsList)
    }

    @Test
    fun testInitialState() {
        assertEquals(4, adapter.count)
        assertEquals("Apple", adapter.getItem(0))
        assertEquals("Banana", adapter.getItem(1))
        assertEquals("Cherry", adapter.getItem(2))
        assertEquals("Apricot", adapter.getItem(3))
    }

    @Test
    fun testFiltering_prefixMatch() {
        // Filter for strings starting with "Ap"
        adapter.filter.filter("Ap")
        ShadowLooper.idleMainLooper()

        // Should match "Apple" and "Apricot"
        assertEquals(2, adapter.count)
        assertEquals("Apple", adapter.getItem(0))
        assertEquals("Apricot", adapter.getItem(1))
    }

    @Test
    fun testFiltering_caseInsensitivePrefixMatch() {
        // Filter for strings starting with lowercase "ap"
        adapter.filter.filter("ap")
        ShadowLooper.idleMainLooper()

        // Should still match "Apple" and "Apricot" due to case-insensitivity
        assertEquals(2, adapter.count)
        assertEquals("Apple", adapter.getItem(0))
        assertEquals("Apricot", adapter.getItem(1))
    }

    @Test
    fun testFiltering_noMatches() {
        // Filter for string with no matches
        adapter.filter.filter("Z")
        ShadowLooper.idleMainLooper()

        // When there are no matches, notifyDataSetInvalidated() is called.
        // Let's verify that the adapter's contents or behavior is invalidated/empty.
        // Note: ArrayAdapter's count becomes 0 or remains unchanged?
        // Let's verify what the count is (it calls notifyDataSetInvalidated without clearing or clears).
        // Since publishResults does clear and addAll ONLY when result.count > 0,
        // if count is 0, publishResults goes to the 'else' branch: notifyDataSetInvalidated().
        // Let's check that state correctly.
        assertEquals(4, adapter.count) // count remains 4 because publishResults did not mutate it, but dataset is invalidated
    }

    @Test
    fun testFiltering_emptyText_restoresAllSuggestions() {
        // First filter to restrict items
        adapter.filter.filter("Ap")
        ShadowLooper.idleMainLooper()
        assertEquals(2, adapter.count)

        // Then filter with empty string
        adapter.filter.filter("")
        ShadowLooper.idleMainLooper()

        // All items should be restored
        assertEquals(4, adapter.count)
        assertEquals("Apple", adapter.getItem(0))
        assertEquals("Banana", adapter.getItem(1))
        assertEquals("Cherry", adapter.getItem(2))
        assertEquals("Apricot", adapter.getItem(3))
    }

    @Test
    fun testGetView_returnsTextViewWithItemText() {
        val parentView = android.widget.LinearLayout(context)
        val view = adapter.getView(1, null, parentView) as android.widget.TextView

        assertNotNull(view)
        assertEquals("Banana", view.text.toString())
    }
}
