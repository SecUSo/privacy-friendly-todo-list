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
    private lateinit var listNames: MutableList<String>
    private lateinit var listSelector: TextView
    private var selectedListName: String = TITLE_PREF_SHOW_ALL_TASKS
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

        setContentView(R.layout.todo_list_widget_configure)

        // Initialize textview that displays selected list
        model!!.getAllToDoListNames { todoListNames ->
            listNames = todoListNames
            listSelector = findViewById(R.id.tv_widget_cfg_list_choose)
            listSelector.setOnClickListener { view ->
                registerForContextMenu(listSelector)
                openContextMenu(listSelector)
            }
            listSelector.setOnCreateContextMenuListener(this)
        }

        findViewById<View>(R.id.bt_widget_cfg_ok).setOnClickListener { view: View? ->
            saveTitlePref(this, appWidgetId, selectedListName)

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
            for (index in 0..<listNames.size) {
                menu.add(Menu.NONE, index, Menu.NONE, listNames[index])
            }
        }
    }

    override fun onMenuItemSelected(featureId: Int, item: MenuItem): Boolean {
        val index = item.itemId
        if (index >= 0 && index < listNames.size) {
            selectedListName = listNames[index]
            listSelector.text = selectedListName
        } else {
            selectedListName = TITLE_PREF_SHOW_ALL_TASKS
            listSelector.text = getString(R.string.all_tasks)
        }

        return super.onMenuItemSelected(featureId, item)
    }

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
        private const val PREFS_NAME =
            "org.secuso.privacyfriendlytodolist.view.widget.TodoListWidget"
        private const val PREF_PREFIX_KEY = "appwidget_"
        const val TITLE_PREF_SHOW_ALL_TASKS = ""

        // Write the prefix to the SharedPreferences object for this widget
        fun saveTitlePref(context: Context, appWidgetId: Int, text: String) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.putString(PREF_PREFIX_KEY + appWidgetId, text)
            prefs.apply()
        }

        // Read the prefix from the SharedPreferences object for this widget.
        // If there is no preference saved, get the default from a resource
        fun loadTitlePref(context: Context, appWidgetId: Int): String? {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            return prefs.getString(PREF_PREFIX_KEY + appWidgetId, null)
        }

        fun deleteTitlePref(context: Context, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.remove(PREF_PREFIX_KEY + appWidgetId)
            prefs.apply()
        }
    }
}
