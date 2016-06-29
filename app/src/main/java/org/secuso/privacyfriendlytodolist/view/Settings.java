package org.secuso.privacyfriendlytodolist.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;


import org.secuso.privacyfriendlytodolist.R;

public class Settings extends AppCompatActivity {

    private static final String TAG = Settings.class.getSimpleName();

    public static final String DEFAULT_REMINDER_TIME_KEY = "pref_default_reminder_time";
   // private static SharedPreferences.Editor prefs;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarr);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

       // prefs = PreferenceManager.getDefaultSharedPreferences(this).edit();

        getFragmentManager().beginTransaction().replace(R.id.fragment_container, new MyPreferenceFragment()).commit();
    }



    public static class MyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);

            // initializes
            initSummary(getPreferenceScreen());

        }

        private void initSummary(Preference p) {
            if (p instanceof PreferenceGroup) {
                PreferenceGroup pGrp = (PreferenceGroup) p;
                for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                    initSummary(pGrp.getPreference(i));
                }
            } else {
                updatePrefSummary(p);
            }
        }

        private void updatePrefSummary(Preference p) {

            if (p instanceof ListPreference) {
                ListPreference listPref = (ListPreference) p;
                p.setSummary(listPref.getEntry());
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            updatePrefSummary(findPreference(key));

            /*

            // default reminder time was changed
            if(key.equals(DEFAULT_REMINDER_TIME_KEY)) {
                Preference preference = findPreference(key);
                if (preference instanceof EditTextPreference){
                    EditTextPreference editTextPreference =  (EditTextPreference)preference;
                    if (editTextPreference.getText().trim().length() > 0){
                        editTextPreference.setSummary("Reminder Time is  " + editTextPreference.getText());
                    }else{
                        editTextPreference.setSummary("Enter the new default reminder time");
                    }
                }
            }*/

        }
    }


}
