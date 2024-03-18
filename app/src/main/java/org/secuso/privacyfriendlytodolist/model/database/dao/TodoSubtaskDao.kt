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

package org.secuso.privacyfriendlytodolist.model.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

import org.secuso.privacyfriendlytodolist.model.database.entities.TodoSubtaskData

@Dao
interface TodoSubtaskDao {
    @Insert
    suspend fun insert(todoSubtaskData: TodoSubtaskData): Long
    @Update
    suspend fun update(todoSubtaskData: TodoSubtaskData): Int
    @Delete
    suspend fun delete(todoSubtaskData: TodoSubtaskData): Int
    @Query("SELECT * FROM todo_subtask WHERE taskId = :taskId")
    suspend fun getAllOfTask(taskId: Int): Array<TodoSubtaskData>
    @Query("SELECT * FROM todo_subtask WHERE taskId = :taskId AND isInRecycleBin = 0")
    suspend fun getAllOfTaskNotInRecycleBin(taskId: Int): Array<TodoSubtaskData>
    @Query("DELETE FROM todo_subtask")
    suspend fun deleteAll(): Int
}