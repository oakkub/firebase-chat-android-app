package com.oakkub.chat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.widget.TextView;

import com.oakkub.chat.R;
import com.oakkub.chat.fragments.AuthenticationFragment;
import com.oakkub.chat.utils.FirebaseUtil;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.State;

public class AuthenticationActivity extends BaseActivity {

    private static final String AUTHENTICATION_TAG = "tag:authenticationFragment";

    @Bind(R.id.login_process_root_view)
    CoordinatorLayout rootView;

    @Bind(R.id.logging_in_text_view)
    TextView loggingTextView;

    @State
    String provider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
        provider = intent.getStringExtra(AuthenticationFragment.PROVIDER);
    }

    private void setViews() {

        setStatusBarColor(getStatusBarColor());
        rootView.setBackgroundColor(getCompatColor(getBackgroundColor()));

        loggingTextView.setText(getString(R.string.authenticating_user));
    }

    private int getStatusBarColor() {
        if (FirebaseUtil.isGoogleLogin(provider)) return R.color.colorPrimaryDark;
        else if (FirebaseUtil.isFacebookLogin(provider)) return R.color.darkerBlue;
        else return R.color.colorPrimary;
    }

    private int getBackgroundColor() {
        if (FirebaseUtil.isGoogleLogin(provider)) return R.color.tomato;
        else if (FirebaseUtil.isFacebookLogin(provider)) return R.color.darkBlue;
        else return R.color.colorPrimary;
    }

    private void findAuthenticationFragment() {
        AuthenticationFragment authenticationFragment =
                (AuthenticationFragment) findFragmentByTag(AUTHENTICATION_TAG);
        if (authenticationFragment == null) {
            if (FirebaseUtil.isEmailLogin(provider)) {
                authenticationFragment = emailLoginAuthenticationFragment();
            } else {
                authenticationFragment = tokenLoginAuthenticationFragment();
            }
            addFragmentByTag(authenticationFragment, AUTHENTICATION_TAG);
        }
    }

    private AuthenticationFragment emailLoginAuthenticationFragment() {
        Intent intent = getIntent();

        String email = intent.getStringExtra(AuthenticationFragment.EMAIL);
        String password = intent.getStringExtra(AuthenticationFragment.PASSWORD);

        return AuthenticationFragment.newInstance(provider, email, password);
    }

    private AuthenticationFragment tokenLoginAuthenticationFragment() {
        Intent intent = getIntent();

        String token = intent.getStringExtra(AuthenticationFragment.TOKEN);

        return AuthenticationFragment.newInstance(provider, token);
    }

    @Override
    public void onBackPressed() {}
}
