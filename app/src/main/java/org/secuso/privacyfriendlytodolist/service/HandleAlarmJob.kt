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
import org.secuso.privacyfriendlytodolist.util.Helper
import org.secuso.privacyfriendlytodolist.util.LogTag
import org.secuso.privacyfriendlytodolist.util.NotificationMgr

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
                var message: String? = null
                val deadline = todoTask.getDeadline()
                if (null != deadline) {
                    message = if (todoTask.isRecurring()) {
                        // Change timestamp to begin of today to ensure that a deadline which is today is not seen
                        // as past because it's time-part (12:00) is behind the current time of day (e.g. 14:00).
                        val now = Helper.changeTimePartToZero(Helper.getCurrentTimestamp())
                        val nextDeadlineAndCount = Helper.getNextRecurringDateAndCount(deadline, todoTask, now)
                        val deadlineStr = Helper.createLocalizedDateString(nextDeadlineAndCount.first)
                        applicationContext.resources.getString(R.string.recurring_deadline_approaching,
                            deadlineStr, nextDeadlineAndCount.second)
                    } else {
                        val deadlineStr = Helper.createLocalizedDateString(deadline)
                        applicationContext.resources.getString(R.string.deadline_approaching, deadlineStr)
                    }
                }
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

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
    }
}