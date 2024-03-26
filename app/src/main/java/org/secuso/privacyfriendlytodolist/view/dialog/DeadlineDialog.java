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
import android.widget.DatePicker;

import org.secuso.privacyfriendlytodolist.R;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;


public class DeadlineDialog extends FullScreenDialog<DeadlineDialog.DeadlineCallback> {

    public interface DeadlineCallback {
        void setDeadline(long deadline);
        void removeDeadline();
    }

    public DeadlineDialog(Context context, long deadline) {
        super(context, R.layout.deadline_dialog);

        Calendar calendar = GregorianCalendar.getInstance();
        if(deadline != -1) calendar.setTimeInMillis(TimeUnit.SECONDS.toMillis(deadline));
        else calendar.setTime(Calendar.getInstance().getTime());
        DatePicker datePicker = (DatePicker) findViewById(R.id.dp_deadline);
        datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

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

}
