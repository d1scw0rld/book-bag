package org.d1scw0rld.bookbag.fields

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import org.d1scw0rld.bookbag.R

class FieldAutoCompleteTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr), Field {

    private lateinit var title: Title
    private lateinit var autoCompleteTextViewX: AutoCompleteTextViewX

    init {
        initialize(context)

        orientation = VERTICAL
        gravity = Gravity.CENTER_VERTICAL

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.FieldAutoCompleteTextView, 0, 0)

            val titleText = typedArray.getString(R.styleable.FieldAutoCompleteTextView_title)
            val titleValueColor = typedArray.getColor(R.styleable.FieldAutoCompleteTextView_titleColor, 0)
            val titleTextSize = typedArray.getDimensionPixelOffset(R.styleable.FieldAutoCompleteTextView_titleTextSize, 0)
            val titleLineSize = typedArray.getDimensionPixelOffset(R.styleable.FieldAutoCompleteTextView_titleLineSize, 0)
            val text = typedArray.getString(R.styleable.FieldAutoCompleteTextView_android_text)
            val hint = typedArray.getString(R.styleable.FieldAutoCompleteTextView_android_hint)

            typedArray.recycle()

            titleText?.let { t -> this.title.setText(t) }
            this.title.setColor(titleValueColor)
            this.title.setTextSize(titleTextSize)
            this.title.setLineSize(titleLineSize)

            autoCompleteTextViewX.setText(text)
            autoCompleteTextViewX.hint = hint
        }
    }

    private fun initialize(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.field_auto_complete_text_view, this, true)

        title = findViewById(R.id.title)
        autoCompleteTextViewX = findViewById(R.id.autoCompleteTextView)
        autoCompleteTextViewX.threshold = 1
    }

    override fun setTitle(text: String) {
        title.setText(text)
    }

    override fun setTitle(resid: Int) {
        title.setText(resid)
    }

    override fun getTitle(): String {
        return title.getTitle()
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

    fun setText(text: String?) {
        autoCompleteTextViewX.setText(text)
    }

    fun setText(resourceId: Int) {
        autoCompleteTextViewX.setText(resourceId)
    }

    fun getText(): Editable? {
        return autoCompleteTextViewX.text
    }

    fun setHint(hint: String?) {
        autoCompleteTextViewX.hint = hint
    }

    fun setHint(resourceId: Int) {
        autoCompleteTextViewX.setHint(resourceId)
    }

    fun setThreshold(threshold: Int) {
        autoCompleteTextViewX.threshold = threshold
    }

    fun setAdapter(adapter: ArrayAdapter<*>?) {
        autoCompleteTextViewX.setAdapter(adapter)
    }

    fun setUpdateListener(onUpdateListener: AutoCompleteTextViewX.OnUpdateListener?) {
        autoCompleteTextViewX.setOnUpdateListener(onUpdateListener)
    }

    fun setCallback(callback: AutoCompleteTextViewX.Callback?) {
        autoCompleteTextViewX.setCallback(callback)
    }

    fun setOnItemSelectedListener(listener: OnItemSelectedListener?) {
        autoCompleteTextViewX.onItemSelectedListener = listener
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        autoCompleteTextViewX.onItemClickListener = listener
    }
}
