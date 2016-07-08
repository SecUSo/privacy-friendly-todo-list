package org.secuso.privacyfriendlytodolist.model;


import android.app.AlarmManager;
import android.content.Context;

public class AlarmManagerHolder {

    private static AlarmManager alarmManager = null;

    public static AlarmManager getAlarmManager(Context context) {
        if(alarmManager == null) {
            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        }

        return alarmManager;
    }

}
