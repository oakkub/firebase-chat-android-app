package com.oakkub.chat.managers;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;

/**
 * Created by OaKKuB on 2/29/2016.
 */
public class BindPreferenceSummaryToValueListener implements Preference.OnPreferenceChangeListener {

    public void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(this);

        onPreferenceChange(preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                .getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String value = newValue.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            bindListPreference(preference, value);
        } else if (preference instanceof RingtonePreference) {
            // For ringtone preferences, look up the correct display value
            // using RingtoneManager.
            bindRingtonePreference(preference, value);
        } else {
            // For all other preferences, set the summary to the value's
            // simple string representation.
            preference.setSummary(value);
        }

        return false;
    }

    private void bindListPreference(Preference preference, String value) {
        ListPreference listPreference = (ListPreference) preference;
        int index = listPreference.findIndexOfValue(value);

        preference.setSummary(index > 0 ? listPreference.getEntries()[index] : null);
    }

    private void bindRingtonePreference(Preference preference, String value) {
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
