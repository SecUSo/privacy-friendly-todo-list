package org.secuso.privacyfriendlytodolist.backup

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.preference.PreferenceManager
import android.util.JsonWriter
import android.util.Log
import org.secuso.privacyfriendlybackup.api.backup.DatabaseUtil.writeDatabase
import org.secuso.privacyfriendlybackup.api.backup.FileUtil.writePath
import org.secuso.privacyfriendlybackup.api.backup.PreferenceUtil.writePreferences
import org.secuso.privacyfriendlybackup.api.pfa.IBackupCreator
import org.secuso.privacyfriendlytodolist.model.database.DatabaseHelper.DATABASE_NAME
import java.io.File
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.*

class BackupCreator : IBackupCreator {


    override fun writeBackup(context: Context, outputStream: OutputStream) {

        val outputStreamWriter = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
        val writer = JsonWriter(outputStreamWriter)
        writer.setIndent("")

        try {
            writer.beginObject()
            val dataBase = SQLiteDatabase.openDatabase(context.getDatabasePath(DATABASE_NAME).path, null, SQLiteDatabase.OPEN_READONLY)
            writer.name("database")
            writeDatabase(writer, dataBase)
            dataBase.close()

            writer.name("preferences")
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            writePreferences(writer, pref)

            writer.endObject()
            writer.close()
        } catch (e: Exception) {
            Log.e("PFA BackupCreator", "Error occurred", e)
            e.printStackTrace()
        }
    }

}