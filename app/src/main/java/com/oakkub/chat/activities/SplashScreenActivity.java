package com.oakkub.chat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.firebase.client.AuthData;
import com.oakkub.chat.R;
import com.oakkub.chat.fragments.AuthStateFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SplashScreenActivity extends BaseActivity implements AuthStateFragment.OnFirebaseAuthentication {

    private static final String FIREBASE_AUTH_TAG = "tag:FirebaseAuth";

    @Bind(R.id.simple_toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        ButterKnife.bind(this);
        initInstances();
    }

    private void initInstances() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        if (findFragmentByTag(FIREBASE_AUTH_TAG) == null) {
            addFragmentByTag(new AuthStateFragment(), FIREBASE_AUTH_TAG);
        }
    }

    @Override
    public void onAuthenticated(AuthData authData) {
        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        mainIntent.putExtra(MainActivity.EXTRA_MY_ID, authData.getUid());
        mainIntent.putExtra(MainActivity.EXTRA_PROVIDER, authData.getProvider());

        startActivity(mainIntent);
        fadeOutFinish();
    }

    @Override
    public void onUnauthenticated() {
        Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(loginIntent);
        fadeOutFinish();
    }
}
