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
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.secuso.privacyfriendlytodolist.model.database.dao.TodoListDao
import org.secuso.privacyfriendlytodolist.model.database.dao.TodoSubtaskDao
import org.secuso.privacyfriendlytodolist.model.database.dao.TodoTaskDao
import org.secuso.privacyfriendlytodolist.model.database.entities.TodoListData
import org.secuso.privacyfriendlytodolist.model.database.entities.TodoSubtaskData
import org.secuso.privacyfriendlytodolist.model.database.entities.TodoTaskData

@Database(
    entities = [ TodoListData::class, TodoTaskData::class, TodoSubtaskData::class ],
    version = TodoListDatabase.DATABASE_VERSION,
    exportSchema = true
)
@TypeConverters(DatabaseTypeConverters::class)
abstract class TodoListDatabase : RoomDatabase() {
    abstract fun getTodoListDao(): TodoListDao

    abstract fun getTodoTaskDao(): TodoTaskDao

    abstract fun getTodoSubtaskDao(): TodoSubtaskDao

    companion object {

        const val DATABASE_NAME = "todo_list"
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
                .build()
        }
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        Log.i("MIGRATION_1_2", "DB migration v1 to v2 starts.")

        // drop column description from todo_list
        db.execSQL("PRAGMA foreign_keys=off")
        db.execSQL("BEGIN TRANSACTION")
        db.execSQL("ALTER TABLE todo_list RENAME TO temp_table")
        db.execSQL("CREATE TABLE todo_list (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL)")
        db.execSQL("INSERT INTO todo_list (_id, name) SELECT _id, name FROM temp_table")
        db.execSQL("DROP TABLE temp_table")
        db.execSQL("COMMIT")
        db.execSQL("PRAGMA foreign_keys=on")

        db.execSQL("ALTER TABLE todo_task ADD in_trash INTEGER NOT NULL DEFAULT 0")

        db.execSQL("ALTER TABLE todo_subtask ADD in_trash INTEGER NOT NULL DEFAULT 0")

        Log.i("MIGRATION_1_2", "DB migration v1 to v2 finished.")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        Log.i("MIGRATION_2_3", "DB migration v2 to v3 starts.")

        db.execSQL("PRAGMA foreign_keys=off")
        db.execSQL("BEGIN TRANSACTION")

        // Create statements were taken from app/schemas/org.secuso.privacyfriendlytodolist.model.database.TodoListDatabase/3.json
        var TABLE_NAME = "todoLists"
        db.execSQL("CREATE TABLE IF NOT EXISTS `${TABLE_NAME}` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
        db.execSQL("INSERT INTO $TABLE_NAME (id, name) SELECT _id, name FROM todo_list")

        TABLE_NAME = "todoTasks"
        db.execSQL("CREATE TABLE IF NOT EXISTS `${TABLE_NAME}` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `listId` INTEGER NOT NULL, `listPosition` INTEGER NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `priority` INTEGER NOT NULL, `deadline` INTEGER NOT NULL, `reminderTime` INTEGER NOT NULL, `progress` INTEGER NOT NULL, `isDone` INTEGER NOT NULL, `isInRecycleBin` INTEGER NOT NULL, FOREIGN KEY(`listId`) REFERENCES `todoLists`(`id`))")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_todoTasks_listId` ON `${TABLE_NAME}` (`listId`)")
        db.execSQL("INSERT INTO $TABLE_NAME (id, listId, listPosition, name, description, priority, deadline, reminderTime, progress, isDone, isInRecycleBin) SELECT _id, todo_list_id, position_in_todo_list, name, description, priority, deadline, deadline_warning_time, progress, done, in_trash FROM todo_task")

        TABLE_NAME = "todoSubtasks"
        db.execSQL("CREATE TABLE IF NOT EXISTS `${TABLE_NAME}` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `taskId` INTEGER NOT NULL, `name` TEXT NOT NULL, `isDone` INTEGER NOT NULL, `isInRecycleBin` INTEGER NOT NULL, FOREIGN KEY(`taskId`) REFERENCES `todoTasks`(`id`))")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_todoSubtasks_taskId` ON `${TABLE_NAME}` (`taskId`)")
        db.execSQL("INSERT INTO $TABLE_NAME (id, taskId, name, isDone, isInRecycleBin) SELECT _id, todo_task_id, title, done, in_trash FROM todo_subtask")

        db.execSQL("DROP TABLE todo_subtask")
        db.execSQL("DROP TABLE todo_task")
        db.execSQL("DROP TABLE todo_list")

        db.execSQL("COMMIT")
        db.execSQL("PRAGMA foreign_keys=on")

        Log.i("MIGRATION_2_3", "DB migration v2 to v3 finished.")
    }
}
