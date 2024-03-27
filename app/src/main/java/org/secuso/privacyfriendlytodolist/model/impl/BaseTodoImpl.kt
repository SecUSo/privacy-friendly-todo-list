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
package org.secuso.privacyfriendlytodolist.model.impl

import org.secuso.privacyfriendlytodolist.model.BaseTodo

abstract class BaseTodoImpl : BaseTodo {
    enum class ObjectStates {
        INSERT_TO_DB,
        UPDATE_DB,
        UPDATE_FROM_POMODORO,
        NO_DB_ACTION
    }

    var dbState: ObjectStates = ObjectStates.NO_DB_ACTION
        protected set

    override fun setCreated() {
        dbState = ObjectStates.INSERT_TO_DB
    }

    override fun setChanged() {
        if (dbState == ObjectStates.NO_DB_ACTION) {
            dbState = ObjectStates.UPDATE_DB
        }
    }

    override fun setChangedFromPomodoro() {
        dbState = ObjectStates.UPDATE_FROM_POMODORO
    }

    override fun setUnchanged() {
        dbState = ObjectStates.NO_DB_ACTION
    }
}
