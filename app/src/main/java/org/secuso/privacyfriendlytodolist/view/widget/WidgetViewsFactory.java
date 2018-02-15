package org.secuso.privacyfriendlytodolist.view.widget;

import android.appwidget.AppWidgetManager;
import android.content.ClipData.Item;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.model.database.DBQueryHandler;
import org.secuso.privacyfriendlytodolist.model.database.DatabaseHelper;

import java.util.ArrayList;

/**
 * Created by Sebastian Lutz on 15.02.2018.
 */

public class WidgetViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private ArrayList<TodoTask> tasks = new ArrayList<TodoTask>();
    private Context context;
    private int appWidgetId;

    public WidgetViewsFactory(Context context, Intent intent){
        this.context = context;

        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        this.tasks = DBQueryHandler.getAllToDoTasks(DatabaseHelper.getInstance(context).getReadableDatabase());
    }


    @Override
    public void onCreate() {

    }

    @Override
    public int getCount() {
        return tasks.size();
    }

    @Override
    public void onDataSetChanged() {
        this.tasks = DBQueryHandler.getAllToDoTasks(DatabaseHelper.getInstance(context).getReadableDatabase());
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews listView = new RemoteViews(context.getPackageName(), R.id.list_widget);
        TodoTask todo = tasks.get(position);
        listView.setTextViewText(android.R.layout.simple_selectable_list_item, todo.getName());
        return listView;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public void onDestroy() {

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
