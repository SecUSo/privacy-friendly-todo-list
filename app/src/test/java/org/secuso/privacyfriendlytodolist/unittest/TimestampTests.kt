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
package org.secuso.privacyfriendlytodolist.unittest

import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.secuso.privacyfriendlytodolist.model.TodoTask.RecurrencePattern
import org.secuso.privacyfriendlytodolist.util.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
class TimestampTests {
    @Test
    fun getNextRecurringDateTest() {
        val start = "1900-03-01"
        var dest = "2025-04-27"
        testNextRecurringDate(start, RecurrencePattern.MONTHLY, 1, dest, "2025-05-01")
        testNextRecurringDate(start, RecurrencePattern.MONTHLY, 2, dest, "2025-05-01")
        testNextRecurringDate("2024-03-01", RecurrencePattern.MONTHLY, 3, dest, "2025-06-01")
        testNextRecurringDate("2024-03-01", RecurrencePattern.MONTHLY, 4, dest, "2025-07-01")
        testNextRecurringDate("2024-03-01", RecurrencePattern.MONTHLY, 5, dest, "2025-06-01")
        testNextRecurringDate(start, RecurrencePattern.MONTHLY, 6, dest, "2025-09-01")

        dest = "2025-02-27"
        testNextRecurringDate(start, RecurrencePattern.YEARLY, 1, dest, "2025-03-01")
        testNextRecurringDate(start, RecurrencePattern.YEARLY, 2, dest, "2026-03-01")
        testNextRecurringDate(start, RecurrencePattern.YEARLY, 3, dest, "2026-03-01")
        testNextRecurringDate(start, RecurrencePattern.YEARLY, 4, dest, "2028-03-01")
        testNextRecurringDate(start, RecurrencePattern.YEARLY, 5, dest, "2025-03-01")
        testNextRecurringDate(start, RecurrencePattern.YEARLY, 6, dest, "2026-03-01")
    }

    @Test
    fun getNextRecurringDateOrTodayTest() {
        val start = "1900-03-01"
        var dest = "2025-05-01"
        testNextRecurringDate(start, RecurrencePattern.MONTHLY, 1, dest, "2025-05-01", true)
        testNextRecurringDate(start, RecurrencePattern.MONTHLY, 2, dest, "2025-05-01", true)
        testNextRecurringDate("2024-03-01", RecurrencePattern.MONTHLY, 3, dest, "2025-06-01", true)
        testNextRecurringDate("2024-03-01", RecurrencePattern.MONTHLY, 4, dest, "2025-07-01", true)
        testNextRecurringDate("2024-03-01", RecurrencePattern.MONTHLY, 5, dest, "2025-06-01", true)
        testNextRecurringDate(start, RecurrencePattern.MONTHLY, 6, dest, "2025-09-01", true)

        dest = "2025-02-27"
        testNextRecurringDate(start, RecurrencePattern.YEARLY, 1, dest, "2025-03-01", true)
        testNextRecurringDate(start, RecurrencePattern.YEARLY, 2, dest, "2026-03-01", true)
        testNextRecurringDate(start, RecurrencePattern.YEARLY, 3, dest, "2026-03-01", true)
        testNextRecurringDate(start, RecurrencePattern.YEARLY, 4, dest, "2028-03-01", true)
        testNextRecurringDate(start, RecurrencePattern.YEARLY, 5, dest, "2025-03-01", true)
        testNextRecurringDate(start, RecurrencePattern.YEARLY, 6, dest, "2026-03-01", true)
    }

