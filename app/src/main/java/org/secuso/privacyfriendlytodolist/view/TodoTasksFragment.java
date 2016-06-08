package org.secuso.privacyfriendlytodolist.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.model.TodoTask;

/**
 * Created by dominik on 24.05.16.
 */
public class TodoTasksFragment extends Fragment {

    private static final String TAG = TodoTasksFragment.class.getSimpleName();

    private ExpandableListView expandableListView;
    private ExpandableToDoTaskAdapter taskAdapter;

    private TodoList listToShow;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        listToShow = getArguments().getParcelable(TodoList.PARCELABLE_ID);

        View v = inflater.inflate(R.layout.todo_list_detailed, container, false);

        initExListViewGUI(v);

        // set toolbar title
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(listToShow.getName());

        return v;
    }

    private void initExListViewGUI(View v) {

        taskAdapter = new ExpandableToDoTaskAdapter(getActivity(), listToShow.getTasks());
        TextView emptyView = (TextView) v.findViewById(R.id.tv_empty_view_no_tasks);
        expandableListView = (ExpandableListView) v.findViewById(R.id.exlv_tasks);

        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
                                                        @Override
                                                        public void onGroupExpand(int groupPosition) {

                                                        }
                                                    }
        );

        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                Object vh = v.getTag();

                if(vh != null && vh instanceof ExpandableToDoTaskAdapter.GroupTaskViewHolder) {

                    ExpandableToDoTaskAdapter.GroupTaskViewHolder viewHolder = (ExpandableToDoTaskAdapter.GroupTaskViewHolder) vh;

                    if(viewHolder.seperator.getVisibility() == View.GONE) {
                        viewHolder.seperator.setVisibility(View.VISIBLE);
                    } else {
                        viewHolder.seperator.setVisibility(View.GONE);
                    }
                }

                return false;

            }
        });

        expandableListView.setEmptyView(emptyView);
        expandableListView.setAdapter(taskAdapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        boolean checked;
        ExpandableToDoTaskAdapter.SortTypes sortType = null;

        switch (item.getItemId()) {
            case R.id.ac_show_all_tasks:
                taskAdapter.setFilter(ExpandableToDoTaskAdapter.Filter.ALL_TASKS);
                taskAdapter.notifyDataSetChanged();
                return true;
            case R.id.ac_show_open_tasks:
                taskAdapter.setFilter(ExpandableToDoTaskAdapter.Filter.OPEN_TASKS);
                taskAdapter.notifyDataSetChanged();
                return true;
            case R.id.ac_show_completed_tasks:
                taskAdapter.setFilter(ExpandableToDoTaskAdapter.Filter.COMPLETED_TASKS);
                taskAdapter.notifyDataSetChanged();
                return true;
            case R.id.ac_group_by_prio:
                checked = !item.isChecked();
                item.setChecked(checked);
                sortType = ExpandableToDoTaskAdapter.SortTypes.PRIORITY;
                break;
            case R.id.ac_sort_by_deadline:
                checked = !item.isChecked();
                item.setChecked(checked);
                sortType = ExpandableToDoTaskAdapter.SortTypes.DEADLINE;
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        if(sortType != null) {
            if(checked)
                taskAdapter.addSortCondition(sortType);
            else
                taskAdapter.removeSortCondition(sortType);
            taskAdapter.notifyDataSetChanged();
        }

        return true;
    }
}
