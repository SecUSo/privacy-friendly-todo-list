/*
Privacy Friendly To-Do List
Copyright (C) 2018-2024  Sebastian Lutz

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
package org.secuso.privacyfriendlytodolist.view

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.ModelServices
import org.secuso.privacyfriendlytodolist.model.TodoSubtask
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.util.Helper
import org.secuso.privacyfriendlytodolist.util.LogTag
import org.secuso.privacyfriendlytodolist.util.PreferenceMgr
import org.secuso.privacyfriendlytodolist.view.dialog.ProcessTodoSubtaskDialog

/**
 * Created by Sebastian Lutz on 06.03.2018
 *
 * This class manages the To-Do task expandable list items.
 *
 * @param todoTasks Data from database in original order
 * @param showListNames Normally the toolbar title contains the list name. However, if all tasks are
 * displayed in a dummy list it is not obvious to what list a tasks belongs. This missing
 * information is then added to each task in an additional text view.
 */
@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class ExpandableTodoTaskAdapter(private val context: Context, private val model: ModelServices,
    private val todoTasks: MutableList<TodoTask>, private val showListNames: Boolean) : BaseExpandableListAdapter() {
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun interface OnTaskMenuClickListener {
        fun onTaskMenuClicked(todoTask: TodoTask)
    }

    fun interface OnSubtaskMenuClickListener {
        fun onSubtaskMenuClicked(todoTask: TodoTask, todoSubtask: TodoSubtask)
    }

    fun interface OnTasksSwappedListener {
        fun onTasksSwapped(groupPositionA: Int, groupPositionB: Int)
    }

    private var onTaskMenuClickListener: OnTaskMenuClickListener? = null

    private var onSubtaskMenuClickListener: OnSubtaskMenuClickListener? = null

    private var onTasksSwappedListener: OnTasksSwappedListener? = null


    enum class GroupType {
        TASK_ROW,
        PRIORITY_ROW
    }

    enum class ChildType {
        TASK_DESCRIPTION_ROW,
        SETTING_ROW,
        SUBTASK_ROW
    }

    enum class TaskFilter {
        ALL_TASKS,
        COMPLETED_TASKS,
        OPEN_TASKS;

        companion object {
            fun fromString(filterString: String?): TaskFilter {
                return TaskFilter.entries.find { value ->
                    value.name == filterString
                } ?: ALL_TASKS
            }
        }
    }

    // FILTER AND SORTING OPTIONS MADE BY THE USER
    var queryString: String?
    var taskFilter: TaskFilter
    var isGroupingByPriority: Boolean
    var isSortingByDeadline: Boolean
    var isSortingByNameAsc: Boolean
    private val filteredTasks: MutableList<TaskHolder> = ArrayList() // data after filtering process
    private val priorityBarPositions = mutableMapOf<TodoTask.Priority, Int>()
    private var listNames = mapOf<Int, String>()

    init {
        val taskFilterString = prefs.getString(PreferenceMgr.P_TASK_FILTER.name, TaskFilter.ALL_TASKS.name)
        taskFilter = TaskFilter.fromString(taskFilterString)
        isGroupingByPriority = prefs.getBoolean(PreferenceMgr.P_GROUP_BY_PRIORITY.name, false)
        isSortingByDeadline = prefs.getBoolean(PreferenceMgr.P_SORT_BY_DEADLINE.name, false)
        isSortingByNameAsc = prefs.getBoolean(PreferenceMgr.P_SORT_BY_NAME_ASC.name, false)
        queryString = null
        notifyDataSetChanged()
    }

    fun setOnTaskMenuClickListener(onTaskMenuClickListener: OnTaskMenuClickListener?) {
        this.onTaskMenuClickListener = onTaskMenuClickListener
    }

    fun setOnSubtaskMenuClickListener(onSubtaskMenuClickListener: OnSubtaskMenuClickListener?) {
        this.onSubtaskMenuClickListener = onSubtaskMenuClickListener
    }

    fun onClickSubtask(groupPosition: Int, childPosition: Int) {
        val taskHolder = getTaskByPosition(groupPosition)
        var subtaskMetaData: SubtaskMetaData? = null
        if (null != taskHolder) {
            val index = childPosition - 1
            subtaskMetaData = taskHolder.getSubtaskMetaData(index)
            if (null != subtaskMetaData) {
                subtaskMetaData.toggleMoveButtonsVisibility()
                notifyDataSetChanged()
            }
        }
        if (null == subtaskMetaData) {
            Log.w(TAG, "Unable to get subtask by position $groupPosition, $childPosition.")
        }
    }

    fun setOnTasksSwappedListener(onTasksSwappedListener: OnTasksSwappedListener?) {
        this.onTasksSwappedListener = onTasksSwappedListener
    }

    /**
     * filter tasks by "done" criterion (show "all", only "open" or only "completed" tasks)
     * If the user changes the filter, it is crucial to call "sortTasks" again.
     */
    private fun filterTasks() {
        val newFilteredTasks: MutableList<TaskHolder> = ArrayList()
        val notOpen = taskFilter != TaskFilter.OPEN_TASKS
        val notCompleted = taskFilter != TaskFilter.COMPLETED_TASKS
        for (task in todoTasks) {
            if ((notOpen && task.isDone() || notCompleted && !task.isDone())
                && task.checkQueryMatch(queryString)) {
                // Try to reuse task-holder to keep meta data while sorting
                var taskHolder = filteredTasks.find { other ->
                    return@find other.todoTask == task
                }
                if (null == taskHolder) {
                    taskHolder = TaskHolder(task)
                }
                newFilteredTasks.add(taskHolder)
            }
        }
        filteredTasks.clear()
        filteredTasks.addAll(newFilteredTasks)

        // Call this method even if sorting is disabled. In the case of enabled sorting, all
        // sorting patterns are automatically employed after having changed the filter on tasks.
        sortTasks()
    }

    /**
     * Sort tasks by selected criteria (priority and/or deadline)
     * This method works on [ExpandableTodoTaskAdapter.filteredTasks]. For that reason it is
     * important to keep [ExpandableTodoTaskAdapter.filteredTasks] up-to-date.
     */
    private fun sortTasks() {
        filteredTasks.sortWith { taskHolder1, taskHolder2 ->
            val t1 = taskHolder1.todoTask
            val t2 = taskHolder2.todoTask
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
            result
        }
        if (isGroupingByPriority) {
            countTasksPerPriority()
        }
    }

    /**
     * Count how many tasks belong to each priority group (tasks are now sorted by priority).
     *
     * If [ExpandableTodoTaskAdapter.sortTasks] sorted by the priority, this method must be
     * called. It computes the position of the dividing bars between the priority ranges. These
     * positions are necessary to distinguish of what group type the current row is.
     */
    private fun countTasksPerPriority() {
        priorityBarPositions.clear()
        if (filteredTasks.size != 0) {
            var pos = 0
            var currentPriority: TodoTask.Priority
            val priorityAlreadySeen = HashSet<TodoTask.Priority>()
            for (taskHolder in filteredTasks) {
                currentPriority = taskHolder.todoTask.getPriority()
                if (!priorityAlreadySeen.contains(currentPriority)) {
                    priorityAlreadySeen.add(currentPriority)
                    priorityBarPositions[currentPriority] = pos
                    ++pos // skip the current priority-line
                }
                ++pos
            }
        }
    }

    /**
     * @param groupPosition position of current row. For that reason the offset to the task must be
     * computed taking into account all preceding dividing priority bars
     * @return null if there is no task at @param groupPosition (but a divider row) or the wanted task
     */
    private fun getTaskByPosition(groupPosition: Int): TaskHolder? {
        var seenPriorityBars = 0
        if (isGroupingByPriority) {
            for (priority in TodoTask.Priority.entries) {
                val priorityPos = priorityBarPositions[priority]
                if (null != priorityPos) {
                    if (groupPosition < priorityPos) {
                        break
                    }
                    ++seenPriorityBars
                }
            }
        }
        val taskIndex = groupPosition - seenPriorityBars
        if (taskIndex >= 0 && taskIndex < filteredTasks.size) {
            return filteredTasks[taskIndex]
        }
        Log.w(TAG, "Unable to get task by group position $groupPosition")
        return null // should never be the case
    }

    private fun getPositionByTask(taskIndex: Int): Int {
        var groupPosition = taskIndex
        if (isGroupingByPriority) {
            val sortedPriorityBarPositions = priorityBarPositions.values.sorted()
            for (priorityBarPosition in sortedPriorityBarPositions) {
                if (priorityBarPosition <= groupPosition) {
                    ++groupPosition
                }
            }
        }
        return groupPosition
    }

    override fun getGroupCount(): Int {
        return if (isGroupingByPriority) filteredTasks.size + priorityBarPositions.size else filteredTasks.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        var count = ChildType.entries.size - 1
        val todoTask = getTaskByPosition(groupPosition)?.todoTask
        if (null != todoTask) {
            count += todoTask.getSubtasks().size
        }
        return count
    }

    override fun getGroupTypeCount(): Int {
        return GroupType.entries.size
    }

    /**
     * @param groupPosition Position of group in range 0 - number of groups minus one.
     */
    private fun getGroupTypeEnum(groupPosition: Int): GroupType {
        return if (isGroupingByPriority && priorityBarPositions.values.contains(groupPosition)) {
            GroupType.PRIORITY_ROW
        } else {
            GroupType.TASK_ROW
        }
    }

    override fun getGroupType(groupPosition: Int): Int {
        return getGroupTypeEnum(groupPosition).ordinal
    }

    override fun getChildTypeCount(): Int {
        return ChildType.entries.size
    }

    /**
     * @param groupPosition Position of group in range 0 - number of groups minus one.
     * @param childPosition Position of child in range 0 - number of children minus one.
     */
    private fun getChildTypeEnum(groupPosition: Int, childPosition: Int): ChildType {
        if (childPosition == 0) {
            return ChildType.TASK_DESCRIPTION_ROW
        }
        val todoTask = getTaskByPosition(groupPosition)?.todoTask
        return if (null != todoTask && childPosition <= todoTask.getSubtasks().size) {
            ChildType.SUBTASK_ROW
        } else {
            ChildType.SETTING_ROW
        }
    }

    override fun getChildType(groupPosition: Int, childPosition: Int): Int {
        return getChildTypeEnum(groupPosition, childPosition).ordinal
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getGroup(groupPosition: Int): Any {
        return filteredTasks[groupPosition]
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return childPosition
    }

    private fun getPriorityNameByBarPos(groupPosition: Int): String {
        var priority: TodoTask.Priority? = null
        for ((key, value) in priorityBarPositions) {
            if (value == groupPosition) {
                priority = key
                break
            }
        }
        return Helper.priorityToString(context, priority)
    }

    override fun notifyDataSetChanged() {
        if (showListNames) {
            model.getAllToDoListNames { todoListNames ->
                listNames = todoListNames
                filterTasks()
                super.notifyDataSetChanged()
            }
        } else {
            filterTasks()
            super.notifyDataSetChanged()
        }
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup): View? {
        var actualConvertView = convertView
        val groupType = getGroupTypeEnum(groupPosition)
        when (groupType) {
            GroupType.TASK_ROW -> {
                val currentTaskHolder = getTaskByPosition(groupPosition) ?: return actualConvertView
                val currentTask = currentTaskHolder.todoTask
                val tvh: GroupTaskViewHolder
                if (actualConvertView?.tag is GroupTaskViewHolder) {
                    tvh = actualConvertView.tag as GroupTaskViewHolder
                } else {
                    actualConvertView = LayoutInflater.from(context)
                        .inflate(R.layout.exlv_tasks_group, parent, false)
                    tvh = GroupTaskViewHolder(
                        actualConvertView.findViewById(R.id.tv_exlv_task_name),
                        actualConvertView.findViewById(R.id.bt_task_move_up),
                        actualConvertView.findViewById(R.id.bt_task_move_down),
                        actualConvertView.findViewById(R.id.bt_task_menu),
                        actualConvertView.findViewById(R.id.iv_exlv_task_deadline),
                        actualConvertView.findViewById(R.id.tv_exlv_task_deadline),
                        actualConvertView.findViewById(R.id.iv_exlv_task_reminder),
                        actualConvertView.findViewById(R.id.tv_exlv_task_reminder),
                        actualConvertView.findViewById(R.id.tv_exlv_task_list_name),
                        actualConvertView.findViewById(R.id.cb_task_done),
                        actualConvertView.findViewById(R.id.v_urgency_task),
                        actualConvertView.findViewById(R.id.pb_task_progress)
                    )
                    tvh.done.tag = currentTask.getId()
                    tvh.done.isChecked = currentTask.isDone()
                    tvh.done.jumpDrawablesToCurrentState()
                    actualConvertView.tag = tvh
                }
                tvh.name.text = currentTask.getName()
                tvh.moveUpButton.visibility = if (isExpanded) View.VISIBLE else View.GONE
                tvh.moveDownButton.visibility = tvh.moveUpButton.visibility
                tvh.moveUpButton.setOnClickListener {
                    moveTask(currentTaskHolder, groupPosition, true)
                }
                tvh.moveDownButton.setOnClickListener {
                    moveTask(currentTaskHolder, groupPosition, false)
                }
                tvh.taskMenuButton.setOnClickListener {
                    onTaskMenuClickListener?.onTaskMenuClicked(currentTask)
                }
                tvh.progressBar.progress = currentTask.getProgress(hasAutoProgress())
                tvh.listName.visibility = View.GONE
                if (showListNames && currentTask.getListId() != null) {
                    val listName = listNames[currentTask.getListId()]
                    if (null != listName) {
                        tvh.listName.text = listName
                        tvh.listName.visibility = View.VISIBLE
                    }
                }
                val deadline = currentTask.getDeadline()
                if (deadline != null) {
                    tvh.deadline.text = Helper.createLocalizedDateString(deadline)
                    tvh.deadlineIcon.visibility = View.VISIBLE
                    tvh.deadline.visibility = View.VISIBLE
                } else {
                    tvh.deadlineIcon.visibility = View.GONE
                    tvh.deadline.visibility = View.GONE
                }
                var reminderTime = currentTask.getReminderTime()
                if (isExpanded && reminderTime != null) {
                    if (currentTask.isRecurring()) {
                        reminderTime = Helper.getNextRecurringDate(reminderTime,
                            currentTask.getRecurrencePattern(),
                            currentTask.getRecurrenceInterval(),
                            Helper.getCurrentTimestamp())
                    }
                    tvh.reminder.text = Helper.createLocalizedDateTimeString(reminderTime)
                    tvh.reminderIcon.visibility = View.VISIBLE
                    tvh.reminder.visibility = View.VISIBLE
                } else {
                    tvh.reminderIcon.visibility = View.GONE
                    tvh.reminder.visibility = View.GONE
                }
                val deadlineColor = currentTask.getDeadlineColor(PreferenceMgr.getDefaultReminderTimeSpan(context))
                tvh.deadlineColorBar.setBackgroundColor(Helper.getDeadlineColor(context, deadlineColor))
                tvh.done.isChecked = currentTask.isDone()
                tvh.done.jumpDrawablesToCurrentState()
                tvh.done.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (buttonView.isPressed) {
                        val snackBar = Snackbar.make(buttonView, R.string.snack_check, Snackbar.LENGTH_LONG)
                        snackBar.setAction(R.string.snack_undo) {
                            val inverted = !isChecked
                            buttonView.isChecked = inverted
                            currentTask.setDone(buttonView.isChecked)
                            currentTask.setAllSubtasksDone(inverted)
                            currentTask.getProgress(hasAutoProgress())
                            currentTask.setChanged()
                            for (subtask: TodoSubtask in currentTask.getSubtasks()) {
                                subtask.setDone(inverted)
                            }
                            model.saveTodoTaskAndSubtasksInDb(currentTask) {
                                notifyDataSetChanged()
                            }
                        }
                        snackBar.show()
                        currentTask.setDone(buttonView.isChecked)
                        currentTask.setAllSubtasksDone(buttonView.isChecked)
                        currentTask.getProgress(hasAutoProgress())
                        currentTask.setChanged()
                        for (subtask: TodoSubtask in currentTask.getSubtasks()) {
                            subtask.setChanged()
                        }
                        model.saveTodoTaskInDb(currentTask) {
                            notifyDataSetChanged()
                        }
                    }
                }
            }

            GroupType.PRIORITY_ROW -> {
                val pvh: GroupPriorityViewHolder
                if (actualConvertView?.tag is GroupPriorityViewHolder) {
                    pvh = actualConvertView.tag as GroupPriorityViewHolder
                } else {
                    actualConvertView =
                        LayoutInflater.from(context).inflate(R.layout.exlv_prio_bar, parent, false)
                    pvh = GroupPriorityViewHolder(
                        actualConvertView.findViewById(R.id.tv_exlv_priority_bar)
                    )
                    actualConvertView.tag = pvh
                }
                pvh.priorityFlag.text = getPriorityNameByBarPos(groupPosition)
                actualConvertView!!.isClickable = true
            }
        }
        return actualConvertView
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean,
                              convertView: View?, parent: ViewGroup): View? {
        var actualConvertView = convertView
        val childType = getChildTypeEnum(groupPosition, childPosition)
        val currentTaskHolder = getTaskByPosition(groupPosition) ?: return actualConvertView
        val currentTask = currentTaskHolder.todoTask
        when (childType) {
            ChildType.TASK_DESCRIPTION_ROW -> {
                val dvh: TaskDescriptionViewHolder
                if (actualConvertView?.tag is TaskDescriptionViewHolder) {
                    dvh = actualConvertView.tag as TaskDescriptionViewHolder
                } else {
                    actualConvertView = LayoutInflater.from(parent.context)
                        .inflate(R.layout.exlv_task_description_row, parent, false)
                    dvh = TaskDescriptionViewHolder(
                        actualConvertView.findViewById(R.id.tv_exlv_task_description),
                        actualConvertView.findViewById(R.id.v_task_description_deadline_color_bar)
                    )
                    actualConvertView.tag = dvh
                }
                val description = currentTask.getDescription()
                if (description.isNotEmpty()) {
                    dvh.taskDescription.visibility = View.VISIBLE
                    dvh.taskDescription.text = description
                } else {
                    dvh.taskDescription.visibility = View.GONE
                }
                dvh.deadlineColorBar.setBackgroundColor(Helper.getDeadlineColor(context,
                    currentTask.getDeadlineColor(PreferenceMgr.getDefaultReminderTimeSpan(context))))
            }

            ChildType.SETTING_ROW -> {
                val sevh: SettingViewHolder
                if (actualConvertView?.tag is SettingViewHolder) {
                    sevh = actualConvertView.tag as SettingViewHolder
                } else {
                    actualConvertView = LayoutInflater.from(parent.context)
                        .inflate(R.layout.exlv_setting_row, parent, false)
                    sevh = SettingViewHolder(
                        actualConvertView.findViewById(R.id.ll_add_subtask),
                        actualConvertView.findViewById(R.id.v_setting_deadline_color_bar)
                    )
                    actualConvertView.tag = sevh
                    if (currentTask.isInRecycleBin()) actualConvertView.visibility = View.GONE
                }
                sevh.addSubtaskButton.setOnClickListener {
                    val newSubtaskDialog = ProcessTodoSubtaskDialog(context)
                    newSubtaskDialog.setDialogCallback { todoSubtask ->
                        currentTask.getSubtasks().add(todoSubtask)
                        todoSubtask.setTaskId(currentTask.getId())
                        model.saveTodoSubtaskInDb(todoSubtask) {
                            notifyDataSetChanged()
                        }
                    }
                    newSubtaskDialog.show()
                }
                sevh.deadlineColorBar.setBackgroundColor(Helper.getDeadlineColor(context,
                    currentTask.getDeadlineColor(PreferenceMgr.getDefaultReminderTimeSpan(context))))
            }

            ChildType.SUBTASK_ROW -> {
                val subtaskIndex = childPosition - 1
                val currentSubtask = currentTask.getSubtasks()[subtaskIndex]
                val currentSubtaskMetaData = currentTaskHolder.getSubtaskMetaData(subtaskIndex)!!
                val svh: SubtaskViewHolder
                if (actualConvertView?.tag is SubtaskViewHolder) {
                    svh = actualConvertView.tag as SubtaskViewHolder
                } else {
                    actualConvertView = LayoutInflater.from(context)
                        .inflate(R.layout.exlv_subtask_row, parent, false)
                    svh = SubtaskViewHolder(
                        actualConvertView.findViewById(R.id.tv_subtask_name),
                        actualConvertView.findViewById(R.id.cb_subtask_done),
                        actualConvertView.findViewById(R.id.v_subtask_deadline_color_bar),
                        actualConvertView.findViewById(R.id.bt_subtask_move_up),
                        actualConvertView.findViewById(R.id.bt_subtask_move_down),
                        actualConvertView.findViewById(R.id.bt_subtask_menu)
                    )
                    actualConvertView.tag = svh
                }
                svh.deadlineColorBar.setBackgroundColor(Helper.getDeadlineColor(context,
                    currentTask.getDeadlineColor(PreferenceMgr.getDefaultReminderTimeSpan(context))))
                svh.done.isChecked = currentSubtask.isDone()
                svh.done.jumpDrawablesToCurrentState()
                svh.done.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (buttonView.isPressed) {
                        currentSubtask.setDone(buttonView.isChecked)
                        currentSubtask.setChanged()
                        model.saveTodoSubtaskInDb(currentSubtask) {
                            if (hasAutoProgress()) {
                                // If having auto-progress, update the progress and save it.
                                currentTask.getProgress(true)
                                model.saveTodoTaskInDb(currentTask) {
                                    notifyDataSetChanged()
                                }
                            } else {
                                notifyDataSetChanged()
                            }
                        }
                    }
                }
                svh.subtaskName.text = currentSubtask.getName()
                svh.moveUpButton.visibility = currentSubtaskMetaData.moveButtonsVisibility
                svh.moveDownButton.visibility = currentSubtaskMetaData.moveButtonsVisibility
                svh.moveUpButton.setOnClickListener {
                    moveSubtask(currentTaskHolder, subtaskIndex, true)
                }
                svh.moveDownButton.setOnClickListener {
                    moveSubtask(currentTaskHolder, subtaskIndex, false)
                }
                svh.subtaskMenuButton.setOnClickListener {
                    onSubtaskMenuClickListener?.onSubtaskMenuClicked(currentTask, currentSubtask)
                }
            }
        }
        return actualConvertView
    }

    private fun moveTask(taskHolder: TaskHolder, groupPosition: Int, moveUp: Boolean) {
        if (filteredTasks.size < 2) {
            return
        }

        // Can't move task if different lists are shown.
        var isFirst = true
        var otherListId: Int? = null
        for (filteredTaskHolder in filteredTasks) {
            val currentListId = filteredTaskHolder.todoTask.getListId()
            if (!isFirst && currentListId != otherListId) {
                Toast.makeText(context, context.getString(R.string.cant_move_task_if_diff_lists),
                    Toast.LENGTH_SHORT).show()
                return
            }
            isFirst = false
            otherListId = currentListId
        }

        // Can't move task if filtering, grouping or sorting is active.
        if (   null != queryString
            || taskFilter != TaskFilter.ALL_TASKS
            || isGroupingByPriority
            || isSortingByDeadline
            || isSortingByNameAsc) {
            Toast.makeText(context, context.getString(R.string.cant_move_task_if_filter_group_sort),
                Toast.LENGTH_SHORT).show()
            return
        }

        val oldIndex = todoTasks.indexOf(taskHolder.todoTask)
        if (oldIndex < 0) {
            Log.e(TAG, "Task ${taskHolder.todoTask} not found.")
            return
        }
        val newIndex = oldIndex + if (moveUp) -1 else 1
        if (newIndex < 0) {
            // Shift all one up.
            val lastIndex = todoTasks.size - 1
            for (index in lastIndex - 1 downTo 0) {
                swapTasks(lastIndex, index)
            }
        } else if (newIndex >= todoTasks.size) {
            // Shift all one down.
            for (index in 1..<todoTasks.size) {
                swapTasks(0, index)
            }
        } else {
            swapTasks(oldIndex, newIndex, groupPosition, getPositionByTask(newIndex))
        }
        // Save changes
        model.saveTodoTasksSortOrderInDb(todoTasks) {
            // Notify view
            notifyDataSetChanged()
        }
    }

    private fun swapTasks(indexA: Int, indexB: Int) {
        swapTasks(indexA, indexB, getPositionByTask(indexA), getPositionByTask(indexB))
    }

    private fun swapTasks(indexA: Int, indexB: Int, positionA: Int, positionB: Int) {
        // Swap tasks in data model
        val taskA = todoTasks[indexA]
        todoTasks[indexA] = todoTasks[indexB]
        todoTasks[indexB] = taskA
        // Swap tasks on UI
        onTasksSwappedListener?.onTasksSwapped(positionA, positionB)
    }

    private fun moveSubtask(taskHolder: TaskHolder, subtaskIndex: Int, moveUp: Boolean) {
        val subtasks = taskHolder.todoTask.getSubtasks()
        if (subtasks.size < 2) {
            return
        }
        if (subtaskIndex < 0 || subtaskIndex >= subtasks.size) {
            Log.e(TAG, "Illegal subtask index $subtaskIndex with ${subtasks.size} subtasks.")
            return
        }
        val newIndex = subtaskIndex + if (moveUp) -1 else 1
        if (newIndex < 0) {
            // Shift all one up.
            val lastIndex = subtasks.size - 1
            for (index in lastIndex - 1 downTo 0) {
                swapSubtasks(taskHolder, subtasks, lastIndex, index)
            }
        } else if (newIndex >= subtasks.size) {
            // Shift all one down.
            for (index in 1..<subtasks.size) {
                swapSubtasks(taskHolder, subtasks, 0, index)
            }
        } else {
            swapSubtasks(taskHolder, subtasks, subtaskIndex, newIndex)
        }
        // Save changes
        model.saveTodoSubtasksSortOrderInDb(taskHolder.todoTask.getSubtasks()) {
            // Notify view
            notifyDataSetChanged()
        }
    }

    private fun swapSubtasks(taskHolder: TaskHolder, subtasks: MutableList<TodoSubtask>,
                             indexA: Int, indexB: Int) {
        // Swap subtasks in dataset
        val subtaskA = subtasks[indexA]
        subtasks[indexA] = subtasks[indexB]
        subtasks[indexB] = subtaskA
        // Swap meta data of subtasks
        val metaDataA = taskHolder.getSubtaskMetaData(indexA)!!
        val metaDataB = taskHolder.getSubtaskMetaData(indexB)!!
        taskHolder.setSubtaskMetaData(indexA, metaDataB)
        taskHolder.setSubtaskMetaData(indexB, metaDataA)
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        val todoTask = getTaskByPosition(groupPosition)?.todoTask
        return null != todoTask && childPosition > 0 && childPosition < todoTask.getSubtasks().size + 1
    }

    private fun hasAutoProgress(): Boolean {
        //automatic-progress enabled?
        return prefs.getBoolean(PreferenceMgr.P_IS_AUTO_PROGRESS.name, false)
    }

    inner class GroupTaskViewHolder(
        val name: TextView,
        val moveUpButton: ImageButton,
        val moveDownButton: ImageButton,
        val taskMenuButton: ImageButton,
        val deadlineIcon: ImageView,
        val deadline: TextView,
        val reminderIcon: ImageView,
        val reminder: TextView,
        val listName: TextView,
        val done: CheckBox,
        val deadlineColorBar: View,
        val progressBar: ProgressBar
    )

    private inner class GroupPriorityViewHolder(
        val priorityFlag: TextView
    )

    private inner class SubtaskViewHolder(
        val subtaskName: TextView,
        val done: CheckBox,
        val deadlineColorBar: View,
        val moveUpButton: ImageButton,
        val moveDownButton: ImageButton,
        val subtaskMenuButton: ImageButton
    )

    private inner class TaskDescriptionViewHolder(
        val taskDescription: TextView,
        val deadlineColorBar: View
    )

    private inner class SettingViewHolder(
        val addSubtaskButton: LinearLayout,
        val deadlineColorBar: View
    )

    private inner class TaskHolder(val todoTask: TodoTask) {
        private val subtasksMetaData = MutableList(todoTask.getSubtasks().size) { index ->
            return@MutableList SubtaskMetaData()
        }

        private fun adaptMetaDataListSize(expectedSize: Int) {
            while (expectedSize > subtasksMetaData.size) {
                subtasksMetaData.add(SubtaskMetaData())
            }
            while (expectedSize < subtasksMetaData.size) {
                subtasksMetaData.removeAt(subtasksMetaData.size - 1)
            }
        }

        fun setSubtaskMetaData(subtaskIndex: Int, value: SubtaskMetaData) {
            val subtasksCount = todoTask.getSubtasks().size
            if (subtaskIndex in 0..<subtasksCount) {
                adaptMetaDataListSize(subtasksCount)
                subtasksMetaData[subtaskIndex] = value
            } else {
                Log.e(TAG, "Invalid subtask index: $subtaskIndex. Subtasks count: $subtasksCount.")
            }
        }

        fun getSubtaskMetaData(subtaskIndex: Int): SubtaskMetaData? {
            var result: SubtaskMetaData? = null
            val subtasksCount = todoTask.getSubtasks().size
            if (subtaskIndex in 0..<subtasksCount) {
                adaptMetaDataListSize(subtasksCount)
                result = subtasksMetaData[subtaskIndex]
            } else {
                Log.e(TAG, "Invalid subtask index: $subtaskIndex. Subtasks count: $subtasksCount.")
            }
            return result
        }
    }

    private inner class SubtaskMetaData(var moveButtonsVisibility: Int = View.GONE) {
        fun toggleMoveButtonsVisibility() {
            moveButtonsVisibility = if (moveButtonsVisibility == View.GONE) View.VISIBLE else View.GONE
        }
    }

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
    }
}