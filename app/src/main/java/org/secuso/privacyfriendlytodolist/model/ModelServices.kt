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

package org.secuso.privacyfriendlytodolist.model

import android.content.Context

/**
 * Created by Christian Adams on 17.02.2024.
 *
 * This class provides an interface to the database services.
 */
interface ModelServices {
    fun getContext(): Context
    fun createTodoList(): TodoList
    fun createTodoTask(): TodoTask
    fun createTodoSubtask(): TodoSubtask
    fun getTaskById(todoTaskId: Int): TodoTask?
    fun getNextDueTask(today: Long): TodoTask?

    /**
     * returns a list of tasks
     *
     * -   which are not fulfilled and whose reminder time is prior to the current time
     * -   the task which is next due
     */
    fun getTasksToRemind(today: Long, lockedIds: Set<Int>?): List<TodoTask>
    fun deleteTodoList(todoList: TodoList): Int
    fun deleteTodoTask(todoTask: TodoTask): Int
    fun deleteTodoSubtask(subtask: TodoSubtask): Int
    fun setTaskInTrash(todoTask: TodoTask, inTrash: Boolean): Int
    fun setSubtaskInTrash(subtask: TodoSubtask, inTrash: Boolean): Int
    fun getNumberOfAllToDoTasks(): Int
    fun getAllToDoTasks(): List<TodoTask>
    fun getBin(): List<TodoTask>
    fun getNumberOfAllToDoLists(): Int
    fun getAllToDoLists(): List<TodoList>
    fun saveTodoSubtaskInDb(subtask: TodoSubtask): Int
    fun saveTodoTaskInDb(todoTask: TodoTask): Int

    // returns the id of the todolist
    fun saveTodoListInDb(todoList: TodoList): Int
    fun deleteAllData()

    companion object {
        const val NO_CHANGES = -2
    }
}
