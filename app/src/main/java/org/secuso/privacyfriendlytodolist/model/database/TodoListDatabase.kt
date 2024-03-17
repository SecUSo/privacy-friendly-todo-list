/*
 This file is part of Privacy Friendly To-Do List.

 Privacy Friendly To-Do List is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.

 Privacy Friendly To-Do List is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Privacy Friendly To-Do List. If not, see <http://www.gnu.org/licenses/>.
 */

package org.secuso.privacyfriendlytodolist.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.secuso.privacyfriendlytodolist.model.database.dao.TodoListDao
import org.secuso.privacyfriendlytodolist.model.database.dao.TodoSubtaskDao
import org.secuso.privacyfriendlytodolist.model.database.dao.TodoTaskDao
import org.secuso.privacyfriendlytodolist.model.database.entities.TodoListData
import org.secuso.privacyfriendlytodolist.model.database.entities.TodoSubtaskData
import org.secuso.privacyfriendlytodolist.model.database.entities.TodoTaskData

/**
 * @TODO Change foreign keys from Long to Room ForeignKey.
 *
 */
@Database(
    entities = [ TodoListData::class, TodoTaskData::class, TodoSubtaskData::class ],
    version = TodoListDatabase.DATABASE_VERSION,
    exportSchema = true
)
abstract class TodoListDatabase : RoomDatabase() {
    abstract fun getTodoListDao(): TodoListDao

    abstract fun getTodoTaskDao(): TodoTaskDao

    abstract fun getTodoSubtaskDao(): TodoSubtaskDao

    companion object {

        const val DATABASE_NAME = "todo_list_db_room"

        const val DATABASE_VERSION = 3

        private var instance: TodoListDatabase? = null

        fun getInstance(context: Context): TodoListDatabase {
            synchronized(DATABASE_NAME) {
                if (instance == null) {
                    instance = createDatabase(context)
                }
                return instance!!
            }
        }

        private fun createDatabase(context: Context): TodoListDatabase {
            return Room.databaseBuilder(context.applicationContext,
                TodoListDatabase::class.java, DATABASE_NAME
            )
                .addMigrations(MIGRATION_1_2)
                .addMigrations(MIGRATION_2_3)
                .addCallback(roomCallback)
                .build()
        }

        private val roomCallback: Callback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // TODO: add migration
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // TODO: add migration
            }
        }
    }
}