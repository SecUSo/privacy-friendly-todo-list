package org.secuso.privacyfriendlytodolist.view.calendar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.view.MainActivity;
import org.secuso.privacyfriendlytodolist.view.TodoTasksFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private CalendarGridAdapter calendarGridAdapter;
    protected MainActivity containerActivity;
    private HashMap<String, ArrayList<TodoTask>> tasksPerDay = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        containerActivity = (MainActivity)getActivity();

        View v = inflater.inflate(R.layout.fragment_calendar, container, false);
        calendarView = (CalendarView) v.findViewById(R.id.calendar_view);
        calendarGridAdapter = new CalendarGridAdapter(getContext(), R.layout.calendar_day);
        calendarView.setGridAdapter(calendarGridAdapter);

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
                Date selectedDate = calendarGridAdapter.getItem(position);
                String key = absSecondsToDate(selectedDate.getTime()/1000);
                ArrayList<TodoTask> todaysTasks = tasksPerDay.get(key);
                if(todaysTasks == null) {
                    Toast.makeText(getContext(), getString(R.string.no_deadline_today), Toast.LENGTH_SHORT).show();
                } else {
                    TodoList dummyList = new TodoList();
                    dummyList = new TodoList();
                    dummyList.setDummyList();
                    dummyList.setName(key);
                    dummyList.setTasks(todaysTasks);
                    containerActivity.setDummyList(dummyList);
                    Bundle bundle = new Bundle();
                    bundle.putLong(TodoList.UNIQUE_DATABASE_ID, TodoList.DUMMY_LIST_ID);
                    bundle.putBoolean(TodoTasksFragment.SHOW_FLOATING_BUTTON, false);
                    TodoTasksFragment fragment = new TodoTasksFragment();
                    fragment.setArguments(bundle);
                    containerActivity.setFragment(fragment);
                }
            }
        });

        return v;
    }

    private String absSecondsToDate(long seconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(seconds * 1000);
        return DateFormat.format("dd-MM-yyyy", cal).toString();
    }

    @Override
    public void onResume() {
        super.onResume();

        ArrayList<TodoList> todoLists = containerActivity.getTodoLists(true);
        tasksPerDay.clear();
        for(TodoList list : todoLists) {
            for(TodoTask task : list.getTasks()) {
                long deadline = task.getDeadline();
                String key = absSecondsToDate(deadline);
                if(!tasksPerDay.containsKey(key)) {
                    tasksPerDay.put(key, new ArrayList<TodoTask>());
                }
                tasksPerDay.get(key).add(task);
            }
        }

        calendarGridAdapter.setTodoTasks(tasksPerDay);
        calendarGridAdapter.notifyDataSetChanged();
        containerActivity.getSupportActionBar().setTitle(R.string.calendar);
    }


}
