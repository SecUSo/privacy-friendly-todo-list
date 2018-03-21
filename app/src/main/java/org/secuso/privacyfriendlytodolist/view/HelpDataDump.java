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

import android.content.Context;

import org.secuso.privacyfriendlytodolist.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by sebbi on 28.02.2018.
 *
 * This class handles the FAQ-style help menu.
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
