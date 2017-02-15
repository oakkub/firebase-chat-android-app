package com.oakkub.chat.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.oakkub.chat.R;
import com.oakkub.chat.activities.AuthenticationActivity;
import com.oakkub.chat.activities.RegisterActivity;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.utils.PrefsUtil;
import com.oakkub.chat.utils.TextUtil;
import com.oakkub.chat.utils.Util;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.Lazy;

/**
 * A simple {@link Fragment} subclass.
 */
public class EmailLoginFragment extends BaseFragment {

    @BindView(R.id.email_edittext)
    EditText emailEditText;

    @BindView(R.id.password_edittext)
    EditText passwordEditText;

    @Inject
    SharedPreferences prefs;

    @Inject
    Lazy<SharedPreferences.Editor> editor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_email_login, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (emailEditText.getText().toString().isEmpty()) {
            emailEditText.setText(prefs.getString(PrefsUtil.PREF_EMAIL, ""));
        }
    }

    @OnClick(R.id.email_login_button)
    public void onEmailLoginButtonClick() {
        login();
    }

    @OnClick(R.id.email_register_button)
    public void onRegisterButtonClick() {
        goToRegisterActivity();
    }

    private void goToRegisterActivity() {
        Intent intent = new Intent(getActivity(), RegisterActivity.class);
        startActivity(intent);
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void login() {

        final String email = emailEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString().trim();

        if (email.length() == 0 || password.length() == 0) {

            Util.showSnackBar(getView(), getString(R.string.error_message_fill_out_information));
            return;
        }

        if (!TextUtil.checkEmailFormat(email)) {

            Util.showSnackBar(getView(), getString(R.string.error_message_email_invalid));
            return;
        }

        editor.get().putString(PrefsUtil.PREF_EMAIL, email).apply();
        beginAuthentication(email, password);
    }

    private void beginAuthentication(String email, String password) {

        Intent authenticationIntent = new Intent(getActivity(), AuthenticationActivity.class);
        authenticationIntent.putExtra(AuthenticationFragment.PROVIDER, TextUtil.EMAIL_PROVIDER);
        authenticationIntent.putExtra(AuthenticationFragment.EMAIL, email);
        authenticationIntent.putExtra(AuthenticationFragment.PASSWORD, password);

        startActivity(authenticationIntent);
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

}
