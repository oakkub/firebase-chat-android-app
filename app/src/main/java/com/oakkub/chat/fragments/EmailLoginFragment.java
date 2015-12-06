package com.oakkub.chat.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.oakkub.chat.R;
import com.oakkub.chat.activities.AuthenticationActivity;
import com.oakkub.chat.activities.RegisterActivity;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.utils.PrefsUtil;
import com.oakkub.chat.utils.TextUtil;
import com.oakkub.chat.utils.Util;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import dagger.Lazy;

/**
 * A simple {@link Fragment} subclass.
 */
public class EmailLoginFragment extends Fragment
        implements View.OnClickListener,
                    EditText.OnEditorActionListener {

    /* Email login view */
    @Bind(R.id.email_edittext)
    EditText emailEditText;
    @Bind(R.id.password_edittext)
    EditText passwordEditText;
    @Bind(R.id.email_register_button)
    Button emailRegisterButton;
    @Bind(R.id.email_login_button)
    Button emailLoginButton;

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
        View rootView = inflater.inflate(R.layout.email_login_fragment, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        passwordEditText.setOnEditorActionListener(this);
        emailLoginButton.setOnClickListener(this);
        emailRegisterButton.setOnClickListener(this);

        if (emailEditText.getText().toString().isEmpty()) {
            emailEditText.setText(prefs.getString(PrefsUtil.PREF_EMAIL, ""));
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {

        switch (actionId) {

            case EditorInfo.IME_ACTION_DONE:

                Util.hideSoftKeyboard(getActivity());
                loginWithEmail();

                return true;
        }

        return false;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.email_register_button:

                goToRegisterActivity();

                break;

            case R.id.email_login_button:

                loginWithEmail();

                break;

        }

    }

    private void goToRegisterActivity() {

        Intent intent = new Intent(getActivity(), RegisterActivity.class);
        startActivity(intent);
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void loginWithEmail() {

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

        rememberEmail(email);
        beginAuthentication(email, password);
    }

    private void rememberEmail(String email) {
        editor.get().putString(PrefsUtil.PREF_EMAIL, email).apply();
    }

    private void beginAuthentication(String email, String password) {

        Intent authenticationIntent = new Intent(getActivity(), AuthenticationActivity.class);
        authenticationIntent.putExtra(AuthenticationActivityFragment.PROVIDER, TextUtil.EMAIL_PROVIDER);
        authenticationIntent.putExtra(AuthenticationActivityFragment.EMAIL, email);
        authenticationIntent.putExtra(AuthenticationActivityFragment.PASSWORD, password);

        startActivity(authenticationIntent);
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

}
