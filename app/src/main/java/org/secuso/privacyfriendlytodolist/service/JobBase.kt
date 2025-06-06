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

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.util.Log
import org.secuso.privacyfriendlytodolist.util.LogTag

/**
 * Job base class.
 */
abstract class JobBase(private val jobName: String) {
    private lateinit var commonJobService: JobService
    protected lateinit var context: Context
    protected lateinit var applicationContext: Context
    protected lateinit var params: JobParameters

    private var isJobStopped = false
    private var isJobFinished = false

    protected val logPrefix: String
        get() = "$jobName #${params.jobId}:"

    fun initialize(jobService: JobService, params: JobParameters) {
        commonJobService = jobService
        context = jobService
        applicationContext = jobService.applicationContext
        this.params = params
    }

    open fun onCreate() {
    }

    open fun onDestroy() {
    }

    open fun onStartJob(): Boolean {
        Log.d(TAG, "$logPrefix Job started.")
        return false
    }

    open fun onStopJob(): Boolean {
        isJobStopped = true
        val shouldBeRescheduled = isJobNotFinished()
        Log.w(TAG, "$logPrefix Gets stopped. Job not finished, rescheduling: $shouldBeRescheduled.")
        // Return true, if job should be rescheduled.
        return shouldBeRescheduled
    }

    protected fun isJobStopped(): Boolean {
        return isJobStopped
    }

    protected fun isJobNotStopped(): Boolean {
        return !isJobStopped
    }

    protected fun jobFinished() {
        isJobFinished = true
        if (isJobStopped()) {
            Log.w(TAG, "$logPrefix Finished while job was already stopped.")
        } else {
            Log.d(TAG, "$logPrefix Finished regularly.")
            commonJobService.jobFinished(params, false)
        }
    }

    protected fun isJobFinished(): Boolean {
        return isJobFinished
    }

    protected fun isJobNotFinished(): Boolean {
        return !isJobFinished
    }

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
    }
}