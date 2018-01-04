package org.secuso.privacyfriendlytodolist.view;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
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
import org.secuso.privacyfriendlytodolist.model.Helper;
import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.model.TodoSubTask;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.model.Tuple;
import org.secuso.privacyfriendlytodolist.model.database.DBQueryHandler;
import org.secuso.privacyfriendlytodolist.view.dialog.ProcessTodoSubTaskDialog;
import org.secuso.privacyfriendlytodolist.view.dialog.ProcessTodoTaskDialog;

import java.util.ArrayList;

public class TodoTasksFragment extends Fragment implements SearchView.OnQueryTextListener {

    private static final String TAG = TodoTasksFragment.class.getSimpleName();

    // The fab is used to create new tasks. However, a task can only be created if the user is inside
    // a certain list. If he chose the "show all task" view, the option to create a new task is not available.
    public static final String SHOW_FLOATING_BUTTON = "SHOW_FAB";
    public static final String KEY = "fragment_selector_key";

    private ExpandableListView expandableListView;
    private ExpandableTodoTaskAdapter taskAdapter;

    private MainActivity containingActivity;

    private TodoList currentList;
    private ArrayList<TodoTask> todoTasks = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        containingActivity = (MainActivity) getActivity();

        if(containingActivity == null) {
            throw new RuntimeException("TodoTasksFragment could not find containing activity.");
        }


        boolean showFab = getArguments().getBoolean(TodoTasksFragment.SHOW_FLOATING_BUTTON);

        // This argument is only set if a dummy list is displayed (a mixture of tasks of different lists) or if
        // a list was selected by clicking on a notification. If the the user selects a list explicitly by clicking on it
        // the list object is instantly available and can be obtained using the method "getClickedList()"
        int selectedListID = getArguments().getInt(TodoList.UNIQUE_DATABASE_ID, -1);
        boolean showListNamesOfTasks = false;
        if(selectedListID >= 0) {
            currentList = containingActivity.getListByID(selectedListID); // MainActivity was started after a notification click
            Log.i(TAG, "List was loaded that was requested by a click on a notification.");
        } else if(selectedListID == TodoList.DUMMY_LIST_ID) {
            currentList = containingActivity.getDummyList();
            showListNamesOfTasks = true;
            Log.i(TAG, "Dummy list was loaded.");
        } else {
            currentList = containingActivity.getClickedList(); // get clicked list
            Log.i(TAG, "Clicked list was loaded.");
        }


        View v = inflater.inflate(R.layout.fragment_todo_tasks, container, false);

