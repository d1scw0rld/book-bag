package org.d1scw0rld.bookbag.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import androidx.core.os.ConfigurationCompat
import java.util.Locale

class FilteredArrayAdapter<T>(
    context: Context,
    viewResourceId: Int,
    private val items: ArrayList<T>
) : ArrayAdapter<T>(context, viewResourceId, items) {

    private val nameFilter: FieldFilter by lazy { FieldFilter(items) }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent) as TextView
        val item = getItem(position)
        requireNotNull(item)
        view.text = item.toString()
        return view
    }

    override fun getFilter(): Filter {
        return nameFilter
    }

    private inner class FieldFilter(objects: List<T>) : Filter() {
        private val suggestions = ArrayList<T>()

        init {
            synchronized(this) {
                suggestions.addAll(objects)
            }
        }

        override fun performFiltering(charSequence: CharSequence?): FilterResults {
            val result = FilterResults()
            if (!charSequence.isNullOrEmpty()) {
                val filterSeq = charSequence.toString().lowercase(Locale.getDefault())
                val filteredSuggestions = ArrayList<T>()

                val locales = ConfigurationCompat.getLocales(context.resources.configuration)
                val locale = if (locales.size() > 0) locales[0] ?: Locale.getDefault() else Locale.getDefault()
                for (obj in suggestions) {
                    if (obj.toString().lowercase(locale).startsWith(filterSeq)) {
                        filteredSuggestions.add(obj)
                    }
                }
                result.count = filteredSuggestions.size
                result.values = filteredSuggestions
            } else {
                synchronized(this) {
                    result.values = suggestions
                    result.count = suggestions.size
                }
            }
            return result
        }

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults?) {
            if (filterResults != null && filterResults.count > 0) {
                val filtered = filterResults.values as ArrayList<T>
                notifyDataSetChanged()
                clear()
                addAll(filtered)
            } else {
                notifyDataSetInvalidated()
            }
        }
    }
}
