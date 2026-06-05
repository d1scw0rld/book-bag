package org.d1scw0rld.bookbag.fileselector

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Class which combines ImageView and TextView in LinearLayout with horizontal
 * orientation
 */
class TextViewWithImage @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    private val imageView: ImageView
    private val textView: TextView

    init {
        orientation = HORIZONTAL
        imageView = ImageView(context)
        textView = TextView(context)

        val imageParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        addView(imageView, imageParams)

        val textParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 3f)
        addView(textView, textParams)
    }

    /** Simple wrapper around the TextView.getText() method. */
    fun getText(): CharSequence {
        return textView.text
    }

    /**
     * Simple wrapper around ImageView.setImageResource() method, but if resourceId is
     * equal to -1 this method sets Image's visibility as GONE.
     */
    fun setImageResource(resourceId: Int) {
        if (resourceId == -1) {
            imageView.visibility = View.GONE
            return
        }
        imageView.visibility = View.VISIBLE
        imageView.setImageResource(resourceId)
    }

    /** Simple wrapper around TextView.setText() method. */
    fun setText(text: String) {
        textView.text = text
    }
}
