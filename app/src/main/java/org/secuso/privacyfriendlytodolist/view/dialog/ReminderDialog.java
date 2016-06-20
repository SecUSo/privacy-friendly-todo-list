package org.secuso.privacyfriendlytodolist.view.dialog;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import org.secuso.privacyfriendlytodolist.R;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;


public class ReminderDialog extends FullScreenDialog {

    private ReminderCallback callback;

    public ReminderDialog(Context context, int layoutId) {
        super(context, layoutId);

        Button buttonDate = (Button) findViewById(R.id.bt_reminder_date);
        buttonDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout layoutDate = (LinearLayout)findViewById(R.id.ll_reminder_date);
                layoutDate.setVisibility(View.VISIBLE);
                LinearLayout layoutTime = (LinearLayout)findViewById(R.id.ll_reminder_time);
                layoutTime.setVisibility(View.GONE);
            }
        });
        Button buttonTime = (Button) findViewById(R.id.bt_reminder_time);
        buttonTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout layoutDate = (LinearLayout)findViewById(R.id.ll_reminder_date);
                layoutDate.setVisibility(View.GONE);
                LinearLayout layoutTime = (LinearLayout)findViewById(R.id.ll_reminder_time);
                layoutTime.setVisibility(View.VISIBLE);
            }
        });

        Button buttonOkay = (Button) findViewById(R.id.bt_reminder_ok);
        buttonOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePicker datePicker = (DatePicker) findViewById(R.id.dp_reminder);
                TimePicker timePicker = (TimePicker) findViewById(R.id.tp_reminder);
                Calendar calendar = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), timePicker.getCurrentHour(), timePicker.getCurrentMinute());

                callback.setReminder(TimeUnit.MILLISECONDS.toSeconds(calendar.getTimeInMillis()));

                dismiss();
            }
        });

        Button buttonNoDeadline = (Button) findViewById(R.id.bt_reminder_noreminder);
        buttonNoDeadline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.removeReminder();
                dismiss();
            }
        });

    }

    public interface ReminderCallback {
        void setReminder(long deadline);

        void removeReminder();
    }

    public void setCallback(ReminderCallback callback) {
        this.callback = callback;
    }

}
