package org.secuso.privacyfriendlytodolist.unittest

import org.junit.Assert
import org.junit.Test
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.util.Helper.computeRepetitions
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
class HelperTests {
    @Test
    fun computeRepetitionsTest() {
        Assert.assertEquals((2 * 365).toLong(), computeRepetitions(
                dateToSec("2001-03-12"), dateToSec("2003-03-12"), TodoTask.RecurrencePattern.DAILY))
        Assert.assertEquals((3 * 365 + 1).toLong(), computeRepetitions(
                dateToSec("2001-03-12"), dateToSec("2004-03-12"), TodoTask.RecurrencePattern.DAILY))
        Assert.assertEquals((20 * 52 + 1).toLong(), computeRepetitions(
                dateToSec("1984-03-12"), dateToSec("2004-03-19"), TodoTask.RecurrencePattern.WEEKLY))
        Assert.assertEquals((20 * 12 + 8).toLong(), computeRepetitions(
                dateToSec("1984-03-12"), dateToSec("2004-11-19"), TodoTask.RecurrencePattern.MONTHLY))
        Assert.assertEquals(20, computeRepetitions(
                dateToSec("1984-03-12"), dateToSec("2004-11-19"), TodoTask.RecurrencePattern.YEARLY))
    }

    private fun dateToSec(dateStr: String): Long {
        val date = LocalDate.parse(dateStr)
        return date.toEpochSecond(LocalTime.MIN, ZoneOffset.UTC)
    }
}