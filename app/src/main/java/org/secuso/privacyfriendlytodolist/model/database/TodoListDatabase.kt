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
package org.secuso.privacyfriendlytodolist.model.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.secuso.privacyfriendlytodolist.model.database.dao.TodoListDao
import org.secuso.privacyfriendlytodolist.model.database.dao.TodoSubtaskDao
import org.secuso.privacyfriendlytodolist.model.database.dao.TodoTaskDao
import org.secuso.privacyfriendlytodolist.model.database.entities.TodoListData
import org.secuso.privacyfriendlytodolist.model.database.entities.TodoSubtaskData
import org.secuso.privacyfriendlytodolist.model.database.entities.TodoTaskData
import org.secuso.privacyfriendlytodolist.model.database.migration.MigrationV1ToV2
import org.secuso.privacyfriendlytodolist.model.database.migration.MigrationV2ToV3
import org.secuso.privacyfriendlytodolist.util.LogTag

@Database(
    entities = [ TodoListData::class, TodoTaskData::class, TodoSubtaskData::class ],
    version = TodoListDatabase.VERSION,
    exportSchema = true
)
@TypeConverters(DatabaseTypeConverters::class)
abstract class TodoListDatabase : RoomDatabase() {
    abstract fun getTodoListDao(): TodoListDao

    abstract fun getTodoTaskDao(): TodoTaskDao

    abstract fun getTodoSubtaskDao(): TodoSubtaskDao

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
        const val NAME = "TodoDatabase.db"
        const val VERSION = 3
        private var instance: TodoListDatabase? = null

        fun getInstance(context: Context): TodoListDatabase {
            synchronized(NAME) {
                if (instance == null) {
                    instance = createDatabase(context)
                }
                return instance!!
            }
        }

        private fun createDatabase(context: Context): TodoListDatabase {
            val builder = Room.databaseBuilder(context.applicationContext,
                TodoListDatabase::class.java, NAME)
            builder.addMigrations(*ALL_MIGRATIONS)
            val db = builder.build()
            Log.d(TAG, "$NAME was created.")
            return db
        }

        val ALL_MIGRATIONS = arrayOf(MigrationV1ToV2(), MigrationV2ToV3())
    }
}
