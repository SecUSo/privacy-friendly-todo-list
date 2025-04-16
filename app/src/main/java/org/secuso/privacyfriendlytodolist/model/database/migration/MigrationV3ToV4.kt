/*
Privacy Friendly To-Do List
Copyright (C) 2024-2025  Christian Adams

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

import androidx.sqlite.db.SupportSQLiteDatabase
import org.secuso.privacyfriendlytodolist.util.Helper
import org.secuso.privacyfriendlytodolist.util.LogTag

class MigrationV3ToV4: MigrationBase(3, 4) {
    override fun doMigrate(db: SupportSQLiteDatabase) {
        db.execSQL("BEGIN TRANSACTION")

        // Nothing to do for "todoLists"

        db.execSQL("ALTER TABLE todoTasks ADD reminderState INTEGER NOT NULL DEFAULT `0`")
        // Set all reminders as done where the reminder time is in the past.
        val now = Helper.getCurrentTimestamp()
        db.execSQL("UPDATE todoTasks SET reminderState = 1 WHERE reminderTime IS NOT NULL AND reminderTime < $now")

        // Nothing to do for "todoSubtasks"

        db.execSQL("END TRANSACTION")
    }

    override val tag = TAG

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
    }
}