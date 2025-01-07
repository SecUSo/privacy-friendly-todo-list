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

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService.RemoteViewsFactory
import androidx.core.content.res.ResourcesCompat
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.ModelServices
import org.secuso.privacyfriendlytodolist.model.ModelServices.DeliveryOption
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.util.LogTag
import org.secuso.privacyfriendlytodolist.util.PreferenceMgr
import org.secuso.privacyfriendlytodolist.util.TaskComparator
import org.secuso.privacyfriendlytodolist.view.TaskFilter
import org.secuso.privacyfriendlytodolist.viewmodel.CustomViewModel

/**
 * Created by Sebastian Lutz on 15.02.2018.
 *
 * This class sets to-do tasks to show up in the widget.
 */
class TodoListWidgetViewsFactory(private val context: Context, private val appWidgetId: Int) : RemoteViewsFactory {
    private var viewModel: CustomViewModel? = null
    private var model: ModelServices? = null
    private val items = ArrayList<Pair<Int, RemoteViews>>()
    private lateinit var defaultTitle: String
    private var currentTitle: String = ""
    private var taskComparator = TaskComparator()

    override fun onCreate() {
        viewModel = CustomViewModel(context)
        model = viewModel!!.model
        defaultTitle = context.getString(R.string.app_name)
    }

    override fun onDestroy() {
        model = null
        viewModel!!.destroy()
        viewModel = null
    }

    override fun onDataSetChanged() {
        val pref = TodoListWidgetConfigureActivity.loadWidgetPreferences(context, appWidgetId) ?: TodoListWidgetPreferences()
        val job: Job
        var newTitle: String? = null
        var changedTodoTasks: MutableList<TodoTask>? = null
        if (null != pref.todoListId) {
            job = model!!.getToDoListById(pref.todoListId!!, DeliveryOption.DIRECT) { todoList ->
                if (null != todoList) {
                    newTitle = todoList.getName()
                    changedTodoTasks = todoList.getTasks()
                }
            }
        } else {
            job = model!!.getAllToDoTasks(DeliveryOption.DIRECT) { todoTasks ->
                newTitle = defaultTitle
                changedTodoTasks = todoTasks
            }
        }
        runBlocking {
            job.join()
        }
        if (null == changedTodoTasks) {
            Log.e(TAG, "Widget $appWidgetId: Failed to get changed tasks.")
            return
        }

        if (null != newTitle && currentTitle != newTitle) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val bundle: Bundle = appWidgetManager.getAppWidgetOptions(appWidgetId)
            bundle.putString(TodoListWidget.OPTION_WIDGET_TITLE, newTitle)
            appWidgetManager.updateAppWidgetOptions(appWidgetId, bundle)
            currentTitle = newTitle!!
        }

        taskComparator.isGroupingByPriority = pref.isGroupingByPriority
        taskComparator.isSortingByDeadline = pref.isSortingByDeadline
        taskComparator.isSortingByNameAsc = pref.isSortingByNameAsc
        changedTodoTasks!!.sortWith(taskComparator)

        items.clear()
        val fillInIntent = Intent()
        fillInIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        fillInIntent.putExtra(TodoListWidget.EXTRA_WIDGET_LIST_ID, pref.todoListId.toString())
        val reminderTimeSpan = PreferenceMgr.getDefaultReminderTimeSpan(context)
        for (todoTask in changedTodoTasks!!) {
            if (   (pref.taskFilter == TaskFilter.OPEN_TASKS      &&   todoTask.isDone())
                || (pref.taskFilter == TaskFilter.COMPLETED_TASKS && ! todoTask.isDone())) {
                continue
            }
            val item = createItem(todoTask, reminderTimeSpan, fillInIntent)
            val tuple = Pair(todoTask.getId(), item)
            items.add(tuple)
        }
        Log.d(TAG, "Widget $appWidgetId: Updated data. Items: ${items.count()}, list ID: ${pref.todoListId}, title: '$currentTitle'.")
    }

    private fun createItem(todoTask: TodoTask, reminderTimeSpan: Long, fillInIntent: Intent): RemoteViews {
        val view = RemoteViews(context.packageName, R.layout.widget_task)
        val urgencyColor = todoTask.getUrgency(reminderTimeSpan).getColor(context)
        view.setInt(R.id.ll_widget_urgency_task, "setBackgroundColor", urgencyColor)
        view.setImageViewResource(R.id.iv_widget_task_state, if (todoTask.isDone()) R.drawable.done else ResourcesCompat.ID_NULL)
        view.setTextViewText(R.id.tv_widget_task_name, todoTask.getName())
        view.setOnClickFillInIntent(R.id.iv_widget_task_state, fillInIntent)
        view.setOnClickFillInIntent(R.id.tv_widget_task_name, fillInIntent)
        return view
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewAt(position: Int): RemoteViews? {
        return if (position in items.indices) items[position].second else null
    }

    override fun getItemId(position: Int): Long {
        return if (position in items.indices) items[position].first.toLong() else 0
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
    }
}
