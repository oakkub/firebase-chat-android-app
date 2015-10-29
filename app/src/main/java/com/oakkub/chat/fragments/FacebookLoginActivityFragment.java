package com.oakkub.chat.fragments;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.oakkub.chat.R;
import com.oakkub.chat.activities.AuthenticationActivity;
import com.oakkub.chat.utils.TextUtil;
import com.oakkub.chat.utils.Util;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class FacebookLoginActivityFragment extends Fragment implements FacebookCallback<LoginResult> {

    private static final String TAG = FacebookLoginActivityFragment.class.getSimpleName();

    public static final String LOGIN_ACTION = "com.oakkub.chat.fragments.FacebookLoginActivityFragment.LOGIN_ACTION";
    public static final String LOGOUT_ACTION = "com.oakkub.chat.fragments.FacebookLoginActivityFragment.LOGOUT_ACTION";
    public static final int RC_FACEBOOK = 2000;

    @Bind(R.id.login_process_root_view)
    RelativeLayout rootView;
    @Bind(R.id.logging_in_text_view)
    TextView loggingTextView;

    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;

    private String action;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        checkAction();
        setupFacebook();

        if (action.equals(LOGIN_ACTION)) performLogin();
        else performLogout();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.login_process, container, false);
        ButterKnife.bind(this, rootView);

        setViews();

        return rootView;
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
        else if (action.equals(LOGIN_ACTION) || action.equals(LOGOUT_ACTION)) return;
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

    private void setViews() {

        rootView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.darkBlue));

        if (action.equals(LOGIN_ACTION)) {
            loggingTextView.setText(getString(R.string.logging_in_with_facebook));
        } else {
            loggingTextView.setText(getString(R.string.logging_out_with_facebook));
        }

    }

    private void beginAuthentication(String token) {

        Intent authenticationIntent =
                Util.intentClearActivity(getActivity().getApplicationContext(),
                        AuthenticationActivity.class);
        authenticationIntent.putExtra(AuthenticationActivityFragment.PROVIDER,
                                        TextUtil.FACEBOOK_PROVIDER);
        authenticationIntent.putExtra(TextUtil.TOKEN, token);

        startActivity(authenticationIntent);
        finishActivity();
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

    public String getAction() {
        return action;
    }
}
