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
package org.secuso.privacyfriendlytodolist.androidtest

import android.util.Log
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.model.database.TodoListDatabase
import org.secuso.privacyfriendlytodolist.util.Helper
import org.secuso.privacyfriendlytodolist.util.LogTag
import java.io.IOException


/**
 * [Room DB testing](https://developer.android.com/training/data-storage/room/migrating-db-versions#single-migration-test)
 */
@Suppress("UseWithIndex")
@RunWith(AndroidJUnit4ClassRunner::class)
class DBMigrationTest {
    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
        private const val TEST_DB_NAME = "TodoDatabaseForMigrationTest.db"
        private const val TASK_PRIORITY_BASE = 1000
        private const val TASK_DEADLINE_BASE = 2000
        private const val TASK_PROGRESS_BASE = 3000
        private const val TASK_NUM_SUBTASKS_BASE = 4000
        private const val TASK_DEADLINE_WARNING_TIME_BASE = 5000
    }

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        TodoListDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory())

    @Test
    @Throws(IOException::class)
    fun allMigrationsTest() {
        val db = helper.createDatabase(TEST_DB_NAME, 1)
        db.use {
            populateDBv1(db)
            // Migrate DB to latest DB format.
            helper.runMigrationsAndValidate(TEST_DB_NAME, TodoListDatabase.VERSION,
                false, *TodoListDatabase.ALL_MIGRATIONS)
            // MigrationTestHelper automatically verifies the schema changes,
            // but whether the data was migrated properly gets checked here:
            checkDataAfterMigration(db)
        }
    }

    /**
     * A user described on GitHub at issue #63 that the app did crash at DB migration with
     * android.database.sqlite.SQLiteException: duplicate column name: in_trash (code 1): , while compiling: ALTER TABLE todo_task ADD in_trash INTEGER NOT NULL DEFAULT 0;
     *
     * The root cause of this exception is unknown. But to be able to proceed with DB migration
     * the check with #checkInTrashColumnExists() was added.
     *
     * This test tests if the workaround works.
     */
    @Test
    @Throws(IOException::class)
    fun specificMigrationErrorTest() {
        val db = helper.createDatabase(TEST_DB_NAME, 1)
        db.use {
            // Create a table v1
            populateDBv1(db)
            // Migrate its content to v2 but DB still is marked as v1
            TodoListDatabase.ALL_MIGRATIONS[0].migrate(db)
            // Migrate DB to latest DB format.
            helper.runMigrationsAndValidate(TEST_DB_NAME, TodoListDatabase.VERSION,
                false, *TodoListDatabase.ALL_MIGRATIONS)
            // Check if data was migrated correctly.
            checkDataAfterMigration(db)
        }
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
                val deadline = if (id % 2 == 0) id + TASK_DEADLINE_BASE else -1L
                val reminderTime = if (id % 2 != 0) id + TASK_DEADLINE_WARNING_TIME_BASE else -1L
                query += "('$id', '$listId', '$task', 'Test task $id', 'Test task description $id', '${id + TASK_PRIORITY_BASE}', '$deadline', '${id % 2}', '${id + TASK_PROGRESS_BASE}', '${id + TASK_NUM_SUBTASKS_BASE}', '$reminderTime'), "
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
                query += "('$id', '$taskId', 'Test subtask $id', '${id % 2}'), "
            }
        }
        query = query.removeSuffix(", ")
        db.execSQL(query)
    }

    private fun checkDataAfterMigration(db: SupportSQLiteDatabase) {
        val listIds = ArrayList<Int>()
        var cursor = db.query("SELECT * FROM todoLists")
        cursor.use {
            // Check values
            var listSortOrder = 0
            for (id in 1 .. 3) {
                if (id == 1) {
                    assertTrue(cursor.moveToFirst())
                } else {
                    assertTrue(cursor.moveToNext())
                }

                var col = 0
                assertEquals(id, cursor.getIntOrNull(col++))
                assertEquals(listSortOrder++, cursor.getIntOrNull(col++))
                assertEquals("Test list $id", cursor.getStringOrNull(col++))

                assertEquals(col, cursor.columnCount)
                listIds.add(id)
            }
            assertFalse(cursor.moveToNext())
        }

        val now = Helper.getCurrentTimestamp()
        val nowRange = LongRange(now - 120, now)

        val taskIds = ArrayList<Int>()
        cursor = db.query("SELECT * FROM todoTasks")
        cursor.use {
            // Check values
            var pos = 0
            var id = 0
            val sortOrders = mutableMapOf<Int?, Int>()
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
                    var taskSortOrder = sortOrders[listId]
                    if (null == taskSortOrder) {
                        taskSortOrder = 0
                        sortOrders[listId] = taskSortOrder
                    } else {
                        ++taskSortOrder
                        sortOrders[listId] = taskSortOrder
                    }
                    val deadline = if (id % 2 == 0) id + TASK_DEADLINE_BASE else null
                    val reminderTime = if (id % 2 != 0) id + TASK_DEADLINE_WARNING_TIME_BASE else null
                    val reminderState = if (reminderTime == null) 0 else 1

                    var col = 0
                    assertEquals(id, cursor.getIntOrNull(col++))
                    assertEquals(listId, cursor.getIntOrNull(col++))
                    assertEquals(taskSortOrder, cursor.getIntOrNull(col++))
                    assertEquals("Test task $id", cursor.getStringOrNull(col++))
                    assertEquals("Test task description $id", cursor.getStringOrNull(col++))
                    assertEquals(id + TASK_PRIORITY_BASE, cursor.getIntOrNull(col++))
                    assertEquals(deadline, cursor.getIntOrNull(col++))
                    assertEquals(TodoTask.RecurrencePattern.NONE.ordinal, cursor.getIntOrNull(col++))
                    assertEquals(1, cursor.getIntOrNull(col++)) // Recurrence interval
                    assertEquals(reminderTime, cursor.getIntOrNull(col++))
                    assertEquals(id + TASK_PROGRESS_BASE, cursor.getIntOrNull(col++))
                    assertTrue(cursor.getIntOrNull(col++)?.toLong() in nowRange) // creationTime
                    if (id % 2 != 0) {
                        assertTrue(cursor.getIntOrNull(col++)?.toLong() in nowRange) // doneTime
                    } else {
                        assertEquals(null, cursor.getIntOrNull(col++))
                    }
                    assertEquals(0, cursor.getIntOrNull(col++)) // isInRecycleBin
                    assertEquals(reminderState, cursor.getIntOrNull(col++))

                    assertEquals(col, cursor.columnCount)
                    assertTrue(listId == null || listIds.contains(listId))
                    taskIds.add(id)
                }
            }
            assertFalse(cursor.moveToNext())
        }

        cursor = db.query("SELECT * FROM todoSubtasks")
        cursor.use {
            // Check values
            assertTrue(cursor.moveToFirst())
            var id = 0
            val sortOrders = mutableMapOf<Int, Int>()
            for (taskId in 1..12) {
                val subtaskCount = taskId % 3
                for (subtask in 1..subtaskCount) {
                    ++id
                    if (id == 1) {
                        assertTrue(cursor.moveToFirst())
                    } else {
                        assertTrue(cursor.moveToNext())
                    }
                    var subtaskSortOrder = sortOrders[taskId]
                    if (null == subtaskSortOrder) {
                        subtaskSortOrder = 0
                        sortOrders[taskId] = subtaskSortOrder
                    } else {
                        ++subtaskSortOrder
                        sortOrders[taskId] = subtaskSortOrder
                    }

                    var col = 0
                    assertEquals(id, cursor.getIntOrNull(col++))
                    assertEquals(taskId, cursor.getIntOrNull(col++))
                    assertEquals(subtaskSortOrder, cursor.getIntOrNull(col++))
                    assertEquals("Test subtask $id", cursor.getStringOrNull(col++))
                    if (id % 2 != 0) {
                        assertTrue(cursor.getIntOrNull(col++)?.toLong() in nowRange) // doneTime
                    } else {
                        assertEquals(null, cursor.getIntOrNull(col++))
                    }
                    assertEquals(0, cursor.getIntOrNull(col++))

                    assertEquals(col, cursor.columnCount)
                    assertTrue(taskIds.contains(taskId))
                }
            }
            assertFalse(cursor.moveToNext())
        }

        Log.i(TAG, "Check of DB migration to v${TodoListDatabase.VERSION} passed.")
    }
}