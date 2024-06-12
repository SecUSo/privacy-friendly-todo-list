package org.secuso.privacyfriendlytodolist.model.database.migration

import android.util.Log
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

abstract class MigrationBase(startVersion: Int, endVersion: Int): Migration(startVersion,endVersion) {
    protected abstract val tag: String

    final override fun migrate(db: SupportSQLiteDatabase) {
        try {
            Log.i(tag, "DB migration v${startVersion} to v${endVersion} starts.")
            doMigrate(db)
            Log.i(tag, "DB migration v${startVersion} to v${endVersion} finished.")
        } catch (t: Throwable) {
            // Do extra logging of SQL errors because not all get logged...
            Log.e(tag, "DB migration v${startVersion} to v${endVersion} failed.", t)
            throw t
        }
    }

    /**
     * Should run the necessary migrations.
     *
     * The Migration class cannot access any generated Dao in this method.
     *
     * This method is already called inside a transaction and that transaction might actually be a
     * composite transaction of all necessary `Migration`s.
     *
     * @param db The database instance
     */
    abstract fun doMigrate(db: SupportSQLiteDatabase)
}
