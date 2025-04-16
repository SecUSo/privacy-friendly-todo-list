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

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Context.JOB_SCHEDULER_SERVICE
import android.os.Build
import android.os.PersistableBundle
import android.util.Log
import org.secuso.privacyfriendlytodolist.service.JobFactory.JobType
import org.secuso.privacyfriendlytodolist.util.AlarmMgr
import org.secuso.privacyfriendlytodolist.util.LogTag
import org.secuso.privacyfriendlytodolist.util.NotificationMgr
import java.util.concurrent.atomic.AtomicBoolean

object JobManager {
    const val KEY_JOB_TYPE = "KEY_JOB_TYPE"
    private val TAG = LogTag.create(this::class.java)
    private const val UPDATE_ALARM_JOB_MINIMUM_LATENCY_MS = 10000L
    /**
     * WorkManager uses ID range 0 .. Integer#MAX_VALUE. The JobInfo ID range must not overlap with
     * this range. See androidx.work.Configuration.Builder#setJobSchedulerJobIdRange() for details.
     */
    private const val JOB_SCHEDULER_JOB_ID_RANGE_BEGIN = -1
    private const val JOB_SCHEDULER_JOB_ID_RANGE_END = -10000

    private var jobIdBuilder = JOB_SCHEDULER_JOB_ID_RANGE_BEGIN
    private var isUpdatedAlarmJobStarted = AtomicBoolean(false)

    fun startUpdateAlarmJob(context: Context) {
        // Some actions trigger a lot of update-alarm-jobs in a short period of time.
        // Try to avoid many unnecessary updates by delaying the job and ignoring further
        // requests while a job is started.
        if (isUpdatedAlarmJobStarted.getAndSet(true)) {
            Log.d(TAG, "update-alarm-job already triggered. Discarding this request.")
        } else {
            scheduleJob(context, JobType.UpdateAlarmsJob, null, UPDATE_ALARM_JOB_MINIMUM_LATENCY_MS)
        }
    }

    fun notifyUpdateAlarmJobExecuted() {
        isUpdatedAlarmJobStarted.set(false)
    }

    fun startHandleAlarmJob(context: Context, alarmId: Int) {
        val extras = PersistableBundle()
        extras.putInt(AlarmMgr.KEY_ALARM_ID, alarmId)
        scheduleJob(context, JobType.HandleAlarmJob, extras)
    }

    fun startNotificationJob(context: Context, action: String, taskId: Int) {
        val extras = PersistableBundle()
        extras.putInt(action, 0)
        extras.putInt(NotificationMgr.EXTRA_NOTIFICATION_TASK_ID, taskId)
        scheduleJob(context, JobType.NotificationJob, extras)
    }

    private fun scheduleJob(context: Context, jobType: JobType, extras: PersistableBundle? = null,
                            minimumLatencyMs: Long = 0) {
        val componentName = ComponentName(context, JobInstanceMultiplier::class.java)
        val jobId = getNextJobId()
        val builder = JobInfo.Builder(jobId, componentName)
        val allExtras = extras ?: PersistableBundle()
        allExtras.putInt(KEY_JOB_TYPE, jobType.ordinal)
        builder.setExtras(allExtras)
        builder.setMinimumLatency(minimumLatencyMs)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // Set some constraint to avoid java.lang.IllegalArgumentException: You're trying to build a job with no constraints, this is not allowed.
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        }
        val jobInfo = builder.build()
        val jobScheduler = context.getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        val result = jobScheduler.schedule(jobInfo)
        if (result != JobScheduler.RESULT_SUCCESS) {
            Log.e(TAG, "JobScheduler failed to schedule job for reminder service. Result: $result")
        }
    }

    private fun getNextJobId(): Int {
        val jobId = jobIdBuilder--
        if (jobIdBuilder < JOB_SCHEDULER_JOB_ID_RANGE_END) {
            jobIdBuilder = JOB_SCHEDULER_JOB_ID_RANGE_BEGIN
        }
        return jobId
    }
}