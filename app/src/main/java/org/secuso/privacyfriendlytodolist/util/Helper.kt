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
package org.secuso.privacyfriendlytodolist.util

import android.content.Context
import android.graphics.Typeface
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.model.TodoTask.DeadlineColors
import java.util.Calendar
import java.util.concurrent.TimeUnit

object Helper {
    const val DATE_FORMAT = "dd.MM.yyyy"
    private const val DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm"

    @JvmStatic
    fun getDate(time: Long): String {
        val calendar = Calendar.getInstance()
        calendar.setTimeInMillis(TimeUnit.SECONDS.toMillis(time))
        return DateFormat.format(DATE_FORMAT, calendar).toString()
    }

    @JvmStatic
    fun getDateTime(time: Long): String {
        val calendar = Calendar.getInstance()
        calendar.setTimeInMillis(TimeUnit.SECONDS.toMillis(time))
        return DateFormat.format(DATE_TIME_FORMAT, calendar).toString()
    }

    @JvmStatic
    fun getCurrentTimestamp(): Long {
        return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
    }

    @JvmStatic
    fun getDeadlineColor(context: Context?, color: DeadlineColors?): Int {
        return when (color) {
            DeadlineColors.RED -> ContextCompat.getColor(context!!, R.color.deadline_red)
            DeadlineColors.BLUE -> ContextCompat.getColor(context!!, R.color.deadline_blue)
            DeadlineColors.ORANGE -> ContextCompat.getColor(context!!, R.color.deadline_orange)
            else -> throw IllegalArgumentException("Deadline color '$color' not defined.")
        }
    }

    @JvmStatic
    fun priority2String(context: Context, priority: TodoTask.Priority?): String {
        return when (priority) {
            TodoTask.Priority.HIGH -> context.resources.getString(R.string.high_priority)
            TodoTask.Priority.MEDIUM -> context.resources.getString(R.string.medium_priority)
            TodoTask.Priority.LOW -> context.resources.getString(R.string.low_priority)
            else -> context.resources.getString(R.string.unknown_priority)
        }
    }

    @JvmStatic
    fun getMenuHeader(context: Context?, title: String?): TextView {
        val blueBackground = TextView(context)
        blueBackground.setLayoutParams(LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT))
        blueBackground.setBackgroundColor(ContextCompat.getColor(context!!, R.color.transparent))
        blueBackground.text = title
        blueBackground.setTextColor(ContextCompat.getColor(context, R.color.black))
        blueBackground.setPadding(65, 65, 65, 65)
        blueBackground.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
        blueBackground.setTypeface(null, Typeface.BOLD)
        return blueBackground
    }
}
