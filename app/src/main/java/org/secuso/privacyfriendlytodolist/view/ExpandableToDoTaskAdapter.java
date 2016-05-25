package org.secuso.privacyfriendlytodolist.view;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.Space;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.Helper;
import org.secuso.privacyfriendlytodolist.model.TodoSubTask;
import org.secuso.privacyfriendlytodolist.model.TodoTask;

import java.util.ArrayList;

/**
 * Created by dominik on 24.05.16.
 */
public class ExpandableToDoTaskAdapter extends BaseExpandableListAdapter {

    private static final String TAG = ExpandableToDoTaskAdapter.class.getSimpleName();

    private ArrayList<TodoTask> data;
    private Context context;
    private HeaderViewHolder headerViewHolder;
    private ChildViewHolder childViewHolder;

    private int progressAccuracy = 10;

    public ExpandableToDoTaskAdapter(Context context, ArrayList<TodoTask> tasks) {
        this.context = context;
        data = tasks;


    }

    @Override
    public int getGroupCount() {
        return data.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return data.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }


    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        TodoTask currentTask = data.get(groupPosition);

        if(convertView == null) {

            convertView = LayoutInflater.from(context).inflate(R.layout.exlv_tasks_header, parent, false);

            headerViewHolder = new HeaderViewHolder();
            headerViewHolder.name = (TextView) convertView.findViewById(R.id.tv_exlv_task_name);
            headerViewHolder.done = (CheckBox) convertView.findViewById(R.id.cb_task_done);
            headerViewHolder.deadline = (TextView) convertView.findViewById(R.id.tv_exlv_task_deadline);
            headerViewHolder.progressIndicator = (LinearLayout) convertView.findViewById(R.id.ll_task_progress);

            // add color boxest that indicate the progress of each task
            int boxWidth = Helper.dp2Px(context, 5);
            int progress = currentTask.getProgress();
            for(int i = 0; i < TodoTask.MAX_PRIORITY; i++) {
                ImageView img = new ImageView(parent.getContext());
                img.setLayoutParams(new LinearLayout.LayoutParams(boxWidth, boxWidth));

                if(progress > i)
                    img.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
                else
                    img.setBackground(ContextCompat.getDrawable(context, R.drawable.border_img_view));
                Space space = new Space(context);
                space.setLayoutParams(new LinearLayout.LayoutParams(0, Helper.dp2Px(context,1), 1f));
                headerViewHolder.progressIndicator.addView(img);
                headerViewHolder.progressIndicator.addView(space);
            }

            headerViewHolder.progressIndicator.removeViewAt(headerViewHolder.progressIndicator.getChildCount()-1);

            convertView.setTag(headerViewHolder);

        } else {
            headerViewHolder = (HeaderViewHolder) convertView.getTag();
        }

        // fill header with content

        headerViewHolder.name.setText(currentTask.getName());
        String deadline = currentTask.getDeadline();
        if(deadline == null)
            deadline = context.getResources().getString(R.string.no_deadline);
        else
            deadline = context.getResources().getString(R.string.deadline) + " " + deadline;
        headerViewHolder.deadline.setText(deadline);


        return convertView;
    }

    private class ChildViewHolder {
        public TextView taskDescription;
        public ListView subtasks;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        TodoTask currentTask = data.get(groupPosition);


        if(convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.exlv_task_child, parent, false);
            childViewHolder = new ChildViewHolder();

            childViewHolder.taskDescription = (TextView) convertView.findViewById(R.id.tv_exlv_task_description);
            childViewHolder.subtasks = (ListView) convertView.findViewById(R.id.lv_subtasks);
            childViewHolder.subtasks.setClickable(false);
            childViewHolder.subtasks.setAdapter(new SubTaskAdapter(context, R.layout.subtask_list, currentTask.getSubTasks()));

            convertView.setTag(childViewHolder);
        } else {
            childViewHolder = (ChildViewHolder) convertView.getTag();
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }


    private class HeaderViewHolder {
        public TextView name;
        public TextView deadline;
        public CheckBox done;
        public View urgencyIndicator;
        public LinearLayout progressIndicator;
    }

    private class SubTaskAdapter extends ArrayAdapter<TodoSubTask> {

        private SubTaskViewHolder viewHolder;

        public SubTaskAdapter(Context context, int resource, ArrayList<TodoSubTask> items) {
            super(context, resource, items);
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(this.getContext())
                        .inflate(R.layout.subtask_list, parent, false);

                viewHolder = new SubTaskViewHolder();
                viewHolder.name = (TextView) convertView.findViewById(R.id.tv_subtask_name);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (SubTaskViewHolder) convertView.getTag();
            }

            TodoSubTask subTask = getItem(position);
            if(subTask != null) {
                viewHolder.name.setText(subTask.getTitle());
            } else {
                Log.i(TAG, "Null");
            }

            return convertView;
        }

        private class SubTaskViewHolder {
            public TextView name;
        }
    }

}
