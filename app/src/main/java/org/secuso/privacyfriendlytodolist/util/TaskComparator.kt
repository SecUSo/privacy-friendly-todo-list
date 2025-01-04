/*
Privacy Friendly To-Do List
Copyright (C) 2024  Christian Adams

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
package org.secuso.privacyfriendlytodolist.util

import org.secuso.privacyfriendlytodolist.model.TodoTask

class TaskComparator(var isGroupingByPriority: Boolean = false,
                     var isSortingByDeadline: Boolean = false,
                     var isSortingByNameAsc: Boolean = false): Comparator<TodoTask> {
    /**
     * Compares its two arguments for order.
     *
     * @param t1 the first to-do task to be compared.
     * @param t2 the second to-do task to be compared.
     * @return A negative integer, zero, or a positive integer as the
     * first argument is less than, equal to, or greater than the second.
     * @throws NullPointerException if an argument is null and this
     * comparator does not permit null arguments
     * @throws ClassCastException if the arguments' types prevent them from
     * being compared by this comparator.
     */
    override fun compare(t1: TodoTask, t2: TodoTask): Int {
        var result = 0
        if (isGroupingByPriority) {
            result = t1.getPriority().compareTo(t2.getPriority())
        }
        if (isSortingByDeadline && result == 0) {
            result = Helper.compareDeadlines(t1, t2)
        }
        if (isSortingByNameAsc && result == 0) {
            // Ignore case at comparison. Otherwise all lowercase names would be at the end.
            result = t1.getName().compareTo(t2.getName(), true)
        }
        if (result == 0) {
            result = t1.getSortOrder().compareTo(t2.getSortOrder())
        }
        return result
    }
}