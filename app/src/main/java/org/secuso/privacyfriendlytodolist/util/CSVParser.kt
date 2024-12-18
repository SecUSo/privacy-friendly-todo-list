/*
Privacy Friendly To-Do List
Copyright (C) 2024  Christian Adams

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

import java.io.Reader

class CSVParser {
    private val fieldSeparator = ','
    private val sb = StringBuilder()

    private enum class State {
        OUTSIDE_ESC_SEQ,
        INSIDE_ESC_SEQ,
        POTENTIAL_END_OF_ESC_SEQ
    }

    fun parse(reader: Reader): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        reader.forEachLine { line ->
            val row = parseLine(line)
            rows.add(row)
        }
        return rows
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
