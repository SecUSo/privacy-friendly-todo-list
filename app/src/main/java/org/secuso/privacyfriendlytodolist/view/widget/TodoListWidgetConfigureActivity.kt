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
import android.widget.TextView
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.ModelServices
import org.secuso.privacyfriendlytodolist.model.TodoList
import org.secuso.privacyfriendlytodolist.util.LogTag
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
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private val pref = TodoListWidgetPreferences()

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

        setContentView(R.layout.todo_list_widget_configure)

        // Initialize textview that displays selected list
        model!!.getAllToDoLists { todoLists ->
            this.todoLists = todoLists
            listSelector = findViewById(R.id.tv_widget_cfg_list_choose)
            listSelector.setOnClickListener { view ->
                registerForContextMenu(listSelector)
                openContextMenu(listSelector)
            }
            listSelector.setOnCreateContextMenuListener(this)
        }

        findViewById<View>(R.id.bt_widget_cfg_ok).setOnClickListener { view: View? ->
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
            menu.setHeaderTitle(R.string.select_list)
            menu.add(Menu.NONE, -1, Menu.NONE, R.string.all_tasks)
            for (index in todoLists.indices) {
                menu.add(Menu.NONE, index, Menu.NONE, todoLists[index].getName())
            }
        }
    }

    override fun onMenuItemSelected(featureId: Int, item: MenuItem): Boolean {
        val index = item.itemId
        if (index >= 0 && index < todoLists.size) {
            val todoList = todoLists[index]
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
        private const val PREF_PREFIX = "_todo_list_widget_"
        private const val PREF_LIST_ID_KEY = PREF_PREFIX + "list_id"
        private const val PREF_NULL_VALUE = "null"

        private fun saveWidgetPreferences(context: Context, appWidgetId: Int, pref: TodoListWidgetPreferences) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            val id = appWidgetId.toString()
            prefs.putString(id + PREF_LIST_ID_KEY, pref.todoListId.toString())
            prefs.apply()
        }

        fun loadWidgetPreferences(context: Context, appWidgetId: Int): TodoListWidgetPreferences {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            val id = appWidgetId.toString()
            var todoListId: Int? = null
            val todoListIdAsString = prefs.getString(id + PREF_LIST_ID_KEY, null)
            if (todoListIdAsString != null && todoListIdAsString != PREF_NULL_VALUE) {
                todoListId = todoListIdAsString.toInt()
            }
            return TodoListWidgetPreferences(todoListId)
        }

        fun deleteWidgetPreferences(context: Context, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            val id = appWidgetId.toString()
            prefs.remove(id + PREF_LIST_ID_KEY)
            prefs.apply()
        }
    }
}
