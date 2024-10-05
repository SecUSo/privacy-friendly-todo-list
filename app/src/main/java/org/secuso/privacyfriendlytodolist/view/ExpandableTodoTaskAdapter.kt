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
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.ModelServices
import org.secuso.privacyfriendlytodolist.model.TodoSubtask
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.model.Tuple
import org.secuso.privacyfriendlytodolist.model.Tuple.Companion.makePair
import org.secuso.privacyfriendlytodolist.util.AlarmMgr
import org.secuso.privacyfriendlytodolist.util.Helper
import org.secuso.privacyfriendlytodolist.util.Helper.createLocalizedDateString
import org.secuso.privacyfriendlytodolist.util.Helper.getDeadlineColor
import org.secuso.privacyfriendlytodolist.util.Helper.priorityToString
import org.secuso.privacyfriendlytodolist.util.LogTag
import org.secuso.privacyfriendlytodolist.util.PreferenceMgr
import org.secuso.privacyfriendlytodolist.view.dialog.ProcessTodoSubtaskDialog
import java.util.Collections

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
    private val todoTasks: List<TodoTask>, private val showListNames: Boolean) : BaseExpandableListAdapter() {
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    /**
     * left item: task that was long clicked,
     * right item: subtask that was long clicked
     */
    var longClickedTodo: Tuple<TodoTask, TodoSubtask?>? = null
        private set

    enum class Filter {
        ALL_TASKS,
        COMPLETED_TASKS,
        OPEN_TASKS
    }

    // FILTER AND SORTING OPTIONS MADE BY THE USER
    var queryString: String?
    var filter: Filter
    var isGroupingByPriority: Boolean
    var isSortingByDeadline: Boolean
    private val filteredTasks: MutableList<TaskHolder> = ArrayList() // data after filtering process
    private val priorityBarPositions = HashMap<TodoTask.Priority, Int>()
    private var listNames: Map<Int, String> = HashMap(0)

    init {
        val filterString = prefs.getString(PreferenceMgr.P_TASK_FILTER.name, Filter.ALL_TASKS.name)
        filter = Filter.entries.find { value ->
            value.name == filterString
        } ?: Filter.ALL_TASKS
        isGroupingByPriority = prefs.getBoolean(PreferenceMgr.P_GROUP_BY_PRIORITY.name, false)
        isSortingByDeadline = prefs.getBoolean(PreferenceMgr.P_SORT_BY_DEADLINE.name, false)
        queryString = null
        notifyDataSetChanged()
    }

    fun setLongClickedTaskByPos(position: Int) {
        longClickedTodo = null
        val todoTask = getTaskByPosition(position)?.todoTask
        if (null != todoTask) {
            longClickedTodo = makePair(todoTask, null)
        } else {
            Log.w(TAG, "Unable to get task by position $position")
        }
    }

    fun setLongClickedSubtaskByPos(groupPosition: Int, childPosition: Int) {
        longClickedTodo = null
        val todoTask = getTaskByPosition(groupPosition)?.todoTask
        if (null != todoTask) {
            val subtasks: List<TodoSubtask> = todoTask.getSubtasks()
            val index = childPosition - 1
            if (index >= 0 && index < subtasks.size) {
                longClickedTodo = makePair(todoTask, subtasks[index])
            }
        }
        if (null == longClickedTodo) {
            Log.w(TAG, "Unable to get subtask by position $groupPosition, $childPosition")
        }
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
            Log.w(TAG, "Unable to get subtask by position $groupPosition, $childPosition")
        }
    }

    /**
     * filter tasks by "done" criterion (show "all", only "open" or only "completed" tasks)
     * If the user changes the filter, it is crucial to call "sortTasks" again.
     */
    private fun filterTasks() {
        val newFilteredTasks: MutableList<TaskHolder> = ArrayList()
        val notOpen = filter != Filter.OPEN_TASKS
        val notCompleted = filter != Filter.COMPLETED_TASKS
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
        val prioritySorting = isGroupingByPriority
        Collections.sort(filteredTasks, object : Comparator<TaskHolder> {
            private fun compareDeadlines(d1: Long?, d2: Long?): Int {
                // tasks with deadlines always first
                if (d1 == null && d2 == null) return 0
                if (d1 == null) return 1
                if (d2 == null) return -1
                if (d1 < d2) return -1
                return if (d1 == d2) 0 else 1
            }

            @Suppress("LiftReturnOrAssignment")
            override fun compare(taskHolder1: TaskHolder, taskHolder2: TaskHolder): Int {
                val result: Int
                val t1 = taskHolder1.todoTask
                val t2 = taskHolder2.todoTask
                if (prioritySorting) {
                    val p1 = t1.getPriority()
                    val p2 = t2.getPriority()
                    val comp = p1.compareTo(p2)
                    if (comp == 0 && isSortingByDeadline) {
                        result = compareDeadlines(t1.getDeadline(), t2.getDeadline())
                    } else {
                        result = comp
                    }
                } else if (isSortingByDeadline) {
                    result = compareDeadlines(t1.getDeadline(), t2.getDeadline())
                } else {
                    result = t1.getSortOrder() - t2.getSortOrder()
                }
                return result
            }
        })
        if (prioritySorting) {
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
                    if (groupPosition < priorityPos) break
                    ++seenPriorityBars
                }
            }
        }
        val pos = groupPosition - seenPriorityBars
        if (pos >= 0 && pos < filteredTasks.size) {
            return filteredTasks[pos]
        }
        Log.w(TAG, "Unable to get task by group position $groupPosition")
        return null // should never be the case
    }

    override fun getGroupCount(): Int {
        return if (isGroupingByPriority) filteredTasks.size + priorityBarPositions.size else filteredTasks.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        var count = 0
        val todoTask = getTaskByPosition(groupPosition)?.todoTask
        if (null != todoTask) {
            count = todoTask.getSubtasks().size + 2
        }
        return count
    }

    override fun getGroupType(groupPosition: Int): Int {
        return if (isGroupingByPriority && priorityBarPositions.values.contains(groupPosition)) GR_PRIORITY_ROW else GR_TASK_ROW
    }

    override fun getGroupTypeCount(): Int {
        return 2
    }

    override fun getChildType(groupPosition: Int, childPosition: Int): Int {
        if (childPosition == 0) return CH_TASK_DESCRIPTION_ROW
        val todoTask = getTaskByPosition(groupPosition)?.todoTask
        return if (null != todoTask && childPosition == (todoTask.getSubtasks().size + 1)) CH_SETTING_ROW else CH_SUBTASK_ROW
    }

    override fun getChildTypeCount(): Int {
        return 3
    }

    override fun getGroup(groupPosition: Int): Any {
        return filteredTasks[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return childPosition
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    private fun getPriorityNameByBarPos(groupPosition: Int): String {
        var priority: TodoTask.Priority? = null
        for ((key, value) in priorityBarPositions) {
            if (value == groupPosition) {
                priority = key
                break
            }
        }
        return priorityToString(context, priority)
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
        val type = getGroupType(groupPosition)
        when (type) {
            GR_PRIORITY_ROW -> {
                val vh1: GroupPriorityViewHolder
                if (actualConvertView?.tag is GroupPriorityViewHolder) {
                    vh1 = actualConvertView.tag as GroupPriorityViewHolder
                } else {
                    actualConvertView =
                        LayoutInflater.from(context).inflate(R.layout.exlv_prio_bar, parent, false)
                    vh1 = GroupPriorityViewHolder()
                    vh1.priorityFlag = actualConvertView.findViewById(R.id.tv_exlv_priority_bar)
                    actualConvertView.tag = vh1
                }
                vh1.priorityFlag!!.text = getPriorityNameByBarPos(groupPosition)
                actualConvertView!!.isClickable = true
            }

            GR_TASK_ROW -> {
                val currentTaskHolder = getTaskByPosition(groupPosition) ?: return actualConvertView
                val currentTask = currentTaskHolder.todoTask
                val vh2: GroupTaskViewHolder
                if (actualConvertView?.tag is GroupTaskViewHolder) {
                    vh2 = actualConvertView.tag as GroupTaskViewHolder
                } else {
                    actualConvertView = LayoutInflater.from(context)
                        .inflate(R.layout.exlv_tasks_group, parent, false)
                    vh2 = GroupTaskViewHolder()
                    vh2.name = actualConvertView.findViewById(R.id.tv_exlv_task_name)
                    vh2.done = actualConvertView.findViewById(R.id.cb_task_done)
                    vh2.deadline = actualConvertView.findViewById(R.id.tv_exlv_task_deadline)
                    vh2.listName = actualConvertView.findViewById(R.id.tv_exlv_task_list_name)
                    vh2.progressBar = actualConvertView.findViewById(R.id.pb_task_progress)
                    vh2.separator = actualConvertView.findViewById(R.id.v_exlv_header_separator)
                    vh2.deadlineColorBar = actualConvertView.findViewById(R.id.v_urgency_task)
                    vh2.done!!.tag = currentTask.getId()
                    vh2.done!!.setChecked(currentTask.isDone())
                    vh2.done!!.jumpDrawablesToCurrentState()
                    actualConvertView.tag = vh2
                }
                vh2.name!!.text = currentTask.getName()
                vh2.progressBar!!.progress = currentTask.getProgress(hasAutoProgress())
                vh2.listName!!.visibility = View.GONE
                if (showListNames && currentTask.getListId() != null) {
                    val listName = listNames[currentTask.getListId()]
                    if (null != listName) {
                        vh2.listName!!.text = listName
                        vh2.listName!!.visibility = View.VISIBLE
                    }
                }
                val deadline = currentTask.getDeadline()
                var deadlineStr: String
                    if (deadline == null) {
                        deadlineStr = context.resources.getString(R.string.no_deadline)
                    } else {
                        deadlineStr = context.resources.getString(R.string.deadline_dd) + " " +
                                createLocalizedDateString(deadline)
                        if (currentTask.isRecurring()) {
                            deadlineStr += " (" + Helper.recurrencePatternToString(context, currentTask.getRecurrencePattern()) + ")"
                        }
                    }
                vh2.deadline!!.text = deadlineStr
                vh2.deadlineColorBar!!.setBackgroundColor(
                    getDeadlineColor(context, currentTask.getDeadlineColor(PreferenceMgr.getDefaultReminderTimeSpan(context))))
                vh2.done!!.setChecked(currentTask.isDone())
                vh2.done!!.jumpDrawablesToCurrentState()
                vh2.done!!.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (buttonView.isPressed) {
                        val snackBar = Snackbar.make(buttonView, R.string.snack_check, Snackbar.LENGTH_LONG)
                        snackBar.setAction(R.string.snack_undo) {
                            val inverted = !isChecked
                            buttonView.setChecked(inverted)
                            currentTask.setDone(buttonView.isChecked)
                            currentTask.setAllSubtasksDone(inverted)
                            currentTask.getProgress(hasAutoProgress())
                            currentTask.setChanged()
                            notifyDataSetChanged()
                            for (subtask: TodoSubtask in currentTask.getSubtasks()) {
                                subtask.setDone(inverted)
                            }
                            model.saveTodoTaskAndSubtasksInDb(currentTask) {
                                AlarmMgr.setAlarmForTask(context, currentTask)
                            }
                        }
                        snackBar.show()
                        currentTask.setDone(buttonView.isChecked)
                        currentTask.setAllSubtasksDone(buttonView.isChecked)
                        currentTask.getProgress(hasAutoProgress())
                        currentTask.setChanged()
                        notifyDataSetChanged()
                        for (subtask: TodoSubtask in currentTask.getSubtasks()) {
                            subtask.setChanged()
                            notifyDataSetChanged()
                        }
                        model.saveTodoTaskInDb(currentTask) {
                            AlarmMgr.setAlarmForTask(context, currentTask)
                        }
                    }
                }
            }

            else -> {}
        }
        return actualConvertView
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean,
                              convertView: View?, parent: ViewGroup): View? {
        var actualConvertView = convertView
        val type = getChildType(groupPosition, childPosition)
        val currentTaskHolder = getTaskByPosition(groupPosition) ?: return actualConvertView
        val currentTask = currentTaskHolder.todoTask
        when (type) {
            CH_TASK_DESCRIPTION_ROW -> {
                val vh1: TaskDescriptionViewHolder
                if (actualConvertView?.tag is TaskDescriptionViewHolder) {
                    vh1 = actualConvertView.tag as TaskDescriptionViewHolder
                } else {
                    actualConvertView = LayoutInflater.from(parent.context)
                        .inflate(R.layout.exlv_task_description_row, parent, false)
                    vh1 = TaskDescriptionViewHolder()
                    vh1.taskDescription = actualConvertView.findViewById(R.id.tv_exlv_task_description)
                    vh1.deadlineColorBar = actualConvertView.findViewById(R.id.v_task_description_deadline_color_bar)
                    actualConvertView.tag = vh1
                }
                val description = currentTask.getDescription()
                if (description.isNotEmpty()) {
                    vh1.taskDescription!!.visibility = View.VISIBLE
                    vh1.taskDescription!!.text = description
                } else {
                    vh1.taskDescription!!.visibility = View.GONE
                }
                vh1.deadlineColorBar!!.setBackgroundColor(
                    getDeadlineColor(context, currentTask.getDeadlineColor(PreferenceMgr.getDefaultReminderTimeSpan(context))))
            }

            CH_SETTING_ROW -> {
                val vh2: SettingViewHolder
                if (actualConvertView?.tag is SettingViewHolder) {
                    vh2 = actualConvertView.tag as SettingViewHolder
                } else {
                    actualConvertView = LayoutInflater.from(parent.context)
                        .inflate(R.layout.exlv_setting_row, parent, false)
                    vh2 = SettingViewHolder()
                    vh2.addSubtaskButton = actualConvertView.findViewById(R.id.rl_add_subtask)
                    vh2.deadlineColorBar = actualConvertView.findViewById(R.id.v_setting_deadline_color_bar)
                    actualConvertView.tag = vh2
                    if (currentTask.isInRecycleBin()) actualConvertView.visibility = View.GONE
                }
                vh2.addSubtaskButton!!.setOnClickListener {
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
                vh2.deadlineColorBar!!.setBackgroundColor(
                    getDeadlineColor(context, currentTask.getDeadlineColor(PreferenceMgr.getDefaultReminderTimeSpan(context))))
            }

            else -> {
                val subtaskIndex = childPosition - 1
                val currentSubtask = currentTask.getSubtasks()[subtaskIndex]
                val currentSubtaskMetaData = currentTaskHolder.getSubtaskMetaData(subtaskIndex)!!
                val vh3: SubtaskViewHolder
                if (actualConvertView?.tag is SubtaskViewHolder) {
                    vh3 = actualConvertView.tag as SubtaskViewHolder
                } else {
                    actualConvertView = LayoutInflater.from(parent.context)
                        .inflate(R.layout.exlv_subtask_row, parent, false)
                    vh3 = SubtaskViewHolder()
                    vh3.subtaskName = actualConvertView.findViewById(R.id.tv_subtask_name)
                    vh3.deadlineColorBar = actualConvertView.findViewById(R.id.v_subtask_deadline_color_bar)
                    vh3.done = actualConvertView.findViewById(R.id.cb_subtask_done)
                    vh3.moveUpButton = actualConvertView.findViewById(R.id.bt_subtask_move_up)
                    vh3.moveDownButton = actualConvertView.findViewById(R.id.bt_subtask_move_down)
                    actualConvertView.tag = vh3
                }
                vh3.deadlineColorBar!!.setBackgroundColor(
                    getDeadlineColor(context, currentTask.getDeadlineColor(PreferenceMgr.getDefaultReminderTimeSpan(context))))
                vh3.done!!.setChecked(currentSubtask.isDone())
                vh3.done!!.jumpDrawablesToCurrentState()
                vh3.done!!.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (buttonView.isPressed) {
                        currentSubtask.setDone(buttonView.isChecked)
                        // check if entire task is now (when all subtasks are done)
                        val doneStatusChanged = currentTask.doneStatusChanged()
                        currentSubtask.setChanged()
                        model.saveTodoSubtaskInDb(currentSubtask) { counter1: Int? ->
                            currentTask.getProgress(hasAutoProgress())
                            model.saveTodoTaskInDb(currentTask) {
                                counter2: Int? -> notifyDataSetChanged()
                                if (doneStatusChanged) {
                                    AlarmMgr.setAlarmForTask(context, currentTask)
                                }
                            }
                        }
                    }
                }
                vh3.subtaskName!!.text = currentSubtask.getName()
                vh3.moveUpButton!!.visibility = currentSubtaskMetaData.moveButtonsVisibility
                vh3.moveDownButton!!.visibility = currentSubtaskMetaData.moveButtonsVisibility
                vh3.moveUpButton!!.setOnClickListener {
                    moveSubtask(currentTaskHolder, subtaskIndex, true)
                }
                vh3.moveDownButton!!.setOnClickListener {
                    moveSubtask(currentTaskHolder, subtaskIndex, false)
                }
            }
        }
        return actualConvertView
    }

    private fun moveSubtask(taskHolder: TaskHolder, subtaskIndex: Int, moveUp: Boolean) {
        val subtasks = taskHolder.todoTask.getSubtasks()
        var newIndex = subtaskIndex + if (moveUp) -1 else 1
        // Wrap around
        if (newIndex < 0) {
            newIndex = subtasks.size - 1
        }
        if (newIndex >= subtasks.size) {
            newIndex = 0
        }
        if (subtaskIndex >= 0 && subtaskIndex < subtasks.size && newIndex >= 0) {
            // Swap subtasks in dataset
            val subtaskA = subtasks[subtaskIndex]
            val subtaskB = subtasks[newIndex]
            subtasks[subtaskIndex] = subtaskB
            subtasks[newIndex] = subtaskA
            // Swap meta data of subtasks
            val metaDataA = taskHolder.getSubtaskMetaData(subtaskIndex)!!
            val metaDataB = taskHolder.getSubtaskMetaData(newIndex)!!
            taskHolder.setSubtaskMetaData(subtaskIndex, metaDataB)
            taskHolder.setSubtaskMetaData(newIndex, metaDataA)
            // Save changes
            model.saveTodoSubtasksSortOrderInDb(taskHolder.todoTask.getSubtasks())
            // Notify view
            notifyDataSetChanged()
        }
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        val todoTask = getTaskByPosition(groupPosition)?.todoTask
        return null != todoTask && childPosition > 0 && childPosition < todoTask.getSubtasks().size + 1
    }

    private fun hasAutoProgress(): Boolean {
        //automatic-progress enabled?
        return prefs.getBoolean(PreferenceMgr.P_IS_AUTO_PROGRESS.name, false)
    }

    inner class GroupTaskViewHolder {
        var name: TextView? = null
        var deadline: TextView? = null
        var listName: TextView? = null
        var done: CheckBox? = null
        var deadlineColorBar: View? = null
        var separator: View? = null
        var progressBar: ProgressBar? = null
    }

    private inner class GroupPriorityViewHolder {
        var priorityFlag: TextView? = null
    }

    private inner class SubtaskViewHolder {
        var subtaskName: TextView? = null
        var done: CheckBox? = null
        var deadlineColorBar: View? = null
        var moveUpButton: ImageButton? = null
        var moveDownButton: ImageButton? = null
    }

    private inner class TaskDescriptionViewHolder {
        var taskDescription: TextView? = null
        var deadlineColorBar: View? = null
    }

    private inner class SettingViewHolder {
        var addSubtaskButton: RelativeLayout? = null
        var deadlineColorBar: View? = null
    }

    private inner class TaskHolder(val todoTask: TodoTask) {
        private val subtasksMetaData = MutableList(todoTask.getSubtasks().size) { index ->
            return@MutableList SubtaskMetaData()
        }

        private fun adaptMetaDataListSize(expectedSize: Int) {
            while (expectedSize > subtasksMetaData.size) {
                subtasksMetaData.add(SubtaskMetaData())
            }
            while (expectedSize < subtasksMetaData.size) {
                subtasksMetaData.removeLast()
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

        // ROW TYPES FOR USED TO CREATE DIFFERENT VIEWS DEPENDING ON ITEM TO SHOW
        private const val GR_TASK_ROW = 0 // gr == group type
        private const val GR_PRIORITY_ROW = 1
        private const val CH_TASK_DESCRIPTION_ROW = 0 // ch == child type
        private const val CH_SETTING_ROW = 1
        private const val CH_SUBTASK_ROW = 2
    }
}