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

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Context.JOB_SCHEDULER_SERVICE
import android.os.PersistableBundle
import android.util.Log
import org.secuso.privacyfriendlytodolist.util.AlarmMgr
import org.secuso.privacyfriendlytodolist.util.LogTag

object JobManager {
    private val TAG = LogTag.create(this::class.java)

    /**
     * WorkManager uses ID range 0 .. Integer#MAX_VALUE. The JobInfo ID range must not overlap with
     * this range. See androidx.work.Configuration.Builder#setJobSchedulerJobIdRange() for details.
     */
    private const val JOB_SCHEDULER_JOB_ID_RANGE_BEGIN = -10000
    private const val JOB_SCHEDULER_JOB_ID_RANGE_END = -1
    private var jobIdBuilder = JOB_SCHEDULER_JOB_ID_RANGE_BEGIN

    fun processAutoStart(context: Context): Int {
        return scheduleJob(context, AutoStartJob::class.java)
    }

    fun processAlarm(context: Context, alarmId: Int): Int {
        val extras = PersistableBundle()
        extras.putInt(AlarmMgr.KEY_ALARM_ID, alarmId)
        return scheduleJob(context, AlarmJob::class.java, extras)
    }

    private fun scheduleJob(context: Context, jobClass: Class<*>, extras: PersistableBundle? = null): Int {
        val componentName = ComponentName(context, jobClass)
        var jobId = getNextJobId()
        val builder = JobInfo.Builder(jobId, componentName)
        if (null != extras) {
            builder.setExtras(extras)
        }
        val jobInfo = builder.build()
        val jobScheduler = context.getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        val result = jobScheduler.schedule(jobInfo)
        if (result != JobScheduler.RESULT_SUCCESS) {
            jobId = 0
            Log.e(TAG, "JobScheduler failed to schedule job for reminder service. Result: $result")
        }
        return jobId
    }

    private fun getNextJobId(): Int {
        val jobId = jobIdBuilder
        jobIdBuilder = if (jobIdBuilder < JOB_SCHEDULER_JOB_ID_RANGE_END) {
            jobIdBuilder + 1
        } else {
            JOB_SCHEDULER_JOB_ID_RANGE_BEGIN
        }
        return jobId
    }
}