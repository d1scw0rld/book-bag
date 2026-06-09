package org.d1scw0rld.bookbag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.CollapsingToolbarLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.d1scw0rld.bookbag.data.AppDatabase
import org.d1scw0rld.bookbag.data.relation.toDto
import org.d1scw0rld.bookbag.databinding.FragmentBookDetailBinding
import org.d1scw0rld.bookbag.dto.Book

class BookDetailFragment : Fragment() {

    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!

    private var book: Book? = null
    private val dbDao get() = AppDatabase.getDatabase(requireContext(), viewLifecycleOwner.lifecycleScope).bookDao()
    private var bookDetailFieldsFactory: BookDetailFieldsFactory? = null

    companion object {
        const val BOOK_ID = "book_id"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val bookIdArg = arguments?.getLong(BOOK_ID) ?: 0L
            if (bookIdArg != 0L) {
                val loadedBookWithFields = dbDao.getBookWithFields(bookIdArg)
                val loadedBook = loadedBookWithFields?.toDto()
                withContext(Dispatchers.Main) {
                    if (!isAdded) return@withContext
                    val ctx = context ?: return@withContext

                    book = loadedBook
                    val appBarLayout = activity?.findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)
                    loadedBook?.let { b ->
                        appBarLayout?.title = b.title.value
                    }
                    bookDetailFieldsFactory = BookDetailFieldsFactory(ctx, dbDao, loadedBook)
                    bookDetailFieldsFactory?.addFields(binding.llCategories)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
