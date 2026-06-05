package org.d1scw0rld.bookbag

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.d1scw0rld.bookbag.dto.BooksAdapter
import org.d1scw0rld.bookbag.fileselector.FileOperation
import org.d1scw0rld.bookbag.fileselector.FileSelectorDialog
import org.d1scw0rld.bookbag.fileselector.OnHandleFileListener
import java.io.File
import java.util.Calendar
import java.util.Locale

class BookListFragment : BaseFragment() {

    companion object {
        private const val PREF_ORDER_ID = "order_id"
        private const val PREF_EXPAND_ALL = "pref_expand_all"
        private const val PREF_EXPORT_FOLDER = "pref_export_folder"
    }

    private val fileFilter = arrayOf("*.*", ".db")
    private val orderItems = ArrayList<OrderItem>()

    private enum class PendingAction {
        NONE, IMPORT, EXPORT
    }

    private var pendingAction = PendingAction.NONE
    private var isExpandAll = false
    private var isTwoPane = false
    private var orderId = DBAdapter.SRT_TTL
    private var clickedItemIndex = -1
    private var bookId: Long = 0
    private var exportFolderAbsPath: String = ""

    private lateinit var booksOrderTextView: TextView
    private lateinit var booksCountTextView: TextView
    private lateinit var dbAdapter: DBAdapter
    private lateinit var booksAdapter: BooksAdapter
    private lateinit var preferences: SharedPreferences
    private var selectedBookView: View? = null
    private lateinit var recyclerView: RecyclerView
    private var actionMode: ActionMode? = null
    private lateinit var fragmentManager: FragmentManager
    private var fileSelectorDialog: FileSelectorDialog? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                checkCreateExportFolder(exportFolderAbsPath)
                executePendingAction()
            } else {
                showToast(R.string.msg_acc_dnd)
                pendingAction = PendingAction.NONE
            }
        }

    private val manageStorageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    checkCreateExportFolder(exportFolderAbsPath)
                    executePendingAction()
                } else {
                    showToast(R.string.msg_acc_dnd)
                    pendingAction = PendingAction.NONE
                }
            }
        }

    private fun executePendingAction() {
        if (pendingAction == PendingAction.IMPORT) {
            showImportDbDialog()
        } else if (pendingAction == PendingAction.EXPORT) {
            showExportDbDialog()
        }
        pendingAction = PendingAction.NONE
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

    private val onActionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.menu_context, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            val itemId = item.itemId
            if (itemId == R.id.action_edit) {
                navigateToEditBook(requireView(), bookId, false)
            } else if (itemId == R.id.action_duplicate) {
                navigateToEditBook(requireView(), bookId, true)
            } else if (itemId == R.id.action_delete) {
                deleteBook()
            } else {
                return false
            }
            mode.finish()
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
        }
    }

    private fun deleteBook() {
        dbAdapter.deleteBook(bookId)
        booksAdapter.removeAt(clickedItemIndex)
        booksCountTextView.text = resources.getQuantityString(
            R.plurals.books,
            booksAdapter.getAllChildrenCount(),
            booksAdapter.getAllChildrenCount()
        )
        deselectBookAndHideDetails()
    }

    private val onLoadFileListener = OnHandleFileListener { filePath ->
        dbAdapter.close()
        if (dbAdapter.importDatabase(filePath)) {
            showToast(R.string.prf_imp_db_scs)
        }
        dbAdapter.open()
        setupRecyclerView(recyclerView, orderId)
    }

    private val onSaveFileListener = OnHandleFileListener { filePath ->
        dbAdapter.close()
        if (dbAdapter.exportDatabase(filePath)) {
            showToast(R.string.prf_xpr_db_scs)
        }
        dbAdapter.open()
    }

    private val onSharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
        if (key.equals(PREF_EXPAND_ALL, ignoreCase = true)) {
            isExpandAll = prefs.getBoolean(PREF_EXPAND_ALL, false)
        }
        if (key.equals(PREF_EXPORT_FOLDER, ignoreCase = true)) {
            exportFolderAbsPath = getExportFolderAbsPath(prefs.getString(PREF_EXPORT_FOLDER, getString(R.string.app_name)) ?: getString(R.string.app_name))
            checkCreateExportFolder(exportFolderAbsPath)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_book_list, container, false)
        @Suppress("DEPRECATION")
        setHasOptionsMenu(true)

        val actionBar = requireActivity().actionBar
        actionBar?.hide()

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = requireActivity().title
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        fragmentManager = requireActivity().supportFragmentManager

        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        preferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
        loadPreferences()
        checkCreateExportFolder(exportFolderAbsPath)

        dbAdapter = DBAdapter(requireContext())

        getOrderItems()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        booksOrderTextView = view.findViewById(R.id.tv_books_order)
        booksCountTextView = view.findViewById(R.id.tv_books_count)

        val fab = view.findViewById<FloatingActionButton>(R.id.fab_add_book)
        fab.setOnClickListener { navigateToEditBook(it) }

        view.findViewById<RecyclerView>(R.id.book_list)?.let { rv ->
            recyclerView = rv
            recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            recyclerView.itemAnimator = DefaultItemAnimator()
            recyclerView.layoutManager = LinearLayoutManager(context)
        }

        if (view.findViewById<View>(R.id.book_detail_container) != null) {
            isTwoPane = true
        }
    }

    override fun onResume() {
        super.onResume()
        dbAdapter.open()
        if (::recyclerView.isInitialized) {
            setupRecyclerView(recyclerView, orderId)
        }
    }

    override fun onPause() {
        dbAdapter.close()
        super.onPause()
    }

    @Suppress("DEPRECATION")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_book_list, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                booksAdapter.expandAll()
                booksAdapter.filter(newText ?: "")
                booksCountTextView.text = resources.getQuantityString(
                    R.plurals.books,
                    booksAdapter.getAllChildrenCount(),
                    booksAdapter.getAllChildrenCount()
                )
                return true
            }
        })
    }

    @Suppress("DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = Navigation.findNavController(requireActivity(), R.id.fragment)
        return NavigationUI.onNavDestinationSelected(item, navController) || optionsItemSelect(item)
    }

    private fun getExportFolderAbsPath(exportFolder: String): String {
        @Suppress("DEPRECATION")
        return Environment.getExternalStorageDirectory().toString() + File.separator + exportFolder + File.separator
    }

    private fun checkAndRequestPermissions(action: PendingAction): Boolean {
        pendingAction = action
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                showRationaleDialog {
                    var intent: Intent? = null
                    try {
                        intent = Intent(
                            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                            Uri.fromParts("package", requireContext().packageName, null)
                        )
                    } catch (e: Exception) {
                        // fallback
                    }
                    if (intent == null) {
                        intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    }
                    manageStorageLauncher.launch(intent)
                }
                return false
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                showRationaleDialog {
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
                return false
            }
        }
        pendingAction = PendingAction.NONE
        return true
    }

    private fun showRationaleDialog(onConfirm: Runnable) {
        AlertDialog.Builder(requireContext(), R.style.AppCompatAlertDialogStyle)
            .setTitle(R.string.permission_required_title)
            .setMessage(R.string.permission_required_message)
            .setPositiveButton(R.string.permission_required_button) { _, _ -> onConfirm.run() }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun checkCreateExportFolder(exportFolderAbsPath: String) {
        val file = File(exportFolderAbsPath)
        if (!file.isDirectory) {
            if (!file.mkdirs()) {
                Log.w("BookListFragment", "Can't create export folder: $exportFolderAbsPath")
            }
        }
    }

    private fun getOrderItems() {
        orderItems.add(OrderItem(DBAdapter.SRT_TTL, getString(R.string.srt_title)))
        orderItems.add(OrderItem(DBAdapter.SRT_AUT, getString(R.string.srt_author)))
        orderItems.add(OrderItem(DBAdapter.SRT_WNT_PBL_TTL, getString(R.string.srt_wanted_pbl_ttl)))
        orderItems.add(OrderItem(DBAdapter.SRT_WNT_PBL_AUT, getString(R.string.srt_wanted_pbl_aut)))
        orderItems.add(OrderItem(DBAdapter.SRT_RD_AUT, getString(R.string.srt_read_aut)))
        orderItems.add(OrderItem(DBAdapter.SRT_RD_TTL, getString(R.string.srt_read_ttl)))
        orderItems.add(OrderItem(DBAdapter.SRT_NOT_RD_AUT, getString(R.string.srt_not_read_aut)))
        orderItems.add(OrderItem(DBAdapter.SRT_NOT_RD_TTL, getString(R.string.srt_not_read_ttl)))
        orderItems.add(OrderItem(DBAdapter.SRT_PBL_AUT, getString(R.string.srt_pbl_aut)))
        orderItems.add(OrderItem(DBAdapter.SRT_PBL_TTL, getString(R.string.srt_pbl_ttl)))
        orderItems.add(OrderItem(DBAdapter.SRT_LND_TTL, getString(R.string.srt_lnd_ttl)))
        orderItems.add(OrderItem(DBAdapter.SRT_LND_BRW, getString(R.string.srt_lnd_brw)))
    }

    private fun optionsItemSelect(item: MenuItem): Boolean {
        deselectBookAndHideDetails()

        val itemId = item.itemId
        return when (itemId) {
            R.id.action_imp_db -> {
                if (checkAndRequestPermissions(PendingAction.IMPORT)) {
                    showImportDbDialog()
                }
                true
            }
            R.id.action_exp_db -> {
                if (checkAndRequestPermissions(PendingAction.EXPORT)) {
                    showExportDbDialog()
                }
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getSelectedBook(v: View) {
        clickedItemIndex = recyclerView.getChildLayoutPosition(v)
        bookId = booksAdapter.getItemId(clickedItemIndex)
        v.isSelected = true
    }

    private fun navigateToBookDetails(v: View) {
        val action = BookListFragmentDirections.actionBookListFragmentToBookFragment(bookId)
        Navigation.findNavController(v).navigate(action)
    }

    private fun navigateToEditBook(v: View) {
        val action = BookListFragmentDirections.actionBookListFragmentToEditBookFragment()
        Navigation.findNavController(v).navigate(action)
    }

    private fun navigateToEditBook(v: View, bookId: Long, isCopy: Boolean) {
        val action = BookListFragmentDirections.actionBookListFragmentToEditBookFragment()
        action.bookID = bookId
        action.isCopy = isCopy
        Navigation.findNavController(v).navigate(action)
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
        requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(R.id.book_detail_container, fragment)
            .commit()
    }

    private fun hideBookDetails() {
        val fragment = requireActivity().supportFragmentManager
            .findFragmentById(R.id.book_detail_container)
        if (fragment != null) {
            requireActivity().supportFragmentManager
                .beginTransaction()
                .remove(fragment)
                .commit()
        }
    }

    private fun showImportDbDialog() {
        val importFolder = File(exportFolderAbsPath)
        fileSelectorDialog = FileSelectorDialog.newInstance(
            importFolder,
            FileOperation.LOAD,
            onLoadFileListener,
            fileFilter
        )
        fileSelectorDialog?.show(fragmentManager, null)
    }

    private fun showExportDbDialog() {
        val fileName = getFileName()
        val exportFile = File(exportFolderAbsPath + fileName)
        fileSelectorDialog = FileSelectorDialog.newInstance(
            exportFile,
            FileOperation.SAVE,
            onSaveFileListener,
            fileFilter
        )
        fileSelectorDialog?.show(fragmentManager, null)
    }

    private fun getFileName(): String {
        val calendar = Calendar.getInstance(Locale.getDefault())
        val extIndex = DBAdapter.DATABASE_NAME.lastIndexOf(".")
        return String.format(
            getString(R.string.fmt_fl_nm),
            DBAdapter.DATABASE_NAME.substring(0, extIndex),
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            DBAdapter.DATABASE_NAME.substring(extIndex + 1)
        )
    }

    private fun showOrderPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        for (orderItem in orderItems) {
            popupMenu.menu
                .add(1, orderItem.id, 0, orderItem.title)
                .setCheckable(true)
                .setChecked(orderItem.id == orderId)
        }
        popupMenu.menu.setGroupCheckable(1, true, true)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            orderId = menuItem.itemId
            saveOrderID(orderId)
            setupRecyclerView(recyclerView, orderId)
            true
        }
        popupMenu.show()
    }

    private fun setupRecyclerView(recyclerView: RecyclerView, orderId: Int) {
        booksAdapter = BooksAdapter(requireContext(), dbAdapter.getBooks(orderId) ?: ArrayList())
        booksAdapter.setBookClickListener(onBookClickListener)
        booksAdapter.setBookLongClickListener(onBookLongClickListener)
        booksAdapter.setHeaderClickListener(onCategoryClickListener)
        if (isExpandAll) {
            booksAdapter.expandAll()
        }
        recyclerView.adapter = booksAdapter
        for (orderItem in orderItems) {
            if (orderItem.id == orderId) {
                booksOrderTextView.text = orderItem.title
                booksCountTextView.text = resources.getQuantityString(
                    R.plurals.books,
                    booksAdapter.getAllChildrenCount(),
                    booksAdapter.getAllChildrenCount()
                )
            }
        }
    }

    private fun loadPreferences() {
        orderId = preferences.getInt(PREF_ORDER_ID, DBAdapter.SRT_TTL)
        isExpandAll = preferences.getBoolean(PREF_EXPAND_ALL, false)
        exportFolderAbsPath = getExportFolderAbsPath(
            preferences.getString(PREF_EXPORT_FOLDER, getString(R.string.app_name)) ?: getString(R.string.app_name)
        )
    }

    private fun saveOrderID(orderId: Int) {
        val editor = preferences.edit()
        editor.putInt(PREF_ORDER_ID, orderId)
        editor.apply()
    }

    private class OrderItem(var id: Int, var title: String)
}
