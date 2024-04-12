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
import android.util.Log
import org.secuso.privacyfriendlytodolist.model.TodoSubtask
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.model.TodoTask.DeadlineColors
import org.secuso.privacyfriendlytodolist.model.database.entities.TodoTaskData
import org.secuso.privacyfriendlytodolist.util.Helper
import java.util.Locale

/**
 * Created by Sebastian Lutz on 12.03.2018.
 *
 * Class to set up To-Do Tasks and its parameters.
 */
class TodoTaskImpl : BaseTodoImpl, TodoTask {
    /** Container for data that gets stored in the database.  */
    val data: TodoTaskData

    /** Important for the reminder service.  */
    private var reminderTimeChanged = false
    private var reminderTimeWasInitialized = false
    private var subtasks: MutableList<TodoSubtask> = ArrayList()

    constructor() {
        data = TodoTaskData()
        // New item needs to be stored in database.
        requiredDBAction = RequiredDBAction.INSERT
    }

    constructor(data: TodoTaskData) {
        this.data = data
    }

    constructor(parcel: Parcel) {
        data = TodoTaskData()
        data.id = parcel.readInt()
        if (0 != parcel.readByte().toInt()) {
            data.listId = parcel.readInt()
        } else {
            parcel.readInt()
            data.listId = null
        }
        data.name = parcel.readString()!!
        data.description = parcel.readString()!!
        data.isDone = parcel.readByte() != 0.toByte()
        data.isInRecycleBin = parcel.readByte() != 0.toByte()
        data.progress = parcel.readInt()
        data.deadline = parcel.readLong()
        data.reminderTime = parcel.readLong()
        reminderTimeChanged = parcel.readByte() != 0.toByte()
        reminderTimeWasInitialized = parcel.readByte() != 0.toByte()
        data.listPosition = parcel.readInt()
        data.priority = TodoTask.Priority.fromOrdinal(parcel.readInt())!!
        parcel.readList(subtasks, TodoSubtaskImpl::class.java.getClassLoader())
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(data.id)
        if (null != data.listId) {
            dest.writeByte(1.toByte())
            dest.writeInt(data.listId!!)
        } else {
            dest.writeByte(0.toByte())
            dest.writeInt(0)
        }
        dest.writeString(data.name)
        dest.writeString(data.description)
        dest.writeByte((if (data.isDone) 1 else 0).toByte())
        dest.writeByte((if (data.isInRecycleBin) 1 else 0).toByte())
        dest.writeInt(data.progress)
        dest.writeLong(data.deadline)
        dest.writeLong(data.reminderTime)
        dest.writeByte((if (reminderTimeChanged) 1 else 0).toByte())
        dest.writeByte((if (reminderTimeWasInitialized) 1 else 0).toByte())
        dest.writeInt(data.listPosition)
        dest.writeInt(data.priority.ordinal)
        dest.writeList(subtasks)
        // Parcel-interface is used for data backup.
        // This use case does not require that 'dbState' gets stored in the parcel.
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

    override fun setDescription(description: String) {
        data.description = description
    }

    override fun getDescription(): String {
        return data.description
    }

    override fun setListId(listId: Int?) {
        data.listId = listId
    }

    override fun getListId(): Int? {
        return data.listId
    }

    override fun setDeadline(deadline: Long) {
        data.deadline = deadline
    }

    override fun getDeadline(): Long {
        return data.deadline
    }

    override fun hasDeadline(): Boolean {
        return data.deadline > 0
    }

    override fun setListPosition(position: Int) {
        data.listPosition = position
    }

    override fun getListPosition(): Int {
        return data.listPosition
    }

    override fun setSubtasks(subtasks: MutableList<TodoSubtask>) {
        this.subtasks = subtasks
    }

    override fun getSubtasks(): MutableList<TodoSubtask> {
        return subtasks
    }

    // This method expects the deadline to be greater than the reminder time.
    override fun getDeadlineColor(defaultReminderTime: Long): DeadlineColors {

        // The default reminder time is a relative value in seconds (e.g. 86400s == 1 day)
        // The user specified reminder time is an absolute timestamp
        var dDeadlineColor = DeadlineColors.BLUE
        val deadline = data.deadline
        val reminderTime = data.reminderTime
        if (!data.isDone && deadline > 0) {
            val currentTimeStamp = Helper.getCurrentTimestamp()
            val remTimeToCalc = if (reminderTime > 0) deadline - reminderTime else defaultReminderTime
            if (currentTimeStamp >= deadline - remTimeToCalc && deadline > currentTimeStamp) {
                dDeadlineColor = DeadlineColors.ORANGE
            } else if (currentTimeStamp > deadline) {
                dDeadlineColor = DeadlineColors.RED
            }
        }
        return dDeadlineColor
    }

    override fun setPriority(priority: TodoTask.Priority) {
        data.priority = priority
    }

    override fun getPriority(): TodoTask.Priority {
        return data.priority
    }

    override fun setProgress(progress: Int) {
        data.progress = progress
    }

    override fun getProgress(computeProgress: Boolean): Int {
        if (computeProgress) {
            val progress: Int
            @Suppress("LiftReturnOrAssignment")
            if (0 == subtasks.size) {
                progress = if (isDone()) 100 else 0
            } else {
                var doneSubtasks = 0
                for (todoSubtask in subtasks) {
                    if (todoSubtask.isDone()) {
                        ++doneSubtasks
                    }
                }
                progress = doneSubtasks * 100 / subtasks.size
            }
            setProgress(progress)
        }
        return data.progress
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun setReminderTime(reminderTime: Long) {
        val deadline = data.deadline
        if (deadline in 1..<reminderTime) {
            Log.i(TAG, "Reminder time must not be greater than the deadline.")
        } else {
            data.reminderTime = reminderTime
        }

        // check if reminder time was already set and now changed -> important for reminder service
        if (reminderTimeWasInitialized) {
            reminderTimeChanged = true
        }
        reminderTimeWasInitialized = true
    }

    override fun getReminderTime(): Long {
        return data.reminderTime
    }

    override fun reminderTimeChanged(): Boolean {
        return reminderTimeChanged
    }

    override fun resetReminderTimeChangedStatus() {
        reminderTimeChanged = false
    }

    override fun setAllSubtasksDone(isDone: Boolean) {
        for (subtask in subtasks) {
            subtask.setDone(isDone)
        }
    }

    override fun setDone(isDone: Boolean) {
        data.isDone = isDone
    }

    override fun isDone(): Boolean {
        return data.isDone
    }

    // A task is done if the user manually sets it done or when all subtasks are done.
    // If a subtask is selected "done", the entire task might be "done" if by now all subtasks are done.
    override fun doneStatusChanged() {
        var allSubtasksAreDone = true
        for (subtask in subtasks) {
            if (!subtask.isDone()) {
                allSubtasksAreDone = false
                break
            }
        }
        if (data.isDone != allSubtasksAreDone) {
            data.isDone = allSubtasksAreDone
            requiredDBAction = RequiredDBAction.UPDATE
        }
    }

    override fun setInRecycleBin(isInRecycleBin: Boolean) {
        data.isInRecycleBin = isInRecycleBin
    }

    override fun isInRecycleBin(): Boolean {
        return data.isInRecycleBin
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
        if (data.description.lowercase(Locale.getDefault()).contains(queryLowerCase)) {
            return true
        }
        if (recursive) {
            for (subtask in subtasks) {
                if (subtask.checkQueryMatch(queryLowerCase)) {
                    return true
                }
            }
        }
        return false
    }

    override fun checkQueryMatch(query: String?): Boolean {
        return checkQueryMatch(query, true)
    }

    override fun toString(): String {
        return "'${getName()}' (id ${getId()})"
    }

    companion object CREATOR : Creator<TodoTaskImpl> {
        private val TAG = TodoTaskImpl::class.java.getSimpleName()
        override fun createFromParcel(parcel: Parcel): TodoTaskImpl {
            return TodoTaskImpl(parcel)
        }

        override fun newArray(size: Int): Array<TodoTaskImpl?> {
            return arrayOfNulls(size)
        }
    }
}
