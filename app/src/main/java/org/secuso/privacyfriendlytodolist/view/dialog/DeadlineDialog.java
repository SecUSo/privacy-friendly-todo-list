package org.secuso.privacyfriendlytodolist.view.dialog;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import org.secuso.privacyfriendlytodolist.R;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;


public class DeadlineDialog extends FullScreenDialog {

    private DeadlineCallback callback;

    public DeadlineDialog(Context context, int layoutId) {
        super(context, layoutId);

        Button buttonOkay = (Button) findViewById(R.id.bt_deadline_ok);
        buttonOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePicker datePicker = (DatePicker) findViewById(R.id.dp_deadline);
                Calendar calendar = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());

                callback.setDeadline(TimeUnit.MILLISECONDS.toSeconds(calendar.getTimeInMillis()));

                dismiss();
            }
        });

        Button buttonNoDeadline = (Button) findViewById(R.id.bt_deadline_nodeadline);
        buttonNoDeadline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.removeDeadline();
                dismiss();
            }
        });

    }

    public interface DeadlineCallback {
        void setDeadline(long deadline);
        void removeDeadline();
    }

    public void setCallback(DeadlineCallback callback) {
        this.callback = callback;
    }

}
