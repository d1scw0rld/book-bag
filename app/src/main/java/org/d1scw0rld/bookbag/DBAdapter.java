package org.d1scw0rld.bookbag;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.text.InputType;
import android.util.Log;

import org.d1scw0rld.bookbag.dto.Book;
import org.d1scw0rld.bookbag.dto.Property;
import org.d1scw0rld.bookbag.dto.Field;
import org.d1scw0rld.bookbag.dto.FileUtils;
import org.d1scw0rld.bookbag.dto.ParentResult;
import org.d1scw0rld.bookbag.dto.Result;

import androidx.annotation.StyleableRes;

public class DBAdapter
{
	static final String DATABASE_NAME = "book_bag.db";

	private static final String TAG = "DB";
	
	private static final String MISSING = "\"(missing)\"";

	private static final int DATABASE_VERSION = 1;

   private static final String TABLE_BOOKS = "books";
   private static final String TABLE_FIELDS = "fields";
   private static final String TABLE_BOOK_FIELDS = "book_fields";

   // Common column names
   private static final String KEY_ID = "_id";

   // BOOKS column names
   private static final String KEY_TTL = "title";
   private static final String KEY_DSCR = "description";
   private static final String KEY_VLM = "volume";
   private static final String KEY_PBL_DT = "publication_date";
   private static final String KEY_PGS = "pages";
   private static final String KEY_PRC = "price";
   private static final String KEY_VL = "value";
   private static final String KEY_DUE_DT = "due_date";
   private static final String KEY_RD_DT = "read_date";
   private static final String KEY_EDN = "edition";
   private static final String KEY_ISBN = "isbn";
   private static final String KEY_WEB = "web";

   // FIELDS column names
   private static final String KEY_NM = "name";
   private static final String KEY_TP_ID = "type_id";

   // BOOK_FIELDS column names
   private static final String KEY_BK_ID = "book_id";
   private static final String KEY_FLD_ID = "field_id";

   // Table Create Statements
   // BOOKS table create statement
   private static final String CREATE_TABLE_BOOKS = "CREATE TABLE " + TABLE_BOOKS + " ("
         + KEY_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
         + KEY_TTL + " TEXT, "
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
         + KEY_WEB + " TEXT)";

   private static final int ID_KEY_ID = 0,
                            ID_KEY_TTL = 1,
                            ID_KEY_DSCR = 2,
                            ID_KEY_VLM = 3,
                            ID_KEY_PBL_DT = 4,
                            ID_KEY_PGS = 5,
                            ID_KEY_PRC = 6,
                            ID_KEY_VL = 7,
                            ID_KEY_DUE_DT = 8,
                            ID_KEY_RD_DT = 9,
                            ID_KEY_EDN = 10,
                            ID_KEY_ISBN = 11,
                            ID_KEY_WEB = 12,
                            ID_KEY_TP_ID = 1,
                            ID_KEY_NM = 2;

   // FIELDS table create statement
   private static final String CREATE_TABLE_FIELDS = "CREATE TABLE " + TABLE_FIELDS + " ("
            + KEY_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
            + KEY_TP_ID + " INTEGER, "
            + KEY_NM + " TEXT)";

   // BOOK_FIELDS table create statement
   private static final String CREATE_TABLE_BOOK_FIELDS = "CREATE TABLE " + TABLE_BOOK_FIELDS + " ("
         + KEY_BK_ID + " INTEGER, "
         + KEY_FLD_ID + " INTEGER)";
//         + KEY_TP_ID + " INTEGER)";
   
	private SQLiteDatabase db;
	private final Context context;
	private final DBOpenHelper dbHelper;
	
	public final static char separator = DecimalFormatSymbols.getInstance().getDecimalSeparator();

   final static int FLD_AUTHOR = 1,
                    FLD_SERIE = 2,
                    FLD_GENRE = 3,
                    FLD_LANGUAGE = 4,
                    FLD_PUBLISHER = 5,
                    FLD_PUBLICATION_LOCATION = 6,
                    FLD_STATUS = 7,
                    FLD_RATING = 8,
                    FLD_FORMAT = 9,
                    FLD_LOCATION = 10,
                    FLD_CONDITION = 11,
                    FLD_CURRENCY = 12,
                    FLD_READ = 13,
                    FLD_LOANED_TO = 14,
                    FLD_TITLE = 99,
                    FLD_DESCRIPTION = 100,
                    FLD_VOLUME = 101,
                    FLD_PUBLICATION_DATE = 102,
                    FLD_PAGES = 103,
                    FLD_PRICE = 104,
                    FLD_VALUE = 105,
                    FLD_DUE_DATE = 106,
                    FLD_READ_DATE = 107,
                    FLD_EDITION = 108,
                    FLD_ISBN = 109,
                    FLD_WEB = 110;
//                            FLD_PRICE_CURRENCY = 111,
//                            FLD_VALUE_CURRENCY = 112;

