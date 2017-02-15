package com.oakkub.chat.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.oakkub.chat.R;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.managers.Font;
import com.oakkub.chat.utils.GCMUtil;
import com.oakkub.chat.utils.TextUtil;
import com.oakkub.chat.utils.Util;
import com.oakkub.chat.views.adapters.LoginViewPagerAdapter;
import com.oakkub.chat.views.transformers.ParallaxLoginPageTransformer;
import com.oakkub.chat.views.widgets.MyToast;
import com.oakkub.chat.views.widgets.viewpager.ViewPagerCommunicator;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.State;
import me.relex.circleindicator.CircleIndicator;

public class LoginActivity extends BaseActivity implements ViewPagerCommunicator {

    public static final String LOGIN_FAILED = "loginFailed";
    private static final String TAG = LoginActivity.class.getSimpleName();

    @BindView(R.id.application_name_textview)
    TextView appNameTextView;

    @BindView(R.id.login_viewpager)
    ViewPager viewPager;

    @BindView(R.id.viewpager_indicator)
    CircleIndicator viewPagerIndicator;

    @State
    boolean isErrorToastShowed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AppController.getComponent(this).inject(this);
        ButterKnife.bind(this);
        checkGooglePlayServices();

        if (!isErrorToastShowed) {
            loginFailed(getIntent());
        }
        setupView();
    }

    private void loginFailed(Intent intent) {

        if (intent.hasExtra(LOGIN_FAILED)) {
            MyToast.make(intent.getStringExtra(LOGIN_FAILED)).show();
            isErrorToastShowed = true;
        }
    }

    private void setupView() {
        int appNameVisibility = Util.isLandScape() ? View.GONE : View.VISIBLE;

        appNameTextView.setTypeface(Font.getInstance().get(TextUtil.POETSENONE_FONT));
        appNameTextView.setVisibility(appNameVisibility);

        LoginViewPagerAdapter loginViewPagerAdapter =
                new LoginViewPagerAdapter(getSupportFragmentManager());
        viewPager.setPageTransformer(true, new ParallaxLoginPageTransformer());
        viewPager.setAdapter(loginViewPagerAdapter);
        viewPagerIndicator.setViewPager(viewPager);
    }

    private void goToMainActivity() {

        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        loginFailed(intent);
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

    @Override
    protected void onPause() {
        super.onPause();
        appNameTextView.clearAnimation();
        viewPager.clearAnimation();
    }

    private boolean checkGooglePlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {

            if (googleApiAvailability.isUserResolvableError(resultCode)) {

                googleApiAvailability.getErrorDialog(this, resultCode,
                        GCMUtil.REQUEST_CODE_UPDATE_GOOGLE_PLAY, new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                finish();
                            }
                        })
                        .show();
            } else {

                try {
                    throw new Exception("This device is not support");
                } catch (Exception e) {
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
