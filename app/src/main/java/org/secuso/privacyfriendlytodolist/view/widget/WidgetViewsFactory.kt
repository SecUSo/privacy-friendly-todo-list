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

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService.RemoteViewsFactory
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.ModelServices
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.viewmodel.CustomViewModel

/**
 * Created by Sebastian Lutz on 15.02.2018.
 *
 * This class sets to-do tasks to show up in the widget
 *
 */
class WidgetViewsFactory(private val context: Context) : RemoteViewsFactory {
    private var todoTasks: List<TodoTask> = ArrayList()
    private var viewModel: CustomViewModel? = null
    private var model: ModelServices? = null
    private var listChosen: String? = null

    override fun onCreate() {
        viewModel = CustomViewModel(context)
        model = viewModel!!.model
        onDataSetChanged()
    }

    override fun onDestroy() {
        model = null
        viewModel!!.destroy()
        viewModel = null
    }

    override fun getCount(): Int {
        return todoTasks.size
    }

    override fun onDataSetChanged() {
        listChosen = TodoListWidgetConfigureActivity.loadTitlePref(context, appWidgetId)
        model!!.getAllToDoLists { todoLists ->
            for (todoList in todoLists) {
                if (todoList.getName() == listChosen) {
                    todoTasks = todoList.getTasks()
                    break
                }
            }
        }
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    @SuppressLint("ResourceType")
    override fun getViewAt(position: Int): RemoteViews {
        var itemView: RemoteViews? = null
        if (position >= 0 && position < todoTasks.size) {
            val todo = todoTasks[position]
            itemView = RemoteViews(context.packageName, R.layout.widget_tasks)
            if (todo.isDone()) {
                itemView.setViewVisibility(R.id.widget_done, View.VISIBLE)
                itemView.setViewVisibility(R.id.widget_undone, View.INVISIBLE)
            } else if (!todo.isDone()) {
                itemView.setViewVisibility(R.id.widget_done, View.INVISIBLE)
                itemView.setViewVisibility(R.id.widget_undone, View.VISIBLE)
            }
            itemView.setTextViewText(R.id.tv_widget_task_name, todo.getName())
            itemView.setEmptyView(R.id.tv_empty_widget, R.string.empty_todo_list)
            val fillInIntent = Intent()
            itemView.setOnClickFillInIntent(R.id.tv_widget_task_name, fillInIntent)
            itemView.setOnClickFillInIntent(R.id.widget_undone, fillInIntent)
            itemView.setOnClickFillInIntent(R.id.widget_done, fillInIntent)
        }
        return itemView!!
    }

    override fun getItemId(position: Int): Long {
        return (ID_CONSTANT + position).toLong()
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    companion object {
        private const val ID_CONSTANT = 0x0101010
        private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

        fun setAppWidgetId(id: Int) {
            appWidgetId = id
        }
    }
}
