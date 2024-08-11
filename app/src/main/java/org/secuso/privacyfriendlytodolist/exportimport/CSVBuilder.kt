package org.secuso.privacyfriendlytodolist.exportimport

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class CSVBuilder {
    @Suppress("MemberVisibilityCanBePrivate")
    val fieldSeparator = ','
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val sb = StringBuilder()
    private var isFirstFieldInRow = true

    fun reset() {
        sb.clear()
        isFirstFieldInRow = true
    }

    private fun addFieldSeparator() {
        if (isFirstFieldInRow) {
            isFirstFieldInRow = false
        } else {
            sb.append(fieldSeparator)
        }
    }

    fun addEmptyField() {
        addFieldSeparator()
    }

    fun addField(content: Int) {
        addFieldSeparator()
        sb.append(content)
    }

    fun addField(content: String) {
        addFieldSeparator()
        var escapedContent = content.replace(Regex("\\R"), " ")
        if (escapedContent.contains(Regex("[,\"']"))) {
            escapedContent = escapedContent.replace("\"", "\"\"")
            escapedContent = "\"" + escapedContent + "\""
        }
        sb.append(escapedContent)
    }

    fun addTimeField(time: Long) {
        addFieldSeparator()
        if (time > 0) {
            val dateTime = Date(TimeUnit.SECONDS.toMillis(time))
            val dateTimeString = dateTimeFormat.format(dateTime)
            sb.append(dateTimeString)
        }
    }

    fun startNewRow() {
        sb.appendLine()
        isFirstFieldInRow = true
    }

    fun getCSV(): String {
        return sb.toString()
    }

    override fun toString(): String {
        return sb.toString()
    }
}
