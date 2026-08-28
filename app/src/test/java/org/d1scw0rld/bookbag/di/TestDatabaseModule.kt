package org.d1scw0rld.bookbag.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.d1scw0rld.bookbag.data.AppDatabase
import org.d1scw0rld.bookbag.data.dao.BookDao
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
object TestDatabaseModule {

    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope = CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO)


    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        scope: CoroutineScope
    ): AppDatabase {
        // Return a fresh in-memory database for every test
        return Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @Provides
    @Singleton
    fun provideBookDao(database: AppDatabase): BookDao {
        return database.bookDao()
    }
}
