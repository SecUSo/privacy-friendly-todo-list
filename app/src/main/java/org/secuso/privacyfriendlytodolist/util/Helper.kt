package org.secuso.privacyfriendlytodolist.util

import android.content.Context
import android.content.pm.PackageManager
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

    fun createDateString(time: Long): String {
        val calendar = Calendar.getInstance()
        calendar.setTimeInMillis(TimeUnit.SECONDS.toMillis(time))
        return DateFormat.format(DATE_FORMAT, calendar).toString()
    }

    fun createDateTimeString(time: Long): String {
        val calendar = Calendar.getInstance()
        calendar.setTimeInMillis(TimeUnit.SECONDS.toMillis(time))
        return createDateTimeString(calendar)
    }

    fun createDateTimeString(calendar: Calendar): String {
        return DateFormat.format(DATE_TIME_FORMAT, calendar).toString()
    }

    fun getCurrentTimestamp(): Long {
        return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
    }

    fun getDeadlineColor(context: Context, color: DeadlineColors?): Int {
        return when (color) {
            DeadlineColors.RED -> ContextCompat.getColor(context, R.color.deadline_red)
            DeadlineColors.BLUE -> ContextCompat.getColor(context, R.color.deadline_blue)
            DeadlineColors.ORANGE -> ContextCompat.getColor(context, R.color.deadline_orange)
            else -> throw IllegalArgumentException("Deadline color '$color' not defined.")
        }
    }

    fun priority2String(context: Context, priority: TodoTask.Priority?): String {
        return when (priority) {
            TodoTask.Priority.HIGH -> context.resources.getString(R.string.high_priority)
            TodoTask.Priority.MEDIUM -> context.resources.getString(R.string.medium_priority)
            TodoTask.Priority.LOW -> context.resources.getString(R.string.low_priority)
            else -> context.resources.getString(R.string.unknown_priority)
        }
    }

    fun getMenuHeader(context: Context, title: String?): TextView {
        val blueBackground = TextView(context)
        blueBackground.setLayoutParams(LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT))
        blueBackground.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
        blueBackground.text = title
        blueBackground.setTextColor(ContextCompat.getColor(context, R.color.black))
        blueBackground.setPadding(65, 65, 65, 65)
        blueBackground.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
        blueBackground.setTypeface(null, Typeface.BOLD)
        return blueBackground
    }

    fun isPackageAvailable(packageManager: PackageManager, packageName: String): Boolean {
        // TODO Try to find a way to get the information without functional use of exception.
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}
