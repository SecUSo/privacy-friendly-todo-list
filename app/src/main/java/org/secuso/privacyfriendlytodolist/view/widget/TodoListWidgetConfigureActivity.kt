/*
Privacy Friendly To-Do List
Copyright (C) 2018-2025  Sebastian Lutz

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
package org.secuso.privacyfriendlytodolist.view.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.TextView
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.ModelServices
import org.secuso.privacyfriendlytodolist.model.TodoList
import org.secuso.privacyfriendlytodolist.util.Helper
import org.secuso.privacyfriendlytodolist.util.LogTag
import org.secuso.privacyfriendlytodolist.view.TaskFilter
import org.secuso.privacyfriendlytodolist.viewmodel.CustomViewModel

/**
 * The configuration screen for the [TodoListWidget] AppWidget.
 * @author Sebastian Lutz
 * @version 1.0
 */
@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class TodoListWidgetConfigureActivity : Activity() {
    private var viewModel: CustomViewModel? = null
    private var model: ModelServices? = null
    private lateinit var todoLists: List<TodoList>
    private lateinit var listSelector: TextView
    private lateinit var pref: TodoListWidgetPreferences
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        // Find the widget id from the intent.
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            // This activity was started with an intent without an app widget ID, finish with an error.
            Log.e(TAG, "Widget configurator started without an app widget ID.")
            finish()
            return
        }
        Log.d(TAG, "Widget configurator started with app widget ID $appWidgetId.")

        viewModel = CustomViewModel(this)
        model = viewModel!!.model

        setContentView(R.layout.widget_configuration)

        listSelector = findViewById(R.id.tv_widget_cfg_list_choose)
        val rbFilterAllTasks = findViewById<RadioButton>(R.id.rb_widget_cfg_all_tasks)
        val rbFilterOpenTasks = findViewById<RadioButton>(R.id.rb_widget_cfg_open_tasks)
        val rbFilterCompletedTasks = findViewById<RadioButton>(R.id.rb_widget_cfg_completed_tasks)
        val cbGroupByPriority = findViewById<CheckBox>(R.id.cb_widget_cfg_group_by_priority)
        val cbSortByDeadline = findViewById<CheckBox>(R.id.cb_widget_cfg_sort_by_deadline)
        val cbSortByNameAsc = findViewById<CheckBox>(R.id.cb_widget_cfg_sort_by_name_asc)
        val cbShowDaysUntilDeadline = findViewById<CheckBox>(R.id.cb_widget_cfg_show_days_until_deadline)

        val loadedPref = loadWidgetPreferences(this, appWidgetId)
        if (null == loadedPref) {
            // No preferences loaded: This is the first configuration of the widget, use the defaults.
            pref = TodoListWidgetPreferences()
        } else {
            // Preferences loaded: This is a re-configuration of the widget, use the loaded preferences.
            pref = loadedPref
            // List selector at first configuration: "Click to choose", at re-configuration: "All tasks"
            listSelector.text = getString(R.string.all_tasks)
        }
        when (pref.taskFilter) {
            TaskFilter.ALL_TASKS -> rbFilterAllTasks.isChecked = true
            TaskFilter.OPEN_TASKS -> rbFilterOpenTasks.isChecked = true
            TaskFilter.COMPLETED_TASKS -> rbFilterCompletedTasks.isChecked = true
        }
        cbGroupByPriority.isChecked = pref.isGroupingByPriority
        cbSortByDeadline.isChecked = pref.isSortingByDeadline
        cbSortByNameAsc.isChecked = pref.isSortingByNameAsc
        cbShowDaysUntilDeadline.isChecked = pref.isShowingDaysUntilDeadline

        // Initialize textview that displays selected list
        model!!.getAllToDoLists { todoLists ->
            this.todoLists = todoLists
            listSelector.setOnClickListener { view ->
                registerForContextMenu(listSelector)
                openContextMenu(listSelector)
            }
            listSelector.setOnCreateContextMenuListener(this)
            if (pref.todoListId != null) {
                val todoList = todoLists.find { current ->
                    current.getId() == pref.todoListId
                }
                if (todoList != null) {
                    listSelector.text = todoList.getName()
                }
            }
        }

        findViewById<View>(R.id.bt_widget_cfg_ok).setOnClickListener { view: View? ->
            // Save preferences
            if (rbFilterOpenTasks.isChecked) {
                pref.taskFilter = TaskFilter.OPEN_TASKS
            } else if (rbFilterCompletedTasks.isChecked) {
                pref.taskFilter = TaskFilter.COMPLETED_TASKS
            } else {
                pref.taskFilter = TaskFilter.ALL_TASKS
            }
            pref.isGroupingByPriority = cbGroupByPriority.isChecked
            pref.isSortingByDeadline = cbSortByDeadline.isChecked
            pref.isSortingByNameAsc = cbSortByNameAsc.isChecked
            pref.isShowingDaysUntilDeadline = cbShowDaysUntilDeadline.isChecked
            saveWidgetPreferences(this, appWidgetId, pref)

            // Trigger update after list name was saved to update the widget title with list name.
            TodoListWidget.triggerWidgetUpdate(this, appWidgetId)

            // Make sure we pass back the original appWidgetId
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
            finish()
        }

        findViewById<View>(R.id.bt_widget_cfg_cancel).setOnClickListener { view: View? ->
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        model = null
        viewModel!!.destroy()
        viewModel = null
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        if (v.id == R.id.tv_widget_cfg_list_choose) {
            val menuHeader = Helper.getMenuHeader(layoutInflater, v, R.string.select_list)
            menu.setHeaderView(menuHeader)
            menu.add(Menu.NONE, -1, Menu.NONE, R.string.all_tasks)
            for (index in todoLists.indices) {
                menu.add(Menu.NONE, index, Menu.NONE, todoLists[index].getName())
            }
        }
    }

    override fun onMenuItemSelected(featureId: Int, item: MenuItem): Boolean {
        val todoList = todoLists.getOrNull(item.itemId)
        if (null != todoList) {
            pref.todoListId = todoList.getId()
            listSelector.text = todoList.getName()
        } else {
            pref.todoListId = null
            listSelector.text = getString(R.string.all_tasks)
        }
        return super.onMenuItemSelected(featureId, item)
    }

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
        private const val PREFS_NAME = "org.secuso.privacyfriendlytodolist.view.widget.TodoListWidget"
        private const val PREF_KEY_LIST_ID = "list_id"
        private const val PREF_KEY_TASK_FILTER = "task_filter"
        private const val PREF_KEY_GROUP_BY_PRIORITY = "group_by_priority"
        private const val PREF_KEY_SORT_BY_DEADLINE = "sort_by_deadline"
        private const val PREF_KEY_SORT_BY_NAME_ASC = "sort_by_name_asc"
        private const val PREF_KEY_SHOW_DAYS_UNTIL_DEADLINE = "show_days_until_deadline"
        private const val PREF_VALUE_NULL = "null"
        private const val PREFIX = "_todo_list_widget_"

        private fun saveWidgetPreferences(context: Context, appWidgetId: Int, pref: TodoListWidgetPreferences) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            val prefix = appWidgetId.toString() + PREFIX
            prefs.putString(prefix + PREF_KEY_LIST_ID, pref.todoListId.toString())
            prefs.putString(prefix + PREF_KEY_TASK_FILTER, pref.taskFilter.toString())
            prefs.putBoolean(prefix + PREF_KEY_GROUP_BY_PRIORITY, pref.isGroupingByPriority)
            prefs.putBoolean(prefix + PREF_KEY_SORT_BY_DEADLINE, pref.isSortingByDeadline)
            prefs.putBoolean(prefix + PREF_KEY_SORT_BY_NAME_ASC, pref.isSortingByNameAsc)
            prefs.putBoolean(prefix + PREF_KEY_SHOW_DAYS_UNTIL_DEADLINE, pref.isShowingDaysUntilDeadline)
            prefs.apply()
            Log.d(TAG, "Preferences saved for app widget $appWidgetId: $pref")
        }

        fun loadWidgetPreferences(context: Context, appWidgetId: Int): TodoListWidgetPreferences? {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            val prefix = appWidgetId.toString() + PREFIX
            val pref = TodoListWidgetPreferences()
            var strValue = prefs.getString(prefix + PREF_KEY_LIST_ID, null)
            // Use list ID value to decide is preferences are available:
            if (strValue == null) {
                Log.d(TAG, "No preferences found for app widget $appWidgetId.")
                return null
            }
            if (strValue != PREF_VALUE_NULL) {
                pref.todoListId = strValue.toInt()
            }
            strValue = prefs.getString(prefix + PREF_KEY_TASK_FILTER, null)
            pref.taskFilter = TaskFilter.fromString(strValue)
            pref.isGroupingByPriority = prefs.getBoolean(prefix + PREF_KEY_GROUP_BY_PRIORITY, false)
            pref.isSortingByDeadline = prefs.getBoolean(prefix + PREF_KEY_SORT_BY_DEADLINE, false)
            pref.isSortingByNameAsc = prefs.getBoolean(prefix + PREF_KEY_SORT_BY_NAME_ASC, false)
            pref.isShowingDaysUntilDeadline = prefs.getBoolean(prefix + PREF_KEY_SHOW_DAYS_UNTIL_DEADLINE, false)
            Log.d(TAG, "Preferences loaded for app widget $appWidgetId: $pref")
            return pref
        }

        fun deleteWidgetPreferences(context: Context, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            val prefix = appWidgetId.toString() + PREFIX
            prefs.remove(prefix + PREF_KEY_LIST_ID)
            prefs.remove(prefix + PREF_KEY_TASK_FILTER)
            prefs.remove(prefix + PREF_KEY_GROUP_BY_PRIORITY)
            prefs.remove(prefix + PREF_KEY_SORT_BY_DEADLINE)
            prefs.remove(prefix + PREF_KEY_SORT_BY_NAME_ASC)
            prefs.remove(prefix + PREF_KEY_SHOW_DAYS_UNTIL_DEADLINE)
            prefs.apply()
            Log.d(TAG, "Preferences deleted for app widget $appWidgetId.")
        }
    }
}
