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

import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ExpandableListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.ModelServices
import org.secuso.privacyfriendlytodolist.util.Helper.getMenuHeader
import org.secuso.privacyfriendlytodolist.viewmodel.LifecycleViewModel

/**
 * Created by Sebastian Lutz on 20.12.2017.
 *
 * This Activity handles deleted tasks in a kind of recycle bin.
 */
@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class RecyclerActivity : AppCompatActivity() {
    private lateinit var model: ModelServices
    private lateinit var tv: TextView
    private lateinit var lv: ExpandableListView
    private var expandableTodoTaskAdapter: ExpandableTodoTaskAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ViewModelProvider(this)[LifecycleViewModel::class.java]
        model = viewModel.model
        setContentView(R.layout.activity_recycler)
        lv = findViewById(R.id.recycle_bin_tasks)
        tv = findViewById(R.id.bin_empty)
        val toolbar: Toolbar = findViewById(R.id.toolbar_recycle_bin)
        toolbar.setTitle(R.string.bin_toolbar)
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
        setSupportActionBar(toolbar)
        val supportActionBarCopy = supportActionBar
        if (supportActionBarCopy != null) {
            supportActionBarCopy.setHomeAsUpIndicator(R.drawable.arrow)
            supportActionBarCopy.setDisplayHomeAsUpEnabled(true)
            supportActionBarCopy.setDisplayShowHomeEnabled(true)
        }
        updateAdapter()
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val longClickedTodo = expandableTodoTaskAdapter!!.longClickedTodo
        if (null != longClickedTodo) {
            val todoTask = longClickedTodo.left
            if (item.itemId == R.id.restore) {
                model.setTaskAndSubtasksInRecycleBin(todoTask, false) { counter: Int? ->
                    updateAdapter()
                }
            }
        }
        return super.onContextItemSelected(item)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
                return true
            }

            R.id.btn_clear -> {
                val builder1 = AlertDialog.Builder(this)
                builder1.setMessage(R.string.alert_clear)
                builder1.setCancelable(true)
                builder1.setPositiveButton(R.string.yes) { dialog, which ->
                    model.clearRecycleBin { counter: Int? ->
                        dialog.cancel()
                        updateAdapter()
                    }
                }
                builder1.setNegativeButton(R.string.no) { dialog, which -> dialog.cancel() }
                val alert = builder1.create()
                alert.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = this.menuInflater
        menu.setHeaderView(getMenuHeader(baseContext, baseContext.getString(R.string.select_option)))
        inflater.inflate(R.menu.deleted_task_long_click, menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.recycle_bin_clear, menu)
        return true
    }

    private fun updateAdapter() {
        model.getRecycleBin { todoTasks ->
            expandableTodoTaskAdapter = ExpandableTodoTaskAdapter(this, model, todoTasks)
            lv.setAdapter(expandableTodoTaskAdapter)
            lv.setEmptyView(tv)
            lv.setOnItemLongClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
                val groupPosition = ExpandableListView.getPackedPositionGroup(id)
                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    val childPosition = ExpandableListView.getPackedPositionChild(id)
                    expandableTodoTaskAdapter!!.setLongClickedSubtaskByPos(groupPosition, childPosition)
                } else {
                    expandableTodoTaskAdapter!!.setLongClickedTaskByPos(groupPosition)
                }
                registerForContextMenu(lv)
                false
            }
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)

        super.onBackPressed()
    }
}
