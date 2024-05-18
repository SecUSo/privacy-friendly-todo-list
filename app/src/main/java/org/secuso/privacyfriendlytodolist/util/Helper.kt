package org.secuso.privacyfriendlytodolist.util

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.text.format.DateFormat
import android.util.Log
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.model.TodoTask.DeadlineColors
import org.secuso.privacyfriendlytodolist.model.TodoTask.RecurrencePattern
import java.util.Calendar
import java.util.concurrent.TimeUnit


object Helper {
    private val TAG = LogTag.create(this::class.java)
    private const val DATE_FORMAT = "dd.MM.yyyy"
    private const val DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm"

    fun createDateString(time: Long): String {
        val calendar = Calendar.getInstance()
        calendar.setTimeInMillis(TimeUnit.SECONDS.toMillis(time))
        return DateFormat.format(DATE_FORMAT, calendar).toString()
    }

    fun createDateTimeString(time: Long): String {
        val calendar = Calendar.getInstance()
        calendar.setTimeInMillis(TimeUnit.SECONDS.toMillis(time))
        return createDateTimeString(calendar)
    }

    fun createDateTimeString(calendar: Calendar): String {
        return DateFormat.format(DATE_TIME_FORMAT, calendar).toString()
    }

    /**
     * @return The current time in seconds.
     */
    fun getCurrentTimestamp(): Long {
        return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
    }

    fun getNextRecurringDate(recurringDate: Long, recurrencePattern: RecurrencePattern, now: Long): Long {
        var result = recurringDate
        if (recurringDate != -1L && recurrencePattern != RecurrencePattern.NONE) {
            val recurringDateCal = Calendar.getInstance()
            recurringDateCal.setTimeInMillis(TimeUnit.SECONDS.toMillis(recurringDate))
            val nowCal = Calendar.getInstance()
            nowCal.setTimeInMillis(TimeUnit.SECONDS.toMillis(now))
            getNextRecurringDate(recurringDateCal, recurrencePattern, nowCal)
            result = TimeUnit.MILLISECONDS.toSeconds(recurringDateCal.timeInMillis)
        }
        return result
    }

    fun getNextRecurringDate(recurringDate: Calendar, recurrencePattern: RecurrencePattern, now: Calendar) {
        if (recurrencePattern != RecurrencePattern.NONE) {
            // TODO When API 26 can be used, use ChronoUnit for a better implementation of this method.
            // Jump to previous year to have less iterations.
            val previousYear = now[Calendar.YEAR] - 1
            if (recurringDate[Calendar.YEAR] < previousYear) {
                recurringDate[Calendar.YEAR] = previousYear
            }
            while (recurringDate < now) {
                addInterval(recurringDate, recurrencePattern)
            }
        }
    }

    fun addInterval(date: Calendar, recurrencePattern: RecurrencePattern, amount: Int = 1) {
        when (recurrencePattern) {
            RecurrencePattern.NONE -> Log.e(TAG, "Unable to add interval because no recurrence pattern set.")
            RecurrencePattern.DAILY -> date.add(Calendar.DAY_OF_YEAR, amount)
            RecurrencePattern.WEEKLY -> date.add(Calendar.WEEK_OF_YEAR, amount)
            RecurrencePattern.MONTHLY -> date.add(Calendar.MONTH, amount)
            RecurrencePattern.YEARLY -> date.add(Calendar.YEAR, amount)
        }
    }

    fun getDeadlineColor(context: Context, color: DeadlineColors?): Int {
        return when (color) {
            DeadlineColors.RED -> ContextCompat.getColor(context, R.color.deadline_red)
            DeadlineColors.BLUE -> ContextCompat.getColor(context, R.color.deadline_blue)
            DeadlineColors.ORANGE -> ContextCompat.getColor(context, R.color.deadline_orange)
            else -> throw IllegalArgumentException("Unknown deadline color '$color'.")
        }
    }

    fun recurrencePatternToString(context: Context, recurrencePattern: RecurrencePattern?): String {
        return when (recurrencePattern) {
            RecurrencePattern.NONE -> context.resources.getString(R.string.none)
            RecurrencePattern.DAILY -> context.resources.getString(R.string.daily)
            RecurrencePattern.WEEKLY -> context.resources.getString(R.string.weekly)
            RecurrencePattern.MONTHLY -> context.resources.getString(R.string.monthly)
            RecurrencePattern.YEARLY -> context.resources.getString(R.string.yearly)
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

    fun snoozeDurationToString(context: Context, snoozeDuration: Long): String {
        val snoozeDurationValues = context.resources.getStringArray(R.array.snooze_duration_values)
        for (index in snoozeDurationValues.indices) {
            if (snoozeDurationValues[index].toLong() == snoozeDuration) {
                val valuesHuman = context.resources.getStringArray(R.array.snooze_duration_values_human)
                if (index < valuesHuman.size) {
                    return valuesHuman[index]
                }
                break
            }
        }
        return "Unknown snooze duration '$snoozeDuration'"
    }

    fun getMenuHeader(context: Context, title: String?): TextView {
        val blueBackground = TextView(context)
        blueBackground.setLayoutParams(LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT))
        blueBackground.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
        blueBackground.text = title
        blueBackground.setTextColor(ContextCompat.getColor(context, R.color.black))
        blueBackground.setPadding(65, 65, 65, 65)
        blueBackground.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
        blueBackground.setTypeface(null, Typeface.BOLD)
        return blueBackground
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
