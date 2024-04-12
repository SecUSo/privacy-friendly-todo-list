/*
 This file is part of Privacy Friendly To-Do MutableList.

 Privacy Friendly To-Do MutableList is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.

 Privacy Friendly To-Do MutableList is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Privacy Friendly To-Do MutableList. If not, see <http://www.gnu.org/licenses/>.
 */

package org.secuso.privacyfriendlytodolist.model

/**
 * Created by Christian Adams on 17.02.2024.
 *
 * This class provides an interface to the database services.
 */
interface ModelServices {
    fun getTaskById(todoTaskId: Int, resultConsumer: ResultConsumer<TodoTask?>)
    fun getNextDueTask(now: Long, resultConsumer: ResultConsumer<TodoTask?>)

    /**
     * Returns a list of tasks
     *
     * -   which are not fulfilled and whose reminder time is prior to the current time
     * -   the task which is next due
     *
     * @param now Current time.
     * @param lockedIds Tasks for which the user was just notified (these tasks are locked).
     * They will be excluded from the result.
     * @param resultConsumer Result consumer that will be notified when the asynchronous database
     * access has finished.
     */
    fun getTasksToRemind(now: Long, lockedIds: Set<Int>?, resultConsumer: ResultConsumer<MutableList<TodoTask>>)
    fun deleteTodoList(todoListId: Int, resultConsumer: ResultConsumer<Int>?)
    fun deleteTodoTask(todoTask: TodoTask, resultConsumer: ResultConsumer<Int>?)
    fun deleteTodoSubtask(subtask: TodoSubtask, resultConsumer: ResultConsumer<Int>?)
    fun setTaskAndSubtasksInRecycleBin(todoTask: TodoTask, inRecycleBin: Boolean, resultConsumer: ResultConsumer<Int>?)
    fun setSubtaskInRecycleBin(subtask: TodoSubtask, inRecycleBin: Boolean, resultConsumer: ResultConsumer<Int>?)

    /**
     * Returns the number of all to-do-lists (left in tuple) and all to-do-tasks that are not in recycle bin (right in tuple).
     */
    fun getNumberOfAllListsAndTasks(resultConsumer: ResultConsumer<Tuple<Int, Int>>)
    fun getAllToDoTasks(resultConsumer: ResultConsumer<MutableList<TodoTask>>)
    fun getRecycleBin(resultConsumer: ResultConsumer<MutableList<TodoTask>>)
    fun clearRecycleBin(resultConsumer: ResultConsumer<Int>?)
    fun getAllToDoLists(resultConsumer: ResultConsumer<MutableList<TodoList>>)
    fun getAllToDoListNames(resultConsumer: ResultConsumer<MutableList<String>>)
    fun getToDoListById(todoListId: Int, resultConsumer: ResultConsumer<TodoList?>)
    fun saveTodoSubtaskInDb(todoSubtask: TodoSubtask, resultConsumer: ResultConsumer<Int>?)
    fun saveTodoTaskInDb(todoTask: TodoTask, resultConsumer: ResultConsumer<Int>?)
    fun saveTodoTaskAndSubtasksInDb(todoTask: TodoTask, resultConsumer: ResultConsumer<Int>?)
    fun saveTodoListInDb(todoList: TodoList, resultConsumer: ResultConsumer<Int>?)
    fun deleteAllData(resultConsumer: ResultConsumer<Int>?)
}