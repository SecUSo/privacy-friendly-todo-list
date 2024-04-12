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
package org.secuso.privacyfriendlytodolist.util

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.view.MainActivity
import org.secuso.privacyfriendlytodolist.view.TodoTasksFragment

/**
 * Created by Sebastian Lutz on 12.03.2018.
 *
 *
 * Creates and manages notifications based on the SDK version.
 * If SDK >= 26 NotificationChannels will be created.
 */
object NotificationMgr {
    private const val CHANNEL_ID = "channel_01"
    private var manager: NotificationManager? = null

    private fun getManager(context: Context): NotificationManager {
        if (manager == null) {
            manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Avoid recursion, provide notificationManager directly.
                createChannel(manager!!)
            }
        }
        return manager!!
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createChannel(notificationManager: NotificationManager) {
        // TODO Channel name and description is visible to the user and therefore should come from resources as language dependent text.
        val channel = NotificationChannel(CHANNEL_ID, "Task Reminder", NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = "Reminders for upcoming tasks."
        channel.enableLights(true)
        channel.lightColor = R.color.colorPrimary
        channel.enableVibration(true)
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        notificationManager.createNotificationChannel(channel)
    }

    fun postTaskNotification(context: Context, title: String?, message: String?, task: TodoTask): Int {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setAutoCancel(true)
            .setLights(ContextCompat.getColor(context, R.color.colorPrimary), 1000, 500)
        if (task.hasDeadline()) {
            builder.setContentText(message)
        }
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        if (prefs.getBoolean(PreferenceMgr.P_IS_NOTIFICATION_SOUND.name, true)) {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            builder.setSound(uri)
        }
        val snooze = Intent(context, MainActivity::class.java)
        val pendingSnooze = PendingIntent.getActivity(context, 0, snooze,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        // TODO Snooze duration should be configurable as a setting.
        snooze.putExtra("snooze", 900000)
        snooze.putExtra("taskId", task.getId())
        val resultIntent = Intent(context, MainActivity::class.java)
        resultIntent.putExtra(MainActivity.KEY_SELECTED_FRAGMENT_BY_NOTIFICATION, TodoTasksFragment.KEY)
        resultIntent.putExtra(MainActivity.PARCELABLE_KEY_FOR_TODO_TASK, task)
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(MainActivity::class.java)
        stackBuilder.addNextIntent(resultIntent)
        stackBuilder.addNextIntent(snooze)
        val resultPendingIntent = stackBuilder.getPendingIntent(0,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        // TODO Texts should be language dependent (come from XML resource file):
        builder.addAction(R.drawable.snooze, "Snooze", pendingSnooze)
        builder.addAction(R.drawable.done, "Set done", resultPendingIntent)
        builder.setContentIntent(resultPendingIntent)
        val notificationId = task.getId()
        val notification = builder.build()
        getManager(context).notify(notificationId, notification)
        return notificationId
    }

    fun cancel(context: Context, notificationId: Int) {
        getManager(context).cancel(notificationId)
    }

    fun cancelAll(context: Context) {
        getManager(context).cancelAll()
    }
}
