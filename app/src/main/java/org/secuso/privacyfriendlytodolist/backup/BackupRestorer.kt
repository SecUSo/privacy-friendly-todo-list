package org.secuso.privacyfriendlytodolist.backup

import android.content.Context
import android.preference.PreferenceManager
import android.util.JsonReader
import org.secuso.privacyfriendlybackup.api.backup.DatabaseUtil
import org.secuso.privacyfriendlybackup.api.backup.DatabaseUtil.readDatabaseContent
import org.secuso.privacyfriendlybackup.api.backup.FileUtil.copyFile
import org.secuso.privacyfriendlybackup.api.pfa.IBackupRestorer
import org.secuso.privacyfriendlytodolist.model.database.DatabaseHelper.DATABASE_NAME
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

class BackupRestorer : IBackupRestorer {

    @Throws(IOException::class)
    private fun readDatabase(reader: JsonReader, context: Context) {
        reader.beginObject()
        val n1 = reader.nextName()
        if (n1 != "version") {
            throw java.lang.RuntimeException("Unknown value $n1")
        }
        val version = reader.nextInt()
        val n2 = reader.nextName()
        if (n2 != "content") {
            throw java.lang.RuntimeException("Unknown value $n2")
        }
        val db = DatabaseUtil.getSupportSQLiteOpenHelper(context, "restoreDatabase", version).writableDatabase
        db.beginTransaction()
        db.version = version
        readDatabaseContent(reader, db)
        db.setTransactionSuccessful()
        db.endTransaction()
        db.close()
        reader.endObject()

        // copy file to correct location
        val databaseFile = context.getDatabasePath("restoreDatabase")
        copyFile(databaseFile, context.getDatabasePath(DATABASE_NAME))
        databaseFile.delete()
    }

    @Throws(IOException::class)
    private fun readPreferences(reader: JsonReader, context: Context) {
        reader.beginObject()
        val pref = PreferenceManager.getDefaultSharedPreferences(context).edit()
        while (reader.hasNext()) {
            when (val name = reader.nextName()) {
                "pref_pin_enabled", "notify", "pref_progress" -> pref.putBoolean(name, reader.nextBoolean())
                "pref_pin" -> pref.putString(name, reader.nextString()) // TODO maybe leave this out
                "pref_default_reminder_time" -> pref.putString(name, reader.nextString())
                else -> throw java.lang.RuntimeException("Unknown preference $name")
            }
        }
        pref.commit()
        reader.endObject()
    }

    override fun restoreBackup(context: Context, restoreData: InputStream): Boolean {
        return try {
            val isReader = InputStreamReader(restoreData)
            val reader = JsonReader(isReader)

            // START
            reader.beginObject()
            while (reader.hasNext()) {
                when (val type = reader.nextName()) {
                    "database" -> readDatabase(reader, context)
                    "preferences" -> readPreferences(reader, context)
                    else -> throw RuntimeException("Can not parse type $type")
                }
            }
            reader.endObject()
            // END


            /*
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = restoreData.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }

            String resultString = result.toString("UTF-8");
            Log.d("PFA BackupRestorer", resultString);
             */true
        } catch (e: Exception) {
            false
        }
    }

}