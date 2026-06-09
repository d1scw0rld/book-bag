package org.d1scw0rld.bookbag.dto

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import org.d1scw0rld.bookbag.R

class BooksAdapter(
    context: Context,
    parentsResults: List<ParentResult> = emptyList(),
) : ExpandableRecyclerAdapter<BooksAdapter.BookListItem>(context) {

    private var allChildrenCount = 0
    private var filterText = ""
    private val listItemsNotFiltered: ArrayList<BookListItem>

    private var onBookClickListener: OnClickListener? = null
    private var onBookLongClickListener: OnLongClickListener? = null
    private var onHeaderClickListener: OnClickListener? = null

    companion object {
        private const val INITIAL_POSITION = 0f
        private const val ROTATED_POSITION = 180f
    }

    init {
        setItems(generateItems(parentsResults))
        listItemsNotFiltered = ArrayList(allItems)
    }

    override fun getItemId(i: Int): Long {
        return visibleItems[i].id
    }

    private fun generateItems(parentsResults: List<ParentResult>): List<BookListItem> {
        allChildrenCount = 0
        val items = ArrayList<BookListItem>()
        for (parentResult in parentsResults) {
            items.add(BookListItem(parentResult.name))
            allChildrenCount += parentResult.childList.size
            for (result in parentResult.childList) {
                items.add(BookListItem(result.id, result.content))
            }
        }
        return items
    }

    fun setHeaderClickListener(onHeaderClickListener: OnClickListener?) {
        this.onHeaderClickListener = onHeaderClickListener
    }

    class BookListItem(
        val id: Long = -1,
        itemType: Int,
        text: String,
    ) : ListItem(itemType, text) {
        constructor(group: String) : this(-1, TYPE_HEADER, group)
        constructor(id: Long, item: String) : this(id, TYPE_ITEM, item)
    }

    inner class HeaderViewHolder(val view: View) : ExpandableRecyclerAdapter<BookListItem>.HeaderViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tv_header)
        private val arrow: ImageView = view.findViewById(R.id.iv_arrow)

        init {
            // Bind listener once during creation to avoid object allocations in onBindViewHolder
            view.setOnClickListener { v ->
                onHeaderClickListener?.onClick(v)
                handleClick()
            }
        }

        override fun bind(position: Int) {
            super.bind(position)
            name.text = visibleItems[position].text
        }

        override fun setExpanded(expanded: Boolean) {
            super.setExpanded(expanded)
            arrow.rotation = if (expanded) ROTATED_POSITION else INITIAL_POSITION
        }

        override fun onExpansionToggled(expanded: Boolean) {
            // Use modern ViewPropertyAnimator (more performant, handles actual view property and runs on RenderThread)
            val targetRotation = if (expanded) INITIAL_POSITION else ROTATED_POSITION
            arrow.animate()
                .rotation(targetRotation)
                .setDuration(200)
                .start()
        }
    }

    inner class ItemViewHolder(val view: View) : ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tv_item)

        init {
            // Bind listeners once during creation to avoid object allocations in onBindViewHolder
            view.setOnClickListener { v ->
                onBookClickListener?.onClick(v)
            }
            view.setOnLongClickListener { v ->
                onBookLongClickListener?.onLongClick(v) ?: false
            }
        }

        fun bind(position: Int) {
            val textValue = visibleItems[position].text
            name.text = SpannableString(textValue).apply {
                // Use case-insensitive indexOf to avoid string allocations from lowercase()
                val filteredStart = textValue.indexOf(filterText, ignoreCase = true)
                if (filteredStart >= 0 && filterText.isNotEmpty()) {
                    setSpan(
                        ForegroundColorSpan(ContextCompat.getColor(context, R.color.accent)),
                        filteredStart,
                        filteredStart + filterText.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(inflate(R.layout.item_header, parent))
            TYPE_ITEM -> ItemViewHolder(inflate(R.layout.item_book, parent))
            else -> ItemViewHolder(inflate(R.layout.item_book, parent))
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_HEADER -> {
                (holder as HeaderViewHolder).bind(position)
            }
            TYPE_ITEM -> {
                (holder as ItemViewHolder).bind(position)
            }
        }
    }

    fun filter(charText: String) {
        filterText = charText
        visibleItems.clear()

        if (charText.isEmpty()) {
            setItems(listItemsNotFiltered)
            allChildrenCount = listItemsNotFiltered.count { it.itemType == TYPE_ITEM }
        } else {
            // Filter using ignoreCase to avoid extra string allocations
            val tempBookListItems = ArrayList(listItemsNotFiltered.filter {
                it.itemType == TYPE_HEADER || it.text.contains(charText, ignoreCase = true)
            })

            for (i in tempBookListItems.indices.reversed()) {
                if (tempBookListItems[i].itemType == TYPE_HEADER &&
                    (i == tempBookListItems.size - 1 || tempBookListItems[i + 1].itemType == TYPE_HEADER)
                ) {
                    tempBookListItems.removeAt(i)
                }
            }

            allChildrenCount = tempBookListItems.count { it.itemType == TYPE_ITEM }
            setItems(tempBookListItems)
        }

        expandAll()
    }

    fun getAllChildrenCount(): Int {
        return allChildrenCount
    }

    fun setBookClickListener(onClickListener: OnClickListener?) {
        this.onBookClickListener = onClickListener
    }

    fun setBookLongClickListener(onLongClickListener: OnLongClickListener?) {
        this.onBookLongClickListener = onLongClickListener
    }

    fun updateData(parentsResults: List<ParentResult>) {
        val items = generateItems(parentsResults)
        setItems(items)
        listItemsNotFiltered.clear()
        listItemsNotFiltered.addAll(allItems)
    }

    fun removeAt(clickedItemIndex: Int) {
        removeItemAt(clickedItemIndex)
        allChildrenCount--
    }

    override fun removeItemAt(visiblePosition: Int) {
        if (visiblePosition < 0 || visiblePosition >= visibleItems.size) return

        listItemsNotFiltered.remove(visibleItems[visiblePosition])
        super.removeItemAt(visiblePosition)

        // Ensure visiblePosition - 1 is within bounds before checking itemType
        if (visiblePosition - 1 >= 0 && visiblePosition - 1 < visibleItems.size) {
            val prevItem = visibleItems[visiblePosition - 1]
            if (prevItem.itemType == TYPE_HEADER) {
                // If there are no items after the header, or the next item is also a header, remove the header
                val isEmptyHeader = visiblePosition >= visibleItems.size || visibleItems[visiblePosition].itemType == TYPE_HEADER
                if (isEmptyHeader) {
                    super.removeItemAt(visiblePosition - 1)
                }
            }
        }
    }
}