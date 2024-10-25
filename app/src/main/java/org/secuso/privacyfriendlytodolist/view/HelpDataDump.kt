/*
Privacy Friendly To-Do List
Copyright (C) 2024  SECUSO (www.secuso.org)

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
import org.secuso.privacyfriendlytodolist.R

/**
 * Created by sebbi on 28.02.2018.
 *
 * This class handles the FAQ-style help menu.
 */
class HelpDataDump(private val context: Context) {
    /** Important: The map preserves the entry iteration order. */
    private val data = mutableMapOf<String, List<String>>()

    val dataGeneral: Map<String, List<String>>
        get() = data

    init {
        addGroup(R.string.help_overview_heading, R.string.help_intro)
        addGroup(R.string.help_group_lists, R.string.help_todo_lists, R.string.help_subtasks, R.string.help_deadline_reminder)
        addGroup(R.string.help_group_app, R.string.help_pin, R.string.help_sound)
        addGroup(R.string.help_group_privacy, R.string.help_privacy,
            R.string.help_permission_receive_boot_completed,
            R.string.help_permission_post_notifications,
            R.string.help_permission_schedule_exact_alarm)
        addGroup(R.string.help_group_data_backup, R.string.help_data_backup)
        addGroup(R.string.help_group_export_import, R.string.help_export, R.string.help_import)
        addGroup(R.string.help_group_widget, R.string.help_widget)
    }

    private fun addGroup(groupId: Int, vararg entryIds: Int) {
        val group = ArrayList<String>()
        for (entryId in entryIds) {
            group.add(context.resources.getString(entryId))
        }
        data[context.resources.getString(groupId)] = group
    }
}
