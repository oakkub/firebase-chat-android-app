package com.oakkub.chat.fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.oakkub.chat.R;
import com.oakkub.chat.managers.BindPreferenceChangeListener;

import java.util.Locale;

import icepick.State;

/**
 * Created by OaKKuB on 3/4/2016.
 */
public class GeneralPreferenceFragment extends PreferenceFragment {

    private static final String TAG = GeneralPreferenceFragment.class.getSimpleName();

    @State
    String languageCodeChanged;

    private OnLanguageChangeListener onLanguageChangeListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        setHasOptionsMenu(true);

        bindLanguagePreference();
        BindPreferenceChangeListener.getInstance().bindPreferenceSummaryToValue(
                findPreference(getString(R.string.pref_notification_ringtone)));
    }

    @Override
    public void onResume() {
        super.onResume();

        onLanguageChangeListener = (OnLanguageChangeListener) getActivity();
        if (languageCodeChanged != null) {
            onLanguageChangeListener.onLanguageChange(languageCodeChanged);
            languageCodeChanged = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        onLanguageChangeListener = null;
    }

    private void bindLanguagePreference() {
        Preference languagePreference = findPreference(getString(R.string.pref_language_list));
        ListPreference languageListPreference = (ListPreference) languagePreference;

        // if never set any language before
        if (languageListPreference.getValue() == null) {
            CharSequence languageValue = getListValue(languageListPreference, getLanguageCode());

            boolean isLanguageValueExists = false;
            for (CharSequence languageCode : languageListPreference.getEntryValues()) {
                if (languageCode.equals(languageValue)) {
                    isLanguageValueExists = true;
                    break;
                }
            }
            languageListPreference.setValue(!isLanguageValueExists ? "en" : languageValue.toString());
        }

        languageListPreference.setTitle(languageListPreference.getEntry());
        languagePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String value = newValue.toString();

                ListPreference listPreference = (ListPreference) preference;
                listPreference.setTitle(getListEntryFromValue(listPreference, value));

                if (onLanguageChangeListener != null) {
                    onLanguageChangeListener.onLanguageChange(value);
                } else {
                    languageCodeChanged = value;
                }

                return true;
            }
        });
    }

    private CharSequence getListEntryFromValue(ListPreference listPreference, String value) {
        int index = listPreference.findIndexOfValue(value);
        return listPreference.getEntries()[index];
    }

    private CharSequence getListValue(ListPreference listPreference, String value) {
        int index = listPreference.findIndexOfValue(value);
        return listPreference.getEntryValues()[index];
    }

    private String getLanguageCode() {
        Locale locale = Resources.getSystem().getConfiguration().locale;
        return locale.getLanguage();
    }

    public interface OnLanguageChangeListener {
        void onLanguageChange(String languageCode);
    }

}
