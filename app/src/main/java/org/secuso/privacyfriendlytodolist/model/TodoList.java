package org.secuso.privacyfriendlytodolist.model;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by dominik on 19.05.16.
 */
public class TodoList implements Parcelable{

    public static final String PARCELABLE_ID = "CURRENT_TODO_LIST";

    private String name;
    private String description;
    private long deadline;

    private ArrayList<TodoTask> tasks = new ArrayList<TodoTask>();

    public TodoList(String name, String description, long deadline) {
        this.name = name;
        this.description = description;
        this.deadline = deadline;
        // TODO compute color
    }

    public TodoList(Parcel parcel) {
        name = parcel.readString();
        description = parcel.readString();
        deadline = parcel.readLong();
        tasks = parcel.readArrayList(null);
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

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDeadline() {
        return Helper.getDate(deadline);
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
        dest.writeString(name);
        dest.writeString(description);
        dest.writeLong(deadline);
        dest.writeList(tasks);
    }
}
