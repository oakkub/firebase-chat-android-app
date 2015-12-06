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
import com.oakkub.chat.activities.FacebookLoginActivity;
import com.oakkub.chat.utils.TextUtil;
import com.oakkub.chat.utils.Util;

import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class FacebookLoginActivityFragment extends Fragment implements FacebookCallback<LoginResult> {

    public static final int RC_FACEBOOK = 2000;

    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;

    private String action;

    public static FacebookLoginActivityFragment newInstance(String action) {
        Bundle args = new Bundle();
        args.putString(FacebookLoginActivity.ACTION, action);

        FacebookLoginActivityFragment facebookLoginActivityFragment = new FacebookLoginActivityFragment();
        facebookLoginActivityFragment.setArguments(args);

        return facebookLoginActivityFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        checkAction();
        setupFacebook();

        action = getArguments().getString(FacebookLoginActivity.ACTION);

        if (action == null) finishActivity();
        else if (action.equals(FacebookLoginActivity.LOGIN_ACTION)) performLogin();
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
                    logoutSuccess();
                }
            }
        };
        LoginManager.getInstance().registerCallback(callbackManager, this);

    }

    private void checkAction() {

        action = getActivity().getIntent().getAction();

        if (action == null) finishActivity();
        else if (action.equals(FacebookLoginActivity.LOGIN_ACTION) ||
                action.equals(FacebookLoginActivity.LOGOUT_ACTION)) return;
        else finishActivity();
    }

    private void performLogin() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LoginManager.getInstance().
                        logInWithReadPermissions(FacebookLoginActivityFragment.this,
                                Arrays.asList("public_profile", "email"));
            }
        }, 500);

    }

    private void performLogout() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LoginManager.getInstance().logOut();
            }
        }, 1000);
    }

    private void logoutSuccess() {
        finishActivity();
    }

    private void beginAuthentication(String token) {

        Intent authenticationIntent =
                Util.intentClearActivity(getActivity().getApplicationContext(),
                        AuthenticationActivity.class);
        authenticationIntent.putExtra(AuthenticationActivityFragment.PROVIDER,
                                        TextUtil.FACEBOOK_PROVIDER);
        authenticationIntent.putExtra(AuthenticationActivityFragment.TOKEN, token);

        startActivity(authenticationIntent);
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void finishActivity() {
        getActivity().finish();
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
