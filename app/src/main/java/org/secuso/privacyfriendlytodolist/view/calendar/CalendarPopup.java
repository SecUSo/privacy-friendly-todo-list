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

package org.secuso.privacyfriendlytodolist.view.calendar;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.ModelServices;
import org.secuso.privacyfriendlytodolist.model.TodoTask;
import org.secuso.privacyfriendlytodolist.view.ExpandableTodoTaskAdapter;
import org.secuso.privacyfriendlytodolist.viewmodel.LifecycleViewModel;

import java.util.ArrayList;

/**
 * Created by sebbi on 07.03.2018.
 *
 * This class helps to show the tasks that are on a specific deadline
 */

public class CalendarPopup extends AppCompatActivity {

    private ExpandableListView lv;
    private ExpandableTodoTaskAdapter expandableTodoTaskAdapter;
    private ArrayList<TodoTask> tasks = new ArrayList<>();
    private ModelServices model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LifecycleViewModel viewModel = new ViewModelProvider(this).get(LifecycleViewModel.class);
        model = viewModel.getModel();

        setContentView(R.layout.calendar_popup);

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

    private void updateAdapter() {
        expandableTodoTaskAdapter = new ExpandableTodoTaskAdapter(this, model, tasks);
        lv.setAdapter(expandableTodoTaskAdapter);
    }
}
