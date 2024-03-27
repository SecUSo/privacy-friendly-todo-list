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
import org.secuso.privacyfriendlytodolist.view.MainActivity

class TodoReReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("todo_id", -1)
        val name = intent.getStringExtra("todo_name")
        val progress = intent.getIntExtra("todo_progress", -1)
        if (id != -1 && name != null) {
            val runIntent = Intent(context, MainActivity::class.java)
            runIntent.putExtra(MainActivity.COMMAND, MainActivity.COMMAND_UPDATE)
                .putExtra("todo_id", id)
                .putExtra("todo_name", name)
                .putExtra("todo_progress", progress)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(runIntent)
        } else {
            Log.e(TAG, "Todo Intent is not complete.")
        }
    }

    companion object {
        private val TAG = TodoReReceiver::class.java.getSimpleName()
    }
}
