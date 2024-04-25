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

package org.secuso.privacyfriendlytodolist.service

import android.annotation.SuppressLint
import android.app.job.JobParameters
import android.util.Log
import org.secuso.privacyfriendlytodolist.model.Model
import org.secuso.privacyfriendlytodolist.receiver.NotificationReceiver
import org.secuso.privacyfriendlytodolist.util.AlarmMgr
import org.secuso.privacyfriendlytodolist.util.Helper
import org.secuso.privacyfriendlytodolist.util.LogTag
import org.secuso.privacyfriendlytodolist.util.NotificationMgr
import org.secuso.privacyfriendlytodolist.util.PreferenceMgr


/**
 * A job service that handles an alarm which means showing a reminder notification.
 *
 * Regarding @SuppressLint("SpecifyJobSchedulerIdRange"):
 * JobManager avoids ID collision by design. See JobManager#JOB_SCHEDULER_JOB_ID_RANGE_BEGIN.
 */
@SuppressLint("SpecifyJobSchedulerIdRange")
class NotificationJob : JobBase() {
    override fun onStartJob(params: JobParameters): Boolean {
        super.onStartJob(params)

        if (!params.extras.containsKey(NotificationMgr.EXTRA_NOTIFICATION_TASK_ID)) {
            Log.e(TAG, "Job $currentJobId started without task ID.")
        } else if (params.extras.getInt(NotificationReceiver.ACTION_SNOOZE, -1) != -1) {
            doSnooze(params.extras.getInt(NotificationMgr.EXTRA_NOTIFICATION_TASK_ID))
        } else if (params.extras.getInt(NotificationReceiver.ACTION_SET_DONE, -1) != -1) {
            doSetDone(params.extras.getInt(NotificationMgr.EXTRA_NOTIFICATION_TASK_ID))
        } else {
            Log.e(TAG, "Job $currentJobId started without (known) notification action).")
        }
        
        // Return true, if job still runs asynchronously.
        // If returning true, jobFinished() shall be called after asynchronous job has been finished.
        return !isJobFinished
    }

    private fun doSnooze(todoTaskId: Int) {
        NotificationMgr.cancel(this, todoTaskId)

        val alarmTime = Helper.getCurrentTimestamp() + PreferenceMgr.getSnoozeDuration(this)
        AlarmMgr.setAlarmForTask(this, todoTaskId, alarmTime)
        jobFinished()
    }

    private fun doSetDone(todoTaskId: Int) {
        NotificationMgr.cancel(this, todoTaskId)

        // Serialize actions to be sure that both actions are done before calling jobFinished.
        model!!.getTaskById(todoTaskId) { todoTask ->
            // Check if job is still active
            if (null != currentJobParams) {
                if (null == todoTask) {
                    Log.e(TAG, "Unable to set task as done. No task with ID $todoTaskId was found.")
                } else if (todoTask.isDone()) {
                    Log.d(TAG, "Task with ID $todoTaskId already is done.")
                } else {
                    todoTask.setDone(true)
                    todoTask.setChanged()
                    model!!.saveTodoTaskInDb(todoTask) { counter ->
                        if (counter == 1) {
                            Log.i(TAG, "Set task with ID $todoTaskId as done.")
                            Model.notifyDataChangedFromOutside()
                        } else {
                            Log.e(TAG, "Failed to set task with ID $todoTaskId as done. Result: $counter")
                        }
                        jobFinished()
                    }
                }
            }
        }
    }

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
    }
}