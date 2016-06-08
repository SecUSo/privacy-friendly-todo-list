package org.secuso.privacyfriendlytodolist.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.Helper;
import org.secuso.privacyfriendlytodolist.model.TodoList;

import java.util.ArrayList;

/**
 * Created by dominik on 19.05.16.
 */
public class TodoListAdapter extends  RecyclerView.Adapter<TodoListAdapter.ViewHolder>  {

    private static final String TAG = TodoListAdapter.class.getSimpleName();
    private MainActivity contextActivity;

    private ArrayList<TodoList> data;

    public TodoListAdapter(Activity ac, ArrayList<TodoList> data) {
        this.data = data;
        this.contextActivity = (MainActivity) ac;
    }


    // invoked by the layout manager
    @Override
    public TodoListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.todo_list_entry, parent, false);

        // TODO set the view's size, margins, paddings and layout parameters

        return new ViewHolder(v);
    }

    // replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TodoList list = data.get(position);
        holder.title.setText(list.getName());
        holder.deadline.setText(list.getDeadline());
        holder.done.setText(String.format("%d/%d", list.getDoneTodos(), list.getSize()));
        holder.urgency.setBackgroundColor(Helper.getDeadlineColor(contextActivity, list.getDeadlineColor()));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView title, deadline, done;
        public View urgency;

        public ViewHolder(View v) {
            super(v);
            title =  (TextView) v.findViewById(R.id.tv_todo_list_title);
            deadline = (TextView) v.findViewById(R.id.tv_todo_list_next_deadline);
            done = (TextView) v.findViewById(R.id.tv_todo_list_status);
            urgency = v.findViewById(R.id.v_urgency_indicator);

            v.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {

            Bundle bundle = new Bundle();

            int pos = getAdapterPosition();
            TodoList currentList = data.get(pos);
            bundle.putParcelable(TodoList.PARCELABLE_ID, currentList);
            TodoTasksFragment fragment = new TodoTasksFragment();
            fragment.setArguments(bundle);

            contextActivity.setFragment(fragment);
        }
    }
}
