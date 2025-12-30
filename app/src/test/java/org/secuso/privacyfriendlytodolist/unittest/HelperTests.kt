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
package org.secuso.privacyfriendlytodolist.unittest

import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.secuso.privacyfriendlytodolist.model.TodoTask.RecurrencePattern
import org.secuso.privacyfriendlytodolist.util.Helper
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
class HelperTests {
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
                                      expected: String) {
        val result = Helper.getNextRecurringDate(dateToSec(recurringDate),
            recurrencePattern, recurrenceInterval, dateToSec(destinationDate))
        val resultStr = secToDate(result)
        assertEquals(expected, resultStr)
    }

    private fun dateToSec(dateStr: String): Long {
        val date = LocalDate.parse(dateStr)
        return date.toEpochSecond(LocalTime.MIN, ZoneOffset.UTC)
    }

    private fun secToDate(timestampS: Long): String {
        val dateTime = Date(TimeUnit.SECONDS.toMillis(timestampS))
        val canonicalDateTimeFormat = SimpleDateFormat("yyyy-MM-dd")
        return canonicalDateTimeFormat.format(dateTime)
    }
}