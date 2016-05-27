package org.secuso.privacyfriendlytodolist.view;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import org.secuso.privacyfriendlytodolist.R;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


		private static final String TAG = MainActivity.class.getSimpleName();


    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Is the activity restored?
        if (savedInstanceState == null) {
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
            if(fragmentManager.getFragments() == null) {
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

        // floating action button setup
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new ActionButtonListener());

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
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            Toast.makeText(this, "Settings pressed", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_help) {
            Toast.makeText(this, "Help pressed", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_about) {
            Intent aboutIntent = new Intent(this, AboutActivity.class);
            startActivity(aboutIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class DeadlineButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Dialog deadlineDialog = new Dialog(context);
            deadlineDialog.setContentView(R.layout.deadline_dialog);
            deadlineDialog.setTitle("I18N: SELECT DEADLINE");

            // required to make the dialog use the full screen width
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            Window window = deadlineDialog.getWindow();
            lp.copyFrom(window.getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.horizontalMargin = 40;
            window.setAttributes(lp);

            deadlineDialog.show();
        }
    }

    private class ActionButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Dialog addListDialog = new Dialog(context);
            addListDialog.setContentView(R.layout.add_todolist_popup);
            addListDialog.setTitle("I18N: ADD LIST");

            TextView tvDeadline = (TextView)addListDialog.findViewById(R.id.tv_todo_list_deadline);
            tvDeadline.setOnClickListener(new DeadlineButtonListener());

            // required to make the dialog use the full screen width
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            Window window = addListDialog.getWindow();
            lp.copyFrom(window.getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.horizontalMargin = 20;
            window.setAttributes(lp);

            addListDialog.show();
        }
    }
}
