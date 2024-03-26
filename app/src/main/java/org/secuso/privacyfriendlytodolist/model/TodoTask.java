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

package org.secuso.privacyfriendlytodolist.model;

import android.os.Parcelable;

import java.util.List;

public interface TodoTask extends BaseTodo, Parcelable {

    enum Priority {
        HIGH, MEDIUM, LOW; // Priority steps must be sorted in the same way like they will be displayed

        public static final Priority DEFAULT_VALUE = MEDIUM;

        /** Number of priorities. */
        public static final int LENGTH = values().length;

        /**
         * Provides the priority enumeration value that matches the given ordinal number.
         *
         * @param ordinal The ordinal number of the requested priority enumeration value.
         * @return The requested priority enumeration value if the given ordinal is valid. Otherwise null.
         */
        public static Priority fromOrdinal(int ordinal) {
            return (ordinal >= 0 && ordinal < LENGTH) ? values()[ordinal] : null;
        }
    }

    enum DeadlineColors {
        BLUE,
        ORANGE,
        RED
    }

    void setId(int id);

    int getId();

    void setName(String name);

    String getName();

    void setDescription(String description);

    String getDescription();

    /**
     * @param listId The ID of the associated list or {@link TodoList#DUMMY_LIST_ID} if no list is associated.
     */
    void setListId(Integer listId);

    /**
     * @return The ID of the associated list or {@link TodoList#DUMMY_LIST_ID} if no list is associated.
     */
    Integer getListId();

    void setDeadline(long deadline);

    long getDeadline();

    boolean hasDeadline();

    void setListPosition(int position);

    int getListPosition();

    void setSubtasks(List<TodoSubtask> subtasks);

    List<TodoSubtask> getSubtasks();

    // This method expects the deadline to be greater than the reminder time.
    DeadlineColors getDeadlineColor(long defaultReminderTime);

    void setPriority(Priority priority);

    Priority getPriority();

    void setProgress(int progress);

    /**
     *
     * @param computeProgress If true, the progress of the task gets computed depending on the
     *                        subtasks done-state. This progress also gets stored so that a further
     *                        call with 'false' will return the same value (until next computation
     *                        or {@link #setProgress(int)} gets called).
     *                        If false, the last stored value gets returned.
     * @return The progress of the task in percent (values in range 0 - 100).
     */
    int getProgress(boolean computeProgress);

    void setReminderTime(long reminderTime);

    long getReminderTime();

    boolean reminderTimeChanged();

    void resetReminderTimeChangedStatus();

    void setAllSubtasksDone(boolean isDone);

    void setDone(boolean isDone);

    boolean isDone();

    /**
     * A task is done if the user manually sets it done or when all subtasks are done.
     * If a subtask is selected "done", the entire task might be "done" if by now all subtasks are done.
     */
    void doneStatusChanged();

    void setInRecycleBin(boolean isInRecycleBin);

    boolean isInRecycleBin ();

    boolean checkQueryMatch(String query, boolean recursive);

    boolean checkQueryMatch(String query);
}
