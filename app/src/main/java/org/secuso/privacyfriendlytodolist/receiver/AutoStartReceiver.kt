/*
Privacy Friendly To-Do List
Copyright (C) 2024-2025  Christian Adams

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
package org.secuso.privacyfriendlytodolist.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.secuso.privacyfriendlytodolist.service.JobManager
import org.secuso.privacyfriendlytodolist.util.LogTag

class AutoStartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != ACTION) {
            Log.e(TAG, "Received intent with unexpected action '$action'.")
            return
        }

        Log.i(TAG, "Received intent with action $action. Starting update-alarm-job.")
        JobManager.startUpdateAlarmJob(context)
    }

    companion object {
        const val ACTION = "android.intent.action.BOOT_COMPLETED"
        private val TAG = LogTag.create(this::class.java.declaringClass)
    }
}
