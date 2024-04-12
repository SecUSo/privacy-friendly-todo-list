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
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.util.AlarmMgr
import org.secuso.privacyfriendlytodolist.util.Helper
import org.secuso.privacyfriendlytodolist.util.NotificationMgr

/**
 * A job service that handles an alarm which means showing a reminder notification.
 *
 * Regarding @SuppressLint("SpecifyJobSchedulerIdRange"):
 * JobManager avoids ID collision by design. See JobManager#JOB_SCHEDULER_JOB_ID_RANGE_BEGIN.
 */
@SuppressLint("SpecifyJobSchedulerIdRange")
class AlarmJob : JobBase() {
    override fun onStartJob(params: JobParameters): Boolean {
        super.onStartJob(params)

        if (params.extras.containsKey(AlarmMgr.KEY_ALARM_ID)) {
            Log.d(TAG, "Job $currentJobId started")
            doAlarm(params.extras.getInt(AlarmMgr.KEY_ALARM_ID))
        } else {
            Log.e(TAG, "Job $currentJobId started without alarm ID.")
        }
        
        // Return true, if job still runs asynchronously.
        // If returning true, jobFinished() shall be called after asynchronous job has been finished.
        return !isJobFinished
    }

    private fun doAlarm(todoTaskId: Int) {
        // Serialize actions to be sure that both actions are done before calling jobFinished.
        model!!.getTaskById(todoTaskId) { todoTask1 ->
            // Check if job is still active
            if (null != currentJobParams) {
                if (null != todoTask1) {
                    notifyAboutAlarm(todoTask1)
                } else {
                    Log.e(TAG, "Unable to process alarm. No task with ID $todoTaskId was found.")
                }

                model!!.getNextDueTask(Helper.getCurrentTimestamp()) { todoTask2 ->
                    // Check if job is still active
                    if (null != currentJobParams) {
                        if (null != todoTask2) {
                            // Set alarm even if it is in the past. But should not occur because
                            // getNextDueTask returns only tasks where reminder time is in the future.
                            AlarmMgr.setAlarmForTask(this, todoTask2, true)
                        }

                        jobFinished()
                    }
                }
            }
        }
    }

    private fun notifyAboutAlarm(task: TodoTask) {
        Log.d(TAG, "Notifying about alarm for task $task.")
        val title = task.getName()
        val deadline = Helper.getDateTime(task.getDeadline())
        val message = applicationContext.resources.getString(R.string.deadline_approaching, deadline)
        NotificationMgr.postTaskNotification(this, title, message, task)
    }

    companion object {
        private val TAG = AlarmJob::class.java.simpleName
    }
}