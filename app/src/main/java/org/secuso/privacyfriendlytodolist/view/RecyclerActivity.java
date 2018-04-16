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

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.Helper;
import org.secuso.privacyfriendlytodolist.model.TodoSubTask;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.model.Tuple;
import org.secuso.privacyfriendlytodolist.model.database.DBQueryHandler;
import org.secuso.privacyfriendlytodolist.model.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sebastian Lutz on 20.12.2017.
 *
 * This Activity handles deleted tasks in a kind of recycle bin.
 */
public class RecyclerActivity extends AppCompatActivity{

    private DatabaseHelper dbhelper;
    private TextView tv;
    private ExpandableListView lv;
    RelativeLayout rl;
    private List<TodoTask> backupTasks = new ArrayList<TodoTask>();
    private ExpandableTodoTaskAdapter expandableTodoTaskAdapter;


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
    public boolean onContextItemSelected(MenuItem item) {
        final Tuple<TodoTask, TodoSubTask> longClickedTodo = expandableTodoTaskAdapter.getLongClickedTodo();

       switch(item.getItemId()){
           case R.id.restore:
               DBQueryHandler.recoverTasks(dbhelper.getWritableDatabase(), longClickedTodo.getLeft());
               ArrayList<TodoSubTask> subTasks = longClickedTodo.getLeft().getSubTasks();
               for (TodoSubTask ts : subTasks){
                   DBQueryHandler.recoverSubtasks(dbhelper.getWritableDatabase(), ts);
               }
               updateAdapter();
               break;

       }
       return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            case R.id.btn_clear:
                dbhelper = DatabaseHelper.getInstance(this);
                final ArrayList<TodoTask> tasks;
                tasks = DBQueryHandler.getBin(dbhelper.getReadableDatabase());
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage(R.string.alert_clear);
                builder1.setCancelable(true);

                builder1.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for ( TodoTask t : tasks){
                            DBQueryHandler.deleteTodoTask(DatabaseHelper.getInstance(getBaseContext()).getReadableDatabase(), t);
                        }
                        dialog.cancel();
                        updateAdapter();
                    }
                });

                builder1.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alert = builder1.create();
                alert.show();

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        ExpandableListView.ExpandableListContextMenuInfo info =
                (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        MenuInflater inflater = this.getMenuInflater();
        menu.setHeaderView(Helper.getMenuHeader(getBaseContext(), getBaseContext().getString(R.string.select_option)));

        inflater.inflate(R.menu.deleted_task_long_click, menu);
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
        backupTasks = getTasksInTrash();
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
        expandableTodoTaskAdapter = new ExpandableTodoTaskAdapter(this, tasks);
        lv.setAdapter(expandableTodoTaskAdapter);
        lv.setEmptyView(tv);
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int groupPosition = ExpandableListView.getPackedPositionGroup(id);

                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {

                    int childPosition = ExpandableListView.getPackedPositionChild(id);
                    expandableTodoTaskAdapter.setLongClickedSubTaskByPos(groupPosition, childPosition);
                } else {
                    expandableTodoTaskAdapter.setLongClickedTaskByPos(groupPosition);
                }
                registerForContextMenu(lv);
                return false;
            }
        });
    }

    public ArrayList<TodoTask> getTasksInTrash() {
       ArrayList<TodoTask> backup = DBQueryHandler.getBin(dbhelper.getReadableDatabase());
       return backup;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        super.onBackPressed();
    }
}
