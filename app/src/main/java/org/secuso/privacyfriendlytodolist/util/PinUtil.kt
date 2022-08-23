package org.secuso.privacyfriendlytodolist.util

import android.content.Context
import android.preference.PreferenceManager

object PinUtil {
    @JvmStatic
    fun hasPin(context: Context): Boolean {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        // pin activated and valid?
        return pref.getBoolean("pref_pin_enabled", false)
            && pref.getString("pref_pin", null) != null
            && pref.getString("pref_pin", "")!!.length >= 4
    }
}