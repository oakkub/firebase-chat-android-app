package com.oakkub.chat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.oakkub.chat.R;
import com.oakkub.chat.activities.LoginActivity;
import com.oakkub.chat.activities.MainActivity;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.models.GoogleInstanceID;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.services.GCMRegistrationIntentService;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.PrefsUtil;
import com.oakkub.chat.utils.TextUtil;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

/**
 * A placeholder fragment containing a simple view.
 */
public class AuthenticationActivityFragment extends Fragment {

    public static final String PROVIDER = "provider";
    public static final String PASSWORD = "password";
    public static final String EMAIL = "email";
    private static final String TAG = AuthenticationActivityFragment.class.getSimpleName();
    @Bind(R.id.login_process_root_view)
    RelativeLayout rootView;
    @Bind(R.id.logging_in_text_view)
    TextView loggingTextView;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOT)
    Firebase firebase;

    @Inject
    @Named(FirebaseUtil.NAMED_USER_INFO)
    Firebase firebaseUserInfo;

    private AuthData authData;

    private String provider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        AppController.getComponent(getActivity()).inject(this);

        beginAuthentication();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.login_process, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setViews();
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

    private void setViews() {

        rootView.setBackgroundColor(ContextCompat.getColor(getActivity(), getBackgroundColor()));

        loggingTextView.setText(getString(R.string.authenticating_user));

    }

    private int getBackgroundColor() {
        if (FirebaseUtil.isGoogleLogin(provider)) return R.color.tomato;
        else if (FirebaseUtil.isFacebookLogin(provider)) return R.color.darkBlue;
        else return R.color.colorPrimary;
    }

    private void beginAuthentication() {
        Intent intent = getActivity().getIntent();

        provider = intent.getStringExtra(PROVIDER);

        if (FirebaseUtil.isEmailLogin(provider)) authenticateWithEmail(intent);
        else authenticateWithToken(intent);
    }

    private void authenticateWithToken(Intent intent) {

        final String token = intent.getStringExtra(TextUtil.TOKEN);

        Log.e(TAG, token);

        firebase.authWithOAuthToken(provider, token,
                new AuthenticationResultHandler());
    }

    private void authenticateWithEmail(Intent intent) {

        final String email = intent.getStringExtra(EMAIL);
        final String password = intent.getStringExtra(PASSWORD);

        firebase.authWithPassword(email, password,
                new AuthenticationResultHandler());
    }

    private void checkUserData(AuthData authData) {

        firebaseUserInfo.child(authData.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null ||
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

        final String email = String.valueOf(providerData.get(FirebaseUtil.PROVIDER_EMAIL));
        final String profileImageURL = String.valueOf(providerData.get(FirebaseUtil.PROVIDER_PROFILE_IMAGE));
        final String displayName = FirebaseUtil.isFirebaseLogin(provider) ?
                email.split("@")[0]
                :
                String.valueOf(providerData.get(FirebaseUtil.PROVIDER_DISPLAY_NAME));

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put(UserInfo.EMAIL, email);
        userInfo.put(UserInfo.DISPLAY_NAME, displayName);
        userInfo.put(UserInfo.PROFILE_IMAGE_URL, profileImageURL);
        userInfo.put(UserInfo.REGISTERED_DATE, System.currentTimeMillis());

        return userInfo;
    }

    @Subscribe
    public void onEvent(GoogleInstanceID googleInstanceID) {
        if (googleInstanceID == null) {
            backToLoginActivity(getString(R.string.error_message_network));
        } else {
            loginSuccess();
        }
    }

    private void loginSuccess() {

        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setAction(MainActivity.LOGIN_SUCCESS_ACTION);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishActivity();
    }

    private void finishActivity() {
        getActivity().finish();
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void unAuthenticateWithFirebase() {

        if (authData != null) {

            firebase.unauth();
            authData = null;
        }
    }

    private void backToLoginActivity(String errorMessage) {

        Intent backToLoginActivityIntent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);

        backToLoginActivityIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        backToLoginActivityIntent.putExtra(LoginActivity.LOGIN_FAILED, errorMessage);
        startActivity(backToLoginActivityIntent);
        finishActivity();
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

        @Override
        public void onAuthenticated(AuthData authDataResult) {
            Log.e(TAG, "onAuthenticated");

            // check if user is newcomer or not.
            checkUserData(authDataResult);
            authData = authDataResult;
        }

        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
            Log.e(TAG, String.valueOf(firebaseError.getMessage()));

            handleFirebaseError(firebaseError);
            unAuthenticateWithFirebase();
        }
    }

}
