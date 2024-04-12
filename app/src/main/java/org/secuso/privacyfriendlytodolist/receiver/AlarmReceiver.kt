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
package org.secuso.privacyfriendlytodolist.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.secuso.privacyfriendlytodolist.service.JobManager
import org.secuso.privacyfriendlytodolist.util.AlarmMgr

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != ACTION) {
            Log.e(TAG, "Received intent with unexpected action '$action'.")
            return
        }

        val extras = intent.extras
        if (null != extras && extras.containsKey(AlarmMgr.KEY_ALARM_ID)) {
            val alarmId = extras.getInt(AlarmMgr.KEY_ALARM_ID)
            Log.i(TAG, "Received intent with action $action and alarm ID $alarmId. Starting reminder service.")
            JobManager.processAlarm(context, alarmId)
        } else {
            Log.e(TAG, "Received alarm without alarm ID.")
        }
    }

    companion object {
        const val ACTION = "org.secuso.privacyfriendlytodolist.ALARM"
        private val TAG = AlarmReceiver::class.java.getSimpleName()
    }
}
