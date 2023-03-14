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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.view.MainActivity;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link TodoListWidgetConfigureActivity TodoListWidgetConfigureActivity}
 *
 * @author Sebastian Lutz
 * @version 1.0
 */

public class TodoListWidget extends AppWidgetProvider {


    public static String listChosen;
    public RemoteViews views;


    public static void getListName(Context context, int AppWidgetId) {
        listChosen = TodoListWidgetConfigureActivity.loadTitlePref(context, AppWidgetId);
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            int widgetId = appWidgetId;

            updateAppWidget(context, appWidgetManager, widgetId);
        }
    }


    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            TodoListWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
        }
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }


    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        updateHelper(context);
    }


    // returns a PendingIntent to update the widget's contents.
    public PendingIntent refreshWidget(Context context, int appWidgetId) {
        Intent update = new Intent(context, TodoListWidget.class);
        update.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        update.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingUpdate = PendingIntent.getBroadcast(context, 0, update, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        return pendingUpdate;
    }


    public void updateHelper(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), TodoListWidget.class.getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

        onUpdate(context, appWidgetManager, appWidgetIds);
    }


    public void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        if (views == null)
            views = new RemoteViews(context.getPackageName(), R.layout.todo_list_widget);

        //Intent to call the Service adding the tasks to the ListView
        Intent intent = new Intent(context, ListViewWidgetService.class);

        //Intents to open the App by clicking on an elements of the LinearLayout
        Intent templateIntent = new Intent(context, MainActivity.class);
        templateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent templatePendingIntent = PendingIntent.getActivity(
                context, 0, templateIntent, PendingIntent.FLAG_IMMUTABLE);

        views.setTextViewText(R.id.widget_title, context.getString(R.string.app_name));
        views.setRemoteAdapter(R.id.listview_widget, intent);
        views.setOnClickPendingIntent(R.id.click_widget, refreshWidget(context, appWidgetId));
        views.setPendingIntentTemplate(R.id.listview_widget, templatePendingIntent);
        views.setEmptyView(R.id.listview_widget, R.id.tv_empty_widget);

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listview_widget);
        appWidgetManager.updateAppWidget(appWidgetId, views);

        Log.d("UPDATE", "Widget was updated here!");
    }


}

