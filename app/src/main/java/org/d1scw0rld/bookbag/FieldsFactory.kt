package org.d1scw0rld.bookbag

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import org.d1scw0rld.bookbag.data.DbConstants
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
    private val propertiesMap: Map<Int, List<Property>>
) : BaseObservable<FieldsFactory.Listener>() {

    private var previousView: View? = null

    // Cached colors using context
    private val primaryColor by lazy { ContextCompat.getColor(context, R.color.primary) }
    private val textColor by lazy { ContextCompat.getColor(context, R.color.text) }

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
        val fieldEditTextUpdatableClearable = FieldEditTextUpdatableClearable(context).apply {
            setTitle(field.name)
            setTitleColor(primaryColor)
            setText(changeableValue.toString())
            setHint(field.name)
            setInputType(field.inputType)
        }

        if (field.id == DbConstants.FLD_TITLE) {
            previousView = fieldEditTextUpdatableClearable.findViewById(R.id.editTextX)
        }

        fieldEditTextUpdatableClearable.setUpdateListener { editText ->
            val textValue = editText.text.toString().trim()
            try {
                // Type-safe conversion without reflection for standard types
                val parsedValue: Any = when (changeableValue.value) {
                    is String -> textValue
                    is Int -> textValue.toIntOrNull() ?: 0
                    else -> {
                        // Fallback to reflection only if it's a non-standard type
                        val clazz = changeableValue.valueType
                        val constructor = clazz.getConstructor(String::class.java)
                        constructor.newInstance(textValue)
                    }
                }
                changeableValue.value = parsedValue as T
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        rootView.addView(fieldEditTextUpdatableClearable)
        if (!field.isVisible && changeableValue.isEmpty()) {
            hideField(fieldEditTextUpdatableClearable, field.name)
        }
    }

    fun addAutocompleteField(rootView: ViewGroup, field: Field) {
        val fieldAutoCompleteTextView = FieldAutoCompleteTextView(context).apply {
            setTitle(field.name)
            setTitleColor(primaryColor)
            setHint(field.name)
        }

        val propertyValues = getPropertyValues(field.id, true)

        // Fix: Use firstOrNull to avoid loop indexing and prevent duplicate property insertion bug
        val property = book.properties.firstOrNull { it.fieldTypeId == field.id }
            ?: Property(field.id).also { book.properties.add(it) }

        fieldAutoCompleteTextView.setText(property.value)
        fieldAutoCompleteTextView.tag = property

        val filteredArrayAdapter = FilteredArrayAdapter(context, R.layout.dropdown, propertyValues)
        fieldAutoCompleteTextView.setAdapter(filteredArrayAdapter)
        fieldAutoCompleteTextView.setOnItemClickListener { adapter, _, position, _ ->
            val selectedField = adapter.getItemAtPosition(position) as Property
            (fieldAutoCompleteTextView.tag as Property).updateFrom(selectedField)
        }

        fieldAutoCompleteTextView.setUpdateListener { editText ->
            val text = editText.text.toString().trim()
            val matchedProperty = propertyValues.firstOrNull { text.equals(it.value.trim(), ignoreCase = true) }
            val currentProperty = fieldAutoCompleteTextView.tag as Property

            if (matchedProperty != null) {
                currentProperty.updateFrom(matchedProperty)
            } else {
                currentProperty.id = 0
                currentProperty.value = editText.text.toString()
            }
        }

        rootView.addView(fieldAutoCompleteTextView)
        if (!field.isVisible && property.value.trim().isEmpty()) {
            hideField(fieldAutoCompleteTextView, field.name)
        }
    }

    fun addFieldSpinner(rootView: ViewGroup, field: Field) {
        val fieldSpinner = FieldSpinner(context).apply {
            setTitle(field.name)
            setTitleColor(primaryColor)
        }

        // Fix: Use firstOrNull to prevent potential duplicate properties and redundant complex loop logic
        val property = book.properties.firstOrNull { it.fieldTypeId == field.id }
            ?: Property(field.id).also { book.properties.add(it) }

        val propertyValues = getPropertyValues(field.id)

        val arrayAdapter = object : ArrayAdapter<String>(context, R.layout.spinner_item) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                view.setPadding(0, view.paddingTop, view.paddingRight, view.paddingBottom)
                if (position == 0) {
                    view.findViewById<TextView>(android.R.id.text1)?.setTextColor(textColor)
                }
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                parent.isVerticalScrollBarEnabled = false
                return if (position == 0) {
                    TextView(context).apply {
                        height = 0
                        visibility = View.GONE
                    }
                } else {
                    super.getDropDownView(position, null, parent)
                }
            }
        }

        arrayAdapter.setDropDownViewResource(R.layout.dropdown)
        arrayAdapter.add(field.name)
        for (propertyOfType in propertyValues) {
            arrayAdapter.add(propertyOfType.value)
        }

        fieldSpinner.setAdapter(arrayAdapter)

        val selectedPosition = propertyValues.indexOfFirst { it == property }.let { index ->
            if (index != -1) index + 1 else 0
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
        val fieldMultiText = FieldMultiText(context).apply {
            id = View.generateViewId()
        }

        if (previousView != null) {
            previousView?.nextFocusDownId = R.id.et_author_1
            previousView = null // Fix: Clear reference to avoid holding strong reference to view hierarchy (Memory Leak)
        }

        val (title, hint) = field.parsedTitleAndHint()
        fieldMultiText.setTitle(title)
        fieldMultiText.setTitleColor(primaryColor)
        fieldMultiText.setHint(hint)

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
                val trimmedValue = value.trim()
                val matchedProperty = propertyValues.firstOrNull { it.value.trim().equals(trimmedValue, ignoreCase = true) }
                val currentProperty = view.tag as Property

                if (matchedProperty != null) {
                    currentProperty.updateFrom(matchedProperty)
                } else {
                    currentProperty.id = 0
                    currentProperty.value = value
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
        val (title, _) = field.parsedTitleAndHint()
        fieldMultiSpinner.setTitle(title)
        fieldMultiSpinner.setTitleColor(primaryColor)
        fieldMultiSpinner.setHint(title)

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
            val matchedProperty = propertyValues.firstOrNull { it.value.equals(item.title, ignoreCase = true) }
            if (matchedProperty != null) {
                if (item.isSelected) {
                    book.properties.add(matchedProperty)
                } else {
                    book.properties.remove(matchedProperty)
                }
            } else {
                val newProperty = Property(field.id, item.title)
                propertyValues.add(newProperty)
                book.properties.add(newProperty)
            }
        }

        rootView.addView(fieldMultiSpinner)

        if (!field.isVisible && hasNotPropertiesOfType(field.id)) {
            hideField(fieldMultiSpinner, title)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun addFieldMoney(rootView: ViewGroup, field: Field) {
        val fieldMoney = FieldMoney(context).apply {
            setTitle(field.name)
            setTitleColor(primaryColor)
            setHint(field.name)
        }

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

            // Fix: Replaced keystroke-level compiled Regex with highly efficient, allocation-free string comparison checks
            val intValue = if (valueStr.isEmpty() || valueStr == "-" || valueStr == "," || valueStr == "-,") {
                0
            } else {
                val valueParts = valueStr.split(DbConstants.separator)
                val firstPart = valueParts[0].toIntOrNull() ?: 0
                val fractionalMultiplier = if (valueStr.contains("-")) -1 else 1

                (firstPart * 100) + if (valueParts.size == 2) {
                    val secondPart = valueParts[1].toIntOrNull() ?: 0
                    fractionalMultiplier * (if (valueParts[1].length == 1) 10 else 1) * secondPart
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

        fieldDate.apply {
            setTitle(field.name)
            setTitleColor(primaryColor)
            setHint(field.name)
            setDate(date)
            setUpdateListener(object : FieldDate.OnUpdateListener {
                override fun onUpdate(date: Date) {}

                override fun onUpdate(fieldDate: FieldDate) {
                    (fieldDate.tag as Changeable<Int>).value = fieldDate.getDate().toInt()
                }
            })
        }
        rootView.addView(fieldDate)

        if (!field.isVisible && (fieldDate.tag as Changeable<Int>).value == 0) {
            hideField(fieldDate, field.name)
        }
    }

    fun addFieldRating(rootView: ViewGroup, field: Field) {
        val fieldRating = FieldRating(context).apply {
            setTitle(field.name)
            setTitleColor(primaryColor)
        }

        val propertyValues = getPropertyValues(field.id)

        // Fix: Use firstOrNull to prevent duplicate property creation and insertion
        val property = book.properties.firstOrNull { it.fieldTypeId == field.id }
            ?: Property(field.id).also { book.properties.add(it) }

        val ratingValue = property.value.toFloatOrNull() ?: 0f
        fieldRating.setRating(ratingValue)
        fieldRating.tag = property

        fieldRating.setOnRatingBarChangeListener { _, rating, _ ->
            val ratingStr = rating.toString()
            val matchedProperty = propertyValues.firstOrNull { it.value.equals(ratingStr, ignoreCase = true) }
            val currentProperty = fieldRating.tag as Property

            if (matchedProperty != null) {
                currentProperty.updateFrom(matchedProperty)
            } else {
                currentProperty.id = 0
                currentProperty.value = ratingStr
            }
        }

        rootView.addView(fieldRating)

        if (!field.isVisible && property.value.trim().isEmpty()) {
            hideField(fieldRating, field.name)
        }
    }

    fun addFieldCheckBox(rootView: ViewGroup, field: Field) {
        val fieldCheckBox = FieldCheckBox(context).apply {
            setTitle(field.name)
            setTitleColor(primaryColor)
        }

        val propertyValues = getPropertyValues(field.id)

        // Fix: Use firstOrNull to prevent duplicate property creation and insertion
        val property = book.properties.firstOrNull { it.fieldTypeId == field.id }
            ?: Property(field.id).also { book.properties.add(it) }

        val checkedValue = property.value.toBoolean()
        fieldCheckBox.setChecked(checkedValue)
        fieldCheckBox.tag = property

        fieldCheckBox.setOnCheckedChangeListener { _, isChecked ->
            val checkedStr = isChecked.toString()
            val matchedProperty = propertyValues.firstOrNull { it.value.equals(checkedStr, ignoreCase = true) }
            val currentProperty = fieldCheckBox.tag as Property

            if (matchedProperty != null) {
                currentProperty.updateFrom(matchedProperty)
            } else {
                currentProperty.id = 0
                currentProperty.value = checkedStr
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
        val properties = propertiesMap[fieldId] ?: emptyList()
        val sorted = if (isOrdered) properties.sortedBy { it.value } else properties
        return ArrayList(sorted)
    }

    private fun hideField(view: View, name: String) {
        view.visibility = View.GONE
        for (listener in getListeners()) {
            listener.onFieldHide(view, name)
        }
    }

    private fun hasNotPropertiesOfType(typeId: Int): Boolean {
        return book.properties.none { it.fieldTypeId == typeId }
    }

    // Helper extension to parse title and optional hint from pipe-separated strings
    private fun Field.parsedTitleAndHint(): Pair<String, String> {
        val split = name.split("|")
        return if (split.size > 1) {
            split[1] to split[0] // title to hint
        } else {
            name to name // fallback to same title and hint
        }
    }

    interface Listener {
        fun onFieldHide(view: View, name: String)
    }
}
