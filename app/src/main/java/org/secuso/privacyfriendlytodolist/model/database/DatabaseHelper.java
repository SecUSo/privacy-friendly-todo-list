package org.secuso.privacyfriendlytodolist.model.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.secuso.privacyfriendlytodolist.model.database.tables.TDefaultSettings;
import org.secuso.privacyfriendlytodolist.model.database.tables.TTodoList;
import org.secuso.privacyfriendlytodolist.model.database.tables.TTodoSubTask;
import org.secuso.privacyfriendlytodolist.model.database.tables.TTodoTask;

/**
 * Created by dominik on 19.05.16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "TodoDatabase.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TTodoList.TABLE_CREATE);
        db.execSQL(TTodoTask.TABLE_CREATE);
        db.execSQL(TTodoSubTask.TABLE_CREATE);
        db.execSQL(TDefaultSettings.TABLE_CREATE);

        Log.i(TAG, "onCreate() finished");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DELETE FROM " + TTodoList.TABLE_NAME);
        db.execSQL("DELETE FROM " + TTodoTask.TABLE_NAME);
        db.execSQL("DELETE FROM " + TTodoSubTask.TABLE_NAME);
        db.execSQL("DELETE FROM " + TDefaultSettings.TABLE_NAME);

        onCreate(db);
        Log.i(TAG, "onUpgrade() finished");
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {

    }
}
