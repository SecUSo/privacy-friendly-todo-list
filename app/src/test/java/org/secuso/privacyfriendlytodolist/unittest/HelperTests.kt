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
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.secuso.privacyfriendlytodolist.model.impl.TodoTaskImpl
import org.secuso.privacyfriendlytodolist.util.Helper
import org.secuso.privacyfriendlytodolist.util.Timestamp

class HelperTests {
    @Test
    fun compareDeadlinesTest() {
        val tNoDl = TodoTaskImpl()
        val tDl1 = TodoTaskImpl()
        tDl1.setDeadline(Timestamp.createBySeconds(-1000))
        val tDl2 = TodoTaskImpl()
        tDl2.setDeadline(Timestamp.createBySeconds(1000))

        assertEquals(0, Helper.compareDeadlines(tNoDl, tNoDl))
        assertEquals(0, Helper.compareDeadlines(tDl1, tDl1))
        assertEquals(0, Helper.compareDeadlines(tDl2, tDl2))
        assertTrue(Helper.compareDeadlines(tNoDl, tDl1) > 0)
        assertTrue(Helper.compareDeadlines(tDl1, tNoDl) < 0)
        assertTrue(Helper.compareDeadlines(tNoDl, tDl2) > 0)
        assertTrue(Helper.compareDeadlines(tDl2, tNoDl) < 0)
        assertTrue(Helper.compareDeadlines(tDl1, tDl2) < 0)
        assertTrue(Helper.compareDeadlines(tDl2, tDl1) > 0)
   }
}