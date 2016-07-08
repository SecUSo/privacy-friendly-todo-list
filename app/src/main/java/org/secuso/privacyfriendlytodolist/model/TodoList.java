package org.secuso.privacyfriendlytodolist.model;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.secuso.privacyfriendlytodolist.model.database.DBQueryHandler;

import java.util.ArrayList;


public class TodoList extends BaseTodo implements Parcelable{

    public static final String PARCELABLE_KEY = "PARCELABLE_KEY_FOR_TODO_LIST";
    public static final String UNIQUE_DATABASE_ID = "CURRENT_TODO_LIST_ID";
    public static final int DUMMY_LIST_ID = -3; // -1 is often used for error codes

    private ArrayList<TodoTask> tasks = new ArrayList<TodoTask>();

    public TodoList(Parcel parcel) {

        id = parcel.readInt();
        name = parcel.readString();
        description = parcel.readString();
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
        dest.writeString(description);
        dest.writeList(tasks);
    }

    public long getNextDeadline() {
        long minDeadLine = -1;
        if(tasks.size() > 0 ) {

            minDeadLine = tasks.get(0).getDeadline();
            for(int i=1; i<tasks.size(); i++) {
                long possNewDeadline = tasks.get(i).getDeadline();
                if (possNewDeadline > 0 && possNewDeadline < minDeadLine)
                    minDeadLine = possNewDeadline;
            }

        }

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



}
