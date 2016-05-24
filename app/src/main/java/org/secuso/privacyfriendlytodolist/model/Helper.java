package org.secuso.privacyfriendlytodolist.model;

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

}
