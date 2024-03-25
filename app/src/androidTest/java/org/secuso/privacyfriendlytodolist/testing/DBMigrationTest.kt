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

package org.secuso.privacyfriendlytodolist.testing

import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.secuso.privacyfriendlytodolist.model.database.TodoListDatabase
import java.io.IOException

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue


/**
 * [Room DB testing](https://developer.android.com/training/data-storage/room/migrating-db-versions#single-migration-test)
 */
@RunWith(AndroidJUnit4ClassRunner::class)
class DBMigrationTest {
    companion object {
        private const val testDB = "todo_list_migration_test"
        private const val task_priority_base = 1000
        private const val task_deadline_base = 2000
        private const val task_done_base = 3000
        private const val task_progress_base = 4000
        private const val task_num_subtasks_base = 5000
        private const val task_deadline_warning_time_base = 6000
        private const val subtask_done_base = 10000
    }

    private val allMigrations = arrayOf(TodoListDatabase.MIGRATION_1_2, TodoListDatabase.MIGRATION_2_3)

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        TodoListDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory())

    @Test
    @Throws(IOException::class)
    fun allMigrationsTest() {
        var db = helper.createDatabase(testDB, 1)
        populateDBv1(db)
        db.close()

        // Open latest version of the database. Room validates the schema  once all migrations execute.
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        Room.databaseBuilder(context, TodoListDatabase::class.java, testDB)
            .addMigrations(*allMigrations)
            .build()
            .apply {
                db = openHelper.readableDatabase
                checkDBv3(db)
                db.close()
            }
        // MigrationTestHelper automatically verifies the schema changes,
        // but you need to validate that the data was migrated properly.
    }

    private fun populateDBv1(db: SupportSQLiteDatabase) {
        db.execSQL("INSERT INTO todo_list (_id, name, description) VALUES "
                + "('1', 'Test list 1', 'Test list description 1'),"
                + "('2', 'Test list 2', 'Test list description 2'),"
                + "('3', 'Test list 3', 'Test list description 3')")

        var query = "INSERT INTO todo_task (_id, todo_list_id, position_in_todo_list, name, description, priority, deadline, done, progress, num_subtasks, deadline_warning_time) VALUES "
        var pos = 0
        var id = 0
        for (listCounter in 1..3) {
            ++pos
            for (task in 1..4) {
                ++id
                // Create the scenario where tasks have listId's that do not point to a list.
                // Seen with listId 0 or -3. Use different not existing values here, if listCounter is 3.
                val listId = if (listCounter != 3) listCounter else 1 - task
                query += "('$id', '$listId', '$task', 'Test task $id', 'Test task description $id', '${id + task_priority_base}', '${id + task_deadline_base}', '${id + task_done_base}', '${id + task_progress_base}', '${id + task_num_subtasks_base}', '${id + task_deadline_warning_time_base}'), "
            }
        }
        query = query.removeSuffix(", ")
        db.execSQL(query)

        query = "INSERT INTO todo_subtask (_id, todo_task_id, title, done) VALUES "
        id = 0
        for (taskId in 1..12) {
            val subtaskCount = taskId % 3
            for (subtask in 1..subtaskCount) {
                ++id
                query += "('$id', '$taskId', 'Test subtask $id', '${id + subtask_done_base}'), "
            }
        }
        query = query.removeSuffix(", ")
        db.execSQL(query)
    }

    private fun checkDBv3(db: SupportSQLiteDatabase) {
        val listIds = ArrayList<Int>()
        var cursor = db.query("SELECT * FROM todoLists")
        // Check if "description" column was removed
        assertEquals(2, cursor.columnCount)
        // Check values
        for (id in 1 .. 3) {
            if (id == 1) {
                assertTrue(cursor.moveToFirst())
            } else {
                assertTrue(cursor.moveToNext())
            }

            var col = 0
            assertEquals(id, cursor.getIntOrNull(col++))
            assertEquals("Test list $id", cursor.getStringOrNull(col))
            listIds.add(id)
        }
        assertFalse(cursor.moveToNext())
        cursor.close()

        val taskIds = ArrayList<Int>()
        cursor = db.query("SELECT * FROM todoTasks")
        assertEquals(11, cursor.columnCount)
        // Check values
        var pos = 0
        var id = 0
        for (listCounter in 1..3) {
            ++pos
            for (task in 1..4) {
                ++id
                if (id == 1) {
                    assertTrue(cursor.moveToFirst())
                } else {
                    assertTrue(cursor.moveToNext())
                }
                val listId: Int? = if (listCounter != 3) listCounter else null
                var col = 0
                assertEquals(id, cursor.getIntOrNull(col++))
                assertEquals(listId, cursor.getIntOrNull(col++))
                assertEquals(task, cursor.getIntOrNull(col++))
                assertEquals("Test task $id", cursor.getStringOrNull(col++))
                assertEquals("Test task description $id", cursor.getStringOrNull(col++))
                assertEquals(id + task_priority_base, cursor.getIntOrNull(col++))
                assertEquals(id + task_deadline_base, cursor.getIntOrNull(col++))
                assertEquals(id + task_deadline_warning_time_base, cursor.getIntOrNull(col++))
                assertEquals(id + task_progress_base, cursor.getIntOrNull(col++))
                assertEquals(id + task_done_base, cursor.getIntOrNull(col++))
                assertEquals(0, cursor.getIntOrNull(col))
                assertTrue(listId == null || listIds.contains(listId))
                taskIds.add(id)
            }
        }
        assertFalse(cursor.moveToNext())
        cursor.close()

        cursor = db.query("SELECT * FROM todoSubtasks")
        assertEquals(5, cursor.columnCount)
        // Check values
        assertTrue(cursor.moveToFirst())
        id = 0
        for (taskId in 1..12) {
            val subtaskCount = taskId % 3
            for (subtask in 1..subtaskCount) {
                ++id
                if (id == 1) {
                    assertTrue(cursor.moveToFirst())
                } else {
                    assertTrue(cursor.moveToNext())
                }

                var col = 0
                assertEquals(id, cursor.getIntOrNull(col++))
                assertEquals(taskId, cursor.getIntOrNull(col++))
                assertEquals("Test subtask $id", cursor.getStringOrNull(col++))
                assertEquals(id + subtask_done_base, cursor.getIntOrNull(col++))
                assertEquals(0, cursor.getIntOrNull(col))
                assertTrue(taskIds.contains(taskId))
            }
        }
        assertFalse(cursor.moveToNext())
        cursor.close()
    }
}