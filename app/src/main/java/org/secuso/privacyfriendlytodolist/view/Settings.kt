/*
Privacy Friendly To-Do List
Copyright (C) 2018-2024  Sebastian Lutz

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

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.SwitchPreference
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.util.PreferenceMgr

/**
 * Created by Sebastian Lutz on 15.03.2018
 *
 *
 * Activity that can enable/disable particular functionalities.
 */
class Settings : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)
        val toolbar: Toolbar = findViewById(R.id.toolbarr)
        setSupportActionBar(toolbar)
        val supportActionBarCopy = supportActionBar
        if (supportActionBarCopy != null) {
            supportActionBarCopy.setDisplayHomeAsUpEnabled(true)
            supportActionBarCopy.setDisplayShowHomeEnabled(true)
            //final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            //upArrow.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP);
            //getSupportActionBar().setHomeAsUpIndicator(upArrow);
        }
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, MyPreferenceFragment())
        transaction.commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    class MyPreferenceFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {
        private var ignoreChanges = false

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.settings)

            // initializes
            initSummary(preferenceScreen)
        }

        private fun initSummary(p: Preference) {
            if (p is PreferenceGroup) {
                for (i in 0 until p.preferenceCount) {
                    initSummary(p.getPreference(i))
                }
            } else {
                updatePrefSummary(p)
            }
        }

        private fun updatePrefSummary(p: Preference?) {
            (p as? ListPreference)?.setSummary(p.getEntry())
        }

        override fun onResume() {
            super.onResume()

            val sharedPreferences = preferenceManager.getSharedPreferences()!!
            sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onPause() {
            // uncheck pin if pin is invalid
            val sharedPreferences = preferenceManager.getSharedPreferences()!!
            val pinEnabled = sharedPreferences.getBoolean(PreferenceMgr.P_IS_PIN_ENABLED.name, false)
            if (pinEnabled) {
                val pin = sharedPreferences.getString(PreferenceMgr.P_PIN.name, null)
                if (!isPinValid(pin)) {
                    // pin invalid: uncheck
                    ignoreChanges = true
                    findPreference<SwitchPreference>(PreferenceMgr.P_IS_PIN_ENABLED.name)!!.setChecked(false)
                    ignoreChanges = false
                }
            }
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
            super.onPause()
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
            if (!ignoreChanges) {
                if (key == PreferenceMgr.P_PIN.name) {
                    val pin = sharedPreferences.getString(key, null)
                    if (pin != null) {
                        if (!isPinValid(pin)) {
                            ignoreChanges = true
                            findPreference<EditTextPreference>(PreferenceMgr.P_PIN.name)!!.setText("")
                            ignoreChanges = false
                            Toast.makeText(activity, getString(R.string.invalid_pin), Toast.LENGTH_LONG).show()
                        }
                    }
                } else if (key == PreferenceMgr.P_IS_PIN_ENABLED.name) {
                    val pinEnabled = sharedPreferences.getBoolean(PreferenceMgr.P_IS_PIN_ENABLED.name, false)
                    if (pinEnabled) {
                        ignoreChanges = true
                        findPreference<EditTextPreference>(PreferenceMgr.P_PIN.name)!!.setText("")
                        ignoreChanges = false
                    }
                }
            }
            updatePrefSummary(findPreference(key!!))
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        super.onBackPressed()
    }

    companion object {
        private const val MINIMAL_PIN_LENGTH = 4
        private const val MAXIMAL_PIN_LENGTH = 32

        /**
         * All the XML attributes like
         *         android:inputType="number|numberPassword|numberDecimal"
         *         android:digits="0123456789"
         *         android:maxLength="32"
         * do not work for EditTextPreference.
         * See [EditTextPreference. Attribute "android:inputType" ignored](https://issuetracker.google.com/issues/118522177)
         * So do the full pin check here in the code.
         */
        private fun isPinValid(pin: String?): Boolean {
            var isValid = true
            if (null != pin && pin.length >= MINIMAL_PIN_LENGTH && pin.length <= MAXIMAL_PIN_LENGTH) {
                for (c in pin) {
                    if (c < '0' || c > '9') {
                        isValid = false
                        break
                    }
                }
            } else {
                isValid = false
            }
            return isValid
        }
    }
}
