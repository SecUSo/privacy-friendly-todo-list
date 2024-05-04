package org.secuso.privacyfriendlytodolist.view.calendar

import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.ModelServices
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.util.Helper
import org.secuso.privacyfriendlytodolist.view.MainActivity
import org.secuso.privacyfriendlytodolist.viewmodel.LifecycleViewModel
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Created by Sebastian Lutz on 31.01.2018.
 *
 * This Activity creates a calendar using CalendarGripAdapter to show deadlines of a task.
 */
@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class CalendarActivity : AppCompatActivity() {
    private lateinit var model: ModelServices
    private lateinit var calendarGridAdapter: CalendarGridAdapter
    private val tasksPerDay = HashMap<String, ArrayList<TodoTask>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ViewModelProvider(this)[LifecycleViewModel::class.java]
        model = viewModel.model
        setContentView(R.layout.fragment_calendar)
        val calendarView = findViewById<CalendarView>(R.id.calendar_view)
        val toolbar = calendarView.findViewById<Toolbar>(R.id.toolbar_calendar)
        toolbar.setTitle(R.string.calendar)
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
        setSupportActionBar(toolbar)
        val supportActionBarCopy = supportActionBar
        if (supportActionBarCopy != null) {
            supportActionBarCopy.setHomeAsUpIndicator(R.drawable.arrow)
            supportActionBarCopy.setDisplayHomeAsUpEnabled(true)
            supportActionBarCopy.setDisplayShowHomeEnabled(true)
        }
        calendarGridAdapter = CalendarGridAdapter(this, R.layout.calendar_day)
        calendarView.setGridAdapter(calendarGridAdapter)
        updateDeadlines()
        calendarView.setNextMonthOnClickListener {
            calendarView.incMonth(1)
            calendarView.refresh()
        }
        calendarView.setPrevMontOnClickListener {
            calendarView.incMonth(-1)
            calendarView.refresh()
        }
        calendarView.setDayOnClickListener { parent, view, position, id ->
            updateDeadlines()
            val selectedDate = calendarGridAdapter.getItem(position)
            val key = absSecondsToDate(selectedDate!!.time / 1000)
            val tasksOfToday = tasksPerDay[key]
            if (tasksOfToday != null) {
                showDeadlineTasks(tasksOfToday)
            } else {
                Toast.makeText(applicationContext, getString(R.string.no_deadline_today), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateDeadlines() {
        model.getAllToDoTasks { todoTasks ->
            tasksPerDay.clear()
            for (todoTask in todoTasks) {
                val deadline = todoTask.getDeadline()
                val key = absSecondsToDate(deadline)
                var tasksOfDay = tasksPerDay[key]
                if (null == tasksOfDay) {
                    tasksOfDay = ArrayList()
                    tasksPerDay[key] = tasksOfDay
                }
                tasksOfDay.add(todoTask)
            }
            calendarGridAdapter.setTodoTasks(tasksPerDay)
            calendarGridAdapter.notifyDataSetChanged()
        }
    }

    private fun absSecondsToDate(seconds: Long): String {
        val cal = Calendar.getInstance()
        cal.setTimeInMillis(TimeUnit.SECONDS.toMillis(seconds))
        return DateFormat.format(Helper.DATE_FORMAT, cal).toString()
    }

    private fun showDeadlineTasks(tasks: ArrayList<TodoTask>) {
        val intent = Intent(this, CalendarPopup::class.java)
        val b = Bundle()
        b.putParcelableArrayList(PARCELABLE_KEY_FOR_DEADLINES, tasks)
        intent.putExtras(b)
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        super.onBackPressed()
    }

    companion object {
        const val PARCELABLE_KEY_FOR_DEADLINES = "PARCELABLE_KEY_FOR_DEADLINES"
    }
}
