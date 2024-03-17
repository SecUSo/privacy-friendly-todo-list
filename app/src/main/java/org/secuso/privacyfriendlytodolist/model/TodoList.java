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

/**
 * Created by Sebastian Lutz on 12.03.2018.
 *
 * Class to set up a To-Do List and its parameters.
 */

public interface TodoList extends BaseTodo, Parcelable {

    int DUMMY_LIST_ID = -3; // -1 is often used for error codes

    void setId(int id);

    int getId();

    void setName(String name);

    String getName();

    void setDummyList();

    boolean isDummyList();

    int getSize();

    void setTasks(List<TodoTask> tasks);

    List<TodoTask> getTasks();

    int getColor();

    int getDoneTodos();

    long getNextDeadline();

    TodoTask.DeadlineColors getDeadlineColor(long defaultReminderTime);

    boolean checkQueryMatch(String query, boolean recursive);

    boolean checkQueryMatch(String query);
}
