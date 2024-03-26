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

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.TodoList;
import org.secuso.privacyfriendlytodolist.util.Helper;

import java.util.ArrayList;
import java.util.List;

public class TodoListAdapter extends  RecyclerView.Adapter<TodoListAdapter.ViewHolder>  {

    private static final String TAG = TodoListAdapter.class.getSimpleName();
    private MainActivity contextActivity;
    private SharedPreferences prefs;

    private List<TodoList> allLists;
    private String queryString;
    private List<TodoList> filteredLists;
    private int position;

    public TodoListAdapter(Activity ac, List<TodoList> data) {
        this.queryString = null;
        updateList(data);
        this.contextActivity = (MainActivity) ac;
        prefs = PreferenceManager.getDefaultSharedPreferences(ac);
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    private long getDefaultReminderTime()  {
        return new Long(prefs.getString(Settings.DEFAULT_REMINDER_TIME_KEY, String.valueOf(contextActivity.getResources().getInteger(R.integer.one_day))));
    }


    // invoked by the layout manager
    @Override
    public TodoListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.todo_list_entry, parent, false);

        return new ViewHolder(v);
    }

    // replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        TodoList list = this.filteredLists.get(this.filteredLists.size()-1-position);
        holder.title.setText(list.getName());
        if (list.getNextDeadline() <= 0)
            holder.deadline.setText(contextActivity.getResources().getString(R.string.no_next_deadline));
        else
            holder.deadline.setText(contextActivity.getResources().getString(R.string.next_deadline_dd, Helper.getDate(list.getNextDeadline())));
        holder.done.setText(String.format("%d/%d", list.getDoneTodos(), list.getSize()));
        holder.urgency.setBackgroundColor(Helper.getDeadlineColor(contextActivity, list.getDeadlineColor(getDefaultReminderTime())));

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setPosition(holder.getAdapterPosition());
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.filteredLists.size();
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        holder.itemView.setOnLongClickListener(null);
        super.onViewRecycled(holder);
    }

    public void updateList(List<TodoList> todoLists) {
        this.allLists = todoLists;
        applyFilter();
    }


    public void setQueryString(String query) {
        this.queryString = query;
        applyFilter();
    }

    private void applyFilter() {
        this.filteredLists = new ArrayList<>(this.allLists.size());
        for (int i = 0; i < this.allLists.size(); i++) {
            if (this.allLists.get(i).checkQueryMatch(this.queryString)) {
                this.filteredLists.add(this.allLists.get(i));
            }
        }
    }

    public TodoList getToDoListFromPosition(int index) {
        if (index < 0 || index >= this.filteredLists.size())
            return null;
        return this.filteredLists.get(this.filteredLists.size()-index-1);
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {
        public TextView title, deadline, done;
        public View urgency;

        public ViewHolder(View v) {
            super(v);
            title =  (TextView) v.findViewById(R.id.tv_todo_list_title);
            deadline = (TextView) v.findViewById(R.id.tv_todo_list_next_deadline);
            done = (TextView) v.findViewById(R.id.tv_todo_list_status);
            urgency = v.findViewById(R.id.v_urgency_indicator);

            v.setOnClickListener(this);
            v.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View v) {

            Bundle bundle = new Bundle();

            // It is important to save the clicked list, because it is possible that it was not yet written to the database and thus cannot be identified by its id.
            contextActivity.setClickedList(filteredLists.get(filteredLists.size()-1-getAdapterPosition()));
            bundle.putBoolean(TodoTasksFragment.SHOW_FLOATING_BUTTON, true);
            TodoTasksFragment fragment = new TodoTasksFragment();
            fragment.setArguments(bundle);

            //contextActivity.setFragment(fragment);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            //TODO ask touchlistener for swipe action
            menu.setHeaderView(Helper.getMenuHeader(contextActivity, contextActivity.getString(R.string.select_option)));
            MenuInflater inflater = contextActivity.getMenuInflater();
            inflater.inflate(R.menu.todo_list_long_click, menu);
        }
    }
}
