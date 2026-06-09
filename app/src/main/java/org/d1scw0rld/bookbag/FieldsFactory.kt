package org.d1scw0rld.bookbag

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.d1scw0rld.bookbag.data.DbConstants
import org.d1scw0rld.bookbag.data.dao.BookDao
import org.d1scw0rld.bookbag.dto.Book
import org.d1scw0rld.bookbag.dto.Changeable
import org.d1scw0rld.bookbag.dto.Date
import org.d1scw0rld.bookbag.dto.Field
import org.d1scw0rld.bookbag.dto.Price
import org.d1scw0rld.bookbag.dto.Property
import org.d1scw0rld.bookbag.fields.FieldAutoCompleteTextView
import org.d1scw0rld.bookbag.fields.FieldCheckBox
import org.d1scw0rld.bookbag.fields.FieldDate
import org.d1scw0rld.bookbag.fields.FieldEditTextUpdatableClearable
import org.d1scw0rld.bookbag.fields.FieldMoney
import org.d1scw0rld.bookbag.fields.FieldMultiSpinner
import org.d1scw0rld.bookbag.fields.FieldMultiText
import org.d1scw0rld.bookbag.fields.FieldRating
import org.d1scw0rld.bookbag.fields.FieldSpinner

class FieldsFactory(
    private val context: Context,
    private val book: Book,
    private val dbDao: BookDao
) : BaseObservable<FieldsFactory.Listener>() {

    private var previousView: View? = null

    fun addFieldText(rootView: ViewGroup, field: Field) {
        when (field.id) {
            DbConstants.FLD_TITLE -> addFieldText(rootView, field, book.title)
            DbConstants.FLD_DESCRIPTION -> addFieldText(rootView, field, book.description)
            DbConstants.FLD_VOLUME -> addFieldText(rootView, field, book.volume)
            DbConstants.FLD_PAGES -> addFieldText(rootView, field, book.pages)
            DbConstants.FLD_EDITION -> addFieldText(rootView, field, book.edition)
            DbConstants.FLD_ISBN -> addFieldText(rootView, field, book.isbn)
            DbConstants.FLD_WEB -> addFieldText(rootView, field, book.web)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> addFieldText(rootView: ViewGroup, field: Field, changeableValue: Changeable<T>) {
        val fieldEditTextUpdatableClearable = FieldEditTextUpdatableClearable(context)

        fieldEditTextUpdatableClearable.setTitle(field.name)
        fieldEditTextUpdatableClearable.setTitleColor(ResourcesCompat.getColor(context.resources, R.color.primary, null))
        fieldEditTextUpdatableClearable.setText(changeableValue.toString())
        fieldEditTextUpdatableClearable.setHint(field.name)
        fieldEditTextUpdatableClearable.setInputType(field.inputType)

        if (field.id == DbConstants.FLD_TITLE) {
            previousView = fieldEditTextUpdatableClearable.findViewById(R.id.editTextX)
        }

        fieldEditTextUpdatableClearable.setUpdateListener { editText ->
            @Suppress("DEPRECATION")
            val clazz = changeableValue.getGenericType()
            try {
                val constructor = clazz.getConstructor(String::class.java)
                val obj = constructor.newInstance(editText.text.toString().trim())
                changeableValue.value = obj as T
            } catch (e: Exception) {
                e.printStackTrace()
                return@setUpdateListener
            }
        }

        rootView.addView(fieldEditTextUpdatableClearable)
        if (!field.isVisible && changeableValue.isEmpty()) {
            hideField(fieldEditTextUpdatableClearable, field.name)
        }
    }

    fun addAutocompleteField(rootView: ViewGroup, field: Field) {
        val fieldAutoCompleteTextView = FieldAutoCompleteTextView(context)
        fieldAutoCompleteTextView.setTitle(field.name)
        fieldAutoCompleteTextView.setTitleColor(ResourcesCompat.getColor(context.resources, R.color.primary, null))
        fieldAutoCompleteTextView.setHint(field.name)

        val focusDummyView = View(context)
        focusDummyView.nextFocusDownId = fieldAutoCompleteTextView.id

        val propertyValues = getPropertyValues(field.id, true)
        var property = Property(field.id)

        for (i in book.properties.indices) {
            if (property.id == 0L && field.id == book.properties[i].fieldTypeId) {
                property = book.properties[i]
            }
        }

        if (property.id == 0L) {
            book.properties.add(property)
        } else {
            fieldAutoCompleteTextView.setText(property.value)
        }

        fieldAutoCompleteTextView.tag = property

        val filteredArrayAdapter = FilteredArrayAdapter(context, R.layout.dropdown, propertyValues)
        fieldAutoCompleteTextView.setAdapter(filteredArrayAdapter)
        fieldAutoCompleteTextView.setOnItemClickListener { adapter, _, position, _ ->
            val selectedField = adapter.getItemAtPosition(position) as Property
            (fieldAutoCompleteTextView.tag as Property).updateFrom(selectedField)
        }

        fieldAutoCompleteTextView.setUpdateListener { editText ->
            var isFound = false
            val text = editText.text.toString().trim()
            for (p in propertyValues) {
                if (text.equals(p.value.trim(), ignoreCase = true)) {
                    isFound = true
                    (fieldAutoCompleteTextView.tag as Property).updateFrom(p)
                    break
                }
            }
            if (!isFound) {
                (fieldAutoCompleteTextView.tag as Property).id = 0
                (fieldAutoCompleteTextView.tag as Property).value = editText.text.toString()
            }
        }

        rootView.addView(fieldAutoCompleteTextView)
        if (!field.isVisible && property.value.trim().isEmpty()) {
            hideField(fieldAutoCompleteTextView, field.name)
        }
    }

    fun addFieldSpinner(rootView: ViewGroup, field: Field) {
        val fieldSpinner = FieldSpinner(context)

        fieldSpinner.setTitle(field.name)
        fieldSpinner.setTitleColor(ResourcesCompat.getColor(context.resources, R.color.primary, null))

        var property = Property(field.id)
        val propertyValues = getPropertyValues(field.id)

        for (i in book.properties.indices) {
            if (property.id == 0L || property.fieldTypeId != field.id) {
                if (book.properties[i].fieldTypeId == field.id) {
                    property = book.properties[i]
                }
            }
        }

        if (property.id == 0L) {
            book.properties.add(property)
        }

        val arrayAdapter = object : ArrayAdapter<String>(context, R.layout.spinner_item) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                view.setPadding(0, view.paddingTop, view.paddingRight, view.paddingBottom)
                if (position == 0) {
                    (view.findViewById<View>(android.R.id.text1) as TextView).setTextColor(
                        ResourcesCompat.getColor(context.resources, R.color.text, null)
                    )
                }
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = if (position == 0) {
                    TextView(context).apply {
                        height = 0
                        visibility = View.GONE
                    }
                } else {
                    super.getDropDownView(position, null, parent)
                }
                parent.isVerticalScrollBarEnabled = false
                return view
            }
        }

        arrayAdapter.setDropDownViewResource(R.layout.dropdown)
        arrayAdapter.add(field.name)
        for (propertyOfType in propertyValues) {
            arrayAdapter.add(propertyOfType.value)
        }

        fieldSpinner.setAdapter(arrayAdapter)
        var selectedPosition = 0
        for (i in propertyValues.indices) {
            if (propertyValues[i] == property) {
                selectedPosition = i + 1
            }
        }
        fieldSpinner.setSelection(selectedPosition)
        fieldSpinner.tag = property
        fieldSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                if (pos > 0) {
                    (fieldSpinner.tag as Property).updateFrom(propertyValues[pos - 1])
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })

        rootView.addView(fieldSpinner)
        if (!field.isVisible && property.id == 0L) {
            hideField(fieldSpinner, field.name)
        }
    }

    fun addFieldMultiText(rootView: ViewGroup, field: Field) {
        val fieldMultiText = FieldMultiText(context)
        fieldMultiText.id = View.generateViewId()
        if (previousView != null) {
            previousView?.nextFocusDownId = R.id.et_author_1
        }
        val splitNames = field.name.split("|")
        fieldMultiText.setTitle(if (splitNames.size > 1) splitNames[1] else field.name)
        fieldMultiText.setTitleColor(ResourcesCompat.getColor(context.resources, R.color.primary, null))
        fieldMultiText.setHint(splitNames[0])

        val propertyValues = getPropertyValues(field.id, true)
        val itemValues = ArrayList(propertyValues)

        val filteredArrayAdapter = FilteredArrayAdapter(context, R.layout.dropdown, itemValues)

        fieldMultiText.setOnAddRemoveListener(object : FieldMultiText.OnAddRemoveFieldListener {
            override fun onFieldRemove(view: View) {
                book.properties.remove(view.tag as Property)
            }

            override fun onAddNewField(view: View) {
                val fieldNew = Property(field.id)
                book.properties.add(fieldNew)
                view.tag = fieldNew
            }

            override fun onFieldUpdated(view: View, value: String) {
                var isExists = false
                val trimmedValue = value.trim()
                for (property in propertyValues) {
                    if (property.value.trim().equals(trimmedValue, ignoreCase = true)) {
                        (view.tag as Property).updateFrom(property)
                        isExists = true
                        break
                    }
                }
                if (!isExists) {
                    (view.tag as Property).id = 0
                    (view.tag as Property).value = value
                }
            }

            override fun onItemSelect(view: View, selection: Property) {
                (view.tag as Property).updateFrom(selection)
            }
        })

        fieldMultiText.setItems(filteredArrayAdapter, book.properties)

        rootView.addView(fieldMultiText)
        fieldMultiText.clearFocus()

        if (!field.isVisible && hasNotPropertiesOfType(field.id)) {
            hideField(fieldMultiText, field.name)
        }
    }

    fun addFieldMultiSpinner(rootView: ViewGroup, field: Field) {
        val fieldMultiSpinner = FieldMultiSpinner(context)
        val splitNames = field.name.split("|")
        fieldMultiSpinner.setTitle(if (splitNames.size > 1) splitNames[1] else field.name)
        fieldMultiSpinner.setTitleColor(ResourcesCompat.getColor(context.resources, R.color.primary, null))
        fieldMultiSpinner.setHint(if (splitNames.size > 1) splitNames[1] else field.name)

        val propertyValues = getPropertyValues(field.id)
        val spinnerItems = ArrayList<FieldMultiSpinner.Item>()
        for (property in propertyValues) {
            val item = FieldMultiSpinner.Item(property.value).apply {
                isSelected = book.properties.contains(property)
            }
            spinnerItems.add(item)
        }

        fieldMultiSpinner.setItems(spinnerItems)
        fieldMultiSpinner.setOnUpdateListener { item ->
            var isFound = false
            for (propertyValue in propertyValues) {
                if (propertyValue.value.equals(item.title, ignoreCase = true)) {
                    isFound = true
                    if (item.isSelected) {
                        book.properties.add(propertyValue)
                    } else {
                        book.properties.remove(propertyValue)
                    }
                    break
                }
            }
            if (!isFound) {
                val newProperty = Property(field.id, item.title)
                propertyValues.add(newProperty)
                book.properties.add(newProperty)
            }
        }

        rootView.addView(fieldMultiSpinner)

        if (!field.isVisible && hasNotPropertiesOfType(field.id)) {
            hideField(fieldMultiSpinner, if (splitNames.size > 1) splitNames[1] else field.name)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun addFieldMoney(rootView: ViewGroup, field: Field) {
        val fieldMoney = FieldMoney(context)
        fieldMoney.setTitle(field.name)
        fieldMoney.setTitleColor(ResourcesCompat.getColor(context.resources, R.color.primary, null))
        fieldMoney.setHint(field.name)

        when (field.id) {
            DbConstants.FLD_PRICE -> fieldMoney.tag = book.price
            DbConstants.FLD_VALUE -> fieldMoney.tag = book.value
            else -> return
        }

        val price = Price((fieldMoney.tag as Changeable<String>).value)
        if (price.value != 0) {
            fieldMoney.setValue(price.value)
        }

        val currencies = getPropertyValues(DbConstants.FLD_CURRENCY)
        var selectedPosition = 0
        val arrayAdapter = ArrayAdapter<String>(context, R.layout.spinner_item)
        for (i in currencies.indices) {
            arrayAdapter.add(currencies[i].value)
            if (price.currencyId == currencies[i].id) {
                selectedPosition = i
            }
        }

        fieldMoney.setAdapter(arrayAdapter)
        fieldMoney.setSelection(selectedPosition)

        fieldMoney.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                price.currencyId = currencies[pos].id
                (fieldMoney.tag as Changeable<String>).value = price.toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })

        fieldMoney.setUpdateListener { editText ->
            val valueStr = editText.text.toString()
            val intValue = if (valueStr.isEmpty() || valueStr.matches(Regex("-|,|-,"))) {
                0
            } else {
                val valueParts = valueStr.split(DbConstants.separator)
                val firstPart = valueParts[0].toIntOrNull() ?: 0
                (firstPart * 100) + if (valueParts.size == 2) {
                    val secondPart = valueParts[1].toIntOrNull() ?: 0
                    (if (valueStr.contains("-")) -1 else 1) * (if (valueParts[1].length == 1) 10 else 1) * secondPart
                } else 0
            }

            price.value = intValue
            (fieldMoney.tag as Changeable<String>).value = price.toString()
        }

        rootView.addView(fieldMoney)

        if (!field.isVisible && (fieldMoney.tag as Changeable<*>).isEmpty()) {
            hideField(fieldMoney, field.name)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun addFieldDate(rootView: ViewGroup, field: Field) {
        val date: Date
        val fieldDate: FieldDate

        when (field.id) {
            DbConstants.FLD_READ_DATE -> {
                date = Date(book.readDate.value)
                fieldDate = FieldDate(context)
                fieldDate.tag = book.readDate
            }
            DbConstants.FLD_DUE_DATE -> {
                date = Date(book.dueDate.value)
                fieldDate = FieldDate(context)
                fieldDate.tag = book.dueDate
            }
            else -> return
        }

        fieldDate.setTitle(field.name)
        fieldDate.setTitleColor(ResourcesCompat.getColor(context.resources, R.color.primary, null))
        fieldDate.setHint(field.name)
        fieldDate.setDate(date)

        fieldDate.setUpdateListener(object : FieldDate.OnUpdateListener {
            override fun onUpdate(date: Date) {}

            override fun onUpdate(fieldDate: FieldDate) {
                (fieldDate.tag as Changeable<Int>).value = fieldDate.getDate().toInt()
            }
        })
        rootView.addView(fieldDate)

        if (!field.isVisible && (fieldDate.tag as Changeable<Int>).value == 0) {
            hideField(fieldDate, field.name)
        }
    }

    fun addFieldRating(rootView: ViewGroup, field: Field) {
        val fieldRating = FieldRating(context)

        fieldRating.setTitle(field.name)
        fieldRating.setTitleColor(ResourcesCompat.getColor(context.resources, R.color.primary, null))

        val propertyValues = getPropertyValues(field.id)
        var property = Property(field.id)

        for (i in book.properties.indices) {
            if (property.id == 0L && field.id == book.properties[i].fieldTypeId) {
                property = book.properties[i]
            }
        }

        if (property.id == 0L) {
            book.properties.add(property)
        } else {
            val ratingValue = property.value.toFloatOrNull() ?: 0f
            fieldRating.setRating(ratingValue)
        }
        fieldRating.tag = property

        fieldRating.setOnRatingBarChangeListener { _, rating, _ ->
            val ratingStr = rating.toString()
            var isFound = false
            for (propertyOfType in propertyValues) {
                if (propertyOfType.value.equals(ratingStr, ignoreCase = true)) {
                    isFound = true
                    (fieldRating.tag as Property).updateFrom(propertyOfType)
                    break
                }
            }
            if (!isFound) {
                (fieldRating.tag as Property).id = 0
                (fieldRating.tag as Property).value = ratingStr
            }
        }

        rootView.addView(fieldRating)

        if (!field.isVisible && property.value.trim().isEmpty()) {
            hideField(fieldRating, field.name)
        }
    }

    fun addFieldCheckBox(rootView: ViewGroup, field: Field) {
        val fieldCheckBox = FieldCheckBox(context)

        fieldCheckBox.setTitle(field.name)
        fieldCheckBox.setTitleColor(ResourcesCompat.getColor(context.resources, R.color.primary, null))

        val propertyValues = getPropertyValues(field.id)
        var property = Property(field.id)

        for (i in book.properties.indices) {
            if (property.id == 0L && field.id == book.properties[i].fieldTypeId) {
                property = book.properties[i]
            }
        }

        if (property.id == 0L) {
            book.properties.add(property)
        } else {
            val checkedValue = property.value.toBoolean()
            fieldCheckBox.setChecked(checkedValue)
        }
        fieldCheckBox.tag = property

        fieldCheckBox.setOnCheckedChangeListener { _, isChecked ->
            val checkedStr = isChecked.toString()
            var isFound = false
            for (propertyValue in propertyValues) {
                if (propertyValue.value.equals(checkedStr, ignoreCase = true)) {
                    isFound = true
                    (fieldCheckBox.tag as Property).updateFrom(propertyValue)
                    break
                }
            }
            if (!isFound) {
                (fieldCheckBox.tag as Property).id = 0
                (fieldCheckBox.tag as Property).value = checkedStr
            }
        }

        rootView.addView(fieldCheckBox)

        if (!field.isVisible && property.value.trim().isEmpty()) {
            hideField(fieldCheckBox, field.name)
        }
    }

    fun getPropertyValues(fieldId: Int): ArrayList<Property> {
        return getPropertyValues(fieldId, false)
    }

    private fun getPropertyValues(fieldId: Int, isOrdered: Boolean): ArrayList<Property> {
        return runBlocking(Dispatchers.IO) {
            val fields = dbDao.getFieldsByTypeId(fieldId)
            val mapped = fields.map { Property(fieldTypeId = it.typeId, value = it.name, id = it.id) }
            val sorted = if (isOrdered) mapped.sortedBy { it.value } else mapped
            ArrayList(sorted)
        }
    }

    private fun hideField(view: View, name: String) {
        view.visibility = View.GONE
        for (listener in getListeners()) {
            listener.onFieldHide(view, name)
        }
    }

    private fun hasNotPropertiesOfType(typeId: Int): Boolean {
        var hasPropertiesOfType = false
        for (property in book.properties) {
            if (property.fieldTypeId == typeId) {
                hasPropertiesOfType = true
                break
            }
        }
        return !hasPropertiesOfType
    }

    interface Listener {
        fun onFieldHide(view: View, name: String)
    }
}
