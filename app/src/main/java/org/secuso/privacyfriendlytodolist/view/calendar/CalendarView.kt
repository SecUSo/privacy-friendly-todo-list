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
    private lateinit var currentDate: Calendar
    private lateinit var buttonPrevMonth: ImageView
    private lateinit var buttonNextMonth: ImageView
    private lateinit var tvCurrentMonth: TextView
    private lateinit var calendarGrid: GridView
    private var gridAdapter: CalendarGridAdapter? = null

    constructor(context: Context) :
            super(context) {
        initGui(context)
    }

    constructor(context: Context, attrs: AttributeSet?) :
            super(context, attrs) {
        initGui(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        initGui(context)
    }

    private fun initGui(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.calendar, this)
        currentDate = Calendar.getInstance()
        buttonPrevMonth = findViewById(R.id.iv_prev_month)
        buttonNextMonth = findViewById(R.id.iv_next_month)
        tvCurrentMonth = findViewById(R.id.tv_current_month)
        calendarGrid = findViewById(R.id.gv_calendargrid)
    }

    fun setGridAdapter(adapter: CalendarGridAdapter?) {
        gridAdapter = adapter
        calendarGrid.setAdapter(gridAdapter)
        refresh()
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
        val selectedMonth = calendar[Calendar.MONTH]

        // determine cell for the current month's beginning
        calendar[Calendar.DAY_OF_MONTH] = 1
        val monthBeginningCell = calendar[Calendar.DAY_OF_WEEK] - 1

        // move calendar backwards to the beginning of the week
        calendar.add(Calendar.DAY_OF_MONTH, -monthBeginningCell)

        // fill cells
        gridAdapter!!.clear()
        var dayCounter = 0
        while (dayCounter < MAX_DAY_COUNT) {
            gridAdapter!!.insert(calendar.time, dayCounter)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            dayCounter++
        }
        gridAdapter!!.setMonth(selectedMonth)
        gridAdapter!!.notifyDataSetChanged()

        // update title
        val text = getMonth(currentDate[Calendar.MONTH]) + " " + currentDate[Calendar.YEAR]
        tvCurrentMonth.text = text
    }

    private fun getMonth(month: Int): String {
        return when (month) {
            0 -> resources.getString(R.string.january)
            1 -> resources.getString(R.string.february)
            2 -> resources.getString(R.string.march)
            3 -> resources.getString(R.string.april)
            4 -> resources.getString(R.string.may)
            5 -> resources.getString(R.string.june)
            6 -> resources.getString(R.string.july)
            7 -> resources.getString(R.string.august)
            8 -> resources.getString(R.string.september)
            9 -> resources.getString(R.string.october)
            10 -> resources.getString(R.string.november)
            11 -> resources.getString(R.string.december)
            else -> "UNKNOWN MONTH"
        }
    }

    fun incMonth(i: Int) {
        currentDate.add(Calendar.MONTH, i)
    }

    companion object {
        private const val MAX_DAY_COUNT = 42
    }
}
