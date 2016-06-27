package org.secuso.privacyfriendlytodolist.model;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.secuso.privacyfriendlytodolist.model.database.DBQueryHandler;

import java.util.ArrayList;


public class TodoList extends BaseTodo implements Parcelable{

    public static final String PARCELABLE_ID = "CURRENT_TODO_LIST";

    private long id;

    private ArrayList<TodoTask> tasks = new ArrayList<TodoTask>();

    public TodoList(Parcel parcel) {
        id = parcel.readLong();
        name = parcel.readString();
        description = parcel.readString();
        tasks = parcel.readArrayList(null);

        dbState = DBQueryHandler.ObjectStates.NO_DB_ACTION;
    }

    public TodoList() {

    }

    public void setId(long id) {
        this.id = id;
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


}
