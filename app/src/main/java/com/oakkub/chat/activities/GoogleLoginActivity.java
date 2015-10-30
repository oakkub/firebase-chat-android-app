package com.oakkub.chat.activities;

import android.accounts.Account;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.oakkub.chat.fragments.AuthenticationActivityFragment;
import com.oakkub.chat.models.GoogleLoginInfo;
import com.oakkub.chat.utils.TextUtil;
import com.oakkub.chat.utils.Util;
import com.oakkub.chat.views.dialogs.GoogleErrorDialog;

import java.io.IOException;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

/**
 * Created by OaKKuB on 10/16/2015.
 */
public class GoogleLoginActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener,
                   GoogleApiClient.ConnectionCallbacks {

    public static final int RC_GOOGLE = 1002;
    public static final String ACTION_LOGIN = "login";
    public static final String ACTION_LOGOUT = "logout";

    private static final int REQUEST_CODE_SOLVING_ERROR = 1001;
    private static final String DIALOG_ERROR = "dialog_error";
    private static final String STATE_RESOLVING_ERROR = "state_resolving_error";
    private static final String STATE_FIRST_TIME = "state_first_time";
    private static final String STATE_ACTION = "action";
    private static final String PROFILE_SCOPE = "https://www.googleapis.com/auth/userinfo.profile";
    private static final String EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";

    @Bind(R.id.login_process_root_view)
    RelativeLayout rootView;
    @Bind(R.id.logging_in_text_view)
    TextView loggingInTextView;

    private GoogleSignInOptions googleSignInOptions;
    private GoogleApiClient googleApiClient;

    private boolean isResolvingError;
    private boolean isFirstTime = true;
    private String action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_process);
        ButterKnife.bind(this);

        restoreVariableState(savedInstanceState);

        setViews();

        googleSignInOptions = buildGoogleSignOptions();
        googleApiClient = buildApiClient(this);

        if (savedInstanceState == null) {
            login();
        }
    }

    private void setViews() {

        rootView.setBackgroundColor(ContextCompat.getColor(this, R.color.tomato));

        loggingInTextView.setText(action.equals(ACTION_LOGIN) ?
                getString(R.string.logging_in_with_google) : getString(R.string.logging_out_with_google));

    }

    private void restoreVariableState(Bundle savedInstanceState) {

        Intent intent = getIntent();
        if (intent != null) {
            action = intent.getAction();
        }

        if (savedInstanceState != null) {
            isResolvingError = savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);
            isFirstTime = savedInstanceState.getBoolean(STATE_FIRST_TIME, false);
        }
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, isResolvingError);
        outState.putBoolean(STATE_FIRST_TIME, isFirstTime);
        outState.putString(STATE_ACTION, action);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (action.equals(ACTION_LOGOUT)) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
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

            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE) {

            GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(googleSignInResult);

        }
    }

    private void handleSignInResult(GoogleSignInResult googleSignInResult) {
        if (googleSignInResult.isSuccess()) {

            if (action.equals(ACTION_LOGOUT)) {
                logout(false);
                finishActivity();
            } else {
                GoogleSignInAccount account = googleSignInResult.getSignInAccount();
                onGoogleServicesConnected(account);
            }
        } else {

            finishActivity();
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
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

        final int errorCode = result.getErrorCode();
        Log.e("google errror code", String.valueOf(errorCode));
        Log.e("google errror code", String.valueOf(isResolvingError));

    }

    private void finishActivity() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
        StringBuilder builder = new StringBuilder();
        for (Scope scope : grantedScopes) {
            builder.append(scope.toString().startsWith("http") ? scope.toString() : "").append(" ");
        }

        return builder.toString().trim();
    }

    @Subscribe
    public void onEvent(GoogleLoginInfo googleLoginInfo) {

        if (googleLoginInfo == null) {

            finishActivity();

        } else {

            Intent authenticateIntent = Util.intentClearActivity(this, AuthenticationActivity.class);
            authenticateIntent.putExtra(AuthenticationActivityFragment.PROVIDER, TextUtil.GOOGLE_PROVIDER);
            authenticateIntent.putExtra(TextUtil.TOKEN, googleLoginInfo.token);

            startActivity(authenticateIntent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }

    }

    public void logout(boolean revokeAccessAndDisconnect) {

        Auth.GoogleSignInApi.signOut(googleApiClient)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.e("google logout", String.valueOf(status.getStatusCode()));
                    }
                });

        googleApiClient.disconnect();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finishActivity();
            }
        }, 1000);
    }

    private void showErrorDialog(int errorCode) {

        GoogleErrorDialog googleErrorDialog =
                GoogleErrorDialog.newInstance(errorCode, REQUEST_CODE_SOLVING_ERROR);
        googleErrorDialog.show(getFragmentManager(), DIALOG_ERROR);
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

            Log.e(TAG, scopes);

            try {
                final String token = GoogleAuthUtil.getToken(getApplicationContext(), account, scopes);
                GoogleLoginInfo googleLoginInfo = new GoogleLoginInfo(token);

                Log.e(TAG, token);

                // send data back to onEvent method
                EventBus.getDefault().post(googleLoginInfo);

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
            EventBus.getDefault().post(null);
        }
    }

}
