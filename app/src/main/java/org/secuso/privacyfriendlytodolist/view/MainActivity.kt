/*
Privacy Friendly To-Do List
Copyright (C) 2024  SECUSO (www.secuso.org)

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

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.AdapterView
import android.widget.ExpandableListView
import android.widget.ExpandableListView.ExpandableListContextMenuInfo
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.Model
import org.secuso.privacyfriendlytodolist.model.ModelObserver
import org.secuso.privacyfriendlytodolist.model.ModelServices
import org.secuso.privacyfriendlytodolist.model.TodoSubtask
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.model.Tuple
import org.secuso.privacyfriendlytodolist.util.AlarmMgr
import org.secuso.privacyfriendlytodolist.util.Helper
import org.secuso.privacyfriendlytodolist.util.Helper.getMenuHeader
import org.secuso.privacyfriendlytodolist.util.LogTag
import org.secuso.privacyfriendlytodolist.util.MarkdownBuilder
import org.secuso.privacyfriendlytodolist.util.NotificationMgr
import org.secuso.privacyfriendlytodolist.util.PinUtil
import org.secuso.privacyfriendlytodolist.util.PreferenceMgr
import org.secuso.privacyfriendlytodolist.view.calendar.CalendarActivity
import org.secuso.privacyfriendlytodolist.view.dialog.PinCallback
import org.secuso.privacyfriendlytodolist.view.dialog.PinDialog
import org.secuso.privacyfriendlytodolist.view.dialog.ProcessTodoListDialog
import org.secuso.privacyfriendlytodolist.view.dialog.ProcessTodoSubtaskDialog
import org.secuso.privacyfriendlytodolist.view.dialog.ProcessTodoTaskDialog
import org.secuso.privacyfriendlytodolist.view.dialog.ResultCallback
import org.secuso.privacyfriendlytodolist.view.widget.TodoListWidget
import org.secuso.privacyfriendlytodolist.viewmodel.LifecycleViewModel
import java.io.StringWriter


/**
 * Created by Sebastian Lutz on 12.03.2018.
 *
 * This Activity handles the navigation and operation on lists and tasks.
 */
