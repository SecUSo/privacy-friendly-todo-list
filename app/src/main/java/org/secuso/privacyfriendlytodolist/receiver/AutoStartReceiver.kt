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
import org.secuso.privacyfriendlytodolist.service.ReminderService

class AutoStartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != ACTION) {
            Log.e(TAG, "Received intent with unexpected action '$action'.")
            return
        }

        Log.i(TAG, "Received intent with action $action. Starting reminder service.")
        ReminderService.processAutoStart(context)
    }

    companion object {
        const val ACTION = "android.intent.action.BOOT_COMPLETED"
        private val TAG = AutoStartReceiver::class.java.getSimpleName()
    }
}
