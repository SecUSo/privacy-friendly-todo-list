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

package org.secuso.privacyfriendlytodolist.model;

import android.content.Context;

import java.util.List;
import java.util.Set;

/**
 * Created by Christian Adams on 17.02.2024.
 *
 * This class provides an interface to the database services.
 */

public interface ModelServices {

    int NO_CHANGES = -2;

    Context getContext();

    TodoList createTodoList();

    TodoTask createTodoTask();

    TodoSubtask createTodoSubtask();

    TodoTask getTaskById(int todoTaskId);

    TodoTask getNextDueTask(long today);

    /**
     * returns a list of tasks
     *
     *  -   which are not fulfilled and whose reminder time is prior to the current time
     *  -   the task which is next due
     */
    List<TodoTask> getTasksToRemind(long today, Set<Integer> lockedIds);

    int deleteTodoList(TodoList todoList);

    int deleteTodoTask(TodoTask todoTask);

    int deleteTodoSubtask(TodoSubtask subtask);

    int setTaskInTrash(TodoTask todoTask, boolean inTrash);

    int setSubtaskInTrash(TodoSubtask subtask, boolean inTrash);

    int getNumberOfAllToDoTasks();

    List<TodoTask> getAllToDoTasks();

    List<TodoTask> getBin();

    int getNumberOfAllToDoLists();

    List<TodoList> getAllToDoLists();

    int saveTodoSubtaskInDb(TodoSubtask subtask);

    int saveTodoTaskInDb(TodoTask todoTask);

    // returns the id of the todolist
    int saveTodoListInDb(TodoList todoList);

    void deleteAllData();
}
