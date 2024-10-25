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
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

import org.secuso.privacyfriendlytodolist.model.database.entities.TodoTaskData

@Dao
interface TodoTaskDao {
    @Insert
    suspend fun insert(todoTaskData: TodoTaskData): Long

    @Update
    suspend fun update(todoTaskData: TodoTaskData): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(todoTaskData: TodoTaskData)

    @Delete
    suspend fun delete(todoTaskData: TodoTaskData): Int

    @Query("DELETE FROM todoTasks")
    suspend fun deleteAll(): Int

    @Query("SELECT COUNT(id) FROM todoTasks WHERE isInRecycleBin = 0")
    suspend fun getCountNotInRecycleBin(): Int

    @Query("SELECT * FROM todoTasks WHERE id = :todoTaskId LIMIT 1")
    suspend fun getById(todoTaskId: Int): TodoTaskData?

    @Query("SELECT * FROM todoTasks WHERE isInRecycleBin = 0 AND listId = :todoListId ORDER BY sortOrder ASC")
    suspend fun getByListIdNotInRecycleBin(todoListId: Int): Array<TodoTaskData>

    @Query("SELECT * FROM todoTasks" +
            " WHERE isInRecycleBin = 0 AND doneTime IS NULL AND reminderTime IS NOT NULL AND reminderTime > :now" +
            " ORDER BY ABS(reminderTime - :now)" +
            " LIMIT 1")
    suspend fun getNextDueTask(now: Long): TodoTaskData?

    @Query("SELECT todoTasks.* FROM todoTasks" +
            " LEFT JOIN todoLists" +
            " ON todoTasks.listId = todoLists.id" +
            " WHERE isInRecycleBin = 0 AND recurrencePattern <> 0 AND reminderTime IS NOT NULL AND reminderTime <= :now" +
            " ORDER BY todoLists.sortOrder ASC, todoTasks.sortOrder ASC")
    suspend fun getOverdueRecurringTasks(now: Long): Array<TodoTaskData>

    @Query("SELECT todoTasks.* FROM todoTasks" +
            " LEFT JOIN todoLists" +
            " ON todoTasks.listId = todoLists.id" +
            " WHERE isInRecycleBin = 0 AND recurrencePattern = 0 AND doneTime IS NULL AND reminderTime IS NOT NULL AND reminderTime <= :now" +
            " ORDER BY todoLists.sortOrder ASC, todoTasks.sortOrder ASC")
    suspend fun getOverdueTasks(now: Long): Array<TodoTaskData>

    @Query("SELECT todoTasks.* FROM todoTasks" +
            " LEFT JOIN todoLists" +
            " ON todoTasks.listId = todoLists.id" +
            " WHERE isInRecycleBin = 0" +
            " ORDER BY todoLists.sortOrder ASC, todoTasks.sortOrder ASC")
    suspend fun getAllNotInRecycleBin(): Array<TodoTaskData>

    @Query("SELECT todoTasks.* FROM todoTasks" +
            " LEFT JOIN todoLists" +
            " ON todoTasks.listId = todoLists.id" +
            " WHERE isInRecycleBin <> 0" +
            " ORDER BY todoLists.sortOrder ASC, todoTasks.sortOrder ASC")
    suspend fun getAllInRecycleBin(): Array<TodoTaskData>

    @Query("SELECT * FROM todoTasks WHERE isInRecycleBin = 0 AND listId = :listId ORDER BY sortOrder ASC")
    suspend fun getAllOfListNotInRecycleBin(listId: Int): Array<TodoTaskData>

    @Query("UPDATE todoTasks SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Int, sortOrder: Int): Int

    @Query("UPDATE todoTasks SET sortOrder = COALESCE((SELECT MAX(sortOrder) + 1 FROM todoTasks WHERE listId = :listId OR (listId IS NULL AND :listId IS NULL)), 0) WHERE id = :id")
    suspend fun updateSortOrderToLast(id: Int, listId: Int?): Int

    @Query("UPDATE todoTasks SET name = :name, progress = :progress, doneTime = :doneTime WHERE id = :id")
    suspend fun updateValuesFromPomodoro(id: Int, name: String, progress: Int, doneTime: Long?): Int
}