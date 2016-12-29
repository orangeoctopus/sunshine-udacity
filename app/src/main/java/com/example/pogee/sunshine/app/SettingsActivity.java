package com.example.pogee.sunshine.app;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.os.Bundle;

        /**
        * A {@link PreferenceActivity} that presents a set of application settings.
        * <p>
        * See <a href="http://developer.android.com/design/patterns/settings.html">
        * Android Design: Settings</a> for design guidelines and the <a
        * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
        * API Guide</a> for more information on developing a Settings UI.
        */
public class SettingsActivity extends PreferenceActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        SettingPrefFragment preferenceFragment = new SettingPrefFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, preferenceFragment).commit();


    }


     public static class SettingPrefFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener{

         @Override
         public void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
             // Add 'general' preferences, defined in the XML file
             // TODO: Add preferences from XML
             addPreferencesFromResource(R.xml.pref_general);
             bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_location_key)));
             bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_tempunit_key)));

         }

         /**
          * Attaches a listener so the summary is always updated with the preference value.
          * Also fires the listener once, to initialize the summary (so it shows up before the value
          * is changed.)
          */
         private void bindPreferenceSummaryToValue(Preference preference) {
             // Set the listener to watch for value changes.
             preference.setOnPreferenceChangeListener(this);

             // Trigger the listener immediately with the preference's
             // current value.
             onPreferenceChange(preference,
                     PreferenceManager
                             .getDefaultSharedPreferences(preference.getContext())
                             .getString(preference.getKey(), ""));
         }

         @Override
         public boolean onPreferenceChange(Preference preference, Object value) {
             String stringValue = value.toString();

             if (preference instanceof ListPreference) {
                 // For list preferences, look up the correct display value in
                 // the preference's 'entries' list (since they have separate labels/values).
                 ListPreference listPreference = (ListPreference) preference;
                 int prefIndex = listPreference.findIndexOfValue(stringValue);
                 if (prefIndex >= 0) {
                     preference.setSummary(listPreference.getEntries()[prefIndex]);
                 }
             } else {
                 // For other preferences, set the summary to the value's simple string representation.
                 preference.setSummary(stringValue);
             }
             return true;
         }


     }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
    return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //if main activity already running in our task, use that insttead of creating new main activity
    }
}
