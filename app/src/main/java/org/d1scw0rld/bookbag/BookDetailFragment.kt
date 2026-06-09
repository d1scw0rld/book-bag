package org.d1scw0rld.bookbag

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.appbar.CollapsingToolbarLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.d1scw0rld.bookbag.data.relation.toDto
import org.d1scw0rld.bookbag.databinding.FragmentBookDetailBinding
import org.d1scw0rld.bookbag.ui.state.UiState
import org.d1scw0rld.bookbag.ui.viewmodel.BookDetailViewModel

@AndroidEntryPoint
class BookDetailFragment : BaseFragment() {

    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookDetailViewModel by viewModels()

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

        val bookIdArg = arguments?.getLong(BOOK_ID) ?: 0L
        viewModel.loadBook(bookIdArg)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            showProgressBar()
                        }
                        is UiState.Success -> {
                            hideProgressBar()
                            val detailData = state.data
                            val loadedBook = detailData.bookWithFields?.toDto()
                            val ctx = context ?: return@collect

                            // Safe search in parent fragment's view hierarchy
                            val appBarLayout = parentFragment?.view?.findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)
                            loadedBook?.let { b ->
                                appBarLayout?.title = b.title.value
                            }
                            
                            // Clear categories container first to prevent duplicates on redraw
                            binding.llCategories.removeAllViews()
                            
                            // Construct fields factory locally (safe from memory leaks) using preloaded currencies
                            val fieldsFactory = BookDetailFieldsFactory(ctx, detailData.currencies, loadedBook)
                            fieldsFactory.addFields(binding.llCategories)
                        }
                        is UiState.Error -> {
                            hideProgressBar()
                            Log.e("BookDetailFragment", "Error loading book detail", state.exception)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
