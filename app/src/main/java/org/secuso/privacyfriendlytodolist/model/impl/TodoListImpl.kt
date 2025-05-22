/*
Privacy Friendly To-Do List
Copyright (C) 2018-2025  Sebastian Lutz

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

import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable.Creator
import org.secuso.privacyfriendlytodolist.model.TodoList
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.model.database.entities.TodoListData
import java.util.Locale


/**
 * Created by Sebastian Lutz on 12.03.2018.
 *
 * Class to set up a To-Do List and its parameters.
 */
class TodoListImpl : BaseTodoImpl, TodoList {
    /** Container for data that gets stored in the database.  */
    val data: TodoListData

    private var tasks: MutableList<TodoTask> = ArrayList()
    private var isDummyList = false

    constructor() {
        data = TodoListData()
        isDummyList = true
        // New item needs to be stored in database.
        requiredDBAction = RequiredDBAction.INSERT
    }

    constructor(data: TodoListData) {
        this.data = data
        isDummyList = false
    }

    constructor(parcel: Parcel) {
        data = TodoListData()
        data.id = parcel.readInt()
        data.sortOrder = parcel.readInt()
        data.name = parcel.readString()!!
        isDummyList = parcel.readByte().toInt() != 0
        parcel.readTypedList(tasks, TodoTaskImpl.CREATOR)
        // The duplicated object shall not duplicate the RequiredDBAction. The original object shall
        // ensure that DB action gets done. So keep initial value RequiredDBAction.NONE.
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(data.id)
        dest.writeInt(data.sortOrder)
        dest.writeString(data.name)
        dest.writeByte((if (isDummyList) 1 else 0).toByte())
        dest.writeTypedList(tasks)
    }

    override fun setId(id: Int) {
        data.id = id
        isDummyList = false
    }

    override fun getId(): Int {
        return data.id
    }

    override fun isDummyList(): Boolean {
        return isDummyList
    }

    override fun setSortOrder(sortOrder: Int) {
        data.sortOrder = sortOrder
    }

    override fun getSortOrder(): Int {
        return data.sortOrder
    }

    override fun setName(name: String) {
        data.name = name
    }

    override fun getName(): String {
        return data.name
    }

    override fun getSize(): Int {
        return tasks.size
    }

    override fun setTasks(tasks: MutableList<TodoTask>) {
        this.tasks = tasks
    }

    override fun getTasks(): MutableList<TodoTask> {
        return tasks
    }

    override fun getColor(): Int {
        return Color.BLACK
    }

    override fun getDoneTodos(): Int {
        var counter = 0
        for (task in tasks) {
            if (task.isDone()) {
                ++counter
            }
        }
        return counter
    }

    override fun getNextDeadline(): Long? {
        var minDeadLine: Long? = null
        for (currentTask in tasks) {
            val currentDeadline = currentTask.getDeadline()
            if (currentTask.isDone() || currentDeadline == null) {
                continue
            }
            if (minDeadLine == null || currentDeadline < minDeadLine) {
                minDeadLine = currentDeadline
            }
        }
        return minDeadLine
    }

    override fun checkQueryMatch(query: String?, recursive: Boolean): Boolean {
        // no query? always match!
        if (query.isNullOrEmpty()) {
            return true
        }
        val queryLowerCase = query.lowercase(Locale.getDefault())
        if (data.name.lowercase(Locale.getDefault()).contains(queryLowerCase)) {
            return true
        }
        if (recursive) {
            for (task in tasks) {
                if (task.checkQueryMatch(queryLowerCase, true)) {
                    return true
                }
            }
        }
        return false
    }

    override fun checkQueryMatch(query: String?): Boolean {
        return checkQueryMatch(query, true)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "TodoList(name=${getName()}, id=${getId()})"
    }

    companion object {
        @JvmField
        val CREATOR = object : Creator<TodoList> {
            override fun createFromParcel(parcel: Parcel): TodoList {
                return TodoListImpl(parcel)
            }

            override fun newArray(size: Int): Array<TodoList?> {
                return arrayOfNulls(size)
            }
        }
    }
}
