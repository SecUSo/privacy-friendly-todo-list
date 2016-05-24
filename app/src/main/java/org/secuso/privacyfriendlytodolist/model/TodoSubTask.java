package org.secuso.privacyfriendlytodolist.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dominik on 19.05.16.
 */
public class TodoSubTask implements Parcelable {

    private String title;
    private boolean done;

    public TodoSubTask(String title, boolean done) {
        this.title = title;
        this.done = done;
    }

    public TodoSubTask(Parcel parcel) {
        title = parcel.readString();
        done = parcel.readByte() != 0;
    }

    public String getTitle(){
        return title;
    }

    public boolean getDone() {
        return done;
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
        dest.writeString(title);
        dest.writeByte((byte) (done ? 1 : 0));
    }
}
