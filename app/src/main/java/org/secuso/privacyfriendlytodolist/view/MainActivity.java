package org.secuso.privacyfriendlytodolist.view;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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

import java.util.ArrayList;


/*
    TODO Maintain a list which modified objects to avoid checking all objects if they need to be rewritten to the database.
 */


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String FRAGMENT_CHOICE = "fragment_choice";

    private TodoList dummyList; // use this list if you need a container for tasks that does not exist in the database (e.g. to show all tasks, tasks of today etc.)
    private ArrayList<TodoList> todoLists = new ArrayList<>();

    private DatabaseHelper dbHelper;
    private ReminderService reminderService;

    // reference to clicked list set by TodoListsFragment.
    private TodoList clickedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        securityCheck();
    }

    private void securityCheck() {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_pin_enabled", false)) {
            PinDialog dialog = new PinDialog(this);
            dialog.setCallback(new PinDialog.PinCallback() {
                @Override
                public void accepted() {
                    initActivity();
                }

                @Override
                public void declined() {
                    finish();
                }
            });
            dialog.show();
        } else {
            initActivity();
        }
    }

    void initActivity() {

        dbHelper = DatabaseHelper.getInstance(this);
        getTodoLists(true);

        Bundle extras = getIntent().getExtras();

        if (extras != null && TodoTasksFragment.KEY.equals(extras.getString(FRAGMENT_CHOICE))) {
            TodoTask dueTask = extras.getParcelable(TodoTask.PARCELABLE_KEY);
            Bundle bundle = new Bundle();
            bundle.putInt(TodoList.UNIQUE_DATABASE_ID, dueTask.getListId());
            bundle.putBoolean(TodoTasksFragment.SHOW_FLOATING_BUTTON, true);
            TodoTasksFragment fragment = new TodoTasksFragment();
            fragment.setArguments(bundle);
            setFragment(fragment);
        } else {
            TodoListsFragment todoListOverviewFragment = new TodoListsFragment();
            setFragment(todoListOverviewFragment);
        }

        guiSetup();
    }

    public void setFragment(Fragment fragment) {

        if(fragment == null)
            return;
        // Check that the activity is using the layout version with the fragment_container FrameLayout

        if (findViewById(R.id.fragment_container) != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            if (fragmentManager.getFragments() == null) {
                fragmentTransaction.add(R.id.fragment_container, fragment);
            } else {
                if (fragmentManager.getFragments().size() > 0) {
                    Fragment currentFragment = fragmentManager.getFragments().get(fragmentManager.getFragments().size() - 1);
                    // current fragment has the same type as new fragment? nothing to do
                    if (currentFragment != null && currentFragment.isVisible() && currentFragment.getClass().equals(fragment.getClass()))
                        return;
                }
                // find another fragment of the same type
                Fragment oldFragment = null;
                for (Fragment f : fragmentManager.getFragments()) {
                    if (f != null && f.getClass().equals((fragment.getClass()))) {
                        oldFragment = f;
                        break;
                    }
                }
                if (oldFragment != null) {
                    // another fragment of this type found? just switch to this one
                    fragment = oldFragment;
                }
                //fragmentTransaction.setCustomAnimations(R.anim.fragment_slide_in, R.anim.fragment_slide_out);
                fragmentTransaction.replace(R.id.fragment_container, fragment);
                fragmentTransaction.addToBackStack(null);
            }

            fragmentTransaction.commit();
        }

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
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, Settings.class);
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
    protected void onResume() {

        if (reminderService == null) {
            bindToReminderService();
        }
        super.onResume();
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
        if (reload)
            todoLists = DBQueryHandler.getAllToDoLists(dbHelper.getReadableDatabase());

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

            // The first two conditions should be always true as it is not possible to set a reminder time which violates theses conditions.
            if(currentTask.getDeadline() > 0 && currentTask.getReminderTime() < currentTask.getDeadline() && currentTask.getReminderTime() >= Helper.getCurrentTimestamp()) {
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
