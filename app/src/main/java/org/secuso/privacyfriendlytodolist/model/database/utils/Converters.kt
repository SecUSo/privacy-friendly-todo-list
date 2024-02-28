/*
 This file is part of Privacy Friendly To-Do List.

 Privacy Friendly To-Do List is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.

 Privacy Friendly To-Do List is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Privacy Friendly To-Do List. If not, see <http://www.gnu.org/licenses/>.
 */

package org.secuso.privacyfriendlytodolist.model.database.utils

import android.util.Log

import androidx.room.TypeConverter

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object Converters {
    private const val DB_DATE_PATTERN = "yyyy-MM-dd"
    private val formatter = SimpleDateFormat(DB_DATE_PATTERN, Locale.ENGLISH)

    @TypeConverter
    @JvmStatic
    fun fromDate(date: Date?): String? {
        return date?.let { formatter.format(it) }
    }

    @TypeConverter
    @JvmStatic
    fun toDate(date: String?): Date? {
        if (date == null) return null
        return try {
            formatter.parse(date)
        } catch (e: ParseException) {
            Log.e(Converters.javaClass.simpleName, e.message, e)
            return null
        }
    }
}