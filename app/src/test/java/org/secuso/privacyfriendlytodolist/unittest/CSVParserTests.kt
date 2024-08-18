package org.secuso.privacyfriendlytodolist.unittest

import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.secuso.privacyfriendlytodolist.util.CSVParser

class CSVParserTests {
    @Test
    fun parseTest() {
        val csvParser = CSVParser()
        val reader = CSV.reader()
        val lines = csvParser.parse(reader)
        assertEquals(3, lines.size)

        var line = lines[0]
        assertEquals("   My Text   ", line[0])
        assertEquals("", line[1])
        assertEquals("", line[2])
        assertEquals(" 123456789 ", line[3])
        assertEquals(".-;:_#'+*~@€´`ß?\\öäüÜÄÖ!§$%&/()=?", line[4])
        assertEquals("", line[5])
        assertEquals(6, line.size)

        line = lines[1]
        assertEquals(0, line.size)

        line = lines[2]
        assertEquals(",\",\"", line[0])
        assertEquals("\"afterEscSeq", line[1])
        assertEquals("\"\",", line[2])
        assertEquals(3, line.size)
    }

    companion object {
        private const val CSV = "   My Text   ,,, 123456789 ,.-;:_#'+*~@€´`ß?\\öäüÜÄÖ!§$%&/()=?,\n\n\",\"\",\"\"\",\"\"\"\"afterEscSeq,\"\"\"\"\",\n"
    }
}