   final static int SRT_TTL = 1,
                    SRT_AUT = 2,
                    SRT_WNT_PBL_AUT = 3,
                    SRT_WNT_PBL_TTL = 4,
                    SRT_RD_AUT = 5,
                    SRT_NOT_RD_AUT = 6,
                    SRT_NOT_RD_TTL = 7,
                    SRT_RD_TTL = 8,
                    SRT_PBL_AUT = 9,
                    SRT_PBL_TTL = 10,
                    SRT_LND_TTL = 11,
                    SRT_LND_BRW = 12;

   final static ArrayList<Field> FIELDS = new ArrayList<>();
   
	DBAdapter(Context context)
	{
		this.context = context;
		dbHelper = new DBOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
		Resources resources = context.getResources();
		FIELDS.clear();
		FIELDS.add(new Field(FLD_TITLE, resources.getString(R.string.fld_title), Field.TYPE_TEXT).setVisibility(true));
		FIELDS.add(new Field(FLD_AUTHOR, resources.getString(R.string.fld_author), Field.TYPE_MULTIFIELD).setVisibility(true));
		FIELDS.add(new Field(FLD_DESCRIPTION, resources.getString(R.string.fld_descrition), Field.TYPE_TEXT).setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE).setVisibility(false));
		FIELDS.add(new Field(FLD_SERIE, resources.getString(R.string.fld_serie), Field.TYPE_TEXT_AUTOCOMPLETE));
		FIELDS.add(new Field(FLD_VOLUME, resources.getString(R.string.fld_volume), Field.TYPE_TEXT).setInputType(InputType.TYPE_CLASS_NUMBER));
		FIELDS.add(new Field(FLD_GENRE, resources.getString(R.string.fld_genre), Field.TYPE_MULTI_SPINNER));
		FIELDS.add(new Field(FLD_LANGUAGE, resources.getString(R.string.fld_language), Field.TYPE_SPINNER));
		FIELDS.add(new Field(FLD_PAGES, resources.getString(R.string.fld_pages), Field.TYPE_TEXT).setInputType(InputType.TYPE_CLASS_NUMBER));
		FIELDS.add(new Field(FLD_PUBLISHER, resources.getString(R.string.fld_publisher), Field.TYPE_TEXT_AUTOCOMPLETE));
		FIELDS.add(new Field(FLD_PUBLICATION_DATE, resources.getString(R.string.fld_publication_date), Field.TYPE_TEXT).setInputType(InputType.TYPE_CLASS_NUMBER));
		FIELDS.add(new Field(FLD_PUBLICATION_LOCATION, resources.getString(R.string.fld_publication_location), Field.TYPE_TEXT_AUTOCOMPLETE));
		FIELDS.add(new Field(FLD_EDITION, resources.getString(R.string.fld_edition), Field.TYPE_TEXT).setInputType(InputType.TYPE_CLASS_NUMBER));
		FIELDS.add(new Field(FLD_PRICE, resources.getString(R.string.fld_price), Field.TYPE_MONEY).setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL));
        FIELDS.add(new Field(FLD_VALUE, resources.getString(R.string.fld_value), Field.TYPE_MONEY).setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL));
        FIELDS.add(new Field(FLD_STATUS, resources.getString(R.string.fld_status), Field.TYPE_SPINNER));
		FIELDS.add(new Field(FLD_LOANED_TO, resources.getString(R.string.fld_loaned_to), Field.TYPE_TEXT_AUTOCOMPLETE));
		FIELDS.add(new Field(FLD_READ, resources.getString(R.string.fld_read), Field.TYPE_CHECK_BOX));
		FIELDS.add(new Field(FLD_READ_DATE, resources.getString(R.string.fld_read_date), Field.TYPE_DATE));
		FIELDS.add(new Field(FLD_RATING, resources.getString(R.string.fld_rating), Field.TYPE_RATING));
		FIELDS.add(new Field(FLD_FORMAT, resources.getString(R.string.fld_format), Field.TYPE_SPINNER));
        FIELDS.add(new Field(FLD_CONDITION, resources.getString(R.string.fld_condition), Field.TYPE_SPINNER));
		FIELDS.add(new Field(FLD_LOCATION, resources.getString(R.string.fld_location), Field.TYPE_TEXT_AUTOCOMPLETE));
		FIELDS.add(new Field(FLD_DUE_DATE, resources.getString(R.string.fld_due_date), Field.TYPE_DATE));
		FIELDS.add(new Field(FLD_ISBN, resources.getString(R.string.fld_isbn), Field.TYPE_TEXT).setInputType(InputType.TYPE_CLASS_NUMBER));
		FIELDS.add(new Field(FLD_WEB, resources.getString(R.string.fld_web), Field.TYPE_TEXT));
	}

	void open() throws SQLiteException
	{
		try
		{
			db = dbHelper.getWritableDatabase();
		}
		catch(SQLiteException ex)
		{
			db = dbHelper.getReadableDatabase();
		}
	}

	void close()
	{
		db.close();
	}

   private ArrayList<ParentResult> getBooksOrderedBy(String query)
   {
      if(Debug.ON)
      {
         return null;
      }
      else
      {
         ArrayList<ParentResult> parentResults = new ArrayList<>();
   
         Cursor cursor = db.rawQuery(query, null);
         
         if(cursor.moveToFirst())
         {
            Result result;
            String parent = cursor.getString(0);
            List<Result> childResults = new ArrayList<>();
            ParentResult parentResult = new ParentResult(parent, childResults);

            do
            {
               parent = cursor.getString(0);
               result = new Result();
               result.id = Integer.parseInt(cursor.getString(1));
               result.content = cursor.getString(2);
               if(!parent.equalsIgnoreCase(parentResult.getName()))
               {
                  parentResults.add(parentResult);
                  childResults = new ArrayList<>();
                  parentResult = new ParentResult(parent, childResults);
               }
               childResults.add(result);
            } while (cursor.moveToNext());
            parentResults.add(parentResult);
         }
         cursor.close();
   
         return parentResults;
      }      
   }
   
   private final static String QR_TTL = "SELECT UPPER(SUBSTR(b." + KEY_TTL + ", 1, 1)) AS parent, b." + KEY_ID + " AS child_id, COALESCE(b." + KEY_TTL + " || \" - \" || GROUP_CONCAT(f_name, \", \"), b." + KEY_TTL + ") AS child"
                                      + " FROM " + TABLE_BOOKS + " AS b"
                                      + " LEFT JOIN "
                                      + "(SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name"
                                      + " FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f._id = bf." + KEY_FLD_ID 
                                      + " WHERE f." + KEY_TP_ID + " = " + FLD_AUTHOR + ") AS a on a.bf_book_id = b._id"
                                      + " GROUP BY b." + KEY_ID 
                                      + " ORDER BY parent, child",
  
                               QR_AUT = "SELECT IFNULL(GROUP_CONCAT(f_name, \", \"), " + MISSING + ") AS parent, b." + KEY_ID + " AS child_id, b." + KEY_TTL + " AS child"
                                      + " FROM " + TABLE_BOOKS 
                                      + " AS b LEFT JOIN"
                                      + " (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID 
                                      + " WHERE f." + KEY_TP_ID + " = " + FLD_AUTHOR + ") AS a"
                                      + " ON a.bf_book_id = b." + KEY_ID 
                                      + " GROUP BY b." + KEY_ID 
                                      + " ORDER BY parent, child",
   
                               QR_WNT_TTL = "SELECT IFNULL(p.f_name, " + MISSING + ") AS parent, b." + KEY_ID + " AS child_id, COALESCE(b." + KEY_TTL + " || \" - \" || GROUP_CONCAT(a.f_name, \", \"), b." + KEY_TTL + ") AS child"
                                          + " FROM " + TABLE_BOOKS
                                          + " AS b LEFT JOIN"
                                          + " (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                                          + " WHERE f." + KEY_TP_ID + " = " + FLD_PUBLISHER + ") AS p"
                                          + " ON p.bf_book_id = b." + KEY_ID
                                          + " LEFT JOIN"
                                          + " (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                                          + " WHERE f." + KEY_TP_ID + " = " + FLD_AUTHOR + ") AS a"
                                          + " ON a.bf_book_id = b." + KEY_ID
                                          + " JOIN " + TABLE_BOOK_FIELDS + " as bf ON b." + KEY_ID + " = bf." + KEY_BK_ID
                                          + " JOIN " + TABLE_FIELDS + " as f ON bf." + KEY_FLD_ID + " = f." + KEY_ID
                                          + " WHERE f." + KEY_TP_ID + " = " + FLD_STATUS + " AND f." + KEY_NM + " = \"Wanted\""
                                          + " GROUP BY b." + KEY_ID
                                          + " ORDER BY parent, child",
   
                               QR_WNT_AUT = "SELECT IFNULL(p.f_name, " + MISSING + ") AS parent, b." + KEY_ID + " AS child_id, COALESCE(GROUP_CONCAT(a.f_name, \", \") || \" - \" || b." + KEY_TTL + ", b." + KEY_TTL + ") AS child"
                                          + " FROM " + TABLE_BOOKS
                                          + " AS b LEFT JOIN"
                                          + " (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                                          + " WHERE f." + KEY_TP_ID + " = " + FLD_PUBLISHER + ") AS p"
                                          + " ON p.bf_book_id = b." + KEY_ID
                                          + " LEFT JOIN"
                                          + " (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                                          + " WHERE f." + KEY_TP_ID + " = " + FLD_AUTHOR + ") AS a"
                                          + " ON a.bf_book_id = b." + KEY_ID
                                          + " JOIN " + TABLE_BOOK_FIELDS + " as bf ON b." + KEY_ID + " = bf." + KEY_BK_ID
                                          + " JOIN " + TABLE_FIELDS + " as f ON bf." + KEY_FLD_ID + " = f." + KEY_ID
                                          + " WHERE f." + KEY_TP_ID + " = " + FLD_STATUS + " AND f." + KEY_NM + " = \"Wanted\""
                                          + " GROUP BY b." + KEY_ID
                                          + " ORDER BY parent, child",
   
                               QR_RD_AUT = "SELECT IFNULL(GROUP_CONCAT(a.f_name, \", \"), " + MISSING + ") AS parent, b." + KEY_ID + " AS child_id, b." + KEY_TTL + " AS child"
                                         + " FROM " + TABLE_BOOKS + " AS b"
                                         + " LEFT JOIN"
                                         + " (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                                         + " WHERE f." + KEY_TP_ID + " = " + FLD_AUTHOR + ") AS a"
                                         + " ON a.bf_book_id = b." + KEY_ID
                                         + " JOIN " + TABLE_BOOK_FIELDS + " as bf ON b." + KEY_ID + " = bf." + KEY_BK_ID
                                         + " JOIN " + TABLE_FIELDS + " as f ON bf." + KEY_FLD_ID + " = f." + KEY_ID
                                         + " WHERE f." + KEY_TP_ID + " = " + FLD_READ + " AND f." + KEY_NM + " = \"true\""
                                         + " GROUP BY b." + KEY_ID
                                         + " ORDER BY parent, child",

                               QR_RD_TTL = "SELECT UPPER(SUBSTR(b." + KEY_TTL + ", 1, 1)) AS parent, b." + KEY_ID + " AS child_id, COALESCE(b." + KEY_TTL + " || \" - \" || GROUP_CONCAT(a.f_name, \", \"), b." + KEY_TTL + ") AS child"
                                         + " FROM " + TABLE_BOOKS + " AS b"
                                         + " LEFT JOIN"
                                         + " (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                                         + " WHERE f." + KEY_TP_ID + " = " + FLD_AUTHOR + ") AS a"
                                         + " ON a.bf_book_id = b." + KEY_ID
                                         + " JOIN " + TABLE_BOOK_FIELDS + " as bf ON b." + KEY_ID + " = bf." + KEY_BK_ID
                                         + " JOIN " + TABLE_FIELDS + " as f ON bf." + KEY_FLD_ID + " = f." + KEY_ID
                                         + " WHERE f." + KEY_TP_ID + " = " + FLD_READ + " AND f." + KEY_NM + " = \"true\""
                                         + " GROUP BY b." + KEY_ID
                                         + " ORDER BY parent, child",
                              
                              QR_NOT_RD_AUT = "SELECT IFNULL(GROUP_CONCAT(a.f_name, \", \"), " + MISSING + ") AS parent, b." + KEY_ID + " AS child_id, b." + KEY_TTL + " AS child"
                                            + " FROM " + TABLE_BOOKS + " AS b"
                                            + " LEFT JOIN"
                                            + " (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                                            + " WHERE f." + KEY_TP_ID + " = " + FLD_AUTHOR + ") AS a"
                                            + " ON a.bf_book_id = b." + KEY_ID
                                            + " LEFT JOIN"
                                            + " (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                                            + " WHERE f." + KEY_TP_ID + " = " + FLD_READ + ") AS r"
                                            + " ON r.bf_book_id = b." + KEY_ID
                                            + " where r.f_name = \"false\" or r.f_name isnull"
                                            + " GROUP BY b." + KEY_ID
                                            + " ORDER BY parent, child",
   
                              QR_NOT_RD_TTL = "SELECT UPPER(SUBSTR(b." + KEY_TTL + ", 1, 1)) AS parent, b." + KEY_ID + " AS child_id, COALESCE(b." + KEY_TTL + " || \" - \" || GROUP_CONCAT(a.f_name, \", \"), b." + KEY_TTL + ") AS child"
                                            + " FROM " + TABLE_BOOKS + " AS b"
                                            + " LEFT JOIN"
                                            + " (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                                            + " WHERE f." + KEY_TP_ID + " = " + FLD_AUTHOR + ") AS a"
                                            + " ON a.bf_book_id = b." + KEY_ID
                                            + " LEFT JOIN"
                                            + " (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                                            + " WHERE f." + KEY_TP_ID + " = " + FLD_READ + ") AS r"
                                            + " ON r.bf_book_id = b." + KEY_ID
                                            + " where r.f_name = \"false\" or r.f_name isnull"
                                            + " GROUP BY b." + KEY_ID
                                            + " ORDER BY parent, child",
   
                               QR_PBL_AUT = "SELECT IFNULL(p.f_name, " + MISSING + ") AS parent, b." + KEY_ID + " AS child_id, COALESCE(GROUP_CONCAT(a.f_name, \", \") || \" - \" || b." + KEY_TTL + ", b." + KEY_TTL + ") AS child"
                                          + " FROM " + TABLE_BOOKS
                                          + " AS b LEFT JOIN"
                                          + " (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                                          + " WHERE f." + KEY_TP_ID + " = " + FLD_PUBLISHER + ") AS p"
                                          + " ON p.bf_book_id = b." + KEY_ID
                                          + " LEFT JOIN"
                                          + " (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                                          + " WHERE f." + KEY_TP_ID + " = " + FLD_AUTHOR + ") AS a"
                                          + " ON a.bf_book_id = b." + KEY_ID
                                          + " GROUP BY b." + KEY_ID
                                          + " ORDER BY parent, child",
   
                               QR_PBL_TTL = "SELECT IFNULL(p.f_name, " + MISSING + ") AS parent, b." + KEY_ID + " AS child_id, COALESCE(b." + KEY_TTL + " || \" - \" || GROUP_CONCAT(a.f_name, \", \"), b." + KEY_TTL + ") AS child"
                                          + " FROM " + TABLE_BOOKS
                                          + " AS b LEFT JOIN"
                                          + " (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                                          + " WHERE f." + KEY_TP_ID + " = " + FLD_PUBLISHER + ") AS p"
                                          + " ON p.bf_book_id = b." + KEY_ID
                                          + " LEFT JOIN"
                                          + " (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                                          + " WHERE f." + KEY_TP_ID + " = " + FLD_AUTHOR + ") AS a"
                                          + " ON a.bf_book_id = b." + KEY_ID
                                          + " GROUP BY b." + KEY_ID
                                          + " ORDER BY parent, child",

                               QR_LND_TTL = "SELECT UPPER(SUBSTR(b." + KEY_TTL + ", 1, 1)) AS parent, b." + KEY_ID + " AS child_id, COALESCE(b." + KEY_TTL + " || \" - \" || GROUP_CONCAT(a.f_name, \", \"), b." + KEY_TTL + ") AS child"
                                          + " FROM " + TABLE_BOOKS + " AS b"
                                          + " LEFT JOIN"
                                          + " (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                                          + " WHERE f." + KEY_TP_ID + " = " + FLD_AUTHOR + ") AS a"
                                          + " ON a.bf_book_id = b." + KEY_ID
                                          + " JOIN " + TABLE_BOOK_FIELDS + " as bf ON b." + KEY_ID + " = bf." + KEY_BK_ID
                                          + " JOIN " + TABLE_FIELDS + " as f ON bf." + KEY_FLD_ID + " = f." + KEY_ID
                                          + " WHERE f." + KEY_TP_ID + " = " + FLD_STATUS + " AND f." + KEY_NM + " = \"Loan\""
                                          + " GROUP BY b." + KEY_ID
                                          + " ORDER BY parent, child",
   
                               QR_LND_BRW = "SELECT IFNULL(l.f_name, " + MISSING + ") AS parent, b." + KEY_ID + " AS child_id, COALESCE(b." + KEY_TTL + " || \" - \" || GROUP_CONCAT(a.f_name, \", \"), b." + KEY_TTL + ") AS child"
                                          + " FROM " + TABLE_BOOKS
                                          + " AS b LEFT JOIN"
                                          + " (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                                          + " WHERE f." + KEY_TP_ID + " = " + FLD_LOANED_TO + ") AS l"
                                          + " ON l.bf_book_id = b." + KEY_ID
                                          + " LEFT JOIN"
                                          + " (SELECT bf." + KEY_FLD_ID + " AS bf_field_id, bf." + KEY_BK_ID + " AS bf_book_id, f." + KEY_NM + " AS f_name FROM " + TABLE_BOOK_FIELDS + " AS bf JOIN " + TABLE_FIELDS + " AS f ON f." + KEY_ID + " = bf." + KEY_FLD_ID
                                          + " WHERE f." + KEY_TP_ID + " = " + FLD_AUTHOR + " ) AS a"
                                          + " ON a.bf_book_id = b." + KEY_ID
                                          + " JOIN " + TABLE_BOOK_FIELDS + " as bf ON b." + KEY_ID + " = bf." + KEY_BK_ID
                                          + " JOIN " + TABLE_FIELDS + " as f ON bf." + KEY_FLD_ID + " = f." + KEY_ID
                                          + " WHERE f." + KEY_TP_ID + " = " + FLD_STATUS + " AND f." + KEY_NM + " = \"Loan\""
                                          + " GROUP BY b." + KEY_ID
                                          + " ORDER BY parent, child";
   

   ArrayList<ParentResult> getBooks(int iOrder)
   {
      String query;
      switch(iOrder)
      {
         case SRT_TTL: 
            query = QR_TTL;
         break;

         case SRT_AUT :
            query = QR_AUT;
         break;
         
         case SRT_WNT_PBL_AUT:
            query = QR_WNT_AUT;
         break;

         case SRT_WNT_PBL_TTL:
            query = QR_WNT_TTL;
         break;

         case SRT_RD_AUT:
            query = QR_RD_AUT;
         break;

         case SRT_RD_TTL:
            query = QR_RD_TTL;
         break;
         
         case SRT_NOT_RD_AUT:
            query = QR_NOT_RD_AUT;
         break;

         case SRT_NOT_RD_TTL:
            query = QR_NOT_RD_TTL;
         break;

         case SRT_PBL_AUT:
            query = QR_PBL_AUT;
         break;

         case SRT_PBL_TTL:
            query = QR_PBL_TTL;
         break;

         case SRT_LND_TTL:
            query = QR_LND_TTL;
         break;

         case SRT_LND_BRW:
            query = QR_LND_BRW;
         break;
         
         default:
            return null;
      }

      return getBooksOrderedBy(query);
   }

   void insertBook(Book book)
   {
      db.beginTransaction();
      try
      {
         ContentValues values = new ContentValues();
         values.put(KEY_TTL, book.title.value);
         values.put(KEY_DSCR, book.description.value);
         values.put(KEY_VLM, book.volume.value);
         values.put(KEY_PBL_DT, book.publicationDate.value);
         values.put(KEY_PGS, book.pages.value);
         values.put(KEY_PRC, book.price.value);
         values.put(KEY_VL, book.value.value);
         values.put(KEY_DUE_DT, book.dueDate.value);
         values.put(KEY_RD_DT, book.readDate.value);
         values.put(KEY_EDN, book.edition.value);
         values.put(KEY_ISBN, book.isbn.value);
         values.put(KEY_WEB, book.web.value);

         long bookId = db.insert(TABLE_BOOKS, null, values);
         
         for(int i = 0; i < book.properties.size(); i++)
         {
            if (book.properties.get(i).id == 0)
            {
               values = new ContentValues();
               values.put(KEY_TP_ID, book.properties.get(i).fieldTypeId);
               values.put(KEY_NM, book.properties.get(i).value);
               book.properties.get(i).id = db.insert(TABLE_FIELDS, null, values);
            }
         }

         for (Property property : book.properties)
         {
            values = new ContentValues();
            values.put(KEY_FLD_ID, property.id);
            values.put(KEY_BK_ID, bookId);
            db.insert(TABLE_BOOK_FIELDS, null, values);
         }

         db.setTransactionSuccessful();
      }
      finally
      {
         db.endTransaction();
      }
      shrink();
   }

   ArrayList<Property> getPropertyValues(int typeId)
   {
      return getPropertyValues(typeId, false);
   }
   
   ArrayList<Property> getPropertyValues(int typeId, boolean isOrdered)
   {

      ArrayList<Property> propertyValues = new ArrayList<>();

      String sql = "SELECT f." + KEY_ID + ", f." + KEY_TP_ID + ", f." + KEY_NM
                   + " FROM " + TABLE_FIELDS + " as f "
                   + " WHERE f." + KEY_TP_ID + " = " + typeId;

      if(isOrdered)
         sql += " ORDER BY f." + KEY_NM;

      Cursor cursor = db.rawQuery(sql, null);

      Property property;
      if(cursor.moveToFirst())
      {
         do
         {
            property = new Property(Integer.parseInt(cursor.getString(ID_KEY_ID)),
                                     Integer.parseInt(cursor.getString(ID_KEY_TP_ID)),
                                     cursor.getString(ID_KEY_NM));

            propertyValues.add(property);
         } while(cursor.moveToNext());
      }
      cursor.close();

      return propertyValues;
   }

   Book getBook(long bookId)
   {
      Book book = null;

      Cursor cursor = db.query(TABLE_BOOKS,
                               null,
                               KEY_ID + " = " + bookId,
                               null,
                               null,
                               null,
                               null);

      if(cursor.moveToFirst())
      {
         book = new Book(Integer.parseInt(cursor.getString(ID_KEY_ID)),
                          cursor.getString(ID_KEY_TTL),
                          cursor.getString(ID_KEY_DSCR),
                          Integer.parseInt(cursor.getString(ID_KEY_VLM)),
                          Integer.parseInt(cursor.getString(ID_KEY_PBL_DT)),
                          Integer.parseInt(cursor.getString(ID_KEY_PGS)),
                          // Integer.parseInt(cursor.getString(ID_KEY_PRC)),
                          // Integer.parseInt(cursor.getString(ID_KEY_VL)),
                          cursor.getString(ID_KEY_PRC),
                          cursor.getString(ID_KEY_VL),
                          Integer.parseInt(cursor.getString(ID_KEY_DUE_DT)),
                          Integer.parseInt(cursor.getString(ID_KEY_RD_DT)),
                          Integer.parseInt(cursor.getString(ID_KEY_EDN)),
                          cursor.getString(ID_KEY_ISBN),
                          cursor.getString(ID_KEY_WEB));
      }

      String sql = "SELECT f." + KEY_ID + ", f." + KEY_TP_ID + ", f." + KEY_NM
                   + " FROM " + TABLE_BOOK_FIELDS + " as bf LEFT JOIN " + TABLE_FIELDS + " AS f ON bf." + KEY_FLD_ID + " = f." + KEY_ID
                   + " WHERE bf." + KEY_BK_ID + " = " + bookId;

      cursor.close();
      cursor = db.rawQuery(sql, null);

      Property property;
      if(cursor.moveToFirst())
      {
         do
         {
            property = new Property(Integer.parseInt(cursor.getString(ID_KEY_ID)),
                                     Integer.parseInt(cursor.getString(ID_KEY_TP_ID)),
                                     cursor.getString(ID_KEY_NM));

            assert book != null;
            book.properties.add(property);
         } while(cursor.moveToNext());
      }
      cursor.close();

      return book;
   }

   void deleteBook(long bookId)
   {
      db.beginTransaction();
      try
      {
         db.delete(TABLE_BOOK_FIELDS, KEY_BK_ID + " = " + bookId, null);
         db.delete(TABLE_BOOKS, KEY_ID + " = " + bookId, null);

         db.setTransactionSuccessful();
      }
      catch(Exception e)
      {
         Log.e(TAG, e.getMessage());
      }
      finally
      {
         db.endTransaction();
         shrink();
      }

   }

   void updateBook(Book book)
   {
      ContentValues values;

      db.beginTransaction();
      try
      {
         for(Property property : book.properties)
         {
            if(property.id == 0)
            {
               values = new ContentValues();
               values.put(KEY_TP_ID, property.fieldTypeId);
               values.put(KEY_NM, property.value);
               property.id = db.insert(TABLE_FIELDS, null, values);
            }
         }

         db.delete(TABLE_BOOK_FIELDS, KEY_BK_ID + " = " + book.id, null);

         for(Property property : book.properties)
         {
            values = new ContentValues();
            values.put(KEY_FLD_ID, property.id);
            values.put(KEY_BK_ID, book.id);
            db.insert(TABLE_BOOK_FIELDS, null, values);
         }

         values = new ContentValues();
         values.put(KEY_TTL, book.title.value);
         values.put(KEY_DSCR, book.description.value);
         values.put(KEY_VLM, book.volume.value);
         values.put(KEY_PBL_DT, book.publicationDate.value);
         values.put(KEY_PGS, book.pages.value);
         values.put(KEY_PRC, book.price.value);
         values.put(KEY_VL, book.value.value);
         values.put(KEY_DUE_DT, book.dueDate.value);
         values.put(KEY_RD_DT, book.readDate.value);
         values.put(KEY_EDN, book.edition.value);
         values.put(KEY_ISBN, book.isbn.value);
         values.put(KEY_WEB, book.web.value);
         db.update(TABLE_BOOKS, values, KEY_ID + " = " + book.id, null);

         db.setTransactionSuccessful();
      }
      catch(Exception e)
      {
         Log.e(TAG, e.getMessage());
      }
      finally
      {
         db.endTransaction();
         shrink();
      }
   }

   private void shrink()
   {
      db.execSQL("VACUUM");
   }

   private static class DBOpenHelper extends SQLiteOpenHelper
	{
      private final Context context;
		
      DBOpenHelper(Context context, String name, CursorFactory factory, int version)
		{
			super(context, name, factory, version);
			this.context = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			db.execSQL(CREATE_TABLE_BOOKS);
			db.execSQL(CREATE_TABLE_FIELDS);
			db.execSQL(CREATE_TABLE_BOOK_FIELDS);

			db.beginTransaction();
	      try
	      {
	         int typeId,
	             valuesId,
                fieldId;

	         String fieldName;
            String[] valuesArray;

            TypedArray fieldTypedArray;
	         
	         ContentValues values;
	         
	         TypedArray fieldsValuesTypedArray = context.getResources().obtainTypedArray(R.array.fields_values);
	         for(int i = 0; i < fieldsValuesTypedArray.length(); i++)
	         {
	            fieldId = fieldsValuesTypedArray.getResourceId(i, -1);
	            fieldTypedArray = context.getResources().obtainTypedArray(fieldId);
	            typeId = fieldTypedArray.getInt(0, -1);
               @StyleableRes int index = 1;
	            valuesId = fieldTypedArray.getResourceId(index, -1);
	            valuesArray = context.getResources().getStringArray(valuesId);
	            fieldName = context.getResources().getResourceEntryName(valuesId);
	            for(String sValue : valuesArray)
	            {
	               values = new ContentValues();
	               values.put(KEY_TP_ID, typeId);
	               values.put(KEY_NM, sValue);
	               if(db.insert(TABLE_FIELDS, null, values) < 0)
	               {
	                  throw new RuntimeException(context.getResources().getString(R.string.err_db, fieldName, typeId, sValue));
	               }              
	            }
	            fieldTypedArray.recycle();
	         }
	         
	         fieldsValuesTypedArray.recycle(); // Important!

            db.setTransactionSuccessful();
	      }
	      catch(RuntimeException e)
	      {
	         Log.e("TaskDBAdapter", e.getMessage());
	      }
            
	      finally
	      {
	         db.endTransaction();
         }
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			Log.w("TaskDBAdapter", "Upgrade from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
			// on upgrade drop older tables
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_FIELDS);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOK_FIELDS);

			// create new tables
			onCreate(db);
		}
	}
   
   /**
    * Copies the database file at the specified location over the current
    * internal application database.
    * */
   boolean importDatabase(String dbPath)
   {

      // Close the SQLiteOpenHelper so it will commit the created empty
      // database to internal storage.
      dbHelper.close();
      File newDb = new File(dbPath);
      File oldDb = context.getDatabasePath(DATABASE_NAME);
      if(newDb.exists())
      {
         try
         {
            FileUtils.copyFile(new FileInputStream(newDb), new FileOutputStream(oldDb));
         } 
         catch(IOException e)
         {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            return false;
         }
         // Access the copied database so SQLiteHelper will cache it and mark
         // it as created.
         dbHelper.getWritableDatabase().close();
         return true;
      }
      return false;
   }
   
   boolean exportDatabase(String dbPath)
   {

      // Close the SQLiteOpenHelper so it will commit the created empty
      // database to internal storage.
      dbHelper.close();
      File newDb = new File(dbPath);
      File oldDb = context.getDatabasePath(DATABASE_NAME);

         try
         {
            FileUtils.copyFile(new FileInputStream(oldDb), new FileOutputStream(newDb));
         } 
         catch(IOException e)
         {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            return false;
         }
      return true;
   }
}
