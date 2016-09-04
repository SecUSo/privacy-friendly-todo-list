package org.secuso.privacyfriendlytodolist.model;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.database.DBQueryHandler;
import org.secuso.privacyfriendlytodolist.model.database.DatabaseHelper;
import org.secuso.privacyfriendlytodolist.view.MainActivity;
import org.secuso.privacyfriendlytodolist.view.TodoTasksFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 *
 * This service implements the following alarm policies:
 *
 * - On startup it sets alarms for all tasks fulfilling all of the subsequent conditions:
 *          1. The reminding time is in the past.
 *          2. The deadline is in the future.
 *          3. The task is not yet done.
 * - On startup the service sets an alarm for next due task in the future.
 *
 * - Whenever an alarm is triggered the service sets the alarm for the next due task in the future. It is possible
 *   that this alarm is already set. In that case is just gets overwritten.
 */


public class ReminderService extends Service {

    private DatabaseHelper dbHelper;
    private static final String TAG = ReminderService.class.getSimpleName();

    public static final String ALARM_TRIGGERED = "ALARM_TRIGGERD";

    private boolean alreadyRunning = false;
    private final IBinder mBinder = new ReminderServiceBinder();

    private NotificationManager mNotificationManager;
    private AlarmManager alarmManager;

    @Override
    public IBinder onBind(Intent intent) {

        Bundle extras = intent.getExtras();
        // Get messager from the Activity
        if (extras != null) {
            Log.i(TAG, "onBind() with extra");
        } else {
            Log.i(TAG, "onBind()");
        }

        return mBinder;
    }

    @Override
    public void onDestroy() {
        dbHelper.close();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "onStartCommand()");

        dbHelper = DatabaseHelper.getInstance(this);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        alarmManager = AlarmManagerHolder.getAlarmManager(this);

        boolean alarmTriggered = false;
        Bundle extras = intent.getExtras();
        if (extras != null)
            alarmTriggered = intent.getExtras().getBoolean(ALARM_TRIGGERED);

        if (alarmTriggered) {
            TodoTask task = intent.getExtras().getParcelable(TodoTask.PARCELABLE_KEY);
            handleAlarm(task);

            // get next alarm
            TodoTask nextDueTask = DBQueryHandler.getNextDueTask(dbHelper.getReadableDatabase(), Helper.getCurrentTimestamp());
            if(nextDueTask != null)
                setAlarmForTask(nextDueTask);
        } else {

            //  service was started for the first time
            if (!alreadyRunning) {
                reloadAlarmsFromDB();
                alreadyRunning = true; // If this service gets killed, alreadyRunning will be false the next time. However, the service is only killed when the resources are scarce. So we deliberately set the alarms again after restarting the service.
                Log.i(TAG, "Service was started the first time.");
            } else {
                Log.i(TAG, "Service was already running.");
            }
        }

        return START_NOT_STICKY; // do not recreate service if the phone runs out of memory
    }

    private void handleAlarm(TodoTask task) {

        String title = task.getName();


        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra(MainActivity.KEY_SELECTED_FRAGMENT_BY_NOTIFICATION, TodoTasksFragment.KEY);
        resultIntent.putExtra(TodoTask.PARCELABLE_KEY, task);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(android.R.drawable.ic_lock_idle_alarm);
        mBuilder.setContentTitle(title);
        if(task.hasDeadline())
            mBuilder.setContentText(getResources().getString(R.string.deadline_approaching, Helper.getDateTime(task.getDeadline())));
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);
        mBuilder.setLights(ContextCompat.getColor(this, R.color.colorPrimary), 1000, 500);
        mNotificationManager.notify(task.getId(), mBuilder.build());

    }

    public void reloadAlarmsFromDB() {
        mNotificationManager.cancelAll(); // cancel all alarms

        ArrayList<TodoTask> tasksToRemind = DBQueryHandler.getTasksToRemind(dbHelper.getReadableDatabase(), Helper.getCurrentTimestamp(), null);

        // set alarms
        for (TodoTask currentTask : tasksToRemind) {
            setAlarmForTask(currentTask);
        }

        if(tasksToRemind.size() == 0) {
            Log.i(TAG, "No alarms set.");
        }
    }

    private void setAlarmForTask(TodoTask task) {

        Intent alarmIntent = new Intent(this, ReminderService.class);
        alarmIntent.putExtra(TodoTask.PARCELABLE_KEY, task);
        alarmIntent.putExtra(ALARM_TRIGGERED, true);

        int alarmID = task.getId(); // use database id as unique alarm id
        PendingIntent pendingAlarmIntent = PendingIntent.getService(this, alarmID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar calendar = Calendar.getInstance();
        long reminderTime = task.getReminderTime();
        Date date = new Date(TimeUnit.SECONDS.toMillis(reminderTime)); // convert to milliseconds
        calendar.setTime(date);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingAlarmIntent);

        Log.i(TAG, "Alarm set for " + task.getName() + " at " + Helper.getDateTime(calendar.getTimeInMillis() / 1000) + " (alarm id: " + alarmID + ")");

    }


    public void processTask(TodoTask changedTask) {

        // TODO add more granularity: You don't need to change the alarm if the name or the description of the task were changed. You actually need this perform the following steps if the reminder time or the "done" status were modified.

        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, changedTask.getId(),
                new Intent(this, ReminderService.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        // check if alarm was set for this task
        if (alarmIntent != null) {

            // 1. cancel old alarm
            alarmManager.cancel(alarmIntent);
            Log.i(TAG, "Alarm of task " + changedTask.getName() + " cancelled. (id="+changedTask.getId()+")");

            // 2. delete old notification if it exists
            mNotificationManager.cancel(changedTask.getId());
            Log.i(TAG, "Notification of task " + changedTask.getName() + " deleted (if existed). (id="+changedTask.getId()+")");
        } else {
            Log.i(TAG, "No alarm found for " + changedTask.getName() + " (alarm id: " + changedTask.getId() + ")");
        }

        setAlarmForTask(changedTask);
    }


    public class ReminderServiceBinder extends Binder {
        public ReminderService getService() {
            return ReminderService.this;
        }
    }

}
