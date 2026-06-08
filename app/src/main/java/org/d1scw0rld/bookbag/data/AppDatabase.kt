package org.d1scw0rld.bookbag.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.d1scw0rld.bookbag.R
import org.d1scw0rld.bookbag.data.dao.BookDao
import org.d1scw0rld.bookbag.data.entity.BookEntity
import org.d1scw0rld.bookbag.data.entity.BookFieldCrossRef
import org.d1scw0rld.bookbag.data.entity.FieldEntity

@Database(
    entities = [BookEntity::class, FieldEntity::class, BookFieldCrossRef::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "book_bag.db"
                )
                .addCallback(AppDatabaseCallback(context, scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val context: Context,
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        @Suppress("ResourceType")
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            scope.launch(Dispatchers.IO) {
                val dbDao = getDatabase(context, this).bookDao()
                try {
                    val fieldsValuesTypedArray = context.resources.obtainTypedArray(R.array.fields_values)
                    for (i in 0 until fieldsValuesTypedArray.length()) {
                        val fieldId = fieldsValuesTypedArray.getResourceId(i, -1)
                        val fieldTypedArray = context.resources.obtainTypedArray(fieldId)
                        val typeId = fieldTypedArray.getInt(0, -1)
                        val valuesId = fieldTypedArray.getResourceId(1, -1)
                        val values = context.resources.getStringArray(valuesId)

                        for (value in values) {
                            dbDao.insertField(FieldEntity(typeId = typeId, name = value))
                        }
                        fieldTypedArray.recycle()
                    }
                    fieldsValuesTypedArray.recycle()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
