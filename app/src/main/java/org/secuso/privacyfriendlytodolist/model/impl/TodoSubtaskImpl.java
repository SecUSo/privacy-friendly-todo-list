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

import org.secuso.privacyfriendlytodolist.model.TodoSubtask;
import org.secuso.privacyfriendlytodolist.model.database.entities.TodoSubtaskData;

import java.util.Objects;

/**
 * Created by Sebastian Lutz on 12.03.2018.
 * <p>
 * Class to set up To-Do subtasks and its parameters.
 */

public class TodoSubtaskImpl extends BaseTodoImpl implements TodoSubtask {

    /** Container for data that gets stored in the database. */
    private final TodoSubtaskData data;

    public TodoSubtaskImpl() {
        data = new TodoSubtaskData();
    }

    public TodoSubtaskImpl(TodoSubtaskData data) {
        this.data = data;
    }

    public TodoSubtaskImpl(Parcel parcel) {
        data = new TodoSubtaskData();
        data.setId(parcel.readInt());
        data.setName(Objects.requireNonNullElse(parcel.readString(), ""));
        data.setDone(parcel.readByte() != 0);
        data.setInRecycleBin(parcel.readByte() != 0);
        data.setTaskId(parcel.readInt());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(data.getId());
        dest.writeString(data.getName());
        dest.writeByte((byte) (data.isDone() ? 1 : 0));
        dest.writeByte((byte) (data.isInRecycleBin() ? 1 : 0));
        dest.writeInt(data.getTaskId());
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
    public void setTaskId(int taskIdForeignKey) {
        data.setTaskId(taskIdForeignKey);
    }

    @Override
    public int getTaskId() {
        return data.getTaskId();
    }

    @Override
    public void setDone(boolean isDone) {
        data.setDone(isDone);
    }

    @Override
    public boolean isDone() {
        return data.isDone();
    }

    @Override
    public void setInRecycleBin(boolean isInRecycleBin) {
        data.setInRecycleBin(isInRecycleBin);
    }

    @Override
    public boolean isInRecycleBin() {
        return data.isInRecycleBin();
    }

    public static final Creator<TodoSubtaskImpl> CREATOR =
            new Creator<>() {
                @Override
                public TodoSubtaskImpl createFromParcel(Parcel source) {
                    return new TodoSubtaskImpl(source);
                }

                @Override
                public TodoSubtaskImpl[] newArray(int size) {
                    return new TodoSubtaskImpl[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean checkQueryMatch(String query)
    {
        // no query? always match!
        if (query == null || query.isEmpty()) {
            return true;
        }

        String queryLowerCase = query.toLowerCase();
        return (data.getName().toLowerCase().contains(queryLowerCase));
    }

    TodoSubtaskData getData() {
        return data;
    }
}
