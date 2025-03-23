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
    fun getNextRecurringDateAndCountTest() {
        var now = "2025-04-27"
        testNextRecurringDate("1900-03-01", RecurrencePattern.MONTHLY, 1, now, "2025-05-01")
        testNextRecurringDate("1900-03-01", RecurrencePattern.MONTHLY, 2, now, "2025-05-01")
        testNextRecurringDate("2024-03-01", RecurrencePattern.MONTHLY, 3, now, "2025-06-01")
        testNextRecurringDate("2024-03-01", RecurrencePattern.MONTHLY, 4, now, "2025-07-01")
        testNextRecurringDate("2024-03-01", RecurrencePattern.MONTHLY, 5, now, "2025-06-01")
        testNextRecurringDate("1900-03-01", RecurrencePattern.MONTHLY, 6, now, "2025-09-01")

        now = "2025-02-27"
        testNextRecurringDate("1900-03-01", RecurrencePattern.YEARLY, 1, now, "2025-03-01")
        testNextRecurringDate("1900-03-01", RecurrencePattern.YEARLY, 2, now, "2026-03-01")
        testNextRecurringDate("1900-03-01", RecurrencePattern.YEARLY, 3, now, "2026-03-01")
        testNextRecurringDate("1900-03-01", RecurrencePattern.YEARLY, 4, now, "2028-03-01")
        testNextRecurringDate("1900-03-01", RecurrencePattern.YEARLY, 5, now, "2025-03-01")
        testNextRecurringDate("1900-03-01", RecurrencePattern.YEARLY, 6, now, "2026-03-01")
    }

    private fun testNextRecurringDate(recurringDate: String,
                                      recurrencePattern: RecurrencePattern,
                                      recurrenceInterval: Int,
                                      now: String,
                                      expected: String,
                                      offset: Int = 0) {
        val result = Helper.getNextRecurringDate(dateToSec(recurringDate), recurrencePattern,
            recurrenceInterval, dateToSec(now), offset)
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