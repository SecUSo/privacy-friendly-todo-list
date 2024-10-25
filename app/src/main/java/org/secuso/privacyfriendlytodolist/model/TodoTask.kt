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

    enum class DeadlineColors {
        BLUE,
        ORANGE,
        RED
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
    fun isRecurring(): Boolean
    fun setSortOrder(sortOrder: Int)
    fun getSortOrder(): Int
    fun setSubtasks(subtasks: MutableList<TodoSubtask>)
    fun getSubtasks(): MutableList<TodoSubtask>

    /**
     * @param reminderTimeSpan The reminder time span is a relative value in seconds (e.g. 86400 s == 1 day).
     */
    fun getDeadlineColor(reminderTimeSpan: Long): DeadlineColors
    fun setPriority(priority: Priority)
    fun getPriority(): Priority
    fun setProgress(progress: Int)

    /**
     *
     * @param computeProgress If true, the progress of the task gets computed depending on the
     * subtasks done-state. This progress also gets stored so that a further
     * call with 'false' will return the same value (until next computation
     * or [.setProgress] gets called).
     * If false, the last stored value gets returned.
     * @return The progress of the task in percent (values in range 0 - 100).
     */
    fun getProgress(computeProgress: Boolean): Int
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
     * A task is done if the user manually sets it done or when all subtasks are done.
     * If a subtask is selected "done", the entire task might be "done" if by now all subtasks are done.
     * This method checks if done-status of task needs an update due to this and if so, does the update.
     * @return true if the done-status was updated, otherwise false.
     */
    fun doneStatusChanged(): Boolean
    fun setInRecycleBin(isInRecycleBin: Boolean)
    fun isInRecycleBin(): Boolean
    fun checkQueryMatch(query: String?, recursive: Boolean): Boolean
    fun checkQueryMatch(query: String?): Boolean
}
