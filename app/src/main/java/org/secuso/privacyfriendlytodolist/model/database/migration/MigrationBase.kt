/*
Privacy Friendly To-Do List
Copyright (C) 2024  Christian Adams

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
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
