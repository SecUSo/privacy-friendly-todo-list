package org.secuso.privacyfriendlytodolist.view;

import android.content.Context;

import org.secuso.privacyfriendlytodolist.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by sebbi on 28.02.2018.
 */

public class HelpDataDump {

    private Context context;

    public HelpDataDump(Context context) {
        this.context = context;
    }

    public LinkedHashMap<String, List<String>> getDataGeneral() {
        LinkedHashMap<String, List<String>> expandableListDetail = new LinkedHashMap<String, List<String>>();

        List<String> general = new ArrayList<String>();
        general.add(context.getResources().getString(R.string.help_intro));

        expandableListDetail.put(context.getResources().getString(R.string.help_overview_heading), general);

        List<String> features = new ArrayList<String>();
        features.add(context.getResources().getString(R.string.help_todo_lists));
        features.add(context.getResources().getString(R.string.help_subtasks));
        features.add(context.getResources().getString(R.string.help_deadline_reminder));

        expandableListDetail.put(context.getResources().getString(R.string.help_group_lists), features);

        List<String> settings = new ArrayList<String>();
        settings.add(context.getResources().getString(R.string.help_pin));
        settings.add(context.getResources().getString(R.string.help_sound));

        expandableListDetail.put(context.getResources().getString(R.string.help_group_app), settings);

        List<String> permissions = new ArrayList<String>();
        permissions.add(context.getResources().getString(R.string.help_permissions));

        expandableListDetail.put(context.getResources().getString(R.string.help_group_privacy), permissions);

        List<String> widget = new ArrayList<String>();
        widget.add(context.getResources().getString(R.string.help_widget));

        expandableListDetail.put("Widget", widget);

        return expandableListDetail;
    }
}
