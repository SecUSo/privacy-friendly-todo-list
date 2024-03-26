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

package org.secuso.privacyfriendlytodolist.model.impl;

import android.os.Parcel;
import android.util.Log;

import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.model.TodoSubtask;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.model.database.entities.TodoTaskData;
import org.secuso.privacyfriendlytodolist.util.Helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Sebastian Lutz on 12.03.2018.
 * <p>
 * Class to set up To-Do Tasks and its parameters.
 */

public class TodoTaskImpl extends BaseTodoImpl implements TodoTask {

    private static final String TAG = TodoTaskImpl.class.getSimpleName();


    /** Container for data that gets stored in the database. */
    private final TodoTaskData data;
    /** Important for the reminder service. */
    private boolean reminderTimeChanged = false;
    private boolean reminderTimeWasInitialized = false;
    private List<TodoSubtask> subtasks = new ArrayList<>();

    public TodoTaskImpl() {
        data = new TodoTaskData();
    }

    public TodoTaskImpl(TodoTaskData data) {
        this.data = data;
    }

    public TodoTaskImpl(Parcel parcel) {
        data = new TodoTaskData();
        data.setId(parcel.readInt());
        if (0 != parcel.readByte()) {
            data.setListId(parcel.readInt());
        } else {
            parcel.readInt();
            data.setListId(TodoList.DUMMY_LIST_ID);
        }
        data.setName(Objects.requireNonNullElse(parcel.readString(), ""));
        data.setDescription(Objects.requireNonNullElse(parcel.readString(), ""));
        data.setDone(parcel.readByte() != 0);
        data.setInRecycleBin(parcel.readByte() != 0);
        data.setProgress(parcel.readInt());
        data.setDeadline(parcel.readLong());
        data.setReminderTime(parcel.readLong());
        data.setListPosition(parcel.readInt());
        Priority priority = Priority.fromOrdinal(parcel.readInt());
        data.setPriority(Objects.requireNonNullElse(priority, Priority.DEFAULT_VALUE));
        parcel.readList(subtasks, TodoSubtaskImpl.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(data.getId());
        if (null != data.getListId()) {
            dest.writeByte((byte)1);
            dest.writeInt(data.getListId());
        } else {
            dest.writeByte((byte)0);
            dest.writeInt(0);
        }
        dest.writeString(data.getName());
        dest.writeString(data.getDescription());
        dest.writeByte((byte)(data.isDone() ? 1 : 0));
        dest.writeByte((byte)(data.isInRecycleBin() ? 1 : 0));
        dest.writeInt(data.getProgress());
        dest.writeLong(data.getDeadline());
        dest.writeLong(data.getReminderTime());
        dest.writeInt(data.getListPosition());
        dest.writeInt(data.getPriority().ordinal());
        dest.writeList(subtasks);
    }

    @Override
    public void setId(int id) {
        data.setId(id);
    }

    @Override
    public int getId() {
        return data.getId();
    }

    @Override
    public void setName(String name) {
        data.setName(name);
    }

    @Override
    public String getName() {
        return data.getName();
    }

    @Override
    public void setDescription(String description) {
        data.setDescription(description);
    }

    @Override
    public String getDescription() {
        return data.getDescription();
    }

    @Override
    public void setListId(Integer listId) {
        data.setListId(listId);
    }

    @Override
    public Integer getListId() {
        return data.getListId();
    }

    @Override
    public void setDeadline(long deadline) {
        data.setDeadline(deadline);
    }

    @Override
    public long getDeadline() {
        return data.getDeadline();
    }

    @Override
    public boolean hasDeadline() {
        return data.getDeadline() > 0;
    }

    @Override
    public void setListPosition(int position) {
        data.setListPosition(position);
    }

    @Override
    public int getListPosition() {
        return data.getListPosition();
    }

    @Override
    public void setSubtasks(List<TodoSubtask> subtasks) {
        this.subtasks = subtasks;
    }

    @Override
    public List<TodoSubtask> getSubtasks() {
        return subtasks;
    }

    @Override
    // This method expects the deadline to be greater than the reminder time.
    public DeadlineColors getDeadlineColor(long defaultReminderTime) {

        // The default reminder time is a relative value in seconds (e.g. 86400s == 1 day)
        // The user specified reminder time is an absolute timestamp

        DeadlineColors dDeadlineColor = DeadlineColors.BLUE;
        final long deadline = data.getDeadline();
        final long reminderTime = data.getReminderTime();
        if (!data.isDone() && deadline > 0) {

            long currentTimeStamp = Helper.getCurrentTimestamp();
            long remTimeToCalc = reminderTime > 0 ? deadline-reminderTime : defaultReminderTime;

            if ((currentTimeStamp >= (deadline - remTimeToCalc)) && (deadline > currentTimeStamp)) {
                dDeadlineColor = DeadlineColors.ORANGE;
            }
            else if (currentTimeStamp > deadline) {
                dDeadlineColor = DeadlineColors.RED;
            }
        }

        return dDeadlineColor;
    }

    @Override
    public void setPriority(Priority priority) {
        data.setPriority(priority);
    }

    @Override
    public Priority getPriority() {
        return data.getPriority();
    }

    @Override
    public void setProgress(int progress) {
        data.setProgress(progress);
    }

    @Override
    public int getProgress(boolean computeProgress) {
        if (computeProgress) {
            int progress;
            if (0 == subtasks.size()) {
                progress = isDone() ? 100 : 0;
            } else {
                int doneSubtasks = 0;
                for (TodoSubtask todoSubtask : subtasks) {
                    if (todoSubtask.isDone()) {
                        ++doneSubtasks;
                    }
                }
                progress = (doneSubtasks * 100) / subtasks.size();
            }
            setProgress(progress);
        }

        return data.getProgress();
    }

    public static final Creator<TodoTaskImpl> CREATOR =
        new Creator<>() {
            @Override
            public TodoTaskImpl createFromParcel(Parcel source) {
                return new TodoTaskImpl(source);
            }

            @Override
            public TodoTaskImpl[] newArray(int size) {
                return new TodoTaskImpl[size];
            }
        };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void setReminderTime(long reminderTime) {
        long deadline = data.getDeadline();
        if(reminderTime > deadline && deadline > 0) {
            Log.i(TAG, "Reminder time must not be greater than the deadline.");
        }
        else {
            data.setReminderTime(reminderTime);
        }

        // check if reminder time was already set and now changed -> important for reminder service
        if (reminderTimeWasInitialized) {
            reminderTimeChanged = true;
        }
        reminderTimeWasInitialized = true;
    }

    @Override
    public long getReminderTime() {
        return data.getReminderTime();
    }

    @Override
    public boolean reminderTimeChanged() {
        return reminderTimeChanged;
    }

    @Override
    public void resetReminderTimeChangedStatus() {
        reminderTimeChanged = false;
    }

    @Override
    public void setAllSubtasksDone(boolean isDone) {
        for(TodoSubtask subtask : subtasks) {
            subtask.setDone(isDone);
        }
    }

    @Override
    public void setDone(boolean isDone) {
        data.setDone(isDone);
    }

    @Override
    public boolean isDone() {
        return data.isDone();
    }

    // A task is done if the user manually sets it done or when all subtasks are done.
    // If a subtask is selected "done", the entire task might be "done" if by now all subtasks are done.
    @Override
    public void doneStatusChanged() {
        boolean allSubtasksAreDone = true;
        for (TodoSubtask subtask : subtasks)
        {
            if (!subtask.isDone())
            {
                allSubtasksAreDone = false;
                break;
            }
        }

        if (allSubtasksAreDone != data.isDone()) {
            dbState = ObjectStates.UPDATE_DB;
        }

        data.setDone(allSubtasksAreDone);
    }

    @Override
    public void setInRecycleBin(boolean isInRecycleBin) {
        data.setInRecycleBin(isInRecycleBin);
    }

    @Override
    public boolean isInRecycleBin() {
        return data.isInRecycleBin();
    }

    @Override
    public boolean checkQueryMatch(String query, boolean recursive) {
        // no query? always match!
        if (query == null || query.isEmpty()) {
            return true;
        }
        String queryLowerCase = query.toLowerCase();
        if (data.getName().toLowerCase().contains(queryLowerCase)) {
            return true;
        }
        if (data.getDescription().toLowerCase().contains(queryLowerCase)) {
            return true;
        }
        if (recursive) {
            for (TodoSubtask subtask : subtasks) {
                if (subtask.checkQueryMatch(queryLowerCase)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean checkQueryMatch(String query) {
        return checkQueryMatch(query, true);
    }

    TodoTaskData getData() {
        return data;
    }
}
