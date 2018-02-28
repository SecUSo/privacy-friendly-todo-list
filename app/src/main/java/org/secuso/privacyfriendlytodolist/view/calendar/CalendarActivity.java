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

package org.secuso.privacyfriendlytodolist.view.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.Helper;
import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.model.database.DBQueryHandler;
import org.secuso.privacyfriendlytodolist.model.database.DatabaseHelper;
import org.secuso.privacyfriendlytodolist.view.ExpandableTodoTaskAdapter;
import org.secuso.privacyfriendlytodolist.view.MainActivity;
import org.secuso.privacyfriendlytodolist.view.TodoTasksFragment;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by Sebastian Lutz on 31.01.2018.
 *
 * This Activity creates a calendar using CalendarGripAdapter to show deadlines of a task.
 */

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private CalendarGridAdapter calendarGridAdapter;
    protected MainActivity containerActivity;
    private HashMap<String, ArrayList<TodoTask>> tasksPerDay = new HashMap<>();
    private DatabaseHelper dbHelper;

    private ExpandableListView expandableListView;
    private ExpandableTodoTaskAdapter taskAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_calendar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_calendar);

        if (toolbar != null) {
            toolbar.setTitle(R.string.calendar);
            toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
            setSupportActionBar(toolbar);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

        }

        calendarView = (CalendarView) findViewById(R.id.calendar_view);
        calendarGridAdapter = new CalendarGridAdapter(this, R.layout.calendar_day);
        calendarView.setGridAdapter(calendarGridAdapter);
        expandableListView = (ExpandableListView) findViewById(R.id.exlv_tasks);

        dbHelper = DatabaseHelper.getInstance(this);

        updateDeadlines();

        calendarView.setNextMonthOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarView.incMonth(1);
                calendarView.refresh();
            }
        });

        calendarView.setPrevMontOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarView.incMonth(-1);
                calendarView.refresh();
            }
        });

        calendarView.setDayOnClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateDeadlines();
                Date selectedDate = calendarGridAdapter.getItem(position);
                String key = absSecondsToDate(selectedDate.getTime()/1000);
                ArrayList<TodoTask> todaysTasks = tasksPerDay.get(key);
                if(todaysTasks == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.no_deadline_today), Toast.LENGTH_SHORT).show();
                } else {
                    showDeadlineTasks(todaysTasks);
                }
            }
        });

    }



    private void updateDeadlines(){
        ArrayList<TodoList> todoLists = DBQueryHandler.getAllToDoLists(dbHelper.getReadableDatabase());
        tasksPerDay.clear();
        for (TodoList list : todoLists){
            for (TodoTask task : list.getTasks()){
                long deadline = task.getDeadline();
                String key = absSecondsToDate(deadline);
                if (!tasksPerDay.containsKey(key)){
                    tasksPerDay.put(key, new ArrayList<TodoTask>());
                }
                tasksPerDay.get(key).add(task);
            }
        }
        calendarGridAdapter.setTodoTasks(tasksPerDay);
        calendarGridAdapter.notifyDataSetChanged();
        //containerActivity.getSupportActionBar().setTitle(R.string.calendar);
    }

    private String absSecondsToDate(long seconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(TimeUnit.SECONDS.toMillis(seconds));
        return DateFormat.format(Helper.DATE_FORMAT, cal).toString();
    }

    private void showDeadlineTasks(ArrayList<TodoTask> tasks){
        setContentView(R.layout.content_main);
        //taskAdapter = new ExpandableTodoTaskAdapter(this, tasks);
        //expandableListView.setAdapter(taskAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
