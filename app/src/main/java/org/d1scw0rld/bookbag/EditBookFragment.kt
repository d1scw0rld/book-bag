package org.d1scw0rld.bookbag

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isEmpty
import androidx.core.view.isGone
import androidx.core.view.size
import androidx.core.view.get
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.d1scw0rld.bookbag.data.DbConstants
import org.d1scw0rld.bookbag.databinding.FragmentEditBookBinding
import org.d1scw0rld.bookbag.dto.Field
import org.d1scw0rld.bookbag.fields.FieldEditTextUpdatableClearable
import org.d1scw0rld.bookbag.ui.state.UiState
import org.d1scw0rld.bookbag.ui.viewmodel.EditBookViewModel

@AndroidEntryPoint
class EditBookFragment : BaseFragment(), IBackPressListener {

    private var _binding: FragmentEditBookBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditBookViewModel by viewModels()
    private val book get() = viewModel.book

    private var hiddenFieldsPopupMenu: PopupMenu? = null
    private var bookTitleField: FieldEditTextUpdatableClearable? = null
    private val hiddenFieldsHashMap = HashMap<MenuItem, View>()
    private lateinit var fieldsFactory: FieldsFactory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditBookBinding.inflate(inflater, container, false)

        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        val toolbar = binding.toolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        if (actionBar != null) {
            showHomeTitle()
            actionBar.displayOptions = androidx.appcompat.app.ActionBar.DISPLAY_SHOW_CUSTOM
            actionBar.setCustomView(R.layout.actionbar_custom_view_done)

            (actionBar.customView.parent as Toolbar).setContentInsetsAbsolute(0, 0)
            actionBar.customView.findViewById<View>(R.id.actionbar_done).setOnClickListener { v ->
                onBookSave(v)
            }
        }

        val addFieldButton = binding.btnAddField
        addFieldButton.setOnClickListener {
            hiddenFieldsPopupMenu?.show()
        }

        hiddenFieldsPopupMenu = PopupMenu(requireContext(), addFieldButton).apply {
            setOnMenuItemClickListener { menuItem ->
                val fieldView = hiddenFieldsHashMap[menuItem]
                if (fieldView != null) {
                    fieldView.visibility = View.VISIBLE
                    fieldView.requestFocus()
                }
                menu.removeItem(menuItem.itemId)
                if (menu.isEmpty()) {
                    addFieldButton.isEnabled = false
                }
                false
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup Modern MenuProvider
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.cancel, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.cancel -> {
                        hideKeyboard()
                        navigateBack()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        val bookId = getBookID()
        val isCopy = getIsCopy()
        viewModel.loadBook(bookId, isCopy)

        observeUiState()
        observeSaveSuccess()
    }

    private fun observeUiState() {
        val fieldsRoot = binding.llFields
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            showProgressBar()
                        }
                        is UiState.Success -> {
                            hideProgressBar()
                            val ctx = context ?: return@collect
                            val loadedBook = state.data.book

                            // 1. Only initialize fields once to prevent losing user edits
                            if (fieldsRoot.childCount == 0) {
                                // 2. Properties map is pre-fetched asynchronously in the ViewModel
                                fieldsFactory = FieldsFactory(ctx, loadedBook, state.data.propertiesMap)

                                addFields(fieldsRoot)
                                createAddFieldsPopupMenu(fieldsRoot)
                            }
                        }
                        is UiState.Error -> {
                            hideProgressBar()
                            Log.e("EditBookFragment", "Error loading book edit state", state.exception)
                        }
                    }
                }
            }
        }
    }

    private fun observeSaveSuccess() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveSuccess.collect { success ->
                    hideProgressBar()
                    if (success) {
                        if (book.id == 0L) {
                            navigateToBookList()
                        } else {
                            navigateBack()
                        }
                    } else {
                        showToast(R.string.err_db)
                    }
                }
            }
        }
    }

    override fun onBackPressed(): Boolean {
        navigateBack()
        return true
    }

    private fun getIsCopy(): Boolean = EditBookFragmentArgs.fromBundle(requireArguments()).isCopy

    private fun getBookID(): Long = EditBookFragmentArgs.fromBundle(requireArguments()).bookID

    private fun createAddFieldsPopupMenu(fieldsRoot: LinearLayout) {
        for (i in 0 until fieldsRoot.childCount) {
            val child = fieldsRoot.getChildAt(i)
            if (child.isGone) {
                // Safe cast to prevent potential ClassCastException crash
                val fieldChild = child as? org.d1scw0rld.bookbag.fields.Field ?: continue
                val menu = hiddenFieldsPopupMenu?.menu ?: continue
                menu.add(Menu.NONE, menu.size, 0, fieldChild.getTitle())
                hiddenFieldsHashMap[menu[menu.size - 1]] = child
            }
        }
    }

    private fun addFields(rootView: ViewGroup) {
        for (field in DbConstants.FIELDS) {
            when (field.type) {
                Field.TYPE_TEXT -> {
                    fieldsFactory.addFieldText(rootView, field)
                    if (field.id == DbConstants.FLD_TITLE) {
                        bookTitleField = rootView.getChildAt(rootView.childCount - 1) as? FieldEditTextUpdatableClearable
                    }
                }
                Field.TYPE_MULTIFIELD -> fieldsFactory.addFieldMultiText(rootView, field)
                Field.TYPE_TEXT_AUTOCOMPLETE -> fieldsFactory.addAutocompleteField(rootView, field)
                Field.TYPE_SPINNER -> fieldsFactory.addFieldSpinner(rootView, field)
                Field.TYPE_MULTI_SPINNER -> fieldsFactory.addFieldMultiSpinner(rootView, field)
                Field.TYPE_MONEY -> fieldsFactory.addFieldMoney(rootView, field)
                Field.TYPE_DATE -> fieldsFactory.addFieldDate(rootView, field)
                Field.TYPE_RATING -> fieldsFactory.addFieldRating(rootView, field)
                Field.TYPE_CHECK_BOX -> fieldsFactory.addFieldCheckBox(rootView, field)
            }
        }
    }

    private fun onBookSave(view: View) {
        val currentFocus = requireActivity().currentFocus
        currentFocus?.clearFocus()
        view.requestFocus()

        hideKeyboard()

        if (book.title.value.trim().isEmpty()) {
            bookTitleField?.setError(getString(R.string.err_emp_ttl))
        } else {
            bookTitleField?.setError(null)
            showProgressBar()
            viewModel.saveBook()
        }
    }

    private fun navigateToBookList() {
        NavHostFragment.findNavController(this).navigate(R.id.action_to_book_list)
    }

    private fun navigateBack() {
        NavHostFragment.findNavController(this).navigateUp()
    }

    private fun hideKeyboard() {
        val window = requireActivity().window
        WindowCompat.getInsetsController(window, binding.root).hide(WindowInsetsCompat.Type.ime())
    }

    private fun showHomeTitle() {
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false)
            actionBar.setDisplayShowHomeEnabled(false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Prevent view memory leaks
        _binding = null
        hiddenFieldsPopupMenu = null
        bookTitleField = null
    }
}