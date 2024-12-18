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

import org.secuso.privacyfriendlytodolist.model.TodoList
import org.secuso.privacyfriendlytodolist.model.TodoTask
import java.io.Writer

class MarkdownBuilder(private val writer: Writer, private val deadlineString: String) {
    fun addList(todoList: TodoList) {
        writer.append("### ")
        writer.appendLine(todoList.getName())
        for (task in todoList.getTasks()) {
            addTask(task)
        }
    }

    fun addTask(todoTask: TodoTask) {
        writer.append("- [")
        writer.append(if (todoTask.isDone()) "x" else " ")
        writer.append("] ")
        val deadline = todoTask.getDeadline()
        if (deadline != null) {
            writer.append(deadlineString)
            writer.append(" ")
            writer.append(Helper.createLocalizedDateString(deadline))
            writer.append(": ")
        }
        writer.append(todoTask.getName())
        if (todoTask.getDescription().isNotEmpty()) {
            writer.append(" (")
            writer.append(todoTask.getDescription())
            writer.append(")")
        }
        writer.appendLine()

        for (subtask in todoTask.getSubtasks()) {
            writer.append("    - [")
            writer.append(if (subtask.isDone()) "x" else " ")
            writer.append("] ")
            writer.appendLine(subtask.getName())
        }
    }
}
