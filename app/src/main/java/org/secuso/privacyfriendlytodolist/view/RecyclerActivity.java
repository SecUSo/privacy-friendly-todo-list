package org.secuso.privacyfriendlytodolist.view;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.LayoutInflater;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.model.database.DBQueryHandler;
import org.secuso.privacyfriendlytodolist.model.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by Sebastian Lutz on 20.12.2017.
 */

public class RecyclerActivity extends AppCompatActivity{

    private DatabaseHelper dbhelper;
    private ExpandableListView expandableListView;
    private ExpandableTodoTaskAdapter taskAdapter;

    private ArrayList<TodoTask> todoTasks = new ArrayList<>();

    private TodoList currentList;

    private MainActivity containingActivity;

    private ExpandableListAdapter expandableListAdapter;


   @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_trash);

        if (toolbar != null) {
            toolbar.setTitle(R.string.bin_toolbar);
            toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
            setSupportActionBar(toolbar);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

        }


        dbhelper = DatabaseHelper.getInstance(this);
        ArrayList<TodoTask> tasks = new ArrayList<>();
        tasks = DBQueryHandler.getBin(dbhelper.getReadableDatabase());

        ExpandableTodoTaskAdapter expandableTodoTaskAdapter = new ExpandableTodoTaskAdapter(this, tasks);
        ArrayAdapter<TodoTask> adapter = new ArrayAdapter<TodoTask>(this, R.layout.exlv_tasks_group, tasks);

        RelativeLayout rl = (RelativeLayout) findViewById(R.id.relative_recycle);
        ExpandableListView lv = (ExpandableListView) findViewById(R.id.trash_tasks);
        TextView tv = (TextView) findViewById(R.id.rv_empty_view_no_tasks);
        lv.setAdapter(expandableTodoTaskAdapter);
        lv.setEmptyView(tv);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

}