    @Test
    fun getNextWeekdayTest() {
        var start = "1900-03-01"
        val dest = "2025-11-28" // Friday
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS_M______, 1, dest, "2025-12-01") // Monday
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS_MT_____, 1, dest, "2025-12-01") // Monday
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS_MTW____, 1, dest, "2025-12-01") // Monday
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS_MTWT___, 1, dest, "2025-12-01") // Monday
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS_MTWTF__, 1, dest, "2025-12-01") // Monday
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS_MTWTFS_, 1, dest, "2025-11-29") // Saturday
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS_MTWTFSS, 1, dest, "2025-11-29") // Saturday

        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS_M______, 1, dest, "2025-12-01") // Monday
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS__T_____, 1, dest, "2025-12-02") // Tuesday
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS___W____, 1, dest, "2025-12-03") // Wednesday
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS____T___, 1, dest, "2025-12-04") // Thursday
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS_____F__, 1, dest, "2025-12-05") // Friday
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS______S_, 1, dest, "2025-11-29") // Saturday
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS_______S, 1, dest, "2025-11-30") // Sunday

        start = dest
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS_M______, 3, dest, "2025-12-15")
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS_MT_____, 3, dest, "2025-12-08")
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS_MTW____, 3, dest, "2025-12-03")
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS_MTWT___, 3, dest, "2025-12-03")
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS_MTWTF__, 3, dest, "2025-12-03")
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS_MTWTFS_, 3, dest, "2025-12-02")
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS_MTWTFSS, 3, dest, "2025-12-01")

        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS_M______, 3, dest, "2025-12-15")
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS__T_____, 3, dest, "2025-12-16")
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS___W____, 3, dest, "2025-12-17")
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS____T___, 3, dest, "2025-12-18")
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS_____F__, 3, dest, "2025-12-19")
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS______S_, 3, dest, "2025-12-13")
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS_______S, 3, dest, "2025-12-14")
    }

    @Test
    fun getPreviousWeekdayTest() {
        val start = "2025-11-28" // Friday
        val dest = start
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS_M______, -2, dest, "2025-11-17")
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS_MT_____, -2, dest, "2025-11-24")
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS_MTW____, -2, dest, "2025-11-25")
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS_MTWT___, -2, dest, "2025-11-26")
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS_MTWTF__, -2, dest, "2025-11-26")
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS_MTWTFS_, -2, dest, "2025-11-26")
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS_MTWTFSS, -2, dest, "2025-11-26")

        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS_M______, -2, dest, "2025-11-17")
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS__T_____, -2, dest, "2025-11-18")
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS___W____, -2, dest, "2025-11-19")
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS____T___, -2, dest, "2025-11-20")
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS_____F__, -2, dest, "2025-11-14")
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS______S_, -2, dest, "2025-11-15")
        testNextRecurringDate(start, RecurrencePattern.WEEKDAYS_______S, -2, dest, "2025-11-16")
    }

    private fun testNextRecurringDate(recurringDate: String,
                                      recurrencePattern: RecurrencePattern,
                                      recurrenceInterval: Int,
                                      destinationDate: String,
                                      expectedDate: String,
                                      acceptDestDate: Boolean = false) {
        val result = dateStrToTimestamp(recurringDate).getNextRecurringDate(
            recurrencePattern, recurrenceInterval, dateStrToTimestamp(destinationDate), acceptDestDate).first
        val resultStr = result.createCanonicalDateString()
        assertEquals(expectedDate, resultStr)
    }

    private fun localToGMT(timeInSeconds: Long): Long {
        var timeMs = timeInSeconds * 1000
        timeMs -= TimeZone.getDefault().getOffset(timeMs)
        return timeMs / 1000
    }

    @Test
    fun timeInDaysTest() {
        for (timeInSeconds in arrayOf(
            Pair(localToGMT(-10000000), null),
            Pair(localToGMT(-86401), -2L),
            Pair(localToGMT(-86400), -1L),
            Pair(localToGMT(-1), -1L),
            Pair(localToGMT(0L), 0L),
            Pair(localToGMT(1), 0L),
            Pair(localToGMT(86399), 0L),
            Pair(localToGMT(86400), 1L),
            Pair(localToGMT(10000000), null))) {
            var time = Timestamp.createBySeconds(timeInSeconds.first)
            val timeInDaysComputed = time.timeInDays
            val timeInDays: Long = timeInSeconds.second ?: timeInDaysComputed
            println("$time timeInSeconds, timeInDays expected: $timeInSeconds, timeInDays computed: $timeInDaysComputed")

            time = setTimePartByTest(time, 0, 0, 0)
            assertEquals("$time", timeInDays, time.timeInDays)

            assertEquals(time.subtractSeconds(1).toString(), timeInDays - 1, time.subtractSeconds(1).timeInDays)

            time = setTimePartByTest(time, 8, 30, 20)
            assertEquals("$time", timeInDays, time.timeInDays)

            time = setTimePartByTest(time, 15, 7, 33)
            assertEquals("$time", timeInDays, time.timeInDays)

            time = setTimePartByTest(time, 23, 59, 59)
            assertEquals("$time", timeInDays, time.timeInDays)

            assertEquals("$time", timeInDays + 1, time.addSeconds(1).timeInDays)
        }
    }

    private fun setTimePartByTest(timestamp: Timestamp, hourOfDay: Int, minute: Int, second: Int): Timestamp {
        val calendar = timestamp.toCalendar()
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, second)
        calendar.set(Calendar.MILLISECOND, 0)
        return Timestamp.createByCalendar(calendar)
    }

    @Test
    fun setTimePartTest() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+01:00"))

        // Epoch timestamp in milliseconds: 1766185200000
        // Date and time (GMT): Friday, 19. December 2025 23:00:00
        // Date and time (current): Saturday, 20. December 2025 00:00:00 GMT+01:00
        var dateStr = "2025-12-20"
        var dateInMillis = 1766185200000
        setAndCheckTimePart(dateStr, 0, 0, 0, dateInMillis)
        setAndCheckTimePart(dateStr, 8, 30, 0, dateInMillis + Duration.parse("8h 30m").inWholeMilliseconds)
        setAndCheckTimePart(dateStr, 12, 13, 14, dateInMillis + Duration.parse("12h 13m 14s").inWholeMilliseconds)
        setAndCheckTimePart(dateStr, 23, 59, 59, dateInMillis + Duration.parse("23h 59m 59s").inWholeMilliseconds)

        // Epoch timestamp in milliseconds: -284086800000
        //Date and time (GMT): Friday, 30. December 1960 23:00:00
        //Date and time (current): Saturday, 31. December 1960 00:00:00 GMT+01:00
        dateStr = "1960-12-31"
        dateInMillis = -284086800000
        setAndCheckTimePart(dateStr, 0, 0, 0, dateInMillis)
        setAndCheckTimePart(dateStr, 8, 30, 0, dateInMillis + Duration.parse("8h 30m").inWholeMilliseconds)
        setAndCheckTimePart(dateStr, 12, 13, 14, dateInMillis + Duration.parse("12h 13m 14s").inWholeMilliseconds)
        setAndCheckTimePart(dateStr, 23, 59, 59, dateInMillis + Duration.parse("23h 59m 59s").inWholeMilliseconds)

        var timeInSeconds = 0L
        setAndCheckTimePart(timeInSeconds, 0, 0, 0)
        setAndCheckTimePart(timeInSeconds, 8, 30, 0)
        setAndCheckTimePart(timeInSeconds, 12, 13, 14)
        setAndCheckTimePart(timeInSeconds, 23, 59, 59)
        timeInSeconds = -1000000L
        setAndCheckTimePart(timeInSeconds, 0, 0, 0)
        setAndCheckTimePart(timeInSeconds, 8, 30, 0)
        setAndCheckTimePart(timeInSeconds, 12, 13, 14)
        setAndCheckTimePart(timeInSeconds, 23, 59, 59)
        timeInSeconds = 1000000L
        setAndCheckTimePart(timeInSeconds, 0, 0, 0)
        setAndCheckTimePart(timeInSeconds, 8, 30, 0)
        setAndCheckTimePart(timeInSeconds, 12, 13, 14)
        setAndCheckTimePart(timeInSeconds, 23, 59, 59)
        timeInSeconds = 123456789L
        setAndCheckTimePart(timeInSeconds, 0, 0, 0)
        setAndCheckTimePart(timeInSeconds, 8, 30, 0)
        setAndCheckTimePart(timeInSeconds, 12, 13, 14)
        setAndCheckTimePart(timeInSeconds, 23, 59, 59)
    }

    private fun setAndCheckTimePart(timeInSeconds: Long, hourOfDay: Int, minute: Int, second: Int, timeInMillisExpectedManually: Long? = null) {
        val timestamp = Timestamp.createBySeconds(timeInSeconds)
        setAndCheckTimePart(timestamp, timestamp.createCanonicalDateString(), hourOfDay, minute, second, timeInMillisExpectedManually)
    }

    private fun setAndCheckTimePart(dateStr: String, hourOfDay: Int, minute: Int, second: Int, timeInMillisExpectedManually: Long? = null) {
        setAndCheckTimePart(dateStrToTimestamp(dateStr), dateStr, hourOfDay, minute, second, timeInMillisExpectedManually)
    }

    private fun setAndCheckTimePart(timestamp: Timestamp, dateStr: String, hourOfDay: Int, minute: Int, second: Int, timeInMillisExpectedManually: Long? = null) {
        val calendar = timestamp.toCalendar()
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, second)
        calendar.set(Calendar.MILLISECOND, 0)
        val timeInMillisExpected = calendar.timeInMillis

        val newTimestamp = timestamp.setTimePart(hourOfDay, minute, second)
        val timeInMillisActual = newTimestamp.timeInMillis
        val diff = (timeInMillisActual - timeInMillisExpected).toDuration(DurationUnit.MILLISECONDS)

        if (null != timeInMillisExpectedManually) {
            assertEquals("Difference: $diff.", timeInMillisExpectedManually, timeInMillisExpected)
            assertEquals("Difference: $diff.", timeInMillisExpectedManually, timeInMillisActual)
        }
        
        val stringExpected = String.format("${dateStr}T%02d:%02d:%02d", hourOfDay, minute, second)
        val stringTimestamp = DATE_TIME_FORMAT.format(Date(timeInMillisActual))
        val stringCalendar = DATE_TIME_FORMAT.format(calendar.time)
        assertEquals("Difference: $diff (str: $stringExpected vs $stringTimestamp).", timeInMillisExpected, timeInMillisActual)
        assertEquals("Timestamp string", stringExpected, stringTimestamp)
        assertEquals("Calendar string", stringExpected, stringCalendar)
    }

    private fun dateStrToTimestamp(dateStr: String): Timestamp {
        val date = DATE_FORMAT.parse(dateStr)
        return Timestamp.createByMillis(date!!.time)
    }

    companion object {
        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd")
        private val DATE_TIME_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    }
}