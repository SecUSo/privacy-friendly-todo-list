package org.secuso.privacyfriendlytodolist.view.calendar

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.TodoTask
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Created by Sebastian Lutz on 12.03.2018.
 *
 * This calender marks a day if there is a deadline of a task which is not yet finished.
 */
class CalendarGridAdapter(context: Context, resource: Int) :
    ArrayAdapter<Date?>(context, resource, ArrayList()) {
    private val inflater: LayoutInflater
    private val dateFormat = SimpleDateFormat("d", Locale.getDefault())
    private var oldColors: ColorStateList? = null
    private var currentMonth = 0
    private var tasksPerDay = HashMap<Long, ArrayList<TodoTask>>(0)

    init {
        inflater = LayoutInflater.from(context)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val dayViewHolder: CalendarDayViewHolder
        val dateAtPos = getItem(position)
        val todayDate = Date()
        val todayCal = Calendar.getInstance()
        val posCal = Calendar.getInstance()
        todayCal.setTime(todayDate)
        posCal.setTime(dateAtPos!!)
        if (view == null) {
            dayViewHolder = CalendarDayViewHolder()
            view = inflater.inflate(R.layout.calendar_day, parent, false)
            dayViewHolder.dayText = view.findViewById(R.id.tv_calendar_day_content)
            oldColors = dayViewHolder.dayText!!.textColors
            view.tag = dayViewHolder
        } else {
            dayViewHolder = view.tag as CalendarDayViewHolder
        }

        // grey day out if it is outside the current month
        if (posCal[Calendar.MONTH] != currentMonth) {
            dayViewHolder.dayText!!.setTextColor(ContextCompat.getColor(context, R.color.middlegrey))
        } else if (sameDay(posCal, todayCal)) {
            dayViewHolder.dayText!!.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
        } else {
            dayViewHolder.dayText!!.setTextColor(oldColors)
        }

        // add color bar if a task has its deadline on this day
        val day = TimeUnit.MILLISECONDS.toDays(dateAtPos.time)
        val tasksToday = tasksPerDay[day]
        if (tasksToday != null) {
            var border = ContextCompat.getDrawable(context, R.drawable.border_green)
            for (t in tasksToday) {
                if (!t.isDone()) {
                    border = ContextCompat.getDrawable(context, R.drawable.border_blue)
                    break
                }
            }
            dayViewHolder.dayText!!.background = border
        } else {
            dayViewHolder.dayText!!.setBackgroundResource(0)
        }
        dayViewHolder.dayText!!.text = dateToStr(posCal)
        return view!!
    }

    private fun sameDay(c1: Calendar, c2: Calendar): Boolean {
        return c1[Calendar.YEAR] == c2[Calendar.YEAR] && c1[Calendar.DAY_OF_YEAR] == c2[Calendar.DAY_OF_YEAR]
    }

    private fun dateToStr(c: Calendar): String {
        return dateFormat.format(c.time)
    }

    fun setMonth(month: Int) {
        currentMonth = month
    }

    fun setTodoTasks(tasksPerDay: HashMap<Long, ArrayList<TodoTask>>) {
        this.tasksPerDay = tasksPerDay
    }

    private inner class CalendarDayViewHolder {
        var dayText: TextView? = null
    }
}
