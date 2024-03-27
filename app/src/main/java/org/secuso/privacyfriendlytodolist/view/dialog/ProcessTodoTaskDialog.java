/*
 This file is part of Privacy Friendly To-Do List.

 Privacy Friendly To-Do List is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.

 Privacy Friendly To-Do List is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Privacy Friendly To-Do List. If not, see <http://www.gnu.org/licenses/>.
 */

package org.secuso.privacyfriendlytodolist.view.dialog;

import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.Model;
import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.util.Helper;

import java.util.List;

/**
 * Created by Sebastian Lutz on 12.03.2018.
 *
 * This class creates a dialog that lets the user create/edit a task.
 */

public class ProcessTodoTaskDialog extends FullScreenDialog<ResultCallback<TodoTask>> {
    private TextView prioritySelector;
    private TextView deadlineTextView;
    private TextView reminderTextView;
    private TextView listSelector;
    private TextView dialogTitleNew;
    private TextView dialogTitleEdit;
    private TextView progressText;
    private TextView progressPercent;
    private RelativeLayout progress_layout;
    private SeekBar progressSelector;
    private EditText taskName;
    private EditText taskDescription;
    private TodoTask.Priority taskPriority = null;
    private Integer selectedListID = null;
    private final List<TodoList> lists;
    private int taskProgress = 0;
    private String name, description;
    private long deadline = -1;
    private long reminderTime = -1;

    private final TodoTask task;

    /**
     * @param context Gets used as context, ViewModelStoreOwner and LifecycleOwner.
     */
    public ProcessTodoTaskDialog(FragmentActivity context, List<TodoList> todoLists) {
        this(context, todoLists, null);
    }

    /**
     * @param context Gets used as context, ViewModelStoreOwner and LifecycleOwner.
     */
    public ProcessTodoTaskDialog(FragmentActivity context, List<TodoList> todoLists, TodoTask todoTask) {
        super(context, R.layout.add_task_dialog);

        lists = todoLists;

        initGui();

        if (null == todoTask){
            task = Model.createNewTodoTask();
            task.setCreated();
        } else {
            task = todoTask;
            task.setChanged();

            deadline = task.getDeadline();
            reminderTime = task.getReminderTime();
            taskName.setText(task.getName());
            taskDescription.setText(task.getDescription());
            prioritySelector.setText(Helper.priority2String(context, task.getPriority()));
            taskPriority = task.getPriority();
            progressSelector.setProgress(task.getProgress(false));
            if (task.getDeadline() <= 0)
                deadlineTextView.setText(context.getString(R.string.no_deadline));
            else
                deadlineTextView.setText(Helper.getDate(deadline));
            if (task.getReminderTime() <= 0)
                reminderTextView.setText(context.getString(R.string.reminder));
            else
                reminderTextView.setText(Helper.getDateTime(reminderTime));
        }
    }

