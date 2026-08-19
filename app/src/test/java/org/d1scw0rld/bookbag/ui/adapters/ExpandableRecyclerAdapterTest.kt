package org.d1scw0rld.bookbag.ui.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.d1scw0rld.bookbag.DisplayNameRobolectricRunner
import org.robolectric.annotation.Config

@RunWith(DisplayNameRobolectricRunner::class)
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

    @DisplayName("Get Item ID - Default Implementation - Returns Position as ID")
    @Test
    fun getItemId_defaultImplementation_returnsPositionAsId() {
        val adapter = TestExpandableAdapter(context)
        adapter.setItems(
            listOf(
                TestListItem(ExpandableRecyclerAdapter.TYPE_HEADER, HEADER_TEXT_1),
                TestListItem(ExpandableRecyclerAdapter.TYPE_HEADER, HEADER_TEXT_2),
            )
        )
        assertEquals(ID_0, adapter.getItemId(INDEX_0))
        assertEquals(ID_1, adapter.getItemId(INDEX_1))
    }

    @DisplayName("Toggle Expanded Items - Accordion Mode Enabled - Collapses Other Headers on Expansion")
    @Test
    fun toggleExpandedItems_accordionModeEnabled_collapsesOtherHeadersOnExpansion() {
        val adapter = TestExpandableAdapter(context)
        adapter.mode = MODE_ACCORDION

        val items = listOf(
            TestListItem(ExpandableRecyclerAdapter.TYPE_HEADER, HEADER_TEXT_1),
            TestListItem(ExpandableRecyclerAdapter.TYPE_ITEM, ITEM_TEXT_1_1),
            TestListItem(ExpandableRecyclerAdapter.TYPE_ITEM, ITEM_TEXT_1_2),
            TestListItem(ExpandableRecyclerAdapter.TYPE_HEADER, HEADER_TEXT_2),
            TestListItem(ExpandableRecyclerAdapter.TYPE_ITEM, ITEM_TEXT_2_1)
        )
        adapter.setItems(items)

        assertEquals(EXPECTED_ITEM_COUNT_2, adapter.itemCount)

        val holder1 = adapter.HeaderViewHolder(View(context))
        val positionField = RecyclerView.ViewHolder::class.java.getDeclaredField(FIELD_MPOSITION)
        positionField.isAccessible = true
        positionField.set(holder1, INDEX_0)

        // Expand Header 1
        holder1.handleClick()
        assertEquals(EXPECTED_ITEM_COUNT_4, adapter.itemCount)

        // Expand Header 2 (Header 2 is at visible index 3)
        val holder2 = adapter.HeaderViewHolder(View(context))
        positionField.set(holder2, INDEX_3)
        holder2.handleClick()

        // Accordion mode should collapse Header 1 automatically!
        assertEquals(EXPECTED_ITEM_COUNT_3, adapter.itemCount)
        assertEquals(ExpandableRecyclerAdapter.TYPE_HEADER, adapter.getItemViewType(INDEX_0)) // Header 1 (collapsed)
        assertEquals(ExpandableRecyclerAdapter.TYPE_HEADER, adapter.getItemViewType(INDEX_1)) // Header 2 (expanded)
        assertEquals(ExpandableRecyclerAdapter.TYPE_ITEM, adapter.getItemViewType(INDEX_2))   // Item 2.1
    }

    @DisplayName("Expand All - Already Fully Expanded - Does Nothing")
    @Test
    fun expandAll_alreadyFullyExpanded_doesNothing() {
        val adapter = TestExpandableAdapter(context)
        val items = listOf(
            TestListItem(ExpandableRecyclerAdapter.TYPE_HEADER, HEADER_TEXT_1),
            TestListItem(ExpandableRecyclerAdapter.TYPE_ITEM, ITEM_TEXT_1_1)
        )
        adapter.setItems(items)
        adapter.expandAll()
        assertEquals(EXPECTED_ITEM_COUNT_2, adapter.itemCount)

        adapter.expandAll()
        assertEquals(EXPECTED_ITEM_COUNT_2, adapter.itemCount)
    }

    @DisplayName("Remove Item At - Valid Position - Recalculates Internal Index List and Expand Map")
    @Test
    fun removeItemAt_validPosition_recalculatesInternalIndexListAndExpandMap() {
        val adapter = TestExpandableAdapter(context)
        val items = listOf(
            TestListItem(ExpandableRecyclerAdapter.TYPE_HEADER, HEADER_TEXT_1),
            TestListItem(ExpandableRecyclerAdapter.TYPE_ITEM, ITEM_TEXT_1_1),
            TestListItem(ExpandableRecyclerAdapter.TYPE_HEADER, HEADER_TEXT_2),
            TestListItem(ExpandableRecyclerAdapter.TYPE_ITEM, ITEM_TEXT_2_1)
        )
        adapter.setItems(items)
        adapter.expandAll()
        assertEquals(EXPECTED_ITEM_COUNT_4, adapter.itemCount)

        adapter.removeItemAt(INDEX_1) // Remove Item 1.1

        assertEquals(EXPECTED_ITEM_COUNT_3, adapter.itemCount)
        assertEquals(ExpandableRecyclerAdapter.TYPE_HEADER, adapter.getItemViewType(INDEX_0)) // Header 1
        assertEquals(ExpandableRecyclerAdapter.TYPE_HEADER, adapter.getItemViewType(INDEX_1)) // Header 2
        assertEquals(ExpandableRecyclerAdapter.TYPE_ITEM, adapter.getItemViewType(INDEX_2))   // Item 2.1
    }

    @DisplayName("Handle Click - Header ViewHolder Clicked - Triggers SetExpanded and OnExpansionToggled Callbacks")
    @Test
    fun handleClick_headerViewHolderClicked_triggersSetExpandedAndOnExpansionToggledCallbacks() {
        val adapter = TestExpandableAdapter(context)
        adapter.setItems(listOf(
            TestListItem(ExpandableRecyclerAdapter.TYPE_HEADER, HEADER_TEXT_1),
            TestListItem(ExpandableRecyclerAdapter.TYPE_ITEM, ITEM_TEXT_1_1)
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
        val positionField = RecyclerView.ViewHolder::class.java.getDeclaredField(FIELD_MPOSITION)
        positionField.isAccessible = true
        positionField.set(customHolder, INDEX_0)

        // Bind triggers setExpanded
        customHolder.bind(INDEX_0)
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

    companion object {
        const val HEADER_TEXT_1 = "Header 1"
        const val HEADER_TEXT_2 = "Header 2"
        const val ITEM_TEXT_1_1 = "Item 1.1"
        const val ITEM_TEXT_1_2 = "Item 1.2"
        const val ITEM_TEXT_2_1 = "Item 2.1"
        
        const val ID_0 = 0L
        const val ID_1 = 1L
        
        const val INDEX_0 = 0
        const val INDEX_1 = 1
        const val INDEX_2 = 2
        const val INDEX_3 = 3

        const val EXPECTED_ITEM_COUNT_2 = 2
        const val EXPECTED_ITEM_COUNT_3 = 3
        const val EXPECTED_ITEM_COUNT_4 = 4

        const val MODE_ACCORDION = 1
        
        const val FIELD_MPOSITION = "mPosition"
    }
}
