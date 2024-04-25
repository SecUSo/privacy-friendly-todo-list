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

import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import android.widget.ExpandableListView.ExpandableListContextMenuInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.ModelServices
import org.secuso.privacyfriendlytodolist.model.TodoList
import org.secuso.privacyfriendlytodolist.model.TodoSubtask
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.util.Helper.getMenuHeader
import org.secuso.privacyfriendlytodolist.util.LogTag
import org.secuso.privacyfriendlytodolist.view.ExpandableTodoTaskAdapter.GroupTaskViewHolder
import org.secuso.privacyfriendlytodolist.view.ExpandableTodoTaskAdapter.SortTypes
import org.secuso.privacyfriendlytodolist.view.dialog.ProcessTodoSubtaskDialog
import org.secuso.privacyfriendlytodolist.view.dialog.ProcessTodoTaskDialog
import org.secuso.privacyfriendlytodolist.view.dialog.ResultCallback
import org.secuso.privacyfriendlytodolist.viewmodel.LifecycleViewModel

@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class TodoTasksFragment : Fragment(), SearchView.OnQueryTextListener {
    private lateinit var containingActivity: MainActivity
    private lateinit var model: ModelServices
    private var expandableListView: ExpandableListView? = null
    private var taskAdapter: ExpandableTodoTaskAdapter? = null
    private var currentList: TodoList? = null
    private var todoTasks: MutableList<TodoTask> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (activity !is MainActivity) {
            throw RuntimeException("TodoTasksFragment could not find containing activity.")
        }
        containingActivity = activity as MainActivity
        val viewModel = ViewModelProvider(this)[LifecycleViewModel::class.java]
        model = viewModel.model
        var showFab = false
        var showListNamesOfTasks = false
        val arguments = arguments
        if (null != arguments) {
            showFab = arguments.getBoolean(SHOW_FLOATING_BUTTON)

            // KEY_SELECTED_LIST_ID_BY_NOTIFICATION argument is set if a list was selected by clicking on a notification.
            // KEY_SELECTED_DUMMY_LIST_BY_NOTIFICATION is set if a dummy list is displayed (a mixture of tasks of different lists).
            // If the the user selects a list explicitly by clicking on it the list object is
            // instantly available and can be obtained using the method "getClickedList()"
            if (arguments.containsKey(MainActivity.KEY_SELECTED_LIST_ID_BY_NOTIFICATION)) {
                // MainActivity was started after a notification click
                val selectedListID = arguments.getInt(MainActivity.KEY_SELECTED_LIST_ID_BY_NOTIFICATION)
                currentList = null
                for (todoList in containingActivity.todoLists) {
                    if (todoList.getId() == selectedListID) {
                        currentList = todoList
                        break
                    }
                }
                Log.i(TAG, "List was loaded that was requested by a click on a notification.")
            } else if (arguments.containsKey(MainActivity.KEY_SELECTED_DUMMY_LIST_BY_NOTIFICATION)) {
                currentList = containingActivity.dummyList
                showListNamesOfTasks = true
                Log.i(TAG, "Dummy list was loaded.")
            } else {
                currentList = containingActivity.clickedList // get clicked list
                Log.i(TAG, "Clicked list was loaded.")
            }
        } else {
            Log.e(TAG, "Expected arguments but got none.")
        }
        val v = inflater.inflate(R.layout.fragment_todo_tasks, container, false)
        if (currentList != null) {
            todoTasks = currentList!!.getTasks()
            initExListViewGUI(v)
            initFab(v, showFab)
            taskAdapter!!.showListNames = showListNamesOfTasks

            // set toolbar title
            val supportActionBar = (activity as AppCompatActivity?)!!.supportActionBar
            if (supportActionBar != null) {
                supportActionBar.title = currentList!!.getName()
            }
        } else {
            Log.d(TAG, "Cannot identify selected list.")
        }
        return v
    }

    private fun initFab(rootView: View, showFab: Boolean) {
        val optionFab: FloatingActionButton = rootView.findViewById(R.id.fab_new_task)
        if (showFab) {
            optionFab.setOnClickListener {
                val dialog = ProcessTodoTaskDialog(containingActivity, containingActivity.todoLists)
                dialog.setDialogCallback(ResultCallback { todoTask: TodoTask ->
                    todoTasks.add(todoTask)
                    saveNewTasks()
                    taskAdapter!!.notifyDataSetChanged()
                })
                dialog.show()
            }
        } else {
            optionFab.visibility = View.GONE
        }
    }

    private fun initExListViewGUI(view: View) {
        taskAdapter = ExpandableTodoTaskAdapter(requireActivity(), model, todoTasks)
        val emptyView: TextView = view.findViewById(R.id.tv_empty_view_no_tasks)
        expandableListView = view.findViewById(R.id.exlv_tasks)
        expandableListView!!.setOnItemLongClickListener { parent, v, position, id ->
            val groupPosition = ExpandableListView.getPackedPositionGroup(id)
            if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                val childPosition = ExpandableListView.getPackedPositionChild(id)
                taskAdapter!!.setLongClickedSubtaskByPos(groupPosition, childPosition)
            } else {
                taskAdapter!!.setLongClickedTaskByPos(groupPosition)
            }
            false
        }


        // react when task expands
        expandableListView!!.setOnGroupClickListener { parent, v, groupPosition, id ->
            val vh = v.tag
            if (vh is GroupTaskViewHolder) {
                val separator: View? = vh.separator
                if (null != separator) {
                    if (separator.visibility == View.GONE) {
                        separator.visibility = View.VISIBLE
                    } else {
                        separator.visibility = View.GONE
                    }
                }
            }
            false
        }

        // long click to delete or change a task
        registerForContextMenu(expandableListView!!)
        expandableListView!!.setEmptyView(emptyView)
        expandableListView!!.setAdapter(taskAdapter)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        val info = menuInfo as ExpandableListContextMenuInfo?
        val type = ExpandableListView.getPackedPositionType(info!!.packedPosition)
        val inflater = requireActivity().getMenuInflater()
        val context = requireContext()
        menu.setHeaderView(getMenuHeader(context, context.getString(R.string.select_option)))

        // context menu for child items
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            inflater.inflate(R.menu.todo_subtask_long_click, menu)
        } else { // context menu for group items
            inflater.inflate(R.menu.todo_task_long_click, menu)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val longClickedTodo = taskAdapter!!.longClickedTodo
        if (null != longClickedTodo) {
            val todoTask = longClickedTodo.left
            val todoSubtask = longClickedTodo.right
            when (item.itemId) {
                R.id.change_subtask -> {
                    val dialog = ProcessTodoSubtaskDialog(containingActivity, todoSubtask!!)
                    dialog.setDialogCallback(ResultCallback { todoSubtask2: TodoSubtask? ->
                        taskAdapter!!.notifyDataSetChanged()
                        Log.i(TAG, "subtask altered")
                    })
                    dialog.show()
                }

                R.id.delete_subtask -> model.setSubtaskInRecycleBin(todoSubtask!!, true) { counter: Int ->
                    todoTask.getSubtasks().remove(todoSubtask)
                    if (counter == 1) {
                        Toast.makeText(context, getString(R.string.subtask_removed), Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d(TAG, "Subtask was not removed from the database. Maybe it was not added beforehand (then this is no error)?")
                    }
                    taskAdapter!!.notifyDataSetChanged()
                }

                R.id.change_task -> {
                    val changeTaskDialog = ProcessTodoTaskDialog(containingActivity, containingActivity.todoLists, todoTask)
                    changeTaskDialog.setDialogCallback(ResultCallback { todoTask2: TodoTask? ->
                        taskAdapter!!.notifyDataSetChanged()
                    })
                    changeTaskDialog.show()
                }

                R.id.delete_task -> model.setTaskAndSubtasksInRecycleBin(todoTask, true) { counter: Int ->
                    todoTasks.remove(todoTask)
                    if (counter == 1) {
                        Toast.makeText(context, getString(R.string.task_removed), Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d(TAG, "Task was not removed from the database. Maybe it was not added beforehand (then this is no error)?")
                    }
                    taskAdapter!!.notifyDataSetChanged()
                }

                else -> throw IllegalArgumentException("Invalid menu item selected.")
            }
        }
        return super.onContextItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        setRetainInstance(true)
    }

    override fun onPause() {
        saveNewTasks()
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main, menu)
        inflater.inflate(R.menu.search, menu)
        inflater.inflate(R.menu.add_list, menu)
        val searchView = menu.findItem(R.id.ac_search).actionView as SearchView
        searchView.setOnQueryTextListener(this)
    }

    private fun collapseAll() {
        // collapse all elements on view change.
        // the expandable list view keeps the expanded indices, so other items
        // get expanded, when they get the old expanded index
        val groupCount = taskAdapter!!.groupCount
        for (i in 0 until groupCount) {
            expandableListView!!.collapseGroup(i)
        }
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        collapseAll()
        taskAdapter!!.queryString = query
        taskAdapter!!.notifyDataSetChanged()
        return false
    }

    override fun onQueryTextChange(query: String): Boolean {
        collapseAll()
        taskAdapter!!.queryString = query
        taskAdapter!!.notifyDataSetChanged()
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val checked: Boolean
        val sortType: SortTypes
        collapseAll()
        when (item.itemId) {
            R.id.ac_show_all_tasks -> {
                taskAdapter!!.filter = ExpandableTodoTaskAdapter.Filter.ALL_TASKS
                taskAdapter!!.notifyDataSetChanged()
                return true
            }

            R.id.ac_show_open_tasks -> {
                taskAdapter!!.filter = ExpandableTodoTaskAdapter.Filter.OPEN_TASKS
                taskAdapter!!.notifyDataSetChanged()
                return true
            }

            R.id.ac_show_completed_tasks -> {
                taskAdapter!!.filter = ExpandableTodoTaskAdapter.Filter.COMPLETED_TASKS
                taskAdapter!!.notifyDataSetChanged()
                return true
            }

            R.id.ac_group_by_prio -> {
                checked = !item.isChecked
                item.setChecked(checked)
                sortType = SortTypes.PRIORITY
            }

            R.id.ac_sort_by_deadline -> {
                checked = !item.isChecked
                item.setChecked(checked)
                sortType = SortTypes.DEADLINE
            }

            else -> return super.onOptionsItemSelected(item)
        }
        if (checked) {
            taskAdapter!!.addSortCondition(sortType)
        } else {
            taskAdapter!!.removeSortCondition(sortType)
        }
        taskAdapter!!.notifyDataSetChanged()
        return true
    }

    // write new tasks to the database
    private fun saveNewTasks() {
        for (todoTask in todoTasks) {
            if (currentList!!.isDummyList()) {
                todoTask.setListId(null)
            } else {
                todoTask.setListId(currentList!!.getId()) // crucial step to not lose the connection to the list
            }
            for (todoSubtask in todoTask.getSubtasks()) {
                todoSubtask.setTaskId(todoTask.getId()) // crucial step to not lose the connection to the task
            }
            model.saveTodoTaskAndSubtasksInDb(todoTask) { counter: Int? ->
                containingActivity.onTaskChange(todoTask)
            }
        }
    }

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)

        // The fab is used to create new tasks. However, a task can only be created if the user is inside
        // a certain list. If he chose the "show all task" view, the option to create a new task is not available.
        const val SHOW_FLOATING_BUTTON = "SHOW_FAB"
    }
}
