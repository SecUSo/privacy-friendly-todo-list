/*
Privacy Friendly To-Do List
Copyright (C) 2016-2025  Dominik Puellen

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
package org.secuso.privacyfriendlytodolist.view.dialog

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.util.PreferenceMgr
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.concurrent.TimeUnit

interface DeadlineCallback {
    fun setDeadline(selectedDeadline: Long)
    fun removeDeadline()
}

class DeadlineDialog(context: Context, private val deadline: Long?) :
    FullScreenDialog<DeadlineCallback>(context, R.layout.deadline_dialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val calendar1 = GregorianCalendar.getInstance()
        if (deadline != null) {
            calendar1.setTimeInMillis(TimeUnit.SECONDS.toMillis(deadline))
        } else {
            calendar1.setTime(Calendar.getInstance().time)
        }
        val datePicker: DatePicker = findViewById(R.id.dp_deadline)
        datePicker.updateDate(
            calendar1[Calendar.YEAR],
            calendar1[Calendar.MONTH],
            calendar1[Calendar.DAY_OF_MONTH]
        )
        datePicker.firstDayOfWeek = PreferenceMgr.getFirstDayOfWeek(context)
        val buttonOkay: Button = findViewById(R.id.bt_deadline_ok)
        buttonOkay.setOnClickListener {
            val calendar2: Calendar = GregorianCalendar(
                datePicker.year, datePicker.month, datePicker.dayOfMonth, 12, 0)
            /*
             TODO Deadline gets stored as milliseconds since epoch UTC, which is a timestamp.
                With changing the timezone, the time of this timestamp changes. If it changes more
                than 12 hours not only the time changes but also the date which is bad for the
                deadline.
                Potential fixes:
                a) Store deadline as days-since-epoch.
                b) General change at timestamp handling in the app: Store them together with the
                   timezone where they were created.
                   This might improve reminder time too: Now the reminder time changes with the
                   timezone. Creating reminder for 5 o'clock and traveling from timezone +2 to 0
                   will result in a reminder at 3 o'clock in the new timezone.
                   With the knowledge of the timezone it would be possible to have the reminder at
                   the local 5 o'clock regardless to which timezone the user travels.
             */
            getDialogCallback().setDeadline(TimeUnit.MILLISECONDS.toSeconds(calendar2.getTimeInMillis()))
            dismiss()
        }
        val buttonNoDeadline: Button = findViewById(R.id.bt_deadline_nodeadline)
        buttonNoDeadline.setOnClickListener {
            getDialogCallback().removeDeadline()
            dismiss()
        }
    }
}
