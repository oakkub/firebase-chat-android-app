package com.oakkub.chat.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;

import com.akexorcist.localizationactivity.LanguageSetting;
import com.akexorcist.localizationactivity.LocalizationActivity;
import com.oakkub.chat.R;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.utils.PrefsUtil;
import com.oakkub.chat.views.dialogs.ProgressDialogFragment;

import java.util.Locale;

import icepick.Icepick;
import icepick.State;

/**
 * Created by OaKKuB on 11/5/2015.
 */
public class BaseActivity extends LocalizationActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();
    private static final String PROGRESS_DIALOG_TAG = TAG + ":tag:progressDialog";
    protected static final String EXTRA_MY_ID = TAG + ":extra:myId";

    @State
    String uid;

    public static Intent getMyIdStartIntent(Context context, String myId, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        intent.putExtra(EXTRA_MY_ID, myId);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        checkLanguage();
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        Icepick.restoreInstanceState(this, savedInstanceState);

        if (savedInstanceState == null) {
            SharedPreferences prefs = AppController.getComponent(this).sharedPreferences();
            uid = prefs.getString(PrefsUtil.PREF_UID, null);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    private void checkLanguage() {
        SharedPreferences prefs = AppController.getComponent(this).sharedPreferences();
        String language = prefs.getString(
                getString(R.string.pref_language_list), Locale.ENGLISH.getLanguage());

        Locale locale = new Locale(language);

        updateLanguageConfiguration(getResources(), locale);
        updateLanguageConfiguration(Resources.getSystem(), locale);

        Locale.setDefault(locale);
        LanguageSetting.setLanguage(this, locale);
    }

    private void updateLanguageConfiguration(Resources res, Locale locale) {
        Configuration configuration = res.getConfiguration();
        configuration.locale = locale;
        res.updateConfiguration(configuration, res.getDisplayMetrics());
    }

    public void clearNotification() {
        NotificationManagerCompat notificationManager = AppController.getComponent(this).notificationManager();
        notificationManager.cancelAll();
    }

    public void setToolbarTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    public void setDisplayHomeAsUpEnabled(boolean setDisplayHomeAsUpEnabled) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(setDisplayHomeAsUpEnabled);
        }
    }

    public ProgressDialogFragment findProgressDialog() {
        return (ProgressDialogFragment) findFragmentByTag(PROGRESS_DIALOG_TAG);
    }

    public void showProgressDialog() {
        ProgressDialogFragment progressDialog = findProgressDialog();
        if (progressDialog == null) {
            progressDialog = ProgressDialogFragment.newInstance();
        }
        progressDialog.show(getSupportFragmentManager(), PROGRESS_DIALOG_TAG);
    }

    public void hideProgressDialog() {
        ProgressDialogFragment progressDialog = findProgressDialog();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public Fragment findOrAddFragmentByTag(FragmentManager fragmentManager, Fragment fragment, String tag) {
        Fragment addedFragment = fragmentManager.findFragmentByTag(tag);
        if (addedFragment == null) {
            fragmentManager.beginTransaction()
                    .add(fragment, tag)
                    .commit();
            return fragment;
        } else {
            fragment = null;
            return addedFragment;
        }
    }

    @SuppressWarnings("UnusedAssignment")
    public Fragment findOrAddFragmentByTag(@IdRes int containerId, Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment addedFragment = fragmentManager.findFragmentByTag(tag);

        if (addedFragment == null) {
            fragmentManager.beginTransaction()
                    .add(containerId, fragment, tag)
                    .commit();
            return fragment;
        } else {
            fragment = null;
            return addedFragment;
        }
    }

    @Nullable
    public Fragment findFragmentByTag(String tag) {
        return getSupportFragmentManager().findFragmentByTag(tag);
    }

    public Fragment addFragmentByTag(@NonNull Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragmentFromTag = fragmentManager.findFragmentByTag(tag);

        if (fragmentFromTag == null) {
            fragmentManager.beginTransaction()
                    .add(fragment, tag)
                    .commit();

            return fragment;
        }

        return fragmentFromTag;
    }

    public void fadeOutFinish() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void setStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(color);
        }
    }

    public int getCompatColor(int color) {
        return ContextCompat.getColor(this, color);
    }

}
