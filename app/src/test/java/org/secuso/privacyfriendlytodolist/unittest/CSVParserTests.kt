package org.secuso.privacyfriendlytodolist.unittest

import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.secuso.privacyfriendlytodolist.exportimport.CSVParser

class CSVParserTests {
    @Test
    fun parseCSVTest() {
        val csvParser = CSVParser()
        val inputStream = CSV.byteInputStream()
        val lines = csvParser.parse(inputStream)
        assertEquals(2, lines.size)
        var line = lines[0]
        assertEquals("   My Text   ", line[0])
        assertEquals("", line[1])
        assertEquals("", line[2])
        assertEquals(" 123456789 ", line[3])
        assertEquals(".-;:_#'+*~@€´`ß?\\öäüÜÄÖ!§$%&/()=?", line[4])
        assertEquals("", line[5])
        assertEquals(6, line.size)
        line = lines[1]
        assertEquals(",\",\"", line[0])
        assertEquals("\"afterEscSeq", line[1])
        assertEquals("\"\",", line[2])
        assertEquals(3, line.size)
    }

    companion object {
        private const val CSV = "   My Text   ,,, 123456789 ,.-;:_#'+*~@€´`ß?\\öäüÜÄÖ!§$%&/()=?,\r\n\",\"\",\"\"\",\"\"\"\"afterEscSeq,\"\"\"\"\","
    }
}
