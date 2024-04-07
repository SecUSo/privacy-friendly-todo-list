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

package org.secuso.privacyfriendlytodolist.backup

import android.content.Context
import android.util.JsonReader
import android.util.Log
import androidx.preference.PreferenceManager
import org.secuso.privacyfriendlybackup.api.backup.DatabaseUtil
import org.secuso.privacyfriendlybackup.api.backup.DatabaseUtil.readDatabaseContent
import org.secuso.privacyfriendlybackup.api.backup.FileUtil.copyFile
import org.secuso.privacyfriendlybackup.api.pfa.IBackupRestorer
import org.secuso.privacyfriendlytodolist.model.database.TodoListDatabase
import org.secuso.privacyfriendlytodolist.util.PrefDataType
import org.secuso.privacyfriendlytodolist.util.PreferenceMgr
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

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
        Log.d(TAG, "Restoring todo list database v$version")
        val db = DatabaseUtil.getSupportSQLiteOpenHelper(context, RESTORE_DB_NAME, version).writableDatabase
        db.beginTransaction()
        db.version = version
        readDatabaseContent(reader, db)
        db.setTransactionSuccessful()
        db.endTransaction()
        db.close()
        reader.endObject()

        // copy file to correct location
        val restoreDBFile = context.getDatabasePath(RESTORE_DB_NAME)
        val destinationDBFile = context.getDatabasePath(TodoListDatabase.NAME)
        copyFile(restoreDBFile, destinationDBFile)
        // Delete meta data files of SQLite DB. Otherwise the restored data will not show up.
        var fileToBeDeleted = File(destinationDBFile.path.plus("-shm"))
        if (fileToBeDeleted.exists()) {
            fileToBeDeleted.delete()
        }
        fileToBeDeleted = File(destinationDBFile.path.plus("-wal"))
        if (fileToBeDeleted.exists()) {
            fileToBeDeleted.delete()
        }
        // Delete temporary restore database files.
        restoreDBFile.delete()
        fileToBeDeleted = File(restoreDBFile.path.plus("-journal"))
        if (fileToBeDeleted.exists()) {
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
            val isReader = InputStreamReader(restoreData)
            val reader = JsonReader(isReader)

            /*
            // Debug-output of backup data
            // ATTENTION! Following backup restore will not work with this code enabled because
            // content of input stream gets read by this code completely. Reset of stream not possible.
            val result = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var length: Int
            while (restoreData.read(buffer).also { length = it } != -1) {
                result.write(buffer, 0, length)
            }
            val resultString = result.toString("UTF-8")
            Log.d(TAG, "Backup data: $resultString")
             */

            Log.d(TAG, "Backup restoring starts")
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
            Log.e(TAG, "Error occurred", e)
            e.printStackTrace()
            return false
        }
        Log.d(TAG, "Backup restored successfully")
        return true
    }

    companion object {
        private val TAG = BackupRestorer::class.java.simpleName
        private const val RESTORE_DB_NAME = "restoreDatabase.db"
    }
}