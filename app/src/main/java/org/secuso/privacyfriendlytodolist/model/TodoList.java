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

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Sebastian Lutz on 12.03.2018.
 *
 * Class to set up a To-Do List and its parameters.
 */


public class TodoList extends BaseTodo implements Parcelable{

    public static final String PARCELABLE_KEY = "PARCELABLE_KEY_FOR_TODO_LIST";
    public static final String UNIQUE_DATABASE_ID = "CURRENT_TODO_LIST_ID";
    public static final int DUMMY_LIST_ID = -3; // -1 is often used for error codes

    private ArrayList<TodoTask> tasks = new ArrayList<TodoTask>();

    public TodoList(Parcel parcel) {

        id = parcel.readInt();
        name = parcel.readString();
        parcel.readList(tasks, TodoTask.class.getClassLoader());
    }

    public TodoList() {
        super();
    }

    public boolean isDummyList() {
        return id == DUMMY_LIST_ID;
    }

    public void setDummyList() {
        id = DUMMY_LIST_ID;
    }

    public int getSize() {
        return tasks.size();
    }

    public void setTasks(ArrayList<TodoTask> tasks) {
        this.tasks = tasks;
    }

    public ArrayList<TodoTask> getTasks() {
        return tasks;
    }

    public int getColor() {
        return Color.BLACK;
    }

    public int getDoneTodos() {
        int counter = 0;
        for(TodoTask task : tasks)
            counter += task.getDone() == true ? 1 : 0;
        return counter;
    }

    public static final Parcelable.Creator<TodoList> CREATOR =
            new Creator<TodoList>() {
                @Override
                public TodoList createFromParcel(Parcel source) {
                    return new TodoList(source);
                }

                @Override
                public TodoList[] newArray(int size) {
                    return new TodoList[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeList(tasks);
    }

    public long getNextDeadline() {

        long minDeadLine = -1;
        for(int i=0; i<tasks.size(); i++) {

            TodoTask currentTask = tasks.get(i);

            if(!currentTask.getDone()) {
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

    public TodoTask.DeadlineColors getDeadlineColor(long defaultReminderTime) {
        int orangeCounter = 0;
        for(TodoTask currentTask : tasks) {
            switch (currentTask.getDeadlineColor(defaultReminderTime)) {
                case RED:
                    return TodoTask.DeadlineColors.RED;
                case ORANGE:
                    orangeCounter++;
                    break;
                default:
                    break;
            }
        }
        if(orangeCounter > 0)
            return TodoTask.DeadlineColors.ORANGE;
        return TodoTask.DeadlineColors.BLUE;
    }


    public boolean checkQueryMatch(String query, boolean recursive) {
        // no query? always match!
        if (query == null || query.isEmpty())
            return true;

        String queryLowerCase = query.toLowerCase();
        if (this.name.toLowerCase().contains(queryLowerCase))
            return true;

        if (recursive)
            for (int i = 0; i < this.tasks.size(); i++)
                if (this.tasks.get(i).checkQueryMatch(queryLowerCase, recursive))
                    return true;
        return false;
    }

    public boolean checkQueryMatch(String query) {
        return checkQueryMatch(query, true);
    }


}
