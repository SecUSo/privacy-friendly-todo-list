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
package org.secuso.privacyfriendlytodolist.view.dialog

import android.os.Bundle
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.Model.createNewTodoTask
import org.secuso.privacyfriendlytodolist.model.TodoList
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.util.Helper.createDateString
import org.secuso.privacyfriendlytodolist.util.Helper.createDateTimeString
import org.secuso.privacyfriendlytodolist.util.Helper.getCurrentTimestamp
import org.secuso.privacyfriendlytodolist.util.Helper.priority2String
import org.secuso.privacyfriendlytodolist.util.PreferenceMgr
import org.secuso.privacyfriendlytodolist.view.dialog.DeadlineDialog.DeadlineCallback
import org.secuso.privacyfriendlytodolist.view.dialog.ReminderDialog.ReminderCallback

/**
 * This class creates a dialog that lets the user create/edit a task.
 *
 * Created by Sebastian Lutz on 12.03.2018.
 */
@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class ProcessTodoTaskDialog : FullScreenDialog<ResultCallback<TodoTask>> {

    private val lists: List<TodoList>
    private var task: TodoTask
    private val changeExistingTask: Boolean
    private var taskPriority: TodoTask.Priority
    private lateinit var prioritySelector: TextView
    private lateinit var deadlineTextView: TextView
    private lateinit var reminderTextView: TextView
    private lateinit var listSelector: TextView
    private lateinit var dialogTitleNew: TextView
    private lateinit var dialogTitleEdit: TextView
    private lateinit var progressText: TextView
    private lateinit var progressPercent: TextView
    private lateinit var progressLayout: RelativeLayout
    private lateinit var progressSelector: SeekBar
    private lateinit var taskName: EditText
    private lateinit var taskDescription: EditText
    private var listSelectorText: String? = null
    private var isTitleEdit = false
    private var selectedListID: Int? = null
    private var taskProgress = 0
    private var deadline: Long = -1
    private var reminderTime: Long = -1

    constructor(context: FragmentActivity, todoLists: List<TodoList>) :
            super(context, R.layout.add_task_dialog) {
        lists = todoLists
        task = createNewTodoTask()
        changeExistingTask = false
        taskPriority = task.getPriority()
    }

    constructor(context: FragmentActivity, todoLists: List<TodoList>, todoTask: TodoTask) :
            super(context, R.layout.add_task_dialog) {
        lists = todoLists
        task = todoTask
        task.setChanged()
        changeExistingTask = true
        taskPriority = task.getPriority()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initGui()
        if (changeExistingTask) {
            deadline = task.getDeadline()
            reminderTime = task.getReminderTime()
            taskName.setText(task.getName())
            taskDescription.setText(task.getDescription())
            prioritySelector.text = priority2String(context, task.getPriority())
            progressSelector.progress = task.getProgress(false)
            deadlineTextView.text = if (task.getDeadline() <= 0)
                context.getString(R.string.no_deadline) else createDateString(deadline)
            reminderTextView.text = if (task.getReminderTime() <= 0)
                context.getString(R.string.reminder) else createDateTimeString(reminderTime)

        }
    }

    private fun initGui() {
        taskName = findViewById(R.id.et_new_task_name)
        taskDescription = findViewById(R.id.et_new_task_description)

        // Request focus for first input field.
        taskName.requestFocus()
        // Show soft-keyboard
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        // initialize textview that displays the selected priority
        prioritySelector = findViewById(R.id.tv_new_task_priority)
        prioritySelector.setOnClickListener {
            registerForContextMenu(prioritySelector)
            openContextMenu(prioritySelector)
        }
        prioritySelector.setOnCreateContextMenuListener(this)
        taskPriority = TodoTask.Priority.DEFAULT_VALUE
        prioritySelector.text = priority2String(context, taskPriority)

        //initialize titles of the dialog
        dialogTitleNew = findViewById(R.id.dialog_title)
        dialogTitleEdit = findViewById(R.id.dialog_edit)
        if (isTitleEdit) {
            titleEdit()
        }

        //initialize textview that displays selected list
        listSelector = findViewById(R.id.tv_new_task_listchoose)
        if (null != listSelectorText) {
            listSelector.text = listSelectorText
        }
        listSelector.setOnClickListener { v: View? ->
            registerForContextMenu(listSelector)
            openContextMenu(listSelector)
        }
        listSelector.setOnCreateContextMenuListener(this)
        progressText = findViewById(R.id.tv_task_progress)
        progressPercent = findViewById(R.id.new_task_progress)
        progressLayout = findViewById(R.id.progress_relative)

        // initialize seekbar that allows to select the progress
        progressSelector = findViewById(R.id.sb_new_task_progress)
        if (hasAutoProgress()) {
            progressLayout.visibility = View.GONE
        } else {
            progressSelector.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    taskProgress = progress
                    val text = "$progress %"
                    progressPercent.text = text
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
        }
        
        // initialize buttons
        val okayButton: Button = findViewById(R.id.bt_new_task_ok)
        okayButton.setOnClickListener { v: View? ->
            val name: String = taskName.getText().toString()
            val description: String = taskDescription.getText().toString()
            if (name.isEmpty()) {
                Toast.makeText(context, context.getString(R.string.todo_name_must_not_be_empty),
                    Toast.LENGTH_SHORT).show()
            } else {
                task.setName(name)
                task.setDescription(description)
                task.setDeadline(deadline)
                task.setPriority(taskPriority)
                task.setListId(selectedListID)
                task.setProgress(taskProgress)
                task.setReminderTime(reminderTime)
                getDialogCallback().onFinish(task)
                dismiss()
            }
        }
        val cancelButton: Button = findViewById(R.id.bt_new_task_cancel)
        cancelButton.setOnClickListener { dismiss() }

        // initialize text-views to get deadline and reminder time
        deadlineTextView = findViewById(R.id.tv_todo_list_deadline)
        deadlineTextView.setTextColor(okayButton.currentTextColor)
        deadlineTextView.setOnClickListener {
            val deadlineDialog = DeadlineDialog(context, deadline)
            deadlineDialog.setDialogCallback(object : DeadlineCallback {
                override fun setDeadline(deadline: Long) {
                    this@ProcessTodoTaskDialog.deadline = deadline
                    deadlineTextView.text = createDateString(deadline)
                }

                override fun removeDeadline() {
                    deadline = -1
                    deadlineTextView.text = context.resources.getString(R.string.deadline)
                }
            })
            deadlineDialog.show()
        }
        reminderTextView = findViewById(R.id.tv_todo_list_reminder)
        reminderTextView.setTextColor(okayButton.currentTextColor)
        reminderTextView.setOnClickListener {
            val reminderDialog = ReminderDialog(context, reminderTime, deadline)
            reminderDialog.setDialogCallback(object : ReminderCallback {
                override fun setReminder(reminderDeadline: Long) {
                    /* if (deadline == -1L) {
                        Toast.makeText(context, context.getString(R.string.set_deadline_before_reminder),
                            Toast.LENGTH_SHORT).show()
                    } else */
                    if (deadline != -1L && deadline < reminderDeadline) {
                        Toast.makeText(context, context.getString(R.string.deadline_smaller_reminder),
                            Toast.LENGTH_SHORT).show()
                    } else if (reminderDeadline < getCurrentTimestamp()) {
                        Toast.makeText(context, context.getString(R.string.reminder_smaller_now),
                            Toast.LENGTH_SHORT).show()
                    } else {
                        reminderTime = reminderDeadline
                        reminderTextView.text = createDateTimeString(reminderTime)
                    }
                }

                override fun removeReminder() {
                    reminderTime = -1L
                    val reminderTextView: TextView = findViewById(R.id.tv_todo_list_reminder)
                    reminderTextView.text = context.resources.getString(R.string.reminder)
                }
            })
            reminderDialog.show()
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        when (v.id) {
            R.id.tv_new_task_priority -> {
                menu.setHeaderTitle(R.string.select_priority)
                for (priority in TodoTask.Priority.entries) {
                    menu.add(Menu.NONE, priority.ordinal, Menu.NONE, priority2String(context, priority))
                }
            }

            R.id.tv_new_task_listchoose -> {
                menu.setHeaderTitle(R.string.select_list)
                menu.add(Menu.NONE, -1, Menu.NONE, R.string.select_no_list)
                var i = 0
                while (i < lists.size) {
                    val todoList = lists[i]
                    // Add offset so that IDs are non-overlapping with priority-IDs
                    menu.add(Menu.NONE, TodoTask.Priority.LENGTH + i, Menu.NONE, todoList.getName())
                    ++i
                }
            }
        }
    }

    override fun onMenuItemSelected(featureId: Int, item: MenuItem): Boolean {
        if (item.itemId >= 0 && item.itemId < TodoTask.Priority.LENGTH) {
            taskPriority = TodoTask.Priority.entries[item.itemId]
            prioritySelector.text = priority2String(context, taskPriority)
        } else {
            val todoListIndex = item.itemId - TodoTask.Priority.LENGTH
            var todoList: TodoList? = null
            if (todoListIndex >= 0 && todoListIndex < lists.size) {
                todoList = lists[todoListIndex]
            }
            setListSelector(todoList)
        }
        return super.onMenuItemSelected(featureId, item)
    }

    //change the dialog title from "new task" to "edit task"
    fun titleEdit() {
        if (::dialogTitleNew.isInitialized) {
            dialogTitleNew.visibility = View.GONE
            dialogTitleEdit.visibility = View.VISIBLE
        } else {
            isTitleEdit = true
        }
    }

    //sets the textview either to list name in context or if no context to default
    fun setListSelector(todoListId: Int?) {
        var todoList: TodoList? = null
        if (null != todoListId) {
            for (currentTodoList: TodoList in lists) {
                if (currentTodoList.getId() == todoListId) {
                    todoList = currentTodoList
                    break
                }
            }
        }
        setListSelector(todoList)
    }

    private fun setListSelector(todoList: TodoList?) {
        if (null != todoList) {
            selectedListID = todoList.getId()
            listSelectorText = todoList.getName()
        } else {
            selectedListID = null
            listSelectorText = context.getString(R.string.click_to_choose)
        }
        if (::listSelector.isInitialized) {
            listSelector.text = listSelectorText
        }
    }

    private fun hasAutoProgress(): Boolean {
        //automatic-progress enabled?
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(PreferenceMgr.P_IS_AUTO_PROGRESS.name, false)
    }
}
