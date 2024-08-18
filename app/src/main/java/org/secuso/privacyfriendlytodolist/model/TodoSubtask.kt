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
package org.secuso.privacyfriendlytodolist.model

import android.os.Parcelable

interface TodoSubtask : BaseTodo, Parcelable {
    fun setId(id: Int)
    fun getId(): Int
    fun setName(name: String)
    fun getName(): String
    fun setTaskId(taskIdForeignKey: Int)
    fun getTaskId(): Int
    fun setDone(isDone: Boolean)
    fun isDone(): Boolean
    fun setDoneTime(doneTime: Long)
    fun getDoneTime(): Long
    fun setInRecycleBin(isInRecycleBin: Boolean)
    fun isInRecycleBin(): Boolean
    fun checkQueryMatch(query: String?): Boolean
}
