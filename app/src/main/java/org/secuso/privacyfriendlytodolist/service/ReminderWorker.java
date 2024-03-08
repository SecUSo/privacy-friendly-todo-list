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

package org.secuso.privacyfriendlytodolist.service;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.AlarmManagerHolder;
import org.secuso.privacyfriendlytodolist.model.Model;
import org.secuso.privacyfriendlytodolist.model.ModelServices;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.util.Helper;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Sebastian Lutz on 12.03.2018.
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

public class ReminderWorker extends Worker {

    public static final String KEY_START_UP = "KEY_START_UP";
    public static final String KEY_ALARM_TASK_ID = "KEY_ALARM_TASK_ID";
    public static final String KEY_TASK_ID = "KEY_TASK_ID";

    private static final String TAG = ReminderWorker.class.getSimpleName();

    private final ModelServices model;
    private final NotificationManager mNotificationManager;
    private final AlarmManager alarmManager;
    private final NotificationHelper helper;

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        model = Model.getServices(context);
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        alarmManager = AlarmManagerHolder.getAlarmManager(context);
        helper = new NotificationHelper(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        int workArg = getInputData().getInt(KEY_START_UP, 0);
        if (0 != workArg) {
            processStartUp();
        }
        workArg = getInputData().getInt(KEY_ALARM_TASK_ID, 0);
        if (0 != workArg) {
            processAlarm(workArg);
        }
        workArg = getInputData().getInt(KEY_TASK_ID, 0);
        if (0 != workArg) {
            processTodoTask(workArg);
        }
        return Result.success();
    }

    private void processStartUp() {
        //  Service was started for the first time
        reloadAlarms();
    }

    private void processAlarm(int todoTaskId) {
        TodoTask task = model.getTaskById(todoTaskId);
        if (null != task) {
            notifyAboutAlarm(task);
        }

        task = model.getNextDueTask(Helper.getCurrentTimestamp());
        if(task != null) {
            setAlarmForTask(task);
        }
    }

    private void notifyAboutAlarm(TodoTask task) {
        String title = task.getName();
        String deadline = Helper.getDateTime(task.getDeadline());
        String message = getApplicationContext().getResources().getString(R.string.deadline_approaching, deadline);
        NotificationCompat.Builder nb = helper.getNotification(title, message, task);
        helper.getManager().notify(task.getId(), nb.build());
    }

    private void reloadAlarms() {
        mNotificationManager.cancelAll(); // cancel all alarms

        List<TodoTask> tasksToRemind = model.getTasksToRemind(Helper.getCurrentTimestamp(), null);

        // set alarms
        for (TodoTask currentTask : tasksToRemind) {
            setAlarmForTask(currentTask);
        }

        if (tasksToRemind.isEmpty()) {
            Log.i(TAG, "No alarms set.");
        }
    }

    private void setAlarmForTask(TodoTask task) {
        Context context = getApplicationContext();
        Intent alarmIntent = new Intent(context, ReminderService.class);
        alarmIntent.putExtra(TodoTask.PARCELABLE_KEY, task);

        int alarmID = task.getId(); // use database id as unique alarm id
        PendingIntent pendingAlarmIntent = PendingIntent.getService(context, alarmID, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Calendar calendar = Calendar.getInstance();
        long reminderTime = task.getReminderTime();

        if (reminderTime != -1 && reminderTime <= Helper.getCurrentTimestamp()){
            Date date = new Date(TimeUnit.SECONDS.toMillis(Helper.getCurrentTimestamp()));
            calendar.setTime(date);
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingAlarmIntent);

            Log.i(TAG, "Alarm set for " + task.getName() + " at " + Helper.getDateTime(calendar.getTimeInMillis() / 1000) + " (alarm id: " + alarmID + ")");
        } else if (reminderTime != -1) {
            Date date = new Date(TimeUnit.SECONDS.toMillis(reminderTime)); // convert to milliseconds
            calendar.setTime(date);
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingAlarmIntent);

            Log.i(TAG, "Alarm set for " + task.getName() + " at " + Helper.getDateTime(calendar.getTimeInMillis() / 1000) + " (alarm id: " + alarmID + ")");
        }
    }

    private void processTodoTask(int todoTaskId) {

        // TODO add more granularity: You don't need to change the alarm if the name or the description of the task were changed. You actually need this perform the following steps if the reminder time or the "done" status were modified.
        TodoTask changedTask = model.getTaskById(todoTaskId);
        if (null != changedTask) {
            Context context = getApplicationContext();
            PendingIntent alarmIntent = PendingIntent.getBroadcast(context, changedTask.getId(),
                    new Intent(context, ReminderService.class),
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            // check if alarm was set for this task
            if (alarmIntent != null) {

                // 1. cancel old alarm
                alarmManager.cancel(alarmIntent);
                Log.i(TAG, "Alarm of task " + changedTask.getName() + " cancelled. (id="+changedTask.getId()+")");

                // 2. delete old notification if it exists
                mNotificationManager.cancel(changedTask.getId());
                Log.i(TAG, "Notification of task " + changedTask.getName() + " deleted (if existed). (id="+changedTask.getId()+")");
            } else  {
                Log.i(TAG, "No alarm found for " + changedTask.getName() + " (alarm id: " + changedTask.getId() + ")");
            }

            setAlarmForTask(changedTask);
        }
    }
}
