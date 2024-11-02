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
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object AlarmMgr {
    const val KEY_ALARM_ID = "KEY_ALARM_ID"
    private val TAG = LogTag.create(this::class.java)
    private var manager: AlarmManager? = null

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

    fun setAlarmForAllTasks(context: Context) {
        val now = Helper.getCurrentTimestamp()
        val viewModel = CustomViewModel(context)
        viewModel.model.getNextDueTaskAndOverdueTasks(now) { dueTasks ->
            for (dueTask in dueTasks) {
                setAlarmForTask(context, dueTask, true)
            }
        }
    }

    /**
     * Sets an alarm for the given task if it is not done and has a reminder time.
     *
     * Timestamp of alarm is determined as follows:
     * - If task has reminder time:
     *      - If it is later than current time it gets used
     *      - Otherwise, if setAlarmEvenItsInPast is true, current time gets used
     *      - Otherwise no alarm gets set
     *
     * @param context
     * @param todoTask
     * @param setAlarmEvenIfItIsInPast Set alarm even if reminder time is in past. In this case
     * 'now' gets used as alarm time.
     * @param showMessage A message gets shown to the user that notifies about a created alarm.
     * 
     * @return If an alarm gets set the alarm ID gets returned (task ID gets used as alarm ID).
     * If no alarm gets set null gets returned.
     */
    fun setAlarmForTask(context: Context, todoTask: TodoTask,
                        setAlarmEvenIfItIsInPast: Boolean = false,
                        showMessage: Boolean = false): Int? {
        // Use task's database ID as unique alarm ID.
        val alarmId = todoTask.getId()
        cancelAlarmForTask(context, alarmId)

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

        val alarmTime: Long
        val duration: Duration
        val logDetail: String
        if (reminderTime > now) {
            // Get full minutes as alarm time.
            alarmTime = reminderTime - (reminderTime % 60)
            duration = (reminderTime - now).toDuration(DurationUnit.SECONDS)
            logDetail = "reminder time"
        } else if (setAlarmEvenIfItIsInPast) {
            alarmTime = now
            duration = Duration.ZERO
            logDetail = "reminder time is in the past, using 'now'"
        } else {
            Log.i(TAG, "No alarm set because reminder time of $todoTask is in the past.")
            return null
        }

        val kindOfAlarm = setAlarm(context, alarmId, alarmTime)
        var timestamp = Helper.createCanonicalDateTimeString(alarmTime)
        Log.i(TAG, "$kindOfAlarm alarm set for $todoTask at $timestamp which is in $duration ($logDetail).")
        if (showMessage) {
            timestamp = Helper.createLocalizedDateTimeString(alarmTime)
            val message = context.getString(R.string.alarm_set_for_task, timestamp, duration.toString(), todoTask.getName())
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
        return alarmId
    }

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
