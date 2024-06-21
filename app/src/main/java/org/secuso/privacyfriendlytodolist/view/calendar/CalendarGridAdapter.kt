package org.secuso.privacyfriendlytodolist.view.calendar

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.util.Helper
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
    private var currentMonth = -1
    private var currentYear = -1
    private var todoTasks: List<TodoTask>? = null
    private var tasksPerDay = HashMap<Long, ArrayList<TodoTask>>(0)
    private var tasksPerDayNeedUpdate = false

    init {
        inflater = LayoutInflater.from(context)
    }

    fun setTodoTasks(todoTasks: List<TodoTask>) {
        this.todoTasks = todoTasks
        tasksPerDayNeedUpdate = true
    }

    fun setSelectedDate(year: Int, month: Int) {
        tasksPerDayNeedUpdate =
            tasksPerDayNeedUpdate || currentYear != year || currentMonth != month
        currentYear = year
        currentMonth = month
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        updateTasksPerDay()
        var view = convertView
        val dayTextView: TextView
        val dateAtPos = getItem(position)
        val todayDate = Date()
        val todayCal = Calendar.getInstance()
        val posCal = Calendar.getInstance()
        todayCal.setTime(todayDate)
        posCal.setTime(dateAtPos!!)
        if (view?.tag is CalendarDayViewHolder) {
            val dayViewHolder = view.tag as CalendarDayViewHolder
            dayTextView = dayViewHolder.dayText
        } else {
            view = inflater.inflate(R.layout.calendar_day, parent, false)
            val dayViewHolder = CalendarDayViewHolder(view.findViewById(R.id.tv_calendar_day_content))
            view.tag = dayViewHolder
            dayTextView = dayViewHolder.dayText
            oldColors = dayTextView.textColors
        }

        val spanString = SpannableString(dateToStr(posCal))
        if (posCal[Calendar.MONTH] != currentMonth) {
            // grey day out if it is outside the current month
            dayTextView.setTextColor(ContextCompat.getColor(context, R.color.middlegrey))
        } else if (sameDay(posCal, todayCal)) {
            // highlight today
            dayTextView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
            spanString.setSpan(UnderlineSpan(), 0, spanString.length, 0)
            spanString.setSpan(StyleSpan(Typeface.BOLD), 0, spanString.length, 0)
        } else {
            // otherwise apply default
            dayTextView.setTextColor(oldColors)
        }

        // add color bar if a task has its deadline on this day
        val day = TimeUnit.MILLISECONDS.toDays(dateAtPos.time)
        val tasksToday = tasksPerDay[day]
        if (tasksToday != null) {
            var border: Drawable? = null
            for (t in tasksToday) {
                if (!t.isDone()) {
                    border = ContextCompat.getDrawable(context, R.drawable.border_blue)
                    break
                }
            }
            dayTextView.background = border ?: ContextCompat.getDrawable(context, R.drawable.border_green)
        } else {
            dayTextView.setBackgroundResource(0)
        }
        dayTextView.text = spanString
        return view!!
    }

    private fun sameDay(c1: Calendar, c2: Calendar): Boolean {
        return c1[Calendar.YEAR] == c2[Calendar.YEAR] && c1[Calendar.DAY_OF_YEAR] == c2[Calendar.DAY_OF_YEAR]
    }

    private fun dateToStr(c: Calendar): String {
        return dateFormat.format(c.time)
    }

    /**
     * All non-recurring tasks can be added easily to the look-up table.
     * For the recurring tasks the look-up table will be filled with all recurring dates in a
     * period of 3 months: one before the selected, the selected and one after the selected month.
     */
    private fun updateTasksPerDay()  {
        val allTodoTasks = todoTasks
        if (!tasksPerDayNeedUpdate || allTodoTasks == null || currentMonth == -1) {
            return
        }

        val startCal = Calendar.getInstance()
        startCal.timeInMillis = 0
        startCal[Calendar.YEAR] = currentYear
        startCal[Calendar.MONTH] = currentMonth
        startCal.add(Calendar.MONTH, -1)
        val endCal = Calendar.getInstance()
        endCal.timeInMillis = 0
        endCal[Calendar.YEAR] = currentYear
        endCal[Calendar.MONTH] = currentMonth
        endCal.add(Calendar.MONTH, 2)
        endCal.add(Calendar.DAY_OF_MONTH, -1)

        tasksPerDay.clear()
        for (todoTask in allTodoTasks) {
            var deadline = todoTask.getDeadline()
            if (deadline == -1L) {
                continue
            }
            if (todoTask.isRecurring()) {
                val recurringDateCal = Calendar.getInstance()
                recurringDateCal.setTimeInMillis(TimeUnit.SECONDS.toMillis(todoTask.getDeadline()))
                Helper.getNextRecurringDate(recurringDateCal, todoTask.getRecurrencePattern(), startCal)
                while (recurringDateCal < endCal) {
                    deadline = TimeUnit.MILLISECONDS.toSeconds(recurringDateCal.timeInMillis)
                    addTaskOfDay(todoTask, deadline)
                    Helper.addInterval(recurringDateCal, todoTask.getRecurrencePattern())
                }
            } else {
                addTaskOfDay(todoTask, deadline)
            }
        }
        tasksPerDayNeedUpdate = false
    }

    private fun addTaskOfDay(todoTask: TodoTask, deadline: Long) {
        val key = TimeUnit.SECONDS.toDays(deadline)
        var tasksOfDay = tasksPerDay[key]
        if (null == tasksOfDay) {
            tasksOfDay = ArrayList()
            tasksPerDay[key] = tasksOfDay
        }
        tasksOfDay.add(todoTask)
    }

    fun getTasksOfDay(position: Int): ArrayList<TodoTask>? {
        val selectedDate = getItem(position) ?: return null
        val key = TimeUnit.MILLISECONDS.toDays(selectedDate.time)
        return tasksPerDay[key]
    }

    private inner class CalendarDayViewHolder(val dayText: TextView)
}
