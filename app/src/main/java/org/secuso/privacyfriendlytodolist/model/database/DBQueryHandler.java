package org.secuso.privacyfriendlytodolist.model.database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import org.secuso.privacyfriendlytodolist.model.database.tables.TTodoList;

/**
 * Created by dominik on 19.05.16.
 *
 * This class encapsulates all sql statements and returns
 */
public class DBQueryHandler {

    public static long insertNewTodoList(SQLiteDatabase db, String name, String description, long deadline) {
        ContentValues values = new ContentValues();
        values.put(TTodoList.COLUMN_NAME, name);
        values.put(TTodoList.COLUMN_DESCRIPTION, description);
        if (deadline != -1)
            values.put(TTodoList.COLUMN_DEADLINE, deadline);

        return db.insert(TTodoList.TABLE_NAME, null, values);
    }
}
