package org.secuso.privacyfriendlytodolist.model.impl

import org.secuso.privacyfriendlytodolist.model.Model
import org.secuso.privacyfriendlytodolist.model.TodoList
import org.secuso.privacyfriendlytodolist.model.TodoSubtask
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.model.TodoTask.Priority
import org.secuso.privacyfriendlytodolist.model.TodoTask.RecurrencePattern
import org.secuso.privacyfriendlytodolist.model.Tuple
import org.secuso.privacyfriendlytodolist.util.CSVBuilder
import org.secuso.privacyfriendlytodolist.util.CSVParser
import java.io.Reader
import java.util.concurrent.TimeUnit

/**
 * Imports To-Do lists, tasks and subtasks from comma separated values (CSV).
 * The same CSV format as created by CSVExporter is expected.
 */
@Suppress("SameParameterValue")
class CSVImporter {
    val lists = HashMap<Int, TodoList>()
    val tasks = HashMap<Int, Tuple<TodoList?, TodoTask>>()
    val subtasks = HashMap<Int, Tuple<TodoTask, TodoSubtask>>()
    private var rowNumber = 0
    private var columnIndex = 0

    fun import(reader: Reader) {
        lists.clear()
        tasks.clear()
        subtasks.clear()

        try {
            val csvParser = CSVParser()
            val rows = csvParser.parse(reader)
            parseRows(rows)
        } catch (e: Exception) {
            throw ParseException("CSV parsing failed at row $rowNumber, column ${columnIndex + 1}: ${e.message}", e)
        }
    }

    private fun parseRows(rows: List<List<String>>) {
        rowNumber = 1
        // First row gets skipped because it contains the column titles.
        for (index in 1..< rows.size) {
            val row = rows[index]
            ++rowNumber
            if (row.isEmpty()) {
                // Skip empty lines.
                continue
            }
            if (row.size != CSVExporter.COLUMN_COUNT) {
                throw IllegalFormatException("Row $rowNumber: Expected ${CSVExporter.COLUMN_COUNT} columns but found ${row.size}.")
            }
            val list = parseList(row)
            val task = parseTask(row, list)
            parseSubtask(row, task)
        }
    }

    private fun parseList(row: List<String>): TodoList? {
        var list: TodoList? = null
        val id = getId(row, LIST)
        if (null != id) {
            list = lists[id]
            // CSV file can contain duplicates of a list, one for each task and its subtasks.
            // Parse only first list, identified by its ID.
            // But return list in both cases, if parsed and if found in list.
            if (null == list) {
                list = Model.createNewTodoList()
                lists[id] = list
                // List ID is not set. It gets set while saving in DB.
                list.setName(getName(row, LIST))
            }
        }
        return list
    }

    private fun parseTask(row: List<String>, list: TodoList?): TodoTask? {
        var task: TodoTask? = null
        val id = getId(row, TASK)
        if (null != id) {
            task = tasks[id]?.right
            // CSV file can contain duplicates of a task, one for each subtasks.
            // Parse only first task, identified by its ID.
            // But return task in both cases, if parsed and if found in list.
            if (null == task) {
                task = Model.createNewTodoTask()
                tasks[id] = Tuple(list, task)
                if (null != list) {
                    task.setListId(list.getId())
                }
                // Task ID is not set. It gets set while saving in DB.
                task.setName(getName(row, TASK))
                val creationTime = getTimestamp(row, TASK, 2)
                if (null != creationTime) {
                    task.setCreationTime(creationTime)
                }
                task.setDoneTime(getTimestamp(row, TASK, 3))
                task.setListPosition(getInteger(row, TASK, 4))
                task.setDescription(getText(row, TASK, 5))
                task.setDeadline(getTimestamp(row, TASK, 6))
                task.setReminderTime(getTimestamp(row, TASK, 7))
                task.setRecurrencePattern(getEnumValue<RecurrencePattern>(row, TASK, 8, RecurrencePattern.NONE))
                task.setProgress(getProgress(row, TASK, 9))
                task.setPriority(getEnumValue<Priority>(row, TASK, 10, Priority.DEFAULT_VALUE))

                if (task.isRecurring() && task.getDeadline() == null) {
                    throw IllegalFormatException("Row $rowNumber: Task with ID $id is recurring but has no deadline.")
                }
            }
        }
        return task
    }

    private fun parseSubtask(row: List<String>, task: TodoTask?) {
        val id = getId(row, SUBTASK)
        if (null != id) {
            if (null == task) {
                throw IllegalFormatException("Row $rowNumber: Subtask with ID $id found but no task(-ID).")
            }
            if (null != subtasks[id]) {
                throw IllegalFormatException("Row $rowNumber: Subtask with ID $id occurs more than once.")
            }
            val subtask = Model.createNewTodoSubtask()
            subtasks[id] = Tuple(task, subtask)
            subtask.setTaskId(task.getId())
            // Subtask ID is not set. It gets set while saving in DB.
            subtask.setName(getName(row, SUBTASK))
            subtask.setDoneTime(getTimestamp(row, SUBTASK, 2))
        }
    }

    private fun getId(row: List<String>, offset: Int, index: Int = 0): Int? {
        columnIndex = offset + index
        val idString = row[columnIndex].trim()
        var id: Int? = null
        if (idString.isNotEmpty()) {
            id = idString.toInt()
        }
        return id
    }

    private fun getName(row: List<String>, offset: Int, index: Int = 1): String {
        columnIndex = offset + index
        val text = row[columnIndex].trim()
        if (text.isEmpty()) {
            throw IllegalFormatException("Row $rowNumber, column ${columnIndex + 1}: Name is empty.")
        }
        return text
    }

    private fun getText(row: List<String>, offset: Int, index: Int): String {
        columnIndex = offset + index
        return row[columnIndex].trim()
    }

    private fun getTimestamp(row: List<String>, offset: Int, index: Int): Long? {
        columnIndex = offset + index
        val text = row[columnIndex].trim()
        var result: Long? = null
        if (text.isNotEmpty()) {
            val date = CSVBuilder.DATE_TIME_FORMAT.parse(text)
            result = TimeUnit.MILLISECONDS.toSeconds(date!!.time)
        }
        return result
    }

    private fun getProgress(row: List<String>, offset: Int, index: Int): Int {
        columnIndex = offset + index
        val text = row[columnIndex].trim()
        var result = 0
        if (text.isNotEmpty()) {
            result = text.toInt()
            if (result < 0 || result > 100) {
                throw IllegalFormatException("Progress not in range 0..100.")
            }
        }
        return result
    }

    private fun getInteger(row: List<String>, offset: Int, index: Int): Int {
        columnIndex = offset + index
        val text = row[columnIndex].trim()
        return text.toInt()
    }

    private inline fun <reified T : Enum<T>> getEnumValue(row: List<String>,
                                                          offset: Int,
                                                          index: Int,
                                                          defaultValue: T): T {
        columnIndex = offset + index
        val text = row[columnIndex].trim()
        var result = defaultValue
        if (text.isNotEmpty()) {
            result = enumValues<T>().firstOrNull { it.name == text }
                ?: throw IllegalFormatException("Invalid ${result.declaringJavaClass.simpleName} value.")
        }
        return result
    }

    companion object {
        private const val LIST = 0
        private const val TASK = 2
        private const val SUBTASK = 13
    }
}
