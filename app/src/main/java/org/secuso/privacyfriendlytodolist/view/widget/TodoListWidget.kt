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
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.Model
import org.secuso.privacyfriendlytodolist.model.ModelObserver
import org.secuso.privacyfriendlytodolist.util.LogTag
import org.secuso.privacyfriendlytodolist.view.MainActivity

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [TodoListWidgetConfigureActivity]
 *
 * @author Sebastian Lutz
 * @version 1.0
 */
class TodoListWidget : AppWidgetProvider(), ModelObserver {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received action '${intent.action}'.")
        if (ACTION_REGISTER_AS_MODEL_OBSERVER == intent.action) {
            Model.registerModelObserver(this)
        } else {
            // Call base implementation. Depending on action, it calls onUpdate, onDelete, ...
            super.onReceive(context, intent)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Model.registerModelObserver(this)
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
        Model.unregisterModelObserver(this)
    }

    override fun onTodoDataChanged(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val thisComponentName = ComponentName(context.packageName, TodoListWidget::class.java.getName())
        val appWidgetIds = appWidgetManager.getAppWidgetIds(thisComponentName)
        for (appWidgetId in appWidgetIds) {
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listview_widget)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            update(context, appWidgetManager, appWidgetId)
        }
    }

    private fun update(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int,
                       title: String? = null) {
        val view = RemoteViews(context.packageName, R.layout.todo_list_widget)
        val uniqueRequestCode = appWidgetId

        // Intent to call the Service adding the tasks to the ListView
        var intent = Intent(context, TodoListWidgetViewsService::class.java)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        // This line of code causes that for every widget an own WidgetViewsFactory gets created:
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)))
        view.setRemoteAdapter(R.id.listview_widget, intent)

        // Intent-template to open the App by clicking on an elements of the LinearLayout.
        // This template gets filled in TodoListWidgetViewsFactory#createItem() via setOnClickFillInIntent()
        intent = Intent(context, MainActivity::class.java)
        var pendingIntent = PendingIntent.getActivity(context, uniqueRequestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        view.setPendingIntentTemplate(R.id.listview_widget, pendingIntent)

        // Intent to open the App by clicking on the widget title.
        val pref = TodoListWidgetConfigureActivity.loadWidgetPreferences(context, appWidgetId)
        intent = Intent(context, MainActivity::class.java)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        intent.putExtra(EXTRA_WIDGET_LIST_ID, pref.todoListId.toString())
        pendingIntent = PendingIntent.getActivity(context, uniqueRequestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        view.setOnClickPendingIntent(R.id.widget_title, pendingIntent)

        // Intent to trigger the update of the widget by clicking on the update icon.
        intent = createWidgetUpdateIntent(context, appWidgetId)
        pendingIntent = PendingIntent.getBroadcast(context, uniqueRequestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        view.setOnClickPendingIntent(R.id.bt_widget_update, pendingIntent)

        view.setEmptyView(R.id.listview_widget, R.id.tv_empty_widget)
        if (null != title) {
            view.setTextViewText(R.id.widget_title, title)
        }

        appWidgetManager.updateAppWidget(appWidgetId, view)
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listview_widget)

        Log.d(TAG, "Widget $appWidgetId: Updated. List ID: ${pref.todoListId}, title: '$title'.")
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager,
        appWidgetId: Int, newOptions: Bundle) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)

        // Check if title was changed.
        val title = newOptions.getString(OPTION_WIDGET_TITLE, null)
        if (null != title) {
            Log.d(TAG, "Widget $appWidgetId: New title: '$title'.")
            update(context, appWidgetManager, appWidgetId, title)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        Log.d(TAG, "Deleting widget(s) ${appWidgetIds.joinToString()}.")
        super.onDeleted(context, appWidgetIds)

        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            TodoListWidgetConfigureActivity.deleteWidgetPreferences(context, appWidgetId)
        }
    }

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
        private const val ACTION_REGISTER_AS_MODEL_OBSERVER = "ACTION_REGISTER_AS_MODEL_OBSERVER"
        const val EXTRA_WIDGET_LIST_ID = "EXTRA_WIDGET_LIST_ID"
        const val OPTION_WIDGET_TITLE = "OPTION_WIDGET_TITLE"

        fun registerAsModelObserver(context: Context) {
            val intent = Intent(context, TodoListWidget::class.java)
            intent.setAction(ACTION_REGISTER_AS_MODEL_OBSERVER)
            context.sendBroadcast(intent)
        }

        fun triggerWidgetUpdate(context: Context, appWidgetId: Int) {
            val intent = createWidgetUpdateIntent(context, appWidgetId)
            context.sendBroadcast(intent)
        }

        private fun createWidgetUpdateIntent(context: Context, appWidgetId: Int): Intent {
            val intent = Intent(context, TodoListWidget::class.java)
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
            val appWidgetIds = IntArray(1)
            appWidgetIds[0] = appWidgetId
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            return intent
        }
    }
}
