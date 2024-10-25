package org.secuso.privacyfriendlytodolist.model.database.migration

import androidx.sqlite.db.SupportSQLiteDatabase
import org.secuso.privacyfriendlytodolist.util.Helper
import org.secuso.privacyfriendlytodolist.util.LogTag

class MigrationV2ToV3: MigrationBase(2, 3) {
    override fun doMigrate(db: SupportSQLiteDatabase) {
        db.execSQL("BEGIN TRANSACTION")

        // Create statements were taken from app/schemas/org.secuso.privacyfriendlytodolist.model.database.TodoListDatabase/3.json
        var TABLE_NAME = "todoLists"
        db.execSQL("CREATE TABLE IF NOT EXISTS `${TABLE_NAME}` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sortOrder` INTEGER NOT NULL, `name` TEXT NOT NULL)")
        // Use ID as sort order. This keeps original order.
        db.execSQL("INSERT INTO $TABLE_NAME (id, sortOrder, name)" +
                " SELECT _id, _id, name FROM todo_list")
        // Set sort order by ascending ID which keeps original order.
        var cursor = db.query("SELECT id FROM $TABLE_NAME ORDER BY id ASC")
        cursor.use {
            var sortOrder1 = 0
            var wasMoved = cursor.moveToFirst()
            while (wasMoved) {
                db.execSQL("UPDATE `${TABLE_NAME}` SET `sortOrder` = $sortOrder1 WHERE id = ${cursor.getInt(0)}")
                ++sortOrder1
                wasMoved = cursor.moveToNext()
            }
        }

        TABLE_NAME = "todoTasks"
        db.execSQL("CREATE TABLE IF NOT EXISTS `${TABLE_NAME}` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `listId` INTEGER, `sortOrder` INTEGER NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `priority` INTEGER NOT NULL, `deadline` INTEGER, `recurrencePattern` INTEGER NOT NULL, `reminderTime` INTEGER, `progress` INTEGER NOT NULL, `creationTime` INTEGER NOT NULL, `doneTime` INTEGER, `isInRecycleBin` INTEGER NOT NULL, FOREIGN KEY(`listId`) REFERENCES `todoLists`(`id`) ON UPDATE CASCADE ON DELETE SET NULL )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_todoTasks_listId` ON `${TABLE_NAME}` (`listId`)")
        // Copy existing columns, fill new columns with any integer that is not null to fulfill not-null constraint.
        db.execSQL("INSERT INTO $TABLE_NAME (creationTime, recurrencePattern, id, listId, sortOrder, name, description, priority, deadline, reminderTime, progress, doneTime, isInRecycleBin)" +
                " SELECT _id, _id, _id, todo_list_id, _id, name, description, priority, deadline, deadline_warning_time, progress, done, in_trash FROM todo_task")
        // In v2 the listId can be a non-existing list-ID (0 or -3 occurs) to show that task does not
        // belong to a list. Now at v3 this violates FK Constraint and leads to an exception.
        // Its unknown why this does not lead to an exception at v2. v2 also has the FK constraints.
        // Find all listId's that do not point to a list and set them to null.
        // null is the only value to indicate that foreign key is not set. See https://www.sqlite.org/foreignkeys.html
        db.execSQL("UPDATE `${TABLE_NAME}` SET `listId` = null WHERE `listId` NOT IN (SELECT `id` FROM `todoLists`)")
        // "No timestamp" was -1 and is now NULL.
        db.execSQL("UPDATE `${TABLE_NAME}` SET `deadline` = NULL WHERE `deadline` = -1")
        db.execSQL("UPDATE `${TABLE_NAME}` SET `reminderTime` = NULL WHERE `reminderTime` = -1")
        // Boolean done (0 or 1) has changed to doneTime (timestamp if done, otherwise null).
        val now = Helper.getCurrentTimestamp()
        db.execSQL("UPDATE `${TABLE_NAME}` SET `doneTime` = $now WHERE `doneTime` <> 0")
        db.execSQL("UPDATE `${TABLE_NAME}` SET `doneTime` = NULL WHERE `doneTime` = 0")
        // Set default value in new column creationTime which is the current time in seconds.
        db.execSQL("UPDATE `${TABLE_NAME}` SET `creationTime` = $now")
        // Set default value in new column recurrencePattern.
        db.execSQL("UPDATE `${TABLE_NAME}` SET `recurrencePattern` = 0")
        // Set sort order by ascending ID which keeps original order. position_in_todo_list was never used so ignore it.
        cursor = db.query("SELECT id, listId FROM $TABLE_NAME ORDER BY id ASC")
        cursor.use {
            val sortOrders = mutableMapOf<Int, Int>()
            var wasMoved = cursor.moveToFirst()
            while (wasMoved) {
                val id = cursor.getInt(0)
                val listId = cursor.getInt(1)
                var sortOrder2 = sortOrders[listId]
                if (null == sortOrder2) {
                    sortOrder2 = 0
                }
                db.execSQL("UPDATE `${TABLE_NAME}` SET `sortOrder` = $sortOrder2 WHERE id = $id")
                sortOrders[listId] = sortOrder2 + 1
                wasMoved = cursor.moveToNext()
            }
        }

        TABLE_NAME = "todoSubtasks"
        db.execSQL("CREATE TABLE IF NOT EXISTS `${TABLE_NAME}` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `taskId` INTEGER NOT NULL, `sortOrder` INTEGER NOT NULL, `name` TEXT NOT NULL, `doneTime` INTEGER, `isInRecycleBin` INTEGER NOT NULL, FOREIGN KEY(`taskId`) REFERENCES `todoTasks`(`id`) ON UPDATE CASCADE ON DELETE CASCADE )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_todoSubtasks_taskId` ON `${TABLE_NAME}` (`taskId`)")
        // Use ID as sort order. This keeps original order.
        db.execSQL("INSERT INTO $TABLE_NAME (id, taskId, sortOrder, name, doneTime, isInRecycleBin)" +
                " SELECT _id, todo_task_id, _id, title, done, in_trash FROM todo_subtask")
        // Boolean done (0 or 1) has changed to doneTime (timestamp if done, otherwise null).
        db.execSQL("UPDATE `${TABLE_NAME}` SET `doneTime` = $now WHERE `doneTime` <> 0")
        db.execSQL("UPDATE `${TABLE_NAME}` SET `doneTime` = NULL WHERE `doneTime` = 0")
        // Set sort order by ascending ID which keeps original order.
        cursor = db.query("SELECT id, taskId FROM $TABLE_NAME ORDER BY id ASC")
        cursor.use {
            val sortOrders = mutableMapOf<Int, Int>()
            var wasMoved = cursor.moveToFirst()
            while (wasMoved) {
                val id = cursor.getInt(0)
                val taskId = cursor.getInt(1)
                var sortOrder3 = sortOrders[taskId]
                if (null == sortOrder3) {
                    sortOrder3 = 0
                }
                db.execSQL("UPDATE `${TABLE_NAME}` SET `sortOrder` = $sortOrder3 WHERE id = $id")
                sortOrders[taskId] = sortOrder3 + 1
                wasMoved = cursor.moveToNext()
            }
        }

        db.execSQL("DROP TABLE todo_subtask")
        db.execSQL("DROP TABLE todo_task")
        db.execSQL("DROP TABLE todo_list")

        db.execSQL("END TRANSACTION")
    }

    override val tag = TAG

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
    }
}