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

package org.secuso.privacyfriendlytodolist.model.impl

import android.content.Context
import android.os.Handler
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.secuso.privacyfriendlytodolist.model.Model
import org.secuso.privacyfriendlytodolist.model.ModelServices
import org.secuso.privacyfriendlytodolist.model.ModelServices.DeliveryOption
import org.secuso.privacyfriendlytodolist.model.ResultConsumer
import org.secuso.privacyfriendlytodolist.model.TodoList
import org.secuso.privacyfriendlytodolist.model.TodoSubtask
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.model.Tuple
import org.secuso.privacyfriendlytodolist.model.database.TodoListDatabase
import org.secuso.privacyfriendlytodolist.model.database.TodoListDatabase.Companion.getInstance
import org.secuso.privacyfriendlytodolist.model.database.entities.TodoListData
import org.secuso.privacyfriendlytodolist.model.database.entities.TodoSubtaskData
import org.secuso.privacyfriendlytodolist.model.database.entities.TodoTaskData
import org.secuso.privacyfriendlytodolist.model.impl.BaseTodoImpl.RequiredDBAction

class ModelServicesImpl(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val resultHandler: Handler): ModelServices {

    companion object {
        private val TAG: String = ModelServicesImpl::class.java.simpleName
    }

    private var db: TodoListDatabase = getInstance(context)

