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
package org.secuso.privacyfriendlytodolist.tutorial

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import org.secuso.privacyfriendlytodolist.R

/**
 * Created by Sebastian Lutz on 06.12.2017.
 *
 * This class is used to help create Preference hierarchies for the tutorial.
 */
class PrefManager(context: Context) {
    private val pref: SharedPreferences
    private val editor: SharedPreferences.Editor

    init {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        editor = pref.edit()
    }

    var isFirstTimeLaunch: Boolean
        get() = pref.getBoolean(IS_FIRST_TIME_LAUNCH, true)
        set(isFirstTime) {
            editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime)
            editor.commit()
        }

    fun setFirstTimeValues(context: Context) {
        PreferenceManager.setDefaultValues(context, R.xml.settings, false)
    }

    companion object {
        // Shared preferences file name
        private const val PREF_NAME = "privacy_friendly_todo"
        private const val IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch"
    }
}
