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
import org.secuso.privacyfriendlytodolist.util.CSVBuilder
import java.io.StringWriter

class CSVBuilderTests {
    @Test
    fun lineBreakTest() {
        val output = StringWriter()
        val csvBuilder = CSVBuilder(output)

        csvBuilder.addField("a${System.lineSeparator()}b")
        assertEquals("a b", output.toString())
    }

    @Test
    fun escapeTest() {
        val output = StringWriter()
        val csvBuilder = CSVBuilder(output)

        csvBuilder.addField("ab")
        csvBuilder.addField("a'b")
        csvBuilder.addField("a,b")
        csvBuilder.addField("a\"b")
        csvBuilder.addField("ba")
        assertEquals("ab,\"a'b\",\"a,b\",\"a\"\"b\",ba", output.toString())
    }
}
