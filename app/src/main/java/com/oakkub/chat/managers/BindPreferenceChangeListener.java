package com.oakkub.chat.managers;

import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.RingtonePreference;
import android.text.TextUtils;

/**
 * Created by OaKKuB on 3/4/2016.
 */
public class BindPreferenceChangeListener implements Preference.OnPreferenceChangeListener {

    private String value;
    private static BindPreferenceChangeListener bindPreferenceChangeListener;

    public static BindPreferenceChangeListener getInstance() {
        if (bindPreferenceChangeListener == null) {
            bindPreferenceChangeListener = new BindPreferenceChangeListener();
        }
        return bindPreferenceChangeListener;
    }

    private BindPreferenceChangeListener() {}

    public void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(bindPreferenceChangeListener);

        SharedPreferences prefs =
                AppController.getComponent(preference.getContext()).sharedPreferences();

        String valuePref;
        try {
            valuePref = prefs.getString(preference.getKey(), "");
        } catch (ClassCastException e) {
            valuePref = String.valueOf(prefs.getBoolean(preference.getKey(), false));
        }

        bindPreferenceChangeListener.onPreferenceChange(preference, valuePref);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        value = newValue.toString().trim();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            bindSummaryListPreference(preference);
        } else if (preference instanceof RingtonePreference) {
            // For ringtone preferences, look up the correct display value
            // using RingtoneManager.
            bindSummaryRingtonePreference(preference);
        } else {
            // For all other preferences, set the summary to the value's
            // simple string representation.
            preference.setSummary(value);
        }

        return true;
    }

    private void bindSummaryListPreference(Preference preference) {
        ListPreference listPreference = (ListPreference) preference;
        int index = listPreference.findIndexOfValue(value);
        preference.setSummary(index > 0 ? listPreference.getEntries()[index] : null);
    }

    private void bindSummaryRingtonePreference(Preference preference) {
        if (TextUtils.isEmpty(value)) {
            preference.setSummary("Silent");
        } else {
            Ringtone ringtone = RingtoneManager.getRingtone(
                    preference.getContext(), Uri.parse(value));

            if (ringtone == null) {
                // Clear the summary if there was a lookup error.
                preference.setSummary(null);
            } else {
                // Set the summary to reflect the new ringtone display
                // name.
                String name = ringtone.getTitle(preference.getContext());
                preference.setSummary(name);
            }
        }
    }

}
