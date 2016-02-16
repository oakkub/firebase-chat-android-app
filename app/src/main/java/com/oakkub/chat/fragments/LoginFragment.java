package com.oakkub.chat.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.common.SignInButton;
import com.oakkub.chat.R;
import com.oakkub.chat.activities.FacebookLoginActivity;
import com.oakkub.chat.activities.GoogleLoginActivity;
import com.oakkub.chat.utils.NetworkUtil;
import com.oakkub.chat.utils.Util;
import com.oakkub.chat.views.widgets.viewpager.ViewPagerCommunicator;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.State;

/**
 * A placeholder fragment containing a simple view.
 */
public class LoginFragment extends BaseFragment implements View.OnClickListener {

    @Bind(R.id.login_with_email_button)
    Button emailLoginButton;

    /* Google view */
    @Bind(R.id.google_login_button)
    SignInButton googleLoginButton;

    /* Facebook view */
    @Bind(R.id.facebook_login_button)
    Button facebookLoginButton;

    @State
    boolean firstTime = true;

    private ViewPagerCommunicator viewPagerCommunicator;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        viewPagerCommunicator = (ViewPagerCommunicator) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setView();
    }

    private void setView() {
        emailLoginButton.setOnClickListener(this);
        googleLoginButton.setOnClickListener(this);
        facebookLoginButton.setOnClickListener(this);

        googleLoginButton.setSize(SignInButton.SIZE_WIDE);
    }

    /* Google method */
    private void loginWithGoogle() {
        goToLogin(GoogleLoginActivity.class, GoogleLoginActivity.ACTION_LOGIN);
    }

    /* Facebook method */
    private void loginWithFacebook() {
        goToLogin(FacebookLoginActivity.class, FacebookLoginActivity.ACTION_LOGIN);
    }

    private void goToLogin(Class<?> cls, String action) {

        if (!NetworkUtil.isNetworkConnected(getActivity())) {
            Util.showSnackBar(getView(), getString(R.string.error_message_internet_connection));
            return;
        }

        Intent goToLogin = new Intent(getActivity(), cls);
        goToLogin.setAction(action);
        startActivity(goToLogin);
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.login_with_email_button:

                viewPagerCommunicator.setCurrentItem(1);

                break;

            case R.id.facebook_login_button:

                loginWithFacebook();

                break;

            case R.id.google_login_button:

                loginWithGoogle();

                break;

        }

    }

    @Override
    public void onDetach() {
        super.onDetach();

        viewPagerCommunicator = null;
    }
}
