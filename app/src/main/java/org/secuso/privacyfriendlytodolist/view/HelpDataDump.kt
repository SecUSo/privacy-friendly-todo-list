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
    val dataGeneral: LinkedHashMap<String, List<String>>
        get() {
            val expandableListDetail = LinkedHashMap<String, List<String>>()

            val general: MutableList<String> = ArrayList()
            general.add(context.resources.getString(R.string.help_intro))
            expandableListDetail[context.resources.getString(R.string.help_overview_heading)] = general

            val features: MutableList<String> = ArrayList()
            features.add(context.resources.getString(R.string.help_todo_lists))
            features.add(context.resources.getString(R.string.help_subtasks))
            features.add(context.resources.getString(R.string.help_deadline_reminder))
            expandableListDetail[context.resources.getString(R.string.help_group_lists)] = features

            val settings: MutableList<String> = ArrayList()
            settings.add(context.resources.getString(R.string.help_pin))
            settings.add(context.resources.getString(R.string.help_sound))
            expandableListDetail[context.resources.getString(R.string.help_group_app)] = settings

            val permissions: MutableList<String> = ArrayList()
            permissions.add(context.resources.getString(R.string.help_permissions))
            expandableListDetail[context.resources.getString(R.string.help_group_privacy)] = permissions

            val dataBackup: MutableList<String> = ArrayList()
            dataBackup.add(context.resources.getString(R.string.help_data_backup))
            expandableListDetail[context.resources.getString(R.string.help_group_data_backup)] = dataBackup

            val widget: MutableList<String> = ArrayList()
            widget.add(context.resources.getString(R.string.help_widget))
            expandableListDetail["Widget"] = widget

            return expandableListDetail
        }
}
