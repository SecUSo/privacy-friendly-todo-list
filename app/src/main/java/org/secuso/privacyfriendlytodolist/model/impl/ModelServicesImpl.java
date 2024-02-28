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

package org.secuso.privacyfriendlytodolist.model.impl;

import android.content.Context;
import android.util.Log;

import org.secuso.privacyfriendlytodolist.model.ModelServices;
import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.model.TodoSubtask;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.model.database.TodoListDatabase;
import org.secuso.privacyfriendlytodolist.model.database.entities.TodoListData;
import org.secuso.privacyfriendlytodolist.model.database.entities.TodoSubtaskData;
import org.secuso.privacyfriendlytodolist.model.database.entities.TodoTaskData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ModelServicesImpl implements ModelServices {

    private static final String TAG = ModelServicesImpl.class.getSimpleName();

    private final Context context;
    private final TodoListDatabase db;

    public ModelServicesImpl(Context context) {
        this.context = context;
        db = TodoListDatabase.Companion.getInstance(context);
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public TodoList createTodoList() {
        return new TodoListImpl();
    }

    @Override
    public TodoTask createTodoTask() {
        return new TodoTaskImpl();
    }

    @Override
    public TodoSubtask createTodoSubtask() {
        return new TodoSubtaskImpl();
    }

    @Override
    public TodoTask getNextDueTask(long today) {
        TodoTaskData nextDueTaskData = db.getTodoTaskDao().getNextDueTask(today);
        TodoTask nextDueTask = null;
        if (null != nextDueTaskData) {
            nextDueTask = new TodoTaskImpl(nextDueTaskData);
        }
        return nextDueTask;
    }

    /**
     * returns a list of tasks
     *
     *  -   which are not fulfilled and whose reminder time is prior to the current time
     *  -   the task which is next due
     *
     * @param today Date of today.
     * @param lockedIds Tasks for which the user was just notified (these tasks are locked).
     *                  They will be excluded.
     */
    @Override
    public List<TodoTask> getTasksToRemind(long today, Set<Integer> lockedIds) {
        TodoTaskData[] dataArray = db.getTodoTaskDao().getTasksToRemind(today, lockedIds);
        ArrayList<TodoTask> tasksToRemind = createTasks(dataArray, false);

        // get task that is next due
        TodoTask nextDueTask = getNextDueTask(today);
        if (nextDueTask != null) {
            tasksToRemind.add(nextDueTask);
        }
        return tasksToRemind;
    }

    @Override
    public int deleteTodoList(TodoList todoList) {
        int counter = 0;
        for (TodoTask task : todoList.getTasks()) {
            counter += setTaskInTrash(task, true);
        }
        Log.i(TAG, counter + " tasks put into trash while removing list");

        TodoListImpl todoListImpl = (TodoListImpl)todoList;
        counter = db.getTodoListDao().delete(todoListImpl.getData());
        Log.i(TAG, counter + " lists removed from database");
        return counter;
    }

    @Override
    public int deleteTodoTask(TodoTask todoTask) {
        int counter = 0;
        for(TodoSubtask subtask : todoTask.getSubtasks()) {
            counter += deleteTodoSubtask(subtask);
        }
        Log.i(TAG, counter + " subtasks removed from database while removing task");

        TodoTaskImpl todoTaskImpl = (TodoTaskImpl)todoTask;
        counter = db.getTodoTaskDao().delete(todoTaskImpl.getData());
        Log.i(TAG, counter + " tasks removed from database");
        return counter;
    }

    @Override
    public int deleteTodoSubtask(TodoSubtask subtask) {
        TodoSubtaskImpl todoSubtaskImpl = (TodoSubtaskImpl)subtask;
        int counter = db.getTodoSubtaskDao().delete(todoSubtaskImpl.getData());
        Log.i(TAG, counter + " subtasks removed from database");
        return counter;
    }

    @Override
    public int setTaskInTrash(TodoTask todoTask, boolean inTrash) {
        int counter = 0;
        for(TodoSubtask subtask : todoTask.getSubtasks()) {
            counter += setSubtaskInTrash(subtask, inTrash);
        }
        Log.i(TAG, counter + " subtasks put into trash while putting task into trash");

        TodoTaskImpl todoTaskImpl = (TodoTaskImpl)todoTask;
        todoTaskImpl.setInTrash(inTrash);
        counter = db.getTodoTaskDao().update(todoTaskImpl.getData());
        Log.i(TAG, counter + " tasks put into trash");
        return counter;
    }

    @Override
    public int setSubtaskInTrash(TodoSubtask subtask, boolean inTrash) {
        TodoSubtaskImpl todoSubtaskImpl = (TodoSubtaskImpl)subtask;
        todoSubtaskImpl.setInTrash(inTrash);
        int counter = db.getTodoSubtaskDao().update(todoSubtaskImpl.getData());
        Log.i(TAG, counter + " subtasks put into trash");
        return counter;
    }

    @Override
    public List<TodoTask> getAllToDoTasks() {
        TodoTaskData[] dataArray = db.getTodoTaskDao().getAllNotInTrash();
        return createTasks(dataArray, false);
    }

    @Override
    public List<TodoTask> getBin() {
        TodoTaskData[] dataArray = db.getTodoTaskDao().getAllInTrash();
        return createTasks(dataArray, true);
    }

    @Override
    public List<TodoList> getAllToDoLists() {
        TodoListData[] dataArray = db.getTodoListDao().getAll();
        return createLists(dataArray);
    }

    private ArrayList<TodoList> createLists(TodoListData[] dataArray) {
        ArrayList<TodoList> lists = new ArrayList<>();
        for (TodoListData data : dataArray)
        {
            TodoListImpl list = new TodoListImpl(data);
            TodoTaskData[] dataArray2 = db.getTodoTaskDao().getAllOfListNotInTrash(list.getId());
            List<TodoTask> tasks = createTasks(dataArray2, false);
            for (TodoTask task : tasks) {
                task.setList(list);
            }
            list.setTasks(tasks);
            lists.add(list);
        }
        return lists;
    }

    private ArrayList<TodoTask> createTasks(TodoTaskData[] dataArray, boolean subtasksFromTrashToo) {
        ArrayList<TodoTask> tasks = new ArrayList<>();
        for (TodoTaskData data : dataArray) {
            TodoTaskImpl task = new TodoTaskImpl(data);
            TodoSubtaskData[] dataArray2 = subtasksFromTrashToo
                ? db.getTodoSubtaskDao().getAllOfTask(task.getId())
                : db.getTodoSubtaskDao().getAllOfTaskNotInTrash(task.getId());
            List<TodoSubtask> subtasks = createSubtasks(dataArray2);
            task.setSubtasks(subtasks);
            tasks.add(task);
        }
        return tasks;
    }

    private ArrayList<TodoSubtask> createSubtasks(TodoSubtaskData[] dataArray) {
        ArrayList<TodoSubtask> subtasks = new ArrayList<>();
        for (TodoSubtaskData data : dataArray) {
            TodoSubtaskImpl subtask = new TodoSubtaskImpl(data);
            subtasks.add(subtask);
        }
        return subtasks;
    }

    @Override
    // returns the id of the todolist
    public int saveTodoListInDb(TodoList todoList) {
        TodoListImpl todoListImpl = (TodoListImpl)todoList;
        TodoListData data = todoListImpl.getData();
        int todoListId;

        switch (todoListImpl.getDBState()) {
            case INSERT_TO_DB:
                todoListId = (int) db.getTodoListDao().insert(data);
                Log.d(TAG, "Todo list was inserted into DB: " + data);
                break;

            case UPDATE_DB:
                int counter = db.getTodoListDao().update(data);
                todoListId = todoListImpl.getId();
                Log.d(TAG, "Todo list was updated in DB (return code " + counter + "): " + data);
                break;

            default:
                todoListId = NO_CHANGES;
                break;
        }
        todoListImpl.setUnchanged();

        return todoListId;
    }

    @Override
    public int saveTodoTaskInDb(TodoTask todoTask) {
        TodoTaskImpl todoTaskImpl = (TodoTaskImpl)todoTask;
        TodoTaskData data = todoTaskImpl.getData();
        int todoTaskId;
        int counter;

        switch (todoTaskImpl.getDBState()) {
            case INSERT_TO_DB:
                todoTaskId = (int) db.getTodoTaskDao().insert(data);
                Log.d(TAG, "Todo task was inserted into DB: " + data);
                break;

            case UPDATE_DB:
                counter = db.getTodoTaskDao().update(data);
                todoTaskId = todoTaskImpl.getId();
                Log.d(TAG, "Todo task was updated in DB (return code " + counter + "): " + data);
                break;

            case UPDATE_FROM_POMODORO:
                counter = db.getTodoTaskDao().updateValuesFromPomodoro(
                        todoTaskImpl.getId(),
                        todoTaskImpl.getName(),
                        todoTaskImpl.getProgress(),
                        todoTaskImpl.isDone());
                todoTaskId = todoTaskImpl.getId();
                Log.d(TAG, "Todo task was updated in DB by values from pomodoro (return code " + counter + "): " + data);
                break;

            default:
                todoTaskId = NO_CHANGES;
                break;
        }
        todoTaskImpl.setUnchanged();

        return todoTaskId;
    }

    @Override
    public int saveTodoSubtaskInDb(TodoSubtask subtask) {
        TodoSubtaskImpl todoSubtaskImpl = (TodoSubtaskImpl)subtask;
        TodoSubtaskData data = todoSubtaskImpl.getData();
        int todoSubtaskId;

        switch (todoSubtaskImpl.getDBState()) {
            case INSERT_TO_DB:
                todoSubtaskId = (int) db.getTodoSubtaskDao().insert(data);
                Log.d(TAG, "Todo subtask was inserted into DB: " + data);
                break;

            case UPDATE_DB:
                int counter = db.getTodoSubtaskDao().update(data);
                todoSubtaskId = todoSubtaskImpl.getId();
                Log.d(TAG, "Todo subtask was updated in DB (return code " + counter + "): " + data);
                break;

            default:
                todoSubtaskId = NO_CHANGES;
                break;
        }
        todoSubtaskImpl.setUnchanged();

        return todoSubtaskId;
    }

    @Override
    public void deleteAllData() {
        db.getTodoSubtaskDao().deleteAll();
        db.getTodoTaskDao().deleteAll();
        db.getTodoListDao().deleteAll();
    }
}