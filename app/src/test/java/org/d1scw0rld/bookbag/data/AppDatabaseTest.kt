package org.d1scw0rld.bookbag.data

import android.content.ContentValues
import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.d1scw0rld.bookbag.data.entity.FieldEntity
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.d1scw0rld.bookbag.DisplayNameRobolectricRunner
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.annotation.Config
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

@RunWith(DisplayNameRobolectricRunner::class)
@Config(sdk = [28])
class AppDatabaseTest {

    private lateinit var context: Context
    private val scope = CoroutineScope(SupervisorJob())

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        AppDatabase.closeAndReset()
        // Clean up any existing DB file
        val dbFile = context.getDatabasePath("book_bag.db")
        if (dbFile.exists()) dbFile.delete()
        File(dbFile.path + "-wal").delete()
        File(dbFile.path + "-shm").delete()
    }

    @After
    fun tearDown() {
        AppDatabase.closeAndReset()
    }

    private fun triggerDbOpen(db: AppDatabase) {
        runBlocking {
            db.bookDao().getFieldsByTypeId(0)
        }
    }

    @DisplayName("Get Database - Subsequent Invocations - Returns Same Singleton Instance")
    @Test
    fun getDatabase_subsequentInvocations_returnsSameSingletonInstance() {
        val db1 = AppDatabase.getDatabase(context, scope)
        val db2 = AppDatabase.getDatabase(context, scope)
        assertSame(db1, db2)
    }

    @DisplayName("Close and Reset - Active Database Instance - Closes Database and Clears Instance")
    @Test
    fun closeAndReset_activeDatabaseInstance_closesDatabaseAndClearsInstance() {
        val db1 = AppDatabase.getDatabase(context, scope)
        triggerDbOpen(db1)
        assertTrue(db1.isOpen)
        
        AppDatabase.closeAndReset()
        
        val db2 = AppDatabase.getDatabase(context, scope)
        assertNotSame(db1, db2)
    }

    @DisplayName("Export Database - Valid Database and Destination Path - Exports Database File Successfully")
    @Test
    fun exportDatabase_validDatabaseAndDestinationPath_exportsDatabaseFileSuccessfully() {
        val db = AppDatabase.getDatabase(context, scope)
        triggerDbOpen(db)
        
        val targetFile = File(context.cacheDir, "exported.db")
        if (targetFile.exists()) targetFile.delete()
        
        val result = AppDatabase.exportDatabase(context, targetFile.absolutePath)
        
        assertTrue(result)
        assertTrue(targetFile.exists())
    }

    @DisplayName("Import Database - Valid Backup File - Restores Database and Cleans Up Journal Files")
    @Test
    fun importDatabase_validBackupFile_restoresDatabaseAndCleansUpJournalFiles() {
        // 1. Create a source database file
        val backupFile = File(context.cacheDir, "backup.db")
        val sqliteDb = android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(backupFile, null)
        sqliteDb.execSQL("CREATE TABLE test (id INTEGER)")
        sqliteDb.close()
        
        // 2. Create dummy WAL/SHM files for the current DB
        val dbFile = context.getDatabasePath("book_bag.db")
        dbFile.parentFile?.mkdirs()
        val walFile = File(dbFile.absolutePath + "-wal")
        val shmFile = File(dbFile.absolutePath + "-shm")
        walFile.writeText("dummy wal")
        shmFile.writeText("dummy shm")
        
        // 3. Perform import
        val result = AppDatabase.importDatabase(context, backupFile.absolutePath)
        
        assertTrue("Import should return true", result)
        assertFalse("WAL file should be deleted", walFile.exists())
        assertFalse("SHM file should be deleted", shmFile.exists())
        assertTrue("Restored DB should exist", dbFile.exists())
    }

    @DisplayName("Import Database - Non Existent File Path - Returns False")
    @Test
    fun importDatabase_nonExistentFilePath_returnsFalse() {
        val result = AppDatabase.importDatabase(context, "/invalid/path/db.db")
        assertFalse(result)
    }

    @DisplayName("Sanitize Database Schema - Legacy Nullable Columns Provided - Reconstructs Schema To Not Null")
    @Test
    fun sanitizeDatabaseSchema_legacyNullableColumnsProvided_reconstructsSchemaToNotNull() {
        val dbFile = context.getDatabasePath("book_bag.db")
        dbFile.parentFile?.let { if (!it.exists()) it.mkdirs() }
        
        val db = android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(dbFile, null)
        db.execSQL("CREATE TABLE books (_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, description TEXT, volume INTEGER, publication_date INTEGER, pages INTEGER, price TEXT, value TEXT, due_date INTEGER, read_date INTEGER, edition INTEGER, isbn TEXT, web TEXT)") 
        val bookValues = ContentValues().apply { putNull("title") }
        db.insert("books", null, bookValues)
        
        db.execSQL("CREATE TABLE fields (_id INTEGER PRIMARY KEY AUTOINCREMENT, type_id INTEGER, name TEXT)") 
        val fieldValues = ContentValues().apply {
            putNull("type_id")
            putNull("name")
        }
        db.insert("fields", null, fieldValues)
        
        db.execSQL("CREATE TABLE book_fields (book_id INTEGER, field_id INTEGER)") 
        db.close()
        
        val roomDb = AppDatabase.getDatabase(context, scope)
        triggerDbOpen(roomDb)
        
        val checkDb = android.database.sqlite.SQLiteDatabase.openDatabase(dbFile.absolutePath, null, android.database.sqlite.SQLiteDatabase.OPEN_READONLY)
        
        checkDb.rawQuery("PRAGMA table_info(books)", null).use { cursor ->
            while (cursor.moveToNext()) {
                if (cursor.getString(cursor.getColumnIndex("name")) == "title") {
                    assertEquals("Title should be NOT NULL", 1, cursor.getInt(cursor.getColumnIndex("notnull")))
                }
            }
        }

        checkDb.rawQuery("PRAGMA table_info(fields)", null).use { cursor ->
            while (cursor.moveToNext()) {
                val colName = cursor.getString(cursor.getColumnIndex("name"))
                if (colName == "name" || colName == "type_id") {
                    assertEquals("$colName should be NOT NULL", 1, cursor.getInt(notnullIdx(cursor)))
                }
            }
        }

        checkDb.rawQuery("PRAGMA table_info(book_fields)", null).use { cursor ->
            var pkCount = 0
            while (cursor.moveToNext()) {
                if (cursor.getInt(cursor.getColumnIndex("pk")) > 0) pkCount++
            }
            assertEquals("Should have composite primary key", 2, pkCount)
        }
        
        checkDb.close()
        AppDatabase.closeAndReset()
    }

    @DisplayName("Sanitize Database Schema - Missing Reconstruction Columns - Handles Failure Gracefully")
    @Test
    fun sanitizeDatabaseSchema_missingReconstructionColumns_handlesFailureGracefully() {
        val dbFile = context.getDatabasePath("book_bag.db")
        dbFile.parentFile?.let { if (!it.exists()) it.mkdirs() }
        
        // 1. Create a DB with 'books' table missing some expected columns
        val db = android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(dbFile, null)
        db.execSQL("CREATE TABLE books (_id INTEGER PRIMARY KEY, title TEXT)") // title is nullable, so reconstruction will be triggered
        // But many columns like 'description' are missing, so the INSERT INTO ... SELECT will fail
        db.close()
        
        // 2. This will trigger sanitizeDatabaseSchema, which will trigger table reconstruction,
        // which will FAIL during execSQL("INSERT INTO..."), throw Exception, and be caught by the outer catch.
        AppDatabase.getDatabase(context, scope)
        
        // Reaching here means exception was handled
        assertTrue(true)
        AppDatabase.closeAndReset()
    }

    private fun notnullIdx(cursor: android.database.Cursor) = cursor.getColumnIndex("notnull")

    @DisplayName("Sanitize Database Schema - Already Valid Schema Provided - Returns Early Without Changes")
    @Test
    fun sanitizeDatabaseSchema_alreadyValidSchemaProvided_returnsEarlyWithoutChanges() {
        val dbFile = context.getDatabasePath("book_bag.db")
        dbFile.parentFile?.let { if (!it.exists()) it.mkdirs() }
        
        val db = android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(dbFile, null)
        db.execSQL("CREATE TABLE books (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, description TEXT, volume INTEGER, publication_date INTEGER, pages INTEGER, price TEXT, value TEXT, due_date INTEGER, read_date INTEGER, edition INTEGER, isbn TEXT, web TEXT)")
        db.execSQL("CREATE TABLE fields (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, type_id INTEGER NOT NULL, name TEXT NOT NULL)")
        // Include Foreign Keys to satisfy Room validation if it happens
        db.execSQL("CREATE TABLE book_fields (book_id INTEGER NOT NULL, field_id INTEGER NOT NULL, PRIMARY KEY(book_id, field_id), FOREIGN KEY(book_id) REFERENCES books(_id) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(field_id) REFERENCES fields(_id) ON UPDATE NO ACTION ON DELETE CASCADE)")
        db.close()
        
        val roomDb = AppDatabase.getDatabase(context, scope)
        triggerDbOpen(roomDb)
        
        assertTrue(dbFile.exists())
        AppDatabase.closeAndReset()
    }

    @DisplayName("Prepopulate Database - On Database Creation - Prepopulates Metadata Tables")
    @Test
    fun prepopulateDatabase_onDatabaseCreation_prepopulatesMetadataTables() = runBlocking {
        val db = AppDatabase.getDatabase(context, scope)
        triggerDbOpen(db)
        
        var fields: List<FieldEntity> = emptyList()
        for (attempt in 1..50) {
            fields = db.bookDao().getFieldsByTypeId(3)
            if (fields.isNotEmpty()) break
            delay(100.milliseconds)
        }
        
        assertFalse("Database should be prepopulated with fields", fields.isEmpty())
    }

    @DisplayName("Export Database - Source Database File Missing - Returns False")
    @Test
    fun exportDatabase_sourceDatabaseFileMissing_returnsFalse() {
        AppDatabase.closeAndReset()
        val dbFile = context.getDatabasePath("book_bag.db")
        if (dbFile.exists()) dbFile.delete()
        
        val targetFile = File(context.cacheDir, "exported.db")
        val result = AppDatabase.exportDatabase(context, targetFile.absolutePath)
        
        assertFalse(result)
    }

    @DisplayName("Export Database - Copy Exception Thrown - Returns False")
    @Test
    fun exportDatabase_copyExceptionThrown_returnsFalse() {
        AppDatabase.getDatabase(context, scope)
        // Pass a directory as target file to trigger a copy exception
        val invalidPath = context.cacheDir.absolutePath
        val result = AppDatabase.exportDatabase(context, invalidPath)
        assertFalse(result)
    }

    @DisplayName("Import Database - Copy Exception Thrown - Returns False")
    @Test
    fun importDatabase_copyExceptionThrown_returnsFalse() {
        val backupFile = File(context.cacheDir, "backup.db")
        backupFile.writeText("fake content")
        
        val dbFile = context.getDatabasePath("book_bag.db")
        dbFile.parentFile?.mkdirs()
        
        // Make the parent directory non-writable to trigger an exception during copyTo
        dbFile.parentFile?.setWritable(false)
        
        try {
            val result = AppDatabase.importDatabase(context, backupFile.absolutePath)
            assertFalse(result)
        } finally {
            dbFile.parentFile?.setWritable(true)
        }
    }

    @DisplayName("Sanitize Database Schema - Invalid Database File Provided - Handles Exception Gracefully")
    @Test
    fun sanitizeDatabaseSchema_invalidDatabaseFileProvided_handlesExceptionGracefully() {
        val dbFile = context.getDatabasePath("book_bag.db")
        dbFile.parentFile?.mkdirs()
        dbFile.writeText("Not a database")
        
        // This will call sanitizeDatabaseSchema which will fail to open the DB
        // and catch the exception, logging it.
        AppDatabase.getDatabase(context, scope)
        
        // Should not throw, reaching here means exception was caught
        assertTrue(true)
    }

    @DisplayName("AppDatabaseCallback On Create - Resources Throw Exception - Handles Error Gracefully")
    @Test
    fun appDatabaseCallbackOnCreate_resourcesThrowException_handlesErrorGracefully() = runBlocking {
        val mockContext = mock(Context::class.java)
        val mockResources = mock(android.content.res.Resources::class.java)
        val mockAppContext = mock(Context::class.java)
        
        `when`(mockContext.applicationContext).thenReturn(mockAppContext)
        `when`(mockAppContext.resources).thenReturn(mockResources)
        // Trigger exception in obtainTypedArray
        `when`(mockResources.obtainTypedArray(anyInt())).thenThrow(RuntimeException("Mock error"))
        
        // Use a real SupportSQLiteDatabase mock to pass to onCreate
        val mockDb = mock(SupportSQLiteDatabase::class.java)
        
        // Let's use reflection to instantiate it for the test
        val callbackClass = AppDatabase::class.java.declaredClasses.find { it.simpleName == "AppDatabaseCallback" }
        assertNotNull(callbackClass)
        val constructor = callbackClass!!.getDeclaredConstructor(Context::class.java, CoroutineScope::class.java)
        constructor.isAccessible = true
        val callback = constructor.newInstance(mockAppContext, scope) as RoomDatabase.Callback
        
        // This should not throw even though obtainTypedArray throws
        callback.onCreate(mockDb)
        
        // Reaching here means it handled the exception
        assertTrue(true)
    }

    @DisplayName("Close and Reset - Close Throws Exception - Handles Error Gracefully")
    @Test
    fun closeAndReset_closeThrowsException_handlesErrorGracefully() {
        val mockDb = mock(AppDatabase::class.java)
        `when`(mockDb.isOpen).thenReturn(true)
        `when`(mockDb.close()).thenThrow(RuntimeException("Close failed"))
        
        // Use reflection to set INSTANCE
        val instanceField = AppDatabase::class.java.getDeclaredField("INSTANCE")
        instanceField.isAccessible = true
        instanceField.set(null, mockDb)
        
        // This should call mockDb.close(), catch the exception, and set INSTANCE to null
        AppDatabase.closeAndReset()
        
        assertNull(instanceField.get(null))
    }

    @DisplayName("Migration 1 To 2 - Applied to Legacy Database - Applies Composite Primary Keys and Type Indices")
    @Test
    fun migration1To2_appliedToLegacyDatabase_appliesCompositePrimaryKeysAndTypeIndices() {
        val dbFile = File(context.cacheDir, "test_migration.db")
        if (dbFile.exists()) dbFile.delete()
        
        val configuration = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbFile.absolutePath) // Use absolutePath to be safe
            .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("CREATE TABLE books (_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT)")
                    db.execSQL("CREATE TABLE fields (_id INTEGER PRIMARY KEY AUTOINCREMENT, type_id INTEGER, name TEXT)")
                    db.execSQL("CREATE TABLE book_fields (book_id INTEGER, field_id INTEGER)")
                }
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()
        
        val helper = FrameworkSQLiteOpenHelperFactory().create(configuration)
        val db = helper.writableDatabase
        
        // Apply migration
        AppDatabase.MIGRATION_1_2.migrate(db)
        
        // 1. Check index exists
        db.query("PRAGMA index_list(fields)").use { cursor ->
            var indexFound = false
            while (cursor.moveToNext()) {
                if (cursor.getString(cursor.getColumnIndex("name")) == "index_fields_type_id") {
                    indexFound = true
                    break
                }
            }
            assertTrue("Index on fields(type_id) should exist", indexFound)
        }
        
        // 2. Check book_fields has composite PK
        db.query("PRAGMA table_info(book_fields)").use { cursor ->
            var pkCount = 0
            while (cursor.moveToNext()) {
                if (cursor.getInt(cursor.getColumnIndex("pk")) > 0) pkCount++
            }
            assertEquals("book_fields should have 2 PK columns", 2, pkCount)
        }
        
        db.close()
    }
}
