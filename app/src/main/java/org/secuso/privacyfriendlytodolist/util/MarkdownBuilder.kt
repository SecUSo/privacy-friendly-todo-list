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
        if (todoTask.hasDeadline()) {
            writer.append(deadlineString)
            writer.append(" ")
            writer.append(Helper.createLocalizedDateString(todoTask.getDeadline()))
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
