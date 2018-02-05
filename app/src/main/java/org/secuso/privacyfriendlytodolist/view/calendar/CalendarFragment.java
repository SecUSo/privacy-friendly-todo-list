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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.Helper;
import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.view.MainActivity;
import org.secuso.privacyfriendlytodolist.view.TodoTasksFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

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
                    dummyList.setDummyList();
                    dummyList.setName(key);
                    dummyList.setTasks(todaysTasks);
                    containerActivity.setDummyList(dummyList);
                    Bundle bundle = new Bundle();
                    bundle.putInt(TodoList.UNIQUE_DATABASE_ID, TodoList.DUMMY_LIST_ID);
                    bundle.putBoolean(TodoTasksFragment.SHOW_FLOATING_BUTTON, false);
                    TodoTasksFragment fragment = new TodoTasksFragment();
                    fragment.setArguments(bundle);
                    //containerActivity.setFragment(fragment);
                }
            }
        });

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    private String absSecondsToDate(long seconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(TimeUnit.SECONDS.toMillis(seconds));
        return DateFormat.format(Helper.DATE_FORMAT, cal).toString();
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
