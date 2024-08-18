package org.secuso.privacyfriendlytodolist.util

import java.io.Writer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class CSVBuilder(private val writer: Writer) {
    private var isFirstFieldInRow = true

    private fun addFieldSeparator() {
        if (isFirstFieldInRow) {
            isFirstFieldInRow = false
        } else {
            writer.write(FIELD_SEPARATOR)
        }
    }

    fun addEmptyField() {
        addFieldSeparator()
    }

    fun addField(content: Int) {
        addFieldSeparator()
        writer.write(content.toString())
    }

    fun addField(content: String) {
        addFieldSeparator()
        var escapedContent = content.replace(Regex("\\R"), " ")
        if (escapedContent.contains(Regex("[,\"']"))) {
            escapedContent = escapedContent.replace("\"", "\"\"")
            escapedContent = "\"" + escapedContent + "\""
        }
        writer.write(escapedContent)
    }

    fun addTimeField(time: Long) {
        addFieldSeparator()
        if (time > 0) {
            val dateTime = Date(TimeUnit.SECONDS.toMillis(time))
            val dateTimeString = DATE_TIME_FORMAT.format(dateTime)
            writer.write(dateTimeString)
        }
    }

    fun startNewRow() {
        writer.appendLine()
        isFirstFieldInRow = true
    }

    companion object {
        val DATE_TIME_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        const val FIELD_SEPARATOR = ","
    }
}
