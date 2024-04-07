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
package org.secuso.privacyfriendlytodolist.util

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import org.secuso.privacyfriendlytodolist.R

enum class PrefDataType {
    BOOLEAN, STRING
}

class PrefMetaData(
    val name: String,
    val dataType: PrefDataType,
    val excludeFromBackup: Boolean = false) {

    init {
        PreferenceMgr.ALL_PREFERENCES[name] = this
    }

    override fun toString(): String {
        return name
    }
}

/**
 * Created by Sebastian Lutz on 06.12.2017.
 *
 * This class is used to help create Preference hierarchies for the tutorial.
 */
class PreferenceMgr(context: Context) {
    private val pref: SharedPreferences
    private val editor: SharedPreferences.Editor

    init {
        pref = context.getSharedPreferences(PREF_FILENAME, Context.MODE_PRIVATE)
        editor = pref.edit()
    }

    var isFirstTimeLaunch: Boolean
        get() = pref.getBoolean(P_IS_FIRST_TIME_LAUNCH.name, true)
        set(isFirstTime) {
            editor.putBoolean(P_IS_FIRST_TIME_LAUNCH.name, isFirstTime)
            editor.commit()
        }

    fun setFirstTimeValues(context: Context) {
        PreferenceManager.setDefaultValues(context, R.xml.settings, false)
    }

    companion object {
        // Shared preferences file name
        private const val PREF_FILENAME = "privacy_friendly_todo"

        val ALL_PREFERENCES = HashMap<String, PrefMetaData>()

        // Note: Preference names must match with names in attributes at app/src/main/res/xml/settings.xml
        val P_IS_FIRST_TIME_LAUNCH = PrefMetaData("isFirstTimeLaunch", PrefDataType.BOOLEAN)
        val P_IS_PIN_ENABLED = PrefMetaData("pref_pin_enabled", PrefDataType.BOOLEAN)
        val P_PIN = PrefMetaData("pref_pin", PrefDataType.STRING, true)
        val P_DEFAULT_REMINDER_TIME = PrefMetaData("pref_default_reminder_time", PrefDataType.STRING)
        val P_IS_AUTO_PROGRESS = PrefMetaData("pref_progress", PrefDataType.BOOLEAN)
        val P_IS_NOTIFICATION_SOUND = PrefMetaData("notify", PrefDataType.BOOLEAN)
        val P_GROUP_BY_PRIORITY = PrefMetaData("PRIORITY", PrefDataType.BOOLEAN)
        val P_SORT_BY_DEADLINE = PrefMetaData("DEADLINE", PrefDataType.BOOLEAN)
        val P_TASK_FILTER = PrefMetaData("FILTER", PrefDataType.STRING)
    }
}
