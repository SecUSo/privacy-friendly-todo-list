/*
Privacy Friendly To-Do List
Copyright (C) 2024  SECUSO (www.secuso.org)

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

    fun addTimeField(time: Long?) {
        addFieldSeparator()
        if (time != null) {
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
