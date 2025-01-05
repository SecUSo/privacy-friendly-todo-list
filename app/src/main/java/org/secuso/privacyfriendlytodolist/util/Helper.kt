/*
Privacy Friendly To-Do List
Copyright (C) 2016-2024  Dominik Puellen

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
package org.secuso.privacyfriendlytodolist.util

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.model.TodoTask.Urgency
import org.secuso.privacyfriendlytodolist.model.TodoTask.RecurrencePattern
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object Helper {
    private val TAG = LogTag.create(this::class.java)

    fun createLocalizedDateString(time: Long): String {
        val dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.DEFAULT, Locale.getDefault())
        val date = Date(TimeUnit.SECONDS.toMillis(time))
        return dateFormat.format(date)
    }

    fun createLocalizedDateTimeString(time: Long): String {
        val dateTimeFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.DEFAULT, SimpleDateFormat.SHORT, Locale.getDefault())
        val dateTime = Date(TimeUnit.SECONDS.toMillis(time))
        return dateTimeFormat.format(dateTime)
    }

    fun createCanonicalDateTimeString(time: Long): String {
        val canonicalDateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
        val dateTime = Date(TimeUnit.SECONDS.toMillis(time))
        return canonicalDateTimeFormat.format(dateTime)
    }

    /**
     * @return The number of seconds since midnight, January 1, 1970 UTC.
     */
    fun getCurrentTimestamp(): Long {
        return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
    }

    fun getNextRecurringDate(recurringDate: Long, recurrencePattern: RecurrencePattern,
                             recurrenceInterval: Int, now: Long): Long {
        var result = recurringDate
        if (recurrencePattern != RecurrencePattern.NONE) {
            val recurringDateCal = Calendar.getInstance()
            recurringDateCal.setTimeInMillis(TimeUnit.SECONDS.toMillis(recurringDate))
            val nowCal = Calendar.getInstance()
            nowCal.setTimeInMillis(TimeUnit.SECONDS.toMillis(now))
            getNextRecurringDate(recurringDateCal, recurrencePattern, recurrenceInterval, nowCal)
            result = TimeUnit.MILLISECONDS.toSeconds(recurringDateCal.timeInMillis)
        }
        return result
    }

    fun getNextRecurringDate(recurringDate: Calendar, recurrencePattern: RecurrencePattern,
                             recurrenceInterval: Int, now: Calendar) {
        if (recurrencePattern != RecurrencePattern.NONE) {
            // TODO When API 26 can be used, use ChronoUnit for a better implementation of this method.
            // Jump to previous year to have less iterations.
            val previousYear = now[Calendar.YEAR] - 1
            if (recurringDate[Calendar.YEAR] < previousYear) {
                recurringDate[Calendar.YEAR] = previousYear
            }
            while (recurringDate <= now) {
                addInterval(recurringDate, recurrencePattern, recurrenceInterval)
            }
        }
    }

    fun addInterval(date: Calendar, recurrencePattern: RecurrencePattern, recurrenceInterval: Int) {
        when (recurrencePattern) {
            RecurrencePattern.NONE -> Log.e(TAG, "Unable to add interval because no recurrence pattern set.")
            RecurrencePattern.DAILY -> date.add(Calendar.DAY_OF_YEAR, recurrenceInterval)
            RecurrencePattern.WEEKLY -> date.add(Calendar.WEEK_OF_YEAR, recurrenceInterval)
            RecurrencePattern.MONTHLY -> date.add(Calendar.MONTH, recurrenceInterval)
            RecurrencePattern.YEARLY -> date.add(Calendar.YEAR, recurrenceInterval)
        }
    }

    fun computeRepetitions(firstDate: Long, followingDate: Long,
                           recurrencePattern: RecurrencePattern, recurrenceInterval: Int): Long {
        if (recurrencePattern == RecurrencePattern.NONE) {
            return 0
        }
        if (recurrencePattern == RecurrencePattern.DAILY) {
            return TimeUnit.DAYS.convert(followingDate - firstDate, TimeUnit.SECONDS) / recurrenceInterval
        }

        val first = Calendar.getInstance()
        first.setTimeInMillis(TimeUnit.SECONDS.toMillis(firstDate))
        val following = Calendar.getInstance()
        following.setTimeInMillis(TimeUnit.SECONDS.toMillis(followingDate))
        val result: Int
        when (recurrencePattern) {
            RecurrencePattern.WEEKLY -> {
                val unitsFirst = 52 * first[Calendar.YEAR] + first[Calendar.WEEK_OF_YEAR]
                val unitsFollowing = 52 * following[Calendar.YEAR] + following[Calendar.WEEK_OF_YEAR]
                result = (unitsFollowing - unitsFirst) / recurrenceInterval
            }
            RecurrencePattern.MONTHLY -> {
                val unitsFirst = 12 * first[Calendar.YEAR] + first[Calendar.MONTH]
                val unitsFollowing = 12 * following[Calendar.YEAR] + following[Calendar.MONTH]
                result = (unitsFollowing - unitsFirst) / recurrenceInterval
            }
            RecurrencePattern.YEARLY -> {
                result = (following[Calendar.YEAR] - first[Calendar.YEAR]) / recurrenceInterval
            }
            else -> throw InternalError("Unhandled recurrence pattern: $recurrencePattern")
        }
        return result.toLong()
    }

    fun compareDeadlines(task1: TodoTask, task2: TodoTask): Int {
        var now: Long? = null
        val d1 = if (task1.isRecurring() && task1.getDeadline() != null) {
            now = getCurrentTimestamp()
            getNextRecurringDate(
                task1.getDeadline()!!,
                task1.getRecurrencePattern(),
                task1.getRecurrenceInterval(),
                now)
        } else {
            task1.getDeadline()
        }
        val d2 = if (task2.isRecurring() && task2.getDeadline() != null) {
            now = now ?: getCurrentTimestamp()
            getNextRecurringDate(
                task2.getDeadline()!!,
                task2.getRecurrencePattern(),
                task2.getRecurrenceInterval(),
                now)
        } else {
            task2.getDeadline()
        }
        // tasks with deadlines always first
        if (d1 == d2) return 0
        if (d1 == null) return 1
        if (d2 == null) return -1
        return d1.compareTo(d2)
    }

    fun recurrencePatternToAdverbString(context: Context, recurrencePattern: RecurrencePattern?): String {
        return when (recurrencePattern) {
            RecurrencePattern.NONE -> context.resources.getString(R.string.none)
            RecurrencePattern.DAILY -> context.resources.getString(R.string.daily)
            RecurrencePattern.WEEKLY -> context.resources.getString(R.string.weekly)
            RecurrencePattern.MONTHLY -> context.resources.getString(R.string.monthly)
            RecurrencePattern.YEARLY -> context.resources.getString(R.string.yearly)
            else -> "Unknown recurrence pattern '$recurrencePattern'"
        }
    }

    fun recurrencePatternToNounString(context: Context, recurrencePattern: RecurrencePattern?): String {
        return when (recurrencePattern) {
            RecurrencePattern.NONE -> context.resources.getString(R.string.none)
            RecurrencePattern.DAILY -> context.resources.getString(R.string.days)
            RecurrencePattern.WEEKLY -> context.resources.getString(R.string.weeks)
            RecurrencePattern.MONTHLY -> context.resources.getString(R.string.months)
            RecurrencePattern.YEARLY -> context.resources.getString(R.string.years)
            else -> "Unknown recurrence pattern '$recurrencePattern'"
        }
    }

    fun priorityToString(context: Context, priority: TodoTask.Priority?): String {
        return when (priority) {
            TodoTask.Priority.HIGH -> context.resources.getString(R.string.high_priority)
            TodoTask.Priority.MEDIUM -> context.resources.getString(R.string.medium_priority)
            TodoTask.Priority.LOW -> context.resources.getString(R.string.low_priority)
            else -> "Unknown priority '$priority'"
        }
    }

    fun snoozeDurationToString(context: Context, snoozeDuration: Long, shortVersion: Boolean = false): String {
        val snoozeDurationValues = context.resources.getStringArray(R.array.snooze_duration_values)
        for (index in snoozeDurationValues.indices) {
            if (snoozeDurationValues[index].toLong() == snoozeDuration) {
                val valuesHuman = context.resources.getStringArray(
                    if (shortVersion) R.array.snooze_duration_values_human_short else R.array.snooze_duration_values_human)
                if (index < valuesHuman.size) {
                    return valuesHuman[index]
                }
                break
            }
        }
        return "Unknown snooze duration '$snoozeDuration'"
    }

    fun inflateLayout(resource: Int, layoutInflater: LayoutInflater, parentView: View, attachToRoot: Boolean = true): View {
        var viewGroup: ViewGroup? = null
        if (parentView is ViewGroup) {
            viewGroup = parentView
        } else if (parentView.rootView is ViewGroup) {
            viewGroup = parentView.rootView as ViewGroup
        }
        return layoutInflater.inflate(resource, viewGroup, attachToRoot)
    }

    fun getMenuHeader(layoutInflater: LayoutInflater, parentView: View, titleResId: Int): View {
        val menuHeader = inflateLayout(R.layout.menu_header, layoutInflater, parentView, false)
        val menuToolbar = menuHeader.findViewById<Toolbar>(R.id.menu_header_toolbar)
        menuToolbar.setTitle(titleResId)
        return menuHeader
    }

    fun isPackageAvailable(packageManager: PackageManager, packageName: String): Boolean {
        // TODO Try to find a way to get the information without functional use of exception.
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}
