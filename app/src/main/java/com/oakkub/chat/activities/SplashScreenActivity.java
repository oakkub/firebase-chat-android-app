package com.oakkub.chat.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.akexorcist.localizationactivity.LanguageSetting;
import com.firebase.client.AuthData;
import com.oakkub.chat.R;
import com.oakkub.chat.fragments.AuthStateFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SplashScreenActivity extends BaseActivity implements AuthStateFragment.OnFirebaseAuthentication {

    private static final String FIREBASE_AUTH_TAG = "tag:FirebaseAuth";

    @Bind(R.id.simple_app_bar_layout)
    AppBarLayout appBarLayout;

    @Bind(R.id.simple_toolbar)
    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LanguageSetting.setLanguage(this, getResources().getConfiguration().locale.getLanguage());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        ButterKnife.bind(this);
        initInstances();
    }

    private void initInstances() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            appBarLayout.setElevation(0f);
        }

        findOrAddFragmentByTag(getSupportFragmentManager(),
                new AuthStateFragment(), FIREBASE_AUTH_TAG);
    }

    @Override
    public void finish() {
        overridePendingTransition(0, 0);
        super.finish();
    }

    @Override
    public void onAuthenticated(AuthData authData) {
        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        mainIntent.putExtra(MainActivity.EXTRA_MY_ID, authData.getUid());
        mainIntent.putExtra(MainActivity.EXTRA_PROVIDER, authData.getProvider());

        startActivity(mainIntent);
        finish();
    }

    @Override
    public void onUnauthenticated() {
        Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(loginIntent);
        finish();
    }
}
