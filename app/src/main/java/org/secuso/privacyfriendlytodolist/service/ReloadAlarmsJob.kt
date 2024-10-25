/*
Privacy Friendly To-Do List
Copyright (C) 2024  SECUSO (www.secuso.org)

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

import android.annotation.SuppressLint
import android.app.job.JobParameters
import android.util.Log
import org.secuso.privacyfriendlytodolist.util.AlarmMgr
import org.secuso.privacyfriendlytodolist.util.Helper
import org.secuso.privacyfriendlytodolist.util.LogTag
import org.secuso.privacyfriendlytodolist.util.NotificationMgr

/**
 * A job service that handles an alarm which means showing a reminder notification.
 *
 * Regarding @SuppressLint("SpecifyJobSchedulerIdRange"):
 * JobManager avoids ID collision by design. See JobManager#JOB_SCHEDULER_JOB_ID_RANGE_BEGIN.
 */
@SuppressLint("SpecifyJobSchedulerIdRange")
class ReloadAlarmsJob : JobBase() {

    override fun onStartJob(params: JobParameters): Boolean {
        super.onStartJob(params)

        doReloadAlarms()

        // Return true, if job still runs asynchronously.
        // If returning true, jobFinished() shall be called after asynchronous job has been finished.
        return !isJobFinished
    }

    private fun doReloadAlarms() {
        // First cancel all alarms
        NotificationMgr.cancelAll(this)
        
        model!!.getNextDueTaskAndOverdueTasks(Helper.getCurrentTimestamp()) { tasksToRemind ->
            if (isJobStopped()) {
                return@getNextDueTaskAndOverdueTasks
            }

            // Then set alarms, if there are tasks to remind.
            if (tasksToRemind.isEmpty()) {
                Log.i(TAG, "No alarms set.")
            } else {
                for (todoTask in tasksToRemind) {
                    // Set alarm even if it is in the past. Phone could be switched off for a while.
                    AlarmMgr.setAlarmForTask(this, todoTask, true)
                }
            }

            jobFinished()
        }
    }

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
    }
}