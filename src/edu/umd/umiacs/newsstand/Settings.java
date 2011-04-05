package edu.umd.umiacs.newsstand;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;

public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        CheckBoxPreference show_all = (CheckBoxPreference)getPreferenceScreen().findPreference("all_topics");
        show_all.setDisableDependentsState(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        if (key.equals("all_topics")) {
            if (pref.getBoolean("all_topics", false)) {
                // put any logic here that should fire when a preference is changed.
                
            }
        }
        //Toast.makeText(getApplicationContext(), "Preference changed!", Toast.LENGTH_SHORT).show();
    }
}
