package com.oakkub.chat.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.oakkub.chat.R;
import com.oakkub.chat.fragments.GeneralPreferenceFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by OaKKuB on 2/29/2016.
 */
public class SettingsActivity extends BaseActivity implements
        GeneralPreferenceFragment.OnLanguageChangeListener {

    @Bind(R.id.simple_toolbar)
    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        ButterKnife.bind(this);

        setToolbar();
        addFragment();
    }

    private void setToolbar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.action_settings);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void addFragment() {
        getFragmentManager().beginTransaction()
                .replace(R.id.settings_container, new GeneralPreferenceFragment())
                .commit();
    }

    @Override
    public void onLanguageChange(String languageCode) {
        setLanguage(languageCode);
    }

}
