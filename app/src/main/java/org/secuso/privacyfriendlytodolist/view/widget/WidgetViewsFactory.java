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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.ModelServices;
import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.viewmodel.CustomViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sebastian Lutz on 15.02.2018.
 *
 * This class sets to-do tasks to show up in the widget
 *
 */


public class WidgetViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final int ID_CONSTANT = 0x0101010;

    private static Context c;
    private static int id;

    private final Context context;
    private CustomViewModel viewModel;
    private ModelServices model;
    private List<TodoTask> todoTasks;
    private String listChosen;

    public static String getListName(Context context, int AppWidgetId) {
        c = context;
        id = AppWidgetId;
        return TodoListWidgetConfigureActivity.loadTitlePref(context, AppWidgetId);
    }

    public WidgetViewsFactory(Context context) {
        this.context = context;
        todoTasks = new ArrayList<>();
    }

    @Override
    public void onCreate() {
        viewModel = new CustomViewModel(context);
        model = viewModel.getModel();

        onDataSetChanged();
    }

    @Override
    public void onDestroy() {
        model = null;
        viewModel.destroy();
        viewModel = null;
    }

    @Override
    public int getCount() {
        return todoTasks.size();
    }

    @Override
    public void onDataSetChanged() {
        listChosen = getListName(c, id);
        model.getAllToDoLists(todoLists -> {
            for (TodoList todoList : todoLists){
                if (todoList.getName().equals(listChosen)) {
                    todoTasks = todoList.getTasks();
                    break;
                }
            }
        });
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @SuppressLint("ResourceType")
    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews itemView = null;
        if (position >= 0 && position < todoTasks.size()) {
            TodoTask todo = todoTasks.get(position);
            itemView = new RemoteViews(context.getPackageName(), R.layout.widget_tasks);
            if (todo.isDone()){
                itemView.setViewVisibility(R.id.widget_done, View.VISIBLE);
                itemView.setViewVisibility(R.id.widget_undone, View.INVISIBLE);
            } else if (!todo.isDone()) {
                itemView.setViewVisibility(R.id.widget_done, View.INVISIBLE);
                itemView.setViewVisibility(R.id.widget_undone, View.VISIBLE);
            }

            itemView.setTextViewText(R.id.tv_widget_task_name, todo.getName());
            itemView.setEmptyView(R.id.tv_empty_widget, R.string.empty_todo_list);

            Intent fillInIntent = new Intent();
            itemView.setOnClickFillInIntent(R.id.tv_widget_task_name, fillInIntent);
            itemView.setOnClickFillInIntent(R.id.widget_undone, fillInIntent);
            itemView.setOnClickFillInIntent(R.id.widget_done, fillInIntent);
        }
        return itemView;
    }

    @Override
    public long getItemId(int position) {
        return ID_CONSTANT + position;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}




