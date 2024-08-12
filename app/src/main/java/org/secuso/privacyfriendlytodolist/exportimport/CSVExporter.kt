package org.secuso.privacyfriendlytodolist.exportimport

import org.secuso.privacyfriendlytodolist.model.TodoList
import org.secuso.privacyfriendlytodolist.model.TodoSubtask
import org.secuso.privacyfriendlytodolist.model.TodoTask
import java.io.OutputStream

class CSVExporter {
    private val csvBuilder = CSVBuilder()
    private var hasAutoProgress = false

    fun export(todoLists:  List<TodoList>, todoTasks: List<TodoTask>, hasAutoProgress: Boolean, outputStream: OutputStream) {
        this.hasAutoProgress = hasAutoProgress
        csvBuilder.reset()
        createHeading()

        for (todoTask in todoTasks) {
            val todoList = todoLists.find { list ->
                list.getId() == todoTask.getListId()
            }
            if (todoTask.getSubtasks().isEmpty()) {
                createRow(todoList, todoTask)
            } else {
                for (todoSubtask in todoTask.getSubtasks()) {
                    createRow(todoList, todoTask, todoSubtask)
                }
            }
        }

        for (todoList in todoLists) {
            if (todoList.getTasks().isEmpty()) {
                createRow(todoList)
            }
        }

        val csv = csvBuilder.getCSV()
        outputStream.use { os ->
            os.bufferedWriter().use { bw ->
                bw.write(csv)
            }
        }
    }

    private fun createHeading() {
        csvBuilder.addField("ListId")
        csvBuilder.addField("ListName")

        csvBuilder.addField("TaskId")
        csvBuilder.addField("TaskName")
        csvBuilder.addField("TaskCreationTime")
        csvBuilder.addField("TaskDoneTime")
        csvBuilder.addField("TaskIsDone")
        csvBuilder.addField("TaskListPosition")
        csvBuilder.addField("TaskDescription")
        csvBuilder.addField("TaskDeadline")
        csvBuilder.addField("TaskReminderTime")
        csvBuilder.addField("TaskRecurrencePattern")
        csvBuilder.addField("TaskProgress")
        csvBuilder.addField("TaskPriority")

        csvBuilder.addField("SubtaskId")
        csvBuilder.addField("SubtaskName")
        csvBuilder.addField("SubtaskDoneTime")
        csvBuilder.addField("SubtaskIsDone")

        csvBuilder.startNewRow()
    }

    private fun createRow(todoList: TodoList? = null, todoTask: TodoTask? = null, todoSubtask: TodoSubtask? = null) {
        if (null != todoList) {
            csvBuilder.addField(todoList.getId())
            csvBuilder.addField(todoList.getName())
        } else {
            for (i in 1..2) {
                csvBuilder.addEmptyField()
            }
        }

        if (null != todoTask) {
            csvBuilder.addField(todoTask.getId())
            csvBuilder.addField(todoTask.getName())
            csvBuilder.addTimeField(todoTask.getCreationTime())
            csvBuilder.addTimeField(todoTask.getDoneTime())
            csvBuilder.addField(todoTask.isDone().toString())
            csvBuilder.addField(todoTask.getListPosition())
            csvBuilder.addField(todoTask.getDescription())
            csvBuilder.addTimeField(todoTask.getDeadline())
            csvBuilder.addTimeField(todoTask.getReminderTime())
            csvBuilder.addField(todoTask.getRecurrencePattern().toString())
            csvBuilder.addField(todoTask.getProgress(hasAutoProgress))
            csvBuilder.addField(todoTask.getPriority().toString())
        } else {
            for (i in 1..12) {
                csvBuilder.addEmptyField()
            }
        }


        if (null != todoSubtask) {
            csvBuilder.addField(todoSubtask.getId())
            csvBuilder.addField(todoSubtask.getName())
            csvBuilder.addTimeField(todoSubtask.getDoneTime())
            csvBuilder.addField(todoSubtask.isDone().toString())
        } else {
            for (i in 1..4) {
                csvBuilder.addEmptyField()
            }
        }


        csvBuilder.startNewRow()
    }
}
