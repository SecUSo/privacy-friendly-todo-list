package org.secuso.privacyfriendlytodolist.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;


import org.secuso.privacyfriendlytodolist.model.database.DBQueryHandler;

import java.util.ArrayList;

public class TodoTask extends BaseTodo implements Parcelable {

    private static final String TAG = TodoTask.class.getSimpleName();
    public static final String PARCELABLE_KEY = "key_for_parcels";

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

    private boolean done;
    private int progress;
    private Priority priority;
    private long reminderTime = -1; // absolute timestamp
    private int listPosition; // indicates at what position inside the list this task it placed
    private int listIdForeignKey;
    protected long deadline; // absolute timestamp

    private boolean reminderTimeChanged = false; // important for the reminder service
    private boolean reminderTimeWasInitialized = false;


    private ArrayList<TodoSubTask> subTasks = new ArrayList<TodoSubTask>();

    public TodoTask() {
        super();
        done = false;
    }

    public TodoTask(Parcel parcel) {
        id = parcel.readInt();
        listIdForeignKey = parcel.readInt();
        name = parcel.readString();
        description = parcel.readString();
        done = parcel.readByte() != 0;
        progress = parcel.readInt();
        deadline = parcel.readLong();
        reminderTime = parcel.readLong();
        listPosition = parcel.readInt();
        priority = Priority.fromInt(parcel.readInt());
        parcel.readList(subTasks, TodoSubTask.class.getClassLoader());
    }

    public long getDeadline() {
        return deadline;
    }
    public void setDeadline(long deadline) {

        this.deadline = deadline;
        if(deadline < reminderTime) {
            Log.i(TAG, "Reminder time resetted, because new deadline is greater.");
        }
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
        return progress;
    }

    public Priority getPriority() {
        return priority;
    }

    public int getListPosition() {
        return listPosition;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    // This method expects the deadline to be greater than the reminder time.
    public DeadlineColors getDeadlineColor(long defaultReminderTime) {

        // The default reminder time is a relative value in seconds (e.g. 86400s == 1 day)
        // The user specified reminder time is an absolute timestamp

        if (!done && deadline > 0) {

            long currentTimeStamp = Helper.getCurrentTimestamp();
            long remTimeToCalc = reminderTime > 0 ? deadline-reminderTime : defaultReminderTime;

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
        dest.writeInt(id);
        dest.writeInt(listIdForeignKey);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeByte((byte) (done ? 1 : 0));
        dest.writeInt(progress);
        dest.writeLong(deadline);
        dest.writeLong(reminderTime);
        dest.writeInt(listPosition);
        dest.writeInt(priority.getValue());
        dest.writeList(subTasks);
    }

    public long getReminderTime() {
        return reminderTime;
    }

    public void setListId(int listId) {
        this.listIdForeignKey = listId;
    }

    public int getListId() {
        return this.listIdForeignKey;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void setReminderTime(long reminderTime) {

        if(reminderTime > deadline && deadline > 0) {
            Log.i(TAG, "Reminder time must not be greater than the deadline.");
        }
        else {
            this.reminderTime = reminderTime;
        }

        // check if reminder time was already set and now changed -> important for reminder service

        if(reminderTimeWasInitialized)
            reminderTimeChanged = true;

        reminderTimeWasInitialized = true;
    }

    public boolean reminderTimeChanged() {
        return reminderTimeChanged;
    }

    public void resetReminderTimeChangedStatus() {
        reminderTimeChanged = false;
    }

    public void setAllSubTasksDone(boolean doneSubTask) {
        for(TodoSubTask subTask : subTasks)
            subTask.setDone(doneSubTask);
    }

    // A task is done if the user manually sets it done or when all subtaks are done.
    // If a subtask is selected "done", the entire task might be "done" if by now all subtasks are done.
    public void doneStatusChanged() {
        boolean doneSubTasks = true;

        int i = 0;
        while(doneSubTasks && i<subTasks.size()) {
            doneSubTasks &= subTasks.get(i).getDone();
            i++;
        }


        if(doneSubTasks != done) {
            dbState = DBQueryHandler.ObjectStates.UPDATE_DB;

        }

        done = doneSubTasks;
    }

}
