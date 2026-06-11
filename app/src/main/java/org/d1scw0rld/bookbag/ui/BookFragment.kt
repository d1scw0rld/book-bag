package org.d1scw0rld.bookbag.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.d1scw0rld.bookbag.R
import org.d1scw0rld.bookbag.data.repository.BookRepository
import org.d1scw0rld.bookbag.databinding.FragmentBookBinding
import javax.inject.Inject

@AndroidEntryPoint
class BookFragment : BaseFragment(), IBackPressListener {

    @Inject
    lateinit var bookRepository: BookRepository

    private var _binding: FragmentBookBinding? = null
    private val binding get() = _binding!!

    private val args: BookFragmentArgs by navArgs()
    private var bookId: Long = 0

    companion object {
        private const val SAVED_BOOK_ID = "saved_book_id"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookBinding.inflate(inflater, container, false)

        val toolbar = binding.detailToolbar
        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.supportActionBar?.setHomeButtonEnabled(true)

        bookId = savedInstanceState?.getLong(SAVED_BOOK_ID) ?: args.bookID

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fabEditBook.setOnClickListener {
            navigateToEditBook(bookId)
        }

        if (savedInstanceState == null) {
            loadFragment(bookId)
        }

        setupMenu()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(SAVED_BOOK_ID, bookId)
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_details, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    android.R.id.home -> {
                        navigateToBookList()
                        true
                    }
                    R.id.action_duplicate -> {
                        navigateToEditBook(bookId, isCopy = true)
                        true
                    }
                    R.id.action_delete -> {
                        viewLifecycleOwner.lifecycleScope.launch {
                            bookRepository.deleteBookAndRelations(bookId)
                            navigateToBookList()
                        }
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onBackPressed(): Boolean {
        navigateToBookList()
        return true
    }

    private fun loadFragment(bookId: Long) {
        val arguments = Bundle().apply {
            putLong(BookDetailFragment.BOOK_ID, bookId)
        }
        val fragment = BookDetailFragment().apply {
            this.arguments = arguments
        }
        childFragmentManager.beginTransaction()
            .replace(R.id.book_detail_container, fragment)
            .commitAllowingStateLoss()
    }

    private fun navigateToEditBook(bookId: Long, isCopy: Boolean = false) {
        val action = BookFragmentDirections.actionBookFragmentToEditBookFragment().apply {
            this.bookID = bookId
            this.isCopy = isCopy
        }
        findNavController().navigate(action)
    }

    private fun navigateToBookList() {
        findNavController().navigateUp()
    }
}
