package org.d1scw0rld.bookbag.di

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.runTest
import org.d1scw0rld.bookbag.DisplayNameRobolectricRunner
import org.d1scw0rld.bookbag.data.AppDatabase
import org.d1scw0rld.bookbag.data.entity.BookEntity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.File

/**
 * Guards the production DI wiring for [org.d1scw0rld.bookbag.data.dao.BookDaoProvider].
 *
 * Importing a backup discards the current [AppDatabase], so the provider must resolve a DAO from
 * the new instance rather than handing back one captured when the graph was built.
 */
@RunWith(DisplayNameRobolectricRunner::class)
@Config(sdk = [28])
class DatabaseModuleTest {

    private lateinit var context: Context
    private lateinit var scope: CoroutineScope

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        scope = CoroutineScope(SupervisorJob())
        AppDatabase.closeAndReset()
        deleteDatabaseFiles()
    }

    @After
    fun tearDown() {
        AppDatabase.closeAndReset()
        deleteDatabaseFiles()
    }

    private fun deleteDatabaseFiles() {
        val dbFile = context.getDatabasePath(DB_NAME)
        if (dbFile.exists()) dbFile.delete()
        File(dbFile.path + WAL_SUFFIX).delete()
        File(dbFile.path + SHM_SUFFIX).delete()
    }

    @DisplayName("Provide Book Dao Provider - Database Instance Replaced - Resolves Dao From Current Instance")
    @Test
    fun provideBookDaoProvider_databaseInstanceReplaced_resolvesDaoFromCurrentInstance() = runTest {
        val daoProvider = DatabaseModule.provideBookDaoProvider(context, scope)

        val firstDao = daoProvider.get()
        firstDao.insertBook(bookEntity())
        assertEquals(ONE_BOOK_MSG, 1, firstDao.getAllBooksWithFields().size)

        // Simulates the database swap performed by an import.
        AppDatabase.closeAndReset()

        val secondDao = daoProvider.get()
        assertNotSame(NEW_DAO_MSG, firstDao, secondDao)
        // Must not throw: the DAO is bound to the live database, not the discarded one.
        assertEquals(REOPENED_MSG, 1, secondDao.getAllBooksWithFields().size)
    }

    private fun bookEntity() = BookEntity(
        title = BOOK_TITLE,
        description = null,
        volume = null,
        publicationDate = null,
        pages = null,
        price = null,
        value = null,
        dueDate = null,
        readDate = null,
        edition = null,
        isbn = null,
        web = null
    )

    companion object {
        private const val DB_NAME = "book_bag.db"
        private const val WAL_SUFFIX = "-wal"
        private const val SHM_SUFFIX = "-shm"

        private const val BOOK_TITLE = "Domain Driven Design"

        private const val ONE_BOOK_MSG = "Book should be stored in the original instance"
        private const val NEW_DAO_MSG = "Provider must return a DAO from the recreated database"
        private const val REOPENED_MSG = "Data must be readable after the instance is replaced"
    }
}
