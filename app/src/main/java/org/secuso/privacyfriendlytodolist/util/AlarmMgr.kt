/*
Privacy Friendly To-Do List
Copyright (C) 2024-2025  Christian Adams

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package org.secuso.privacyfriendlytodolist.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.receiver.AlarmReceiver
import java.util.concurrent.TimeUnit
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object AlarmMgr {
    const val KEY_ALARM_ID = "KEY_ALARM_ID"
    private val TAG = LogTag.create(this::class.java)
    private var manager: AlarmManager? = null
    private var lastTaskToRemindAlarmID: Int? = null

    private fun getManager(context: Context): AlarmManager {
        if (manager == null) {
            manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        }
        return manager!!
    }

    fun checkForPermissions(context: Context) {
        val sysAlarmMgr = getManager(context)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            Log.d(TAG, "SDK version is ${Build.VERSION.SDK_INT}. Exact alarms can be scheduled always.")
        } else if (sysAlarmMgr.canScheduleExactAlarms()) {
            Log.d(TAG, "Permission to schedule exact alarms is granted.")
        } else {
            Log.i(TAG, "Requesting permission to schedule exact alarms.")
            MaterialAlertDialogBuilder(context).apply {
                setMessage(R.string.dialog_need_permission_exact_alarm_message)
                setPositiveButton(R.string.yes) { _, _ ->
                    context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                }
                setNegativeButton(R.string.no) { _, _ ->
                }
                setTitle(R.string.dialog_need_permission_exact_alarm_title)
                show()
            }
        }
    }

    /**
     * Sets an alarm for the given next due task if it is not done and has a reminder time in the future.
     * If an alarm for a due task was set before it gets cancelled.
     *
     * @param context
     * @param todoTask
     *
     * @return If an alarm gets set the alarm ID gets returned (task ID gets used as alarm ID).
     * If no alarm gets set null gets returned.
     */
    fun setAlarmForNextTaskToRemind(context: Context, todoTask: TodoTask): Int? {
        // First cancel existing alarm to ensure it doesn't fire.
        if (lastTaskToRemindAlarmID != null) {
            Log.i(TAG, "Cancelling alarm of old next-due-task with ID $lastTaskToRemindAlarmID.")
            cancelAlarmForTask(context, lastTaskToRemindAlarmID!!)
            lastTaskToRemindAlarmID = null
        }

        val reminderTime = todoTask.getReminderTime()
        if (reminderTime == null) {
            Log.i(TAG, "No alarm set because $todoTask has no reminder time.")
            return null
        }

        if (todoTask.isDone() && !todoTask.isRecurring()) {
            Log.i(TAG, "No alarm set because $todoTask is done and not recurring.")
            return null
        }

        val now = Helper.getCurrentTimestamp()
        if (reminderTime < now) {
            Log.i(TAG, "No alarm set because reminder time of $todoTask is in the past.")
            return null
        }

        // Use task's database ID as unique alarm ID.
        val alarmId = todoTask.getId()
        // Cut off the seconds part to have the event at full minute.
        val alarmTime = reminderTime - (reminderTime % 60)
        val timestamp = Helper.createCanonicalDateTimeString(alarmTime)
        val kindOfAlarm = setAlarm(context, alarmId, alarmTime)
        lastTaskToRemindAlarmID = alarmId
        // Logging.
        val durationAsInt = TimeUnit.SECONDS.toMinutes(reminderTime - now)
        val duration = durationAsInt.toDuration(DurationUnit.MINUTES)
        Log.i(TAG, "$kindOfAlarm alarm set for $todoTask at $timestamp which is in $duration.")

        return alarmId
    }

    /**
     * Sets an alarm with the given alarm ID and alarm time without any further checks.
     *
     * @param context
     * @param alarmId The alarm ID.
     * @param alarmTime The alarm time in seconds.
     *
     * @return The alarm ID as given to this method.
     */
    fun setAlarmForTask(context: Context, alarmId: Int, alarmTime: Long): Int {
        // Use task's database ID as unique alarm ID.
        cancelAlarmForTask(context, alarmId)

        val kindOfAlarm = setAlarm(context, alarmId, alarmTime)
        Log.i(TAG, "$kindOfAlarm alarm set for task $alarmId at ${Helper.createCanonicalDateTimeString(alarmTime)}.")
        return alarmId
    }

    private fun setAlarm(context: Context, alarmId: Int, alarmTime: Long): String {
        // Use task's database ID as unique alarm ID.
        val pendingIntent = getPendingAlarmIntent(context, alarmId, true)!!
        val sysAlarmMgr = getManager(context)
        // On targets with SDK_INT < VERSION_CODES.S exact alarms can always be scheduled.
        // On targets with SDK_INT >= VERSION_CODES.S exact alarms can be scheduled if user grants permissions.
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || sysAlarmMgr.canScheduleExactAlarms()) {
            sysAlarmMgr.setExact(AlarmManager.RTC_WAKEUP, TimeUnit.SECONDS.toMillis(alarmTime), pendingIntent)
            "Exact"
        } else {
            sysAlarmMgr.set(AlarmManager.RTC_WAKEUP, TimeUnit.SECONDS.toMillis(alarmTime), pendingIntent)
            "Inexact"
        }
    }

    fun cancelAlarmForTask(context: Context, alarmId: Int): Boolean {
        val pendingIntent = getPendingAlarmIntent(context, alarmId, false)
        return if (pendingIntent != null) {
            getManager(context).cancel(pendingIntent)
            Log.i(TAG, "Alarm $alarmId cancelled.")
            true
        } else {
            Log.d(TAG, "Failed to cancel alarm. No alarm with ID $alarmId was found.")
            false
        }
    }

    private fun getPendingAlarmIntent(context: Context, alarmId: Int, createIfNotExist: Boolean): PendingIntent? {
        var flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        if (!createIfNotExist) {
            flags = flags or PendingIntent.FLAG_NO_CREATE
        }
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.setAction(AlarmReceiver.ACTION)
        intent.putExtra(KEY_ALARM_ID, alarmId)
        return PendingIntent.getBroadcast(context, alarmId, intent, flags)
    }
}
