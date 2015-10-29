package com.oakkub.chat.fragments;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.oakkub.chat.R;
import com.oakkub.chat.activities.AuthenticationActivity;
import com.oakkub.chat.activities.MainActivity;
import com.oakkub.chat.models.UserInfo;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.utils.TextUtil;
import com.oakkub.chat.utils.Util;
import com.oakkub.chat.views.dialogs.ProgressDialogFragment;

import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class RegisterActivityFragment extends Fragment
        implements Firebase.ResultHandler,
                   View.OnClickListener,
                   TextWatcher,
                   EditText.OnEditorActionListener {

    private static final String TAG = RegisterActivityFragment.class.getSimpleName();
    private static final String PROGRESS_DIALOG_TAG = "registerProgressDialog";

    @Bind(R.id.email_register_edittext)
    EditText emailEditText;
    @Bind(R.id.password_register_edittext)
    EditText passwordEditText;
    @Bind(R.id.confirm_password_register_edittext)
    EditText confirmPasswordEditText;
    @Bind(R.id.register_button)
    Button registerButton;

    private Context context;
    private Firebase firebase;

    private ProgressDialogFragment progressDialog;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        firebase = new Firebase(FirebaseUtil.FIREBASE_USER_URL);
        progressDialog = ProgressDialogFragment.newInstance(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_register, container, false);

        ButterKnife.bind(this, rootView);

        confirmPasswordEditText.addTextChangedListener(this);
        confirmPasswordEditText.setOnEditorActionListener(this);
        registerButton.setOnClickListener(this);

        return rootView;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

        switch (actionId) {

            case EditorInfo.IME_ACTION_DONE:

                Util.hideSoftKeyboard((AppCompatActivity) getActivity());
                createUser();

                return true;

        }

        return false;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.register_button:

                createUser();

                break;

        }

    }

    private void createUser() {

        Log.e(TAG, "createUser");

        progressDialog.show(getActivity().getSupportFragmentManager(), PROGRESS_DIALOG_TAG);

        final String email = emailEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString().trim();

        if (email.length() == 0 || password.length() == 0) {

            progressDialog.dismiss();
            Util.showSnackBar(getView(), getString(R.string.error_message_fill_out_information));
            return;
        }

        if (!TextUtil.checkEmailFormat(email)) {

            progressDialog.dismiss();
            Util.showSnackBar(getView(), getString(R.string.error_message_email_invalid));
            return;
        }

        firebase.createUser(email, password, this);

        Log.e(TAG, "userCreated");
    }

    @Override
    public void onSuccess() {

        final String email = emailEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString().trim();

        goToAuthenticationActivity(email, password);

    }

    @Override
    public void onError(FirebaseError firebaseError) {

        progressDialog.dismiss();

        Log.e(TAG, firebaseError.getMessage());

        handleFirebaseError(firebaseError);

    }

    private void handleFirebaseError(FirebaseError firebaseError) {

        Log.e(TAG, String.valueOf(firebaseError.getCode()));

        switch (firebaseError.getCode()) {

            case FirebaseError.EMAIL_TAKEN:

                Util.showSnackBar(getView(), getString(R.string.error_message_email_taken));

                break;

            case FirebaseError.INVALID_EMAIL:

                Util.showSnackBar(getView(), getString(R.string.error_message_email_invalid));

                break;

            case FirebaseError.NETWORK_ERROR:

                Util.showSnackBar(getView(), getString(R.string.error_message_network));

                break;

        }

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}
    @Override
    public void afterTextChanged(Editable editable) {

        Editable confirmPasswordEditTextText = confirmPasswordEditText.getText();

        if (confirmPasswordEditTextText.hashCode() == editable.hashCode()) {

            final String confirmPassword = confirmPasswordEditTextText.toString();
            final String password = passwordEditText.getText().toString();

            if (!password.equals(confirmPassword)) {

                confirmPasswordEditText.setError(getString(R.string.error_message_confirm_password));
            }
        }

    }

    private void goToAuthenticationActivity(String email, String password) {

        Intent authenticationIntent = Util.intentClearActivity(getActivity().getApplicationContext(), AuthenticationActivity.class);
        authenticationIntent.putExtra(AuthenticationActivityFragment.PROVIDER, TextUtil.EMAIL_PROVIDER);
        authenticationIntent.putExtra(AuthenticationActivityFragment.EMAIL, email);
        authenticationIntent.putExtra(AuthenticationActivityFragment.PASSWORD, password);

        startActivity(authenticationIntent);
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

}
