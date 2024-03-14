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
import android.util.Log
import org.secuso.privacyfriendlytodolist.model.ModelServices
import org.secuso.privacyfriendlytodolist.model.TodoList
import org.secuso.privacyfriendlytodolist.model.TodoSubtask
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.model.database.TodoListDatabase
import org.secuso.privacyfriendlytodolist.model.database.TodoListDatabase.Companion.getInstance
import org.secuso.privacyfriendlytodolist.model.database.entities.TodoListData
import org.secuso.privacyfriendlytodolist.model.database.entities.TodoSubtaskData
import org.secuso.privacyfriendlytodolist.model.database.entities.TodoTaskData
import org.secuso.privacyfriendlytodolist.model.impl.BaseTodoImpl.ObjectStates

class ModelServicesImpl(private val context: Context) : ModelServices {

    companion object {
        private val TAG: String = ModelServicesImpl::class.java.simpleName
    }

    private var db: TodoListDatabase = getInstance(context)

    override fun getContext(): Context {
        return context
    }

    override fun createTodoList(): TodoList {
        return TodoListImpl()
    }

    override fun createTodoTask(): TodoTask {
        return TodoTaskImpl()
    }

    override fun createTodoSubtask(): TodoSubtask {
        return TodoSubtaskImpl()
    }

    override fun getTaskById(todoTaskId: Int): TodoTask? {
        val todoTaskData = db.getTodoTaskDao().getTaskById(todoTaskId)
        var todoTask: TodoTask? = null
        if (null != todoTaskData) {
            todoTask = TodoTaskImpl(todoTaskData)
        }
        return todoTask
    }

