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
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
import android.util.JsonWriter
import android.util.Log
import androidx.preference.PreferenceManager
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.secuso.privacyfriendlybackup.api.backup.DatabaseUtil
import org.secuso.privacyfriendlybackup.api.backup.DatabaseUtil.writeDatabase
import org.secuso.privacyfriendlybackup.api.backup.PreferenceUtil.writePreferences
import org.secuso.privacyfriendlybackup.api.pfa.IBackupCreator
import org.secuso.privacyfriendlytodolist.model.database.TodoListDatabase
import org.secuso.privacyfriendlytodolist.util.LogTag
import org.secuso.privacyfriendlytodolist.util.PinUtil.hasPin
import org.secuso.privacyfriendlytodolist.util.PreferenceMgr
import org.secuso.privacyfriendlytodolist.view.PinActivity
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

class BackupCreator : IBackupCreator {
    override fun writeBackup(context: Context, outputStream: OutputStream) : Boolean {
        return runBlocking {
            val pinCheck = async {
                // check if a pin is set and validate it first
                if (hasPin(context)) {
                    // wait for pin
                    PinActivity.reset()
                    context.startActivity(Intent(context, PinActivity::class.java).apply {
                        flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TOP or FLAG_ACTIVITY_SINGLE_TOP or FLAG_ACTIVITY_CLEAR_TASK
                    })

                    while (PinActivity.isAuthenticated == null) {
                        delay(200)
                    }

                    return@async PinActivity.isAuthenticated!!
                } else {
                    return@async true
                }
            }

            if (pinCheck.await()) {
                return@runBlocking writeBackupInternal(context, outputStream)
            } else {
                return@runBlocking false
            }
        }
    }

    private fun writeBackupInternal(context: Context, outputStream: OutputStream) : Boolean {
        Log.d(TAG, "Backup creation starts")
        val outputStreamWriter = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
        val writer = JsonWriter(outputStreamWriter)
        writer.setIndent("")

        try {
            writer.beginObject()

            Log.d(TAG, "Writing database")
            val dataBase = DatabaseUtil.getSupportSQLiteOpenHelper(context,
                TodoListDatabase.NAME, TodoListDatabase.VERSION).readableDatabase
            writer.name("database")
            writeDatabase(writer, dataBase)
            dataBase.close()

            Log.d(TAG, "Writing preferences")
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            val excludedPreferences = ArrayList<String>()
            for (name in pref.all.keys) {
                val prefMetaData = PreferenceMgr.ALL_PREFERENCES[name]
                    ?: throw RuntimeException("Unknown preference $name")
                if (prefMetaData.excludeFromBackup) {
                    excludedPreferences.add(name)
                }
            }
            writer.name("preferences")
            writePreferences(writer, pref, excludedPreferences.toTypedArray())

            Log.d(TAG, "Writing files")
            writer.endObject()
            writer.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error occurred", e)
            return false
        }
        Log.d(TAG, "Backup created successfully")
        return true
    }

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
    }
}