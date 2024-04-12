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

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.receiver.AlarmReceiver
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

object AlarmMgr {
    const val KEY_ALARM_ID = "KEY_ALARM_ID"
    private val TAG = AlarmMgr::class.java.getSimpleName()
    private var manager: AlarmManager? = null

    private fun getManager(context: Context): AlarmManager {
        if (manager == null) {
            manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        }
        return manager!!
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
     * @return If an alarm gets set the alarm ID gets returned (task ID gets used as alarm ID).
     * If no alarm gets set null gets returned.
     */
    fun setAlarmForTask(context: Context, todoTask: TodoTask, setAlarmEvenIfItIsInPast: Boolean): Int? {
        if (todoTask.isDone()) {
            Log.i(TAG, "No alarm set because task $todoTask is done.")
            return null
        }

        val reminderTime = todoTask.getReminderTime()
        val alarmTime: Long
        val logMessage: String
        if (reminderTime != -1L) {
            val now = Helper.getCurrentTimestamp()
            if (reminderTime > now) {
                alarmTime = reminderTime
                logMessage = "reminder time"
            } else if (setAlarmEvenIfItIsInPast) {
                alarmTime = now
                logMessage = "reminder time is in the past, using 'now'"
            } else {
                Log.i(TAG, "No alarm set because reminder time of task $todoTask is in the past.")
                return null
            }
        } else {
            Log.i(TAG, "No alarm set because task $todoTask has no reminder time.")
            return null
        }

        val date = Date(TimeUnit.SECONDS.toMillis(alarmTime))
        val calendar = Calendar.getInstance()
        calendar.setTime(date)
        // Use task's database ID as unique alarm ID.
        val alarmId = todoTask.getId()
        val pendingIntent = getPendingAlarmIntent(context, alarmId, true)!!
        getManager(context)[AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis()] = pendingIntent
        Log.i(TAG, "Alarm set for task $todoTask at ${Helper.getDateTime(calendar)} ($logMessage).")
        return alarmId
    }

    fun cancelAlarmForTask(context: Context, alarmId: Int): Boolean {
        val pendingIntent = getPendingAlarmIntent(context, alarmId, false)
        var alarmWasSet = false
        if (pendingIntent != null) {
            alarmWasSet = true
            getManager(context).cancel(pendingIntent)
            Log.i(TAG, "Alarm $alarmId cancelled.")
        }
        return alarmWasSet
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
