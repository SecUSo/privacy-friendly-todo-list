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
package org.secuso.privacyfriendlytodolist.view.dialog

import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.TimePicker
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.util.Helper
import org.secuso.privacyfriendlytodolist.util.PreferenceMgr
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.concurrent.TimeUnit

interface ReminderCallback {
    fun setReminderTime(selectedReminderTime: Long)
    fun removeReminderTime()
}

@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class ReminderDialog(context: Context, private val reminderTime: Long?, private val deadline: Long?) :
    FullScreenDialog<ReminderCallback>(context, R.layout.reminder_dialog) {

    private lateinit var layoutDate: LinearLayout
    private lateinit var layoutTime: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutDate = findViewById(R.id.ll_reminder_date)
        layoutTime = findViewById(R.id.ll_reminder_time)

        val now = Helper.getCurrentTimestamp()
        @Suppress("IfThenToElvis")
        val reminderTimeSuggestion =
            if (reminderTime != null) {
                reminderTime
            } else if (deadline != null && deadline - DEFAULT_REMINDER_TIME_OFFSET >= now) {
                deadline - DEFAULT_REMINDER_TIME_OFFSET
            } else {
                now + PreferenceMgr.getDefaultReminderTimeSpan(context)
            }
        val calendar = GregorianCalendar.getInstance()
        calendar.setTimeInMillis(TimeUnit.SECONDS.toMillis(reminderTimeSuggestion))

        val datePicker: DatePicker = findViewById(R.id.dp_reminder)
        datePicker.init(
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]) { view, year, monthOfYear, dayOfMonth ->
            layoutDate.visibility = View.GONE
            layoutTime.visibility = View.VISIBLE
        }
        val timePicker: TimePicker = findViewById(R.id.tp_reminder)
        timePicker.setIs24HourView(DateFormat.is24HourFormat(context))
        timePicker.currentHour = calendar[Calendar.HOUR_OF_DAY]
        timePicker.currentMinute = calendar[Calendar.MINUTE]
        val buttonDate: Button = findViewById(R.id.bt_reminder_date)
        buttonDate.setOnClickListener {
            layoutDate.visibility = View.VISIBLE
            layoutTime.visibility = View.GONE
        }
        val buttonTime: Button = findViewById(R.id.bt_reminder_time)
        buttonTime.setOnClickListener {
            layoutDate.visibility = View.GONE
            layoutTime.visibility = View.VISIBLE
        }
        val buttonOkay: Button = findViewById(R.id.bt_reminder_ok)
        buttonOkay.setOnClickListener {
            val calendar2: Calendar = GregorianCalendar(
                datePicker.year,
                datePicker.month,
                datePicker.dayOfMonth,
                timePicker.currentHour,
                timePicker.currentMinute)
            val reminderTime = TimeUnit.MILLISECONDS.toSeconds(calendar2.getTimeInMillis())
            getDialogCallback().setReminderTime(reminderTime)
            dismiss()
        }
        val buttonNoReminder: Button = findViewById(R.id.bt_reminder_noreminder)
        buttonNoReminder.setOnClickListener {
            getDialogCallback().removeReminderTime()
            dismiss()
        }
    }

    companion object {
        /**
         * Default reminder time offset to deadline: 24 hours
         */
        private const val DEFAULT_REMINDER_TIME_OFFSET = 24L * 60L * 60L
    }
}
