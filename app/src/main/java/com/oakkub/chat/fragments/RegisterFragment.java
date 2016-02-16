package com.oakkub.chat.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.oakkub.chat.R;
import com.oakkub.chat.managers.AppController;
import com.oakkub.chat.utils.FirebaseUtil;
import com.oakkub.chat.views.widgets.MyToast;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * A placeholder fragment containing a simple view.
 */
public class RegisterFragment extends Fragment implements Firebase.ResultHandler {

    private static final String TAG = RegisterFragment.class.getSimpleName();

    @Inject
    @Named(FirebaseUtil.NAMED_ROOT)
    Firebase firebase;

    private boolean isSuccess;
    private boolean isFailed;

    private OnRegisterListener registerListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        registerListener = (OnRegisterListener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.getComponent(getActivity()).inject(this);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (isSuccess) {
            registerListener.onRegisterSuccess();
            isSuccess = false;
        }

        if (isFailed) {
            registerListener.onRegisterFailed();
            isFailed = false;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        registerListener = null;
    }

    public void createUser(String email, String password) {
        firebase.createUser(email, password, this);
    }

    @Override
    public void onSuccess() {
        if (registerListener != null) {
            registerListener.onRegisterSuccess();
        } else {
            isSuccess = true;
        }
    }

    @Override
    public void onError(FirebaseError firebaseError) {
        Log.e(TAG, firebaseError.getMessage());
        handleFirebaseError(firebaseError);

        if (registerListener != null) {
            registerListener.onRegisterFailed();
        } else {
            isFailed = true;
        }
    }

    private void handleFirebaseError(FirebaseError firebaseError) {

        Log.e(TAG, String.valueOf(firebaseError.getCode()));

        switch (firebaseError.getCode()) {

            case FirebaseError.EMAIL_TAKEN:
                MyToast.make(getString(R.string.error_message_email_taken)).show();
                break;

            case FirebaseError.INVALID_EMAIL:
                MyToast.make(getString(R.string.error_message_email_invalid)).show();
                break;

            case FirebaseError.NETWORK_ERROR:
                MyToast.make(getString(R.string.error_message_network)).show();
                break;

        }

    }

    public interface OnRegisterListener {
        void onRegisterSuccess();

        void onRegisterFailed();
    }

}
