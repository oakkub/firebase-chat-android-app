package com.oakkub.chat.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import icepick.Icepick;

/**
 * Created by OaKKuB on 11/5/2015.
 */
public class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();
    protected static final String EXTRA_MY_ID = TAG + ":extra:myId";

    public static Intent getMyIdStartIntent(Context context, String myId, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        intent.putExtra(EXTRA_MY_ID, myId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    public void setToolbarTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
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

    protected void setStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(color);
        }
    }

    protected int getCompatColor(int color) {
        return ContextCompat.getColor(this, color);
    }

}
