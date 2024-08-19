package org.secuso.privacyfriendlytodolist.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.secuso.privacyfriendlytodolist.service.JobManager
import org.secuso.privacyfriendlytodolist.util.LogTag

class PermissionStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != ACTION) {
            Log.e(TAG, "Received intent with unexpected action '$action'.")
            return
        }

        Log.i(TAG, "Received intent with action $action. Reloading alarms.")
        JobManager.processAlarmPermissionStateChanged(context)
    }

    companion object {
        const val ACTION = "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"
        private val TAG = LogTag.create(this::class.java.declaringClass)
    }
}