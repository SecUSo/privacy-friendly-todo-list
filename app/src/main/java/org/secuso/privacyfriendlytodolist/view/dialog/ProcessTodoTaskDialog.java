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

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.Helper;
import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.model.database.DBQueryHandler;
import org.secuso.privacyfriendlytodolist.model.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

import static org.secuso.privacyfriendlytodolist.model.TodoList.DUMMY_LIST_ID;

/**
 * This class creates a dialog that lets the user create/edit a task.
 */

public class ProcessTodoTaskDialog extends FullScreenDialog {

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
    private int selectedListID;
    private List<TodoList> lists = new ArrayList<>();
    private DatabaseHelper dbHelper;
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
        task.setCreated();
        //task.setDbState(DBQueryHandler.ObjectStates.INSERT_TO_DB);
    }


    public ProcessTodoTaskDialog(Context context, TodoTask task) {
        super(context, R.layout.add_task_dialog);

        initGui();
        task.setChanged();
        //task.setDbState(DBQueryHandler.ObjectStates.UPDATE_DB);
        deadline = task.getDeadline();
        reminderTime = task.getReminderTime();
        taskName.setText(task.getName());
        taskDescription.setText(task.getDescription());
        prioritySelector.setText(Helper.priority2String(context, task.getPriority()));
        taskPriority = task.getPriority();
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

        //initialize titles of the dialog
        dialogTitleNew = (TextView) findViewById(R.id.dialog_title);
        dialogTitleEdit = (TextView) findViewById(R.id.dialog_edit);


        //initialize textview that displays selected list
        listSelector = (TextView) findViewById (R.id.tv_new_task_listchoose);
        listSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerForContextMenu(listSelector);
                openContextMenu(listSelector);
            }
        });
        listSelector.setOnCreateContextMenuListener(this);


        progressText = (TextView) findViewById(R.id.tv_task_progress);
        progressPercent = (TextView) findViewById(R.id.new_task_progress);
        progress_layout = (RelativeLayout) findViewById(R.id.progress_relative);

        // initialize seekbar that allows to select the progress
        final TextView selectedProgress = (TextView) findViewById(R.id.new_task_progress);
        progressSelector = (SeekBar) findViewById(R.id.sb_new_task_progress);

        if (!hasAutoProgress()) {
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
        } else {
            makeProgressGone();
        }


        // initialize buttons
        Button okayButton = (Button) findViewById(R.id.bt_new_task_ok);
        okayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = taskName.getText().toString();
                String description = taskDescription.getText().toString();

                String listName = listSelector.getText().toString();

                if(name.equals("")) {
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
        deadlineTextView.setTextColor(okayButton.getCurrentTextColor());
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
        reminderTextView.setTextColor(okayButton.getCurrentTextColor());
        reminderTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReminderDialog reminderDialog = new ReminderDialog(getContext(), reminderTime, deadline);
                reminderDialog.setCallback(new ReminderDialog.ReminderCallback() {
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
                for (TodoTask.Priority prio : TodoTask.Priority.values()) {
                    menu.add(Menu.NONE, prio.getValue(), Menu.NONE, Helper.priority2String(getContext(), prio));

                }
                break;

            case R.id.tv_new_task_listchoose:
                menu.setHeaderTitle(R.string.select_list);
                updateLists();
                for (TodoList tl : lists){
                    //+3 so that IDs are non-overlapping with prio-IDs
                    menu.add(Menu.NONE, tl.getId()+3, Menu.NONE, tl.getName());
                }
            break;
        }
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int numValues = TodoTask.Priority.values().length;
        if (item != null && item.getItemId() < numValues && item.getItemId() >= 0 ) {
            taskPriority = TodoTask.Priority.values()[item.getItemId()];
            prioritySelector.setText(Helper.priority2String(getContext(), taskPriority));
        }

        for (TodoList tl : lists){
            if (item.getItemId()-3 == tl.getId()){
                this.selectedListID = tl.getId();
                listSelector.setText(tl.getName());
            } else if (item.getTitle() == getContext().getString(R.string.to_choose_list)){
                this.selectedListID = -3;
            }
        }

        return super.onMenuItemSelected(featureId, item);
    }


    //updates the lists array
    public void updateLists(){
        dbHelper = DatabaseHelper.getInstance(getContext());
        lists = DBQueryHandler.getAllToDoLists(dbHelper.getReadableDatabase());
    }


    //change the dialogtitle from "new task" to "edit task"
    public void titleEdit(){
        dialogTitleNew.setVisibility(View.GONE);
        dialogTitleEdit.setVisibility(View.VISIBLE);

    }

    //sets the textview either to listname in context or if no context to default
    public void setListSelector(int id, boolean idExists){
        updateLists();
        for (TodoList tl : lists){
            if (id == tl.getId() && idExists == true){
                listSelector.setText(tl.getName());
                selectedListID = tl.getId();
            }else if (!idExists){
                listSelector.setText(getContext().getString(R.string.click_to_choose));
                selectedListID = -3;
            }
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

    private void autoProgress() {
        int size = task.getSubTasks().size();
        int i = 5;
        int j = 3;
        double computedProgress = ((double)j/(double)i)*100;
        taskProgress = (int) computedProgress;
        progressPercent.setText(String.valueOf(computedProgress) + "%");
    }


}
