package org.d1scw0rld.bookbag.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.d1scw0rld.bookbag.data.AppDatabase
import org.d1scw0rld.bookbag.data.dao.BookDao
import org.d1scw0rld.bookbag.data.dao.BookDaoProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        scope: CoroutineScope
    ): AppDatabase {
        return AppDatabase.getDatabase(context, scope)
    }

    @Provides
    @Singleton
    fun provideBookDao(database: AppDatabase): BookDao {
        return database.bookDao()
    }

    /**
     * Resolves the DAO from the current [AppDatabase] on every call so that consumers keep
     * working after an import swaps the underlying database file.
     */
    @Provides
    @Singleton
    fun provideBookDaoProvider(
        @ApplicationContext context: Context,
        scope: CoroutineScope
    ): BookDaoProvider = BookDaoProvider { AppDatabase.getDatabase(context, scope).bookDao() }
}
