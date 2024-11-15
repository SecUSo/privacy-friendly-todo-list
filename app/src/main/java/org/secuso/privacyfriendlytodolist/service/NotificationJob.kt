/*
Privacy Friendly To-Do List
Copyright (C) 2024  Christian Adams

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
package org.secuso.privacyfriendlytodolist.service

import android.util.Log
import org.secuso.privacyfriendlytodolist.model.Model
import org.secuso.privacyfriendlytodolist.receiver.NotificationReceiver
import org.secuso.privacyfriendlytodolist.util.AlarmMgr
import org.secuso.privacyfriendlytodolist.util.Helper
import org.secuso.privacyfriendlytodolist.util.LogTag
import org.secuso.privacyfriendlytodolist.util.NotificationMgr
import org.secuso.privacyfriendlytodolist.util.PreferenceMgr


/**
 * A job service that executes the actions offered by a reminder notification.
 */
class NotificationJob : ModelJobBase("Notification job") {
    override fun onStartJob(): Boolean {
        super.onStartJob()

        if (!params.extras.containsKey(NotificationMgr.EXTRA_NOTIFICATION_TASK_ID)) {
            Log.e(TAG, "$logPrefix Started without task ID.")
        } else if (params.extras.getInt(NotificationReceiver.ACTION_SNOOZE, -1) != -1) {
            doSnooze(params.extras.getInt(NotificationMgr.EXTRA_NOTIFICATION_TASK_ID))
        } else if (params.extras.getInt(NotificationReceiver.ACTION_SET_DONE, -1) != -1) {
            doSetDone(params.extras.getInt(NotificationMgr.EXTRA_NOTIFICATION_TASK_ID))
        } else {
            Log.e(TAG, "$logPrefix Started without (known) notification action).")
        }
        
        // Return true, if job still runs asynchronously.
        // If returning true, jobFinished() shall be called after asynchronous job has been finished.
        return isJobOngoing()
    }

    private fun doSnooze(todoTaskId: Int) {
        NotificationMgr.cancelNotification(context, todoTaskId)

        val alarmTime = Helper.getCurrentTimestamp() + PreferenceMgr.getSnoozeDuration(context)
        AlarmMgr.setAlarmForTask(context, todoTaskId, alarmTime)
        jobFinished()
    }

    private fun doSetDone(todoTaskId: Int) {
        NotificationMgr.cancelNotification(context, todoTaskId)

        // Serialize actions to be sure that both actions are done before calling jobFinished.
        model!!.getTaskById(todoTaskId) { todoTask ->
            if (isJobStopped()) {
                return@getTaskById
            }

            if (null == todoTask) {
                Log.e(TAG, "$logPrefix Unable to set task as done. No task with ID $todoTaskId was found.")
            } else if (todoTask.isDone()) {
                Log.d(TAG, "$logPrefix Task with ID $todoTaskId already is done.")
            } else {
                todoTask.setDone(true)
                todoTask.setChanged()
                model!!.saveTodoTaskInDb(todoTask) { counter ->
                    if (counter > 0) {
                        Log.i(TAG, "$logPrefix Set task with ID $todoTaskId as done.")
                        Model.notifyDataChangedFromOutside(context)
                    } else {
                        Log.e(TAG, "$logPrefix Failed to set task with ID $todoTaskId as done. Result: $counter")
                    }
                    jobFinished()
                }
            }
        }
    }

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
    }
}