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
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.ModelServices;
import org.secuso.privacyfriendlytodolist.model.TodoSubtask;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.model.Tuple;
import org.secuso.privacyfriendlytodolist.util.Helper;
import org.secuso.privacyfriendlytodolist.viewmodel.LifecycleViewModel;

/**
 * Created by Sebastian Lutz on 20.12.2017.
 *
 * This Activity handles deleted tasks in a kind of recycle bin.
 */
public class RecyclerActivity extends AppCompatActivity{

    private ModelServices model;
    private TextView tv;
    private ExpandableListView lv;
    private ExpandableTodoTaskAdapter expandableTodoTaskAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LifecycleViewModel viewModel = new ViewModelProvider(this).get(LifecycleViewModel.class);
        model = viewModel.getModel();

        setContentView(R.layout.activity_recycle);

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
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        final Tuple<TodoTask, TodoSubtask> longClickedTodo = expandableTodoTaskAdapter.getLongClickedTodo();
        if (null != longClickedTodo) {
            final TodoTask todoTask = longClickedTodo.getLeft();

            switch (item.getItemId()) {
                case R.id.restore:
                    model.setTaskAndSubtasksInTrash(todoTask, false, counter -> {
                        updateAdapter();
                    });
                    break;
            }
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
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage(R.string.alert_clear);
                builder1.setCancelable(true);

                builder1.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        model.clearBin(counter -> {
                            dialog.cancel();
                            updateAdapter();
                        });
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.trash_clear, menu);
        return true;
    }

    private void updateAdapter() {
        model.getBin(todoTasks -> {
            expandableTodoTaskAdapter = new ExpandableTodoTaskAdapter(this, model, todoTasks);
            lv.setAdapter(expandableTodoTaskAdapter);
            lv.setEmptyView(tv);
            lv.setOnItemLongClickListener((parent, view, position, id) -> {
                int groupPosition = ExpandableListView.getPackedPositionGroup(id);

                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {

                    int childPosition = ExpandableListView.getPackedPositionChild(id);
                    expandableTodoTaskAdapter.setLongClickedSubtaskByPos(groupPosition, childPosition);
                } else {
                    expandableTodoTaskAdapter.setLongClickedTaskByPos(groupPosition);
                }
                registerForContextMenu(lv);
                return false;
            });
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        super.onBackPressed();
    }
}
