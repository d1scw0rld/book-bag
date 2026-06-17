package org.d1scw0rld.bookbag.ui.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ExpandableRecyclerAdapterTest {

    private lateinit var context: Context

    private class TestListItem(itemType: Int, text: String) : ExpandableRecyclerAdapter.ListItem(itemType, text)

    private class TestExpandableAdapter(context: Context) : ExpandableRecyclerAdapter<TestListItem>(context) {
        var setExpandedCallback: ((Boolean) -> Unit)? = null
        var onExpansionToggledCallback: ((Boolean) -> Unit)? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(View(context))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {}
        
        public override fun removeItemAt(visiblePosition: Int) {
            super.removeItemAt(visiblePosition)
        }

        inner class CallbackHeaderViewHolder(view: View) : HeaderViewHolder(view) {
            override fun setExpanded(expanded: Boolean) {
                super.setExpanded(expanded)
                setExpandedCallback?.invoke(expanded)
            }

            override fun onExpansionToggled(expanded: Boolean) {
                super.onExpansionToggled(expanded)
                onExpansionToggledCallback?.invoke(expanded)
            }
        }
    }

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testDefaultGetItemId() {
        val adapter = TestExpandableAdapter(context)
        adapter.setItems(
            listOf(
                TestListItem(ExpandableRecyclerAdapter.TYPE_HEADER, "Header 1"),
                TestListItem(ExpandableRecyclerAdapter.TYPE_HEADER, "Header 2"),
            )
        )
        assertEquals(0L, adapter.getItemId(0))
        assertEquals(1L, adapter.getItemId(1))
    }

    @Test
    fun testAccordionMode_collapsesOtherHeadersOnExpansion() {
        val adapter = TestExpandableAdapter(context)
        adapter.mode = 1 // Set to MODE_ACCORDION

        val items = listOf(
            TestListItem(ExpandableRecyclerAdapter.TYPE_HEADER, "Header 1"),
            TestListItem(ExpandableRecyclerAdapter.TYPE_ITEM, "Item 1.1"),
            TestListItem(ExpandableRecyclerAdapter.TYPE_ITEM, "Item 1.2"),
            TestListItem(ExpandableRecyclerAdapter.TYPE_HEADER, "Header 2"),
            TestListItem(ExpandableRecyclerAdapter.TYPE_ITEM, "Item 2.1")
        )
        adapter.setItems(items)

        assertEquals(2, adapter.itemCount)

        val holder1 = adapter.HeaderViewHolder(View(context))
        val positionField = androidx.recyclerview.widget.RecyclerView.ViewHolder::class.java.getDeclaredField("mPosition")
        positionField.isAccessible = true
        positionField.set(holder1, 0)

        // Expand Header 1
        holder1.handleClick()
        assertEquals(4, adapter.itemCount)

        // Expand Header 2 (Header 2 is at visible index 3)
        val holder2 = adapter.HeaderViewHolder(View(context))
        positionField.set(holder2, 3)
        holder2.handleClick()

        // Accordion mode should collapse Header 1 automatically!
        assertEquals(3, adapter.itemCount)
        assertEquals(ExpandableRecyclerAdapter.TYPE_HEADER, adapter.getItemViewType(0)) // Header 1 (collapsed)
        assertEquals(ExpandableRecyclerAdapter.TYPE_HEADER, adapter.getItemViewType(1)) // Header 2 (expanded)
        assertEquals(ExpandableRecyclerAdapter.TYPE_ITEM, adapter.getItemViewType(2))   // Item 2.1
    }

    @Test
    fun testExpandAll_whenAlreadyExpanded_doesNothing() {
        val adapter = TestExpandableAdapter(context)
        val items = listOf(
            TestListItem(ExpandableRecyclerAdapter.TYPE_HEADER, "Header 1"),
            TestListItem(ExpandableRecyclerAdapter.TYPE_ITEM, "Item 1.1")
        )
        adapter.setItems(items)
        adapter.expandAll()
        assertEquals(2, adapter.itemCount)

        adapter.expandAll()
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun testRemoveItemAt_recalculatesInternalIndexListAndExpandMap() {
        val adapter = TestExpandableAdapter(context)
        val items = listOf(
            TestListItem(ExpandableRecyclerAdapter.TYPE_HEADER, "Header 1"),
            TestListItem(ExpandableRecyclerAdapter.TYPE_ITEM, "Item 1.1"),
            TestListItem(ExpandableRecyclerAdapter.TYPE_HEADER, "Header 2"),
            TestListItem(ExpandableRecyclerAdapter.TYPE_ITEM, "Item 2.1")
        )
        adapter.setItems(items)
        adapter.expandAll()
        assertEquals(4, adapter.itemCount)

        adapter.removeItemAt(1) // Remove Item 1.1

        assertEquals(3, adapter.itemCount)
        assertEquals(ExpandableRecyclerAdapter.TYPE_HEADER, adapter.getItemViewType(0)) // Header 1
        assertEquals(ExpandableRecyclerAdapter.TYPE_HEADER, adapter.getItemViewType(1)) // Header 2
        assertEquals(ExpandableRecyclerAdapter.TYPE_ITEM, adapter.getItemViewType(2))   // Item 2.1
    }

    @Test
    fun testHeaderViewHolder_setExpandedAndToggledCallbacks() {
        val adapter = TestExpandableAdapter(context)
        adapter.setItems(listOf(
            TestListItem(ExpandableRecyclerAdapter.TYPE_HEADER, "Header 1"),
            TestListItem(ExpandableRecyclerAdapter.TYPE_ITEM, "Item 1.1")
        ))

        var setExpandedCalled = false
        var onExpansionToggledCalled = false
        var lastExpandedValue = false
        var lastToggledValue = false

        adapter.setExpandedCallback = { expanded ->
            setExpandedCalled = true
            lastExpandedValue = expanded
        }
        adapter.onExpansionToggledCallback = { expanded ->
            onExpansionToggledCalled = true
            lastToggledValue = expanded
        }

        val customHolder = adapter.CallbackHeaderViewHolder(View(context))
        val positionField = androidx.recyclerview.widget.RecyclerView.ViewHolder::class.java.getDeclaredField("mPosition")
        positionField.isAccessible = true
        positionField.set(customHolder, 0)

        // Bind triggers setExpanded
        customHolder.bind(0)
        assertTrue(setExpandedCalled)
        assertFalse(lastExpandedValue)

        // Click to expand toggles expansion callbacks
        setExpandedCalled = false
        customHolder.handleClick()
        assertTrue(setExpandedCalled)
        assertTrue(lastExpandedValue)
        assertTrue(onExpansionToggledCalled)
        assertFalse(lastToggledValue)
    }
}
