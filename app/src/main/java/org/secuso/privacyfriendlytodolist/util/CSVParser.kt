package org.secuso.privacyfriendlytodolist.util

import java.io.InputStream

class CSVParser {
    private val fieldSeparator = ','
    private val sb = StringBuilder()

    private enum class State {
        OUTSIDE_ESC_SEQ,
        INSIDE_ESC_SEQ,
        POTENTIAL_END_OF_ESC_SEQ
    }

    fun parse(inputStream: InputStream): List<List<String>> {
        val lines = mutableListOf<List<String>>()
        inputStream.bufferedReader().use { br ->
            var line = br.readLine()
            while (null != line) {
                lines.add(parseLine(line))
                line = br.readLine()
            }
        }
        return lines
    }

    private fun parseLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        sb.clear()
        var lastChar = '"'
        var state = State.OUTSIDE_ESC_SEQ
        for (char in line) {
            when (state) {
                State.OUTSIDE_ESC_SEQ -> {
                    when (char) {
                        '"' -> {
                            state = State.INSIDE_ESC_SEQ
                        }
                        fieldSeparator -> {
                            fields.add(sb.toString())
                            sb.clear()
                        }
                        else -> {
                            sb.append(char)
                        }
                    }
                }

                State.INSIDE_ESC_SEQ -> {
                    if (char == '"') {
                        state = State.POTENTIAL_END_OF_ESC_SEQ
                    } else {
                        sb.append(char)
                    }
                }

                State.POTENTIAL_END_OF_ESC_SEQ -> {
                    when (char) {
                        '"' -> {
                            // Double double quote ("") is the escape sequence for ". So add " to sb.
                            state = State.INSIDE_ESC_SEQ
                            sb.append(char)
                        }
                        fieldSeparator -> {
                            // Single double quote means end of escape sequence.
                            state = State.OUTSIDE_ESC_SEQ
                            fields.add(sb.toString())
                            sb.clear()
                        }
                        else -> {
                            // Single double quote means end of escape sequence.
                            state = State.OUTSIDE_ESC_SEQ
                            sb.append(char)
                        }
                    }
                }
            }
            lastChar = char
        }

        if (lastChar == fieldSeparator || sb.isNotEmpty()) {
            fields.add(sb.toString())
        }

        return fields
    }
}
