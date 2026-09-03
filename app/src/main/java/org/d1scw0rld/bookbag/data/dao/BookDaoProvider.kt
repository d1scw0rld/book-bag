package org.d1scw0rld.bookbag.data.dao

/**
 * Resolves the [BookDao] belonging to the *current* database instance.
 *
 * Importing a backup replaces the underlying database file, which forces the previous
 * [org.d1scw0rld.bookbag.data.AppDatabase] instance to be closed and discarded. A DAO captured
 * once at construction time would keep pointing at that closed instance and every query would
 * silently return nothing, so consumers must resolve the DAO through this provider on each use.
 */
fun interface BookDaoProvider {
    fun get(): BookDao
}
