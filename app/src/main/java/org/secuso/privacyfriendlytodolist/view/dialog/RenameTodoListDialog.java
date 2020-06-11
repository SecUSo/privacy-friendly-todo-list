package org.secuso.privacyfriendlytodolist.view.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.EditText;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.model.database.DBQueryHandler;
import org.secuso.privacyfriendlytodolist.model.database.DatabaseHelper;

import java.util.ArrayList;

public class RenameTodoListDialog extends DialogFragment {

    private Toolbar toolbar;
    private DatabaseHelper dbHelper;
    private MenuItem currentItem;

    public RenameTodoListDialog(Toolbar t, DatabaseHelper dh, MenuItem i) {
        this.toolbar = t;
        this.dbHelper = dh;
        this.currentItem = i;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        final EditText input = new EditText(getContext());

        return new AlertDialog.Builder(getActivity())
                .setMessage(R.string.list_name_must_not_be_empty)
                .setView(input)
                .setCancelable(true)
                .setPositiveButton(R.string.exit_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!input.getText().toString().isEmpty()) {
                            String currentTitle = toolbar.getTitle().toString();
                            toolbar.setTitle(input.getText().toString());
                            currentItem.setTitle(input.getText().toString());

                            ArrayList<TodoList> todoLists = DBQueryHandler.getAllToDoLists(dbHelper.getReadableDatabase());
                            TodoList todoList = new TodoList();

                            for (TodoList t : todoLists) {
                                if (t.getName().equals(currentTitle)) {
                                    todoList = t;
                                    break;
                                }
                            }

                            todoList.setName(input.getText().toString());
                            todoList.setDBState(DBQueryHandler.ObjectStates.UPDATE_DB);
                            DBQueryHandler.saveTodoListInDb(dbHelper.getWritableDatabase(), todoList);
                        }
                    }
                })
                .setNegativeButton(R.string.exit_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create();
    }
}
