package org.secuso.privacyfriendlytodolist.model.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.secuso.privacyfriendlytodolist.model.database.tables.TTodoList;
import org.secuso.privacyfriendlytodolist.model.database.tables.TTodoSubTask;
import org.secuso.privacyfriendlytodolist.model.database.tables.TTodoTask;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    private static DatabaseHelper mInstance = null;

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "TodoDatabase.db";

    public static DatabaseHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return mInstance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    public void deleteAll(SQLiteDatabase db) {
        db.execSQL("DROP TABLE " + TTodoList.TABLE_NAME);
        db.execSQL("DROP TABLE " + TTodoTask.TABLE_NAME);
        db.execSQL("DROP TABLE " + TTodoSubTask.TABLE_NAME);
    }

    public void deleteAll() {
        deleteAll(this.getWritableDatabase());
    }

    public void createAll(SQLiteDatabase db) {
        db.execSQL(TTodoList.TABLE_CREATE);
        db.execSQL(TTodoTask.TABLE_CREATE);
        db.execSQL(TTodoSubTask.TABLE_CREATE);
    }

    public void createAll() {
        createAll(this.getWritableDatabase());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createAll(db);

        Log.i(TAG, "onCreate() finished");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        deleteAll(db);
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
