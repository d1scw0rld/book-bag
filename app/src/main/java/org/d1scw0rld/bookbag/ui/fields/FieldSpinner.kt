package org.d1scw0rld.bookbag.ui.fields

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import org.d1scw0rld.bookbag.R

class FieldSpinner(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr), Field {

    private lateinit var title: Title
    private lateinit var spinner: Spinner

    init {
        initialize(context)

        orientation = VERTICAL
        gravity = Gravity.CENTER_VERTICAL

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.FieldSpinner, 0, 0)

            val titleText = typedArray.getString(R.styleable.FieldSpinner_title)
            val titleValueColor = typedArray.getColor(R.styleable.FieldSpinner_titleColor, 0)
            val titleTextSize = typedArray.getDimensionPixelOffset(R.styleable.FieldSpinner_titleTextSize, 0)
            val titleLineSize = typedArray.getDimensionPixelOffset(R.styleable.FieldSpinner_titleLineSize, 0)
            val contentDescription = typedArray.getString(R.styleable.FieldSpinner_android_contentDescription)

            typedArray.recycle()

            titleText?.let { t -> this.title.setText(t) }
            this.title.setColor(titleValueColor)
            this.title.setTextSize(titleTextSize)
            this.title.setLineSize(titleLineSize)
            spinner.contentDescription = contentDescription
        }
    }

    private fun initialize(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.field_spinner, this, true)

        title = findViewById(R.id.title)
        spinner = findViewById(R.id.action_select_type)
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

    fun setContentDescriptionX(contentDescription: String?) {
        spinner.contentDescription = contentDescription
    }

    fun setAdapter(adapter: ArrayAdapter<*>?) {
        spinner.adapter = adapter
    }

    fun setOnItemSelectedListener(listener: OnItemSelectedListener?) {
        spinner.onItemSelectedListener = listener
    }

    fun setSelection(position: Int) {
        if (position >= 0) {
            spinner.setSelection(position)
        }
    }

    interface OnUpdateListener {
        fun onUpdate(fieldSpinner: FieldSpinner, position: Int)
    }
}
