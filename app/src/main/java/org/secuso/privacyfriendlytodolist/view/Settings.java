package org.secuso.privacyfriendlytodolist.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.SwitchPreference;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import org.secuso.privacyfriendlytodolist.R;

public class Settings extends AppCompatActivity {

    private static final String TAG = Settings.class.getSimpleName();

    public static final String DEFAULT_REMINDER_TIME_KEY = "pref_default_reminder_time";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarr);
        if (toolbar != null)
            setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            //final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            //upArrow.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP);
            //getSupportActionBar().setHomeAsUpIndicator(upArrow);
        }

        getFragmentManager().beginTransaction().replace(R.id.fragment_container, new MyPreferenceFragment()).commit();
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


    public static class MyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        boolean ignoreChanges = false;


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
            // uncheck pin if pin is invalid
            SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
            boolean pinEnabled = sharedPreferences.getBoolean("pref_pin_enabled", false);
            if(pinEnabled) {
                String pin = sharedPreferences.getString("pref_pin", null);
                if(pin == null || pin.length() < 4) {
                    // pin invalid: uncheck
                    ignoreChanges = true;
                    ((SwitchPreference) findPreference("pref_pin_enabled")).setChecked(false);
                    ignoreChanges = false;
                }
            }

            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if(!ignoreChanges) {
                if (key.equals("pref_pin")) {
                    String pin = sharedPreferences.getString(key, null);

                    if (pin != null) {
                        if (pin.length() < 4) {
                            ignoreChanges = true;
                            ((EditTextPreference) findPreference("pref_pin")).setText("");
                            ignoreChanges = false;
                            Toast.makeText(getActivity(), getString(R.string.invalid_pin), Toast.LENGTH_LONG).show();
                        }
                    }
                } else if (key.equals("pref_pin_enabled")) {
                    boolean pinEnabled = sharedPreferences.getBoolean("pref_pin_enabled", false);

                    if (pinEnabled) {
                        ignoreChanges = true;
                        ((EditTextPreference) findPreference("pref_pin")).setText("");
                        ignoreChanges = false;
                    }
                }
            }

            updatePrefSummary(findPreference(key));
        }

    }


}
