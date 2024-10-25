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

import android.os.Parcel
import android.os.Parcelable.Creator
import org.secuso.privacyfriendlytodolist.model.TodoSubtask
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.model.TodoTask.DeadlineColors
import org.secuso.privacyfriendlytodolist.model.TodoTask.Priority
import org.secuso.privacyfriendlytodolist.model.TodoTask.RecurrencePattern
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
        data.listId = parcel.readValue(Int::class.java.classLoader) as Int?
        data.name = parcel.readString()!!
        data.description = parcel.readString()!!
        data.creationTime = parcel.readLong()
        data.doneTime = parcel.readValue(Long::class.java.classLoader) as Long?
        data.isInRecycleBin = parcel.readByte() != 0.toByte()
        data.progress = parcel.readInt()
        data.deadline = parcel.readValue(Long::class.java.classLoader) as Long?
        data.recurrencePattern = RecurrencePattern.fromOrdinal(parcel.readInt())!!
        data.reminderTime = parcel.readValue(Long::class.java.classLoader) as Long?
        reminderTimeChanged = parcel.readByte() != 0.toByte()
        reminderTimeWasInitialized = parcel.readByte() != 0.toByte()
        data.sortOrder = parcel.readInt()
        data.priority = Priority.fromOrdinal(parcel.readInt())!!
        parcel.readTypedList(subtasks, TodoSubtaskImpl.CREATOR)
        // The duplicated object shall not duplicate the RequiredDBAction. The original object shall
        // ensure that DB action gets done. So keep initial value RequiredDBAction.NONE.
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(data.id)
        dest.writeValue(data.listId)
        dest.writeString(data.name)
        dest.writeString(data.description)
        dest.writeLong(data.creationTime)
        dest.writeValue(data.doneTime)
        dest.writeByte((if (data.isInRecycleBin) 1 else 0).toByte())
        dest.writeInt(data.progress)
        dest.writeValue(data.deadline)
        dest.writeInt(data.recurrencePattern.ordinal)
        dest.writeValue(data.reminderTime)
        dest.writeByte((if (reminderTimeChanged) 1 else 0).toByte())
        dest.writeByte((if (reminderTimeWasInitialized) 1 else 0).toByte())
        dest.writeInt(data.sortOrder)
        dest.writeInt(data.priority.ordinal)
        dest.writeTypedList(subtasks)
    }

    override fun setId(id: Int) {
        data.id = id
    }

    override fun getId(): Int {
        return data.id
    }

    override fun setCreationTime(creationTime: Long) {
        data.creationTime = creationTime
    }

    override fun getCreationTime(): Long {
        return data.creationTime
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

    override fun setDeadline(deadline: Long?) {
        data.deadline = deadline
    }

    override fun getDeadline(): Long? {
        return data.deadline
    }

    override fun hasDeadline(): Boolean {
        return data.deadline != null
    }

    override fun setRecurrencePattern(recurrencePattern: RecurrencePattern) {
        data.recurrencePattern = recurrencePattern
    }

    override fun getRecurrencePattern(): RecurrencePattern {
        return data.recurrencePattern
    }

    override fun isRecurring(): Boolean {
        return data.recurrencePattern != RecurrencePattern.NONE
    }

    override fun setSortOrder(sortOrder: Int) {
        data.sortOrder = sortOrder
    }

    override fun getSortOrder(): Int {
        return data.sortOrder
    }

    override fun setSubtasks(subtasks: MutableList<TodoSubtask>) {
        this.subtasks = subtasks
    }

    override fun getSubtasks(): MutableList<TodoSubtask> {
        return subtasks
    }

    override fun getDeadlineColor(reminderTimeSpan: Long): DeadlineColors {
        var color = DeadlineColors.BLUE
        var deadline = data.deadline
        if (!isDone() && deadline != null) {
            val now = Helper.getCurrentTimestamp()
            val finalReminderTimeSpan: Long
            if (isRecurring()) {
                deadline = Helper.getNextRecurringDate(deadline, data.recurrencePattern, now)
                finalReminderTimeSpan = reminderTimeSpan
            } else {
                val reminderTime = data.reminderTime
                finalReminderTimeSpan = if (reminderTime != null && reminderTime < deadline)
                    deadline - reminderTime else reminderTimeSpan
            }

            if (deadline <= now) {
                color = DeadlineColors.RED
            } else if ((deadline - finalReminderTimeSpan) <= now) {
                color = DeadlineColors.ORANGE
            }
        }
        return color
    }

    override fun setPriority(priority: Priority) {
        data.priority = priority
    }

    override fun getPriority(): Priority {
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

    override fun setReminderTime(reminderTime: Long?) {
        data.reminderTime = reminderTime

        // check if reminder time was already set and now changed -> important for reminder service
        if (reminderTimeWasInitialized) {
            reminderTimeChanged = true
        }
        reminderTimeWasInitialized = true
    }

    override fun getReminderTime(): Long? {
        return data.reminderTime
    }

    override fun hasReminderTime(): Boolean {
        return data.reminderTime != null
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

    // A task is done if the user manually sets it done or when all subtasks are done.
    // If a subtask is selected "done", the entire task might be "done" if by now all subtasks are done.
    override fun doneStatusChanged(): Boolean {
        var allSubtasksAreDone = true
        for (subtask in subtasks) {
            if (!subtask.isDone()) {
                allSubtasksAreDone = false
                break
            }
        }
        val doneStatusChanged = isDone() != allSubtasksAreDone
        if (doneStatusChanged) {
            setDone(allSubtasksAreDone)
            requiredDBAction = RequiredDBAction.UPDATE
        }
        return doneStatusChanged
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
        return "TodoTask(name=${getName()}, id=${getId()}, r=${getRecurrencePattern()})"
    }

    companion object {
        @JvmField
        val CREATOR = object : Creator<TodoTask> {
            override fun createFromParcel(parcel: Parcel): TodoTask {
                return TodoTaskImpl(parcel)
            }

            override fun newArray(size: Int): Array<TodoTask?> {
                return arrayOfNulls(size)
            }
        }
    }
}
