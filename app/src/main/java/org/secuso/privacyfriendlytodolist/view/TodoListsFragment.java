package org.secuso.privacyfriendlytodolist.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.model.TodoSubTask;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import java.util.ArrayList;

/**
 * Created by dominik on 24.05.16.
 */
public class TodoListsFragment extends Fragment {

    private ArrayList<TodoList> lists = new ArrayList<>();
    private TodoRecyclerView mRecyclerView;
    private TodoRecyclerView.LayoutManager mLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.todo_list_overview, container, false);

        prepareData();

        mRecyclerView = (TodoRecyclerView) v.findViewById(R.id.rv_todo_lists);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(new TodoListAdapter(getActivity(), lists));
        mRecyclerView.setEmptyView((TextView) v.findViewById(R.id.tv_rv_empty_view));

        return v;
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.toolbar_title_main);

    }

    private void prepareData() {
        ArrayList<TodoSubTask> s1 = new ArrayList<TodoSubTask>();
        s1.add(new TodoSubTask("Sub 1", false));
        s1.add(new TodoSubTask("Sub 2", false));
        s1.add(new TodoSubTask("Sub 3", false));

        ArrayList<TodoTask> tlist1 = new ArrayList<TodoTask>();
        ArrayList<TodoTask> tlist2 = new ArrayList<TodoTask>();
        TodoTask t1 = new TodoTask(0, 0, "Task 1", "Das ist eine Beschreibung", false, 3, 1465381890, TodoTask.Priority.MEDIUM, 3, 0, 86400);
        TodoTask t2 = new TodoTask(1, 1, "Task 2", "", false, 5, -1, TodoTask.Priority.HIGH, 1, 0, 0);
        TodoTask t3 = new TodoTask(2, 2, "Task 3", "Das ist eine Beschreibung", false, 7, 1464030736, TodoTask.Priority.MEDIUM, 3, 0, 0);
        TodoTask t4 = new TodoTask(3, 3, "Task 4", "Das ist eine Beschreibung", true, 1, 1478860290, TodoTask.Priority.LOW, 3, 0, 0);
        TodoTask t5 = new TodoTask(4, 4, "Task 5", "", false, 5, -1, TodoTask.Priority.HIGH, 1, 0, 0);
        TodoTask t6 = new TodoTask(5, 5, "Task 6", "", true, 5, -1, TodoTask.Priority.MEDIUM, 1, 0, 0);

        t1.setSubTasks(s1);
        t2.setSubTasks(s1);

        tlist1.add(t1);
        tlist1.add(t2);
        tlist1.add(t3);
        tlist1.add(t4);
        tlist1.add(t5);
        tlist1.add(t6);

        tlist2.add(t1);
        tlist2.add(t2);
        tlist2.add(t4);
        tlist2.add(t5);
        tlist2.add(t6);

        TodoList l1 = new TodoList("List 1", "1.2.2012", 1464030736);
        TodoList l2 = new TodoList("List 2", "4.5.2015", 1464030736);
        TodoList l3 = new TodoList("List 3", "5.3.2013", 1464030736);
        TodoList l4 = new TodoList("List 4", "1.1.2023", 1464030736);
        TodoList l5 = new TodoList("List 5", "3.4.2002", 1464030736);

        l1.setTasks(tlist1);
        l2.setTasks(tlist2);
        l3.setTasks(tlist1);
        l4.setTasks(tlist2);

        lists = new ArrayList<TodoList>();
        lists.add(l1);
        lists.add(l2);
        lists.add(l3);
        lists.add(l4);
        lists.add(l5);
    }

}
