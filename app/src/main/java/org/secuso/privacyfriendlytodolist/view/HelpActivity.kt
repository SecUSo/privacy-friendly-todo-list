/*
Privacy Friendly To-Do List
Copyright (C) 2018-2025  Sebastian Lutz

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

import android.os.Bundle
import android.view.MenuItem
import android.widget.ExpandableListView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.secuso.privacyfriendlytodolist.R

/**
 * Created by Sebastian Lutz on 15.01.2018
 *
 * Activity that gives the user some help regarding the handling of the app.
 */
class HelpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        val helpDataDump = HelpDataDump(this)
        val expandableListDetail = helpDataDump.dataGeneral
        val expandableListTitleGeneral: List<String> = ArrayList(expandableListDetail.keys)
        val expandableListAdapter = ExpandableListAdapter(this, expandableListTitleGeneral, expandableListDetail)
        val generalExpandableListView = findViewById<ExpandableListView>(R.id.generalExpandableListView)
        generalExpandableListView.setAdapter(expandableListAdapter)
        val toolbar: Toolbar = findViewById(R.id.toolbar_help)
        setSupportActionBar(toolbar)
        val supportActionBarCopy = supportActionBar
        if (supportActionBarCopy != null) {
            supportActionBarCopy.setDisplayHomeAsUpEnabled(true)
            supportActionBarCopy.setDisplayShowHomeEnabled(true)
            supportActionBarCopy.setHomeAsUpIndicator(R.drawable.ic_arrow_left_white_24dp)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}