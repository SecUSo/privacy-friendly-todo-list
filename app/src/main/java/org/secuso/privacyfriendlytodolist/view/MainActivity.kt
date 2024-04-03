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

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.Menu
import android.view.MenuInflater
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
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.MenuItemCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import org.secuso.privacyfriendlytodolist.R
import org.secuso.privacyfriendlytodolist.model.Model.createNewTodoTask
import org.secuso.privacyfriendlytodolist.model.ModelServices
import org.secuso.privacyfriendlytodolist.model.TodoList
import org.secuso.privacyfriendlytodolist.model.TodoSubtask
import org.secuso.privacyfriendlytodolist.model.TodoTask
import org.secuso.privacyfriendlytodolist.model.Tuple
import org.secuso.privacyfriendlytodolist.service.ReminderService
import org.secuso.privacyfriendlytodolist.service.ReminderService.ReminderServiceBinder
import org.secuso.privacyfriendlytodolist.util.PrefManager
import org.secuso.privacyfriendlytodolist.util.Helper
import org.secuso.privacyfriendlytodolist.util.Helper.getMenuHeader
import org.secuso.privacyfriendlytodolist.util.PinUtil.hasPin
import org.secuso.privacyfriendlytodolist.view.ExpandableTodoTaskAdapter.SortTypes
import org.secuso.privacyfriendlytodolist.view.calendar.CalendarActivity
import org.secuso.privacyfriendlytodolist.view.dialog.PinDialog
import org.secuso.privacyfriendlytodolist.view.dialog.PinDialog.PinCallback
import org.secuso.privacyfriendlytodolist.view.dialog.ProcessTodoListDialog
import org.secuso.privacyfriendlytodolist.view.dialog.ProcessTodoSubtaskDialog
import org.secuso.privacyfriendlytodolist.view.dialog.ProcessTodoTaskDialog
import org.secuso.privacyfriendlytodolist.view.dialog.ResultCallback
import org.secuso.privacyfriendlytodolist.viewmodel.LifecycleViewModel

/**
 * Created by Sebastian Lutz on 12.03.2018.
 *
 * This Activity handles the navigation and operation on lists and tasks.
 */
