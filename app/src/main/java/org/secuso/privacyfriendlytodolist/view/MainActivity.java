package org.secuso.privacyfriendlytodolist.view;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.BaseTodo;
import org.secuso.privacyfriendlytodolist.model.Helper;
import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.model.TodoSubTask;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.model.database.DBQueryHandler;
import org.secuso.privacyfriendlytodolist.model.database.DatabaseHelper;
import org.secuso.privacyfriendlytodolist.model.ReminderService;
import org.secuso.privacyfriendlytodolist.view.calendar.CalendarFragment;
import org.secuso.privacyfriendlytodolist.view.dialog.PinDialog;
import org.secuso.privacyfriendlytodolist.view.dialog.WelcomeDialog;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    // Keys
    private static final String KEY_TODO_LISTS = "restore_todo_list_key_with_savedinstancestate";
    private static final String KEY_CLICKED_LIST = "restore_clicked_list_with_savedinstancestate";
    private static final String KEY_DUMMY_LIST = "restore_dummy_list_with_savedinstancestate";
    private static final String KEY_IS_UNLOCKED = "restore_is_unlocked_key_with_savedinstancestate";
    private static final String KEY_UNLOCK_UNTIL = "restore_unlock_until_key_with_savedinstancestate";
    public static final String KEY_SELECTED_FRAGMENT_BY_NOTIFICATION = "fragment_choice";
    private static final String KEY_FRAGMENT_CONFIG_CHANGE_SAVE = "current_fragment";


    // Fragment administration
    private Fragment currentFragment;
    private FragmentManager fragmentManager = getSupportFragmentManager();


    // Database administration
    private DatabaseHelper dbHelper;

    // TodoList administration
    private ArrayList<TodoList> todoLists = new ArrayList<>();
    private TodoList dummyList; // use this list if you need a container for tasks that does not exist in the database (e.g. to show all tasks, tasks of today etc.)
    private TodoList clickedList; // reference of last clicked list for fragment

    // Service that triggers notifications for upcoming tasks
    private ReminderService reminderService;

    // GUI
    private NavigationView navigationView;

    // Others
    boolean isInitialized = false;
    boolean isUnlocked = false;
    long unlockUntil = -1;
    private static final long UnlockPeriod = 30000; // keep the app unlocked for 30 seconds after switching to another activity (settings/help/about)



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = DatabaseHelper.getInstance(this);
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        authAndGuiInit(savedInstanceState);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(KEY_TODO_LISTS, todoLists);
        outState.putParcelable(KEY_CLICKED_LIST, clickedList);
        outState.putParcelable(KEY_DUMMY_LIST, dummyList);
        outState.putBoolean(KEY_IS_UNLOCKED, isUnlocked);
        outState.putLong(KEY_UNLOCK_UNTIL, unlockUntil);

        super.onSaveInstanceState(outState);
    }

    private void authAndGuiInit(final Bundle savedInstanceState) {

        if(savedInstanceState != null) {
            isUnlocked = savedInstanceState.getBoolean(KEY_IS_UNLOCKED);
            unlockUntil = savedInstanceState.getLong(KEY_UNLOCK_UNTIL);
        }

        if (!this.isUnlocked && hasPin()) {
            final PinDialog dialog = new PinDialog(this);
            dialog.setCallback(new PinDialog.PinCallback() {
                @Override
                public void accepted() {
                    initActivity(savedInstanceState);
                }

                @Override
                public void declined() {
                    finishAffinity();
                }

                @Override
                public void resetApp() {
                    PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().clear().commit();
                    dbHelper.deleteAll();
                    dbHelper.createAll();
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    dialog.dismiss();
                    startActivity(intent);
                }
            });
            dialog.show();
        } else {
            initActivity(savedInstanceState);
        }
    }

    private boolean hasPin() {
        // pin activated?
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_pin_enabled", false))
            return false;
        // pin valid?
        if (PreferenceManager.getDefaultSharedPreferences(this).getString("pref_pin", null) == null || PreferenceManager.getDefaultSharedPreferences(this).getString("pref_pin", "").length() < 4)
            return false;
        return true;
    }

    public void initActivity(Bundle savedInstanceState) {


        this.isUnlocked = true;
        getTodoLists(true);

        Bundle extras = getIntent().getExtras();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        currentFragment = fragmentManager.findFragmentByTag(KEY_FRAGMENT_CONFIG_CHANGE_SAVE);

        // check if app was started by clicking on a reminding notification
        if (extras != null && TodoTasksFragment.KEY.equals(extras.getString(KEY_SELECTED_FRAGMENT_BY_NOTIFICATION))) {
            TodoTask dueTask = extras.getParcelable(TodoTask.PARCELABLE_KEY);
            Bundle bundle = new Bundle();
            bundle.putInt(TodoList.UNIQUE_DATABASE_ID, dueTask.getListId());
            bundle.putBoolean(TodoTasksFragment.SHOW_FLOATING_BUTTON, true);
            currentFragment = new TodoTasksFragment();
            currentFragment.setArguments(bundle);
        } else {


            if(currentFragment == null) {
                currentFragment = new TodoListsFragment();
                Log.i(TAG, "Activity was not retained.");

            } else {

                // restore state before configuration change
                if(savedInstanceState != null) {
                    todoLists = savedInstanceState.getParcelableArrayList(KEY_TODO_LISTS);
                    clickedList = (TodoList) savedInstanceState.get(KEY_CLICKED_LIST);
                    dummyList = (TodoList) savedInstanceState.get(KEY_DUMMY_LIST);
                } else {
                    Log.i(TAG, "Could not restore old state because savedInstanceState is null.");
                }

                Log.i(TAG, "Activity was retained.");
            }
        }

        boolean firstStart = preferences.getBoolean("firstStart", true);
        if (firstStart) {
            preferences.edit().putBoolean("firstStart", false).commit();
            WelcomeDialog dialog = new WelcomeDialog();
            dialog.show(getFragmentManager(), "WelcomeDialog");
        }

        guiSetup();
        setFragment(currentFragment);
        this.isInitialized = true;
    }


    public void onStart() {
        super.onStart();
        uncheckNavigationEntries();
    }


    public void setFragment(Fragment fragment) {

        if(fragment == null)
            return;

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // If a fragment is currently displayed, replace it by the new one.
        List<Fragment> addedFragments = fragmentManager.getFragments();
        if(addedFragments != null && addedFragments.size() > 0 ) {
            transaction.replace(R.id.fragment_container, fragment, KEY_FRAGMENT_CONFIG_CHANGE_SAVE);
        } else { // no fragment is currently displayed
            transaction.add(R.id.fragment_container, fragment, KEY_FRAGMENT_CONFIG_CHANGE_SAVE);
        }

        transaction.addToBackStack(null);
        transaction.commit();
        fragmentManager.executePendingTransactions();

    }

    private void guiSetup() {

        // toolbar setup
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // side menu setup
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void uncheckNavigationEntries() {
        // uncheck all navigtion entries
        if(navigationView != null) {
            int size = navigationView.getMenu().size();
            for (int i = 0; i < size; i++) {
                navigationView.getMenu().getItem(i).setChecked(false);
            }

            Log.i(TAG, "Navigation entries unchecked.");
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, Settings.class);
            this.unlockUntil = System.currentTimeMillis() + UnlockPeriod;
            startActivity(intent);
        } else if (id == R.id.menu_calendar_view) {
            CalendarFragment fragment = new CalendarFragment();
            setFragment(fragment);
        } else if (id == R.id.menu_show_all_tasks) {

            // create a dummy list containing all tasks
            ArrayList<TodoTask> allTasks = new ArrayList<>();
            for (TodoList currentList : todoLists)
                allTasks.addAll(currentList.getTasks());

            dummyList = new TodoList();
            dummyList.setDummyList();
            dummyList.setName(getString(R.string.all_tasks));
            dummyList.setTasks(allTasks);

            TodoTasksFragment fragment = new TodoTasksFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(TodoList.UNIQUE_DATABASE_ID, dummyList.getId());
            bundle.putBoolean(TodoTasksFragment.SHOW_FLOATING_BUTTON, false);
            fragment.setArguments(bundle);
            setFragment(fragment);

        } else if (id == R.id.nav_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            this.unlockUntil = System.currentTimeMillis() + UnlockPeriod;
            startActivity(intent);
        } else if (id == R.id.nav_help) {
            Intent intent = new Intent(this, HelpActivity.class);
            this.unlockUntil = System.currentTimeMillis() + UnlockPeriod;
            startActivity(intent);
        } else if (id == R.id.menu_home) {
            TodoListsFragment fragment = new TodoListsFragment();
            setFragment(fragment);
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
    protected void onResume() {
        if(this.isInitialized && !this.isUnlocked && (this.unlockUntil == -1 || System.currentTimeMillis() > this.unlockUntil)) {
            // restart activity to show pin dialog again
            Intent intent = new Intent(this, MainActivity.class);
            finish();
            startActivity(intent);
            super.onResume();
            return;
        }
        // isUnlocked might be false when returning from another activity. set to true if the unlock period was not expired:
        this.isUnlocked = (this.isUnlocked || (this.unlockUntil != -1 && this.unlockUntil <= System.currentTimeMillis()));
        this.unlockUntil = -1;

        if (reminderService == null) {
            bindToReminderService();
        }
        super.onResume();

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
        } else {
            super.onBackPressed();
        }
    }

    public ArrayList<TodoList> getTodoLists(boolean reload) {
        if (reload) {
            if (dbHelper != null)
                todoLists = DBQueryHandler.getAllToDoLists(dbHelper.getReadableDatabase());
        }

        return todoLists;
    }


    public DatabaseHelper getDbHelper() {
        return dbHelper;
    }

    public void setDummyList(TodoList dummyList) {
        this.dummyList = dummyList;
    }

    private ServiceConnection reminderServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.d("ServiceConnection","connected");
            reminderService = ((ReminderService.ReminderServiceBinder) binder).getService();
        }
        //binder comes from server to communicate with method's of

        public void onServiceDisconnected(ComponentName className) {
            Log.d("ServiceConnection","disconnected");
            reminderService = null;
        }
    };

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

        if(reminderService != null) {

            // Report changes to the reminder task if the reminder time is prior to the deadline or if no deadline is set at all. The reminder time must always be after the the current time. The task must not be completed.
            if((currentTask.getReminderTime() < currentTask.getDeadline() || !currentTask.hasDeadline()) && currentTask.getReminderTime() >= Helper.getCurrentTimestamp() && !currentTask.getDone()) {
                reminderService.processTask(currentTask);
            } else {
                Log.i(TAG, "Reminder service was not informed about the task " + currentTask.getName());
            }

        } else {
            Log.i(TAG, "Service is null. Cannot update alarms");
        }
    }

    // returns true if object was created in the database
    public boolean sendToDatabase(BaseTodo todo) {

        int databaseID = -5;
        String errorMessage = "";

        // call appropriate method depending on type
        if(todo instanceof TodoList) {
            databaseID = DBQueryHandler.saveTodoListInDb(dbHelper.getWritableDatabase(), (TodoList) todo);
            errorMessage = getString(R.string.list_to_db_error);
        } else if (todo instanceof TodoTask) {
            databaseID = DBQueryHandler.saveTodoTaskInDb(dbHelper.getWritableDatabase(), (TodoTask) todo);
            errorMessage = getString(R.string.task_to_db_error);
        } else if (todo instanceof TodoSubTask) {
            databaseID = DBQueryHandler.saveTodoSubTaskInDb(dbHelper.getWritableDatabase(), (TodoSubTask) todo);
            errorMessage = getString(R.string.subtask_to_db_error);
        } else {
            throw new IllegalArgumentException("Cannot save unknown descendant of BaseTodo in the database.");
        }

        // set unique database id (primary key) to the current object
        if(databaseID == -1) {
            Log.e(TAG, errorMessage);
            return false;
        }
        else if(databaseID != DBQueryHandler.NO_CHANGES){
            todo.setId(databaseID);
            return true;
        }

        return false;
    }

    public TodoList getListByID(int id) {
        for(TodoList currentList : todoLists) {
            if(currentList.getId() == id)
                return currentList;
        }

        return null;
    }
}
