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

package org.secuso.privacyfriendlytodolist.model.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.secuso.privacyfriendlytodolist.model.TodoTask.Priority

@Entity(tableName = "todo_task")
data class TodoTaskData(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var name: String? = null,
    var listId: Int = 0,
    var listPosition: Int = 0,
    var description: String? = null,
    var priority: Priority = Priority.MEDIUM,
    var deadline: Long = 0,
    var reminderTime: Long = -1,
    var progress: Int = 0,
    var numberSubtasks: Int = 0,
    var isDone: Boolean = false,
    var isInTrash: Boolean = false
)