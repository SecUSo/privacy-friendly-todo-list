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
import android.content.ClipData.Item;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Binder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.model.database.DBQueryHandler;
import org.secuso.privacyfriendlytodolist.model.database.DatabaseHelper;
import org.secuso.privacyfriendlytodolist.model.database.tables.TTodoTask;
import org.secuso.privacyfriendlytodolist.view.MainActivity;

import java.util.ArrayList;

/**
 * Created by Sebastian Lutz on 15.02.2018.
 *
 * This class sets to-do tasks to show
 *
 */

public class WidgetViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private ArrayList<TodoTask> tasks;
    private Context mContext;
    private static final int ID_CONSTANT = 0x0101010;
    private static int appWidgetId;
    //private Cursor cursor;

    public WidgetViewsFactory(Context context, Intent intent){
        mContext = context;
        tasks = new ArrayList<TodoTask>();

        /*appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        this.tasks = DBQueryHandler.getAllToDoTasks(DatabaseHelper.getInstance(context).getReadableDatabase()); */
    }


    @Override
    public void onCreate() {

        tasks = DBQueryHandler.getAllToDoTasks(DatabaseHelper.getInstance(mContext).getReadableDatabase());

    }

    @Override
    public int getCount() {
        return tasks.size();
    }


    @Override
    public void onDataSetChanged() {

        tasks = DBQueryHandler.getAllToDoTasks(DatabaseHelper.getInstance(mContext).getReadableDatabase());
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION){
            return null;
        }

        TodoTask todo = tasks.get(position);

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

        /*Intent intent = new Intent();
        intent.putExtra(TodoListWidget.EXTRA_ITEM, todo);
        itemView.setOnClickFillInIntent(R.id.listview_widget, intent); */

        Intent detailIntent = new Intent(mContext, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(mContext, 0, detailIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        itemView.setOnClickPendingIntent(R.id.widget_undone, pIntent);
        return itemView;
    }

    @Override
    public long getItemId(int position) {

        return ID_CONSTANT + position;
    }

    @Override
    public void onDestroy() {
        tasks.clear();
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
