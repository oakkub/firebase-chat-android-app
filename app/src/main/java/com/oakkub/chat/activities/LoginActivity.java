package com.oakkub.chat.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.animation.Animation;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.oakkub.chat.R;
import com.oakkub.chat.managers.AppComponent;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.managers.Font;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.GoogleUtil;
import com.oakkub.chat.utils.TextUtil;
import com.oakkub.chat.views.adapters.LoginViewPagerAdapter;
import com.oakkub.chat.views.widgets.viewpager.ViewPagerCommunicator;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity implements ViewPagerCommunicator {

    public static final String LOGIN_FAILED = "loginFailed";
    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final String SNACK_BAR_SHOWED_STATE = "snackBarShowedState";
    @Bind(R.id.login_root_view)
    CoordinatorLayout rootView;
    @Bind(R.id.application_name_textview)
    TextView applicationNameTextView;
    @Bind(R.id.login_viewpager)
    ViewPager viewPager;

    @Inject
    @Named(FirebaseUtil.NAMED_ROOT)
    Firebase firebase;

    private boolean isErrorSnackBarShowed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppController.getComponent(this).inject(this);

        if (checkGooglePlayServices()) {
            checkAuthentication();
        }

        if (savedInstanceState != null) {
            isErrorSnackBarShowed = savedInstanceState.getBoolean(SNACK_BAR_SHOWED_STATE, false);
        }

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        if (!isErrorSnackBarShowed) {
            loginFailed(getIntent());
        }

        setupView();
        setupAnimation(savedInstanceState);
    }

    private void checkAuthentication() {
        try {
            if (firebase.getAuth() != null && firebase.getAuth().getUid() != null) {
                goToMainActivity();
            }
        } catch (NullPointerException e) {
        }
    }

    private void loginFailed(Intent intent) {

        if (intent.hasExtra(LOGIN_FAILED)) {
            Snackbar.make(rootView, intent.getStringExtra(LOGIN_FAILED), Snackbar.LENGTH_LONG).show();
            isErrorSnackBarShowed = true;
        }
    }

    private void setupView() {

        applicationNameTextView.setTypeface(Font.get(this, TextUtil.POETSENONE_FONT));

        LoginViewPagerAdapter loginViewPagerAdapter =
                new LoginViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(loginViewPagerAdapter);
    }

    private void setupAnimation(Bundle onSavedInstanceState) {

        if (onSavedInstanceState == null) {

            AppComponent appComponent = AppController.getComponent(this);

            Animation alphaScaleAnim = appComponent.scaleAlphaAnimation();
            applicationNameTextView.startAnimation(alphaScaleAnim);

            Animation alphaAnim = appComponent.alphaAnimation();
            alphaAnim.setStartOffset(alphaScaleAnim.getDuration() + 100);
            viewPager.startAnimation(alphaAnim);
        }
    }

    private void goToMainActivity() {

        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.setAction(MainActivity.LOGIN_SUCCESS_ACTION);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Log.e(TAG, "onNewIntent");

        loginFailed(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(SNACK_BAR_SHOWED_STATE, isErrorSnackBarShowed);
    }

    @Override
    public void setCurrentItem(int position) {
        viewPager.setCurrentItem(position);
    }

    @Override
    public void onBackPressed() {

        if (viewPager.getCurrentItem() != 0) setCurrentItem(0);
        else super.onBackPressed();

    }

    private boolean checkGooglePlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        final int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {

            if (googleApiAvailability.isUserResolvableError(resultCode)) {

                googleApiAvailability.getErrorDialog(this, resultCode,
                        GoogleUtil.REQUEST_CODE_UPDATE_GOOGLE_PLAY)
                        .show();
            } else {

                try {
                    throw new Exception("This device is not support");
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    return false;
                }
            }
        }

        return true;
    }

    private void generateFacebookKeyHash() {

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

    }

}
