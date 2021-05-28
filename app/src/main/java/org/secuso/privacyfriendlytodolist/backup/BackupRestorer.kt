package org.secuso.privacyfriendlytodolist.backup

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.preference.PreferenceManager
import android.util.JsonReader
import org.secuso.privacyfriendlybackup.api.backup.DatabaseUtil.readDatabaseContent
import org.secuso.privacyfriendlybackup.api.backup.FileUtil.copyFile
import org.secuso.privacyfriendlybackup.api.backup.FileUtil.readPath
import org.secuso.privacyfriendlybackup.api.pfa.IBackupRestorer
import org.secuso.privacyfriendlytodolist.model.database.DatabaseHelper.DATABASE_NAME
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

class BackupRestorer : IBackupRestorer {

    @Throws(IOException::class)
    private fun readFiles(reader: JsonReader, context: Context) {
        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName()
            when (name) {
                "sketches", "audio_notes" -> {
                    val f = File(context.filesDir, name)
                    readPath(reader, f)
                }
                else -> throw java.lang.RuntimeException("Unknown folder $name")
            }
        }
        reader.endObject()
    }

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
        val db = SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath("restoreDatabase"), null)
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
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        while (reader.hasNext()) {
            val name = reader.nextName()
            when (name) {
                "settings_use_custom_font_size", "settings_del_notes" -> pref.edit().putBoolean(name, reader.nextBoolean()).apply()
                "settings_font_size" -> pref.edit().putString(name, reader.nextString()).apply()
                else -> throw java.lang.RuntimeException("Unknown preference $name")
            }
        }
        reader.endObject()
    }

    override fun restoreBackup(context: Context, restoreData: InputStream): Boolean {
        return try {
            val isReader = InputStreamReader(restoreData)
            val reader = JsonReader(isReader)

            // START
            reader.beginObject()
            while (reader.hasNext()) {
                val type = reader.nextName()
                when (type) {
                    "database" -> readDatabase(reader, context)
                    "preferences" -> readPreferences(reader, context)
                    "files" -> readFiles(reader, context)
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