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

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.AlarmManagerHolder
import org.secuso.privacyfriendlytodolist.model.ModelServices
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.util.Helper
import org.secuso.privacyfriendlytodolist.viewmodel.CustomViewModel
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

class ReminderService : Service() {
    class ReminderServiceBinder(val service: ReminderService) : Binder()

    private var viewModel: CustomViewModel? = null
    private var model: ModelServices? = null
    private lateinit var alarmManager: AlarmManager
    private lateinit var notificationHelper: NotificationHelper
    private val binder: IBinder = ReminderServiceBinder(this)
    private var isAlreadyRunning = false

    override fun onCreate() {
        super.onCreate()

        Log.i(TAG, "Reminder service created.")
        viewModel = CustomViewModel(applicationContext)
        model = viewModel!!.model
        alarmManager = AlarmManagerHolder.getAlarmManager(applicationContext)
        notificationHelper = NotificationHelper(applicationContext)
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
            processAlarm(extras.getInt(KEY_ALARM_TASK_ID))
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
                setAlarmForTask(todoTask)
            }
        }
    }

    private fun notifyAboutAlarm(task: TodoTask) {
        Log.d(TAG, "Notifying about alarm for task $task.")
        val title = task.getName()
        val deadline = Helper.getDateTime(task.getDeadline())
        val message = applicationContext.resources.getString(R.string.deadline_approaching, deadline)
        val nb = notificationHelper.getNotification(title, message, task)
        val notification = nb.build()
        notificationHelper.getManager().notify(task.getId(), notification)
    }

    private fun reloadAlarms() {
        notificationHelper.getManager().cancelAll() // cancel all alarms
        model!!.getTasksToRemind(Helper.getCurrentTimestamp(), null) { tasksToRemind ->
            // set alarms
            for (todoTask in tasksToRemind) {
                setAlarmForTask(todoTask)
            }
            if (tasksToRemind.isEmpty()) {
                Log.i(TAG, "No alarms set.")
            }
        }
    }

    private fun setAlarmForTask(task: TodoTask) {
        val context = applicationContext
        val alarmID = task.getId() // use database id as unique alarm id
        val alarmIntent = Intent(context, ReminderService::class.java)
        alarmIntent.putExtra(KEY_ALARM_TASK_ID, alarmID)
        val pendingAlarmIntent = PendingIntent.getService(context, alarmID, alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val calendar = Calendar.getInstance()
        val reminderTime = task.getReminderTime()
        if (reminderTime != -1L && reminderTime <= Helper.getCurrentTimestamp()) {
            val date = Date(TimeUnit.SECONDS.toMillis(Helper.getCurrentTimestamp()))
            calendar.time = date
            alarmManager[AlarmManager.RTC_WAKEUP, calendar.timeInMillis] = pendingAlarmIntent
            Log.i(TAG, "Alarm set for task $task at " + Helper.getDateTime(calendar.timeInMillis / 1000) + " (note: alarm id == task id)")
        } else if (reminderTime != -1L) {
            val date = Date(TimeUnit.SECONDS.toMillis(reminderTime)) // convert to milliseconds
            calendar.time = date
            alarmManager[AlarmManager.RTC_WAKEUP, calendar.timeInMillis] = pendingAlarmIntent
            Log.i(TAG, "Alarm set for task $task at " + Helper.getDateTime(calendar.timeInMillis / 1000) + " (note: alarm id == task id)")
        } else {
            Log.d(TAG, "No alarm set for task $task because it has no reminder time (note: alarm id == task id)")
        }
    }

    fun processTodoTask(todoTaskId: Int) {
        val modelCopy = model
        if (modelCopy == null) {
            Log.e(TAG, "Reminder service is not ready to process todo task now.")
            return
        }

        // TODO add more granularity: You don't need to change the alarm if the name or the description of the task were changed. You actually need this perform the following steps if the reminder time or the "done" status were modified.
        modelCopy.getTaskById(todoTaskId) { changedTask ->
            if (null != changedTask) {
                val context = applicationContext
                val alarmIntent = PendingIntent.getBroadcast(
                    context, changedTask.getId(),
                    Intent(context, ReminderService::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                // check if alarm was set for this task
                if (alarmIntent != null) {

                    // 1. cancel old alarm
                    alarmManager.cancel(alarmIntent)
                    Log.i(TAG, "Alarm of task $changedTask cancelled.")

                    // 2. delete old notification if it exists
                    notificationHelper.getManager().cancel(changedTask.getId())
                    Log.i(TAG, "Notification of task $changedTask deleted (if exists).")
                } else {
                    Log.w(TAG, "No alarm found for task $changedTask.")
                }
                setAlarmForTask(changedTask)
            } else {
                Log.e(TAG, "Task with ID $todoTaskId not found.")
            }
        }
    }

    companion object {
        private val TAG = ReminderService::class.java.simpleName
        private const val KEY_ALARM_TASK_ID = "KEY_ALARM_TASK_ID"
    }
}