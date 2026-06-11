package org.d1scw0rld.bookbag.ui.fields

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import org.d1scw0rld.bookbag.R

class FieldEditTextUpdatableClearable(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr), Field {

    private lateinit var title: Title
    private lateinit var editTextX: EditTextX

    init {
        initialize(context)

        orientation = VERTICAL
        gravity = Gravity.CENTER_VERTICAL

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.FieldEditTextUpdatableClearable, 0, 0)

            val titleText = typedArray.getString(R.styleable.FieldEditTextUpdatableClearable_title)
            val titleValueColor = typedArray.getColor(R.styleable.FieldEditTextUpdatableClearable_titleColor, 0)
            val titleTextSize = typedArray.getDimensionPixelOffset(R.styleable.FieldEditTextUpdatableClearable_titleTextSize, 0)
            val titleLineSize = typedArray.getDimensionPixelOffset(R.styleable.FieldEditTextUpdatableClearable_titleLineSize, 0)
            val text = typedArray.getString(R.styleable.FieldEditTextUpdatableClearable_android_text)
            val inputType = typedArray.getInteger(R.styleable.FieldEditTextUpdatableClearable_android_inputType, 0)
            val contentDescription = typedArray.getString(R.styleable.FieldEditTextUpdatableClearable_android_contentDescription)
            val hint = typedArray.getString(R.styleable.FieldEditTextUpdatableClearable_android_hint)

            typedArray.recycle()

            titleText?.let { t -> this.title.setText(t) }
            this.title.setColor(titleValueColor)
            this.title.setTextSize(titleTextSize)
            this.title.setLineSize(titleLineSize)

            editTextX.setText(text)
            if (inputType > 0) {
                editTextX.inputType = inputType
            }
            editTextX.contentDescription = contentDescription
            editTextX.hint = hint
        }
    }

    private fun initialize(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.field_edit_text_updatable_clearable, this, true)

        title = findViewById(R.id.title)
        editTextX = findViewById(R.id.editTextX)
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
        editTextX.setText(text)
    }

    fun setText(resourceId: Int) {
        editTextX.setText(resourceId)
    }

    fun getText(): Editable? {
        return editTextX.text
    }

    fun setInputType(type: Int) {
        if (type > 0) {
            editTextX.inputType = type
        }
    }

    fun setMultiline() {
        @Suppress("DEPRECATION")
        editTextX.isSingleLine = false
        editTextX.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
    }

    fun setDigits(digits: String) {
        editTextX.inputType = InputType.TYPE_CLASS_PHONE
        editTextX.keyListener = DigitsKeyListener.getInstance(digits)
    }

    fun setContentDescriptionX(contentDescription: String?) {
        editTextX.contentDescription = contentDescription
    }

    fun setHint(hint: String?) {
        editTextX.hint = hint
    }

    fun setHint(resourceId: Int) {
        editTextX.setHint(resourceId)
    }

    fun setError(error: CharSequence?) {
        editTextX.error = error
    }

    fun setUpdateListener(onUpdateListener: EditTextX.OnUpdateListener) {
        editTextX.setOnUpdateListener(onUpdateListener)
    }

    fun setCallback(callback: EditTextX.Callback) {
        editTextX.setCallback(callback)
    }
}
