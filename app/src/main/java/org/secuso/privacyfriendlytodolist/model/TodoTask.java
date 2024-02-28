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

    String PARCELABLE_KEY = "PARCELABLE_KEY_FOR_TODO_TASK";

    enum Priority {
        HIGH, MEDIUM, LOW; // Priority steps must be sorted in the same way like they will be displayed

        public static Priority fromInt(int i) {
            for (Priority p : Priority.values()) {
                if (p.ordinal() == i) {
                    return p;
                }
            }
            throw new IllegalArgumentException("No such priority defined.");
        }
    }

    enum DeadlineColors {
        BLUE,
        ORANGE,
        RED
    }

    void setList(TodoList list);

    TodoList getList();

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

    int getProgress();

    void setListId(int listId);

    int getListId();

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

    void setInTrash(boolean isInTrash);

    boolean isInTrash ();

    boolean checkQueryMatch(String query, boolean recursive);

    boolean checkQueryMatch(String query);
}
