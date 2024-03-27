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

package org.secuso.privacyfriendlytodolist.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.Model;
import org.secuso.privacyfriendlytodolist.model.ModelServices;
import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.model.TodoSubtask;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.model.Tuple;
import org.secuso.privacyfriendlytodolist.service.ReminderService;
import org.secuso.privacyfriendlytodolist.tutorial.PrefManager;
import org.secuso.privacyfriendlytodolist.tutorial.TutorialActivity;
import org.secuso.privacyfriendlytodolist.util.Helper;
import org.secuso.privacyfriendlytodolist.util.PinUtil;
import org.secuso.privacyfriendlytodolist.view.calendar.CalendarActivity;
import org.secuso.privacyfriendlytodolist.view.dialog.PinDialog;
import org.secuso.privacyfriendlytodolist.view.dialog.ProcessTodoListDialog;
import org.secuso.privacyfriendlytodolist.view.dialog.ProcessTodoSubtaskDialog;
import org.secuso.privacyfriendlytodolist.view.dialog.ProcessTodoTaskDialog;
import org.secuso.privacyfriendlytodolist.viewmodel.LifecycleViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Sebastian Lutz on 12.03.2018.
 *
 * This Activity handles the navigation and operation on lists and tasks.
 *
 */

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String COMMAND = "command";
    public static final int COMMAND_UPDATE = 3;
    public static final int COMMAND_RUN_TODO = 2;

    private static final String TAG = MainActivity.class.getSimpleName();

    // Keys
    private static final String KEY_TODO_LISTS = "restore_todo_list_key_with_savedinstancestate";
    private static final String KEY_CLICKED_LIST = "restore_clicked_list_with_savedinstancestate";
    private static final String KEY_DUMMY_LIST = "restore_dummy_list_with_savedinstancestate";
    private static final String KEY_IS_UNLOCKED = "restore_is_unlocked_key_with_savedinstancestate";
    private static final String KEY_UNLOCK_UNTIL = "restore_unlock_until_key_with_savedinstancestate";
    public static final String KEY_SELECTED_FRAGMENT_BY_NOTIFICATION = "fragment_choice";
    public static final String KEY_SELECTED_LIST_ID_BY_NOTIFICATION = "KEY_SELECTED_LIST_ID_BY_NOTIFICATION";
    public static final String KEY_SELECTED_DUMMY_LIST_BY_NOTIFICATION = "KEY_SELECTED_DUMMY_LIST_BY_NOTIFICATION";
    private static final String KEY_FRAGMENT_CONFIG_CHANGE_SAVE = "current_fragment";
    private static final String KEY_ACTIVE_LIST_IS_DUMMY = "KEY_ACTIVE_LIST_IS_DUMMY";
    private static final String KEY_ACTIVE_LIST = "KEY_ACTIVE_LIST";
    private static final String POMODORO_ACTION = "org.secuso.privacyfriendlytodolist.TODO_ACTION";
    public static final String PARCELABLE_KEY_FOR_TODO_TASK = "PARCELABLE_KEY_FOR_TODO_TASK";

    // Fragment administration
    //private final FragmentManager fragmentManager = getSupportFragmentManager();
    private Fragment currentFragment;

    //TodoTask administration
    private ExpandableListView exLv;
    private TextView tv;
    private ExpandableTodoTaskAdapter expandableTodoTaskAdapter;
    private TextView initialAlert;
    private TextView secondAlert;
    private FloatingActionButton optionFab;

    private ModelServices model;

    private SharedPreferences mPref;

    // TodoList administration
    private List<TodoList> todoLists = new ArrayList<>();
    /** Use this list if you need a container for tasks that does not exist in the database (e.g. to show all tasks, tasks of today etc.) */
    private TodoList dummyList;
    /** reference of last clicked list for fragment */
    private TodoList clickedList;
    private TodoRecyclerView mRecyclerView;
    private TodoListAdapter adapter;
    private MainActivity containerActivity;

    /** Service that triggers notifications for upcoming tasks */
    private ReminderService reminderService;

    // GUI
    private NavigationView navigationView;
    private NavigationView navigationBottomView;
    private Toolbar toolbar;
    private DrawerLayout drawer;

    // Others
    private boolean inList;
    boolean isInitialized = false;
    boolean isUnlocked = false;
    long unlockUntil = -1;
    private static final long UnlockPeriod = 30000; // keep the app unlocked for 30 seconds after switching to another activity (settings/help/about)
    int notificationDone;
    private Integer activeListId = null;

    //Pomodoro
    private boolean pomodoroInstalled = false;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        getMenuInflater().inflate(R.menu.search, menu);
        getMenuInflater().inflate(R.menu.add_list, menu);

        MenuItem searchItem = menu.findItem(R.id.ac_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                collapseAll();
                expandableTodoTaskAdapter.setQueryString(query);
                expandableTodoTaskAdapter.notifyDataSetChanged();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                collapseAll();
                expandableTodoTaskAdapter.setQueryString(query);
                expandableTodoTaskAdapter.notifyDataSetChanged();
                return false;
            }
        });

        MenuItem priotiryGroup = menu.findItem(R.id.ac_group_by_prio);
        priotiryGroup.setChecked(mPref.getBoolean("PRIORITY", false));

        MenuItem deadlineGroup = menu.findItem(R.id.ac_sort_by_deadline);
        deadlineGroup.setChecked(mPref.getBoolean("DEADLINE", false));

        return super.onCreateOptionsMenu(menu);
    }



    private void collapseAll()
    {
        // collapse all elements on view change.
        // the expandable list view keeps the expanded indices, so other items
        // get expanded, when they get the old expanded index
        int groupCount = expandableTodoTaskAdapter.getGroupCount();
        for(int i = 0; i < groupCount; i++)
            exLv.collapseGroup(i);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean checked = false;
        ExpandableTodoTaskAdapter.SortTypes sortType;
        sortType = ExpandableTodoTaskAdapter.SortTypes.DEADLINE;

        collapseAll();

        switch (item.getItemId()) {
            case R.id.ac_add:
                startListDialog();
                addListToNav();
                break;
            case R.id.ac_show_all_tasks:
                expandableTodoTaskAdapter.setFilter(ExpandableTodoTaskAdapter.Filter.ALL_TASKS);
                expandableTodoTaskAdapter.notifyDataSetChanged();
                mPref.edit().putString("FILTER", "ALL_TASKS").commit();
                return true;
            case R.id.ac_show_open_tasks:
                expandableTodoTaskAdapter.setFilter(ExpandableTodoTaskAdapter.Filter.OPEN_TASKS);
                expandableTodoTaskAdapter.notifyDataSetChanged();
                mPref.edit().putString("FILTER", "OPEN_TASKS").commit();
                return true;
            case R.id.ac_show_completed_tasks:
                expandableTodoTaskAdapter.setFilter(ExpandableTodoTaskAdapter.Filter.COMPLETED_TASKS);
                expandableTodoTaskAdapter.notifyDataSetChanged();
                mPref.edit().putString("FILTER", "COMPLETED_TASKS").commit();
                return true;
            case R.id.ac_group_by_prio:
                checked = !item.isChecked();
                item.setChecked(checked);
                sortType = ExpandableTodoTaskAdapter.SortTypes.PRIORITY;
                mPref.edit().putBoolean("PRIORITY", checked).commit();
                break;
            case R.id.ac_sort_by_deadline:
                checked = !item.isChecked();
                item.setChecked(checked);
                sortType = ExpandableTodoTaskAdapter.SortTypes.DEADLINE;
                mPref.edit().putBoolean("DEADLINE", checked).commit();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        if(checked) {
            expandableTodoTaskAdapter.addSortCondition(sortType);
        }  else {
            expandableTodoTaskAdapter.removeSortCondition(sortType);
        }

        expandableTodoTaskAdapter.notifyDataSetChanged();
        return true;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }

        LifecycleViewModel viewModel = new ViewModelProvider(this).get(LifecycleViewModel.class);
        model = viewModel.getModel();

        PrefManager prefManager = new PrefManager(this);
        if (prefManager.isFirstTimeLaunch()) {
            prefManager.setFirstTimeValues(this);
            startTut();
            finish();
        }

        if(savedInstanceState != null) {
            restore(savedInstanceState);
        } else {
            isUnlocked = false;
            unlockUntil = -1;
        }

        setContentView(R.layout.activity_main);

        exLv = (ExpandableListView) findViewById(R.id.exlv_tasks);
        tv = (TextView) findViewById(R.id.tv_empty_view_no_tasks);
        optionFab = (FloatingActionButton) findViewById(R.id.fab_new_task);
        initialAlert = (TextView) findViewById(R.id.initial_alert);
        secondAlert = (TextView) findViewById(R.id.second_alert);

        hints();

        mPref = PreferenceManager.getDefaultSharedPreferences(this);

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

        if (getIntent().getIntExtra(COMMAND, -1) == COMMAND_UPDATE) {
            updateTodoFromPomodoro();
        }

        authAndGuiInit(savedInstanceState);
        if(activeListId != null) {
            showTasksOfList(activeListId);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<TodoList> todoListsInArrayList = todoLists instanceof ArrayList<TodoList>
                ? (ArrayList<TodoList>)todoLists
                : new ArrayList<>(todoLists);
        outState.putParcelableArrayList(KEY_TODO_LISTS, todoListsInArrayList);
        outState.putParcelable(KEY_CLICKED_LIST, clickedList);
        outState.putParcelable(KEY_DUMMY_LIST, dummyList);
        outState.putBoolean(KEY_IS_UNLOCKED, isUnlocked);
        outState.putLong(KEY_UNLOCK_UNTIL, unlockUntil);
        if (activeListId != null) {
            outState.putByte(KEY_ACTIVE_LIST_IS_DUMMY, (byte) 0);
            outState.putInt(KEY_ACTIVE_LIST, activeListId);
        } else {
            outState.putByte(KEY_ACTIVE_LIST_IS_DUMMY, (byte) 1);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        restore(savedInstanceState);
    }

    private void restore(Bundle savedInstanceState) {
        todoLists = savedInstanceState.getParcelableArrayList(KEY_TODO_LISTS);
        clickedList = savedInstanceState.getParcelable(KEY_CLICKED_LIST);
        dummyList = savedInstanceState.getParcelable(KEY_DUMMY_LIST);
        isUnlocked = savedInstanceState.getBoolean(KEY_IS_UNLOCKED);
        unlockUntil = savedInstanceState.getLong(KEY_UNLOCK_UNTIL);
        if (savedInstanceState.getByte(KEY_ACTIVE_LIST_IS_DUMMY) != 0) {
            activeListId = null;
        } else {
            activeListId = savedInstanceState.getInt(KEY_ACTIVE_LIST);
        }
    }

    private void authAndGuiInit(final Bundle savedInstanceState) {

        if (PinUtil.hasPin(this) && !this.isUnlocked && (this.unlockUntil == -1 || System.currentTimeMillis() > this.unlockUntil)) {
            final PinDialog dialog = new PinDialog(this);
            dialog.setDialogCallback(new PinDialog.PinCallback() {
                @Override
                public void accepted() {
                    initActivityStage1(savedInstanceState);
                }

                @Override
                public void declined() {
                    finishAffinity();
                }

                @Override
                public void resetApp() {
                    PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().clear().commit();
                    model.deleteAllData(null);
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    dialog.dismiss();
                    startActivity(intent);
                }
            });
            dialog.show();
        } else {
            initActivityStage1(savedInstanceState);
        }
    }

    private void initActivityStage1(Bundle savedInstanceState) {
        this.isUnlocked = true;

        model.getAllToDoLists(todoLists -> {
            this.todoLists = todoLists;
            initActivityStage2(savedInstanceState);
        });
    }

    private void initActivityStage2(Bundle savedInstanceState) {
        Bundle extras = getIntent().getExtras();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        showAllTasks();
        //currentFragment = fragmentManager.findFragmentByTag(KEY_FRAGMENT_CONFIG_CHANGE_SAVE);

        // check if app was started by clicking on a reminding notification
        if (extras != null && TodoTasksFragment.KEY.equals(extras.getString(KEY_SELECTED_FRAGMENT_BY_NOTIFICATION))) {
            TodoTask dueTask = extras.getParcelable(PARCELABLE_KEY_FOR_TODO_TASK);
            Bundle bundle = new Bundle();
            Integer listId;
            if (null != dueTask) {
                listId = dueTask.getListId();
            } else {
                listId = null;
                Log.e(TAG, "Failed to get todo task from parcelable after click on reminding notification.");
            }
            if (listId != null) {
                bundle.putInt(KEY_SELECTED_LIST_ID_BY_NOTIFICATION, listId);
            } else {
                bundle.putByte(KEY_SELECTED_DUMMY_LIST_BY_NOTIFICATION, (byte) 0);
            }
            bundle.putBoolean(TodoTasksFragment.SHOW_FLOATING_BUTTON, true);
            currentFragment = new TodoTasksFragment();
            currentFragment.setArguments(bundle);
        } else {


            if (currentFragment == null) {
                showAllTasks();
                Log.i(TAG, "Activity was not retained.");

            } else {

                // restore state before configuration change
                if (savedInstanceState != null) {
                    todoLists = savedInstanceState.getParcelableArrayList(KEY_TODO_LISTS);
                    clickedList = (TodoList) savedInstanceState.get(KEY_CLICKED_LIST);
                    dummyList = (TodoList) savedInstanceState.get(KEY_DUMMY_LIST);
                } else {
                    Log.i(TAG, "Could not restore old state because savedInstanceState is null.");
                }

                Log.i(TAG, "Activity was retained.");
            }
        }

        guiSetup();
        this.isInitialized = true;
        this.inList = false;
    }



    public void onStart() {
        super.onStart();
        uncheckNavigationEntries();
        if (navigationView != null){
            navigationView.getMenu().getItem(0).setChecked(true);
        }
    }



    private void guiSetup() {

        // toolbar setup
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // side menu setup
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        addListToNav();

        //LinearLayout l = (LinearLayout) findViewById(R.id.footer);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationBottomView = (NavigationView) findViewById(R.id.nav_view_bottom);
        navigationView.setNavigationItemSelectedListener(this);
        navigationBottomView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().getItem(0).setChecked(true);

    }



    public void uncheckNavigationEntries() {
        // uncheck all navigation entries
        if (navigationView != null) {
            int size = navigationView.getMenu().size();
            for (int i = 0; i < size; i++) {
                navigationView.getMenu().getItem(i).setChecked(false);
            }
            Log.i(TAG, "Navigation entries unchecked.");
        }

        if (navigationBottomView != null) {
            int size = navigationBottomView.getMenu().size();
            for (int i = 0; i < size; i++) {
                navigationBottomView.getMenu().getItem(i).setChecked(false);
            }
            Log.i(TAG, "Navigation-Bottom entries unchecked.");
        }
    }



    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            uncheckNavigationEntries();
            Intent intent = new Intent(this, Settings.class);
            this.unlockUntil = System.currentTimeMillis() + UnlockPeriod;
            startActivity(intent);
        } else if (id == R.id.nav_tutorial) {
            uncheckNavigationEntries();
            Intent intent = new Intent(this, TutorialActivity.class);
            this.unlockUntil = System.currentTimeMillis() + UnlockPeriod;
            startActivity(intent);
        } else if (id == R.id.menu_calendar_view) {
            uncheckNavigationEntries();
            Intent intent = new Intent(this, CalendarActivity.class);
            this.unlockUntil = System.currentTimeMillis() + UnlockPeriod;
            startActivity(intent);
        } else if (id == R.id.nav_recycle_bin) {
            uncheckNavigationEntries();
            Intent intent = new Intent(this, RecyclerActivity.class);
            this.unlockUntil = System.currentTimeMillis() + UnlockPeriod;
            startActivity(intent);
        } else if (id == R.id.nav_about) {
            uncheckNavigationEntries();
            Intent intent = new Intent(this, AboutActivity.class);
            this.unlockUntil = System.currentTimeMillis() + UnlockPeriod;
            startActivity(intent);
        } else if (id == R.id.nav_help) {
            uncheckNavigationEntries();
            Intent intent = new Intent(this, HelpActivity.class);
            this.unlockUntil = System.currentTimeMillis() + UnlockPeriod;
            startActivity(intent);
        } else if (id == R.id.menu_home) {
            uncheckNavigationEntries();
            inList = false;
            showAllTasks();
            toolbar.setTitle(R.string.home);
            item.setCheckable(true);
            item.setChecked(true);
        } else if (id == R.id.nav_dummy1 || id == R.id.nav_dummy2 || id == R.id.nav_dummy3) {
            if (!inList) {
                uncheckNavigationEntries();
                navigationView.getMenu().getItem(0).setChecked(true);
            }
            return false;
        } else{
            showTasksOfList(id);
            toolbar.setTitle(item.getTitle());
            item.setChecked(true);
        }
        DrawerLayout drawer = (DrawerLayout) this.findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }



    @Override
    protected void onStop() {
        this.isUnlocked = false;
        super.onStop();
    }



    @Override
    protected void onRestart() {
        super.onRestart();
    }



    @Override
    protected void onResume() {
        super.onResume();

        // Check if Pomodoro is installed
        pomodoroInstalled = checkIfPomodoroInstalled();

        if (this.isInitialized && !this.isUnlocked && (this.unlockUntil == -1 || System.currentTimeMillis() > this.unlockUntil)) {
            // restart activity to show pin dialog again
            //Intent intent = new Intent(this, MainActivity.class);
            //finish();
            //startActivity(intent);
            if (reminderService == null) {
                bindToReminderService();
            }
            guiSetup();
            if (activeListId != null) {
                showTasksOfList(activeListId);
            } else {
                showAllTasks();
            }
            return;
        }

        // isUnlocked might be false when returning from another activity. set to true if the unlock period was not expired:
        this.isUnlocked = (this.isUnlocked || (this.unlockUntil != -1 && System.currentTimeMillis() <= this.unlockUntil));
        this.unlockUntil = -1;

        if (reminderService == null) {
            bindToReminderService();
        };

        Log.i(TAG, "onResume()");
    }



    @Override
    protected void onDestroy() {

        if (reminderService != null) {
            unbindService(reminderServiceConnection);
            reminderService = null;
            Log.i(TAG, "service is now null");
        }

        super.onDestroy();
    }



    @Override
    protected void onUserLeaveHint() {
        // prevents unlocking the app by rotating while the app is inactive and then returning
        this.isUnlocked = false;
    }



    private void bindToReminderService() {

        Log.i(TAG, "bindToReminderService()");

        Intent intent = new Intent(this, ReminderService.class);
        bindService(intent, reminderServiceConnection, 0); // no Context.BIND_AUTO_CREATE, because service will be started by startService and thus live longer than this activity
        startService(intent);
    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (inList){
            showAllTasks();
            toolbar.setTitle(R.string.home);
            inList = false;
            uncheckNavigationEntries();
            navigationView.getMenu().getItem(0).setChecked(true);
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
            super.onBackPressed();
        }
    }


    private final ServiceConnection reminderServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.d("ServiceConnection", "connected");
            reminderService = ((ReminderService.ReminderServiceBinder) binder).getService();
        }
        //binder comes from server to communicate with method's of

        public void onServiceDisconnected(ComponentName className) {
            Log.d("ServiceConnection", "disconnected");
            reminderService = null;
        }
    };


    public void setDummyList(TodoList dummyList) {
        this.dummyList = dummyList;
    }

    public TodoList getDummyList() {
        return dummyList;
    }


    public TodoList getClickedList() {
        return clickedList;
    }


    public void setClickedList(TodoList clickedList) {
        this.clickedList = clickedList;
    }


    public void notifyReminderService(TodoTask currentTask) {

        // TODO This method is called from other fragments as well (e.g. after opening MainActivity by reminder). In such cases the service is null and alarms cannot be updated. Fix this!
        if (reminderService != null) {

            // Report changes to the reminder task if the reminder time is prior to the deadline or if no deadline is set at all. The reminder time must always be after the the current time. The task must not be completed.
            if ((currentTask.getReminderTime() < currentTask.getDeadline() || !currentTask.hasDeadline())  && !currentTask.isDone()) {
                reminderService.processTodoTask(currentTask.getId());
                Log.i(TAG, "Reminder is set!");
            } else {
                Log.i(TAG, "Reminder service was not informed about the task " + currentTask.getName());
            }

        } else {
            Log.i(TAG, "Service is null. Cannot update alarms");
        }
    }

    public List<TodoList> getTodoLists() {
        return todoLists;
    }



    //Adds To do-Lists to the navigation-drawer
    private void addListToNav() {
        NavigationView nv = (NavigationView) findViewById(R.id.nav_view);
        Menu navMenu = nv.getMenu();
        navMenu.clear();

        MenuInflater mf = new MenuInflater(getApplicationContext());
        mf.inflate(R.menu.nav_content, navMenu);

        ArrayList<TodoList> help = new ArrayList<>();
        help.addAll(todoLists);

        for (int i = 0; i < help.size(); i++) {
            String name = help.get(i).getName();
            int id = help.get(i).getId();
            MenuItem item = navMenu.add(R.id.drawer_group2, id, 1, name);
            item.setCheckable(true);
            item.setIcon(R.drawable.ic_label_black_24dp);
            ImageButton v = new ImageButton(this, null, R.style.BorderlessButtonStyle);
            v.setImageResource(R.drawable.ic_delete_black_24dp);
            v.setOnClickListener(new OnCustomMenuItemClickListener(help.get(i).getId(), MainActivity.this));
            item.setActionView(v);
        }
    }



    // Method to add a new To do-List
    private void startListDialog() {
        model.getAllToDoLists(todoLists -> {
            this.todoLists = todoLists;
            adapter = new TodoListAdapter(this, todoLists);

            ProcessTodoListDialog pl = new ProcessTodoListDialog(this);
            pl.setDialogCallback(todoList -> {
                todoLists.add(todoList);
                adapter.updateList(todoLists); // run filter again
                adapter.notifyDataSetChanged();
                model.saveTodoListInDb(todoList, counter -> {
                    hints();
                    addListToNav();
                    Log.i(TAG, "List added");
                });
            });
            pl.show();
        });
    }



    // Method starting tutorial
    private void startTut() {
        Intent intent = new Intent(MainActivity.this, TutorialActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }



    // ClickListener to delete a To-Do list
    public class OnCustomMenuItemClickListener implements View.OnClickListener {
        private final int id;
        private final Context context;

        OnCustomMenuItemClickListener(int id, Context context) {
            this.id = id;
            this.context = context;
        }

        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
            builder1.setMessage(R.string.alert_listdelete);
            builder1.setCancelable(true);

            builder1.setPositiveButton(
                    R.string.alert_delete_yes,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int setId) {
                            model.deleteTodoList(id, null);
                            dialog.cancel();
                            Intent intent = new Intent (context, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    });

            builder1.setNegativeButton(
                    R.string.alert_delete_no,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();
            return;
        }
    }



    private void showAllTasks() {
        model.getAllToDoTasks(todoTasks -> {
            expandableTodoTaskAdapter = new ExpandableTodoTaskAdapter(this, model, todoTasks);

            exLv.setOnItemLongClickListener((parent, view, position, id) -> {
                final int groupPosition = ExpandableListView.getPackedPositionGroup(id);

                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    final int childPosition = ExpandableListView.getPackedPositionChild(id);
                    expandableTodoTaskAdapter.setLongClickedSubtaskByPos(groupPosition, childPosition);
                } else {
                    expandableTodoTaskAdapter.setLongClickedTaskByPos(groupPosition);
                }
                registerForContextMenu(exLv);
                return false;
            });
            exLv.setAdapter(expandableTodoTaskAdapter);
            exLv.setEmptyView(tv);
            optionFab.setVisibility(View.VISIBLE);
            initFAB(null);
            hints();
        });
    }

    private void showTasksOfList(int listId) {
        activeListId = listId;
        inList = true;

        uncheckNavigationEntries();
        if (navigationView != null) {
            for (int i = 0; i < navigationView.getMenu().size(); ++i) {
                final MenuItem item = navigationView.getMenu().getItem(i);
                if (item.getItemId() == listId) {
                    item.setChecked(true);
                    toolbar.setTitle(item.getTitle());
                    Log.i(TAG, "Active navigation entry checked.");
                    break;
                }
            }
        }

        model.getToDoListById(listId, todoList -> {
            List<TodoTask> todoListTasks = (null != todoList) ? todoList.getTasks() : new ArrayList<>();
            expandableTodoTaskAdapter = new ExpandableTodoTaskAdapter(this, model, todoListTasks);
            exLv.setAdapter(expandableTodoTaskAdapter);
            exLv.setEmptyView(tv);
            optionFab.setVisibility(View.VISIBLE);
            initFAB(listId);
        });
    }

    // todoListId != 0 means id is given from list. otherwise new task was created in all-tasks.
    private void initFAB(Integer todoListId) {
        optionFab.setOnClickListener(v -> {
            ProcessTodoTaskDialog pt = new ProcessTodoTaskDialog(MainActivity.this, todoLists);
            pt.setListSelector(todoListId);
            pt.setDialogCallback(todoTask -> {
                model.saveTodoTaskInDb(todoTask, counter -> {
                    notifyReminderService(todoTask);
                    hints();
                    // show List if created in certain list, else show all tasks
                    if (null != todoListId) {
                        showTasksOfList(todoListId);
                    } else {
                        showAllTasks();
                    }
                });
            });
            pt.show();
        });
    }



    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {

        ExpandableListView.ExpandableListContextMenuInfo info =
                (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        MenuInflater inflater = this.getMenuInflater();
        menu.setHeaderView(Helper.getMenuHeader(getBaseContext(), getBaseContext().getString(R.string.select_option)));

        int workItemId;
        // context menu for child items
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            inflater.inflate(R.menu.todo_subtask_long_click, menu);
            workItemId = R.id.work_subtask;
        } else { // context menu for group items
            inflater.inflate(R.menu.todo_task_long_click, menu);
            workItemId = R.id.work_task;
        }

        if (pomodoroInstalled) {
            menu.findItem(workItemId).setVisible(true);
        }
    }



    public boolean onContextItemSelected(@NonNull MenuItem item) {
        final Tuple<TodoTask, TodoSubtask> longClickedTodo = expandableTodoTaskAdapter.getLongClickedTodo();
        if (null != longClickedTodo) {
            final TodoTask todoTask = longClickedTodo.getLeft();
            final TodoSubtask todoSubtask = longClickedTodo.getRight();

            switch (item.getItemId()) {
                case R.id.change_subtask:
                    final ProcessTodoSubtaskDialog dialog = new ProcessTodoSubtaskDialog(this, todoSubtask);
                    dialog.titleEdit();
                    dialog.setDialogCallback(todoSubtask2 -> {
                        model.saveTodoSubtaskInDb(todoSubtask2, counter -> {
                            expandableTodoTaskAdapter.notifyDataSetChanged();
                            Log.i(TAG, "Subtask altered");
                        });
                    });
                    dialog.show();
                    break;

                case R.id.delete_subtask:
                    model.deleteTodoSubtask(todoSubtask, counter -> {
                        todoTask.getSubtasks().remove(todoSubtask);
                        if (counter == 1)
                            Toast.makeText(getBaseContext(), getString(R.string.subtask_removed), Toast.LENGTH_SHORT).show();
                        else
                            Log.d(TAG, "Subtask was not removed from the database. Maybe it was not added beforehand (then this is no error)?");
                        expandableTodoTaskAdapter.notifyDataSetChanged();
                    });
                    break;

                case R.id.change_task:
                    final Integer oldListId = todoTask.getListId();
                    final ProcessTodoTaskDialog editTaskDialog = new ProcessTodoTaskDialog(this, todoLists, todoTask);
                    editTaskDialog.titleEdit();
                    editTaskDialog.setListSelector(oldListId);
                    editTaskDialog.setDialogCallback(todoTask2 -> {
                        model.saveTodoTaskInDb(todoTask2, counter -> {
                            notifyReminderService(todoTask2);
                            expandableTodoTaskAdapter.notifyDataSetChanged();
                            if (inList && oldListId != null) {
                                showTasksOfList(oldListId);
                            } else {
                                showAllTasks();
                            }
                        });
                    });
                    editTaskDialog.show();
                    break;

                case R.id.delete_task:
                    Snackbar snackbar = Snackbar.make(optionFab, R.string.task_removed, Snackbar.LENGTH_LONG);

                    snackbar.setAction(R.string.snack_undo, v -> {
                        model.setTaskAndSubtasksInRecycleBin(todoTask, false, counter -> {
                            if (inList && todoTask.getListId() != null) {
                                showTasksOfList(todoTask.getListId());
                            } else {
                                showAllTasks();
                            }
                            hints();
                        });
                    });

                    model.setTaskAndSubtasksInRecycleBin(todoTask, true, counter -> {
                        if (counter == 1) {
                            hints();
                        } else {
                            Log.d(TAG, "Task was not removed from the database. Maybe it was not added beforehand (then this is no error)?");
                        }

                        // Dependent on the current View, update All-tasks or a certain List
                        if (this.inList && todoTask.getListId() != null) {
                            showTasksOfList(todoTask.getListId());
                        } else {
                            showAllTasks();
                        }

                        snackbar.show();
                    });
                    break;

                case R.id.work_task:
                    Log.i(TAG, "START TASK");
                    sendToPomodoro(todoTask);
                    break;

                case R.id.work_subtask:
                    Log.i(TAG, "START SUBTASK");
                    sendToPomodoro(todoSubtask);
                    break;

                default:
                    throw new IllegalArgumentException("Invalid menu item selected.");
            }
        }
        return super.onContextItemSelected(item);
    }

    private void sendToPomodoro(TodoTask task) {
        Intent pomodoro = new Intent(POMODORO_ACTION);
        pomodoro.putExtra("todo_id", task.getId())
                .putExtra("todo_name", task.getName())
                .putExtra("todo_description", task.getDescription())
                .putExtra("todo_progress", task.getProgress(false));
        sendToPomodoro(pomodoro);
    }

    private void sendToPomodoro(TodoSubtask subtask) {
        Intent pomodoro = new Intent(POMODORO_ACTION);
        pomodoro.putExtra("todo_id", subtask.getId())
                .putExtra("todo_name", subtask.getName())
                .putExtra("todo_description", "")
                .putExtra("todo_progress", -1);
        sendToPomodoro(pomodoro);
    }

    private void sendToPomodoro(Intent pomodoro) {
        pomodoro.setPackage("org.secuso.privacyfriendlyproductivitytimer")
                .setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        sendBroadcast(pomodoro, "org.secuso.privacyfriendlytodolist.TODO_PERMISSION");
        finish();
    }

    private void updateTodoFromPomodoro() {
        TodoTask todoRe = Model.createNewTodoTask();
        todoRe.setChangedFromPomodoro(); //Change the dbState to UPDATE_FROM_POMODORO
        //todoRe.setPriority(TodoTask.Priority.HIGH);
        todoRe.setName(getIntent().getStringExtra("todo_name"));
        todoRe.setId(getIntent().getIntExtra("todo_id", -1));
        todoRe.setProgress(getIntent().getIntExtra("todo_progress", -1));
        if (todoRe.getProgress(false) == 100) {
            // Set task as done
            todoRe.setDone(true);
            //todoRe.doneStatusChanged();
        }
        if (todoRe.getProgress(false) != -1) {
            // Update the existing entry, if no subtask
            model.saveTodoTaskInDb(todoRe, counter -> {
                notifyReminderService(todoRe);
            });
        }

        //super.onResume();
    }

    public void hints() {
        model.getNumberOfAllListsAndTasks(tuple -> {
            final int numberOfLists = tuple.getLeft();
            final int numberOfTasksNotInRecycleBin = tuple.getRight();
            Animation anim = new AlphaAnimation(0.0f, 1.0f);
            if (numberOfLists == 0 && numberOfTasksNotInRecycleBin == 0) {
                initialAlert.setVisibility(View.VISIBLE);
                anim.setDuration(1500);
                anim.setStartOffset(20);
                anim.setRepeatMode(Animation.REVERSE);
                anim.setRepeatCount(Animation.INFINITE);
                initialAlert.startAnimation(anim);
            } else /* if numberOfLists != 0 || numberOfTasksNotInRecycleBin != 0 */ {
                initialAlert.setVisibility(View.GONE);
                initialAlert.clearAnimation();
            }

            if (numberOfTasksNotInRecycleBin == 0) {
                secondAlert.setVisibility(View.VISIBLE);
                anim.setDuration(1500);
                anim.setStartOffset(20);
                anim.setRepeatMode(Animation.REVERSE);
                anim.setRepeatCount(Animation.INFINITE);
                secondAlert.startAnimation(anim);
            } else /* if numberOfTasksNotInRecycleBin != 0 */ {
                secondAlert.setVisibility(View.GONE);
                secondAlert.clearAnimation();
            }
        });
    }

    private boolean checkIfPomodoroInstalled() {
        try {
            getPackageManager().getPackageInfo("org.secuso.privacyfriendlyproductivitytimer", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}