@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    // Fragment administration
    private var currentFragment: Fragment? = null

    //TodoTask administration
    private var exLv: ExpandableListView? = null
    private var tv: TextView? = null
    private var expandableTodoTaskAdapter: ExpandableTodoTaskAdapter? = null
    private var initialAlert: TextView? = null
    private var secondAlert: TextView? = null
    private var optionFab: FloatingActionButton? = null
    private var model: ModelServices? = null
    private var mPref: SharedPreferences? = null

    // TodoList administration
    var todoLists: List<TodoList> = ArrayList()
        private set

    /** Use this list if you need a container for tasks that does not exist in the database (e.g. to show all tasks, tasks of today etc.)  */
    var dummyList: TodoList? = null

    /** reference of last clicked list for fragment  */
    var clickedList: TodoList? = null

    private var adapter: TodoListAdapter? = null

    /** Service that triggers notifications for upcoming tasks  */
    private var reminderService: ReminderService? = null

    // GUI
    private var navigationView: NavigationView? = null
    private var navigationBottomView: NavigationView? = null
    private var toolbar: Toolbar? = null
    private var drawer: DrawerLayout? = null

    // Others
    private var inList = false
    private var isInitialized = false
    private var isUnlocked = false
    private var unlockUntil: Long = -1
    private var activeListId: Int? = null

    //Pomodoro
    private var pomodoroInstalled = false

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        menuInflater.inflate(R.menu.search, menu)
        menuInflater.inflate(R.menu.add_list, menu)
        val searchItem = menu.findItem(R.id.ac_search)
        val searchView = MenuItemCompat.getActionView(searchItem) as SearchView
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
        priorityGroup.setChecked(mPref!!.getBoolean(PrefManager.P_GROUP_BY_PRIORITY.name, false))
        val deadlineGroup = menu.findItem(R.id.ac_sort_by_deadline)
        deadlineGroup.setChecked(mPref!!.getBoolean(PrefManager.P_SORT_BY_DEADLINE.name, false))
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
        var checked = false
        var sortType: SortTypes
        sortType = SortTypes.DEADLINE
        collapseAll()
        when (item.itemId) {
            R.id.ac_add -> {
                startListDialog()
                addListToNav()
            }

            R.id.ac_show_all_tasks -> {
                expandableTodoTaskAdapter!!.filter = ExpandableTodoTaskAdapter.Filter.ALL_TASKS
                expandableTodoTaskAdapter!!.notifyDataSetChanged()
                mPref!!.edit().putString(PrefManager.P_TASK_FILTER.name, "ALL_TASKS").apply()
                return true
            }

            R.id.ac_show_open_tasks -> {
                expandableTodoTaskAdapter!!.filter = ExpandableTodoTaskAdapter.Filter.OPEN_TASKS
                expandableTodoTaskAdapter!!.notifyDataSetChanged()
                mPref!!.edit().putString(PrefManager.P_TASK_FILTER.name, "OPEN_TASKS").apply()
                return true
            }

            R.id.ac_show_completed_tasks -> {
                expandableTodoTaskAdapter!!.filter = ExpandableTodoTaskAdapter.Filter.COMPLETED_TASKS
                expandableTodoTaskAdapter!!.notifyDataSetChanged()
                mPref!!.edit().putString(PrefManager.P_TASK_FILTER.name, "COMPLETED_TASKS").apply()
                return true
            }

            R.id.ac_group_by_prio -> {
                checked = !item.isChecked
                item.setChecked(checked)
                sortType = SortTypes.PRIORITY
                mPref!!.edit().putBoolean(PrefManager.P_GROUP_BY_PRIORITY.name, checked).apply()
            }

            R.id.ac_sort_by_deadline -> {
                checked = !item.isChecked
                item.setChecked(checked)
                sortType = SortTypes.DEADLINE
                mPref!!.edit().putBoolean(PrefManager.P_SORT_BY_DEADLINE.name, checked).apply()
            }

            else -> return super.onOptionsItemSelected(item)
        }
        if (checked) {
            expandableTodoTaskAdapter!!.addSortCondition(sortType)
        } else {
            expandableTodoTaskAdapter!!.removeSortCondition(sortType)
        }
        expandableTodoTaskAdapter!!.notifyDataSetChanged()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT != 0) {
            finish()
            return
        }
        val viewModel = ViewModelProvider(this)[LifecycleViewModel::class.java]
        model = viewModel.model
        val prefManager = PrefManager(this)
        if (prefManager.isFirstTimeLaunch) {
            prefManager.setFirstTimeValues(this)
            startTut()
            finish()
        }
        if (savedInstanceState != null) {
            restore(savedInstanceState)
        } else {
            isUnlocked = false
            unlockUntil = -1
        }
        setContentView(R.layout.activity_main)
        exLv = findViewById(R.id.exlv_tasks)
        tv = findViewById(R.id.tv_empty_view_no_tasks)
        optionFab = findViewById(R.id.fab_new_task)
        initialAlert = findViewById(R.id.initial_alert)
        secondAlert = findViewById(R.id.second_alert)
        hints()
        mPref = PreferenceManager.getDefaultSharedPreferences(this)

        //Try to snooze the task by notification
        /*if (savedInstanceState == null) {
            Bundle b = getIntent().getExtras();
            if (b != null){
                notificationDone = b.getInt("snooze");
                int taskID = b.getInt("taskId");
                TodoList tasks = getTodoTasks();
                TodoTask currentTask = tasks.getTasks().get(taskID);
                currentTask.setReminderTime(System.currentTimeMillis() + notificationDone);
                sendToDatabase(currentTask);

            }
        } */
        if (intent.getIntExtra(COMMAND, -1) == COMMAND_UPDATE) {
            updateTodoFromPomodoro()
        }
        authAndGuiInit(savedInstanceState)
        if (activeListId != null) {
            showTasksOfList(activeListId!!)
        }
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val arrayList = if (todoLists is ArrayList<TodoList>) todoLists as ArrayList<TodoList> else ArrayList(todoLists)
        outState.putParcelableArrayList(KEY_TODO_LISTS, arrayList)
        outState.putParcelable(KEY_CLICKED_LIST, clickedList)
        outState.putParcelable(KEY_DUMMY_LIST, dummyList)
        outState.putBoolean(KEY_IS_UNLOCKED, isUnlocked)
        outState.putLong(KEY_UNLOCK_UNTIL, unlockUntil)
        if (activeListId != null) {
            outState.putByte(KEY_ACTIVE_LIST_IS_DUMMY, 0.toByte())
            outState.putInt(KEY_ACTIVE_LIST, activeListId!!)
        } else {
            outState.putByte(KEY_ACTIVE_LIST_IS_DUMMY, 1.toByte())
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        restore(savedInstanceState)
    }

    private fun restore(savedInstanceState: Bundle) {
        todoLists = savedInstanceState.getParcelableArrayList<TodoList>(KEY_TODO_LISTS) as List<TodoList>
        clickedList = savedInstanceState.getParcelable(KEY_CLICKED_LIST)
        dummyList = savedInstanceState.getParcelable(KEY_DUMMY_LIST)
        isUnlocked = savedInstanceState.getBoolean(KEY_IS_UNLOCKED)
        unlockUntil = savedInstanceState.getLong(KEY_UNLOCK_UNTIL)
        activeListId = if (savedInstanceState.getByte(KEY_ACTIVE_LIST_IS_DUMMY).toInt() != 0)
            null else savedInstanceState.getInt(KEY_ACTIVE_LIST)
    }

    private fun authAndGuiInit(savedInstanceState: Bundle?) {
        if (hasPin(this) && !isUnlocked && (unlockUntil == -1L || System.currentTimeMillis() > unlockUntil)) {
            val dialog = PinDialog(this, true)
            dialog.setDialogCallback(object : PinCallback {
                override fun accepted() {
                    initActivityStage1(savedInstanceState)
                }

                override fun declined() {
                    finishAffinity()
                }

                override fun resetApp() {
                    PreferenceManager.getDefaultSharedPreferences(this@MainActivity).edit().clear().apply()
                    model!!.deleteAllData(null)
                    val intent = Intent(this@MainActivity, MainActivity::class.java)
                    dialog.dismiss()
                    startActivity(intent)
                }
            })
            dialog.show()
        } else {
            initActivityStage1(savedInstanceState)
        }
    }

    private fun initActivityStage1(savedInstanceState: Bundle?) {
        isUnlocked = true
        model!!.getAllToDoLists { todoLists ->
            this.todoLists = todoLists
            initActivityStage2(savedInstanceState)
        }
    }

    private fun initActivityStage2(savedInstanceState: Bundle?) {
        showAllTasks()

        // check if app was started by clicking on a reminding notification
        val extras = intent.extras
        if (extras != null && TodoTasksFragment.KEY == extras.getString(KEY_SELECTED_FRAGMENT_BY_NOTIFICATION)) {
            val dueTask = extras.getParcelable<TodoTask>(PARCELABLE_KEY_FOR_TODO_TASK)
            val bundle = Bundle()
            val listId: Int?
            if (null != dueTask) {
                listId = dueTask.getListId()
            } else {
                listId = null
                Log.e(TAG, "Failed to get todo task from parcelable after click on reminding notification.")
            }
            if (listId != null) {
                bundle.putInt(KEY_SELECTED_LIST_ID_BY_NOTIFICATION, listId)
            } else {
                bundle.putByte(KEY_SELECTED_DUMMY_LIST_BY_NOTIFICATION, 0.toByte())
            }
            bundle.putBoolean(TodoTasksFragment.SHOW_FLOATING_BUTTON, true)
            currentFragment = TodoTasksFragment()
            currentFragment!!.setArguments(bundle)
        } else {
            if (currentFragment == null) {
                showAllTasks()
                Log.i(TAG, "Activity was not retained.")
            } else {
                // restore state before configuration change
                if (savedInstanceState != null) {
                    todoLists = savedInstanceState.getParcelableArrayList<TodoList>(KEY_TODO_LISTS) as List<TodoList>
                    clickedList = savedInstanceState[KEY_CLICKED_LIST] as TodoList?
                    dummyList = savedInstanceState[KEY_DUMMY_LIST] as TodoList?
                } else {
                    Log.i(TAG, "Could not restore old state because savedInstanceState is null.")
                }
                Log.i(TAG, "Activity was retained.")
            }
        }
        guiSetup()
        this.isInitialized = true
        inList = false
    }

    public override fun onStart() {
        super.onStart()

        uncheckNavigationEntries()
        if (navigationView != null) {
            navigationView!!.menu.getItem(0).setChecked(true)
        }
    }

    private fun guiSetup() {
        // toolbar setup
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // side menu setup
        drawer = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(this, drawer, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer!!.setDrawerListener(toggle)
        toggle.syncState()
        addListToNav()

        //LinearLayout l = (LinearLayout) findViewById(R.id.footer);
        navigationView = findViewById(R.id.nav_view)
        navigationBottomView = findViewById(R.id.nav_view_bottom)
        navigationView!!.setNavigationItemSelectedListener(this)
        navigationBottomView!!.setNavigationItemSelectedListener(this)
        navigationView!!.menu.getItem(0).setChecked(true)
    }

    private fun uncheckNavigationEntries() {
        // uncheck all navigation entries
        if (navigationView != null) {
            val size = navigationView!!.menu.size()
            for (i in 0 until size) {
                navigationView!!.menu.getItem(i).setChecked(false)
            }
            Log.i(TAG, "Navigation entries unchecked.")
        }
        if (navigationBottomView != null) {
            val size = navigationBottomView!!.menu.size()
            for (i in 0 until size) {
                navigationBottomView!!.menu.getItem(i).setChecked(false)
            }
            Log.i(TAG, "Navigation-Bottom entries unchecked.")
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_settings -> {
                uncheckNavigationEntries()
                val intent = Intent(this, Settings::class.java)
                unlockUntil = System.currentTimeMillis() + UNLOCK_PERIOD
                startActivity(intent)
            }
            R.id.nav_tutorial -> {
                uncheckNavigationEntries()
                val intent = Intent(this, TutorialActivity::class.java)
                unlockUntil = System.currentTimeMillis() + UNLOCK_PERIOD
                startActivity(intent)
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
            R.id.nav_about -> {
                uncheckNavigationEntries()
                val intent = Intent(this, AboutActivity::class.java)
                unlockUntil = System.currentTimeMillis() + UNLOCK_PERIOD
                startActivity(intent)
            }
            R.id.nav_help -> {
                uncheckNavigationEntries()
                val intent = Intent(this, HelpActivity::class.java)
                unlockUntil = System.currentTimeMillis() + UNLOCK_PERIOD
                startActivity(intent)
            }
            R.id.menu_home -> {
                uncheckNavigationEntries()
                inList = false
                showAllTasks()
                toolbar!!.setTitle(R.string.home)
                item.setCheckable(true)
                item.setChecked(true)
            }
            R.id.nav_dummy1, R.id.nav_dummy2, R.id.nav_dummy3 -> {
                if (!inList) {
                    uncheckNavigationEntries()
                    navigationView!!.menu.getItem(0).setChecked(true)
                }
                return false
            }
            else -> {
                showTasksOfList(item.itemId)
                toolbar!!.setTitle(item.title)
                item.setChecked(true)
            }
        }
        val drawer: DrawerLayout = findViewById(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onStop() {
        isUnlocked = false
        super.onStop()
    }

    override fun onResume() {
        super.onResume()

        // Check if Pomodoro is installed
        pomodoroInstalled = checkIfPomodoroInstalled()
        if (this.isInitialized && !isUnlocked && (unlockUntil == -1L || System.currentTimeMillis() > unlockUntil)) {
            // restart activity to show pin dialog again
            //Intent intent = new Intent(this, MainActivity.class);
            //finish();
            //startActivity(intent);
            if (reminderService == null) {
                bindToReminderService()
            }
            guiSetup()
            if (activeListId != null) {
                showTasksOfList(activeListId!!)
            } else {
                showAllTasks()
            }
            return
        }

        // isUnlocked might be false when returning from another activity. set to true if the unlock period was not expired:
        isUnlocked = isUnlocked || unlockUntil != -1L && System.currentTimeMillis() <= unlockUntil
        unlockUntil = -1
        if (reminderService == null) {
            bindToReminderService()
        }
        Log.i(TAG, "onResume()")
    }

    override fun onDestroy() {
        if (reminderService != null) {
            unbindService(reminderServiceConnection)
            reminderService = null
            Log.i(TAG, "service is now null")
        }
        super.onDestroy()
    }

    override fun onUserLeaveHint() {
        // prevents unlocking the app by rotating while the app is inactive and then returning
        isUnlocked = false
    }

    private fun bindToReminderService() {
        Log.i(TAG, "bindToReminderService()")
        val intent = Intent(this, ReminderService::class.java)
        // no Context.BIND_AUTO_CREATE, because service will be started by startService and thus live longer than this activity
        bindService(intent, reminderServiceConnection, 0 )
        startService(intent)
    }

    override fun onBackPressed() {
        val drawer: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else if (inList) {
            showAllTasks()
            toolbar!!.setTitle(R.string.home)
            inList = false
            uncheckNavigationEntries()
            navigationView!!.menu.getItem(0).setChecked(true)
        } else {
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setMessage(R.string.exit_app);
//            builder.setCancelable(true);
//            builder.setPositiveButton(R.string.exit_positive, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    finish();
//                }
//            });
//            builder.setNegativeButton(R.string.exit_negative, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.cancel();
//                }
//            });
//            AlertDialog alert = builder.create();
//            alert.show();
            super.onBackPressed()
        }
    }

    private val reminderServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            Log.d("ServiceConnection", "connected")
            reminderService = (binder as ReminderServiceBinder).service
        }

        //binder comes from server to communicate with method's of
        override fun onServiceDisconnected(className: ComponentName) {
            Log.d("ServiceConnection", "disconnected")
            reminderService = null
        }
    }

    fun notifyReminderService(currentTask: TodoTask) {
        // TODO This method is called from other fragments as well (e.g. after opening MainActivity by reminder). In such cases the service is null and alarms cannot be updated. Fix this!
        if (reminderService != null) {
            // Report changes to the reminder task if the reminder time is prior to the deadline or if no deadline is set at all. The reminder time must always be after the the current time. The task must not be completed.
            if ((currentTask.getReminderTime() < currentTask.getDeadline() || !currentTask.hasDeadline()) && !currentTask.isDone()) {
                reminderService!!.processTodoTask(currentTask.getId())
                Log.i(TAG, "Reminder is set!")
            } else {
                Log.i(TAG, "Reminder service was not informed about the task " + currentTask.getName())
            }
        } else {
            Log.i(TAG, "Service is null. Cannot update alarms")
        }
    }

    //Adds To do-Lists to the navigation-drawer
    private fun addListToNav() {
        val nv: NavigationView = findViewById(R.id.nav_view)
        val navMenu = nv.menu
        navMenu.clear()
        val mf = MenuInflater(applicationContext)
        mf.inflate(R.menu.nav_content, navMenu)
        val help = ArrayList<TodoList>()
        help.addAll(todoLists)
        for (i in help.indices) {
            val name = help[i].getName()
            val id = help[i].getId()
            val item = navMenu.add(R.id.drawer_group2, id, 1, name)
            item.setCheckable(true)
            item.setIcon(R.drawable.ic_label_black_24dp)
            val v = ImageButton(this, null, R.style.BorderlessButtonStyle)
            v.setImageResource(R.drawable.ic_delete_black_24dp)
            v.setOnClickListener(OnCustomMenuItemClickListener(help[i].getId(), this@MainActivity))
            item.setActionView(v)
        }
    }

    // Method to add a new To do-List
    private fun startListDialog() {
        model!!.getAllToDoLists { todoLists ->
            this.todoLists = todoLists
            adapter = TodoListAdapter(this, todoLists)
            val pl = ProcessTodoListDialog(this)
            pl.setDialogCallback(ResultCallback { todoList: TodoList ->
                todoLists.add(todoList)
                adapter!!.updateList(todoLists)
                adapter!!.notifyDataSetChanged()
                model!!.saveTodoListInDb(todoList) { counter: Int? ->
                    hints()
                    addListToNav()
                    Log.i(TAG, "List added")
                }
            })
            pl.show()
        }
    }

    // Method starting tutorial
    private fun startTut() {
        val intent = Intent(this@MainActivity, TutorialActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    // ClickListener to delete a To-Do list
    inner class OnCustomMenuItemClickListener internal constructor(private val id: Int,
        private val context: Context) : View.OnClickListener {
        override fun onClick(view: View) {
            val builder1 = AlertDialog.Builder(context)
            builder1.setMessage(R.string.alert_listdelete)
            builder1.setCancelable(true)
            builder1.setPositiveButton(R.string.alert_delete_yes) { dialog, setId ->
                model!!.deleteTodoList(id, null)
                dialog.cancel()
                val intent = Intent(context, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
            builder1.setNegativeButton(R.string.alert_delete_no) { dialog, id -> dialog.cancel() }
            val alert11 = builder1.create()
            alert11.show()
            return
        }
    }

    private fun showAllTasks() {
        model!!.getAllToDoTasks { todoTasks ->
            expandableTodoTaskAdapter = ExpandableTodoTaskAdapter(this, model!!, todoTasks)
            exLv!!.setOnItemLongClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
                val groupPosition = ExpandableListView.getPackedPositionGroup(id)
                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    val childPosition = ExpandableListView.getPackedPositionChild(id)
                    expandableTodoTaskAdapter!!.setLongClickedSubtaskByPos(groupPosition, childPosition)
                } else {
                    expandableTodoTaskAdapter!!.setLongClickedTaskByPos(groupPosition)
                }
                registerForContextMenu(exLv)
                false
            }
            exLv!!.setAdapter(expandableTodoTaskAdapter)
            exLv!!.setEmptyView(tv)
            optionFab!!.visibility = View.VISIBLE
            initFAB(null)
            hints()
        }
    }

    private fun showTasksOfList(listId: Int) {
        activeListId = listId
        inList = true
        uncheckNavigationEntries()
        if (navigationView != null) {
            for (i in 0 until navigationView!!.menu.size()) {
                val item = navigationView!!.menu.getItem(i)
                if (item.itemId == listId) {
                    item.setChecked(true)
                    toolbar!!.setTitle(item.title)
                    Log.i(TAG, "Active navigation entry checked.")
                    break
                }
            }
        }
        model!!.getToDoListById(listId) { todoList: TodoList? ->
            val todoListTasks = todoList?.getTasks() ?: ArrayList()
            expandableTodoTaskAdapter = ExpandableTodoTaskAdapter(this, model!!, todoListTasks)
            exLv!!.setAdapter(expandableTodoTaskAdapter)
            exLv!!.setEmptyView(tv)
            optionFab!!.visibility = View.VISIBLE
            initFAB(listId)
        }
    }

    // todoListId != 0 means id is given from list. otherwise new task was created in all-tasks.
    private fun initFAB(todoListId: Int?) {
        optionFab!!.setOnClickListener { v: View? ->
            val pt = ProcessTodoTaskDialog(this@MainActivity, todoLists)
            pt.setListSelector(todoListId)
            pt.setDialogCallback { todoTask ->
                model!!.saveTodoTaskInDb(todoTask) { counter: Int? ->
                    notifyReminderService(todoTask)
                    hints()
                    // show List if created in certain list, else show all tasks
                    if (null != todoListId) {
                        showTasksOfList(todoListId)
                    } else {
                        showAllTasks()
                    }
                }
            }
            pt.show()
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
        val info = menuInfo as ExpandableListContextMenuInfo
        val type = ExpandableListView.getPackedPositionType(info.packedPosition)
        val inflater = this.menuInflater
        menu.setHeaderView(getMenuHeader(baseContext, baseContext.getString(R.string.select_option)))
        // context menu for child items
        val workItemId: Int = if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            inflater.inflate(R.menu.todo_subtask_long_click, menu)
            R.id.work_subtask
        } else { // context menu for group items
            inflater.inflate(R.menu.todo_task_long_click, menu)
            R.id.work_task
        }
        if (pomodoroInstalled) {
            menu.findItem(workItemId).setVisible(true)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val longClickedTodo = expandableTodoTaskAdapter!!.longClickedTodo
        if (null != longClickedTodo) {
            val todoTask = longClickedTodo.left
            val todoSubtask = longClickedTodo.right
            when (item.itemId) {
                R.id.change_subtask -> {
                    val dialog = ProcessTodoSubtaskDialog(this, todoSubtask!!)
                    dialog.titleEdit()
                    dialog.setDialogCallback(ResultCallback { todoSubtask2: TodoSubtask? ->
                        model!!.saveTodoSubtaskInDb(todoSubtask2!!) { counter: Int? ->
                            expandableTodoTaskAdapter!!.notifyDataSetChanged()
                            Log.i(TAG, "Subtask altered")
                        }
                    })
                    dialog.show()
                }

                R.id.delete_subtask -> model!!.deleteTodoSubtask(todoSubtask!!) { counter: Int ->
                    todoTask.getSubtasks().remove(todoSubtask)
                    if (counter == 1) {
                        Toast.makeText(baseContext, getString(R.string.subtask_removed), Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d(TAG, "Subtask was not removed from the database. Maybe it was not added beforehand (then this is no error)?")
                    }
                    expandableTodoTaskAdapter!!.notifyDataSetChanged()
                }

                R.id.change_task -> {
                    val oldListId = todoTask.getListId()
                    val editTaskDialog = ProcessTodoTaskDialog(this, todoLists, todoTask)
                    editTaskDialog.titleEdit()
                    editTaskDialog.setListSelector(oldListId)
                    editTaskDialog.setDialogCallback(ResultCallback { todoTask2: TodoTask ->
                        model!!.saveTodoTaskInDb(todoTask2) { counter: Int? ->
                            notifyReminderService(todoTask2)
                            expandableTodoTaskAdapter!!.notifyDataSetChanged()
                            if (inList && oldListId != null) {
                                showTasksOfList(oldListId)
                            } else {
                                showAllTasks()
                            }
                        }
                    })
                    editTaskDialog.show()
                }

                R.id.delete_task -> {
                    val snackbar = Snackbar.make(optionFab!!, R.string.task_removed, Snackbar.LENGTH_LONG)
                    snackbar.setAction(R.string.snack_undo) { v: View? ->
                        model!!.setTaskAndSubtasksInRecycleBin(todoTask, false) { counter: Int? ->
                            if (inList && todoTask.getListId() != null) {
                                showTasksOfList(todoTask.getListId()!!)
                            } else {
                                showAllTasks()
                            }
                            hints()
                        }
                    }
                    model!!.setTaskAndSubtasksInRecycleBin(todoTask, true) { counter: Int ->
                        if (counter == 1) {
                            hints()
                        } else {
                            Log.d(TAG, "Task was not removed from the database. Maybe it was not added beforehand (then this is no error)?")
                        }

                        // Dependent on the current View, update All-tasks or a certain List
                        if (inList && todoTask.getListId() != null) {
                            showTasksOfList(todoTask.getListId()!!)
                        } else {
                            showAllTasks()
                        }
                        snackbar.show()
                    }
                }

                R.id.work_task -> {
                    Log.i(TAG, "START TASK")
                    sendToPomodoro(todoTask)
                }

                R.id.work_subtask -> {
                    Log.i(TAG, "START SUBTASK")
                    sendToPomodoro(todoSubtask!!)
                }

                else -> throw IllegalArgumentException("Invalid menu item selected.")
            }
        }
        return super.onContextItemSelected(item)
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
        val todoRe = createNewTodoTask()
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
            model!!.saveTodoTaskInDb(todoRe) { counter: Int? -> notifyReminderService(todoRe) }
        }

        //super.onResume();
    }

    private fun hints() {
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
        const val COMMAND = "command"
        const val COMMAND_RUN_TODO = 2
        const val COMMAND_UPDATE = 3
        private val TAG = MainActivity::class.java.getSimpleName()

        // Keys
        private const val KEY_TODO_LISTS = "restore_todo_list_key_with_savedinstancestate"
        private const val KEY_CLICKED_LIST = "restore_clicked_list_with_savedinstancestate"
        private const val KEY_DUMMY_LIST = "restore_dummy_list_with_savedinstancestate"
        private const val KEY_IS_UNLOCKED = "restore_is_unlocked_key_with_savedinstancestate"
        private const val KEY_UNLOCK_UNTIL = "restore_unlock_until_key_with_savedinstancestate"
        const val KEY_SELECTED_FRAGMENT_BY_NOTIFICATION = "fragment_choice"
        const val KEY_SELECTED_LIST_ID_BY_NOTIFICATION = "KEY_SELECTED_LIST_ID_BY_NOTIFICATION"
        const val KEY_SELECTED_DUMMY_LIST_BY_NOTIFICATION = "KEY_SELECTED_DUMMY_LIST_BY_NOTIFICATION"
        private const val KEY_ACTIVE_LIST_IS_DUMMY = "KEY_ACTIVE_LIST_IS_DUMMY"
        private const val KEY_ACTIVE_LIST = "KEY_ACTIVE_LIST"
        private const val POMODORO_ACTION = "org.secuso.privacyfriendlytodolist.TODO_ACTION"
        const val PARCELABLE_KEY_FOR_TODO_TASK = "PARCELABLE_KEY_FOR_TODO_TASK"
        /** keep the app unlocked for 30 seconds after switching to another activity (settings/help/about) */
        private const val UNLOCK_PERIOD: Long = 30000
    }
}
