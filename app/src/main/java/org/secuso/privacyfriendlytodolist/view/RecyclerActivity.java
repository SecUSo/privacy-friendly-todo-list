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

import android.content.ClipData;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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
import android.widget.Toast;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.model.database.DBQueryHandler;
import org.secuso.privacyfriendlytodolist.model.database.DatabaseHelper;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by Sebastian Lutz on 20.12.2017.
 */

public class RecyclerActivity extends AppCompatActivity{

    private DatabaseHelper dbhelper;
    private TextView tv;
    private ExpandableListView lv;
    RelativeLayout rl;


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
            case R.id.btn_clear:
                dbhelper = DatabaseHelper.getInstance(this);
                final ArrayList<TodoTask> tasks;
                tasks = DBQueryHandler.getBin(dbhelper.getReadableDatabase());
                for ( TodoTask t : tasks){
                    DBQueryHandler.deleteTodoTask(this.dbhelper.getReadableDatabase(), t);
                }
                updateAdapter();

                Snackbar snackbar = Snackbar.make(this.findViewById(R.id.btn_clear), R.string.toast_del, Snackbar.LENGTH_LONG);
                snackbar.setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       for (TodoTask task : tasks) {
                           DBQueryHandler.saveTodoTaskInDb(dbhelper.getWritableDatabase(), task);
                       }
                       updateAdapter();
                    }
                });
                snackbar.show();

        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle);

        rl = (RelativeLayout) findViewById(R.id.relative_recycle);
        lv = (ExpandableListView) findViewById(R.id.trash_tasks);
        tv = (TextView) findViewById(R.id.bin_empty);


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
        updateAdapter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.trash_clear, menu);
        return true;
    }

    public void updateAdapter() {
        dbhelper = DatabaseHelper.getInstance(this);
        ArrayList<TodoTask> tasks;
        tasks = DBQueryHandler.getBin(dbhelper.getReadableDatabase());
        ExpandableTodoTaskAdapter expandableTodoTaskAdapter = new ExpandableTodoTaskAdapter(this, tasks);
        lv.setAdapter(expandableTodoTaskAdapter);
        lv.setEmptyView(tv);
    }

    public ArrayList<TodoTask> getTasksInTrash() {
       ArrayList<TodoTask> backup = DBQueryHandler.getBin(dbhelper.getReadableDatabase());
       return backup;
    }

}
