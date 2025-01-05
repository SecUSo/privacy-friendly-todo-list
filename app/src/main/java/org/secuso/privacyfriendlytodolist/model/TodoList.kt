/*
Privacy Friendly To-Do List
Copyright (C) 2018-2024  Sebastian Lutz

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

import android.os.Parcelable
import org.secuso.privacyfriendlytodolist.model.TodoTask.Urgency

/**
 * Created by Sebastian Lutz on 12.03.2018.
 *
 * Class to set up a To-Do List and its parameters.
 */
interface TodoList : BaseTodo, Parcelable {
    fun setId(id: Int)
    fun getId(): Int
    fun isDummyList(): Boolean
    fun setSortOrder(sortOrder: Int)
    fun getSortOrder(): Int
    fun setName(name: String)
    fun getName(): String
    fun getSize(): Int
    fun setTasks(tasks: MutableList<TodoTask>)
    fun getTasks(): MutableList<TodoTask>
    fun getColor(): Int
    fun getDoneTodos(): Int
    fun getNextDeadline(): Long?
    fun getUrgency(reminderTimeSpan: Long): Urgency
    fun checkQueryMatch(query: String?, recursive: Boolean): Boolean
    fun checkQueryMatch(query: String?): Boolean
}
