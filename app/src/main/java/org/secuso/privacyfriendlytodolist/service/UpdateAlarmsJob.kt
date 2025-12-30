/*
Privacy Friendly To-Do List
Copyright (C) 2024-2025  Christian Adams

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
package org.secuso.privacyfriendlytodolist.service

import android.util.Log
import org.secuso.privacyfriendlytodolist.util.AlarmMgr
import org.secuso.privacyfriendlytodolist.util.LogTag
import org.secuso.privacyfriendlytodolist.util.Timestamp

/**
 * A job service that ensures that an alarm is set for the task with the next reminder.
 * But first it triggers alarms for all tasks with overdue reminders.
 */
class UpdateAlarmsJob : ModelJobBase("Update-alarm-job") {

    override fun onStartJob(): Boolean {
        super.onStartJob()

        // To keep race condition of lost update small, give notification right at the start of the job.
        JobManager.notifyUpdateAlarmJobExecuted()

        // Serialize (async) actions at sub calls to be sure that all actions are done before calling jobFinished.
        triggerAlarmsForOverdueReminders()

        // Return true, if job still runs asynchronously. If returning true, this action shall call
        // jobFinished() after asynchronous tasks have been finished.
        return isJobNotFinished()
    }

    private fun triggerAlarmsForOverdueReminders() {
        model.getTasksWithOverdueReminders(Timestamp.createCurrent()) { tasksAndCounter ->
            if (isJobStopped()) {
                return@getTasksWithOverdueReminders
            }

            if (tasksAndCounter.first.isNotEmpty()) {
                for (todoTask in tasksAndCounter.first) {
                    Log.i(TAG, "$logPrefix Due to overdue reminder starting handle-alarm-job for $todoTask.")
                    JobManager.startHandleAlarmJob(context, todoTask.getId())
                }
            } else {
                Log.d(TAG, "$logPrefix No task with overdue reminder.")
            }

            doUpdateNextAlarm(tasksAndCounter.second)
        }
    }

    private fun doUpdateNextAlarm(alreadyUpdatedTasks: Int) {
        model.getNextTaskToRemind(Timestamp.createCurrent()) { taskAndCounter ->
            if (isJobStopped()) {
                return@getNextTaskToRemind
            }

            val nextTaskToRemind = taskAndCounter.first
            if (null != nextTaskToRemind) {
                AlarmMgr.setAlarmForNextTaskToRemind(context, nextTaskToRemind)
            } else {
                Log.d(TAG, "$logPrefix No next task with due reminder so no alarm to set.")
            }

            val overallUpdatedTasks = alreadyUpdatedTasks + taskAndCounter.second
            if (overallUpdatedTasks > 0) {
                Log.d(TAG, "$overallUpdatedTasks tasks were updated outside UI while updating alarms. Notifying UI!")
                model.notifyDataChangedOutsideAppUI(0, overallUpdatedTasks, 0)
            }

            jobFinished()
        }
    }

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
    }
}