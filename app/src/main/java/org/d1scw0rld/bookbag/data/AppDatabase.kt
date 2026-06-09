package org.d1scw0rld.bookbag.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.core.database.sqlite.transaction
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.d1scw0rld.bookbag.R
import org.d1scw0rld.bookbag.data.dao.BookDao
import org.d1scw0rld.bookbag.data.entity.BookEntity
import org.d1scw0rld.bookbag.data.entity.BookFieldCrossRef
import org.d1scw0rld.bookbag.data.entity.FieldEntity
import java.io.File

@Database(
    entities = [BookEntity::class, FieldEntity::class, BookFieldCrossRef::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao

    companion object {
        private const val TAG = "AppDatabase"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create index on fields (type_id)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_fields_type_id` ON `fields` (`type_id`)")

                // 2. Recreate book_fields with foreign keys to match version 2 expected schema
                db.execSQL("CREATE TABLE IF NOT EXISTS `book_fields_new` (`book_id` INTEGER NOT NULL, `field_id` INTEGER NOT NULL, PRIMARY KEY(`book_id`, `field_id`), FOREIGN KEY(`book_id`) REFERENCES `books`(`_id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`field_id`) REFERENCES `fields`(`_id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("INSERT OR IGNORE INTO `book_fields_new` (`book_id`, `field_id`) SELECT `book_id`, `field_id` FROM `book_fields`")
                db.execSQL("DROP TABLE IF EXISTS `book_fields`")
                db.execSQL("ALTER TABLE `book_fields_new` RENAME TO `book_fields`")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val appContext = context.applicationContext
                sanitizeDatabaseSchema(appContext)
                val instance = Room.databaseBuilder(
                    appContext,
                    AppDatabase::class.java,
                    "book_bag.db"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .addCallback(AppDatabaseCallback(appContext, scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        fun closeAndReset() {
            synchronized(this) {
                INSTANCE?.let {
                    try {
                        if (it.isOpen) {
                            it.close()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error closing database", e)
                    }
                }
                INSTANCE = null
            }
        }

        fun importDatabase(context: Context, dbPath: String): Boolean {
            closeAndReset()
            val newDb = File(dbPath)
            val oldDb = context.getDatabasePath("book_bag.db")
            val walFile = File(oldDb.path + "-wal")
            val shmFile = File(oldDb.path + "-shm")

            if (newDb.exists()) {
                try {
                    if (walFile.exists()) walFile.delete()
                    if (shmFile.exists()) shmFile.delete()

                    newDb.copyTo(oldDb, overwrite = true)
                    sanitizeDatabaseSchema(context)
                    return true
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to import database", e)
                }
            }
            return false
        }

        fun exportDatabase(context: Context, dbPath: String): Boolean {
            closeAndReset()
            val newDb = File(dbPath)
            val oldDb = context.getDatabasePath("book_bag.db")
            return try {
                oldDb.copyTo(newDb, overwrite = true)
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to export database", e)
                false
            }
        }

        private fun sanitizeDatabaseSchema(context: Context) {
            val dbFile = context.getDatabasePath("book_bag.db")
            if (!dbFile.exists()) return

            try {
                SQLiteDatabase.openDatabase(
                    dbFile.absolutePath,
                    null,
                    SQLiteDatabase.OPEN_READWRITE
                ).use { db ->

                    // 1. Sanitize 'books' table (title column nullability)
                    var isTitleNullable = false
                    db.rawQuery("PRAGMA table_info(books)", null).use { cursor ->
                        val nameColIndex = cursor.getColumnIndex("name")
                        val notnullColIndex = cursor.getColumnIndex("notnull")
                        if (nameColIndex != -1 && notnullColIndex != -1) {
                            while (cursor.moveToNext()) {
                                val name = cursor.getString(nameColIndex)
                                if (name == "title") {
                                    val notNull = cursor.getInt(notnullColIndex)
                                    isTitleNullable = (notNull == 0)
                                    break
                                }
                            }
                        }
                    }

                    if (isTitleNullable) {
                        Log.i(TAG, "Detected nullable 'title' column in 'books' table. Re-constructing table to set NOT NULL constraint.")
                        db.transaction {
                            execSQL("CREATE TABLE books_new (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, description TEXT, volume INTEGER, publication_date INTEGER, pages INTEGER, price TEXT, value TEXT, due_date INTEGER, read_date INTEGER, edition INTEGER, isbn TEXT, web TEXT)")
                            execSQL("INSERT INTO books_new SELECT _id, IFNULL(title, ''), description, volume, publication_date, pages, price, value, due_date, read_date, edition, isbn, web FROM books")
                            execSQL("DROP TABLE books")
                            execSQL("ALTER TABLE books_new RENAME TO books")
                        }
                        Log.i(TAG, "Successfully updated 'books' table schema to match Room non-null expectation.")
                    }

                    // 2. Sanitize 'fields' table (name and type_id columns nullability)
                    var isFieldNullable = false
                    db.rawQuery("PRAGMA table_info(fields)", null).use { cursor ->
                        val nameColIndex = cursor.getColumnIndex("name")
                        val notnullColIndex = cursor.getColumnIndex("notnull")
                        if (nameColIndex != -1 && notnullColIndex != -1) {
                            while (cursor.moveToNext()) {
                                val name = cursor.getString(nameColIndex)
                                if (name == "name" || name == "type_id") {
                                    val notNull = cursor.getInt(notnullColIndex)
                                    if (notNull == 0) {
                                        isFieldNullable = true
                                        break
                                    }
                                }
                            }
                        }
                    }

                    if (isFieldNullable) {
                        Log.i(TAG, "Detected nullable 'name' or 'type_id' column in 'fields' table. Re-constructing table.")
                        db.transaction {
                            execSQL("CREATE TABLE fields_new (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, type_id INTEGER NOT NULL, name TEXT NOT NULL)")
                            execSQL("INSERT INTO fields_new SELECT _id, IFNULL(type_id, 0), IFNULL(name, '') FROM fields")
                            execSQL("DROP TABLE fields")
                            execSQL("ALTER TABLE fields_new RENAME TO fields")
                        }
                        Log.i(TAG, "Successfully updated 'fields' table schema to match Room non-null expectations.")
                    }

                    // 3. Sanitize 'book_fields' table (book_id and field_id columns nullability and primary keys)
                    var isCrossRefNullable = false
                    db.rawQuery("PRAGMA table_info(book_fields)", null).use { cursor ->
                        val nameColIndex = cursor.getColumnIndex("name")
                        val notnullColIndex = cursor.getColumnIndex("notnull")
                        val pkColIndex = cursor.getColumnIndex("pk")
                        if (nameColIndex != -1 && notnullColIndex != -1 && pkColIndex != -1) {
                            while (cursor.moveToNext()) {
                                val name = cursor.getString(nameColIndex)
                                if (name == "book_id" || name == "field_id") {
                                    val notNull = cursor.getInt(notnullColIndex)
                                    val pk = cursor.getInt(pkColIndex)
                                    if (notNull == 0 || pk == 0) {
                                        isCrossRefNullable = true
                                        break
                                    }
                                }
                            }
                        }
                    }

                    if (isCrossRefNullable) {
                        Log.i(TAG, "Detected nullable columns or missing primary keys in 'book_fields' table. Re-constructing table.")
                        db.transaction {
                            execSQL("CREATE TABLE book_fields_new (book_id INTEGER NOT NULL, field_id INTEGER NOT NULL, PRIMARY KEY(book_id, field_id))")
                            execSQL("INSERT OR IGNORE INTO book_fields_new SELECT IFNULL(book_id, 0), IFNULL(field_id, 0) FROM book_fields")
                            execSQL("DROP TABLE book_fields")
                            execSQL("ALTER TABLE book_fields_new RENAME TO book_fields")
                        }
                        Log.i(TAG, "Successfully updated 'book_fields' table schema to match Room composite primary key expectations.")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sanitize database schema", e)
            }
        }
    }

    private class AppDatabaseCallback(
        context: Context,
        private val scope: CoroutineScope
    ) : Callback() {
        private val appContext = context.applicationContext

        @Suppress("ResourceType")
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            scope.launch(Dispatchers.IO) {
                val dbDao = getDatabase(appContext, scope).bookDao()
                try {
                    val fieldsValuesTypedArray = appContext.resources.obtainTypedArray(R.array.fields_values)
                    try {
                        for (i in 0 until fieldsValuesTypedArray.length()) {
                            val fieldId = fieldsValuesTypedArray.getResourceId(i, -1)
                            if (fieldId != -1) {
                                val fieldTypedArray = appContext.resources.obtainTypedArray(fieldId)
                                try {
                                    val typeId = fieldTypedArray.getInt(0, -1)
                                    val valuesId = fieldTypedArray.getResourceId(1, -1)
                                    if (valuesId != -1) {
                                        val valuesArray = appContext.resources.getStringArray(valuesId)
                                        for (sValue in valuesArray) {
                                            dbDao.insertField(FieldEntity(typeId = typeId, name = sValue))
                                        }
                                    }
                                } finally {
                                    fieldTypedArray.recycle()
                                }
                            }
                        }
                    } finally {
                        fieldsValuesTypedArray.recycle()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to prepopulate database", e)
                }
            }
        }
    }
}
