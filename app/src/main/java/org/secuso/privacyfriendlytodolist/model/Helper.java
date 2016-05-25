package org.secuso.privacyfriendlytodolist.model;

import android.content.Context;
import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by dominik on 19.05.16.
 */
public class Helper {

    public static String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        return DateFormat.format("dd.MM.yyyy", cal).toString();
    }

    public static int dp2Px(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

}
