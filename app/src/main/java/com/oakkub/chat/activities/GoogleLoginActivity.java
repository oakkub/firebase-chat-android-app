package com.oakkub.chat.activities;

import android.accounts.Account;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.IntentService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.oakkub.chat.R;
import com.oakkub.chat.fragments.AuthenticationActivityFragment;
import com.oakkub.chat.models.GoogleLoginInfo;
import com.oakkub.chat.utils.TextUtil;
import com.oakkub.chat.utils.Util;

import java.io.IOException;

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
    private static final String DIALOG_ERROR_CODE = "dialog_error_code";
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

        googleApiClient = buildApiClient(this);

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
            action = savedInstanceState.getString(STATE_ACTION, "");
        }
    }

    private GoogleApiClient buildApiClient(Context context) {
        return new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addScope(new Scope(PROFILE_SCOPE))
                .addScope(new Scope(EMAIL_SCOPE))
                .addApi(Plus.API)
                .build();
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

        if (isFirstTime) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    googleApiClient.connect();
                }
            }, 1000);
            isFirstTime = false;
        } else {
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

        if (requestCode == REQUEST_CODE_SOLVING_ERROR) {

            switch (resultCode) {

                case RESULT_OK:

                    if (!googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
                        isResolvingError = true;
                        googleApiClient.connect();
                    }

                    break;

                default:

                    setResult(RESULT_CANCELED);
                    finishActivity();

            }

        }
    }

    @Subscribe
    public void onEvent(GoogleLoginInfo googleLoginInfo) {

        Log.e("google", "onTokenReceived");

        if (googleLoginInfo.token.equalsIgnoreCase("")) {

            finishActivity();

        } else {

            Intent authenticateIntent = Util.intentClearActivity(this, AuthenticationActivity.class);
            authenticateIntent.putExtra(AuthenticationActivityFragment.PROVIDER, TextUtil.GOOGLE_PROVIDER);
            authenticateIntent.putExtra(TextUtil.TOKEN, googleLoginInfo.token);

            startActivity(authenticateIntent);
            finishActivity();

        }

    }

    @Override
    public void onConnected(Bundle bundle) {

        Person person = Plus.PeopleApi.getCurrentPerson(googleApiClient);
        String email = Plus.AccountApi.getAccountName(googleApiClient);

        if (person == null) {
            finishActivity();
            return;
        }

        if (action.equals(ACTION_LOGOUT)) {
            logout(false);
            finishActivity();
            return;
        }

        onGoogleServicesConnected(person, email);
    }

    @Override
    public void onConnectionSuspended(int cause) {

        Log.e("google cause", String.valueOf(cause));

        if (!googleApiClient.isConnected()) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

        final int errorCode = result.getErrorCode();
        Log.e("google errror code", String.valueOf(errorCode));
        Log.e("google errror code", String.valueOf(isResolvingError));

        if (isResolvingError || googleApiClient.isConnected() || action.equals(ACTION_LOGOUT)) {
            return;
        }

        if (result.hasResolution()) {
            try {

                isResolvingError = true;
                result.startResolutionForResult(this, REQUEST_CODE_SOLVING_ERROR);
            } catch (IntentSender.SendIntentException e) {

                isResolvingError = false;
                googleApiClient.connect();
            }
        } else {

            isResolvingError = true;
            showErrorDialog(errorCode);
        }

    }

    private void finishActivity() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void onGoogleServicesConnected(Person person, String email) {

        if (!person.hasDisplayName()) {
            onEvent(null);
            return;
        }
        getToken(email);
    }

    private void getToken(String email) {

        Intent tokenIntent = new Intent(this, GetTokenService.class);
        tokenIntent.putExtra(GetTokenService.EMAIL_EXTRA, email);
        startService(tokenIntent);

    }

    public void logout(boolean revokeAccessAndDisconnect) {

        Plus.AccountApi.clearDefaultAccount(googleApiClient);
        if (revokeAccessAndDisconnect) {
            Plus.AccountApi.revokeAccessAndDisconnect(googleApiClient);
        }

        googleApiClient.disconnect();
    }

    public static class GetTokenService extends IntentService {

        public static final String EMAIL_EXTRA = "GoogleLoginActivity.GetTokenService.EMAIL_EXTRA";

        public GetTokenService() {
            super(GetTokenService.class.getSimpleName());
        }

        @Override
        protected void onHandleIntent(Intent intent) {

            String accountName = intent.getStringExtra(EMAIL_EXTRA);
            String scope = String.format("oauth2:%s %s", PROFILE_SCOPE, EMAIL_SCOPE);

            Account account = new Account(accountName, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);

            try {
                final String token = GoogleAuthUtil.getToken(getApplicationContext(), account, scope);
                GoogleLoginInfo googleLoginInfo = new GoogleLoginInfo(token);

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
            EventBus.getDefault().post(new GoogleLoginInfo(""));
        }

    }

    private void showErrorDialog(int errorCode) {

        GoogleErrorDialog googleErrorDialog = GoogleErrorDialog.newInstance(errorCode);
        googleErrorDialog.show(getFragmentManager(), DIALOG_ERROR);

    }

    public static class GoogleErrorDialog extends DialogFragment {

        public static GoogleErrorDialog newInstance(int errorCode) {

            Bundle args = new Bundle();
            args.putInt(DIALOG_ERROR_CODE, errorCode);

            GoogleErrorDialog googleErrorDialog = new GoogleErrorDialog();
            googleErrorDialog.setArguments(args);

            return googleErrorDialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int errorCode = getArguments().getInt(DIALOG_ERROR_CODE);

            return GoogleApiAvailability.getInstance()
                    .getErrorDialog(getActivity(), errorCode, REQUEST_CODE_SOLVING_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
        }
    }

}
