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
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.view.MainActivity;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link TodoListWidgetConfigureActivity TodoListWidgetConfigureActivity}
 *
 * @author Sebastian Lutz
 * @version 1.0
 *
 */

public class TodoListWidget extends AppWidgetProvider {

    public static final String EXTRA_ITEM = "com.example.edockh.EXTRA_ITEM";
    public static final String ACTION_VIEW_DETAILS = "com.company.android.ACTION_VIEW_DETAILS";



    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            int widgetId = appWidgetId;

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.todo_list_widget);

            Intent intent = new Intent (context, ListViewWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            //views.setTextViewText(R.id.widget_title, "To-Do List:");
            views.setRemoteAdapter(R.id.listview_widget, intent);
            views.setEmptyView(R.id.listview_widget, R.id.tv_empty_widget);

            Intent detailIntent = new Intent(context, MainActivity.class);
            PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, detailIntent, 0);
            views.setOnClickPendingIntent(R.layout.todo_list_widget, pIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }


        super.onUpdate(context, appWidgetManager, appWidgetIds);

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
        if(intent.getAction().equals(ACTION_VIEW_DETAILS)) {
            TodoTask todo = (TodoTask) intent.getSerializableExtra(EXTRA_ITEM);
            if(todo != null) {

                // Handle the click here.
                // Maybe start a details activity?
                // Maybe consider using an Activity PendingIntent instead of a Broadcast?
            }
        }

        super.onReceive(context, intent);
    }
}

