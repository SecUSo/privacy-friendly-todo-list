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
import org.secuso.privacyfriendlytodolist.model.database.DBQueryHandler;
import org.secuso.privacyfriendlytodolist.model.database.DatabaseHelper;

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
        mRecyclerView.setEmptyView((TextView) v.findViewById(R.id.empty_view));

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
        TodoList l1 = new TodoList("List 1", "1.2.2012", 1464030736);
        TodoList l2 = new TodoList("List 2", "4.5.2015", 1464030736);
        TodoList l3 = new TodoList("List 3", "5.3.2013", 1464030736);
        TodoList l4 = new TodoList("List 4", "1.1.2023", 1464030736);
        TodoList l5 = new TodoList("List 5", "3.4.2002", 1464030736);

        lists = new ArrayList<TodoList>();
        lists.add(l1);
        lists.add(l2);
        lists.add(l3);
        lists.add(l4);
        lists.add(l5);
    }

}
