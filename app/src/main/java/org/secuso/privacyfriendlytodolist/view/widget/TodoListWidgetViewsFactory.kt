/*
Privacy Friendly To-Do List
Copyright (C) 2025  Christian Adams

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
import android.util.Log
import android.widget.RemoteViews
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
import java.util.ArrayList

/**
 * This class creates to-do task items to be shown on the widget.
 */
open class TodoListWidgetViewsFactory() {
    private var taskComparator = TaskComparator()
    private var viewModel: CustomViewModel? = null
    private var defaultListName = ""

    var listName = ""
    val numberOfViewTypes: Int
        get() = 1

    fun destroy() {
        viewModel?.destroy()
        viewModel = null
        defaultListName = ""
        listName = ""
    }

    fun getModel(context: Context): ModelServices {
        var vm = viewModel
        if (null == vm) {
            vm = CustomViewModel(context)
            viewModel = vm
            defaultListName = context.getString(R.string.app_name)
            listName = defaultListName
        }
        return vm.model
    }

    /**
     * Creates remote views for to-do tasks and updates the list name.
     *
     * Note: This is a expensive task. To-Do Tasks get synchronously read from DB.
     */
    fun createItems(context: Context, appWidgetId: Int): List<Pair<Int, RemoteViews>> {
        val pref = TodoListWidgetConfigureActivity.loadWidgetPreferences(context, appWidgetId) ?: TodoListWidgetPreferences()
        val job: Job
        var newListName: String? = null
        var changedTodoTasks: MutableList<TodoTask>? = null
        val items = ArrayList<Pair<Int, RemoteViews>>()

        if (null != pref.todoListId) {
            job = getModel(context).getToDoListById(pref.todoListId!!, DeliveryOption.DIRECT) { todoList ->
                if (null != todoList) {
                    newListName = todoList.getName()
                    changedTodoTasks = todoList.getTasks()
                }
            }
        } else {
            job = getModel(context).getAllToDoTasks(DeliveryOption.DIRECT) { todoTasks ->
                newListName = defaultListName
                changedTodoTasks = todoTasks
            }
        }
        runBlocking {
            job.join()
        }
        if (null == changedTodoTasks) {
            Log.e(TAG, "Widget $appWidgetId: Failed to get changed tasks.")
            return items
        }

        if (null != newListName && listName != newListName) {
            listName = newListName
        }

        taskComparator.isGroupingByPriority = pref.isGroupingByPriority
        taskComparator.isSortingByDeadline = pref.isSortingByDeadline
        taskComparator.isSortingByNameAsc = pref.isSortingByNameAsc
        changedTodoTasks.sortWith(taskComparator)

        items.clear()
        val fillInIntent = Intent()
        fillInIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        fillInIntent.putExtra(TodoListWidget.EXTRA_TODO_WIDGET_LIST_ID, pref.todoListId.toString())
        val reminderTimeSpan = PreferenceMgr.getDefaultReminderTimeSpan(context)
        for (todoTask in changedTodoTasks) {
            if (   (pref.taskFilter == TaskFilter.OPEN_TASKS      &&   todoTask.isDone())
                || (pref.taskFilter == TaskFilter.COMPLETED_TASKS && ! todoTask.isDone())) {
                continue
            }
            val item = createItem(context, todoTask, reminderTimeSpan, fillInIntent, pref.isShowingDaysUntilDeadline)
            val tuple = Pair(todoTask.getId(), item)
            items.add(tuple)
        }
        Log.d(TAG, "Widget $appWidgetId: Updated data. Items: ${items.count()}, list ID: ${pref.todoListId}, list name: '$listName'.")
        return items
    }

    private fun createItem(context: Context, todoTask: TodoTask,
                           reminderTimeSpan: Long, fillInIntent: Intent,
                           isShowingDaysUntilDeadline: Boolean): RemoteViews {
        val view = RemoteViews(context.packageName, R.layout.widget_task)
        val urgency = todoTask.getUrgency(reminderTimeSpan)
        view.setInt(R.id.ll_widget_urgency_task, "setBackgroundColor", urgency.getColor(context))
        view.setImageViewResource(R.id.iv_widget_task_state, if (todoTask.isDone()) R.drawable.ic_done_black_24dp else ResourcesCompat.ID_NULL)
        view.setTextViewText(R.id.tv_widget_task_name, todoTask.getName())
        var daysUntilDeadlineStr = ""
        if (isShowingDaysUntilDeadline) {
            val daysUntilDeadline = urgency.daysUntilDeadline
            if (daysUntilDeadline != null) {
                daysUntilDeadlineStr = daysUntilDeadline.toString()
            }
        }
        view.setTextViewText(R.id.tv_widget_days_until_deadline, daysUntilDeadlineStr)
        view.setOnClickFillInIntent(R.id.iv_widget_task_state, fillInIntent)
        view.setOnClickFillInIntent(R.id.tv_widget_task_name, fillInIntent)
        return view
    }

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
    }
}
