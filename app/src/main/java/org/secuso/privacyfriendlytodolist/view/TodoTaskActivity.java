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

package org.secuso.privacyfriendlytodolist.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.model.database.DBQueryHandler;
import org.secuso.privacyfriendlytodolist.model.database.DatabaseHelper;

import java.util.ArrayList;

/**
 * Created by sebbi on 07.01.2018.
 */

public class TodoTaskActivity extends AppCompatActivity {

    private DatabaseHelper dbhelper;
    private ExpandableListView expandableListView;
    private ExpandableTodoTaskAdapter taskAdapter;

    private ArrayList<TodoTask> tasks = new ArrayList<>();

    private TodoList currentList;

    private MainActivity containingActivity;

    private ExpandableListAdapter expandableListAdapter;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_todo_tasks);
        dbhelper = DatabaseHelper.getInstance(this);

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_tasks);

        if (toolbar != null) {
            toolbar.setTitle("All tasks");
            toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
            setSupportActionBar(toolbar);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

        } */
        showAllTasks();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    public void showAllTasks() {
        tasks = DBQueryHandler.getAllToDoTasks(dbhelper.getReadableDatabase());
        ExpandableTodoTaskAdapter expandableTodoTaskAdapter = new ExpandableTodoTaskAdapter(this, tasks);
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.relative_task);
        ExpandableListView lv = (ExpandableListView) findViewById(R.id.exlv_tasks);
        TextView tv = (TextView) findViewById(R.id.tv_empty_view_no_tasks);
        lv.setAdapter(expandableTodoTaskAdapter);
        lv.setEmptyView(tv);
    }

    public void showTasksOfList(TodoList todoList) {
    }

}
