package org.d1scw0rld.bookbag.data

import org.d1scw0rld.bookbag.R
import org.d1scw0rld.bookbag.dto.Field
import java.text.DecimalFormatSymbols

object DbConstants {
    const val DATABASE_NAME = "book_bag.db"
    const val TAG = "DB"

    val separator: Char = DecimalFormatSymbols.getInstance().decimalSeparator

    const val FLD_AUTHOR = 1
    const val FLD_SERIE = 2
    const val FLD_GENRE = 3
    const val FLD_LANGUAGE = 4
    const val FLD_PUBLISHER = 5
    const val FLD_PUBLICATION_LOCATION = 6
    const val FLD_STATUS = 7
    const val FLD_RATING = 8
    const val FLD_FORMAT = 9
    const val FLD_LOCATION = 10
    const val FLD_CONDITION = 11
    const val FLD_CURRENCY = 12
    const val FLD_READ = 13
    const val FLD_LOANED_TO = 14
    const val FLD_TITLE = 99
    const val FLD_DESCRIPTION = 100
    const val FLD_VOLUME = 101
    const val FLD_PUBLICATION_DATE = 102
    const val FLD_PAGES = 103
    const val FLD_PRICE = 104
    const val FLD_VALUE = 105
    const val FLD_DUE_DATE = 106
    const val FLD_READ_DATE = 107
    const val FLD_EDITION = 108
    const val FLD_ISBN = 109
    const val FLD_WEB = 110

    const val SRT_TTL = 1
    const val SRT_AUT = 2
    const val SRT_WNT_PBL_AUT = 3
    const val SRT_WNT_PBL_TTL = 4
    const val SRT_RD_AUT = 5
    const val SRT_NOT_RD_AUT = 6
    const val SRT_NOT_RD_TTL = 7
    const val SRT_RD_TTL = 8
    const val SRT_PBL_AUT = 9
    const val SRT_PBL_TTL = 10
    const val SRT_LND_TTL = 11
    const val SRT_LND_BRW = 12

    val FIELDS = ArrayList<Field>()

    fun initFields(resources: android.content.res.Resources) {
        FIELDS.clear()
        FIELDS.add(Field(FLD_TITLE, resources.getString(R.string.fld_title), Field.TYPE_TEXT).setVisibility(true))
        FIELDS.add(Field(FLD_AUTHOR, resources.getString(R.string.fld_author), Field.TYPE_MULTIFIELD).setVisibility(true))
        FIELDS.add(Field(FLD_DESCRIPTION, resources.getString(R.string.fld_descrition), Field.TYPE_TEXT).setInputType(android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE).setVisibility(false))
        FIELDS.add(Field(FLD_SERIE, resources.getString(R.string.fld_serie), Field.TYPE_TEXT_AUTOCOMPLETE))
        FIELDS.add(Field(FLD_VOLUME, resources.getString(R.string.fld_volume), Field.TYPE_TEXT).setInputType(android.text.InputType.TYPE_CLASS_NUMBER))
        FIELDS.add(Field(FLD_GENRE, resources.getString(R.string.fld_genre), Field.TYPE_MULTI_SPINNER))
        FIELDS.add(Field(FLD_LANGUAGE, resources.getString(R.string.fld_language), Field.TYPE_SPINNER))
        FIELDS.add(Field(FLD_PAGES, resources.getString(R.string.fld_pages), Field.TYPE_TEXT).setInputType(android.text.InputType.TYPE_CLASS_NUMBER))
        FIELDS.add(Field(FLD_PUBLISHER, resources.getString(R.string.fld_publisher), Field.TYPE_TEXT_AUTOCOMPLETE))
        FIELDS.add(Field(FLD_PUBLICATION_DATE, resources.getString(R.string.fld_publication_date), Field.TYPE_TEXT).setInputType(android.text.InputType.TYPE_CLASS_NUMBER))
        FIELDS.add(Field(FLD_PUBLICATION_LOCATION, resources.getString(R.string.fld_publication_location), Field.TYPE_TEXT_AUTOCOMPLETE))
        FIELDS.add(Field(FLD_EDITION, resources.getString(R.string.fld_edition), Field.TYPE_TEXT).setInputType(android.text.InputType.TYPE_CLASS_NUMBER))
        FIELDS.add(Field(FLD_PRICE, resources.getString(R.string.fld_price), Field.TYPE_MONEY).setInputType(android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL))
        FIELDS.add(Field(FLD_VALUE, resources.getString(R.string.fld_value), Field.TYPE_MONEY).setInputType(android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL))
        FIELDS.add(Field(FLD_STATUS, resources.getString(R.string.fld_status), Field.TYPE_SPINNER))
        FIELDS.add(Field(FLD_LOANED_TO, resources.getString(R.string.fld_loaned_to), Field.TYPE_TEXT_AUTOCOMPLETE))
        FIELDS.add(Field(FLD_READ, resources.getString(R.string.fld_read), Field.TYPE_CHECK_BOX))
        FIELDS.add(Field(FLD_READ_DATE, resources.getString(R.string.fld_read_date), Field.TYPE_DATE))
        FIELDS.add(Field(FLD_RATING, resources.getString(R.string.fld_rating), Field.TYPE_RATING))
        FIELDS.add(Field(FLD_FORMAT, resources.getString(R.string.fld_format), Field.TYPE_SPINNER))
        FIELDS.add(Field(FLD_CONDITION, resources.getString(R.string.fld_condition), Field.TYPE_SPINNER))
        FIELDS.add(Field(FLD_LOCATION, resources.getString(R.string.fld_location), Field.TYPE_TEXT_AUTOCOMPLETE))
        FIELDS.add(Field(FLD_DUE_DATE, resources.getString(R.string.fld_due_date), Field.TYPE_DATE))
        FIELDS.add(Field(FLD_ISBN, resources.getString(R.string.fld_isbn), Field.TYPE_TEXT).setInputType(android.text.InputType.TYPE_CLASS_NUMBER))
        FIELDS.add(Field(FLD_WEB, resources.getString(R.string.fld_web), Field.TYPE_TEXT))
    }
}
