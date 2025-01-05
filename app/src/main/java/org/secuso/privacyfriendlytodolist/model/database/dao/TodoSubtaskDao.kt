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

    @Query("DELETE FROM todoSubtasks")
    suspend fun deleteAll(): Int

    @Query("SELECT * FROM todoSubtasks WHERE taskId = :taskId ORDER BY sortOrder ASC")
    suspend fun getAllOfTask(taskId: Int): Array<TodoSubtaskData>

    @Query("SELECT * FROM todoSubtasks WHERE isInRecycleBin = 0 AND taskId = :taskId ORDER BY sortOrder ASC")
    suspend fun getAllOfTaskNotInRecycleBin(taskId: Int): Array<TodoSubtaskData>

    @Query("UPDATE todoSubtasks SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Int, sortOrder: Int): Int

    @Query("UPDATE todoSubtasks SET sortOrder = COALESCE((SELECT MAX(sortOrder) + 1 FROM todoSubtasks WHERE taskId = :taskId OR (taskId IS NULL AND :taskId IS NULL)), 0) WHERE id = :id")
    suspend fun updateSortOrderToLast(id: Int, taskId: Int): Int
}