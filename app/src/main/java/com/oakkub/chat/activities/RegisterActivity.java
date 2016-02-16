package com.oakkub.chat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.MenuItem;
import android.widget.EditText;

import com.oakkub.chat.R;
import com.oakkub.chat.fragments.AuthenticationFragment;
import com.oakkub.chat.fragments.RegisterFragment;
import com.oakkub.chat.utils.TextUtil;
import com.oakkub.chat.utils.Util;
import com.oakkub.chat.views.dialogs.ProgressDialogFragment;
import com.oakkub.chat.views.widgets.MyToast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class RegisterActivity extends BaseActivity implements RegisterFragment.OnRegisterListener {

    private static final String TAG = RegisterActivity.class.getSimpleName();
    private static final String PROGRESS_DIALOG_TAG = "tag:progressDialog";
    private static final String REGISTER_FRAG_TAG = "tag:registerFragment";

    @Bind(R.id.simple_toolbar)
    Toolbar toolbar;

    @Bind(R.id.email_register_edittext)
    EditText emailEditText;

    @Bind(R.id.password_register_edittext)
    EditText passwordEditText;

    @Bind(R.id.confirm_password_register_edittext)
    EditText confirmPasswordEditText;

    private ProgressDialogFragment progressDialog;
    private RegisterFragment registerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        initInstances();
    }

    private void initInstances() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.registration);
        }

        progressDialog = (ProgressDialogFragment) findFragmentByTag(PROGRESS_DIALOG_TAG);
        if (progressDialog == null) {
            progressDialog = ProgressDialogFragment.newInstance();
        }

        registerFragment = (RegisterFragment) findFragmentByTag(REGISTER_FRAG_TAG);
        if (registerFragment == null) {
            registerFragment = (RegisterFragment)
                    addFragmentByTag(new RegisterFragment(), REGISTER_FRAG_TAG);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:

                fadeOutFinish();

                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        fadeOutFinish();
    }

    @OnClick(R.id.register_button)
    public void onRegisterButtonClick() {
        checkInput();
    }

    @OnTextChanged(value = R.id.confirm_password_register_edittext, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onConfirmedPasswordEditTextChanged(Editable editable) {
        String confirmedPassword = editable.toString();
        checkConfirmedPassword(confirmedPassword);
    }

    private void checkInput() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmedPassword = confirmPasswordEditText.getText().toString().trim();

        if (email.length() == 0 || password.length() == 0) {
            MyToast.make(getString(R.string.error_message_fill_out_information)).show();
            return;
        }

        if (!TextUtil.checkEmailFormat(email)) {
            MyToast.make(getString(R.string.error_message_email_invalid)).show();
            return;
        }

        if (!checkConfirmedPassword(confirmedPassword)) {
            return;
        }

        progressDialog.show(getSupportFragmentManager(), PROGRESS_DIALOG_TAG);
        registerFragment.createUser(email, password);
    }

    private boolean checkConfirmedPassword(String confirmedPassword) {
        String password = passwordEditText.getText().toString();

        if (!password.equals(confirmedPassword)) {
            confirmPasswordEditText.setError(getString(R.string.error_message_confirm_password));
            return false;
        }

        return true;
    }

    private void goToAuthenticationActivity() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        Intent authenticationIntent = Util.intentClearActivity(getApplicationContext(), AuthenticationActivity.class);
        authenticationIntent.putExtra(AuthenticationFragment.PROVIDER, TextUtil.EMAIL_PROVIDER);
        authenticationIntent.putExtra(AuthenticationFragment.EMAIL, email);
        authenticationIntent.putExtra(AuthenticationFragment.PASSWORD, password);

        startActivity(authenticationIntent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onRegisterSuccess() {
        progressDialog.dismiss();
        goToAuthenticationActivity();
    }

    @Override
    public void onRegisterFailed() {
        progressDialog.dismiss();
    }
}
