package org.d1scw0rld.bookbag.fields

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.RatingBar.OnRatingBarChangeListener
import androidx.appcompat.widget.AppCompatRatingBar
import org.d1scw0rld.bookbag.R

class FieldRating(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr), Field {

    private lateinit var title: Title
    private var isIndicator: Boolean = false
    private var numStars: Int = 5
    private var rating: Float = 0.0f
    private var stepSize: Float = 0.5f
    private lateinit var ratingBar: AppCompatRatingBar

    init {
        initialize(context)

        orientation = VERTICAL
        gravity = Gravity.CENTER_VERTICAL

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.FieldRating, 0, 0)

            val titleText = typedArray.getString(R.styleable.FieldRating_title)
            val titleValueColor = typedArray.getColor(R.styleable.FieldRating_titleColor, 0)
            val titleTextSize = typedArray.getDimensionPixelOffset(R.styleable.FieldRating_titleTextSize, 0)
            val titleLineSize = typedArray.getDimensionPixelOffset(R.styleable.FieldRating_titleLineSize, 0)
            val contentDescription = typedArray.getString(R.styleable.FieldRating_android_contentDescription)
            setNumStars(typedArray.getInteger(R.styleable.FieldRating_android_numStars, 5))
            setRating(typedArray.getFloat(R.styleable.FieldRating_android_rating, 0.0f))
            setStepSize(typedArray.getFloat(R.styleable.FieldRating_android_stepSize, 0.5f))
            setIsIndicator(typedArray.getBoolean(R.styleable.FieldRating_android_isIndicator, false))

            typedArray.recycle()

            titleText?.let { t -> this.title.setText(t) }
            this.title.setColor(titleValueColor)
            this.title.setTextSize(titleTextSize)
            this.title.setLineSize(titleLineSize)
            ratingBar.contentDescription = contentDescription
            ratingBar.numStars = numStars
            ratingBar.rating = rating
            ratingBar.stepSize = stepSize
            ratingBar.setIsIndicator(isIndicator)
        }
    }

    private fun initialize(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.field_rating, this, true)

        title = findViewById(R.id.title)
        ratingBar = findViewById(R.id.rating_bar)
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
        ratingBar.contentDescription = contentDescription
    }

    fun getNumStars(): Int {
        return numStars
    }

    fun setNumStars(numStars: Int) {
        this.numStars = numStars
    }

    fun getRating(): Float {
        return rating
    }

    fun setRating(rating: Float) {
        this.rating = rating
        ratingBar.rating = rating
    }

    fun getStepSize(): Float {
        return stepSize
    }

    fun setStepSize(stepSize: Float) {
        this.stepSize = stepSize
    }

    fun isIndicator(): Boolean {
        return isIndicator
    }

    fun setIsIndicator(isIndicator: Boolean) {
        this.isIndicator = isIndicator
    }

    fun setOnRatingBarChangeListener(onRatingBarChangeListener: OnRatingBarChangeListener?) {
        ratingBar.onRatingBarChangeListener = onRatingBarChangeListener
    }
}
