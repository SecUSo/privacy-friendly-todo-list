/*
Privacy Friendly To-Do List
Copyright (C) 2024  Christian Adams

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
package org.secuso.privacyfriendlytodolist.model.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todoLists")
data class TodoListData(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    /**
     * Sort order should start with 0. Use -1 as initial value. First entry will get
     * MAX(sortOrder)+1 after inserting, which results in 0.
     */
    var sortOrder: Int = -1,
    var name: String = ""
)