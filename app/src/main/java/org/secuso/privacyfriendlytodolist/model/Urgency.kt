/*
Privacy Friendly To-Do List
Copyright (C) 2025  Christian Adams

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

import android.content.Context
import androidx.core.content.ContextCompat
import org.secuso.privacyfriendlytodolist.R

class Urgency(val level: Level, val daysUntilDeadline: Long?): Comparable<Urgency> {

    /**
     * The urgency of a to-do task.
     *
     * The entries of this enumeration are sorted: From lowest urgency (first entry) to highest
     * urgency (last entry).
     */
    enum class Level(val colorId: Int) {
        /** The task is done. */
        NONE(R.color.urgencyNone),
        /** The task is not done and the task has no deadline or the deadline is far away. */
        LOW(R.color.urgencyLow),
        /** The task is not done and the deadline is near. */
        IMMINENT(R.color.urgencyImminent),
        /** The task is not done and the deadline is today. */
        DUE(R.color.urgencyDue),
        /** The task is not done and the deadline is in the past. */
        EXCEEDED(R.color.urgencyExceeded);
    }

    fun getColor(context: Context): Int {
        return ContextCompat.getColor(context, level.colorId)
    }

    override fun compareTo(other: Urgency): Int {
        var result = level.ordinal - other.level.ordinal
        if (result == 0 && daysUntilDeadline != null && other.daysUntilDeadline != null) {
            result = (other.daysUntilDeadline - daysUntilDeadline).toInt()
        }
        return result
    }

    override fun equals(other: Any?): Boolean {
        return other is Urgency && other.level == level && other.daysUntilDeadline == daysUntilDeadline
    }

    override fun hashCode(): Int {
        var result = daysUntilDeadline?.hashCode() ?: 0
        result = 31 * result + level.hashCode()
        return result
    }

    override fun toString(): String {
        return level.name
    }
}
