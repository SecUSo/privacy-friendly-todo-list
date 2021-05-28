package org.secuso.privacyfriendlytodolist.view.calendar;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.model.database.DatabaseHelper;
import org.secuso.privacyfriendlytodolist.view.ExpandableTodoTaskAdapter;

import java.util.ArrayList;

/**
 * Created by sebbi on 07.03.2018.
 *
 * This class helps to show the tasks that are on a specific deadline
 */

public class CalendarPopup extends AppCompatActivity {

    private DatabaseHelper dbhelper;
    private ExpandableListView lv;
    RelativeLayout rl;
    private ExpandableTodoTaskAdapter expandableTodoTaskAdapter;
    private ArrayList<TodoTask> tasks = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.calendar_popup);

        rl = (RelativeLayout) findViewById(R.id.relative_deadline);
        lv = (ExpandableListView) findViewById(R.id.deadline_tasks);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_deadlineTasks);

        if (toolbar != null) {
            toolbar.setTitle(R.string.deadline);
            toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
            setSupportActionBar(toolbar);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

        }
        Bundle b = getIntent().getExtras();
        if(b != null)
            tasks = b.getParcelableArrayList("Deadlines");
        updateAdapter();


    }


    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void updateAdapter() {

        expandableTodoTaskAdapter = new ExpandableTodoTaskAdapter(this, tasks);
        lv.setAdapter(expandableTodoTaskAdapter);

    }


}




