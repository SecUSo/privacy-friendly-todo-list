/*
Privacy Friendly To-Do List
Copyright (C) 2024  SECUSO (www.secuso.org)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package org.secuso.privacyfriendlytodolist.model.impl

import org.secuso.privacyfriendlytodolist.model.BaseTodo

abstract class BaseTodoImpl : BaseTodo {
    enum class RequiredDBAction {
        NONE,
        INSERT,
        UPDATE,
        UPDATE_FROM_POMODORO
    }

    var requiredDBAction: RequiredDBAction = RequiredDBAction.NONE

    override fun setChanged() {
        if (requiredDBAction == RequiredDBAction.NONE) {
            requiredDBAction = RequiredDBAction.UPDATE
        }
    }

    override fun setChangedFromPomodoro() {
        requiredDBAction = RequiredDBAction.UPDATE_FROM_POMODORO
    }

    override fun setUnchanged() {
        requiredDBAction = RequiredDBAction.NONE
    }
}
