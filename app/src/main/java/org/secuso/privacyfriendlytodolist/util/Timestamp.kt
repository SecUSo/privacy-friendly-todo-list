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
package org.secuso.privacyfriendlytodolist.util

import android.util.Log
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.model.TodoTask.RecurrencePattern
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Immutable class that holds a timestamp with granularity of one second.
 */
class Timestamp private constructor (
    /** The number of seconds since midnight, January 1, 1970 UTC. */
    val timeInSeconds: Long): Comparable<Timestamp> {

    /** The [timeInSeconds] converted to milliseconds. */
    val timeInMillis: Long
        get() = TimeUnit.SECONDS.toMillis(timeInSeconds)

    /** The [timeInSeconds] converted to days.
     *
     * This property uses the default time zone to convert number of seconds to number of days.
     * This ensures that for every time of the same day (from 00:00:00 to 23:59:59) the same number of days gets returned.
     */
    val timeInDays: Long
        get() {
            var timeMs = timeInMillis
            // Convert to GMT which is in full days beginning with epoch.
            timeMs += TimeZone.getDefault().getOffset(timeMs)
            return if (timeMs >= 0) {
                TimeUnit.MILLISECONDS.toDays(timeMs)
            } else {
                // Because positive and negative values around 0 result in 0 days
                // the day of epoch would be day 0 and the day before epoch would be day 0.
                // That's wrong so day before epoch shall be day -1.
                TimeUnit.MILLISECONDS.toDays(timeMs + 1) - 1
            }
        }

    /**
     * Sets the time-part of the timestamp to the given values and leaves the date-part as it is.
     *
     * @param hourOfDay Hour of day, used for 24-hour-clock.
     * @param minute Minute within the hour.
     * @param second Second within the minute.
     * @return New timestamp with changed time-part.
     */
    fun setTimePart(hourOfDay: Int, minute: Int, second: Int): Timestamp {
        val offsetTimeZoneS = TimeUnit.MILLISECONDS.toSeconds(TimeZone.getDefault().getOffset(timeInMillis).toLong())
        // Offset for time zone gets subtracted because timestamp is GMT and time of day shall be fix.
        // To get local time from GMT time and keeping time of day fix, the offset needs to be subtracted.
        val timeS = (((timeInDays * 24) + hourOfDay) * 3600) + (minute * 60L) + second - offsetTimeZoneS
        return createBySecondsIfDifferent(timeS)
    }

    private fun createBySecondsIfDifferent(timeInSeconds: Long): Timestamp {
        return if (timeInSeconds == this.timeInSeconds) this else createBySeconds(timeInSeconds)
    }

    private fun createByMillisIfDifferent(timeInMillis: Long): Timestamp {
        return if (timeInMillis == this.timeInMillis) this else createByMillis(timeInMillis)
    }

    fun toCalendar(): Calendar {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        return calendar
    }

    /**
     * @return The duration between this timestamp and the other timestamp.
     * If this timestamp is smaller than the other, the duration is positive.
     * Otherwise it is negative.
     */
    fun getDuration(other: Timestamp): Duration {
        return (other.timeInSeconds - timeInSeconds).toDuration(DurationUnit.SECONDS)
    }

    fun getNextRecurringDate(todoTask: TodoTask, destinationDate: Timestamp,
                             acceptDestDate: Boolean = false): Pair<Timestamp, Int> {
        return getNextRecurringDate(todoTask.getRecurrencePattern(),
            todoTask.getRecurrenceInterval(), destinationDate, acceptDestDate)
    }

    /**
     * Depending on a positive / negative recurrence interval, the function increases / decreases
     * the recurrence date to return the first recurrence date which is greater than / less than
     * the destination time.
     *
     * @todo When API 26 can be used, use ChronoUnit for a better implementation of this method.
     *
     * @param recurrencePattern The recurrence pattern.
     * @param recurrenceInterval The recurrence interval.
     * @param destinationDate The destination date for the recurrence date computation.
     * @param acceptDestDate If false, the next recurring date will be after / before the destination date
     * (depending on a positive or negative recurrence interval).
     * If true, the next recurring date might be at the destination date.
     * @return The absolute number of recurrences from base recurring date to computed recurring date.
     */
    fun getNextRecurringDate(recurrencePattern: RecurrencePattern, recurrenceInterval: Int,
                             destinationDate: Timestamp, acceptDestDate: Boolean = false): Pair<Timestamp, Int> {
        var nextRecurringDate = this
        var recurrences = 0
        if (recurrenceInterval == 0) {
            Log.e(TAG, "Invalid recurrence interval of zero.")
        } else if (recurrencePattern != RecurrencePattern.NONE) {
            val recurringDate = toCalendar()
            var destinationDateInDays = destinationDate.timeInDays
            if (recurrenceInterval > 0) {
                if (acceptDestDate) {
                    --destinationDateInDays
                }
                while (calendarToDays(recurringDate) <= destinationDateInDays) {
                    addInterval(recurringDate, recurrencePattern, recurrenceInterval)
                    ++recurrences
                }
            } else {
                // recurrenceInterval < 0
                if (acceptDestDate) {
                    ++destinationDateInDays
                }
                while (calendarToDays(recurringDate) >= destinationDateInDays) {
                    addInterval(recurringDate, recurrencePattern, recurrenceInterval)
                    ++recurrences
                }
            }
            nextRecurringDate = createByMillisIfDifferent(recurringDate.timeInMillis)
        }
        return Pair(nextRecurringDate, recurrences)
    }

    fun addInterval(todoTask: TodoTask): Timestamp {
        return addInterval(todoTask.getRecurrencePattern(), todoTask.getRecurrenceInterval())
    }

    fun addInterval(recurrencePattern: RecurrencePattern, recurrenceInterval: Int): Timestamp {
        var result = this
        if (recurrencePattern != RecurrencePattern.NONE) {
            val recurringDateCal = toCalendar()
            addInterval(recurringDateCal, recurrencePattern, recurrenceInterval)
            result = createByMillisIfDifferent(recurringDateCal.timeInMillis)
        }
        return result
    }

    private fun addInterval(date: Calendar, recurrencePattern: RecurrencePattern, recurrenceInterval: Int) {
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

    fun addSeconds(seconds: Long): Timestamp {
        return Timestamp(timeInSeconds + seconds)
    }

    fun subtractSeconds(seconds: Long): Timestamp {
        return Timestamp(timeInSeconds - seconds)
    }

    fun createLocalizedDateString(): String {
        val date = Date(timeInMillis)
        val dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.DEFAULT, Locale.getDefault())
        return dateFormat.format(date)
    }

    fun createLocalizedDateTimeString(): String {
        val dateTime = Date(timeInMillis)
        val dateTimeFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.DEFAULT, SimpleDateFormat.SHORT, Locale.getDefault())
        return dateTimeFormat.format(dateTime)
    }

    fun createCanonicalDateString(): String {
        val date = Date(timeInMillis)
        val canonicalDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return canonicalDateFormat.format(date)
    }

    fun createCanonicalDateTimeString(): String {
        val dateTime = Date(timeInMillis)
        val canonicalDateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
        return canonicalDateTimeFormat.format(dateTime)
    }

    override fun compareTo(other: Timestamp): Int {
        return timeInSeconds.compareTo(other.timeInSeconds)
    }

    override fun equals(other: Any?): Boolean {
        return other is Timestamp && other.timeInSeconds == timeInSeconds
    }

    override fun hashCode(): Int {
        return timeInSeconds.toInt()
    }

    override fun toString(): String {
        return createCanonicalDateTimeString()
    }

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)

        private fun calendarToDays(calendar: Calendar): Long {
            val timeMs = calendar.timeInMillis
            val offsetTimeZoneMs = TimeZone.getDefault().getOffset(timeMs)
            return TimeUnit.MILLISECONDS.toDays(offsetTimeZoneMs + timeMs)
        }

        fun createBySeconds(timeInSeconds: Long): Timestamp {
            return Timestamp(timeInSeconds)
        }

        fun createBySecondsIfNotNull(timeInSeconds: Long?): Timestamp? {
            return if (timeInSeconds != null) Timestamp(timeInSeconds) else null
        }

        fun createByMillis(timeInMillis: Long): Timestamp {
            return Timestamp(TimeUnit.MILLISECONDS.toSeconds(timeInMillis))
        }

        fun createByCalendar(calendar: Calendar): Timestamp {
            return Timestamp(TimeUnit.MILLISECONDS.toSeconds(calendar.timeInMillis))
        }

        fun createCurrent(): Timestamp {
            return Timestamp(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()))
        }
    }
}
