package org.d1scw0rld.bookbag.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import org.d1scw0rld.bookbag.R
import org.d1scw0rld.bookbag.dto.BookResult
import org.d1scw0rld.bookbag.dto.ParentResult
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.d1scw0rld.bookbag.DisplayNameRobolectricRunner
import org.robolectric.annotation.Config

@RunWith(DisplayNameRobolectricRunner::class)
@Config(sdk = [28])
class BooksAdapterTest {

    private lateinit var context: Context
    private lateinit var parentsResults: List<ParentResult>

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        parentsResults = listOf(
            ParentResult(
                name = AUTHOR_MARTIN,
                childList = listOf(
                    BookResult(id = 101L, content = BOOK_CLEAN_CODE),
                    BookResult(id = 102L, content = BOOK_CLEAN_ARCH)
                )
            ),
            ParentResult(
                name = AUTHOR_FOWLER,
                childList = listOf(
                    BookResult(id = 201L, content = BOOK_REFACTORING)
                )
            )
        )
    }

    @DisplayName("Initial State - Only Headers Provided - Only Headers Visible")
    @Test
    fun initialState_onlyHeadersProvided_onlyHeadersVisible() {
        val adapter = BooksAdapter(context, parentsResults)

        // Initially, only headers are added to the visible items
        assertEquals(2, adapter.itemCount)
        assertEquals(3, adapter.getAllChildrenCount())

        // Validate view types and item IDs for the headers
        assertEquals(ExpandableRecyclerAdapter.TYPE_HEADER, adapter.getItemViewType(0))
        assertEquals(ExpandableRecyclerAdapter.TYPE_HEADER, adapter.getItemViewType(1))
        assertEquals(-1L, adapter.getItemId(0))
        assertEquals(-1L, adapter.getItemId(1))
    }

    @DisplayName("Expand and Collapse All - Headers Expanded and Collapsed - Updates Visible Item Counts")
    @Test
    fun expandAndCollapseAll_headersExpandedAndCollapsed_updatesVisibleItemCounts() {
        val adapter = BooksAdapter(context, parentsResults)

        // Expand all should make children visible
        adapter.expandAll()
        assertEquals(5, adapter.itemCount)

        // Check view types at index positions
        assertEquals(ExpandableRecyclerAdapter.TYPE_HEADER, adapter.getItemViewType(0)) // Robert C. Martin
        assertEquals(ExpandableRecyclerAdapter.TYPE_ITEM, adapter.getItemViewType(1))   // Clean Code
        assertEquals(ExpandableRecyclerAdapter.TYPE_ITEM, adapter.getItemViewType(2))   // Clean Architecture
        assertEquals(ExpandableRecyclerAdapter.TYPE_HEADER, adapter.getItemViewType(3)) // Martin Fowler
        assertEquals(ExpandableRecyclerAdapter.TYPE_ITEM, adapter.getItemViewType(4))   // Refactoring

        // Check exact item IDs
        assertEquals(101L, adapter.getItemId(1))
        assertEquals(102L, adapter.getItemId(2))
        assertEquals(201L, adapter.getItemId(4))

        // Collapse all should restore back to only headers visible
        adapter.collapseAll()
        assertEquals(2, adapter.itemCount)
        assertEquals(ExpandableRecyclerAdapter.TYPE_HEADER, adapter.getItemViewType(0))
        assertEquals(ExpandableRecyclerAdapter.TYPE_HEADER, adapter.getItemViewType(1))
    }

    @DisplayName("Filter - Matches Substring - Hides Empty Header and Shows Matching Items")
    @Test
    fun filter_matchesSubString_hidesEmptyHeaderAndShowsMatchingItems() {
        val adapter = BooksAdapter(context, parentsResults)

        // Filter by "Code" (matches "Clean Code" under "Robert C. Martin")
        adapter.filter(QUERY_CODE)

        // "Martin Fowler" group has no matching items and should be completely hidden
        // Robert C. Martin header + Clean Code child should be visible (total 2 visible items)
        assertEquals(2, adapter.itemCount)
        assertEquals(1, adapter.getAllChildrenCount())

        assertEquals(ExpandableRecyclerAdapter.TYPE_HEADER, adapter.getItemViewType(0))
        assertEquals(ExpandableRecyclerAdapter.TYPE_ITEM, adapter.getItemViewType(1))
        assertEquals(101L, adapter.getItemId(1))
    }

    @DisplayName("Filter - Empty Query Text - Shows All Original Items")
    @Test
    fun filter_emptyQueryText_showsAllOriginalItems() {
        val adapter = BooksAdapter(context, parentsResults)

        adapter.filter(QUERY_CODE)
        assertEquals(2, adapter.itemCount)

        // Filtering with empty string resets list to all items fully expanded
        adapter.filter(QUERY_EMPTY)
        assertEquals(5, adapter.itemCount)
        assertEquals(3, adapter.getAllChildrenCount())
    }

    @DisplayName("Remove At - Last Child Removed - Decrements Counts and Cleans Empty Header")
    @Test
    fun removeAt_lastChildRemoved_decrementsCountsAndCleansEmptyHeader() {
        val adapter = BooksAdapter(context, parentsResults)
        adapter.expandAll()
        assertEquals(5, adapter.itemCount)

        // Remove item at index 4 ("Refactoring" under "Martin Fowler")
        adapter.removeAt(4)

        // Remaining children count decreases from 3 to 2
        assertEquals(2, adapter.getAllChildrenCount())

        // Since "Refactoring" was the only child under "Martin Fowler",
        // the "Martin Fowler" empty header should be automatically cleaned and removed.
        // Therefore, only 3 items remain: Header "Robert C. Martin", Child "Clean Code", Child "Clean Architecture"
        assertEquals(3, adapter.itemCount)
        assertEquals(ExpandableRecyclerAdapter.TYPE_HEADER, adapter.getItemViewType(0))
        assertEquals(ExpandableRecyclerAdapter.TYPE_ITEM, adapter.getItemViewType(1))
        assertEquals(ExpandableRecyclerAdapter.TYPE_ITEM, adapter.getItemViewType(2))
    }

    @DisplayName("Update Data - New Data Set Provided - Replaces All Items and Resets State")
    @Test
    fun updateData_newDataSetProvided_replacesAllItemsAndResetsState() {
        val adapter = BooksAdapter(context, parentsResults)
        assertEquals(2, adapter.itemCount)

        val newParents = listOf(
            ParentResult(
                name = AUTHOR_PRAGMATIC,
                childList = listOf(
                    BookResult(id = 301L, content = BOOK_PRAGMATIC)
                )
            )
        )

        adapter.updateData(newParents)
        assertEquals(1, adapter.itemCount)
        assertEquals(1, adapter.getAllChildrenCount())

        adapter.expandAll()
        assertEquals(2, adapter.itemCount)
        assertEquals(301L, adapter.getItemId(1))
    }

    @DisplayName("ViewHolder - Instantiated and Bound - Binds Text and Properties Correctly")
    @Test
    fun viewHolder_instantiatedAndBound_bindsTextAndPropertiesCorrectly() {
        val adapter = BooksAdapter(context, parentsResults)
        adapter.expandAll()

        val parentView = FrameLayout(context)
        val headerView = LayoutInflater.from(context).inflate(R.layout.item_header, parentView, false)
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_book, parentView, false)

        val headerHolder = adapter.HeaderViewHolder(headerView)
        val itemHolder = adapter.ItemViewHolder(itemView)

        // Bind header at position 0
        headerHolder.bind(0)
        assertEquals(AUTHOR_MARTIN, headerHolder.name.text.toString())

        // Bind item at position 1
        itemHolder.bind(1)
        assertEquals(BOOK_CLEAN_CODE, itemHolder.name.text.toString())
    }

    @DisplayName("OnCreate and Bind ViewHolder - Delegated by Adapter - Delegates ViewHolder Inflation and Binding")
    @Test
    fun onCreateAndBindViewHolder_delegatedByAdapter_delegatesViewHolderInflationAndBinding() {
        val adapter = BooksAdapter(context, parentsResults)
        adapter.expandAll()

        val parentView = FrameLayout(context)
        
        val headerHolder = adapter.onCreateViewHolder(parentView, ExpandableRecyclerAdapter.TYPE_HEADER)
        val itemHolder = adapter.onCreateViewHolder(parentView, ExpandableRecyclerAdapter.TYPE_ITEM)

        assertTrue(headerHolder is BooksAdapter.HeaderViewHolder)
        assertTrue(itemHolder is BooksAdapter.ItemViewHolder)

        adapter.onBindViewHolder(headerHolder, 0)
        assertEquals(AUTHOR_MARTIN, (headerHolder as BooksAdapter.HeaderViewHolder).name.text.toString())

        adapter.onBindViewHolder(itemHolder, 1)
        assertEquals(BOOK_CLEAN_CODE, (itemHolder as BooksAdapter.ItemViewHolder).name.text.toString())
    }

    @DisplayName("Perform Click - Click Listeners Invoked - Notifies Registered Callbacks")
    @Test
    fun performClick_clickListenersInvoked_notifiesRegisteredCallbacks() {
        val adapter = BooksAdapter(context, parentsResults)
        adapter.expandAll()

        var headerClicked = false
        var bookClicked = false
        var bookLongClicked = false

        adapter.setHeaderClickListener { headerClicked = true }
        adapter.setBookClickListener { bookClicked = true }
        adapter.setBookLongClickListener {
            bookLongClicked = true
            true
        }

        val parentView = FrameLayout(context)
        val headerHolder = adapter.onCreateViewHolder(parentView, ExpandableRecyclerAdapter.TYPE_HEADER) as BooksAdapter.HeaderViewHolder
        val itemHolder = adapter.onCreateViewHolder(parentView, ExpandableRecyclerAdapter.TYPE_ITEM) as BooksAdapter.ItemViewHolder

        // Use reflection to set layoutPosition so handleClick() toggles expansion without throwing IndexOutOfBoundsException
        val positionField = RecyclerView.ViewHolder::class.java.getDeclaredField("mPosition")
        positionField.isAccessible = true
        positionField.set(headerHolder, 0)
        positionField.set(itemHolder, 1)

        // Act & Assert clicks
        headerHolder.view.performClick()
        assertTrue(headerClicked)

        itemHolder.view.performClick()
        assertTrue(bookClicked)

        itemHolder.view.performLongClick()
        assertTrue(bookLongClicked)
    }

    @DisplayName("OnCreateViewHolder - Invalid View Type Provided - Falls Back to ItemViewHolder")
    @Test
    fun onCreateViewHolder_invalidViewTypeProvided_fallsBackToItemViewHolder() {
        val adapter = BooksAdapter(context, parentsResults)
        val parentView = FrameLayout(context)
        
        // Pass an invalid view type (999) to trigger the else branch in onCreateViewHolder
        val holder = adapter.onCreateViewHolder(parentView, 999)
        assertTrue(holder is BooksAdapter.ItemViewHolder)
    }

    @DisplayName("Bind - Spannable Query Filter Matched - Highlights Matching Substring with ForegroundColorSpan")
    @Test
    fun bind_spannableQueryFilterMatched_highlightsMatchingSubStringWithForegroundColorSpan() {
        val adapter = BooksAdapter(context, parentsResults)
        adapter.filter(QUERY_CODE)

        val parentView = FrameLayout(context)
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_book, parentView, false) as android.view.ViewGroup
        val tvItem = itemView.findViewById<android.widget.TextView>(R.id.tv_item)
        val index = itemView.indexOfChild(tvItem)
        itemView.removeView(tvItem)

        // Capture the text assigned to the textview exactly as passed to find the ForegroundColorSpan
        var capturedText: CharSequence? = null
        val customTextView = object : AppCompatTextView(context) {
            override fun setText(text: CharSequence?, type: BufferType?) {
                capturedText = text
                super.setText(text, type)
            }
        }.apply {
            id = R.id.tv_item
        }
        itemView.addView(customTextView, index)

        val itemHolder = adapter.ItemViewHolder(itemView)

        // Bind item at index 1 ("Clean Code")
        itemHolder.bind(1)

        val text = capturedText
        assertTrue(text is android.text.Spannable)
        val spannable = text as android.text.Spannable
        val spans = spannable.getSpans(0, spannable.length, android.text.style.ForegroundColorSpan::class.java)
        
        // Assert that the ForegroundColorSpan is added
        assertEquals(1, spans.size)
    }

    companion object {
        private const val AUTHOR_MARTIN = "Robert C. Martin"
        private const val AUTHOR_FOWLER = "Martin Fowler"
        private const val AUTHOR_PRAGMATIC = "Pragmatic Programmers"

        private const val BOOK_CLEAN_CODE = "Clean Code"
        private const val BOOK_CLEAN_ARCH = "Clean Architecture"
        private const val BOOK_REFACTORING = "Refactoring"
        private const val BOOK_PRAGMATIC = "Pragmatic Programmer"

        private const val QUERY_CODE = "Code"
        private const val QUERY_EMPTY = ""
    }
}
