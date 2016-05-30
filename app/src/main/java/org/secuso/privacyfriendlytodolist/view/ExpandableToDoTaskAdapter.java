package org.secuso.privacyfriendlytodolist.view;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.Space;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.secuso.privacyfriendlytodolist.R;
import org.secuso.privacyfriendlytodolist.model.Helper;
import org.secuso.privacyfriendlytodolist.model.TodoTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by dominik on 24.05.16.
 */
public class ExpandableToDoTaskAdapter extends BaseExpandableListAdapter {

    private static final String TAG = ExpandableToDoTaskAdapter.class.getSimpleName();

    // group rows
    private static final int GR_TASK_ROW = 0;
    private static final int GR_PRIO_ROW = 1;

    // child rows
    private static final int CH_TASK_DESCRIPTION_ROW = 0;
    private static final int CH_SETTING_ROW = 1;
    private static final int CH_SUBTASK_ROW = 2;

    private ArrayList<TodoTask> data;
    private Context context;
    private HashMap<TodoTask.Priority, Integer> prioBarPositions = new HashMap<>();

    private int progressAccuracy = 10;

    public ExpandableToDoTaskAdapter(Context context, ArrayList<TodoTask> tasks) {
        this.context = context;
        data = tasks;

        // sort tasks by priority
        Collections.sort(data, new Comparator<TodoTask>() {
            @Override
            public int compare(TodoTask t1, TodoTask t2) {
                TodoTask.Priority p1 = t1.getPriority();
                TodoTask.Priority p2 = t2.getPriority();
                return p1.compareTo(p2);
            }
        });

        // count how many tasks belong to each priority group (tasks are now sorted by priority)
        if (data.size() != 0) {

            int pos = 0;
            TodoTask.Priority currentPrio;
            HashSet<TodoTask.Priority> prioAlreadySeen = new HashSet<>();
            for(TodoTask task : data) {
                currentPrio = task.getPriority();

                if(!prioAlreadySeen.contains(currentPrio)) {
                    prioAlreadySeen.add(currentPrio);
                    prioBarPositions.put(currentPrio, pos);
                    pos++;
                }

                pos++;
            }
        }
    }

    private TodoTask getTaskByPosition(int groupPosition) {

        int seenPrioBars = 0;

        for(TodoTask.Priority priority : TodoTask.Priority.values()) {
            if(prioBarPositions.containsKey(priority)) {
                if(groupPosition < prioBarPositions.get(priority))
                    break;
                seenPrioBars++;
            }
        }

        return data.get(groupPosition-seenPrioBars);
    }

    @Override
    public int getGroupCount() {
        // There are three groups (high, middle, low) that indicate the priority of the following tasks
        return data.size() + prioBarPositions.size();
    }

    @Override
    public int getGroupType(int groupPosition) {

        if(prioBarPositions.values().contains(groupPosition))
            return GR_PRIO_ROW;
        return GR_TASK_ROW;
    }

    @Override
    public int getGroupTypeCount() {
        return 2;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return getTaskByPosition(groupPosition).getSubTasks().size() + 2;
    }

