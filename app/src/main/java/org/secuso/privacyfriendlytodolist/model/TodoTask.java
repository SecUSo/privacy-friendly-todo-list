package org.secuso.privacyfriendlytodolist.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by dominik on 19.05.16.
 */
public class TodoTask implements Parcelable{

    private String title;
    private String description;
    private boolean done;
    private int progress;
    private int deadline;
    private int priority;
    private int numSubtasks;
    private int reminderTime;
    private int deadlineWarning;

    private ArrayList<TodoSubTask> subTasks = new ArrayList<TodoSubTask>();

    public TodoTask(String title, String description, boolean done, int progress, int deadline, int priority, int numSubtasks, int reminderTime, int deadlineWarning ) {
        this.title = title;
        this.description = description;
        this.progress = progress;
        this.done = done;
        this.priority = priority;
        this.numSubtasks = numSubtasks;
        this.reminderTime = reminderTime;
        this.deadlineWarning = deadlineWarning;
    }

    public TodoTask(Parcel parcel) {
        title = parcel.readString();
        description = parcel.readString();
        progress = parcel.readInt();
        deadline = parcel.readInt();
        priority = parcel.readInt();
        numSubtasks = parcel.readInt();
        reminderTime = parcel.readInt();
        done = parcel.readByte() != 0;
        subTasks = parcel.readArrayList(null);
    }

    public void setSubTasks(ArrayList<TodoSubTask> tasks) {
        this.subTasks = tasks;
    }

    public ArrayList<TodoSubTask> getSubTasks() {
        return subTasks;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean getDone() {
        return done;
    }

    public int getProgress() {
        return progress;
    }

    public int getPriority() {
        return priority;
    }

    public int getNumSubtasks() {
        return numSubtasks;
    }

    public int getDeadlineWarning() {
        return deadlineWarning;
    }

    public static final Parcelable.Creator<TodoTask> CREATOR =
            new Creator<TodoTask>() {
                @Override
                public TodoTask createFromParcel(Parcel source) {
                    return new TodoTask(source);
                }

                @Override
                public TodoTask[] newArray(int size) {
                    return new TodoTask[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(description);
        dest.writeInt(progress);
        dest.writeInt(deadline);
        dest.writeInt(priority);
        dest.writeInt(numSubtasks);
        dest.writeInt(reminderTime);
        dest.writeByte((byte) (done ? 1 : 0));
        dest.writeList(subTasks);
    }
}
