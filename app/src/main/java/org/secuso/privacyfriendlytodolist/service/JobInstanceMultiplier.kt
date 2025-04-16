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

import android.annotation.SuppressLint
import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log
import org.secuso.privacyfriendlytodolist.util.LogTag

/**
 * An instance of a job service class gets used for multiple jobs in parallel by the OS.
 * As consequence the job service class cannot use member to store things that belong to a
 * specific job. To solve that, this class creates a new job instance for every single job.
 *
 * Regarding @SuppressLint("SpecifyJobSchedulerIdRange"):
 * JobManager avoids ID collision by design. See JobManager#JOB_SCHEDULER_JOB_ID_RANGE_BEGIN.
 */
@SuppressLint("SpecifyJobSchedulerIdRange")
class JobInstanceMultiplier : JobService() {
    private val jobs = mutableMapOf<Int, JobBase>()

    override fun onDestroy() {
        super.onDestroy()

        for (job in jobs.values) {
            job.onDestroy()
        }
        jobs.clear()
    }

    override fun onStartJob(params: JobParameters): Boolean {
        var job = jobs[params.jobId]
        if (null == job) {
            val jobType = params.extras.getInt(JobManager.KEY_JOB_TYPE, -1)
            job = JobFactory.createJob(jobType)
            if (null == job) {
                Log.e(TAG, "Failed to create job of type $jobType.")
                return false
            }

            Log.d(TAG, "Job with ID ${params.jobId} gets created.")
            job.initialize(this, params)
            job.onCreate()
        } else {
            Log.d(TAG, "Job with ID ${params.jobId} gets re-started.")
        }

        val isJobOngoing = job.onStartJob()
        if (isJobOngoing) {
            jobs[params.jobId] = job
        } else {
            Log.d(TAG, "Job with ID ${params.jobId} gets destroyed.")
            job.onDestroy()
        }
        return isJobOngoing
    }

    override fun onStopJob(params: JobParameters): Boolean {
        var isJobOngoing = false
        val job = jobs[params.jobId]
        if (job != null) {
            Log.d(TAG, "Job with ID ${params.jobId} gets stopped.")
            isJobOngoing = job.onStopJob()
            if (!isJobOngoing) {
                Log.d(TAG, "Job with ID ${params.jobId} gets destroyed.")
                job.onDestroy()
                jobs.remove(params.jobId)
            }
        } else {
            Log.w(TAG, "Found no job with ID ${params.jobId} to be stopped.")
        }
        // Return true, if job should be rescheduled.
        return isJobOngoing
    }

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
    }
}