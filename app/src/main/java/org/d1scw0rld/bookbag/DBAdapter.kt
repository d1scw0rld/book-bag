package org.d1scw0rld.bookbag

import android.content.ContentValues
import android.content.Context
import android.content.res.TypedArray
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.text.InputType
import android.util.Log
import org.d1scw0rld.bookbag.dto.Book
import org.d1scw0rld.bookbag.dto.BookResult
import org.d1scw0rld.bookbag.dto.Field
import org.d1scw0rld.bookbag.dto.FileUtils
import org.d1scw0rld.bookbag.dto.ParentResult
import org.d1scw0rld.bookbag.dto.Property
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.DecimalFormatSymbols
import androidx.core.database.sqlite.transaction

class DBAdapter(private val context: Context) {

    private var db: SQLiteDatabase? = null
    private val dbHelper: DBOpenHelper = DBOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION)

    init {
        val resources = context.resources
        FIELDS.clear()
        FIELDS.add(Field(FLD_TITLE, resources.getString(R.string.fld_title), Field.TYPE_TEXT).setVisibility(true))
        FIELDS.add(Field(FLD_AUTHOR, resources.getString(R.string.fld_author), Field.TYPE_MULTIFIELD).setVisibility(true))
        FIELDS.add(Field(FLD_DESCRIPTION, resources.getString(R.string.fld_descrition), Field.TYPE_TEXT).setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE).setVisibility(false))
        FIELDS.add(Field(FLD_SERIE, resources.getString(R.string.fld_serie), Field.TYPE_TEXT_AUTOCOMPLETE))
        FIELDS.add(Field(FLD_VOLUME, resources.getString(R.string.fld_volume), Field.TYPE_TEXT).setInputType(InputType.TYPE_CLASS_NUMBER))
        FIELDS.add(Field(FLD_GENRE, resources.getString(R.string.fld_genre), Field.TYPE_MULTI_SPINNER))
        FIELDS.add(Field(FLD_LANGUAGE, resources.getString(R.string.fld_language), Field.TYPE_SPINNER))
        FIELDS.add(Field(FLD_PAGES, resources.getString(R.string.fld_pages), Field.TYPE_TEXT).setInputType(InputType.TYPE_CLASS_NUMBER))
        FIELDS.add(Field(FLD_PUBLISHER, resources.getString(R.string.fld_publisher), Field.TYPE_TEXT_AUTOCOMPLETE))
        FIELDS.add(Field(FLD_PUBLICATION_DATE, resources.getString(R.string.fld_publication_date), Field.TYPE_TEXT).setInputType(InputType.TYPE_CLASS_NUMBER))
        FIELDS.add(Field(FLD_PUBLICATION_LOCATION, resources.getString(R.string.fld_publication_location), Field.TYPE_TEXT_AUTOCOMPLETE))
        FIELDS.add(Field(FLD_EDITION, resources.getString(R.string.fld_edition), Field.TYPE_TEXT).setInputType(InputType.TYPE_CLASS_NUMBER))
        FIELDS.add(Field(FLD_PRICE, resources.getString(R.string.fld_price), Field.TYPE_MONEY).setInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL))
        FIELDS.add(Field(FLD_VALUE, resources.getString(R.string.fld_value), Field.TYPE_MONEY).setInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL))
        FIELDS.add(Field(FLD_STATUS, resources.getString(R.string.fld_status), Field.TYPE_SPINNER))
        FIELDS.add(Field(FLD_LOANED_TO, resources.getString(R.string.fld_loaned_to), Field.TYPE_TEXT_AUTOCOMPLETE))
        FIELDS.add(Field(FLD_READ, resources.getString(R.string.fld_read), Field.TYPE_CHECK_BOX))
        FIELDS.add(Field(FLD_READ_DATE, resources.getString(R.string.fld_read_date), Field.TYPE_DATE))
        FIELDS.add(Field(FLD_RATING, resources.getString(R.string.fld_rating), Field.TYPE_RATING))
        FIELDS.add(Field(FLD_FORMAT, resources.getString(R.string.fld_format), Field.TYPE_SPINNER))
        FIELDS.add(Field(FLD_CONDITION, resources.getString(R.string.fld_condition), Field.TYPE_SPINNER))
        FIELDS.add(Field(FLD_LOCATION, resources.getString(R.string.fld_location), Field.TYPE_TEXT_AUTOCOMPLETE))
        FIELDS.add(Field(FLD_DUE_DATE, resources.getString(R.string.fld_due_date), Field.TYPE_DATE))
        FIELDS.add(Field(FLD_ISBN, resources.getString(R.string.fld_isbn), Field.TYPE_TEXT).setInputType(InputType.TYPE_CLASS_NUMBER))
        FIELDS.add(Field(FLD_WEB, resources.getString(R.string.fld_web), Field.TYPE_TEXT))
    }

    @Throws(SQLiteException::class)
    fun open() {
        db = try {
            dbHelper.writableDatabase
        } catch (_: SQLiteException) {
            dbHelper.readableDatabase
        }
    }

    fun close() {
        dbHelper.close()
    }

    private fun getBooksOrderedBy(query: String): ArrayList<ParentResult>? {
        if (Debug.ON) {
            return null
        }
        val database = db ?: return null
        val parentResults = ArrayList<ParentResult>()

        database.rawQuery(query, null).use { cursor ->
            if (cursor.moveToFirst()) {
                var childResults = ArrayList<BookResult>()
                var parent = cursor.getString(0).orEmpty()
                var parentResult = ParentResult(parent, childResults)

                do {
                    parent = cursor.getString(0).orEmpty()
                    val result = BookResult(cursor.getLong(1), cursor.getString(2).orEmpty())
                    if (!parent.equals(parentResult.name, ignoreCase = true)) {
                        parentResults.add(parentResult)
                        childResults = ArrayList()
                        parentResult = ParentResult(parent, childResults)
                    }
                    childResults.add(result)
                } while (cursor.moveToNext())
                parentResults.add(parentResult)
            }
        }

        return parentResults
    }

    fun getBooks(iOrder: Int): ArrayList<ParentResult>? {
        val query = when (iOrder) {
            SRT_TTL -> QR_TTL
            SRT_AUT -> QR_AUT
            SRT_WNT_PBL_AUT -> QR_WNT_AUT
            SRT_WNT_PBL_TTL -> QR_WNT_TTL
            SRT_RD_AUT -> QR_RD_AUT
            SRT_RD_TTL -> QR_RD_TTL
            SRT_NOT_RD_AUT -> QR_NOT_RD_AUT
            SRT_NOT_RD_TTL -> QR_NOT_RD_TTL
            SRT_PBL_AUT -> QR_PBL_AUT
            SRT_PBL_TTL -> QR_PBL_TTL
            SRT_LND_TTL -> QR_LND_TTL
            SRT_LND_BRW -> QR_LND_BRW
            else -> return null
        }

        return getBooksOrderedBy(query)
    }

    fun insertBook(book: Book) {
        val database = db ?: return
        database.transaction {
            try {
                val values = ContentValues().apply {
                    put(KEY_TTL, book.title.value)
                    put(KEY_DSCR, book.description.value)
                    put(KEY_VLM, book.volume.value)
                    put(KEY_PBL_DT, book.publicationDate.value)
                    put(KEY_PGS, book.pages.value)
                    put(KEY_PRC, book.price.value)
                    put(KEY_VL, book.value.value)
                    put(KEY_DUE_DT, book.dueDate.value)
                    put(KEY_RD_DT, book.readDate.value)
                    put(KEY_EDN, book.edition.value)
                    put(KEY_ISBN, book.isbn.value)
                    put(KEY_WEB, book.web.value)
                }

                val bookId = insert(TABLE_BOOKS, null, values)

                for (i in 0 until book.properties.size) {
                    val property = book.properties[i]
                    if (property.id == 0L) {
                        val pValues = ContentValues().apply {
                            put(KEY_TP_ID, property.fieldTypeId)
                            put(KEY_NM, property.value)
                        }
                        property.id = insert(TABLE_FIELDS, null, pValues)
                    }
                }

                for (property in book.properties) {
                    val fValues = ContentValues().apply {
                        put(KEY_FLD_ID, property.id)
                        put(KEY_BK_ID, bookId)
                    }
                    insert(TABLE_BOOK_FIELDS, null, fValues)
                }

            } finally {
            }
        }
        shrink()
    }

    fun getPropertyValues(typeId: Int): ArrayList<Property> {
        return getPropertyValues(typeId, false)
    }

    fun getPropertyValues(typeId: Int, isOrdered: Boolean): ArrayList<Property> {
        val propertyValues = ArrayList<Property>()
        val database = db ?: return propertyValues

        var sql = "SELECT f.$KEY_ID, f.$KEY_TP_ID, f.$KEY_NM" +
                " FROM $TABLE_FIELDS as f " +
                " WHERE f.$KEY_TP_ID = $typeId"

        if (isOrdered) {
            sql += " ORDER BY f.$KEY_NM"
        }

        database.rawQuery(sql, null).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val property = Property(
                        id = cursor.getLong(ID_KEY_ID),
                        fieldTypeId = cursor.getInt(ID_KEY_TP_ID),
                        value = cursor.getString(ID_KEY_NM).orEmpty()
                    )
                    propertyValues.add(property)
                } while (cursor.moveToNext())
            }
        }

        return propertyValues
    }

    fun getBook(bookId: Long): Book? {
        val database = db ?: return null
        var book: Book? = null

        database.query(
            TABLE_BOOKS,
            null,
            "$KEY_ID = $bookId",
            null,
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                book = Book(
                    cursor.getInt(ID_KEY_ID),
                    cursor.getString(ID_KEY_TTL).orEmpty(),
                    cursor.getString(ID_KEY_DSCR).orEmpty(),
                    if (cursor.isNull(ID_KEY_VLM)) 0 else cursor.getInt(ID_KEY_VLM),
                    if (cursor.isNull(ID_KEY_PBL_DT)) 0 else cursor.getInt(ID_KEY_PBL_DT),
                    if (cursor.isNull(ID_KEY_PGS)) 0 else cursor.getInt(ID_KEY_PGS),
                    cursor.getString(ID_KEY_PRC).orEmpty(),
                    cursor.getString(ID_KEY_VL).orEmpty(),
                    if (cursor.isNull(ID_KEY_DUE_DT)) 0 else cursor.getInt(ID_KEY_DUE_DT),
                    if (cursor.isNull(ID_KEY_RD_DT)) 0 else cursor.getInt(ID_KEY_RD_DT),
                    if (cursor.isNull(ID_KEY_EDN)) 0 else cursor.getInt(ID_KEY_EDN),
                    cursor.getString(ID_KEY_ISBN).orEmpty(),
                    cursor.getString(ID_KEY_WEB).orEmpty()
                )
            }
        }

        val sql = "SELECT f.$KEY_ID, f.$KEY_TP_ID, f.$KEY_NM" +
                " FROM $TABLE_BOOK_FIELDS as bf LEFT JOIN $TABLE_FIELDS AS f ON bf.$KEY_FLD_ID = f.$KEY_ID" +
                " WHERE bf.$KEY_BK_ID = $bookId"

        database.rawQuery(sql, null).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val property = Property(
                        id = cursor.getLong(ID_KEY_ID),
                        fieldTypeId = cursor.getInt(ID_KEY_TP_ID),
                        value = cursor.getString(ID_KEY_NM).orEmpty()
                    )
                    book?.properties?.add(property)
                } while (cursor.moveToNext())
            }
        }

        return book
    }

    fun deleteBook(bookId: Long) {
        val database = db ?: return
        database.transaction {
            try {
                delete(TABLE_BOOK_FIELDS, "$KEY_BK_ID = $bookId", null)
                delete(TABLE_BOOKS, "$KEY_ID = $bookId", null)
            } catch (e: Exception) {
                Log.e(TAG, e.message ?: "")
            }
        }
        shrink()
    }

    fun updateBook(book: Book) {
        val database = db ?: return
        database.transaction {
            try {
                for (property in book.properties) {
                    if (property.id == 0L) {
                        val pValues = ContentValues().apply {
                            put(KEY_TP_ID, property.fieldTypeId)
                            put(KEY_NM, property.value)
                        }
                        property.id = insert(TABLE_FIELDS, null, pValues)
                    }
                }

                delete(TABLE_BOOK_FIELDS, "$KEY_BK_ID = ${book.id}", null)

                for (property in book.properties) {
                    val fValues = ContentValues().apply {
                        put(KEY_FLD_ID, property.id)
                        put(KEY_BK_ID, book.id)
                    }
                    insert(TABLE_BOOK_FIELDS, null, fValues)
                }

                val values = ContentValues().apply {
                    put(KEY_TTL, book.title.value)
                    put(KEY_DSCR, book.description.value)
                    put(KEY_VLM, book.volume.value)
                    put(KEY_PBL_DT, book.publicationDate.value)
                    put(KEY_PGS, book.pages.value)
                    put(KEY_PRC, book.price.value)
                    put(KEY_VL, book.value.value)
                    put(KEY_DUE_DT, book.dueDate.value)
                    put(KEY_RD_DT, book.readDate.value)
                    put(KEY_EDN, book.edition.value)
                    put(KEY_ISBN, book.isbn.value)
                    put(KEY_WEB, book.web.value)
                }
                update(TABLE_BOOKS, values, "$KEY_ID = ${book.id}", null)

            } catch (e: Exception) {
                Log.e(TAG, e.message ?: "")
            }
        }
        shrink()
    }

    private fun shrink() {
        db?.execSQL("VACUUM")
    }

    fun importDatabase(dbPath: String): Boolean {
        dbHelper.close()
        val newDb = File(dbPath)
        val oldDb = context.getDatabasePath(DATABASE_NAME)
        if (newDb.exists()) {
            try {
                FileUtils.copyFile(FileInputStream(newDb), FileOutputStream(oldDb))
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, e.message ?: "")
                return false
            }
            dbHelper.writableDatabase.close()
            return true
        }
        return false
    }

    fun exportDatabase(dbPath: String): Boolean {
        dbHelper.close()
        val newDb = File(dbPath)
        val oldDb = context.getDatabasePath(DATABASE_NAME)
        return try {
            FileUtils.copyFile(FileInputStream(oldDb), FileOutputStream(newDb))
            true
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, e.message ?: "")
            false
        }
    }

    private class DBOpenHelper(
        private val context: Context,
        name: String,
        factory: CursorFactory?,
        version: Int
    ) : SQLiteOpenHelper(context, name, factory, version) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(CREATE_TABLE_BOOKS)
            db.execSQL(CREATE_TABLE_FIELDS)
            db.execSQL(CREATE_TABLE_BOOK_FIELDS)

            db.transaction {
                try {
                    var typeId: Int
                    var valuesId: Int
                    var fieldId: Int
                    var fieldName: String
                    var valuesArray: Array<String>
                    var fieldTypedArray: TypedArray
                    var values: ContentValues

                    val fieldsValuesTypedArray =
                        context.resources.obtainTypedArray(R.array.fields_values)
                    for (i in 0 until fieldsValuesTypedArray.length()) {
                        fieldId = fieldsValuesTypedArray.getResourceId(i, -1)
                        fieldTypedArray = context.resources.obtainTypedArray(fieldId)
                        typeId = fieldTypedArray.getInt(0, -1)
                        val index = 1
                        valuesId = fieldTypedArray.getResourceId(index, -1)
                        valuesArray = context.resources.getStringArray(valuesId)
                        fieldName = context.resources.getResourceEntryName(valuesId)
                        for (sValue in valuesArray) {
                            values = ContentValues()
                            values.put(KEY_TP_ID, typeId)
                            values.put(KEY_NM, sValue)
                            if (insert(TABLE_FIELDS, null, values) < 0) {
                                throw RuntimeException(
                                    context.resources.getString(
                                        R.string.err_db,
                                        fieldName,
                                        typeId,
                                        sValue
                                    )
                                )
                            }
                        }
                        fieldTypedArray.recycle()
                    }

                    fieldsValuesTypedArray.recycle() // Important!
                } catch (e: RuntimeException) {
                    Log.e("TaskDBAdapter", e.message ?: "")
                } finally {
                }
            }
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            Log.w("TaskDBAdapter", "Upgrade from version $oldVersion to $newVersion, which will destroy all old data")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_BOOKS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_FIELDS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_BOOK_FIELDS")
            onCreate(db)
        }
    }

    companion object {
        const val DATABASE_NAME = "book_bag.db"
        const val TAG = "DB"
        const val MISSING = "\"(missing)\""
        const val DATABASE_VERSION = 1
        const val TABLE_BOOKS = "books"
        const val TABLE_FIELDS = "fields"
        const val TABLE_BOOK_FIELDS = "book_fields"

        const val KEY_ID = "_id"
        const val KEY_TTL = "title"
        const val KEY_DSCR = "description"
        const val KEY_VLM = "volume"
        const val KEY_PBL_DT = "publication_date"
        const val KEY_PGS = "pages"
        const val KEY_PRC = "price"
        const val KEY_VL = "value"
        const val KEY_DUE_DT = "due_date"
        const val KEY_RD_DT = "read_date"
        const val KEY_EDN = "edition"
        const val KEY_ISBN = "isbn"
        const val KEY_WEB = "web"

        const val KEY_NM = "name"
        const val KEY_TP_ID = "type_id"

        const val KEY_BK_ID = "book_id"
        const val KEY_FLD_ID = "field_id"

        const val CREATE_TABLE_BOOKS = ("CREATE TABLE " + TABLE_BOOKS + " ("
                + KEY_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                + KEY_TTL + " TEXT NOT NULL, "
                + KEY_DSCR + " TEXT, "
                + KEY_VLM + " INTEGER, "
                + KEY_PBL_DT + " INTEGER, "
                + KEY_PGS + " INTEGER, "
                + KEY_PRC + " TEXT, "
                + KEY_VL + " TEXT, "
                + KEY_DUE_DT + " INTEGER, "
                + KEY_RD_DT + " INTEGER, "
                + KEY_EDN + " INTEGER, "
                + KEY_ISBN + " TEXT, "
                + KEY_WEB + " TEXT)")

        const val ID_KEY_ID = 0
        const val ID_KEY_TTL = 1
        const val ID_KEY_DSCR = 2
        const val ID_KEY_VLM = 3
        const val ID_KEY_PBL_DT = 4
        const val ID_KEY_PGS = 5
        const val ID_KEY_PRC = 6
        const val ID_KEY_VL = 7
        const val ID_KEY_DUE_DT = 8
        const val ID_KEY_RD_DT = 9
        const val ID_KEY_EDN = 10
        const val ID_KEY_ISBN = 11
        const val ID_KEY_WEB = 12
        const val ID_KEY_TP_ID = 1
        const val ID_KEY_NM = 2

        const val CREATE_TABLE_FIELDS = ("CREATE TABLE " + TABLE_FIELDS + " ("
                + KEY_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                + KEY_TP_ID + " INTEGER, "
                + KEY_NM + " TEXT NOT NULL)")

        const val CREATE_TABLE_BOOK_FIELDS = ("CREATE TABLE " + TABLE_BOOK_FIELDS + " ("
                + KEY_BK_ID + " INTEGER, "
                + KEY_FLD_ID + " INTEGER)")

        @JvmField
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

        @JvmField
        val FIELDS = ArrayList<Field>()

        const val QR_TTL = ("SELECT UPPER(SUBSTR(b." + KEY_TTL + ", 1, 1)) AS parent, b." + KEY_ID + " AS child_id, COALESCE(b." + KEY_TTL + " || \" - \" || GROUP_CONCAT(f_name, \", \"), b." + KEY_TTL + ") AS child"
                + " FROM " + TABLE_BOOKS + " AS b"
                + " LEFT JOIN "
                + "(SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name"
                + " FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f._id = bf." + KEY_FLD_ID
                + " WHERE f." + KEY_TP_ID + " = " + FLD_AUTHOR + ") AS a on a.bf_book_id = b._id"
                + " GROUP BY b." + KEY_ID
                + " ORDER BY parent, child")

        const val QR_AUT = ("SELECT IFNULL(GROUP_CONCAT(f_name, \", \"), " + MISSING + ") AS parent, b." + KEY_ID + " AS child_id, b." + KEY_TTL + " AS child"
                + " FROM " + TABLE_BOOKS
                + " AS b LEFT JOIN"
                + " (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                + " WHERE f." + KEY_TP_ID + " = " + FLD_AUTHOR + ") AS a"
                + " ON a.bf_book_id = b." + KEY_ID
                + " GROUP BY b." + KEY_ID
                + " ORDER BY parent, child")

        const val QR_WNT_TTL = ("SELECT IFNULL(p.f_name, " + MISSING + ") AS parent, b." + KEY_ID + " AS child_id, COALESCE(b." + KEY_TTL + " || \" - \" || GROUP_CONCAT(a.f_name, \", \"), b." + KEY_TTL + ") AS child"
                + " FROM " + TABLE_BOOKS + " AS b LEFT JOIN"
                + " (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                + " WHERE f." + KEY_TP_ID + " = " + FLD_AUTHOR + ") AS a"
                + " ON a.bf_book_id = b." + KEY_ID
                + " JOIN (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                + " WHERE f." + KEY_TP_ID + " = " + FLD_STATUS + ") AS p"
                + " ON p.bf_book_id = b." + KEY_ID
                + " WHERE LOWER(p.f_name) != LOWER('In Bag') AND LOWER(p.f_name) != LOWER('Read')"
                + " GROUP BY b." + KEY_ID
                + " ORDER BY parent, child")

        const val QR_WNT_AUT = ("SELECT IFNULL(p.f_name, " + MISSING + ") AS parent, b." + KEY_ID + " AS child_id, COALESCE(GROUP_CONCAT(a.f_name, \", \") || \" - \" || b." + KEY_TTL + ", b." + KEY_TTL + ") AS child"
                + " FROM " + TABLE_BOOKS + " AS b LEFT JOIN"
                + " (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                + " WHERE f." + KEY_TP_ID + " = " + FLD_AUTHOR + ") AS a"
                + " ON a.bf_book_id = b." + KEY_ID
                + " JOIN (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                + " WHERE f." + KEY_TP_ID + " = " + FLD_STATUS + ") AS p"
                + " ON p.bf_book_id = b." + KEY_ID
                + " WHERE LOWER(p.f_name) != LOWER('In Bag') AND LOWER(p.f_name) != LOWER('Read')"
                + " GROUP BY b." + KEY_ID
                + " ORDER BY parent, child")

        const val QR_RD_AUT = ("SELECT IFNULL(GROUP_CONCAT(a.f_name, \", \"), " + MISSING + ") AS parent, b." + KEY_ID + " AS child_id, b." + KEY_TTL + " AS child"
                + " FROM " + TABLE_BOOKS + " AS b LEFT JOIN"
                + " (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                + " WHERE f." + KEY_TP_ID + " = " + FLD_AUTHOR + ") AS a"
                + " ON a.bf_book_id = b." + KEY_ID
                + " JOIN (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                + " WHERE f." + KEY_TP_ID + " = " + FLD_STATUS + ") AS p"
                + " ON p.bf_book_id = b." + KEY_ID
                + " WHERE LOWER(p.f_name) = LOWER('Read')"
                + " GROUP BY b." + KEY_ID
                + " ORDER BY parent, child")

        const val QR_RD_TTL = ("SELECT UPPER(SUBSTR(b." + KEY_TTL + ", 1, 1)) AS parent, b." + KEY_ID + " AS child_id, COALESCE(b." + KEY_TTL + " || \" - \" || GROUP_CONCAT(a.f_name, \", \"), b." + KEY_TTL + ") AS child"
                + " FROM " + TABLE_BOOKS + " AS b LEFT JOIN"
                + " (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                + " WHERE f." + KEY_TP_ID + " = " + FLD_AUTHOR + ") AS a"
                + " ON a.bf_book_id = b." + KEY_ID
                + " JOIN (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                + " WHERE f." + KEY_TP_ID + " = " + FLD_STATUS + ") AS p"
                + " ON p.bf_book_id = b." + KEY_ID
                + " WHERE LOWER(p.f_name) = LOWER('Read')"
                + " GROUP BY b." + KEY_ID
                + " ORDER BY parent, child")

        const val QR_NOT_RD_AUT = ("SELECT IFNULL(GROUP_CONCAT(a.f_name, \", \"), " + MISSING + ") AS parent, b." + KEY_ID + " AS child_id, b." + KEY_TTL + " AS child"
                + " FROM " + TABLE_BOOKS + " AS b LEFT JOIN"
                + " (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                + " WHERE f." + KEY_TP_ID + " = " + FLD_AUTHOR + ") AS a"
                + " ON a.bf_book_id = b." + KEY_ID
                + " JOIN (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                + " WHERE f." + KEY_TP_ID + " = " + FLD_STATUS + ") AS p"
                + " ON p.bf_book_id = b." + KEY_ID
                + " WHERE LOWER(p.f_name) != LOWER('Read')"
                + " GROUP BY b." + KEY_ID
                + " ORDER BY parent, child")

        const val QR_NOT_RD_TTL = ("SELECT UPPER(SUBSTR(b." + KEY_TTL + ", 1, 1)) AS parent, b." + KEY_ID + " AS child_id, COALESCE(b." + KEY_TTL + " || \" - \" || GROUP_CONCAT(a.f_name, \", \"), b." + KEY_TTL + ") AS child"
                + " FROM " + TABLE_BOOKS + " AS b LEFT JOIN"
                + " (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                + " WHERE f." + KEY_TP_ID + " = " + FLD_AUTHOR + ") AS a"
                + " ON a.bf_book_id = b." + KEY_ID
                + " JOIN (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                + " WHERE f." + KEY_TP_ID + " = " + FLD_STATUS + ") AS p"
                + " ON p.bf_book_id = b." + KEY_ID
                + " WHERE LOWER(p.f_name) != LOWER('Read')"
                + " GROUP BY b." + KEY_ID
                + " ORDER BY parent, child")

        const val QR_PBL_AUT = ("SELECT IFNULL(p.f_name, " + MISSING + ") AS parent, b." + KEY_ID + " AS child_id, COALESCE(GROUP_CONCAT(a.f_name, \", \") || \" - \" || b." + KEY_TTL + ", b." + KEY_TTL + ") AS child"
                + " FROM " + TABLE_BOOKS + " AS b LEFT JOIN"
                + " (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                + " WHERE f." + KEY_TP_ID + " = " + FLD_AUTHOR + ") AS a"
                + " ON a.bf_book_id = b." + KEY_ID
                + " JOIN (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                + " WHERE f." + KEY_TP_ID + " = " + FLD_PUBLISHER + ") AS p"
                + " ON p.bf_book_id = b." + KEY_ID
                + " GROUP BY b." + KEY_ID
                + " ORDER BY parent, child")

        const val QR_PBL_TTL = ("SELECT IFNULL(p.f_name, " + MISSING + ") AS parent, b." + KEY_ID + " AS child_id, COALESCE(b." + KEY_TTL + " || \" - \" || GROUP_CONCAT(a.f_name, \", \"), b." + KEY_TTL + ") AS child"
                + " FROM " + TABLE_BOOKS + " AS b LEFT JOIN"
                + " (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                + " WHERE f." + KEY_TP_ID + " = " + FLD_AUTHOR + ") AS a"
                + " ON a.bf_book_id = b." + KEY_ID
                + " JOIN (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                + " WHERE f." + KEY_TP_ID + " = " + FLD_PUBLISHER + ") AS p"
                + " ON p.bf_book_id = b." + KEY_ID
                + " GROUP BY b." + KEY_ID
                + " ORDER BY parent, child")

        const val QR_LND_TTL = ("SELECT UPPER(SUBSTR(b." + KEY_TTL + ", 1, 1)) AS parent, b." + KEY_ID + " AS child_id, COALESCE(b." + KEY_TTL + " || \" - \" || GROUP_CONCAT(a.f_name, \", \"), b." + KEY_TTL + ") AS child"
                + " FROM " + TABLE_BOOKS + " AS b LEFT JOIN"
                + " (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                + " WHERE f." + KEY_TP_ID + " = " + FLD_AUTHOR + ") AS a"
                + " ON a.bf_book_id = b." + KEY_ID
                + " JOIN (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                + " WHERE f." + KEY_TP_ID + " = " + FLD_STATUS + ") AS p"
                + " ON p.bf_book_id = b." + KEY_ID
                + " WHERE LOWER(p.f_name) = LOWER('Loaned')"
                + " GROUP BY b." + KEY_ID
                + " ORDER BY parent, child")

        const val QR_LND_BRW = ("SELECT IFNULL(l.f_name, " + MISSING + ") AS parent, b." + KEY_ID + " AS child_id, COALESCE(b." + KEY_TTL + " || \" - \" || GROUP_CONCAT(a.f_name, \", \"), b." + KEY_TTL + ") AS child"
                + " FROM " + TABLE_BOOKS + " AS b LEFT JOIN"
                + " (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                + " WHERE f." + KEY_TP_ID + " = " + FLD_AUTHOR + ") AS a"
                + " ON a.bf_book_id = b." + KEY_ID
                + " JOIN (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                + " WHERE f." + KEY_TP_ID + " = " + FLD_STATUS + ") AS p"
                + " ON p.bf_book_id = b." + KEY_ID
                + " JOIN (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                + " WHERE f." + KEY_TP_ID + " = " + FLD_LOANED_TO + ") AS l"
                + " ON l.bf_book_id = b." + KEY_ID
                + " WHERE LOWER(p.f_name) = LOWER('Loaned')"
                + " GROUP BY b." + KEY_ID
                + " ORDER BY parent, child")
    }
}
