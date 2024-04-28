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
    private var thisComponentName: ComponentName? = null

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received action '${intent.action}'.")
        // Call base implementation. Depending on action, it calls onUpdate, onDelete, ...
        super.onReceive(context, intent)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Model.registerModelObserver(this)
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
        Model.unregisterModelObserver(this)
    }

    private fun getComponentName(context: Context): ComponentName {
        if (null == thisComponentName) {
            thisComponentName = ComponentName(context.packageName, TodoListWidget::class.java.getName())
        }
        return thisComponentName!!
    }

    override fun onTodoDataChanged(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(getComponentName(context))
        onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        var title = TodoListWidgetConfigureActivity.loadTitlePref(context, appWidgetId)
        if (title == null || title == TodoListWidgetConfigureActivity.TITLE_PREF_SHOW_ALL_TASKS) {
            title = context.getString(R.string.app_name)
        }

        var view = views[appWidgetId]
        if (view == null) {
            view = RemoteViews(context.packageName, R.layout.todo_list_widget)
            views[appWidgetId] = view

            // Intent to call the Service adding the tasks to the ListView
            val intent = Intent(context, TodoListWidgetViewsService::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            // This line of code causes that for every widget an own WidgetViewsFactory gets created:
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)))
            view.setRemoteAdapter(R.id.listview_widget, intent)

            // Intent to open the App by clicking on an elements of the LinearLayout
            val templateIntent = Intent(context, MainActivity::class.java)
            templateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            val templatePendingIntent = PendingIntent.getActivity(
                context, appWidgetId, templateIntent, PendingIntent.FLAG_IMMUTABLE)
            view.setPendingIntentTemplate(R.id.listview_widget, templatePendingIntent)

            view.setOnClickPendingIntent(R.id.click_widget, createWidgetUpdatePendingIntent(context, appWidgetId))
            view.setEmptyView(R.id.listview_widget, R.id.tv_empty_widget)
            Log.d(TAG, "Widget $appWidgetId: Created (title '$title').")
        } else {
            Log.d(TAG, "Widget $appWidgetId: Updated (title '$title').")
        }

        view.setTextViewText(R.id.widget_title, title)
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listview_widget)
        appWidgetManager.updateAppWidget(appWidgetId, view)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        Log.d(TAG, "Deleting widget(s) ${appWidgetIds.joinToString()}.")
        super.onDeleted(context, appWidgetIds)

        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            TodoListWidgetConfigureActivity.deleteTitlePref(context, appWidgetId)
            views.remove(appWidgetId)
        }
    }

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
        private var views = HashMap<Int, RemoteViews>()

        fun triggerWidgetUpdate(context: Context, appWidgetId: Int) {
            val intent = createWidgetUpdateIntent(context, appWidgetId)
            context.sendBroadcast(intent)
        }

        private fun createWidgetUpdatePendingIntent(context: Context, appWidgetId: Int): PendingIntent {
            val intent = createWidgetUpdateIntent(context, appWidgetId)
            return PendingIntent.getBroadcast(context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
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
