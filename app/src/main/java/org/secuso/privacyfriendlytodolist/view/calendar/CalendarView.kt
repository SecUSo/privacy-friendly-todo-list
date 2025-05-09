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
package org.secuso.privacyfriendlytodolist.view.calendar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.AdapterView.OnItemClickListener
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import org.secuso.privacyfriendlytodolist.R
import java.util.Calendar

class CalendarView : LinearLayout {
    private var currentDate: Calendar
    private var buttonPrevMonth: ImageView
    private var buttonNextMonth: ImageView
    private var tvCurrentMonth: TextView
    private var daysOfWeek: Array<TextView>
    private var calendarGrid: GridView
    private var monthNames: Array<String>
    private var daysOfWeekNames: Array<String>
    private var gridAdapter: CalendarGridAdapter? = null
    private var firstDayOfWeek = Calendar.SUNDAY

    constructor(context: Context) :
            super(context)

    constructor(context: Context, attrs: AttributeSet?) :
            super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.calendar, this)
        currentDate = Calendar.getInstance()
        buttonPrevMonth = findViewById(R.id.iv_prev_month)
        buttonNextMonth = findViewById(R.id.iv_next_month)
        tvCurrentMonth = findViewById(R.id.tv_current_month)
        daysOfWeek = arrayOf(
            findViewById(R.id.tv_1_day_of_week),
            findViewById(R.id.tv_2_day_of_week),
            findViewById(R.id.tv_3_day_of_week),
            findViewById(R.id.tv_4_day_of_week),
            findViewById(R.id.tv_5_day_of_week),
            findViewById(R.id.tv_6_day_of_week),
            findViewById(R.id.tv_7_day_of_week))
        calendarGrid = findViewById(R.id.gv_calendar_grid)

        monthNames = arrayOf(
            resources.getString(R.string.january),
            resources.getString(R.string.february),
            resources.getString(R.string.march),
            resources.getString(R.string.april),
            resources.getString(R.string.may),
            resources.getString(R.string.june),
            resources.getString(R.string.july),
            resources.getString(R.string.august),
            resources.getString(R.string.september),
            resources.getString(R.string.october),
            resources.getString(R.string.november),
            resources.getString(R.string.december))
        daysOfWeekNames = arrayOf(
            resources.getString(R.string.sunday_abbr),
            resources.getString(R.string.monday_abbr),
            resources.getString(R.string.tuesday_abbr),
            resources.getString(R.string.wednesday_abbr),
            resources.getString(R.string.thursday_abbr),
            resources.getString(R.string.friday_abbr),
            resources.getString(R.string.saturday_abbr))

        setFirstDayOfWeek(firstDayOfWeek)
    }

    fun setGridAdapter(adapter: CalendarGridAdapter?) {
        gridAdapter = adapter
        calendarGrid.setAdapter(gridAdapter)
    }

    /**
     * Sets the first day of a week for this calendar.
     *
     * @param day A weekday constant from [Calendar] class, e.g. [Calendar.MONDAY].
     */
    fun setFirstDayOfWeek(day: Int) {
        if (day >= Calendar.SUNDAY && day <= Calendar.SATURDAY) {
            firstDayOfWeek = day
            var dayNameIndex = firstDayOfWeek - 1
            for (dayViewIndex in 0..6) {
                daysOfWeek[dayViewIndex].text = daysOfWeekNames[dayNameIndex]
                dayNameIndex = (dayNameIndex + 1) % 7
            }
        }
    }

    fun setDayOnClickListener(listener: OnItemClickListener?) {
        calendarGrid.onItemClickListener = listener
    }

    fun setNextMonthOnClickListener(listener: OnClickListener?) {
        buttonNextMonth.setOnClickListener(listener)
    }

    fun setPrevMontOnClickListener(listener: OnClickListener?) {
        buttonPrevMonth.setOnClickListener(listener)
    }

    fun refresh() {
        val calendar = currentDate.clone() as Calendar
        val selectedYear = calendar[Calendar.YEAR]
        val selectedMonth = calendar[Calendar.MONTH]

        // determine cell for the current month's beginning
        calendar[Calendar.DAY_OF_MONTH] = 1
        val monthBeginningCell = (7 + calendar[Calendar.DAY_OF_WEEK] - firstDayOfWeek) % 7

        // move calendar backwards to the beginning of the week
        calendar.add(Calendar.DAY_OF_MONTH, -monthBeginningCell)

        // update title
        val text = getMonthName(currentDate[Calendar.MONTH]) + " " + currentDate[Calendar.YEAR]
        tvCurrentMonth.text = text

        // fill cells
        val adapter = gridAdapter ?: return
        adapter.clear()
        for (dayIndex in 0..<MAX_DAY_COUNT) {
            adapter.insert(calendar.time, dayIndex)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        adapter.setSelectedDate(selectedYear, selectedMonth)
        adapter.notifyDataSetChanged()
    }

    private fun getMonthName(month: Int): String {
        return if (month in monthNames.indices) monthNames[month] else "UNKNOWN MONTH $month"
    }

    fun incMonth(i: Int) {
        currentDate.add(Calendar.MONTH, i)
    }

    companion object {
        private const val MAX_DAY_COUNT = 42
    }
}
