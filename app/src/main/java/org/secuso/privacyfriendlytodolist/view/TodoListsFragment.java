package org.secuso.privacyfriendlytodolist.view;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.BaseTodo;
import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.model.database.DBQueryHandler;
import org.secuso.privacyfriendlytodolist.view.dialog.ProcessTodoListDialog;

import java.util.ArrayList;

public class TodoListsFragment extends Fragment implements SearchView.OnQueryTextListener {

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        setRetainInstance(true);
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
                        if (newList instanceof TodoList) {
                            Toast.makeText(getContext(), getContext().getString(R.string.add_list_feedback, newList.getName()), Toast.LENGTH_SHORT).show();
                            todoLists.add((TodoList) newList);
                            adapter.updateList(todoLists); // run filter again
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
    public void onPause() {

        for (TodoList currentList : todoLists) {
            containerActivity.sendToDatabase(currentList);
        }

        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.i(TAG, "onCreateOptionsMenu");
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.search, menu);

        MenuItem searchItem = menu.findItem(R.id.ac_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        this.adapter.setQueryString(query);
        this.adapter.notifyDataSetChanged();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        this.adapter.setQueryString(query);
        this.adapter.notifyDataSetChanged();
        return false;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        final TodoList todoList = adapter.getToDoListFromPosition(adapter.getPosition());

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
                Toast.makeText(getContext(), getContext().getString(R.string.delete_list_feedback, todoList.getName()), Toast.LENGTH_SHORT).show();
                DBQueryHandler.deleteTodoList(containerActivity.getDbHelper().getWritableDatabase(), todoList);
                todoLists.remove(todoList);
                adapter.updateList(todoLists);
                adapter.notifyDataSetChanged();
                break;
            case R.id.show_description_list:

                String listDescription = todoList.getDescription();
                if(listDescription == null || "".equals(listDescription)) {
                    Toast.makeText(getContext(), getString(R.string.no_description_available), Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(listDescription).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // nothing todo
                        }
                    });
                    builder.show();
                }

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

        if (containerActivity.getSupportActionBar() != null)
            containerActivity.getSupportActionBar().setTitle(R.string.toolbar_title_main);
    }

    public void addList() {
        ProcessTodoListDialog addListDialog = new ProcessTodoListDialog(getActivity());
        addListDialog.setDialogResult(new TodoCallback() {

            @Override
            public void finish(BaseTodo newList) {
                if (newList instanceof TodoList) {
                    Toast.makeText(getContext(), getContext().getString(R.string.add_list_feedback, newList.getName()), Toast.LENGTH_SHORT).show();
                    todoLists.add((TodoList) newList);
                    adapter.updateList(todoLists); // run filter again
                    adapter.notifyDataSetChanged();
                    Log.i(TAG, "list added");
                }
            }
        });
        addListDialog.show();
    }


}
