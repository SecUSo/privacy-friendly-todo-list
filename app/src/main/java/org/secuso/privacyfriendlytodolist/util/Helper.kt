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
import kotlin.math.abs

@Suppress("MemberVisibilityCanBePrivate")
object Helper {
    private val TAG = LogTag.create(this::class.java)

    const val SECONDS_PER_DAY = 24 * 60 * 60

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

    fun changeTimePartToZero(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = TimeUnit.SECONDS.toMillis(timestamp)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return TimeUnit.MILLISECONDS.toSeconds(calendar.timeInMillis)
    }

    fun getNextRecurringDate(recurringDate: Long, todoTask: TodoTask, destinationDate: Long): Long {
        return getNextRecurringDateAndCount(recurringDate, todoTask.getRecurrencePattern(),
            todoTask.getRecurrenceInterval(), destinationDate).first
    }

    fun getNextRecurringDateAndCount(recurringDate: Long, todoTask: TodoTask,
                                     destinationDate: Long): Pair<Long, Int> {
        return getNextRecurringDateAndCount(recurringDate, todoTask.getRecurrencePattern(),
            todoTask.getRecurrenceInterval(), destinationDate)
    }

    fun getNextRecurringDate(recurringDate: Long, recurrencePattern: RecurrencePattern,
                             recurrenceInterval: Int, destinationDate: Long): Long {
        return getNextRecurringDateAndCount(recurringDate, recurrencePattern, recurrenceInterval,
            destinationDate).first
    }

    fun getNextRecurringDateAndCount(recurringDate: Long, recurrencePattern: RecurrencePattern,
                                     recurrenceInterval: Int, destinationDate: Long): Pair<Long, Int> {
        var nextRecurringDate = recurringDate
        var recurrences = 0
        if (recurrencePattern != RecurrencePattern.NONE) {
            val recurringDateCal = Calendar.getInstance()
            recurringDateCal.timeInMillis = TimeUnit.SECONDS.toMillis(recurringDate)
            val nowCal = Calendar.getInstance()
            nowCal.timeInMillis = TimeUnit.SECONDS.toMillis(destinationDate)
            recurrences = getNextRecurringDateAndCount(recurringDateCal, recurrencePattern,
                recurrenceInterval, nowCal)
            nextRecurringDate = TimeUnit.MILLISECONDS.toSeconds(recurringDateCal.timeInMillis)
        }
        return Pair(nextRecurringDate, recurrences)
    }

    fun getNextRecurringDateAndCount(recurringDate: Calendar, todoTask: TodoTask,
                                     destinationDate: Calendar): Int {
        return getNextRecurringDateAndCount(recurringDate, todoTask.getRecurrencePattern(),
            todoTask.getRecurrenceInterval(), destinationDate)
    }

    /**
     * Depending on a positive / negative recurrence interval, the function increases / decreases
     * the recurrence date to return the first recurrence date which is greater than / less than
     * the destination time.
     *
     * @todo When API 26 can be used, use ChronoUnit for a better implementation of this method.
     *
     * @param recurringDate In: The base recurring date. Out: The computed recurring date.
     * @param recurrencePattern The recurrence pattern.
     * @param recurrenceInterval The recurrence interval.
     * @param destinationDate The destination time for the recurrence date computation.
     * @return The absolute number of recurrences from base recurring date to computed recurring date.
     */
    fun getNextRecurringDateAndCount(recurringDate: Calendar, recurrencePattern: RecurrencePattern,
                                     recurrenceInterval: Int, destinationDate: Calendar): Int {
        var recurrences = 0
        if (recurrencePattern != RecurrencePattern.NONE) {
            if (recurrenceInterval > 0) {
                while (recurringDate <= destinationDate) {
                    addInterval(recurringDate, recurrencePattern, recurrenceInterval)
                    ++recurrences
                }
            } else if (recurrenceInterval < 0) {
                while (recurringDate >= destinationDate) {
                    addInterval(recurringDate, recurrencePattern, recurrenceInterval)
                    ++recurrences
                }
            } else {
                Log.e(TAG, "Invalid recurrence interval of zero.")
            }
        }
        return recurrences
    }

