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

        ArrayList<TodoTask> tlist = new ArrayList<TodoTask>();
        TodoTask t1 = new TodoTask("Task 1", "Das ist eine Beschreibung", true, 3, 1464030736, TodoTask.Priority.MEDIUM, 3, 0, 0);
        TodoTask t2 = new TodoTask("Task 2", "", false, 5, -1, TodoTask.Priority.HIGH, 1, 0, 0);
        TodoTask t3 = new TodoTask("Task 3", "Das ist eine Beschreibung", false, 7, 1464030736, TodoTask.Priority.MEDIUM, 3, 0, 0);
        TodoTask t4 = new TodoTask("Task 4", "Das ist eine Beschreibung", true, 1, 1464030736, TodoTask.Priority.LOW, 3, 0, 0);
        TodoTask t5 = new TodoTask("Task 5", "", false, 5, -1, TodoTask.Priority.HIGH, 1, 0, 0);
        TodoTask t6 = new TodoTask("Task 6", "", false, 5, -1, TodoTask.Priority.MEDIUM, 1, 0, 0);

        t1.setSubTasks(s1);
        t2.setSubTasks(s1);

        tlist.add(t1);
        tlist.add(t2);
        tlist.add(t3);
        tlist.add(t4);
        tlist.add(t5);
        tlist.add(t6);

        TodoList l1 = new TodoList("List 1", "1.2.2012", 1464030736);
        TodoList l2 = new TodoList("List 2", "4.5.2015", 1464030736);
        TodoList l3 = new TodoList("List 3", "5.3.2013", 1464030736);
        TodoList l4 = new TodoList("List 4", "1.1.2023", 1464030736);
        TodoList l5 = new TodoList("List 5", "3.4.2002", 1464030736);

        l1.setTasks(tlist);
        l2.setTasks(tlist);
        l3.setTasks(tlist);
        l4.setTasks(tlist);

        lists = new ArrayList<TodoList>();
        lists.add(l1);
        lists.add(l2);
        lists.add(l3);
        lists.add(l4);
        lists.add(l5);
    }

}
