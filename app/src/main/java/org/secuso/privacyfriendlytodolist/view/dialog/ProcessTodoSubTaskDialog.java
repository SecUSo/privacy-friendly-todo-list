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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.TodoSubTask;

/**
 * This class shows a dialog that lets the user create/edit a subtask.
 */


public class ProcessTodoSubTaskDialog extends FullScreenDialog {

    private EditText etSubtaskName;
    private Button cancelButton;
    private TodoSubTask subtask;
    private TextView dialogTitleNew;
    private TextView dialogTitleEdit;

    public ProcessTodoSubTaskDialog(Context context) {
        super(context, R.layout.add_subtask_dialog);

        initGui();
        this.subtask = new TodoSubTask();
        this.subtask.setCreated();
        //this.subtask.setDbState(DBQueryHandler.ObjectStates.INSERT_TO_DB);
    }

    public ProcessTodoSubTaskDialog(Context context, TodoSubTask subTask) {
        super(context, R.layout.add_subtask_dialog);

        initGui();
        this.subtask = subTask;
        this.subtask.setChanged();
        //this.subtask.setDbState(DBQueryHandler.ObjectStates.UPDATE_DB);

        etSubtaskName.setText(subTask.getName());
    }

    private void initGui() {
        etSubtaskName = (EditText) findViewById(R.id.et_new_subtask_name);
        Button okButton = (Button) findViewById(R.id.bt_new_subtask_ok);
        cancelButton = (Button) findViewById(R.id.bt_new_subtask_cancel);

        //initialize titles of the dialog
        dialogTitleEdit = (TextView) findViewById(R.id.dialog_edit_sub);
        dialogTitleNew = (TextView) findViewById(R.id.dialog_subtitle);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = etSubtaskName.getText().toString();

                if(name.equals("")) {
                    Toast.makeText(getContext(), getContext().getString(R.string.todo_name_must_not_be_empty), Toast.LENGTH_SHORT).show();
                } else {

                    subtask.setName(name);
                    callback.finish(subtask);
                    ProcessTodoSubTaskDialog.this.dismiss();
                }
            }
        });

        Button cancelButton = (Button) findViewById(R.id.bt_new_subtask_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ProcessTodoSubTaskDialog.this.dismiss();
            }
        });
    }

    public void titleEdit() {
        dialogTitleNew.setVisibility(View.GONE);
        dialogTitleEdit.setVisibility(View.VISIBLE);
    }
}
