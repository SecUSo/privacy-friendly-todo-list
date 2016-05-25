package org.secuso.privacyfriendlytodolist.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.TodoList;

/**
 * Created by dominik on 24.05.16.
 */
public class TodoTasksFragment extends Fragment {

    private ExpandableListView expandableListView;
    private ExpandableToDoTaskAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        TodoList currentList = getArguments().getParcelable(TodoList.PARCELABLE_ID);

        View v = inflater.inflate(R.layout.todo_list_detailed, container, false);

        // initialize adapter and expandable listview
        adapter = new ExpandableToDoTaskAdapter(getActivity(), currentList.getTasks());
        TextView emptyView = (TextView) v.findViewById(R.id.tv_empty_view_no_tasks);
        expandableListView = (ExpandableListView) v.findViewById(R.id.exlv_tasks);

        expandableListView.setEmptyView(emptyView);
        expandableListView.setAdapter(adapter);

        // set toolbar title
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(currentList.getName());

        return v;
    }
}
