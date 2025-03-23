/*
Privacy Friendly To-Do List
Copyright (C) 2016-2025  Dominik Puellen

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
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.model.TodoTask.RecurrencePattern
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Suppress("MemberVisibilityCanBePrivate")
object Helper {
    private val TAG = LogTag.create(this::class.java)

    fun createLocalizedDateString(timestampS: Long): String {
        val date = Date(TimeUnit.SECONDS.toMillis(timestampS))
        return createLocalizedDateString(date)
    }

    fun createLocalizedDateString(date: Date): String {
        val dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.DEFAULT, Locale.getDefault())
        return dateFormat.format(date)
    }

    fun createLocalizedDateTimeString(timestampS: Long): String {
        val dateTime = Date(TimeUnit.SECONDS.toMillis(timestampS))
        return createLocalizedDateTimeString(dateTime)
    }

    fun createLocalizedDateTimeString(dateTime: Date): String {
        val dateTimeFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.DEFAULT, SimpleDateFormat.SHORT, Locale.getDefault())
        return dateTimeFormat.format(dateTime)
    }

    fun createCanonicalDateString(timestampS: Long): String {
        val date = Date(TimeUnit.SECONDS.toMillis(timestampS))
        return createCanonicalDateString(date)
    }

    fun createCanonicalDateString(date: Date): String {
        val canonicalDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return canonicalDateFormat.format(date)
    }

    fun createCanonicalDateTimeString(timestampS: Long): String {
        val dateTime = Date(TimeUnit.SECONDS.toMillis(timestampS))
        return createCanonicalDateTimeString(dateTime)
    }

    fun createCanonicalDateTimeString(dateTime: Date): String {
        val canonicalDateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
        return canonicalDateTimeFormat.format(dateTime)
    }

    /**
     * @return The number of seconds since midnight, January 1, 1970 UTC.
     */
    fun getCurrentTimestamp(): Long {
        return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
    }

    fun changeTimePart(timestamp: Long, h: Int = 0, m: Int = 0, s: Int = 0): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = TimeUnit.SECONDS.toMillis(timestamp)
        calendar.set(Calendar.HOUR_OF_DAY, h)
        calendar.set(Calendar.MINUTE, m)
        calendar.set(Calendar.SECOND, s)
        calendar.set(Calendar.MILLISECOND, 0)
        return TimeUnit.MILLISECONDS.toSeconds(calendar.timeInMillis)
    }

    fun getNextRecurringDate(recurringDate: Long, todoTask: TodoTask, now: Long, offset: Int = 0): Long {
        return getNextRecurringDateAndCount(recurringDate, todoTask.getRecurrencePattern(),
            todoTask.getRecurrenceInterval(), now, offset).first
    }

    fun getNextRecurringDateAndCount(recurringDate: Long, todoTask: TodoTask, now: Long, offset: Int = 0): Pair<Long, Int> {
        return getNextRecurringDateAndCount(recurringDate, todoTask.getRecurrencePattern(),
            todoTask.getRecurrenceInterval(), now, offset)
    }

    /**
     * @param offset After the next recurring date was determined, a negative or positive number of
     * intervals can be subtracted or added. For example with an offset of -1 the last recurring
     * date can be determined.
     */
    fun getNextRecurringDate(recurringDate: Long, recurrencePattern: RecurrencePattern,
                             recurrenceInterval: Int, now: Long, offset: Int = 0): Long {
        return getNextRecurringDateAndCount(recurringDate, recurrencePattern, recurrenceInterval,
            now, offset).first
    }

    /**
     * @param offset After the next recurring date was determined, a negative or positive number of
     * intervals can be subtracted or added. For example with an offset of -1 the last recurring
     * date can be determined.
     */
    fun getNextRecurringDateAndCount(recurringDate: Long, recurrencePattern: RecurrencePattern,
                                     recurrenceInterval: Int, now: Long, offset: Int = 0): Pair<Long, Int> {
        var nextRecurringDate = recurringDate
        var recurrences = 0
        if (recurrencePattern != RecurrencePattern.NONE) {
            val recurringDateCal = Calendar.getInstance()
            recurringDateCal.setTimeInMillis(TimeUnit.SECONDS.toMillis(recurringDate))
            val nowCal = Calendar.getInstance()
            nowCal.setTimeInMillis(TimeUnit.SECONDS.toMillis(now))
            recurrences = getNextRecurringDateAndCount(recurringDateCal, recurrencePattern,
                recurrenceInterval, nowCal, offset)
            nextRecurringDate = TimeUnit.MILLISECONDS.toSeconds(recurringDateCal.timeInMillis)
        }
        return Pair(nextRecurringDate, recurrences)
    }

    fun getNextRecurringDateAndCount(recurringDate: Calendar, todoTask: TodoTask, now: Calendar, offset: Int = 0): Int {
        return getNextRecurringDateAndCount(recurringDate, todoTask.getRecurrencePattern(),
            todoTask.getRecurrenceInterval(), now, offset)
    }

    /**
     * Computes the next recurring date, based on the given recurring date, the recurrence pattern
     * and the recurrence interval. The next recurring date will be greater than 'now' and greater
     * than or equal to the given recurrence date.
     *
     * Optionally an positive or negative offset can be specified to add a number of intervals to
     * the computed next recurring date.
     *
     * @todo When API 26 can be used, use ChronoUnit for a better implementation of this method.
     *
     * @param recurringDate In: The base recurring date.
     * Out: The next recurring date. Optionally shifted by a positive or negative number of intervals.
     * @param recurrencePattern The recurrence pattern.
     * @param recurrenceInterval The recurrence interval.
     * @param now The current date and time.
     * @param offset The number of intervals to add to the next recurring date. For example with an
     * offset of -1 the last recurring date can be determined.
     * @return The number of recurrences.
     */
    fun getNextRecurringDateAndCount(recurringDate: Calendar, recurrencePattern: RecurrencePattern,
                                     recurrenceInterval: Int, now: Calendar, offset: Int = 0): Int {
        var recurrences = 0
        if (recurrencePattern != RecurrencePattern.NONE) {
            while (recurringDate <= now) {
                addInterval(recurringDate, recurrencePattern, recurrenceInterval)
                ++recurrences
            }
            if (offset != 0) {
                addInterval(recurringDate, recurrencePattern, offset * recurrenceInterval)
                recurrences += offset
            }
        }
        return recurrences
    }

    fun addInterval(timestamp: Long, recurrencePattern: RecurrencePattern, recurrenceInterval: Int): Long {
        var result = timestamp
        if (recurrencePattern != RecurrencePattern.NONE) {
            val recurringDateCal = Calendar.getInstance()
            recurringDateCal.setTimeInMillis(TimeUnit.SECONDS.toMillis(timestamp))
            addInterval(recurringDateCal, recurrencePattern, recurrenceInterval)
            result = TimeUnit.MILLISECONDS.toSeconds(recurringDateCal.timeInMillis)
        }
        return result
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

    fun bundleToString(bundle: Bundle?): String? {
        var result: String? = null
        if (null != bundle) {
            val sb = StringBuilder("Bundle[")
            val keys = bundle.keySet()
            var isFirst = true
            for (key in keys) {
                if (!isFirst) {
                    sb.append(',')
                } else {
                    isFirst = false
                }
                sb.append(key)
                sb.append('=')
                @Suppress("DEPRECATION")
                sb.append(bundle.get(key))
            }
            sb.append("]")
            result = sb.toString()
        }
        return result
    }
}
