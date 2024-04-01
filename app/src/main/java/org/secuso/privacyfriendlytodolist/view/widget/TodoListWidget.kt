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

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.view.MainActivity

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [TodoListWidgetConfigureActivity]
 *
 * @author Sebastian Lutz
 * @version 1.0
 */
class TodoListWidget : AppWidgetProvider() {
    private var views: RemoteViews? = null

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            TodoListWidgetConfigureActivity.deleteTitlePref(context, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context, intent: Intent) {
        updateHelper(context)
    }

    // returns a PendingIntent to update the widget's contents.
    private fun refreshWidget(context: Context, appWidgetId: Int): PendingIntent {
        val update = Intent(context, TodoListWidget::class.java)
        update.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
        update.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        return PendingIntent.getBroadcast(context, 0, update,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun updateHelper(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val thisAppWidget = ComponentName(context.packageName, TodoListWidget::class.java.getName())
        val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget)
        onUpdate(context, appWidgetManager, appWidgetIds)
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        if (views == null) {
            views = RemoteViews(context.packageName, R.layout.todo_list_widget)
        }

        //Intent to call the Service adding the tasks to the ListView
        val intent = Intent(context, ListViewWidgetService::class.java)

        //Intents to open the App by clicking on an elements of the LinearLayout
        val templateIntent = Intent(context, MainActivity::class.java)
        templateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        val templatePendingIntent = PendingIntent.getActivity(
            context, 0, templateIntent, PendingIntent.FLAG_IMMUTABLE)
        views!!.setTextViewText(R.id.widget_title, context.getString(R.string.app_name))
        views!!.setRemoteAdapter(R.id.listview_widget, intent)
        views!!.setOnClickPendingIntent(R.id.click_widget, refreshWidget(context, appWidgetId))
        views!!.setPendingIntentTemplate(R.id.listview_widget, templatePendingIntent)
        views!!.setEmptyView(R.id.listview_widget, R.id.tv_empty_widget)
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listview_widget)
        appWidgetManager.updateAppWidget(appWidgetId, views)
        Log.d("UPDATE", "Widget was updated here!")
    }
}
