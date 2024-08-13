package org.secuso.privacyfriendlytodolist.exportimport

import java.io.InputStream

class CSVImporter {
    private val csvParser = CSVParser()

    fun import(inputStream: InputStream) {
        val lines = csvParser.parse(inputStream)
        // TODO
    }
}
