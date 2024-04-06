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
package org.secuso.privacyfriendlytodolist.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AutoStartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == "android.intent.action.BOOT_COMPLETED") {
            val serviceClass = ReminderService::class.java
            val reminderServiceIntent = Intent(context, serviceClass)
            context.startService(reminderServiceIntent)
            Log.i(TAG, serviceClass.getSimpleName() + " was started by $action.")
        } else {
            Log.e(TAG, "Received unexpected action '$action'.")
        }
    }

    companion object {
        private val TAG = AutoStartReceiver::class.java.getSimpleName()
    }
}
