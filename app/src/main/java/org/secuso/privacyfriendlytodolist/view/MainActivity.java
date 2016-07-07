package org.secuso.privacyfriendlytodolist.view;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
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
import org.secuso.privacyfriendlytodolist.model.Helper;
import org.secuso.privacyfriendlytodolist.model.NotifyService;
import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.model.database.DBQueryHandler;
import org.secuso.privacyfriendlytodolist.model.database.DatabaseHelper;
import org.secuso.privacyfriendlytodolist.view.calendar.CalendarFragment;
import org.secuso.privacyfriendlytodolist.view.dialog.PinDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String FRAGMENT_CHOICE = "fragment_choice" ;

    private TodoList dummyList; // use this list if you need a container for tasks that does not exist in the database (e.g. to show all tasks, tasks of today etc.)
    private ArrayList<TodoList> todoLists = new ArrayList<>();

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_pin_enabled", false))
        {
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
        }
        else {
            initActivity();
        }
    }

    void initActivity() {
        dbHelper = new DatabaseHelper(this);
        getTodoLists(true);

        Bundle extras = getIntent().getExtras();

        if (extras != null && TodoTasksFragment.KEY.equals(extras.getString(FRAGMENT_CHOICE))) {
            TodoTask dueTask = extras.getParcelable(TodoTask.PARCELABLE_KEY);
            Bundle bundle = new Bundle();
            bundle.putLong(TodoList.UNIQUE_DATABASE_ID, dueTask.getListId());
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

        // Check that the activity is using the layout version with the fragment_container FrameLayout

        if (findViewById(R.id.fragment_container) != null) {


            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            if (fragmentManager.getFragments() == null) {
                fragmentTransaction.add(R.id.fragment_container, fragment);
            } else {
                fragmentTransaction.setCustomAnimations(R.anim.fragment_slide_in, R.anim.fragment_slide_out);
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

            // create a dummy list containg all tasks
            ArrayList<TodoTask> allTasks = new ArrayList<>();
            for(TodoList currentList : todoLists)
                allTasks.addAll(currentList.getTasks());

            dummyList = new TodoList();
            dummyList.setDummyList();
            dummyList.setName(getString(R.string.all_tasks));
            dummyList.setTasks(allTasks);

            TodoTasksFragment fragment = new TodoTasksFragment();
            Bundle bundle = new Bundle();
            bundle.putLong(TodoList.UNIQUE_DATABASE_ID, dummyList.getId());
            bundle.putBoolean(TodoTasksFragment.SHOW_FLOATING_BUTTON, false);
            fragment.setArguments(bundle);
            setFragment(fragment);

        } else if (id == R.id.nav_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        } else if(id == R.id.menu_home) {
            finish();
            startActivity(getIntent());
        }

        DrawerLayout drawer = (DrawerLayout) this.findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
        if(reload)
            todoLists = DBQueryHandler.getAllToDoLists(dbHelper.getReadableDatabase());

        return todoLists;
    }

    @Override
    public void onResume() {
        super.onResume();
        for(TodoList currentList : todoLists) {
            for(TodoTask currentTask : currentList.getTasks()) {
                setReminderAlarmNotifications(currentTask);
            }
        }
    }

    public DatabaseHelper getDbHelper() {
        return dbHelper;
    }

    public TodoList getListByID(long currentListID) {

        if(currentListID == TodoList.DUMMY_LIST_ID)
            return dummyList;

        for(TodoList currentList : todoLists)
            if(currentList.getId() == currentListID)
                return currentList;

        throw new IllegalArgumentException("Id " + String.valueOf(currentListID + " does not belong to any todo list."));

    }


    public void syncWithDatabase() {

        // write new data back to the database
        for(TodoList currentList : todoLists) {
            long id = DBQueryHandler.saveTodoListInDb(getDbHelper().getWritableDatabase(), currentList);
            if(id == -1)
                Log.e(TAG, getString(R.string.list_to_db_error));
            else if(id == DBQueryHandler.NO_CHANGES)
                Log.i(TAG, getString(R.string.no_changes_in_db));
            else
                currentList.setId(id);
        }
    }


    private void setReminderAlarmNotifications(TodoTask task) {

        long reminderTimeStamp = task.getReminderTime();

        if(!task.getDone() && task.getDeadline()*1000 > Helper.getCurrentTimestamp() && reminderTimeStamp > 0) {

            Intent alarmIntent = new Intent(this, NotifyService.class);
            alarmIntent.putExtra(TodoTask.PARCELABLE_KEY, task);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, alarmIntent, 0);

            Calendar calendar = Calendar.getInstance();
            Date date = new Date(reminderTimeStamp*1000);
            calendar.setTime(date);
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

            Log.i(TAG, "Alarm set for " + task.getName() + " at " + Helper.getDateTime(calendar.getTimeInMillis()/1000));

        } else {
            Log.i(TAG, "No alarm set for " + task.getName());
        }

    }

    public void setDummyList(TodoList dummyList) {
        this.dummyList = dummyList;
    }
}
