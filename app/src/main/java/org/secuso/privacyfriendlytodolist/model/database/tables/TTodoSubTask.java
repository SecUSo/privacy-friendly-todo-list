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

package org.secuso.privacyfriendlytodolist.model.database.tables;

/**
 * This Class is responsible for creating sql table for To-Do subtasks.
 */

public final class TTodoSubTask {

    // TAG
    private static final String TAG = TTodoList.class.getSimpleName();

    // columns + tablename
    public static final String TABLE_NAME = "todo_subtask";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TASK_ID = "todo_task_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DONE = "done";
    public static final String COLUMN_TRASH = "in_trash";

    // sql table creation
    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + "(" + COLUMN_ID +
            " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_TASK_ID + " INTEGER NOT NULL, " + COLUMN_TITLE + " TEXT NOT NULL, " +
            COLUMN_DONE + " INTEGER, " + COLUMN_TRASH + " INTEGER NOT NULL DEFAULT 0, FOREIGN KEY (" + COLUMN_TASK_ID + ") REFERENCES " + TTodoTask.TABLE_NAME + "(" + TTodoTask.COLUMN_ID + "));";

}
//  , " + COLUMN_TRASH + " INTEGER NOT NULL DEFAULT 0