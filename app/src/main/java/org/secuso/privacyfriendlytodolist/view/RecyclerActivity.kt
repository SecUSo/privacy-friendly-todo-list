/*
Privacy Friendly To-Do List
Copyright (C) 2017-2025  Sebastian Lutz

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
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ExpandableListView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.ModelServices
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.util.Helper
import org.secuso.privacyfriendlytodolist.util.LogTag
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
    private lateinit var exLv: ExpandableListView
    private var expandableTodoTaskAdapter: ExpandableTodoTaskAdapter? = null
    private var contextMenuTodoTask: TodoTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ViewModelProvider(this)[LifecycleViewModel::class.java]
        model = viewModel.model
        setContentView(R.layout.activity_recycler)
        exLv = findViewById(R.id.recycle_bin_tasks)
        tv = findViewById(R.id.bin_empty)
        val toolbar: Toolbar = findViewById(R.id.toolbar_recycle_bin)
        setSupportActionBar(toolbar)
        val supportActionBarCopy = supportActionBar
        if (supportActionBarCopy != null) {
            supportActionBarCopy.setHomeAsUpIndicator(R.drawable.ic_arrow_left_white_24dp)
            supportActionBarCopy.setDisplayHomeAsUpEnabled(true)
            supportActionBarCopy.setDisplayShowHomeEnabled(true)
        }

        onBackPressedDispatcher.addCallback(this) {
            val intent = Intent(this@RecyclerActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }

        updateAdapter()
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val todoTask = contextMenuTodoTask
        if (null != todoTask) {
            if (item.itemId == R.id.restore) {
                model.setTaskAndSubtasksInRecycleBin(todoTask, false) { counter ->
                    if (counter.first > 0) {
                        updateAdapter()
                    } else {
                        Log.w(TAG, "Failed to restore $todoTask from recycle bin.")
                    }
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
                MaterialAlertDialogBuilder(this).apply {
                    setMessage(R.string.alert_clear)
                    setCancelable(true)
                    setPositiveButton(R.string.yes) { dialog, which ->
                        model.clearRecycleBin { counter ->
                            dialog.cancel()
                            updateAdapter()
                        }
                    }
                    setNegativeButton(R.string.no) { dialog, which ->
                        dialog.cancel()
                    }
                    show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = this.menuInflater
        val menuHeader = Helper.getMenuHeader(layoutInflater, v, R.string.select_option)
        menu.setHeaderView(menuHeader)
        inflater.inflate(R.menu.deleted_task_context, menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.recycle_bin_clear, menu)
        return true
    }

    private fun updateAdapter() {
        model.getRecycleBin { todoTasks ->
            expandableTodoTaskAdapter = ExpandableTodoTaskAdapter(this, model, todoTasks, true)
            expandableTodoTaskAdapter!!.setOnTaskMenuClickListener { todoTask: TodoTask ->
                contextMenuTodoTask = todoTask
                registerForContextMenu(exLv)
                exLv.isLongClickable = false
                openContextMenu(exLv)
            }
            exLv.setAdapter(expandableTodoTaskAdapter)
            exLv.emptyView = tv
        }
    }

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
    }
}
