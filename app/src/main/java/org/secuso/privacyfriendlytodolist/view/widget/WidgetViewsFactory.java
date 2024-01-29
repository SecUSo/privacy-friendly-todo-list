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
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ClipData.Item;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Binder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.model.database.DBQueryHandler;
import org.secuso.privacyfriendlytodolist.model.database.DatabaseHelper;
import org.secuso.privacyfriendlytodolist.model.database.tables.TTodoTask;
import org.secuso.privacyfriendlytodolist.view.MainActivity;

import java.util.ArrayList;

/**
 * Created by Sebastian Lutz on 15.02.2018.
 *
 * This class sets to-do tasks to show up in the widget
 *
 */


public class WidgetViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private ArrayList<TodoList> lists;
    private Context mContext;
    private static final int ID_CONSTANT = 0x0101010;
    private ArrayList<TodoTask> listTasks;
    private String listChosen;
    private static Context c;
    private static int id;



    public WidgetViewsFactory(Context context, Intent intent){
        mContext = context;
        lists = new ArrayList<TodoList>();
        listTasks = new ArrayList<TodoTask>();
    }


    @Override
    public void onCreate() {
        listChosen = getListName(c, id);
            lists = DBQueryHandler.getAllToDoLists(DatabaseHelper.getInstance(mContext).getReadableDatabase());
            for (int i=0; i < lists.size(); i++){
                if (lists.get(i).getName().equals(this.listChosen))
                    listTasks = lists.get(i).getTasks();

            }


    }

    @Override
    public int getCount() {
        return listTasks.size();
    }



    @Override
    public void onDataSetChanged() {
        listChosen = getListName(c, id);
            lists = DBQueryHandler.getAllToDoLists(DatabaseHelper.getInstance(mContext).getReadableDatabase());
            for (int i=0; i < lists.size(); i++){
                if (lists.get(i).getName().equals(this.listChosen))
                    listTasks = lists.get(i).getTasks();
            }
    }



    @Override
    public int getViewTypeCount() {
        return 1;
    }



    @SuppressLint("ResourceType")
    @Override
    public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION){
            return null;
        }

        TodoTask todo = listTasks.get(position);

        RemoteViews itemView = new RemoteViews(mContext.getPackageName(), R.layout.widget_tasks);
        if (todo.getDone()){
            itemView.setViewVisibility(R.id.widget_done, View.VISIBLE);
            itemView.setViewVisibility(R.id.widget_undone, View.INVISIBLE);
        } else if (!todo.getDone()) {
            itemView.setViewVisibility(R.id.widget_done, View.INVISIBLE);
            itemView.setViewVisibility(R.id.widget_undone, View.VISIBLE);
        }

        itemView.setTextViewText(R.id.tv_widget_task_name, todo.getName());
        itemView.setEmptyView(R.id.tv_empty_widget, R.string.empty_todo_list);

        Intent fillInIntent = new Intent();
        itemView.setOnClickFillInIntent(R.id.tv_widget_task_name, fillInIntent);
        itemView.setOnClickFillInIntent(R.id.widget_undone, fillInIntent);
        itemView.setOnClickFillInIntent(R.id.widget_done, fillInIntent);




        return itemView;
    }



    @Override
    public long getItemId(int position) {

        return ID_CONSTANT + position;
    }



    @Override
    public void onDestroy() {
        lists.clear();
    }



    @Override
    public RemoteViews getLoadingView() {
        return null;
    }



    @Override
    public boolean hasStableIds() {
        return true;
    }



    public static String getListName(Context context, int AppWidgetId) {
        c = context;
        id = AppWidgetId;
        return TodoListWidgetConfigureActivity.loadTitlePref(context, AppWidgetId);
    }


}




