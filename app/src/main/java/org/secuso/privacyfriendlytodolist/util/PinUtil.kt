package org.secuso.privacyfriendlytodolist.util

import android.content.Context
import androidx.preference.PreferenceManager

object PinUtil {
    @JvmStatic
    fun hasPin(context: Context): Boolean {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        val prefPinEnabled = pref.getBoolean(PreferenceMgr.P_IS_PIN_ENABLED.name, false)
        val prefPin: String? = pref.getString(PreferenceMgr.P_PIN.name, null)
        // pin activated and valid?
        return prefPinEnabled && prefPin != null && prefPin.length >= 4
    }
}