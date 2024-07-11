package org.secuso.privacyfriendlytodolist.unittest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.util.Helper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class HelperTests {
    @Test
    public void computeRepetitionsTest() {
        assertEquals(2 * 365, Helper.INSTANCE.computeRepetitions(
                dateToSec("2001-03-12"), dateToSec("2003-03-12"), TodoTask.RecurrencePattern.DAILY));
        assertEquals(3 * 365 + 1, Helper.INSTANCE.computeRepetitions(
                dateToSec("2001-03-12"), dateToSec("2004-03-12"), TodoTask.RecurrencePattern.DAILY));

        assertEquals(20 * 52 + 1, Helper.INSTANCE.computeRepetitions(
                dateToSec("1984-03-12"), dateToSec("2004-03-19"), TodoTask.RecurrencePattern.WEEKLY));

        assertEquals(20 * 12 + 8, Helper.INSTANCE.computeRepetitions(
                dateToSec("1984-03-12"), dateToSec("2004-11-19"), TodoTask.RecurrencePattern.MONTHLY));

        assertEquals(20, Helper.INSTANCE.computeRepetitions(
                dateToSec("1984-03-12"), dateToSec("2004-11-19"), TodoTask.RecurrencePattern.YEARLY));
    }

    private long dateToSec(String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        return date.toEpochSecond(LocalTime.MIN, ZoneOffset.UTC);
    }
}