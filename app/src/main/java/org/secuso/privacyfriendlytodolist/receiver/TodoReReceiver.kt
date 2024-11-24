/*
Privacy Friendly To-Do List
Copyright (C) 2020-2024  Fabian Ballreich

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
package org.secuso.privacyfriendlytodolist.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.secuso.privacyfriendlytodolist.util.LogTag
import org.secuso.privacyfriendlytodolist.view.MainActivity

class TodoReReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != ACTION) {
            Log.e(TAG, "Received intent with unexpected action '$action'.")
            return
        }

        Log.i(TAG, "Received intent with action $action. Starting main activity.")
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
        const val ACTION = "org.secuso.privacyfriendlyproductivitytimer.TODO_RE_ACTION"
        private val TAG = LogTag.create(this::class.java.declaringClass)
    }
}
