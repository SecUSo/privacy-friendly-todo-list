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
import android.widget.Button
import android.widget.DatePicker
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.view.dialog.DeadlineDialog.DeadlineCallback
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.concurrent.TimeUnit

class DeadlineDialog(context: Context, private val deadline: Long) :
    FullScreenDialog<DeadlineCallback>(context, R.layout.deadline_dialog) {
    interface DeadlineCallback {
        fun setDeadline(deadline: Long)
        fun removeDeadline()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val calendar1 = GregorianCalendar.getInstance()
        if (deadline != -1L) {
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
        val buttonOkay: Button = findViewById(R.id.bt_deadline_ok)
        buttonOkay.setOnClickListener {
            val calendar2: Calendar = GregorianCalendar(datePicker.year, datePicker.month, datePicker.dayOfMonth)
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
