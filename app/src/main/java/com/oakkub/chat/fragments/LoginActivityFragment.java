package com.oakkub.chat.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.common.SignInButton;
import com.oakkub.chat.R;
import com.oakkub.chat.activities.FacebookLoginActivity;
import com.oakkub.chat.activities.GoogleLoginActivity;
import com.oakkub.chat.activities.LoginActivity;
import com.oakkub.chat.utils.NetworkUtil;
import com.oakkub.chat.utils.Util;
import com.oakkub.chat.views.widgets.viewpager.ViewPagerCommunicator;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class LoginActivityFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = LoginActivityFragment.class.getSimpleName();
    private static final String PACKAGE_NAME = LoginActivityFragment.class.getPackage().getName() + "/";
    private static final String IN_VIEWPAGER = PACKAGE_NAME + TAG + "IN_VIEWPAGER";

    @Bind(R.id.login_with_email_button)
    Button loginWithEmailButton;

    /* Google view */
    @Bind(R.id.google_login_button)
    SignInButton googleLoginButton;

    /* Facebook view */
    @Bind(R.id.facebook_login_button)
    Button facebookButton;

    private Context context;
    private ViewPagerCommunicator viewPagerCommunicator;

    private boolean firstTime = true;

    public static LoginActivityFragment newInstance() {

        Bundle args = new Bundle();
        args.putString(IN_VIEWPAGER, IN_VIEWPAGER);

        LoginActivityFragment loginActivityFragment = new LoginActivityFragment();
        loginActivityFragment.setArguments(args);

        return loginActivityFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;

        viewPagerCommunicator = (ViewPagerCommunicator) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkArguments();

        setRetainInstance(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        ButterKnife.bind(this, rootView);

        setView();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        checkAction();

    }

    private void checkArguments() {

        Bundle args = getArguments();
        if (args == null) {
            getActivity().finish();
        } else {
            if (!args.getString(IN_VIEWPAGER).equals(IN_VIEWPAGER)) {
                getActivity().finish();
            }
        }
    }

    private void checkAction() {

        Intent intent = getActivity().getIntent();
        if (intent.getAction() != null) {

            if (getActivity().getIntent().getAction().equals(LoginActivity.LOGIN_FAILED)) {

                if (firstTime) {
                    Util.showSnackBar(getView(), getString(R.string.error_message_login));
                    firstTime = false;
                }
            }
        }

    }

    private void setView() {

        loginWithEmailButton.setOnClickListener(this);
        googleLoginButton.setOnClickListener(this);
        facebookButton.setOnClickListener(this);

        googleLoginButton.setSize(SignInButton.SIZE_WIDE);

    }

    /* Google method */
    private void loginWithGoogle() {
        goToLogin(GoogleLoginActivity.class, GoogleLoginActivity.ACTION_LOGIN);
    }

    /* Facebook method */
    private void loginWithFacebook() {
        goToLogin(FacebookLoginActivity.class, FacebookLoginActivityFragment.LOGIN_ACTION);
    }

    private void goToLogin(Class<?> cls, String action) {

        if (!NetworkUtil.isNetworkConnected(context)) {
            Util.showSnackBar(getView(), getString(R.string.error_message_internet_connection));
            return;
        }

        Intent goToLogin = new Intent(context, cls);
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

}
