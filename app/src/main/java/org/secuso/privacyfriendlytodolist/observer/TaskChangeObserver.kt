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
package org.secuso.privacyfriendlytodolist.observer

import android.content.Context
import android.util.Log
import org.secuso.privacyfriendlytodolist.model.ModelObserver
import org.secuso.privacyfriendlytodolist.service.JobManager
import org.secuso.privacyfriendlytodolist.util.LogTag

object TaskChangeObserver: ModelObserver {
    private val TAG = LogTag.create(this::class.java)

    override fun onTodoDataChanged(context: Context, changedLists: Int, changedTasks: Int, changedSubtasks: Int) {
        if (0 != changedTasks) {
            Log.d(TAG, "$changedTasks tasks did change. Starting update-alarm-job.")
            JobManager.startUpdateAlarmJob(context)
        }
    }

    override fun onTodoDataChangedFromOutside(context: Context, changedLists: Int, changedTasks: Int, changedSubtasks: Int) {
        if (0 != changedTasks) {
            Log.d(TAG, "$changedTasks tasks did change from outside. Starting update-alarm-job.")
            JobManager.startUpdateAlarmJob(context)
        }
    }
}