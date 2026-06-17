package org.d1scw0rld.bookbag.ui

import android.content.Context
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.recyclerview.widget.RecyclerView

abstract class ExpandableRecyclerAdapter<T : ExpandableRecyclerAdapter.ListItem>(
    protected val context: Context
) : RecyclerView.Adapter<ExpandableRecyclerAdapter.ViewHolder>() {

    protected var allItems: MutableList<T> = ArrayList()
    protected var visibleItems: MutableList<T> = ArrayList()
    private var indexList: MutableList<Int> = ArrayList()
    private var expandMap = SparseIntArray()
    var mode: Int = 0

    companion object {
        const val TYPE_HEADER = 1000
        const val TYPE_ITEM = 1001
        private const val MODE_ACCORDION = 1
    }

    open class ListItem(var itemType: Int, var text: String)

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getItemCount(): Int {
        return visibleItems.size
    }

    protected fun inflate(resourceId: Int, viewGroup: ViewGroup): View {
        return LayoutInflater.from(context).inflate(resourceId, viewGroup, false)
    }

    open class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    open inner class HeaderViewHolder(view: View) : ViewHolder(view) {
        init {
            view.setOnClickListener { handleClick() }
        }

        internal fun handleClick() {
            if (toggleExpandedItems(layoutPosition, false)) {
                setExpanded(true)
                onExpansionToggled(false)
            } else {
                setExpanded(false)
                onExpansionToggled(true)
            }
        }

        open fun bind(position: Int) {
            setExpanded(isExpanded(position))
        }

        /**
         * Setter method for expanded state, used for initialization of expanded state.
         * Changes to the state are given in [onExpansionToggled]
         *
         * @param expanded true if expanded, false if not
         */
        @UiThread
        open fun setExpanded(expanded: Boolean) {}

        /**
         * Callback triggered when expansion state is changed, but not during
         * initialization.
         *
         * Useful for implementing animations on expansion.
         *
         * @param expanded true if view is expanded before expansion is toggled, false if not
         */
        @UiThread
        open fun onExpansionToggled(expanded: Boolean) {}
    }

    private fun toggleExpandedItems(position: Int, notify: Boolean): Boolean {
        return if (isExpanded(position)) {
            collapseItems(position, notify)
            false
        } else {
            expandItems(position, notify)
            if (mode == MODE_ACCORDION) {
                collapseAllExcept(position)
            }
            true
        }
    }

    private fun expandItems(position: Int, notify: Boolean) {
        var count = 0
        val index = indexList[position]
        var insert = position

        var i = index + 1
        while (i < allItems.size && allItems[i].itemType != TYPE_HEADER) {
            insert++
            count++
            visibleItems.add(insert, allItems[i])
            indexList.add(insert, i)
            i++
        }

        notifyItemRangeInserted(position + 1, count)

        val allItemsPosition = indexList[position]
        expandMap.put(allItemsPosition, 1)

        if (notify) {
            notifyItemChanged(position)
        }
    }

    private fun collapseItems(position: Int, notify: Boolean) {
        var count = 0
        val index = indexList[position]

        var i = index + 1
        while (i < allItems.size && allItems[i].itemType != TYPE_HEADER) {
            count++
            visibleItems.removeAt(position + 1)
            indexList.removeAt(position + 1)
            i++
        }

        notifyItemRangeRemoved(position + 1, count)

        val allItemsPosition = indexList[position]
        expandMap.delete(allItemsPosition)

        if (notify) {
            notifyItemChanged(position)
        }
    }

    private fun isExpanded(position: Int): Boolean {
        val allItemsPosition = indexList[position]
        return expandMap.get(allItemsPosition, -1) >= 0
    }

    override fun getItemViewType(position: Int): Int {
        return visibleItems[position].itemType
    }

    open fun setItems(items: List<T>) {
        allItems = items.toMutableList()
        val visibleItemsList = ArrayList<T>()
        expandMap.clear()
        indexList.clear()

        for (i in items.indices) {
            if (items[i].itemType == TYPE_HEADER) {
                indexList.add(i)
                visibleItemsList.add(items[i])
            }
        }

        this.visibleItems = visibleItemsList
        notifyDataSetChanged()
    }

    protected open fun removeItemAt(visiblePosition: Int) {
        val allItemsPosition = indexList[visiblePosition]

        allItems.removeAt(allItemsPosition)
        visibleItems.removeAt(visiblePosition)

        incrementIndexList(allItemsPosition, visiblePosition, -1)
        incrementExpandMapAfter(allItemsPosition, -1)

        notifyItemRemoved(visiblePosition)
    }

    private fun incrementExpandMapAfter(position: Int, direction: Int) {
        val newExpandMap = SparseIntArray()

        for (i in 0 until expandMap.size()) {
            val index = expandMap.keyAt(i)
            newExpandMap.put(if (index < position) index else index + direction, 1)
        }

        expandMap = newExpandMap
    }

    private fun incrementIndexList(
        allItemsPosition: Int,
        visiblePosition: Int,
        direction: Int
    ) {
        val newIndexList = ArrayList<Int>()

        for (i in indexList.indices) {
            if (i == visiblePosition) {
                if (direction > 0) {
                    newIndexList.add(allItemsPosition)
                } else {
                    continue
                }
            }

            val v = indexList[i]
            newIndexList.add(if (v < allItemsPosition) v else v + direction)
        }

        indexList = newIndexList
    }

    fun collapseAll() {
        collapseAllExcept(-1)
    }

    private fun collapseAllExcept(position: Int) {
        for (i in visibleItems.indices.reversed()) {
            if (i != position && getItemViewType(i) == TYPE_HEADER) {
                if (isExpanded(i)) {
                    collapseItems(i, true)
                }
            }
        }
    }

    fun expandAll() {
        if (visibleItems.size == allItems.size) return

        for (i in visibleItems.indices.reversed()) {
            if (getItemViewType(i) == TYPE_HEADER) {
                if (!isExpanded(i)) {
                    expandItems(i, true)
                }
            }
        }
    }
}