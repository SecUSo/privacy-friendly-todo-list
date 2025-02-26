/*
Privacy Friendly To-Do List
Copyright (C) 2024-2025  Christian Adams

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
package org.secuso.privacyfriendlytodolist.observer

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import org.secuso.privacyfriendlytodolist.util.LogTag

/**
 * The system preference manager does not currently store a strong reference to the listener.
 * This preference observer handles this correctly and so provides a simple mechanism to register
 * listener for preference changes.
 */
object PreferenceObserver {
    private val TAG = LogTag.create(this::class.java)

    fun interface OnPreferenceChangeListener {
        fun onPreferenceChange(sharedPreferences: SharedPreferences, key: String?)
    }

    private val preferenceChangeListeners = ArrayList<OnPreferenceChangeListener>()

    fun registerPreferenceChangeListener(preferenceChangeListener: OnPreferenceChangeListener) {
        if (!preferenceChangeListeners.contains(preferenceChangeListener)) {
            preferenceChangeListeners.add(preferenceChangeListener)
        }
    }

    fun unregisterPreferenceChangeListener(preferenceChangeListener: OnPreferenceChangeListener) {
        preferenceChangeListeners.remove(preferenceChangeListener)
    }

    private val sharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            Log.d(TAG, "Preference $key changed.")
            for (listener in preferenceChangeListeners) {
                listener.onPreferenceChange(sharedPreferences, key)
            }
        }

    fun initialize(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        /*
         * From registerOnSharedPreferenceChangeListener():
         * "Caution: The preference manager does not currently store a strong reference to the listener.
         * You must store a strong reference to the listener, or it will be susceptible to garbage collection.
         * We recommend you keep a reference to the listener in the instance data of an object
         * that will exist as long as you need the listener."
         */
        prefs.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
    }
}