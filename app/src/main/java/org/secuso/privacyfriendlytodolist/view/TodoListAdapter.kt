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

import android.app.Activity
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnCreateContextMenuListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.TodoList
import org.secuso.privacyfriendlytodolist.util.Helper.createDateString
import org.secuso.privacyfriendlytodolist.util.Helper.getDeadlineColor
import org.secuso.privacyfriendlytodolist.util.Helper.getMenuHeader
import org.secuso.privacyfriendlytodolist.util.PreferenceMgr

class TodoListAdapter(ac: Activity, data: List<TodoList>?) :
    RecyclerView.Adapter<TodoListAdapter.ViewHolder>() {
    private val contextActivity: MainActivity
    private var allLists: List<TodoList>? = null

    init {
        updateList(data)
        contextActivity = ac as MainActivity
    }

    // invoked by the layout manager
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.todo_list_entry, parent, false)
        return ViewHolder(v)
    }

    // replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lists = allLists
        if (null != lists) {
            val index = lists.size - 1 - position
            if (index >= 0 && index < lists.size) {
                val list = lists[index]
                holder.title.text = list.getName()
                val res = contextActivity.getResources()
                if (list.getNextDeadline() <= 0) {
                    holder.deadline.text = res.getString(R.string.no_next_deadline)
                } else {
                    holder.deadline.text = res.getString(R.string.next_deadline_dd, createDateString(list.getNextDeadline()))
                }
                holder.done.text = String.format("%d/%d", list.getDoneTodos(), list.getSize())
                holder.urgency.setBackgroundColor(getDeadlineColor(contextActivity,
                    list.getDeadlineColor(PreferenceMgr.getDefaultReminderTimeSpan(contextActivity))))
            }
        }
    }

    override fun getItemCount(): Int {
        return allLists?.size ?: 0
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.itemView.setOnLongClickListener(null)
        super.onViewRecycled(holder)
    }

    fun updateList(todoLists: List<TodoList>?) {
        allLists = todoLists
    }

    inner class ViewHolder(view: View) :
        RecyclerView.ViewHolder(view), View.OnClickListener, OnCreateContextMenuListener {
        var title: TextView
        var deadline: TextView
        var done: TextView
        var urgency: View

        init {
            title = view.findViewById(R.id.tv_todo_list_title)
            deadline = view.findViewById(R.id.tv_todo_list_next_deadline)
            done = view.findViewById(R.id.tv_todo_list_status)
            urgency = view.findViewById(R.id.v_urgency_indicator)
            view.setOnClickListener(this)
            view.setOnCreateContextMenuListener(this)
        }

        override fun onClick(v: View) {
            val lists = allLists
            if (null != lists) {
                val index = lists.size - 1 - bindingAdapterPosition
                if (index >= 0 && index < lists.size) {
                    contextActivity.clickedList = lists[index]
                }
            }
        }

        override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
            // TODO ask touchListener for swipe action
            menu.setHeaderView(getMenuHeader(contextActivity, contextActivity.getString(R.string.select_option)))
            val inflater = contextActivity.menuInflater
            inflater.inflate(R.menu.todo_list_long_click, menu)
        }
    }
}
