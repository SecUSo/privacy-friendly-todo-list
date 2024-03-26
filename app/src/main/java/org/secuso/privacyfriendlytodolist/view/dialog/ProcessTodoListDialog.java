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
import android.widget.Toast;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.Model;
import org.secuso.privacyfriendlytodolist.model.TodoList;


/**
 * Created by Sebastian Lutz on 12.03.2018.
 *
 * Defines the dialog that lets the user create a list
 */

public class ProcessTodoListDialog extends FullScreenDialog<ResultCallback<TodoList>> {

    private EditText etName, etDescription;
    private final TodoList todoList;


    public ProcessTodoListDialog(Context context) {
        super(context, R.layout.add_todolist_dialog);

        initGui();

        todoList = Model.createNewTodoList();
        todoList.setCreated();
        //todoList.setDbState(DBQueryHandler.ObjectStates.INSERT_TO_DB);
    }

    public ProcessTodoListDialog(Context context, TodoList list2Change) {
        super(context, R.layout.add_todolist_dialog);

        initGui();
        etName.setText(list2Change.getName());
        todoList = list2Change;
        todoList.setChanged();
        //todoList.setDbState(DBQueryHandler.ObjectStates.UPDATE_DB);
    }



    private void initGui() {
        Button buttonOkay = (Button) findViewById(R.id.bt_newtodolist_ok);
        buttonOkay.setOnClickListener(new OkayButtonListener());
        Button buttonCancel = (Button) findViewById(R.id.bt_newtodolist_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        etName = (EditText) findViewById(R.id.et_todo_list_name);
    }

    private class OkayButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            // prepare list data
            String listName = etName.getText().toString();

            if (listName.equals("")) {
                Toast.makeText(getContext(), getContext().getString(R.string.list_name_must_not_be_empty), Toast.LENGTH_SHORT).show();
            } else {

                if (changesMade(listName)) {
                    todoList.setName(listName);
                    callback.onFinish(todoList);
                }
                ProcessTodoListDialog.this.dismiss();
            }
        }
    }

    private boolean changesMade(String listName) {
        // check if real changes were made
        return ! listName.equals(todoList.getName());
    }
}
