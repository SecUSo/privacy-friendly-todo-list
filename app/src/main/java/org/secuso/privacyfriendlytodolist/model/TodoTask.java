package org.secuso.privacyfriendlytodolist.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by dominik on 19.05.16.
 */
public class TodoTask implements Parcelable{

    private static final String TAG = TodoTask.class.getSimpleName();

    public enum Priority {
        HIGH(0), MEDIUM(1), LOW(2); // Priority steps must be sorted in the same way like they will be displayed

        private final int value;

        Priority(final int newValue) {
            value = newValue;
        }

        public int getValue() { return value; }
    };

    public enum DeadlineColors {
        BLUE,
        ORANGE,
        RED
    }

    public static final int MAX_PRIORITY = 10;

    private String title;
    private String description;
    private boolean done;
    private int id;
    private int progress;
    private int deadline;
    private Priority priority;
    private int numSubtasks;
    private int reminderTime;
    private int deadlineWarning;
    private int listPosition; // indicates at what position inside the list this task it placed

    private boolean changed = false; // true if task was changed and must written back to database

    private ArrayList<TodoSubTask> subTasks = new ArrayList<TodoSubTask>();

    public TodoTask(int id, int listPosition, String title, String description, boolean done, int progress, int deadline, Priority priority, int numSubtasks, int reminderTime, int deadlineWarning ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.progress = progress;
        this.done = done;
        this.priority = priority;
        this.numSubtasks = numSubtasks;
        this.reminderTime = reminderTime;
        this.deadline = deadline;
        this.deadlineWarning = deadlineWarning;
        this.listPosition = listPosition;
    }

    public TodoTask(Parcel parcel) {
        id = parcel.readInt();
        title = parcel.readString();
        description = parcel.readString();
        progress = parcel.readInt();
        deadline = parcel.readInt();
        priority = Priority.valueOf(parcel.readString());
        numSubtasks = parcel.readInt();
        reminderTime = parcel.readInt();
        done = parcel.readByte() != 0;
        subTasks = parcel.readArrayList(null);
        listPosition = parcel.readInt();
    }

    public void setSubTasks(ArrayList<TodoSubTask> tasks) {
        this.subTasks = tasks;
    }

    public ArrayList<TodoSubTask> getSubTasks() {
        return subTasks;
    }

    public String getName() {
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

    public Priority getPriority() {
        return priority;
    }

    public int getNumSubtasks() {
        return numSubtasks;
    }

    public int getDeadlineWarning() {
        return deadlineWarning;
    }

    public int getListPosition() {
        return listPosition;
    }

    public void setDone(boolean done) {
        this.done = done;
        changed = true;
    }

    public String getDeadline() {
        if(deadline < 0)
            return null;
        return Helper.getDate(deadline);
    }

    public int getId() {
        return id;
    }

    public long getDeadlineTs() {
        if(deadline < 0)
            return Long.MAX_VALUE;
        return deadline;
    }

    public DeadlineColors getDeadlineColor() {
        long currentTimeStamp = Helper.getCurrentTimestamp();

        if(!done) {
            if (deadline > currentTimeStamp - deadlineWarning && deadline < currentTimeStamp)
                return DeadlineColors.ORANGE;

            if (currentTimeStamp > deadline && deadline > 0)
                return DeadlineColors.RED;
        }

        return DeadlineColors.BLUE;
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
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeInt(progress);
        dest.writeInt(deadline);
        dest.writeString(priority.toString());
        dest.writeInt(numSubtasks);
        dest.writeInt(reminderTime);
        dest.writeByte((byte) (done ? 1 : 0));
        dest.writeList(subTasks);
        dest.writeInt(listPosition);
    }
}
