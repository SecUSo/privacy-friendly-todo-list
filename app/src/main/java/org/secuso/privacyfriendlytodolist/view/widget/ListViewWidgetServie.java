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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.model.database.DBQueryHandler;
import org.secuso.privacyfriendlytodolist.model.database.DatabaseHelper;

import java.util.ArrayList;

/**
 * Created by Sebastian Lutz on 14.02.2018.
 *
 * Service needed to give the widget its data.
 *
 */

public class ListViewWidgetServie extends RemoteViewsService {


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListViewRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    class ListViewRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

        private Context mContext;
        private ArrayList<String> records = new ArrayList<String>();


        public ListViewRemoteViewsFactory(Context context, Intent intent) {

            mContext = context;

        }

        @Override
        public void onCreate() {
            ArrayList<TodoTask> tasks = new ArrayList<TodoTask>();
            DatabaseHelper dbhelper = DatabaseHelper.getInstance(this.mContext);
            tasks = DBQueryHandler.getAllToDoTasks(dbhelper.getReadableDatabase());
            for (int i=0; i<tasks.size(); i++) {
                records.add(tasks.get(i).getName());
            }

        }

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public void onDataSetChanged() {

        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.todo_list_widget);
            String data=records.get(position);
            rv.setTextViewText(R.id.item, data);

            Bundle extras = new Bundle();

            extras.putInt(TodoListWidget.EXTRA_ITEM, position);

            Intent fillInIntent = new Intent();

            fillInIntent.putExtra("homescreen_meeting",data);

            fillInIntent.putExtras(extras);

            rv.setOnClickFillInIntent(R.id.item, fillInIntent);

            return rv;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public void onDestroy() {
            records.clear();
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
}
