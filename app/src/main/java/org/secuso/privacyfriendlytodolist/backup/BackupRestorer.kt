/*
Privacy Friendly To-Do List
Copyright (C) 2021-2025  Christopher Beckmann

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package org.secuso.privacyfriendlytodolist.backup

import android.content.Context
import android.util.JsonReader
import android.util.Log
import androidx.preference.PreferenceManager
import kotlinx.coroutines.runBlocking
import org.secuso.privacyfriendlybackup.api.backup.DatabaseUtil
import org.secuso.privacyfriendlybackup.api.backup.DatabaseUtil.readDatabaseContent
import org.secuso.privacyfriendlybackup.api.backup.FileUtil.copyFile
import org.secuso.privacyfriendlybackup.api.pfa.IBackupRestorer
import org.secuso.privacyfriendlytodolist.model.database.TodoListDatabase
import org.secuso.privacyfriendlytodolist.util.LogTag
import org.secuso.privacyfriendlytodolist.util.PrefDataType
import org.secuso.privacyfriendlytodolist.util.PreferenceMgr
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.StringReader

class BackupRestorer : IBackupRestorer {

    @Throws(IOException::class)
    private fun readDatabase(reader: JsonReader, context: Context) {
        reader.beginObject()
        val n1 = reader.nextName()
        if (n1 != "version") {
            throw RuntimeException("Unknown value $n1")
        }
        val version = reader.nextInt()
        val n2 = reader.nextName()
        if (n2 != "content") {
            throw RuntimeException("Unknown value $n2")
        }
        val restoreDBFile = context.getDatabasePath(RESTORE_DB_NAME)
        Log.d(TAG, "Restoring temporary todo list database v$version at ${restoreDBFile.canonicalPath}.")
        val db = DatabaseUtil.getSupportSQLiteOpenHelper(context, RESTORE_DB_NAME, version).writableDatabase
        db.use {
            db.beginTransaction()
            db.version = version
            readDatabaseContent(reader, db)
            db.setTransactionSuccessful()
            db.endTransaction()
        }
        reader.endObject()

        // Close database before overwriting it.
        runBlocking {
            TodoListDatabase.closeInstance()
        }

        // copy file to correct location
        val destinationDBFile = context.getDatabasePath(TodoListDatabase.NAME)
        Log.d(TAG, "Copying temporary todo list database to ${destinationDBFile.canonicalPath}.")
        copyFile(restoreDBFile, destinationDBFile)
        // Delete meta data files of SQLite DB. Otherwise the restored data will not show up.
        deleteFile(destinationDBFile.path.plus("-shm"))
        deleteFile(destinationDBFile.path.plus("-wal"))
        // Delete temporary restore database files.
        deleteFile(restoreDBFile)
        deleteFile(restoreDBFile.path.plus("-journal"))
    }

    private fun deleteFile(filePath: String) {
        deleteFile(File(filePath))
    }

    private fun deleteFile(fileToBeDeleted: File) {
        if (fileToBeDeleted.exists()) {
            Log.d(TAG, "Deleting ${fileToBeDeleted.canonicalPath}.")
            fileToBeDeleted.delete()
        }
    }

    @Throws(IOException::class)
    private fun readPreferences(reader: JsonReader, context: Context) {
        Log.d(TAG, "Restoring todo list preferences")
        reader.beginObject()
        val pref = PreferenceManager.getDefaultSharedPreferences(context).edit()
        while (reader.hasNext()) {
            val name = reader.nextName()
            val prefMetaData = PreferenceMgr.ALL_PREFERENCES[name]
                ?: throw RuntimeException("Unknown preference $name")
            when (prefMetaData.dataType) {
                PrefDataType.BOOLEAN -> pref.putBoolean(name, reader.nextBoolean())
                PrefDataType.STRING -> pref.putString(name, reader.nextString())
            }
        }
        pref.apply()
        reader.endObject()
    }

    override fun restoreBackup(context: Context, restoreData: InputStream): Boolean {
        try {
            val reader: JsonReader

            if (PRINT_BACKUP_DATA) {
                /*
                This code prints the backup data for debug purpose.
                !!! THIS CODE MUST NOT BE ENABLED IN A RELEASE VERSION OF THE APP !!!
                Because the personal backup data gets written to the logger.
                The content of the  input stream gets read by this code completely. A reset of the
                stream is not possible. So the JSON reader does use the string data.
                 */
                val backupDataRaw = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                var length: Int
                while (restoreData.read(buffer).also { length = it } != -1) {
                    backupDataRaw.write(buffer, 0, length)
                }
                val backupData = backupDataRaw.toString("UTF-8")
                Log.d(TAG, "Backup data: $backupData")
                reader = JsonReader(StringReader(backupData))
            } else {
                reader = JsonReader(InputStreamReader(restoreData))
            }

            Log.d(TAG, "Backup restoring starts.")
            reader.beginObject()
            while (reader.hasNext()) {
                when (val type = reader.nextName()) {
                    "database" -> readDatabase(reader, context)
                    "preferences" -> readPreferences(reader, context)
                    else -> throw RuntimeException("Can not parse type $type")
                }
            }
            reader.endObject()
        } catch (e: Exception) {
            Log.e(TAG, "Backup restore failed.", e)
            e.printStackTrace()
            return false
        }
        Log.i(TAG, "Backup restored successfully.")
        return true
    }

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
        private const val RESTORE_DB_NAME = "restoreDatabase.db"

        /**
         * This flag enables / disables logging of the backup data during restore for debug purpose.
         * !!! THIS FLAG MUST NOT BE SET TO TRUE IN A RELEASE VERSION OF THE APP !!!
         */
        private const val PRINT_BACKUP_DATA = false
    }
}