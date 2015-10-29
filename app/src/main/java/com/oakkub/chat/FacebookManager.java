package com.oakkub.chat;

import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;

import static com.oakkub.chat.utils.TextUtil.FACEBOOK_PROVIDER;

/**
 * Created by OaKKuB on 10/12/2015.
 */
public class FacebookManager implements FacebookCallback<LoginResult> {

    private static final String TAG = FacebookManager.class.getSimpleName();

    /* Facebook variables */
    private CallbackManager facebookCallbackManager;
    private AccessTokenTracker facebookAccessTokenTracker;

    private Firebase firebase;
    private AuthData authData;

    public FacebookManager(Firebase firebase, AuthData authData) {
        this.firebase = firebase;
        this.authData = authData;
    }

    /* Facebook Method */
    private void setupFacebookComponent() {

        facebookCallbackManager = CallbackManager.Factory.create();

        facebookAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {
                if (currentAccessToken != null) {
                    // there is login
                    authenticateWithFacebook(currentAccessToken.getToken(), null);
                } else {
                    unAuthenticateWithFacebook();
                }
            }
        };
    }

    /* Facebook Method */
    private void authenticateWithFacebook(String token, Firebase.AuthResultHandler authResultHandler) {

        firebase.authWithOAuthToken(FACEBOOK_PROVIDER, token,
                 authResultHandler);
    }

    /* Facebook Method */
    private void unAuthenticateWithFacebook() {

        if (authData != null && authData.getProvider().equals(FACEBOOK_PROVIDER)) {
            LoginManager.getInstance().logOut();

        }
    }

    /* Facebook Override Method */
    @Override
    public void onSuccess(LoginResult loginResult) {
        Log.e(TAG, "Facebook login success: " + loginResult.toString());
    }

    /* Facebook Override Method */
    @Override
    public void onCancel() {
        Log.e(TAG, "Facebook login has been canceled");
    }

    /* Facebook Override Method */
    @Override
    public void onError(FacebookException error) {
        Log.e(TAG, error.toString());
    }
}
