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
import org.secuso.privacyfriendlytodolist.R

/**
 * Created by sebbi on 28.02.2018.
 *
 * This class handles the FAQ-style help menu.
 */
class HelpDataDump(private val context: Context) {
    /** Using LinkedHashMap to keep order of groups. */
    private val data = LinkedHashMap<String, List<String>>()

    val dataGeneral: Map<String, List<String>>
        get() = data

    init {
        addGroup(R.string.help_overview_heading, R.string.help_intro)
        addGroup(R.string.help_group_lists, R.string.help_todo_lists, R.string.help_subtasks, R.string.help_deadline_reminder)
        addGroup(R.string.help_group_app, R.string.help_pin, R.string.help_sound)
        addGroup(R.string.help_group_privacy, R.string.help_permissions)
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
