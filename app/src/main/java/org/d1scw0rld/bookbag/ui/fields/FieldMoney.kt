package org.d1scw0rld.bookbag.ui.fields

import android.content.Context
import android.text.InputFilter
import android.text.Spanned
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import org.d1scw0rld.bookbag.R
import org.d1scw0rld.bookbag.data.DbConstants
import java.util.regex.Pattern

class FieldMoney(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr), Field {

    companion object {
        val FILTER = "0*[1-9]?\\d{0,3}(\\" + DbConstants.separator + "\\d{0,2})?"
    }

    private lateinit var title: Title
    private lateinit var spinner: Spinner
    private lateinit var editTextX: EditTextX

    init {
        initialize(context)

        orientation = VERTICAL
        gravity = Gravity.CENTER_VERTICAL

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.FieldMoney, 0, 0)

            val titleText = typedArray.getString(R.styleable.FieldMoney_title)
            val titleValueColor = typedArray.getColor(R.styleable.FieldMoney_titleColor, 0)
            val titleTextSize = typedArray.getDimensionPixelOffset(R.styleable.FieldMoney_titleTextSize, 0)
            val titleLineSize = typedArray.getDimensionPixelOffset(R.styleable.FieldMoney_titleLineSize, 0)
            val contentDescription = typedArray.getString(R.styleable.FieldMoney_android_contentDescription)
            val hint = typedArray.getString(R.styleable.FieldMoney_android_hint)

            typedArray.recycle()

            titleText?.let { t -> this.title.setText(t) }
            this.title.setColor(titleValueColor)
            this.title.setTextSize(titleTextSize)
            this.title.setLineSize(titleLineSize)

            spinner.contentDescription = contentDescription
            editTextX.hint = hint
        }
    }

    private fun initialize(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.field_money, this, true)

        title = findViewById(R.id.title)
        spinner = findViewById(R.id.action_select_type)
        editTextX = findViewById(R.id.editTextX)
        editTextX.filters = arrayOf(DecimalDigitsInputFilter())
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

    fun setValue(value: Int) {
        editTextX.setText(
            String.format(
                resources.getString(R.string.amn_vl),
                value / 100,
                DbConstants.separator,
                value % 100
            )
        )
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

    fun setHint(hint: String?) {
        editTextX.hint = hint
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
        fun onUpdate(fieldMoney: FieldMoney)
    }

    private inner class DecimalDigitsInputFilter : InputFilter {
        private val pattern: Pattern = Pattern.compile(FILTER)

        override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dstart: Int,
            dend: Int,
        ): CharSequence? {
            val formattedSource = source.subSequence(start, end).toString()
            val destPrefix = dest.subSequence(0, dstart).toString()
            val destSuffix = dest.subSequence(dend, dest.length).toString()
            var result = destPrefix + formattedSource + destSuffix
            result = result.replace(",", ".")

            val matcher = pattern.matcher(result)
            return if (matcher.matches()) null else ""
        }
    }

    fun setUpdateListener(onUpdateListener: EditTextX.OnUpdateListener) {
        editTextX.setOnUpdateListener(onUpdateListener)
    }
}
