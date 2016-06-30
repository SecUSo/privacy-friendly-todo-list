package org.secuso.privacyfriendlytodolist.model;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.view.MainActivity;
import org.secuso.privacyfriendlytodolist.view.TodoTasksFragment;

public class NotifyService extends Service {


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        TodoTask task = intent.getExtras().getParcelable(TodoTask.PARCELABLE_KEY);
        createNotification(task);

        return START_STICKY;
    }

    private void createNotification(TodoTask task) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

        mBuilder.setSmallIcon(android.R.drawable.ic_lock_idle_alarm);
        mBuilder.setContentTitle("\"" + task.getName() + "\"" + getResources().getString(R.string.is_due));
        mBuilder.setContentText(getResources().getString(R.string.task_deadline) + " " + Helper.getDateTime(task.getDeadline()));

        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra(MainActivity.FRAGMENT_CHOICE, TodoTasksFragment.KEY);
        resultIntent.putExtra(TodoTask.PARCELABLE_KEY, task);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);
        mBuilder.setLights(ContextCompat.getColor(this, R.color.colorPrimary), 1000, 500);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0, mBuilder.build());
    }

}
