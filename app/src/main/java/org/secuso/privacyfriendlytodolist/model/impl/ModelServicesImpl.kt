/*
Privacy Friendly To-Do List
Copyright (C) 2018-2024  Sebastian Lutz

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
package org.secuso.privacyfriendlytodolist.model.impl

import android.content.Context
import android.net.Uri
import android.util.Log
import org.secuso.privacyfriendlytodolist.model.TodoList
import org.secuso.privacyfriendlytodolist.model.TodoSubtask
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.model.Tuple
import org.secuso.privacyfriendlytodolist.model.database.TodoListDatabase
import org.secuso.privacyfriendlytodolist.model.database.entities.TodoListData
import org.secuso.privacyfriendlytodolist.model.database.entities.TodoSubtaskData
import org.secuso.privacyfriendlytodolist.model.database.entities.TodoTaskData
import org.secuso.privacyfriendlytodolist.model.impl.BaseTodoImpl.RequiredDBAction
import org.secuso.privacyfriendlytodolist.util.Helper
import org.secuso.privacyfriendlytodolist.util.LogTag
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream

class ModelServicesImpl(private val context: Context) {

    private var db = TodoListDatabase.getInstance(context)

    suspend fun getTaskById(todoTaskId: Int): TodoTask? {
        val todoTaskData = db.getTodoTaskDao().getById(todoTaskId)
        var todoTask: TodoTask? = null
        if (null != todoTaskData) {
            todoTask = loadTasksSubtasks(false, todoTaskData)[0]
        }
        return todoTask
    }

    private suspend fun updateReminderTimeOfRecurringTasks(now: Long): Int {
        val dataArray = db.getTodoTaskDao().getOverdueRecurringTasks(now)
        val todoTasks = loadTasksSubtasks(false, *dataArray)
        for (todoTask in todoTasks) {
            // Note: getOverdueRecurringTasks() ensures that reminderTime and recurrencePattern is set.
            val oldReminderTime = todoTask.getReminderTime()!!
            // Get the upcoming due date of the recurring task.
            val newReminderTime = Helper.getNextRecurringDate(oldReminderTime,
                todoTask.getRecurrencePattern(), todoTask.getRecurrenceInterval(), now)
            todoTask.setReminderTime(newReminderTime)
            todoTask.setChanged()
            if (saveTodoTaskInDb(todoTask) > 0) {
                Log.i(TAG, "Updating reminder time of $todoTask from " +
                        "${Helper.createCanonicalDateTimeString(oldReminderTime)} to " +
                        "${Helper.createCanonicalDateTimeString(newReminderTime)}.")
            } else {
                Log.e(TAG, "Failed to save $todoTask after updating reminder time.")
            }
        }
        return todoTasks.size
    }

    suspend fun getNextDueTask(now: Long): Tuple<TodoTask?, Int> {
        // Ensure that reminder time of recurring tasks is up-to-date before determining next due task.
        val updatedTasks = updateReminderTimeOfRecurringTasks(now)
        // Get next due task.
        val nextDueTaskData = db.getTodoTaskDao().getNextDueTask(now)
        var todoTask: TodoTask? = null
        if (null != nextDueTaskData) {
            todoTask = loadTasksSubtasks(false, nextDueTaskData)[0]
        }
        return Tuple(todoTask, updatedTasks)
    }

    suspend fun getOverdueTasks(now: Long): MutableList<TodoTask> {
        val dataArray = db.getTodoTaskDao().getOverdueTasks(now)
        val data = loadTasksSubtasks(false, *dataArray)
        @Suppress("UNCHECKED_CAST")
        return data as MutableList<TodoTask>
    }

    suspend fun deleteTodoList(todoListId: Int): Triple<Int, Int, Int> {
        var counterLists = 0
        var counterTasks = 0
        var counterSubtasks = 0
        val todoListData = db.getTodoListDao().getById(todoListId)
        if (null != todoListData) {
            val todoList = loadListsTasksSubtasks(todoListData)[0]
            for (task in todoList.getTasks()) {
                val counter = setTaskAndSubtasksInRecycleBin(task, true)
                counterTasks += counter.left
                counterSubtasks += counter.right
            }
            counterLists += db.getTodoListDao().delete(todoList.data)
        }
        Log.i(TAG, "$counterLists list with $counterTasks tasks and $counterSubtasks subtasks removed from database.")
        return Triple(counterLists, counterTasks, counterSubtasks)
    }

    suspend fun deleteTodoTaskAndSubtasks(todoTask: TodoTask): Tuple<Int, Int> {
        var counterSubtasks = 0
        for (subtask in todoTask.getSubtasks()) {
            counterSubtasks += deleteTodoSubtask(subtask)
        }
        val todoTaskImpl = todoTask as TodoTaskImpl
        val counterTasks = db.getTodoTaskDao().delete(todoTaskImpl.data)
        Log.i(TAG, "$counterTasks task and $counterSubtasks subtasks removed from database.")
        return Tuple(counterTasks, counterSubtasks)
    }

    suspend fun deleteTodoSubtask(subtask: TodoSubtask): Int {
        val todoSubtaskImpl = subtask as TodoSubtaskImpl
        val counter = db.getTodoSubtaskDao().delete(todoSubtaskImpl.data)
        Log.i(TAG, "$counter subtask removed from database.")
        return counter
    }

    suspend fun setTaskAndSubtasksInRecycleBin(todoTask: TodoTask, inRecycleBin: Boolean): Tuple<Int, Int> {
        var counterSubtasks = 0
        for (subtask in todoTask.getSubtasks()) {
            counterSubtasks += setSubtaskInRecycleBin(subtask, inRecycleBin)
        }
        val todoTaskImpl = todoTask as TodoTaskImpl
        todoTaskImpl.setInRecycleBin(inRecycleBin)
        val counterTasks = db.getTodoTaskDao().update(todoTaskImpl.data)
        val action = if (inRecycleBin) "put into" else "restored from"
        Log.i(TAG, "$counterTasks task and $counterSubtasks subtasks $action recycle bin.")
        return Tuple(counterTasks, counterSubtasks)
    }

    suspend fun setSubtaskInRecycleBin(subtask: TodoSubtask, inRecycleBin: Boolean): Int {
        val todoSubtaskImpl = subtask as TodoSubtaskImpl
        todoSubtaskImpl.setInRecycleBin(inRecycleBin)
        val counter = db.getTodoSubtaskDao().update(todoSubtaskImpl.data)
        val action = if (inRecycleBin) "put into" else "restored from"
        Log.i(TAG, "$counter subtask $action recycle bin.")
        return counter
    }

    suspend fun getNumberOfAllListsAndTasks(): Tuple<Int, Int> {
        val numberOfLists = db.getTodoListDao().getCount()
        val numberOfTasksNotInRecycleBin = db.getTodoTaskDao().getCountNotInRecycleBin()
        return Tuple(numberOfLists, numberOfTasksNotInRecycleBin)
    }

    suspend fun getAllToDoTasks(): MutableList<TodoTask> {
        val dataArray = db.getTodoTaskDao().getAllNotInRecycleBin()
        val data = loadTasksSubtasks(false, *dataArray)
        @Suppress("UNCHECKED_CAST")
        return data as MutableList<TodoTask>
    }

    suspend fun getAllToDoTasksOfList(listId: Int): MutableList<TodoTask> {
        val dataArray = db.getTodoTaskDao().getByListIdNotInRecycleBin(listId)
        val data = loadTasksSubtasks(false, *dataArray)
        @Suppress("UNCHECKED_CAST")
        return data as MutableList<TodoTask>
    }

    suspend fun getRecycleBin(): MutableList<TodoTask> {
        val dataArray = db.getTodoTaskDao().getAllInRecycleBin()
        val data = loadTasksSubtasks(true, *dataArray)
        @Suppress("UNCHECKED_CAST")
        return data as MutableList<TodoTask>
    }

    suspend fun clearRecycleBin(): Tuple<Int, Int> {
        var counterTasks = 0
        var counterSubtasks = 0
        val dataArray = db.getTodoTaskDao().getAllInRecycleBin()
        val todoTasks = loadTasksSubtasks(true, *dataArray)
        for (todoTask in todoTasks) {
            val counter = deleteTodoTaskAndSubtasks(todoTask)
            counterTasks += counter.left
            counterSubtasks += counter.right
        }
        return Tuple(counterTasks, counterSubtasks)
    }

    suspend fun getAllToDoListIds(): MutableList<Int> {
        val dataArray = db.getTodoListDao().getAllIds()
        return dataArray.toMutableList()
    }

    suspend fun getAllToDoListNames(): Map<Int, String> {
        val dataArray = db.getTodoListDao().getAllNames()
        /** Important: The map preserves the entry iteration order. */
        val map = mutableMapOf<Int, String>()
        for (tuple in dataArray) {
            map[tuple.id] = tuple.name
        }
        return map
    }

    suspend fun getAllToDoLists(): MutableList<TodoList> {
        val dataArray = db.getTodoListDao().getAll()
        val todoLists = loadListsTasksSubtasks(*dataArray)
        @Suppress("UNCHECKED_CAST")
        return todoLists as MutableList<TodoList>
    }

    suspend fun getToDoListById(todoListId: Int): TodoList? {
        val todoListData = db.getTodoListDao().getById(todoListId)
        var todoList: TodoList? = null
        if (null != todoListData) {
            todoList = loadListsTasksSubtasks(todoListData)[0]
        }
        return todoList
    }

    // returns the id of the todolist
    suspend fun saveTodoListInDb(todoList: TodoList): Int {
        val todoListImpl = todoList as TodoListImpl
        val data = todoListImpl.data
        var counter = 0
        when (todoListImpl.requiredDBAction) {
            RequiredDBAction.INSERT -> {
                val listId = db.getTodoListDao().insert(data).toInt()
                todoListImpl.setId(listId)
                counter = 1
                Log.d(TAG, "Todo list was inserted into DB: $data")
                if (0 == db.getTodoListDao().updateSortOrderToLast(listId)) {
                    Log.e(TAG, "Failed to update sort order for list.")
                }
            }

            RequiredDBAction.UPDATE -> {
                counter = db.getTodoListDao().update(data)
                Log.d(TAG, "Todo list was updated in DB (return code $counter): $data")
            }

            else -> {}
        }
        todoListImpl.setUnchanged()
        return counter
    }

    suspend fun saveTodoTaskAndSubtasksInDb(todoTask: TodoTask): Tuple<Int, Int> {
        val counterTasks = saveTodoTaskInDb(todoTask)
        var counterSubtasks = 0
        for (subtask in todoTask.getSubtasks()) {
            counterSubtasks += saveTodoSubtaskInDb(subtask)
        }
        return Tuple(counterTasks, counterSubtasks)
    }

    suspend fun saveTodoTaskInDb(todoTask: TodoTask): Int {
        val todoTaskImpl = todoTask as TodoTaskImpl
        val data = todoTaskImpl.data
        var counter = 0
        when (todoTaskImpl.requiredDBAction) {
            RequiredDBAction.INSERT -> {
                val taskId = db.getTodoTaskDao().insert(data).toInt()
                todoTaskImpl.setId(taskId)
                counter = 1
                Log.d(TAG, "Todo task was inserted into DB: $data")
                if (0 == db.getTodoTaskDao().updateSortOrderToLast(taskId, todoTaskImpl.getListId())) {
                    Log.e(TAG, "Failed to update sort order for task.")
                }
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
                    todoTaskImpl.getDoneTime())
                Log.d(TAG, "Todo task was updated in DB by values from pomodoro (return code $counter): $data")
            }

            else -> {}
        }
        todoTaskImpl.setUnchanged()
        return counter
    }

    suspend fun saveTodoSubtaskInDb(todoSubtask: TodoSubtask): Int {
        val todoSubtaskImpl = todoSubtask as TodoSubtaskImpl
        val data = todoSubtaskImpl.data
        var counter = 0
        when (todoSubtaskImpl.requiredDBAction) {
            RequiredDBAction.INSERT -> {
                val subtaskId = db.getTodoSubtaskDao().insert(data).toInt()
                todoSubtaskImpl.setId(subtaskId)
                counter = 1
                Log.d(TAG, "Todo subtask was inserted into DB: $data")
                if (0 == db.getTodoSubtaskDao().updateSortOrderToLast(subtaskId, todoSubtaskImpl.getTaskId())) {
                    Log.e(TAG, "Failed to update sort order for subtask.")
                }
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

    suspend fun saveTodoListsSortOrderInDb(todoListIds: List<Int>): Int {
        var counter = 0
        for ((sortOrder, todoListId) in todoListIds.withIndex()) {
            counter += db.getTodoListDao().updateSortOrder(todoListId, sortOrder)
        }
        Log.d(TAG, "Sort order of $counter todo lists was updated in DB.")
        return counter
    }

    suspend fun saveTodoTasksSortOrderInDb(todoTasks: List<TodoTask>): Int {
        var counter = 0
        for ((sortOrder, todoTask) in todoTasks.withIndex()) {
            val todoTaskImpl = todoTask as TodoTaskImpl
            todoTaskImpl.data.sortOrder = sortOrder
            counter += db.getTodoTaskDao().updateSortOrder(todoTask.getId(), sortOrder)
        }
        Log.d(TAG, "Sort order of $counter todo tasks was updated in DB.")
        return counter
    }

    suspend fun saveTodoSubtasksSortOrderInDb(todoSubtasks: List<TodoSubtask>): Int {
        var counter = 0
        for ((sortOrder, todoSubtask) in todoSubtasks.withIndex()) {
            val todoSubtaskImpl = todoSubtask as TodoSubtaskImpl
            todoSubtaskImpl.data.sortOrder = sortOrder
            counter += db.getTodoSubtaskDao().updateSortOrder(todoSubtask.getId(), todoSubtaskImpl.data.sortOrder)
        }
        Log.d(TAG, "Sort order of $counter todo subtasks was updated in DB.")
        return counter
    }

    suspend fun deleteAllData(): Triple<Int, Int, Int> {
        val counterSubtasks = db.getTodoSubtaskDao().deleteAll()
        val counterTasks = db.getTodoTaskDao().deleteAll()
        val counterLists = db.getTodoListDao().deleteAll()
        return Triple(counterLists, counterTasks, counterSubtasks)
    }

    suspend fun exportCSVData(listId: Int?, hasAutoProgress: Boolean, csvDataUri: Uri): String? {
        val todoLists: MutableList<TodoList>
        val todoTasks: MutableList<TodoTask>
        if (null == listId) {
            todoLists = getAllToDoLists()
            todoTasks = getAllToDoTasks()
        } else {
            val todoList = getToDoListById(listId) ?: return "Todo list with ID $listId not found."
            todoLists = mutableListOf(todoList)
            todoTasks = getAllToDoTasksOfList(listId)
        }
        val outputStream: OutputStream?
        try {
            outputStream = context.contentResolver.openOutputStream(csvDataUri, "wt")
        } catch (e: FileNotFoundException) {
            return "Output file not found."
        }
        if (null == outputStream) {
            return "Failed to open output file."
        }
        try {
            outputStream.bufferedWriter().use { writer ->
                val csvExporter = CSVExporter()
                csvExporter.export(todoLists, todoTasks, hasAutoProgress, writer)
            }
        } catch (e: Exception) {
            return e.toString()
        }
        return null
    }

    suspend fun importCSVData(deleteAllDataBeforeImport: Boolean, csvDataUri: Uri): Tuple<String?, Triple<Int, Int, Int>> {
        val inputStream: InputStream?
        try {
            inputStream = context.contentResolver.openInputStream(csvDataUri)
        } catch (e: FileNotFoundException) {
            return Tuple("Input file not found.", Triple(0, 0, 0))
        }
        if (null == inputStream) {
            return Tuple("Failed to open input file.", Triple(0, 0, 0))
        }
        val csvImporter = CSVImporter()
        try {
            inputStream.bufferedReader().use { reader ->
                csvImporter.import(reader)
            }
        } catch (e: Exception) {
            return Tuple(e.message, Triple(0, 0, 0))
        }

        if (deleteAllDataBeforeImport) {
            Log.i(TAG, "Deleting all data due to CSV data import.")
            deleteAllData()
        }

        var counterLists = 0
        for (list in csvImporter.lists.values) {
            counterLists += saveTodoListInDb(list)
        }
        var counterTasks = 0
        for (task in csvImporter.tasks.values) {
            // The tasks list gets its ID while saving it in DB.
            // So update the list ID at the task after the list was saved in DB.
            if (null != task.left) {
                task.right.setListId(task.left.getId())
            }
            counterTasks += saveTodoTaskInDb(task.right)
        }
        var counterSubtasks = 0
        for (subtask in csvImporter.subtasks.values) {
            // The subtasks task gets its ID while saving it in DB.
            // So update the task ID at the subtask after the task was saved in DB.
            subtask.right.setTaskId(subtask.left.getId())
            counterSubtasks += saveTodoSubtaskInDb(subtask.right)
        }
        return Tuple(null, Triple(counterLists, counterTasks, counterSubtasks))
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
            val task = loadTaskSubtasks(subtasksFromRecycleBinToo, data)
            tasks.add(task)
        }
        return tasks
    }

    private suspend fun loadTaskSubtasks(
        subtasksFromRecycleBinToo: Boolean,
        data: TodoTaskData
    ): TodoTaskImpl {
        val task = TodoTaskImpl(data)
        val dataArray = if (subtasksFromRecycleBinToo)
            db.getTodoSubtaskDao().getAllOfTask(task.getId()) else
            db.getTodoSubtaskDao().getAllOfTaskNotInRecycleBin(task.getId())
        val subtasks = loadSubtasks(*dataArray)
        @Suppress("UNCHECKED_CAST")
        task.setSubtasks(subtasks as MutableList<TodoSubtask>)
        return task
    }

    private fun loadSubtasks(vararg dataArray: TodoSubtaskData): MutableList<TodoSubtaskImpl> {
        val subtasks = ArrayList<TodoSubtaskImpl>()
        for (data in dataArray) {
            val subtask = TodoSubtaskImpl(data)
            subtasks.add(subtask)
        }
        return subtasks
    }

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
    }
}