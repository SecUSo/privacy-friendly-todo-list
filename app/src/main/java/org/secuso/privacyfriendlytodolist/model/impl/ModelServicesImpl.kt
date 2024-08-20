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
    private var csvExporter = CSVExporter()
    private var csvImporter = CSVImporter()

    suspend fun getTaskById(todoTaskId: Int): TodoTask? {
        val todoTaskData = db.getTodoTaskDao().getById(todoTaskId)
        var todoTask: TodoTask? = null
        if (null != todoTaskData) {
            todoTask = loadTasksSubtasks(false, todoTaskData)[0]
        }
        return todoTask
    }

    suspend fun getNextDueTask(now: Long): TodoTask? {
        // Get next due task in recurring tasks.
        var nextRecurringDueTaskData: TodoTaskData? = null
        var nextRecurringDueTaskReminderTime = -1L
        val dataArray = db.getTodoTaskDao().getAllRecurringWithReminder(now)
        for (data in dataArray) {
            val nextDate = Helper.getNextRecurringDate(data.reminderTime, data.recurrencePattern, now)
            if (nextRecurringDueTaskData == null || nextDate < nextRecurringDueTaskReminderTime) {
                nextRecurringDueTaskData = data
                nextRecurringDueTaskReminderTime = nextDate
            }
        }

        // Get next due task in regular tasks.
        var nextDueTaskData = db.getTodoTaskDao().getNextDueTask(now)

        // Take the earliest of the two due tasks.
        if (nextDueTaskData == null || (nextRecurringDueTaskData != null &&
            nextDueTaskData.reminderTime > nextRecurringDueTaskReminderTime)) {
            nextDueTaskData = nextRecurringDueTaskData
        }

        var nextDueTask: TodoTask? = null
        if (null != nextDueTaskData) {
            nextDueTask = loadTasksSubtasks(false, nextDueTaskData)[0]
        }
        return nextDueTask
    }

    suspend fun getTasksToRemind(now: Long): MutableList<TodoTask> {
        val dataArray = db.getTodoTaskDao().getAllToRemind(now)
        val tasksToRemind = loadTasksSubtasks(false, *dataArray)

        // get task that is next due
        val nextDueTask = getNextDueTask(now)
        if (nextDueTask != null) {
            tasksToRemind.add(nextDueTask as TodoTaskImpl)
        }
        @Suppress("UNCHECKED_CAST")
        return tasksToRemind as MutableList<TodoTask>
    }

    suspend fun deleteTodoList(todoListId: Int): Int {
        var counter = 0
        val todoListData = db.getTodoListDao().getById(todoListId)
        if (null != todoListData) {
            val todoList = loadListsTasksSubtasks(todoListData)[0]
            for (task in todoList.getTasks()) {
                counter += setTaskAndSubtasksInRecycleBin(task, true)
            }
            Log.i(TAG, "$counter tasks put into recycle bin while removing list")
            counter = db.getTodoListDao().delete(todoList.data)
        }
        Log.i(TAG, "$counter lists removed from database")
        return counter
    }

    suspend fun deleteTodoTask(todoTask: TodoTask): Int {
        var counter = 0
        for (subtask in todoTask.getSubtasks()) {
            counter += deleteTodoSubtask(subtask)
        }
        Log.i(TAG, "$counter subtasks removed from database while removing task")
        val todoTaskImpl = todoTask as TodoTaskImpl
        counter = db.getTodoTaskDao().delete(todoTaskImpl.data)
        Log.i(TAG, "$counter tasks removed from database")
        return counter
    }

    suspend fun deleteTodoSubtask(subtask: TodoSubtask): Int {
        val todoSubtaskImpl = subtask as TodoSubtaskImpl
        val counter = db.getTodoSubtaskDao().delete(todoSubtaskImpl.data)
        Log.i(TAG, "$counter subtasks removed from database")
        return counter
    }

    suspend fun setTaskAndSubtasksInRecycleBin(todoTask: TodoTask, inRecycleBin: Boolean): Int {
        var counter = 0
        for (subtask in todoTask.getSubtasks()) {
            counter += setSubtaskInRecycleBin(subtask, inRecycleBin)
        }
        Log.i(TAG, "$counter subtasks put into recycle bin while putting task into recycle bin")
        val todoTaskImpl = todoTask as TodoTaskImpl
        todoTaskImpl.setInRecycleBin(inRecycleBin)
        counter = db.getTodoTaskDao().update(todoTaskImpl.data)
        Log.i(TAG, "$counter tasks put into recycle bin")
        return counter
    }

    suspend fun setSubtaskInRecycleBin(subtask: TodoSubtask,
                                                       inRecycleBin: Boolean): Int {
        val todoSubtaskImpl = subtask as TodoSubtaskImpl
        todoSubtaskImpl.setInRecycleBin(inRecycleBin)
        val counter = db.getTodoSubtaskDao().update(todoSubtaskImpl.data)
        Log.i(TAG, "$counter subtasks put into recycle bin")
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

    suspend fun clearRecycleBin(): Int {
        val dataArray = db.getTodoTaskDao().getAllInRecycleBin()
        val todoTasks = loadTasksSubtasks(true, *dataArray)
        var counter = 0
        for (todoTask in todoTasks) {
            counter += deleteTodoTask(todoTask)
        }
        return counter
    }

    suspend fun getAllToDoLists(): MutableList<TodoList> {
        val dataArray = db.getTodoListDao().getAll()
        val todoLists = loadListsTasksSubtasks(*dataArray)
        @Suppress("UNCHECKED_CAST")
        return todoLists as MutableList<TodoList>
    }

    suspend fun getAllToDoListNames(): Map<Int, String> {
        val dataArray = db.getTodoListDao().getAllNames()
        val map = HashMap<Int, String>(dataArray.size)
        for (tuple in dataArray) {
            map[tuple.id] = tuple.name
        }
        return map
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
        return counter
    }

    suspend fun saveTodoTaskAndSubtasksInDb(todoTask: TodoTask): Int {
        var counter = saveTodoTaskInDb(todoTask)
        for (subtask in todoTask.getSubtasks()) {
            counter += saveTodoSubtaskInDb(subtask)
        }
        return counter
    }

    suspend fun saveTodoTaskInDb(todoTask: TodoTask): Int {
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

    suspend fun deleteAllData(): Int {
        var counter = db.getTodoSubtaskDao().deleteAll()
        counter += db.getTodoTaskDao().deleteAll()
        counter += db.getTodoListDao().deleteAll()
        return counter
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
                csvExporter.export(todoLists, todoTasks, hasAutoProgress, writer)
            }
        } catch (e: Exception) {
            return e.toString()
        }
        return null
    }

    suspend fun importCSVData(deleteAllDataBeforeImport: Boolean, csvDataUri: Uri): String? {
        val inputStream: InputStream?
        try {
            inputStream = context.contentResolver.openInputStream(csvDataUri)
        } catch (e: FileNotFoundException) {
            return "Input file not found."
        }
        if (null == inputStream) {
            return "Failed to open input file."
        }
        try {
            inputStream.bufferedReader().use { reader ->
                csvImporter.import(reader)
            }
        } catch (e: Exception) {
            return e.toString()
        }

        if (deleteAllDataBeforeImport) {
            Log.i(TAG, "Deleting all data due to CSV data import.")
            deleteAllData()
        }

        for (list in csvImporter.lists.values) {
            saveTodoListInDb(list)
        }
        for (task in csvImporter.tasks.values) {
            // The tasks list gets its ID while saving it in DB.
            // So update the list ID at the task after the list was saved in DB.
            if (null != task.left) {
                task.right.setListId(task.left.getId())
            }
            saveTodoTaskInDb(task.right)
        }
        for (subtask in csvImporter.subtasks.values) {
            // The subtasks task gets its ID while saving it in DB.
            // So update the task ID at the subtask after the task was saved in DB.
            subtask.right.setTaskId(subtask.left.getId())
            saveTodoSubtaskInDb(subtask.right)
        }

        return null
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