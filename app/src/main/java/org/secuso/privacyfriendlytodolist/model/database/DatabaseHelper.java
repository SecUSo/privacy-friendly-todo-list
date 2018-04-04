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

package org.secuso.privacyfriendlytodolist.model.database;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import org.secuso.privacyfriendlytodolist.model.database.tables.TTodoList;
import org.secuso.privacyfriendlytodolist.model.database.tables.TTodoSubTask;
import org.secuso.privacyfriendlytodolist.model.database.tables.TTodoTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

/**
 *
 * Created by Sebastian Lutz on 13.3.2018.
 *
 * This class extends SQLiteOpenHelper and is responsible for fundamental things such as:
 *
 *  - Create all tables mentioned above (#createAll)
 *  - Delete all tables (#deleteAll)
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    private static DatabaseHelper mInstance = null;
    private Context context;

    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "TodoDatabase.db";

    public static DatabaseHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return mInstance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
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

    /**
     * Taken from https://riggaroo.co.za/android-sqlite-database-use-onupgrade-correctly/ .
     * @param db the writeable database to update.
     * @param oldVersion the old version to update from
     * @param newVersion the new version to update to
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e(TAG, "Updating table from " + oldVersion + " to " + newVersion);
        // You will not need to modify this unless you need to do some android specific things.
        // When upgrading the database, all you need to do is add a file to the assets folder and name it:
        // from_1_to_2.sql with the version that you are upgrading to as the last version.
        for (int i = oldVersion; i < newVersion; ++i) {
            String migrationName = String.format(Locale.ENGLISH, "from_%d_to_%d.sql", i, (i + 1));
            Log.d(TAG, "Looking for migration file: " + migrationName);
            readAndExecuteSQLScript(db, context, migrationName);
        }

    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    private void readAndExecuteSQLScript(SQLiteDatabase db, Context ctx, String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            Log.d(TAG, "SQL script file name is empty");
            return;
        }

        Log.d(TAG, "Script found. Executing...");
        AssetManager assetManager = ctx.getAssets();
        BufferedReader reader = null;

        try {
            InputStream is = assetManager.open(fileName);
            InputStreamReader isr = new InputStreamReader(is);
            reader = new BufferedReader(isr);
            executeSQLScript(db, reader);
        } catch (IOException e) {
            Log.e(TAG, "IOException:", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "IOException:", e);
                }
            }
        }

    }

    private void executeSQLScript(SQLiteDatabase db, BufferedReader reader) throws IOException {
        String line;
        StringBuilder statement = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            statement.append(line);
            statement.append("\n");
            if (line.endsWith(";")) {
                db.execSQL(statement.toString());
                statement = new StringBuilder();
            }
        }
    }
}
