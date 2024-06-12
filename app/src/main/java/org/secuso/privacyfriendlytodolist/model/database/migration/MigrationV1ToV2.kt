package org.secuso.privacyfriendlytodolist.model.database.migration

import android.util.Log
import androidx.sqlite.db.SupportSQLiteDatabase
import org.secuso.privacyfriendlytodolist.util.LogTag

class MigrationV1ToV2 : MigrationBase(1, 2) {
    override fun doMigrate(db: SupportSQLiteDatabase) {
        // drop column description from todo_list
        db.execSQL("PRAGMA foreign_keys=off")
        db.execSQL("BEGIN TRANSACTION")
        db.execSQL("ALTER TABLE todo_list RENAME TO temp_table")
        db.execSQL("CREATE TABLE todo_list (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL)")
        db.execSQL("INSERT INTO todo_list (_id, name) SELECT _id, name FROM temp_table")
        db.execSQL("DROP TABLE temp_table")
        db.execSQL("COMMIT")
        db.execSQL("PRAGMA foreign_keys=on")

        /*
        A user described on GitHub at issue #63 that the app did crash at DB migration with
        android.database.sqlite.SQLiteException: duplicate column name: in_trash (code 1): , while compiling: ALTER TABLE todo_task ADD in_trash INTEGER NOT NULL DEFAULT 0;

        The root cause of this exception is unknown. But to be able to proceed with DB migration
        the check with #checkInTrashColumnExists() was added.
        */
        if (!checkInTrashColumnExists(db, "todo_task")) {
            db.execSQL("ALTER TABLE todo_task ADD in_trash INTEGER NOT NULL DEFAULT 0")
        }
        if (!checkInTrashColumnExists(db, "todo_subtask")) {
            db.execSQL("ALTER TABLE todo_subtask ADD in_trash INTEGER NOT NULL DEFAULT 0")
        }
    }

    private fun checkInTrashColumnExists(db: SupportSQLiteDatabase, tableName: String): Boolean {
        val columnName = "in_trash"
        var wasFound = false
        val cursor = db.query("PRAGMA table_info($tableName)")
        cursor.use {
            var hasData = cursor.moveToFirst()
            while (hasData) {
                val index = cursor.getColumnIndex("name")
                if (index >= 0) {
                    if (cursor.getString(index) == columnName) {
                        wasFound = true
                        break
                    }
                } else {
                    Log.e(TAG, "Column 'name' not found in table info.")
                }
                hasData = cursor.moveToNext()
            }
        }

        val found = if (wasFound) "found" else "not found"
        Log.i(TAG, "Column $columnName $found in table $tableName.")
        return wasFound
    }

    override val tag = TAG

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
    }
}