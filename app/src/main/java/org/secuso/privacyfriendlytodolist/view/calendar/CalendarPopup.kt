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

import android.os.Bundle
import android.view.MenuItem
import android.widget.ExpandableListView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.BundleCompat
import androidx.lifecycle.ViewModelProvider
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.view.ExpandableTodoTaskAdapter
import org.secuso.privacyfriendlytodolist.viewmodel.LifecycleViewModel

/**
 * Created by sebbi on 07.03.2018.
 *
 * This class helps to show the tasks that are on a specific deadline
 */
class CalendarPopup : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ViewModelProvider(this)[LifecycleViewModel::class.java]
        val model = viewModel.model
        setContentView(R.layout.calendar_popup)
        val toolbar: Toolbar = findViewById(R.id.toolbar_deadlineTasks)
        setSupportActionBar(toolbar)
        val supportActionBarCopy = supportActionBar
        if (supportActionBarCopy != null) {
            supportActionBarCopy.setHomeAsUpIndicator(R.drawable.arrow)
            supportActionBarCopy.setDisplayHomeAsUpEnabled(true)
            supportActionBarCopy.setDisplayShowHomeEnabled(true)
        }
        var tasks: java.util.ArrayList<TodoTask>? = null
        val bundle = intent.extras
        if (null != bundle) {
            tasks = BundleCompat.getParcelableArrayList(bundle,
                CalendarActivity.PARCELABLE_KEY_FOR_DEADLINES, TodoTask::class.java)
        }
        val adapter = ExpandableTodoTaskAdapter(this, model,
            tasks ?: ArrayList(0), true)
        val lv = findViewById<ExpandableListView>(R.id.deadline_tasks)
        lv.setAdapter(adapter)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
