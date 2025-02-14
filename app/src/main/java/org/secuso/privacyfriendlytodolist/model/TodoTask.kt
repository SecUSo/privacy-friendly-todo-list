/*
Privacy Friendly To-Do List
Copyright (C) 2016-2025  Dominik Puellen

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
package org.secuso.privacyfriendlytodolist.model

import android.os.Parcelable


interface TodoTask : BaseTodo, Parcelable {

    enum class RecurrencePattern {
        NONE,
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY;

        companion object {
            /** Number of enumeration entries. */
            @JvmField
            val LENGTH = entries.size

            /**
             * Provides the enumeration value that matches the given ordinal number.
             *
             * @param ordinal The ordinal number of the requested enumeration value.
             * @return The requested enumeration value if the given ordinal is valid. Otherwise null.
             */
            fun fromOrdinal(ordinal: Int): RecurrencePattern? {
                return if (ordinal in 0..<LENGTH) entries[ordinal] else null
            }
        }
    }

    /**
     * Priority steps must be sorted in the same way like they will be displayed
     */
    enum class Priority {
        HIGH,
        MEDIUM,
        LOW;

        companion object {
            @JvmField
            val DEFAULT_VALUE = MEDIUM

            /** Number of enumeration entries. */
            @JvmField
            val LENGTH = entries.size

            /**
             * Provides the enumeration value that matches the given ordinal number.
             *
             * @param ordinal The ordinal number of the requested enumeration value.
             * @return The requested enumeration value if the given ordinal is valid. Otherwise null.
             */
            fun fromOrdinal(ordinal: Int): Priority? {
                return if (ordinal in 0..<LENGTH) entries[ordinal] else null
            }
        }
    }

    fun setId(id: Int)
    fun getId(): Int
    fun setCreationTime(creationTime: Long)
    fun getCreationTime(): Long
    fun setName(name: String)
    fun getName(): String
    fun setDescription(description: String)
    fun getDescription(): String

    /**
     * @param listId The ID of the associated list or null if no list is associated.
     */
    fun setListId(listId: Int?)

    /**
     * @return The ID of the associated list or null if no list is associated.
     */
    fun getListId(): Int?
    fun setDeadline(deadline: Long?)
    fun getDeadline(): Long?
    fun hasDeadline(): Boolean
    fun setRecurrencePattern(recurrencePattern: RecurrencePattern)
    fun getRecurrencePattern(): RecurrencePattern
    fun setRecurrenceInterval(recurrenceInterval: Int)
    fun getRecurrenceInterval(): Int
    fun isRecurring(): Boolean
    fun setSortOrder(sortOrder: Int)
    fun getSortOrder(): Int
    fun setSubtasks(subtasks: MutableList<TodoSubtask>)
    fun getSubtasks(): MutableList<TodoSubtask>

    /**
     * @param reminderTimeSpan The reminder time span is a relative value in seconds
     * (e.g. 86400 s == 1 day). This is the time span before the deadline elapses where
     * Urgency#IMMINENT gets returned.
     * @return Returns Urgency EXCEEDED if the deadline is in the past.
     * Returns Urgency DUE if the deadline is today.
     * Returns Urgency IMMINENT if the deadline is later than today but within reminder time span
     * (the given one or the one set by the user).
     * Returns Urgency NONE in any other case.
     */
    fun getUrgency(reminderTimeSpan: Long): Urgency
    fun setPriority(priority: Priority)
    fun getPriority(): Priority
    fun setProgress(progress: Int)
    /**
     * The progress of a task is the number of done subtasks compared to the overall number of subtasks.
     *
     * @return The progress of the task in percent (value in range 0 - 100).
     */
    fun getProgress(): Int
    /**
     * Computes the progress of the task depending on the subtasks done-state. This progress gets
     * stored and can be retrieved by [getProgress].
     *
     * @return True if a new progress value was computed, false if the same value was computed as
     * currently is stored.
     */
    fun computeProgress(): Boolean
    fun setReminderTime(reminderTime: Long?)
    fun getReminderTime(): Long?
    fun hasReminderTime(): Boolean
    fun reminderTimeChanged(): Boolean
    fun resetReminderTimeChangedStatus()
    fun setAllSubtasksDone(isDone: Boolean)
    fun setDone(isDone: Boolean)
    fun isDone(): Boolean
    fun setDoneTime(doneTime: Long?)
    fun getDoneTime(): Long?

    /**
     * This method updates the done-status of the task depending on the done-status of all subtasks:
     * - If all subtasks are done and the task is not set as done, it gets set as done.
     * - If not all subtasks are done and the task is set as done, it gets set as undone.
     * @return true if the done-status was updated, otherwise false.
     */
    fun updateDoneStatus(): Boolean
    fun setInRecycleBin(isInRecycleBin: Boolean)
    fun isInRecycleBin(): Boolean
    fun checkQueryMatch(query: String?, recursive: Boolean): Boolean
    fun checkQueryMatch(query: String?): Boolean
}
