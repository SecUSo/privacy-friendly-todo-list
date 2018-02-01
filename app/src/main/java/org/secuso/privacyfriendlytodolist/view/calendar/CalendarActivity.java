package org.secuso.privacyfriendlytodolist.view.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
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
}
