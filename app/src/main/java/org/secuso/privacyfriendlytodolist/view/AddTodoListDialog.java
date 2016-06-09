package org.secuso.privacyfriendlytodolist.view;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.Helper;
import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.model.database.DBQueryHandler;
import org.secuso.privacyfriendlytodolist.model.database.DatabaseHelper;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by Simon on 30.05.2016.
 */
public class AddTodoListDialog extends Dialog {

    private static final String TAG = AddTodoListDialog.class.getSimpleName();

    private Context context;
    private AddTodoListDialogResult acCallback;
    private AddTodoListDialog self = this;
    private long deadline;


    public long getDeadline() {
        return this.deadline;
    }

    public boolean hasDeadline() {
        return this.deadline == -1;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
        TextView deadlineTextView = (TextView) findViewById(R.id.tv_todo_list_deadline);
        deadlineTextView.setText(Helper.getDate(deadline));
    }

    public void removeDeadline() {
        this.deadline = -1;
        TextView deadlineTextView = (TextView) findViewById(R.id.tv_todo_list_deadline);
        deadlineTextView.setText(context.getResources().getString(R.string.deadline));
    }


    public AddTodoListDialog(Context context) {
        super(context);
        this.context = context;
        setContentView(R.layout.add_todolist_popup);
        // setTitle("I18N: ADD LIST");

        TextView tvDeadline = (TextView) findViewById(R.id.tv_todo_list_deadline);
        tvDeadline.setOnClickListener(new DeadlineButtonListener());

        // required to make the dialog use the full screen width
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = getWindow();
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.horizontalMargin = 40;
        window.setAttributes(lp);

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

            if(listName.equals("") || listName == null) {
                Toast.makeText(getContext(), getContext().getString(R.string.list_name_must_not_be_empty), Toast.LENGTH_SHORT).show();
            } else {
                TodoList newList = new TodoList(listName, listDescription, self.getDeadline());
                newList.setWriteDbFlag();
                acCallback.finish(newList);
                self.dismiss();
            }


        }
    }

    public interface AddTodoListDialogResult {
        void finish(TodoList newList);
    }

    public void setDialogResult(AddTodoListDialogResult resultCallback) {
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
            Dialog deadlineDialog = new Dialog(context);
            deadlineDialog.setContentView(R.layout.deadline_dialog);

            // required to make the dialog use the full screen width
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            Window window = deadlineDialog.getWindow();
            lp.copyFrom(window.getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.horizontalMargin = 40;
            window.setAttributes(lp);

            Button buttonOkay = (Button) deadlineDialog.findViewById(R.id.bt_deadline_ok);
            buttonOkay.setOnClickListener(new OkayButtonListener(deadlineDialog));

            Button buttonNoDeadline = (Button) deadlineDialog.findViewById(R.id.bt_deadline_nodeadline);
            buttonNoDeadline.setOnClickListener(new NoDeadlineButtonListener(deadlineDialog));

            deadlineDialog.show();
        }

        private class OkayButtonListener implements View.OnClickListener {
            Dialog dialog;

            public OkayButtonListener(Dialog dialog) {
                this.dialog = dialog;
            }

            @Override
            public void onClick(View view) {
                DatePicker datePicker = (DatePicker) dialog.findViewById(R.id.dp_deadline);
                Calendar calendar = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                self.setDeadline(TimeUnit.MILLISECONDS.toSeconds(calendar.getTimeInMillis()));
                dialog.dismiss();
            }
        }

        private class NoDeadlineButtonListener implements View.OnClickListener {
            Dialog dialog;

            public NoDeadlineButtonListener(Dialog dialog) {
                this.dialog = dialog;
            }

            @Override
            public void onClick(View view) {
                self.removeDeadline();
                dialog.dismiss();
            }
        }
    }
}
