package org.secuso.privacyfriendlytodolist.model;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;


public class TodoList implements Parcelable, BaseTodo{

    public static final String PARCELABLE_ID = "CURRENT_TODO_LIST";

    private long id;
    private String name;
    private String description;
    private long deadline;

    private boolean changed = false;

    private ArrayList<TodoTask> tasks = new ArrayList<TodoTask>();

    public TodoList(String name, String description, long deadline) {
        this.name = name;
        this.description = description;
        this.deadline = deadline;
    }

    public TodoList(Parcel parcel) {
        id = parcel.readLong();
        name = parcel.readString();
        description = parcel.readString();
        deadline = parcel.readLong();
        tasks = parcel.readArrayList(null);
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getSize() {
        return tasks.size();
    }

    public void setDescription(String description) {
        this.description = description;
        changed = true;
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
        if (deadline <= 0)
            return null;

        return Helper.getDate(deadline);
    }

    public int getColor() {
        return Color.BLACK;
    }

    public long getId() {
        return id;
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
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeLong(deadline);
        dest.writeList(tasks);
    }

    public TodoTask.DeadlineColors getDeadlineColor() {
        int orangeCounter = 0;
        for(TodoTask currentTask : tasks) {
            switch (currentTask.getDeadlineColor()) {
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

    public void setWriteDbFlag() {
        changed = true;
    }

    public boolean isChanged() {
        return changed;
    }
}
