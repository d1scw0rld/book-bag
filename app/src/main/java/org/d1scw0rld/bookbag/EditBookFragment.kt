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
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.d1scw0rld.bookbag.data.AppDatabase
import org.d1scw0rld.bookbag.data.DbConstants
import org.d1scw0rld.bookbag.data.entity.BookEntity
import org.d1scw0rld.bookbag.data.entity.BookFieldCrossRef
import org.d1scw0rld.bookbag.data.entity.FieldEntity
import org.d1scw0rld.bookbag.data.relation.toDto
import org.d1scw0rld.bookbag.databinding.FragmentEditBookBinding
import org.d1scw0rld.bookbag.dto.Book
import org.d1scw0rld.bookbag.dto.Field
import org.d1scw0rld.bookbag.fields.FieldEditTextUpdatableClearable
import androidx.core.view.isEmpty
import androidx.core.view.isGone
import androidx.core.view.size
import androidx.core.view.get

class EditBookFragment : Fragment(), IBackPressListener {

    private var _binding: FragmentEditBookBinding? = null
    private val binding get() = _binding!!

    private lateinit var book: Book
    private val dbDao get() = AppDatabase.getDatabase(requireContext(), viewLifecycleOwner.lifecycleScope).bookDao()
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
        @Suppress("DEPRECATION")
        setHasOptionsMenu(true)

        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        val toolbar = binding.toolbar
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

        val fieldsRoot = binding.llFields

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val bookId = getBookID()
            val isCopy = getIsCopy()

            val loadedBook = if (bookId != 0L) {
                dbDao.getBookWithFields(bookId)?.toDto() ?: Book()
            } else {
                Book()
            }

            if (isCopy) {
                loadedBook.id = 0
            }

            withContext(Dispatchers.Main) {
                if (!isAdded) return@withContext
                val ctx = context ?: return@withContext

                book = loadedBook
                fieldsFactory = FieldsFactory(ctx, loadedBook, dbDao)
                addFields(fieldsRoot)
                createAddFieldsPopupMenu(fieldsRoot)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
            if (child.isGone) {
                val menu = hiddenFieldsPopupMenu?.menu ?: continue
                menu.add(Menu.NONE,
                    menu.size, 0, (child as org.d1scw0rld.bookbag.fields.Field).getTitle())
                hiddenFieldsHashMap[menu[menu.size() - 1]] = child
            }
        }
    }

    private fun addFields(rootView: ViewGroup) {
        for (field in DbConstants.FIELDS) {
            when (field.type) {
                Field.TYPE_TEXT -> {
                    fieldsFactory.addFieldText(rootView, field)
                    if (field.id == DbConstants.FLD_TITLE) {
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
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                saveBook()
                withContext(Dispatchers.Main) {
                    if (book.id == 0L) {
                        navigateToBookList()
                    } else {
                        navigateBack()
                    }
                }
            }
        }
    }

    private suspend fun saveBook() {
        clearEmptyFields()
        val bookEntity = BookEntity(
            id = book.id,
            title = book.title.value,
            description = book.description.value,
            volume = book.volume.value,
            publicationDate = book.publicationDate.value,
            pages = book.pages.value,
            price = book.price.value,
            value = book.value.value,
            dueDate = book.dueDate.value,
            readDate = book.readDate.value,
            edition = book.edition.value,
            isbn = book.isbn.value,
            web = book.web.value
        )

        val idToUse = if (book.id != 0L) {
            dbDao.updateBook(bookEntity)
            book.id
        } else {
            dbDao.insertBook(bookEntity)
        }

        dbDao.deleteBookFields(idToUse)
        for (property in book.properties) {
            if (property.id == 0L) {
                val fieldEntity = FieldEntity(typeId = property.fieldTypeId, name = property.value)
                property.id = dbDao.insertField(fieldEntity)
            }
            dbDao.insertBookFieldCrossRef(BookFieldCrossRef(bookId = idToUse, fieldId = property.id))
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
