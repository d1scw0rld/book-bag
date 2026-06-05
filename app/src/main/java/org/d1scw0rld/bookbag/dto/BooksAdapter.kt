package org.d1scw0rld.bookbag.dto

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import org.d1scw0rld.bookbag.R
import java.util.Locale

class BooksAdapter(
    context: Context,
    parentsResults: ArrayList<ParentResult>
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

    override fun getItemId(position: Int): Long {
        return visibleItems[position].id
    }

    private fun generateItems(parentsResults: ArrayList<ParentResult>): List<BookListItem> {
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
        var id: Long = -1,
        itemType: Int,
        text: String
    ) : ListItem(itemType, text) {
        constructor(group: String) : this(-1, TYPE_HEADER, group)
        constructor(id: Long, item: String) : this(id, TYPE_ITEM, item)
    }

    inner class HeaderViewHolder(val view: View) : ExpandableRecyclerAdapter<BookListItem>.HeaderViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tv_header)
        private val arrow: ImageView = view.findViewById(R.id.iv_arrow)

        override fun bind(position: Int) {
            super.bind(position)
            name.text = visibleItems[position].text
        }

        override fun setExpanded(expanded: Boolean) {
            super.setExpanded(expanded)
            arrow.rotation = if (expanded) ROTATED_POSITION else INITIAL_POSITION
        }

        override fun onExpansionToggled(expanded: Boolean) {
            val rotateAnimation = if (expanded) {
                RotateAnimation(
                    ROTATED_POSITION,
                    INITIAL_POSITION,
                    RotateAnimation.RELATIVE_TO_SELF,
                    0.5f,
                    RotateAnimation.RELATIVE_TO_SELF,
                    0.5f
                )
            } else {
                RotateAnimation(
                    -1 * ROTATED_POSITION,
                    INITIAL_POSITION,
                    RotateAnimation.RELATIVE_TO_SELF,
                    0.5f,
                    RotateAnimation.RELATIVE_TO_SELF,
                    0.5f
                )
            }

            rotateAnimation.duration = 200
            rotateAnimation.fillAfter = true
            arrow.startAnimation(rotateAnimation)
        }
    }

    inner class ItemViewHolder(val view: View) : ExpandableRecyclerAdapter.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tv_item)

        fun bind(position: Int) {
            val textValue = visibleItems[position].text
            val spContent = SpannableString(textValue)
            var filteredStart = textValue.lowercase(Locale.getDefault())
                .indexOf(filterText.lowercase(Locale.getDefault()))
            val filterEnd = if (filteredStart < 0) {
                filteredStart = 0
                0
            } else {
                filteredStart + filterText.length
            }
            spContent.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(context, R.color.accent)),
                filteredStart,
                filterEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            name.text = spContent
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
                val headerHolder = holder as HeaderViewHolder
                headerHolder.bind(position)
                headerHolder.view.setOnClickListener { v ->
                    onHeaderClickListener?.onClick(v)
                    headerHolder.handleClick()
                }
            }
            TYPE_ITEM -> {
                val itemHolder = holder as ItemViewHolder
                itemHolder.bind(position)
                itemHolder.view.setOnClickListener(onBookClickListener)
                itemHolder.view.setOnLongClickListener(onBookLongClickListener)
            }
        }
    }

    fun filter(charText: String) {
        val query = charText.lowercase(Locale.getDefault())
        filterText = query
        visibleItems.clear()

        if (query.isEmpty()) {
            setItems(listItemsNotFiltered)
            allChildrenCount = listItemsNotFiltered.count { it.itemType == TYPE_ITEM }
        } else {
            val tempBookListItems = ArrayList(listItemsNotFiltered.filter {
                it.itemType == TYPE_HEADER || it.text.lowercase(Locale.getDefault()).contains(query)
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

    fun removeAt(clickedItemIndex: Int) {
        removeItemAt(clickedItemIndex)
        allChildrenCount--
    }

    override fun removeItemAt(visiblePosition: Int) {
        listItemsNotFiltered.remove(visibleItems[visiblePosition])
        super.removeItemAt(visiblePosition)
        if (visibleItems[visiblePosition - 1].itemType == TYPE_HEADER &&
            (visiblePosition == visibleItems.size || visibleItems[visiblePosition].itemType == TYPE_HEADER)
        ) {
            super.removeItemAt(visiblePosition - 1)
        }
    }
}
