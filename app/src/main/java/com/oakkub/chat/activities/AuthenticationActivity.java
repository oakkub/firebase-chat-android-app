package com.oakkub.chat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.oakkub.chat.R;
import com.oakkub.chat.fragments.AuthenticationActivityFragment;
import com.oakkub.chat.utils.FirebaseUtil;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.State;

public class AuthenticationActivity extends BaseActivity {

    private static final String AUTHENTICATION_TAG = "tag:authenticationFragment";

    @Bind(R.id.login_process_root_view)
    RelativeLayout rootView;

    @Bind(R.id.logging_in_text_view)
    TextView loggingTextView;

    @State
    String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_process);
        ButterKnife.bind(this);
        getDataFromIntent(savedInstanceState);

        setViews();
        findAuthenticationFragment();
    }

    private void getDataFromIntent(Bundle savedInstanceState) {
        if (savedInstanceState != null) return;

        Intent intent = getIntent();
        provider = intent.getStringExtra(AuthenticationActivityFragment.PROVIDER);
    }

    private void setViews() {

        rootView.setBackgroundColor(ContextCompat.getColor(this, getBackgroundColor()));

        loggingTextView.setText(getString(R.string.authenticating_user));
    }

    private int getBackgroundColor() {
        if (FirebaseUtil.isGoogleLogin(provider)) return R.color.tomato;
        else if (FirebaseUtil.isFacebookLogin(provider)) return R.color.darkBlue;
        else return R.color.colorPrimary;
    }

    private void findAuthenticationFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        AuthenticationActivityFragment authenticationActivityFragment = (AuthenticationActivityFragment) fragmentManager.findFragmentByTag(AUTHENTICATION_TAG);

        if (authenticationActivityFragment == null) {
            if (FirebaseUtil.isEmailLogin(provider)) {
                authenticationActivityFragment = findEmailLoginAuthenticationFragment();
            } else {
                authenticationActivityFragment = findTokenLoginAuthenticationFragment();
            }

            fragmentManager.beginTransaction()
                    .add(authenticationActivityFragment, AUTHENTICATION_TAG)
                    .commit();
        }
    }

    private AuthenticationActivityFragment findEmailLoginAuthenticationFragment() {
        Intent intent = getIntent();

        String email = intent.getStringExtra(AuthenticationActivityFragment.EMAIL);
        String password = intent.getStringExtra(AuthenticationActivityFragment.PASSWORD);

        return AuthenticationActivityFragment.newInstance(provider, email, password);
    }

    private AuthenticationActivityFragment findTokenLoginAuthenticationFragment() {
        Intent intent = getIntent();

        String token = intent.getStringExtra(AuthenticationActivityFragment.TOKEN);

        return AuthenticationActivityFragment.newInstance(provider, token);
    }

    @Override
    public void onBackPressed() {}
}
