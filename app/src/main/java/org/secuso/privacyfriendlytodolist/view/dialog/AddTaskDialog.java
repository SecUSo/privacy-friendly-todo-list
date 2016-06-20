package org.secuso.privacyfriendlytodolist.view.dialog;

import android.content.Context;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.Helper;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.view.TodoCallback;

public class AddTaskDialog extends FullScreenDialog {

    final private TextView prioritySelector;
    final private SeekBar progressSelector;
    private EditText taskName;
    private EditText taskDescription;
    private TodoTask.Priority taskPriority = null;
    private int taskProgress = 0;
    private String name, description;
    private long deadline = -1;
    private long reminderTime = -1;

    private TodoTask.Priority defaultPriority = TodoTask.Priority.MEDIUM;

    private TodoCallback taskToFragCallback;


    public AddTaskDialog(final Context context) {
        super(context, R.layout.add_task_dialog);

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
        taskPriority = defaultPriority;
        prioritySelector.setText(Helper.priority2String(getContext(), taskPriority));

        // initialize seekbar that allows to select the progress
        final TextView selectedProgress = (TextView) findViewById(R.id.new_task_progress);
        progressSelector = (SeekBar) findViewById(R.id.sb_new_task_progress);
        progressSelector.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int progress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                taskProgress = progress;
                selectedProgress.setText(String.valueOf(progress) + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // initialize buttons
        Button okayButton = (Button) findViewById(R.id.bt_new_task_ok);
        okayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = parseAndSaveNewTask();
                if(msg == null) {
                    TodoTask newTask = new TodoTask(name, description, taskProgress, taskPriority, deadline, reminderTime);
                    newTask.setWriteDbFlag();
                    taskToFragCallback.finish(newTask);
                    AddTaskDialog.this.dismiss();
                } else {
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button cancelButton = (Button) findViewById(R.id.bt_new_task_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AddTaskDialog.this.dismiss();
            }
        });

        // initialize textviews to get deadline and reminder time
        TextView tvDeadline = (TextView) findViewById(R.id.tv_todo_list_deadline);
        tvDeadline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeadlineDialog deadlineDialog = new DeadlineDialog(getContext(), deadline);
                deadlineDialog.setCallback(new DeadlineDialog.DeadlineCallback() {
                    @Override
                    public void setDeadline(long d) {
                        deadline = d;
                        TextView deadlineTextView = (TextView) findViewById(R.id.tv_todo_list_deadline);
                        deadlineTextView.setText(Helper.getDate(deadline));
                    }

                    @Override
                    public void removeDeadline() {
                        deadline = -1;
                        TextView deadlineTextView = (TextView) findViewById(R.id.tv_todo_list_deadline);
                        deadlineTextView.setText(getContext().getResources().getString(R.string.deadline));
                    }
                });
                deadlineDialog.show();
            }
        });

        TextView tvReminder = (TextView) findViewById(R.id.tv_todo_list_reminder);
        tvReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReminderDialog reminderDialog = new ReminderDialog(getContext(), reminderTime, deadline);
                reminderDialog.setCallback(new ReminderDialog.ReminderCallback() {
                    @Override
                    public void setReminder(long d) {
                        reminderTime = d;
                        TextView reminderTextView = (TextView) findViewById(R.id.tv_todo_list_reminder);
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


    private String parseAndSaveNewTask() {

        name = taskName.getText().toString();
        description = taskDescription.getText().toString();

        if(name.equals("") || name == null)
            return getContext().getResources().getString(R.string.no_task_name);

        if(taskPriority == null)
            return getContext().getResources().getString(R.string.no_task_priority);


        return null;
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        menu.setHeaderTitle(getContext().getString(R.string.select_priority));
        for (TodoTask.Priority prio : TodoTask.Priority.values()) {
            menu.add(Menu.NONE, prio.getValue(), Menu.NONE, Helper.priority2String(getContext(), prio));
        }

    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        for (TodoTask.Priority prio : TodoTask.Priority.values()) {
            if (item.getItemId() == prio.getValue()) {
                taskPriority = prio;
                prioritySelector.setText(Helper.priority2String(getContext(), taskPriority));
            }
        }

        return super.onMenuItemSelected(featureId, item);
    }

    public void setDialogResult(TodoCallback resultCallback) {
        taskToFragCallback = resultCallback;
    }


}
