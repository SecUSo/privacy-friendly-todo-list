package org.secuso.privacyfriendlytodolist.view;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ListView;

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

    float historicX = Float.NaN, historicY = Float.NaN;
    static final int DELTA = 50;
    enum Direction {LEFT, RIGHT;}

    private ArrayList<TodoList> todoLists = new ArrayList<>();
    private TodoRecyclerView mRecyclerView;
    private TodoRecyclerView.LayoutManager mLayoutManager;
    private TodoListAdapter adapter;
    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_todo_lists, container, false);
        dbHelper = new DatabaseHelper(getActivity());

        todoLists = DBQueryHandler.getAllToDoLists(dbHelper.getReadableDatabase());
        guiSetup(inflater, v);

        return v;
    }

    private void guiSetup(LayoutInflater inflater, View rootView) {

        // initialize recycler view
        mRecyclerView = (TodoRecyclerView) rootView.findViewById(R.id.rv_todo_lists);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        adapter = new TodoListAdapter(getActivity(), todoLists);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setEmptyView(rootView.findViewById(R.id.tv_rv_empty_view));
        /*mRecyclerView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                Log.i("TodoListsFragment", event.getAction() + " at "+ event.getX() + "x"+event.getY());
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        historicX = event.getX();
                        historicY = event.getY();
                        break;

                    case MotionEvent.ACTION_UP:
                        if (event.getX() - historicX < -DELTA)
                        {
                            Log.i("TodoListsFragment", "slide left");
                            return true;
                        }
                        else if (event.getX() - historicX > DELTA)
                        {
                            Log.i("TodoListsFragment", "slide right");
                            return true;
                        } break;
                    default: return false;
                }
                return false;
            }
        });*/
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
                            todoLists.set(pos, todoList);
                            adapter.notifyDataSetChanged();
                            Log.i(TAG, "list altered");
                        }
                    }
                });
                addListDialog.show();
                break;
            case R.id.delete_list:

                DBQueryHandler.deleteTodoList(dbHelper.getWritableDatabase(), todoList);
                todoLists.remove(todoList);
                adapter.notifyDataSetChanged();
                break;
            default:
                throw new IllegalArgumentException("Invalid menu item selected.");
        }

        return super.onContextItemSelected(item);
    }


    @Override
    public void onPause() {
        super.onPause();

        // write new data back to the database
        for(TodoList currentList : todoLists) {
            long id = DBQueryHandler.saveTodoListInDb(dbHelper.getWritableDatabase(), currentList);
            if(id == -1)
                Log.e(TAG, getString(R.string.list_to_db_error));
            else if(id == DBQueryHandler.NO_CHANGES)
                Log.i(TAG, getString(R.string.no_changes_in_db));
            else
                currentList.setId(id);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.toolbar_title_main);
    }

    private void prepareData() { /*
        ArrayList<TodoSubTask> s1 = new ArrayList<TodoSubTask>();
        s1.add(new TodoSubTask(0, "Sub 1", false));
        s1.add(new TodoSubTask(1, "Sub 2", false));
        s1.add(new TodoSubTask(2, "Sub 3", false));

        ArrayList<TodoTask> tlist1 = new ArrayList<TodoTask>();
        ArrayList<TodoTask> tlist2 = new ArrayList<TodoTask>();

        // (int id, int listPosition, String title, String description, boolean done, int progress, int priority, int deadline, int reminderTime)

        TodoTask t1 = new TodoTask("Task 1", "Das ist eine Beschreibung", 0, TodoTask.Priority.LOW, 1465381890, 86400);
        TodoTask t2 = new TodoTask("Task 2", "", 0, TodoTask.Priority.HIGH, -1, 0);
        TodoTask t3 = new TodoTask("Task 3", "Das ist eine Beschreibung", 7, TodoTask.Priority.MEDIUM, 1464030736, 0);
        TodoTask t4 = new TodoTask("Task 4", "Das ist eine Beschreibung", 2, TodoTask.Priority.MEDIUM, 1478860290, 0);
        TodoTask t5 = new TodoTask("Task 5", "", 5, TodoTask.Priority.HIGH, -1,  0);
        TodoTask t6 = new TodoTask("Task 6", "", 5, TodoTask.Priority.MEDIUM, -1, 0);

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

        todoLists = new ArrayList<TodoList>();
        todoLists.add(l1);
        todoLists.add(l2);
        todoLists.add(l3);
        todoLists.add(l4);
        todoLists.add(l5);
        */
    }

}
