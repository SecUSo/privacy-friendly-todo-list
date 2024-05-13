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
import org.secuso.privacyfriendlytodolist.receiver.NotificationReceiver
import org.secuso.privacyfriendlytodolist.view.MainActivity

/**
 * Created by Sebastian Lutz on 12.03.2018.
 *
 *
 * Creates and manages notifications based on the SDK version.
 * If SDK >= 26 NotificationChannels will be created.
 */
object NotificationMgr {
    const val EXTRA_NOTIFICATION_TASK_ID = "EXTRA_NOTIFICATION_TASK_ID"
    private const val CHANNEL_ID = "my_channel_01"
    private var manager: NotificationManager? = null

    private fun getManager(context: Context): NotificationManager {
        if (manager == null) {
            manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Avoid recursion, provide notificationManager directly.
                createChannel(context, manager!!)
            }
        }
        return manager!!
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createChannel(context: Context, notificationManager: NotificationManager) {
        val channel = NotificationChannel(CHANNEL_ID,
            context.resources.getString(R.string.notif_reminder_ch_name),
            NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = context.resources.getString(R.string.notif_reminder_ch_desc)
        channel.enableLights(true)
        channel.lightColor = R.color.colorPrimary
        channel.enableVibration(true)
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        notificationManager.createNotificationChannel(channel)
    }

    fun postTaskNotification(context: Context, title: String, message: String, task: TodoTask): Int {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setAutoCancel(true)
            .setLights(ContextCompat.getColor(context, R.color.colorPrimary), 1000, 500)
        if (task.hasDeadline()) {
            builder.setContentText(message)
        }
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        // If Build.VERSION.SDK_INT >= Build.VERSION_CODES.O its no longer possible for the app to
        // change notification sound after channel was created. Only user can change that in
        // system notification settings.
        if (   Build.VERSION.SDK_INT < Build.VERSION_CODES.O
            && prefs.getBoolean(PreferenceMgr.P_IS_NOTIFICATION_SOUND.name, true)) {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            builder.setSound(uri)
        }

        // Main Action -> Show task in MainActivity
        var uniqueRequestCode = 0
        var intent = Intent(context, MainActivity::class.java)
        intent.putExtra(EXTRA_NOTIFICATION_TASK_ID, task.getId())
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(MainActivity::class.java)
        stackBuilder.addNextIntent(intent)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        var pendingIntent = stackBuilder.getPendingIntent(++uniqueRequestCode, flags)
        builder.setContentIntent(pendingIntent)

        // Snooze Action -> Restart reminder without showing activity
        intent = Intent(context, NotificationReceiver::class.java)
        intent.setAction(NotificationReceiver.ACTION_SNOOZE)
        intent.putExtra(EXTRA_NOTIFICATION_TASK_ID, task.getId())
        pendingIntent = PendingIntent.getBroadcast(context, ++uniqueRequestCode, intent, flags)
        var actionTitle = context.resources.getString(R.string.notif_reminder_act_snooze) + " " +
                Helper.snoozeDurationToString(context, PreferenceMgr.getSnoozeDuration(context))
        builder.addAction(R.drawable.snooze, actionTitle, pendingIntent)

        // Done Action
        intent = Intent(context, NotificationReceiver::class.java)
        intent.setAction(NotificationReceiver.ACTION_SET_DONE)
        intent.putExtra(EXTRA_NOTIFICATION_TASK_ID, task.getId())
        pendingIntent = PendingIntent.getBroadcast(context, ++uniqueRequestCode, intent, flags)
        actionTitle = context.resources.getString(R.string.notif_reminder_act_done)
        builder.addAction(R.drawable.done, actionTitle, pendingIntent)

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
