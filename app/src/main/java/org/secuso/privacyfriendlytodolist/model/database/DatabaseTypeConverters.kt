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
package org.secuso.privacyfriendlytodolist.model.database

import androidx.room.TypeConverter
import org.secuso.privacyfriendlytodolist.model.TodoTask.Priority
import org.secuso.privacyfriendlytodolist.model.TodoTask.RecurrencePattern

object DatabaseTypeConverters {
    @TypeConverter
    @JvmStatic
    fun fromRecurrencePattern(recurrencePattern: RecurrencePattern?): Int? {
        return recurrencePattern?.ordinal
    }

    @TypeConverter
    @JvmStatic
    fun toRecurrencePattern(ordinal: Int?): RecurrencePattern? {
        return if (null != ordinal) RecurrencePattern.fromOrdinal(ordinal) else null
    }

    @TypeConverter
    @JvmStatic
    fun fromPriority(priority: Priority?): Int? {
        return priority?.ordinal
    }

    @TypeConverter
    @JvmStatic
    fun toPriority(ordinal: Int?): Priority? {
        return if (null != ordinal) Priority.fromOrdinal(ordinal) else null
    }
}