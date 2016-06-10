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
import org.secuso.privacyfriendlytodolist.view.TodoCallback;


public class AddTodoListDialog extends FullScreenDialog {

    private TodoCallback acCallback;
    private AddTodoListDialog self = this;
    private long deadline;
    public long getDeadline() {
        return this.deadline;
    }


    public AddTodoListDialog(Context context) {
        super(context, R.layout.add_todolist_dialog);

        TextView tvDeadline = (TextView) findViewById(R.id.tv_todo_list_deadline);
        tvDeadline.setOnClickListener(new DeadlineButtonListener());

        Button buttonOkay = (Button) findViewById(R.id.bt_newtodolist_ok);
        buttonOkay.setOnClickListener(new OkayButtonListener());

        Button buttonCancel = (Button) findViewById(R.id.bt_newtodolist_cancel);
        buttonCancel.setOnClickListener(new CancelButtonListener());
    }

    private class OkayButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            // prepare list data
            String listName = ((EditText) findViewById(R.id.et_todo_list_name)).getText().toString();
            String listDescription = ((EditText) findViewById(R.id.et_todo_list_description)).getText().toString();

            if (listName.equals("") || listName == null) {
                Toast.makeText(getContext(), getContext().getString(R.string.list_name_must_not_be_empty), Toast.LENGTH_SHORT).show();
            } else {
                TodoList newList = new TodoList(listName, listDescription, self.getDeadline());
                newList.setWriteDbFlag();
                acCallback.finish(newList);
                self.dismiss();
            }
        }
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

            DeadlineDialog deadlineDialog = new DeadlineDialog(getContext(), R.layout.deadline_dialog);
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
