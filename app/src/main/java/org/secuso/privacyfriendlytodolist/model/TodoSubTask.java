package org.secuso.privacyfriendlytodolist.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dominik on 19.05.16.
 */
public class TodoSubTask implements Parcelable, BaseTodo {

    private int id;
    private String title;
    private boolean done;

    private boolean changed = false; // if true, subtask is written back to database

    public TodoSubTask(int id, String title, boolean done) {
        this.title = title;
        this.done = done;
    }

    public TodoSubTask(Parcel parcel) {
        id = parcel.readInt();
        title = parcel.readString();
        done = parcel.readByte() != 0;
    }

    public String getTitle(){
        return title;
    }

    public boolean getDone() {
        return done;
    }

    public void setDone(boolean d) {
        done = d;
        changed = true;
    }

    public static final Parcelable.Creator<TodoSubTask> CREATOR =
            new Creator<TodoSubTask>() {
                @Override
                public TodoSubTask createFromParcel(Parcel source) {
                    return new TodoSubTask(source);
                }

                @Override
                public TodoSubTask[] newArray(int size) {
                    return new TodoSubTask[size];
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
        dest.writeByte((byte) (done ? 1 : 0));
    }

    public long getId() {
        return id;
    }
}
