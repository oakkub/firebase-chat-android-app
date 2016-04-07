package com.oakkub.chat.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.R;
import com.oakkub.chat.activities.BaseActivity;
import com.oakkub.chat.activities.LoginActivity;
import com.oakkub.chat.activities.MainActivity;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.models.eventbus.EventBusGoogleInstanceID;
import com.oakkub.chat.services.GCMRegistrationIntentService;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.PrefsUtil;
import com.oakkub.chat.utils.UserInfoUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * A placeholder fragment containing a simple view.
 */
public class AuthenticationFragment extends BaseFragment {

    public static final String PROVIDER = "extra:provider";
    public static final String PASSWORD = "extra:password";
    public static final String EMAIL = "extra:email";
    public static final String TOKEN = "extra:token";

    private static final String TAG = AuthenticationFragment.class.getSimpleName();

    @Inject
    @Named(FirebaseUtil.NAMED_ROOT)
    Firebase firebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_INFO)
    Firebase firebaseUserInfo;

    private AuthData authData;

    public static AuthenticationFragment newInstance(String provider, String token) {
        Bundle args = new Bundle();
        args.putString(PROVIDER, provider);
        args.putString(TOKEN, token);

        return initFragment(args);
    }

    public static AuthenticationFragment newInstance(String provider, String email, String password) {
        Bundle args = new Bundle();
        args.putString(EMAIL, email);
        args.putString(PASSWORD, password);

        return initFragment(args);
    }

    private static AuthenticationFragment initFragment(Bundle args) {
        AuthenticationFragment authenticationFragment = new AuthenticationFragment();
        authenticationFragment.setArguments(args);

        return authenticationFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        AppController.getComponent(getActivity()).inject(this);
        beginAuthentication();
    }

    @Override
    public void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        EventBus.getDefault().unregister(this);
    }

    private void beginAuthentication() {
        Bundle args = getArguments();

        if (args.containsKey(EMAIL) && args.containsKey(PASSWORD)) authenticateWithEmail(args);
        else if (args.containsKey(TOKEN)) authenticateWithToken(args);
        else ((BaseActivity) getActivity()).fadeOutFinish();
    }

    private void authenticateWithToken(Bundle args) {

        final String token = args.getString(TOKEN);
        final String provider = args.getString(PROVIDER);

        assert provider != null;
        assert token != null;

        firebase.authWithOAuthToken(provider, token,
                new AuthenticationResultHandler());
    }

    private void authenticateWithEmail(Bundle args) {

        final String email = args.getString(EMAIL);
        final String password = args.getString(PASSWORD);

        assert email != null;
        assert password != null;

        firebase.authWithPassword(email, password,
                new AuthenticationResultHandler());
    }

    private void checkUserData(AuthData authData) {

        firebaseUserInfo.child(authData.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists() ||
                        PrefsUtil.shouldUpdateInstanceId(getActivity())) {
                    /*user not logged in before, there is no data.
                      or instance id should be updated*/
                    saveUserData();
                } else {
                    // user already logged in, we don't have to save data.
                    saveGCMInstanceID();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                handleFirebaseError(firebaseError);
            }
        });

    }

    private void saveUserData() {

        Map<String, Object> userInfo = getUserInfo(authData);
        firebaseUserInfo.child(authData.getUid()).setValue(userInfo,
                new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {

                if (firebaseError != null) {

                    backToLoginActivity(getString(R.string.error_message_network));
                    return;
                }

                saveGCMInstanceID();
            }
        });
    }

    private void saveGCMInstanceID() {

        Intent gcmInstanceIDService = new Intent(getActivity().getApplicationContext(), GCMRegistrationIntentService.class);
        gcmInstanceIDService.setAction(GCMRegistrationIntentService.LOGIN_ACTION);

        getActivity().startService(gcmInstanceIDService);
    }

    private Map<String, Object> getUserInfo(AuthData authData) {

        Map<String, Object> providerData = authData.getProviderData();

        String email = providerData.get(FirebaseUtil.PROVIDER_EMAIL) == null ?
                ""
                :
                String.valueOf(providerData.get(FirebaseUtil.PROVIDER_EMAIL));
        String displayName = providerData.get(FirebaseUtil.PROVIDER_DISPLAY_NAME) == null ?
                email.split("@")[0]
                :
                String.valueOf(providerData.get(FirebaseUtil.PROVIDER_DISPLAY_NAME));
        String profileImageURL = String.valueOf(providerData.get(FirebaseUtil.PROVIDER_PROFILE_IMAGE));

        ArrayMap<String, Object> userInfo = new ArrayMap<>(4);
        userInfo.put(UserInfoUtil.EMAIL, email);
        userInfo.put(UserInfoUtil.DISPLAY_NAME, displayName);
        userInfo.put(UserInfoUtil.PROFILE_IMAGE_URL, profileImageURL);
        userInfo.put(UserInfoUtil.REGISTERED_DATE, System.currentTimeMillis());

        return userInfo;
    }

    @Subscribe
    public void onEvent(EventBusGoogleInstanceID eventBusGoogleInstanceID) {
        if (eventBusGoogleInstanceID == null) {
            backToLoginActivity(getString(R.string.error_message_network));
        } else {
            loginSuccess();
        }
    }

    private void loginSuccess() {

        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(MainActivity.EXTRA_MY_ID, authData.getUid());
        intent.putExtra(MainActivity.EXTRA_PROVIDER, authData.getProvider());

        startActivity(intent);
    }

    private void unAuthenticateWithFirebase() {
        if (authData != null) {
            firebase.unauth();
            authData = null;
        }
    }

    private void backToLoginActivity(String errorMessage) {
        Intent backToLoginActivityIntent = new Intent(getActivity(), LoginActivity.class);
        backToLoginActivityIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        backToLoginActivityIntent.putExtra(LoginActivity.LOGIN_FAILED, errorMessage);

        startActivity(backToLoginActivityIntent);
        ((BaseActivity) getActivity()).fadeOutFinish();
    }

    private void handleFirebaseError(FirebaseError firebaseError) {

        Log.e(TAG, String.valueOf(firebaseError.getCode()));

        switch (firebaseError.getCode()) {

            case FirebaseError.USER_DOES_NOT_EXIST:
            case FirebaseError.INVALID_PASSWORD:

                Log.e(TAG, "user does not exist");
                backToLoginActivity(getString(R.string.error_message_incorrect_id_or_password));

                break;

            case FirebaseError.EMAIL_TAKEN:

                Log.e(TAG, "email taken");
                backToLoginActivity(getString(R.string.error_message_email_taken));

                break;

            case FirebaseError.INVALID_EMAIL:

                Log.e(TAG, "this email is invalid");
                backToLoginActivity(getString(R.string.error_message_email_invalid));

                break;

            default:

                Log.e(TAG, "error occurred");
                backToLoginActivity(getString(R.string.error_message_network));

                break;

        }

    }

    /* Firebase class */
    private class AuthenticationResultHandler implements Firebase.AuthResultHandler {

        private final String TAG = AuthenticationResultHandler.class.getSimpleName();

        private SharedPreferences prefs;
        private SharedPreferences.Editor editor;

        public AuthenticationResultHandler() {
            prefs = AppController.getComponent(getActivity()).sharedPreferences();
            editor = prefs.edit();
        }

        @Override
        public void onAuthenticated(AuthData authDataResult) {
            Log.e(TAG, "onAuthenticated");

            // check if user is newcomer or not.
            checkUserData(authDataResult);
            authData = authDataResult;

            if (authDataResult != null) {
                editor.putString(PrefsUtil.PREF_UID, authDataResult.getUid());
            } else {
                editor.remove(PrefsUtil.PREF_UID);
            }
            editor.apply();
        }

        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
            Log.e(TAG, String.valueOf(firebaseError.getMessage()));

            unAuthenticateWithFirebase();
            handleFirebaseError(firebaseError);

            editor.remove(PrefsUtil.PREF_UID);
            editor.apply();
        }
    }

}
