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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.model.TodoTask.RecurrencePattern

object Helper {

    /**
     * Compares task 1 with task 2 regarding the order of their deadlines.
     *
     * Tasks with deadlines shall be always first. 'First' means to be less than the other.
     * So a task with no deadline is always greater than a task with a deadline.
     *
     * @return Zero if task 1 is equal to task 2, a negative number if task 1 is less than task 2,
     * or a positive number if task 1 is greater than task 2.
     */
    fun compareDeadlines(task1: TodoTask, task2: TodoTask): Int {
        var now: Timestamp? = null
        val d1: Timestamp? = if (task1.isRecurring() && task1.getDeadline() != null) {
            now = Timestamp.createCurrent()
            task1.getDeadline()!!.getNextRecurringDate(task1, now, acceptDestDate = true).first
        } else {
            task1.getDeadline()
        }
        val d2: Timestamp? = if (task2.isRecurring() && task2.getDeadline() != null) {
            now = now ?: Timestamp.createCurrent()
            task2.getDeadline()!!.getNextRecurringDate(task2, now, acceptDestDate = true).first
        } else {
            task2.getDeadline()
        }

        return if (d1 == null && d2 == null) {
            0
        } else if (d1 == null) {
            1
        } else if (d2 == null) {
            -1
        } else {
            d1.compareTo(d2)
        }
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
