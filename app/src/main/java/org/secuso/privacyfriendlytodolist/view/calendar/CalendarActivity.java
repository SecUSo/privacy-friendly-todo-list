package org.secuso.privacyfriendlytodolist.view.calendar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.*;
import android.widget.*;
import android.widget.Toolbar;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.model.TodoTask;

import java.util.ArrayList;


public class CalendarActivity extends AppCompatActivity {

    private ArrayList<TodoList> todoLists;
    private CalendarGridAdapter gridAdapter;
    private CalendarView calendarView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_calendar);
        calendarView = (CalendarView) findViewById(R.id.calendar_view);

        // toolbar setup
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.calendar));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // get todos
        Bundle extras = getIntent().getExtras();
        todoLists = extras.getParcelableArrayList(TodoList.PARCELABLE_KEY);
        ArrayList<TodoTask> tasks = new ArrayList<>();
        for(TodoList list : todoLists)
            tasks.addAll(list.getTasks());
        calendarView.setTodoTasks(tasks);
    }


}
