package org.d1scw0rld.bookbag

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import org.d1scw0rld.bookbag.data.DbConstants
import org.d1scw0rld.bookbag.dto.Book
import org.d1scw0rld.bookbag.dto.Date
import org.d1scw0rld.bookbag.dto.Field
import org.d1scw0rld.bookbag.dto.Price
import org.d1scw0rld.bookbag.dto.Property

class BookDetailFieldsFactory(
    private val context: Context,
    private val currencies: List<Property>,
    private val book: Book?,
) {
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    companion object {
        private const val SEP = ", "
    }

    fun addFields(rootView: ViewGroup) {
        if (book == null) return

        val categoriesLayout = rootView.findViewById<LinearLayout>(R.id.ll_categories) ?: return

        for (field in DbConstants.FIELDS) {
            var name = field.name
            val valueBuilder = StringBuilder()

            if (field.id > 99) {
                when (field.type) {
                    Field.TYPE_TEXT -> {
                        val textValue = when (field.id) {
                            DbConstants.FLD_TITLE -> book.title.value
                            DbConstants.FLD_DESCRIPTION -> book.description.value
                            DbConstants.FLD_VOLUME -> if (book.volume.value != 0) book.volume.value.toString() else ""
                            DbConstants.FLD_PAGES -> if (book.pages.value != 0) book.pages.value.toString() else ""
                            DbConstants.FLD_EDITION -> if (book.edition.value != 0) book.edition.value.toString() else ""
                            DbConstants.FLD_ISBN -> book.isbn.value
                            DbConstants.FLD_WEB -> book.web.value
                            else -> ""
                        }
                        valueBuilder.append(textValue)
                    }
                    Field.TYPE_MONEY -> {
                        val price = when (field.id) {
                            DbConstants.FLD_PRICE -> Price(book.price.value)
                            DbConstants.FLD_VALUE -> Price(book.value.value)
                            else -> null
                        }

                        if ((price != null) && (price.value != 0)) {
                            val fieldCurrency = currencies.firstOrNull { it.id == price.currencyId }
                            val formattedValue = if (fieldCurrency == null) {
                                String.format(
                                    context.resources.getString(R.string.amn_vl),
                                    price.value / 100,
                                    DbConstants.separator,
                                    price.value % 100
                                )
                            } else {
                                String.format(
                                    context.resources.getString(R.string.amn_vl_crn),
                                    price.value / 100,
                                    DbConstants.separator,
                                    price.value % 100,
                                    fieldCurrency.value
                                )
                            }
                            valueBuilder.append(formattedValue)
                        }
                    }
                    Field.TYPE_DATE -> {
                        val date = when (field.id) {
                            DbConstants.FLD_READ_DATE -> Date(book.readDate.value)
                            DbConstants.FLD_DUE_DATE -> Date(book.dueDate.value)
                            else -> null
                        }
                        if (date != null && date.toInt() != 0) {
                            valueBuilder.append(date.toString())
                        }
                    }
                }
            } else {
                for (property in book.properties) {
                    if (property.fieldTypeId == field.id) {
                        when (field.type) {
                            Field.TYPE_MULTIFIELD, Field.TYPE_MULTI_SPINNER -> {
                                val splitNames = field.name.split("|")
                                if (splitNames.size > 1) {
                                    name = splitNames[1]
                                }
                                if (valueBuilder.isNotEmpty()) {
                                    valueBuilder.append(SEP)
                                }
                                valueBuilder.append(property.value)
                            }
                            else -> {
                                valueBuilder.append(property.value)
                            }
                        }
                    }
                }
            }

            val finalValue = valueBuilder.toString().trim()
            if (finalValue.isNotEmpty()) {
                when (field.type) {
                    Field.TYPE_RATING -> addRatingField(categoriesLayout, name, finalValue)
                    Field.TYPE_CHECK_BOX -> addCheckBoxField(categoriesLayout, name, finalValue)
                    else -> addField(categoriesLayout, name, finalValue)
                }
            }
        }
    }

    private fun addField(rootView: LinearLayout, name: String, value: String) {
        val rowView = inflater.inflate(R.layout.row_category_new, rootView, false)
        rowView.findViewById<TextView>(R.id.tv_title).text = name
        rowView.findViewById<TextView>(R.id.tv_value).text = value
        rootView.addView(rowView)
    }

    private fun addRatingField(rootView: LinearLayout, name: String, value: String) {
        val rowView = inflater.inflate(R.layout.row_category_rating, rootView, false)
        rowView.findViewById<TextView>(R.id.tv_title).text = name
        rowView.findViewById<RatingBar>(R.id.rating_bar).rating = value.toFloatOrNull() ?: 0f
        rootView.addView(rowView)
    }

    private fun addCheckBoxField(rootView: LinearLayout, name: String, value: String) {
        val rowView = inflater.inflate(R.layout.row_category_check_box, rootView, false)
        rowView.findViewById<TextView>(R.id.tv_title).text = name
        rowView.findViewById<CheckBox>(R.id.check_box).isChecked = value.toBoolean()
        rootView.addView(rowView)
    }
}
