package org.d1scw0rld.bookbag.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.d1scw0rld.bookbag.R
import org.d1scw0rld.bookbag.data.DbConstants
import org.d1scw0rld.bookbag.data.relation.BookRelationsMapper
import org.d1scw0rld.bookbag.databinding.FragmentBookListBinding
import org.d1scw0rld.bookbag.ui.adapters.BooksAdapter
import org.d1scw0rld.bookbag.ui.fileselector.FileOperation
import org.d1scw0rld.bookbag.ui.fileselector.FileSelectorDialog
import org.d1scw0rld.bookbag.ui.fileselector.OnHandleFileListener
import org.d1scw0rld.bookbag.ui.state.UiState
import org.d1scw0rld.bookbag.viewmodel.BookListViewModel
import org.d1scw0rld.bookbag.viewmodel.FileOperationType
import org.d1scw0rld.bookbag.viewmodel.PendingAction
import org.d1scw0rld.bookbag.viewmodel.PermissionEvent
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class BookListFragment : BaseFragment() {

    @Inject
    lateinit var activityResultRegistry: ActivityResultRegistry

    companion object {
        private const val TAG = "BookListFragment"
    }

    private var _binding: FragmentBookListBinding? = null
    private val binding get() = _binding!!

    private val fileFilter = arrayOf("*.*", ".db")

    private var isTwoPane = false
    private var clickedItemIndex = -1
    private var bookId: Long = 0

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal val viewModel: BookListViewModel by viewModels()
    private lateinit var booksAdapter: BooksAdapter
    private var selectedBookView: View? = null
    private lateinit var recyclerView: RecyclerView
    
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal var actionMode: ActionMode? = null
    private var fileSelectorDialog: FileSelectorDialog? = null

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var manageStorageLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
            activityResultRegistry
        ) { isGranted ->
            if (!isGranted) showToast(R.string.msg_acc_dnd)
            viewModel.onPermissionResult(isGranted)
        }

        manageStorageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            activityResultRegistry
        ) { _ ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) showToast(R.string.msg_acc_dnd)
                viewModel.onManageStorageResult()
            }
        }
    }

    private fun executePendingAction() {
        val action = viewModel.pendingAction.value
        if (action == PendingAction.IMPORT) {
            showImportDbDialog()
        } else if (action == PendingAction.EXPORT) {
            showExportDbDialog()
        }
        viewModel.resetPendingAction()
    }

    private val onCategoryClickListener = View.OnClickListener { deselectBookView() }

    private val onBookClickListener = View.OnClickListener { v ->
        getSelectedBook(v)

        actionMode?.finish()

        if (isTwoPane) {
            selectBookView(v)
            showBookDetails()
        } else {
            navigateToBookDetails(v)
        }
    }

    private val onBookLongClickListener = View.OnLongClickListener { v ->
        getSelectedBook(v)

        if (actionMode != null) return@OnLongClickListener false

        actionMode = requireActivity().startActionMode(onActionModeCallback)

        if (isTwoPane) {
            selectBookView(v)
            showBookDetails()
        }
        true
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal val onActionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.menu_context, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.action_edit -> {
                    navigateToEditBook(requireView(), bookId, false)
                    mode.finish()
                    true
                }
                R.id.action_duplicate -> {
                    navigateToEditBook(requireView(), bookId, true)
                    mode.finish()
                    true
                }
                R.id.action_delete -> {
                    deleteBook()
                    mode.finish()
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
        }
    }

    private fun deleteBook() {
        viewModel.deleteBook(bookId)
        deselectBookAndHideDetails()
    }

    private val onLoadFileListener = OnHandleFileListener { filePath ->
        viewModel.importDatabase(filePath)
    }

    private val onSaveFileListener = OnHandleFileListener { filePath ->
        viewModel.exportDatabase(filePath)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookListBinding.inflate(inflater, container, false)

        val actionBar = requireActivity().actionBar
        actionBar?.hide()

        val toolbar = binding.toolbar
        toolbar.title = requireActivity().title
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        DbConstants.initFields(resources)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabAddBook.setOnClickListener { navigateToEditBook(it) }

        recyclerView = binding.include.bookList.apply {
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(context)
        }

        booksAdapter = BooksAdapter(requireContext()).apply {
            setBookClickListener(onBookClickListener)
            setBookLongClickListener(onBookLongClickListener)
            setHeaderClickListener(onCategoryClickListener)
        }
        recyclerView.adapter = booksAdapter

        if (view.findViewById<View>(R.id.book_detail_container) != null) {
            isTwoPane = true
        }

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_book_list, menu)

                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem.actionView as SearchView

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        booksAdapter.expandAll()
                        booksAdapter.filter(newText ?: "")
                        binding.include.tvBooksCount.text = resources.getQuantityString(
                            R.plurals.books,
                            booksAdapter.getAllChildrenCount(),
                            booksAdapter.getAllChildrenCount()
                        )
                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                val navController = requireActivity().findNavController(R.id.fragment)
                return NavigationUI.onNavDestinationSelected(menuItem, navController) || optionsItemSelect(menuItem)
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        observeUiState()
        observeFileOperationState()
        observePermissionEvents()
    }

    private fun showRationaleDialog(onConfirm: Runnable) {
        AlertDialog.Builder(requireContext(), R.style.AppCompatAlertDialogStyle)
            .setTitle(R.string.permission_required_title)
            .setMessage(R.string.permission_required_message)
            .setPositiveButton(R.string.permission_required_button) { _, _ -> onConfirm.run() }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun observePermissionEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.permissionEvent.collect { event ->
                    when (event) {
                        is PermissionEvent.ShowRationale -> {
                            showRationaleDialog { viewModel.onPermissionRationaleConfirmed() }
                        }
                        is PermissionEvent.RequestLegacyPermission -> {
                            requestPermissionLauncher.launch(event.permission)
                        }
                        is PermissionEvent.RequestManageStorage -> {
                            manageStorageLauncher.launch(event.intent)
                        }
                        is PermissionEvent.PermissionGranted -> {
                            executePendingAction()
                        }
                    }
                }
            }
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            showProgressBar()
                        }
                        is UiState.Success -> {
                            hideProgressBar()
                            val books = state.data
                            val mappedParents = BookRelationsMapper.mapBooksToParents(books, viewModel.orderId.value)
                            booksAdapter.updateData(mappedParents)
                            if (viewModel.isExpandAll.value) {
                                booksAdapter.expandAll()
                            }
                            
                            val currentOrder = viewModel.orderItems.find { it.id == viewModel.orderId.value }
                            if (currentOrder != null) {
                                binding.include.tvBooksOrder.text = currentOrder.title
                                binding.include.tvBooksCount.text = resources.getQuantityString(
                                    R.plurals.books,
                                    booksAdapter.getAllChildrenCount(),
                                    booksAdapter.getAllChildrenCount()
                                )
                            }
                        }
                        is UiState.Error -> {
                            hideProgressBar()
                            Log.e(TAG, getString(R.string.log_err_loading_books), state.exception)
                        }
                    }
                }
            }
        }
    }

    private fun observeFileOperationState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fileOpState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            showProgressBar()
                        }
                        is UiState.Success -> {
                            hideProgressBar()
                            when (state.data) {
                                FileOperationType.IMPORT -> {
                                    showToast(R.string.prf_imp_db_scs)
                                }
                                FileOperationType.EXPORT -> {
                                    showToast(R.string.prf_xpr_db_scs)
                                }
                            }
                            viewModel.consumeFileOperation()
                        }
                        is UiState.Error -> {
                            hideProgressBar()
                            Log.e(TAG, getString(R.string.log_err_file_op), state.exception)
                            viewModel.consumeFileOperation()
                        }
                        null -> {
                            // Idle/No-op
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        selectedBookView = null
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        if (::recyclerView.isInitialized) {
            setupRecyclerView()
        }
    }

    private fun optionsItemSelect(item: MenuItem): Boolean {
        deselectBookAndHideDetails()

        return when (item.itemId) {
            R.id.action_imp_db -> {
                viewModel.onActionClicked(PendingAction.IMPORT)
                true
            }
            R.id.action_exp_db -> {
                viewModel.onActionClicked(PendingAction.EXPORT)
                true
            }
            R.id.action_exp_all -> {
                booksAdapter.expandAll()
                true
            }
            R.id.action_clp_all -> {
                booksAdapter.collapseAll()
                true
            }
            R.id.action_sort -> {
                val menuItemView = requireActivity().findViewById<View>(item.itemId)
                showOrderPopupMenu(menuItemView)
                true
            }
            else -> false
        }
    }

    private fun getSelectedBook(v: View) {
        clickedItemIndex = recyclerView.getChildLayoutPosition(v)
        bookId = booksAdapter.getItemId(clickedItemIndex)
        v.isSelected = true
    }

    private fun navigateToBookDetails(v: View) {
        val action = BookListFragmentDirections.actionBookListFragmentToBookFragment(bookId)
        v.findNavController().navigate(action)
    }

    private fun navigateToEditBook(v: View) {
        val action = BookListFragmentDirections.actionBookListFragmentToEditBookFragment()
        v.findNavController().navigate(action)
    }

    private fun navigateToEditBook(v: View, bookId: Long, isCopy: Boolean) {
        val action = BookListFragmentDirections.actionBookListFragmentToEditBookFragment()
        action.bookID = bookId
        action.isCopy = isCopy
        v.findNavController().navigate(action)
    }

    private fun selectBookView(view: View) {
        if (selectedBookView != null && selectedBookView != view) {
            selectedBookView?.isSelected = false
        }
        selectedBookView = view
    }

    private fun deselectBookView() {
        selectedBookView?.isSelected = false
    }

    private fun deselectBookAndHideDetails() {
        deselectBookView()
        hideBookDetails()
    }

    private fun showBookDetails() {
        val arguments = Bundle().apply {
            putLong(BookDetailFragment.BOOK_ID, bookId)
        }
        val fragment = BookDetailFragment().apply {
            this.arguments = arguments
        }
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.book_detail_container, fragment)
            .commit()
    }

    private fun hideBookDetails() {
        val fragment = parentFragmentManager
            .findFragmentById(R.id.book_detail_container)
        if (fragment != null) {
            parentFragmentManager
                .beginTransaction()
                .remove(fragment)
                .commit()
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun showImportDbDialog() {
        val importFolder = File(viewModel.exportFolderAbsPath.value)
        fileSelectorDialog = FileSelectorDialog.newInstance(
            importFolder,
            FileOperation.LOAD,
            onLoadFileListener,
            fileFilter
        )
        fileSelectorDialog?.show(parentFragmentManager, null)
    }

    private fun showExportDbDialog() {
        val fileName = viewModel.getExportFileName()
        val exportFile = File(viewModel.exportFolderAbsPath.value + fileName)
        fileSelectorDialog = FileSelectorDialog.newInstance(
            exportFile,
            FileOperation.SAVE,
            onSaveFileListener,
            fileFilter
        )
        fileSelectorDialog?.show(parentFragmentManager, null)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun showOrderPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        for (orderItem in viewModel.orderItems) {
            popupMenu.menu
                .add(1, orderItem.id, 0, orderItem.title)
                .apply {
                    isCheckable = true
                    isChecked = (orderItem.id == viewModel.orderId.value)
                }
        }
        popupMenu.menu.setGroupCheckable(1, true, true)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            viewModel.updateOrderId(menuItem.itemId)
            true
        }
        popupMenu.show()
    }

    private fun setupRecyclerView() {
        viewModel.loadBooks()
    }
}
