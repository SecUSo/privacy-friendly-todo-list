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

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.secuso.privacyfriendlytodolist.model.impl.ModelServicesImpl
import org.secuso.privacyfriendlytodolist.util.LogTag

/**
 * Created by Christian Adams on 17.02.2024.
 *
 * This class provides (asynchronous) access to the database services.
 */
class ModelServices(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val resultHandler: Handler) {

    /**
     * The model services work asynchronously to not block the callers thread. These delivery
     * options allow to decide how the result of a model service gets delivered.
     */
    enum class DeliveryOption {
        /**
         * The results get posted via the result handler that was given to the model services while
         * creating them. The result consumer will work in the result handler thread.
         * This is usually the caller thread which is usually the main thread / GUI thread.
         *
         * If the result consumer is optional and not set, the result gets discarded.
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

    private var services = ModelServicesImpl(context)

    fun getTaskById(todoTaskId: Int,
                    deliveryOption: DeliveryOption = DeliveryOption.POST,
                    resultConsumer: ResultConsumer<TodoTask?>): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val todoTask = services.getTaskById(todoTaskId)
            dispatchResult(deliveryOption, resultConsumer, todoTask)
        }
    }

    fun getNextDueTask(now: Long,
                       deliveryOption: DeliveryOption = DeliveryOption.POST,
                       resultConsumer: ResultConsumer<TodoTask?>): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val todoTask = services.getNextDueTask(now)
            dispatchResult(deliveryOption, resultConsumer, todoTask.left)
            notifyDataChanged(todoTask.right)
        }
    }

    /**
     * Returns a list of tasks
     *
     * -   which are not fulfilled and whose reminder time is prior to the current time
     * -   the task which is next due
     *
     * @param now Current time.
     * @param resultConsumer Result consumer that will be notified when the asynchronous database
     * access has finished.
     */
    fun getNextDueTaskAndOverdueTasks(now: Long, deliveryOption: DeliveryOption = DeliveryOption.POST,
                         resultConsumer: ResultConsumer<MutableList<TodoTask>>): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val tasksToRemind = services.getNextDueTaskAndOverdueTasks(now)
            dispatchResult(deliveryOption, resultConsumer, tasksToRemind.left)
            notifyDataChanged(tasksToRemind.right)
        }
    }

    fun deleteTodoList(todoListId: Int,
                       deliveryOption: DeliveryOption = DeliveryOption.POST,
                       resultConsumer: ResultConsumer<Int>? = null): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val counter = services.deleteTodoList(todoListId)
            dispatchResult(deliveryOption, resultConsumer, counter)
            notifyDataChanged(counter)
        }
    }

    fun deleteTodoTask(todoTask: TodoTask,
                       deliveryOption: DeliveryOption = DeliveryOption.POST,
                       resultConsumer: ResultConsumer<Int>? = null): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val counter = services.deleteTodoTask(todoTask)
            dispatchResult(deliveryOption, resultConsumer, counter)
            notifyDataChanged(counter)
        }
    }

    fun deleteTodoSubtask(subtask: TodoSubtask,
                          deliveryOption: DeliveryOption = DeliveryOption.POST,
                          resultConsumer: ResultConsumer<Int>? = null): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val counter = services.deleteTodoSubtask(subtask)
            dispatchResult(deliveryOption, resultConsumer, counter)
            notifyDataChanged(counter)
        }
    }

    fun setTaskAndSubtasksInRecycleBin(todoTask: TodoTask,
                                       inRecycleBin: Boolean,
                                       deliveryOption: DeliveryOption = DeliveryOption.POST,
                                       resultConsumer: ResultConsumer<Int>? = null): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val counter = services.setTaskAndSubtasksInRecycleBin(todoTask, inRecycleBin)
            dispatchResult(deliveryOption, resultConsumer, counter)
            notifyDataChanged(counter)
        }
    }

    fun setSubtaskInRecycleBin(subtask: TodoSubtask,
                               inRecycleBin: Boolean,
                               deliveryOption: DeliveryOption = DeliveryOption.POST,
                               resultConsumer: ResultConsumer<Int>? = null): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val counter = services.setSubtaskInRecycleBin(subtask, inRecycleBin)
            dispatchResult(deliveryOption, resultConsumer, counter)
            notifyDataChanged(counter)
        }
    }

    /**
     * Returns the number of all to-do-lists (left in tuple) and all to-do-tasks that are not in recycle bin (right in tuple).
     */
    fun getNumberOfAllListsAndTasks(deliveryOption: DeliveryOption = DeliveryOption.POST,
                                    resultConsumer: ResultConsumer<Tuple<Int, Int>>): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val result = services.getNumberOfAllListsAndTasks()
            dispatchResult(deliveryOption, resultConsumer, result)
        }
    }

    fun getAllToDoTasks(deliveryOption: DeliveryOption = DeliveryOption.POST,
                        resultConsumer: ResultConsumer<MutableList<TodoTask>>): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val todoTasks = services.getAllToDoTasks()
            dispatchResult(deliveryOption, resultConsumer, todoTasks)
        }
    }

    fun getAllToDoTasksOfList(todoListId: Int,
                              deliveryOption: DeliveryOption = DeliveryOption.POST,
                              resultConsumer: ResultConsumer<MutableList<TodoTask>>): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val todoTasks = services.getAllToDoTasksOfList(todoListId)
            dispatchResult(deliveryOption, resultConsumer, todoTasks)
        }
    }

    fun getRecycleBin(deliveryOption: DeliveryOption = DeliveryOption.POST,
                      resultConsumer: ResultConsumer<MutableList<TodoTask>>): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val todoTasks = services.getRecycleBin()
            dispatchResult(deliveryOption, resultConsumer, todoTasks)
        }
    }

    fun clearRecycleBin(deliveryOption: DeliveryOption = DeliveryOption.POST,
                        resultConsumer: ResultConsumer<Int>? = null): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val counter = services.clearRecycleBin()
            dispatchResult(deliveryOption, resultConsumer, counter)
            notifyDataChanged(counter)
        }
    }

    fun getAllToDoLists(deliveryOption: DeliveryOption = DeliveryOption.POST,
                        resultConsumer: ResultConsumer<MutableList<TodoList>>): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val todoLists = services.getAllToDoLists()
            dispatchResult(deliveryOption, resultConsumer, todoLists)
        }
    }

    fun getAllToDoListNames(deliveryOption: DeliveryOption = DeliveryOption.POST,
                            resultConsumer: ResultConsumer<Map<Int, String>>): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val result = services.getAllToDoListNames()
            dispatchResult(deliveryOption, resultConsumer, result)
        }
    }

    fun getToDoListById(todoListId: Int,
                        deliveryOption: DeliveryOption = DeliveryOption.POST,
                        resultConsumer: ResultConsumer<TodoList?>): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val todoList = services.getToDoListById(todoListId)
            dispatchResult(deliveryOption, resultConsumer, todoList)
        }
    }

    // returns the id of the todolist
    fun saveTodoListInDb(todoList: TodoList,
                         deliveryOption: DeliveryOption = DeliveryOption.POST,
                         resultConsumer: ResultConsumer<Int>? = null): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val counter = services.saveTodoListInDb(todoList)
            dispatchResult(deliveryOption, resultConsumer, counter)
            notifyDataChanged(counter)
        }
    }

    fun saveTodoTaskInDb(todoTask: TodoTask,
                         deliveryOption: DeliveryOption = DeliveryOption.POST,
                         resultConsumer: ResultConsumer<Int>? = null): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val counter = services.saveTodoTaskInDb(todoTask)
            dispatchResult(deliveryOption, resultConsumer, counter)
            notifyDataChanged(counter)
        }
    }

    fun saveTodoTaskAndSubtasksInDb(todoTask: TodoTask,
                                    deliveryOption: DeliveryOption = DeliveryOption.POST,
                                    resultConsumer: ResultConsumer<Int>? = null): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val counter = services.saveTodoTaskAndSubtasksInDb(todoTask)
            dispatchResult(deliveryOption, resultConsumer, counter)
            notifyDataChanged(counter)
        }
    }

    fun saveTodoSubtaskInDb(todoSubtask: TodoSubtask,
                            deliveryOption: DeliveryOption = DeliveryOption.POST,
                            resultConsumer: ResultConsumer<Int>? = null): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val counter = services.saveTodoSubtaskInDb(todoSubtask)
            dispatchResult(deliveryOption, resultConsumer, counter)
            notifyDataChanged(counter)
        }
    }

    fun saveTodoListsSortOrderInDb(todoLists: List<TodoList>,
                                      deliveryOption: DeliveryOption = DeliveryOption.POST,
                                      resultConsumer: ResultConsumer<Int>? = null): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val counter = services.saveTodoListsSortOrderInDb(todoLists)
            dispatchResult(deliveryOption, resultConsumer, counter)
            notifyDataChanged(counter)
        }
    }

    fun saveTodoTasksSortOrderInDb(todoTasks: List<TodoTask>,
                                      deliveryOption: DeliveryOption = DeliveryOption.POST,
                                      resultConsumer: ResultConsumer<Int>? = null): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val counter = services.saveTodoTasksSortOrderInDb(todoTasks)
            dispatchResult(deliveryOption, resultConsumer, counter)
            notifyDataChanged(counter)
        }
    }

    fun saveTodoSubtasksSortOrderInDb(todoSubtasks: List<TodoSubtask>,
                                         deliveryOption: DeliveryOption = DeliveryOption.POST,
                                         resultConsumer: ResultConsumer<Int>? = null): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val counter = services.saveTodoSubtasksSortOrderInDb(todoSubtasks)
            dispatchResult(deliveryOption, resultConsumer, counter)
            notifyDataChanged(counter)
        }
    }

    fun deleteAllData(deliveryOption: DeliveryOption = DeliveryOption.POST,
                      resultConsumer: ResultConsumer<Int>? = null): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val counter = services.deleteAllData()
            dispatchResult(deliveryOption, resultConsumer, counter)
            notifyDataChanged(counter)
        }
    }

    fun exportCSVData(listId: Int?, hasAutoProgress: Boolean, csvDataUri: Uri,
                      deliveryOption: DeliveryOption = DeliveryOption.POST,
                      resultConsumer: ResultConsumer<String?>? = null): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val errorMessage = services.exportCSVData(listId, hasAutoProgress, csvDataUri)
            dispatchResult(deliveryOption, resultConsumer, errorMessage)
        }
    }

    fun importCSVData(deleteAllDataBeforeImport: Boolean, csvDataUri: Uri,
                      deliveryOption: DeliveryOption = DeliveryOption.POST,
                      resultConsumer: ResultConsumer<String?>? = null): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val result = services.importCSVData(deleteAllDataBeforeImport, csvDataUri)
            dispatchResult(deliveryOption, resultConsumer, result.left)
            notifyDataChanged(result.right)
        }
    }

    private inline fun <reified T>dispatchResult(deliveryOption: DeliveryOption,
                                                 resultConsumer: ResultConsumer<T>?,
                                                 result: T) {
        if (null != resultConsumer) {
            when (deliveryOption) {
                DeliveryOption.POST -> {
                    if (!resultHandler.post { resultConsumer.consume(result) }) {
                        Log.e(TAG, "Failed to post data model result of type " + T::class.java.simpleName)
                    }
                }
                DeliveryOption.DIRECT -> {
                    resultConsumer.consume(result)
                }
            }
        }
    }

    /**
     * Do always post, never direct. Model changes always need to be handled in GUI thread.
     */
    private fun notifyDataChanged(changedItems: Int) {
        if (changedItems > 0) {
            if (!resultHandler.post { Model.notifyDataChanged(context) }) {
                Log.e(TAG, "Failed to post Model.notifyDataChanged().")
            }
        }
    }

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
    }
}
