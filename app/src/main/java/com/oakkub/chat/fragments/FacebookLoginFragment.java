package com.oakkub.chat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.oakkub.chat.activities.AuthenticationActivity;
import com.oakkub.chat.activities.BaseActivity;
import com.oakkub.chat.activities.FacebookLoginActivity;
import com.oakkub.chat.utils.TextUtil;
import com.oakkub.chat.utils.Util;

import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class FacebookLoginFragment extends Fragment implements FacebookCallback<LoginResult> {

    public static final int RC_FACEBOOK = 2000;

    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;

    private String action;

    public static FacebookLoginFragment newInstance(String action) {
        Bundle args = new Bundle();
        args.putString(FacebookLoginActivity.ACTION, action);

        FacebookLoginFragment facebookLoginFragment = new FacebookLoginFragment();
        facebookLoginFragment.setArguments(args);

        return facebookLoginFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        checkAction();
        setupFacebook();

        action = getArguments().getString(FacebookLoginActivity.ACTION);

        if (action == null) finishActivity();
        else if (action.equals(FacebookLoginActivity.ACTION_LOGIN)) performLogin();
        else performLogout();
    }

    private void setupFacebook() {

        callbackManager = CallbackManager.Factory.create();
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken != null) {
                    beginAuthentication(currentAccessToken.getToken());
                } else {
                    finishActivity();
                }
            }
        };
        LoginManager.getInstance().registerCallback(callbackManager, this);
    }

    private void checkAction() {

        action = getActivity().getIntent().getAction();

        if (action == null) finishActivity();
        else if (action.equals(FacebookLoginActivity.ACTION_LOGIN) ||
                action.equals(FacebookLoginActivity.ACTION_LOGOUT)) return;
        else finishActivity();
    }

    private void performLogin() {
        LoginManager.getInstance().logInWithReadPermissions(this,
                Arrays.asList("public_profile", "email"));
    }

    private void performLogout() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LoginManager.getInstance().logOut();
            }
        }, 500);
    }

    void logoutSuccess() {
        finishActivity();
    }

    void beginAuthentication(String token) {

        Intent authenticationIntent = Util.intentClearActivity(getActivity().getApplicationContext(),
                        AuthenticationActivity.class);
        authenticationIntent.putExtra(AuthenticationFragment.PROVIDER, TextUtil.FACEBOOK_PROVIDER);
        authenticationIntent.putExtra(AuthenticationFragment.TOKEN, token);

        startActivity(authenticationIntent);
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    void finishActivity() {
        ((BaseActivity) getActivity()).fadeOutFinish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSuccess(LoginResult loginResult) {}

    @Override
    public void onCancel() {
        finishActivity();
    }

    @Override
    public void onError(FacebookException error) {
        finishActivity();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (accessTokenTracker.isTracking()) {
            accessTokenTracker.stopTracking();
        }
    }

}
