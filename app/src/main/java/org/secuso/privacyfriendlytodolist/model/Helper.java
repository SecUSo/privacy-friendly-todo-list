package org.secuso.privacyfriendlytodolist.model;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.TodoTask.DeadlineColors;
import org.secuso.privacyfriendlytodolist.model.TodoTask.Priority;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.secuso.privacyfriendlytodolist.model.TodoTask.DeadlineColors.*;
import static org.secuso.privacyfriendlytodolist.model.TodoTask.Priority.*;

/**
 * Created by dominik on 19.05.16.
 */
public class Helper {

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

    public static TextView getMenuHeader(Context context) {
        TextView blueBackground = new TextView(context);
        blueBackground.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        blueBackground.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
        blueBackground.setText(context.getResources().getString(R.string.select_option));
        blueBackground.setTextColor(ContextCompat.getColor(context, R.color.white));
        blueBackground.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        blueBackground.setGravity(Gravity.CENTER);

        return blueBackground;
    }
}

