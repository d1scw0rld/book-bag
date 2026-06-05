package org.d1scw0rld.bookbag

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import org.d1scw0rld.bookbag.dto.Book
import org.d1scw0rld.bookbag.dto.Field
import org.d1scw0rld.bookbag.fields.FieldEditTextUpdatableClearable

class EditBookFragment : Fragment(), IBackPressListener {

    private lateinit var book: Book
    private lateinit var dbAdapter: DBAdapter
    private var hiddenFieldsPopupMenu: PopupMenu? = null
    private var bookTitleField: FieldEditTextUpdatableClearable? = null
    private val hiddenFieldsHashMap = HashMap<MenuItem, View>()
    private lateinit var fieldsFactory: FieldsFactory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_book, container, false)
        @Suppress("DEPRECATION")
        setHasOptionsMenu(true)

        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        if (actionBar != null) {
            showHomeTitle(false)
            actionBar.displayOptions = androidx.appcompat.app.ActionBar.DISPLAY_SHOW_CUSTOM
            actionBar.setCustomView(R.layout.actionbar_custom_view_done)

            (actionBar.customView.parent as Toolbar).setContentInsetsAbsolute(0, 0)
            actionBar.customView.findViewById<View>(R.id.actionbar_done).setOnClickListener { v ->
                onBookSave(v)
            }
        }

        dbAdapter = DBAdapter(requireContext())
        dbAdapter.open()

        val bookId = getBookID()
        val isCopy = getIsCopy()

        if (bookId != 0L) {
            book = dbAdapter.getBook(bookId) ?: Book()
            if (isCopy) {
                book.id = 0
            }
        } else {
            book = Book()
        }

        fieldsFactory = FieldsFactory(requireContext(), book, dbAdapter)

        val addFieldButton = view.findViewById<Button>(R.id.btn_add_field)
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
                if (menu.size() == 0) {
                    addFieldButton.isEnabled = false
                }
                false
            }
        }

        val fieldsRoot = view.findViewById<LinearLayout>(R.id.ll_fields)
        addFields(fieldsRoot)
        createAddFieldsPopupMenu(fieldsRoot)

        return view
    }

    override fun onPause() {
        dbAdapter.close()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        dbAdapter.open()
    }

    override fun onDestroy() {
        showHomeTitle(true)
        super.onDestroy()
    }

    @Suppress("DEPRECATION")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.cancel, menu)
    }

    @Suppress("DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.cancel) {
            hideKeyboard()
            navigateBack()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed(): Boolean {
        navigateBack()
        return true
    }

    private fun getIsCopy(): Boolean {
        return EditBookFragmentArgs.fromBundle(requireArguments()).isCopy
    }

    private fun getBookID(): Long {
        return EditBookFragmentArgs.fromBundle(requireArguments()).bookID
    }

    private fun createAddFieldsPopupMenu(fieldsRoot: LinearLayout) {
        for (i in 0 until fieldsRoot.childCount) {
            val child = fieldsRoot.getChildAt(i)
            if (child.visibility == View.GONE) {
                val menu = hiddenFieldsPopupMenu?.menu ?: continue
                menu.add(Menu.NONE, menu.size(), 0, (child as org.d1scw0rld.bookbag.fields.Field).getTitle())
                hiddenFieldsHashMap[menu.getItem(menu.size() - 1)] = child
            }
        }
    }

    private fun addFields(rootView: ViewGroup) {
        for (field in DBAdapter.FIELDS) {
            when (field.type) {
                Field.TYPE_TEXT -> {
                    fieldsFactory.addFieldText(rootView, field)
                    if (field.id == DBAdapter.FLD_TITLE) {
                        bookTitleField = rootView.getChildAt(rootView.childCount - 1) as FieldEditTextUpdatableClearable
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
            bookTitleField?.setError(resources.getString(R.string.err_emp_ttl))
        } else {
            bookTitleField?.setError(null)
            saveBook()
            if (book.id == 0L) {
                navigateToBookList()
            } else {
                navigateBack()
            }
        }
    }

    private fun saveBook() {
        clearEmptyFields()
        if (book.id != 0L) {
            dbAdapter.updateBook(book)
        } else {
            dbAdapter.insertBook(book)
        }
    }

    private fun clearEmptyFields() {
        for (i in book.properties.indices.reversed()) {
            if (book.properties[i].value.trim().isEmpty()) {
                book.properties.removeAt(i)
            }
        }
    }

    private fun navigateToBookList() {
        NavHostFragment.findNavController(this).navigate(R.id.action_to_book_list)
    }

    private fun navigateBack() {
        NavHostFragment.findNavController(this).navigateUp()
    }

    private fun hideKeyboard() {
        val view = requireActivity().currentFocus
        if (view != null) {
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun showHomeTitle(show: Boolean) {
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(show)
            actionBar.setDisplayShowHomeEnabled(show)
        }
    }
}