    @Override
    public int getChildType(int groupPosition, int childPosition) {
        if (childPosition == 0)
            return CH_TASK_DESCRIPTION_ROW;
        else if (childPosition == getTaskByPosition(groupPosition).getSubTasks().size() + 1)
            return CH_SETTING_ROW;
        return CH_SUBTASK_ROW;
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

    private String getPriorityNameByBarPos(int groupPosition) {
        for (Map.Entry<TodoTask.Priority, Integer> entry : prioBarPositions.entrySet()) {
            if (entry.getValue() == groupPosition) {
                if(entry.getKey() == TodoTask.Priority.HIGH)
                    return context.getResources().getString(R.string.high_priority);
                else if(entry.getKey() == TodoTask.Priority.MEDIUM)
                    return context.getResources().getString(R.string.medium_priority);
                else
                    return context.getResources().getString(R.string.low_priority);
            }
        }
        return "UNKNOWN PRIORITY";
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        int type = getGroupType(groupPosition);

        switch (type) {

            case GR_PRIO_ROW:

                GroupPrioViewHolder vh1;

                if (convertView == null) {
                    convertView = LayoutInflater.from(context).inflate(R.layout.exlv_prio_bar, parent, false);
                    vh1 = new GroupPrioViewHolder();
                    vh1.prioFlag = (TextView) convertView.findViewById(R.id.tv_exlv_priority_bar);
                    convertView.setTag(vh1);
                } else {
                    vh1 = (GroupPrioViewHolder) convertView.getTag();
                }

                vh1.prioFlag.setText(getPriorityNameByBarPos(groupPosition));
                convertView.setClickable(true);

                break;

            case GR_TASK_ROW:

                TodoTask currentTask = getTaskByPosition(groupPosition);
                GroupTaskViewHolder vh2;

                if (convertView == null) {

                    convertView = LayoutInflater.from(context).inflate(R.layout.exlv_tasks_header, parent, false);

                    vh2 = new GroupTaskViewHolder();
                    vh2.name = (TextView) convertView.findViewById(R.id.tv_exlv_task_name);
                    vh2.done = (CheckBox) convertView.findViewById(R.id.cb_task_done);
                    vh2.deadline = (TextView) convertView.findViewById(R.id.tv_exlv_task_deadline);
                    vh2.progressIndicator = (LinearLayout) convertView.findViewById(R.id.ll_task_progress);
                    vh2.seperator = (View) convertView.findViewById(R.id.v_exlv_header_separator);

                    // add color boxest that indicate the progress of each task
                    int boxWidth = Helper.dp2Px(context, 5);
                    int progress = currentTask.getProgress();
                    for (int i = 0; i < TodoTask.MAX_PRIORITY; i++) {
                        ImageView img = new ImageView(parent.getContext());
                        img.setLayoutParams(new LinearLayout.LayoutParams(boxWidth, boxWidth));

                        if (progress > i)
                            img.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
                        else
                            img.setBackground(ContextCompat.getDrawable(context, R.drawable.border_img_view));
                        Space space = new Space(context);
                        space.setLayoutParams(new LinearLayout.LayoutParams(0, Helper.dp2Px(context, 1), 1f));
                        vh2.progressIndicator.addView(img);
                        vh2.progressIndicator.addView(space);
                    }

                    vh2.progressIndicator.removeViewAt(vh2.progressIndicator.getChildCount() - 1);

                    convertView.setTag(vh2);

                } else {
                    vh2 = (GroupTaskViewHolder) convertView.getTag();
                }

                // fill header with content
                vh2.name.setText(currentTask.getName());
                String deadline = currentTask.getDeadline();
                if (deadline == null)
                    deadline = context.getResources().getString(R.string.no_deadline);
                else
                    deadline = context.getResources().getString(R.string.deadline_dd) + " " + deadline;
                vh2.deadline.setText(deadline);

                break;
            default:
                // TODO Exception
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        int type = getChildType(groupPosition, childPosition);

        switch (type) {
            case CH_TASK_DESCRIPTION_ROW:

                TaskDescriptionViewHolder vh1 = new TaskDescriptionViewHolder();
                if (convertView == null) {
                    convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.exlv_task_description_row, parent, false);
                    vh1.taskDescription = (TextView) convertView.findViewById(R.id.tv_exlv_task_description);
                    convertView.setTag(vh1);
                } else {
                    vh1 = (TaskDescriptionViewHolder) convertView.getTag();
                }

                String description = getTaskByPosition(groupPosition).getDescription();
                if (description == null || description.equals(""))
                    vh1.taskDescription.setText(context.getString(R.string.no_task_description));
                else
                    vh1.taskDescription.setText(description);

                break;

            case CH_SETTING_ROW:
                SettingViewHolder vh2 = new SettingViewHolder();
                if (convertView == null) {
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
                if (convertView == null) {
                    convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.exlv_subtask_row, parent, false);
                    vh3.subtaskName = (TextView) convertView.findViewById(R.id.tv_subtask_name);
                    convertView.setTag(vh3);
                } else {
                    vh3 = (SubTaskViewHolder) convertView.getTag();
                }

                vh3.subtaskName.setText(getTaskByPosition(groupPosition).getSubTasks().get(childPosition - 1).getTitle());

        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }


    public class GroupTaskViewHolder {
        public TextView name;
        public TextView deadline;
        public CheckBox done;
        public View urgencyIndicator;
        public View seperator;
        public LinearLayout progressIndicator;
    }

    public class GroupPrioViewHolder {
        public TextView prioFlag;
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