    fun addInterval(timestamp: Long, recurrencePattern: RecurrencePattern, recurrenceInterval: Int): Long {
        var result = timestamp
        if (recurrencePattern != RecurrencePattern.NONE) {
            val recurringDateCal = Calendar.getInstance()
            recurringDateCal.timeInMillis = TimeUnit.SECONDS.toMillis(timestamp)
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
            in RecurrencePattern.WEEKDAYS_M______ .. RecurrencePattern.WEEKDAYS_MTWTFSS -> {
                val weekdaysBitmask = recurrencePattern.ordinal - RecurrencePattern.WEEKDAYS_M______.ordinal + 1
                var steps = abs(recurrenceInterval)
                val step = if (recurrenceInterval > 0) 1 else -1
                while (steps > 0) {
                    date.add(Calendar.DAY_OF_YEAR, step)
                    if (dayOfWeekMatchesBitmask(date[Calendar.DAY_OF_WEEK], weekdaysBitmask)) {
                        --steps
                    }
                }
            }
            else -> Log.e(TAG, "Unhandled recurrence pattern: $recurrencePattern")
        }
    }

    private fun dayOfWeekMatchesBitmask(dayOfWeek: Int, weekdaysBitmask: Int): Boolean {
        var result = false
        for (index in 0 .. 6) {
            if ((weekdaysBitmask and (1 shl index)) != 0) {
                result = when (index) {
                    0 -> Calendar.MONDAY == dayOfWeek
                    1 -> Calendar.TUESDAY == dayOfWeek
                    2 -> Calendar.WEDNESDAY == dayOfWeek
                    3 -> Calendar.THURSDAY == dayOfWeek
                    4 -> Calendar.FRIDAY == dayOfWeek
                    5 -> Calendar.SATURDAY == dayOfWeek
                    6 -> Calendar.SUNDAY == dayOfWeek
                    else -> false
                }
                if (result) {
                    break
                }
            }
        }
        return result
    }

    fun compareDeadlines(task1: TodoTask, task2: TodoTask): Int {
        var now: Long? = null
        val d1 = if (task1.isRecurring() && task1.getDeadline() != null) {
            // Change timestamp to begin of today to ensure that a deadline which is today is not seen
            // as past because it's time-part (12:00) is behind the current time of day (e.g. 14:00).
            now = changeTimePartToZero(getCurrentTimestamp())
            getNextRecurringDate(
                task1.getDeadline()!!,
                task1.getRecurrencePattern(),
                task1.getRecurrenceInterval(),
                now)
        } else {
            task1.getDeadline()
        }
        val d2 = if (task2.isRecurring() && task2.getDeadline() != null) {
            now = now ?: changeTimePartToZero(getCurrentTimestamp())
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

    fun recurrencePatternToNounString(context: Context, recurrencePattern: RecurrencePattern?): String {
        return when (recurrencePattern) {
            null -> "Recurrence pattern is null"
            RecurrencePattern.NONE -> context.resources.getString(R.string.none)
            RecurrencePattern.DAILY -> context.resources.getString(R.string.days)
            RecurrencePattern.WEEKLY -> context.resources.getString(R.string.weeks)
            RecurrencePattern.MONTHLY -> context.resources.getString(R.string.months)
            RecurrencePattern.YEARLY -> context.resources.getString(R.string.years)
            in RecurrencePattern.WEEKDAYS_M______ .. RecurrencePattern.WEEKDAYS_MTWTFSS -> {
                val sb = StringBuilder()
                val weekdaysBitmask = recurrencePattern.ordinal - RecurrencePattern.WEEKDAYS_M______.ordinal + 1
                appendWeekdayInitial(context, sb, weekdaysBitmask, 0, R.string.monday_abbr)
                appendWeekdayInitial(context, sb, weekdaysBitmask, 1, R.string.tuesday_abbr)
                appendWeekdayInitial(context, sb, weekdaysBitmask, 2, R.string.wednesday_abbr)
                appendWeekdayInitial(context, sb, weekdaysBitmask, 3, R.string.thursday_abbr)
                appendWeekdayInitial(context, sb, weekdaysBitmask, 4, R.string.friday_abbr)
                appendWeekdayInitial(context, sb, weekdaysBitmask, 5, R.string.saturday_abbr)
                appendWeekdayInitial(context, sb, weekdaysBitmask, 6, R.string.sunday_abbr)
                sb.toString()
            }
            else -> "Unknown recurrence pattern '$recurrencePattern'"
        }
    }

    private fun appendWeekdayInitial(context: Context, stringBuilder: StringBuilder,
                                     weekdaysBitmask: Int, weekdayBitPosition: Int, stringResId: Int) {
        val weekdayBit = (1 shl weekdayBitPosition)
        if ((weekdaysBitmask and weekdayBit) != 0) {
            stringBuilder.append(context.resources.getString(stringResId))
            if (weekdaysBitmask >= (weekdayBit shl 1)) {
                stringBuilder.append(", ")
            }
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
        } catch (_: PackageManager.NameNotFoundException) {
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