        if(currentList != null) {
            todoTasks = currentList.getTasks();

            initExListViewGUI(v);
            initFab(v, showFab);

            taskAdapter.setListNames(showListNamesOfTasks);

            // set toolbar title
            if(((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(currentList.getName());
            }

        } else {
            Log.d(TAG, "Cannot identify selected list.");
        }

        return v;
    }

    private void initFab(View rootView, boolean showFab) {

        FloatingActionButton optionFab = (FloatingActionButton) rootView.findViewById(R.id.fab_new_task);

        if(showFab) {
            optionFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                ProcessTodoTaskDialog addListDialog = new ProcessTodoTaskDialog(getActivity());
                addListDialog.setDialogResult(new TodoCallback() {
                    @Override
                    public void finish(BaseTodo b) {
                    if (b instanceof TodoTask) {
                        todoTasks.add((TodoTask) b);
                        taskAdapter.notifyDataSetChanged();
                    }
                    }
                });
                addListDialog.show();
                }
            });
        } else
            optionFab.setVisibility(View.GONE);
    }

    private void initExListViewGUI(View v) {

        taskAdapter = new ExpandableTodoTaskAdapter(getActivity(), todoTasks);
        TextView emptyView = (TextView) v.findViewById(R.id.tv_empty_view_no_tasks);
        expandableListView = (ExpandableListView) v.findViewById(R.id.exlv_tasks);
        expandableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int groupPosition = ExpandableListView.getPackedPositionGroup(id);

                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {

                    int childPosition = ExpandableListView.getPackedPositionChild(id);
                    taskAdapter.setLongClickedSubTaskByPos(groupPosition, childPosition);
                } else {
                    taskAdapter.setLongClickedTaskByPos(groupPosition);
                }

                return false;
            }

        });



        // react when task expands
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

            Object vh = v.getTag();

            if(vh != null && vh instanceof ExpandableTodoTaskAdapter.GroupTaskViewHolder) {

                ExpandableTodoTaskAdapter.GroupTaskViewHolder viewHolder = (ExpandableTodoTaskAdapter.GroupTaskViewHolder) vh;

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

        ExpandableListView.ExpandableListContextMenuInfo info =
                (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        MenuInflater inflater = getActivity().getMenuInflater();
        menu.setHeaderView(Helper.getMenuHeader(getContext(), getContext().getString(R.string.select_option)));

        // context menu for child items
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            inflater.inflate(R.menu.todo_subtask_long_click, menu);
        } else { // context menu for group items
            inflater.inflate(R.menu.todo_task_long_click, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        final Tuple<TodoTask, TodoSubTask> longClickedTodo = taskAdapter.getLongClickedTodo();
        int affectedRows;

        switch(item.getItemId()) {
            case R.id.change_subtask:

                ProcessTodoSubTaskDialog dialog = new ProcessTodoSubTaskDialog(containingActivity, longClickedTodo.getRight());
                dialog.setDialogResult(new TodoCallback() {
                    @Override
                    public void finish(BaseTodo b) {
                    if(b instanceof TodoTask) {
                        taskAdapter.notifyDataSetChanged();
                        Log.i(TAG, "subtask altered");
                    }
                    }
                });
                dialog.show();
                break;

            case R.id.delete_subtask:
                affectedRows = DBQueryHandler.deleteTodoSubTask(containingActivity.getDbHelper().getWritableDatabase(), longClickedTodo.getRight());
                longClickedTodo.getLeft().getSubTasks().remove(longClickedTodo.getRight());
                if(affectedRows == 1)
                    Toast.makeText(getContext(), getString(R.string.subtask_removed), Toast.LENGTH_SHORT).show();
                else
                    Log.d(TAG, "Subtask was not removed from the database. Maybe it was not added beforehand (then this is no error)?");
                taskAdapter.notifyDataSetChanged();
                break;
            case R.id.change_task:
                ProcessTodoTaskDialog editTaskDialog = new ProcessTodoTaskDialog(getActivity(), longClickedTodo.getLeft());
                editTaskDialog.setDialogResult(new TodoCallback() {

                    @Override
                    public void finish(BaseTodo alteredTask) {
                    if(alteredTask instanceof TodoTask) {
                        taskAdapter.notifyDataSetChanged();
                    }
                    }
                });
                editTaskDialog.show();
                break;
            case R.id.delete_task:
                affectedRows = DBQueryHandler.deleteTodoTask(containingActivity.getDbHelper().getWritableDatabase(), longClickedTodo.getLeft());
                todoTasks.remove(longClickedTodo.getLeft());
                if(affectedRows == 1)
                    Toast.makeText(getContext(), getString(R.string.task_removed), Toast.LENGTH_SHORT).show();
                else
                    Log.d(TAG, "Task was not removed from the database. Maybe it was not added beforehand (then this is no error)?");
                taskAdapter.notifyDataSetChanged();
                break;
            default:
                throw new IllegalArgumentException("Invalid menu item selected.");
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        setRetainInstance(true);
    }

    @Override
    public void onPause() {

        // write new tasks to the database
        for(int i=0; i<todoTasks.size(); i++) {
            TodoTask currentTask = todoTasks.get(i);

            // If a dummy list is displayed, its id must not be assigned to the task.
            if(!currentList.isDummyList())
                currentTask.setListId(currentList.getId()); // crucial step to not lose the connection to the list

            boolean dbChanged = containingActivity.sendToDatabase(currentTask);
            if(dbChanged)
                containingActivity.notifyReminderService(currentTask);

            // write subtasks to the database
            for(TodoSubTask subTask : currentTask.getSubTasks()) {
                subTask.setTaskId(currentTask.getId()); // crucial step to not lose the connection to the task
                containingActivity.sendToDatabase(subTask);
            }
        }

        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.main, menu);
        inflater.inflate(R.menu.search, menu);
        inflater.inflate(R.menu.add_list, menu);

        MenuItem searchItem = menu.findItem(R.id.ac_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
    }

    private void collapseAll()
    {
        // collapse all elements on view change.
        // the expandable list view keeps the expanded indices, so other items
        // get expanded, when they get the old expanded index
        int groupCount = taskAdapter.getGroupCount();
        for(int i = 0; i < groupCount; i++)
            expandableListView.collapseGroup(i);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        collapseAll();
        taskAdapter.setQueryString(query);
        taskAdapter.notifyDataSetChanged();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        collapseAll();
        taskAdapter.setQueryString(query);
        taskAdapter.notifyDataSetChanged();
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        boolean checked;
        ExpandableTodoTaskAdapter.SortTypes sortType;

        collapseAll();

        switch (item.getItemId()) {
            case R.id.ac_show_all_tasks:
                taskAdapter.setFilter(ExpandableTodoTaskAdapter.Filter.ALL_TASKS);
                taskAdapter.notifyDataSetChanged();
                return true;
            case R.id.ac_show_open_tasks:
                taskAdapter.setFilter(ExpandableTodoTaskAdapter.Filter.OPEN_TASKS);
                taskAdapter.notifyDataSetChanged();
                return true;
            case R.id.ac_show_completed_tasks:
                taskAdapter.setFilter(ExpandableTodoTaskAdapter.Filter.COMPLETED_TASKS);
                taskAdapter.notifyDataSetChanged();
                return true;
            case R.id.ac_group_by_prio:
                checked = !item.isChecked();
                item.setChecked(checked);
                sortType = ExpandableTodoTaskAdapter.SortTypes.PRIORITY;
                break;
            case R.id.ac_sort_by_deadline:
                checked = !item.isChecked();
                item.setChecked(checked);
                sortType = ExpandableTodoTaskAdapter.SortTypes.DEADLINE;
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
