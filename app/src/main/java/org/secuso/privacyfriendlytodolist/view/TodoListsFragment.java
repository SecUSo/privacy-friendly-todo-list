package org.secuso.privacyfriendlytodolist.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.BaseTodo;
import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.model.TodoSubTask;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.model.database.DBQueryHandler;
import org.secuso.privacyfriendlytodolist.model.database.DatabaseHelper;
import org.secuso.privacyfriendlytodolist.view.dialog.ProcessTodoListDialog;

import java.util.ArrayList;

public class TodoListsFragment extends Fragment {

    private final static String TAG = TodoListsFragment.class.getSimpleName();

    private TodoRecyclerView mRecyclerView;
    private TodoRecyclerView.LayoutManager mLayoutManager;
    private TodoListAdapter adapter;
    private MainActivity containerActivity;
    private ArrayList<TodoList> todoLists;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        containerActivity = (MainActivity)getActivity();
        todoLists = containerActivity.getTodoLists(false);
        View v = inflater.inflate(R.layout.fragment_todo_lists, container, false);

        guiSetup(v);

        return v;
    }

    private void guiSetup(View rootView) {

        // initialize recycler view
        mRecyclerView = (TodoRecyclerView) rootView.findViewById(R.id.rv_todo_lists);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        adapter = new TodoListAdapter(getActivity(), todoLists);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setEmptyView(rootView.findViewById(R.id.tv_rv_empty_view));
        registerForContextMenu(mRecyclerView);

        // floating action button setup
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab_new_list);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            ProcessTodoListDialog addListDialog = new ProcessTodoListDialog(getActivity());
            addListDialog.setDialogResult(new TodoCallback() {

                @Override
                public void finish(BaseTodo newList) {
                if(newList instanceof TodoList) {
                    todoLists.add((TodoList) newList);
                    adapter.notifyDataSetChanged();
                    Log.i(TAG, "list added");
                }
                }
            });
            addListDialog.show();
            }
        });

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        final int pos = adapter.getPosition();
        final TodoList todoList = todoLists.get(todoLists.size()-1-pos);

        switch(item.getItemId()) {
            case R.id.change_list:
                ProcessTodoListDialog addListDialog = new ProcessTodoListDialog(getActivity(), todoList);
                addListDialog.setDialogResult(new TodoCallback() {

                    @Override
                    public void finish(BaseTodo alterdList) {
                        if(alterdList instanceof TodoList) {
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
                addListDialog.show();
                break;
            case R.id.delete_list:

                DBQueryHandler.deleteTodoList(containerActivity.getDbHelper().getWritableDatabase(), todoList);
                todoLists.remove(todoList);
                adapter.notifyDataSetChanged();
                break;
            default:
                throw new IllegalArgumentException("Invalid menu item selected.");
        }

        return super.onContextItemSelected(item);
    }


    @Override
    public void onResume() {
        super.onResume();

        todoLists = containerActivity.getTodoLists(true);
        adapter.updateList(todoLists);
        adapter.notifyDataSetChanged();

        containerActivity.getSupportActionBar().setTitle(R.string.toolbar_title_main);
    }
}
