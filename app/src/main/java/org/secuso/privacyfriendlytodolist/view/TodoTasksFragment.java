package org.secuso.privacyfriendlytodolist.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.TodoList;

/**
 * Created by dominik on 24.05.16.
 */
public class TodoTasksFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        TodoList currentList = getArguments().getParcelable(TodoList.PARCELABLE_ID);

        // set toolbar title
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(currentList.getName());

        return inflater.inflate(R.layout.todo_list_detailed, container, false);
    }
}
