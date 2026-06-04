package org.d1scw0rld.bookbag.fields

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import org.d1scw0rld.bookbag.R

class AutoCompleteTextViewX @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.autoCompleteTextViewStyle,
) : AppCompatAutoCompleteTextView(context, attrs, defStyleAttr) {

    private var onUpdateListener: OnUpdateListener? = null
    private var callback: Callback? = null

    private val onEditorActionListener = OnEditorActionListener { textView, actionId, _ ->
        if ((actionId == EditorInfo.IME_ACTION_DONE) || (actionId == EditorInfo.IME_ACTION_NEXT)) {
            onUpdateListener?.onUpdate(textView as EditText)
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
            }
            textView.clearFocus()
        }
        false
    }

    private val onFocusChangeListenerInternal = OnFocusChangeListener { view, hasFocus ->
        updateDeleteIcon(hasFocus)
        if (!hasFocus) {
            onUpdateListener?.onUpdate(view as EditText)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private val onTouchListenerInternal = OnTouchListener { view, event ->
        view.performClick()
        val drawables = compoundDrawables
        val rightDrawable = drawables[2]
        if ((event.action == MotionEvent.ACTION_UP) && (rightDrawable != null)) {
            if (event.rawX >= (right - rightDrawable.bounds.width())) {
                callback?.beforeClear(this)
                setText("")
                requestFocus()
                (adapter as? ArrayAdapter<*>)?.filter?.filter("")
                callback?.afterClear(this)
                return@OnTouchListener false
            }
        }
        false
    }

    init {
        updateDeleteIcon(isFocused)
        setOnEditorActionListener(onEditorActionListener)
        onFocusChangeListener = onFocusChangeListenerInternal
        
        doOnTextChanged { text, _, _, _ ->
            updateDeleteIcon(text?.toString(), isFocused)
        }

        @Suppress("DEPRECATION")
        isSingleLine = true
        maxLines = 1
        setLines(1)

        setOnTouchListener(onTouchListenerInternal)
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ENTER) {
            clearFocus()
            return false
        }
        return false
    }

    private fun hideKeyboard() {
        val activity = context.findActivity() ?: return
        WindowCompat.getInsetsController(activity.window, this).hide(WindowInsetsCompat.Type.ime())
    }

    fun setOnUpdateListener(onUpdateListener: OnUpdateListener?) {
        this.onUpdateListener = onUpdateListener
    }

    fun interface OnUpdateListener {
        fun onUpdate(editText: EditText)
    }

    fun setCallback(callback: Callback?) {
        this.callback = callback
    }

    interface Callback {
        fun beforeClear(editText: EditText)
        fun afterClear(editText: EditText)
    }

    private fun updateDeleteIcon(focused: Boolean) {
        updateDeleteIcon(null, focused)
    }

    private fun updateDeleteIcon(text: String?, focused: Boolean) {
        val currentText = text ?: (this.text?.toString() ?: "")
        post {
            if (TextUtils.isEmpty(currentText) || !focused) {
                setCompoundDrawables(null, null, null, null)
            } else {
                setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_clear_search_api_holo_light, 0)
            }
        }
    }
}
