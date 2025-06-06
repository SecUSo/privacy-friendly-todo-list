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
import java.util.concurrent.TimeUnit

class Urgency(val level: Level, val daysUntilDeadline: Long?) {

    /**
     * The urgency of a to-do task.
     *
     * The entries of this enumeration are sorted: From lowest urgency (first entry) to highest
     * urgency (last entry).
     */
    enum class Level(val colorId: Int) {
        /** Task has no deadline or the deadline is far away. */
        NONE(R.color.urgencyNone),
        /** The deadline is near. */
        IMMINENT(R.color.urgencyImminent),
        /** The deadline is now. */
        DUE(R.color.urgencyDue),
        /** The deadline has been exceeded. */
        EXCEEDED(R.color.urgencyExceeded);
    }

    fun getColor(context: Context): Int {
        return ContextCompat.getColor(context, level.colorId)
    }
}
