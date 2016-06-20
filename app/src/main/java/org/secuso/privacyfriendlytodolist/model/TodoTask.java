package org.secuso.privacyfriendlytodolist.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;

/**
 * Created by dominik on 19.05.16.
 */
public class TodoTask implements Parcelable, BaseTodo {

    private static final String TAG = TodoTask.class.getSimpleName();



    public enum Priority {
        HIGH(0), MEDIUM(1), LOW(2); // Priority steps must be sorted in the same way like they will be displayed

        private final int value;

        Priority(final int newValue) {
            value = newValue;
        }

        public int getValue() {
            return value;
        }

        public static Priority fromInt(int i) {
            for (Priority p : Priority.values()) {
                if (p.getValue() == i) {
                    return p;
                }
            }
            throw new IllegalArgumentException("No such priority defined.");
        }
    }

    public enum DeadlineColors {
        BLUE,
        ORANGE,
        RED
    }

    public static final int MAX_PRIORITY = 10;

    private String title;
    private String description;
    private boolean done;
    private long id;
    private int progress;
    private long deadline;
    private Priority priority;
    private long reminderTime;
    private int listPosition; // indicates at what position inside the list this task it placed

    private long listIdForeignKey;

    private boolean writeBackToDb = false; // true if task was changed and must written back to database

    private ArrayList<TodoSubTask> subTasks = new ArrayList<TodoSubTask>();

    public TodoTask(String title, String description, int progress, Priority priority, long deadline, long reminderTime) {
        this.reminderTime = reminderTime;
        this.title = title;
        this.description = description;
        this.done = false;
        this.progress = progress;
        this.priority = priority;
        this.deadline = deadline;
    }

    public TodoTask(Parcel parcel) {
        id = parcel.readLong();
        reminderTime = parcel.readInt();
        title = parcel.readString();
        description = parcel.readString();
        done = parcel.readByte() != 0;
        progress = parcel.readInt();
        deadline = parcel.readLong();
        listPosition = parcel.readInt();
        priority = Priority.fromInt(parcel.readInt());
        subTasks = parcel.readArrayList(null);
    }

    public void setId(long id){
        this.id = id;
    }

    public void setPositionInList(int pos) {
        this.listPosition = pos;
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
        if(subTasks.size() == 0)
            return progress;

        int numDoneSubTasks = 0;
        for(TodoSubTask subTask : subTasks) {
            numDoneSubTasks += subTask.getDone() ? 1 : 0;
        }
        return (numDoneSubTasks * MAX_PRIORITY) / subTasks.size();
    }

    public Priority getPriority() {
        return priority;
    }

    public void setWriteDbFlag() {
        writeBackToDb = true;
    }

    public int getNumSubtasks() {
        if (subTasks != null)
            return subTasks.size();
        return 0;
    }


    public int getListPosition() {
        return listPosition;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public String getDeadline() {
        if (deadline <= 0)
            return null;
        return Helper.getDate(deadline);
    }

    public long getId() {
        return id;
    }

    public long getDeadlineTs() {
        if (deadline < 0)
            return Long.MAX_VALUE;
        return deadline;
    }

    public DeadlineColors getDeadlineColor() {
        long currentTimeStamp = Helper.getCurrentTimestamp();

        if (!done) {
            if (deadline > currentTimeStamp - reminderTime && deadline < currentTimeStamp)
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
        dest.writeLong(id);
        dest.writeLong(reminderTime);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeByte((byte) (done ? 1 : 0));
        dest.writeInt(progress);
        dest.writeLong(deadline);
        dest.writeInt(listPosition);
        dest.writeInt(priority.getValue());
        dest.writeList(subTasks);
    }

    public boolean isChanged() {
        return writeBackToDb;
    }

    public long getReminderTime() {
        return reminderTime;
    }

    public void setListId(long listId) {
        this.listIdForeignKey = listId;
    }

    public long getListId() {
        return this.listIdForeignKey;
    }
}
