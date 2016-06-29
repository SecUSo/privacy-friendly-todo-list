package org.secuso.privacyfriendlytodolist.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;


import java.util.ArrayList;

public class TodoTask extends BaseTodo implements Parcelable {

    public TodoTask() {

    }

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

    private enum ReminderTimeStatus {
        NOT_SET
    }

    public static final int MAX_PRIORITY = 10;

    private String title;
    private boolean done;
    private long id;
    private int progress;
    private Priority priority;
    private long reminderTime = -1;
    private int listPosition; // indicates at what position inside the list this task it placed

    private long listIdForeignKey;

    private boolean writeBackToDb = false; // true if task was changed and must written back to database


    protected long deadline;


    private ArrayList<TodoSubTask> subTasks = new ArrayList<TodoSubTask>();

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

    public long getDeadline() {
        return deadline;
    }
    public void setDeadline(long deadline) {
        this.deadline = deadline;
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

    public long getId() {
        return id;
    }


    // This method expects the deadline to be greater than the reminder time.
    public DeadlineColors getDeadlineColor(long defaultReminderTime) {

        // The default reminder time is a relative value in seconds (e.g. 86400s == 1 day)
        // The user specified reminder time is an absolute timestamp

        if (!done && deadline > 0) {

            long currentTimeStamp = Helper.getCurrentTimestamp();
            long remTimeToCalc = reminderTime > 0 ? deadline-reminderTime : defaultReminderTime;

            String d = Helper.getDateTime(deadline);
            String r = String.valueOf(remTimeToCalc / 60 / 60 / 24);
            String t = Helper.getDateTime(currentTimeStamp);

            Log.i("TASK NAME", name);
            Log.i("TASK USER REMINDER TIME", String.valueOf(reminderTime));
            Log.i("TASK DEADLINE ", d + " " + deadline);
            Log.i("TASK REMINDER ", r + " " + remTimeToCalc);
            Log.i("TASK TIMESTAMP", t + " " + currentTimeStamp);
            Log.i("DIFF", Helper.getDateTime(deadline - remTimeToCalc));

            if ((currentTimeStamp >= (deadline - remTimeToCalc)) && (deadline > currentTimeStamp))
                return DeadlineColors.ORANGE;

            if ((currentTimeStamp > deadline) && (deadline > 0))
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

    public void setPriority(Priority prio) {
        priority = prio;
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

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void setReminderTime(long reminderTime) {
        this.reminderTime = reminderTime;
    }

}
