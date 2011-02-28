package edu.umd.umiacs.newsstand;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class NewsStandPreferences extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}