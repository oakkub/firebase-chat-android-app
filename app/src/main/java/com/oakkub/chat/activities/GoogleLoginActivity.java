package com.oakkub.chat.activities;

import android.accounts.Account;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.util.Log;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.oakkub.chat.R;
import com.oakkub.chat.fragments.AuthenticationFragment;
import com.oakkub.chat.models.eventbus.EventBusGoogleLoginInfo;
import com.oakkub.chat.utils.TextUtil;
import com.oakkub.chat.utils.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.State;

/**
 * Created by OaKKuB on 10/16/2015.
 */
public class GoogleLoginActivity extends BaseActivity
        implements GoogleApiClient.OnConnectionFailedListener,
                   GoogleApiClient.ConnectionCallbacks {

    public static final int RC_GOOGLE = 1002;
    public static final String ACTION_LOGIN = "login";
    public static final String ACTION_LOGOUT = "logout";

    private static final int REQUEST_CODE_SOLVING_ERROR = 1001;
    private static final String DIALOG_ERROR = "dialog_error";
    private static final String PROFILE_SCOPE = "https://www.googleapis.com/auth/userinfo.profile";
    private static final String EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";

    @Bind(R.id.login_process_root_view)
    CoordinatorLayout rootView;
    @Bind(R.id.logging_in_text_view)
    TextView loggingInTextView;

    @State
    boolean isResolvingError;

    @State
    boolean isTryAgain;

    @State
    String action;

    private GoogleSignInOptions googleSignInOptions;
    private GoogleApiClient googleApiClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_process);
        ButterKnife.bind(this);

        getDataFromIntent(savedInstanceState);

        setViews();

        googleSignInOptions = buildGoogleSignOptions();
        googleApiClient = buildApiClient(this);

        if (savedInstanceState == null) {
            login();
        }
    }

    private void setViews() {
        loggingInTextView.setText(action.equals(ACTION_LOGIN) ?
                getString(R.string.logging_in_with_google) : getString(R.string.logging_out_with_google));
    }

    private void getDataFromIntent(Bundle savedInstanceState) {
        if (savedInstanceState != null) return;
        action = getIntent().getAction();
    }

    private GoogleSignInOptions buildGoogleSignOptions() {
        return new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
    }

    private GoogleApiClient buildApiClient(Context context) {
        return new GoogleApiClient.Builder(context)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();
    }

    private void login() {
        if (action.equals(ACTION_LOGIN)) {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
            startActivityForResult(signInIntent, RC_GOOGLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (action.equals(ACTION_LOGOUT)) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {

        if (action.equals(ACTION_LOGIN)) {
            isResolvingError = true;
            googleApiClient.disconnect();
            fadeOutFinish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE && resultCode == RESULT_OK) {

            GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(googleSignInResult);

        } else {
            onBackPressed();
        }
    }

    private void handleSignInResult(GoogleSignInResult googleSignInResult) {
        if (googleSignInResult.isSuccess()) {

            if (action.equals(ACTION_LOGOUT)) {
                logout(false);
                fadeOutFinish();
            } else {
                GoogleSignInAccount account = googleSignInResult.getSignInAccount();
                onGoogleServicesConnected(account);
            }
        } else {
            fadeOutFinish();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (action.equals(ACTION_LOGOUT)) {
            logout(false);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (!isTryAgain) {
            login();
            isTryAgain = true;
        } else {
            onBackPressed();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        final int errorCode = result.getErrorCode();
        Log.e("google errror code", String.valueOf(errorCode));
        Log.e("google errror code", String.valueOf(isResolvingError));
        Crashlytics.log(result.getErrorCode() + " : " + result.getErrorMessage());
    }

    private void onGoogleServicesConnected(GoogleSignInAccount account) {
        if (account.getDisplayName() == null) {
            onEvent(null);
            return;
        }

        Intent tokenService = new Intent(getApplicationContext(), GetGoogleTokenService.class);
        tokenService.putExtra(GetGoogleTokenService.EXTRA_EMAIL, account.getEmail());
        tokenService.putExtra(GetGoogleTokenService.EXTRA_SCOPES, getScopes(account.getGrantedScopes()));
        startService(tokenService);
    }

    private String getScopes(Set<Scope> grantedScopes) {
        StringBuilder builder = new StringBuilder(39 + 46 + 48);
        for (Scope scope : grantedScopes) {
            builder.append(scope.toString().startsWith("http") ? scope.toString() : "").append(" ");
        }

        return builder.toString().trim();
    }

    @Subscribe
    public void onEvent(EventBusGoogleLoginInfo eventBusGoogleLoginInfo) {

        if (eventBusGoogleLoginInfo == null) {
            onBackPressed();
        } else if (eventBusGoogleLoginInfo.token == null) {
            onBackPressed();
        } else {

            Intent authenticateIntent = Util.intentClearActivity(this, AuthenticationActivity.class);
            authenticateIntent.putExtra(AuthenticationFragment.PROVIDER, TextUtil.GOOGLE_PROVIDER);
            authenticateIntent.putExtra(AuthenticationFragment.TOKEN, eventBusGoogleLoginInfo.token);

            startActivity(authenticateIntent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }

    }

    public void logout(boolean revokeAccessAndDisconnect) {

        Auth.GoogleSignInApi.signOut(googleApiClient)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        Log.e("google logout", String.valueOf(status.getStatusCode()));
                    }
                });

        googleApiClient.disconnect();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fadeOutFinish();
            }
        }, 1000);
    }

    public static class GetGoogleTokenService extends IntentService {

        public static final String EXTRA_EMAIL = "extra:email";
        public static final String EXTRA_SCOPES = "extra:scopes";
        private static final String TAG = GetGoogleTokenService.class.getSimpleName();

        public GetGoogleTokenService() {
            super(TAG);
        }

        @Override
        protected void onHandleIntent(Intent intent) {

            String email = intent.getStringExtra(EXTRA_EMAIL);
            String scopes = "oauth2:" + intent.getStringExtra(EXTRA_SCOPES);
            Account account = new Account(email, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);

            try {
                String token = GoogleAuthUtil.getToken(getApplicationContext(), account, scopes);
                EventBusGoogleLoginInfo eventBusGoogleLoginInfo = new EventBusGoogleLoginInfo(token);

                // send data back to onEvent method
                EventBus.getDefault().post(eventBusGoogleLoginInfo);

            } catch (UserRecoverableAuthException e) {
                handleTokenException(e);
            } catch (GoogleAuthException e) {
                handleTokenException(e);
            } catch (IOException e) {
                handleTokenException(e);
            } catch (Exception e) {
                handleTokenException(e);
            }

        }

        private void handleTokenException(Exception e) {
            Log.e("GetTokenService", e.getMessage());
            EventBus.getDefault().post(new EventBusGoogleLoginInfo(null));
        }
    }

}
