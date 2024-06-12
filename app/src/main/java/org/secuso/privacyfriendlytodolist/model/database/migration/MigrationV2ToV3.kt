package org.secuso.privacyfriendlytodolist.model.database.migration

import androidx.sqlite.db.SupportSQLiteDatabase
import org.secuso.privacyfriendlytodolist.util.Helper
import org.secuso.privacyfriendlytodolist.util.LogTag

class MigrationV2ToV3: MigrationBase(2, 3) {
    override fun doMigrate(db: SupportSQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys=off")
        db.execSQL("BEGIN TRANSACTION")

        // Create statements were taken from app/schemas/org.secuso.privacyfriendlytodolist.model.database.TodoListDatabase/3.json
        var TABLE_NAME = "todoLists"
        db.execSQL("CREATE TABLE IF NOT EXISTS `${TABLE_NAME}` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
        db.execSQL("INSERT INTO $TABLE_NAME (id, name) SELECT _id, name FROM todo_list")

        TABLE_NAME = "todoTasks"
        db.execSQL("CREATE TABLE IF NOT EXISTS `${TABLE_NAME}` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `listId` INTEGER, `listPosition` INTEGER NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `priority` INTEGER NOT NULL, `deadline` INTEGER NOT NULL, `recurrencePattern` INTEGER NOT NULL, `reminderTime` INTEGER NOT NULL, `progress` INTEGER NOT NULL, `creationTime` INTEGER NOT NULL, `doneTime` INTEGER NOT NULL, `isInRecycleBin` INTEGER NOT NULL, FOREIGN KEY(`listId`) REFERENCES `todoLists`(`id`) ON UPDATE CASCADE ON DELETE SET NULL )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_todoTasks_listId` ON `${TABLE_NAME}` (`listId`)")
        // Copy existing columns, fill new columns with any integer that is not null to fulfill not-null constraint.
        db.execSQL("INSERT INTO $TABLE_NAME (creationTime, recurrencePattern, id, listId, listPosition, name, description, priority, deadline, reminderTime, progress, doneTime, isInRecycleBin) " +
                "SELECT _id, _id, _id, todo_list_id, position_in_todo_list, name, description, priority, deadline, deadline_warning_time, progress, done, in_trash FROM todo_task")
        // In v2 the listId can be a non-existing list-ID (0 or -3 occurs) to show that task does not
        // belong to a list. Now at v3 this violates FK Constraint and leads to an exception.
        // Its unknown why this does not lead to an exception at v2. v2 also has the FK constraints.
        // Find all listId's that do not point to a list and set them to null.
        // null is the only value to indicate that foreign key is not set. See https://www.sqlite.org/foreignkeys.html
        db.execSQL("UPDATE `${TABLE_NAME}` SET `listId` = null WHERE `listId` NOT IN (SELECT `id` FROM `todoLists`)")
        // listPosition (position_in_todo_list) was not used before. Ensure same initial value of -1.
        db.execSQL("UPDATE `${TABLE_NAME}` SET `listPosition` = -1")
        // Boolean done (0 or 1) has changed to doneTime (-1 or timestamp when set as done).
        val now = Helper.getCurrentTimestamp()
        db.execSQL("UPDATE `${TABLE_NAME}` SET `doneTime` = $now WHERE `doneTime` <> 0")
        db.execSQL("UPDATE `${TABLE_NAME}` SET `doneTime` = -1 WHERE `doneTime` = 0")
        // Set default value for new column creationTime which is the current time in seconds.
        db.execSQL("UPDATE `${TABLE_NAME}` SET `creationTime` = $now")
        // Set default value for new column recurrencePattern which is zero.
        db.execSQL("UPDATE `${TABLE_NAME}` SET `recurrencePattern` = 0")

        TABLE_NAME = "todoSubtasks"
        db.execSQL("CREATE TABLE IF NOT EXISTS `${TABLE_NAME}` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `taskId` INTEGER NOT NULL, `name` TEXT NOT NULL, `doneTime` INTEGER NOT NULL, `isInRecycleBin` INTEGER NOT NULL, FOREIGN KEY(`taskId`) REFERENCES `todoTasks`(`id`) ON UPDATE CASCADE ON DELETE CASCADE )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_todoSubtasks_taskId` ON `${TABLE_NAME}` (`taskId`)")
        db.execSQL("INSERT INTO $TABLE_NAME (id, taskId, name, doneTime, isInRecycleBin) SELECT _id, todo_task_id, title, done, in_trash FROM todo_subtask")
        // Boolean done (0 or 1) has changed to doneTime (-1 or timestamp when set as done).
        db.execSQL("UPDATE `${TABLE_NAME}` SET `doneTime` = $now WHERE `doneTime` <> 0")
        db.execSQL("UPDATE `${TABLE_NAME}` SET `doneTime` = -1 WHERE `doneTime` = 0")

        db.execSQL("DROP TABLE todo_subtask")
        db.execSQL("DROP TABLE todo_task")
        db.execSQL("DROP TABLE todo_list")

        db.execSQL("COMMIT")
        db.execSQL("PRAGMA foreign_keys=on")
    }

    override val tag = TAG

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
    }
}