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
import org.d1scw0rld.bookbag.dto.Field;
import org.d1scw0rld.bookbag.dto.FieldType;
import org.d1scw0rld.bookbag.dto.FileUtils;
import org.d1scw0rld.bookbag.dto.ParentResult;
import org.d1scw0rld.bookbag.dto.Result;

public class DBAdapter
{
	public static final String DATABASE_NAME = "book_bag.db",
	                           DB_PATH = "//data//org.d1scw0rld.bookbag//databases//";

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
	private DBOpenHelper dbHelper;
	
	public final static char separator = DecimalFormatSymbols.getInstance().getDecimalSeparator();

   public final static int FLD_AUTHOR = 1,
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
                           FLD_WEB = 110,
                           FLD_PRICE_CURRENCY = 111,
                           FLD_VALUE_CURRENCY = 112;                           

   public final static int SRT_TTL = 1,
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

   public final static ArrayList<FieldType> FIELD_TYPES = new ArrayList<FieldType>();
   
	public DBAdapter(Context _context)
	{
		this.context = _context;
		dbHelper = new DBOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
		Resources r = context.getResources();
		FIELD_TYPES.clear();
		FIELD_TYPES.add(new FieldType(FLD_TITLE, r.getString(R.string.fld_title), FieldType.TYPE_TEXT).setVisibility(true));
		FIELD_TYPES.add(new FieldType(FLD_AUTHOR, r.getString(R.string.fld_author), FieldType.TYPE_MULTIFIELD).setVisibility(true));
		FIELD_TYPES.add(new FieldType(FLD_DESCRIPTION, r.getString(R.string.fld_descrition), FieldType.TYPE_TEXT).setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE).setVisibility(false));
		FIELD_TYPES.add(new FieldType(FLD_SERIE, r.getString(R.string.fld_serie), FieldType.TYPE_TEXT_AUTOCOMPLETE));
		FIELD_TYPES.add(new FieldType(FLD_VOLUME, r.getString(R.string.fld_volume), FieldType.TYPE_TEXT).setInputType(InputType.TYPE_CLASS_NUMBER));
		FIELD_TYPES.add(new FieldType(FLD_GENRE, r.getString(R.string.fld_genre), FieldType.TYPE_MULTI_SPINNER));
		FIELD_TYPES.add(new FieldType(FLD_LANGUAGE, r.getString(R.string.fld_language), FieldType.TYPE_SPINNER));
		FIELD_TYPES.add(new FieldType(FLD_PAGES, r.getString(R.string.fld_pages), FieldType.TYPE_TEXT).setInputType(InputType.TYPE_CLASS_NUMBER));
		FIELD_TYPES.add(new FieldType(FLD_PUBLISHER, r.getString(R.string.fld_publisher), FieldType.TYPE_TEXT_AUTOCOMPLETE));
		FIELD_TYPES.add(new FieldType(FLD_PUBLICATION_DATE, r.getString(R.string.fld_publication_date), FieldType.TYPE_TEXT).setInputType(InputType.TYPE_CLASS_NUMBER));
		FIELD_TYPES.add(new FieldType(FLD_PUBLICATION_LOCATION, r.getString(R.string.fld_publication_location), FieldType.TYPE_TEXT_AUTOCOMPLETE));
		FIELD_TYPES.add(new FieldType(FLD_EDITION, r.getString(R.string.fld_edition), FieldType.TYPE_TEXT).setInputType(InputType.TYPE_CLASS_NUMBER));
		FIELD_TYPES.add(new FieldType(FLD_PRICE, r.getString(R.string.fld_price), FieldType.TYPE_MONEY).setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL));
      FIELD_TYPES.add(new FieldType(FLD_VALUE, r.getString(R.string.fld_value), FieldType.TYPE_MONEY).setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL));
		FIELD_TYPES.add(new FieldType(FLD_STATUS, r.getString(R.string.fld_status), FieldType.TYPE_SPINNER));
		FIELD_TYPES.add(new FieldType(FLD_LOANED_TO, r.getString(R.string.fld_loaned_to), FieldType.TYPE_TEXT_AUTOCOMPLETE));
		FIELD_TYPES.add(new FieldType(FLD_READ, r.getString(R.string.fld_read), FieldType.TYPE_CHECK_BOX));
		FIELD_TYPES.add(new FieldType(FLD_READ_DATE, r.getString(R.string.fld_read_date), FieldType.TYPE_DATE));
		FIELD_TYPES.add(new FieldType(FLD_RATING, r.getString(R.string.fld_rating), FieldType.TYPE_RATING));
		FIELD_TYPES.add(new FieldType(FLD_FORMAT, r.getString(R.string.fld_format), FieldType.TYPE_SPINNER));
      FIELD_TYPES.add(new FieldType(FLD_CONDITION, r.getString(R.string.fld_condition), FieldType.TYPE_SPINNER));
		FIELD_TYPES.add(new FieldType(FLD_LOCATION, r.getString(R.string.fld_location), FieldType.TYPE_TEXT_AUTOCOMPLETE));
		FIELD_TYPES.add(new FieldType(FLD_DUE_DATE, r.getString(R.string.fld_due_date), FieldType.TYPE_DATE));
		FIELD_TYPES.add(new FieldType(FLD_ISBN, r.getString(R.string.fld_isbn), FieldType.TYPE_TEXT).setInputType(InputType.TYPE_CLASS_NUMBER));
		FIELD_TYPES.add(new FieldType(FLD_WEB, r.getString(R.string.fld_web), FieldType.TYPE_TEXT));
	}

	public void open() throws SQLiteException
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

	public void close()
	{
		db.close();
	}

   public ArrayList<Result> getBooksOrderedBy(String query)
   {
      if(Debug.ON)
      {
//         return DummyContent.BOOKS;
         return null;
      }
      else
      {
         ArrayList<Result> alResults = new ArrayList<Result>();
   
         Cursor cursor = db.rawQuery(query, null);
         Result result;
   
         if(cursor.moveToFirst())
         {
            do
            {

               result = new Result();
               result.id = Integer.parseInt(cursor.getString(0));
               result.content = cursor.getString(1);
               alResults.add(result);
            } while (cursor.moveToNext());
         }
         cursor.close();
   
         return alResults;
      }      
   }

   public ArrayList<ParentResult> getBooksOrderedBy1(String query)
   {
      if(Debug.ON)
      {
         return null;
      }
      else
      {
         ArrayList<ParentResult> alParrentResults = new ArrayList<ParentResult>();
   
         Cursor cursor = db.rawQuery(query, null);
         
         if(cursor.moveToFirst())
         {
            Result result;
            String sParrent = cursor.getString(0);
            List<Result> alChildResults = new ArrayList<Result>();
            ParentResult oParrentResult = new ParentResult(sParrent, alChildResults);

            do
            {
               sParrent = cursor.getString(0);
               result = new Result();
               result.id = Integer.parseInt(cursor.getString(1));
               result.content = cursor.getString(2);
               if(!sParrent.equalsIgnoreCase(oParrentResult.getName()))
               {
                  alParrentResults.add(oParrentResult);
                  alChildResults = new ArrayList<Result>();
                  oParrentResult = new ParentResult(sParrent, alChildResults);
//                  alParrentResults.add(oParrentResult);
//                  sParrentTmp = sParrent;
               }
//               oParrentResult.addChildResult(result);
               alChildResults.add(result);
            } while (cursor.moveToNext());
            alParrentResults.add(oParrentResult);
         }
         cursor.close();
   
         return alParrentResults;
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
   

   public ArrayList<ParentResult> getBooks(int iOrder)
   {
      String query = "";
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

      ArrayList<ParentResult> alParrentResults = getBooksOrderedBy1(query);
      
      return alParrentResults;
   }

   public void insertBook(Book oBook)
   {
      db.beginTransaction();
      try
      {
         ContentValues values = new ContentValues();
         values.put(KEY_TTL, oBook.csTitle.value);
         values.put(KEY_DSCR, oBook.csDescription.value);
         values.put(KEY_VLM, oBook.ciVolume.value);
         values.put(KEY_PBL_DT, oBook.ciPublicationDate.value);
         values.put(KEY_PGS, oBook.ciPages.value);
         values.put(KEY_PRC, oBook.csPrice.value);
         values.put(KEY_VL, oBook.csValue.value);
         values.put(KEY_DUE_DT, oBook.ciDueDate.value);
         values.put(KEY_RD_DT, oBook.ciReadDate.value);
         values.put(KEY_EDN, oBook.ciEdition.value);
         values.put(KEY_ISBN, oBook.csISBN.value);
         values.put(KEY_WEB, oBook.csWeb.value);

         long iBookID = db.insert(TABLE_BOOKS, null, values);
         
         for(int i = 0; i < oBook.alFields.size(); i++)
         {
            if (oBook.alFields.get(i).iID == 0)
            {
               values = new ContentValues();
               values.put(KEY_TP_ID, oBook.alFields.get(i).iTypeID);
               values.put(KEY_NM, oBook.alFields.get(i).sValue);
               oBook.alFields.get(i).iID = db.insert(TABLE_FIELDS, null, values);
            }
         }

         for (Field oField : oBook.alFields)
         {
            values = new ContentValues();
            values.put(KEY_FLD_ID, oField.iID);
            values.put(KEY_BK_ID, iBookID);
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

   public ArrayList<Field> getFieldValues(int iTypeID)
   {
      return getFieldValues(iTypeID, false);
   }
   
   public ArrayList<Field> getFieldValues(int iTypeID, boolean isOrdered)
   {

      ArrayList<Field> alFieldValues = new ArrayList<>();

      // SELECT f.id, bf.type_id, f.name
      // FROM book_fields as bf LEFT JOIN fields AS f ON bf.field_id = f.id
      // WHERE bf.type_id =1
      // ORDER BY f.name

      // String sql = "SELECT f." + KEY_ID + ", bf." + KEY_TP_ID + ", f." +
      // KEY_NM
      // + " FROM " + TABLE_BOOK_FIELDS + " as bf LEFT JOIN " + TABLE_FIELDS +
      // " AS f ON bf." + KEY_FLD_ID + " = f." + KEY_ID
      // + " WHERE bf." + KEY_TP_ID + " = " + iTypeID
      // + " ORDER BY f." + KEY_NM;

      String sql = "SELECT f." + KEY_ID + ", f." + KEY_TP_ID + ", f." + KEY_NM
                   + " FROM " + TABLE_FIELDS + " as f "
                   + " WHERE f." + KEY_TP_ID + " = " + iTypeID;
      // + " ORDER BY f." + KEY_NM;

      if(isOrdered)
         sql += " ORDER BY f." + KEY_NM;

      Cursor cursor = db.rawQuery(sql, null);

      Field oField;
      // ArrayList<Field> alFields = new ArrayList<>();
      if(cursor.moveToFirst())
      {
         do
         {
            oField = new Field(Integer.parseInt(cursor.getString(ID_KEY_ID)),
                               Integer.parseInt(cursor.getString(ID_KEY_TP_ID)),
                               cursor.getString(ID_KEY_NM));

            alFieldValues.add(oField);
         } while(cursor.moveToNext());
      }

      return alFieldValues;
   }

   public Book getBook(long iBookID)
   {
      Book oBook = null;

      Cursor cursor = db.query(TABLE_BOOKS,
                               null,
                               KEY_ID + " = " + iBookID,
                               null,
                               null,
                               null,
                               null);

      if(cursor.moveToFirst())
      {
         oBook = new Book(Integer.parseInt(cursor.getString(ID_KEY_ID)),
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
                   + " WHERE bf." + KEY_BK_ID + " = " + iBookID;

      cursor = db.rawQuery(sql, null);

      Field oField;
      // ArrayList<Field> alFields = new ArrayList<>();
      if(cursor.moveToFirst())
      {
         do
         {
            oField = new Field(Integer.parseInt(cursor.getString(ID_KEY_ID)),
                               Integer.parseInt(cursor.getString(ID_KEY_TP_ID)),
                               cursor.getString(ID_KEY_NM));

            oBook.alFields.add(oField);
         } while(cursor.moveToNext());
      }
      // oBook.alFields = alFields;

      return oBook;
   }

   public boolean deleteBook(long iBookID)
   {
      boolean result = true;

      db.beginTransaction();
      try
      {
         db.delete(TABLE_BOOK_FIELDS, KEY_BK_ID + " = " + iBookID, null);
         db.delete(TABLE_BOOKS, KEY_ID + " = " + iBookID, null);

         db.setTransactionSuccessful();
      }
      catch(Exception e)
      {
         Log.e(TAG, e.getMessage());
         result = false;
      }
      finally
      {
         db.endTransaction();
         shrink();
      }

      return result;
   }

   public boolean updateBook(Book oBook)
   {
      ContentValues oValues;
      boolean result = true;

      db.beginTransaction();
      try
      {
         for(Field oField : oBook.alFields)
         {
            if(oField.iID == 0)
            {
               oValues = new ContentValues();
               oValues.put(KEY_TP_ID, oField.iTypeID);
               oValues.put(KEY_NM, oField.sValue);
               oField.iID = db.insert(TABLE_FIELDS, null, oValues);
            }
         }

         db.delete(TABLE_BOOK_FIELDS, KEY_BK_ID + " = " + oBook.iID, null);

         for(Field oField : oBook.alFields)
         {
            oValues = new ContentValues();
            oValues.put(KEY_FLD_ID, oField.iID);
            oValues.put(KEY_BK_ID, oBook.iID);
            db.insert(TABLE_BOOK_FIELDS, null, oValues);
         }

         oValues = new ContentValues();
         oValues.put(KEY_TTL, oBook.csTitle.value);
         oValues.put(KEY_DSCR, oBook.csDescription.value);
         oValues.put(KEY_VLM, oBook.ciVolume.value);
         oValues.put(KEY_PBL_DT, oBook.ciPublicationDate.value);
         oValues.put(KEY_PGS, oBook.ciPages.value);
         oValues.put(KEY_PRC, oBook.csPrice.value);
         oValues.put(KEY_VL, oBook.csValue.value);
         oValues.put(KEY_DUE_DT, oBook.ciDueDate.value);
         oValues.put(KEY_RD_DT, oBook.ciReadDate.value);
         oValues.put(KEY_EDN, oBook.ciEdition.value);
         oValues.put(KEY_ISBN, oBook.csISBN.value);
         oValues.put(KEY_WEB, oBook.csWeb.value);
         db.update(TABLE_BOOKS, oValues, KEY_ID + " = " + oBook.iID, null);

         db.setTransactionSuccessful();
      }
      catch(Exception e)
      {
         Log.e(TAG, e.getMessage());
         result = false;
      }
      finally
      {
         db.endTransaction();
         shrink();
      }
      return result;
   }

   public void shrink()
   {
      db.execSQL("VACUUM");
   }

   private static class DBOpenHelper extends SQLiteOpenHelper
	{
      private Context context;
		
      public DBOpenHelper(Context context, String name, CursorFactory factory, int version)
		{
			super(context, name, factory, version);
			this.context = context;
		}

		@Override
		public void onCreate(SQLiteDatabase _db)
		{
			_db.execSQL(CREATE_TABLE_BOOKS);
			_db.execSQL(CREATE_TABLE_FIELDS);
			_db.execSQL(CREATE_TABLE_BOOK_FIELDS);

			_db.beginTransaction();
	      try
	      {
	         int iTypeID,
	             iValuesID,
                iFieldID;

	         String sFieldName, 
	                tsValues[];

	         TypedArray taField;
	         
	         ContentValues values;
	         
	         TypedArray taFieldsValues = context.getResources().obtainTypedArray(R.array.fields_values);
	         for(int i = 0; i < taFieldsValues.length(); i++)
	         {
	            iFieldID = taFieldsValues.getResourceId(i, -1);
	            taField = context.getResources().obtainTypedArray(iFieldID);
	            iTypeID = taField.getInt(0, -1);
	            iValuesID = taField.getResourceId(1, -1);
	            tsValues = context.getResources().getStringArray(iValuesID);
	            sFieldName = context.getResources().getResourceEntryName(iValuesID);
	            for(String sValue : tsValues)
	            {
	               values = new ContentValues();
	               values.put(KEY_TP_ID, iTypeID);
	               values.put(KEY_NM, sValue);
	               if(_db.insert(TABLE_FIELDS, null, values) < 0)
	               {
	                  throw new RuntimeException(context.getResources().getString(R.string.err_db, sFieldName, iTypeID, sValue));
	               }              
	            }
	            taField.recycle();
	         }
	         
	         taFieldsValues.recycle(); // Important!

            _db.setTransactionSuccessful();
	      }
	      catch(RuntimeException e)
	      {
	         Log.e("TaskDBAdapter", e.getMessage());
	      }
            
	      finally
	      {
	         _db.endTransaction();
         }
		}

		@Override
		public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion)
		{
			Log.w("TaskDBAdapter", "Upgrade from version " + _oldVersion + " to " + _newVersion + ", which will destroy all old data");
			// on upgrade drop older tables
			_db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
			_db.execSQL("DROP TABLE IF EXISTS " + TABLE_FIELDS);
			_db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOK_FIELDS);

			// create new tables
			onCreate(_db);
		}
	}
   
   /**
    * Copies the database file at the specified location over the current
    * internal application database.
    * */
   public boolean importDatabase(String dbPath) 
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
   
   public boolean exportDatabase(String dbPath)
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