    override fun getTaskById(todoTaskId: Int,
                             deliveryOption: DeliveryOption,
                             resultConsumer: ResultConsumer<TodoTask?>): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val todoTaskData = db.getTodoTaskDao().getById(todoTaskId)
            var todoTask: TodoTask? = null
            if (null != todoTaskData) {
                todoTask = loadTasksSubtasks(false, todoTaskData)[0]
            }
            dispatchResult(deliveryOption, resultConsumer, todoTask)
        }
    }

    override fun getNextDueTask(now: Long,
                                deliveryOption: DeliveryOption,
                                resultConsumer: ResultConsumer<TodoTask?>): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val data = getNextDueTaskBlocking(now)
            dispatchResult(deliveryOption, resultConsumer, data)
        }
    }

    private suspend fun getNextDueTaskBlocking(now: Long): TodoTask? {
        val nextDueTaskData = db.getTodoTaskDao().getNextDueTask(now)
        var nextDueTask: TodoTask? = null
        if (null != nextDueTaskData) {
            nextDueTask = loadTasksSubtasks(false, nextDueTaskData)[0]
        }
        return nextDueTask
    }

    override fun getTasksToRemind(now: Long, lockedIds: Set<Int>?,
                                  deliveryOption: DeliveryOption,
                                  resultConsumer: ResultConsumer<MutableList<TodoTask>>): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val dataArray = db.getTodoTaskDao().getAllToRemind(now, lockedIds)
            val tasksToRemind = loadTasksSubtasks(false, *dataArray)

            // get task that is next due
            val nextDueTask = getNextDueTaskBlocking(now)
            if (nextDueTask != null) {
                tasksToRemind.add(nextDueTask as TodoTaskImpl)
            }
            @Suppress("UNCHECKED_CAST")
            dispatchResult(deliveryOption, resultConsumer, tasksToRemind as MutableList<TodoTask>)
        }
    }

    override fun deleteTodoList(todoListId: Int,
                                deliveryOption: DeliveryOption?,
                                resultConsumer: ResultConsumer<Int>?): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            var counter = 0
            val todoListData = db.getTodoListDao().getById(todoListId)
            if (null != todoListData) {
                val todoList = loadListsTasksSubtasks(todoListData)[0]
                for (task in todoList.getTasks()) {
                    counter += setTaskAndSubtasksInRecycleBinBlocking(task, true)
                }
                Log.i(TAG, "$counter tasks put into recycle bin while removing list")
                counter = db.getTodoListDao().delete(todoList.data)
            }
            Log.i(TAG, "$counter lists removed from database")
            dispatchResult(deliveryOption, resultConsumer, counter)
            notifyDataChanged(counter)
        }
    }

    override fun deleteTodoTask(todoTask: TodoTask,
                                deliveryOption: DeliveryOption?,
                                resultConsumer: ResultConsumer<Int>?): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val counter = deleteTodoTaskBlocking(todoTask)
            dispatchResult(deliveryOption, resultConsumer, counter)
            notifyDataChanged(counter)
        }
    }

    private suspend fun deleteTodoTaskBlocking(todoTask: TodoTask): Int {
        var counter = 0
        for (subtask in todoTask.getSubtasks()) {
            counter += deleteTodoSubtaskBlocking(subtask)
        }
        Log.i(TAG, "$counter subtasks removed from database while removing task")
        val todoTaskImpl = todoTask as TodoTaskImpl
        counter = db.getTodoTaskDao().delete(todoTaskImpl.data)
        Log.i(TAG, "$counter tasks removed from database")
        return counter
    }

    override fun deleteTodoSubtask(subtask: TodoSubtask,
                                   deliveryOption: DeliveryOption?,
                                   resultConsumer: ResultConsumer<Int>?): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val counter = deleteTodoSubtaskBlocking(subtask)
            dispatchResult(deliveryOption, resultConsumer, counter)
            notifyDataChanged(counter)
        }
    }

    private suspend fun deleteTodoSubtaskBlocking(subtask: TodoSubtask): Int {
        val todoSubtaskImpl = subtask as TodoSubtaskImpl
        val counter = db.getTodoSubtaskDao().delete(todoSubtaskImpl.data)
        Log.i(TAG, "$counter subtasks removed from database")
        return counter
    }

    override fun setTaskAndSubtasksInRecycleBin(todoTask: TodoTask,
                                                inRecycleBin: Boolean,
                                                deliveryOption: DeliveryOption?,
                                                resultConsumer: ResultConsumer<Int>?): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val counter = setTaskAndSubtasksInRecycleBinBlocking(todoTask, inRecycleBin)
            dispatchResult(deliveryOption, resultConsumer, counter)
            notifyDataChanged(counter)
        }
    }

    private suspend fun setTaskAndSubtasksInRecycleBinBlocking(todoTask: TodoTask,
                                                               inRecycleBin: Boolean): Int {
        var counter = 0
        for (subtask in todoTask.getSubtasks()) {
            counter += setSubtaskInRecycleBinBlocking(subtask, inRecycleBin)
        }
        Log.i(TAG, "$counter subtasks put into recycle bin while putting task into recycle bin")
        val todoTaskImpl = todoTask as TodoTaskImpl
        todoTaskImpl.setInRecycleBin(inRecycleBin)
        counter = db.getTodoTaskDao().update(todoTaskImpl.data)
        Log.i(TAG, "$counter tasks put into recycle bin")
        return counter
    }

    override fun setSubtaskInRecycleBin(subtask: TodoSubtask,
                                        inRecycleBin: Boolean,
                                        deliveryOption: DeliveryOption?,
                                        resultConsumer: ResultConsumer<Int>?): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val counter = setSubtaskInRecycleBinBlocking(subtask, inRecycleBin)
            dispatchResult(deliveryOption, resultConsumer, counter)
            notifyDataChanged(counter)
        }
    }

    private suspend fun setSubtaskInRecycleBinBlocking(subtask: TodoSubtask,
                                                       inRecycleBin: Boolean): Int {
        val todoSubtaskImpl = subtask as TodoSubtaskImpl
        todoSubtaskImpl.setInRecycleBin(inRecycleBin)
        val counter = db.getTodoSubtaskDao().update(todoSubtaskImpl.data)
        Log.i(TAG, "$counter subtasks put into recycle bin")
        return counter
    }

    override fun getNumberOfAllListsAndTasks(deliveryOption: DeliveryOption,
                                             resultConsumer: ResultConsumer<Tuple<Int, Int>>): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val numberOfLists = db.getTodoListDao().getCount()
            val numberOfTasksNotInRecycleBin = db.getTodoTaskDao().getCountNotInRecycleBin()
            dispatchResult(deliveryOption, resultConsumer, Tuple(numberOfLists, numberOfTasksNotInRecycleBin))
        }
    }

    override fun getAllToDoTasks(deliveryOption: DeliveryOption,
                                 resultConsumer: ResultConsumer<MutableList<TodoTask>>): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val dataArray = db.getTodoTaskDao().getAllNotInRecycleBin()
            val data = loadTasksSubtasks(false, *dataArray)
            @Suppress("UNCHECKED_CAST")
            dispatchResult(deliveryOption, resultConsumer, data as MutableList<TodoTask>)
        }
    }

    override fun getRecycleBin(deliveryOption: DeliveryOption,
                               resultConsumer: ResultConsumer<MutableList<TodoTask>>): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val dataArray = db.getTodoTaskDao().getAllInRecycleBin()
            val data = loadTasksSubtasks(true, *dataArray)
            @Suppress("UNCHECKED_CAST")
            dispatchResult(deliveryOption, resultConsumer, data as MutableList<TodoTask>)
        }
    }

    override fun clearRecycleBin(deliveryOption: DeliveryOption?,
                                 resultConsumer: ResultConsumer<Int>?): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val dataArray = db.getTodoTaskDao().getAllInRecycleBin()
            val todoTasks = loadTasksSubtasks(true, *dataArray)
            var counter = 0
            for (todoTask in todoTasks) {
                counter += deleteTodoTaskBlocking(todoTask)
            }
            dispatchResult(deliveryOption, resultConsumer, counter)
            notifyDataChanged(counter)
        }
    }

    override fun getAllToDoLists(deliveryOption: DeliveryOption,
                                 resultConsumer: ResultConsumer<MutableList<TodoList>>): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val dataArray = db.getTodoListDao().getAll()
            val todoLists = loadListsTasksSubtasks(*dataArray)
            @Suppress("UNCHECKED_CAST")
            dispatchResult(deliveryOption, resultConsumer, todoLists as MutableList<TodoList>)
        }
    }

    override fun getAllToDoListNames(deliveryOption: DeliveryOption,
                                     resultConsumer: ResultConsumer<MutableList<String>>): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val dataArray = db.getTodoListDao().getAllNames()
            val todoListNames = dataArray.toCollection(ArrayList(dataArray.size))
            dispatchResult(deliveryOption, resultConsumer, todoListNames)
        }
    }

    override fun getToDoListById(todoListId: Int,
                                 deliveryOption: DeliveryOption,
                                 resultConsumer: ResultConsumer<TodoList?>): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val todoListData = db.getTodoListDao().getById(todoListId)
            var todoList: TodoList? = null
            if (null != todoListData) {
                todoList = loadListsTasksSubtasks(todoListData)[0]
            }
            dispatchResult(deliveryOption, resultConsumer, todoList)
        }
    }

    // returns the id of the todolist
    override fun saveTodoListInDb(todoList: TodoList,
                                  deliveryOption: DeliveryOption?,
                                  resultConsumer: ResultConsumer<Int>?): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val todoListImpl = todoList as TodoListImpl
            val data = todoListImpl.data
            var counter = 0
            when (todoListImpl.requiredDBAction) {
                RequiredDBAction.INSERT -> {
                    todoListImpl.setId(db.getTodoListDao().insert(data).toInt())
                    counter = 1
                    Log.d(TAG, "Todo list was inserted into DB: $data")
                }

                RequiredDBAction.UPDATE -> {
                    counter = db.getTodoListDao().update(data)
                    Log.d(TAG, "Todo list was updated in DB (return code $counter): $data")
                }

                else -> {}
            }
            todoListImpl.setUnchanged()
            dispatchResult(deliveryOption, resultConsumer, counter)
            notifyDataChanged(counter)
        }
    }

    override fun saveTodoTaskInDb(todoTask: TodoTask,
                                  deliveryOption: DeliveryOption?,
                                  resultConsumer: ResultConsumer<Int>?): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val counter = saveTodoTaskInDbBlocking(todoTask)
            dispatchResult(deliveryOption, resultConsumer, counter)
            notifyDataChanged(counter)
        }
    }

    override fun saveTodoTaskAndSubtasksInDb(todoTask: TodoTask,
                                             deliveryOption: DeliveryOption?,
                                             resultConsumer: ResultConsumer<Int>?): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            var counter = saveTodoTaskInDbBlocking(todoTask)
            for (subtask in todoTask.getSubtasks()) {
                counter += saveTodoSubtaskInDbBlocking(subtask)
            }
            dispatchResult(deliveryOption, resultConsumer, counter)
            notifyDataChanged(counter)
        }
    }

    private suspend fun saveTodoTaskInDbBlocking(todoTask: TodoTask): Int {
        val todoTaskImpl = todoTask as TodoTaskImpl
        val data = todoTaskImpl.data
        var counter = 0
        when (todoTaskImpl.requiredDBAction) {
            RequiredDBAction.INSERT -> {
                todoTaskImpl.setId(db.getTodoTaskDao().insert(data).toInt())
                counter = 1
                Log.d(TAG, "Todo task was inserted into DB: $data")
            }

            RequiredDBAction.UPDATE -> {
                counter = db.getTodoTaskDao().update(data)
                Log.d(TAG, "Todo task was updated in DB (return code $counter): $data")
            }

            RequiredDBAction.UPDATE_FROM_POMODORO -> {
                counter = db.getTodoTaskDao().updateValuesFromPomodoro(
                    todoTaskImpl.getId(),
                    todoTaskImpl.getName(),
                    todoTaskImpl.getProgress(false),
                    todoTaskImpl.isDone()
                )
                Log.d(TAG, "Todo task was updated in DB by values from pomodoro (return code $counter): $data")
            }

            else -> {}
        }
        todoTaskImpl.setUnchanged()
        return counter
    }

    override fun saveTodoSubtaskInDb(todoSubtask: TodoSubtask,
                                     deliveryOption: DeliveryOption?,
                                     resultConsumer: ResultConsumer<Int>?): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            val counter = saveTodoSubtaskInDbBlocking(todoSubtask)
            dispatchResult(deliveryOption, resultConsumer, counter)
            notifyDataChanged(counter)
        }
    }

    private suspend fun saveTodoSubtaskInDbBlocking(todoSubtask: TodoSubtask): Int {
        val todoSubtaskImpl = todoSubtask as TodoSubtaskImpl
        val data = todoSubtaskImpl.data
        var counter = 0
        when (todoSubtaskImpl.requiredDBAction) {
            RequiredDBAction.INSERT -> {
                todoSubtaskImpl.setId(db.getTodoSubtaskDao().insert(data).toInt())
                counter = 1
                Log.d(TAG, "Todo subtask was inserted into DB: $data")
            }

            RequiredDBAction.UPDATE -> {
                counter = db.getTodoSubtaskDao().update(data)
                Log.d(TAG, "Todo subtask was updated in DB (return code $counter): $data")
            }

            else -> {}
        }
        todoSubtaskImpl.setUnchanged()
        return counter
    }

    override fun deleteAllData(deliveryOption: DeliveryOption?,
                               resultConsumer: ResultConsumer<Int>?): Job {
        return coroutineScope.launch(Dispatchers.IO) {
            var counter = db.getTodoSubtaskDao().deleteAll()
            counter += db.getTodoTaskDao().deleteAll()
            counter += db.getTodoListDao().deleteAll()
            dispatchResult(deliveryOption, resultConsumer, counter)
            notifyDataChanged(counter)
        }
    }

    private suspend fun loadListsTasksSubtasks(vararg dataArray: TodoListData): MutableList<TodoListImpl> {
        val lists = ArrayList<TodoListImpl>()
        for (data in dataArray) {
            val list = TodoListImpl(data)
            val dataArray2 = db.getTodoTaskDao().getAllOfListNotInRecycleBin(list.getId())
            val tasks = loadTasksSubtasks(false, *dataArray2)
            for (task in tasks) {
                task.setListId(list.getId())
            }
            @Suppress("UNCHECKED_CAST")
            list.setTasks(tasks as MutableList<TodoTask>)
            lists.add(list)
        }
        return lists
    }

    private suspend fun loadTasksSubtasks(
        subtasksFromRecycleBinToo: Boolean,
        vararg dataArray: TodoTaskData
    ): MutableList<TodoTaskImpl> {
        val tasks = ArrayList<TodoTaskImpl>()
        for (data in dataArray) {
            val task = TodoTaskImpl(data)
            val dataArray2 = if (subtasksFromRecycleBinToo)
                db.getTodoSubtaskDao().getAllOfTask(task.getId()) else
                db.getTodoSubtaskDao().getAllOfTaskNotInRecycleBin(task.getId())
            val subtasks = loadSubtasks(*dataArray2)
            @Suppress("UNCHECKED_CAST")
            task.setSubtasks(subtasks as MutableList<TodoSubtask>)
            tasks.add(task)
        }
        return tasks
    }

    private fun loadSubtasks(vararg dataArray: TodoSubtaskData): MutableList<TodoSubtaskImpl> {
        val subtasks = ArrayList<TodoSubtaskImpl>()
        for (data in dataArray) {
            val subtask = TodoSubtaskImpl(data)
            subtasks.add(subtask)
        }
        return subtasks
    }

    private inline fun <reified T>dispatchResult(deliveryOption: DeliveryOption?,
                                                 resultConsumer: ResultConsumer<T>?, result: T) {
        if (null != deliveryOption && null != resultConsumer) {
            if (deliveryOption == DeliveryOption.POST) {
                if (!resultHandler.post { resultConsumer.consume(result) }) {
                    Log.e(TAG, "Failed to post data model result of type " + T::class.java.simpleName)
                }
            } else {
                // DeliveryOption.DIRECT
                resultConsumer.consume(result)
            }
        }
    }

    private fun notifyDataChanged(changedItems: Int) {
        if (changedItems > 0) {
            if (!resultHandler.post { Model.notifyDataChanged(context) }) {
                Log.e(TAG, "Failed to post Model.notifyDataChanged().")
            }
        }
    }
}