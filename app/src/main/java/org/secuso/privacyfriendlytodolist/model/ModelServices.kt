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

import kotlinx.coroutines.Job

/**
 * Created by Christian Adams on 17.02.2024.
 *
 * This class provides an interface to the database services.
 */
interface ModelServices {
    /**
     * The model services work asynchronously to not block the callers thread. These delivery
     * options allow to decide how the result of a model service gets delivered.
     */
    enum class DeliveryOption {
        /**
         * The results get posted via the result handler that was given to the model services while
         * creating them. The result consumer will work in the result handler thread.
         * This is usually the caller thread which is usually the main thread / GUI thread.
         */
        POST,

        /**
         * The results get posted directly from the asynchronous worker thread. So the result
         * consumer will run asynchronously to the caller thread.
         * This can be used to wait for the results, if asynchronous behavior is not wished.
         *
         * <pre>
         * var changedTodoTasks: List<TodoTask>? = null
         * val job = model.someService(DeliveryOption.DIRECT) { todoTasks ->
         *     changedTodoTasks = todoTasks
         * }
         * runBlocking {
         *     job.join()
         * }
         * if (null != changedTodoTasks) {
         *     // ...
         * }
         * </pre>
         */
        DIRECT
    }
    
    fun getTaskById(todoTaskId: Int, deliveryOption: DeliveryOption = DeliveryOption.POST, resultConsumer: ResultConsumer<TodoTask?>): Job
    fun getNextDueTask(now: Long, deliveryOption: DeliveryOption = DeliveryOption.POST, resultConsumer: ResultConsumer<TodoTask?>): Job

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
    fun getTasksToRemind(now: Long, lockedIds: Set<Int>?, deliveryOption: DeliveryOption = DeliveryOption.POST, resultConsumer: ResultConsumer<MutableList<TodoTask>>): Job
    fun deleteTodoList(todoListId: Int, deliveryOption: DeliveryOption? = DeliveryOption.POST, resultConsumer: ResultConsumer<Int>?): Job
    fun deleteTodoTask(todoTask: TodoTask, deliveryOption: DeliveryOption? = DeliveryOption.POST, resultConsumer: ResultConsumer<Int>?): Job
    fun deleteTodoSubtask(subtask: TodoSubtask, deliveryOption: DeliveryOption? = DeliveryOption.POST, resultConsumer: ResultConsumer<Int>?): Job
    fun setTaskAndSubtasksInRecycleBin(todoTask: TodoTask, inRecycleBin: Boolean, deliveryOption: DeliveryOption? = DeliveryOption.POST, resultConsumer: ResultConsumer<Int>?): Job
    fun setSubtaskInRecycleBin(subtask: TodoSubtask, inRecycleBin: Boolean, deliveryOption: DeliveryOption? = DeliveryOption.POST, resultConsumer: ResultConsumer<Int>?): Job

    /**
     * Returns the number of all to-do-lists (left in tuple) and all to-do-tasks that are not in recycle bin (right in tuple).
     */
    fun getNumberOfAllListsAndTasks(deliveryOption: DeliveryOption = DeliveryOption.POST, resultConsumer: ResultConsumer<Tuple<Int, Int>>): Job
    fun getAllToDoTasks(deliveryOption: DeliveryOption = DeliveryOption.POST, resultConsumer: ResultConsumer<MutableList<TodoTask>>): Job
    fun getRecycleBin(deliveryOption: DeliveryOption = DeliveryOption.POST, resultConsumer: ResultConsumer<MutableList<TodoTask>>): Job
    fun clearRecycleBin(deliveryOption: DeliveryOption? = DeliveryOption.POST, resultConsumer: ResultConsumer<Int>?): Job
    fun getAllToDoLists(deliveryOption: DeliveryOption = DeliveryOption.POST, resultConsumer: ResultConsumer<MutableList<TodoList>>): Job
    fun getAllToDoListNames(deliveryOption: DeliveryOption = DeliveryOption.POST, resultConsumer: ResultConsumer<MutableList<String>>): Job
    fun getToDoListById(todoListId: Int, deliveryOption: DeliveryOption = DeliveryOption.POST, resultConsumer: ResultConsumer<TodoList?>): Job
    fun saveTodoSubtaskInDb(todoSubtask: TodoSubtask, deliveryOption: DeliveryOption? = DeliveryOption.POST, resultConsumer: ResultConsumer<Int>?): Job
    fun saveTodoTaskInDb(todoTask: TodoTask, deliveryOption: DeliveryOption? = DeliveryOption.POST, resultConsumer: ResultConsumer<Int>?): Job
    fun saveTodoTaskAndSubtasksInDb(todoTask: TodoTask, deliveryOption: DeliveryOption? = DeliveryOption.POST, resultConsumer: ResultConsumer<Int>?): Job
    fun saveTodoListInDb(todoList: TodoList, deliveryOption: DeliveryOption? = DeliveryOption.POST, resultConsumer: ResultConsumer<Int>?): Job
    fun deleteAllData(deliveryOption: DeliveryOption? = DeliveryOption.POST, resultConsumer: ResultConsumer<Int>?): Job
}