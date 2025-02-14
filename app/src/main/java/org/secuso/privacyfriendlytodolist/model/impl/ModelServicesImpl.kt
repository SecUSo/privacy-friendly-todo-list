/*
Privacy Friendly To-Do List
Copyright (C) 2018-2025  Sebastian Lutz

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
import java.util.concurrent.TimeUnit

class ModelServicesImpl(private val context: Context) {

    private suspend fun getDB(): TodoListDatabase {
        return TodoListDatabase.getInstance(context)
    }

    suspend fun getTaskById(todoTaskId: Int): TodoTask? {
        val todoTaskData = getDB().getTodoTaskDao().getById(todoTaskId)
        var todoTask: TodoTask? = null
        if (null != todoTaskData) {
            todoTask = loadTasksSubtasks(false, todoTaskData)[0]
        }
        return todoTask
    }

    /**
     * This method does two things:
     * 1) Updates the reminder time of recurring tasks.
     * 2) Sets every recurring task to undone where the done-date is before a deadline and the
     * deadline is in the past. In other words: Recurring tasks need to be done again and again and
     * this algorithm should set them to undone when the done deadline is in the past and next
     * deadline comes near.
     */
    private suspend fun updateRecurringTasks(now: Long): Int {
        var dataArray = getDB().getTodoTaskDao().getOverdueRecurringTasks(now)
        var todoTasks = loadTasksSubtasks(false, *dataArray)
        var updatedTasks = todoTasks.size
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

        dataArray = getDB().getTodoTaskDao().getDoneRecurringTasks()
        todoTasks = loadTasksSubtasks(false, *dataArray)
        for (todoTask in todoTasks) {
            // Note: getDoneRecurringTasks() ensures that doneTime is set.
            val doneTime = todoTask.getDoneTime()!!
            val deadlineTime = todoTask.getDeadline()
            if (null == deadlineTime) {
                Log.e(TAG, "Recurring task has no deadline: ID ${todoTask.getId()}, name ${todoTask.getName()}.")
                continue
            }
            // Get the last deadline by usage of offset -1.
            val lastDeadlineTime = Helper.getNextRecurringDate(deadlineTime,
                todoTask.getRecurrencePattern(), todoTask.getRecurrenceInterval(), now, -1)
            // Convert to days to avoid setting to undone while deadline-day is not completely in past.
            val doneDay = TimeUnit.SECONDS.toDays(doneTime)
            val lastDeadlineDay = TimeUnit.SECONDS.toDays(lastDeadlineTime)
            val nowDay = TimeUnit.SECONDS.toDays(now)
            // If done-marker belongs to last deadline and last deadline day is over then the task should be set to undone.
            @Suppress("ConvertTwoComparisonsToRangeCheck")
            if (doneDay <= lastDeadlineDay && lastDeadlineDay < nowDay) {
                todoTask.setDone(false)
                todoTask.setChanged()
                if (saveTodoTaskInDb(todoTask) > 0) {
                    ++updatedTasks
                    Log.i(TAG, "Setting done recurring task $todoTask to undone because done date " +
                        "${Helper.createCanonicalDateString(doneTime)} is <= last deadline " +
                        "${Helper.createCanonicalDateString(lastDeadlineTime)} which is < today.")
                } else {
                    Log.e(TAG, "Failed to save $todoTask after setting to undone.")
                }
            }
        }
        return updatedTasks
    }

    suspend fun getNextDueTask(now: Long): Pair<TodoTask?, Int> {
        // Ensure that reminder time of recurring tasks is up-to-date before determining next due task.
        val updatedTasks = updateRecurringTasks(now)
        // Get next due task.
        val nextDueTaskData = getDB().getTodoTaskDao().getNextDueTask(now)
        var todoTask: TodoTask? = null
        if (null != nextDueTaskData) {
            todoTask = loadTasksSubtasks(false, nextDueTaskData)[0]
        }
        return Pair(todoTask, updatedTasks)
    }

    suspend fun getOverdueTasks(now: Long): MutableList<TodoTask> {
        val dataArray = getDB().getTodoTaskDao().getOverdueTasks(now)
        val data = loadTasksSubtasks(false, *dataArray)
        @Suppress("UNCHECKED_CAST")
        return data as MutableList<TodoTask>
    }

    suspend fun deleteTodoList(todoListId: Int): Triple<Int, Int, Int> {
        var counterLists = 0
        var counterTasks = 0
        var counterSubtasks = 0
        val todoListData = getDB().getTodoListDao().getById(todoListId)
        if (null != todoListData) {
            val todoList = loadListsTasksSubtasks(todoListData)[0]
            for (task in todoList.getTasks()) {
                val counter = setTaskAndSubtasksInRecycleBin(task, true)
                counterTasks += counter.first
                counterSubtasks += counter.second
            }
            counterLists += getDB().getTodoListDao().delete(todoList.data)
        }
        Log.i(TAG, "$counterLists list with $counterTasks tasks and $counterSubtasks subtasks removed from database.")
        return Triple(counterLists, counterTasks, counterSubtasks)
    }

    suspend fun deleteTodoTaskAndSubtasks(todoTask: TodoTask): Pair<Int, Int> {
        var counterSubtasks = 0
        for (subtask in todoTask.getSubtasks()) {
            counterSubtasks += deleteTodoSubtask(subtask)
        }
        val todoTaskImpl = todoTask as TodoTaskImpl
        val counterTasks = getDB().getTodoTaskDao().delete(todoTaskImpl.data)
        Log.i(TAG, "$counterTasks task and $counterSubtasks subtasks removed from database.")
        return Pair(counterTasks, counterSubtasks)
    }

    suspend fun deleteTodoSubtask(subtask: TodoSubtask): Int {
        val todoSubtaskImpl = subtask as TodoSubtaskImpl
        val counter = getDB().getTodoSubtaskDao().delete(todoSubtaskImpl.data)
        Log.i(TAG, "$counter subtask removed from database.")
        return counter
    }

    suspend fun setAllDoneTasksInRecycleBin(): Pair<Int, Int> {
        val dataArray = getDB().getTodoTaskDao().getAllDoneNotInRecycleBin()
        val tasks = loadTasksSubtasks(false, *dataArray)
        var counterTasks = 0
        var counterSubtasks = 0
        for (task in tasks) {
            val counters = setTaskAndSubtasksInRecycleBin(task, true)
            counterTasks += counters.first
            counterSubtasks += counters.second
        }
        Log.i(TAG, "$counterTasks task and $counterSubtasks subtasks put into recycle bin.")
        return Pair(counterTasks, counterSubtasks)
    }

    suspend fun setTaskAndSubtasksInRecycleBin(todoTask: TodoTask, inRecycleBin: Boolean): Pair<Int, Int> {
        var counterSubtasks = 0
        for (subtask in todoTask.getSubtasks()) {
            counterSubtasks += setSubtaskInRecycleBin(subtask, inRecycleBin)
        }
        val todoTaskImpl = todoTask as TodoTaskImpl
        todoTaskImpl.setInRecycleBin(inRecycleBin)
        val counterTasks = getDB().getTodoTaskDao().update(todoTaskImpl.data)
        val action = if (inRecycleBin) "put into" else "restored from"
        Log.i(TAG, "$counterTasks task and $counterSubtasks subtasks $action recycle bin.")
        return Pair(counterTasks, counterSubtasks)
    }

    suspend fun setSubtaskInRecycleBin(subtask: TodoSubtask, inRecycleBin: Boolean): Int {
        val todoSubtaskImpl = subtask as TodoSubtaskImpl
        todoSubtaskImpl.setInRecycleBin(inRecycleBin)
        val counter = getDB().getTodoSubtaskDao().update(todoSubtaskImpl.data)
        val action = if (inRecycleBin) "put into" else "restored from"
        Log.i(TAG, "$counter subtask $action recycle bin.")
        return counter
    }

    suspend fun getNumberOfAllListsAndTasks(): Pair<Int, Int> {
        val numberOfLists = getDB().getTodoListDao().getCount()
        val numberOfTasksNotInRecycleBin = getDB().getTodoTaskDao().getCountNotInRecycleBin()
        return Pair(numberOfLists, numberOfTasksNotInRecycleBin)
    }

    suspend fun getAllToDoTasks(): MutableList<TodoTask> {
        val dataArray = getDB().getTodoTaskDao().getAllNotInRecycleBin()
        val data = loadTasksSubtasks(false, *dataArray)
        @Suppress("UNCHECKED_CAST")
        return data as MutableList<TodoTask>
    }

    suspend fun getAllToDoTasksOfList(listId: Int): MutableList<TodoTask> {
        val dataArray = getDB().getTodoTaskDao().getByListIdNotInRecycleBin(listId)
        val data = loadTasksSubtasks(false, *dataArray)
        @Suppress("UNCHECKED_CAST")
        return data as MutableList<TodoTask>
    }

    suspend fun getRecycleBin(): MutableList<TodoTask> {
        val dataArray = getDB().getTodoTaskDao().getAllInRecycleBin()
        val data = loadTasksSubtasks(true, *dataArray)
        @Suppress("UNCHECKED_CAST")
        return data as MutableList<TodoTask>
    }

    suspend fun clearRecycleBin(): Pair<Int, Int> {
        var counterTasks = 0
        var counterSubtasks = 0
        val dataArray = getDB().getTodoTaskDao().getAllInRecycleBin()
        val todoTasks = loadTasksSubtasks(true, *dataArray)
        for (todoTask in todoTasks) {
            val counter = deleteTodoTaskAndSubtasks(todoTask)
            counterTasks += counter.first
            counterSubtasks += counter.second
        }
        return Pair(counterTasks, counterSubtasks)
    }

    suspend fun getAllToDoListIds(): MutableList<Int> {
        val dataArray = getDB().getTodoListDao().getAllIds()
        return dataArray.toMutableList()
    }

    suspend fun getAllToDoListNames(): Map<Int, String> {
        val dataArray = getDB().getTodoListDao().getAllNames()
        /** Important: The map preserves the entry iteration order. */
        val map = mutableMapOf<Int, String>()
        for (tuple in dataArray) {
            map[tuple.id] = tuple.name
        }
        return map
    }

    suspend fun getAllToDoLists(): MutableList<TodoList> {
        val dataArray = getDB().getTodoListDao().getAll()
        val todoLists = loadListsTasksSubtasks(*dataArray)
        @Suppress("UNCHECKED_CAST")
        return todoLists as MutableList<TodoList>
    }

    suspend fun getToDoListById(todoListId: Int): TodoList? {
        val todoListData = getDB().getTodoListDao().getById(todoListId)
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
            RequiredDBAction.NONE -> {
                Log.d(TAG, "Todo list NOT saved because no DB action required: $data")
            }

            RequiredDBAction.INSERT -> {
                val listId = getDB().getTodoListDao().insert(data).toInt()
                todoListImpl.setId(listId)
                counter = 1
                Log.d(TAG, "Todo list was inserted into DB: $data")
                if (0 == getDB().getTodoListDao().updateSortOrderToLast(listId)) {
                    Log.e(TAG, "Failed to update sort order for list.")
                }
            }

            RequiredDBAction.UPDATE, RequiredDBAction.UPDATE_FROM_POMODORO -> {
                counter = getDB().getTodoListDao().update(data)
                Log.d(TAG, "Todo list was updated in DB (return code $counter): $data")
            }
        }
        todoListImpl.setUnchanged()
        return counter
    }

    suspend fun saveTodoTaskAndSubtasksInDb(todoTask: TodoTask): Pair<Int, Int> {
        val counterTasks = saveTodoTaskInDb(todoTask)
        var counterSubtasks = 0
        for (subtask in todoTask.getSubtasks()) {
            counterSubtasks += saveTodoSubtaskInDb(subtask)
        }
        return Pair(counterTasks, counterSubtasks)
    }

    suspend fun saveTodoTaskInDb(todoTask: TodoTask): Int {
        val todoTaskImpl = todoTask as TodoTaskImpl
        val data = todoTaskImpl.data
        var counter = 0
        when (todoTaskImpl.requiredDBAction) {
            RequiredDBAction.NONE -> {
                Log.d(TAG, "Todo task NOT saved because no DB action required: $data")
            }

            RequiredDBAction.INSERT -> {
                val taskId = getDB().getTodoTaskDao().insert(data).toInt()
                todoTaskImpl.setId(taskId)
                counter = 1
                Log.d(TAG, "Todo task was inserted into DB: $data")
                if (0 == getDB().getTodoTaskDao().updateSortOrderToLast(taskId, todoTaskImpl.getListId())) {
                    Log.e(TAG, "Failed to update sort order for task.")
                }
            }

            RequiredDBAction.UPDATE -> {
                counter = getDB().getTodoTaskDao().update(data)
                Log.d(TAG, "Todo task was updated in DB (return code $counter): $data")
            }

            RequiredDBAction.UPDATE_FROM_POMODORO -> {
                counter = getDB().getTodoTaskDao().updateValuesFromPomodoro(
                    todoTaskImpl.getId(),
                    todoTaskImpl.getName(),
                    todoTaskImpl.getProgress(),
                    todoTaskImpl.getDoneTime())
                Log.d(TAG, "Todo task was updated in DB by values from pomodoro (return code $counter): $data")
            }
        }
        todoTaskImpl.setUnchanged()
        return counter
    }

    suspend fun saveTodoSubtaskInDb(todoSubtask: TodoSubtask): Int {
        val todoSubtaskImpl = todoSubtask as TodoSubtaskImpl
        val data = todoSubtaskImpl.data
        var counter = 0
        when (todoSubtaskImpl.requiredDBAction) {
            RequiredDBAction.NONE -> {
                Log.d(TAG, "Todo subtask NOT saved because no DB action required: $data")
            }

            RequiredDBAction.INSERT -> {
                val subtaskId = getDB().getTodoSubtaskDao().insert(data).toInt()
                todoSubtaskImpl.setId(subtaskId)
                counter = 1
                Log.d(TAG, "Todo subtask was inserted into DB: $data")
                if (0 == getDB().getTodoSubtaskDao().updateSortOrderToLast(subtaskId, todoSubtaskImpl.getTaskId())) {
                    Log.e(TAG, "Failed to update sort order for subtask.")
                }
            }

            RequiredDBAction.UPDATE, RequiredDBAction.UPDATE_FROM_POMODORO -> {
                counter = getDB().getTodoSubtaskDao().update(data)
                Log.d(TAG, "Todo subtask was updated in DB (return code $counter): $data")
            }
        }
        todoSubtaskImpl.setUnchanged()
        return counter
    }

    suspend fun saveTodoListsSortOrderInDb(todoListIds: List<Int>): Int {
        var counter = 0
        for ((sortOrder, todoListId) in todoListIds.withIndex()) {
            counter += getDB().getTodoListDao().updateSortOrder(todoListId, sortOrder)
        }
        Log.d(TAG, "Sort order of $counter todo lists was updated in DB.")
        return counter
    }

    suspend fun saveTodoTasksSortOrderInDb(todoTasks: List<TodoTask>): Int {
        var counter = 0
        for ((sortOrder, todoTask) in todoTasks.withIndex()) {
            val todoTaskImpl = todoTask as TodoTaskImpl
            todoTaskImpl.data.sortOrder = sortOrder
            counter += getDB().getTodoTaskDao().updateSortOrder(todoTask.getId(), sortOrder)
        }
        Log.d(TAG, "Sort order of $counter todo tasks was updated in DB.")
        return counter
    }

    suspend fun saveTodoSubtasksSortOrderInDb(todoSubtasks: List<TodoSubtask>): Int {
        var counter = 0
        for ((sortOrder, todoSubtask) in todoSubtasks.withIndex()) {
            val todoSubtaskImpl = todoSubtask as TodoSubtaskImpl
            todoSubtaskImpl.data.sortOrder = sortOrder
            counter += getDB().getTodoSubtaskDao().updateSortOrder(todoSubtask.getId(), todoSubtaskImpl.data.sortOrder)
        }
        Log.d(TAG, "Sort order of $counter todo subtasks was updated in DB.")
        return counter
    }

    suspend fun deleteAllData(): Triple<Int, Int, Int> {
        val counterSubtasks = getDB().getTodoSubtaskDao().deleteAll()
        val counterTasks = getDB().getTodoTaskDao().deleteAll()
        val counterLists = getDB().getTodoListDao().deleteAll()
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

    suspend fun importCSVData(deleteAllDataBeforeImport: Boolean, csvDataUri: Uri): Pair<String?, Triple<Int, Int, Int>> {
        val inputStream: InputStream?
        try {
            inputStream = context.contentResolver.openInputStream(csvDataUri)
        } catch (e: FileNotFoundException) {
            return Pair("Input file not found.", Triple(0, 0, 0))
        }
        if (null == inputStream) {
            return Pair("Failed to open input file.", Triple(0, 0, 0))
        }
        val csvImporter = CSVImporter()
        try {
            inputStream.bufferedReader().use { reader ->
                csvImporter.import(reader)
            }
        } catch (e: Exception) {
            return Pair(e.message, Triple(0, 0, 0))
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
            val list = task.first
            if (null != list) {
                task.second.setListId(list.getId())
            }
            counterTasks += saveTodoTaskInDb(task.second)
        }
        var counterSubtasks = 0
        for (subtask in csvImporter.subtasks.values) {
            // The subtasks task gets its ID while saving it in DB.
            // So update the task ID at the subtask after the task was saved in DB.
            subtask.second.setTaskId(subtask.first.getId())
            counterSubtasks += saveTodoSubtaskInDb(subtask.second)
        }
        return Pair(null, Triple(counterLists, counterTasks, counterSubtasks))
    }

    private suspend fun loadListsTasksSubtasks(vararg dataArray: TodoListData): MutableList<TodoListImpl> {
        val lists = ArrayList<TodoListImpl>()
        for (data in dataArray) {
            val list = TodoListImpl(data)
            val dataArray2 = getDB().getTodoTaskDao().getAllOfListNotInRecycleBin(list.getId())
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
            getDB().getTodoSubtaskDao().getAllOfTask(task.getId()) else
            getDB().getTodoSubtaskDao().getAllOfTaskNotInRecycleBin(task.getId())
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