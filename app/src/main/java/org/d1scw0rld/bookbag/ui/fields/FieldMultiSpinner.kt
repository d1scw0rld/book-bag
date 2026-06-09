package org.d1scw0rld.bookbag.ui.fields

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import org.d1scw0rld.bookbag.R

class FieldMultiSpinner(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr), Field {

    private lateinit var title: Title
    private lateinit var selectButton: Button
    private var hint: String = ""
    private var items = ArrayList<Item>()
    private var onUpdateListener: OnUpdateListener? = null

    init {
        initialize(context)

        orientation = VERTICAL
        gravity = Gravity.CENTER_VERTICAL

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.FieldMultiSpinner, 0, 0)

            val titleText = typedArray.getString(R.styleable.FieldMultiSpinner_title)
            val titleValueColor = typedArray.getColor(R.styleable.FieldMultiSpinner_titleColor, 0)
            val titleTextSize = typedArray.getDimensionPixelOffset(R.styleable.FieldMultiSpinner_titleTextSize, 0)
            val titleLineSize = typedArray.getDimensionPixelOffset(R.styleable.FieldMultiSpinner_titleLineSize, 0)
            val contentDescription = typedArray.getString(R.styleable.FieldMultiSpinner_android_contentDescription)
            hint = typedArray.getString(R.styleable.FieldMultiSpinner_android_hint) ?: ""

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
        inflater.inflate(R.layout.field_multi_spinner, this, true)

        title = findViewById(R.id.title)
        selectButton = findViewById(R.id.action_select_type)

        setButtonText(selectButton, items)
        selectButton.setOnClickListener { v ->
            displayPopupWindow(v, items)
        }
    }

    private fun setButtonText(button: Button, itemsList: ArrayList<Item>) {
        val buttonTextBuilder = StringBuilder()
        for (item in itemsList) {
            if (item.isSelected) {
                buttonTextBuilder.append(if (buttonTextBuilder.isEmpty()) "" else ", ")
                    .append(item.title)
            }
        }

        if (buttonTextBuilder.isNotEmpty()) {
            button.text = buttonTextBuilder.toString()
            button.setTextColor(Color.BLACK)
        } else {
            button.text = hint
            button.setTextColor(Color.GRAY)
        }
    }

    private fun displayPopupWindow(anchorView: View, itemsList: ArrayList<Item>?) {
        if (itemsList == null) return
        val popupMenu = PopupMenu(context, anchorView)
        initPopupMenu(popupMenu, itemsList)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            if (menuItem.order < itemsList.size) {
                menuItem.isChecked = !menuItem.isChecked
                val item = itemsList[menuItem.order]
                item.isSelected = menuItem.isChecked

                setButtonText(anchorView as Button, itemsList)
                onUpdateListener?.onUpdate(item)

                popupMenu.show()
            } else {
                val builder = AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle)
                builder.setTitle(R.string.add_new)
                val newValueEditText = AppCompatEditText(context)
                builder.setView(newValueEditText)
                builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
                    val newValue = newValueEditText.text.toString().trim()
                    val item = Item(newValue)
                    item.id = itemsList.size.toLong()
                    item.isSelected = true
                    itemsList.add(item)
                    setButtonText(anchorView as Button, itemsList)
                    onUpdateListener?.onUpdate(item)
                    popupMenu.dismiss()
                    initPopupMenu(popupMenu, itemsList)

                    context.findActivity()?.let { activity ->
                        WindowCompat.getInsetsController(activity.window, anchorView).hide(WindowInsetsCompat.Type.ime())
                    }
                    dialog.cancel()
                    popupMenu.show()
                }

                builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    context.findActivity()?.let { activity ->
                        WindowCompat.getInsetsController(activity.window, anchorView).hide(WindowInsetsCompat.Type.ime())
                    }
                    dialog.cancel()
                    popupMenu.show()
                }

                builder.show()
            }
            true
        }

        popupMenu.show()
    }

    private fun initPopupMenu(popupMenu: PopupMenu, itemsList: ArrayList<Item>) {
        popupMenu.menu.clear()

        for (i in itemsList.indices) {
            popupMenu.menu.add(Menu.NONE, 0, i, itemsList[i].title)
                .setCheckable(true)
                .setChecked(itemsList[i].isSelected)
        }
        popupMenu.menu.add(Menu.NONE, 0, itemsList.size, "<add>")
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
        setButtonText(selectButton, items)
    }

    fun setItems(items: ArrayList<Item>) {
        this.items = items
        setButtonText(selectButton, items)
    }

    fun setOnUpdateListener(onUpdateListener: OnUpdateListener?) {
        this.onUpdateListener = onUpdateListener
    }

    class Item {
        var title: String = ""
        var isSelected: Boolean = false
        var id: Long = -1

        constructor()

        constructor(title: String) {
            this.title = title
        }

        constructor(id: Long, title: String) {
            this.id = id
        }
    }

    fun interface OnUpdateListener {
        fun onUpdate(item: Item)
    }
}
