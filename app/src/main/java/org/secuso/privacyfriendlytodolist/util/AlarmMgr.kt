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

    fun setAlarmForTask(context: Context, alarmId: Int, reminderTime: Long) {
        if (reminderTime != -1L) {
            var alarmTime = Helper.getCurrentTimestamp()
            if (reminderTime > alarmTime) {
                alarmTime = reminderTime
            }
            val date = Date(TimeUnit.SECONDS.toMillis(alarmTime))
            val calendar = Calendar.getInstance()
            calendar.setTime(date)
            val pendingIntent = getPendingAlarmIntent(context, alarmId, true)!!
            getManager(context)[AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis()] = pendingIntent
            Log.i(TAG, "Alarm $alarmId set at ${Helper.getDateTime(calendar)}.")
        } else {
            Log.d(TAG, "Alarm $alarmId not set because it has no reminder time.")
        }
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
