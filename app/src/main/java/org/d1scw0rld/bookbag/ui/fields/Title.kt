package org.d1scw0rld.bookbag.ui.fields

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import org.d1scw0rld.bookbag.R

class Title : LinearLayout {

    private lateinit var title: TextView
    private lateinit var line: LinearLayout

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        if (!isInEditMode) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.Title, 0, 0)
            val titleText = typedArray.getString(R.styleable.Title_text)
            val valueColor = typedArray.getColor(
                R.styleable.Title_color,
                ResourcesCompat.getColor(resources, android.R.color.black, context.theme)
            )
            val textSize = typedArray.getDimensionPixelOffset(R.styleable.Title_textSize, 0)
            val lineSize = typedArray.getDimensionPixelOffset(R.styleable.Title_lineSize, 0)
            typedArray.recycle()

            orientation = VERTICAL
            gravity = Gravity.CENTER_VERTICAL

            initialize(context)

            title.text = titleText
            title.setTextColor(valueColor)
            if (textSize > 0) {
                title.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
            }

            line.setBackgroundColor(valueColor)
            val params = line.layoutParams
            if (lineSize > 0 && params != null) {
                params.height = lineSize
                params.width = LayoutParams.MATCH_PARENT
                line.layoutParams = params
            }
        }
    }

    private fun initialize(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.title, this, true)

        title = findViewById(R.id.tv_title)
        line = findViewById(R.id.ll_line)
    }

    fun setText(text: String?) {
        title.text = text
    }

    fun getTitle(): String {
        return title.text.toString()
    }

    fun setText(resourceId: Int) {
        title.setText(resourceId)
    }

    fun setTextSize(textSize: Int) {
        if (textSize > 0) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
        }
    }

    fun setColor(valueColor: Int) {
        title.setTextColor(valueColor)
        line.setBackgroundColor(valueColor)
    }

    fun setLineSize(lineSize: Int) {
        val params = line.layoutParams
        if (lineSize > 0 && params != null) {
            params.height = lineSize
            params.width = LayoutParams.MATCH_PARENT
            line.layoutParams = params
        }
    }
}
