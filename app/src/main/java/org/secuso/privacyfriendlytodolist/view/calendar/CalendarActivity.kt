/*
Privacy Friendly To-Do List
Copyright (C) 2018-2024  Sebastian Lutz

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

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.ModelServices
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.view.MainActivity
import org.secuso.privacyfriendlytodolist.viewmodel.LifecycleViewModel

/**
 * Created by Sebastian Lutz on 31.01.2018.
 *
 * This Activity creates a calendar using CalendarGripAdapter to show deadlines of a task.
 */
@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class CalendarActivity : AppCompatActivity() {
    private lateinit var model: ModelServices
    private lateinit var calendarGridAdapter: CalendarGridAdapter

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
        calendarView.setNextMonthOnClickListener {
            calendarView.incMonth(1)
            calendarView.refresh()
        }
        calendarView.setPrevMontOnClickListener {
            calendarView.incMonth(-1)
            calendarView.refresh()
        }
        calendarView.setDayOnClickListener { parent, view, position, id ->
            val tasksOfToday = calendarGridAdapter.getTasksOfDay(position)
            if (tasksOfToday != null) {
                showDeadlineTasks(tasksOfToday)
            } else {
                Toast.makeText(applicationContext, getString(R.string.no_deadline_today),
                    Toast.LENGTH_SHORT).show()
            }
        }

        onBackPressedDispatcher.addCallback(this) {
            val intent = Intent(this@CalendarActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }

        loadTodoTasks()
    }

    private fun loadTodoTasks() {
        model.getAllToDoTasks { todoTasks ->
            calendarGridAdapter.setTodoTasks(todoTasks)
            calendarGridAdapter.notifyDataSetChanged()
        }
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

    companion object {
        const val PARCELABLE_KEY_FOR_DEADLINES = "PARCELABLE_KEY_FOR_DEADLINES"
    }
}
