package org.secuso.privacyfriendlytodolist.model.impl

import org.secuso.privacyfriendlytodolist.util.CSVParser
import java.io.InputStream

class CSVImporter {
    private val csvParser = CSVParser()

    fun import(inputStream: InputStream) {
        val lines = csvParser.parse(inputStream)
        // TODO
    }
}
