package org.secuso.privacyfriendlytodolist.model.database.tables;

/**
 * Created by dominik on 19.05.16.
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

    // sql table creation
    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + "(" + COLUMN_ID +
            " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_TASK_ID + " TEXT NOT NULL, " + COLUMN_TITLE + " TEXT NOT NULL, " +
            COLUMN_DONE + " INTEGER, FOREIGN KEY (" + COLUMN_TASK_ID + ") REFERENCES " + TTodoTask.TABLE_NAME + "(" + TTodoTask.COLUMN_ID + "));";

}