@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, ModelObserver {
    // TodoTask administration
    private var exLv: ExpandableListView? = null
    private var emptyView: TextView? = null
    private var expandableTodoTaskAdapter: ExpandableTodoTaskAdapter? = null
    private var initialAlert: TextView? = null
    private var secondAlert: TextView? = null
    private var fabNewTodoTask: FloatingActionButton? = null
    private var model: ModelServices? = null
    private var mPref: SharedPreferences? = null

    // TodoList administration
    private var selectedTodoListId: Int = -1

    // GUI
    private var navigationView: NavigationView? = null
    private var toolbar: Toolbar? = null
    private var drawer: DrawerLayout? = null
    private var drawerToggle: ActionBarDrawerToggle? = null
    private lateinit var exportTasksLauncher: ActivityResultLauncher<Intent>
    private lateinit var importTasksLauncher: ActivityResultLauncher<Intent>

    // Export / Import
    private var exportListId: Int? = null
    private var deleteAllDataBeforeImport = false

    // Others
    private var isUnlocked = false
    private var unlockUntil: Long = -1L
    private var activeListId: Int? = null

    //Pomodoro
    private var pomodoroInstalled = false

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        menuInflater.inflate(R.menu.search, menu)
        menuInflater.inflate(R.menu.add_list, menu)
        val searchItem = menu.findItem(R.id.ac_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                collapseAll()
                expandableTodoTaskAdapter!!.queryString = query
                expandableTodoTaskAdapter!!.notifyDataSetChanged()
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                collapseAll()
                expandableTodoTaskAdapter!!.queryString = query
                expandableTodoTaskAdapter!!.notifyDataSetChanged()
                return false
            }
        })
        val priorityGroup = menu.findItem(R.id.ac_group_by_prio)
        priorityGroup.setChecked(mPref!!.getBoolean(PreferenceMgr.P_GROUP_BY_PRIORITY.name, false))
        val deadlineGroup = menu.findItem(R.id.ac_sort_by_deadline)
        deadlineGroup.setChecked(mPref!!.getBoolean(PreferenceMgr.P_SORT_BY_DEADLINE.name, false))
        return super.onCreateOptionsMenu(menu)
    }

    private fun collapseAll() {
        // collapse all elements on view change.
        // the expandable list view keeps the expanded indices, so other items
        // get expanded, when they get the old expanded index
        val groupCount = expandableTodoTaskAdapter!!.groupCount
        for (i in 0 until groupCount) {
            exLv!!.collapseGroup(i)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        collapseAll()
        when (item.itemId) {
            R.id.ac_add -> {
                startAddListDialog()
            }

            R.id.ac_show_all_tasks -> {
                expandableTodoTaskAdapter!!.filter = ExpandableTodoTaskAdapter.Filter.ALL_TASKS
                expandableTodoTaskAdapter!!.notifyDataSetChanged()
                mPref!!.edit().putString(PreferenceMgr.P_TASK_FILTER.name, "ALL_TASKS").apply()
                return true
            }

            R.id.ac_show_open_tasks -> {
                expandableTodoTaskAdapter!!.filter = ExpandableTodoTaskAdapter.Filter.OPEN_TASKS
                expandableTodoTaskAdapter!!.notifyDataSetChanged()
                mPref!!.edit().putString(PreferenceMgr.P_TASK_FILTER.name, "OPEN_TASKS").apply()
                return true
            }

            R.id.ac_show_completed_tasks -> {
                expandableTodoTaskAdapter!!.filter = ExpandableTodoTaskAdapter.Filter.COMPLETED_TASKS
                expandableTodoTaskAdapter!!.notifyDataSetChanged()
                mPref!!.edit().putString(PreferenceMgr.P_TASK_FILTER.name, "COMPLETED_TASKS").apply()
                return true
            }

            R.id.ac_group_by_prio -> {
                item.isChecked = !item.isChecked
                expandableTodoTaskAdapter!!.isGroupingByPriority = item.isChecked
                mPref!!.edit().putBoolean(PreferenceMgr.P_GROUP_BY_PRIORITY.name, item.isChecked).apply()
            }

            R.id.ac_sort_by_deadline -> {
                item.isChecked = !item.isChecked
                expandableTodoTaskAdapter!!.isSortingByDeadline = item.isChecked
                mPref!!.edit().putBoolean(PreferenceMgr.P_SORT_BY_DEADLINE.name, item.isChecked).apply()
            }

            else -> return super.onOptionsItemSelected(item)
        }
        expandableTodoTaskAdapter!!.notifyDataSetChanged()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate() action: ${intent?.action}, savedInstanceState: ${null != savedInstanceState}, extras: ${intent?.extras?.keySet()?.joinToString()}")
        super.onCreate(savedInstanceState)
        val viewModel = ViewModelProvider(this)[LifecycleViewModel::class.java]
        model = viewModel.model
        if (PreferenceMgr.isFirstTimeLaunch(this)) {
            PreferenceMgr.loadDefaultValues(this)
            startTut()
            finish()
        }
        if (savedInstanceState != null) {
            restore(savedInstanceState)
        } else {
            isUnlocked = false
            unlockUntil = -1L
        }
        setContentView(R.layout.activity_main)
        exLv = findViewById(R.id.exlv_tasks)
        emptyView = findViewById(R.id.tv_empty_view_no_tasks)
        fabNewTodoTask = findViewById(R.id.fab_new_task)
        initialAlert = findViewById(R.id.initial_alert)
        secondAlert = findViewById(R.id.second_alert)
        showHints()
        mPref = PreferenceManager.getDefaultSharedPreferences(this)

        exLv!!.setOnChildClickListener { parent: AdapterView<*>?, view: View?, groupPosition: Int, position: Int, id: Long ->
            expandableTodoTaskAdapter?.onClickSubtask(groupPosition, position)
            return@setOnChildClickListener false
        }
        exLv!!.setOnItemLongClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
            val groupPosition = ExpandableListView.getPackedPositionGroup(id)
            if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                val childPosition = ExpandableListView.getPackedPositionChild(id)
                expandableTodoTaskAdapter?.setLongClickedSubtaskByPos(groupPosition, childPosition)
            } else {
                expandableTodoTaskAdapter?.setLongClickedTaskByPos(groupPosition)
            }
            registerForContextMenu(exLv)
            return@setOnItemLongClickListener false
        }

        fabNewTodoTask!!.setOnClickListener { v: View? ->
            val pt = ProcessTodoTaskDialog(this@MainActivity, activeListId)
            pt.setDialogCallback { todoTask ->
                model!!.saveTodoTaskInDb(todoTask) { counter ->
                    onTaskChange(todoTask)
                    showHints()
                    showTasksOfListOrAllTasks(todoTask.getListId())
                    if (todoTask.hasReminderTime()) {
                        AlarmMgr.checkForPermissions(this)
                    }
                }
            }
            pt.show()
        }

        exportTasksLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data
                if (null != uri) {
                    doExport(uri)
                } else {
                    Log.e(TAG, "CSV export failed: Uri is null.")
                    Toast.makeText(baseContext, getString(R.string.export_failed), Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.i(TAG, "CSV export aborted by user. Result: ${result.resultCode}")
            }
        }
        importTasksLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data
                if (null != uri) {
                    doImport(uri)
                } else {
                    Log.e(TAG, "CSV import failed: Uri is null.")
                    Toast.makeText(baseContext, getString(R.string.import_failed), Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.i(TAG, "CSV import aborted by user. Result: ${result.resultCode}")
            }
        }

        if (intent.getIntExtra(COMMAND, -1) == COMMAND_UPDATE) {
            updateTodoFromPomodoro()
        }
    }

    private fun doExport(uri: Uri) {
        Log.i(TAG, "CSV export to $uri starts. List ID: $exportListId.")
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val hasAutoProgress = prefs.getBoolean(PreferenceMgr.P_IS_AUTO_PROGRESS.name, false)
        model!!.exportCSVData(exportListId, hasAutoProgress, uri) { errorMessage ->
            val id = if (null == errorMessage) {
                R.string.export_succeeded
            } else {
                Log.e(TAG, "CSV export failed: $errorMessage")
                R.string.export_failed
            }
            Toast.makeText(baseContext, getString(id), Toast.LENGTH_SHORT).show()
        }
    }

    private fun doImport(uri: Uri) {
        Log.i(TAG, "CSV import from $uri starts. Delete existing data: $deleteAllDataBeforeImport")
        model!!.importCSVData(deleteAllDataBeforeImport, uri) { errorMessage ->
            showHints()
            addTodoListsToNavigationMenu()

            if (null == errorMessage) {
                Toast.makeText(baseContext, getString(R.string.import_succeeded), Toast.LENGTH_SHORT).show()
                // Renew alarms. Might be a due- or overdue-task was imported.
                AlarmMgr.setAlarmForAllTasks(this)
            } else {
                Log.e(TAG, "CSV import failed: $errorMessage")
                AlertDialog.Builder(this).apply {
                    setTitle(R.string.import_failed)
                    setMessage(errorMessage)
                    setPositiveButton(R.string.ok) { dialog, which -> }
                    show()
                }
            }
        }
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_IS_UNLOCKED, isUnlocked)
        outState.putLong(KEY_UNLOCK_UNTIL, unlockUntil)
        if (activeListId != null) {
            outState.putByte(KEY_ACTIVE_LIST_IS_DUMMY, 0.toByte())
            outState.putInt(KEY_ACTIVE_LIST_ID, activeListId!!)
        } else {
            outState.putByte(KEY_ACTIVE_LIST_IS_DUMMY, 1.toByte())
        }
        isUnlocked = false
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        restore(savedInstanceState)
    }

    private fun restore(savedInstanceState: Bundle) {
        isUnlocked = savedInstanceState.getBoolean(KEY_IS_UNLOCKED)
        unlockUntil = savedInstanceState.getLong(KEY_UNLOCK_UNTIL)
        activeListId = if (savedInstanceState.getByte(KEY_ACTIVE_LIST_IS_DUMMY).toInt() != 0)
            null else savedInstanceState.getInt(KEY_ACTIVE_LIST_ID)
    }

    private fun authAndGuiInit() {
        if (PinUtil.hasPin(this) && !isUnlocked && (unlockUntil == -1L || System.currentTimeMillis() > unlockUntil)) {
            clearTaskListView()
            isUnlocked = false
            unlockUntil = -1L
            val dialog = PinDialog(this, true)
            dialog.setDialogCallback(object : PinCallback {
                override fun accepted() {
                    isUnlocked = true
                    unlockUntil = System.currentTimeMillis() + UNLOCK_PERIOD
                    initializeActivity()
                }

                override fun declined() {
                    finishAffinity()
                }

                override fun resetApp() {
                    PreferenceManager.getDefaultSharedPreferences(this@MainActivity).edit().clear().apply()
                    model!!.deleteAllData()
                    val intent = Intent(this@MainActivity, MainActivity::class.java)
                    dialog.dismiss()
                    startActivity(intent)
                }
            })
            dialog.show()
        } else {
            initializeActivity()
        }
    }

    private fun initializeActivity() {
        // toolbar setup
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // side menu setup
        drawer = findViewById(R.id.drawer_layout)
        if (null != drawerToggle) {
            drawer!!.removeDrawerListener(drawerToggle!!)
        }
        drawerToggle = ActionBarDrawerToggle(this, drawer, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer!!.addDrawerListener(drawerToggle!!)
        drawerToggle!!.syncState()

        navigationView = findViewById(R.id.nav_view)
        navigationView!!.setNavigationItemSelectedListener(this)

        addTodoListsToNavigationMenu()

        var tasksGetDisplayed = false
        val extras = intent.extras
        if (extras != null) {
            tasksGetDisplayed = processExtras(extras)
        }
        if (!tasksGetDisplayed) {
            showTasksOfListOrAllTasks(activeListId)
        }
    }

    private fun processExtras(extras: Bundle): Boolean {
        var tasksGetDisplayed = false

        // check if app was started by clicking on a reminding notification
        if (extras.containsKey(NotificationMgr.EXTRA_NOTIFICATION_TASK_ID)) {
            tasksGetDisplayed = true
            val dueTaskId = extras.getInt(NotificationMgr.EXTRA_NOTIFICATION_TASK_ID)
            model!!.getTaskById(dueTaskId) { dueTask ->
                var listId: Int? = null
                if (null != dueTask) {
                    listId = dueTask.getListId()
                    Log.d(TAG, "Reminding notification started MainActivity for task $dueTask and its list $listId.")
                } else {
                    Log.w(TAG, "Task with ID $dueTaskId not found after click on reminding notification.")
                }
                showTasksOfListOrAllTasks(listId)
            }
        }

        val listIdFromWidget = extras.getString(TodoListWidget.EXTRA_WIDGET_LIST_ID, null)
        if (null != listIdFromWidget) {
            tasksGetDisplayed = true
            Log.d(TAG, "Widget started MainActivity to show tasks of list $listIdFromWidget.")
            val listId = if (listIdFromWidget != "null") listIdFromWidget.toInt() else null
            showTasksOfListOrAllTasks(listId)
        }

        return tasksGetDisplayed
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        super.onDestroy()
    }

    private fun clearTaskListView() {
        toolbar?.setTitle(R.string.home)
        exLv?.setAdapter(ExpandableTodoTaskAdapter(
            this, model!!, ArrayList(0), false))
    }

    private fun uncheckNavigationEntries() {
        // uncheck all navigation entries
        if (navigationView != null) {
            val size = navigationView!!.menu.size()
            for (i in 0 until size) {
                navigationView!!.menu.getItem(i).setChecked(false)
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.menu_home -> {
                showAllTasks()
            }
            R.id.menu_calendar_view -> {
                uncheckNavigationEntries()
                val intent = Intent(this, CalendarActivity::class.java)
                unlockUntil = System.currentTimeMillis() + UNLOCK_PERIOD
                startActivity(intent)
            }
            R.id.nav_recycle_bin -> {
                uncheckNavigationEntries()
                val intent = Intent(this, RecyclerActivity::class.java)
                unlockUntil = System.currentTimeMillis() + UNLOCK_PERIOD
                startActivity(intent)
            }
            R.id.nav_settings -> {
                uncheckNavigationEntries()
                val intent = Intent(this, Settings::class.java)
                unlockUntil = System.currentTimeMillis() + UNLOCK_PERIOD
                startActivity(intent)
            }
            R.id.nav_share -> {
                shareAllTasks()
            }
            R.id.nav_export -> {
                initiateTaskExport()
            }
            R.id.nav_import -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/comma-separated-values"
                }
                AlertDialog.Builder(this).apply {
                    setTitle(R.string.import_question_title)
                    setMessage(R.string.import_question_text)
                    setPositiveButton(R.string.delete_existing_data) { dialog, which ->
                        deleteAllDataBeforeImport = true
                        importTasksLauncher.launch(intent)
                    }
                    setNegativeButton(R.string.keep_existing_data) { dialog, which ->
                        deleteAllDataBeforeImport = false
                        importTasksLauncher.launch(intent)
                    }
                    setNeutralButton(R.string.abort) { dialog, which ->
                    }
                    show()
                }
            }
            R.id.nav_tutorial -> {
                uncheckNavigationEntries()
                val intent = Intent(this, TutorialActivity::class.java)
                unlockUntil = System.currentTimeMillis() + UNLOCK_PERIOD
                startActivity(intent)
            }
            R.id.nav_help -> {
                uncheckNavigationEntries()
                val intent = Intent(this, HelpActivity::class.java)
                unlockUntil = System.currentTimeMillis() + UNLOCK_PERIOD
                startActivity(intent)
            }
            R.id.nav_about -> {
                uncheckNavigationEntries()
                val intent = Intent(this, AboutActivity::class.java)
                unlockUntil = System.currentTimeMillis() + UNLOCK_PERIOD
                startActivity(intent)
            }
            else -> {
                showTasksOfList(item.itemId)
            }
        }
        val drawer: DrawerLayout = findViewById(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onStop() {
        // Call order if API level < 28: 1. onSaveInstanceState(), 2. onStop()
        // Beginning with API level 28 this call order was reversed.
        // To not reset the flag before saving it, 'isUnlocked = false' gets done at the end of onSaveInstanceState()
        // for all API levels. Here too, if API level is < 28 (to keep old behavior (don't know if necessary)).
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            isUnlocked = false
        }
        super.onStop()
    }

    override fun onTodoDataChangedFromOutside(context: Context) {
        Log.i(TAG, "Refreshing task list view because data model was changed from outside.")
        showTasksOfListOrAllTasks(activeListId)
    }

    override fun onPause() {
        Log.d(TAG, "onPause()")
        super.onPause()

        if (PinUtil.hasPin(this)) {
            // Clear task list view so that tasks are not visible before the right pin was entered.
            clearTaskListView()
        }

        Model.unregisterModelObserver(this)
    }

    override fun onNewIntent(intent: Intent?) {
        Log.d(TAG, "onNewIntent() action: ${intent?.action}, extras: ${intent?.extras?.keySet()?.joinToString()}")
        super.onNewIntent(intent)
        // Store intent to process it via onResume() and authAndGuiInit().
        setIntent(intent)
    }

    override fun onResume() {
        Log.d(TAG, "onResume() action: ${intent?.action}, extras: ${intent?.extras?.keySet()?.joinToString()}")
        super.onResume()

        Model.registerModelObserver(this)

        // Check if Pomodoro is installed
        pomodoroInstalled = checkIfPomodoroInstalled()

        // isUnlocked might be false when returning from another activity. See onUserLeaveHint().
        // Set to true if the unlock period was not expired:
        if (!isUnlocked && unlockUntil != -1L && System.currentTimeMillis() <= unlockUntil) {
            isUnlocked = true
        }
        unlockUntil = -1L

        authAndGuiInit()
    }

    override fun onUserLeaveHint() {
        // prevents unlocking the app by rotating while the app is inactive and then returning
        isUnlocked = false
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        val drawer: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else if (null != activeListId) {
            showAllTasks()
        } else {
            super.onBackPressed()
        }
    }

    private fun onTaskChange(todoTask: TodoTask) {
        // TODO add more granularity: You don't need to change the alarm if the name or the description of the task were changed. You actually need this perform the following steps if the reminder time or the "done" status were modified.
        Log.d(TAG, "$todoTask changed. Setting its alarm.")
        // Direct user action lead to task change. So no need to set alarm if it is in the past.
        // User should see that.
        AlarmMgr.setAlarmForTask(this, todoTask)
    }

    // Adds To do-Lists to the navigation-drawer
    private fun addTodoListsToNavigationMenu() {
        model!!.getAllToDoListNames { allTodoListNames ->
            val navView: NavigationView = findViewById(R.id.nav_view)
            val navMenu = navView.menu
            navMenu.removeGroup(R.id.menu_group_todo_lists)
            for (entry in allTodoListNames.entries) {
                val item = navMenu.add(R.id.menu_group_todo_lists, entry.key, 1, entry.value)
                item.setCheckable(true)
                item.setIcon(R.drawable.ic_label_black_24dp)
                item.setActionView(R.layout.list_action_view)
                val actionButton: ImageButton = item.actionView!!.findViewById(R.id.action_button)
                actionButton.tag = entry.key
                actionButton.setOnClickListener {
                    registerForContextMenu(actionButton)
                    openContextMenu(actionButton)
                    unregisterForContextMenu(actionButton)
                }
            }
        }
    }

    // Method to add a new To do-List
    private fun startAddListDialog() {
        val pl = ProcessTodoListDialog(this)
        pl.setDialogCallback { todoList ->
            model!!.saveTodoListInDb(todoList) { counter ->
                showHints()
                addTodoListsToNavigationMenu()
                Log.i(TAG, "List '${todoList.getName()}' with ID ${todoList.getId()} added.")
            }
        }
        pl.show()
    }

    // Method to change an existing To do-List
    private fun startEditListDialog() {
        model!!.getToDoListById(selectedTodoListId) { existingTodoList ->
            if (null == existingTodoList) {
                Log.e(TAG, "Todo list with ID $selectedTodoListId not found.")
                return@getToDoListById
            }
            val pl = ProcessTodoListDialog(this, existingTodoList)
            pl.setDialogCallback { todoList ->
                todoList.setChanged()
                model!!.saveTodoListInDb(todoList) { counter ->
                    showHints()
                    addTodoListsToNavigationMenu()
                    expandableTodoTaskAdapter?.notifyDataSetChanged()
                    if (activeListId == todoList.getId()) {
                        // In case of changed list name:
                        toolbar?.setTitle(todoList.getName())
                    }
                    if (counter > 0) {
                        Log.i(TAG, "List '${todoList.getName()}' with ID ${todoList.getId()} changed.")
                    } else {
                        Log.e(TAG, "Failed to save list with ID ${todoList.getId()}.")
                    }
                }
            }
            pl.show()
        }
    }

    // Method starting tutorial
    private fun startTut() {
        val intent = Intent(this@MainActivity, TutorialActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun showTasksOfListOrAllTasks(listId: Int? = null) {
        if (null != listId) {
            showTasksOfList(listId)
        } else {
            showAllTasks()
        }
    }

    private fun showAllTasks() {
        activeListId = null
        toolbar?.setTitle(R.string.home)
        uncheckNavigationEntries()
        val homeMenuEntry = navigationView!!.menu.getItem(0)
        homeMenuEntry.setCheckable(true)
        homeMenuEntry.setChecked(true)
        model!!.getAllToDoTasks { todoTasks ->
            createExpandableTodoTaskAdapter(todoTasks, true)
            showHints()
        }
    }

    private fun showTasksOfList(listId: Int) {
        activeListId = listId
        if (navigationView != null) {
            for (i in 0 until navigationView!!.menu.size()) {
                val item = navigationView!!.menu.getItem(i)
                item.setChecked(item.itemId == listId)
            }
        }
        model!!.getToDoListById(listId) { todoList ->
            if (null != todoList) {
                toolbar?.setTitle(todoList.getName())
                createExpandableTodoTaskAdapter(todoList.getTasks(), false)
            } else {
                Log.e(TAG, "Todo list with id $listId not found. Showing all tasks instead.")
                showAllTasks()
            }
        }
    }

    private fun createExpandableTodoTaskAdapter(todoTasks: MutableList<TodoTask>, showListNames: Boolean) {
        expandableTodoTaskAdapter = ExpandableTodoTaskAdapter(this, model!!, todoTasks, showListNames)
        expandableTodoTaskAdapter!!.setOnTasksSwappedListener { groupPositionA: Int, groupPositionB: Int ->
            if (null != exLv) {
                val isGroupAExpanded = exLv!!.isGroupExpanded(groupPositionA)
                val isGroupBExpanded = exLv!!.isGroupExpanded(groupPositionB)
                if (isGroupAExpanded != isGroupBExpanded) {
                    if (isGroupAExpanded) {
                        exLv!!.collapseGroup(groupPositionA)
                        exLv!!.expandGroup(groupPositionB, false)
                    } else {
                        exLv!!.expandGroup(groupPositionA, false)
                        exLv!!.collapseGroup(groupPositionB)
                    }
                }
            }
        }
        exLv!!.setAdapter(expandableTodoTaskAdapter)
        exLv!!.setEmptyView(emptyView)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        if (menuInfo is ExpandableListContextMenuInfo) {
            val type = ExpandableListView.getPackedPositionType(menuInfo.packedPosition)
            val menuHeader = getMenuHeader(baseContext, baseContext.getString(R.string.select_option))
            menu.setHeaderView(menuHeader)
            // context menu for child items
            val workItemId: Int = if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                menuInflater.inflate(R.menu.todo_subtask_context, menu)
                R.id.work_subtask
            } else { // context menu for group items
                menuInflater.inflate(R.menu.todo_task_context, menu)
                R.id.work_task
            }
            if (pomodoroInstalled) {
                menu.findItem(workItemId).setVisible(true)
            }
        } else if (v.tag is Int) {
            selectedTodoListId = v.tag as Int
            val menuHeader = getMenuHeader(baseContext, baseContext.getString(R.string.select_option))
            menu.setHeaderView(menuHeader)
            menuInflater.inflate(R.menu.todo_list_context, menu)
        } else {
            Log.w(TAG, "Unhandled context menu owner: ${v.javaClass.simpleName}")
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.move_up_list -> {
                moveList(true)
            }

            R.id.move_down_list -> {
                moveList(false)
            }

            R.id.edit_list -> {
                startEditListDialog()
            }

            R.id.share_list -> {
                shareList()
            }

            R.id.export_list -> {
                initiateTaskExport(selectedTodoListId)
            }

            R.id.delete_list -> {
                deleteList()
            }

            R.id.edit_task -> {
                val todoTask = expandableTodoTaskAdapter?.longClickedTodo?.left
                if (null != todoTask) {
                    val editTaskDialog = ProcessTodoTaskDialog(this, todoTask)
                    editTaskDialog.setDialogCallback(ResultCallback { changedTodoTask: TodoTask ->
                        model!!.saveTodoTaskInDb(changedTodoTask) { counter ->
                            onTaskChange(changedTodoTask)
                            expandableTodoTaskAdapter!!.notifyDataSetChanged()
                            showTasksOfListOrAllTasks(changedTodoTask.getListId())
                        }
                    })
                    editTaskDialog.show()
                }
            }

            R.id.share_task -> {
                val todoTask = expandableTodoTaskAdapter?.longClickedTodo?.left
                if (null != todoTask) {
                    shareTask(todoTask)
                }
            }

            R.id.delete_task -> {
                val todoTask = expandableTodoTaskAdapter?.longClickedTodo?.left
                if (null != todoTask) {
                    val snackBar = Snackbar.make(fabNewTodoTask!!, R.string.task_removed, Snackbar.LENGTH_LONG)
                    snackBar.setAction(R.string.snack_undo) { v: View? ->
                        model!!.setTaskAndSubtasksInRecycleBin(todoTask, false) { counter ->
                            if (counter > 0) {
                                AlarmMgr.setAlarmForTask(this, todoTask)
                                showTasksOfListOrAllTasks(activeListId)
                                showHints()
                            } else {
                                Log.w(TAG, "Task was not removed from the database.")
                            }
                        }
                    }
                    model!!.setTaskAndSubtasksInRecycleBin(todoTask, true) { counter ->
                        if (counter > 0) {
                            AlarmMgr.cancelAlarmForTask(this, todoTask.getId())
                            showTasksOfListOrAllTasks(activeListId)
                            showHints()
                            snackBar.show()
                        } else {
                            Log.w(TAG, "Task was not removed from the database.")
                        }
                    }
                }
            }

            R.id.work_task -> {
                val todoTask = expandableTodoTaskAdapter?.longClickedTodo?.left
                if (null != todoTask) {
                    Log.i(TAG, "START TASK")
                    sendToPomodoro(todoTask)
                }
            }

            R.id.edit_subtask -> {
                val todoSubtask = expandableTodoTaskAdapter?.longClickedTodo?.right
                if (null != todoSubtask) {
                    val dialog = ProcessTodoSubtaskDialog(this, todoSubtask)
                    dialog.setDialogCallback(ResultCallback { todoSubtask2: TodoSubtask? ->
                        model!!.saveTodoSubtaskInDb(todoSubtask2!!) { counter ->
                            expandableTodoTaskAdapter!!.notifyDataSetChanged()
                            Log.i(TAG, "Subtask altered")
                        }
                    })
                    dialog.show()
                }
            }

            R.id.delete_subtask -> {
                val todoTask = expandableTodoTaskAdapter?.longClickedTodo?.left
                val todoSubtask = expandableTodoTaskAdapter?.longClickedTodo?.right
                if (null != todoTask && null != todoSubtask) {
                    model!!.deleteTodoSubtask(todoSubtask) { counter ->
                        todoTask.getSubtasks().remove(todoSubtask)
                        if (counter > 0) {
                            Toast.makeText(baseContext, getString(R.string.subtask_removed), Toast.LENGTH_SHORT).show()
                        } else {
                            Log.d(TAG, "Subtask was not removed from the database. Maybe it was not added beforehand (then this is no error)?")
                        }
                        expandableTodoTaskAdapter!!.notifyDataSetChanged()
                    }
                }
            }

            R.id.work_subtask -> {
                val todoSubtask = expandableTodoTaskAdapter?.longClickedTodo?.right
                if (null != todoSubtask) {
                    Log.i(TAG, "START SUBTASK")
                    sendToPomodoro(todoSubtask)
                }
            }

            else -> Log.e(TAG, "Unhandled context menu item ID: ${item.itemId}")
        }
        return super.onContextItemSelected(item)
    }

    private fun moveList(moveUp: Boolean) {
        model!!.getAllToDoListIds { allTodoListIds ->
            if (allTodoListIds.size < 2) {
                return@getAllToDoListIds
            }
            val oldIndex = allTodoListIds.indexOf(selectedTodoListId)
            if (oldIndex < 0) {
                Log.e(TAG, "Selected todo list ID $selectedTodoListId not found in list IDs: $allTodoListIds.")
                return@getAllToDoListIds
            }
            val newIndex = oldIndex + if (moveUp) -1 else 1
            if (newIndex < 0) {
                // Shift all one up.
                val lastIndex = allTodoListIds.size - 1
                for (index in lastIndex - 1 downTo 0) {
                    swapListIds(allTodoListIds, lastIndex, index)
                }
            } else if (newIndex >= allTodoListIds.size) {
                // Shift all one down.
                for (index in 1..<allTodoListIds.size) {
                    swapListIds(allTodoListIds, 0, index)
                }
            } else {
                swapListIds(allTodoListIds, oldIndex, newIndex)
            }
            // Save changes
            model!!.saveTodoListsSortOrderInDb(allTodoListIds) {
                // Notify view
                addTodoListsToNavigationMenu()
            }
        }
    }

    private fun swapListIds(listIds: MutableList<Int>, indexA: Int, indexB: Int) {
        val listIdA = listIds[indexA]
        listIds[indexA] = listIds[indexB]
        listIds[indexB] = listIdA
    }

    private fun shareList() {
        model!!.getToDoListById(selectedTodoListId) { todoList ->
            if (null == todoList) {
                Log.e(TAG, "Todo list with ID $selectedTodoListId not found.")
                return@getToDoListById
            }
            val text = StringWriter()
            val builder = MarkdownBuilder(text, getString(R.string.deadline))
            builder.addList(todoList)
            shareMarkdownText(text.toString())
        }
    }

    private fun deleteList() {
        model!!.getToDoListById(selectedTodoListId) { todoList ->
            if (null == todoList) {
                Log.e(TAG, "Todo list with ID $selectedTodoListId not found.")
                return@getToDoListById
            }
            AlertDialog.Builder(this).apply {
                setMessage(R.string.alert_list_delete)
                setCancelable(true)
                setPositiveButton(R.string.alert_delete_yes) { dialog, setId ->
                    model!!.deleteTodoList(todoList.getId()) { counter ->
                        if (counter > 0) {
                            Log.i(TAG, "List '${todoList.getName()}' with ID ${todoList.getId()} deleted.")
                            val text = getString(R.string.delete_list_feedback, todoList.getName())
                            Toast.makeText(baseContext, text, Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e(TAG, "Failed to delete list with ID ${todoList.getId()}.")
                        }
                        showHints()
                        addTodoListsToNavigationMenu()
                        if (activeListId == todoList.getId()) {
                            // Currently active list was deleted
                            showAllTasks()
                        }
                        dialog.cancel()
                    }
                }
                setNegativeButton(R.string.alert_delete_no) { dialog, id ->
                    dialog.cancel()
                }
                create().show()
            }
        }
    }

    private fun shareTask(todoTask: TodoTask) {
        val text = StringWriter()
        val builder = MarkdownBuilder(text, getString(R.string.deadline))
        builder.addTask(todoTask)
        shareMarkdownText(text.toString())
    }

    private fun shareAllTasks() {
        model!!.getAllToDoTasks { todoTasks ->
            val text = StringWriter()
            val builder = MarkdownBuilder(text, getString(R.string.deadline))
            for (todoTask in todoTasks) {
                builder.addTask(todoTask)
            }
            shareMarkdownText(text.toString())
        }
    }

    private fun shareMarkdownText(text: String) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/markdown"
        }

        val shareIntent = Intent.createChooser(intent, null)
        startActivity(shareIntent)
    }

    private fun initiateTaskExport(listId: Int? = null) {
        exportListId = listId
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/comma-separated-values"
            putExtra(Intent.EXTRA_TITLE, if (null == listId) "ToDo Data.csv" else "ToDo List.csv")
        }
        exportTasksLauncher.launch(intent)
    }

    private fun sendToPomodoro(task: TodoTask) {
        val pomodoro = Intent(POMODORO_ACTION)
        pomodoro.putExtra("todo_id", task.getId())
            .putExtra("todo_name", task.getName())
            .putExtra("todo_description", task.getDescription())
            .putExtra("todo_progress", task.getProgress(false))
        sendToPomodoro(pomodoro)
    }

    private fun sendToPomodoro(subtask: TodoSubtask) {
        val pomodoro = Intent(POMODORO_ACTION)
        pomodoro.putExtra("todo_id", subtask.getId())
            .putExtra("todo_name", subtask.getName())
            .putExtra("todo_description", "")
            .putExtra("todo_progress", -1)
        sendToPomodoro(pomodoro)
    }

    private fun sendToPomodoro(pomodoro: Intent) {
        pomodoro.setPackage("org.secuso.privacyfriendlyproductivitytimer")
            .setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
        sendBroadcast(pomodoro, "org.secuso.privacyfriendlytodolist.TODO_PERMISSION")
        finish()
    }

    private fun updateTodoFromPomodoro() {
        val todoRe = Model.createNewTodoTask()
        todoRe.setChangedFromPomodoro()
        //todoRe.setPriority(TodoTask.Priority.HIGH);
        todoRe.setName(intent.getStringExtra("todo_name")!!)
        todoRe.setId(intent.getIntExtra("todo_id", -1))
        todoRe.setProgress(intent.getIntExtra("todo_progress", -1))
        val progress = todoRe.getProgress(false)
        if (progress == 100) {
            // Set task as done
            todoRe.setDone(true)
        }
        if (progress != -1) {
            // Update the existing entry, if no subtask
            model!!.saveTodoTaskInDb(todoRe) { counter ->
                onTaskChange(todoRe)
            }
        }
    }

    private fun showHints() {
        model!!.getNumberOfAllListsAndTasks { tuple: Tuple<Int, Int> ->
            val numberOfLists = tuple.left
            val numberOfTasksNotInRecycleBin = tuple.right
            val anim: Animation = AlphaAnimation(0.0f, 1.0f)
            if (numberOfLists == 0 && numberOfTasksNotInRecycleBin == 0) {
                initialAlert!!.visibility = View.VISIBLE
                anim.setDuration(1500)
                anim.startOffset = 20
                anim.repeatMode = Animation.REVERSE
                anim.setRepeatCount(Animation.INFINITE)
                initialAlert!!.startAnimation(anim)
            } else  /* if numberOfLists != 0 || numberOfTasksNotInRecycleBin != 0 */ {
                initialAlert!!.visibility = View.GONE
                initialAlert!!.clearAnimation()
            }
            if (numberOfTasksNotInRecycleBin == 0) {
                secondAlert!!.visibility = View.VISIBLE
                anim.setDuration(1500)
                anim.startOffset = 20
                anim.repeatMode = Animation.REVERSE
                anim.setRepeatCount(Animation.INFINITE)
                secondAlert!!.startAnimation(anim)
            } else  /* if numberOfTasksNotInRecycleBin != 0 */ {
                secondAlert!!.visibility = View.GONE
                secondAlert!!.clearAnimation()
            }
        }
    }

    private fun checkIfPomodoroInstalled(): Boolean {
        return Helper.isPackageAvailable(packageManager, "org.secuso.privacyfriendlyproductivitytimer")
    }

    companion object {
        private val TAG = LogTag.create(this::class.java.declaringClass)
        const val COMMAND = "command"
        //const val COMMAND_RUN_TODO = 2
        const val COMMAND_UPDATE = 3

        // Keys
        private const val KEY_IS_UNLOCKED = "restore_is_unlocked_key_with_savedinstancestate"
        private const val KEY_UNLOCK_UNTIL = "restore_unlock_until_key_with_savedinstancestate"
        private const val KEY_ACTIVE_LIST_IS_DUMMY = "KEY_ACTIVE_LIST_IS_DUMMY"
        private const val KEY_ACTIVE_LIST_ID = "KEY_ACTIVE_LIST_ID"
        private const val POMODORO_ACTION = "org.secuso.privacyfriendlytodolist.TODO_ACTION"
        /** keep the app unlocked for 30 seconds after switching to another activity (settings/help/about) */
        private const val UNLOCK_PERIOD: Long = 30000
    }
}
