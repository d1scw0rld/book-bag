package org.d1scw0rld.bookbag.fields

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.CompoundButton.OnCheckedChangeListener
import android.widget.LinearLayout
import org.d1scw0rld.bookbag.R

class FieldCheckBox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr), Field {

    private lateinit var title: Title
    private lateinit var checkBox: CheckBox

    init {
        initialize(context)

        orientation = VERTICAL
        gravity = Gravity.CENTER_VERTICAL

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.FieldCheckBox, 0, 0)

            val titleText = typedArray.getString(R.styleable.FieldCheckBox_title)
            val titleValueColor = typedArray.getColor(R.styleable.FieldCheckBox_titleColor, 0)
            val titleTextSize = typedArray.getDimensionPixelOffset(R.styleable.FieldCheckBox_titleTextSize, 0)
            val titleLineSize = typedArray.getDimensionPixelOffset(R.styleable.FieldCheckBox_titleLineSize, 0)
            val checked = typedArray.getBoolean(R.styleable.FieldCheckBox_android_checked, false)

            typedArray.recycle()

            titleText?.let { t -> this.title.setText(t) }
            this.title.setColor(titleValueColor)
            this.title.setTextSize(titleTextSize)
            this.title.setLineSize(titleLineSize)
            checkBox.isChecked = checked
        }
    }

    private fun initialize(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.field_check_box, this, true)

        title = findViewById(R.id.title)
        checkBox = findViewById(R.id.check_box)
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

    fun setOnCheckedChangeListener(onCheckedChangeListener: OnCheckedChangeListener?) {
        checkBox.setOnCheckedChangeListener(onCheckedChangeListener)
    }

    fun setChecked(checked: Boolean) {
        checkBox.isChecked = checked
    }

    fun isChecked(): Boolean {
        return checkBox.isChecked
    }
}
