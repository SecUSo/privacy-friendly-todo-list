package org.secuso.privacyfriendlytodolist.view;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.BaseTodo;
import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.model.database.DBQueryHandler;
import org.secuso.privacyfriendlytodolist.model.database.DatabaseHelper;
import org.secuso.privacyfriendlytodolist.view.dialog.AddTaskDialog;

import java.util.ArrayList;

public class TodoTasksFragment extends Fragment {

    private static final String TAG = TodoTasksFragment.class.getSimpleName();

    private ExpandableListView expandableListView;
    private ExpandableToDoTaskAdapter taskAdapter;
    private ArrayList<TodoTask> todoTasks;

    private TodoList currentList;
    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        currentList = getArguments().getParcelable(TodoList.PARCELABLE_ID);
        todoTasks = currentList.getTasks();
        dbHelper = new DatabaseHelper(getActivity());

        View v = inflater.inflate(R.layout.fragment_todo_tasks, container, false);

        initExListViewGUI(v);
        initFab(v);

        // set toolbar title
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(currentList.getName());

        return v;
    }

    private void initFab(View rootView) {
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab_new_task);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            AddTaskDialog addListDialog = new AddTaskDialog(getActivity());
            addListDialog.setDialogResult(new TodoCallback() {
                @Override
                public void finish(BaseTodo b) {
                    if(b instanceof TodoTask) {
                        todoTasks.add((TodoTask)b);
                        taskAdapter.notifyDataSetChanged();
                    }
                }
            });
            addListDialog.show();
            }
        });
    }

    private void initExListViewGUI(View v) {

        taskAdapter = new ExpandableToDoTaskAdapter(getActivity(), todoTasks);
        TextView emptyView = (TextView) v.findViewById(R.id.tv_empty_view_no_tasks);
        expandableListView = (ExpandableListView) v.findViewById(R.id.exlv_tasks);

        // react when task expands
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

            Object vh = v.getTag();

            if(vh != null && vh instanceof ExpandableToDoTaskAdapter.GroupTaskViewHolder) {

                ExpandableToDoTaskAdapter.GroupTaskViewHolder viewHolder = (ExpandableToDoTaskAdapter.GroupTaskViewHolder) vh;

                if(viewHolder.seperator.getVisibility() == View.GONE) {
                    viewHolder.seperator.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.seperator.setVisibility(View.GONE);
                }
            }

            return false;

            }
        });

        // long click to delete or change a task
        registerForContextMenu(expandableListView);

        expandableListView.setEmptyView(emptyView);
        expandableListView.setAdapter(taskAdapter);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {

        menu.setHeaderTitle(R.string.select_option);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.todo_task_long_click, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        long posClickedTask = -1;

        switch(item.getItemId()) {
            case R.id.modify_task:
                Toast.makeText(getContext(), "change", Toast.LENGTH_SHORT).show();
                break;
            case R.id.delete_task:
                posClickedTask = taskAdapter.getLongClickTaskId();
                TodoTask taskToDelete = getTaskById(posClickedTask);
                DBQueryHandler.deleteTodoTask(dbHelper.getWritableDatabase(), taskToDelete);
                todoTasks.remove(taskToDelete);
                taskAdapter.notifyDataSetChanged();
                Toast.makeText(getContext(), getString(R.string.task_removed), Toast.LENGTH_SHORT).show();
                break;
            default:
                throw new IllegalArgumentException("Invalid menu item selected.");
        }

        return super.onContextItemSelected(item);
    }

    private TodoTask getTaskById(long id) {
        for(TodoTask task : todoTasks)
            if(task.getId() == id)
                return task;

        throw new IllegalArgumentException(getString(R.string.unknown_task_id) + " " + String.valueOf(id));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        // write new tasks to the database
        for(int i=0; i<todoTasks.size(); i++) {
            TodoTask currentTask = todoTasks.get(i);
            if(currentTask.isChanged()) {
                currentTask.setListId(currentList.getId());
                currentTask.setPositionInList(i);
                long id = DBQueryHandler.insertNewTask(dbHelper.getWritableDatabase(), currentTask);
                if(id == -1)
                    Log.e(TAG, getString(R.string.list_to_db_error));
                else
                    currentTask.setId(id);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        boolean checked;
        ExpandableToDoTaskAdapter.SortTypes sortType;

        switch (item.getItemId()) {
            case R.id.ac_show_all_tasks:
                taskAdapter.setFilter(ExpandableToDoTaskAdapter.Filter.ALL_TASKS);
                taskAdapter.notifyDataSetChanged();
                return true;
            case R.id.ac_show_open_tasks:
                taskAdapter.setFilter(ExpandableToDoTaskAdapter.Filter.OPEN_TASKS);
                taskAdapter.notifyDataSetChanged();
                return true;
            case R.id.ac_show_completed_tasks:
                taskAdapter.setFilter(ExpandableToDoTaskAdapter.Filter.COMPLETED_TASKS);
                taskAdapter.notifyDataSetChanged();
                return true;
            case R.id.ac_group_by_prio:
                checked = !item.isChecked();
                item.setChecked(checked);
                sortType = ExpandableToDoTaskAdapter.SortTypes.PRIORITY;
                break;
            case R.id.ac_sort_by_deadline:
                checked = !item.isChecked();
                item.setChecked(checked);
                sortType = ExpandableToDoTaskAdapter.SortTypes.DEADLINE;
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        if(checked)
            taskAdapter.addSortCondition(sortType);
        else
            taskAdapter.removeSortCondition(sortType);
        taskAdapter.notifyDataSetChanged();

        return true;
    }
}
