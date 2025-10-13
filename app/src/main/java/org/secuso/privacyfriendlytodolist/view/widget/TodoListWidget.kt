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

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import androidx.core.net.toUri
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
    private val factory = TodoListWidgetViewsFactory()

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received action '${intent.action}'.")
        if (ACTION_REGISTER_AS_MODEL_OBSERVER == intent.action) {
            Model.registerModelObserver(this)
            initialUpdate(context)
        } else {
            // Call base implementation. Depending on action, it calls onUpdate, onDelete, ...
            super.onReceive(context, intent)
        }
    }

    override fun onEnabled(context: Context) {
        Log.d(TAG, "onEnabled().")
        Model.registerModelObserver(this)
        TodoListWidgetPeriodicUpdater.startPeriodicUpdates(context)
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context?) {
        Log.d(TAG, "onDisabled().")
        super.onDisabled(context)
        Model.unregisterModelObserver(this)
        factory.destroy()
    }

    override fun onTodoDataChanged(context: Context, changedLists: Int, changedTasks: Int, changedSubtasks: Int) {
        triggerWidgetUpdate(context, "ToDo data changed")
    }

    private fun initialUpdate(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = getAppWidgetIds(context, appWidgetManager)
        onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.d(TAG, "onUpdate() updates widgets with IDs ${appWidgetIds.contentToString()}.")
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            update(context, appWidgetManager, appWidgetId)
        }
    }

    @Suppress("UnnecessaryVariable")
    private fun update(context: Context, appWidgetManager: AppWidgetManager,
                       appWidgetId: Int, title: String? = null) {
        var finalTitle = title
        val view = RemoteViews(context.packageName, R.layout.widget_list)
        val uniqueRequestCode = appWidgetId

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            ////////// Legacy API to set remote adapter //////////
            // Intent to call the Service adding the tasks to the ListView
            var intent1 = Intent(context, TodoListWidgetViewsService::class.java)
            intent1.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            // This line of code causes that for every widget an own WidgetViewsFactory gets created:
            intent1.setData(intent1.toUri(Intent.URI_INTENT_SCHEME).toUri())
            view.setRemoteAdapter(R.id.listview_widget, intent1)
        } else {
            ////////// Up-to-date API to set remote adapter //////////
            val items = factory.createItems(context, appWidgetId)
            finalTitle = factory.listName
            val builder = RemoteViews.RemoteCollectionItems.Builder()
                .setHasStableIds(true)
                .setViewTypeCount(factory.numberOfViewTypes)
            for (item in items) {
                builder.addItem(item.first.toLong(), item.second)
            }
            view.setRemoteAdapter(R.id.listview_widget, builder.build())
        }

        // Intent-template to open the App by clicking on an elements of the LinearLayout.
        // This template gets filled in TodoListWidgetViewsFactory#createItem() via setOnClickFillInIntent()
        val intent2 = Intent(context, MainActivity::class.java)
        var pendingIntent = PendingIntent.getActivity(context, uniqueRequestCode, intent2,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        view.setPendingIntentTemplate(R.id.listview_widget, pendingIntent)

        // Intent to open the App by clicking on the widget title or the widget background.
        val pref = TodoListWidgetConfigureActivity.loadWidgetPreferences(context, appWidgetId) ?: TodoListWidgetPreferences()
        val intent3 = Intent(context, MainActivity::class.java)
        intent3.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        intent3.putExtra(EXTRA_TODO_WIDGET_LIST_ID, pref.todoListId.toString())
        pendingIntent = PendingIntent.getActivity(context, uniqueRequestCode, intent3,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        view.setOnClickPendingIntent(R.id.ll_todo_list_widget_root, pendingIntent)

        view.setEmptyView(R.id.listview_widget, R.id.tv_empty_widget)
        if (null != finalTitle) {
            view.setTextViewText(R.id.widget_title, finalTitle)
        }

        appWidgetManager.updateAppWidget(appWidgetId, view)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listview_widget)
        }

        Log.d(TAG, "Widget $appWidgetId: Updated. List ID: ${pref.todoListId}, title: '$finalTitle'.")
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager,
                                           appWidgetId: Int, newOptions: Bundle) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)

        // Check if title was changed.
        val title = newOptions.getString(OPTION_TODO_WIDGET_TITLE, null)
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
        const val EXTRA_TODO_WIDGET_LIST_ID = "EXTRA_TODO_WIDGET_LIST_ID"
        const val OPTION_TODO_WIDGET_TITLE = "OPTION_TODO_WIDGET_TITLE"

        private fun getAppWidgetIds(context: Context): IntArray {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            return getAppWidgetIds(context, appWidgetManager)
        }

        private fun getAppWidgetIds(context: Context, appWidgetManager: AppWidgetManager): IntArray {
            val thisComponentName = ComponentName(context.packageName, TodoListWidget::class.java.name)
            return appWidgetManager.getAppWidgetIds(thisComponentName)
        }

        fun registerAsModelObserver(context: Context) {
            val intent = Intent(context, TodoListWidget::class.java)
            intent.setAction(ACTION_REGISTER_AS_MODEL_OBSERVER)
            context.sendBroadcast(intent)
        }

        fun triggerWidgetUpdate(context: Context, reason: String): Int {
            val appWidgetIds = getAppWidgetIds(context)
            return triggerWidgetUpdate(context, appWidgetIds, reason)
        }

        fun triggerWidgetUpdate(context: Context, appWidgetIds: IntArray, reason: String): Int {
            if (appWidgetIds.isNotEmpty()) {
                Log.d(TAG, "Triggering update of widgets ${appWidgetIds.contentToString()}. Reason: $reason.")
                val intent = Intent(context, TodoListWidget::class.java)
                intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                context.sendBroadcast(intent)
            } else {
                Log.d(TAG, "Skipped widget update ($reason) because no widget available.")
            }
            return appWidgetIds.size
        }
    }
}
