package org.secuso.privacyfriendlytodolist.view.dialog;

import android.content.Context;
import android.provider.SyncStateContract;
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
import org.secuso.privacyfriendlytodolist.model.database.DBQueryHandler;
import org.secuso.privacyfriendlytodolist.view.TodoCallback;

public class ProcessTodoTaskDialog extends FullScreenDialog {

    private TextView prioritySelector;
    private TextView deadlineTextView;
    private TextView reminderTextView;
    private SeekBar progressSelector;
    private EditText taskName;
    private EditText taskDescription;
    private TodoTask.Priority taskPriority = null;
    private int taskProgress = 0;
    private String name, description;
    private long deadline = -1;
    private long reminderTime = -1;

    private TodoTask.Priority defaultPriority = TodoTask.Priority.MEDIUM;

    private TodoTask task;


    public ProcessTodoTaskDialog(final Context context) {
        super(context, R.layout.add_task_dialog);

        initGui();
        task = new TodoTask();
        task.setDbState(DBQueryHandler.ObjectStates.INSERT_TO_DB);
    }

    public ProcessTodoTaskDialog(Context context, TodoTask task) {
        super(context, R.layout.add_task_dialog);

        initGui();
        task.setDbState(DBQueryHandler.ObjectStates.UPDATE_DB);
        deadline = task.getDeadline();
        reminderTime = task.getReminderTime();
        taskName.setText(task.getName());
        taskDescription.setText(task.getDescription());
        prioritySelector.setText(Helper.priority2String(context, task.getPriority()));
        progressSelector.setProgress(task.getProgress());
        if(task.getDeadline() <= 0)
            deadlineTextView.setText(context.getString(R.string.no_deadline));
        else
            deadlineTextView.setText(Helper.getDate(deadline));
        if(task.getReminderTime() <= 0)
            reminderTextView.setText(context.getString(R.string.reminder));
        else
            reminderTextView.setText(Helper.getDateTime(reminderTime));

        this.task = task;
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
        taskPriority = defaultPriority;
        prioritySelector.setText(Helper.priority2String(getContext(), taskPriority));

        // initialize seekbar that allows to select the progress
        final TextView selectedProgress = (TextView) findViewById(R.id.new_task_progress);
        progressSelector = (SeekBar) findViewById(R.id.sb_new_task_progress);
        progressSelector.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

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

                String name = taskName.getText().toString();
                String description = taskDescription.getText().toString();

                if(name.equals("")) {
                    Toast.makeText(getContext(), getContext().getString(R.string.todo_name_must_not_be_empty), Toast.LENGTH_SHORT).show();
                } else {

                    task.setName(name);
                    task.setDescription(description);
                    task.setDeadline(deadline);
                    task.setPriority(taskPriority);
                    task.setProgress(taskProgress);
                    task.setReminderTime(reminderTime);
                    callback.finish(task);
                    ProcessTodoTaskDialog.this.dismiss();
                }
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
        deadlineTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeadlineDialog deadlineDialog = new DeadlineDialog(getContext(), deadline);
                deadlineDialog.setCallback(new DeadlineDialog.DeadlineCallback() {
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
        reminderTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReminderDialog reminderDialog = new ReminderDialog(getContext(), reminderTime, deadline);
                reminderDialog.setCallback(new ReminderDialog.ReminderCallback() {
                    @Override
                    public void setReminder(long r) {

                        if(deadline == -1) {
                            Toast.makeText(getContext(), getContext().getString(R.string.set_deadline_before_reminder), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if(deadline < r) {
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

        menu.setHeaderView(Helper.getMenuHeader(getContext(), getContext().getString(R.string.select_priority)));
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
}
