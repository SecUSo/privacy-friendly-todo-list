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

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.ModelServices
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.util.AlarmMgr
import org.secuso.privacyfriendlytodolist.util.Helper
import org.secuso.privacyfriendlytodolist.util.NotificationMgr
import org.secuso.privacyfriendlytodolist.viewmodel.CustomViewModel

class ReminderService : Service() {
    class ReminderServiceBinder(val service: ReminderService) : Binder()

    private var viewModel: CustomViewModel? = null
    private var model: ModelServices? = null
    private val binder: IBinder = ReminderServiceBinder(this)
    private var isAlreadyRunning = false

    override fun onCreate() {
        super.onCreate()

        Log.i(TAG, "Reminder service created.")
        viewModel = CustomViewModel(applicationContext)
        model = viewModel!!.model
    }

    override fun onDestroy() {
        super.onDestroy()

        model = null
        viewModel!!.destroy()
        viewModel = null
        Log.i(TAG, "Reminder service destroyed.")
    }

    override fun onBind(intent: Intent): IBinder {
        Log.i(TAG, "onBind()" + if (intent.extras != null) " with extra" else "")
        return binder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand()")
        val extras = intent.extras
        if (extras == null) {
            //  service was started for the first time
            if (!isAlreadyRunning) {
                Log.i(TAG, "Service was started the first time.")
                // If this service gets killed, alreadyRunning will be false the next time.
                // However, the service is only killed when the resources are scarce. So we
                // deliberately set the alarms again after restarting the service.
                isAlreadyRunning = true
                processStartUp()
            } else {
                Log.i(TAG, "Service was already running.")
            }
        } else {
            processAlarm(extras.getInt(AlarmMgr.KEY_ALARM_ID))
        }
        return START_NOT_STICKY // do not recreate service if the phone runs out of memory
    }

    private fun processStartUp() {
        //  Service was started for the first time
        reloadAlarms()
    }

    private fun processAlarm(todoTaskId: Int) {
        model!!.getTaskById(todoTaskId) { todoTask ->
            if (null != todoTask) {
                notifyAboutAlarm(todoTask)
            } else {
                Log.e(TAG, "Unable to process alarm. No task with ID $todoTaskId was found.")
            }
        }
        model!!.getNextDueTask(Helper.getCurrentTimestamp()) { todoTask ->
            if (null != todoTask) {
                AlarmMgr.setAlarmForTask(this, todoTask.getId(), todoTask.getReminderTime())
            }
        }
    }

    private fun notifyAboutAlarm(task: TodoTask) {
        Log.d(TAG, "Notifying about alarm for task $task.")
        val title = task.getName()
        val deadline = Helper.getDateTime(task.getDeadline())
        val message = applicationContext.resources.getString(R.string.deadline_approaching, deadline)
        NotificationMgr.post(this, title, message, task)
    }

    private fun reloadAlarms() {
        NotificationMgr.cancelAll(this) // First cancel all alarms
        model!!.getTasksToRemind(Helper.getCurrentTimestamp(), null) { tasksToRemind ->
            // Then set alarms
            for (todoTask in tasksToRemind) {
                AlarmMgr.setAlarmForTask(this, todoTask.getId(), todoTask.getReminderTime())
            }
            if (tasksToRemind.isEmpty()) {
                Log.i(TAG, "No alarms set.")
            }
        }
    }

    companion object {
        private val TAG = ReminderService::class.java.simpleName

        fun processAutoStart(context: Context) {
            context.startService(Intent(context, ReminderService::class.java))
        }

        fun processAlarm(context: Context, alarmId: Int) {
            val intent = Intent(context, ReminderService::class.java)
            intent.putExtra(AlarmMgr.KEY_ALARM_ID, alarmId)
            context.startService(intent)
        }
    }
}