    override fun getNextDueTask(today: Long): TodoTask? {
        val nextDueTaskData = db.getTodoTaskDao().getNextDueTask(today)
        var nextDueTask: TodoTask? = null
        if (null != nextDueTaskData) {
            nextDueTask = TodoTaskImpl(nextDueTaskData)
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
    override fun getTasksToRemind(today: Long, lockedIds: Set<Int>?): List<TodoTask> {
        val dataArray = db.getTodoTaskDao().getTasksToRemind(today, lockedIds)
        val tasksToRemind = createTasks(dataArray, false)

        // get task that is next due
        val nextDueTask = getNextDueTask(today)
        if (nextDueTask != null) {
            tasksToRemind.add(nextDueTask)
        }
        return tasksToRemind
    }

    override fun deleteTodoList(todoList: TodoList): Int {
        var counter = 0
        for (task in todoList.tasks) {
            counter += setTaskInTrash(task, true)
        }
        Log.i(TAG, "$counter tasks put into trash while removing list")
        val todoListImpl = todoList as TodoListImpl
        counter = db.getTodoListDao().delete(todoListImpl.data)
        Log.i(TAG, "$counter lists removed from database")
        return counter
    }

    override fun deleteTodoTask(todoTask: TodoTask): Int {
        var counter = 0
        for (subtask in todoTask.subtasks) {
            counter += deleteTodoSubtask(subtask)
        }
        Log.i(TAG, "$counter subtasks removed from database while removing task")
        val todoTaskImpl = todoTask as TodoTaskImpl
        counter = db.getTodoTaskDao().delete(todoTaskImpl.data)
        Log.i(TAG, "$counter tasks removed from database")
        return counter
    }

    override fun deleteTodoSubtask(subtask: TodoSubtask): Int {
        val todoSubtaskImpl = subtask as TodoSubtaskImpl
        val counter = db.getTodoSubtaskDao().delete(todoSubtaskImpl.data)
        Log.i(TAG, "$counter subtasks removed from database")
        return counter
    }

    override fun setTaskInTrash(todoTask: TodoTask, inTrash: Boolean): Int {
        var counter = 0
        for (subtask in todoTask.subtasks) {
            counter += setSubtaskInTrash(subtask, inTrash)
        }
        Log.i(TAG, "$counter subtasks put into trash while putting task into trash")
        val todoTaskImpl = todoTask as TodoTaskImpl
        todoTaskImpl.isInTrash = inTrash
        counter = db.getTodoTaskDao().update(todoTaskImpl.data)
        Log.i(TAG, "$counter tasks put into trash")
        return counter
    }

    override fun setSubtaskInTrash(subtask: TodoSubtask, inTrash: Boolean): Int {
        val todoSubtaskImpl = subtask as TodoSubtaskImpl
        todoSubtaskImpl.isInTrash = inTrash
        val counter = db.getTodoSubtaskDao().update(todoSubtaskImpl.data)
        Log.i(TAG, "$counter subtasks put into trash")
        return counter
    }

    override fun getNumberOfAllToDoTasks(): Int {
        return 0
    }

    override fun getAllToDoTasks(): List<TodoTask> {
        val dataArray = db.getTodoTaskDao().getAllNotInTrash()
        return createTasks(dataArray, false)
    }

    override fun getBin(): List<TodoTask> {
        val dataArray = db.getTodoTaskDao().getAllInTrash()
        return createTasks(dataArray, true)
    }

    override fun getNumberOfAllToDoLists(): Int {
        return 0
    }

    override fun getAllToDoLists(): List<TodoList> {
        val dataArray = db.getTodoListDao().getAll()
        return createLists(dataArray)
    }

    private fun createLists(dataArray: Array<TodoListData>): ArrayList<TodoList> {
        val lists = ArrayList<TodoList>()
        for (data in dataArray) {
            val list = TodoListImpl(data)
            val dataArray2 = db.getTodoTaskDao().getAllOfListNotInTrash(list.id)
            val tasks: List<TodoTask> = createTasks(dataArray2, false)
            for (task in tasks) {
                task.list = list
            }
            list.tasks = tasks
            lists.add(list)
        }
        return lists
    }

    private fun createTasks(
        dataArray: Array<TodoTaskData>,
        subtasksFromTrashToo: Boolean
    ): ArrayList<TodoTask> {
        val tasks = ArrayList<TodoTask>()
        for (data in dataArray) {
            val task = TodoTaskImpl(data)
            val dataArray2 = if (subtasksFromTrashToo) db.getTodoSubtaskDao()
                .getAllOfTask(task.id) else db.getTodoSubtaskDao().getAllOfTaskNotInTrash(task.id)
            val subtasks: List<TodoSubtask> = createSubtasks(dataArray2)
            task.subtasks = subtasks
            tasks.add(task)
        }
        return tasks
    }

    private fun createSubtasks(dataArray: Array<TodoSubtaskData>): ArrayList<TodoSubtask> {
        val subtasks = ArrayList<TodoSubtask>()
        for (data in dataArray) {
            val subtask = TodoSubtaskImpl(data)
            subtasks.add(subtask)
        }
        return subtasks
    }

    // returns the id of the todolist
    override fun saveTodoListInDb(todoList: TodoList): Int {
        val todoListImpl = todoList as TodoListImpl
        val data = todoListImpl.data
        val todoListId: Int
        when (todoListImpl.getDBState()) {
            ObjectStates.INSERT_TO_DB -> {
                todoListId = db.getTodoListDao().insert(data).toInt()
                Log.d(TAG, "Todo list was inserted into DB: $data")
            }

            ObjectStates.UPDATE_DB -> {
                val counter = db.getTodoListDao().update(data)
                todoListId = todoListImpl.id
                Log.d(TAG, "Todo list was updated in DB (return code $counter): $data")
            }

            else -> todoListId = ModelServices.NO_CHANGES
        }
        todoListImpl.setUnchanged()
        return todoListId
    }

    override fun saveTodoTaskInDb(todoTask: TodoTask): Int {
        val todoTaskImpl = todoTask as TodoTaskImpl
        val data = todoTaskImpl.data
        val todoTaskId: Int
        val counter: Int
        when (todoTaskImpl.getDBState()) {
            ObjectStates.INSERT_TO_DB -> {
                todoTaskId = db.getTodoTaskDao().insert(data).toInt()
                Log.d(TAG, "Todo task was inserted into DB: $data")
            }

            ObjectStates.UPDATE_DB -> {
                counter = db.getTodoTaskDao().update(data)
                todoTaskId = todoTaskImpl.id
                Log.d(TAG, "Todo task was updated in DB (return code $counter): $data")
            }

            ObjectStates.UPDATE_FROM_POMODORO -> {
                counter = db.getTodoTaskDao().updateValuesFromPomodoro(
                    todoTaskImpl.id,
                    todoTaskImpl.name,
                    todoTaskImpl.progress,
                    todoTaskImpl.isDone
                )
                todoTaskId = todoTaskImpl.id
                Log.d(
                    TAG,
                    "Todo task was updated in DB by values from pomodoro (return code $counter): $data"
                )
            }

            else -> todoTaskId = ModelServices.NO_CHANGES
        }
        todoTaskImpl.setUnchanged()
        return todoTaskId
    }

    override fun saveTodoSubtaskInDb(subtask: TodoSubtask): Int {
        val todoSubtaskImpl = subtask as TodoSubtaskImpl
        val data = todoSubtaskImpl.data
        val todoSubtaskId: Int
        when (todoSubtaskImpl.getDBState()) {
            ObjectStates.INSERT_TO_DB -> {
                todoSubtaskId = db.getTodoSubtaskDao().insert(data).toInt()
                Log.d(TAG, "Todo subtask was inserted into DB: $data")
            }

            ObjectStates.UPDATE_DB -> {
                val counter = db.getTodoSubtaskDao().update(data)
                todoSubtaskId = todoSubtaskImpl.id
                Log.d(TAG, "Todo subtask was updated in DB (return code $counter): $data")
            }

            else -> todoSubtaskId = ModelServices.NO_CHANGES
        }
        todoSubtaskImpl.setUnchanged()
        return todoSubtaskId
    }

    override fun deleteAllData() {
        db.getTodoSubtaskDao().deleteAll()
        db.getTodoTaskDao().deleteAll()
        db.getTodoListDao().deleteAll()
    }
}