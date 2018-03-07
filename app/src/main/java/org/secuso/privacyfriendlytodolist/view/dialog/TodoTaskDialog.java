package org.secuso.privacyfriendlytodolist.view.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.model.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sebbi on 01.03.2018.
 */

public class TodoTaskDialog extends DialogFragment {

    private TextView prioritySelector;
    private TextView deadlineTextView;
    private TextView reminderTextView;
    private TextView listSelector;
    private TextView dialogTitleNew;
    private TextView dialogTitleEdit;
    private TextView progressText;
    private TextView progressPercent;
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


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_task_dialog, null);

        builder.setView(view).setTitle(R.string.new_todo_task)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TodoTaskDialog.this.dismiss();
                    }
                })
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = taskName.getText().toString();
                        String description = taskDescription.getText().toString();

                        String listName = listSelector.getText().toString();

                        if(name.equals("")) {
                            Toast.makeText(getContext(), getContext().getString(R.string.todo_name_must_not_be_empty), Toast.LENGTH_SHORT).show();
                        } else if (listName.equals(getContext().getString(R.string.click_to_choose))) {
                            Toast.makeText(getContext(), getContext().getString(R.string.to_choose_list), Toast.LENGTH_SHORT).show();
                        }
                        else {

                            task.setName(name);
                            task.setDescription(description);
                            task.setDeadline(deadline);
                            task.setPriority(taskPriority);
                            task.setListId(selectedListID);
                            task.setProgress(taskProgress);
                            task.setReminderTime(reminderTime);
                            //callback.finish(task);
                            TodoTaskDialog.this.dismiss();
                        }
                    }
                });
            return builder.create();
        }



}
