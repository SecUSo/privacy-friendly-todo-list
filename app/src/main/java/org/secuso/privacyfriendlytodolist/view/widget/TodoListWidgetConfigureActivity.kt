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
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.ModelServices
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
    private lateinit var spinner: Spinner
    private var lists: ArrayAdapter<String>? = null
    private var mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)

        viewModel = CustomViewModel(this)
        model = viewModel!!.model
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)
        setContentView(R.layout.todo_list_widget_configure)
        spinner = findViewById(R.id.spinner1)
        findViewById<View>(R.id.add_button).setOnClickListener { view: View? ->
            // When the button is clicked, store the string locally
            val listsCopy = lists
            if (null != listsCopy && !listsCopy.isEmpty) {
                val listTitle = spinner.getSelectedItem().toString()
                saveTitlePref(this, mAppWidgetId, listTitle)

                // It is the responsibility of the configuration activity to update the app widget
                //AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                WidgetViewsFactory.setAppWidgetId(mAppWidgetId)

                // Make sure we pass back the original appWidgetId
                val resultValue = Intent()
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
                setResult(RESULT_OK, resultValue)
                finish()
            } else {
                Toast.makeText(this, "No list available", Toast.LENGTH_SHORT).show()
            }
        }
        //updates the lists array and prepare adapter for spinner
        model!!.getAllToDoListNames { todoListNames ->
            lists = ArrayAdapter(this, android.R.layout.simple_spinner_item, todoListNames)
            //initialize spinner dropdown
            lists!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.setAdapter(lists)
        }

        // Find the widget id from the intent.
        val extras = intent.extras
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        model = null
        viewModel!!.destroy()
        viewModel = null
    }

    companion object {
        private const val PREFS_NAME =
            "org.secuso.privacyfriendlytodolist.view.widget.TodoListWidget"
        private const val PREF_PREFIX_KEY = "appwidget_"

        // Write the prefix to the SharedPreferences object for this widget
        fun saveTitlePref(context: Context, appWidgetId: Int, text: String?) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.putString(PREF_PREFIX_KEY + appWidgetId, text)
            prefs.apply()
        }

        // Read the prefix from the SharedPreferences object for this widget.
        // If there is no preference saved, get the default from a resource
        fun loadTitlePref(context: Context, appWidgetId: Int): String {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            val titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null)
            return titleValue ?: context.getString(R.string.appwidget_text)
        }

        fun deleteTitlePref(context: Context, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.remove(PREF_PREFIX_KEY + appWidgetId)
            prefs.apply()
        }
    }
}
