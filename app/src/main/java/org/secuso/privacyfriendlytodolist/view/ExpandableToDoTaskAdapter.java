package org.secuso.privacyfriendlytodolist.view;

import android.content.Context;
import android.media.Image;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.Space;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.Helper;
import org.secuso.privacyfriendlytodolist.model.TodoTask;

import java.util.ArrayList;

/**
 * Created by dominik on 24.05.16.
 */
public class ExpandableToDoTaskAdapter extends BaseExpandableListAdapter {

    private static final String TAG = ExpandableToDoTaskAdapter.class.getSimpleName();
    private static final int TASK_DESCRIPTION_ROW = 0;
    private static final int SETTING_ROW = 1;
    private static final int SUBTASK_ROW = 2;

    private ArrayList<TodoTask> data;
    private Context context;
    private HeaderViewHolder headerViewHolder;

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
        Log.i(TAG, String.valueOf(data.get(groupPosition).getSubTasks().size() + 2));
        return data.get(groupPosition).getSubTasks().size() + 2;
    }

    @Override
    public int getChildType(int groupPosition, int childPosition) {
        if(childPosition == 0)
            return TASK_DESCRIPTION_ROW;
        else if(childPosition == data.get(groupPosition).getSubTasks().size()+1)
            return SETTING_ROW;
        return SUBTASK_ROW;
    }

    @Override
    public int getChildTypeCount() {
        return 3;
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
            headerViewHolder.seperator = (View) convertView.findViewById(R.id.v_exlv_header_separator);

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




    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        TodoTask currentTask = data.get(groupPosition);
        int type = getChildType(groupPosition, childPosition);

        switch(type) {
            case TASK_DESCRIPTION_ROW:

                TaskDescriptionViewHolder vh1 = new TaskDescriptionViewHolder();
                if(convertView == null) {
                    convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.exlv_task_description_row, parent, false);
                    vh1.taskDescription = (TextView) convertView.findViewById(R.id.tv_exlv_task_description);
                    convertView.setTag(vh1);
                } else {
                    vh1 = (TaskDescriptionViewHolder) convertView.getTag();
                }

                String description = data.get(groupPosition).getDescription();
                if(description == null || description.equals(""))
                    vh1.taskDescription.setText(context.getString(R.string.no_task_description));
                else
                    vh1.taskDescription.setText(data.get(groupPosition).getDescription());

                break;

            case SETTING_ROW:
                SettingViewHolder vh2 = new SettingViewHolder();
                if(convertView == null) {
                    convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.exlv_setting_row, parent, false);
                    vh2.addSubTaskButton = (ImageView) convertView.findViewById(R.id.iv_add_subtask);
                    convertView.setTag(vh2);
                } else {
                    vh2 = (SettingViewHolder) convertView.getTag();
                }

                vh2.addSubTaskButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(context, "Add a new subtask", Toast.LENGTH_SHORT).show();
                    }
                });

                break;
            default:
                SubTaskViewHolder vh3 = new SubTaskViewHolder();
                if(convertView == null) {
                    convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.exlv_subtask_row, parent, false);
                    vh3.subtaskName = (TextView) convertView.findViewById(R.id.tv_subtask_name);
                    convertView.setTag(vh3);
                } else {
                    vh3 = (SubTaskViewHolder) convertView.getTag();
                }

                vh3.subtaskName.setText(data.get(groupPosition).getSubTasks().get(childPosition-1).getTitle());

        }


        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }



    public class HeaderViewHolder {
        public TextView name;
        public TextView deadline;
        public CheckBox done;
        public View urgencyIndicator;
        public View seperator;
        public LinearLayout progressIndicator;
    }


    private class SubTaskViewHolder {
        public TextView subtaskName;
    }

    private class TaskDescriptionViewHolder {
        public TextView taskDescription;
    }

    private class SettingViewHolder {
        public ImageView addSubTaskButton;
    }

}
