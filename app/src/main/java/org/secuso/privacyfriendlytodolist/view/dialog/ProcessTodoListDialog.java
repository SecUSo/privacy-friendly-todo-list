package org.secuso.privacyfriendlytodolist.view.dialog;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.Helper;
import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.model.database.DBQueryHandler;
import org.secuso.privacyfriendlytodolist.view.TodoCallback;


public class ProcessTodoListDialog extends FullScreenDialog {

    private TodoCallback acCallback;
    private ProcessTodoListDialog self = this;

    private TextView tvDeadline;
    private Button buttonOkay;
    private Button buttonCancel;
    private EditText etName, etDescription;

    private long deadline;

    public long getDeadline() {
        return this.deadline;
    }

    private TodoList todoList;


    public ProcessTodoListDialog(Context context) {
        super(context, R.layout.add_todolist_dialog);

        initGui();

        todoList = new TodoList();
        deadline = -1;
        todoList.setDbState(DBQueryHandler.ObjectStates.INSERT_TO_DB);
    }

    public ProcessTodoListDialog(Context context, TodoList list2Change) {
        super(context, R.layout.add_todolist_dialog);

        initGui();
        if(list2Change.getDeadline() == -1)
            tvDeadline.setText(context.getResources().getString(R.string.no_deadline));
        else
            tvDeadline.setText(Helper.getDate(list2Change.getDeadline()));
        etName.setText(list2Change.getName());
        etDescription.setText(list2Change.getDescription());
        todoList = list2Change;
        todoList.setDbState(DBQueryHandler.ObjectStates.UPDATE_DB);
    }


    private void initGui() {
        tvDeadline = (TextView) findViewById(R.id.tv_todo_list_deadline);
        tvDeadline.setOnClickListener(new DeadlineButtonListener());
        buttonOkay = (Button) findViewById(R.id.bt_newtodolist_ok);
        buttonOkay.setOnClickListener(new OkayButtonListener());
        buttonCancel = (Button) findViewById(R.id.bt_newtodolist_cancel);
        buttonCancel.setOnClickListener(new CancelButtonListener());
        etName = (EditText) findViewById(R.id.et_todo_list_name);
        etDescription = (EditText) findViewById(R.id.et_todo_list_description);
    }

    private class OkayButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            // prepare list data
            String listName = etName.getText().toString();
            String listDescription = etDescription.getText().toString();

            if (listName.equals("") || listName == null) {
                Toast.makeText(getContext(), getContext().getString(R.string.list_name_must_not_be_empty), Toast.LENGTH_SHORT).show();
            } else {

                if (changesMade(listName, listDescription, deadline)) {
                    todoList.setName(listName);
                    todoList.setDescription(listDescription);
                    todoList.setDeadline(deadline);
                    acCallback.finish(todoList);
                }
                self.dismiss();
            }
        }
    }

    private boolean changesMade(String listName, String listDescription, long deadline) {

        // check if real changes were made
        if (listName.equals(todoList.getName()) &&
                listDescription.equals(todoList.getDescription()) &&
                todoList.getDeadline() == deadline) {

            return false;
        }

        return true;
    }

    public void setDialogResult(TodoCallback resultCallback) {
        acCallback = resultCallback;
    }

    private class CancelButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            self.hide();
        }
    }

    private class DeadlineButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

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
    }
}
