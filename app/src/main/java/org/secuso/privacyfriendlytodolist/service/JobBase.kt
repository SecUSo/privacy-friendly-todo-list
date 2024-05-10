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
import android.app.job.JobService
import android.util.Log
import org.secuso.privacyfriendlytodolist.model.ModelServices
import org.secuso.privacyfriendlytodolist.util.LogTag
import org.secuso.privacyfriendlytodolist.viewmodel.CustomViewModel

/**
 * A job service that handles an alarm which means showing a reminder notification.
 *
 * Regarding @SuppressLint("SpecifyJobSchedulerIdRange"):
 * JobManager avoids ID collision by design. See JobManager#JOB_SCHEDULER_JOB_ID_RANGE_BEGIN.
 */
@SuppressLint("SpecifyJobSchedulerIdRange")
abstract class JobBase : JobService() {
    private var viewModel: CustomViewModel? = null
    protected var model: ModelServices? = null
    protected var currentJobParams: JobParameters? = null
    protected var currentJobId = 0
    protected var isJobFinished = false

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "Job created.")
        viewModel = CustomViewModel(applicationContext)
        model = viewModel!!.model
    }

    override fun onDestroy() {
        super.onDestroy()

        model = null
        viewModel!!.destroy()
        viewModel = null
        Log.d(TAG, "Job destroyed.")
    }

    override fun onStartJob(params: JobParameters): Boolean {
        currentJobParams = params
        currentJobId = params.jobId
        isJobFinished = false
        Log.d(TAG, "Job $currentJobId started.")
        return false
    }

    override fun onStopJob(params: JobParameters): Boolean {
        currentJobParams = null
        Log.w(TAG, "Job $currentJobId gets stopped. Finished: $isJobFinished. Rescheduling: ${!isJobFinished}.")
        // Return true, if job should be rescheduled.
        return !isJobFinished
    }

    protected fun isJobStillActive(): Boolean {
        return null != currentJobParams
    }

    protected fun jobFinished() {
        isJobFinished = true
        if (isJobStillActive()) {
            Log.d(TAG, "Job $currentJobId finished regularly.")
            jobFinished(currentJobParams, false)
        } else {
            Log.w(TAG, "Job $currentJobId finished while job is already inactive.")
        }
    }

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
    }
}