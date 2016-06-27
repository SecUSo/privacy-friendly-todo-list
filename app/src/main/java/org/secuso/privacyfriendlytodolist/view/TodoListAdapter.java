package org.secuso.privacyfriendlytodolist.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MotionEvent;
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

    enum Direction {LEFT, RIGHT;}

    private ArrayList<TodoList> data;
    private int position;

    public TodoListAdapter(Activity ac, ArrayList<TodoList> data) {
        this.data = data;
        this.contextActivity = (MainActivity) ac;
    }



    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
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
    public void onBindViewHolder(final ViewHolder holder, int position) {
        TodoList list = data.get(data.size()-1-position);
        holder.title.setText(list.getName());
        if (list.getDeadline() <= 0)
            holder.deadline.setText(contextActivity.getResources().getString(R.string.no_next_deadline));
        else
            holder.deadline.setText(contextActivity.getResources().getString(R.string.deadline_dd) + " " + Helper.getDate(list.getDeadline()));
        holder.done.setText(String.format("%d/%d", list.getDoneTodos(), list.getSize()));
        holder.urgency.setBackgroundColor(Helper.getDeadlineColor(contextActivity, list.getDeadlineColor()));

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
        return data.size();
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        holder.itemView.setOnLongClickListener(null);
        super.onViewRecycled(holder);
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
            //v.setOnCreateContextMenuListener(this);

            v.setOnTouchListener(new View.OnTouchListener() {
                float historicX = Float.NaN, historicY = Float.NaN;
                static final int DELTA = 50;

                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    Log.i("TodoListAdapter","onTouch: " + event.getAction());
                    switch (event.getAction())
                    {
                        case MotionEvent.ACTION_DOWN:
                            historicX = event.getX();
                            historicY = event.getY();
                            break;

                        case MotionEvent.ACTION_UP:
                            if (event.getX() - historicX < -DELTA)
                            {
                                Log.i("TodoListAdapter", "slide left ("+ getAdapterPosition() + ")");
                                return true;
                            }
                            else if (event.getX() - historicX > DELTA)
                            {
                                Log.i("TodoListAdapter", "slide right ("+ getAdapterPosition() + ")");
                                return true;
                            }
                        default: return false;
                    }
                    return false;
                }
            });
        }

        @Override
        public void onClick(View v) {

            Bundle bundle = new Bundle();

            int pos = getAdapterPosition();
            TodoList currentList = data.get(data.size()-1-pos);
            bundle.putParcelable(TodoList.PARCELABLE_ID, currentList);
            TodoTasksFragment fragment = new TodoTasksFragment();
            fragment.setArguments(bundle);

            contextActivity.setFragment(fragment);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            //TODO ask touchlistener for swipe action
            menu.setHeaderTitle(R.string.select_option);
            MenuInflater inflater = contextActivity.getMenuInflater();
            inflater.inflate(R.menu.todo_list_long_click, menu);
        }
    }
}
