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
import org.secuso.privacyfriendlytodolist.util.NotificationMgr

/**
 * Receives user-actions from reminder notifications.
 */
class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != ACTION_SNOOZE && action != ACTION_SET_DONE) {
            Log.e(TAG, "Received intent with unexpected action '$action'.")
            return
        }
        val extras = intent.extras
        if (null == extras || !extras.containsKey(NotificationMgr.EXTRA_NOTIFICATION_TASK_ID)) {
            Log.e(TAG, "Received intent without extras.")
            return
        }

        val taskId = extras.getInt(NotificationMgr.EXTRA_NOTIFICATION_TASK_ID)
        Log.i(TAG, "Received intent with action $action for task $taskId.")
        JobManager.startNotificationJob(context, action, taskId)
    }

    companion object {
        const val ACTION_SNOOZE = "org.secuso.privacyfriendlytodolist.SNOOZE"
        const val ACTION_SET_DONE = "org.secuso.privacyfriendlytodolist.SET_DONE"
        private val TAG = LogTag.create(this::class.java.declaringClass)
    }
}
