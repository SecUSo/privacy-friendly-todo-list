/*
Privacy Friendly To-Do List
Copyright (C) 2025  Christian Adams

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
package org.secuso.privacyfriendlytodolist.view.widget

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.secuso.privacyfriendlytodolist.util.Helper
import org.secuso.privacyfriendlytodolist.util.LogTag
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Updating the widget periodically is needed to have up-to-date urgency colors
 * and days-until-deadline. For this purpose it is enough to update the widget
 * once per day, short after midnight. That's the reason why
 * android:updatePeriodMillis is not used.
 */
class TodoListWidgetPeriodicUpdater(private val context: Context,
                                    workerParams: WorkerParameters): Worker(context, workerParams) {

    override fun doWork(): Result {
        if (TodoListWidget.triggerWidgetUpdate(context, "Periodic update") == 0) {
            Log.d(TAG, "Periodic widget updates cancelled because no widget installed.")
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
        return Result.success()
    }

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
        private const val WORK_NAME = "PERIODIC_WIDGET_UPDATE"

        fun startPeriodicUpdates(context: Context) {
            // Update the widget daily short after midnight.
            val calendar = Calendar.getInstance()
            val now = calendar.timeInMillis
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 0)
            val updateTime = calendar.timeInMillis + 31000
            val initialDelay = updateTime - now
            val periodicWidgetUpdateRequest =
                PeriodicWorkRequestBuilder<TodoListWidgetPeriodicUpdater>(1, TimeUnit.DAYS)
                    .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                    .build()

            Log.i(TAG, "Next periodic widget update scheduled for " +
                    "${Helper.createCanonicalDateTimeString(TimeUnit.MILLISECONDS.toSeconds(updateTime))} " +
                    "which is in ${initialDelay.toDuration(DurationUnit.MILLISECONDS)}.")
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE, periodicWidgetUpdateRequest)
        }
    }
}