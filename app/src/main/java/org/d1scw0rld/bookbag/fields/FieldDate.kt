package org.d1scw0rld.bookbag.fields

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog.OnDateSetListener
import org.d1scw0rld.bookbag.R
import org.d1scw0rld.bookbag.dto.Date
import java.util.Calendar

class FieldDate @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr), OnDateSetListener, Field {

    private lateinit var title: Title
    private lateinit var selectButton: Button
    private var date = Date(0)
    private var hint: String = ""
    private var onUpdateListener: OnUpdateListener? = null
    var datePickerDialog: DatePickerDialog? = null

    init {
        initialize(context)

        orientation = VERTICAL
        gravity = Gravity.CENTER_VERTICAL

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.FieldDate, 0, 0)

            val titleText = typedArray.getString(R.styleable.FieldDate_title)
            val titleValueColor = typedArray.getColor(R.styleable.FieldDate_titleColor, 0)
            val titleTextSize = typedArray.getDimensionPixelOffset(R.styleable.FieldDate_titleTextSize, 0)
            val titleLineSize = typedArray.getDimensionPixelOffset(R.styleable.FieldDate_titleLineSize, 0)
            val contentDescription = typedArray.getString(R.styleable.FieldDate_android_contentDescription)
            hint = typedArray.getString(R.styleable.FieldDate_android_hint) ?: ""

            typedArray.recycle()

            titleText?.let { t -> this.title.setText(t) }
            this.title.setColor(titleValueColor)
            this.title.setTextSize(titleTextSize)
            this.title.setLineSize(titleLineSize)

            selectButton.contentDescription = contentDescription
        }
    }

    private fun initialize(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.field_date, this, true)

        title = findViewById(R.id.title)
        selectButton = findViewById(R.id.action_select_type)

        selectButton.setOnClickListener {
            val activity = context.findActivity() ?: return@setOnClickListener

            if (date.toInt() == 0) {
                // Use the current date as the default date in the picker
                val calendar = Calendar.getInstance()
                date = Date(
                    calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.YEAR)
                )
            }

            datePickerDialog = DatePickerDialog.newInstance(
                this,
                date.year,
                date.month - 1,
                date.day
            ).apply {
                isThemeDark = false
                showYearPickerFirst(false)
                setAccentColor(context.getColor(R.color.primary))
                setCancelColor(context.getColor(R.color.accent))
                setOkColor(context.getColor(R.color.accent))
                setTitle("Select Date From DatePickerDialog")
                @Suppress("DEPRECATION")
                show(activity.fragmentManager, "DatePickerDialog")
            }
        }
    }

    private fun setButtonText(button: Button, date: Date) {
        if (date.toInt() != 0) {
            button.text = "${date.day}/${date.month}/${date.year}"
            button.setTextColor(Color.BLACK)
        } else {
            button.text = hint
            button.setTextColor(Color.GRAY)
        }
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
        selectButton.contentDescription = contentDescription
    }

    fun setHint(hint: String) {
        this.hint = hint
        setButtonText(selectButton, date)
    }

    fun getDate(): Date {
        return date
    }

    fun setDate(date: Date) {
        this.date = date
        setButtonText(selectButton, date)
    }

    fun setUpdateListener(onUpdateListener: OnUpdateListener?) {
        this.onUpdateListener = onUpdateListener
    }

    interface OnUpdateListener {
        fun onUpdate(date: Date)
        fun onUpdate(fieldDate: FieldDate)
    }

    override fun onDateSet(
        view: DatePickerDialog?,
        year: Int,
        monthOfYear: Int,
        dayOfMonth: Int
    ) {
        date = Date(dayOfMonth, monthOfYear + 1, year)
        setButtonText(selectButton, date)
        onUpdateListener?.onUpdate(this)
    }
}
