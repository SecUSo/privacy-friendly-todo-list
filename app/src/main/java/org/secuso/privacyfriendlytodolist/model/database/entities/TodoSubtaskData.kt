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
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "todoSubtasks",
    indices = [
        Index("taskId")
    ],
    foreignKeys = [
        ForeignKey(
            entity = TodoTaskData::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("taskId"))
    ]
)
data class TodoSubtaskData(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var taskId: Int = 0,
    var name: String = "",
    var isDone: Boolean = false,
    var isInRecycleBin: Boolean = false
)