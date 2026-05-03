/*
Privacy Friendly To-Do List
Copyright (C) 2026  Christian Adams

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
package org.secuso.privacyfriendlytodolist.util

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import org.secuso.privacyfriendlytodolist.model.TodoTask

/**
 * Class to filter and sort To Do tasks depending on current preferences.
 */
class TaskFilterSorter(context: Context) {

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    // FILTER AND SORTING OPTIONS MADE BY THE USER
    var queryString: String? = null
    var taskFilter: TaskFilter
    private val taskComparator = TaskComparator()
    var isGroupingByPriority: Boolean
        get() = taskComparator.isGroupingByPriority
        set(value) { taskComparator.isGroupingByPriority = value }
    var isSortingByDeadline: Boolean
        get() = taskComparator.isSortingByDeadline
        set(value) { taskComparator.isSortingByDeadline = value }
    var isSortingByNameAsc: Boolean
        get() = taskComparator.isSortingByNameAsc
        set(value) { taskComparator.isSortingByNameAsc = value }

    init {
        val taskFilterString = prefs.getString(PreferenceMgr.P_TASK_FILTER.name, TaskFilter.ALL_TASKS.name)
        taskFilter = TaskFilter.fromString(taskFilterString)
        isGroupingByPriority = prefs.getBoolean(PreferenceMgr.P_GROUP_BY_PRIORITY.name, false)
        isSortingByDeadline = prefs.getBoolean(PreferenceMgr.P_SORT_BY_DEADLINE.name, false)
        isSortingByNameAsc = prefs.getBoolean(PreferenceMgr.P_SORT_BY_NAME_ASC.name, false)
    }

    /**
     * filter tasks by "done" criterion (show "all", only "open" or only "completed" tasks)
     * If the user changes the filter, it is crucial to call "sortTasks" again.
     */
    fun filterAndSortTasks(todoTasks: List<TodoTask>): MutableList<TodoTask> {
        val fiSoTasks: MutableList<TodoTask> = ArrayList()
        for (todoTask in todoTasks) {
            if (todoTask.checkQueryMatch(queryString)) {
                when (taskFilter) {
                    TaskFilter.OPEN_TASKS -> {
                        if (!todoTask.isDone()) {
                            fiSoTasks.add(todoTask)
                        }
                    }

                    TaskFilter.COMPLETED_TASKS -> {
                        if (todoTask.isDone()) {
                            fiSoTasks.add(todoTask)
                        }
                    }

                    else -> {
                        fiSoTasks.add(todoTask)
                    }
                }
            }
        }

        // Call this method even if sorting is disabled. In the case of enabled sorting, all
        // sorting patterns are automatically employed after having changed the filter on tasks.
        fiSoTasks.sortWith { todoTask1, todoTask2 ->
            taskComparator.compare(todoTask1, todoTask2)
        }

        return fiSoTasks
    }
}
