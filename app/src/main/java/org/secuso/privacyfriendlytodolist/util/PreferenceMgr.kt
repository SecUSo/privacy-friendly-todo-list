/*
Privacy Friendly To-Do List
Copyright (C) 2017-2024  Sebastian Lutz

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
package org.secuso.privacyfriendlytodolist.util

import android.content.Context
import androidx.preference.PreferenceManager
import org.secuso.privacyfriendlytodolist.R

enum class PrefDataType {
    BOOLEAN, LONG, STRING
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
object PreferenceMgr {
    val ALL_PREFERENCES = mutableMapOf<String, PrefMetaData>()

    // Note: Preference names must match with names in attributes at app/src/main/res/xml/settings.xml
    private val P_IS_FIRST_TIME_LAUNCH = PrefMetaData("isFirstTimeLaunch", PrefDataType.BOOLEAN)
    val P_IS_PIN_ENABLED = PrefMetaData("pref_pin_enabled", PrefDataType.BOOLEAN)
    val P_PIN = PrefMetaData("pref_pin", PrefDataType.STRING, true)
    private val P_DEFAULT_REMINDER_TIME = PrefMetaData("pref_default_reminder_time", PrefDataType.STRING)
    val P_IS_AUTO_PROGRESS = PrefMetaData("pref_progress", PrefDataType.BOOLEAN)
    val P_IS_NOTIFICATION_SOUND = PrefMetaData("notify", PrefDataType.BOOLEAN)
    val P_GROUP_BY_PRIORITY = PrefMetaData("PRIORITY", PrefDataType.BOOLEAN)
    val P_SORT_BY_DEADLINE = PrefMetaData("DEADLINE", PrefDataType.BOOLEAN)
    val P_TASK_FILTER = PrefMetaData("FILTER", PrefDataType.STRING)
    private val P_SNOOZE_DURATION = PrefMetaData("pref_snooze_duration", PrefDataType.STRING)
    val P_APP_THEME = PrefMetaData("pref_app_theme", PrefDataType.STRING)

    fun setFirstTimeLaunch(context: Context, isFirstTimeLaunch: Boolean) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefs.edit()
        editor.putBoolean(P_IS_FIRST_TIME_LAUNCH.name, isFirstTimeLaunch)
        editor.apply()
    }

    fun isFirstTimeLaunch(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(P_IS_FIRST_TIME_LAUNCH.name, true)
    }

    fun loadDefaultValues(context: Context) {
        // readAgain needs to be true to also get default values for settings that were recently
        // added to the app. This does not delete changes at the current settings set by the user.
        PreferenceManager.setDefaultValues(context, R.xml.settings, true)
    }

    /**
     * Returns the default reminder time span, as it is stored in the app preference.
     *
     * The default reminder time is a relative time span in seconds (e.g. 86400s == 1 day).
     * In opposite to the user specified reminder times, which are absolute timestamps.
     *
     * @return The default reminder time span in seconds.
     */
    fun getDefaultReminderTimeSpan(context: Context): Long {
        return getStringPrefAsLong(context, P_DEFAULT_REMINDER_TIME.name)
    }

    /**
     * @return The snooze duration, as it is stored in the app preference.
     */
    fun getSnoozeDuration(context: Context): Long {
        return getStringPrefAsLong(context, P_SNOOZE_DURATION.name)
    }

    /**
     * @return The selected app theme, as it is stored in the app preference.
     * Or null if no preference is set.
     */
    fun getAppTheme(context: Context): String? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(P_APP_THEME.name, null)
    }

    private fun getStringPrefAsLong(context: Context, prefName: String): Long {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        var value = prefs.getString(prefName, null)
        if (null == value) {
            loadDefaultValues(context)
            value = prefs.getString(prefName, null)!!
        }
        return value.toLong()
    }
}
