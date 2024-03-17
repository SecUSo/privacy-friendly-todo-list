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

package org.secuso.privacyfriendlytodolist.util;

import android.content.Context;
import android.graphics.Typeface;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.TodoTask.DeadlineColors;
import org.secuso.privacyfriendlytodolist.model.TodoTask.Priority;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class Helper {

    public static final CharSequence DATE_FORMAT = "dd.MM.yyyy";

    public static String getDate(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(TimeUnit.SECONDS.toMillis(time));
        return DateFormat.format("dd.MM.yyyy", calendar).toString();
    }

    public static String getDateTime(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(TimeUnit.SECONDS.toMillis(time));
        return DateFormat.format("dd.MM.yyyy HH:mm", calendar).toString();
    }

    public static long getCurrentTimestamp() {
        return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
    }

    public static int getDeadlineColor(Context context, DeadlineColors color) {
        switch (color) {
            case RED:
                return ContextCompat.getColor(context, R.color.deadline_red);
            case BLUE:
                return ContextCompat.getColor(context, R.color.deadline_blue);
            case ORANGE:
                return ContextCompat.getColor(context, R.color.deadline_orange);
        }

        throw new IllegalArgumentException("Deadline color not defined.");
    }

    public static String priority2String(Context context, Priority prio) {

        switch (prio) {
            case HIGH:
                return context.getResources().getString(R.string.high_priority);
            case MEDIUM:
                return context.getResources().getString(R.string.medium_priority);
            case LOW:
                return context.getResources().getString(R.string.low_priority);
            default:
                return context.getResources().getString(R.string.unknown_priority);

        }
    }

    public static TextView getMenuHeader(Context context, String title) {
        TextView blueBackground = new TextView(context);
        blueBackground.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        blueBackground.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
        blueBackground.setText(title);
        blueBackground.setTextColor(ContextCompat.getColor(context, R.color.black));
        blueBackground.setPadding(65, 65, 65, 65);
        blueBackground.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        blueBackground.setTypeface(null, Typeface.BOLD);

        return blueBackground;
    }

}

