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

import android.os.Parcel
import android.os.Parcelable.Creator
import org.secuso.privacyfriendlytodolist.model.TodoSubtask
import org.secuso.privacyfriendlytodolist.model.database.entities.TodoSubtaskData
import org.secuso.privacyfriendlytodolist.util.Helper
import java.util.Locale

/**
 * Created by Sebastian Lutz on 12.03.2018.
 *
 *
 * Class to set up To-Do subtasks and its parameters.
 */
class TodoSubtaskImpl : BaseTodoImpl, TodoSubtask {
    /** Container for data that gets stored in the database.  */
    val data: TodoSubtaskData

    constructor() {
        data = TodoSubtaskData()
        // New item needs to be stored in database.
        requiredDBAction = RequiredDBAction.INSERT
    }

    constructor(data: TodoSubtaskData) {
        this.data = data
    }

    constructor(parcel: Parcel) {
        data = TodoSubtaskData()
        data.id = parcel.readInt()
        data.sortOrder = parcel.readInt()
        data.name = parcel.readString()!!
        data.doneTime = parcel.readValue(Long::class.java.classLoader) as Long?
        data.isInRecycleBin = parcel.readByte().toInt() != 0
        data.taskId = parcel.readInt()
        // The duplicated object shall not duplicate the RequiredDBAction. The original object shall
        // ensure that DB action gets done. So keep initial value RequiredDBAction.NONE.
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(data.id)
        dest.writeInt(data.sortOrder)
        dest.writeString(data.name)
        dest.writeValue(data.doneTime)
        dest.writeByte((if (data.isInRecycleBin) 1 else 0).toByte())
        dest.writeInt(data.taskId)
    }

    override fun setId(id: Int) {
        data.id = id
    }

    override fun getId(): Int {
        return data.id
    }

    override fun setName(name: String) {
        data.name = name
    }

    override fun getName(): String {
        return data.name
    }

    override fun setTaskId(taskIdForeignKey: Int) {
        data.taskId = taskIdForeignKey
    }

    override fun getTaskId(): Int {
        return data.taskId
    }

    override fun setSortOrder(sortOrder: Int) {
        data.sortOrder = sortOrder
    }

    override fun getSortOrder(): Int {
        return data.sortOrder
    }

    override fun setDone(isDone: Boolean) {
        data.doneTime = if (isDone) Helper.getCurrentTimestamp() else null
    }

    override fun isDone(): Boolean {
        return data.doneTime != null
    }

    override fun setDoneTime(doneTime: Long?) {
        data.doneTime = doneTime
    }

    override fun getDoneTime(): Long? {
        return data.doneTime
    }

    override fun setInRecycleBin(isInRecycleBin: Boolean) {
        data.isInRecycleBin = isInRecycleBin
    }

    override fun isInRecycleBin(): Boolean {
        return data.isInRecycleBin
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun checkQueryMatch(query: String?): Boolean {
        // no query? always match!
        if (query.isNullOrEmpty()) {
            return true
        }
        val queryLowerCase = query.lowercase(Locale.getDefault())
        return data.name.lowercase(Locale.getDefault()).contains(queryLowerCase)
    }

    override fun toString(): String {
        return "TodoSubtask(name=${getName()}, id=${getId()})"
    }

    companion object {
        @JvmField
        val CREATOR = object : Creator<TodoSubtask> {
            override fun createFromParcel(parcel: Parcel): TodoSubtask {
                return TodoSubtaskImpl(parcel)
            }

            override fun newArray(size: Int): Array<TodoSubtask?> {
                return arrayOfNulls(size)
            }
        }
    }
}
