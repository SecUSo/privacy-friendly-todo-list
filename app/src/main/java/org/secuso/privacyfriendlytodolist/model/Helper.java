package org.secuso.privacyfriendlytodolist.model;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.format.DateFormat;


import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.TodoTask.DeadlineColors;
import org.secuso.privacyfriendlytodolist.model.TodoTask.Priority;

import java.util.Calendar;
import java.util.Locale;

import static org.secuso.privacyfriendlytodolist.model.TodoTask.DeadlineColors.*;
import static org.secuso.privacyfriendlytodolist.model.TodoTask.Priority.*;

/**
 * Created by dominik on 19.05.16.
 */
public class Helper {

    public static String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time * 1000);
        return DateFormat.format("dd.MM.yyyy", cal).toString();
    }

    public static long getCurrentTimestamp() {
        return System.currentTimeMillis()/1000;
    }

    public static int dp2Px(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
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

        switch(prio) {
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
}
