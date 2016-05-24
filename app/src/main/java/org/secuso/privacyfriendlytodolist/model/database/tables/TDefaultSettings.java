package org.secuso.privacyfriendlytodolist.model.database.tables;

/**
 * Created by dominik on 19.05.16.
 */
public class TDefaultSettings {

    private static final String TAG = TDefaultSettings.class.getSimpleName();

    // columns + tablename
    public static final String TABLE_NAME = "default_settings";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DEADLINE_WARNING_BORDER = "deadline_warning_border";

    // sql table creation
    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + "(" + COLUMN_ID +
            " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_DEADLINE_WARNING_BORDER + " NUMERIC NOT NULL);";
}
