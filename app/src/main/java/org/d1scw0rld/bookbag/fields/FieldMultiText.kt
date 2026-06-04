package org.d1scw0rld.bookbag.fields

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.LinearLayout
import org.d1scw0rld.bookbag.FilteredArrayAdapter
import org.d1scw0rld.bookbag.R
import org.d1scw0rld.bookbag.dto.Property

class FieldMultiText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr), Field {

    interface OnAddRemoveFieldListener {
        fun onFieldRemove(view: View)
        fun onAddNewField(view: View)
        fun onFieldUpdated(view: View, value: String)
        fun onItemSelect(view: View, selection: Property)
    }

    private lateinit var inflater: LayoutInflater
    private lateinit var title: Title
    private lateinit var fieldsLayout: LinearLayout
    private var hint: String = ""
    private var contentDescription: String = ""
    private var adapter: FilteredArrayAdapter<Property>? = null
    private var onAddRemoveFieldListener: OnAddRemoveFieldListener? = null

    init {
        initialize(context)

        orientation = VERTICAL
        gravity = Gravity.CENTER_VERTICAL

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.FieldMultiText, 0, 0)

            val titleText = typedArray.getString(R.styleable.FieldMultiText_title)
            val titleValueColor = typedArray.getColor(R.styleable.FieldMultiText_titleColor, 0)
            val titleTextSize = typedArray.getDimensionPixelOffset(R.styleable.FieldMultiText_titleTextSize, 0)
            val titleLineSize = typedArray.getDimensionPixelOffset(R.styleable.FieldMultiText_titleLineSize, 0)
            contentDescription = typedArray.getString(R.styleable.FieldMultiText_android_contentDescription) ?: ""
            hint = typedArray.getString(R.styleable.FieldMultiText_android_hint) ?: ""

            typedArray.recycle()

            titleText?.let { t -> this.title.setText(t) }
            this.title.setColor(titleValueColor)
            this.title.setTextSize(titleTextSize)
            this.title.setLineSize(titleLineSize)
        }
    }

    private fun initialize(context: Context) {
        inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.field_multi_text, this, true)

        title = findViewById(R.id.title)
        fieldsLayout = findViewById(R.id.ll_fields)

        findViewById<View>(R.id.ib_add_field).setOnClickListener {
            addNewField()
        }
    }

    private fun addRow(): View {
        val rowView = inflater.inflate(R.layout.row_field, null)
        rowView.findViewById<View>(R.id.ib_remove_field).setOnClickListener { view ->
            val parentView = view.parent as View
            removeField(parentView)
        }

        val valueAutoCompleteTextView = rowView.findViewById<AutoCompleteTextViewX>(R.id.et_value)
        valueAutoCompleteTextView.hint = hint
        valueAutoCompleteTextView.contentDescription = contentDescription
        valueAutoCompleteTextView.setAdapter(adapter)
        valueAutoCompleteTextView.threshold = 1

        valueAutoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, _, position, _ ->
            val selection = adapterView.getItemAtPosition(position) as Property
            valueAutoCompleteTextView.setText(selection.value)
            valueAutoCompleteTextView.setSelection(selection.value.length)
            onAddRemoveFieldListener?.onItemSelect(rowView, selection)
        }

        valueAutoCompleteTextView.setOnUpdateListener { editText ->
            onAddRemoveFieldListener?.onFieldUpdated(rowView, editText.text.toString().trim())
        }

        fieldsLayout.addView(rowView)

        if (fieldsLayout.childCount == 1) {
            rowView.findViewById<View>(R.id.ib_remove_field).visibility = View.INVISIBLE
        } else {
            valueAutoCompleteTextView.requestFocus()
        }

        return rowView
    }

    private fun addNewField() {
        onAddRemoveFieldListener?.onAddNewField(addRow())
    }

    private fun addField(fieldsLayout: LinearLayout, property: Property) {
        val rowView = addRow()

        val valueEditText = rowView.findViewById<EditText>(R.id.et_value)
        if (fieldsLayout.childCount == 1) {
            valueEditText.id = R.id.et_author_1
        }

        valueEditText.setText(property.value)
        rowView.tag = property
    }

    private fun removeField(fieldView: View) {
        onAddRemoveFieldListener?.onFieldRemove(fieldView)
        val parent = fieldView.parent as ViewGroup
        parent.removeView(fieldView)
    }

    override fun setTitle(text: String) {
        title.setText(text)
    }

    override fun getTitle(): String {
        return title.getTitle()
    }

    override fun setTitle(resid: Int) {
        title.setText(resid)
    }

    override fun setTitleColor(valueColor: Int) {
        title.setColor(valueColor)
    }

    override fun setTitleTextSize(textSize: Int) {
        title.setTextSize(textSize)
    }

    fun setLineSize(lineSize: Int) {
        title.setLineSize(lineSize)
    }

    fun setContentDescriptionX(contentDescription: String) {
        this.contentDescription = contentDescription
    }

    fun setHint(hint: String) {
        this.hint = hint
    }

    fun setOnAddRemoveListener(onAddRemoveFieldListener: OnAddRemoveFieldListener?) {
        this.onAddRemoveFieldListener = onAddRemoveFieldListener
    }

    fun setItems(adapter: FilteredArrayAdapter<Property>, items: ArrayList<Property>) {
        this.adapter = adapter

        var hasFieldsOfType = false
        for (item in items) {
            val i = adapter.getPosition(item)
            if (i >= 0) {
                addField(fieldsLayout, item)
                hasFieldsOfType = true
            }
        }

        if (!hasFieldsOfType) {
            addNewField()
        }
    }
}
