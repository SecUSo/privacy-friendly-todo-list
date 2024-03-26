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
import kotlinx.coroutines.launch
import org.secuso.privacyfriendlytodolist.model.ModelServices
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
import org.secuso.privacyfriendlytodolist.model.impl.BaseTodoImpl.ObjectStates

class ModelServicesImpl(
    context: Context,
    private val coroutineScope: CoroutineScope,
    private val resultHandler: Handler): ModelServices {

    companion object {
        private val TAG: String = ModelServicesImpl::class.java.simpleName
    }

    private var db: TodoListDatabase = getInstance(context)

    override fun getTaskById(todoTaskId: Int, resultConsumer: ResultConsumer<TodoTask?>) {
        coroutineScope.launch(Dispatchers.IO) {
            val todoTaskData = db.getTodoTaskDao().getById(todoTaskId)
            var todoTask: TodoTask? = null
            if (null != todoTaskData) {
                todoTask = loadTasksSubtasks(false, todoTaskData)[0]
            }
            dispatchResult(resultConsumer, todoTask)
        }
    }

    override fun getNextDueTask(today: Long, resultConsumer: ResultConsumer<TodoTask?>) {
        coroutineScope.launch(Dispatchers.IO) {
            val data = getNextDueTaskBlocking(today)
            dispatchResult(resultConsumer, data)
        }
    }

    private suspend fun getNextDueTaskBlocking(today: Long): TodoTask? {
        val nextDueTaskData = db.getTodoTaskDao().getNextDueTask(today)
        var nextDueTask: TodoTask? = null
        if (null != nextDueTaskData) {
            nextDueTask = loadTasksSubtasks(false, nextDueTaskData)[0]
        }
        return nextDueTask
    }

    /**
     * returns a list of tasks
     *
     * -   which are not fulfilled and whose reminder time is prior to the current time
     * -   the task which is next due
     *
     * @param today Date of today.
     * @param lockedIds Tasks for which the user was just notified (these tasks are locked).
     * They will be excluded.
     */
    override fun getTasksToRemind(today: Long, lockedIds: Set<Int>?, resultConsumer: ResultConsumer<List<TodoTask>>) {
        coroutineScope.launch(Dispatchers.IO) {
            val dataArray = db.getTodoTaskDao().getAllToRemind(today, lockedIds)
            val tasksToRemind = loadTasksSubtasks(false, *dataArray)

            // get task that is next due
            val nextDueTask = getNextDueTaskBlocking(today)
            if (nextDueTask != null) {
                tasksToRemind.add(nextDueTask as TodoTaskImpl)
            }
            dispatchResult(resultConsumer, tasksToRemind)
        }
    }

    override fun deleteTodoList(todoListId: Int, resultConsumer: ResultConsumer<Int>?) {
        coroutineScope.launch(Dispatchers.IO) {
            var counter = 0
            val todoListData = db.getTodoListDao().getById(todoListId)
            if (null != todoListData) {
                val todoList = loadListsTasksSubtasks(todoListData)[0]
                for (task in todoList.tasks) {
                    counter += setTaskAndSubtasksInRecycleBinBlocking(task, true)
                }
                Log.i(TAG, "$counter tasks put into recycle bin while removing list")
                counter = db.getTodoListDao().delete(todoList.data)
            }
            Log.i(TAG, "$counter lists removed from database")
            dispatchResult(resultConsumer, counter)
        }
    }

    override fun deleteTodoTask(todoTask: TodoTask, resultConsumer: ResultConsumer<Int>?) {
        coroutineScope.launch(Dispatchers.IO) {
            val data = deleteTodoTaskBlocking(todoTask)
            dispatchResult(resultConsumer, data)
        }
    }

    private suspend fun deleteTodoTaskBlocking(todoTask: TodoTask): Int {
        var counter = 0
        for (subtask in todoTask.subtasks) {
            counter += deleteTodoSubtaskBlocking(subtask)
        }
        Log.i(TAG, "$counter subtasks removed from database while removing task")
        val todoTaskImpl = todoTask as TodoTaskImpl
        counter = db.getTodoTaskDao().delete(todoTaskImpl.data)
        Log.i(TAG, "$counter tasks removed from database")
        return counter
    }

    override fun deleteTodoSubtask(subtask: TodoSubtask, resultConsumer: ResultConsumer<Int>?) {
        coroutineScope.launch(Dispatchers.IO) {
            val data = deleteTodoSubtaskBlocking(subtask)
            dispatchResult(resultConsumer, data)
        }
    }

    private suspend fun deleteTodoSubtaskBlocking(subtask: TodoSubtask): Int {
        val todoSubtaskImpl = subtask as TodoSubtaskImpl
        val counter = db.getTodoSubtaskDao().delete(todoSubtaskImpl.data)
        Log.i(TAG, "$counter subtasks removed from database")
        return counter
    }

    override fun setTaskAndSubtasksInRecycleBin(todoTask: TodoTask, inRecycleBin: Boolean, resultConsumer: ResultConsumer<Int>?) {
        coroutineScope.launch(Dispatchers.IO) {
            val data = setTaskAndSubtasksInRecycleBinBlocking(todoTask, inRecycleBin)
            dispatchResult(resultConsumer, data)
        }
    }

    private suspend fun setTaskAndSubtasksInRecycleBinBlocking(todoTask: TodoTask, inRecycleBin: Boolean): Int {
        var counter = 0
        for (subtask in todoTask.subtasks) {
            counter += setSubtaskInRecycleBinBlocking(subtask, inRecycleBin)
        }
        Log.i(TAG, "$counter subtasks put into recycle bin while putting task into recycle bin")
        val todoTaskImpl = todoTask as TodoTaskImpl
        todoTaskImpl.isInRecycleBin = inRecycleBin
        counter = db.getTodoTaskDao().update(todoTaskImpl.data)
        Log.i(TAG, "$counter tasks put into recycle bin")
        return counter
    }

    override fun setSubtaskInRecycleBin(subtask: TodoSubtask, inRecycleBin: Boolean, resultConsumer: ResultConsumer<Int>?) {
        coroutineScope.launch(Dispatchers.IO) {
            val data = setSubtaskInRecycleBinBlocking(subtask, inRecycleBin)
            dispatchResult(resultConsumer, data)
        }
    }

    private suspend fun setSubtaskInRecycleBinBlocking(subtask: TodoSubtask, inRecycleBin: Boolean): Int {
        val todoSubtaskImpl = subtask as TodoSubtaskImpl
        todoSubtaskImpl.isInRecycleBin = inRecycleBin
        val counter = db.getTodoSubtaskDao().update(todoSubtaskImpl.data)
        Log.i(TAG, "$counter subtasks put into recycle bin")
        return counter
    }

    override fun getNumberOfAllListsAndTasks(resultConsumer: ResultConsumer<Tuple<Int, Int>>) {
        coroutineScope.launch(Dispatchers.IO) {
            val numberOfLists = db.getTodoListDao().getCount()
            val numberOfTasksNotInRecycleBin = db.getTodoTaskDao().getCountNotInRecycleBin()
            dispatchResult(resultConsumer, Tuple(numberOfLists, numberOfTasksNotInRecycleBin))
        }
    }

    override fun getAllToDoTasks(resultConsumer: ResultConsumer<List<TodoTask>>) {
        coroutineScope.launch(Dispatchers.IO) {
            val dataArray = db.getTodoTaskDao().getAllNotInRecycleBin()
            val data = loadTasksSubtasks(false, *dataArray)
            dispatchResult(resultConsumer, data)
        }
    }

    override fun getRecycleBin(resultConsumer: ResultConsumer<List<TodoTask>>) {
        coroutineScope.launch(Dispatchers.IO) {
            val dataArray = db.getTodoTaskDao().getAllInRecycleBin()
            val data = loadTasksSubtasks(true, *dataArray)
            dispatchResult(resultConsumer, data)
        }
    }

    override fun clearRecycleBin(resultConsumer: ResultConsumer<Int>?) {
        coroutineScope.launch(Dispatchers.IO) {
            val dataArray = db.getTodoTaskDao().getAllInRecycleBin()
            val todoTasks = loadTasksSubtasks(true, *dataArray)
            var counter = 0
            for (todoTask in todoTasks) {
                counter += deleteTodoTaskBlocking(todoTask)
            }
            dispatchResult(resultConsumer, counter)
        }
    }

    override fun getAllToDoLists(resultConsumer: ResultConsumer<List<TodoList>>) {
        coroutineScope.launch(Dispatchers.IO) {
            val dataArray = db.getTodoListDao().getAll()
            val todoLists = loadListsTasksSubtasks(*dataArray)
            dispatchResult(resultConsumer, todoLists)
        }
    }

    override fun getToDoListById(todoListId: Int, resultConsumer: ResultConsumer<TodoList?>) {
        coroutineScope.launch(Dispatchers.IO) {
            val todoListData = db.getTodoListDao().getById(todoListId)
            var todoList: TodoList? = null
            if (null != todoListData) {
                todoList = loadListsTasksSubtasks(todoListData)[0]
            }
            dispatchResult(resultConsumer, todoList)
        }
    }

    // returns the id of the todolist
    override fun saveTodoListInDb(todoList: TodoList, resultConsumer: ResultConsumer<Int>?) {
        coroutineScope.launch(Dispatchers.IO) {
            val todoListImpl = todoList as TodoListImpl
            val data = todoListImpl.data
            var counter = 0
            when (todoListImpl.getDBState()) {
                ObjectStates.INSERT_TO_DB -> {
                    todoListImpl.id = db.getTodoListDao().insert(data).toInt()
                    counter = 1
                    Log.d(TAG, "Todo list was inserted into DB: $data")
                }

                ObjectStates.UPDATE_DB -> {
                    counter = db.getTodoListDao().update(data)
                    todoListImpl.id
                    Log.d(TAG, "Todo list was updated in DB (return code $counter): $data")
                }

                else -> {}
            }
            todoListImpl.setUnchanged()
            dispatchResult(resultConsumer, counter)
        }
    }

    override fun saveTodoTaskInDb(todoTask: TodoTask, resultConsumer: ResultConsumer<Int>?) {
        coroutineScope.launch(Dispatchers.IO) {
            val data = saveTodoTaskInDbBlocking(todoTask)
            dispatchResult(resultConsumer, data)
        }
    }

    override fun saveTodoTaskAndSubtasksInDb(todoTask: TodoTask, resultConsumer: ResultConsumer<Int>?) {
        coroutineScope.launch(Dispatchers.IO) {
            val data = saveTodoTaskInDbBlocking(todoTask)
            for (subtask in todoTask.subtasks) {
                saveTodoSubtaskInDbBlocking(subtask)
            }
            dispatchResult(resultConsumer, data)
        }
    }

    private suspend fun saveTodoTaskInDbBlocking(todoTask: TodoTask): Int {
        val todoTaskImpl = todoTask as TodoTaskImpl
        val data = todoTaskImpl.data
        var counter = 0
        when (todoTaskImpl.getDBState()) {
            ObjectStates.INSERT_TO_DB -> {
                todoTaskImpl.id = db.getTodoTaskDao().insert(data).toInt()
                counter = 1
                Log.d(TAG, "Todo task was inserted into DB: $data")
            }

            ObjectStates.UPDATE_DB -> {
                counter = db.getTodoTaskDao().update(data)
                Log.d(TAG, "Todo task was updated in DB (return code $counter): $data")
            }

            ObjectStates.UPDATE_FROM_POMODORO -> {
                counter = db.getTodoTaskDao().updateValuesFromPomodoro(
                    todoTaskImpl.id,
                    todoTaskImpl.name,
                    todoTaskImpl.getProgress(false),
                    todoTaskImpl.isDone
                )
                Log.d(TAG, "Todo task was updated in DB by values from pomodoro (return code $counter): $data")
            }

            else -> {}
        }
        todoTaskImpl.setUnchanged()
        return counter
    }

    override fun saveTodoSubtaskInDb(todoSubtask: TodoSubtask, resultConsumer: ResultConsumer<Int>?) {
        coroutineScope.launch(Dispatchers.IO) {
            val data = saveTodoSubtaskInDbBlocking(todoSubtask)
            dispatchResult(resultConsumer, data)
        }
    }

    private suspend fun saveTodoSubtaskInDbBlocking(todoSubtask: TodoSubtask): Int {
        val todoSubtaskImpl = todoSubtask as TodoSubtaskImpl
        val data = todoSubtaskImpl.data
        var counter = 0
        when (todoSubtaskImpl.getDBState()) {
            ObjectStates.INSERT_TO_DB -> {
                todoSubtaskImpl.id = db.getTodoSubtaskDao().insert(data).toInt()
                counter = 1
                Log.d(TAG, "Todo subtask was inserted into DB: $data")
            }

            ObjectStates.UPDATE_DB -> {
                counter = db.getTodoSubtaskDao().update(data)
                Log.d(TAG, "Todo subtask was updated in DB (return code $counter): $data")
            }

            else -> {}
        }
        todoSubtaskImpl.setUnchanged()
        return counter
    }

    override fun deleteAllData(resultConsumer: ResultConsumer<Int>?) {
        coroutineScope.launch(Dispatchers.IO) {
            var counter = db.getTodoSubtaskDao().deleteAll()
            counter += db.getTodoTaskDao().deleteAll()
            counter += db.getTodoListDao().deleteAll()
            dispatchResult(resultConsumer, counter)
        }
    }

    private suspend fun loadListsTasksSubtasks(vararg dataArray: TodoListData): MutableList<TodoListImpl> {
        val lists = ArrayList<TodoListImpl>()
        for (data in dataArray) {
            val list = TodoListImpl(data)
            val dataArray2 = db.getTodoTaskDao().getAllOfListNotInRecycleBin(list.id)
            val tasks: List<TodoTaskImpl> = loadTasksSubtasks(false, *dataArray2)
            for (task in tasks) {
                task.listId = list.id
            }
            list.tasks = tasks
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
                db.getTodoSubtaskDao().getAllOfTask(task.id) else
                db.getTodoSubtaskDao().getAllOfTaskNotInRecycleBin(task.id)
            val subtasks: List<TodoSubtaskImpl> = loadSubtasks(*dataArray2)
            task.subtasks = subtasks
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

    private inline fun <reified T>dispatchResult(resultConsumer: ResultConsumer<T>?, result: T) {
        if (null != resultConsumer) {
            if (!resultHandler.post { resultConsumer.consume(result) }) {
                Log.e(TAG, "Failed to post data model result of type " + T::class.java.simpleName)
            }
        }
    }
}