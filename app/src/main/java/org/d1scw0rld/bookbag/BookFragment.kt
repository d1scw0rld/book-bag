package org.d1scw0rld.bookbag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.d1scw0rld.bookbag.databinding.FragmentBookBinding

class BookFragment : BaseFragment(), IBackPressListener {

    private var _binding: FragmentBookBinding? = null
    private val binding get() = _binding!!

    private var bookId: Long = 0
    private val args: BookFragmentArgs by navArgs()

    companion object {
        private const val SAVED_BOOK_ID = "saved_book_id"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookBinding.inflate(inflater, container, false)
        @Suppress("DEPRECATION")
        setHasOptionsMenu(true)

        val toolbar = binding.detailToolbar
        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.supportActionBar?.setHomeButtonEnabled(true)

        bookId = savedInstanceState?.getLong(SAVED_BOOK_ID) ?: getBookID()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fabEditBook.setOnClickListener { v ->
            navigateToEditBook(v, bookId)
        }

        if (savedInstanceState == null) {
            loadFragment(bookId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(SAVED_BOOK_ID, bookId)
    }

    @Suppress("DEPRECATION")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_details, menu)
    }

    @Suppress("DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        return when (itemId) {
            android.R.id.home -> {
                navigateToBookList(requireView())
                true
            }
            R.id.action_duplicate -> {
                navigateToEditBook(requireView(), bookId, true)
                true
            }
            R.id.action_delete -> {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    deleteBook()
                    withContext(Dispatchers.Main) {
                        navigateToBookList(requireView())
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed(): Boolean {
        navigateToBookList(requireView())
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

    private fun navigateToEditBook(view: View, bookId: Long) {
        val action = BookFragmentDirections.actionBookFragmentToEditBookFragment()
        action.bookID = bookId
        Navigation.findNavController(view).navigate(action)
    }

    private fun navigateToEditBook(view: View, bookId: Long, isCopy: Boolean) {
        val action = BookFragmentDirections.actionBookFragmentToEditBookFragment()
        action.bookID = bookId
        action.isCopy = isCopy
        Navigation.findNavController(view).navigate(action)
    }

    private fun navigateToBookList(view: View) {
        Navigation.findNavController(view).navigateUp()
    }

    private fun getBookID(): Long {
        return arguments?.let { BookFragmentArgs.fromBundle(it).bookID } ?: 0
    }

    private fun deleteBook() {
        val dbAdapter = DBAdapter(requireContext())
        dbAdapter.open()
        dbAdapter.deleteBook(bookId)
        dbAdapter.close()
    }
}
