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

import android.graphics.Color;
import android.os.Parcel;

import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.model.database.entities.TodoListData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Sebastian Lutz on 12.03.2018.
 *
 * Class to set up a To-Do List and its parameters.
 */

public class TodoListImpl extends BaseTodoImpl implements TodoList {

    private final TodoListData data;
    private List<TodoTask> tasks = new ArrayList<>();
    private boolean isDummyList;

    public TodoListImpl() {
        data = new TodoListData();
        isDummyList = true;
    }

    public TodoListImpl(TodoListData data) {
        this.data = data;
        isDummyList = false;
    }

    public TodoListImpl(Parcel parcel) {
        data = new TodoListData();
        data.setId(parcel.readInt());
        isDummyList = parcel.readByte() != 0;
        data.setName(Objects.requireNonNullElse(parcel.readString(), ""));
        parcel.readList(tasks, TodoTask.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(data.getId());
        dest.writeByte((byte) (isDummyList ? 1 : 0));
        dest.writeString(data.getName());
        dest.writeList(tasks);
    }

    @Override
    public void setId(int id) {
        data.setId(id);
        isDummyList = false;
    }

    @Override
    public int getId() {
        return data.getId();
    }

    @Override
    public boolean isDummyList() {
        return isDummyList;
    }

    @Override
    public void setName(String name) {
        data.setName(name);
    }

    @Override
    public String getName() {
        return data.getName();
    }

    @Override
    public int getSize() {
        return tasks.size();
    }

    @Override
    public void setTasks(List<TodoTask> tasks) {
        this.tasks = tasks;
    }

    @Override
    public List<TodoTask> getTasks() {
        return tasks;
    }

    @Override
    public int getColor() {
        return Color.BLACK;
    }

    @Override
    public int getDoneTodos() {
        int counter = 0;
        for(TodoTask task : tasks) {
            if (task.isDone()) {
                ++counter;
            }
        }
        return counter;
    }

    public static final Creator<TodoListImpl> CREATOR =
            new Creator<TodoListImpl>() {
                @Override
                public TodoListImpl createFromParcel(Parcel source) {
                    return new TodoListImpl(source);
                }

                @Override
                public TodoListImpl[] newArray(int size) {
                    return new TodoListImpl[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public long getNextDeadline() {

        long minDeadLine = -1;
        for(int i=0; i<tasks.size(); i++) {

            TodoTask currentTask = tasks.get(i);

            if(!currentTask.isDone()) {
                if (minDeadLine == -1 && currentTask.getDeadline() > 0)
                    minDeadLine = currentTask.getDeadline();
                else {
                    long possNewDeadline = currentTask.getDeadline();
                    if (possNewDeadline > 0 && possNewDeadline < minDeadLine) {
                        minDeadLine = possNewDeadline;
                    }
                }
            }
        }


/*

        long minDeadLine = -1;
        if(tasks.size() > 0 ) {

            minDeadLine = tasks.get(0).getDeadline();
            for(int i=1; i<tasks.size(); i++) {
                long possNewDeadline = tasks.get(i).getDeadline();
                if (possNewDeadline > 0 && possNewDeadline < minDeadLine)
                    minDeadLine = possNewDeadline;
            }

        } */

        return minDeadLine;
    }

    @Override
    public TodoTask.DeadlineColors getDeadlineColor(long defaultReminderTime) {
        TodoTask.DeadlineColors result = TodoTask.DeadlineColors.BLUE;
        for(TodoTask currentTask : tasks) {
            switch (currentTask.getDeadlineColor(defaultReminderTime)) {
                case RED:
                    return TodoTask.DeadlineColors.RED;

                case ORANGE:
                    result = TodoTask.DeadlineColors.ORANGE;
                    break;

                default:
                    break;
            }
        }

        return result;
    }


    @Override
    public boolean checkQueryMatch(String query, boolean recursive) {
        // no query? always match!
        if (query == null || query.isEmpty())
            return true;

        String queryLowerCase = query.toLowerCase();
        if (data.getName().toLowerCase().contains(queryLowerCase)) {
            return true;
        }
        if (recursive) {
            for (TodoTask task : tasks) {
                if (task.checkQueryMatch(queryLowerCase, true)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean checkQueryMatch(String query) {
        return checkQueryMatch(query, true);
    }

    TodoListData getData() {
        return data;
    }
}
