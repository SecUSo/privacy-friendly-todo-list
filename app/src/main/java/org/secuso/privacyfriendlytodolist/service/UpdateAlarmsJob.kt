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
import org.secuso.privacyfriendlytodolist.util.AlarmMgr
import org.secuso.privacyfriendlytodolist.util.Helper
import org.secuso.privacyfriendlytodolist.util.LogTag

/**
 * A job service that ensures that an alarm is set for the next due task.
 * Optionally it triggers alarms for all overdue tasks immediately.
 */
class UpdateAlarmsJob : ModelJobBase("Update-alarm-job") {

    override fun onStartJob(): Boolean {
        super.onStartJob()

        doUpdateNextAlarm(params.extras.getInt(KEY_TRIGGER_ALARMS_FOR_OVERDUE_TASKS, 0))

        // Return true, if job still runs asynchronously.
        // If returning true, jobFinished() shall be called after asynchronous job has been finished.
        return isJobOngoing()
    }

    private fun doUpdateNextAlarm(alsoTriggerAlarmsForOverdueTasks: Int) {
        model.getNextDueTask(Helper.getCurrentTimestamp()) { nextDueTask ->
            if (isJobStopped()) {
                return@getNextDueTask
            }

            if (null != nextDueTask) {
                AlarmMgr.setAlarmForNextDueTask(context, nextDueTask)
            } else {
                Log.d(TAG, "$logPrefix No next due task so no alarm to set.")
            }

            if (alsoTriggerAlarmsForOverdueTasks != 0) {
                triggerAlarmsForOverdueTasks()
            } else {
                jobFinished()
            }
        }
    }

    private fun triggerAlarmsForOverdueTasks() {
        model.getOverdueTasks(Helper.getCurrentTimestamp()) { overdueTasks ->
            if (isJobStopped()) {
                return@getOverdueTasks
            }

            Log.i(TAG, "$logPrefix Triggering alarms for ${overdueTasks.size} overdue tasks.")
            for (todoTask in overdueTasks) {
                JobManager.startHandleAlarmJob(context, todoTask.getId())
            }

            jobFinished()
        }
    }

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
        const val KEY_TRIGGER_ALARMS_FOR_OVERDUE_TASKS = "KEY_TRIGGER_ALARMS_FOR_OVERDUE_TASKS"
    }
}