    private void initGui() {

        // initialize textview that displays the selected priority
        prioritySelector = (TextView) findViewById(R.id.tv_new_task_priority);
        prioritySelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerForContextMenu(prioritySelector);
                openContextMenu(prioritySelector);
            }
        });
        prioritySelector.setOnCreateContextMenuListener(this);
        taskPriority = TodoTask.Priority.DEFAULT_VALUE;
        prioritySelector.setText(Helper.priority2String(getContext(), taskPriority));

        //initialize titles of the dialog
        dialogTitleNew = findViewById(R.id.dialog_title);
        dialogTitleEdit = findViewById(R.id.dialog_edit);


        //initialize textview that displays selected list
        listSelector = findViewById(R.id.tv_new_task_listchoose);
        listSelector.setOnClickListener(v -> {
            registerForContextMenu(listSelector);
            openContextMenu(listSelector);
        });
        listSelector.setOnCreateContextMenuListener(this);


        progressText = findViewById(R.id.tv_task_progress);
        progressPercent = findViewById(R.id.new_task_progress);
        progress_layout = findViewById(R.id.progress_relative);

        // initialize seekbar that allows to select the progress
        progressSelector = (SeekBar) findViewById(R.id.sb_new_task_progress);

        if (!hasAutoProgress()) {
            progressSelector.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    taskProgress = progress;
                    progressPercent.setText(progress + "%");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        } else {
            makeProgressGone();
        }


        // initialize buttons
        Button okayButton = (Button) findViewById(R.id.bt_new_task_ok);
        okayButton.setOnClickListener(v -> {
            String name = taskName.getText().toString();
            String description = taskDescription.getText().toString();

            if (name.isEmpty()) {
                Toast.makeText(getContext(), getContext().getString(R.string.todo_name_must_not_be_empty), Toast.LENGTH_SHORT).show();
            } /* else if (listName.equals(getContext().getString(R.string.click_to_choose))) {
                Toast.makeText(getContext(), getContext().getString(R.string.to_choose_list), Toast.LENGTH_SHORT).show();
            } */
            else {
                task.setName(name);
                task.setDescription(description);
                task.setDeadline(deadline);
                task.setPriority(taskPriority);
                task.setListId(selectedListID);
                task.setProgress(taskProgress);
                task.setReminderTime(reminderTime);
                callback.onFinish(task);
                ProcessTodoTaskDialog.this.dismiss();
            }
        });

        Button cancelButton = (Button) findViewById(R.id.bt_new_task_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ProcessTodoTaskDialog.this.dismiss();
            }
        });

        // initialize textviews to get deadline and reminder time
        deadlineTextView = (TextView) findViewById(R.id.tv_todo_list_deadline);
        deadlineTextView.setTextColor(okayButton.getCurrentTextColor());
        deadlineTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeadlineDialog deadlineDialog = new DeadlineDialog(getContext(), deadline);
                deadlineDialog.setDialogCallback(new DeadlineDialog.DeadlineCallback() {
                    @Override
                    public void setDeadline(long d) {
                        deadline = d;
                        deadlineTextView.setText(Helper.getDate(deadline));
                    }

                    @Override
                    public void removeDeadline() {
                        deadline = -1;
                        deadlineTextView.setText(getContext().getResources().getString(R.string.deadline));
                    }
                });
                deadlineDialog.show();
            }
        });

        reminderTextView = (TextView) findViewById(R.id.tv_todo_list_reminder);
        reminderTextView.setTextColor(okayButton.getCurrentTextColor());
        reminderTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReminderDialog reminderDialog = new ReminderDialog(getContext(), reminderTime, deadline);
                reminderDialog.setDialogCallback(new ReminderDialog.ReminderCallback() {
                    @Override
                    public void setReminder(long r) {

                        /*if(deadline == -1) {
                            Toast.makeText(getContext(), getContext().getString(R.string.set_deadline_before_reminder), Toast.LENGTH_SHORT).show();
                            return;
                        }*/

                        if(deadline != -1 && deadline < r) {
                            Toast.makeText(getContext(), getContext().getString(R.string.deadline_smaller_reminder), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        reminderTime = r;
                        reminderTextView.setText(Helper.getDateTime(reminderTime));
                    }

                    @Override
                    public void removeReminder() {
                        reminderTime = -1;
                        TextView reminderTextView = (TextView) findViewById(R.id.tv_todo_list_reminder);
                        reminderTextView.setText(getContext().getResources().getString(R.string.reminder));
                    }
                });
                reminderDialog.show();
            }
        });

        taskName = (EditText) findViewById(R.id.et_new_task_name);
        taskDescription = (EditText) findViewById(R.id.et_new_task_description);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        switch (v.getId()){
            case R.id.tv_new_task_priority:
                menu.setHeaderTitle(R.string.select_priority);
                for (TodoTask.Priority priority : TodoTask.Priority.getEntries()) {
                    menu.add(Menu.NONE, priority.ordinal(), Menu.NONE, Helper.priority2String(getContext(), priority));
                }
                break;

            case R.id.tv_new_task_listchoose:
                menu.setHeaderTitle(R.string.select_list);
                menu.add(Menu.NONE, -1, Menu.NONE, R.string.select_no_list);
                for (int i = 0; i < lists.size(); ++i) {
                    TodoList todoList = lists.get(i);
                    // Add offset so that IDs are non-overlapping with priority-IDs
                    menu.add(Menu.NONE, TodoTask.Priority.LENGTH + i, Menu.NONE, todoList.getName());
                }
            break;
        }
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() >= 0 && item.getItemId() < TodoTask.Priority.LENGTH) {
            taskPriority = TodoTask.Priority.getEntries().get(item.getItemId());
            prioritySelector.setText(Helper.priority2String(getContext(), taskPriority));
        } else {
            final int todoListIndex = item.getItemId() - TodoTask.Priority.LENGTH;
            TodoList todoList = null;
            if (todoListIndex >= 0 && todoListIndex < lists.size()) {
                todoList = lists.get(todoListIndex);
            }
            setListSelector(todoList);
        }

        return super.onMenuItemSelected(featureId, item);
    }

    //change the dialog title from "new task" to "edit task"
    public void titleEdit(){
        dialogTitleNew.setVisibility(View.GONE);
        dialogTitleEdit.setVisibility(View.VISIBLE);
    }

    //sets the textview either to list name in context or if no context to default
    public void setListSelector(Integer todoListId) {
        TodoList todoList = null;
        if (null != todoListId) {
            for (TodoList currentTodoList : lists) {
                if (currentTodoList.getId() == todoListId) {
                    todoList = currentTodoList;
                    break;
                }
            }
        }
        setListSelector(todoList);
    }

    private void setListSelector(TodoList todoList) {
        if (null != todoList) {
            selectedListID = todoList.getId();
            listSelector.setText(todoList.getName());
        } else {
            selectedListID = null;
            listSelector.setText(getContext().getString(R.string.click_to_choose));
        }
    }

    private boolean hasAutoProgress() {
        //automatic-progress enabled?
        if (!PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("pref_progress", false))
            return false;
        return true;
    }

    //Make progress-selectionbar disappear
    private void makeProgressGone() {
        progress_layout.setVisibility(View.GONE);
           /* progressSelector.setVisibility(View.INVISIBLE);
            progressPercent.setVisibility(View.INVISIBLE);
            progressText.setVisibility(View.INVISIBLE); */
    }
}
