package org.secuso.privacyfriendlytodolist.view;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
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

public class TodoTasksFragment extends Fragment {

    private static final String TAG = TodoTasksFragment.class.getSimpleName();

    // The fab is used to create new tasks. However, a task can only be created if the user is inside
    // a certain list. If he chose the "show all task" view, the option to create a new task is not available.
    public static final String SHOW_FLOATING_BUTTON = "SHOW_FAB";


    private ExpandableListView expandableListView;
    private ExpandableTodoTaskAdapter taskAdapter;

    private MainActivity containingActivity;

    private TodoList currentList;
    private ArrayList<TodoTask> todoTasks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        containingActivity = (MainActivity) getActivity();



        if(containingActivity == null) {
            throw new RuntimeException("TodoTasksFragment could not find containing activity.");
        }


        boolean showFab = getArguments().getBoolean(TodoTasksFragment.SHOW_FLOATING_BUTTON);

        long currentListID = getArguments().getLong(TodoList.UNIQUE_DATABASE_ID);
        currentList = containingActivity.getListByID(currentListID);
        todoTasks = currentList.getTasks();

        View v = inflater.inflate(R.layout.fragment_todo_tasks, container, false);

        initExListViewGUI(v);
        initFab(v, showFab);

        // set toolbar title
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(currentList.getName());

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

                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    int childPosition = ExpandableListView.getPackedPositionChild(id);
                    taskAdapter.setLongClickedSubTaskByPos(groupPosition, childPosition);
                } else {
                    taskAdapter.setLongClickedTaskByPos(position);
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
        menu.setHeaderView(Helper.getMenuHeader(getContext()));
        // context menu for child items
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {

            int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
            int child = ExpandableListView.getPackedPositionChild(info.packedPosition);
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
                if(affectedRows == 1) {
                    longClickedTodo.getLeft().getSubTasks().remove(longClickedTodo.getRight());
                    taskAdapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), getString(R.string.subtask_removed), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), getString(R.string.error_removing_subtask), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.change_task:
                ProcessTodoTaskDialog editTaskDialog = new ProcessTodoTaskDialog(getActivity(), longClickedTodo.getLeft());
                editTaskDialog.setDialogResult(new TodoCallback() {

                    @Override
                    public void finish(BaseTodo alteredTask) {
                        if(alteredTask instanceof TodoTask) {
                            taskAdapter.notifyDataSetChanged();
                            Log.i(TAG, "task altered");
                        }
                    }
                });
                editTaskDialog.show();
                break;
            case R.id.delete_task:
                affectedRows = DBQueryHandler.deleteTodoTask(containingActivity.getDbHelper().getWritableDatabase(), longClickedTodo.getLeft());
                if(affectedRows == 1) {
                    todoTasks.remove(longClickedTodo.getLeft());
                    taskAdapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), getString(R.string.task_removed), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), getString(R.string.error_removing_task), Toast.LENGTH_SHORT).show();
                }
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
    }

    @Override
    public void onPause() {
        super.onPause();

        // write new tasks to the database
        for(int i=0; i<todoTasks.size(); i++) {
            TodoTask currentTask = todoTasks.get(i);

            // write subtasks to the database
            for(TodoSubTask subTask : currentTask.getSubTasks()) {
                long subTaskID = DBQueryHandler.saveTodoSubTaskInDb(containingActivity.getDbHelper().getWritableDatabase(), subTask);
                subTask.setId(subTaskID);
            }

            // If a dummy list is displayed, its id must not be assigned to the task.
            if(!currentList.isDummyList())
                currentTask.setListId(currentList.getId());
                // currentTask.setPositionInList(i); // TODO improve it to set a custom position

            long id = DBQueryHandler.saveTodoTaskInDb(containingActivity.getDbHelper().getWritableDatabase(), currentTask);
            if(id == -1)
                Log.e(TAG, getString(R.string.list_to_db_error));
            else if(id == -2)
                Log.i(TAG, getString(R.string.no_changes_in_db));
            else
                currentTask.setId(id); // if the task already had had an id, the id is overwritten with the old value
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
        ExpandableTodoTaskAdapter.SortTypes sortType;

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
