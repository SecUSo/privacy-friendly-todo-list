/*
Privacy Friendly To-Do List
Copyright (C) 2025  Christian Adams

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
import android.util.Log
import android.widget.Button
import android.widget.RadioButton
import android.widget.CheckBox
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.TodoTask.RecurrencePattern
import org.secuso.privacyfriendlytodolist.util.LogTag

/**
 * This class creates a dialog that lets the user choose a recurrence pattern for a task.
 *
 * Created by Christian Adams on 26.11.2025.
 */
@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class RecurrencePatternDialog(context: Context, private var recurrencePattern: RecurrencePattern):
        FullScreenDialog<ResultCallback<RecurrencePattern>>(context, R.layout.recurrence_pattern_dialog) {

    // GUI elements
    private lateinit var patternNoneButton: RadioButton
    private lateinit var patternDaysButton: RadioButton
    private lateinit var patternWeeksButton: RadioButton
    private lateinit var patternMonthsButton: RadioButton
    private lateinit var patternYearsButton: RadioButton
    private lateinit var weekdayButtons: Array<CheckBox>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        patternNoneButton = findViewById(R.id.rb_pattern_none)
        patternDaysButton = findViewById(R.id.rb_pattern_days)
        patternWeeksButton = findViewById(R.id.rb_pattern_weeks)
        patternMonthsButton = findViewById(R.id.rb_pattern_months)
        patternYearsButton = findViewById(R.id.rb_pattern_years)
        val patternButtons = arrayOf(patternNoneButton, patternDaysButton, patternWeeksButton, patternMonthsButton, patternYearsButton)
        weekdayButtons = arrayOf(
            findViewById(R.id.cb_weekday_monday),
            findViewById(R.id.cb_weekday_tuesday),
            findViewById(R.id.cb_weekday_wednesday),
            findViewById(R.id.cb_weekday_thursday),
            findViewById(R.id.cb_weekday_friday),
            findViewById(R.id.cb_weekday_saturday),
            findViewById(R.id.cb_weekday_sunday)
        )

        if (recurrencePattern in RecurrencePattern.WEEKDAYS_M______ .. RecurrencePattern.WEEKDAYS_MTWTFSS) {
            patternWeeksButton.isChecked = true
            val weekdaysBitmask = recurrencePattern.ordinal - RecurrencePattern.WEEKDAYS_M______.ordinal + 1
            for (index in 0 .. 6) {
                weekdayButtons[index].isChecked = ((weekdaysBitmask and (1 shl index)) != 0)
            }
        }
        else {
            when (recurrencePattern) {
                RecurrencePattern.NONE -> patternNoneButton.isChecked = true
                RecurrencePattern.DAILY -> patternDaysButton.isChecked = true
                RecurrencePattern.WEEKLY -> patternWeeksButton.isChecked = true
                RecurrencePattern.MONTHLY -> patternMonthsButton.isChecked = true
                RecurrencePattern.YEARLY -> patternYearsButton.isChecked = true
                else -> Log.e(TAG, "Unhandled recurrence pattern: $recurrencePattern")
            }
        }

        for (button in patternButtons) {
            button.setOnClickListener {
                for (button in weekdayButtons) {
                    button.isChecked = false
                }
            }
        }

        for (button in weekdayButtons) {
            button.setOnClickListener {
                patternWeeksButton.isChecked = true
            }
        }

        val okButton: Button = findViewById(R.id.bt_pattern_ok)
        okButton.setOnClickListener {
            updateRecurrencePattern()
            getDialogCallback().onFinish(recurrencePattern)
            dismiss()
        }

        val cancelButton: Button = findViewById(R.id.bt_pattern_cancel)
        cancelButton.setOnClickListener {
            dismiss()
        }
    }

    private fun updateRecurrencePattern() {
        var weekdaysBitmask = 0
        for (index in 0..6) {
            if (weekdayButtons[index].isChecked) {
                weekdaysBitmask = weekdaysBitmask or (1 shl index)
            }
        }
        if (weekdaysBitmask != 0) {
            val newRecurrencePattern = RecurrencePattern.fromOrdinal(weekdaysBitmask + RecurrencePattern.WEEKDAYS_M______.ordinal - 1)
            if (null != newRecurrencePattern) {
                recurrencePattern = newRecurrencePattern
            } else {
                recurrencePattern = RecurrencePattern.WEEKLY
                Log.e(TAG, "Failed to create weekdays-pattern with weekdays-bitmask $weekdaysBitmask.")
            }
        } else {
            recurrencePattern =
                if (patternNoneButton.isChecked) RecurrencePattern.NONE
                else if (patternDaysButton.isChecked) RecurrencePattern.DAILY
                else if (patternMonthsButton.isChecked) RecurrencePattern.MONTHLY
                else if (patternYearsButton.isChecked) RecurrencePattern.YEARLY
                else {
                    Log.e(TAG, "Unhandled pattern button.")
                    RecurrencePattern.NONE
                }
        }
    }

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
    }
}
