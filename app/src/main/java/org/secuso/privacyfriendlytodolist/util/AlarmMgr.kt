/*
Privacy Friendly To-Do List
Copyright (C) 2024  Christian Adams

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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.Model
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.receiver.AlarmReceiver
import org.secuso.privacyfriendlytodolist.viewmodel.CustomViewModel
import java.util.concurrent.TimeUnit
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object AlarmMgr {
    const val KEY_ALARM_ID = "KEY_ALARM_ID"
    private val TAG = LogTag.create(this::class.java)
    private var manager: AlarmManager? = null
    private var lastDueTaskAlarmID: Int? = null
    private var lastDueTaskAlarmTime: Long = 0

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
            AlertDialog.Builder(context)
                .setMessage(R.string.dialog_need_permission_exact_alarm_message)
                .setPositiveButton(R.string.yes) { _, _ ->
                    context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                }
                .setNegativeButton(R.string.no) { _, _ ->
                }
                .setTitle(R.string.dialog_need_permission_exact_alarm_title)
                .create()
                .show()
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
    fun setAlarmForNextDueTask(context: Context, todoTask: TodoTask): Int? {
        var reminderTime = todoTask.getReminderTime()
        if (reminderTime == null) {
            Log.i(TAG, "No alarm set because $todoTask has no reminder time.")
            return null
        }

        if (todoTask.isDone() && !todoTask.isRecurring()) {
            Log.i(TAG, "No alarm set because $todoTask is done and not recurring.")
            return null
        }

        val now = Helper.getCurrentTimestamp()
        if (todoTask.isRecurring()) {
            // Get the upcoming due date of the recurring task. The initial reminder time might not
            // be the right date for the alarm.
            val oldReminderTime = reminderTime
            reminderTime = Helper.getNextRecurringDate(reminderTime,
                todoTask.getRecurrencePattern(), todoTask.getRecurrenceInterval(), now)
            if (oldReminderTime != reminderTime) {
                // Store new reminder time to make it visible for the user.
                Log.i(TAG, "Updating reminder time of $todoTask from " +
                        "${Helper.createCanonicalDateTimeString(oldReminderTime)} to " +
                        "${Helper.createCanonicalDateTimeString(reminderTime)}.")
                todoTask.setReminderTime(reminderTime)
                todoTask.setChanged()
                val viewModel = CustomViewModel(context)
                viewModel.model.saveTodoTaskInDb(todoTask) {
                    Model.notifyDataChangedFromOutside(context)
                }
            }
        }

        if (reminderTime < now) {
            Log.i(TAG, "No alarm set because reminder time of $todoTask is in the past.")
            return null
        }

        // Use task's database ID as unique alarm ID.
        val alarmId = todoTask.getId()
        // Get next full minute as alarm time.
        val rest = reminderTime % 60
        val alarmTime = if (rest == 0L) reminderTime else reminderTime - rest + 60
        val canonicalTimestamp = Helper.createCanonicalDateTimeString(alarmTime)

        if (lastDueTaskAlarmID == alarmId && lastDueTaskAlarmTime == alarmTime) {
            Log.i(TAG, "Alarm with ID $alarmId and alarm time $canonicalTimestamp is already set.")
            return null
        }

        if (lastDueTaskAlarmID != null) {
            Log.i(TAG, "Cancelling alarm of old next-due-task with ID $lastDueTaskAlarmID.")
            cancelAlarmForTask(context, lastDueTaskAlarmID!!)
        }

        val kindOfAlarm = setAlarm(context, alarmId, alarmTime)
        // Logging.
        val durationAsInt = TimeUnit.SECONDS.toMinutes(reminderTime - now)
        val duration = durationAsInt.toDuration(DurationUnit.MINUTES)
        Log.i(TAG, "$kindOfAlarm alarm set for $todoTask at $canonicalTimestamp which is in $duration.")
        // Message to the user if something changed at next-due-task-alarm.
        val localizedTimestamp = Helper.createLocalizedDateTimeString(alarmTime)
        val message = context.getString(R.string.next_reminder, localizedTimestamp, duration.toString(), todoTask.getName())
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()

        lastDueTaskAlarmID = alarmId
        lastDueTaskAlarmTime = alarmTime
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
