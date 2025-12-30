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
package org.secuso.privacyfriendlytodolist.service

import android.util.Log
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.util.AlarmMgr
import org.secuso.privacyfriendlytodolist.util.LogTag
import org.secuso.privacyfriendlytodolist.util.NotificationMgr
import org.secuso.privacyfriendlytodolist.util.Timestamp

/**
 * A job service that handles an alarm which means showing a reminder notification.
 */
class HandleAlarmJob : ModelJobBase("Handle-alarm-job") {
    override fun onStartJob(): Boolean {
        super.onStartJob()

        if (params.extras.containsKey(AlarmMgr.KEY_ALARM_ID)) {
            doAlarm(params.extras.getInt(AlarmMgr.KEY_ALARM_ID))
        } else {
            Log.e(TAG, "$logPrefix Started without alarm ID.")
            jobFinished()
        }

        // Return true, if job still runs asynchronously. If returning true, this action shall call
        // jobFinished() after asynchronous tasks have been finished.
        return isJobNotFinished()
    }

    private fun doAlarm(todoTaskId: Int) {
        model.getTaskById(todoTaskId) { todoTask ->
            if (isJobStopped()) {
                return@getTaskById
            }

            if (null != todoTask) {
                Log.i(TAG, "$logPrefix Notifying about alarm for $todoTask.")
                val title = todoTask.getName()
                val message = getMessage(todoTask)
                NotificationMgr.postTaskNotification(context, title, message, todoTask)

                todoTask.setReminderState(TodoTask.ReminderState.DONE)
                todoTask.setChanged()
                model.saveTodoTaskInDb(todoTask) { counter ->
                    if (counter != 0) {
                        Log.d(TAG, "$logPrefix Set reminder state to DONE for $todoTask.")
                    } else {
                        Log.e(TAG, "$logPrefix Failed to set reminder state to DONE for $todoTask.")
                    }
                    jobFinished()
                }
            } else {
                Log.e(TAG, "$logPrefix Unable to process alarm. No task with ID $todoTaskId was found.")
                jobFinished()
            }
        }
    }

    private fun getMessage(todoTask: TodoTask): String? {
        val deadline = todoTask.getDeadline() ?: return null
        val now = Timestamp.createCurrent()
        val today = now.timeInDays
        return if (todoTask.isRecurring()) {
            // Accept today as 'next recurring date' if it is one.
            val nextDeadline = deadline.getNextRecurringDate(todoTask, now, acceptDestDate = true)
            val deadlineDay = nextDeadline.first.timeInDays
            if (today == deadlineDay) {
                applicationContext.resources.getString(R.string.recurring_deadline_today, nextDeadline.second)
            } else {
                val stringId = if (today < deadlineDay) R.string.recurring_deadline_approaching else R.string.recurring_deadline_passed
                val deadlineStr = nextDeadline.first.createLocalizedDateString()
                applicationContext.resources.getString(stringId, deadlineStr, nextDeadline.second)
            }
        } else {
            val deadlineDay = deadline.timeInDays
            if (today == deadlineDay) {
                applicationContext.resources.getString(R.string.deadline_today)
            } else {
                val stringId = if (today < deadlineDay) R.string.deadline_approaching else R.string.deadline_passed
                val deadlineStr = deadline.createLocalizedDateString()
                applicationContext.resources.getString(stringId, deadlineStr)
            }
        }
    }

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
    }
}