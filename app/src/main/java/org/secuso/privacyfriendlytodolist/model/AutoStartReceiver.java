package org.secuso.privacyfriendlytodolist.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class AutoStartReceiver extends BroadcastReceiver {

    private static final String TAG = AutoStartReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent reminderServiceIntent = new Intent(context, ReminderService.class);
        context.startService(reminderServiceIntent);
        Log.i(TAG, ReminderService.class.getSimpleName() + " started.");
    }
}
