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
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
class HelperTests {
    @Test
    fun computeRepetitionsTest() {
        for (interval in 1..100) {
            assertEquals((2 * 365 / interval).toLong(), computeRepetitions(
                "2001-03-12", "2003-03-12", RecurrencePattern.DAILY, interval))
            assertEquals(((3 * 365 + 1) / interval).toLong(), computeRepetitions(
                "2001-03-12", "2004-03-12", RecurrencePattern.DAILY, interval))
            assertEquals(((20 * 52 + 1) / interval).toLong(), computeRepetitions(
                "1984-03-12", "2004-03-19", RecurrencePattern.WEEKLY, interval))
            assertEquals(((20 * 12 + 8) / interval).toLong(), computeRepetitions(
                "1984-03-12", "2004-11-19", RecurrencePattern.MONTHLY, interval))
            assertEquals((20 / interval).toLong(), computeRepetitions(
                "1984-03-12", "2004-11-19", RecurrencePattern.YEARLY, interval))
        }
    }

    private fun computeRepetitions(firstDate: String, followingDate: String,
                                   recurrencePattern: RecurrencePattern, recurrenceInterval: Int): Long {
        return Helper.computeRepetitions(dateToSec(firstDate), dateToSec(followingDate),
            recurrencePattern, recurrenceInterval)
    }

    private fun dateToSec(dateStr: String): Long {
        val date = LocalDate.parse(dateStr)
        return date.toEpochSecond(LocalTime.MIN, ZoneOffset.UTC)
    }
}