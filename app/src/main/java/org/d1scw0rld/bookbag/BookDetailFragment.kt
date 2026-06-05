package org.d1scw0rld.bookbag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.CollapsingToolbarLayout
import org.d1scw0rld.bookbag.dto.Book

class BookDetailFragment : Fragment() {

    private var book: Book? = null
    private val dbAdapter: DBAdapter by lazy { DBAdapter(requireContext()) }
    private var bookDetailFieldsFactory: BookDetailFieldsFactory? = null
    private lateinit var categoriesLayout: LinearLayout

    companion object {
        const val BOOK_ID = "book_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbAdapter.open()

        arguments?.let { args ->
            if (args.containsKey(BOOK_ID)) {
                book = dbAdapter.getBook(args.getLong(BOOK_ID))
                bookDetailFieldsFactory = BookDetailFieldsFactory(requireContext(), dbAdapter, book)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_book_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appBarLayout = requireActivity().findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)
        book?.let { b ->
            appBarLayout?.title = b.title.value
        }

        categoriesLayout = view.findViewById(R.id.ll_categories)
    }

    override fun onPause() {
        dbAdapter.close()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        dbAdapter.open()
        if (book != null) {
            bookDetailFieldsFactory?.addFields(categoriesLayout)
        }
    }
}
