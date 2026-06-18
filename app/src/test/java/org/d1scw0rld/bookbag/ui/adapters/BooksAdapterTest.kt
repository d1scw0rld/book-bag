package org.d1scw0rld.bookbag.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import org.d1scw0rld.bookbag.R
import org.d1scw0rld.bookbag.dto.BookResult
import org.d1scw0rld.bookbag.dto.ParentResult
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BooksAdapterTest {

    private lateinit var context: Context
    private lateinit var parentsResults: List<ParentResult>

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        parentsResults = listOf(
            ParentResult(
                name = "Robert C. Martin",
                childList = listOf(
                    BookResult(id = 101L, content = "Clean Code"),
                    BookResult(id = 102L, content = "Clean Architecture")
                )
            ),
            ParentResult(
                name = "Martin Fowler",
                childList = listOf(
                    BookResult(id = 201L, content = "Refactoring")
                )
            )
        )
    }

    @Test
    fun testInitialState_onlyHeadersVisible() {
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

    @Test
    fun testExpandAndCollapseAll() {
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

    @Test
    fun testFiltering_matchesSubString_hidesEmptyHeader() {
        val adapter = BooksAdapter(context, parentsResults)

        // Filter by "Code" (matches "Clean Code" under "Robert C. Martin")
        adapter.filter("Code")

        // "Martin Fowler" group has no matching items and should be completely hidden
        // Robert C. Martin header + Clean Code child should be visible (total 2 visible items)
        assertEquals(2, adapter.itemCount)
        assertEquals(1, adapter.getAllChildrenCount())

        assertEquals(ExpandableRecyclerAdapter.TYPE_HEADER, adapter.getItemViewType(0))
        assertEquals(ExpandableRecyclerAdapter.TYPE_ITEM, adapter.getItemViewType(1))
        assertEquals(101L, adapter.getItemId(1))
    }

    @Test
    fun testFiltering_emptyText_showsAll() {
        val adapter = BooksAdapter(context, parentsResults)

        adapter.filter("Code")
        assertEquals(2, adapter.itemCount)

        // Filtering with empty string resets list to all items fully expanded
        adapter.filter("")
        assertEquals(5, adapter.itemCount)
        assertEquals(3, adapter.getAllChildrenCount())
    }

    @Test
    fun testRemoveAt_decrementsCountsAndCleansEmptyHeaders() {
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

    @Test
    fun testUpdateData_replacesAllItems() {
        val adapter = BooksAdapter(context, parentsResults)
        assertEquals(2, adapter.itemCount)

        val newParents = listOf(
            ParentResult(
                name = "Pragmatic Programmers",
                childList = listOf(
                    BookResult(id = 301L, content = "Pragmatic Programmer")
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

    @Test
    fun testViewHolderInstantiationAndBinding() {
        val adapter = BooksAdapter(context, parentsResults)
        adapter.expandAll()

        val parentView = FrameLayout(context)
        val headerView = LayoutInflater.from(context).inflate(R.layout.item_header, parentView, false)
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_book, parentView, false)

        val headerHolder = adapter.HeaderViewHolder(headerView)
        val itemHolder = adapter.ItemViewHolder(itemView)

        // Bind header at position 0
        headerHolder.bind(0)
        assertEquals("Robert C. Martin", headerHolder.name.text.toString())

        // Bind item at position 1
        itemHolder.bind(1)
        assertEquals("Clean Code", itemHolder.name.text.toString())
    }

    @Test
    fun testAdapterOnCreateAndBindViewHolderDelegation() {
        val adapter = BooksAdapter(context, parentsResults)
        adapter.expandAll()

        val parentView = FrameLayout(context)
        
        val headerHolder = adapter.onCreateViewHolder(parentView, ExpandableRecyclerAdapter.TYPE_HEADER)
        val itemHolder = adapter.onCreateViewHolder(parentView, ExpandableRecyclerAdapter.TYPE_ITEM)

        assertTrue(headerHolder is BooksAdapter.HeaderViewHolder)
        assertTrue(itemHolder is BooksAdapter.ItemViewHolder)

        adapter.onBindViewHolder(headerHolder, 0)
        assertEquals("Robert C. Martin", (headerHolder as BooksAdapter.HeaderViewHolder).name.text.toString())

        adapter.onBindViewHolder(itemHolder, 1)
        assertEquals("Clean Code", (itemHolder as BooksAdapter.ItemViewHolder).name.text.toString())
    }

    @Test
    fun testClickListenersInvocation() {
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

    @Test
    fun testCreateViewHolder_elseBranch() {
        val adapter = BooksAdapter(context, parentsResults)
        val parentView = FrameLayout(context)
        
        // Pass an invalid view type (999) to trigger the else branch in onCreateViewHolder
        val holder = adapter.onCreateViewHolder(parentView, 999)
        assertTrue(holder is BooksAdapter.ItemViewHolder)
    }

    @Test
    fun testItemViewHolder_bindingWithSpannableFiltering() {
        val adapter = BooksAdapter(context, parentsResults)
        adapter.filter("Code")

        val parentView = FrameLayout(context)
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_book, parentView, false) as android.view.ViewGroup
        val tvItem = itemView.findViewById<TextView>(R.id.tv_item)
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
}
