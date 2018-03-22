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

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import org.secuso.privacyfriendlytodolist.view.ExpandableListAdapter;

import org.secuso.privacyfriendlytodolist.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Sebastian Lutz on 15.01.2018
 *
 * Activity that gives the user some help regarding the handling of the app.
 */

public class HelpActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_help);

        ExpandableListAdapter expandableListAdapter;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_help);
        ExpandableListView generalExpandableListView = (ExpandableListView) findViewById(R.id.generalExpandableListView);
        HelpDataDump helpDataDump = new HelpDataDump(this);

        LinkedHashMap<String, List<String>> expandableListDetail = helpDataDump.getDataGeneral();
        List<String> expandableListTitleGeneral = new ArrayList<String>(expandableListDetail.keySet());
        expandableListAdapter = new ExpandableListAdapter(this, expandableListTitleGeneral, expandableListDetail);
        generalExpandableListView.setAdapter(expandableListAdapter);

        if (toolbar != null) {
            toolbar.setTitle(R.string.help);
            toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
            setSupportActionBar(toolbar);

        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow);
        }

        //getFragmentManager().beginTransaction().replace(R.id.help_fragment_container, new HelpFragment()).commit();
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


    public static class HelpFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.help);
        }
    }

}