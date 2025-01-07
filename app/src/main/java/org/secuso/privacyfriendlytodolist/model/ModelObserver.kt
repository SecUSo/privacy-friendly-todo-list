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
package org.secuso.privacyfriendlytodolist.model

import android.content.Context

interface ModelObserver {
    /**
     * Gets called when a the data in the data model (lists, tasks, subtasks) gets changed.
     *
     * For example if a task gets changed by the user and saved.
     */
    fun onTodoDataChanged(context: Context, changedLists: Int, changedTasks: Int, changedSubtasks: Int) {
    }

    /**
     * Gets called when a non-GUI component changes the data in the data model (lists, tasks,
     * subtasks) to notify the currently registered GUI components.
     *
     * For example if a task gets changed and saved by a reminder notification action.
     */
    fun onTodoDataChangedFromOutside(context: Context, changedLists: Int, changedTasks: Int, changedSubtasks: Int) {
    }
}