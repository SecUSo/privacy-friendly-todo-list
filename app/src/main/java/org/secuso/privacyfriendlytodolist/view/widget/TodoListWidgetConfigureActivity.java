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

package org.secuso.privacyfriendlytodolist.view.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.model.ModelServices;
import org.secuso.privacyfriendlytodolist.viewmodel.CustomViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * The configuration screen for the {@link TodoListWidget TodoListWidget} AppWidget.
 * @author Sebastian Lutz
 * @version 1.0
 */
public class TodoListWidgetConfigureActivity extends Activity {

    private static final String PREFS_NAME = "org.secuso.privacyfriendlytodolist.view.widget.TodoListWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";

   // Write the prefix to the SharedPreferences object for this widget
    static void saveTitlePref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
        prefs.apply();
    }



    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadTitlePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
        if (titleValue != null) {
            return titleValue;
        } else {
            return context.getString(R.string.appwidget_text);
        }
    }



    static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }


    private CustomViewModel viewModel;
    private ModelServices model;
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Spinner spinner;
    private ArrayAdapter<String> lists;

    public TodoListWidgetConfigureActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        viewModel = new CustomViewModel(this);
        model = viewModel.getModel();

        setContentView(R.layout.todo_list_widget_configure);
        findViewById(R.id.add_button).setOnClickListener(view -> {
            // When the button is clicked, store the string locally
            if (null != lists && !lists.isEmpty()) {
                String listTitle = spinner.getSelectedItem().toString();
                saveTitlePref(this, mAppWidgetId, listTitle);

                // It is the responsibility of the configuration activity to update the app widget
                //AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                WidgetViewsFactory.getListName(this, mAppWidgetId);
                TodoListWidget.getListName(this, mAppWidgetId);

                // Make sure we pass back the original appWidgetId
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            } else {
                Toast.makeText(this, "No list available", Toast.LENGTH_SHORT).show();
            }
        });

        updateLists();

        //initialize spinner dropdown
        spinner = (Spinner) findViewById(R.id.spinner1);
        lists.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(lists);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        model = null;
        viewModel.destroy();
        viewModel = null;
    }

    //updates the lists array and prepare adapter for spinner
    private void updateLists(){
        model.getAllToDoLists(todoLists -> {
            List<String> help = new ArrayList<>();
            for (TodoList todoList : todoLists) {
                help.add(todoList.getName());
            }
            lists = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, help);
            lists.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        });
    }
}

