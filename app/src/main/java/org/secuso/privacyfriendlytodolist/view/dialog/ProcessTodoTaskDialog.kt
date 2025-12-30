/*
Privacy Friendly To-Do List
Copyright (C) 2018-2025  Sebastian Lutz

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
package org.secuso.privacyfriendlytodolist.view.dialog

import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.Model
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.model.TodoTask.RecurrencePattern
import org.secuso.privacyfriendlytodolist.util.Helper
import org.secuso.privacyfriendlytodolist.util.LogTag
import org.secuso.privacyfriendlytodolist.util.PreferenceMgr
import org.secuso.privacyfriendlytodolist.util.Timestamp
import org.secuso.privacyfriendlytodolist.viewmodel.CustomViewModel
import java.util.Locale

/**
 * This class creates a dialog that lets the user create/edit a task.
 *
 * Created by Sebastian Lutz on 12.03.2018.
 */
@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class ProcessTodoTaskDialog(context: FragmentActivity,
                            private var todoTask: TodoTask):
        FullScreenDialog<ResultCallback<TodoTask>>(context, R.layout.task_dialog) {

    private var deadline: Timestamp?
    private var recurrencePattern: RecurrencePattern
    private var recurrenceInterval: Int
    private var reminderTime: Timestamp?
    private var taskProgress: Int
    private var taskPriority: TodoTask.Priority
    private var assignedTodoListId: Int?

    private var todoLists: Map<Int, String> = mapOf()
    private var editExistingTask: Boolean = true

    // GUI elements
    private lateinit var recurrencePatternTextView: TextView
    private lateinit var recurrenceIntervalEditText: EditText
    private lateinit var progressPercent: TextView
    private lateinit var prioritySelector: TextView
    private lateinit var listSelector: TextView

    private enum class GroupId {
        TASK_PRIORITY, NO_TASK_LIST, TASK_LIST_CHOOSE
    }

    init {
        deadline = todoTask.getDeadline()
        recurrencePattern = todoTask.getRecurrencePattern()
        recurrenceInterval = todoTask.getRecurrenceInterval()
        reminderTime = todoTask.getReminderTime()
        taskProgress = todoTask.getProgress()
        taskPriority = todoTask.getPriority()
        assignedTodoListId = todoTask.getListId()
    }

    constructor(context: FragmentActivity,
                todoListId: Int?) : this(context, Model.createNewTodoTask()) {
        assignedTodoListId = todoListId
        editExistingTask = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Dialog title
        if (editExistingTask) {
            findViewById<Toolbar>(R.id.task_dialog_title).setTitle(R.string.edit_todo_task)
        }
        // Task name
        val taskName: EditText = findViewById(R.id.et_task_name)
        taskName.setText(todoTask.getName())
        // Task description
        val taskDescription: EditText = findViewById(R.id.et_task_description)
        taskDescription.setText(todoTask.getDescription())
        // Task deadline
        val deadlineTextView: TextView = findViewById(R.id.tv_todo_list_deadline)
        deadlineTextView.text = deadline?.createLocalizedDateString() ?: context.getString(R.string.deadline)
        deadlineTextView.setOnClickListener {
            val deadlineDialog = DeadlineDialog(context, deadline)
            deadlineDialog.setDialogCallback(object : DeadlineCallback {
                override fun setDeadline(selectedDeadline: Timestamp) {
                    deadline = selectedDeadline
                    deadlineTextView.text = selectedDeadline.createLocalizedDateString()
                }

                override fun removeDeadline() {
                    deadline = null
                    deadlineTextView.text = context.resources.getString(R.string.deadline)
                }
            })
            deadlineDialog.show()
        }
        // Task reminder
        val reminderTextView: TextView = findViewById(R.id.tv_todo_list_reminder)
        reminderTextView.text = reminderTime?.createLocalizedDateTimeString() ?: context.getString(R.string.reminder)
        reminderTextView.setOnClickListener {
            val reminderDialog = ReminderDialog(context, reminderTime, deadline)
            reminderDialog.setDialogCallback(object : ReminderCallback {
                override fun setReminderTime(selectedReminderTime: Timestamp) {
                    var resIdErrorMsg = 0
                    val deadlineCopy = deadline
                    if (recurrencePattern == RecurrencePattern.NONE) {
                        /* if (deadline == null) {
                            resIdErrorMsg = R.string.set_deadline_before_reminder
                        } else */
                        if (deadlineCopy != null && deadlineCopy < selectedReminderTime) {
                            resIdErrorMsg = R.string.deadline_smaller_reminder
                        } else if (selectedReminderTime < Timestamp.createCurrent()) {
                            resIdErrorMsg = R.string.reminder_smaller_now
                        }
                    }
                    if (resIdErrorMsg == 0) {
                        reminderTime = selectedReminderTime
                        reminderTextView.text = selectedReminderTime.createLocalizedDateTimeString()
                    } else {
                        Toast.makeText(context, context.getString(resIdErrorMsg), Toast.LENGTH_SHORT).show()
                    }
                }

                override fun removeReminderTime() {
                    reminderTime = null
                    reminderTextView.text = context.resources.getString(R.string.reminder)
                }
            })
            reminderDialog.show()
        }
        // Task recurrence pattern
        recurrencePatternTextView = findViewById(R.id.tv_task_recurrence_pattern)
        recurrencePatternTextView.setOnClickListener {
            val dialog = RecurrencePatternDialog(context, recurrencePattern)
            dialog.setDialogCallback { newRecurrencePattern ->
                recurrencePattern = newRecurrencePattern
                updateRecurrencePatternText()
            }
            dialog.show()
        }
        recurrencePatternTextView.setOnCreateContextMenuListener(this)
        recurrenceIntervalEditText = findViewById(R.id.tv_task_recurrence_interval)
        updateRecurrencePatternText()
        // Task progress
        val progressSelector: SeekBar = findViewById(R.id.sb_task_progress)
        progressPercent = findViewById(R.id.tv_task_progress)
        progressSelector.progress = taskProgress
        updateProgressPercentText()
        if (hasAutoProgress()) {
            findViewById<TextView>(R.id.tv_task_progress_str).visibility = View.GONE
            progressSelector.visibility = View.GONE
            progressPercent.visibility = View.GONE
        } else {
            progressSelector.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    taskProgress = progress
                    updateProgressPercentText()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}

                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
        }
        // Task priority
        prioritySelector = findViewById(R.id.tv_task_priority)
        prioritySelector.text = Helper.priorityToString(context, taskPriority)
        prioritySelector.setOnCreateContextMenuListener(this)
        prioritySelector.setOnClickListener {
            registerForContextMenu(prioritySelector)
            openContextMenu(prioritySelector)
        }
        // Task list
        listSelector = findViewById(R.id.tv_task_list_choose)
        listSelector.setOnClickListener {
            registerForContextMenu(listSelector)
            openContextMenu(listSelector)
        }
        listSelector.setOnCreateContextMenuListener(this)
        // OK button
        val okayButton: Button = findViewById(R.id.bt_process_task_ok)
        okayButton.setOnClickListener {
            val name = taskName.text.toString()
            val description = taskDescription.text.toString()
            val recurrenceIntervalText = recurrenceIntervalEditText.text.toString()
            val recurrenceInterval = recurrenceIntervalText.toIntOrNull()
            val isRecurrenceIntervalBad = recurrenceInterval == null || recurrenceInterval < 1
            if (name.isEmpty()) {
                Toast.makeText(context, context.getString(R.string.todo_name_must_not_be_empty),
                    Toast.LENGTH_SHORT).show()
            } else if (recurrencePattern != RecurrencePattern.NONE && deadline == null) {
                Toast.makeText(context, context.getString(R.string.set_deadline_if_recurring),
                    Toast.LENGTH_SHORT).show()
            } else if (recurrencePattern != RecurrencePattern.NONE && isRecurrenceIntervalBad) {
                Toast.makeText(context, context.getString(R.string.recurrence_interval_invalid),
                    Toast.LENGTH_SHORT).show()
            } else {
                todoTask.setName(name)
                todoTask.setDescription(description)
                todoTask.setDeadline(deadline)
                todoTask.setRecurrencePattern(recurrencePattern)
                if (recurrencePattern in RecurrencePattern.WEEKDAYS_M______ .. RecurrencePattern.WEEKDAYS_MTWTFSS) {
                    // For these pattern an interval of 1 gets forced.
                    todoTask.setRecurrenceInterval(1)
                } else if ( ! isRecurrenceIntervalBad) {
                    todoTask.setRecurrenceInterval(recurrenceInterval)
                }
                todoTask.setReminderTime(reminderTime)
                todoTask.setProgress(taskProgress)
                todoTask.setPriority(taskPriority)
                todoTask.setListId(assignedTodoListId)
                todoTask.setChanged()
                getDialogCallback().onFinish(todoTask)
                dismiss()
            }
        }
        // Cancel button
        val cancelButton: Button = findViewById(R.id.bt_process_task_cancel)
        cancelButton.setOnClickListener { dismiss() }

        val viewModel = CustomViewModel(context)
        val model = viewModel.model
        model.getAllToDoListNames { allToDoListNames ->
            todoLists = allToDoListNames
            updateListSelector()
        }

        if (!editExistingTask) {
            // Request focus for first input field.
            taskName.requestFocus()
            // Show soft-keyboard
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        when (v.id) {
            R.id.tv_task_priority -> {
                val menuHeader = Helper.getMenuHeader(layoutInflater, v, R.string.select_priority)
                menu.setHeaderView(menuHeader)
                for (priority in TodoTask.Priority.entries) {
                    menu.add(GroupId.TASK_PRIORITY.ordinal, priority.ordinal, Menu.NONE,
                        Helper.priorityToString(context, priority))
                }
            }

            R.id.tv_task_list_choose -> {
                val menuHeader = Helper.getMenuHeader(layoutInflater, v, R.string.select_list)
                menu.setHeaderView(menuHeader)
                menu.add(GroupId.NO_TASK_LIST.ordinal, Menu.NONE, Menu.NONE, R.string.select_no_list)
                for (entry in todoLists.entries) {
                    menu.add(GroupId.TASK_LIST_CHOOSE.ordinal, entry.key, Menu.NONE, entry.value)
                }
            }
        }
    }

    override fun onMenuItemSelected(featureId: Int, item: MenuItem): Boolean {
        when (item.groupId) {
            GroupId.TASK_PRIORITY.ordinal -> {
                taskPriority = TodoTask.Priority.fromOrdinal(item.itemId)!!
                prioritySelector.text = Helper.priorityToString(context, taskPriority)
            }

            GroupId.NO_TASK_LIST.ordinal -> {
                assignedTodoListId = null
                updateListSelector()
            }

            GroupId.TASK_LIST_CHOOSE.ordinal -> {
                assignedTodoListId = item.itemId
                updateListSelector()
            }

            else -> {
                Log.e(TAG, "Unhandled menu item group ID ${item.groupId}.")
            }
        }

        return super.onMenuItemSelected(featureId, item)
    }

    private fun hasAutoProgress(): Boolean {
        //automatic-progress enabled?
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(PreferenceMgr.P_IS_AUTO_PROGRESS.name, false)
    }

    private fun updateProgressPercentText() {
        val text = "$taskProgress %"
        progressPercent.text = text
    }

    private fun updateRecurrencePatternText() {
        if (recurrencePattern == RecurrencePattern.NONE) {
            recurrencePatternTextView.text = context.getString(R.string.recurrence_pattern_hint)
            recurrenceIntervalEditText.setText("")
        } else {
            recurrencePatternTextView.text = Helper.recurrencePatternToNounString(context, recurrencePattern)
            val forceInterval1 = (recurrencePattern in RecurrencePattern.WEEKDAYS_M______ .. RecurrencePattern.WEEKDAYS_MTWTFSS)
            if (forceInterval1) {
                recurrenceInterval = 1
            }
            if (forceInterval1 || recurrenceIntervalEditText.text.isEmpty()) {
                recurrenceIntervalEditText.setText(String.format(Locale.getDefault(), "%d", recurrenceInterval))
            }
        }
    }

    private fun updateListSelector() {
        var text: String? = null
        if (null != assignedTodoListId) {
            text = todoLists[assignedTodoListId]
        }
        if (null == text) {
            assignedTodoListId = null
            text = context.getString(R.string.click_to_choose)
        }
        listSelector.text = text
    }

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
    }
}
